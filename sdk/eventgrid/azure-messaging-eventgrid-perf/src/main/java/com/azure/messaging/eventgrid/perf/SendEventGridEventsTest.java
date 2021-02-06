// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.perf;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class SendEventGridEventsTest extends ServiceTest<PerfStressOptions> {

    public SendEventGridEventsTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        eventGridEventPublisherClient.sendEventGridEvents(createEvents());
    }

    // Perform the Async API call to be tested here
    @Override
    public Mono<Void> runAsync() {
        return eventGridEventPublisherAsyncClient.sendEventGridEvents(createEvents());
    }

    private List<EventGridEvent> createEvents() {
        List<EventGridEvent> events = new ArrayList<>();
        String dataPayload = "A".repeat(options.getCount());
        for (int i = 0; i < options.getCount(); i++) {
            EventGridEvent event = new EventGridEvent("https://www.eventgrid.com/", "EG.Perf",
                new TestModelClass(dataPayload), "v1");
            events.add(event);
        }
        return events;
    }
}
