package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.List;

public class Head {
    private String name;

    private List<Variable> variables;

    private SumAggregate agg;

    public Head(String name, List<Variable> variables, SumAggregate agg) {
        this.name = name;
        this.variables = variables;
        this.agg = agg;
    }

    public String getName() {
        return name;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public SumAggregate getSumAggregate() {
        return agg;
    }

    @Override
    public String toString() {
        if (agg == null) {
            return name + "(" + Utils.join(variables, ", ") + ")";
        }
        if (variables.isEmpty()) {
            return name + "(" + agg + ")";
        }
        return name + "(" + Utils.join(variables, ", ") + ", " + agg + ")";
    }
}
