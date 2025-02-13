/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.data.processor.visitors.finders.annotated;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCommonAbstractCriteria;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaDelete;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaQuery;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaUpdate;
import io.micronaut.data.model.jpa.criteria.impl.AbstractPersistentEntityCriteriaDelete;
import io.micronaut.data.model.jpa.criteria.impl.AbstractPersistentEntityCriteriaUpdate;
import io.micronaut.data.model.jpa.criteria.impl.QueryResultPersistentEntityCriteriaQuery;
import io.micronaut.data.model.query.builder.QueryBuilder;
import io.micronaut.data.model.query.builder.QueryResult;
import io.micronaut.data.processor.jdql.JakartaDataQueryLanguageBuilder;
import io.micronaut.data.processor.model.SourcePersistentEntity;
import io.micronaut.data.processor.model.criteria.SourcePersistentEntityCriteriaUpdate;
import io.micronaut.data.processor.model.criteria.impl.MethodMatchSourcePersistentEntityCriteriaBuilderImpl;
import io.micronaut.data.processor.visitors.MethodMatchContext;
import io.micronaut.data.processor.visitors.finders.AbstractCriteriaMethodMatch;
import io.micronaut.data.processor.visitors.finders.FindersUtils;
import io.micronaut.data.processor.visitors.finders.MethodMatchInfo;
import io.micronaut.data.processor.visitors.finders.MethodMatcher;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.processing.ProcessingException;

import java.util.List;
import java.util.Optional;

/**
 * The Jakarta Data Query annotation matcher.
 *
 * @author Denis Stepanov
 * @since 4.12
 */
@Internal
public final class JakartaDataQueryAnnotatedMethodMatcher implements MethodMatcher {

    @Override
    public MethodMatch match(MethodMatchContext matchContext) {
        Optional<String> jdqlQuery = matchContext.getMethodElement().stringValue("jakarta.data.repository.Query");
        if (jdqlQuery.isPresent()) {

            PersistentEntityCommonAbstractCriteria criteriaQuery = JakartaDataQueryLanguageBuilder.build(
                jdqlQuery.get(),
                matchContext.getRootEntity(),
                matchContext.getMethodElement(),
                name -> {
                    SourcePersistentEntity rootEntity = matchContext.getRootEntity();
                    if (rootEntity.getSimpleName().equals(name)) {
                        return rootEntity.getClassElement();
                    }
                    SourcePersistentEntity persistentEntity = matchContext.getEntityBySimplyNameResolver().apply(name);
                    if (persistentEntity != null) {
                        return persistentEntity.getClassElement();
                    }
                    return matchContext.getVisitorContext().getClassElement(name)
                        .orElseThrow(() -> new ProcessingException(matchContext.getMethodElement(), "Unable to find an entity: " + name));
                },
                new MethodMatchSourcePersistentEntityCriteriaBuilderImpl(matchContext)
            );

            if (criteriaQuery instanceof PersistentEntityCriteriaUpdate<?>) {
                return new AbstractCriteriaMethodMatch(List.of()) {
                    @Override
                    protected DataMethod.OperationType getOperationType() {
                        return DataMethod.OperationType.UPDATE;
                    }

                    @Override
                    protected MethodMatchInfo build(MethodMatchContext matchContext) {
                        FindersUtils.InterceptorMatch interceptorMatch = resolveReturnTypeAndInterceptor(matchContext);
                        ClassElement resultType = interceptorMatch.returnType();
                        boolean isDto = false;
                        ClassElement interceptorType = interceptorMatch.interceptor();

                        SourcePersistentEntityCriteriaUpdate<?> criteriaUpdate = (SourcePersistentEntityCriteriaUpdate<?>) criteriaQuery;

//                        MethodResult result = analyzeMethodResult(
//                            matchContext,
//                            criteriaUpdate.getQueryResultTypeName(),
//                            matchContext.getVisitorContext().getClassElement(Long.class).orElseThrow(), // Default result type
//                            interceptorMatch,
//                            true
//                        );
//                        resultType = result.resultType();
//                        isDto = resultType.isInterface();

//                        if (result.isDto() && !result.isRuntimeDtoConversion()) {
//                            List<SourcePersistentProperty> dtoProjectionProperties = getDtoProjectionProperties(matchContext.getRootEntity(), resultType);
//                            if (!dtoProjectionProperties.isEmpty()) {
//                                List<Selection<?>> selectionList = dtoProjectionProperties.stream()
//                                    .map(p -> {
//                                        if (matchContext.getQueryBuilder().shouldAliasProjections()) {
//                                            return root.get(p.getName()).alias(p.getName());
//                                        } else {
//                                            return root.get(p.getName());
//                                        }
//                                    })
//                                    .collect(Collectors.toList());
//                                criteriaUpdate.returningMulti(
//                                    selectionList
//                                );
//                            }
//                        }

                        AbstractPersistentEntityCriteriaUpdate<?> query = (AbstractPersistentEntityCriteriaUpdate<?>) criteriaQuery;

                        boolean optimisticLock = query.hasVersionRestriction();

                        final AnnotationMetadataHierarchy annotationMetadataHierarchy = new AnnotationMetadataHierarchy(
                            matchContext.getRepositoryClass().getAnnotationMetadata(),
                            matchContext.getAnnotationMetadata()
                        );
                        QueryBuilder queryBuilder = matchContext.getQueryBuilder();

                        QueryResult queryResult = ((QueryResultPersistentEntityCriteriaQuery) criteriaQuery).buildQuery(annotationMetadataHierarchy, queryBuilder);

                        return new MethodMatchInfo(
                            getOperationType(),
                            resultType,
                            interceptorType
                        )
                            .dto(isDto)
                            .optimisticLock(optimisticLock)
                            .queryResult(queryResult);
                    }
                };
            }
            if (criteriaQuery instanceof PersistentEntityCriteriaDelete<?>) {
                return new AbstractCriteriaMethodMatch(List.of()) {

                    @Override
                    protected DataMethod.OperationType getOperationType() {
                        return DataMethod.OperationType.DELETE;
                    }

                    @Override
                    protected MethodMatchInfo build(MethodMatchContext matchContext) {
                        FindersUtils.InterceptorMatch interceptorMatch = resolveReturnTypeAndInterceptor(matchContext);
                        ClassElement resultType = interceptorMatch.returnType();
                        ClassElement interceptorType = interceptorMatch.interceptor();

                        boolean optimisticLock = ((AbstractPersistentEntityCriteriaDelete<?>) criteriaQuery).hasVersionRestriction();

                        final AnnotationMetadataHierarchy annotationMetadataHierarchy = new AnnotationMetadataHierarchy(
                            matchContext.getRepositoryClass().getAnnotationMetadata(),
                            matchContext.getAnnotationMetadata()
                        );

//                        AbstractCriteriaMethodMatch.MethodResult result = analyzeMethodResult(
//                            matchContext,
//                            resultType,
//                            interceptorMatch,
//                            true
//                        );

//                        if (result.isDto() && !result.isRuntimeDtoConversion()) {
//                            List<SourcePersistentProperty> dtoProjectionProperties = getDtoProjectionProperties(matchContext.getRootEntity(), resultType);
//                            if (!dtoProjectionProperties.isEmpty()) {
//                                List<Selection<?>> selectionList = dtoProjectionProperties.stream()
//                                    .map(p -> {
//                                        if (matchContext.getQueryBuilder().shouldAliasProjections()) {
//                                            return root.get(p.getName()).alias(p.getName());
//                                        } else {
//                                            return root.get(p.getName());
//                                        }
//                                    })
//                                    .collect(Collectors.toList());
//                                criteriaQuery.returningMulti(
//                                    selectionList
//                                );
//                            }
//                        }

                        QueryBuilder queryBuilder = matchContext.getQueryBuilder();
                        QueryResult queryResult = ((QueryResultPersistentEntityCriteriaQuery) criteriaQuery).buildQuery(annotationMetadataHierarchy, queryBuilder);

                        return new MethodMatchInfo(
                            getOperationType(),
                            resultType,
                            interceptorType
                        )
                            .optimisticLock(optimisticLock)
                            .queryResult(queryResult);
                    }
                };
            }
            if (criteriaQuery instanceof PersistentEntityCriteriaQuery<?>) {
                return new AbstractCriteriaMethodMatch(List.of()) {

                    @Override
                    protected DataMethod.OperationType getOperationType() {
                        return DataMethod.OperationType.QUERY;
                    }

                    @Override
                    protected MethodMatchInfo build(MethodMatchContext matchContext) {
                        FindersUtils.InterceptorMatch interceptorMatch = resolveReturnTypeAndInterceptor(matchContext);
                        ClassElement resultType = interceptorMatch.returnType();
                        ClassElement interceptorType = interceptorMatch.interceptor();

                        final AnnotationMetadataHierarchy annotationMetadataHierarchy = new AnnotationMetadataHierarchy(
                            matchContext.getRepositoryClass().getAnnotationMetadata(),
                            matchContext.getAnnotationMetadata()
                        );

//                        AbstractCriteriaMethodMatch.MethodResult result = analyzeMethodResult(
//                            matchContext,
//                            resultType,
//                            interceptorMatch,
//                            true
//                        );

//                        if (result.isDto() && !result.isRuntimeDtoConversion()) {
//                            List<SourcePersistentProperty> dtoProjectionProperties = getDtoProjectionProperties(matchContext.getRootEntity(), resultType);
//                            if (!dtoProjectionProperties.isEmpty()) {
//                                List<Selection<?>> selectionList = dtoProjectionProperties.stream()
//                                    .map(p -> {
//                                        if (matchContext.getQueryBuilder().shouldAliasProjections()) {
//                                            return root.get(p.getName()).alias(p.getName());
//                                        } else {
//                                            return root.get(p.getName());
//                                        }
//                                    })
//                                    .collect(Collectors.toList());
//                                criteriaQuery.returningMulti(
//                                    selectionList
//                                );
//                            }
//                        }

                        QueryBuilder queryBuilder = matchContext.getQueryBuilder();
                        QueryResult queryResult = ((QueryResultPersistentEntityCriteriaQuery) criteriaQuery).buildQuery(annotationMetadataHierarchy, queryBuilder);

                        return new MethodMatchInfo(
                            getOperationType(),
                            resultType,
                            interceptorType
                        )
                            .queryResult(queryResult);
                    }
                };
            }
            return null;
        }
        return null;
    }

    @Override
    public int getOrder() {
        return MethodMatcher.DEFAULT_POSITION - 3000;
    }
}
