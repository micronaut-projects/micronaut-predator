package io.micronaut.data.processor.visitors.finders.annotated;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.annotation.Update;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.processor.visitors.MethodMatchContext;
import io.micronaut.data.processor.visitors.finders.MethodMatcher;
import io.micronaut.data.processor.visitors.finders.SaveMethodMatcher;
import io.micronaut.data.processor.visitors.finders.TypeUtils;
import io.micronaut.data.processor.visitors.finders.UpdateMethodMatcher;
import io.micronaut.data.processor.visitors.finders.criteria.DeleteCriteriaMethodMatch;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.processing.ProcessingException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Internal
public final class UpdateAnnotatedMethodMatcher implements MethodMatcher {

    @Override
    public MethodMatch match(MethodMatchContext matchContext) {
        if (matchContext.getMethodElement().hasStereotype(Update.class)) {
            if (matchContext.getRootEntity() == null) {
                throw new ProcessingException(matchContext.getMethodElement(), "Repository does not have a well-defined primary entity type");
            }
            ParameterElement[] parameters = matchContext.getParameters();
            final ParameterElement entityParameter = Arrays.stream(parameters).filter(p -> TypeUtils.isEntity(p.getGenericType())).findFirst().orElse(null);
            final ParameterElement entitiesParameter = Arrays.stream(parameters).filter(p -> TypeUtils.isIterableOfEntity(p.getGenericType())).findFirst().orElse(null);

            if (entityParameter != null || entitiesParameter != null) {
                return UpdateMethodMatcher.entityUpdate(Collections.emptyList(), entityParameter, entitiesParameter, false);
            }
            throw new ProcessingException(matchContext.getMethodElement(), "Update method should include an entity to update");
        }
        return null;
    }

    @Override
    public int getOrder() {
        return MethodMatcher.DEFAULT_POSITION - 3000;
    }
}
