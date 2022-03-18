// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.core.util.BinaryData;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BinaryDataSendTest extends RestProxyTestBase<CorePerfStressOptions> {
    private final long length;

    private final Path tempFile;
    private final String tempFilePath;

    public BinaryDataSendTest(CorePerfStressOptions options) {
        super(options);
        try {
            tempFile = Files.createTempFile("binarydatasendtest", null);
            tempFile.toFile().deleteOnExit();
            tempFilePath = tempFile.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        length = options.getSize();
    }


    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(populateTempFile());
    }

    private Mono<Void> populateTempFile() {
        return Mono.fromCallable(() -> {
            TestDataCreationHelper.writeToFile(tempFilePath, options.getSize(), 8192);
            return 1;
        }).then();
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        return service.setBinaryData(endpoint, BinaryData.fromFile(tempFile), length)
            .then();
    }
}
