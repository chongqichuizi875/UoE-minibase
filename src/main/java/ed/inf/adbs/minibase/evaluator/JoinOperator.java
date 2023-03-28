package ed.inf.adbs.minibase.evaluator;

import base.*;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class JoinOperator extends Operator{
    private Operator left_child;
    private Operator right_child;
    private List<ComparisonAtom> comparisonAtomList;
    private BufferedReader right_br;
    private Tuple left_previous;
    private List<Integer> index_left_lis, index_right_lis;
    private List<ComparisonOperator> comparisonOperatorList;
    private Tuple new_tuple;
    private Set<Tuple> return_set;
    private HashMap<List<Integer>, ComparisonOperator> compare_map;

    public JoinOperator(Operator left_child, Operator right_child, List<ComparisonAtom> comparisonAtomList){
        return_set = new HashSet<>();
        compare_map = new HashMap<>();
        this.left_child = left_child;
        this.right_child = right_child;
        this.comparisonAtomList = comparisonAtomList;
        this.right_br = new BufferedReader(right_child.getBr());
        // initialize a new tuple using 'R&S' as name
        new_tuple = new Tuple(left_child.getRelation_atom().getName()+"&"+right_child.getRelation_atom().getName());
        // get terms in atom
        List<Term> left_terms = left_child.getRelation_atom().getTerms();
        List<Term> right_terms = right_child.getRelation_atom().getTerms();
        index_left_lis = new ArrayList<>();
        index_right_lis = new ArrayList<>();
        comparisonOperatorList = new ArrayList<>();
        // detect implicit equi-join!!
        for (int i = 0; i < left_terms.size(); i++){
            if (left_terms.get(i) instanceof Variable){
                int index_in_right = right_terms.indexOf(left_terms.get(i));
                if (index_in_right!=-1){ // find implicit equi-join
                    compare_map.put(new ArrayList<>(Arrays.asList(i, index_in_right)), ComparisonOperator.EQ);
                    index_left_lis.add(i);
                    index_right_lis.add(index_in_right);
                    comparisonOperatorList.add(ComparisonOperator.EQ);
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
                    index_left_lis.add(index1left);
                    index_right_lis.add(index2right);
                    compare_map.put(new ArrayList<>(Arrays.asList(index1left, index2right)), atom.getOp());
                }
                else if((index1right != -1)&&(index2left != -1)){ // term1 can be found in right child and term2 can be found in left child
                    index_left_lis.add(index2left);
                    index_right_lis.add(index1right);
                    compare_map.put(new ArrayList<>(Arrays.asList(index2left, index1right)), atom.getOp());
                }
                else { // comparisons like R(x,y,z), S(u,v,w), x=y / x<z / x=c -> in a word: the two terms can not match the two relation separately
                    continue;
                }
                comparisonOperatorList.add(atom.getOp());
            }
        }
    }

    @Override
    public RelationalAtom getRelation_atom() {
        return left_child.getRelation_atom();
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


        Tuple left_next;
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
                TypeWrapper leftwrap = left.getWrapInTuple(index_list.get(0));
                TypeWrapper rightwrap = right.getWrapInTuple(index_list.get(1));
                ComparisonOperator op = compare_map.get(index_list);
                if (!(Comparing(leftwrap, rightwrap, op))){
                    equal = false;
                    break;
                }
            }
//            for (int i = 0; i < index_left_lis.size(); i++){
//                TypeWrapper leftwrap = left.getWrapInTuple(index_left_lis.get(i));
//                TypeWrapper rightwrap = right.getWrapInTuple(index_right_lis.get(i));
//                ComparisonOperator op = comparisonOperatorList.get(i);
//                if (!(Comparing(leftwrap, rightwrap, op))){
//                    equal = false;
//                    break;
//                }
//            }
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
