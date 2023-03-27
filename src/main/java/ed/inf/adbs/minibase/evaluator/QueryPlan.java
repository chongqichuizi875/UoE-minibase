package ed.inf.adbs.minibase.evaluator;

import base.*;
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
    private ProjectOperator projectOperator;
    private Operator current_root;

    public QueryPlan(Query query) throws IOException {
        // parse the query from the input file
        System.out.println("Original query: "+ query);
        this.head = query.getHead();
        this.body = query.getBody();
        // body type transform
        this.relation_body = new ArrayList<>();
        this.compare_body = new ArrayList<>();
        for (Atom i: body){
            if (i instanceof RelationalAtom) relation_body.add((RelationalAtom) i);
            else if (i instanceof ComparisonAtom) compare_body.add((ComparisonAtom) i);
        }
        databaseCatalog = DatabaseCatalog.getCatalog();

        // construct a list of ScanOperators for the relation in the body of the query
        /* Scan*/
        scanOperator = new ScanOperator(relation_body.get(0), databaseCatalog);
        current_root = scanOperator;
        // decide if there are comparisons
        /* Select*/
        if (compare_body.size()>0){
            selectOperator = new SelectOperator(current_root, relation_body.get(0), compare_body);
            current_root = selectOperator;
        }
        // decide if there are projections
        /* Projection*/
        if (head.getVariables().size()>0){
            projectOperator = new ProjectOperator(current_root, head);
            current_root = projectOperator;
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
