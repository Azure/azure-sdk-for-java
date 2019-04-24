// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus.rules;

/**
 * Represents a SQL language-based filter expression that is evaluated against a message.
 *
 * @since 1.0
 */
public class SqlFilter extends Filter {

    private String sqlExpression;

    /**
     * Creates an instance of <code>SqlFilter</code> with the given match expression.
     *
     * @param sqlExpression SQL language-based filter expression
     */
    public SqlFilter(String sqlExpression) {
        this.sqlExpression = sqlExpression;
    }

    /**
     * Gets the match expression of this filter.
     *
     * @return SQL language-based expression of this filter
     */
    public String getSqlExpression() {
        return this.sqlExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SqlFilter)) {
            return false;
        }

        SqlFilter other = (SqlFilter)o;
        return (this.sqlExpression == null ? other.sqlExpression == null : this.sqlExpression.equals(other.sqlExpression));
    }

    @Override
    public int hashCode() {
        if (this.sqlExpression != null) {
            return this.sqlExpression.hashCode();
        }

        return super.hashCode();
    }
}
