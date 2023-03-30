package ed.inf.adbs.minibase.evaluator;

import base.*;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;
import sun.jvm.hotspot.HSDB;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class JoinOperator extends Operator{
    /**
     *    left_child:  left child
     *   right_child:  right child
     * left_previous:  the left child's tuple of last time
     *    return_set:  a set to avoid duplication
     *   compare_map:  map from one constant index to a ComparisonOperator (to detect implicit comparisons)
     * relation_name:  atom name
     * */
    private final Operator left_child;
    private final Operator right_child;
    private Tuple left_previous;
    private final Set<Tuple> return_set;
    private final HashMap<List<Integer>, ComparisonOperator> compare_map;
    private final String relation_name;

    public JoinOperator(Operator left_child, Operator right_child){
        /**
         * constructor.
         * @params:
         *  left_child:  left operator child
         * right_child:  right operator child
         * */
        return_set = new HashSet<>();
        compare_map = new HashMap<>();
        this.left_child = left_child;
        this.right_child = right_child;
        // calculate the union comparisonAtomList of left and right child
        List<ComparisonAtom> union_compare_list = left_child.getRemaining_compare_list().parallelStream().collect(Collectors.toList());
        List<ComparisonAtom> union_compare_list2 = right_child.getRemaining_compare_list().parallelStream().collect(Collectors.toList());
        union_compare_list.addAll(union_compare_list2); // get the comparisons list
        List<ComparisonAtom> comparisonAtomList = union_compare_list.stream().distinct().collect(Collectors.toList());
        remaining_compare_list = new ArrayList<>(comparisonAtomList);


        // initialize a new tuple using 'R&S' as name
        Tuple new_tuple = new Tuple(left_child.getRelation_atom().getName() + "&" + right_child.getRelation_atom().getName());
        relation_name = new_tuple.getName();
        // get terms in atom
        List<Term> left_terms = left_child.getRelation_atom().getTerms();
        List<Term> right_terms = right_child.getRelation_atom().getTerms();

        // detect implicit equality-join!!
        for (int i = 0; i < left_terms.size(); i++){
            if (left_terms.get(i) instanceof Variable){
                int index_in_right = right_terms.indexOf(left_terms.get(i));
                if (index_in_right!=-1){ // find implicit equality-join
                    compare_map.put(new ArrayList<>(Arrays.asList(i, index_in_right)), ComparisonOperator.EQ);
                }
            }
        }
        HashMap<ComparisonOperator, ComparisonOperator> inverted_map = new HashMap<>();
        inverted_map.put(ComparisonOperator.EQ, ComparisonOperator.EQ);
        inverted_map.put(ComparisonOperator.NEQ, ComparisonOperator.NEQ);
        inverted_map.put(ComparisonOperator.GT, ComparisonOperator.LT);
        inverted_map.put(ComparisonOperator.LT, ComparisonOperator.GT);
        inverted_map.put(ComparisonOperator.LEQ, ComparisonOperator.GEQ);
        inverted_map.put(ComparisonOperator.GEQ, ComparisonOperator.LEQ);
        for (ComparisonAtom atom: comparisonAtomList){
            // assert all terms are variable
            if ((atom.getTerm1() instanceof Variable)&&(atom.getTerm2() instanceof Variable)){
                int index1left = left_terms.indexOf(atom.getTerm1());
                int index1right = right_terms.indexOf(atom.getTerm1());
                int index2left = left_terms.indexOf(atom.getTerm2());
                int index2right = right_terms.indexOf(atom.getTerm2());
                if ((index1left != -1)&&(index2right != -1)){ // term1 can be found in left child and term2 can be found in right child
                    compare_map.put(new ArrayList<>(Arrays.asList(index1left, index2right)), atom.getOp());
                    remaining_compare_list.remove(atom);
                }
                else if((index1right != -1)&&(index2left != -1)){ // term1 can be found in right child and term2 can be found in left child
                    compare_map.put(new ArrayList<>(Arrays.asList(index2left, index1right)), inverted_map.get(atom.getOp()));
                    remaining_compare_list.remove(atom);
                }
                // comparisons like R(x,y,z), S(u,v,w), x=y / x<z / x=c -> in a word: the two terms can not match the two relation separately
            }
            else {
                // there might x != 'ppls' or other comparisons with one variable and another constant
                // by selection operator, no comparisons with 2 constants will be left to this stage

            }
        }
        System.out.println("JoinOperator for "+left_child.getRelation_atom()+" and "+right_child.getRelation_atom()+
                " with comparison "+ comparisonAtomList);
        System.out.println("remaining_compare_list: "+remaining_compare_list);
        System.out.println("----------------------------------------------------------------------------------------");
    }

    @Override
    public RelationalAtom getRelation_atom() {
        /**
         * get the union relation atom of left and right child
         * */
        List<Term> terms = new ArrayList<>(left_child.getRelation_atom().getTerms());
        terms.addAll(right_child.getRelation_atom().getTerms());
        return new RelationalAtom(relation_name, terms);
    }

    @Override
    public BufferedReader getBr() {
        return left_child.getBr();
    }

    @Override
    public void reset() throws IOException {
        left_child.reset();
    }

    @Override
    public Tuple getNextTuple() throws IOException {
        /**
         * main join logic
         * if right page not go to end, then if the 8th line in page R match the 7th line in page S
         * then after return the outer while will go to line 9 in page R. So we can not know whether
         * line 8 in page R can match line > 7 in page S.
         *
         * */
        Tuple left_next, new_tuple;
        // SO we need to make sure the right child has moved to end
        if (left_previous!=null){
            new_tuple = getNextTupleOnlyMovingRight(left_previous);
            if (new_tuple != null) {
                return new_tuple;
            }
            else right_child.reset();
        }
        // when the right child goes over all page and reset, we should perform
        // left line+1, so its necessary to maintain a left previous Tuple
        while ((left_next = left_child.getNextTuple())!=null){ // left child next
            right_child.reset();
            new_tuple = getNextTupleOnlyMovingRight(left_next);
            if (new_tuple != null) {
                left_previous = left_next;
                return new_tuple;
            }
        }
        return null;
    }
    public Tuple getNextTupleOnlyMovingRight(Tuple left) throws IOException {
        /**
         * fixed the buffer reader of the left child, only move the right child forward.
         * */
        Tuple right;
        Tuple new_tuple = new Tuple(left_child.getRelation_atom().getName()+"&"+right_child.getRelation_atom().getName());
        while ((right = right_child.getNextTuple())!=null){ // right child next
            boolean equal = true;
            for (List<Integer> index_list: compare_map.keySet()){
                TypeWrapper left_wrap = left.getWrapInTuple(index_list.get(0));
                TypeWrapper right_wrap = right.getWrapInTuple(index_list.get(1));
                ComparisonOperator op = compare_map.get(index_list);
                if (!(Comparing(left_wrap, right_wrap, op))){
                    equal = false;
                    break;
                }
            }

            if (equal) {
                new_tuple.add(left.getTuple());
                new_tuple.add(right.getTuple());
                if (!return_set.contains(new_tuple)) {
                    return_set.add(new_tuple);
                    return new_tuple;
                }
                else new_tuple.removeAll();
            }
        }
        return null;
    }
}
