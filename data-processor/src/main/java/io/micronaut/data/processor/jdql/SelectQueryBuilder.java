package io.micronaut.data.processor.jdql;

import io.micronaut.data.jdql.JDQLParser;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaQuery;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.processor.model.criteria.SourcePersistentEntityCriteriaBuilder;
import io.micronaut.data.processor.model.criteria.SourcePersistentEntityCriteriaQuery;
import io.micronaut.inject.ast.ClassElement;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Selection;

import java.util.List;
import java.util.function.Function;

final class SelectQueryBuilder extends AbstractWhereBuilder {

    PersistentEntityCriteriaQuery<?> build(PersistentEntity rootPersistentEntity,
                                           JDQLParser.Select_statementContext selectStatementContext,
                                           Function<String, ClassElement> classElementResolver,
                                           SourcePersistentEntityCriteriaBuilder criteriaBuilder) {

        SourcePersistentEntityCriteriaQuery<Object> query = criteriaBuilder
            .createQuery(null);
        PersistentEntityRoot<Object> root;
        JDQLParser.From_clauseContext fromClauseContext = selectStatementContext.from_clause();
        if (fromClauseContext != null) {
            String entityName = fromClauseContext.entity_name().getText();
            root = query.from(classElementResolver.apply(entityName));
        } else {
            root = query.from(rootPersistentEntity);
        }
        Predicate predicate = getPredicate(selectStatementContext.where_clause(), root, criteriaBuilder);
        if (predicate != null) {
            query.where(predicate);
        }
        query.orderBy(
            getOrders(selectStatementContext.orderby_clause(), root, criteriaBuilder)
        );
        JDQLParser.Select_clauseContext selectClauseContext = selectStatementContext.select_clause();
        if (selectClauseContext != null) {
            JDQLParser.Select_listContext selectList = selectClauseContext.select_list();
            JDQLParser.Aggregate_expressionContext aggregateExpression = selectList.aggregate_expression();
            if (aggregateExpression != null) {
                query.select(criteriaBuilder.count(root));
            } else {
                query.multiselect(
                    selectList.state_field_path_expression().stream()
                        .<Selection<?>>map(s -> getExpression(s, root, criteriaBuilder))
                        .toList()
                );
            }
        }
        return query;
    }
}
