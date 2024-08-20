// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.perf.BlobPerfStressOptions;
import reactor.core.publisher.Mono;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;
import static com.azure.perf.test.core.TestDataCreationHelper.createRandomInputStream;

public abstract class AbstractDownloadTest <TOptions extends BlobPerfStressOptions> extends BlobTestBase<TOptions> {
    private static final long GB = 1024 * 1024 * 1024;

    public AbstractDownloadTest(TOptions options) {
        super(options, BLOB_NAME_PREFIX);
    }

    // Upload one blob for the whole test run. All tests can download the same blob
    @Override
    public Mono<Void> globalSetupAsync() {
        /*
         * Uploading the blob is set to use a "single shot" and block size of 1GB to have the blob upload over a single
         * connection. There was an investigation into an issue in the performance tests where all connections were
         * being handled by a single IO thread, the root cause was found to be that when 1GB download set up resources
         * here the sequential uploading of 4MB blocks resulted in Reactor Netty using the same thread to manage all
         * upload connections (1GB / 4MB = 256). The test only used 8 threads to perform parallel 1GB download and since
         * 8 connections already existed they were reused and managed by that single IO thread. So, changing upload to
         * be done with a single connection fixes that, where when 8 threads begin performing download at the same time
         * Reactor Netty has to even spread those requests over the available IO threads instead of being pinned to
         * the one IO thread.
         *
         * In the future there will be work to separate the HttpClients used to perform resource preparation and running
         * the performance test. As part of that work this can be reverted to using the default ParallelTransferOptions.
         */
        return super.globalSetupAsync()
            .then(blobAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), new ParallelTransferOptions()
                .setMaxSingleUploadSizeLong(GB).setBlockSizeLong(GB)))
            .then();
    }

    @Override
    public void globalSetup() {
        /*
         * Uploading the blob is set to use a "single shot" and block size of 1GB to have the blob upload over a single
         * connection. There was an investigation into an issue in the performance tests where all connections were
         * being handled by a single IO thread, the root cause was found to be that when 1GB download set up resources
         * here the sequential uploading of 4MB blocks resulted in Reactor Netty using the same thread to manage all
         * upload connections (1GB / 4MB = 256). The test only used 8 threads to perform parallel 1GB download and since
         * 8 connections already existed they were reused and managed by that single IO thread. So, changing upload to
         * be done with a single connection fixes that, where when 8 threads begin performing download at the same time
         * Reactor Netty has to even spread those requests over the available IO threads instead of being pinned to
         * the one IO thread.
         *
         * In the future there will be work to separate the HttpClients used to perform resource preparation and running
         * the performance test. As part of that work this can be reverted to using the default ParallelTransferOptions.
         */
        super.globalSetup();
        blobClient.uploadWithResponse(new BlobParallelUploadOptions(createRandomInputStream(options.getSize()))
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong(GB)
                .setBlockSizeLong(GB)), null, Context.NONE);
    }
}
