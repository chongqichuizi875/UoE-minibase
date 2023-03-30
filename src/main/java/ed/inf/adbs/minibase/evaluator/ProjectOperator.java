package ed.inf.adbs.minibase.evaluator;

import base.Head;
import base.RelationalAtom;
import base.Term;
import base.Variable;
import ed.inf.adbs.minibase.structures.Tuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectOperator extends Operator{
    /**
     *
     *           child:  operator child
     * projection_list:  variables need to be projected
     *   relation_atom:  relation atom containing tuples
     *     out_put_set:  a set to prevent duplication output
     * */
    private final Operator child;
    private final List<Variable> projection_list;
    private final RelationalAtom relation_atom;
    private final Set<Tuple> out_put_set;

    public ProjectOperator(Operator child, Head head){
        this.child = child;
        projection_list = head.getVariables();
        relation_atom = child.getRelation_atom();
        out_put_set = new HashSet<>();
    }


    @Override
    public void reset() throws IOException {
        child.reset();
    }

    @Override
    public Tuple getNextTuple() throws IOException {
        /**
         * according to the variables of head to decide which columns to
         * output. And projection may encounter duplication, so use a set
         * to prevent it.
         * */
        Tuple next;
        // initialize a new tuple having same name with the child
        List<Term> terms = relation_atom.getTerms();
        while ((next = child.getNextTuple()) != null) {
            Tuple new_tuple = new Tuple(getRelation_atom().getName());
            for (Variable head_var: projection_list){ // variables in head
                int index = terms.indexOf(head_var); // assume index != -1. its child's business!
                new_tuple.tupleProjection(next.getWrapInTuple(index));
            }
            if ((new_tuple.getTuple().size() != 0) && (!out_put_set.contains(new_tuple))) {
                out_put_set.add(new_tuple);
                return new_tuple;
            }

        }
        return null;
    }
    @Override
    public BufferedReader getBr(){return child.getBr();}

    @Override
    public RelationalAtom getRelation_atom() {
        return relation_atom;
    }
}
