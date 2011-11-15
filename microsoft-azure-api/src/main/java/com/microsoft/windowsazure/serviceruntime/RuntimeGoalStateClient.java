/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

/**
 * 
 */
interface RuntimeGoalStateClient {
    public GoalState getCurrentGoalState() throws InterruptedException;

    public RoleEnvironmentData getRoleEnvironmentData()
            throws InterruptedException;

    public void addGoalStateChangedListener(GoalStateChangedListener listener);

    public void removeGoalStateChangedListener(GoalStateChangedListener listener);
}
