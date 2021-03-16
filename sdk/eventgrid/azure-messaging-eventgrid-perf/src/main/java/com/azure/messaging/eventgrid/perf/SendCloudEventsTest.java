// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.perf;

import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test performance of sending cloud events
 */
public class SendCloudEventsTest extends ServiceTest<PerfStressOptions> {
    /**
     * Create the SendCloudEventsTest
     * @param options options
     */
    public SendCloudEventsTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        cloudEventPublisherClient.sendEvents(createEvents());
    }

    // Perform the Async API call to be tested here
    @Override
    public Mono<Void> runAsync() {
        return cloudEventPublisherAsyncClient.sendEvents(createEvents());
    }

    private List<CloudEvent> createEvents() {
        List<CloudEvent> events = new ArrayList<>();
        for (int i = 0; i < options.getCount(); i++) {
            String dataPayload = String.join("", Collections.nCopies(options.getCount(), "A"));
            CloudEvent ce = new CloudEvent("https://www.eventgrid.com/", "EG.Perf",
                BinaryData.fromObject(new TestModelClass(dataPayload)), CloudEventDataFormat.JSON, "application/json");
            events.add(ce);
        }
        return events;
    }
}
