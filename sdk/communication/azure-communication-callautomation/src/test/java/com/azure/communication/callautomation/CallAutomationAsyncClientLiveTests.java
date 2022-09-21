// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallSource;
import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.CreateCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.RecordingState;
import com.azure.communication.callautomation.models.RecordingStateResult;
import com.azure.communication.callautomation.models.ServerCallLocator;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class CallAutomationAsyncClientLiveTests extends CallAutomationLiveTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void createVOIPCall(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. hang up the call.
         * 5. once call is hung up, verify that call connection cannot be found.
         */

        CallAutomationAsyncClient callClient = getCallingServerClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("createVOIPCall", next))
            .buildAsyncClient();

        CommunicationIdentityAsyncClient identityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("createVOIPCall", next))
            .buildAsyncClient();

        try {
            String callbackUrl = "https://localhost";
            CommunicationIdentifier source = identityClient.createUser().block();
            List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(new CommunicationUserIdentifier(ACS_USER_1)));

            CreateCallOptions createCallOptions = new CreateCallOptions(source, targets, callbackUrl);
            Response<CreateCallResult> result = callClient.createCallWithResponse(createCallOptions).block();
            assertNotNull(result);
            assertNotNull(result.getValue());
            assertNotNull(result.getValue().getCallConnection());
            assertNotNull(result.getValue().getCallConnectionProperties());

//            recordingResponse = callRecording.getRecordingState(recordingId);
//            assertNotNull(recordingResponse);
//            assertEquals(RecordingState.ACTIVE, recordingResponse.getRecordingState());
//
//            callRecording.pauseRecording(recordingId);
//            recordingResponse = callRecording.getRecordingState(recordingId);
//            assertNotNull(recordingResponse);
//            assertEquals(RecordingState.INACTIVE, recordingResponse.getRecordingState());
//
//            callRecording.resumeRecording(recordingId);
//            recordingResponse = callRecording.getRecordingState(recordingId);
//            assertNotNull(recordingResponse);
//            assertEquals(RecordingState.ACTIVE, recordingResponse.getRecordingState());
//
//            callRecording.stopRecording(recordingId);
//            assertThrows(CallingServerErrorException.class, () -> callRecording.getRecordingState(recordingId));
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }
}
