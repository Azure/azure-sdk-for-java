// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters.crontab;

import java.time.ZonedDateTime;

/**
 * The crontab expression with Minute, Hour, Day of month, Month, Day of week fields.
 * */
public class CrontabExpression {
    private static final int NUMBER_OF_FIELDS = 5;
    private final CrontabField[] crontabFields = {
        new CrontabField(CrontabFiledType.MINUTE),
        new CrontabField(CrontabFiledType.HOUR),
        new CrontabField(CrontabFiledType.DAY_OF_MONTH),
        new CrontabField(CrontabFiledType.MONTH),
        new CrontabField(CrontabFiledType.DAY_OF_WEEK)
    };

    /**
     * If the expression is invalid, an IllegalArgumentException will be thrown.
     * @param expression The expression that describe crontab.
     */
    public CrontabExpression(String expression) {
        this.tryParse(expression);
    }

    /**
     * Check whether the given expression can be parsed by the crontab expression.
     *
     * @param expression The expression that describe crontab.
     */
    private void tryParse(String expression) {
        final String exceptionStringFormat = "Crontab expression: %s is invalid. %s";
        if (expression == null) {
            throw new IllegalArgumentException(exceptionStringFormat.formatted(null, "Expression is null."));
        }

        final String[] fields = expression.split("[\t\n ]");
        if (fields.length != NUMBER_OF_FIELDS) {
            throw new IllegalArgumentException(exceptionStringFormat.formatted(expression,
                "Five fields in the sequence of Minute, Hour, Day of month, Month, and Day of week are required."));
        }

        for (int i = 0; i < NUMBER_OF_FIELDS; i++) {
            crontabFields[i].tryParse(fields[i]);
        }
    }

    /**
     * Checks whether the given timestamp match crontab expression specified time.
     * @param now The timestamp.
     * @return True if the given timestamp match crontab expression specified time, otherwise false.
     * */
    public boolean isMatch(ZonedDateTime now) {
        return crontabFields[0].isMatch(now.getMinute())
            && crontabFields[1].isMatch(now.getHour())
            && crontabFields[2].isMatch(now.getDayOfMonth())
            && crontabFields[3].isMatch(now.getMonthValue())
            && crontabFields[4].isMatch(now.getDayOfWeek().getValue());
    }
}
