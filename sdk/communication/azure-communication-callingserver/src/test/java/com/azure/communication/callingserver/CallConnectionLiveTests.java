// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.converters.CallLocatorConverter;
import com.azure.communication.callingserver.models.*;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class CallConnectionLiveTests extends CallingServerTestBase {

    private final String fromUser = getNewUserId();
    private final String toUser = getNewUserId();

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreatePlayCancelHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreatePlayCancelHangupScenario");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);

        CallingServerTestUtils.validateCallConnection(callConnection);

        try {
            // Play Audio
            String playAudioOperationContext = UUID.randomUUID().toString();
            PlayAudioOptions playAudioOptions = new PlayAudioOptions()
                .setAudioFileId(UUID.randomUUID().toString())
                .setCallbackUri(URI.create(CALLBACK_URI))
                .setLoop(false)
                .setOperationContext(playAudioOperationContext);
            PlayAudioResult playAudioResult = callConnection.playAudio(
                URI.create(AUDIO_FILE_URI), playAudioOptions);
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult);

            // Cancel All Media Operations
            callConnection.cancelAllMediaOperations();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            callConnection.hangup();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreatePlayCancelHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreatePlayCancelHangupScenarioWithResponse");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        Response<CallConnection> callConnectionResponse =
            callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options,
                null);

        CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
        CallConnection callConnection = callConnectionResponse.getValue();

        try {
            // Play Audio
            String operationContext = UUID.randomUUID().toString();
            PlayAudioOptions playAudioOptions =
                new PlayAudioOptions()
                    .setLoop(false)
                    .setAudioFileId(UUID.randomUUID().toString())
                    .setCallbackUri(URI.create(CALLBACK_URI))
                    .setOperationContext(operationContext);
            Response<PlayAudioResult> playAudioResult =
                callConnection.playAudioWithResponse(URI.create(AUDIO_FILE_URI), playAudioOptions, null);
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResult);

            // Cancel All Media Operations
            callConnection.cancelAllMediaOperationsWithResponse(null);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateAddRemoveHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateAddRemoveHangupScenario");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);

        CallingServerTestUtils.validateCallConnection(callConnection);

        try {
            // Add User
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            AddParticipantResult addParticipantResult = callConnection.addParticipant(addUser, null, operationContext);
            assert addParticipantResult != null;
            // Remove User
            callConnection.removeParticipant(addUser);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            callConnection.hangup();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateAddRemoveHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreateAddRemoveHangupScenarioWithResponse");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        Response<CallConnection> callConnectionResponse =
            callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options,
                null);

        CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
        CallConnection callConnection = callConnectionResponse.getValue();

        try {
            // Add User
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            Response<AddParticipantResult> addParticipantResponse = callConnection
                .addParticipantWithResponse(
                    addUser,
                    null,
                    operationContext,
                    Context.NONE);
            CallingServerTestUtils.validateAddParticipantResponse(addParticipantResponse);

            // Remove User
            Response<Void> removeParticipantResponse =
                callConnection.removeParticipantWithResponse(addUser, null);
            CallingServerTestUtils.validateResponse(removeParticipantResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateJoinHangupScenarioWithConnectionStringClient(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateJoinHangupScenarioWithConnectionStringClient");
        runCreateJoinHangupScenario(callingServerClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateJoinHangupScenarioWithTokenCredentialClient(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingTokenCredential(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateJoinHangupScenarioWithTokenCredentialClient");
        runCreateJoinHangupScenario(callingServerClient);
    }

    private void runCreateJoinHangupScenario(CallingServerClient callingServerClient) {

        try {
            // Establish a call
            CreateCallOptions createCallOptions = new CreateCallOptions(
                URI.create(CALLBACK_URI),
                Collections.singletonList(CallMediaType.AUDIO),
                Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

            createCallOptions.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

            CallConnection callConnection = callingServerClient.createCallConnection(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                createCallOptions);

            CallingServerTestUtils.validateCallConnection(callConnection);

            // Join
            /*
              Waiting for an update to be able to get this serverCallId when using
              createCallConnection()
             */
            // serverCallId looks like this: "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L3VodHNzZEZ3NFVHX1J4d1lHYWlLRmc_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4"
            String serverCallId = CallLocatorConverter.convert(callConnection.getCall().getCallLocator()).getServerCallId();

            JoinCallOptions joinCallOptions = new JoinCallOptions(
                URI.create(CALLBACK_URI),
                Collections.singletonList(CallMediaType.AUDIO),
                Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
            CallConnection joinedCallConnection =
                callingServerClient.joinCall(new ServerCallLocator(serverCallId), new CommunicationUserIdentifier(toUser), joinCallOptions);
            CallingServerTestUtils.validateCallConnection(joinedCallConnection);

            //Hangup
            callConnection.hangup();
            joinedCallConnection.hangup();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateJoinHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreateJoinHangupScenarioWithResponse");

        try {
            // Establish a call
            CreateCallOptions createCallOptions = new CreateCallOptions(
                URI.create(CALLBACK_URI),
                Collections.singletonList(CallMediaType.AUDIO),
                Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

            createCallOptions.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

            Response<CallConnection> callConnectionResponse = callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                createCallOptions,
                null);

            CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
            CallConnection callConnection = callConnectionResponse.getValue();

            // Join
            // serverCallId looks like this: "aHR0cHM6Ly94LWNvbnYtdXN3ZS0wMS5jb252LnNreXBlLmNvbS9jb252L3VodHNzZEZ3NFVHX1J4d1lHYWlLRmc_aT0yJmU9NjM3NTg0Mzk2NDM5NzQ5NzY4"
            String serverCallId = CallLocatorConverter.convert(callConnection.getCall().getCallLocator()).getServerCallId();
            JoinCallOptions joinCallOptions = new JoinCallOptions(
                URI.create(CALLBACK_URI),
                Collections.singletonList(CallMediaType.AUDIO),
                Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));
            Response<CallConnection> joinedCallConnectionResponse =
                callingServerClient.joinCallWithResponse(
                    new ServerCallLocator(serverCallId),
                    new CommunicationUserIdentifier(toUser),
                    joinCallOptions,
                    null);
            CallingServerTestUtils.validateJoinCallConnectionResponse(joinedCallConnectionResponse);
            CallConnection joinedCallConnection = joinedCallConnectionResponse.getValue();

            //Hangup
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
            Response<Void> joinCallHangupResponse = joinedCallConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(joinCallHangupResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateAddMuteUnmuteGetParticipantRemoveHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateAddMuteUnmuteGetParticipantRemoveHangupScenario");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);

        CallingServerTestUtils.validateCallConnection(callConnection);

        try {
            // Add User
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            AddParticipantResult addParticipantResult = callConnection.addParticipant(addUser, null, operationContext);
            assert addParticipantResult != null;
            // Mute User
            callConnection.muteParticipant(addUser);
            // Get User
            CallParticipant getMutedParticipantresult = callConnection.getParticipant(addUser);
            assertTrue(getMutedParticipantresult.isMuted());
            // Unmute User
            callConnection.unmuteParticipant(addUser);
            // Get User
            CallParticipant getUnmutedParticipantresult = callConnection.getParticipant(addUser);
            assertFalse(getUnmutedParticipantresult.isMuted());
            // Get Users
            List <CallParticipant> getParticipantsResult = callConnection.getParticipants();
            assertTrue(getParticipantsResult.size() > 2);
            // Remove User
            callConnection.removeParticipant(addUser);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            callConnection.hangup();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateAddMuteUnmuteGetParticipantRemoveHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreateAddMuteUnmuteGetParticipantRemoveHangupScenarioWithResponse");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        Response<CallConnection> callConnectionResponse =
            callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options,
                null);

        CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
        CallConnection callConnection = callConnectionResponse.getValue();

        try {
            // Add User
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            Response<AddParticipantResult> addParticipantResponse = callConnection
                .addParticipantWithResponse(
                    addUser,
                    null,
                    operationContext,
                    Context.NONE);
            CallingServerTestUtils.validateAddParticipantResponse(addParticipantResponse);
            
            // Mute User
            Response<Void> muteParticipantResponse =
                callConnection.muteParticipantWithResponse(addUser, null);
            CallingServerTestUtils.validateApiResponse(muteParticipantResponse);

            // Get User
            Response<CallParticipant> getMutedParticipantresponse =
                callConnection.getParticipantWithResponse(addUser, null);
            assertTrue(getMutedParticipantresponse.getValue().isMuted());
            CallingServerTestUtils.validateGetParticipantResponse(getMutedParticipantresponse);

            // Unmute User
            Response<Void> unmuteParticipantResponse = 
                callConnection.unmuteParticipantWithResponse(addUser, null);
            CallingServerTestUtils.validateApiResponse(unmuteParticipantResponse);

            // Get User
            Response<CallParticipant> getUnmutedParticipantresponse =
                callConnection.getParticipantWithResponse(addUser, null);
            assertFalse(getUnmutedParticipantresponse.getValue().isMuted());
            CallingServerTestUtils.validateGetParticipantResponse(getUnmutedParticipantresponse);

            // Get Users
            Response<List<CallParticipant>> getParticipantsResponse = 
                callConnection.getParticipantsWithResponse(null);
            assertTrue(getParticipantsResponse.getValue().size() > 2);
            CallingServerTestUtils.validateGetParticipantsResponse(getParticipantsResponse);

            // Remove User
            Response<Void> removeParticipantResponse =
                callConnection.removeParticipantWithResponse(addUser, null);
            CallingServerTestUtils.validateResponse(removeParticipantResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateAddRemoveFromDefaultAudioGroupAddToDefaultAudioGroupRemoveHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateAddRemoveFromDefaultAudioGroupAddToDefaultAudioGroupRemoveHangupScenario");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);

        CallingServerTestUtils.validateCallConnection(callConnection);

        try {
            // Add User
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            AddParticipantResult addParticipantResult = callConnection.addParticipant(addUser, null, operationContext);
            assert addParticipantResult != null;
            // Remove Participant from Default Audio Group
            callConnection.removeParticipantFromDefaultAudioGroup(addUser);  
            // Add Participant To Default Audio Group
            callConnection.addParticipantToDefaultAudioGroup(addUser);
            // Remove User
            callConnection.removeParticipant(addUser);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            callConnection.hangup();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateAddRemoveFromDefaultAudioGroupAddToDefaultAudioGroupRemoveHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreateAddRemoveFromDefaultAudioGroupAddToDefaultAudioGroupRemoveHangupScenarioWithResponse");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        Response<CallConnection> callConnectionResponse =
            callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options,
                null);

        CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
        CallConnection callConnection = callConnectionResponse.getValue();

        try {
            // Add User
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            Response<AddParticipantResult> addParticipantResponse = callConnection
                .addParticipantWithResponse(
                    addUser,
                    null,
                    operationContext,
                    Context.NONE);
            CallingServerTestUtils.validateAddParticipantResponse(addParticipantResponse);

            // Get Call
            Response<CallConnectionProperties> getCallResponse =
                callConnection.getCallWithResponse(null);
            assertEquals(getCallResponse.getValue().getCallConnectionId(), callConnection.getCallConnectionId());
            CallingServerTestUtils.validateGetCallResponse(getCallResponse);

            // Remove Participant from Default Audio Group
            Response<Void> removeParticipantFromDefaultAudioGroupResponse =
                callConnection.removeParticipantFromDefaultAudioGroupWithResponse(addUser, null);
            CallingServerTestUtils.validateApiResponse(removeParticipantFromDefaultAudioGroupResponse);

            // Add Participant To Default Audio Group
            Response<Void> addParticipantToDefaultAudioGroupResponse = 
                callConnection.addParticipantToDefaultAudioGroupWithResponse(addUser, null);
            CallingServerTestUtils.validateApiResponse(addParticipantToDefaultAudioGroupResponse);

            // Remove User
            Response<Void> removeParticipantResponse =
                callConnection.removeParticipantWithResponse(addUser, null);
            CallingServerTestUtils.validateResponse(removeParticipantResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateKeepAliveDeleteCallScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateKeepAliveDeleteCallScenario");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);

        CallingServerTestUtils.validateCallConnection(callConnection);

        try {
            // Keep Call Alive
            callConnection.keepAlive();
            // Delete Call
            callConnection.delete();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } 
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateKeepAliveDeleteCallScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreateKeepAliveDeleteCallScenariooWithResponse");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        Response<CallConnection> callConnectionResponse =
            callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options,
                null);

        CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
        CallConnection callConnection = callConnectionResponse.getValue();

        try {
            // Keep Call Alive
            Response<Void> keepAliveResponse =
                callConnection.keepAliveWithResponse(null);
            CallingServerTestUtils.validateApiResponse(keepAliveResponse);

            // Delete Call
            Response<Void> deleteCallResponse = 
                callConnection.deleteWithResponse(null);
            CallingServerTestUtils.validateResponse(deleteCallResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateTransferToParticipantScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateTransferToParticipantScenario");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);

        CallingServerTestUtils.validateCallConnection(callConnection);

        try {
            // Transfer Call To Participant
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier transferUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            TransferCallResult transferParticipantResult = callConnection.transferToParticipant(transferUser, null, null, operationContext);
            assert transferParticipantResult != null;
            assertEquals(CallingOperationStatus.RUNNING, transferParticipantResult.getStatus());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateTransferToParticipantScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreateTransferToParticipantScenarioWithResponse");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        Response<CallConnection> callConnectionResponse =
            callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options,
                null);

        CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
        CallConnection callConnection = callConnectionResponse.getValue();

        try {
            // Transfer Call To Participant
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier transferUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            Response<TransferCallResult> transferParticipantResponse = callConnection
                .transferToParticipantWithResponse(
                    transferUser,
                    null,
                    operationContext,
                    null,
                    null);
            CallingServerTestUtils.validateTransferResponse(transferParticipantResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @Disabled("Skip test as it is not working now")
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateTransferCallScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateTransferCallScenario");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);

        CallingServerTestUtils.validateCallConnection(callConnection);

        try {
            // Transfer Call
            String operationContext = UUID.randomUUID().toString();
            String targetCallConnectionId = getTargetCallConnectionId();
            TransferCallResult transferCallResult = callConnection.transferToCall(targetCallConnectionId, null, operationContext);
            assert transferCallResult != null;
            assertEquals(CallingOperationStatus.RUNNING, transferCallResult.getStatus());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @Disabled("Skip test as it is not working now")
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateTransferCallScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreateTransferCallScenarioWithResponse");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        Response<CallConnection> callConnectionResponse =
            callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options,
                null);

        CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
        CallConnection callConnection = callConnectionResponse.getValue();

        try {
            // Transfer Call
            String operationContext = UUID.randomUUID().toString(); 
            String targetCallConnectionId = getTargetCallConnectionId();
            Response<TransferCallResult> transferCallResponse = callConnection
                .transferToCallWithResponse(
                    targetCallConnectionId,
                    null,
                    operationContext,
                    null);
            CallingServerTestUtils.validateTransferResponse(transferCallResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateAddPlayAudioToParticipantCancelRemoveHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateAddPlayAudioToParticipantRemoveHangupScenario");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);

        CallingServerTestUtils.validateCallConnection(callConnection);

        try {
            // Add User
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            AddParticipantResult addParticipantResult = callConnection.addParticipant(addUser, null, operationContext);
            assert addParticipantResult != null;
            // Play Audio To Participant
            String playAudioOperationContext = UUID.randomUUID().toString();
            PlayAudioOptions playAudioOptions = new PlayAudioOptions()
                .setAudioFileId(UUID.randomUUID().toString())
                .setCallbackUri(URI.create(CALLBACK_URI))
                .setLoop(true)
                .setOperationContext(playAudioOperationContext);
            PlayAudioResult playAudioResult = callConnection.playAudioToParticipant(
                addUser, URI.create(AUDIO_FILE_URI), playAudioOptions);
            CallingServerTestUtils.validatePlayAudioResult(playAudioResult);
            String mediaOperationId = playAudioResult.getOperationId();
            // Cancel Participant Media Operation
            callConnection.cancelParticipantMediaOperation(addUser, mediaOperationId);
            // Remove User
            callConnection.removeParticipant(addUser);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            callConnection.hangup();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateAddPlayAudioToParticipantCancelRemoveHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreateAddPlayAudioToParticipantCancelRemoveHangupScenarioWithResponse");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        Response<CallConnection> callConnectionResponse =
            callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options,
                null);

        CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
        CallConnection callConnection = callConnectionResponse.getValue();

        try {
            // Add User
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            Response<AddParticipantResult> addParticipantResponse = callConnection
                .addParticipantWithResponse(
                    addUser,
                    null,
                    operationContext,
                    Context.NONE);
            CallingServerTestUtils.validateAddParticipantResponse(addParticipantResponse);
            
            // Play Audio
            String playAudioOperationContext = UUID.randomUUID().toString();
            PlayAudioOptions playAudioOptions =
                new PlayAudioOptions()
                    .setLoop(true)
                    .setAudioFileId(UUID.randomUUID().toString())
                    .setCallbackUri(URI.create(CALLBACK_URI))
                    .setOperationContext(playAudioOperationContext);
            Response<PlayAudioResult> playAudioResult =
                callConnection.playAudioToParticipantWithResponse(addUser, URI.create(AUDIO_FILE_URI), playAudioOptions, null);
            CallingServerTestUtils.validatePlayAudioResponse(playAudioResult);
            String mediaOperationId = playAudioResult.getValue().getOperationId();

            // Cancel All Media Operations
            callConnection.cancelParticipantMediaOperationWithResponse(addUser, mediaOperationId, null);

            // Remove User
            Response<Void> removeParticipantResponse =
                callConnection.removeParticipantWithResponse(addUser, null);
            CallingServerTestUtils.validateResponse(removeParticipantResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateAddCreateAudioGroupRemoveHangupScenario(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "runCreateAddCreateAudioGroupRemoveHangupScenario");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        CallConnection callConnection = callingServerClient.createCallConnection(
            new CommunicationUserIdentifier(fromUser),
            Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
            options);

        CallingServerTestUtils.validateCallConnection(callConnection);

        try {
            // Add User
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            AddParticipantResult addParticipantResult = callConnection.addParticipant(addUser, null, operationContext);
            assert addParticipantResult != null;
            // Creating List And Adding User
            List<CommunicationIdentifier> userList = new ArrayList<CommunicationIdentifier>();
            userList.add(addUser);
            // Create Audio Group
            CreateAudioGroupResult createAudioGroupResult = callConnection.createAudioGroup(AudioRoutingMode.MULTICAST, userList);
            String audioGroupId = createAudioGroupResult.getAudioGroupId();
            // Get Audio Group
            AudioGroupResult getAudioGroupResult = callConnection.getAudioGroups(audioGroupId);
            CallingServerTestUtils.validateAudioGroupResult(getAudioGroupResult);
            // Add Another User
            CommunicationUserIdentifier addAnotherUser = new CommunicationUserIdentifier(getUserId(ANOTHER_USER_IDENTIFIER));
            AddParticipantResult addAnotherParticipantResult = callConnection.addParticipant(addAnotherUser, null, operationContext);
            assert addAnotherParticipantResult != null;
            // Creating Another List And Adding User
            List<CommunicationIdentifier> usersList = new ArrayList<CommunicationIdentifier>();
            usersList.add(addAnotherUser);
            // Update Audio Group
            callConnection.updateAudioGroup(audioGroupId, usersList);
            // Get Audio Group
            AudioGroupResult getUpdatedAudioGroupResult = callConnection.getAudioGroups(audioGroupId);
            CallingServerTestUtils.validateAudioGroupResult(getUpdatedAudioGroupResult);
            // Delete Audio Group
            callConnection.deleteAudioGroup(audioGroupId);
            // Remove First Added User
            callConnection.removeParticipant(addUser);
            // Remove Another User
            callConnection.removeParticipant(addAnotherUser);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            callConnection.hangup();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void runCreateAddCreateAudioGroupRemoveHangupScenarioWithResponse(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient =
            setupClient(builder, "runCreateAddCreateAudioGroupRemoveHangupScenarioWithResponse");

        // Establish a call
        CreateCallOptions options = new CreateCallOptions(
            URI.create(CALLBACK_URI),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        options.setAlternateCallerId(new PhoneNumberIdentifier(FROM_PHONE_NUMBER));

        Response<CallConnection> callConnectionResponse =
            callingServerClient.createCallConnectionWithResponse(
                new CommunicationUserIdentifier(fromUser),
                Collections.singletonList(new PhoneNumberIdentifier(TO_PHONE_NUMBER)),
                options,
                null);

        CallingServerTestUtils.validateCallConnectionResponse(callConnectionResponse);
        CallConnection callConnection = callConnectionResponse.getValue();

        try {
            // Add User
            String operationContext = UUID.randomUUID().toString();
            CommunicationUserIdentifier addUser = new CommunicationUserIdentifier(getUserId(USER_IDENTIFIER));
            Response<AddParticipantResult> addParticipantResponse = callConnection
                .addParticipantWithResponse(
                    addUser,
                    null,
                    operationContext,
                    Context.NONE);
            CallingServerTestUtils.validateAddParticipantResponse(addParticipantResponse);
            
            // Creating List And Adding User
            List<CommunicationIdentifier> userList = new ArrayList<CommunicationIdentifier>();
            userList.add(addUser);

            // Create Audio Group
            Response<CreateAudioGroupResult> createAudioGroupResult = callConnection.createAudioGroupWithResponse(AudioRoutingMode.MULTICAST, userList, null);
            String audioGroupId = createAudioGroupResult.getValue().getAudioGroupId();

            // Get Audio Group
            Response<AudioGroupResult> getAudioGroupResponse = callConnection.getAudioGroupsWithResponse(audioGroupId, null);
            CallingServerTestUtils.validateAudioGroupResponse(getAudioGroupResponse);

            // Add Another User
            CommunicationUserIdentifier addAnotherUser = new CommunicationUserIdentifier(getUserId(ANOTHER_USER_IDENTIFIER));
            Response<AddParticipantResult> addAnotherParticipantResponse = callConnection
                .addParticipantWithResponse(
                    addAnotherUser,
                    null,
                    operationContext,
                    Context.NONE);
            CallingServerTestUtils.validateAddParticipantResponse(addAnotherParticipantResponse);

            // Creating Another List And Adding User
            List<CommunicationIdentifier> usersList = new ArrayList<CommunicationIdentifier>();
            usersList.add(addAnotherUser);

            // Update Audio Group
            callConnection.updateAudioGroupWithResponse(audioGroupId, usersList, null);

            // Get Audio Group
            Response<AudioGroupResult> getUpdatedAudioGroupResponse = callConnection.getAudioGroupsWithResponse(audioGroupId, null);
            CallingServerTestUtils.validateAudioGroupResponse(getUpdatedAudioGroupResponse);

            // Delete Audio Group
            callConnection.deleteAudioGroupWithResponse(audioGroupId, null);

            // Remove First Added User
            Response<Void> removeParticipantResponse =
                callConnection.removeParticipantWithResponse(addUser, null);
            CallingServerTestUtils.validateResponse(removeParticipantResponse);

            // Remove Another User
            Response<Void> removeAnotherParticipantResponse =
                callConnection.removeParticipantWithResponse(addAnotherUser, null);
            CallingServerTestUtils.validateResponse(removeAnotherParticipantResponse);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        } finally {
            // Hang up
            Response<Void> hangupResponse = callConnection.hangupWithResponse(null);
            CallingServerTestUtils.validateResponse(hangupResponse);
        }
    }

    private CallingServerClient setupClient(CallingServerClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected CallingServerClientBuilder addLoggingPolicy(CallingServerClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
