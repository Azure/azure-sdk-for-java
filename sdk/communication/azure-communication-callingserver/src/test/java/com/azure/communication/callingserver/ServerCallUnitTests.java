// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.InvalidParameterException;

import com.azure.core.test.http.NoOpHttpClient;

import org.junit.jupiter.api.Test;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode. The runAllClientFunctions and runAllClientFunctionsWithResponse
 * test will not run in LIVE or RECORD as they cannot get their own conversationId.
 */
public class ServerCallUnitTests {
    private String serverCallId = "aHR0cHM6Ly9jb252LXVzd2UtMDguY29udi5za3lwZS5jb20vY29udi8tby1FWjVpMHJrS3RFTDBNd0FST1J3P2k9ODgmZT02Mzc1Nzc0MTY4MDc4MjQyOTM";

    // Calling Tests
    static final String MOCK_URL = "https://REDACTED.communication.azure.com";
    static final String MOCK_ACCESS_KEY = "eyKfcHciOiJIUzI1NiIsInR5cCI6IkqXVCJ9eyJzdWIiOiIxMjM0NTY5ODkwIiwibmFtZSI7IkpvaGfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJVadUs4s5d";
    static final String MOCK_CONNECTION_STRING = "endpoint=https://REDACTED.communication.azure.com/;accesskey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaGfQSflKxwRJSMeKKF2QT4fwpMeJf36POk6yJVadQssw5c";
    private static final String APPLICATION_ID = "833bad32-4432-4d41-8bb4";

    @Test
    public void startRecordingRelativeUriFails() {
        ServerCall serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildClient()
            .initializeServerCall(serverCallId);

        assertThrows(InvalidParameterException.class, () -> {
            serverCall.startRecording("/not/absolute/uri");
        });
    }

    @Test
    public void addParticipantNullParticipantFails() {
        ServerCall serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildClient()
            .initializeServerCall(serverCallId);

        assertThrows(NullPointerException.class, () -> {
            serverCall.addParticipant(null, null, null, null);
        });
    }

    @Test
    public void removeParticipantNullParticipantFails() {
        ServerCall serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildClient()
            .initializeServerCall(serverCallId);

        assertThrows(NullPointerException.class, () -> {
            serverCall.removeParticipant(null);
        });
    }

    @Test
    public void startRecordingAsyncFails() {
        ServerCallAsync serverCall = new CallingServerClientBuilder()
            .httpClient(new NoOpHttpClient())
            .connectionString(MOCK_CONNECTION_STRING)
            .buildAsyncClient()
            .initializeServerCall(serverCallId);

        assertThrows(InvalidParameterException.class, () -> {
            serverCall.startRecording("/not/absolute/uri").block();
        });
    }    
}
