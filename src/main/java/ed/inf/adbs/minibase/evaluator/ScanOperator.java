package ed.inf.adbs.minibase.evaluator;


import base.RelationalAtom;
import base.Term;
import ed.inf.adbs.minibase.structures.DatabaseCatalog;
import ed.inf.adbs.minibase.structures.Tuple;
import ed.inf.adbs.minibase.structures.TypeWrapper;

import javax.xml.xpath.XPath;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScanOperator extends Operator{
    private DatabaseCatalog catalog;
    private Tuple tuple;
    private List<Term> original_terms;
    private BufferedReader br;
    private List<Boolean> types;

    private String filepath;

    public ScanOperator(RelationalAtom atom, DatabaseCatalog catalog){
        String atom_name = atom.getName();
        this.tuple = new Tuple(atom_name); // get the relation/table/atom name
        this.original_terms = atom.getTerms();
        this.catalog = catalog;

        this.types = catalog.getType_map().get(atom_name);
        filepath = catalog.getLocation_map().get(atom_name);
        try{
            this.br = new BufferedReader(new FileReader(filepath));
            this.br.mark(10000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
        List<String> sn = Arrays.asList(str.split(","));
        for (int i = 0; i<this.types.size();i++){
            line_wrapper.add(new TypeWrapper(this.types.get(i), sn.get(i).trim()));
        }
        return line_wrapper;
    }
}
