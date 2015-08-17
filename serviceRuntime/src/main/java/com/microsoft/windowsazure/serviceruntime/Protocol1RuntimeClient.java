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

/**
 * 
 */
class Protocol1RuntimeClient implements RuntimeClient {
    private final Protocol1RuntimeGoalStateClient goalStateClient;
    private final Protocol1RuntimeCurrentStateClient currentStateClient;

    public Protocol1RuntimeClient(
            Protocol1RuntimeGoalStateClient goalStateClient,
            Protocol1RuntimeCurrentStateClient currentStateClient,
            String endpoint) {
        this.goalStateClient = goalStateClient;
        this.currentStateClient = currentStateClient;

        this.goalStateClient.setEndpoint(endpoint);
    }

    @Override
    public GoalState getCurrentGoalState() throws InterruptedException {
        return goalStateClient.getCurrentGoalState();
    }

    @Override
    public RoleEnvironmentData getRoleEnvironmentData()
            throws InterruptedException {
        return goalStateClient.getRoleEnvironmentData();
    }

    @Override
    public void addGoalStateChangedListener(GoalStateChangedListener listener) {
        goalStateClient.addGoalStateChangedListener(listener);
    }

    @Override
    public void removeGoalStateChangedListener(GoalStateChangedListener listener) {
        goalStateClient.removeGoalStateChangedListener(listener);
    }

    @Override
    public void setCurrentState(CurrentState state) {
        currentStateClient.setCurrentState(state);
    }
}
