// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.specialized;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceVersion;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of Storage Lease
 * clients. Lease clients are able to interact only with share file clients and act as a supplement client. A
 * new instance of {@link ShareLeaseClient} and {@link ShareLeaseAsyncClient} are constructed every time
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} are called
 * respectively.
 *
 * <p>When a client is instantiated and a {@link #leaseId(String) leaseId} hasn't been set a {@link UUID} will be used
 * as the lease identifier.</p>
 *
 * <p><strong>Instantiating LeaseClients</strong></p>
 *
 * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.syncInstantiationWithLeaseId}
 *
 * <p><strong>Instantiating LeaseAsyncClients</strong></p>
 *
 * {@codesnippet com.azure.storage.file.specialized.ShareLeaseClientBuilder.asyncInstantiationWithLeaseId}
 *
 * @see ShareLeaseClient
 * @see ShareLeaseAsyncClient
 */
@ServiceClientBuilder(serviceClients = { ShareLeaseClient.class, ShareLeaseAsyncClient.class })
public final class ShareLeaseClientBuilder {
    private HttpPipeline pipeline;
    private String url;
    private String leaseId;
    private String accountName;
    private ShareServiceVersion serviceVersion;

    /**
     * Creates a {@link ShareLeaseClient} based on the configurations set in the builder.
     *
     * @return a {@link ShareLeaseClient} based on the configurations in this builder.
     */
    public ShareLeaseClient buildClient() {
        return new ShareLeaseClient(buildAsyncClient());
    }

    /**
     * Creates a {@link ShareLeaseAsyncClient} based on the configurations set in the builder.
     *
     * @return a {@link ShareLeaseAsyncClient} based on the configurations in this builder.
     */
    public ShareLeaseAsyncClient buildAsyncClient() {
        ShareServiceVersion version = (serviceVersion == null) ? ShareServiceVersion.getLatest() : serviceVersion;
        return new ShareLeaseAsyncClient(pipeline, url, getLeaseId(), accountName, version.getVersion());
    }

    /**
     * Configures the builder based on the passed {@link ShareFileClient}. This will set the {@link HttpPipeline} and
     * {@link URL} that are used to interact with the service.
     *
     * @param fileClient ShareFileClient used to configure the builder.
     * @return the updated ShareLeaseClientBuilder object
     * @throws NullPointerException If {@code fileClient} is {@code null}.
     */
    public ShareLeaseClientBuilder fileClient(ShareFileClient fileClient) {
        Objects.requireNonNull(fileClient);
        this.pipeline = fileClient.getHttpPipeline();
        this.url = fileClient.getFileUrl();
        this.accountName = fileClient.getAccountName();
        this.serviceVersion = fileClient.getServiceVersion();
        return this;
    }

    /**
     * Configures the builder based on the passed {@link ShareFileAsyncClient}. This will set the {@link HttpPipeline}
     * and {@link URL} that are used to interact with the service.
     *
     * @param fileAsyncClient BlobAsyncClient used to configure the builder.
     * @return the updated ShareLeaseClientBuilder object
     * @throws NullPointerException If {@code fileAsyncClient} is {@code null}.
     */
    public ShareLeaseClientBuilder fileAsyncClient(ShareFileAsyncClient fileAsyncClient) {
        Objects.requireNonNull(fileAsyncClient);
        this.pipeline = fileAsyncClient.getHttpPipeline();
        this.url = fileAsyncClient.getFileUrl();
        this.accountName = fileAsyncClient.getAccountName();
        this.serviceVersion = fileAsyncClient.getServiceVersion();
        return this;
    }

    /**
     * Sets the identifier for the lease.
     *
     * <p>If a lease ID isn't set then a {@link UUID} will be used.</p>
     *
     * @param leaseId Identifier for the lease.
     * @return the updated ShareLeaseClientBuilder object
     */
    public ShareLeaseClientBuilder leaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    private String getLeaseId() {
        return (leaseId == null) ? UUID.randomUUID().toString() : leaseId;
    }
}
