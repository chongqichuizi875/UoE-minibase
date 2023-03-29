package ed.inf.adbs.minibase;

import base.Atom;
import base.Head;
import base.Query;
import ed.inf.adbs.minibase.evaluator.QueryPlan;
import ed.inf.adbs.minibase.parser.QueryParser;
import ed.inf.adbs.minibase.structures.Tuple;

import java.io.IOException;
import java.nio.file.Paths;
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
            QueryPlan queryPlan = new QueryPlan(query);
            List<Tuple> dump_list = queryPlan.dump();
            for(Tuple tuple:dump_list) System.out.println(tuple);
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
