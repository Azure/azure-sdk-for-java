// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.CreateCallResult;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import java.util.ArrayList;
import java.util.List;

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

        List<CommunicationIdentifier> targets = new ArrayList<>();

        CommunicationIdentifier firstCallee = new CommunicationUserIdentifier("<acs-user-identity-1>");
        CommunicationIdentifier secondCallee = new CommunicationUserIdentifier("<acs-user-identity-2>");

        targets.add(firstCallee);
        targets.add(secondCallee);

        String callbackUri = "<callback-uri-for-notification>";

        List<CallModality> requestedModalities = new ArrayList<>();
        requestedModalities.add(CallModality.AUDIO);
        requestedModalities.add(CallModality.VIDEO);

        List<EventSubscriptionType> requestedCallEvents = new ArrayList<>();
        requestedCallEvents.add(EventSubscriptionType.DTMF_RECEIVED);
        requestedCallEvents.add(EventSubscriptionType.PARTICIPANTS_UPDATED);

        CreateCallOptions createCallOptions = new CreateCallOptions(
            callbackUri,
            requestedModalities,
            requestedCallEvents);

        CreateCallResult createCallResult =  callClient.createCall(source, targets, createCallOptions);

        String callId = createCallResult.getCallLegId();
    }

    /**
     * Sample code for deleting a call using the sync call client.
     */
    public void deleteCall() {
        CallClient callClient = createCallClient();

        String callId = "callId";
        callClient.deleteCall(callId);
    }
}
