/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.serviceruntime;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Represents the Windows Azure environment in which an instance of a role is
 * running.
 */
public final class RoleEnvironment {
    private static final String VERSION_ENDPOINT_ENVIRONMENT_NAME = "WaRuntimeEndpoint";
    private static final String VERSION_ENDPOINT_FIXED_PATH = "\\\\.\\pipe\\WindowsAzureRuntime";
    private static final String CLIENT_ID;

    private static RuntimeClient runtimeClient;
    private static AtomicReference<GoalState> currentGoalState;
    private static AtomicReference<RoleEnvironmentData> currentEnvironmentData;
    private static List<RoleEnvironmentChangingListener> changingListeners;
    private static List<RoleEnvironmentChangedListener> changedListeners;
    private static List<RoleEnvironmentStoppingListener> stoppingListeners;
    private static AtomicReference<CurrentState> lastState;
    private static final Calendar MAX_DATE_TIME;

    static {
        try {
            JAXBContext.newInstance(RoleEnvironment.class.getPackage()
                    .getName());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        CLIENT_ID = UUID.randomUUID().toString();
        MAX_DATE_TIME = javax.xml.bind.DatatypeConverter
                .parseDateTime("9999-12-31T23:59:59.9999999");
    }

    private RoleEnvironment() {
    };

    private static synchronized void initialize() {
        if (runtimeClient == null) {
            String endpoint = System.getenv(VERSION_ENDPOINT_ENVIRONMENT_NAME);

            if (endpoint == null) {
                endpoint = VERSION_ENDPOINT_FIXED_PATH;
            }

            RuntimeKernel kernel = RuntimeKernel.getKernel();

            try {
                runtimeClient = kernel.getRuntimeVersionManager()
                        .getRuntimeClient(endpoint);
            } catch (Throwable t) {
                throw new RoleEnvironmentNotAvailableException(t);
            }

            changingListeners = new LinkedList<RoleEnvironmentChangingListener>();
            changedListeners = new LinkedList<RoleEnvironmentChangedListener>();
            stoppingListeners = new LinkedList<RoleEnvironmentStoppingListener>();

            try {
                currentGoalState = new AtomicReference<GoalState>(
                        runtimeClient.getCurrentGoalState());
                currentEnvironmentData = new AtomicReference<RoleEnvironmentData>(
                        runtimeClient.getRoleEnvironmentData());
            } catch (InterruptedException e) {
                throw new RoleEnvironmentNotAvailableException(e);
            }

            lastState = new AtomicReference<CurrentState>();

            runtimeClient
                    .addGoalStateChangedListener(new GoalStateChangedListener() {
                        @Override
                        public void goalStateChanged(GoalState newGoalState) {
                            switch (newGoalState.getExpectedState()) {
                            case STARTED:
                                if (newGoalState.getIncarnation()
                                        .compareTo(
                                                currentGoalState.get()
                                                        .getIncarnation()) > 0) {
                                    processGoalStateChange(newGoalState);
                                }
                                break;
                            case STOPPED:
                                raiseStoppingEvent();

                                CurrentState stoppedState = new AcquireCurrentState(
                                        CLIENT_ID,
                                        newGoalState.getIncarnation(),
                                        CurrentStatus.STOPPED, MAX_DATE_TIME);

                                runtimeClient.setCurrentState(stoppedState);
                                break;
                            default:
                                throw new IllegalArgumentException();
                            }
                        }
                    });
        } else {
            try {
                currentGoalState.set(runtimeClient.getCurrentGoalState());
                currentEnvironmentData.set(runtimeClient
                        .getRoleEnvironmentData());
            } catch (InterruptedException e) {
                throw new RoleEnvironmentNotAvailableException(e);
            }
        }
    }

    private static void processGoalStateChange(GoalState newGoalState) {
        List<RoleEnvironmentChange> changes = new LinkedList<RoleEnvironmentChange>();
        RoleEnvironmentChangingEvent changingEvent = new RoleEnvironmentChangingEvent(
                changes);
        CurrentState last = lastState.get();

        calculateChanges(changes);

        if (changes.isEmpty()) {
            acceptLatestIncarnation(newGoalState, last);
        } else {
            for (RoleEnvironmentChangingListener listener : changingListeners) {
                try {
                    listener.roleEnvironmentChanging(changingEvent);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            if (changingEvent.isCancelled()) {
                CurrentState recycleState = new AcquireCurrentState(CLIENT_ID,
                        newGoalState.getIncarnation(), CurrentStatus.RECYCLE,
                        MAX_DATE_TIME);

                runtimeClient.setCurrentState(recycleState);

                return;
            }

            acceptLatestIncarnation(newGoalState, last);

            try {
                currentEnvironmentData.set(runtimeClient
                        .getRoleEnvironmentData());
            } catch (InterruptedException e) {
                throw new RoleEnvironmentNotAvailableException(e);
            }

            for (RoleEnvironmentChangedListener listener : changedListeners) {
                try {
                    listener.roleEnvironmentChanged(new RoleEnvironmentChangedEvent(
                            changes));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    private static void acceptLatestIncarnation(GoalState newGoalState,
            CurrentState last) {
        if (last != null && last instanceof AcquireCurrentState) {
            AcquireCurrentState acquireState = (AcquireCurrentState) last;

            CurrentState acceptState = new AcquireCurrentState(CLIENT_ID,
                    newGoalState.getIncarnation(), acquireState.getStatus(),
                    acquireState.getExpiration());

            runtimeClient.setCurrentState(acceptState);
        }

        currentGoalState.set(newGoalState);
    }

    private static void calculateChanges(List<RoleEnvironmentChange> changes) {
        RoleEnvironmentData current = currentEnvironmentData.get();
        RoleEnvironmentData newData;

        try {
            newData = runtimeClient.getRoleEnvironmentData();
        } catch (InterruptedException e) {
            throw new RoleEnvironmentNotAvailableException(e);
        }

        Map<String, String> currentConfig = current.getConfigurationSettings();
        Map<String, String> newConfig = newData.getConfigurationSettings();
        Map<String, Role> currentRoles = current.getRoles();
        Map<String, Role> newRoles = newData.getRoles();

        for (String setting : currentConfig.keySet()) {
            if (newConfig.containsKey(setting)) {
                if (!newConfig.get(setting).equals(currentConfig.get(setting))) {
                    changes.add(new RoleEnvironmentConfigurationSettingChange(
                            setting));
                }
            } else {
                changes.add(new RoleEnvironmentConfigurationSettingChange(
                        setting));
            }
        }

        for (String setting : newConfig.keySet()) {
            if (!currentConfig.containsKey(setting)) {
                changes.add(new RoleEnvironmentConfigurationSettingChange(
                        setting));
            }
        }

        Set<String> changedRoleSet = new HashSet<String>();

        for (String role : currentRoles.keySet()) {
            if (newRoles.containsKey(role)) {
                Role currentRole = currentRoles.get(role);
                Role newRole = newRoles.get(role);

                for (String instance : currentRole.getInstances().keySet()) {
                    if (newRole.getInstances().containsKey(instance)) {
                        RoleInstance currentInstance = currentRole
                                .getInstances().get(instance);
                        RoleInstance newInstance = newRole.getInstances().get(
                                instance);

                        if (currentInstance.getUpdateDomain() == newInstance
                                .getUpdateDomain()
                                && currentInstance.getFaultDomain() == newInstance
                                        .getFaultDomain()) {
                            for (String endpoint : currentInstance
                                    .getInstanceEndpoints().keySet()) {
                                if (newInstance.getInstanceEndpoints()
                                        .containsKey(endpoint)) {
                                    RoleInstanceEndpoint currentEndpoint = currentInstance
                                            .getInstanceEndpoints().get(
                                                    endpoint);
                                    RoleInstanceEndpoint newEndpoint = newInstance
                                            .getInstanceEndpoints().get(
                                                    endpoint);

                                    if (!currentEndpoint.getProtocol().equals(
                                            newEndpoint.getProtocol())
                                            || !currentEndpoint.getIpEndPoint()
                                                    .equals(newEndpoint
                                                            .getIpEndPoint())) {
                                        changedRoleSet.add(role);
                                    }
                                } else {
                                    changedRoleSet.add(role);
                                }
                            }
                        } else {
                            changedRoleSet.add(role);
                        }
                    } else {
                        changedRoleSet.add(role);
                    }
                }
            } else {
                changedRoleSet.add(role);
            }
        }

        for (String role : newRoles.keySet()) {
            if (currentRoles.containsKey(role)) {
                Role currentRole = currentRoles.get(role);
                Role newRole = newRoles.get(role);

                for (String instance : newRole.getInstances().keySet()) {
                    if (currentRole.getInstances().containsKey(instance)) {
                        RoleInstance currentInstance = currentRole
                                .getInstances().get(instance);
                        RoleInstance newInstance = newRole.getInstances().get(
                                instance);

                        if (currentInstance.getUpdateDomain() == newInstance
                                .getUpdateDomain()
                                && currentInstance.getFaultDomain() == newInstance
                                        .getFaultDomain()) {
                            for (String endpoint : newInstance
                                    .getInstanceEndpoints().keySet()) {
                                if (currentInstance.getInstanceEndpoints()
                                        .containsKey(endpoint)) {
                                    RoleInstanceEndpoint currentEndpoint = currentInstance
                                            .getInstanceEndpoints().get(
                                                    endpoint);
                                    RoleInstanceEndpoint newEndpoint = newInstance
                                            .getInstanceEndpoints().get(
                                                    endpoint);

                                    if (!currentEndpoint.getProtocol().equals(
                                            newEndpoint.getProtocol())
                                            || !currentEndpoint.getIpEndPoint()
                                                    .equals(newEndpoint
                                                            .getIpEndPoint())) {
                                        changedRoleSet.add(role);
                                    }
                                } else {
                                    changedRoleSet.add(role);
                                }
                            }
                        } else {
                            changedRoleSet.add(role);
                        }
                    } else {
                        changedRoleSet.add(role);
                    }
                }
            } else {
                changedRoleSet.add(role);
            }
        }

        for (String role : changedRoleSet) {
            changes.add(new RoleEnvironmentTopologyChange(role));
        }
    }

    private static synchronized void raiseStoppingEvent() {
        for (RoleEnvironmentStoppingListener listener : stoppingListeners) {
            try {
                listener.roleEnvironmentStopping();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * Returns a {@link RoleInstance} object that represents the role instance
     * in which this code is currently executing.
     * 
     * @return A <code>RoleInstance</code> object that represents the role
     *         instance in which this code is currently executing.
     */
    public static RoleInstance getCurrentRoleInstance() {
        initialize();

        return currentEnvironmentData.get().getCurrentInstance();
    }

    /**
     * Returns the deployment ID that uniquely identifies the deployment in
     * which this role instance is running.
     * 
     * @return A <code>String</code> object that represents the deployment ID.
     */
    public static String getDeploymentId() {
        initialize();

        return currentEnvironmentData.get().getDeploymentId();
    }

    /**
     * Indicates whether the role instance is running in the Windows Azure
     * environment.
     * 
     * @return <code>true</code> if this instance is running in the development
     *         fabric or in the Windows Azure environment in the cloud;
     *         otherwise, <code>false</code>.
     */
    public static boolean isAvailable() {
        try {
            initialize();
        } catch (RoleEnvironmentNotAvailableException ex) {
        }

        return runtimeClient != null;
    }

    /**
     * Indicates whether the role instance is running in the development fabric.
     * 
     * @return <code>true</code> if this instance is running in the development
     *         fabric; otherwise, <code>false</code>.
     */
    public static boolean isEmulated() {
        initialize();

        return currentEnvironmentData.get().isEmulated();
    }

    /**
     * Returns the set of {@link Role} objects defined for your service.
     * <p>
     * Roles are defined in the service definition file.
     * 
     * @return A <code>java.util.Map</code> object containing the set of
     *         {@link Role} objects that represent the roles defined for your
     *         service.
     */
    public static Map<String, Role> getRoles() {
        initialize();

        return currentEnvironmentData.get().getRoles();
    }

    /**
     * Retrieves the settings in the service configuration file.
     * <p>
     * A role's configuration settings are defined in the service definition
     * file. Values for configuration settings are set in the service
     * configuration file.
     * 
     * @return A <code>java.util.Map</code> object containing the
     *         <code>String</code> objects that represent the configuration
     *         settings.
     */
    public static Map<String, String> getConfigurationSettings() {
        initialize();

        return currentEnvironmentData.get().getConfigurationSettings();
    }

    /**
     * Retrieves the set of named local storage resources.
     * 
     * @return A <code>java.util.Map</code> object containing the
     *         <code>String</code> objects that represent the local storage
     *         resources.
     */
    public static Map<String, LocalResource> getLocalResources() {
        initialize();

        return currentEnvironmentData.get().getLocalResources();
    }

    /**
     * Requests that the current role instance be stopped and restarted.
     * <p>
     * Before the role instance is recycled, the Windows Azure load balancer
     * takes the role instance out of rotation. This ensures that no new
     * requests are routed to the instance while it is restarting.
     * 
     * A call to <code>RequestRecycle</code> initiates the normal shutdown
     * cycle. Windows Azure raises the <code>Stopping</code> event and calls the
     * <code>OnStop</code> method so that you can run the necessary code to
     * prepare the instance to be recycled.
     */
    public static void requestRecycle() {
        initialize();

        CurrentState recycleState = new AcquireCurrentState(CLIENT_ID,
                currentGoalState.get().getIncarnation(), CurrentStatus.RECYCLE,
                MAX_DATE_TIME);

        runtimeClient.setCurrentState(recycleState);
    }

    /**
     * Sets the status of the role instance.
     * <p>
     * An instance may indicate that it is in one of two states: Ready or Busy.
     * If an instance's state is Ready, it is prepared to receive requests from
     * the load balancer. If the instance's state is Busy, it will not receive
     * requests from the load balancer.
     * 
     * @param status
     *            A {@link RoleInstanceStatus} value that indicates whether the
     *            instance is ready or busy.
     * @param expirationUtc
     *            A <code>java.util.Date</code> value that specifies the
     *            expiration date and time of the status.
     * 
     */
    public static void setStatus(RoleInstanceStatus status, Date expirationUtc) {
        initialize();

        CurrentStatus currentStatus = CurrentStatus.STARTED;

        switch (status) {
        case Busy:
            currentStatus = CurrentStatus.BUSY;
            break;
        case Ready:
            currentStatus = CurrentStatus.STARTED;
        default:
            throw new IllegalArgumentException();
        }

        Calendar expiration = Calendar.getInstance();
        expiration.setTime(expirationUtc);

        CurrentState newState = new AcquireCurrentState(CLIENT_ID,
                currentGoalState.get().getIncarnation(), currentStatus,
                expiration);

        lastState.set(newState);

        runtimeClient.setCurrentState(newState);
    }

    /**
     * Clears the status of the role instance.
     * <p>
     * An instance may indicate that it has completed communicating status by
     * calling this method.
     * 
     */
    public static void clearStatus() {
        initialize();

        CurrentState newState = new ReleaseCurrentState(CLIENT_ID);

        lastState.set(newState);

        runtimeClient.setCurrentState(newState);
    }

    /**
     * Adds an event listener for the <code>Changed</code> event, which occurs
     * after a configuration change has been applied to a role instance.
     * <p>
     * A <code>Changed</code> event is encapsulated in a
     * {@link RoleEnvironmentChangedEvent} object.
     * 
     * @param listener
     *            A {@link RoleEnvironmentChangedListener} object that
     *            represents the event listener to add.
     * 
     * @see #removeRoleEnvironmentChangedListener
     */
    public static synchronized void addRoleEnvironmentChangedListener(
            RoleEnvironmentChangedListener listener) {
        initialize();

        changedListeners.add(listener);
    }

    /**
     * Removes an event listener for the <code>Changed</code> event.
     * 
     * @param listener
     *            A {@link RoleEnvironmentChangedListener} object that
     *            represents the event listener to remove.
     * 
     * @see #addRoleEnvironmentChangedListener
     */
    public static synchronized void removeRoleEnvironmentChangedListener(
            RoleEnvironmentChangedListener listener) {
        initialize();

        changedListeners.remove(listener);
    }

    /**
     * Adds an event listener for the <code>Changing</code> event, which occurs
     * before a change to the service configuration is applied to the running
     * instances of the role.
     * <p>
     * Service configuration changes are applied on-the-fly to running role
     * instances. Configuration changes include changes to the service
     * configuration changes and changes to the number of instances in the
     * service.
     * <p>
     * This event occurs after the new configuration file has been submitted to
     * Windows Azure but before the changes have been applied to each running
     * role instance. This event can be cancelled for a given instance to
     * prevent the configuration change.
     * <p>
     * Note that cancelling this event causes the instance to be automatically
     * recycled. When the instance is recycled, the configuration change is
     * applied when it restarts.
     * <p>
     * A <code>Changing</code> event is encapsulated in a
     * {@link RoleEnvironmentChangingEvent} object.
     * 
     * @param listener
     *            A {@link RoleEnvironmentChangingListener} object that
     *            represents the event listener to add.
     * 
     * @see #removeRoleEnvironmentChangingListener
     */
    public static synchronized void addRoleEnvironmentChangingListener(
            RoleEnvironmentChangingListener listener) {
        initialize();

        changingListeners.add(listener);
    }

    /**
     * Removes an event listener for the <code>Changing</code> event.
     * 
     * @param listener
     *            A {@link RoleEnvironmentChangingListener} object that
     *            represents the event listener to remove.
     * 
     * @see #addRoleEnvironmentChangingListener
     */
    public static void removeRoleEnvironmentChangingListener(
            RoleEnvironmentChangingListener listener) {
        initialize();

        changingListeners.remove(listener);
    }

    /**
     * Adds an event listener for the <code>Stopping</code> event, which occurs
     * wheen the role is stopping.
     * 
     * @param listener
     *            A {@link RoleEnvironmentStoppingListener} object that
     *            represents the event listener to add.
     * 
     * @see #removeRoleEnvironmentStoppingListener
     */
    public static synchronized void addRoleEnvironmentStoppingListener(
            RoleEnvironmentStoppingListener listener) {
        initialize();

        stoppingListeners.add(listener);
    }

    /**
     * Removes an event listener for the <code>Stopping</code> event.
     * 
     * @param listener
     *            A {@link RoleEnvironmentStoppingListener} object that
     *            represents the event listener to remove.
     * 
     * @see #addRoleEnvironmentStoppingListener
     */
    public static synchronized void removeRoleEnvironmentStoppingListener(
            RoleEnvironmentStoppingListener listener) {
        initialize();

        stoppingListeners.remove(listener);
    }
}
