// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.storage.StoragePerfStressOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import com.azure.storage.blob.specialized.BlobOutputStream;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static com.azure.perf.test.core.TestDataCreationHelper.writeBytesToOutputStream;

public class UploadOutputStreamTest extends BlobTestBase<StoragePerfStressOptions> {
    public UploadOutputStreamTest(StoragePerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        try {
            BlockBlobOutputStreamOptions blockBlobOutputStreamOptions = new BlockBlobOutputStreamOptions()
                .setParallelTransferOptions(
                    new ParallelTransferOptions()
                        .setMaxSingleUploadSizeLong(options.getTransferSingleUploadSize())
                        .setBlockSizeLong(options.getTransferBlockSize())
                        .setMaxConcurrency(options.getTransferConcurrency())
                );
            BlobOutputStream blobOutputStream = blockBlobClient.getBlobOutputStream(blockBlobOutputStreamOptions);
            writeBytesToOutputStream(blobOutputStream, options.getSize());
            blobOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
