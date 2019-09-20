// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.cryptography;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BaseBlobClientBuilder;
import com.azure.storage.blob.BlobURLParts;
import com.azure.storage.blob.URLParser;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.common.credentials.SASTokenCredential;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation Storage Blob clients.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the endpoint through {@code .endpoint()}, including the container name and blob name, in the format of
 * {@code https://{accountName}.blob.core.windows.net/{containerName}/{blobName}}.
 * <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly
 * accessible.
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder use the following mapping to construct the given client:
 * <ul>
 * <li>{@link EncryptedBlobClientBuilder#buildEncryptedBlockBlobClient()} - {@link EncryptedBlockBlobClient}</li>
 * <li>{@link EncryptedBlobClientBuilder#buildEncryptedBlockBlobClient()} - {@link EncryptedBlockBlobClient}</li>
 * </ul>
 */
public final class EncryptedBlobClientBuilder extends BaseBlobClientBuilder<EncryptedBlobClientBuilder> {

    private final ClientLogger logger = new ClientLogger(EncryptedBlobClientBuilder.class);

    private String containerName;
    private String blobName;
    private String snapshot;

    private BlobEncryption encryptionPolicy;
    private BlobDecryptionPolicy decryptionPolicy;

    /**
     * Creates a builder instance that is able to configure and construct Storage Blob clients.
     *
     * @param key An object of type {@link IKey} that is used to wrap/unwrap the content encryption key.
     * @param keyResolver  The key resolver used to select the correct key for decrypting existing blobs.
     */
    public EncryptedBlobClientBuilder(IKey key, IKeyResolver keyResolver) {
        if (key == null && keyResolver == null) {
            throw new IllegalArgumentException("Key and KeyResolver cannot both be null");
        }

        this.encryptionPolicy = new BlobEncryption(key);
        this.decryptionPolicy = new BlobDecryptionPolicy(key, keyResolver);
    }

    private AzureBlobStorageImpl constructImpl() {
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(blobName);

        HttpPipeline pipeline = super.getPipeline();
        if (pipeline == null) {
            pipeline = buildPipeline();
        }

        return new AzureBlobStorageBuilder()
            .url(String.format("%s/%s/%s", endpoint, containerName, blobName))
            .pipeline(pipeline)
            .build();
    }

    // TODO : Add more information about encrypted block blobs
    /**
     * Creates a {@link EncryptedBlockBlobClient} based on options set in the Builder.
     *
     * @return a {@link EncryptedBlockBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public EncryptedBlockBlobClient buildEncryptedBlockBlobClient() {
        return new EncryptedBlockBlobClient(buildEncryptedBlockBlobAsyncClient());
    }

    /**
     * Creates a {@link EncryptedBlockBlobAsyncClient} based on options set in the Builder.
     *
     * @return a {@link EncryptedBlockBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public EncryptedBlockBlobAsyncClient buildEncryptedBlockBlobAsyncClient() {
        return new EncryptedBlockBlobAsyncClient(constructImpl(), snapshot, cpk, encryptionPolicy);
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name, blob name)
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClientBuilder.endpoint#String}
     *
     * @param endpoint URL of the service
     * @return the updated BlobClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    @Override
    public EncryptedBlobClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobURLParts parts = URLParser.parse(url);

            this.endpoint = parts.getScheme() + "://" + parts.getHost();
            this.containerName = parts.getContainerName();
            this.blobName = parts.getBlobName();
            this.snapshot = parts.getSnapshot();

            SASTokenCredential sasTokenCredential =
                SASTokenCredential.fromSASTokenString(parts.getSasQueryParameters().encode());
            if (sasTokenCredential != null) {
                super.credential(sasTokenCredential);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage Blob endpoint url is malformed."));
        }
        return this;
    }

    @Override
    protected void addOptionalEncryptionPolicy(List<HttpPipelinePolicy> policies) {
        if (decryptionPolicy != null) {
            policies.add(decryptionPolicy);
        }
    }

    /**
     * Sets the name of the container this client is connecting to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobClientBuilder.containerName#String}
     *
     * @param containerName the name of the container
     * @return the updated EncryptedBlobClientBuilder  object
     * @throws NullPointerException If {@code containerName} is {@code null}
     */
    public EncryptedBlobClientBuilder containerName(String containerName) {
        this.containerName = Objects.requireNonNull(containerName);
        return this;
    }

    /**
     * Sets the name of the blob this client is connecting to.
     *
     * @param blobName the name of the blob
     * @return the updated EncryptedBlobClientBuilder object
     * @throws NullPointerException If {@code blobName} is {@code null}
     */
    public EncryptedBlobClientBuilder blobName(String blobName) {
        this.blobName = Objects.requireNonNull(blobName);
        return this;
    }

    /**
     * Sets the snapshot of the blob this client is connecting to.
     *
     * @param snapshot the snapshot identifier for the blob
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }
}
