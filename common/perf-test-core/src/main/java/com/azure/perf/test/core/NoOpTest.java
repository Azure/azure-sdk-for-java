// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.util.concurrent.Future;
import reactor.core.publisher.Mono;

class NoOpTest extends PerfStressTest<PerfStressOptions> {

    /**
     * Sets up the No op test.
     * @param options the options to setup the test with.
     */
    public NoOpTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.empty();
    }

    @Override
    public Future<Void> runAsyncCompletableFuture() {
        return null;
    }
}
