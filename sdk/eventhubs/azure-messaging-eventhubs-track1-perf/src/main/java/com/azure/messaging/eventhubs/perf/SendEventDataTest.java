// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

public class SendEventDataTest extends ServiceTest {
    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public SendEventDataTest(EventHubsOptions options) {
        super(options);
    }

    @Override
    public void run() {

    }

    @Override
    public Mono<Void> runAsync() {
        return null;
    }
}
