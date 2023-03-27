package ed.inf.adbs.minibase.evaluator;


import base.RelationalAtom;
import base.Term;
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
    private Tuple tuple;
    private List<Term> original_terms;
    private BufferedReader br;
    private List<Boolean> schema;
    private RelationalAtom relation_atom;
    private String filepath;

    public ScanOperator(RelationalAtom atom, DatabaseCatalog catalog){
        this.relation_atom = atom;
        String atom_name = atom.getName();
        this.tuple = new Tuple(atom_name); // get the relation/table/atom name

        this.schema = catalog.getSchema_map().get(atom_name);
        filepath = catalog.getLocation_map().get(atom_name);
        try{
            this.br = new BufferedReader(new FileReader(filepath));
            this.br.mark(10000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RelationalAtom getRelation_atom(){return this.relation_atom;}

    @Override
    public void reset() throws IOException {
        try{
            br.reset();
        }catch (Exception e){
            System.out.println("The br has run out of BufferedReader mark limit!");
            this.br = new BufferedReader(new FileReader(filepath));
        }
    }

    @Override
    public Tuple getNextTuple() throws IOException {
        String next;
        try{
            if ((next = br.readLine())!=null){
                this.tuple.setTuple(LineWrapper(next));
                return this.tuple;
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        return null;
    }


    public List<TypeWrapper> LineWrapper(String str){
        List<TypeWrapper> line_wrapper = new ArrayList<>();
        List<String> ls = Arrays.asList(str.split(","));
        for (int i = 0; i<this.schema.size();i++){
            line_wrapper.add(new TypeWrapper(this.schema.get(i), ls.get(i).trim()));
        }
        return line_wrapper;
    }
}
