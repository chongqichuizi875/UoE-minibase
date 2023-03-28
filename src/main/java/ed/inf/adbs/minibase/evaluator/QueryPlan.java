package ed.inf.adbs.minibase.evaluator;

import base.*;
import ed.inf.adbs.minibase.CQMinimizer;
import ed.inf.adbs.minibase.parser.QueryParser;
import ed.inf.adbs.minibase.structures.DatabaseCatalog;
import ed.inf.adbs.minibase.structures.Tuple;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class QueryPlan {
    private Head head;
    private List<Atom> body;
    private List<RelationalAtom> relation_body;
    private List<ComparisonAtom> compare_body;
    private DatabaseCatalog databaseCatalog;
    private ScanOperator scanOperator;
    private SelectOperator selectOperator;
    private Operator current_root;
    private List<SelectOperator> selectOperatorList;
    private List<JoinOperator> joinOperatorList;

    public QueryPlan(Query query) throws IOException {
        // parse the query from the input file
        databaseCatalog = DatabaseCatalog.getCatalog();
        System.out.println("Original query: "+ query);
        this.head = query.getHead();
        this.body = query.getBody();
        // body type transform
        this.relation_body = new ArrayList<>();
        this.compare_body = new ArrayList<>();
        selectOperatorList = new ArrayList<>();
        joinOperatorList = new ArrayList<>();
        for (Atom i: body){
            if (i instanceof RelationalAtom) {
                relation_body.add((RelationalAtom) i);
            }
            else if (i instanceof ComparisonAtom) compare_body.add((ComparisonAtom) i);
        }
        while (CQMinimizer.removeOne(head, relation_body));
        System.out.println("Query after parsing: "+head+" :- "+relation_body+compare_body);
        // build the leaf scan operators
        for (RelationalAtom atom: relation_body) {
            // build the select operators
            selectOperatorList.add(new SelectOperator(new ScanOperator(atom, databaseCatalog),atom,compare_body));
        }
        if (selectOperatorList.size() == 0) current_root = null; // no Relation
        else if (selectOperatorList.size() == 1) { // no join
            current_root = new ProjectOperator(selectOperatorList.get(0), head);
        }
        else {
            JoinOperator join_root = new JoinOperator(selectOperatorList.get(0), selectOperatorList.get(1), compare_body);
            for (int i = 1; i < selectOperatorList.size()-1; i++){
                join_root = new JoinOperator(join_root, selectOperatorList.get(i+1),compare_body);
            }
            current_root = new ProjectOperator(join_root, head);
        }
        SumAggregate head_terms;
        if ((head_terms = head.getSumAggregate())!=null){ // need group-by agg
            System.out.println("has agg!!!");
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
