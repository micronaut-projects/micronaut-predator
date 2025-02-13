package io.micronaut.data.processor.jdql;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.jdql.JDQLParser;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCommonAbstractCriteria;
import io.micronaut.data.processor.model.criteria.SourcePersistentEntityCriteriaBuilder;
import io.micronaut.data.processor.visitors.MatchFailedException;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.Element;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.BitSet;
import java.util.function.Function;

public class JakartaDataQueryLanguageBuilder {

    public static PersistentEntityCommonAbstractCriteria build(String query,
                                                               @Nullable
                                                               PersistentEntity rootPersistentEntity,
                                                               @Nullable Element originatingElement,
                                                               Function<String, ClassElement> classElementResolver,
                                                               SourcePersistentEntityCriteriaBuilder criteriaBuilder) {

        var inputStream = new ANTLRInputStream(query);
        var lexer = new io.micronaut.data.jdql.JDQLLexer(inputStream);
        ANTLRErrorListener errorListener = new ANTLRErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new MatchFailedException("Failed to parse Jakarta Data query: " + prettifyAntlrError(offendingSymbol, line, charPositionInLine, msg, e, query, true), originatingElement);
            }

            @Override
            public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
            }

            @Override
            public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
            }

            @Override
            public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
            }
        };
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new io.micronaut.data.jdql.JDQLParser(tokenStream);
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);
        io.micronaut.data.jdql.JDQLParser.StatementContext statement = parser.statement();
        ParseTree child = statement.getChild(0);
        if (child instanceof io.micronaut.data.jdql.JDQLParser.Delete_statementContext deleteStatementContext) {
            return new DeleteQueryBuilder()
                .build(deleteStatementContext, classElementResolver, criteriaBuilder);
        }
        if (child instanceof JDQLParser.Update_statementContext updateStatementContext) {
            return new UpdateQueryBuilder()
                .build(updateStatementContext, classElementResolver, criteriaBuilder);
        }
        if (child instanceof JDQLParser.Select_statementContext select_clauseContext) {
            return new SelectQueryBuilder()
                .build(rootPersistentEntity, select_clauseContext, classElementResolver, criteriaBuilder);
        }

        throw new MatchFailedException("Unrecognized query: " + statement, originatingElement);
    }

    // Copied from Hibernate
    private static String prettifyAntlrError(Object offendingSymbol,
                                             int line, int charPositionInLine,
                                             String message,
                                             RecognitionException e,
                                             String hql,
                                             boolean includeLocation) {
        String errorText = "";
        if (includeLocation) {
            errorText += "At " + line + ":" + charPositionInLine;
            if (offendingSymbol instanceof CommonToken commonToken) {
                String token = commonToken.getText();
                if (token != null && !token.isEmpty()) {
                    errorText += " and token '" + token + "'";
                }
            }
            errorText += ", ";
        }
        if (e instanceof NoViableAltException) {
            errorText += message.substring(0, message.indexOf('\''));
            if (hql.isEmpty()) {
                errorText += "'*' (empty query string)";
            } else {
                String lineText = hql.lines().toList().get(line - 1);
                String text = lineText.substring(0, charPositionInLine) + "*" + lineText.substring(charPositionInLine);
                errorText += "'" + text + "'";
            }
        } else if (e instanceof InputMismatchException) {
            errorText += message.substring(0, message.length() - 1)
                .replace(" expecting {", ", expecting one of the following tokens: ");
        } else {
            errorText += message;
        }
        return errorText;
    }

}
