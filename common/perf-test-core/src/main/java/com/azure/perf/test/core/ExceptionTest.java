// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

class ExceptionTest extends PerfStressTest<PerfStressOptions> {
    private final ClientLogger logger = new ClientLogger(ExceptionTest.class);

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
        } catch (Exception ex) {
            logger.info("Test exception.", ex);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.error(new IllegalArgumentException());
    }
}
