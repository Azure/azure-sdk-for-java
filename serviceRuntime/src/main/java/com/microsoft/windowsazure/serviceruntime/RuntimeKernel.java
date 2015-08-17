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
public final class RuntimeKernel {
    private static RuntimeKernel theKernel;

    private final CurrentStateSerializer currentStateSerializer;
    private final GoalStateDeserializer goalStateDeserializer;
    private final InputChannel inputChannel;
    private final OutputChannel outputChannel;
    private final Protocol1RuntimeCurrentStateClient protocol1RuntimeCurrentStateClient;
    private final RoleEnvironmentDataDeserializer roleEnvironmentDataDeserializer;
    private final Protocol1RuntimeGoalStateClient protocol1RuntimeGoalStateClient;
    private final RuntimeVersionProtocolClient runtimeVersionProtocolClient;
    private final RuntimeVersionManager runtimeVersionManager;

    private RuntimeKernel() {
        this.currentStateSerializer = new XmlCurrentStateSerializer();
        this.goalStateDeserializer = new ChunkedGoalStateDeserializer();
        this.inputChannel = new FileInputChannel();
        this.outputChannel = new FileOutputChannel();
        this.protocol1RuntimeCurrentStateClient = new Protocol1RuntimeCurrentStateClient(
                currentStateSerializer, outputChannel);
        this.roleEnvironmentDataDeserializer = new XmlRoleEnvironmentDataDeserializer();
        this.protocol1RuntimeGoalStateClient = new Protocol1RuntimeGoalStateClient(
                protocol1RuntimeCurrentStateClient, goalStateDeserializer,
                roleEnvironmentDataDeserializer, inputChannel);
        this.runtimeVersionProtocolClient = new RuntimeVersionProtocolClient(
                inputChannel);
        this.runtimeVersionManager = new RuntimeVersionManager(
                runtimeVersionProtocolClient);
    }

    public static RuntimeKernel getKernel() {
        if (theKernel == null) {
            theKernel = new RuntimeKernel();
        }

        return theKernel;
    }

    public CurrentStateSerializer getCurrentStateSerializer() {
        return currentStateSerializer;
    }

    public GoalStateDeserializer getGoalStateDeserializer() {
        return goalStateDeserializer;
    }

    public InputChannel getInputChannel() {
        return inputChannel;
    }

    public OutputChannel getOutputChannel() {
        return outputChannel;
    }

    public Protocol1RuntimeCurrentStateClient getProtocol1RuntimeCurrentStateClient() {
        return protocol1RuntimeCurrentStateClient;
    }

    public RoleEnvironmentDataDeserializer getRoleEnvironmentDataDeserializer() {
        return roleEnvironmentDataDeserializer;
    }

    public Protocol1RuntimeGoalStateClient getProtocol1RuntimeGoalStateClient() {
        return protocol1RuntimeGoalStateClient;
    }

    public RuntimeVersionProtocolClient getRuntimeVersionProtocolClient() {
        return runtimeVersionProtocolClient;
    }

    public RuntimeVersionManager getRuntimeVersionManager() {
        return runtimeVersionManager;
    }
}
