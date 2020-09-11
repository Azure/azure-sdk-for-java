// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents set of actions written in SQL language-based syntax that is performed against a {@link
 * ServiceBusMessage}.
 */
public class SqlRuleAction extends RuleAction {
    private final Map<String, Object> properties = new HashMap<>();
    private final String sqlExpression;
    private final String compatibilityLevel;
    private final Boolean requiresPreprocessing;

    /**
     * Creates a new instance with the given SQL expression.
     *
     * @param sqlExpression SQL expression for the action.
     *
     * @throws NullPointerException if {@code sqlExpression} is null.
     * @throws IllegalArgumentException if {@code sqlExpression} is an empty string.
     */
    public SqlRuleAction(String sqlExpression) {
        final ClientLogger logger = new ClientLogger(SqlRuleAction.class);

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
     * @param sqlExpression SQL expression for the action.
     * @param compatibilityLevel The compatibility level.
     * @param requiresPreprocessing Whether or not it requires preprocessing
     */
    SqlRuleAction(String sqlExpression, String compatibilityLevel, Boolean requiresPreprocessing) {
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
    Boolean getRequiresPreprocessing() {
        return requiresPreprocessing;
    }

    /**
     * Gets the properties for this action.
     *
     * @return the properties for this action.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Gets the SQL expression.
     *
     * @return the SQL expression.
     */
    public String getSqlExpression() {
        return sqlExpression;
    }
}
