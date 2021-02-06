// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.perf;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    protected final EventGridPublisherClient cloudEventPublisherClient;
    protected final EventGridPublisherAsyncClient cloudEventPublisherAsyncClient;

    protected final EventGridPublisherClient eventGridEventPublisherClient;
    protected final EventGridPublisherAsyncClient eventGridEventPublisherAsyncClient;

    public ServiceTest(TOptions options) {
        super(options);

        // client to send to cloud event topic
        EventGridPublisherClientBuilder cloudEventClientBuilder = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"))  // make sure it accepts CloudEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")));
        cloudEventPublisherClient = cloudEventClientBuilder.buildClient();
        cloudEventPublisherAsyncClient = cloudEventClientBuilder.buildAsyncClient();

        // client to send to event grid event topic
        EventGridPublisherClientBuilder eventGridGridClientBuilder = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_EVENT_ENDPOINT"))  // make sure it accepts EventGridEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_EVENT_KEY")));
        eventGridEventPublisherClient = eventGridGridClientBuilder.buildClient();
        eventGridEventPublisherAsyncClient = eventGridGridClientBuilder.buildAsyncClient();
    }
}
