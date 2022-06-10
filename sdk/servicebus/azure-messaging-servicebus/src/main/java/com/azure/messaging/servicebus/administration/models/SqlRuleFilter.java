// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private static final ClientLogger LOGGER = new ClientLogger(SqlRuleFilter.class);

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
        if (sqlExpression == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'sqlExpression' cannot be null."));
        } else if (sqlExpression.isEmpty()) {
            throw LOGGER.logExceptionAsError(
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

    /**
     * Converts the value of the current instance to its equivalent string representation.
     *
     * @return A string representation of the current instance.
     */
    @Override
    public String toString() {
        return String.format("SqlRuleFilter: %s", sqlExpression);
    }

    /**
     *  Compares this RuleFilter to the specified object. The result is true if and only if the argument is not null
     *  and is a SqlRuleFilter object that with the same parameters as this object.
     *
     * @param other - the object to which the current SqlRuleFilter should be compared.
     * @return True, if the passed object is a SqlRuleFilter with the same parameter values, False otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SqlRuleFilter)) {
            return false;
        }
        SqlRuleFilter that = (SqlRuleFilter) other;
        return sqlExpression.equals(that.sqlExpression)
            && Objects.equals(compatibilityLevel, that.compatibilityLevel)
            && Objects.equals(requiresPreprocessing, that.requiresPreprocessing)
            && Objects.equals(properties, that.properties);
    }

    /**
     * Returns a hash code for this SqlRuleFilter, which is the hashcode for the SqlExpression.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return sqlExpression.hashCode();
    }
}
