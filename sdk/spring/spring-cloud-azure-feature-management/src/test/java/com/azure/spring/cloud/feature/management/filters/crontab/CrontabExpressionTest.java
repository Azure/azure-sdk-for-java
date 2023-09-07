package com.azure.spring.cloud.feature.management.filters.crontab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CrontabExpressionTest {

    @Test
    public void validateExpressionTest() {
        validateExpression("* * * * *", true);
        validateExpression("1 2 3 Apr Fri", true);
        validateExpression("* * * 1,2 Fri", true);
        validateExpression("* * * * 0-7", true);
        validateExpression("1    2  \n3      Apr     Fri", true);
        validateExpression("00-59/3,1,2-2 01,3,20-23 */10,31-1/100 Apr,1-Feb,oct-DEC/1 Sun-Sat/2,1-7", true);
        validateExpression("* * * * * *", false);
        validateExpression("* * * 1,2", false);
        validateExpression("Fri * * * *", false);
        validateExpression("1 2 Wed 4 5", false);
        validateExpression("* * * 1, *", false);
        validateExpression("* * * ,2 *", false);
        validateExpression("* * , * *", false);
        validateExpression("* * */-1 * *", false);
        validateExpression("*****", false);
        validateExpression("* * * # *", false);
        validateExpression("0-60 * * * *", false);
        validateExpression("* 24 * * *", false);
        validateExpression("* * 32 * *", false);
        validateExpression("* * * 13 *", false);
        validateExpression("* * * * 8", false);
        validateExpression("* * 0 * *", false);
        validateExpression("* * * 0 *", false);
        validateExpression("* * * */ *", false);
        validateExpression("* * * *// *", false);
        validateExpression("* * * --- *", false);
        validateExpression("* * * - *", false);
    }

    private void validateExpression(String expression, boolean isValid) {
        boolean result = true;
        try {
            final CrontabExpression crontabExpression = new CrontabExpression(expression);
        } catch (final IllegalArgumentException e) {
            result = false;
        }
        assertEquals(result, isValid);
    }
}
