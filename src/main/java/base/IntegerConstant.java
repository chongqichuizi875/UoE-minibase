package base;

public class IntegerConstant extends Constant {
    private Integer value;

    public IntegerConstant(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof IntegerConstant) && (((IntegerConstant) obj).value.equals(this.value));
    }

    @Override
    public int hashCode(){
        return 17*31 + value.hashCode();
    }
}
