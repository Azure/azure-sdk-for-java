// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.RecordingState;
import com.azure.communication.callautomation.models.RecordingStateResult;
import com.azure.communication.callautomation.models.ServerCallLocator;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class CallRecordingAsyncLiveTests extends CallAutomationLiveTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void recordingOperations(HttpClient httpClient) {
        CallAutomationAsyncClient client = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("recordingOperationsAsync", next))
            .buildAsyncClient();

        CommunicationIdentityAsyncClient communicationIdentityAsyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .buildAsyncClient();

        String callConnectionId = "";
        try {
            CommunicationUserIdentifier sourceUser = communicationIdentityAsyncClient.createUser().block();

            String targetUserId = Optional.ofNullable(ACS_USER_CALL_RECORDING).orElse("8:acs:ad7b4e1f-5b71-4d2f-9db2-b1bae6d4f392_00000014-0b21-aee5-85f4-343a0d0065cf");
            List<CommunicationIdentifier> targets = new ArrayList<CommunicationIdentifier>() {
                {
                    add(new CommunicationUserIdentifier(targetUserId));
                }
            };

            String ngrok = "https://localhost";

            CreateCallResult createCallResult = client.createCall(sourceUser, targets, ngrok).block();

            assertNotNull(createCallResult);
            waitForOperationCompletion(10000);

            CallConnectionProperties callConnectionProperties =
                client.getCallConnectionAsync(createCallResult.getCallConnectionProperties().getCallConnectionId()).getCallProperties().block();

            String serverCallId = callConnectionProperties.getServerCallId();

            callConnectionId = callConnectionProperties.getCallConnectionId();

            CallRecordingAsync callRecording = client.getCallRecordingAsync();
            RecordingStateResult recordingResponse = callRecording.startRecording(
                new StartRecordingOptions(new ServerCallLocator(serverCallId))
                    .setRecordingStateCallbackUrl(ngrok))
                .block();
            assertNotNull(recordingResponse);
            String recordingId = recordingResponse.getRecordingId();
            assertNotNull(recordingId);
            waitForOperationCompletion(10000);

            recordingResponse = callRecording.getRecordingState(recordingId).block();
            assertNotNull(recordingResponse);
            assertEquals(RecordingState.ACTIVE, recordingResponse.getRecordingState());

            callRecording.pauseRecording(recordingId).block();
            waitForOperationCompletion(10000);
            recordingResponse = callRecording.getRecordingState(recordingId).block();
            assertNotNull(recordingResponse);
            assertEquals(RecordingState.INACTIVE, recordingResponse.getRecordingState());

            callRecording.resumeRecording(recordingId).block();
            waitForOperationCompletion(10000);
            recordingResponse = callRecording.getRecordingState(recordingId).block();
            assertNotNull(recordingResponse);
            assertEquals(RecordingState.ACTIVE, recordingResponse.getRecordingState());

            callRecording.stopRecording(recordingId).block();
            waitForOperationCompletion(10000);
            assertThrows(CallingServerErrorException.class, () -> callRecording.getRecordingState(recordingId).block());
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        } finally {
            CallConnectionAsync callConnection = client.getCallConnectionAsync(callConnectionId);
            callConnection.hangUp(true).block();
        }
    }
}
