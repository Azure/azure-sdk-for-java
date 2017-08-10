// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus.rules;

/**
 * Represents a SQL language-based transformation expression that is performed against a message.
 *
 * @since 1.0
 */
public class SqlRuleAction extends RuleAction {
    private String sqlExpression;

    /**
     * Creates an instance of <code>SqlRuleAction</code> with the given transformation expression.
     *
     * @param sqlExpression SQL language-based transformation expression
     */
    public SqlRuleAction(String sqlExpression) {
        this.sqlExpression = sqlExpression;
    }

    /**
     * Gets the transformation expression of this rule action.
     *
     * @return SQL language-based transformation expression
     */
    public String getSqlExpression() {
        return this.sqlExpression;
    }
}
