package ed.inf.adbs.minibase.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Tuple {
    private List<TypeWrapper> tuple;
    private String name;
    public Tuple(){
        this.name = null;
        this.tuple = new ArrayList<>();
    }
    public Tuple(Tuple tuple){
        this.name = tuple.getName();
        this.tuple = tuple.getTuple();
    }
    public Tuple(String name){
        this.name = name;
        this.tuple = new ArrayList<>();
    }
    public Tuple(String name, List<TypeWrapper> tuple){
        this.name = name;
        this.tuple = tuple;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }


    public List<TypeWrapper> getTuple(){
        return this.tuple;
    }

    public void tupleProjection(TypeWrapper wrap){
        this.tuple.add(wrap);
    }
    public void add(List<TypeWrapper> tuple){
        this.tuple.addAll(tuple);
    }
    public void removeAll(){
        tuple.removeAll(tuple);
    }

    public TypeWrapper getWrapInTuple(int i){
        try{
            return this.tuple.get(i);
        }
        catch (Exception e){
            System.err.println("index out of tuple upper bound");
            return null;
        }
    }

    public void setTuple(List<TypeWrapper> tuple){
        this.tuple = tuple; // can not use this.tuple.addAll(tuple), for all the tuples will append and flatten
    }

    @Override
    public String toString(){
        return this.name + ": " + this.tuple.stream().map(s->s.getValue().toString()).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof Tuple)) return false;
        if (this.tuple.size() != ((Tuple) obj).getTuple().size()) return false;
        if (!Objects.equals(this.name, ((Tuple) obj).getName())) return false;
        for (int i = 0; i < this.tuple.size(); i++){
            if (!(this.tuple.get(i).equals(((Tuple) obj).getTuple().get(i)))) return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        return 17*31 + this.getTuple().hashCode() + this.getName().hashCode();
    }
}
