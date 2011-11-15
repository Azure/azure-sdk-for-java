/**
 * 
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