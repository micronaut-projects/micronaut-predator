package io.micronaut.data.processor.jdql;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.jdql.JDQLParser;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaBuilder;
import io.micronaut.data.processor.model.criteria.SourcePersistentEntityCriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

abstract sealed class AbstractWhereBuilder extends io.micronaut.data.jdql.JDQLBaseListener permits DeleteQueryBuilder, SelectQueryBuilder, UpdateQueryBuilder {

    protected static Predicate getPredicate(@Nullable JDQLParser.Where_clauseContext whereClause,
                                            Root<?> root,
                                            PersistentEntityCriteriaBuilder criteriaBuilder) {
        if (whereClause == null) {
            return null;
        }
        JDQLParser.Conditional_expressionContext conditionalExpression = whereClause.conditional_expression();
        return getPredicate(conditionalExpression, root, criteriaBuilder);
    }

    protected static List<Order> getOrders(@Nullable JDQLParser.Orderby_clauseContext orderByClause,
                                           Root<?> root,
                                           PersistentEntityCriteriaBuilder criteriaBuilder) {
        if (orderByClause == null) {
            return List.of();
        }
        List<JDQLParser.Orderby_itemContext> orderbyItemContexts = orderByClause.orderby_item();
        List<Order> orders = new ArrayList<>(orderbyItemContexts.size());
        for (JDQLParser.Orderby_itemContext orderbyItemContext : orderbyItemContexts) {
            Expression<?> expression = getExpression(orderbyItemContext.state_field_path_expression(), root, criteriaBuilder);
            orders.add(
                orderbyItemContext.DESC() == null ? criteriaBuilder.asc(expression) : criteriaBuilder.desc(expression)
            );
        }
        return orders;
    }

    private static Predicate getPredicate(JDQLParser.Conditional_expressionContext conditionalExpression,
                                          Root<?> root,
                                          PersistentEntityCriteriaBuilder criteriaBuilder) {
        if (conditionalExpression.LPAREN() != null) {
            return getPredicate(conditionalExpression.conditional_expression(0), root, criteriaBuilder);
        }
        if (conditionalExpression.AND() != null) {
            return criteriaBuilder.and(
                getPredicate(conditionalExpression.conditional_expression(0), root, criteriaBuilder),
                getPredicate(conditionalExpression.conditional_expression(1), root, criteriaBuilder)
            );
        }
        if (conditionalExpression.OR() != null) {
            return criteriaBuilder.or(
                getPredicate(conditionalExpression.conditional_expression(0), root, criteriaBuilder),
                getPredicate(conditionalExpression.conditional_expression(1), root, criteriaBuilder)
            );
        }
        if (conditionalExpression.NOT() != null) {
            return criteriaBuilder.not(
                getPredicate(conditionalExpression.conditional_expression(0), root, criteriaBuilder)
            );
        }
        JDQLParser.Comparison_expressionContext comparisonExpression = conditionalExpression.comparison_expression();
        if (comparisonExpression != null) {
            Expression<?> firstExp = getExpression(
                comparisonExpression.scalar_expression(0),
                root,
                criteriaBuilder
            );
            Expression<?> secondExp = getExpression(
                comparisonExpression.scalar_expression(1),
                root,
                criteriaBuilder
            );
            JDQLParser.Comparison_operatorContext comparisonOperator = comparisonExpression.comparison_operator();
            if (comparisonOperator.EQ() != null) {
                return criteriaBuilder.equal(firstExp, secondExp);
            }
            if (comparisonOperator.NEQ() != null) {
                return criteriaBuilder.notEqual(firstExp, secondExp);
            }
            if (comparisonOperator.GT() != null) {
                return criteriaBuilder.greaterThan((Expression) firstExp, (Expression) secondExp);
            }
            if (comparisonOperator.GTEQ() != null) {
                return criteriaBuilder.greaterThanOrEqualTo((Expression) firstExp, (Expression) secondExp);
            }
            if (comparisonOperator.LT() != null) {
                return criteriaBuilder.lessThan((Expression) firstExp, (Expression) secondExp);
            }
            if (comparisonOperator.LTEQ() != null) {
                return criteriaBuilder.lessThanOrEqualTo((Expression) firstExp, (Expression) secondExp);
            }
            throw new IllegalStateException("Unsupported comparison operator: " + comparisonOperator);
        }
        JDQLParser.Like_expressionContext likeExpression = conditionalExpression.like_expression();
        if (likeExpression != null) {
            Expression<String> pattern;
            if (likeExpression.STRING() != null) {
                pattern = criteriaBuilder.literal(
                    getString(
                        likeExpression.getChild(likeExpression.getChildCount() - 1).getText()
                    )
                );
            } else {
                pattern = (Expression<String>) getExpression(likeExpression.input_parameter(), criteriaBuilder);
            }
            Expression<String> expression = (Expression<String>) getExpression(likeExpression.scalar_expression(), root, criteriaBuilder);
            if (likeExpression.NOT() != null) {
                return criteriaBuilder.notLike(expression, pattern);
            }
            return criteriaBuilder.like(expression, pattern);
        }
        JDQLParser.In_expressionContext inExpression = conditionalExpression.in_expression();
        if (inExpression != null) {
            Expression<?> expression = getExpression(inExpression.state_field_path_expression(), root, criteriaBuilder);
            CriteriaBuilder.In<?> in = criteriaBuilder.in(expression);
            for (JDQLParser.In_itemContext item : inExpression.in_item()) {
                Expression e = getExpression(item, criteriaBuilder);
                in.value(e);
            }
            if (inExpression.NOT() != null) {
                return in.not();
            }
            return in;
        }
        JDQLParser.Between_expressionContext betweenExpression = conditionalExpression.between_expression();
        if (betweenExpression != null) {
            Predicate between = criteriaBuilder.between(
                (Expression<String>) getExpression(betweenExpression.scalar_expression(0), root, criteriaBuilder),
                (Expression<String>) getExpression(betweenExpression.scalar_expression(1), root, criteriaBuilder),
                (Expression<String>) getExpression(betweenExpression.scalar_expression(2), root, criteriaBuilder)
            );
            if (betweenExpression.NOT() != null) {
                return between.not();
            }
            return between;
        }
        JDQLParser.Null_comparison_expressionContext nullComparisonExpression = conditionalExpression.null_comparison_expression();
        if (nullComparisonExpression != null) {
            Expression<?> expression = getExpression(nullComparisonExpression.state_field_path_expression(), root, criteriaBuilder);
            if (nullComparisonExpression.NOT() != null) {
                return criteriaBuilder.isNotNull(expression);
            }
            return criteriaBuilder.isNull(expression);
        }
        throw new IllegalStateException("Unsupported conditional expression: " + conditionalExpression);
    }

    private static String getString(String text) {
        return text.substring(1, text.length() - 1);
    }

    protected static Expression<?> getExpression(JDQLParser.Scalar_expressionContext scalarExpression,
                                                 Root<?> root,
                                                 CriteriaBuilder criteriaBuilder) {
        JDQLParser.Primary_expressionContext primaryExpression = scalarExpression.primary_expression();
        if (primaryExpression != null) {
            return getExpression(primaryExpression, root, criteriaBuilder);
        }
        Expression<?> firstExp = getExpression(
            Objects.requireNonNull((JDQLParser.Scalar_expressionContext) scalarExpression.getChild(0), "First expression cannot be null"),
            root,
            criteriaBuilder
        );
        Expression<?> secondExp = getExpression(
            Objects.requireNonNull((JDQLParser.Scalar_expressionContext) scalarExpression.getChild(2), "First expression cannot be null"),
            root,
            criteriaBuilder
        );
        if (scalarExpression.PLUS() != null) {
            return criteriaBuilder.sum((Expression) firstExp, (Expression) secondExp);
        }
        if (scalarExpression.MINUS() != null) {
            return criteriaBuilder.diff((Expression) firstExp, (Expression) secondExp);
        }
        if (scalarExpression.CONCAT() != null) {
            return criteriaBuilder.concat((Expression) firstExp, (Expression) secondExp);
        }
        if (scalarExpression.MUL() != null) {
            return criteriaBuilder.prod((Expression) firstExp, (Expression) secondExp);
        }
        if (scalarExpression.DIV() != null) {
            return criteriaBuilder.quot((Expression) firstExp, (Expression) secondExp);
        }
        throw new IllegalStateException("Unknown primary expression");
    }

    private static Expression<?> getExpression(JDQLParser.Primary_expressionContext context,
                                               Root<?> root,
                                               CriteriaBuilder criteriaBuilder) {
        if (context.literal() != null) {
            return getExpression(context.literal(), criteriaBuilder);
        }
        if (context.input_parameter() != null) {
            return getExpression(context.input_parameter(), criteriaBuilder);
        }
        if (context.special_expression() != null) {
            var specialExpression = context.special_expression().getText();
            return switch (specialExpression.toUpperCase(Locale.US)) {
                case "TRUE" -> criteriaBuilder.literal(true);
                case "FALSE" -> criteriaBuilder.literal(false);
                default ->
                    throw new UnsupportedOperationException("Unsupported special expression: " + specialExpression);
            };
        }
        if (context.enum_literal() != null) {
            return getExpression(context.enum_literal());
        }
        if (context.state_field_path_expression() != null) {
            var stateContext = context.state_field_path_expression();
            return getExpression(stateContext, root, criteriaBuilder);
        }
        JDQLParser.Function_expressionContext functionExpression = context.function_expression();
        if (functionExpression != null) {
            SourcePersistentEntityCriteriaBuilder sourcePersistentEntityCriteriaBuilder = (SourcePersistentEntityCriteriaBuilder) criteriaBuilder;
            Expression expression = getExpression(functionExpression.scalar_expression(0), root, criteriaBuilder);
            String functionName = functionExpression.getChild(0).getText().toLowerCase();
            return switch (functionName) {
                case "abs(" -> criteriaBuilder.abs(expression);
                case "length(" -> criteriaBuilder.length(expression);
                case "lower(" -> criteriaBuilder.lower(expression);
                case "upper(" -> criteriaBuilder.upper(expression);
                case "left(" -> sourcePersistentEntityCriteriaBuilder.startsWithString(
                    expression,
                    (Expression) getExpression(functionExpression.scalar_expression(1), root, criteriaBuilder)
                );
                case "right(" -> sourcePersistentEntityCriteriaBuilder.endingWithString(
                    expression,
                    (Expression) getExpression(functionExpression.scalar_expression(1), root, criteriaBuilder)
                );
                default ->
                    throw new UnsupportedOperationException("Unsupported function expression: " + functionName);
            };
        }
        throw new UnsupportedOperationException("Not supported expression: " + context.getText());
    }

    private static Expression<?> getExpression(JDQLParser.Enum_literalContext enumLiteralContext) {
        throw new UnsupportedOperationException("Unsupported enum: " + enumLiteralContext);
    }

    private static Expression<? extends Serializable> getExpression(JDQLParser.LiteralContext literal, CriteriaBuilder criteriaBuilder) {
        if (literal.STRING() != null) {
            return criteriaBuilder.literal(
                getString(
                    literal.STRING().getText()
                )
            );
        } else if (literal.INTEGER() != null) {
            return criteriaBuilder.literal(Integer.valueOf(literal.INTEGER().getText()));
        } else if (literal.DOUBLE() != null) {
            return criteriaBuilder.literal(Double.valueOf(literal.DOUBLE().getText()));
        } else if (literal.FLOAT() != null) {
            return criteriaBuilder.literal(Float.valueOf(literal.FLOAT().getText()));
        }
        throw new IllegalStateException("Unknown literal parameter: " + literal);
    }

    protected static Expression<?> getExpression(JDQLParser.State_field_path_expressionContext stateFieldPathExpression,
                                               Root<?> root,
                                               CriteriaBuilder criteriaBuilder) {
        var text = stateFieldPathExpression.getText();
        if (stateFieldPathExpression.FULLY_QUALIFIED_IDENTIFIER() != null) {
            return criteriaBuilder.literal(text);
        }
        return root.get(text);
    }

    private static Expression<?> getExpression(JDQLParser.Input_parameterContext inputParameter,
                                               CriteriaBuilder criteriaBuilder) {
        SourcePersistentEntityCriteriaBuilder sourcePersistentEntityCriteriaBuilder = (SourcePersistentEntityCriteriaBuilder) criteriaBuilder;
        String text = inputParameter.getChild(0).getText();
        if (text.equals("?")) {
            int parameterIndex = Integer.parseInt(inputParameter.getChild(1).getText()) - 1;
            return sourcePersistentEntityCriteriaBuilder.parameterReferencingMethodParameter(parameterIndex);
        }
        if (text.equals(":")) {
            return sourcePersistentEntityCriteriaBuilder.parameterReferencingMethodParameter(inputParameter.getChild(1).getText());
        }
        throw new IllegalStateException("Unknown input parameter: " + text);
    }

    private static Expression<?> getExpression(JDQLParser.In_itemContext inItem,
                                               CriteriaBuilder criteriaBuilder) {
        JDQLParser.LiteralContext literal = inItem.literal();
        if (literal != null) {
            return getExpression(literal, criteriaBuilder);
        }
        JDQLParser.Enum_literalContext enumLiteral = inItem.enum_literal();
        if (enumLiteral != null) {
            return getExpression(enumLiteral);
        }
        JDQLParser.Input_parameterContext inputParameter = inItem.input_parameter();
        if (inputParameter != null) {
            return getExpression(inputParameter, criteriaBuilder);
        }
        throw new IllegalStateException("Unknown IN item: " + inItem);
    }

}
