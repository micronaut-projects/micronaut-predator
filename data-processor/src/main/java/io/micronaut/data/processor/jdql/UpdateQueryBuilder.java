package io.micronaut.data.processor.jdql;

import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaBuilder;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaUpdate;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.processor.model.criteria.SourcePersistentEntityCriteriaBuilder;
import io.micronaut.data.processor.model.criteria.SourcePersistentEntityCriteriaUpdate;
import io.micronaut.inject.ast.ClassElement;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.function.Function;

import static io.micronaut.data.jdql.JDQLParser.Update_itemContext;
import static io.micronaut.data.jdql.JDQLParser.Update_statementContext;
import static io.micronaut.data.jdql.JDQLParser.Where_clauseContext;

final class UpdateQueryBuilder extends AbstractWhereBuilder {

    private PersistentEntityCriteriaBuilder criteriaBuilder;
    private PersistentEntityCriteriaUpdate<?> updateQuery;
    private PersistentEntityRoot<Object> root;

    PersistentEntityCriteriaUpdate<?> build(Update_statementContext updateStatementContext,
                                            Function<String, ClassElement> classElementResolver,
                                            SourcePersistentEntityCriteriaBuilder criteriaBuilder) {
        String entityName = updateStatementContext.entity_name().getText();

        Where_clauseContext whereClauseContext = updateStatementContext.where_clause();

        SourcePersistentEntityCriteriaUpdate<Object> updateQuery = criteriaBuilder
            .createCriteriaUpdate(null);
        this.root = updateQuery.from(classElementResolver.apply(entityName));
        Predicate predicate = getPredicate(whereClauseContext, root, criteriaBuilder);
        if (predicate != null) {
            updateQuery.where(predicate);
        }
        this.updateQuery = updateQuery;
        this.criteriaBuilder = criteriaBuilder;

        ParseTreeWalker.DEFAULT.walk(this, updateStatementContext);

        return updateQuery;
    }

    @Override
    public void exitUpdate_item(Update_itemContext ctx) {
        String name = ctx.state_field_path_expression().getText();
//        if(isArithmeticOperation(scalarContext)) {
//            throw new UnsupportedOperationException("Eclipse JNoSQL does not support arithmetic operations in the UPDATE clause: " + scalarContext.getText());
//        }
//        if(hasParenthesis(scalarContext)) {
//            throw new UnsupportedOperationException("Eclipse JNoSQL does not support parenthesis in the UPDATE clause: " + scalarContext.getText());
//        }
        Expression<?> expression = getExpression(ctx.scalar_expression(), root, criteriaBuilder);
        updateQuery.set(name, expression);
    }

}
