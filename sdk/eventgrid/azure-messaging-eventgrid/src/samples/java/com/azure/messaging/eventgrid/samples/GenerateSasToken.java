// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.models.CloudEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;

import java.time.OffsetDateTime;

public class GenerateSasToken {
    public static void main(String[] args) {
        // 1. Generate the SAS token.
        String sasToken = EventGridPublisherClient.generateSas(
            System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"),
            new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")),
            OffsetDateTime.now().plusMinutes(20));
        System.out.println(sasToken);

        // 2. Use the SAS token to create an client to send events.
        //  For how to use the client to send events, refer to the Publish* samples in the same folder.
        EventGridPublisherClient<CloudEvent> publisherClient = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"))
            .credential(new AzureSasCredential(sasToken))
            .buildCloudEventPublisherClient();

    }
}
