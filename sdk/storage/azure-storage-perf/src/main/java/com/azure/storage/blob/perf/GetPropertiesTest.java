package com.azure.storage.blob.perf;

import com.azure.storage.blob.perf.core.AbstractDownloadTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class GetPropertiesTest extends AbstractDownloadTest<BlobPerfStressOptions> {

    public GetPropertiesTest(BlobPerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        blobClient.getProperties();
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.getProperties().then();
    }
}
