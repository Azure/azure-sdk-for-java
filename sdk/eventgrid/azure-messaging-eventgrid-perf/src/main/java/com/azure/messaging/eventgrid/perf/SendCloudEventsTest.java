// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.perf;

import com.azure.messaging.eventgrid.CloudEvent;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class SendCloudEventsTest extends ServiceTest<PerfStressOptions> {
    public SendCloudEventsTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        cloudEventPublisherClient.sendCloudEvents(createEvents());
    }

    // Perform the Async API call to be tested here
    @Override
    public Mono<Void> runAsync() {
        return cloudEventPublisherAsyncClient.sendCloudEvents(createEvents());
    }

    private List<CloudEvent> createEvents() {
        String dataPayload = "A".repeat(options.getCount());
        List<CloudEvent> events = new ArrayList<>();
        for (int i = 0; i < options.getCount(); i++) {
            CloudEvent ce = new CloudEvent("https://www.eventgrid.com/", "EG.Perf",
                new TestModelClass(dataPayload));
            events.add(ce);
        }
        return events;
    }
}
