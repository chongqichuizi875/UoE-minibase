package ed.inf.adbs.minibase.evaluator;

import base.ComparisonAtom;
import base.ComparisonOperator;
import base.RelationalAtom;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class Operator {
    /**
     * base abstract class for all the operators
     *
     * remaining_compare_list:  record the comparison atoms can not be solved in current stage, and pass it to
     *                          the next operator.
     * */
    protected List<ComparisonAtom> remaining_compare_list;
    public abstract void reset() throws IOException;
    public abstract Tuple getNextTuple() throws IOException;
    public List<Tuple> dump() throws IOException {
        /**
         * dump to the output
         * @return
         *  tuple_list:  the list of all possible output
         * */
        List<Tuple> tuple_list = new ArrayList<>();
        Tuple next_tuple;
        while ((next_tuple = getNextTuple()) != null) {
            tuple_list.add(new Tuple(next_tuple));
        }
        reset(); // dont forget to reset for future use!
        return tuple_list;
    }
    public List<ComparisonAtom> getRemaining_compare_list(){return remaining_compare_list;}

    public abstract RelationalAtom getRelation_atom();
    public abstract BufferedReader getBr();

    public boolean Comparing(TypeWrapper wrap1, TypeWrapper wrap2, ComparisonOperator op) {
        /**
         * for comparing the TypeWrappers
         * @return
         *  return the boolean compare results.
         * @params
         * wrap1:  one TypeWrapper instance
         * wrap2:  another TypeWrapper instance
         *    op:  comparison operation
         * */
        switch (op.toString()) {
            case "=":
                return wrap1.equals(wrap2);
            case "!=":
                return !wrap1.equals(wrap2);
            case ">":
                return wrap1.isGreaterThan(wrap2);
            case "<":
                return wrap1.isLessThan(wrap2);
            case ">=":
                return wrap1.isGreaterAndEqual(wrap2);
            case "<=":
                return wrap1.isLessAndEqual(wrap2);
        }
        System.out.println("Invalid comparison symbol! ");
        return false;
    }

}
