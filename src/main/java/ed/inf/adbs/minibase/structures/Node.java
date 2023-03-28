package ed.inf.adbs.minibase.structures;

import base.ComparisonAtom;
import ed.inf.adbs.minibase.evaluator.Operator;

import java.util.List;

public class Node {
    private Operator operator;
    private List<ComparisonAtom> comparisonAtomList;
    private Node left_child, right_child;
    private boolean isleaf, isroot;

    public Node(){};
    public Node(Operator operator){this.operator = operator;}
    public Node(Operator operator, List<ComparisonAtom> comparisonAtomList){
        this.operator = operator;
        this.comparisonAtomList = comparisonAtomList;
    }

    public void addComparisonAtom(ComparisonAtom comparisonAtom){
        comparisonAtomList.add(comparisonAtom);
    }

    public void setLeft_child(Node left_child) {
        this.left_child = left_child;
    }

    public void setRight_child(Node right_child) {
        this.right_child = right_child;
    }

    public void setComparisonAtomList(List<ComparisonAtom> comparisonAtomList) {
        this.comparisonAtomList = comparisonAtomList;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }
    public Operator getOperator(){return operator;}
    public Node getLeft_child(){return left_child;}
    public Node getRight_child(){return right_child;}
    public List<ComparisonAtom> getComparisonAtomList(){return comparisonAtomList;}
}
