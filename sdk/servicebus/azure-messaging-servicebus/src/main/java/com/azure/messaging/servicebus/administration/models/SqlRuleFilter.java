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
 */
public class SqlRuleFilter extends RuleFilter {
    static final int MAXIMUM_SQL_RULE_ACTION_STATEMENT_LENGTH = 1024;

    private final Map<String, Object> properties = new HashMap<>();
    private final String sqlExpression;

    public SqlRuleFilter(String sqlExpression) {
        final ClientLogger logger = new ClientLogger(SqlRuleAction.class);

        if (sqlExpression == null) {
            throw logger.logThrowableAsError(new NullPointerException("'sqlExpression' cannot be null."));
        } else if (sqlExpression.length() > MAXIMUM_SQL_RULE_ACTION_STATEMENT_LENGTH) {
            throw logger.logThrowableAsError(new IllegalArgumentException(String.format(
                "The argument '%s' cannot exceed %s characters.",
                sqlExpression, MAXIMUM_SQL_RULE_ACTION_STATEMENT_LENGTH)));
        }

        this.sqlExpression = sqlExpression;
    }

    /**
     * Gets the value of a filter expression. Allowed types: string, int, long, bool, double
     *
     * @return Gets the value of a filter expression.
     */
    public Map<String, Object> getProperties() {
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
