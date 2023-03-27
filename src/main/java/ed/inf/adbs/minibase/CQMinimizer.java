package ed.inf.adbs.minibase;

import base.*;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Minimization of conjunctive queries
 *
 */
public class CQMinimizer {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: CQMinimizer input_file output_file");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        minimizeCQ(inputFile, outputFile);

//        parsingExample(inputFile);
    }

    /**
     * CQ minimization procedure
     *
     * Assume the body of the query from inputFile has no comparison atoms
     * but could potentially have constants in its relational atoms.
     *
     */
    public static void minimizeCQ(String inputFile, String outputFile) {
        try{
            Query query = QueryParser.parse(Paths.get(inputFile));
//            Query query = QueryParser.parse("Q(x, y) :- R(x, z), S('zxc', z, w), S(4, z, w), R(x, 'qwe')");
            Head head = query.getHead();
            List<Atom> body = query.getBody();
            // body type transform
            List<RelationalAtom> relation_body = new ArrayList<>();
            for (Atom i: body){
                relation_body.add((RelationalAtom) i);
            }
            System.out.println("Before: " + query);
            // use a hashmap to store the map relation
            HashMap<Variable, Term> relation_map = new HashMap<>();
            while (removeOne(head, relation_body, relation_map));
            Query new_query = new Query(head, relation_body);
            System.out.println("After: " + new_query);


        }
        catch (Exception e){
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }
    public static boolean removeOne(Head head, List<RelationalAtom> body, HashMap<Variable, Term> relation_map){
        AtomMap atom_map = new AtomMap();
        // compare each pair of atom in body
        for (int i = 0; i < body.size(); i++){
            RelationalAtom atom1 = body.get(i);
            for (int j = 0; j < body.size(); j++) {
                // do not compare self
                if (i == j) continue;
                // else: -> atom1 map to atom2
                RelationalAtom atom2 = body.get(j);
                List<Term> terms1 = atom1.getTerms();
                List<Term> terms2 = atom2.getTerms();
                // compare atom name
                boolean cond1 = atom1.getName().equals(atom2.getName());
                // compare atom terms size
                boolean cond2 = terms1.size() == terms2.size();
                if (cond1 && cond2){
                    boolean has_map = true;
                    // compare each term
                    for (int k = 0; k < terms1.size(); k++){
                        // both not Variable
                        if (!(terms1.get(k) instanceof Variable || terms2.get(k) instanceof Variable)){
                            // term1 != term2, compare the next pair
                            if (!(terms1.get(k).equals(terms2.get(k)))){
                                has_map = false;
                                break;
                            }
                        }
                        // at least one is Variable

                        // term1 not Variable
                        else if (!(terms1.get(k) instanceof Variable)) {
                            has_map = false;
                            break;
                        }
                        // term1 Variable but term2 unknown
                        else atom_map.put((Variable) terms1.get(k), terms2.get(k));
                    }
                    if (has_map){
                        atom_map.setAtom_to_remove(i);
                        atom_map.setAtom_pointed(j);
                        // check if the atom can be removed
                        if (removeAtom(head, body, atom_map)) return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean removeAtom(Head head, List<RelationalAtom> relation_body, AtomMap atom_map){
        Integer atom_to_remove = atom_map.getAtom_to_remove();
        HashMap<Variable, Term> term_map = atom_map.getTerm_map();
        List<Variable> head_var = head.getVariables();
        // generate a new body for duplicate
        List<RelationalAtom> new_body = new ArrayList<>();
        for (RelationalAtom atom: relation_body) new_body.add(new RelationalAtom(atom));
        // remove the aton in new_body
        new_body.remove((int) atom_to_remove);
        // check if all head variables still exist in body \ {alpha}
        // gather all the variables in body \ {alpha}
        Set<String> body_set = new HashSet<>();
        for (RelationalAtom atom: new_body){
            for (Term term: atom.getTerms()){
                if (term instanceof Variable) body_set.add(term.toString());
            }
        }
        // check if all head variables still in body_set
        if (head_var.size() == 0){
            relation_body.remove((int) atom_to_remove);
            return true;
        }
        for (Variable var: head_var){
            if (!body_set.contains(var.toString())) return false;
        }
        relation_body.remove((int) atom_to_remove);
        return true;
    }

    public static void applyMap(List<RelationalAtom> relation_body, HashMap<Variable, Term> term_map){
        for (RelationalAtom atom: relation_body){
            atom.setTerms(atom.getTerms().stream()
                    .map(s -> term_map.getOrDefault(s, s))
                    .collect(Collectors.toList()));
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
//             Query query = QueryParser.parse("Q(x, y) :- R(x, z), S('string1', z, w), S(4, z, w)");
            // Query query = QueryParser.parse("Q(x) :- R(x, 'z'), S(4, z, w)");

            System.out.println("Entire query: " + query);
            Head head = query.getHead();
            System.out.println("");
            System.out.println("Head: " + head);
            List<Atom> body = query.getBody();
            for (Atom i : body){
                System.out.println("Relation: " + i);
            }
            System.out.println("Body: " + body);
        }
        catch (Exception e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

}
