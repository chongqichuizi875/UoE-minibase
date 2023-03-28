package ed.inf.adbs.minibase.evaluator;

import base.RelationalAtom;
import base.SumAggregate;
import ed.inf.adbs.minibase.structures.Tuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class SumOperator extends Operator{
    private Operator child;
    private SumAggregate aggregate;
    private List<Tuple> dump_list;
    public SumOperator(Operator child, SumAggregate aggregate){
        this.aggregate = aggregate;
        this.child = child;

    }

    @Override
    public void reset() throws IOException {
        child.reset();
    }

    @Override
    public Tuple getNextTuple() throws IOException {
        List<Tuple> dump_list = child.dump();

        return null;
    }

    @Override
    public RelationalAtom getRelation_atom() {
        return null;
    }

    @Override
    public BufferedReader getBr() {
        return null;
    }
}
