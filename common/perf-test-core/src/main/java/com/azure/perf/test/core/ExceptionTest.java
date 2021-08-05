// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import reactor.core.publisher.Mono;

class ExceptionTest extends PerfStressTest<PerfStressOptions> {
    /**
     * Sets up the Exception test.
     * @param options the options to setup the test with.
     */
    public ExceptionTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        try {
            throw new IllegalArgumentException();
        } catch (Exception e) {
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.error(new IllegalArgumentException());
    }
}
