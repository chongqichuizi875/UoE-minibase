package ed.inf.adbs.minibase.evaluator;


import base.RelationalAtom;
import ed.inf.adbs.minibase.structures.DatabaseCatalog;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScanOperator extends Operator{
    /**
     *              br:  the buffered-reader point
     *          schema:  schema of the tuple when reading a certain file(which column is string not int)
     *   relation_atom:  relation atom to be processed
     *        filepath:  the file path to be read
     * read_head_limit:  for br mark limit
     *
     * */
    private BufferedReader br;
    private final List<Boolean> schema;
    private final RelationalAtom relation_atom;
    private final String filepath;
    private static final int read_head_limit = 10000;


    public ScanOperator(RelationalAtom atom, DatabaseCatalog catalog){
        /**
         * constructor. get the filepath and schema information from DatabaseCatalog singleton according to
         * the name of the relation atom.
         * */
        this.relation_atom = atom;
        String atom_name = atom.getName();

        this.schema = catalog.getSchema_map().get(atom_name);
        filepath = catalog.getLocation_map().get(atom_name);
        try{
            this.br = new BufferedReader(new FileReader(filepath));
            this.br.mark(read_head_limit);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RelationalAtom getRelation_atom(){return this.relation_atom;}

    @Override
    public void reset() throws IOException {
        /**
         * reset the br to its mark
         * if it has read over 10000 lines ahead, then create a new buffer reader
         * */
        try{
            br.reset();
        }catch (Exception e){
            System.out.println("The br has run out of BufferedReader mark limit!");
            this.br = new BufferedReader(new FileReader(filepath));
        }
    }


    @Override
    public Tuple getNextTuple() throws IOException {
        /**
         * if buffer reader does not return a null, and the returned tuple has > 0 length (get rid of empty line)
         * then return the result of buffer reader
         * @return:
         *  Tuple:  if not an empty line and the buffer reader can successfully return a string, then use Tuple
         *          to wrap the readed string
         *   null:  if buffer reader read to the end
         * */
        String next;
        Tuple new_tuple = new Tuple(relation_atom.getName());
        try{
            if (((next = br.readLine())!=null) && next.length()>0){
                new_tuple.setTuple(LineWrapper(next));
                return new_tuple;
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        return null;
    }


    public List<TypeWrapper> LineWrapper(String str){
        /**
         * To wrap a String object to a list of TypeWrapper
         * @return:
         *  line_wrapper:  a list of TypeWrapper instances, wrap of String
         * @params:
         *           str:  string object to be wrapped
         * */
        List<TypeWrapper> line_wrapper = new ArrayList<>();
        List<String> ls = Arrays.asList(str.split(","));
        for (int i = 0; i<schema.size();i++){
            line_wrapper.add(new TypeWrapper(schema.get(i), ls.get(i).trim()));
        }
        return line_wrapper;
    }
    @Override
    public BufferedReader getBr(){return br;}
}
