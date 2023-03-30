package ed.inf.adbs.minibase.evaluator;

import base.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.StreamSupport;

public class QueryEvaluator {
    /**
     *                                 head:  query head
     *                   relationalAtomList:  relation atoms of the original query
     *                   comparisonAtomList:  comparison atoms of the original query
     *            mapRelationNameToAtomList:  Map name (R, S, T) to relations R(x,y,z), S(u,v,w)
     *                   listOfRelationList:  each sub list contains relation atoms with same name
     *                                         -> [[R(x,y,z),R(x,y,w)],[S(q,p)]]
     *                 listOfComparisonList: each sub list contains comparison list for select operator
     *                                         -> [[x>5, y<v], [u='shit']]
     *        mapRelationNameToVariableList:  map the relation name to a list of variables exists in its corresponding relation
     * mapRelationNameToOutsideVariableList:  map the relation name to a list of variables exists in all others' relation
     *                     allVariablesList:  the sum of all the list in mapRelationNameToVariableList, u
     *                                        sed for computing mapRelationNameToOutsideVariableList
     *               mapVariableToAtomIndex:  maintain position of each variable in each relation
     *                                         (each sub list assign to corresponding relation)
     *                newMapIndexToVariable:  when the new relation atoms (shorter) are generated, use a map to maintain
     *                                         the relationship between variables and index
     *
     * */
    private Head head;
    private List<RelationalAtom> relationalAtomList;
    private List<ComparisonAtom> comparisonAtomList;
    private HashMap<String, List<RelationalAtom>> mapRelationNameToAtomList; // Map name (R, S, T) to relations R(x,y,z), S(u,v,w)
    private List<List<RelationalAtom>> listOfRelationList; // each sub list contains relation atoms with same name
    private List<List<ComparisonAtom>> listOfComparisonList; // each sub list contains comparison list for select operator
    private HashMap<String, List<Variable>> mapRelationNameToVariableList; // map name R to variables [x,y,z,p] exist in relation R
    private HashMap<String, List<Variable>> mapRelationNameToOutsideVariableList; // map name R to variables exist in relations except R
    private List<Variable> allVariablesList; // contain the sum of "mapRelationNameToVariableList", used for computing "mapRelationNameToOutsideVariableList"
    private HashMap<Variable, List<HashMap<String, Integer>>> mapVariableToAtomIndex; // maintain position of each variable in each relation
                                                                                      // (each sub list assign to corresponding relation)
    private HashMap<HashMap<String, Integer>, List<Variable>> newMapIndexToVariable; // when the new relation atoms (shorter) are generated, use a map to
                                                                                         // maintain the relationship between variables and index
    private List<Variable> allVariablesInNewQuery;

    private List<String> JoinOrderList = new ArrayList<>(); // maintain the join order of the tree
    public QueryEvaluator(Head head, List<RelationalAtom> relationalAtomList, List<ComparisonAtom> comparisonAtomList){
        /**
         * Constructor.
         * 1: generate maps and lists containing information of query
         *    (which term appear in which relation, its index, the order of them...)
         * 2: rearrange the relation atoms according to its variables
         *    and assign the generated comparisons to them
         * 3: generate new maps according to the rearranged relation atoms
         * 4: according to the new maps and old maps, do some replacement of the
         *    original comparisons and assign them to corresponding relation atoms
         * */
        // do some initialization
        this.head = head;
        this.relationalAtomList = relationalAtomList;
        this.comparisonAtomList = comparisonAtomList;
        allVariablesList = new ArrayList<>();
        mapVariableToAtomIndex = new HashMap<>();
        // get head variables for building Operator tree
        List<Variable> head_vars = new ArrayList<>(head.getVariables());
        List<Term> head_aggs = new ArrayList<>();
        boolean need_agg = head.getSumAggregate() != null;
        if (need_agg) {
            List<Term> terms = head.getSumAggregate().getProductTerms();
            head_aggs.addAll(terms);
        }
        for (Term term : head_aggs) {
            if (term instanceof Variable)
                head_vars.add((Variable) term);
        }

        // initialize maps for latter search and compare
        mapRelationNameToAtomList = new HashMap<>();
        // hashmap Disorderly, use a list to help
        JoinOrderList = new ArrayList<>();
        listOfRelationList = new ArrayList<>();
        listOfComparisonList = new ArrayList<>();
        mapRelationNameToVariableList = new HashMap<>();
        mapRelationNameToOutsideVariableList = new HashMap<>();
        // step 1: generate the maps and lists which maintain the info of query for future use
        generateRelationMap();
        // step 2: the main loop
        for (String atom_name: JoinOrderList){ // iterate all relation atoms with same name e.g.[R(x,y,z), R(x,5,7), R(u,v,w)]
            // the overall goal is to convert [R(x,y,z), R(x,5,7), R(u,v,w)] to R(x,y,z),[x=u,y=5,z=7]
            // 1: remove the unnecessary relation atom to minimize self join but maintain all the variables needing projection
            // 2: translate variables to comparison atoms
            // 3: group comparison atoms with corresponding relation atoms(variable related)
            // 4: build the left deep join tree with the same join order as original query R&S&T will always be R&S&T
            List<RelationalAtom> atomList = mapRelationNameToAtomList.get(atom_name);
            // the new lists are what we should generate to store the new query body
            List<RelationalAtom> newAtomList = new ArrayList<>();
            List<ComparisonAtom> newComparisonList = new ArrayList<>();
            HashMap<Integer, List<Variable>> indexMap = buildIndexMap(atomList, head_vars);
            // loop the indexMap, get the max length of List -> Minimum relations of the same name required
            // Q(x, y, z, u):- R(x,y,z),R(x,y,w) -> need at least 2 R for projection
            int max_length = 1;
            for (List<Variable> variables : indexMap.values()) {
                max_length = Math.max(max_length, variables.size());
            }

            for (List<Variable> variables : indexMap.values()) {
                // fill the vacant place
                // Q(x, y, z, w):- R(x,y,z),R(u,v,w) -> R(x,y,z),R(x,y,w), because u and v will be discarded
                while (variables.size() < max_length) {
                    variables.add(variables.get(0));
                }
            }

            // iterate each index in a relation for replacing the useless variables or constants
            // and generate a (or the minimum number of) relations
            // e.g. Q(x, y, z):- R(x,4,z),R(u,y,w) -> R(x,y,z)
            for (int relation_id = 0; relation_id < max_length; relation_id++){
                newAtomList.add(getNewAtom(atomList, indexMap, relation_id, newComparisonList));
            }

            // 1: we have shortened the relations
            // 2: we have generated corresponding comparison atoms according to the compression of variables
            // 3: now we should care about the new relation atom list and the original comparison atoms
            //   3.1: we would better also assign comparison atoms in original query to its corresponding relation atom
            //        so that we can release the pressure of the bottom select operation
            //        e.g. u = 5 should go to S(u,v,w) but not R(x,y,z)
            //   3.2: On the other hand, we should handle the problem that. If the sql writer is stupid, or the parser
            //        stupid-> Q(x,y):- R(x,y),R(x,s), s=3. We will remain R(x,y), so what about the s=3?
            //        one way is to generate y=s, however I do not like it. So I decide to replace the s=3 to y=3
            //        That's why there are so many fking maps and lists. I wanted to die when I was writing code!!!
            listOfRelationList.add(newAtomList);
            listOfComparisonList.add(newComparisonList);
        }


        // what we should do is to
        // 1: create the new map of index to variable
        // 2: create a new set of variables in new relation lists
        // 3: detect the variable that has been discarded
        // 4: use the old (variable to index map) to find the replacement
        // 5: replace it!
        allVariablesInNewQuery = new ArrayList<>(); // saving all remaining variables
        newMapIndexToVariable = new HashMap<>(); // map to replacements
        generateNewMapIndexToVariable();
        // until now, if we find k = 3 while k not in any relation atoms, we can search mapVariableToAtomIndex for
        // what relation atom and index it belonged to, and use the atom name and index to find a replacement
        // Tips (any replacement is ok since they all exist in the new relation atoms)
        transformOriginalComparisonAtomList(comparisonAtomList);
    }

    public List<List<RelationalAtom>> getListOfRelationList(){return listOfRelationList;}
    public List<List<ComparisonAtom>> getListOfComparisonList(){return listOfComparisonList;}
    public void generateNewMapIndexToVariable(){
        /**
         * generate the new map according to the newly generated relation atom lists
         * gather the information of variables and their indexes in each relation atom
         * */
        for (List<RelationalAtom> atomList: listOfRelationList){
            // we build another map to search for variables for replacement in the new query
            // if new query: Q(x,y):- R(x,y), then map<R, 0> -> x, map<R, 1> -> y
            for (RelationalAtom atom: atomList){
                for (int index = 0; index < atom.getTerms().size();index++){
                    String atom_name = atom.getName();
                    Term term = atom.getTerms().get(index);
                    if (term instanceof Variable){

                        HashMap<String, Integer> map = new HashMap<>();
                        map.put(atom_name, index);
                        // e.g. R(x,y),R(x,z), then map<R,1> -> [y,z]
                        if (newMapIndexToVariable.containsKey(map)){
                            newMapIndexToVariable.get(map).add((Variable) term);
                            allVariablesInNewQuery.add((Variable) term); // gather all variables remaining in atoms
                        }
                        else {
                            List<Variable> lst= new ArrayList<>();
                            lst.add((Variable) term);
                            newMapIndexToVariable.put(map, lst);
                            allVariablesInNewQuery.add((Variable) term);
                        }
                    }
                }
            }
        }
    }
    public void generateRelationMap(){
        /**
         * generate some maps reflecting the relationships between the head variables,
         *  variables of each relation atom, and the index of them.
         * */
        // generate the mapRelationNameToList and JoinOrderList
        for (RelationalAtom atom : relationalAtomList) { // for each atom
            if (!JoinOrderList.contains(atom.getName())){
                JoinOrderList.add(atom.getName()); // maintain a join order list
                                                   // so we can keep the order of join
            }
            // e.g. R -> [R(x,y,z), R(x,y,w)]
            if (mapRelationNameToAtomList.containsKey(atom.getName())) {
                mapRelationNameToAtomList.get(atom.getName()).add(atom);
            } else {
                List<RelationalAtom> relationalAtomList1 = new ArrayList<>();
                relationalAtomList1.add(atom);
                mapRelationNameToAtomList.put(atom.getName(), relationalAtomList1);
            }
        }

        for (String atom_name: mapRelationNameToAtomList.keySet()){
            // generate several maps and lists to maintain the info of original query
            // e.g. R -> [x,y,z,w]
            mapRelationNameToVariableList.put(atom_name, new ArrayList<>());
            for (RelationalAtom atom: mapRelationNameToAtomList.get(atom_name)){
                List<Term> terms = atom.getTerms();
                for (Term term: terms){
                    if ((term instanceof Variable) &&
                            !(mapRelationNameToVariableList.get(atom_name).contains(term))) {
                        // the map contains distinct variables exists in each relation
                        mapRelationNameToVariableList.get(atom_name).add((Variable) term);
                        allVariablesList.add((Variable) term); // e.g. [x,y,z,u,v,w,m,n]
                        // e.g. for R(x,y,z):  x -> map<R, 0>, and y -> map<R, 1>
                        if (mapVariableToAtomIndex.containsKey((Variable) term)){
                            // variable already exists in one place
                            HashMap<String, Integer> map = new HashMap<>();
                            map.put(atom_name, terms.indexOf(term));
                            mapVariableToAtomIndex.get(term).add(map);
                        }
                        else{
                            // variable first occurrence
                            List<HashMap<String, Integer>> lst = new ArrayList<>();
                            HashMap<String, Integer> map = new HashMap<>();
                            map.put(atom_name, terms.indexOf(term));
                            lst.add(map);
                            mapVariableToAtomIndex.put((Variable) term, lst);
                        }
                    }
                }
            }
        }
        // then compute the mapRelationNameToOutsideVariableList
        // e.g. Q(x,y,z):- R(x,y,z),R(x,y,c), c=5 -> R(x,y,c) will be discarded, so we should also delete c=5 from
        // the original query <--> equals that c does not exist in other relations, as is the usage of this map
        for (String atom_name: mapRelationNameToVariableList.keySet()){
            mapRelationNameToOutsideVariableList.put(atom_name, new ArrayList<>());
            List<Variable> lst = new ArrayList<>(allVariablesList);
            // e.g. allVariablesList = [x,x,y,z,u], mapRelationNameToVariableList = [x,y,u]
            // then the lst -> [x,z]
            mapRelationNameToVariableList.get(atom_name).forEach(lst::remove);
            mapRelationNameToOutsideVariableList.get(atom_name).addAll(lst);
        }
    }
    public HashMap<Integer, List<Variable>> buildIndexMap(List<RelationalAtom> atomList, List<Variable> head_vars){
        /**
         * build the indexMap for each position of each relation
         * aiming at finding replacements of the variables which have been kicked out of the newly generated
         * relation atoms, but still exists in the original comparisons
         * e.g. Q(x,y):- R(x,y),R(x,z),z=2 --> the z=2 in the original comparisons should change to y=2
         * @return
         *  i ndexMap:  a hashmap mapping from the position index of one atom to a list of variables who are
         *                    at that position
         * @params:
         *   atomList:  a list of atoms with same name
         *  head_vars:  list of variables in head
         * */
        // in each relation atom
        // map the index of variable in the relation atom to a list for removing unnecessary variables or constants
        // e.g. Q(x,y,z):- R(x,y,z),R(x,y,c), so 2 -> [z,c], finally z will remain and c discarded
        HashMap<Integer, List<Variable>> indexMap = new HashMap<>();
        for (RelationalAtom atom: atomList){ // loop each atom
            for (Term term: atom.getTerms()){ // loop each term
                // find if this term is a variable && need to finally project
                if ((term instanceof Variable) && (head_vars.contains(term))) {
                    if (!(indexMap.containsKey(atom.getTerms().indexOf(term)))) {
                        List<Variable> ls = new ArrayList<>();
                        ls.add((Variable) term);
                        indexMap.put(atom.getTerms().indexOf(term), ls);
                    } else {
                        // if the index exists, like x is index 0 in first R and u is also index 0 in the second R
                        // e.g. R(x,y),R(u,y)
                        if (!(indexMap.get(atom.getTerms().indexOf(term)).contains(term)))
                            indexMap.get(atom.getTerms().indexOf(term)).add((Variable) term);
                    }
                }
            }
        }
        return indexMap;
    }
    public RelationalAtom getNewAtom(List<RelationalAtom> atomList, HashMap<Integer, List<Variable>> indexMap, int relation_id, List<ComparisonAtom> newComparisonList){
        /**
         * according to the variables in atoms and in head, we can rearrange the relation atom, delete some unnecessary variables
         * even unnecessary relation atoms. We use a map of relation variable position index to decide which variable should be
         * kept and which atom should be kept.
         * @return:
         *           new_atom:  return a RelationalAtom each time for a loop of all the relation atoms with the same name
         * @params:
         *           atomList:  [R1, R2, R3...] a list of relation atoms
         *           indexMap:  e.g. Q(x,y,z):- R(x,y,z),R(x,y,c), so 2 -> [z,c]. Position index -> all variables exists in that place
         *        relation_id:  the relation index in the atomList
         *  newComparisonList:  gather the newly generated comparisons
         * */
        // iterate each index in a relation e.g. (0,1,2) for R(x,y,z)
        RelationalAtom new_atom = new RelationalAtom(atomList.get(0).getName());
        for (int i = 0; i < atomList.get(0).getTerms().size(); i++) {
            if (indexMap.containsKey(i)) { // if the ith position need a projection
                new_atom.addTerm(indexMap.get(i).get(relation_id));
                // loop all the terms in all the relations
                // to generate comparisons like y=5, y=v
                for (RelationalAtom atom : atomList) {
                    Term term = atom.getTerms().get(i);
                    // if the selected term is not the variable which needs projection
                    if (!(term.equals(indexMap.get(i).get(relation_id)))) {
                        // and if it is constant, or it exists in other relations
                        // then the term should be translated into a comparison
                        // like y = 5 for selection in current relation or y = v for join
                        if ((term instanceof Constant) ||
                                ((term instanceof Variable) &&
                                        (mapRelationNameToOutsideVariableList.get(atom.getName()).contains(term)))) {
                            newComparisonList.add(
                                    new ComparisonAtom(indexMap.get(i).get(relation_id),
                                            atom.getTerms().get(i), ComparisonOperator.EQ));
                        }
                    }
                }
            }
            else {
                // if this index position do not need a projection
                // then pick a variable if available and generate comparisonAtoms
                // assign the first term to new_atom then replace it
                new_atom.addTerm(atomList.get(0).getTerms().get(i));
                for (RelationalAtom atom : atomList) {
                    if (atom.getTerms().get(i) instanceof Variable) {
                        // if variable exists in this position, then use it as search
                        new_atom.replaceTerm(i, atom.getTerms().get(i));
                        break;
                    }
                }
                // use another loop to build comparisons
                // if this position contains variable not projected, comparisons will be like: w=9,w=p,w=n
                // if contains constant, comparisons will be like: 'ppls'='ips', 'ppls'='adbs'
                for (RelationalAtom atom : atomList) {
                    Term term = atom.getTerms().get(i);
                    if (!(term.equals(new_atom.getTerms().get(i)))) {
                        if ((term instanceof Constant) ||
                                ((term instanceof Variable) &&
                                        (mapRelationNameToOutsideVariableList.get(atom.getName()).contains(term)))) {
                            newComparisonList.add(
                                    new ComparisonAtom(new_atom.getTerms().get(i),
                                            atom.getTerms().get(i), ComparisonOperator.EQ));
                        }
                    }
                }
            }
        }
        return new_atom;
    }
    public void transformOriginalComparisonAtomList(List<ComparisonAtom> comparisonAtomList){
        /**
         * splitting the original comparisons, assigning them into corresponding relations
         * e.g. x>5 assign to R(x,y,z), u='adbs' assign to S(u,v,w). And if exists variables that
         * no longer exist in the new relation atoms: Q(x,y):- R(x,y),R(x,z),z=3 -> Q(x,y):- R(x,y),y=3
         * now in this function
         * we can fetch where z belong to(R, index at 1), and find in the newly generated map that
         * at (R, index at 1) there are replacement y still exists in head variables, so we replace z with y!
         * @params:
         *  comparisonAtomList:  all the original comparisons in the old query
         *
         * */
        for (String atom_name: JoinOrderList){ // follow the same order as we measure the generated comparisons
            // in the end of for loop, combine the generated comparisons and original comparisons
            int combineListId = JoinOrderList.indexOf(atom_name);
            List<ComparisonAtom> originalComparisonAtoms = new ArrayList<>();
            for (int i = 0; i < comparisonAtomList.size(); i++){
                Term term1 = comparisonAtomList.get(i).getTerm1();
                Term term2 = comparisonAtomList.get(i).getTerm2();
                ComparisonOperator op = comparisonAtomList.get(i).getOp();
                if ((term1 instanceof Constant) && (term2 instanceof Constant)){
                    // 2 constant, simply assign it to the first/or random relation for selection
                    originalComparisonAtoms.add(comparisonAtomList.get(i));
                }
                else if ((term1 instanceof Variable) && (term2 instanceof Constant)){
                    if (!allVariablesInNewQuery.contains(term1)){
                        ComparisonAtom newComparison;
                        if((newComparison=getNewComparisonAtom(atom_name, comparisonAtomList.get(i), true))!=null) {
                            originalComparisonAtoms.add(newComparison);
                        }
                    }
                    else {originalComparisonAtoms.add(new ComparisonAtom(term1, term2, op));}
                }
                else if ((term2 instanceof Variable) && (term1 instanceof Constant)){
                    if (!allVariablesInNewQuery.contains(term2)) {
                        ComparisonAtom newComparison;
                        if((newComparison=getNewComparisonAtom(atom_name, comparisonAtomList.get(i), false))!=null) {
                            originalComparisonAtoms.add(newComparison);
                        }
                    }
                    else {originalComparisonAtoms.add(new ComparisonAtom(term1, term2, op));}
                }
                // for 2 variables, we assume it is a valid join operation,
                // so we just detect one of its term
                else {
                    assert term1 instanceof Variable;
                    assert term2 instanceof Variable;
                    if (allVariablesInNewQuery.contains(term1) &&
                            (!allVariablesInNewQuery.contains(term2))){
                        ComparisonAtom newComparison;
                        if((newComparison=getNewComparisonAtom(atom_name, comparisonAtomList.get(i), false))!=null) {
                            originalComparisonAtoms.add(newComparison);
                        }
                    }
                    else {originalComparisonAtoms.add(new ComparisonAtom(term1, term2, op));}
                    if (!allVariablesInNewQuery.contains(term1) &&
                            (allVariablesInNewQuery.contains(term2))){
                        ComparisonAtom newComparison;
                        if((newComparison=getNewComparisonAtom(atom_name, comparisonAtomList.get(i), true))!=null) {
                            originalComparisonAtoms.add(newComparison);
                        }
                    }
                    else {originalComparisonAtoms.add(new ComparisonAtom(term1, term2, op));}
                    if (!allVariablesInNewQuery.contains(term1) &&
                            (!allVariablesInNewQuery.contains(term2))){
                        ComparisonAtom newComparison;
                        if((newComparison=getNewComparisonAtom(atom_name,
                                getNewComparisonAtom(atom_name, comparisonAtomList.get(i), false), true))!=null) {
                            originalComparisonAtoms.add(newComparison);
                        }
                    }
                    // both variable can be found in region of the new relation atoms
                    else {originalComparisonAtoms.add(new ComparisonAtom(term1, term2, op));}
                }
            }
            listOfComparisonList.get(combineListId).addAll(originalComparisonAtoms);
        }

    }
    public ComparisonAtom getNewComparisonAtom(String atomName, ComparisonAtom atom, boolean isLeft){
        /**
         * the utils function for replacing one term in the comparison atom
         * @return:
         *     null:  if atom==null, or we can not find a replacement for the term
         *  newAtom:  return a new ComparisonAtom instance replacing left or right of its terms
         * @params:
         *  atomName:  name of the atom
         *  atom:  the ComparisonAtom which we want to replace part of it
         *  isLeft:  true -> replace the left term(term1), false -> term2
         * */
        if (atom == null) return null;
        // if isLeft, term1 will be replaced, else term2
        Term term1 = atom.getTerm1();
        Term term2 = atom.getTerm2();
        ComparisonOperator op = atom.getOp();
        List<HashMap<String, Integer>> mapList = new ArrayList<>();
        if (isLeft)
            mapList  = mapVariableToAtomIndex.get((Variable) term1);
        else mapList = mapVariableToAtomIndex.get((Variable) term2);

        for (HashMap<String, Integer> map: mapList){ // loop the mapList for (name, index) pair that will eventually help
            if (map.containsKey(atomName)){
                // if the atom name corresponds to the replacement's atom name, then we get its index
                int index = map.get(atomName);
                // use name and index to find the replacement
                HashMap<String, Integer> searchMap = new HashMap<>();
                searchMap.put(atomName, index);
                if (newMapIndexToVariable.containsKey(searchMap)){
                    // we can just use the first replacement since they all appear in the new query
                    Variable replacement = newMapIndexToVariable.get(searchMap).get(0);
                    if (isLeft)
                        return new ComparisonAtom(replacement, term2, op);
                    else return new ComparisonAtom(term1, replacement, op);
                }
            }
        }
        return null;
    }

}
