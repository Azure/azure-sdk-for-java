// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.template.perf;

import com.azure.perf.test.core.PerfStressTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;
import com.azure.sdk.template.Hello;

public class GetMessageTest extends PerfStressTest<PerfStressOptions> {
    private final Hello hello;

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
