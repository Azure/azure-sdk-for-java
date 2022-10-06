// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallConnectionState;
import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.CreateCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class CallMediaAsyncLiveTests extends CallAutomationLiveTestBase {
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void playMediaInACall(HttpClient httpClient) {
        /* Test case:
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target and A PSTN target.
         * 3. play a media file to all participants.
         * 4. terminate the call.
         */

        CallAutomationAsyncClient callClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("playMediaInACall", next))
            .buildAsyncClient();

        CommunicationIdentityAsyncClient identityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("playMediaInACall", next))
            .buildAsyncClient();

        try {
            String callbackUrl = "https://localhost";
            CommunicationIdentifier source = identityClient.createUser().block();
            List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(new PhoneNumberIdentifier(PHONE_USER_1),
                new CommunicationUserIdentifier(ACS_USER_1)));

            CreateCallOptions createCallOptions = new CreateCallOptions(source, targets, callbackUrl)
                .setSourceCallerId(ACS_RESOURCE_PHONE);
            Response<CreateCallResult> result = callClient.createCallWithResponse(createCallOptions).block();
            assertNotNull(result);
            assertNotNull(result.getValue());
            assertNotNull(result.getValue().getCallConnection());
            assertNotNull(result.getValue().getCallConnectionProperties());
            waitForOperationCompletion(15000);

            CallConnectionAsync callConnectionAsync = callClient.getCallConnectionAsync(result.getValue().getCallConnectionProperties().getCallConnectionId());
            assertNotNull(callConnectionAsync);
            CallConnectionProperties callConnectionProperties = callConnectionAsync.getCallProperties().block();
            assertNotNull(callConnectionProperties);
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());

            CallMediaAsync callMediaAsync = callConnectionAsync.getCallMediaAsync();
            callMediaAsync.playToAll(new FileSource().setUri(MEDIA_SOURCE)).block();
            waitForOperationCompletion(5000);

            callConnectionAsync.hangUp(true).block();
            waitForOperationCompletion(5000);
            assertThrows(Exception.class, () -> callConnectionAsync.getCallProperties().block());
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void recognizeDtmfInACall(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. prompt and recognize dtmf tones from target participant
         * 5. hang up the call.
         */

        CallAutomationAsyncClient callClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("recognizeDtmfInACall", next))
            .buildAsyncClient();

        CommunicationIdentityAsyncClient identityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("recognizeDtmfInACall", next))
            .buildAsyncClient();

        try {
            String callbackUrl = "https://localhost";
            CommunicationIdentifier source = identityClient.createUser().block();
            PhoneNumberIdentifier targetUser = new PhoneNumberIdentifier(PHONE_USER_1);
            List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(targetUser));

            CreateCallOptions createCallOptions = new CreateCallOptions(source, targets, callbackUrl)
                .setSourceCallerId(ACS_RESOURCE_PHONE);

            Response<CreateCallResult> callResponse = callClient.createCallWithResponse(createCallOptions).block();
            assertNotNull(callResponse);
            assertNotNull(callResponse.getValue());
            assertNotNull(callResponse.getValue().getCallConnection());
            assertNotNull(callResponse.getValue().getCallConnectionProperties());
            waitForOperationCompletion(15000);

            CallConnectionAsync callConnectionAsync = callClient.getCallConnectionAsync(callResponse.getValue().getCallConnectionProperties().getCallConnectionId());
            assertNotNull(callConnectionAsync);
            CallConnectionProperties callConnectionProperties = callConnectionAsync.getCallProperties().block();
            assertNotNull(callConnectionProperties);
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());

            CallMediaAsync callMediaAsync = callConnectionAsync.getCallMediaAsync();

            List<DtmfTone> stopTones = new ArrayList<>();
            stopTones.add(DtmfTone.POUND);
            CallMediaRecognizeDtmfOptions callMediaRecognizeDtmfOptions = new CallMediaRecognizeDtmfOptions(targetUser, 5)
                .setStopTones(stopTones)
                .setInterToneTimeout(Duration.ofSeconds(5));
            callMediaRecognizeDtmfOptions.setInitialSilenceTimeout(Duration.ofSeconds(15));
            callMediaRecognizeDtmfOptions.setPlayPrompt(new FileSource().setUri(MEDIA_SOURCE));

            Response<Void> dtmfResponse = callMediaAsync.startRecognizingWithResponse(new CallMediaRecognizeDtmfOptions(targetUser, 5)).block();
            assertNotNull(dtmfResponse);
            assertEquals(202, dtmfResponse.getStatusCode());

            callConnectionAsync.hangUp(true).block();
            waitForOperationCompletion(5000);
            assertThrows(Exception.class, () -> callConnectionAsync.getCallProperties().block());

        } catch (Exception ex) {
            fail("Unexpeceted exception received", ex);
        }
    }
}
