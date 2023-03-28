package ed.inf.adbs.minibase.evaluator;

import base.*;
import com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SelectOperator extends Operator {
    private Operator child;
    private RelationalAtom relation_atom;
    private List<ComparisonAtom> comparisonAtomList;
    private List<Term> terms_in_atom;
    private List<Integer> compare_index_list;
    private List<TypeWrapper> compare_wrap_list;
    private List<ComparisonOperator> comparisonOperatorList;
    private boolean return_empty;
    private HashMap<List<Integer>, ComparisonOperator> self_compare_map;
    private Tuple new_tuple;

    public SelectOperator(Operator child, RelationalAtom relation_atom, List<ComparisonAtom> compare_list) {
        return_empty = false;
        this.child = child;
        this.relation_atom = relation_atom;
        this.comparisonAtomList = compare_list;
        this.compare_index_list = new ArrayList<>();
        this.compare_wrap_list = new ArrayList<>();
        this.comparisonOperatorList = new ArrayList<>();
        this.self_compare_map = new HashMap<>();
        List<Term> terms_in_atom = this.relation_atom.getTerms();
        new_tuple = new Tuple(relation_atom.getName());
        // detect implicit comparison R(x, y, 4)
        for (int i = 0; i < terms_in_atom.size(); i++){
            Term term = terms_in_atom.get(i);
            if (term instanceof Constant){
                compare_index_list.add(i);
                comparisonOperatorList.add(ComparisonOperator.EQ);
                compare_wrap_list.add(new TypeWrapper(term));
            }
        }
        for (ComparisonAtom comparison_atom : comparisonAtomList){
            Term term1 = comparison_atom.getTerm1();
            Term term2 = comparison_atom.getTerm2();
            ComparisonOperator op = comparison_atom.getOp();
            if (!((term1 instanceof Constant || term1 instanceof Variable)
                    && (term2 instanceof Constant || term2 instanceof Variable))) {
                System.out.println("Invalid term format! \n terms must be Constant or Variable");
            }
            else {
                // both terms are variable or constant
                if ((term1 instanceof Constant) && (term2 instanceof Constant)){ // 2 constants
                    TypeWrapper wrap1 = new TypeWrapper(term1);
                    TypeWrapper wrap2 = new TypeWrapper(term2);
                    if(!this.Comparing(wrap1, wrap2, op)) return_empty = true;
                }
                if((term1 instanceof Constant) && (term2 instanceof Variable)){
                    TypeWrapper wrap1 = new TypeWrapper(term1);
                    compare_wrap_list.add(wrap1);
                    Variable var = new Variable(((Variable) term2).getName());
                    // find the index of the variable in the tuple
                    int index = terms_in_atom.indexOf(var);
                    if (index != -1) compare_index_list.add(index);
                    comparisonOperatorList.add(op);
                }
                if((term1 instanceof Variable) && (term2 instanceof Constant)){
                    TypeWrapper wrap2 = new TypeWrapper(term2);
                    compare_wrap_list.add(wrap2);
                    Variable var = new Variable(((Variable) term1).getName());
                    // find the index of the variable in the tuple
                    int index = terms_in_atom.indexOf(var);
                    if (index != -1) compare_index_list.add(index);
                    comparisonOperatorList.add(op);
                }
                if((term1 instanceof Variable) && (term2 instanceof Variable)){
                    // only care those: both variables in atom relation
                    Variable var1 = new Variable(((Variable) term1).getName());
                    Variable var2 = new Variable(((Variable) term2).getName());
                    int index1 = terms_in_atom.indexOf(var1);
                    int index2 = terms_in_atom.indexOf(var2);
                    if ((index1 != -1) && (index2 != -1) && (index1 != index2)){
                        self_compare_map.put(new ArrayList<>(Arrays.asList(index1, index2)), op);
                    }
                }
            }
        }
    }

    @Override
    public void reset() throws IOException {
        this.child.reset();
    }

    @Override
    public Tuple getNextTuple() throws IOException {
        if (return_empty) return null;
        Tuple new_tuple = new Tuple(getRelation_atom().getName());
        while ((new_tuple = this.child.getNextTuple()) != null) {
            if (validMatch(new_tuple)) return new_tuple;
        }
        return null;
    }

    public RelationalAtom getRelation_atom(){return this.relation_atom;}

    public boolean validMatch(Tuple tuple) {
        // duplicate in case of writing or changing
        Tuple new_tuple = new Tuple(tuple);
        // check first self compare map
        for (List<Integer> index_pair: self_compare_map.keySet()){
            ComparisonOperator op = self_compare_map.get(index_pair);
            TypeWrapper wrap1 = new_tuple.getWrapInTuple(index_pair.get(0));
            TypeWrapper wrap2 = new_tuple.getWrapInTuple(index_pair.get(1));
            if(!this.Comparing(wrap1, wrap2, op)) return false;
        }
        // then check the outer comparisons
        for (int i = 0; i < compare_index_list.size(); i++){
            TypeWrapper wrap1 = new_tuple.getWrapInTuple(compare_index_list.get(i));
            TypeWrapper wrap2 = compare_wrap_list.get(i);
            ComparisonOperator op = comparisonOperatorList.get(i);
            if(!this.Comparing(wrap1, wrap2, op)) return false;
        }
        return true;
    }


    @Override
    public BufferedReader getBr(){return child.getBr();}
}