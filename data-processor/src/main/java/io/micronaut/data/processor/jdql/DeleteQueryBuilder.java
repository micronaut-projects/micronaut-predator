package io.micronaut.data.processor.jdql;

import io.micronaut.data.jdql.JDQLParser;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaDelete;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.processor.model.criteria.SourcePersistentEntityCriteriaBuilder;
import io.micronaut.data.processor.model.criteria.SourcePersistentEntityCriteriaDelete;
import io.micronaut.inject.ast.ClassElement;
import jakarta.persistence.criteria.Predicate;

import java.util.function.Function;

final class DeleteQueryBuilder extends AbstractWhereBuilder {

    PersistentEntityCriteriaDelete<?> build(JDQLParser.Delete_statementContext deleteStatementContext,
                                            Function<String, ClassElement> classElementResolver,
                                            SourcePersistentEntityCriteriaBuilder criteriaBuilder) {

        JDQLParser.From_clauseContext fromClauseContext = deleteStatementContext.from_clause();
        String entityName = fromClauseContext.entity_name().getText();
        JDQLParser.Where_clauseContext whereClauseContext = deleteStatementContext.where_clause();

        SourcePersistentEntityCriteriaDelete<Object> deleteQuery = criteriaBuilder
            .createCriteriaDelete(null);
        PersistentEntityRoot<Object> root = deleteQuery.from(classElementResolver.apply(entityName));
        Predicate predicate = getPredicate(whereClauseContext, root, criteriaBuilder);
        if (predicate != null) {
            deleteQuery.where(predicate);
        }
        return deleteQuery;
    }
}
