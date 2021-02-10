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
import com.azure.storage.file.share.options.ShareAcquireLeaseOptions;
import com.azure.storage.file.share.options.ShareBreakLeaseOptions;
import reactor.core.publisher.Mono;

import java.net.URL;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;

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
 * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-file">Azure Docs</a>.
 *
 * @see ShareLeaseClientBuilder
 */
@ServiceClient(builder = ShareLeaseClientBuilder.class, isAsync = true)
public final class ShareLeaseAsyncClient {
    private final ClientLogger logger = new ClientLogger(ShareLeaseAsyncClient.class);

    private final boolean isShareFile;
    private final String leaseId;
    private final AzureFileStorageImpl client;
    private final String accountName;

    ShareLeaseAsyncClient(HttpPipeline pipeline, String url, String leaseId, boolean isShareFile, String accountName,
        String serviceVersion) {
        this.isShareFile = isShareFile;
        this.leaseId = leaseId;
        this.client = new AzureFileStorageBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion)
            .build();
        this.accountName = accountName;
    }

    /**
     * @return URL of the lease client.
     * @deprecated Please use {@link #getResourceUrl()}
     */
    @Deprecated
    public String getFileUrl() {
        return this.client.getUrl();
    }

    /**
     * Gets the {@link URL} of the lease client.
     *
     * <p>The lease will either be a share or share file URL depending on which the lease client is associated.</p>
     *
     * @return URL of the lease client.
     */
    public String getResourceUrl() {
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
     * Acquires an infinite lease for write and delete operations.
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
     * Acquires an infinite lease for write and delete operations.
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
            return acquireLeaseWithResponse(new ShareAcquireLeaseOptions());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Acquires a lease for write and delete operations. Note: Share files only support infinite lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.acquireLeaseWithResponse#ShareAcquireLeaseOptions}
     *
     * @param options {@link ShareAcquireLeaseOptions}
     * @return A reactive response containing the lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> acquireLeaseWithResponse(ShareAcquireLeaseOptions options) {
        try {
            return withContext(context -> acquireLeaseWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<String>> acquireLeaseWithResponse(ShareAcquireLeaseOptions options, Context context) {
        options = options == null ? new ShareAcquireLeaseOptions() : options;
        context = context == null ? Context.NONE : context;

        if (this.isShareFile) {
            return this.client.files().acquireLeaseWithRestResponseAsync(null, null, null,
                options.getDuration(), this.leaseId, null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        } else {
            return this.client.shares().acquireLeaseWithRestResponseAsync(null, null,
                options.getDuration(), this.leaseId, null,
                null, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        }
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
        context = context == null ? Context.NONE : context;
        if (this.isShareFile) {
            return this.client.files().releaseLeaseWithRestResponseAsync(null, null, this.leaseId,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .map(response -> new SimpleResponse<>(response, null));
        } else {
            return this.client.shares().releaseLeaseWithRestResponseAsync(null, this.leaseId, null,
                null, null, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .map(response -> new SimpleResponse<>(response, null));
        }
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
     * Breaks the previously acquired lease, if it exists.
     * <p>For files, leases will break immediately.</p>
     * <p>For shares, leases will break after the specified duration.</p>
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
            return breakLeaseWithResponse(new ShareBreakLeaseOptions());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     * <p>For files, leases will break immediately.</p>
     * <p>For shares, leases will break after the specified duration.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.breakLeaseWithResponse#ShareBreakLeaseOptions}
     *
     * @param options {@link ShareBreakLeaseOptions}
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> breakLeaseWithResponse(ShareBreakLeaseOptions options) {
        try {
            return withContext(context -> breakLeaseWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> breakLeaseWithResponse(ShareBreakLeaseOptions options, Context context) {
        options = options == null ? new ShareBreakLeaseOptions() : options;
        context = context == null ? Context.NONE : context;
        Integer breakPeriod = options.getBreakPeriod() == null ? null
            : Math.toIntExact(options.getBreakPeriod().getSeconds());
        if (this.isShareFile) {
            return this.client.files().breakLeaseWithRestResponseAsync(null, null, null, null, null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .map(rb -> new SimpleResponse<>(rb, null));
        } else {
            return this.client.shares().breakLeaseWithRestResponseAsync(null, null, breakPeriod,
                null, null, null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .map(rb -> new SimpleResponse<>(rb, null));
        }
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
        context = context == null ? Context.NONE : context;
        if (this.isShareFile) {
            return this.client.files().changeLeaseWithRestResponseAsync(null, null, this.leaseId, null, proposedId,
                null, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        } else {
            return this.client.shares().changeLeaseWithRestResponseAsync(null, this.leaseId, null, proposedId, null,
                null, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        }
    }

    /**
     * Renews the previously acquired lease on a share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.renewLease}
     *
     * @return A reactive response containing the renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> renewLease() {
        try {
            return renewLeaseWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Renews the previously acquired lease on a share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.renewLeaseWithResponse}
     *
     * @return A reactive response containing the renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> renewLeaseWithResponse() {
        try {
            return withContext(this::renewLeaseWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<String>> renewLeaseWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        if (this.isShareFile) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(
                "Cannot renew a lease on a share file."));
        } else {
            return this.client.shares().renewLeaseWithRestResponseAsync(null, this.leaseId, null,
                null, null, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        }
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
