package ed.inf.adbs.minibase.evaluator;

import base.ComparisonOperator;
import base.RelationalAtom;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class Operator {
    public abstract void reset() throws IOException;
    public abstract Tuple getNextTuple() throws IOException;
    public List<Tuple> dump() throws IOException {
        List<Tuple> tuple_list = new ArrayList<>();
        Tuple next_tuple;
        while ((next_tuple = getNextTuple()) != null) {
            tuple_list.add(new Tuple(next_tuple));
        }
        reset();
        return tuple_list;
    };

    public abstract RelationalAtom getRelation_atom();
    public abstract BufferedReader getBr();

    public boolean Comparing(TypeWrapper wrap1, TypeWrapper wrap2, ComparisonOperator op) {
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
