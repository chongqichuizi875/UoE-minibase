package ed.inf.adbs.minibase.evaluator;

import ed.inf.adbs.minibase.structures.Tuple;

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
        return tuple_list;
    };

}
