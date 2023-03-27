package ed.inf.adbs.minibase.evaluator;

import base.Head;
import base.RelationalAtom;
import base.Term;
import base.Variable;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;

import java.io.IOException;
import java.util.List;

public class ProjectOperator extends Operator{
    private Operator child;
    private List<Tuple> buffer;
    private List<Variable> projection_list;
    private RelationalAtom relation_atom;

    public ProjectOperator(Operator child, Head head){
        this.child = child;
        this.projection_list = head.getVariables();
        this.relation_atom = child.getRelation_atom();
    }


    @Override
    public void reset() throws IOException {
        this.child.reset();
    }

    @Override
    public Tuple getNextTuple() throws IOException {
        Tuple next;
        // initialize a new tuple having same name with the child
        Tuple new_tuple = new Tuple(this.relation_atom.getName());
        List<Term> terms = this.relation_atom.getTerms();

        while ((next = this.child.getNextTuple()) != null) {
            for (Variable head_var: this.projection_list){ // variables in head
                for (int i = 0; i < terms.size(); i++){ // variables in relational atom
                    if (head_var.equals(terms.get(i))){
                        // get the ith item in Tuple
                        new_tuple.tupleProjection(next.getWrapInTuple(i));
                    }
                }
            }
            if (new_tuple.getTuple().size() != 0) return new_tuple;

        }
        return null;
    }
}
