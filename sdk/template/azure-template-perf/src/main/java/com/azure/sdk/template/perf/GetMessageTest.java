// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.template.perf;

import com.azure.perf.test.core.PerfStressTest;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.sdk.template.Hello;
import reactor.core.publisher.Mono;

/**
 * Performance test for getting messages.
 */
public class GetMessageTest extends PerfStressTest<PerfStressOptions> {
    private final Hello hello;

    /**
     * Creates a get messages performance test.
     *
     * @param options Performance test configuration options.
     */
    public GetMessageTest(PerfStressOptions options) {
        super(options);

        hello = new Hello();
    }

    @Override
    public void run() {
        hello.getMessage();
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
