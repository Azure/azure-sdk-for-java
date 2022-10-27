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
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.identity.CommunicationIdentityClient;
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

public class CallConnectionLiveTests extends CallAutomationLiveTestBase {

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

        CallAutomationClient callClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("removeAPSTNUserFromAnOngoingCallTest", next))
            .buildClient();

        CommunicationIdentityClient identityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("removeAPSTNUserFromAnOngoingCallTest", next))
            .buildClient();

        try {
            String callbackUrl = "https://localhost";
            CommunicationIdentifier source = identityClient.createUser();
            List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(new PhoneNumberIdentifier(PHONE_USER_1),
                new CommunicationUserIdentifier(ACS_USER_1)));

            CreateCallOptions createCallOptions = new CreateCallOptions(source, targets, callbackUrl)
                .setSourceCallerId(ACS_RESOURCE_PHONE);
            Response<CreateCallResult> createCallResultResponse = callClient.createCallWithResponse(createCallOptions, null);
            assertNotNull(createCallResultResponse);
            CreateCallResult createCallResult = createCallResultResponse.getValue();
            assertNotNull(createCallResult);
            assertNotNull(createCallResult.getCallConnection());
            assertNotNull(createCallResult.getCallConnectionProperties());
            waitForOperationCompletion(15000);

            CallConnection callConnection = callClient.getCallConnection(createCallResult.getCallConnectionProperties().getCallConnectionId());
            assertNotNull(callConnection);
            CallConnectionProperties callConnectionProperties = callConnection.getCallProperties();
            assertNotNull(callConnectionProperties);
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());
            assertEquals(2, callConnectionProperties.getTargets().size());

            ListParticipantsResult listParticipantsResult = callConnection.listParticipants();
            assertNotNull(listParticipantsResult);
            assertEquals(3, listParticipantsResult.getValues().size());

            RemoveParticipantsResult removeParticipantsResult = callConnection.removeParticipants(
                new ArrayList<>(Arrays.asList(new PhoneNumberIdentifier(PHONE_USER_1))));

            callConnectionProperties = callConnection.getCallProperties();
            assertNotNull(callConnectionProperties);
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());

            listParticipantsResult = callConnection.listParticipants();
            assertNotNull(listParticipantsResult);
            assertEquals(2, listParticipantsResult.getValues().size());

            callConnection.hangUp(true);
            waitForOperationCompletion(5000);
            assertThrows(Exception.class, callConnection::getCallProperties);
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

        CallAutomationClient callClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("removeAPSTNUserAndAcsUserFromAnOngoingCallTest", next))
            .buildClient();

        CommunicationIdentityClient identityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("removeAPSTNUserAndAcsUserFromAnOngoingCallTest", next))
            .buildClient();

        try {
            String callbackUrl = "https://localhost";
            CommunicationIdentifier source = identityClient.createUser();
            List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(new PhoneNumberIdentifier(PHONE_USER_1),
                new CommunicationUserIdentifier(ACS_USER_1), new CommunicationUserIdentifier(ACS_USER_2)));

            CreateCallOptions createCallOptions = new CreateCallOptions(source, targets, callbackUrl)
                .setSourceCallerId(ACS_RESOURCE_PHONE);
            Response<CreateCallResult> createCallResultResponse = callClient.createCallWithResponse(createCallOptions, null);
            assertNotNull(createCallResultResponse);
            CreateCallResult createCallResult = createCallResultResponse.getValue();
            assertNotNull(createCallResult);
            assertNotNull(createCallResult.getCallConnection());
            assertNotNull(createCallResult.getCallConnectionProperties());
            waitForOperationCompletion(20000);

            CallConnection callConnection = callClient.getCallConnection(createCallResult.getCallConnectionProperties().getCallConnectionId());
            assertNotNull(callConnection);
            CallConnectionProperties callConnectionProperties = callConnection.getCallProperties();
            assertNotNull(callConnectionProperties);
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());
            assertEquals(3, callConnectionProperties.getTargets().size());

            ListParticipantsResult listParticipantsResult = callConnection.listParticipants();
            assertNotNull(listParticipantsResult);
            assertEquals(4, listParticipantsResult.getValues().size());

            callConnection.removeParticipants(new ArrayList<>(Arrays.asList(new PhoneNumberIdentifier(PHONE_USER_1),
                new CommunicationUserIdentifier(ACS_USER_2))));

            callConnectionProperties = callConnection.getCallProperties();
            assertNotNull(callConnectionProperties);
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());

            listParticipantsResult = callConnection.listParticipants();
            assertNotNull(listParticipantsResult);
            assertEquals(2, listParticipantsResult.getValues().size());

            callConnection.hangUp(true);
            waitForOperationCompletion(5000);
            assertThrows(Exception.class, callConnection::getCallProperties);
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

        CallAutomationClient callClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("transferACallFromOneUserToAnotherUserTest", next))
            .buildClient();

        CommunicationIdentityClient identityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("transferACallFromOneUserToAnotherUserTest", next))
            .buildClient();

        try {
            String callbackUrl = "https://localhost";
            CommunicationIdentifier source = identityClient.createUser();
            List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(new PhoneNumberIdentifier(PHONE_USER_1)));

            CreateCallOptions createCallOptions = new CreateCallOptions(source, targets, callbackUrl)
                .setSourceCallerId(ACS_RESOURCE_PHONE);
            Response<CreateCallResult> createCallResultResponse = callClient.createCallWithResponse(createCallOptions, null);
            assertNotNull(createCallResultResponse);
            CreateCallResult createCallResult = createCallResultResponse.getValue();
            assertNotNull(createCallResult);
            assertNotNull(createCallResult.getCallConnection());
            assertNotNull(createCallResult.getCallConnectionProperties());
            waitForOperationCompletion(12000);

            CallConnection callConnection = callClient.getCallConnection(createCallResult.getCallConnectionProperties().getCallConnectionId());
            assertNotNull(callConnection);
            CallConnectionProperties callConnectionProperties = callConnection.getCallProperties();
            assertNotNull(callConnectionProperties);
            assertEquals(CallConnectionState.CONNECTED, callConnectionProperties.getCallConnectionState());

            TransferCallResult transferCallResult = callConnection.transferToParticipantCall(new CommunicationUserIdentifier(ACS_USER_1));
            assertNotNull(transferCallResult);

            waitForOperationCompletion(5000);
            assertThrows(Exception.class, callConnection::getCallProperties);
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }
}
