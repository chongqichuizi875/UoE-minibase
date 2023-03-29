package ed.inf.adbs.minibase.structures;

import base.IntegerConstant;
import base.StringConstant;
import base.Variable;

public class TypeWrapper {
    private boolean type; // true->string  false->int
    private String str;
    private int num;
    public TypeWrapper(String str){
        this.type = true;
        this.str = str;
    }
    public TypeWrapper(int num){
        this.type = false;
        this.num = num;
    }
    public <E> TypeWrapper(E value){
        if (value instanceof Variable){
            this.type = true;
            this.str = value.toString();
        }
        else if(value instanceof StringConstant){
            this.type = true;
            this.str = value.toString();
        }
        else if(value instanceof IntegerConstant){
            this.type = false;
            this.num = ((IntegerConstant) value).getValue();
        }
    }
    // use a flag and a String to initialize
    public TypeWrapper(boolean is_string, String str){
        this.type = is_string;
        if (is_string) this.str = str;
        else this.num = Integer.parseInt(str);
    }

    public boolean isString(){
        return this.type;
    }

    public Object getValue(){
        if (this.isString()) return this.str;
        return this.num;
    }

    @Override
    public boolean equals(Object obj){
        // obj is a TypeWrapper
        if (obj instanceof TypeWrapper) return this.getValue().equals(((TypeWrapper) obj).getValue());
        //
        else return this.getValue().equals(obj);
    }

    @Override
    public int hashCode(){
        return 17*31 + this.getValue().hashCode();
    }

    @Override
    public String toString(){
        if (this.isString()) return this.str;
        return Integer.toString(this.num);
    }

    public int compareTo(Object obj){
        return (this.toString().compareTo(obj.toString()));
    }
    public boolean isGreaterThan(Object obj){
        if (obj instanceof TypeWrapper){
            if (this.isString()) return (this.compareTo(obj)>0);
            else return (this.num > ((TypeWrapper) obj).num);
        }
        else {
            if (this.isString()) return (this.toString().compareTo((String) obj)>0);
            else return (this.num > (int) obj);
        }
    }
    public boolean isLessThan(Object obj){
        if (obj instanceof TypeWrapper){
            if (this.isString()) return (this.compareTo(obj)<0);
            else return (this.num < ((TypeWrapper) obj).num);
        }
        else {
            if (this.isString()) return (this.toString().compareTo((String) obj)<0);
            else return (this.num < (int) obj);
        }
    }
    public boolean isGreaterAndEqual(Object obj){
        return !(this.isLessThan(obj));
    }
    public boolean isLessAndEqual(Object obj){
        return !(this.isGreaterThan(obj));
    }
}
