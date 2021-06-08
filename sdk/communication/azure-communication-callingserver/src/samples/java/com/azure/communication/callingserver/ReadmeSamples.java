// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.CreateCallResponse;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */

public class ReadmeSamples {

    /**
     * Sample code for creating a sync call client.
     *
     * @return the call client.
     */
    public CallClient createCallClient() {
        String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

        // Your connectionString retrieved from your Azure Communication Service
        String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

        // Initialize the call client
        final CallClientBuilder builder = new CallClientBuilder();
        builder.endpoint(endpoint)
            .connectionString(connectionString);
        CallClient callClient = builder.buildClient();

        return callClient;
    }

    /**
     * Sample code for creating a call using the sync call client.
     */
    public void createCall() {

        CallClient callClient = createCallClient();

        CommunicationIdentifier source = new CommunicationUserIdentifier("<acs-user-identity>");



        CommunicationIdentifier firstCallee = new CommunicationUserIdentifier("<acs-user-identity-1>");
        CommunicationIdentifier secondCallee = new CommunicationUserIdentifier("<acs-user-identity-2>");

        CommunicationIdentifier[] targets = new CommunicationIdentifier[] { firstCallee, secondCallee };

        String callbackUri = "<callback-uri-for-notification>";

        CallModality[] requestedModalities = new CallModality[] { CallModality.AUDIO, CallModality.VIDEO };

        EventSubscriptionType[] requestedCallEvents = new EventSubscriptionType[] {
            EventSubscriptionType.DTMF_RECEIVED,
            EventSubscriptionType.PARTICIPANTS_UPDATED
        };

        CreateCallOptions createCallOptions = new CreateCallOptions(
            callbackUri,
            requestedModalities,
            requestedCallEvents);

        CreateCallResponse createCallResult =  callClient.createCall(source, targets, createCallOptions);
    }

    /**
     * Sample code for hanging up a call using the sync call client.
     */
    public void hangupCall() {
        CallClient callClient = createCallClient();

        String callId = "callId";
        callClient.hangupCall(callId);
    }

    /**
     * Sample code for deleting a call using the sync call client.
     */
    public void deleteCall() {
        CallClient callClient = createCallClient();

        String callId = "callId";
        callClient.deleteCall(callId);
    }

    /**
     * Sample code for deleting a call using the sync call client.
     */
    public void addParticipant() {
        CallClient callClient = createCallClient();

        String callId = "callId";
        CommunicationIdentifier thirdCallee = new CommunicationUserIdentifier("<acs-user-identity-2>");
        callClient.addParticipant(callId, thirdCallee, "ACS User 2", "<string-for-tracing-responses>");
    }
}
