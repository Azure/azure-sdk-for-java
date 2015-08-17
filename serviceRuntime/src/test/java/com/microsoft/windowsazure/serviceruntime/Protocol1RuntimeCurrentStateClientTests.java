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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class Protocol1RuntimeCurrentStateClientTests {
    @Test
    public void setCurrentStateSerializesToEndpoint() {
        final CurrentState expectedState = new AcquireCurrentState(null, null,
                null, null);
        final OutputStream expectedStream = new ByteArrayOutputStream();
        final String expectedEndpoint = "endpoint";
        final AtomicBoolean serializeCalled = new AtomicBoolean(false);

        CurrentStateSerializer serializer = new CurrentStateSerializer() {
            @Override
            public void serialize(CurrentState state, OutputStream stream) {
                assertThat(state, is(expectedState));
                assertThat(stream, is(expectedStream));

                serializeCalled.set(true);
            }
        };

        OutputChannel outputChannel = new OutputChannel() {
            @Override
            public OutputStream getOutputStream(String name) {
                assertThat(name, is(expectedEndpoint));

                return expectedStream;
            }
        };

        Protocol1RuntimeCurrentStateClient currentStateClient = new Protocol1RuntimeCurrentStateClient(
                serializer, outputChannel);

        currentStateClient.setEndpoint(expectedEndpoint);

        currentStateClient.setCurrentState(expectedState);

        assertThat(serializeCalled.get(), is(true));
    }

    @Test
    public void streamCloseFailureThrowsNotAvailableException() {
        final CurrentState expectedState = new AcquireCurrentState(null, null,
                null, null);
        final OutputStream expectedStream = new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {
            }

            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        };

        final String expectedEndpoint = "endpoint";
        final AtomicBoolean serializeCalled = new AtomicBoolean(false);

        CurrentStateSerializer serializer = new CurrentStateSerializer() {
            @Override
            public void serialize(CurrentState state, OutputStream stream) {
                assertThat(state, is(expectedState));
                assertThat(stream, is(expectedStream));

                serializeCalled.set(true);
            }
        };

        OutputChannel outputChannel = new OutputChannel() {
            @Override
            public OutputStream getOutputStream(String name) {
                assertThat(name, is(expectedEndpoint));

                return expectedStream;
            }
        };

        Protocol1RuntimeCurrentStateClient currentStateClient = new Protocol1RuntimeCurrentStateClient(
                serializer, outputChannel);

        currentStateClient.setEndpoint(expectedEndpoint);

        try {
            currentStateClient.setCurrentState(expectedState);
        } catch (RoleEnvironmentNotAvailableException e) {
            return;
        }

        fail();
    }
}
