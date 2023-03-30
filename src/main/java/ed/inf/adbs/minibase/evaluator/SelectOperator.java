package ed.inf.adbs.minibase.evaluator;

import base.*;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SelectOperator extends Operator {
    /**
     *                  child:  logic child
     *          relation_atom:  the relation to be selected
     *     compare_index_list:  column index of the variable needed to be compared
     *      compare_wrap_list:  a list of TypeWrapper for wrapping the scanned terms
     * comparisonOperatorList:  the comparison list of all the comparisons need to be computed
     *           return_empty:  if true, getNextTuple will always return null
     *                          (for early detected situation e.g. 'a'='b' will always be false)
     *       self_compare_map:  map of self comparison like R(x,x) but it seems will not happen
     *
     * */
    private final Operator child;
    private final RelationalAtom relation_atom;
    private final List<Integer> compare_index_list;
    private final List<TypeWrapper> compare_wrap_list;
    private final List<ComparisonOperator> comparisonOperatorList;
    private boolean return_empty;
    private final HashMap<List<Integer>, ComparisonOperator> self_compare_map;

    public SelectOperator(Operator child, RelationalAtom relation_atom, List<ComparisonAtom> compare_list) {
        /**
         * constructor. find the columns needed to be compared
         * @params:
         *          child:  logic child of the operation tree
         *  relation_atom:  RelationAtom, the relation to be selected
         *   compare_list:  a list of comparisons conditions
         *
         * */
        return_empty = false;
        this.child = child;
        this.relation_atom = relation_atom;
        remaining_compare_list = new ArrayList<>(compare_list);
        compare_index_list = new ArrayList<>();
        compare_wrap_list = new ArrayList<>();
        comparisonOperatorList = new ArrayList<>();
        self_compare_map = new HashMap<>();
        List<Term> terms_in_atom = this.relation_atom.getTerms();
        // detect implicit comparison R(x, y, 4)
        for (int i = 0; i < terms_in_atom.size(); i++){
            Term term = terms_in_atom.get(i);
            if (term instanceof Constant){
                compare_index_list.add(i);
                comparisonOperatorList.add(ComparisonOperator.EQ);
                compare_wrap_list.add(new TypeWrapper(term));
            }
        }
        // for loop the comparisons to detect whether we should apply other comparisons
        for (ComparisonAtom comparison_atom : compare_list){
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
                    remaining_compare_list.remove(comparison_atom);
                    TypeWrapper wrap1 = new TypeWrapper(term1);
                    TypeWrapper wrap2 = new TypeWrapper(term2);
                    if(!this.Comparing(wrap1, wrap2, op)) return_empty = true;

                }
                if((term1 instanceof Constant) && (term2 instanceof Variable)){
                    TypeWrapper wrap1 = new TypeWrapper(term1);
                    Variable var = new Variable(((Variable) term2).getName());
                    // find the index of the variable in the tuple
                    int index = terms_in_atom.indexOf(var);
                    if (index != -1) {
                        compare_index_list.add(index);
                        compare_wrap_list.add(wrap1);
                        comparisonOperatorList.add(op);
                        // if one comparison has been calculated, then we can delete it from the
                        // comparison list passing to the next stage (like another select, join, projection)
                        // which can reduce some computing
                        remaining_compare_list.remove(comparison_atom);
                    }

                }
                if((term1 instanceof Variable) && (term2 instanceof Constant)){
                    TypeWrapper wrap2 = new TypeWrapper(term2);
                    Variable var = new Variable(((Variable) term1).getName());
                    // find the index of the variable in the tuple
                    int index = terms_in_atom.indexOf(var);
                    if (index != -1) {
                        compare_wrap_list.add(wrap2);
                        compare_index_list.add(index);
                        comparisonOperatorList.add(op);
                        remaining_compare_list.remove(comparison_atom);
                    }
                }
                if((term1 instanceof Variable) && (term2 instanceof Variable)){
                    // only care those: both variables in atom relation
                    Variable var1 = new Variable(((Variable) term1).getName());
                    Variable var2 = new Variable(((Variable) term2).getName());
                    int index1 = terms_in_atom.indexOf(var1);
                    int index2 = terms_in_atom.indexOf(var2);
                    if ((index1 != -1) && (index2 != -1) && (index1 != index2)){
                        self_compare_map.put(new ArrayList<>(Arrays.asList(index1, index2)), op);
                        remaining_compare_list.remove(comparison_atom);
                    }
                }
            }
        }
        System.out.println("SelectionOperator for "+relation_atom+" with comparison "+compare_list);
        System.out.println("remaining_compare_list: "+remaining_compare_list);
        System.out.println("----------------------------------------------------------------------------------------");
    }

    @Override
    public void reset() throws IOException {
        this.child.reset();
    }

    @Override
    public Tuple getNextTuple() throws IOException {
        /**
         * if the child's nextTuple is not null, then judge whether it is valid according to comparisons
         * @return:
         *   null:  if return empty(e.g. in the initialization detecting 'adbs' = 'ppls')
         *  Tuple:  a new tuple selected according to the child's nextTuple and the comparisons
         * */
        if (return_empty) return null;
        Tuple new_tuple;
        while ((new_tuple = this.child.getNextTuple()) != null) {
            if (validMatch(new_tuple)) return new_tuple;
        }
        return null;
    }


    public RelationalAtom getRelation_atom(){return this.relation_atom;}

    public boolean validMatch(Tuple tuple) {
        /**
         * Test whether the child's next tuple is valid under the comparisons
         * @return:
         *   true:  if valid
         *  false:  if not valid, the tuple not match all the comparisons
         * @params:
         *  tuple:  tuple to be tested
         * */
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