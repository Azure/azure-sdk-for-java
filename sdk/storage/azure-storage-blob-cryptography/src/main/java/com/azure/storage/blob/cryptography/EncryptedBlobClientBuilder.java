// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.cryptography;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BaseBlobClientBuilder;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobURLParts;
import com.azure.storage.blob.BlockBlobAsyncClient;
import com.azure.storage.blob.BlockBlobClient;
import com.azure.storage.blob.URLParser;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.CustomerProvidedKey;
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
@ServiceClientBuilder(serviceClients = {EncryptedBlockBlobAsyncClient.class, EncryptedBlockBlobClient.class})
public final class EncryptedBlobClientBuilder extends BaseBlobClientBuilder<EncryptedBlobClientBuilder> {

    private final ClientLogger logger = new ClientLogger(EncryptedBlobClientBuilder.class);

    private String containerName;
    private String blobName;
    private String snapshot;

    private IKey keyWrapper;
    private IKeyResolver keyResolver;

    /**
     * Creates a new instance of the EncryptedBlobClientBuilder
     */
    public EncryptedBlobClientBuilder() {
    }

    private AzureBlobStorageImpl constructImpl() {
        Objects.requireNonNull(containerName);
        Objects.requireNonNull(blobName);

        checkValidEncryptionParameters();

        HttpPipeline pipeline = super.getPipeline();
        if (pipeline == null) {
            pipeline = buildPipeline();
        }

        return new AzureBlobStorageBuilder()
            .url(String.format("%s/%s/%s", endpoint, containerName, blobName))
            .pipeline(pipeline)
            .build();
    }

    /**
     * Creates an {@code EncryptedBlockBlobAsyncClient} from a {@code BlockBlobAsyncClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobAsyncClient#blockblobasyncclient}
     *
     * @param client The client to convert.
     * @return The encrypted client.
     */
    public EncryptedBlockBlobAsyncClient buildEncryptedBlockBlobAsyncClient(BlockBlobAsyncClient client) {

        checkValidEncryptionParameters();

        AzureBlobStorageImpl impl = new AzureBlobStorageBuilder()
            .url(client.getBlobUrl().toString())
            .pipeline(addDecryptionPolicy(client.getHttpPipeline(),
                client.getHttpPipeline().getHttpClient()))
            .build();

        return new EncryptedBlockBlobAsyncClient(impl, client.getSnapshotId(),
            new BlobEncryptionPolicy(this.keyWrapper), client);
    }

    /**
     * Creates an {@code EncryptedBlockBlobClient} from a {@code BlockBlobClient}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobClient#blockblobclient}
     *
     * @param client The client to convert.
     * @return The encrypted client.
     */
    public EncryptedBlockBlobClient buildEncryptedBlockBlobClient(BlockBlobClient client) {

        checkValidEncryptionParameters();

        AzureBlobStorageImpl impl = new AzureBlobStorageBuilder()
            .url(client.getBlobUrl().toString())
            .pipeline(addDecryptionPolicy(client.getHttpPipeline(),
                client.getHttpPipeline().getHttpClient()))
            .build();

        // TODO (gapra) : How best to get async from sync client
        return new EncryptedBlockBlobClient(
            new EncryptedBlockBlobAsyncClient(impl, client.getSnapshotId(),
                new BlobEncryptionPolicy(this.keyWrapper), null));
    }
    /**
     * Creates a {@link EncryptedBlockBlobClient} based on options set in the Builder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobAsyncClient}
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlockBlobClient}
     *
     * @return a {@link EncryptedBlockBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     */
    public EncryptedBlockBlobAsyncClient buildEncryptedBlockBlobAsyncClient() {
        // TODO (gapra) : review this, am I creating this with everything it needs?
        // Can I just set the endpoint?
        BlockBlobAsyncClient client = new BlobClientBuilder()
            .endpoint(endpoint)
            .containerName(containerName)
            .blobName(blobName)
            .snapshot(snapshot)
            .buildBlockBlobAsyncClient();

        return new EncryptedBlockBlobAsyncClient(constructImpl(), snapshot,
            new BlobEncryptionPolicy(this.keyWrapper), client);
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name, blob name)
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
        BlobDecryptionPolicy decryptionPolicy = new BlobDecryptionPolicy(keyWrapper, keyResolver);
        policies.add(decryptionPolicy);
    }

    /**
     * Sets the name of the container this client is connecting to.
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

    /**
     * Sets the customer provided key for a Storage Builder. This will not work for an Encrypted Client
     * since they are not compatible functions.
     *
     * @param key The customer provided key
     * @return the updated EncryptedBlobClientBuilder object
     * @throws UnsupportedOperationException Since customer provided key and client side encryption
     * are different functions
     */
    @Override
    public EncryptedBlobClientBuilder customerProvidedKey(CustomerProvidedKey key) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Customer Provided Key "
            + "and Encryption are not compatible"));
    }

    /**
     * Creates a builder instance that is able to configure and construct Storage Blob clients.
     *
     * @param key An object of type {@link IKey} that is used to wrap/unwrap the content encryption key.
     * @param keyResolver The key resolver used to select the correct key for decrypting existing blobs.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder keyAndKeyResolver(IKey key, IKeyResolver keyResolver) {
        this.keyWrapper = key;
        this.keyResolver = keyResolver;
        return this;
    }

    private void checkValidEncryptionParameters() {
        // Check that key and key wrapper are not both null.
        if (this.keyWrapper == null && this.keyResolver == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Key and KeyResolver cannot both be null"));
        }
    }

    private HttpPipeline addDecryptionPolicy(HttpPipeline originalPipeline, HttpClient client) {
        HttpPipelinePolicy[] policies = new HttpPipelinePolicy[originalPipeline.getPolicyCount() + 1];
        policies[0] = new BlobDecryptionPolicy(keyWrapper, keyResolver);
        for (int i = 0; i < originalPipeline.getPolicyCount(); i++) {
            policies[i + 1] = originalPipeline.getPolicy(i);
        }
        return new HttpPipelineBuilder()
            .httpClient(client)
            .policies(policies)
            .build();
    }

}
