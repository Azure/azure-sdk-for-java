// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.perf;

import com.azure.core.util.BinaryData;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Test performance of sending custom events
 */
public class SendCustomEventsTest extends ServiceTest<PerfStressOptions> {

    /**
     * Create the SendCustomEventsTest
     * @param options options.
     */
    public SendCustomEventsTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        customEventPublisherClient.sendEvents(createEvents());
    }

    // Perform the Async API call to be tested here
    @Override
    public Mono<Void> runAsync() {
        return customEventPublisherAsyncClient.sendEvents(createEvents());
    }

    private List<BinaryData> createEvents() {
        List<BinaryData> events = new ArrayList<>();
        for (int i = 0; i < options.getCount(); i++) {
            String dataPayload = String.join("", Collections.nCopies(options.getCount(), "A"));
            CustomEvent customEvent = new CustomEvent(
                UUID.randomUUID().toString(),
                OffsetDateTime.now(),
                "Test",
                "bar",
                "Microsoft.MockPublisher.TestEvent",
                dataPayload,
                "0.1"
            );
            events.add(BinaryData.fromObject(customEvent));
        }
        return events;
    }
}
