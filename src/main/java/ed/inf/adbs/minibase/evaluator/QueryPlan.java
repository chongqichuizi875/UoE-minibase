package ed.inf.adbs.minibase.evaluator;

import base.*;
import ed.inf.adbs.minibase.CQMinimizer;
import ed.inf.adbs.minibase.structures.DatabaseCatalog;
import ed.inf.adbs.minibase.structures.Tuple;

import java.io.IOException;
import java.security.PublicKey;
import java.util.*;

public class QueryPlan {
    /**
     *    current_root:  the current root Operator, whose getNextValue() is the output of the whole query
     * databaseCatalog:  DatabaseCatalog singleton
     *        need_agg:  indicate whether head contains SUM aggregates
     *
     * */
    private Operator current_root;
    private final DatabaseCatalog databaseCatalog;
    private boolean need_agg;

    public QueryPlan(Query query) throws IOException {
        /**
         * constructor. process the query, get head, body(transform to List of Relational)
         * use default / optimized evaluator to generate the Operator tree
         * @params:
         *  query:  inputted query
         * */
        // parse the query from the input file
        databaseCatalog = DatabaseCatalog.getCatalog();
        System.out.println("Original query: "+ query);
        Head head = query.getHead();
        List<Atom> body = query.getBody();
        need_agg = head.getSumAggregate() != null; // need group-by agg
        // body type transform
        List<RelationalAtom> relation_body = new ArrayList<>(); // relation atoms
        List<ComparisonAtom> compare_body = new ArrayList<>(); // comparison atoms
        for (Atom i: body){ // transform Atom to either relation atom or comparison atom
            if (i instanceof RelationalAtom) {
                relation_body.add((RelationalAtom) i);
            }
            else if (i instanceof ComparisonAtom) compare_body.add((ComparisonAtom) i);
        }
        // the while sentence itself is the task1 CQ minimization. you can try it hah.
//        while (CQMinimizer.removeOne(head, relation_body));
        // evaluate the old query and generate a high Highly customised operator tree
        // there are often less(no more) joins(while join order remain, just remove the unnecessary relation)

        operatorTreeBuilder(head, relation_body, compare_body);
//        defaultOperatorBuilder(head, relation_body, compare_body); // the default evaluator, you can try both
    }
    public void defaultOperatorBuilder(Head head, List<RelationalAtom> relationalAtomList, List<ComparisonAtom> comparisonAtomList) throws IOException {
        /**
         *  default evaluator for the query, build a left deep operator tree
         * @params:
         *                head:  query head
         *  relationalAtomList:  list of relation atoms
         *  comparisonAtomList:  list of comparison atoms
         * */
        List<SelectOperator> selectOperatorList = new ArrayList<>();
        // assign each relation a scan-operator
        // then use the scan operator to initialize select operator
        // all the select operator share all the comparisons sentences
        for (RelationalAtom atom: relationalAtomList){
            ScanOperator scanOperator = new ScanOperator(atom, databaseCatalog);
            SelectOperator selectOperator = new SelectOperator(scanOperator, atom, comparisonAtomList);
            selectOperatorList.add(selectOperator);
        }
        if (selectOperatorList.size() == 0) current_root = null; // no Relation
        else if (selectOperatorList.size() == 1) { // no join
            // if only 1 select, then use a projection or aggregation (according to need_agg) as the root
            // and initialize it by the select operator
            current_root = (need_agg) ? new SumOperator(selectOperatorList.get(0), head):
                    new ProjectOperator(selectOperatorList.get(0), head);
        }
        else {
            // if more than one select, then we shall build a left deep operator tree
            JoinOperator join_root = new JoinOperator(selectOperatorList.get(0), selectOperatorList.get(1));
            for (int i = 1; i < selectOperatorList.size()-1; i++){
                join_root = new JoinOperator(join_root, selectOperatorList.get(i+1));
            }
            current_root = (need_agg) ? new SumOperator(join_root, head):
                    new ProjectOperator(join_root, head);
        }
    }
    public void operatorTreeBuilder(Head head,
                                    List<RelationalAtom> relation_body,
                                    List<ComparisonAtom> compare_body) throws IOException {
        /**
         *  use QueryEvaluator to first evaluate the structure and semantic of the query
         *  then parse it to form an better structured operation tree.
         *  with probable less join, less redundant comparisons on irrelevant relation atom
         * @params:
         *                head:  query head
         *  relationalAtomList:  list of relation atoms
         *  comparisonAtomList:  list of comparison atoms
         * */
        QueryEvaluator evaluator = new QueryEvaluator(head, relation_body, compare_body); // the optimized evaluator
        List<List<RelationalAtom>> listOfRelationList = new ArrayList<>(evaluator.getListOfRelationList());
        List<List<ComparisonAtom>> listOfComparisonList = new ArrayList<>(evaluator.getListOfComparisonList());
        // each join operator has a both of its children as select operator or join operator
        // and select operator has a scan operator as child
        // for the projection, if aggregate detected in head, the root will be SumOperator,
        // otherwise will be projection
        List<SelectOperator> selectOperatorList = new ArrayList<>();
        for (int nameIndex = 0; nameIndex<listOfComparisonList.size(); nameIndex++){
            // for loop the two-dimensional arrays
            List<ComparisonAtom> comparisonAtomList = listOfComparisonList.get(nameIndex);
            for (RelationalAtom atom: listOfRelationList.get(nameIndex)){
                ScanOperator scanOperator = new ScanOperator(atom, databaseCatalog);
                SelectOperator selectOperator = new SelectOperator(scanOperator, atom, comparisonAtomList);
                selectOperatorList.add(selectOperator);
            }
        }
        if (selectOperatorList.size() == 0) current_root = null; // no Relation
        else if (selectOperatorList.size() == 1) { // no join
            current_root = (need_agg) ? new SumOperator(selectOperatorList.get(0), head):
                    new ProjectOperator(selectOperatorList.get(0), head);
        }
        else {
            JoinOperator join_root = new JoinOperator(selectOperatorList.get(0), selectOperatorList.get(1));
            for (int i = 1; i < selectOperatorList.size()-1; i++){
                join_root = new JoinOperator(join_root, selectOperatorList.get(i+1));
            }
            current_root = (need_agg) ? new SumOperator(join_root, head):
                    new ProjectOperator(join_root, head);
        }
    }

    public Tuple getNextTuple() throws IOException {
        return current_root.getNextTuple();
    }

    public void reset() throws IOException {
        current_root.reset();
    }
    public List<Tuple> dump() throws IOException {
        return current_root.dump();
    }
}
