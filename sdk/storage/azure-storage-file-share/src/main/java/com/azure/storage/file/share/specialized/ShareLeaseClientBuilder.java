// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.specialized;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.storage.file.share.ShareAsyncClient;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceVersion;
import com.azure.storage.file.share.models.ShareTokenIntent;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of Storage Lease
 * clients. Lease clients are able to interact with both share and share file clients and act as a supplement client. A
 * new instance of {@link ShareLeaseClient} and {@link ShareLeaseAsyncClient} are constructed every time
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} are called
 * respectively.
 *
 * <p>When a client is instantiated and a {@link #leaseId(String) leaseId} hasn't been set a {@link UUID} will be used
 * as the lease identifier.</p>
 *
 * <p><strong>Instantiating LeaseClients</strong></p>
 *
 * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.syncInstantiationWithFileAndLeaseId -->
 * <pre>
 * ShareLeaseClient fileLeaseClient = new ShareLeaseClientBuilder&#40;&#41;
 *     .fileClient&#40;shareFileClient&#41;
 *     .leaseId&#40;leaseId&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.syncInstantiationWithFileAndLeaseId -->
 *
 * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.syncInstantiationWithShareAndLeaseId -->
 * <pre>
 * ShareLeaseClient fileLeaseClient = new ShareLeaseClientBuilder&#40;&#41;
 *     .shareClient&#40;shareClient&#41;
 *     .leaseId&#40;leaseId&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.syncInstantiationWithShareAndLeaseId -->
 *
 * <p><strong>Instantiating LeaseAsyncClients</strong></p>
 *
 * <!-- src_embed com.azure.storage.file.specialized.ShareLeaseClientBuilder.asyncInstantiationWithFileAndLeaseId -->
 * <pre>
 * ShareLeaseAsyncClient fileLeaseAsyncClient = new ShareLeaseClientBuilder&#40;&#41;
 *     .fileAsyncClient&#40;shareFileAsyncClient&#41;
 *     .leaseId&#40;leaseId&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.specialized.ShareLeaseClientBuilder.asyncInstantiationWithFileAndLeaseId -->
 *
 * <!-- src_embed com.azure.storage.file.specialized.ShareLeaseClientBuilder.asyncInstantiationWithShareAndLeaseId -->
 * <pre>
 * ShareLeaseAsyncClient fileLeaseAsyncClient = new ShareLeaseClientBuilder&#40;&#41;
 *     .shareAsyncClient&#40;shareAsyncClient&#41;
 *     .leaseId&#40;leaseId&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.specialized.ShareLeaseClientBuilder.asyncInstantiationWithShareAndLeaseId -->
 *
 * @see ShareLeaseClient
 * @see ShareLeaseAsyncClient
 */
@ServiceClientBuilder(serviceClients = { ShareLeaseClient.class, ShareLeaseAsyncClient.class })
public final class ShareLeaseClientBuilder {
    private String shareName;
    private String resourcePath;
    private String shareSnapshot;
    private HttpPipeline pipeline;
    private String url;
    private String leaseId;
    private boolean isShareFile;
    private String accountName;
    private ShareServiceVersion serviceVersion;
    private ShareTokenIntent shareTokenIntent;
    private boolean allowSourceTrailingDot;
    private boolean allowTrailingDot;

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
        return new ShareLeaseAsyncClient(pipeline, url, shareName, shareSnapshot, resourcePath, getLeaseId(),
            isShareFile, accountName, version.getVersion(), allowTrailingDot, allowSourceTrailingDot, shareTokenIntent);
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
        this.url = fileClient.getAccountUrl();
        this.shareName = fileClient.getShareName();
        this.resourcePath = fileClient.getFilePath();
        this.isShareFile = true;
        this.accountName = fileClient.getAccountName();
        this.serviceVersion = fileClient.getServiceVersion();
        return this;
    }

    /**
     * Configures the builder based on the passed {@link ShareFileAsyncClient}. This will set the {@link HttpPipeline}
     * and {@link URL} that are used to interact with the service.
     *
     * @param fileAsyncClient ShareFileAsyncClient used to configure the builder.
     * @return the updated ShareLeaseClientBuilder object
     * @throws NullPointerException If {@code fileAsyncClient} is {@code null}.
     */
    public ShareLeaseClientBuilder fileAsyncClient(ShareFileAsyncClient fileAsyncClient) {
        Objects.requireNonNull(fileAsyncClient);
        this.pipeline = fileAsyncClient.getHttpPipeline();
        this.url = fileAsyncClient.getAccountUrl();
        this.shareName = fileAsyncClient.getShareName();
        this.resourcePath = fileAsyncClient.getFilePath();
        this.isShareFile = true;
        this.accountName = fileAsyncClient.getAccountName();
        this.serviceVersion = fileAsyncClient.getServiceVersion();
        return this;
    }

    /**
     * Configures the builder based on the passed {@link ShareClient}. This will set the {@link HttpPipeline} and
     * {@link URL} that are used to interact with the service.
     *
     * @param shareClient ShareClient used to configure the builder.
     * @return the updated ShareLeaseClientBuilder object
     * @throws NullPointerException If {@code fileClient} is {@code null}.
     */
    public ShareLeaseClientBuilder shareClient(ShareClient shareClient) {
        Objects.requireNonNull(shareClient);
        this.pipeline = shareClient.getHttpPipeline();
        this.url = shareClient.getAccountUrl();
        this.shareName = shareClient.getShareName();
        this.shareSnapshot = shareClient.getSnapshotId();
        this.isShareFile = false;
        this.accountName = shareClient.getAccountName();
        this.serviceVersion = shareClient.getServiceVersion();
        return this;
    }

    /**
     * Configures the builder based on the passed {@link ShareAsyncClient}. This will set the {@link HttpPipeline}
     * and {@link URL} that are used to interact with the service.
     *
     * @param shareAsyncClient ShareAsyncClient used to configure the builder.
     * @return the updated ShareLeaseClientBuilder object
     * @throws NullPointerException If {@code fileAsyncClient} is {@code null}.
     */
    public ShareLeaseClientBuilder shareAsyncClient(ShareAsyncClient shareAsyncClient) {
        Objects.requireNonNull(shareAsyncClient);
        this.pipeline = shareAsyncClient.getHttpPipeline();
        this.url = shareAsyncClient.getAccountUrl();
        this.shareName = shareAsyncClient.getShareName();
        this.shareSnapshot = shareAsyncClient.getSnapshotId();
        this.isShareFile = false;
        this.accountName = shareAsyncClient.getAccountName();
        this.serviceVersion = shareAsyncClient.getServiceVersion();
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


    /**
     * Set the trailing dot property to specify whether trailing dot will be trimmed or not from the source URI.
     *
     * If set to true, trailing dot (.) will be allowed to suffix directory and file names.
     * If false, the trailing dot will be trimmed. Supported by x-ms-version 2022-11-02 and above.
     *
     * @param allowSourceTrailingDot the allowSourceTrailingDot value.
     * @return the updated ShareLeaseClientBuilder object
     */
    public ShareLeaseClientBuilder allowSourceTrailingDot(boolean allowSourceTrailingDot) {
        this.allowSourceTrailingDot = allowSourceTrailingDot;
        return this;
    }

    /**
     * Set the trailing dot property to specify whether trailing dot will be trimmed or not from the target URI.
     *
     * If set to true, trailing dot (.) will be allowed to suffix directory and file names.
     * If false, the trailing dot will be trimmed. Supported by x-ms-version 2022-11-02 and above.
     *
     * @param allowTrailingDot the allowTrailingDot value.
     * @return the updated ShareLeaseClientBuilder object
     */
    public ShareLeaseClientBuilder allowTrailingDot(boolean allowTrailingDot) {
        this.allowTrailingDot = allowTrailingDot;
        return this;
    }

    /**
     * Sets the {@link ShareTokenIntent} that specifies whether there is intent for a file to be backed up.
     * This is currently required when using {@link TokenCredential}, and ignored for other forms of authentication.
     *
     * @param shareTokenIntent the {@link ShareTokenIntent} value.
     * @return the updated ShareLeaseClientBuilder object
     */
    public ShareLeaseClientBuilder shareTokenIntent(ShareTokenIntent shareTokenIntent) {
        this.shareTokenIntent = shareTokenIntent;
        return this;
    }
}
