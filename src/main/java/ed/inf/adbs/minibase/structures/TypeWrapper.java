package ed.inf.adbs.minibase.structures;

import com.sun.org.apache.xml.internal.utils.res.IntArrayWrapper;

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
}
