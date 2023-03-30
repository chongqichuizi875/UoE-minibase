package base;

import java.util.HashMap;

public class AtomMap {
    private Integer atom_to_remove, atom_pointed;
    private HashMap<Variable, Term> term_map;

    public AtomMap(HashMap<Variable, Term> term_map, Integer atom_to_remove, Integer atom_pointed){
        this.term_map = new HashMap<>(term_map);
        this.atom_to_remove = atom_to_remove;
        this.atom_pointed = atom_pointed;
    }
    public AtomMap(){
        this.term_map = new HashMap<>();
        this.atom_to_remove = null;
        this.atom_pointed = null;
    }
    public HashMap<Variable, Term> getTerm_map(){
        return this.term_map;
    }
    public Integer getAtom_to_remove(){
        return this.atom_to_remove;
    }
    public  Integer getAtom_pointed(){
        return this.atom_pointed;
    }

    public void setTerm_map(HashMap<Variable, Term> term_map){
        this.term_map = new HashMap<>(term_map);
    }
    public void setAtom_to_remove(Integer atom_to_remove){
        this.atom_to_remove = atom_to_remove;
    }
    public void setAtom_pointed(Integer atom_pointed){
        this.atom_pointed = atom_pointed;
    }
    public void put(Variable term1, Term term2){
        this.term_map.put(new Variable(term1.toString()), term2);
    }
}
