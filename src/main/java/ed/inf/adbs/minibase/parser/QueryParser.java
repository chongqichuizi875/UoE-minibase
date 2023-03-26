package ed.inf.adbs.minibase.parser;

import ed.inf.adbs.minibase.parser.generated.MinibaseBaseVisitor;
import ed.inf.adbs.minibase.parser.generated.MinibaseLexer;
import ed.inf.adbs.minibase.parser.generated.MinibaseParser;
import ed.inf.adbs.minibase.base.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class QueryParser {

    public static Query parse(Path path) throws IOException {
        String content = new String(Files.readAllBytes(path));
        return parse(content);
    }

    public static Query parse(String input) {
        CharStream charStream = CharStreams.fromString(input);
        MinibaseLexer lexer = new MinibaseLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        MinibaseParser parser = new MinibaseParser(tokens);

        QueryVisitor queryVisitor = new QueryVisitor();
        return parser.query().accept(queryVisitor);
    }

    private static class QueryVisitor extends MinibaseBaseVisitor<Query> {
        @Override
        public Query visitQuery(MinibaseParser.QueryContext ctx) {
            AtomVisitor atomVisitor = new AtomVisitor();
            List<Atom> body = ctx.body().atom()
                    .stream()
                    .map(a -> a.accept(atomVisitor))
                    .collect(toList());

            String headName = ctx.head().ID_UPPER().getText();
            VariableVisitor variableVisitor = new VariableVisitor();
            List<Variable> headVariables = ctx.head().variable()
                    .stream()
                    .map(v -> v.accept(variableVisitor))
                    .collect(toList());
            
            SumAggregate headSumAggregate = null;
            if (ctx.head().sumagg() != null) {                
                TermVisitor termVisitor = new TermVisitor();
                List<Term> productTerms = ctx.head().sumagg().term()
                        .stream()
                        .map(v -> v.accept(termVisitor))
                        .collect(toList());
                headSumAggregate = new SumAggregate(productTerms);
            }

            Head head = new Head(headName, headVariables, headSumAggregate);

            return new Query(head, body);
        }
    }

    private static class AtomVisitor extends MinibaseBaseVisitor<Atom> {
        @Override
        public Atom visitAtom(MinibaseParser.AtomContext ctx) {
            if (ctx.relationalAtom() != null) {
                return ctx.relationalAtom().accept(new RelationalAtomVisitor());
            }
            if (ctx.comparisonAtom() != null) {
                return ctx.comparisonAtom().accept(new ComparisonAtomVisitor());
            }
            return null;
        }
    }

    private static class RelationalAtomVisitor extends MinibaseBaseVisitor<RelationalAtom> {
        @Override
        public RelationalAtom visitRelationalAtom(MinibaseParser.RelationalAtomContext ctx) {
            String atomName = ctx.ID_UPPER().getText();
            TermVisitor termVisitor = new TermVisitor();
            List<Term> terms = ctx.term()
                    .stream()
                    .map(t -> t.accept(termVisitor))
                    .collect(toList());
            return new RelationalAtom(atomName, terms);
        }
    }

    private static class ComparisonAtomVisitor extends MinibaseBaseVisitor<ComparisonAtom> {
        @Override
        public ComparisonAtom visitComparisonAtom(MinibaseParser.ComparisonAtomContext ctx) {
            TermVisitor termVisitor = new TermVisitor();
            Term term1 = ctx.term(0).accept(termVisitor);
            Term term2 = ctx.term(1).accept(termVisitor);
            ComparisonOperator op = ComparisonOperator.fromString(ctx.cmpOp().getText());
            return new ComparisonAtom(term1, term2, op);
        }
    }

    private static class TermVisitor extends MinibaseBaseVisitor<Term> {
        @Override
        public Term visitTerm(MinibaseParser.TermContext ctx) {
            if (ctx.variable() != null) {
                return ctx.variable().accept(new VariableVisitor());
            }
            if (ctx.constant() != null) {
                return ctx.constant().accept(new ConstantVisitor());
            }
            return null;
        }
    }

    private static class VariableVisitor extends MinibaseBaseVisitor<Variable> {
        @Override
        public Variable visitVariable(MinibaseParser.VariableContext ctx) {
            String variableName = ctx.ID_LOWER().getText();
            return new Variable(variableName);
        }
    }

    private static class ConstantVisitor extends MinibaseBaseVisitor<Constant> {
        @Override
        public Constant visitConstant(MinibaseParser.ConstantContext ctx) {
            if (ctx.INT() != null) {
                String text = ctx.INT().getText();
                return new IntegerConstant(Integer.valueOf(text));
            }
            if (ctx.STRING() != null) {
                String text = ctx.STRING().getText();
                String unquotedText = text.substring(1, text.length() - 1);
                return new StringConstant(unquotedText);
            }
            return null;
        }
    }

}
