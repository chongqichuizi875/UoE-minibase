package ed.inf.adbs.minibase.evaluator;

import base.*;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SumOperator extends Operator{
    private Operator child;
    private SumAggregate aggregate;
    private List<Tuple> dump_list;
    private List<Term> relation_body;
    private List<Variable> head_variables;
    private HashMap<List<TypeWrapper>, Integer> group_map;
    private Integer mul;
    List<Integer> group_column_list, agg_column_list;
    private List<List<TypeWrapper>> retrive_list; // store the key of group_map for getNextTuple
    public SumOperator(Operator child, Head head) throws IOException {
        aggregate = head.getSumAggregate();
        this.child = child;
        head_variables = head.getVariables();
        dump_list = new ArrayList<>(child.dump()); // all the tuple by child
        relation_body = getRelation_atom().getTerms(); // terms in relation (compare with head variables)
        retrive_list = new ArrayList<>();
        group_map = new HashMap<>();
        group_column_list = new ArrayList<>();
        agg_column_list = new ArrayList<>();
        // stats the group by terms
        for (Variable head_var: head_variables){
            // [1, 3] means we need to group by the 1st and 3rd columns
            group_column_list.add(relation_body.indexOf(head_var));
        }
        // parse the aggregate
        List<Term> agg_terms = aggregate.getProductTerms();
        mul = 1; // use a mul to represent the results of all constant multiply
        for (Term term: agg_terms){
            if (term instanceof IntegerConstant) {
                mul *= ((IntegerConstant) term).getValue();
            }
            else { // find the column index corresponding to the term
                // assuming var in head must be in relation_body, its child's or sql writer's responsibility
                // to examine the format
                // e.g. Q(x,y,SUM(z)):- R(x,z),S(u,v) should not come to this step
                agg_column_list.add(relation_body.indexOf(term));
            }
        }

        for (Tuple tuple: dump_list){ // group by columns
            List<TypeWrapper> group_mark_list = new ArrayList<>();
            for (int index:group_column_list){ // Q(x,y,SUM(z*z)) -> save the list of [x, y]
                group_mark_list.add(tuple.getWrapInTuple(index));
            }
            int mul = 1; // a local mul for the corresponding variable mul value
            for (int index:agg_column_list){ // Q(x,y,SUM(z)) -> save the list of [z*z]
                mul *= (Integer) tuple.getWrapInTuple(index).getValue();
            }
            // then use group_mark_list as hash key
            if(group_map.containsKey(group_mark_list)){
                group_map.put(group_mark_list, group_map.get(group_mark_list) + this.mul*mul);
            }
            else {
                group_map.put(group_mark_list, this.mul*mul);
                retrive_list.add(group_mark_list); // group_map.size() == retrive_list.size()
            }
        }

    }

    @Override
    public void reset() throws IOException {
        child.reset();
    }

    @Override
    public Tuple getNextTuple(){
        if (!group_map.isEmpty()) {
            List<TypeWrapper> key_group = retrive_list.remove(0);
            Tuple new_tuple = new Tuple(getRelation_atom().getName());
            new_tuple.add(key_group);
            new_tuple.tupleProjection(new TypeWrapper((int) group_map.remove(key_group)));
            return new_tuple;
        }
        return null;
    }

    @Override
    public RelationalAtom getRelation_atom() {
        return child.getRelation_atom();
    }

    @Override
    public BufferedReader getBr() {
        return null;
    }
}
