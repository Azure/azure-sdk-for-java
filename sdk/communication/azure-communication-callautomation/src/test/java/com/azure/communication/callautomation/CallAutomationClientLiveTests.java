// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallConnectionProperties;
import com.azure.communication.callautomation.models.CallConnectionState;
import com.azure.communication.callautomation.models.CreateCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.ListParticipantsResult;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class CallAutomationClientLiveTests extends CallAutomationLiveTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void createVOIPCallAndHangupTest(HttpClient httpClient) {
        /* Test case: ACS to ACS call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to one ACS target.
         * 3. get updated call properties and check for the connected state.
         * 4. hang up the call.
         * 5. once call is hung up, verify that call connection cannot be found.
         */
        CallAutomationClient callClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("createVOIPCallAndHangupTest", next))
            .buildClient();

        CommunicationIdentityClient identityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("createVOIPCallAndHangupTest", next))
            .buildClient();

        try {
            String callbackUrl = "https://localhost";
            CommunicationIdentifier source = identityClient.createUser();
            List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(new CommunicationUserIdentifier(ACS_USER_1)));

            CreateCallResult result = callClient.createCall(source, targets, callbackUrl);
            assertNotNull(result);
            assertNotNull(result.getCallConnection());
            assertNotNull(result.getCallConnectionProperties());
            waitForOperationCompletion(10000);

            CallConnection callConnection = callClient.getCallConnection(result.getCallConnectionProperties().getCallConnectionId());
            assertNotNull(callConnection);
            CallConnectionProperties callConnectionProperties = callConnection.getCallProperties();
            assertNotNull(callConnectionProperties);

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
    public void createPSTNCallAndHangupTest(HttpClient httpClient) {
        /* Test case: ACS to PSTN call
         * 1. create a CallAutomationClient.
         * 2. create a call from source to a PSTN target.
         * 3. get updated call properties and check for the connected state.
         * 4. hang up the call.
         * 5. once call is hung up, verify the call connection cannot be found.
         */
        CallAutomationClient callClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("createPSTNCallAndHangupTest", next))
            .buildClient();

        CommunicationIdentityClient identityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("createPSTNCallAndHangupTest", next))
            .buildClient();

        try {
            String callbackUrl = "https://localhost";
            CommunicationIdentifier source = identityClient.createUser();
            List<CommunicationIdentifier> targets = new ArrayList<>(Collections.singletonList(new PhoneNumberIdentifier(PHONE_USER_1)));

            CreateCallOptions createCallOptions = new CreateCallOptions(source, targets, callbackUrl)
                .setSourceCallerId(ACS_RESOURCE_PHONE);
            Response<CreateCallResult> createCallResultResponse = callClient.createCallWithResponse(createCallOptions, null);
            assertNotNull(createCallResultResponse);
            CreateCallResult createCallResult = createCallResultResponse.getValue();
            assertNotNull(createCallResult);
            assertNotNull(createCallResult.getCallConnection());
            assertNotNull(createCallResult.getCallConnectionProperties());
            waitForOperationCompletion(10000);

            CallConnection callConnection = callClient.getCallConnection(createCallResult.getCallConnectionProperties().getCallConnectionId());
            assertNotNull(callConnection);
            CallConnectionProperties callConnectionProperties = callConnection.getCallProperties();
            assertNotNull(callConnectionProperties);

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
    public void startACallWithMultipleTargetsTest(HttpClient httpClient) {
        /* Test case:
         * 1. create a CallAutomationClient.
         * 2. create a group call.
         * 3. get updated call properties and check for the connected state.
         * 4. hang up the call for everyone.
         * 5. verify that call connection cannot be found.
         */

        CallAutomationClient callClient = getCallAutomationClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("startACallWithMultipleTargetsTest", next))
            .buildClient();

        CommunicationIdentityClient identityClient = getCommunicationIdentityClientUsingConnectionString(httpClient)
            .addPolicy((context, next) -> logHeaders("startACallWithMultipleTargetsTest", next))
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

            callConnection.hangUp(true);
            waitForOperationCompletion(5000);
            assertThrows(Exception.class, callConnection::getCallProperties);
        } catch (Exception ex) {
            fail("Unexpected exception received", ex);
        }
    }
}
