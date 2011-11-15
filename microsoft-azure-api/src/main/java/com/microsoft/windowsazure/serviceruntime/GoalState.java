/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

import java.math.BigInteger;
import java.util.Calendar;

/**
 * 
 */
class GoalState {
    private final BigInteger incarnation;
    private final ExpectedState expectedState;
    private final String environmentPath;
    private final Calendar deadline;
    private final String currentStateEndpoint;

    public GoalState(BigInteger incarnation, ExpectedState expectedState,
            String environmentPath, Calendar deadline,
            String currentStateEndpoint) {
        this.incarnation = incarnation;
        this.expectedState = expectedState;
        this.environmentPath = environmentPath;
        this.deadline = deadline;
        this.currentStateEndpoint = currentStateEndpoint;
    }

    public BigInteger getIncarnation() {
        return incarnation;
    }

    public ExpectedState getExpectedState() {
        return expectedState;
    }

    public String getEnvironmentPath() {
        return environmentPath;
    }

    public Calendar getDeadline() {
        return deadline;
    }

    public String getCurrentStateEndpoint() {
        return currentStateEndpoint;
    }
}
