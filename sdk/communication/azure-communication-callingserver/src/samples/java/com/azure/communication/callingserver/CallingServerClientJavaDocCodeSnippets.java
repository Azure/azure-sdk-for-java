// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.MediaType;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;

public class CallingServerClientJavaDocCodeSnippets {
    public CallingServerClient createCallingServerClientWithPipeline() {

        String connectionString = getConnectionString();
        // BEGIN: com.azure.communication.callingserver.CallingServerClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        CallingServerClient callingServerClient = new CallingServerClientBuilder()
            .pipeline(pipeline)
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.communication.callingserver.CallingServerClient.pipeline.instantiation
        return callingServerClient;
    }

    private String getConnectionString() {
        return "endpoint=https://<resource-name>.communication.azure.com/;accesskey=<access-key>";
    }

    /**
     * Sample code for creating a call connection using the sync call client.
     */
    public void createCallConnection() {
        CallingServerClient callingServerClient = createCallingServerClientWithPipeline();

        CommunicationIdentifier source = new CommunicationUserIdentifier("<acs-user-identity>");
        CommunicationIdentifier firstCallee = new CommunicationUserIdentifier("<acs-user-identity-1>");
        CommunicationIdentifier secondCallee = new CommunicationUserIdentifier("<acs-user-identity-2>");
        String callbackUri = "<callback-uri-for-notification>";

        // BEGIN: com.azure.communication.callingserver.CallingServerClient.create.call.connection
        CommunicationIdentifier[] targets = new CommunicationIdentifier[] { firstCallee, secondCallee };
        MediaType[] requestedMediaTypes = new MediaType[] { MediaType.AUDIO, MediaType.VIDEO };
        EventSubscriptionType[] requestedCallEvents = new EventSubscriptionType[] {
            EventSubscriptionType.DTMF_RECEIVED,
            EventSubscriptionType.PARTICIPANTS_UPDATED
        };
        CreateCallOptions createCallOptions = new CreateCallOptions(
            callbackUri,
            requestedMediaTypes,
            requestedCallEvents);
        CallConnection callConnection = callingServerClient.createCallConnection(source, targets, createCallOptions);
        // END: com.azure.communication.callingserver.CallingServerClient.create.call.connection
    }

}
