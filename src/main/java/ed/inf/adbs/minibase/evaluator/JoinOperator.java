package ed.inf.adbs.minibase.evaluator;

import base.*;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class JoinOperator extends Operator{
    private final Operator left_child;
    private final Operator right_child;
    private Tuple left_previous;
    private final Set<Tuple> return_set;
    private final HashMap<List<Integer>, ComparisonOperator> compare_map;
    private final String relation_name;

    public JoinOperator(Operator left_child, Operator right_child, List<ComparisonAtom> comparisonAtomList){
        return_set = new HashSet<>();
        compare_map = new HashMap<>();
        this.left_child = left_child;
        this.right_child = right_child;


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

        for (ComparisonAtom atom: comparisonAtomList){
            // assert all terms are variable
            if ((atom.getTerm1() instanceof Variable)&&(atom.getTerm2() instanceof Variable)){
                int index1left = left_terms.indexOf(atom.getTerm1());
                int index1right = right_terms.indexOf(atom.getTerm1());
                int index2left = left_terms.indexOf(atom.getTerm2());
                int index2right = right_terms.indexOf(atom.getTerm2());
                if ((index1left != -1)&&(index2right != -1)){ // term1 can be found in left child and term2 can be found in right child
                    compare_map.put(new ArrayList<>(Arrays.asList(index1left, index2right)), atom.getOp());
                }
                else if((index1right != -1)&&(index2left != -1)){ // term1 can be found in right child and term2 can be found in left child
                    compare_map.put(new ArrayList<>(Arrays.asList(index2left, index1right)), atom.getOp());
                }
                // comparisons like R(x,y,z), S(u,v,w), x=y / x<z / x=c -> in a word: the two terms can not match the two relation separately

            }

        }
    }

    @Override
    public RelationalAtom getRelation_atom() {
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
        Tuple left_next, new_tuple;
        if (left_previous!=null){
            new_tuple = getNextTupleOnlyMovingRight(left_previous);
            if (new_tuple != null) {
                return new_tuple;
            }
            else right_child.reset();
        }

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
