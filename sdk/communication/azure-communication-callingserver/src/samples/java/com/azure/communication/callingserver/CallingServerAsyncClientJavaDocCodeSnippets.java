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

import java.util.Arrays;
import java.util.List;

public class CallingServerAsyncClientJavaDocCodeSnippets {
    public CallingServerAsyncClient createCallingServerAsyncClientWithPipeline() {

        String connectionString = getConnectionString();
        // BEGIN: com.azure.communication.callingserver.CallingServerAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        CallingServerAsyncClient callingServerAsyncClient = new CallingServerClientBuilder()
            .pipeline(pipeline)
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.communication.callingserver.CallingServerAsyncClient.pipeline.instantiation
        return callingServerAsyncClient;
    }

    private String getConnectionString() {
        return "endpoint=https://<resource-name>.communication.azure.com/;accesskey=<access-key>";
    }

    /**
     * Sample code for creating a call connection using the async call client.
     */
    public void createCallConnectionAsync() {
        CallingServerAsyncClient callingServerAsyncClient = createCallingServerAsyncClientWithPipeline();

        CommunicationIdentifier source = new CommunicationUserIdentifier("<acs-user-identity>");
        CommunicationIdentifier firstCallee = new CommunicationUserIdentifier("<acs-user-identity-1>");
        CommunicationIdentifier secondCallee = new CommunicationUserIdentifier("<acs-user-identity-2>");
        String callbackUri = "<callback-uri-for-notification>";

        // BEGIN: com.azure.communication.callingserver.CallingServerAsyncClient.create.call.connection.async
        List<CommunicationIdentifier> targets = Arrays.asList(firstCallee, secondCallee);
        List<MediaType> requestedMediaTypes = Arrays.asList(MediaType.AUDIO, MediaType.VIDEO);
        List<EventSubscriptionType> requestedCallEvents = Arrays.asList(
            EventSubscriptionType.DTMF_RECEIVED,
            EventSubscriptionType.PARTICIPANTS_UPDATED);
        CreateCallOptions createCallOptions = new CreateCallOptions(
            callbackUri,
            requestedMediaTypes,
            requestedCallEvents);
        CallConnectionAsync callAsyncConnection = callingServerAsyncClient
            .createCallConnection(source, targets, createCallOptions).block();
        // END: com.azure.communication.callingserver.CallingServerAsyncClient.create.call.connection.async
    }
}
