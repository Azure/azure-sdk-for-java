// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import reactor.core.publisher.Mono;

import java.net.URL;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client that contains all the leasing operations for {@link ShareFileAsyncClient files}.
 * This client acts as a supplement to that client and only handles leasing operations.
 *
 * <p><strong>Instantiating a ShareLeaseAsyncClient</strong></p>
 *
 * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.asyncInstantiation}
 *
 * <p>View {@link ShareLeaseClientBuilder this} for additional ways to construct the client.</p>
 *
 * <p>For more information about leasing see the
 * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-file">Azure Docs</a>.
 *
 * @see ShareLeaseClientBuilder
 */
@ServiceClient(builder = ShareLeaseClientBuilder.class, isAsync = true)
public final class ShareLeaseAsyncClient {
    private final ClientLogger logger = new ClientLogger(ShareLeaseAsyncClient.class);

    private final String leaseId;
    private final AzureFileStorageImpl client;
    private final String accountName;

    ShareLeaseAsyncClient(HttpPipeline pipeline, String url, String leaseId, String accountName,
        String serviceVersion) {
        this.leaseId = leaseId;
        this.client = new AzureFileStorageBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion)
            .build();
        this.accountName = accountName;
    }

    /**
     * Gets the {@link URL} of the lease client.
     *
     * @return URL of the lease client.
     */
    public String getFileUrl() {
        return this.client.getUrl();
    }

    /**
     * Get the lease ID for this lease.
     *
     * @return the lease ID.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Acquires a lease for write and delete operations. All leases are infinite.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.acquireLease}
     *
     * @return A reactive response containing the lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> acquireLease() {
        try {
            return acquireLeaseWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Acquires a lease for write and delete operations. All leases are infinite.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.acquireLeaseWithResponse}
     *
     * @return A reactive response containing the lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> acquireLeaseWithResponse() {
        try {
            return withContext(this::acquireLeaseWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<String>> acquireLeaseWithResponse(Context context) {
        return this.client.files().acquireLeaseWithRestResponseAsync(null, null, null, -1, this.leaseId, null, context)
            .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.releaseLease}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> releaseLease() {
        try {
            return releaseLeaseWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.releaseLeaseWithResponse}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> releaseLeaseWithResponse() {
        try {
            return withContext(this::releaseLeaseWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> releaseLeaseWithResponse(Context context) {
        return this.client.files().releaseLeaseWithRestResponseAsync(null, null, this.leaseId, context)
            .map(response -> new SimpleResponse<>(response, null));

    }

    /**
     * Breaks the previously acquired lease, if it exists. Leases will break immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.breakLease}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> breakLease() {
        try {
            return breakLeaseWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Breaks the previously acquired lease, if it exists. Leases will break immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.breakLeaseWithResponse}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> breakLeaseWithResponse() {
        try {
            return withContext(this::breakLeaseWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> breakLeaseWithResponse(Context context) {
        return this.client.files().breakLeaseWithRestResponseAsync(null, null, null, null, null, context)
            .map(rb -> new SimpleResponse<>(rb, null));
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.changeLease#String}
     *
     * @param proposedId A new lease ID in a valid GUID format.
     * @return A reactive response containing the new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> changeLease(String proposedId) {
        try {
            return changeLeaseWithResponse(proposedId).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.changeLeaseWithResponse#String}
     *
     * @param proposedId A new lease ID in a valid GUID format.
     * @return A reactive response containing the new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> changeLeaseWithResponse(String proposedId) {
        try {
            return withContext(context -> changeLeaseWithResponse(proposedId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<String>> changeLeaseWithResponse(String proposedId, Context context) {
        return this.client.files().changeLeaseWithRestResponseAsync(null, null, this.leaseId, null, proposedId,
            null, context).map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }
}
