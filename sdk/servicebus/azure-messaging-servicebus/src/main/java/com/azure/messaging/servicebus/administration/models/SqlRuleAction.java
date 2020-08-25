package com.azure.messaging.servicebus.administration.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents set of actions written in SQL language-based syntax that is performed against a
 * {@link ServiceBusMessage}.
 */
public class SqlRuleAction extends RuleAction {
    private static final int MAXIMUM_SQL_RULE_ACTION_STATEMENT_LENGTH = 1024;

    private final Map<String, Object> properties = new HashMap<>();
    private final String sqlExpression;

    public SqlRuleAction(String sqlExpression) {
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

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getSqlExpression() {
        return sqlExpression;
    }
}
