// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.perf;

import com.azure.core.util.BinaryData;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SendCustomEventsTest extends ServiceTest<PerfStressOptions> {

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
            String dataPayload = "A".repeat(options.getCount());
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

    private static class CustomEvent {
        private final String id;
        private final OffsetDateTime time;
        private final String subject;
        private final String foo;
        private final String type;
        private final String data;
        private final String dataVersion;

        public CustomEvent(String id, OffsetDateTime time, String subject, String foo,
            String type, String data, String dataVersion) {
            this.id = id;
            this.time = time;
            this.subject = subject;
            this.foo = foo;
            this.type = type;
            this.data = data;
            this.dataVersion = dataVersion;
        }
    }
}
