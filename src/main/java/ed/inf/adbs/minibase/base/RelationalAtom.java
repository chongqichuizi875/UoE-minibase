package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.List;

public class RelationalAtom extends Atom {
    private String name;

    private List<Term> terms;

    public RelationalAtom(RelationalAtom atom){
        this.name = atom.getName();
        this.terms = atom.getTerms();
    }

    public RelationalAtom(String name, List<Term> terms) {
        this.name = name;
        this.terms = terms;
    }
    public void setName(String name) {this.name = name;}

    public void setTerms(List<Term> terms) {this.terms = terms;}

    public String getName() {
        return name;
    }

    public List<Term> getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return name + "(" + Utils.join(terms, ", ") + ")";
    }


}
