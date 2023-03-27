package base;

public class Variable extends Term {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){ this.name = name;}

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return ((obj instanceof Variable) && (this.name.equals( obj.toString())));
    }

    @Override
    public int hashCode(){
        return 17*31 + this.name.hashCode();
    }



}
