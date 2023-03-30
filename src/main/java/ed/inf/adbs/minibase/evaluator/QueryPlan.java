package ed.inf.adbs.minibase.evaluator;

import base.*;
import ed.inf.adbs.minibase.CQMinimizer;
import ed.inf.adbs.minibase.structures.DatabaseCatalog;
import ed.inf.adbs.minibase.structures.Tuple;

import java.io.IOException;
import java.security.PublicKey;
import java.util.*;

public class QueryPlan {
    private Operator current_root;
    private final DatabaseCatalog databaseCatalog;
    private boolean need_agg;
    private List<List<RelationalAtom>> listOfRelationList;
    private List<List<ComparisonAtom>> listOfComparisonList;

    public QueryPlan(Query query) throws IOException {
        // parse the query from the input file
        databaseCatalog = DatabaseCatalog.getCatalog();
        System.out.println("Original query: "+ query);
        Head head = query.getHead();
        List<Atom> body = query.getBody();
        need_agg = head.getSumAggregate() != null; // need group-by agg
        // body type transform
        List<RelationalAtom> relation_body = new ArrayList<>();
        List<ComparisonAtom> compare_body = new ArrayList<>();
        List<SelectOperator> selectOperatorList = new ArrayList<>();
        for (Atom i: body){
            if (i instanceof RelationalAtom) {
                relation_body.add((RelationalAtom) i);
            }
            else if (i instanceof ComparisonAtom) compare_body.add((ComparisonAtom) i);
        }
//        while (CQMinimizer.removeOne(head, relation_body));
        // evaluate the old query and generate a high Highly customised operator tree
        // there are often less(no more) joins(while join order remain, just remove the unnecessary relation)
        QueryEvaluator evaluator = new QueryEvaluator(head, relation_body, compare_body);
        List<List<RelationalAtom>> listOfRelationList = new ArrayList<>(evaluator.getListOfRelationList());
        List<List<ComparisonAtom>> listOfComparisonList = new ArrayList<>(evaluator.getListOfComparisonList());

        operatorTreeBuilder(head, listOfRelationList, listOfComparisonList);
//        defaultOperatorBuilder(head, relation_body, compare_body);
    }
    public void defaultOperatorBuilder(Head head, List<RelationalAtom> relationalAtomList, List<ComparisonAtom> comparisonAtomList) throws IOException {
        List<SelectOperator> selectOperatorList = new ArrayList<>();
        for (RelationalAtom atom: relationalAtomList){
            ScanOperator scanOperator = new ScanOperator(new RelationalAtom(atom), databaseCatalog);
            SelectOperator selectOperator = new SelectOperator(scanOperator, atom, comparisonAtomList);
            selectOperatorList.add(selectOperator);
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
    public void operatorTreeBuilder(Head head,
                                    List<List<RelationalAtom>> listOfRelationList,
                                    List<List<ComparisonAtom>> listOfComparisonList) throws IOException {
        // each join operator has a both of its children as select operator or join operator
        // and select operator has a scan operator as child
        // for the projection, if aggregate detected in head, the root will be SumOperator,
        // otherwise will be projection
        List<SelectOperator> selectOperatorList = new ArrayList<>();
        for (int nameIndex = 0; nameIndex<listOfComparisonList.size(); nameIndex++){
            // for loop the two-dimensional arrays
            List<ComparisonAtom> comparisonAtomList = listOfComparisonList.get(nameIndex);
            for (RelationalAtom atom: listOfRelationList.get(nameIndex)){
                ScanOperator scanOperator = new ScanOperator(new RelationalAtom(atom), databaseCatalog);
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
