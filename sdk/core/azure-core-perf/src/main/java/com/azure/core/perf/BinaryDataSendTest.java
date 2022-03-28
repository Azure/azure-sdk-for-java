// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public class BinaryDataSendTest extends RestProxyTestBase<CorePerfStressOptions> {
    private final long length;

    private final Supplier<BinaryData> binaryDataSupplier;

    public BinaryDataSendTest(CorePerfStressOptions options) {
        super(options);
        length = options.getSize();
        binaryDataSupplier = createBinaryDataSupplier(options);
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        return service.setBinaryData(endpoint, id, binaryDataSupplier.get(), length)
            .then();
    }
}
