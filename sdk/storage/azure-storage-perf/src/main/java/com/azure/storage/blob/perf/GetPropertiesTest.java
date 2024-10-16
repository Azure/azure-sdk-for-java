package com.azure.storage.blob.perf;

import com.azure.storage.blob.perf.core.AbstractUploadTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class GetPropertiesTest extends AbstractUploadTest<BlobPerfStressOptions> {

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

    @Override
    public Mono<Void> globalSetupAsync() {
        // Setup small data since size is not dependent on the getProperties
        byte[] smallBlobBytes = "default".getBytes(StandardCharsets.UTF_8);
        return super.globalSetupAsync().then(blobAsyncClient.upload(Flux.just(ByteBuffer.wrap(smallBlobBytes)), null, true).then());
    }
}
