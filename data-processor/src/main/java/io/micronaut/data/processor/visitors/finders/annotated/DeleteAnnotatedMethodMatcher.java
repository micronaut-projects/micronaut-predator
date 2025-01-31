package io.micronaut.data.processor.visitors.finders.annotated;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.annotation.Delete;
import io.micronaut.data.processor.model.SourcePersistentEntity;
import io.micronaut.data.processor.visitors.MatchFailedException;
import io.micronaut.data.processor.visitors.MethodMatchContext;
import io.micronaut.data.processor.visitors.finders.MethodMatcher;
import io.micronaut.data.processor.visitors.finders.TypeUtils;
import io.micronaut.data.processor.visitors.finders.criteria.DeleteCriteriaMethodMatch;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.processing.ProcessingException;

import java.util.Arrays;
import java.util.List;

@Internal
public final class DeleteAnnotatedMethodMatcher implements MethodMatcher {

    @Override
    public MethodMatch match(MethodMatchContext matchContext) {
        if (matchContext.getMethodElement().hasStereotype(Delete.class)) {
            if (matchContext.getRootEntity() == null) {
                throw new ProcessingException(matchContext.getMethodElement(), "Repository does not have a well-defined primary entity type");
            }
            ParameterElement[] parameters = matchContext.getParameters();
            ParameterElement entityParameter = null;
            ParameterElement entitiesParameter = null;
            if (matchContext.getParametersNotInRole().size() == 1) {
                entityParameter = Arrays.stream(parameters).filter(p -> TypeUtils.isEntity(p.getGenericType())).findFirst().orElse(null);
                entitiesParameter = Arrays.stream(parameters).filter(p -> TypeUtils.isIterableOfEntity(p.getGenericType())).findFirst().orElse(null);
            }

            SourcePersistentEntity rootEntity = matchContext.getRootEntity();
            if (!rootEntity.hasIdentity() && !rootEntity.hasCompositeIdentity()) {
                throw new MatchFailedException("Delete all not supported for entities with no ID");
            }

            if (entityParameter == null && entitiesParameter == null && parameters.length != 0) {
                return new DeleteCriteriaMethodMatch(List.of(), false);
            }

            ParameterElement finalEntityParameter = entityParameter;
            ParameterElement finalEntitiesParameter = entitiesParameter;

            if (finalEntitiesParameter != null || finalEntityParameter != null) {
                return new DeleteCriteriaMethodMatch(List.of(), false) {

                    @Override
                    protected boolean supportedByImplicitQueries() {
                        return true;
                    }

                    @Override
                    protected ParameterElement getEntityParameter() {
                        return finalEntityParameter;
                    }

                    @Override
                    protected ParameterElement getEntitiesParameter() {
                        return finalEntitiesParameter;
                    }

                };
            }
            throw new ProcessingException(matchContext.getMethodElement(), "Delete method should include an entity to update");
        }
        return null;
    }

    @Override
    public int getOrder() {
        return MethodMatcher.DEFAULT_POSITION - 3000;
    }
}
