/**
 * 
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
        final CurrentState expectedState = new AcquireCurrentState(null, null, null, null);
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

        Protocol1RuntimeCurrentStateClient currentStateClient = new Protocol1RuntimeCurrentStateClient(serializer,
                outputChannel);

        currentStateClient.setEndpoint(expectedEndpoint);

        currentStateClient.setCurrentState(expectedState);

        assertThat(serializeCalled.get(), is(true));
    }

    @Test
    public void streamCloseFailureThrowsNotAvailableException() {
        final CurrentState expectedState = new AcquireCurrentState(null, null, null, null);
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

        Protocol1RuntimeCurrentStateClient currentStateClient = new Protocol1RuntimeCurrentStateClient(serializer,
                outputChannel);

        currentStateClient.setEndpoint(expectedEndpoint);

        try {
            currentStateClient.setCurrentState(expectedState);
        }
        catch (RoleEnvironmentNotAvailableException e) {
            return;
        }

        fail();
    }
}
