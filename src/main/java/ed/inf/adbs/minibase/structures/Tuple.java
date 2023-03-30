package ed.inf.adbs.minibase.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Tuple {
    /**
     * Tuple aiming at Wrapping the relation atom at first
     * For TypeWrapper corresponds to each Term, and have the same name as string format
     *
     * tuple:  a list of TypeWrappers for wrapping the term value(Variable or Constant)
     *  name:  the name of the Tuple, often the same as relation atom name
     * */
    private List<TypeWrapper> tuple;
    private final String name;
    public Tuple(){
        this.name = null;
        this.tuple = new ArrayList<>();
    }
    public Tuple(Tuple tuple){
        /**
         * constructor, for duplicating a tuple
         * */
        this.name = tuple.getName();
        this.tuple = tuple.getTuple();
    }
    public Tuple(String name){
        /**
         * constructor, we can use a String to initialize the tuple first
         * */
        this.name = name;
        this.tuple = new ArrayList<>();
    }
    public Tuple(String name, List<TypeWrapper> tuple){
        /**
         * constructor, we can also use a String and a List of TypeWrapper for initialization
         * */
        this.name = name;
        this.tuple = tuple;
    }

    public String getName(){
        return this.name;
    }


    public List<TypeWrapper> getTuple(){
        return this.tuple;
    }

    public void tupleProjection(TypeWrapper wrap){
        /**
         * add a TypeWrapper item to the Tuple instance
         *
         * @params:
         *  wrap:  one TypeWrapper added to this
         * */
        this.tuple.add(wrap);
    }
    public void add(List<TypeWrapper> tuple){
        /**
         * add a list of TypeWrapper items to the Tuple instance
         *
         * @params:
         *  tuple:  a list of TypeWrapper items added to this
         * */
        this.tuple.addAll(tuple);
    }
    public void removeAll(){
        /**
         * remove all the TypeWrappers in the tuple
         * */
        tuple.clear();
    }

    public TypeWrapper getWrapInTuple(int i){
        /**
         * get the ith TypeWrapper in the tuple by an index i
         * @return:
         *         null:  if input index i out of list upper bound
         *  TypeWrapper:  if input index i in within the list.size()
         * @params:
         *            i:  index

         * */
        try{
            return this.tuple.get(i);
        }
        catch (Exception e){
            System.err.println("index out of tuple upper bound");
            return null;
        }
    }

    public void setTuple(List<TypeWrapper> tuple){
        /**
         * Assigning a List of TypeWrappers to this instance
         * @params:
         *   tuple:  a List of TypeWrappers
         * */
        this.tuple = tuple; // can not use this.tuple.addAll(tuple), for all the tuples will append and flatten
    }

    @Override
    public String toString(){
        return this.name + ": " + this.tuple.stream().map(s->s.getValue().toString()).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj){
        /**
         * compare whether two TypeWrapper instance is equal
         * @return:
         *   true:  this equals obj
         *  flase:  not equal
         * @params:
         *    obj:  input object for comparison
         * */
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
