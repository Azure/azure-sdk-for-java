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

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 
 */
class Protocol1RuntimeGoalStateClient implements RuntimeGoalStateClient {
    private final Protocol1RuntimeCurrentStateClient currentStateClient;
    private Thread ioThread;
    private final GoalStateDeserializer goalStateDeserializer;
    private final RoleEnvironmentDataDeserializer roleEnvironmentDeserializer;
    private final InputChannel inputChannel;
    private final CountDownLatch goalStateLatch;
    private final List<GoalStateChangedListener> listeners;
    private String endpoint;

    private AtomicReference<GoalState> currentGoalState;
    private AtomicReference<RoleEnvironmentData> currentEnvironmentData;

    public Protocol1RuntimeGoalStateClient(
            Protocol1RuntimeCurrentStateClient currentStateClient,
            GoalStateDeserializer goalStateDeserializer,
            RoleEnvironmentDataDeserializer roleEnvironmentDeserializer,
            InputChannel inputChannel) {
        this.currentStateClient = currentStateClient;
        this.goalStateDeserializer = goalStateDeserializer;
        this.roleEnvironmentDeserializer = roleEnvironmentDeserializer;
        this.inputChannel = inputChannel;

        this.listeners = new LinkedList<GoalStateChangedListener>();
        this.goalStateLatch = new CountDownLatch(1);

        this.currentGoalState = new AtomicReference<GoalState>();
        this.currentEnvironmentData = new AtomicReference<RoleEnvironmentData>();
    }

    public GoalState getCurrentGoalState() throws InterruptedException {
        ensureGoalStateRetrieved();

        return currentGoalState.get();
    }

    public synchronized RoleEnvironmentData getRoleEnvironmentData()
            throws InterruptedException {
        ensureGoalStateRetrieved();

        if (currentEnvironmentData.get() == null) {
            GoalState current = currentGoalState.get();

            if (current.getEnvironmentPath() == null) {
                throw new InterruptedException(
                        "No role environment data for the current goal state.");
            }

            InputStream environmentStream = inputChannel.getInputStream(current
                    .getEnvironmentPath());

            currentEnvironmentData.set(roleEnvironmentDeserializer
                    .deserialize(environmentStream));
        }

        return currentEnvironmentData.get();
    }

    public void addGoalStateChangedListener(GoalStateChangedListener listener) {
        listeners.add(listener);
    }

    public void removeGoalStateChangedListener(GoalStateChangedListener listener) {
        listeners.remove(listener);
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    private void ensureGoalStateRetrieved() throws InterruptedException {
        ensureThread();

        if (currentGoalState.get() == null) {
            while (!goalStateLatch.await(100, TimeUnit.MILLISECONDS)) {
                ensureThread();
            }
        }
    }

    private void ensureThread() {
        if (ioThread == null || !ioThread.isAlive()) {
            startGoalStateTask();
        }
    }

    private void startGoalStateTask() {
        Runnable goalStateListener = new Runnable() {
            public void run() {
                InputStream inputStream = inputChannel.getInputStream(endpoint);

                goalStateDeserializer.initialize(inputStream);

                GoalState goalState = goalStateDeserializer.deserialize();

                if (goalState == null) {
                    return;
                }

                currentGoalState.set(goalState);

                if (goalState.getEnvironmentPath() != null) {
                    currentEnvironmentData.set(null);
                }

                currentStateClient.setEndpoint(currentGoalState.get()
                        .getCurrentStateEndpoint());

                goalStateLatch.countDown();

                while (true) {
                    goalState = goalStateDeserializer.deserialize();

                    if (goalState == null) {
                        return;
                    }

                    currentGoalState.set(goalState);

                    if (goalState.getEnvironmentPath() != null) {
                        currentEnvironmentData.set(null);
                    }

                    currentStateClient.setEndpoint(currentGoalState.get()
                            .getCurrentStateEndpoint());

                    for (GoalStateChangedListener listener : listeners) {
                        listener.goalStateChanged(currentGoalState.get());
                    }
                }
            }
        };

        ioThread = new Thread(goalStateListener);

        ioThread.setDaemon(true);

        ioThread.start();
    }
}
