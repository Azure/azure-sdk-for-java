// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallMediaType;
import com.azure.communication.callingserver.models.CallingEventSubscriptionType;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
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
     * Sample code for creating a sync calling server client.
     *
     * @return the calling server client.
     */
    public CallingServerClient createCallingServerClient() {
        // Your connectionString retrieved from your Azure Communication Service
        String connectionString = "endpoint=https://<resource-name>.communication.azure.com/;accesskey=<access-key>";

        // Initialize the calling server client
        final CallingServerClientBuilder builder = new CallingServerClientBuilder();
        builder.connectionString(connectionString);
        CallingServerClient callingServerClient = builder.buildClient();

        return callingServerClient;
    }

    /**
     * Sample code for creating a call connection using the sync call client.
     */
    public void createCallConnection() {

        CallingServerClient callingServerClient = createCallingServerClient();

        CommunicationIdentifier source = new CommunicationUserIdentifier("<acs-user-identity>");
        CommunicationIdentifier firstCallee = new CommunicationUserIdentifier("<acs-user-identity-1>");
        CommunicationIdentifier secondCallee = new CommunicationUserIdentifier("<acs-user-identity-2>");

        List<CommunicationIdentifier> targets = Arrays.asList(firstCallee, secondCallee);

        String callbackUri = "<callback-uri-for-notification>";

        List<CallMediaType> requestedMediaTypes = Arrays.asList(CallMediaType.AUDIO, CallMediaType.VIDEO);

        List<CallingEventSubscriptionType> requestedCallEvents = Arrays.asList(
            CallingEventSubscriptionType.TONE_RECEIVED,
            CallingEventSubscriptionType.PARTICIPANTS_UPDATED);

        CreateCallOptions createCallOptions = new CreateCallOptions(
            URI.create(callbackUri),
            requestedMediaTypes,
            requestedCallEvents);

        CallConnection callConnection = callingServerClient.createCallConnection(source, targets, createCallOptions);
    }

    /**
     * Sample code for hanging up a call connection using the sync call client.
     */
    public void hangupCallConnection() {
        String callConnectionId = "callId";
        CallingServerClient callingServerClient = createCallingServerClient();
        CallConnection callConnection = callingServerClient.getCallConnection(callConnectionId);
        callConnection.hangup();
    }

    /**
     * Sample code for deleting a call using the sync call client.
     */
    public void addParticipant() {
        String callConnectionId = "callId";
        CallingServerClient callingServerClient = createCallingServerClient();
        CallConnection callConnection = callingServerClient.getCallConnection(callConnectionId);
        CommunicationIdentifier thirdCallee = new CommunicationUserIdentifier("<acs-user-identity-3>");
        String callbackUri = "<callback-uri-for-notification>";
        callConnection.addParticipant(thirdCallee, "ACS User 3", "<string-for-tracing-responses>", URI.create(callbackUri));
    }

    /**
     * Sample code for downloading a recording into a file.
     */
    public void getRecordingStream() {
        String recordingUrl = "https://ams.skype.com/objects/v1/document_id/video";
        String filePath = "filePath.mp4";
        CallingServerClient callingServerClient = createCallingServerClient();
        callingServerClient.downloadTo(
            recordingUrl,
            Paths.get(filePath),
            null,
            true
        );
    }

    /**
     * Sample code for creating async calling server client with token credential.
     *
     * @return the calling server client.
     */
    public CallingServerClient createCallingServerClientWithTokenCredential() {
        // Your endpoint retrieved from your Azure Communication Service
        String endpoint = "https://<resource-name>.communication.azure.com";

        // Token credential used for managed identity authentication. Depends on `AZURE_CLIENT_SECRET`,
        // `AZURE_CLIENT_ID`, and `AZURE_TENANT_ID` environment variables to be set up.
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Initialize the calling server client
        CallingServerClient callingServerClient  = new CallingServerClientBuilder()
            .endpoint(endpoint)
            .credential(tokenCredential)
            .buildClient();

        return callingServerClient;
    }
}
