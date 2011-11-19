/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 
 */
class Protocol1RuntimeCurrentStateClient implements RuntimeCurrentStateClient {
    private final CurrentStateSerializer serializer;
    private final OutputChannel outputChannel;
    private final AtomicReference<String> endpoint;

    public Protocol1RuntimeCurrentStateClient(CurrentStateSerializer serializer, OutputChannel outputChannel) {
        this.serializer = serializer;
        this.outputChannel = outputChannel;
        this.endpoint = new AtomicReference<String>();
    }

    public void setEndpoint(String endpoint) {
        this.endpoint.set(endpoint);
    }

    @Override
    public void setCurrentState(CurrentState state) {
        OutputStream outputStream = outputChannel.getOutputStream(endpoint.get());

        serializer.serialize(state, outputStream);

        try {
            outputStream.close();
        }
        catch (IOException e) {
            throw new RoleEnvironmentNotAvailableException(e);
        }
    }
}
