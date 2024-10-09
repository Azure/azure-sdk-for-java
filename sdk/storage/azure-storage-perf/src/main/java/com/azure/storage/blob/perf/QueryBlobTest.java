package com.azure.storage.blob.perf;

import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.perf.core.AbstractUploadTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class QueryBlobTest extends AbstractUploadTest<BlobPerfStressOptions> {

    //private final BlobQueryOptions queryOptions;
    private final String expression = "SELECT * from BlobStorage";

    public QueryBlobTest(BlobPerfStressOptions options) {
        super(options);

        // Prepare query options (modify based on your query needs)
    }

    @Override
    public void run() {
        // Synchronous queryWithResponse
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        blobClient.query(os, expression);
    }

    @Override
    public Mono<Void> runAsync() {
        // Asynchronous queryWithResponse
        return blobAsyncClient.query(expression)
            .then();
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        // Setup any data that needs to be queried
        return super.globalSetupAsync()
            .then(blobAsyncClient.upload(Flux.just(ByteBuffer.wrap(new byte[(int) options.getSize()])),
                new ParallelTransferOptions()
                    .setMaxSingleUploadSizeLong(options.getTransferSingleUploadSize())
                    .setBlockSizeLong(options.getTransferBlockSize())
                    .setMaxConcurrency(options.getTransferConcurrency()),
                true).then());
    }
}
