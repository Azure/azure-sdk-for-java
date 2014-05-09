/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
