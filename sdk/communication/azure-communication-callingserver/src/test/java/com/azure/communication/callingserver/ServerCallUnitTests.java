// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.security.InvalidParameterException;
import com.azure.core.test.http.NoOpHttpClient;
import org.junit.jupiter.api.Test;

public class ServerCallUnitTests {

    private final String serverCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDguY29udi5za3lwZS5jb20vY29udi8tby1FWjVpMHJrS3RFTDBNd0FST1J3P2k9ODgmZT02Mzc1Nzc0MTY4MDc4MjQyOTM";
    static final String MOCK_CONNECTION_STRING = "endpoint=https://REDACTED.communication.azure.com/;accesskey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaGfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJVadQssw5c";

    @Test
    public void startRecordingRelativeUriFails() {
        ServerCall serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildClient()
            .initializeServerCall(serverCallId);

        assertThrows(
            InvalidParameterException.class,
            () -> serverCall.startRecording("/not/absolute/uri"));
    }

    @Test
    public void startRecordingWithResponseRelativeUriFails() {
        ServerCall serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildClient()
            .initializeServerCall(serverCallId);

        assertThrows(
            InvalidParameterException.class,
            () -> serverCall.startRecordingWithResponse("/not/absolute/uri", null));
    }

    @Test
    public void addParticipantNullParticipantFails() {
        ServerCall serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildClient()
            .initializeServerCall(serverCallId);

        assertThrows(
            NullPointerException.class,
            () -> serverCall.addParticipant(null, null, null, null));
    }

    @Test
    public void startRecordingAsyncFails() {
        ServerCallAsync serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildAsyncClient()
            .initializeServerCall(serverCallId);

        assertThrows(
            InvalidParameterException.class,
            () -> serverCall.startRecording("/not/absolute/uri")
                .block());
    }
}
