package base;

public class ComparisonAtom extends Atom {

    private Term term1;

    private Term term2;

    private ComparisonOperator op;

    public ComparisonAtom(Term term1, Term term2, ComparisonOperator op) {
        this.term1 = term1;
        this.term2 = term2;
        this.op = op;
    }

    public Term getTerm1() {
        return term1;
    }

    public Term getTerm2() {
        return term2;
    }

    public ComparisonOperator getOp() {
        return op;
    }

    @Override
    public String toString() {
        return term1 + " " + op + " " + term2;
    }

    @Override
    public boolean equals(Object obj){
        return (obj instanceof ComparisonAtom) &&
                (term1.getClass().equals(((ComparisonAtom) obj).getTerm1().getClass())) &&
                (term2.getClass().equals(((ComparisonAtom) obj).getTerm2().getClass())) &&
                (term1.toString().equals(((ComparisonAtom) obj).getTerm1().toString())) &&
                (term2.toString().equals(((ComparisonAtom) obj).getTerm2().toString())) &&
                (op.toString().equals(((ComparisonAtom) obj).getOp().toString()));
    }

    @Override
    public int hashCode(){
        return 17*31 + this.toString().hashCode();
    }

}
