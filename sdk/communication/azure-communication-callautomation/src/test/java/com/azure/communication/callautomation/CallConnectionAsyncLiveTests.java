// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallConnectionState;
import com.azure.communication.callautomation.models.CreateCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.ListParticipantsResult;
import com.azure.communication.callautomation.models.RemoveParticipantsResult;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferToParticipantCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class CallConnectionAsyncLiveTests extends CallAutomationLiveTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void removeAPSTNUserFromAnOngoingCallTest(HttpClient httpClient) {
        /* Test case:
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target and A PSTN target.
         * 3. get updated call properties and check for the connected state and check for 3 participants in total.
         * 4. remove the PSTN call leg by calling RemoveParticipants.
         * 5. verify existing call is still ongoing and has 2 participants now.
         */

        CallAutomationAsyncClient callClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("removeAPSTNUserFromAnOngoingCallTest", next))
            .buildAsyncClient();

        CommunicationIdentityAsyncClient identityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("removeAPSTNUserFromAnOngoingCallTest", next))
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
            assertEquals(2, callConnectionProperties.getTargets().size());

            Response<ListParticipantsResult> listParticipantsResultResponse = callConnectionAsync.listParticipantsWithResponse().block();
            assertNotNull(listParticipantsResultResponse);
            assertEquals(3, listParticipantsResultResponse.getValue().getValues().size());

            RemoveParticipantsResult removeParticipantsResult = callConnectionAsync.removeParticipants(
                new ArrayList<>(Arrays.asList(new PhoneNumberIdentifier(PHONE_USER_1)))).block();

            callConnectionProperties = callConnectionAsync.getCallProperties().block();
            assertNotNull(callConnectionProperties);
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());

            listParticipantsResultResponse = callConnectionAsync.listParticipantsWithResponse().block();
            assertNotNull(listParticipantsResultResponse);
            assertEquals(2, listParticipantsResultResponse.getValue().getValues().size());

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
    public void removeAPSTNUserAndAcsUserFromAnOngoingCallTest(HttpClient httpClient) {
        /* Test case:
         * 1. create a CallAutomationClient.
         * 2. create a call from source to 2 ACS targets and A PSTN target.
         * 3. get updated call properties and check for the connected state and check for 4 participants in total.
         * 4. remove the PSTN call leg and 1 Acs call leg by calling RemoveParticipants.
         * 5. verify existing call is still ongoing and has 2 participants now.
         */

        CallAutomationAsyncClient callClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("removeAPSTNUserAndAcsUserFromAnOngoingCallTest", next))
            .buildAsyncClient();

        CommunicationIdentityAsyncClient identityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("removeAPSTNUserAndAcsUserFromAnOngoingCallTest", next))
            .buildAsyncClient();

        try {
            String callbackUrl = "https://localhost";
            CommunicationIdentifier source = identityClient.createUser().block();
            List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(new PhoneNumberIdentifier(PHONE_USER_1),
                new CommunicationUserIdentifier(ACS_USER_1), new CommunicationUserIdentifier(ACS_USER_2)));

            CreateCallOptions createCallOptions = new CreateCallOptions(source, targets, callbackUrl)
                .setSourceCallerId(ACS_RESOURCE_PHONE);
            Response<CreateCallResult> result = callClient.createCallWithResponse(createCallOptions).block();
            assertNotNull(result);
            assertNotNull(result.getValue());
            assertNotNull(result.getValue().getCallConnection());
            assertNotNull(result.getValue().getCallConnectionProperties());
            waitForOperationCompletion(20000);

            CallConnectionAsync callConnectionAsync = callClient.getCallConnectionAsync(result.getValue().getCallConnectionProperties().getCallConnectionId());
            assertNotNull(callConnectionAsync);
            CallConnectionProperties callConnectionProperties = callConnectionAsync.getCallProperties().block();
            assertNotNull(callConnectionProperties);
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());
            assertEquals(3, callConnectionProperties.getTargets().size());

            Response<ListParticipantsResult> listParticipantsResultResponse = callConnectionAsync.listParticipantsWithResponse().block();
            assertNotNull(listParticipantsResultResponse);
            assertEquals(4, listParticipantsResultResponse.getValue().getValues().size());

            callConnectionAsync.removeParticipants(new ArrayList<>(Arrays.asList(new PhoneNumberIdentifier(PHONE_USER_1),
                new CommunicationUserIdentifier(ACS_USER_2)))).block();

            callConnectionProperties = callConnectionAsync.getCallProperties().block();
            assertNotNull(callConnectionProperties);
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());

            listParticipantsResultResponse = callConnectionAsync.listParticipantsWithResponse().block();
            assertNotNull(listParticipantsResultResponse);
            assertEquals(2, listParticipantsResultResponse.getValue().getValues().size());

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
    public void transferACallFromOneUserToAnotherUserTest(HttpClient httpClient) {
        /* Test case:
         * 1. create a CallAutomationClient.
         * 2. create a call to a single target.
         * 3. get updated call properties and check for the connected state.
         * 4. transfer the call to another target.
         */

        CallAutomationAsyncClient callClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("removeAPSTNUserAndAcsUserFromAnOngoingCallTest", next))
            .buildAsyncClient();

        CommunicationIdentityAsyncClient identityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("removeAPSTNUserAndAcsUserFromAnOngoingCallTest", next))
            .buildAsyncClient();

        try {
            String callbackUrl = "https://localhost";
            CommunicationIdentifier source = identityClient.createUser().block();
            List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(new PhoneNumberIdentifier(PHONE_USER_1)));

            CreateCallOptions createCallOptions = new CreateCallOptions(source, targets, callbackUrl)
                .setSourceCallerId(ACS_RESOURCE_PHONE);
            Response<CreateCallResult> result = callClient.createCallWithResponse(createCallOptions).block();
            assertNotNull(result);
            assertNotNull(result.getValue());
            assertNotNull(result.getValue().getCallConnection());
            assertNotNull(result.getValue().getCallConnectionProperties());
            waitForOperationCompletion(10000);

            CallConnectionAsync callConnectionAsync = callClient.getCallConnectionAsync(result.getValue().getCallConnectionProperties().getCallConnectionId());
            assertNotNull(callConnectionAsync);
            CallConnectionProperties callConnectionProperties = callConnectionAsync.getCallProperties().block();
            assertNotNull(callConnectionProperties);
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());

            TransferToParticipantCallOptions transferToParticipantCallOptions = new TransferToParticipantCallOptions(new CommunicationUserIdentifier(ACS_USER_1));
            Response<TransferCallResult> transferCallResultResponse = callConnectionAsync.transferToParticipantCallWithResponse(transferToParticipantCallOptions).block();
            assertNotNull(transferCallResultResponse);
            assertNotNull(transferCallResultResponse.getValue());

            waitForOperationCompletion(5000);
            assertThrows(Exception.class, () -> callConnectionAsync.getCallProperties().block());
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }
}
