package ed.inf.adbs.minibase.evaluator;

import base.*;
import ed.inf.adbs.minibase.structures.Tuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JoinOperator extends Operator{
    private Operator left_child;
    private Operator right_child;
    private List<ComparisonAtom> comparisonAtomList;
    private BufferedReader right_br;
    private Tuple left_previous;
    private List<Integer> index1_lis, index2_lis;
    private Tuple new_tuple;
    private Set<Tuple> return_set;

    public JoinOperator(Operator left_child, Operator right_child, List<ComparisonAtom> comparisonAtomList){
        return_set = new HashSet<>();
        this.left_child = left_child;
        this.right_child = right_child;
        this.comparisonAtomList = comparisonAtomList;
        this.right_br = new BufferedReader(right_child.getBr());
        // initialize a new tuple using 'R&S' as name
        new_tuple = new Tuple(left_child.getRelation_atom().getName()+"&"+right_child.getRelation_atom().getName());
        // get terms in atom
        List<Term> left_terms = left_child.getRelation_atom().getTerms();
        List<Term> right_terms = right_child.getRelation_atom().getTerms();
        index1_lis = new ArrayList<>();
        index2_lis = new ArrayList<>();
        for (ComparisonAtom atom: comparisonAtomList){
            // assert all terms are variable
            // assert if one term in left, the other must in right!
            assert ((atom.getTerm1() instanceof Variable)&&(atom.getTerm2() instanceof Variable));{
                int index1 = left_terms.indexOf(atom.getTerm1());
                if (index1 == -1){ // term1 in right terms
                    index1_lis.add(left_terms.indexOf(atom.getTerm2()));
                    index2_lis.add(right_terms.indexOf(atom.getTerm1()));
                }
                else { // term1 in left terms
                    index1_lis.add(index1);
                    index2_lis.add(right_terms.indexOf(atom.getTerm2()));
                }
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


        Tuple left_next, right_next;
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
            for (int i = 0; i < index1_lis.size(); i++){
                if (!(left.getWrapInTuple(index1_lis.get(i)).equals(right.getWrapInTuple(index2_lis.get(i))))){
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
