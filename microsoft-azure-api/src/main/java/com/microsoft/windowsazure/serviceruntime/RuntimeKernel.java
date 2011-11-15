/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

/**
 * 
 */
class RuntimeKernel {
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
