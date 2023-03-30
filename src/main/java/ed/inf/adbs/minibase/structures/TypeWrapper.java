package ed.inf.adbs.minibase.structures;

import base.IntegerConstant;
import base.StringConstant;
import base.Variable;

public class TypeWrapper {
    /**
     * Used for wrapping a String, Integer, Variable, Term, Constant
     * Aiming at uniforming format when executing operators
     * When comparing wrapped values, original String, Variable, StringConstant will be viewed as String
     * while Integer, IntegerConstant will be viewed as Integer
     *
     * type:  used for indicate whether it is a string or an int, true for string, false for int
     *  str:  the value of this wrapper of viewed as String (type==true)
     *  num:  the value of this wrapper of viewed as Integer
     * */
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
        /**
         * for wrapping the class extending Term
         * use its value to initialize and type detected automatically
         *
         * @params:
         *  value:  value for initialization
         * */
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
        /**
         * Manually initialize using a flag, used for manually
         * wrap the String object as an int or String, like '9'
         *
         * @params:
         *  is_string:  indicate whether the input str is a string or int
         *        str:  the value of the input string
         * */
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
        /**
         * Compares two strings lexicographically.
         * @return:
         *  an int value equals to compareTo() results of this.toString() and the obj.toString()
         * @params:
         *  obj:  inputted obj for String comparison
         * */
        return (this.toString().compareTo(obj.toString()));
    }
    public boolean isGreaterThan(Object obj){
        /**
         * Compares to the obj whether this is larger than obj
         * if the compared obj is a TypeWrapper class, assert obj has the same value type with this
         * (instruction says only int compare to int, string compare to string)
         * if the obj is not a TypeWrapper class, then use String.compareTo to compare
         * @return:
         *   true:  this larger than obj
         *  false:  this is not larger than obj
         * @params:
         *    obj:  inputted obj for value comparison
         * */
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
        /**
         * Compares to the obj whether this is less than obj
         * if the compared obj is a TypeWrapper class, assert obj has the same value type with this
         * (instruction says only int compare to int, string compare to string)
         * if the obj is not a TypeWrapper class, then use String.compareTo to compare
         * @return:
         *   true:  this less than obj
         *  false:  this is not less than obj
         * @params:
         *    obj:  inputted obj for value comparison
         * */
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
        /**
         * Compares to the obj whether this is not larger than obj
         * if the compared obj is a TypeWrapper class, assert obj has the same value type with this
         * (instruction says only int compare to int, string compare to string)
         * if the obj is not a TypeWrapper class, then use String.compareTo to compare
         * return the opposite results as isGreaterThan
         * @return:
         *   true:  this not larger than obj
         *  false:  this is less than obj
         * @params:
         *    obj:  inputted obj for value comparison
         * */
        return !(this.isLessThan(obj));
    }
    public boolean isLessAndEqual(Object obj){
        /**
         * Compares to the obj whether this is not less than obj
         * if the compared obj is a TypeWrapper class, assert obj has the same value type with this
         * (instruction says only int compare to int, string compare to string)
         * if the obj is not a TypeWrapper class, then use String.compareTo to compare
         * return the opposite results as isLessThan
         * @return:
         *   true:  this not less than obj
         *  false:  this is larger than obj
         * @params:
         *    obj:  inputted obj for value comparison
         * */
        return !(this.isGreaterThan(obj));
    }
}
