package base;

import ed.inf.adbs.minibase.Utils;

import java.util.List;
import java.util.stream.Collectors;

public class Query {
    private Head head;

    private List<Atom> body;

    public <T> Query(Head head, List<T> body){
        this.head = head;
        this.body = body.stream()
                .map(s->(Atom) s).collect(Collectors.toList());
    }

    public Head getHead() {
        return head;
    }

    public List<Atom> getBody() {
        return body;
    }

    @Override
    public String toString() {
        return head + " :- " + Utils.join(body, ", ");
    }
}
