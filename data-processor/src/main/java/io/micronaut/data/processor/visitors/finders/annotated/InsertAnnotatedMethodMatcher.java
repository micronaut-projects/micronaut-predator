package io.micronaut.data.processor.visitors.finders.annotated;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.annotation.Insert;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.processor.visitors.MethodMatchContext;
import io.micronaut.data.processor.visitors.finders.MethodMatcher;
import io.micronaut.data.processor.visitors.finders.SaveMethodMatcher;
import io.micronaut.data.processor.visitors.finders.TypeUtils;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.processing.ProcessingException;

@Internal
public final class InsertAnnotatedMethodMatcher implements MethodMatcher {

    @Override
    public MethodMatch match(MethodMatchContext matchContext) {
        if (matchContext.getMethodElement().hasStereotype(Insert.class)) {
            if (matchContext.getRootEntity() == null) {
                throw new ProcessingException(matchContext.getMethodElement(), "Repository does not have a well-defined primary entity type");
            }
            MethodElement methodElement = matchContext.getMethodElement();
            boolean producesAnEntity = TypeUtils.doesMethodProducesAnEntityIterableOfAnEntity(methodElement);
            if (!TypeUtils.doesReturnVoid(methodElement)
                && !TypeUtils.doesMethodProducesANumber(methodElement)
                && !producesAnEntity) {
                ClassElement producingItem = TypeUtils.getMethodProducingItemType(methodElement);
                throw new ProcessingException(methodElement, "Unsupported return type for a save method: " + producingItem.getName());
            }
            return SaveMethodMatcher.saveEntity(DataMethod.OperationType.INSERT);
        }
        return null;
    }

    @Override
    public int getOrder() {
        return MethodMatcher.DEFAULT_POSITION - 3000;
    }
}
