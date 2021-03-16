// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.perf;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.CloudEvent;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

/**
 * Parent class of EventGrid service tests.
 * @param <TOptions> options.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    protected final EventGridPublisherClient<CloudEvent> cloudEventPublisherClient;
    protected final EventGridPublisherAsyncClient<CloudEvent> cloudEventPublisherAsyncClient;

    protected final EventGridPublisherClient<EventGridEvent> eventGridEventPublisherClient;
    protected final EventGridPublisherAsyncClient<EventGridEvent> eventGridEventPublisherAsyncClient;

    protected final EventGridPublisherClient<BinaryData> customEventPublisherClient;
    protected final EventGridPublisherAsyncClient<BinaryData> customEventPublisherAsyncClient;


    /**
     * Create a ServiceTest
     * @param options otpions.
     */
    public ServiceTest(TOptions options) {
        super(options);

        // client to send to cloud event topic
        EventGridPublisherClientBuilder cloudEventClientBuilder = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_ENDPOINT"))  // make sure it accepts CloudEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CLOUDEVENT_KEY")));
        cloudEventPublisherClient = cloudEventClientBuilder.buildCloudEventPublisherClient();
        cloudEventPublisherAsyncClient = cloudEventClientBuilder.buildCloudEventPublisherAsyncClient();

        // client to send to event grid event topic
        EventGridPublisherClientBuilder eventGridEventClientBuilder = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_EVENT_ENDPOINT"))  // make sure it accepts EventGridEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_EVENT_KEY")));
        eventGridEventPublisherClient = eventGridEventClientBuilder.buildEventGridEventPublisherClient();
        eventGridEventPublisherAsyncClient = eventGridEventClientBuilder.buildEventGridEventPublisherAsyncClient();

        // client to send to event grid event topic
        EventGridPublisherClientBuilder customEventClientBuilder = new EventGridPublisherClientBuilder()
            .endpoint(System.getenv("AZURE_EVENTGRID_CUSTOM_ENDPOINT"))  // make sure it accepts EventGridEvent
            .credential(new AzureKeyCredential(System.getenv("AZURE_EVENTGRID_CUSTOM_KEY")));
        customEventPublisherClient = customEventClientBuilder.buildCustomEventPublisherClient();
        customEventPublisherAsyncClient = customEventClientBuilder.buildCustomEventPublisherAsyncClient();
    }
}
