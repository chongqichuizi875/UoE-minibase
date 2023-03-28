package ed.inf.adbs.minibase;

import base.*;
import ed.inf.adbs.minibase.evaluator.*;
import ed.inf.adbs.minibase.parser.QueryParser;
import ed.inf.adbs.minibase.structures.DatabaseCatalog;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * In-memory database system
 *
 */
public class Minibase {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Usage: Minibase database_dir input_file output_file");
            return;
        }

        String databaseDir = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        evaluateCQ(databaseDir, inputFile, outputFile);

//        parsingExample(inputFile);
    }

    public static void evaluateCQ(String databaseDir, String inputFile, String outputFile) {
        // parse the query from the input file
        try{
            // parse the query from the input file
            Query query = QueryParser.parse(Paths.get(inputFile));
//            QueryPlan queryPlan = new QueryPlan(query);
//            System.out.println(queryPlan.getNextTuple());
//            System.out.println(queryPlan.getNextTuple());
//            System.out.println(queryPlan.getNextTuple());
//            System.out.println(queryPlan.getNextTuple());
//            queryPlan.reset();
//            System.out.println("----------------------------------------");
//            System.out.println(queryPlan.getNextTuple());
//            System.out.println(queryPlan.getNextTuple());
//            System.out.println(queryPlan.getNextTuple());
//            queryPlan.reset();
//            System.out.println("----------------------------------------");
//            List<Tuple> lst = queryPlan.dump();
//            for (Tuple tp: lst){
//                System.out.println(tp);
//            }


            System.out.println("Original query: "+ query);
            Head head = query.getHead();
            List<Atom> body = query.getBody();
            // body type transform
            List<RelationalAtom> relation_body = new ArrayList<>();
            List<ComparisonAtom> compare_body = new ArrayList<>();
            for (Atom i: body){
                if (i instanceof RelationalAtom) relation_body.add((RelationalAtom) i);
                else if (i instanceof ComparisonAtom) compare_body.add((ComparisonAtom) i);
            }
            DatabaseCatalog databaseCatalog = DatabaseCatalog.getCatalog();
            // construct a ScanOperator for the relation in the body of the query
            /* Scan*/
            RelationalAtom relation1 = relation_body.get(0);
            ScanOperator scanOperator1 = new ScanOperator(relation1, databaseCatalog);
            RelationalAtom relation2 = relation_body.get(1);
            ScanOperator scanOperator2 = new ScanOperator(relation2, databaseCatalog);
            JoinOperator joinOperator = new JoinOperator(scanOperator1, scanOperator2, compare_body);
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());
            System.out.println(joinOperator.getNextTuple());

//            // call dump() on your ScanOperator to send the results somewhere helpful
//            List<Tuple> lst = scanOperator.dump();
//            System.out.println("----------------------------------------");
//            for (Tuple tp: lst){
//                System.out.println(tp);
//            }
//            System.out.println("----------------------------------------");
//            /* Select*/
//            SelectOperator selectOperator = new SelectOperator(scanOperator, relation, compare_body);
//            System.out.println(selectOperator.getNextTuple());
//            System.out.println(selectOperator.getNextTuple());
//            System.out.println(selectOperator.getNextTuple());
//            System.out.println("----------------------------------------");
//            /* Projection*/
//            // p1 has child scan , p2 has child select
//            ProjectOperator projectOperator1 = new ProjectOperator(scanOperator, head);
//            ProjectOperator projectOperator2 = new ProjectOperator(selectOperator, head);
//            projectOperator1.reset();
//            System.out.println("p1: " + projectOperator1.getNextTuple());
//            System.out.println("p1: " + projectOperator1.getNextTuple());
//            System.out.println("p1: " + projectOperator1.getNextTuple());
//            projectOperator2.reset();
//            System.out.println("p2: " + projectOperator2.getNextTuple());
//            System.out.println("p2: " + projectOperator2.getNextTuple());
//            System.out.println("p2: " + projectOperator2.getNextTuple());


        } catch (IOException e) {
            System.err.println("Exception occurred during Scan Operation");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Example method for getting started with the parser.
     * Reads CQ from a file and prints it to screen, then extracts Head and Body
     * from the query and prints them to screen.
     */

    public static void parsingExample(String filename) {
        try {
            Query query = QueryParser.parse(Paths.get(filename));
            // Query query = QueryParser.parse("Q(x, y) :- R(x, z), S(y, z, w), z < w");
            // Query query = QueryParser.parse("Q(SUM(x * 2 * x)) :- R(x, 'z'), S(4, z, w), 4 < 'test string' ");

            System.out.println("Entire query: " + query);
            Head head = query.getHead();
            System.out.println("Head: " + head);
            List<Atom> body = query.getBody();
            System.out.println("Body: " + body);
        }
        catch (Exception e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

}
