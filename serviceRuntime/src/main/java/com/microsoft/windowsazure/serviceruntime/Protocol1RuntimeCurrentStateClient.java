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

    public Protocol1RuntimeCurrentStateClient(
            CurrentStateSerializer serializer, OutputChannel outputChannel) {
        this.serializer = serializer;
        this.outputChannel = outputChannel;
        this.endpoint = new AtomicReference<String>();
    }

    public void setEndpoint(String endpoint) {
        this.endpoint.set(endpoint);
    }

    @Override
    public void setCurrentState(CurrentState state) {
        OutputStream outputStream = outputChannel.getOutputStream(endpoint
                .get());

        serializer.serialize(state, outputStream);

        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RoleEnvironmentNotAvailableException(e);
        }
    }
}
