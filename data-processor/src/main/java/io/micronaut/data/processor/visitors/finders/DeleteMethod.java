/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.data.processor.visitors.finders;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.annotation.TypeRole;
import io.micronaut.data.intercept.DataInterceptor;
import io.micronaut.data.model.Embedded;
import io.micronaut.data.model.query.QueryModel;
import io.micronaut.data.model.query.QueryParameter;
import io.micronaut.data.processor.model.SourcePersistentEntity;
import io.micronaut.data.processor.model.SourcePersistentProperty;
import io.micronaut.data.processor.visitors.MatchContext;
import io.micronaut.data.processor.visitors.MethodMatchContext;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Support for simple delete operations.
 *
 * @author graemerocher
 * @since 1.0.0
 */
public class DeleteMethod extends AbstractListMethod {

    public static final Pattern METHOD_PATTERN = Pattern.compile("^((delete|remove|erase|eliminate)(\\S*?))$");

    /**
     * Default constructor.
     */
    public DeleteMethod() {
        super("delete", "remove", "erase", "eliminate");
    }

    @Override
    public final int getOrder() {
        // lower priority than dynamic finder
        return DEFAULT_POSITION + 100;
    }

    @Override
    public boolean isMethodMatch(MethodElement methodElement, MatchContext matchContext) {
        return super.isMethodMatch(methodElement, matchContext) && TypeUtils.isValidBatchUpdateReturnType(methodElement);
    }

    @NonNull
    @Override
    protected MethodMatchInfo.OperationType getOperationType() {
        return MethodMatchInfo.OperationType.DELETE;
    }

    @Nullable
    @Override
    public MethodMatchInfo buildMatchInfo(@NonNull MethodMatchContext matchContext) {
        ParameterElement[] parameters = matchContext.getParameters();
        ParameterElement entityParameter = Arrays.stream(parameters).filter(p -> TypeUtils.isEntity(p.getGenericType())).findFirst().orElse(null);
        ParameterElement entitiesParameter = Arrays.stream(parameters).filter(p -> TypeUtils.isIterableOfEntity(p.getGenericType())).findFirst().orElse(null);
        if (entityParameter == null && entitiesParameter == null) {
            // Delete all
            return super.buildMatchInfo(matchContext);
        }

        SourcePersistentEntity rootEntity = matchContext.getRootEntity();
        Map.Entry<ClassElement, Class<? extends DataInterceptor>> entry = FindersUtils.resolveInterceptorTypeByOperationType(
                entityParameter != null,
                entitiesParameter != null,
                MethodMatchInfo.OperationType.DELETE,
                matchContext);
        ClassElement resultType = entry.getKey();
        Class<? extends DataInterceptor> interceptorType = entry.getValue();
        MethodMatchInfo methodMatchInfo;
        if (matchContext.supportsImplicitQueries()) {
            methodMatchInfo = new MethodMatchInfo(
                    resultType,
                    null,
                    getInterceptorElement(matchContext, interceptorType),
                    getOperationType()
            );
        } else {
            QueryModel queryModel = QueryModel.from(rootEntity);
            if (!rootEntity.hasIdentity() && !rootEntity.hasCompositeIdentity()) {
                matchContext.fail("Delete all not supported for entities with no ID");
                return null;
            }
            QueryParameter queryParameter = new QueryParameter(entitiesParameter == null ? entityParameter.getName() : entitiesParameter.getName());
            SourcePersistentProperty identity = rootEntity.getIdentity();
            if (rootEntity.hasCompositeIdentity() || identity instanceof Embedded) {
                queryModel.idEq(queryParameter);
            } else if (identity != null && interceptorType.getSimpleName().startsWith("DeleteAll")) {
                queryModel.inList(identity.getName(), queryParameter);
            }
            methodMatchInfo = new MethodMatchInfo(
                    resultType,
                    queryModel,
                    getInterceptorElement(matchContext, interceptorType),
                    getOperationType()
            );
        }
        if (entityParameter != null) {
            methodMatchInfo.addParameterRole(TypeRole.ENTITY, entityParameter.getName());
        } else {
            methodMatchInfo.addParameterRole(TypeRole.ENTITIES, entitiesParameter.getName());
        }
        return methodMatchInfo;
    }

    @Nullable
    @Override
    protected MethodMatchInfo buildInfo(@NonNull MethodMatchContext matchContext, @NonNull ClassElement queryResultType, @Nullable QueryModel query) {
        Map.Entry<ClassElement, Class<? extends DataInterceptor>> entry = FindersUtils.pickDeleteAllInterceptor(matchContext, matchContext.getReturnType());
        Class<? extends DataInterceptor> interceptor = entry.getValue();
        if (query != null) {
            return new MethodMatchInfo(
                    entry.getKey(),
                    query,
                    getInterceptorElement(matchContext, interceptor),
                    getOperationType()
            );
        } else {
            return new MethodMatchInfo(
                    entry.getKey(),
                    matchContext.supportsImplicitQueries() ? null : QueryModel.from(matchContext.getRootEntity()),
                    getInterceptorElement(matchContext, interceptor),
                    getOperationType()
            );
        }
    }

}
