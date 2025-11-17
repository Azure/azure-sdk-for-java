package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stress test for blob metadata operations including setting, getting, and updating metadata.
 * This test validates the performance and reliability of metadata operations under high load.
 */
public class BlobMetadataOperations extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(BlockBlobOutputStream.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncNoFaultClient;
    private final ParallelTransferOptions parallelTransferOptions;

    private static final String BLOB_NAME_PREFIX = "metadata-test-blob-";
    private static final int METADATA_PAIRS_COUNT = 10;

    public BlobMetadataOperations(StorageStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClientNoFault().getBlobClient(blobName);
        this.parallelTransferOptions = new ParallelTransferOptions().setMaxConcurrency(options.getMaxConcurrency());
    }

    @Override
    protected void runInternal(Context span) {

        Map<String, String> metadata = generateRandomMetadata();
        syncClient.setMetadata(metadata);
        BlobProperties properties = syncClient.getProperties();
        Map<String, String> retrievedMetadata = properties.getMetadata();
        if (retrievedMetadata == null || retrievedMetadata.size() != metadata.size()) {
            throw new RuntimeException("Metadata validation failed: expected " + metadata.size()
                + " pairs but got " + (retrievedMetadata != null ? retrievedMetadata.size() : 0));
        }
        Map<String, String> updatedMetadata = generateRandomMetadata();
        syncClient.setMetadata(updatedMetadata);
        syncClient.setMetadata(new HashMap<>());
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        return ensureBlobExistsAsync()
            .then(setInitialMetadataAsync())
            .flatMap(this::validateMetadataAsync)
            .then(updateMetadataAsync())
            .then(clearMetadataAsync());
    }


    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(originalContent.setupBlob(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.delete()
            .then(super.cleanupAsync());
    }

    private Map<String, String> generateRandomMetadata() {
        Map<String, String> metadata = new HashMap<>();
        for (int i = 0; i < METADATA_PAIRS_COUNT; i++) {
            String key = "key" + i + "_" + System.currentTimeMillis();
            String value = "value_" + UUID.randomUUID().toString().substring(0, 8);
            metadata.put(key, value);
        }
        return metadata;
    }

    private Flux<ByteBuffer> generateRandomByteBuffer(int size) {
        byte[] data = new byte[size];
        new SecureRandom().nextBytes(data);
        return Flux.just(ByteBuffer.wrap(data));
    }


    private InputStream generateRandomBinaryData(int size) {
        byte[] data = new byte[size];
        new SecureRandom().nextBytes(data);
        return new ByteArrayInputStream(data);
    }


    private Mono<Void> ensureBlobExistsAsync() {
        return asyncNoFaultClient.exists()
            .flatMap(exists -> {
                if (exists) {
                    return Mono.empty();
                }
                return asyncNoFaultClient.upload(generateRandomByteBuffer(1024), parallelTransferOptions, true)
                    .then();
            });
    }

    private Mono<Map<String, String>> setInitialMetadataAsync() {
        Map<String, String> metadata = generateRandomMetadata();
        return asyncNoFaultClient.setMetadata(metadata)
            .thenReturn(metadata);
    }

    private Mono<Void> validateMetadataAsync(Map<String, String> expectedMetadata) {
        return asyncNoFaultClient.getProperties()
            .flatMap(properties -> {
                Map<String, String> retrievedMetadata = properties.getMetadata();
                if (retrievedMetadata == null || retrievedMetadata.size() != expectedMetadata.size()) {
                    return Mono.error(new RuntimeException("Async metadata validation failed"));
                }
                return Mono.empty();
            });
    }

    private Mono<Void> updateMetadataAsync() {
        Map<String, String> updatedMetadata = generateRandomMetadata();
        return asyncNoFaultClient.setMetadata(updatedMetadata)
            .then();
    }

    private Mono<Void> clearMetadataAsync() {
        return asyncNoFaultClient.setMetadata(new HashMap<>())
            .then();
    }

}
