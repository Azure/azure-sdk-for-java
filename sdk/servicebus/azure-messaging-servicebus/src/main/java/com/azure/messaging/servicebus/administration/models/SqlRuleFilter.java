// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a filter which is a composition of an expression and an action that is executed in the pub/sub pipeline.
 * <p>
 * A {@link SqlRuleFilter} holds a SQL-like condition expression that is evaluated in the broker against the arriving
 * messages' user-defined properties and system properties. All system properties (which are all properties explicitly
 * listed on the {@link ServiceBusMessage} class) must be prefixed with {@code sys.} in the condition expression. The
 * SQL subset implements testing for existence of properties (EXISTS), testing for null-values (IS NULL), logical
 * NOT/AND/OR, relational operators, numeric arithmetic, and simple text pattern matching with LIKE.
 * </p>
 *
 * @see CreateRuleOptions#setFilter(RuleFilter)
 * @see RuleProperties#setFilter(RuleFilter)
 */
public class SqlRuleFilter extends RuleFilter {
    private final Map<String, Object> properties = new HashMap<>();
    private final String sqlExpression;
    private final String compatibilityLevel;
    private final Boolean requiresPreprocessing;

    /**
     * Creates a new instance with the given SQL expression.
     *
     * @param sqlExpression SQL expression for the filter.
     *
     * @throws NullPointerException if {@code sqlExpression} is null.
     * @throws IllegalArgumentException if {@code sqlExpression} is an empty string.
     */
    public SqlRuleFilter(String sqlExpression) {
        final ClientLogger logger = new ClientLogger(SqlRuleFilter.class);

        if (sqlExpression == null) {
            throw logger.logExceptionAsError(new NullPointerException("'sqlExpression' cannot be null."));
        } else if (sqlExpression.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'sqlExpression' cannot be an empty string."));
        }

        this.sqlExpression = sqlExpression;
        this.compatibilityLevel = null;
        this.requiresPreprocessing = null;
    }

    /**
     * Package private constructor for creating a model deserialised from the service.
     *
     * @param sqlExpression SQL expression for the filter.
     * @param compatibilityLevel The compatibility level.
     * @param requiresPreprocessing Whether or not it requires preprocessing
     */
    SqlRuleFilter(String sqlExpression, String compatibilityLevel, Boolean requiresPreprocessing) {
        this.sqlExpression = sqlExpression;
        this.compatibilityLevel = compatibilityLevel;
        this.requiresPreprocessing = requiresPreprocessing;
    }

    /**
     * Gets the compatibility level.
     *
     * @return The compatibility level.
     */
    String getCompatibilityLevel() {
        return compatibilityLevel;
    }

    /**
     * Gets whether or not requires preprocessing.
     *
     * @return Whether or not requires preprocessing.
     */
    Boolean isPreprocessingRequired() {
        return requiresPreprocessing;
    }

    /**
     * Gets the value of a filter expression. Allowed types: string, int, long, bool, double
     *
     * @return Gets the value of a filter expression.
     */
    public Map<String, Object> getParameters() {
        return properties;
    }

    /**
     * Gets the SQL expression.
     *
     * @return The SQL expression.
     */
    public String getSqlExpression() {
        return sqlExpression;
    }
}
