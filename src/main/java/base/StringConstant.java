package base;

public class StringConstant extends Constant {
    private String value;

    public StringConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof StringConstant) && (((StringConstant) obj).value.equals(this.value));
    }

    @Override
    public int hashCode(){
        return 17*31 + value.hashCode();
    }
}