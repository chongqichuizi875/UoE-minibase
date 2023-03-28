package ed.inf.adbs.minibase.evaluator;

import base.*;
import com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class SelectOperator extends Operator {
    private Operator child;
    private RelationalAtom relation_atom;
    private List<ComparisonAtom> compare_list;

    public SelectOperator(Operator child, RelationalAtom relation_atom, List<ComparisonAtom> compare_list) {
        this.child = child;
        this.relation_atom = relation_atom;
        this.compare_list = compare_list;
    }

    @Override
    public void reset() throws IOException {
        this.child.reset();
    }

    @Override
    public Tuple getNextTuple() throws IOException {
        Tuple next;
        while ((next = this.child.getNextTuple()) != null) {
            if (validMatch(next, this.compare_list)) return next;
        }
        return null;
    }

    public RelationalAtom getRelation_atom(){return this.relation_atom;}

    public boolean validMatch(Tuple tuple, List<ComparisonAtom> compare_list) {
        // duplicate the tuple for other operation
        Tuple dp_tuple = new Tuple(tuple);
        // check query like R(x,y,5)
        List<Term> terms_in_atom = this.relation_atom.getTerms();
        for (int i = 0; i < terms_in_atom.size(); i++){
            if (terms_in_atom.get(i) instanceof Constant){
                TypeWrapper wrap_in_tup = dp_tuple.getWrapInTuple(i);
                TypeWrapper wrap_term = new TypeWrapper(terms_in_atom.get(i));
                if (!(wrap_term.equals(wrap_in_tup))) return false;
            }
        }
        for (ComparisonAtom comparison_atom : compare_list) {
            // if exists one comparison not satisfied, then return false
            // get the 2 terms and the opeartion
            Term term1 = comparison_atom.getTerm1();
            Term term2 = comparison_atom.getTerm2();
            ComparisonOperator op = comparison_atom.getOp();
            if (!((term1 instanceof Constant || term1 instanceof Variable)
                    && (term2 instanceof Constant || term2 instanceof Variable))) {
                System.out.println("Invalid term format! \n terms must be Constant or Variable");
                return false;
            }

            // term1 constant
            if (term1 instanceof Constant) {
                // term2 constant
                if (term2 instanceof Constant) {
                    TypeWrapper wrap1 = new TypeWrapper(term1);
                    TypeWrapper wrap2 = new TypeWrapper(term2);
                    if(!this.Comparing(wrap1, wrap2, op)) return false;
                }
                else {
                    // term2 variable
                    Variable var = new Variable(((Variable) term2).getName());
                    TypeWrapper wrap1 = new TypeWrapper(term1);
                    // find the index of the variable in the tuple

                    int index = terms_in_atom.indexOf(var);
                    if (index != -1) {
                        // get the wrap in tuple using the index
                        TypeWrapper wrap2 = dp_tuple.getWrapInTuple(index);
                        if(!this.Comparing(wrap1, wrap2, op)) return false;
                    } // variable does not in the relation
                }
            }
            // term1 variable
            else {
                // term2 constant
                if (term2 instanceof Constant) {
                    Variable var = new Variable(((Variable) term1).getName());
                    TypeWrapper wrap2 = new TypeWrapper(term2);
                    // find the index of the variable in the tuple
                    int index = terms_in_atom.indexOf(var);
                    if (index != -1) {
                        // get the wrap in tuple using the index
                        TypeWrapper wrap1 = dp_tuple.getWrapInTuple(index);
                        if(!this.Comparing(wrap1, wrap2, op)) return false;
                    } // variable does not in the relation
                }
                else {
                    // both variables
                    Variable var1 = new Variable(((Variable) term1).getName());
                    Variable var2 = new Variable(((Variable) term2).getName());
                    int index1 = terms_in_atom.indexOf(var1);
                    int index2 = terms_in_atom.indexOf(var2);
                    if ((index1!=-1)&&(index2!=-1)){ // both vars in the relation
                        // get the wrap in tuple using the index
                        TypeWrapper wrap1 = dp_tuple.getWrapInTuple(index1);
                        TypeWrapper wrap2 = dp_tuple.getWrapInTuple(index2);
                        if(!this.Comparing(wrap1, wrap2, op)) return false;
                    }
                    else System.out.println("either term1 or term2 not found in the relation");
                }
            }

        }
        return true;
    }


    @Override
    public BufferedReader getBr(){return child.getBr();}
}