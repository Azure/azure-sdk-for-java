// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public class TracingTest extends RestProxyTestBase<CorePerfStressOptions> {
    private final long length;

    private final Supplier<BinaryData> binaryDataSupplier;

    public TracingTest(CorePerfStressOptions options) {
        super(options, null, new MockTracer());
        length = options.getSize();
        binaryDataSupplier = createBinaryDataSupplier(options);
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.fromSupplier(binaryDataSupplier).flatMap(data -> service.setBinaryData(endpoint, id, data, length));
    }

    private static class MockTracer implements Tracer {

        @Override
        public Context start(String methodName, Context context) {
            return context;
        }

        @Override
        public void end(String statusMessage, Throwable error, Context context) {
        }

        @Override
        public void setAttribute(String key, String value, Context context) {
        }
    }
}
