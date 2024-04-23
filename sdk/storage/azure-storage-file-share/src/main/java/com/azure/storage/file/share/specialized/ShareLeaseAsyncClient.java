// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.AzureFileStorageImplBuilder;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.options.ShareAcquireLeaseOptions;
import com.azure.storage.file.share.options.ShareBreakLeaseOptions;
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
 * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.asyncInstantiation -->
 * <pre>
 * ShareLeaseAsyncClient fileLeaseAsyncClient = new ShareLeaseClientBuilder&#40;&#41;
 *     .fileAsyncClient&#40;shareFileAsyncClient&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.asyncInstantiation -->
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
    private static final ClientLogger LOGGER = new ClientLogger(ShareLeaseAsyncClient.class);

    private final String shareName;
    private final String shareSnapshot;
    private final String resourcePath;
    private final boolean isShareFile;
    private final AzureFileStorageImpl client;
    private final String accountName;

    private volatile String leaseId;

    ShareLeaseAsyncClient(HttpPipeline pipeline, String url, String shareName, String shareSnapshot,
        String resourcePath, String leaseId, boolean isShareFile, String accountName, String serviceVersion,
        boolean allowTrailingDot, boolean allowSourceTrailingDot, ShareTokenIntent shareTokenIntent) {
        this.isShareFile = isShareFile;
        this.leaseId = leaseId;
        this.client = new AzureFileStorageImplBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion)
            .fileRequestIntent(shareTokenIntent)
            .allowTrailingDot(allowTrailingDot)
            .allowSourceTrailingDot(allowSourceTrailingDot)
            .buildClient();
        this.accountName = accountName;
        this.shareName = shareName;
        this.shareSnapshot = shareSnapshot;
        this.resourcePath = resourcePath;
    }

    /**
     * @return URL of the lease client.
     * @deprecated Please use {@link #getResourceUrl()}
     */
    @Deprecated
    public String getFileUrl() {
        return this.getResourceUrl();
    }

    /**
     * Gets the {@link URL} of the lease client.
     *
     * <p>The lease will either be a share or share file URL depending on which the lease client is associated.</p>
     *
     * @return URL of the lease client.
     */
    public String getResourceUrl() {
        StringBuilder resourceUrlString = new StringBuilder(this.client.getUrl()).append("/").append(shareName);
        if (this.isShareFile) {
            resourceUrlString.append("/").append(resourcePath);
        }
        if (shareSnapshot != null) {
            resourceUrlString.append("?sharesnapshot=").append(shareSnapshot);
        }
        return resourceUrlString.toString();
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
     * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.acquireLease -->
     * <pre>
     * client.acquireLease&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Lease ID is %s%n&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.acquireLease -->
     *
     * @return A reactive response containing the lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> acquireLease() {
        return acquireLeaseWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Acquires an infinite lease for write and delete operations.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.acquireLeaseWithResponse -->
     * <pre>
     * client.acquireLeaseWithResponse&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Lease ID is %s%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.acquireLeaseWithResponse -->
     *
     * @return A reactive response containing the lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> acquireLeaseWithResponse() {
        return acquireLeaseWithResponse(new ShareAcquireLeaseOptions());
    }

    /**
     * Acquires a lease for write and delete operations. Note: Share files only support infinite lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.acquireLeaseWithResponse#ShareAcquireLeaseOptions -->
     * <pre>
     * client.acquireLeaseWithResponse&#40;new ShareAcquireLeaseOptions&#40;&#41;.setDuration&#40;10&#41;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Lease ID is %s%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.acquireLeaseWithResponse#ShareAcquireLeaseOptions -->
     *
     * @param options {@link ShareAcquireLeaseOptions}
     * @return A reactive response containing the lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> acquireLeaseWithResponse(ShareAcquireLeaseOptions options) {
        try {
            return withContext(context -> acquireLeaseWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<String>> acquireLeaseWithResponse(ShareAcquireLeaseOptions options, Context context) {
        options = options == null ? new ShareAcquireLeaseOptions() : options;
        context = context == null ? Context.NONE : context;

        Mono<Response<String>> response;
        if (this.isShareFile) {
            response = this.client.getFiles().acquireLeaseWithResponseAsync(shareName, resourcePath, null,
                options.getDuration(), this.leaseId, null, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsLeaseId()));
        } else {
            response = this.client.getShares().acquireLeaseWithResponseAsync(shareName, null,
                options.getDuration(), this.leaseId, shareSnapshot, null, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsLeaseId()));
        }

        response = response.doOnSuccess(r -> this.leaseId = r.getValue());
        return response;
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.releaseLease -->
     * <pre>
     * client.releaseLease&#40;&#41;.subscribe&#40;response -&gt; System.out.println&#40;&quot;Completed release lease&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.releaseLease -->
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> releaseLease() {
        return releaseLeaseWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.releaseLeaseWithResponse -->
     * <pre>
     * client.releaseLeaseWithResponse&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Release lease completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.releaseLeaseWithResponse -->
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> releaseLeaseWithResponse() {
        try {
            return withContext(this::releaseLeaseWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> releaseLeaseWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        if (this.isShareFile) {
            return this.client.getFiles().releaseLeaseWithResponseAsync(shareName, resourcePath, this.leaseId,
                null, null, context)
                .map(response -> new SimpleResponse<>(response, null));
        } else {
            return this.client.getShares().releaseLeaseWithResponseAsync(shareName, this.leaseId, null,
                shareSnapshot, null, context)
                .map(response -> new SimpleResponse<>(response, null));
        }
    }

    /**
     * Breaks the previously acquired lease, if it exists. Leases will break immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.breakLease -->
     * <pre>
     * client.breakLease&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.println&#40;&quot;The lease has been successfully broken&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.breakLease -->
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> breakLease() {
        return breakLeaseWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     * <p>For files, leases will break immediately.</p>
     * <p>For shares, leases will break after the specified duration.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.breakLeaseWithResponse -->
     * <pre>
     * client.breakLeaseWithResponse&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.println&#40;&quot;The lease has been successfully broken&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.breakLeaseWithResponse -->
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> breakLeaseWithResponse() {
        return breakLeaseWithResponse(new ShareBreakLeaseOptions());
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     * <p>For files, leases will break immediately.</p>
     * <p>For shares, leases will break after the specified duration.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.breakLeaseWithResponse#ShareBreakLeaseOptions -->
     * <pre>
     * client.breakLeaseWithResponse&#40;new ShareBreakLeaseOptions&#40;&#41;.setBreakPeriod&#40;Duration.ofSeconds&#40;25&#41;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;The lease has been successfully broken&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.breakLeaseWithResponse#ShareBreakLeaseOptions -->
     *
     * @param options {@link ShareBreakLeaseOptions}
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> breakLeaseWithResponse(ShareBreakLeaseOptions options) {
        try {
            return withContext(context -> breakLeaseWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> breakLeaseWithResponse(ShareBreakLeaseOptions options, Context context) {
        options = options == null ? new ShareBreakLeaseOptions() : options;
        context = context == null ? Context.NONE : context;
        Integer breakPeriod = options.getBreakPeriod() == null ? null
            : Math.toIntExact(options.getBreakPeriod().getSeconds());
        if (this.isShareFile) {
            return this.client.getFiles().breakLeaseWithResponseAsync(shareName, resourcePath, null, null, null, context)
                .map(rb -> new SimpleResponse<>(rb, null));
        } else {
            return this.client.getShares().breakLeaseWithResponseAsync(shareName, null, breakPeriod,
                null, null, shareSnapshot, context)
                .map(rb -> new SimpleResponse<>(rb, null));
        }
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.changeLease#String -->
     * <pre>
     * client.changeLease&#40;&quot;proposedId&quot;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Changed lease ID is %s%n&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.changeLease#String -->
     *
     * @param proposedId A new lease ID in a valid GUID format.
     * @return A reactive response containing the new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> changeLease(String proposedId) {
        return changeLeaseWithResponse(proposedId).flatMap(FluxUtil::toMono);
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.changeLeaseWithResponse#String -->
     * <pre>
     * client.changeLeaseWithResponse&#40;&quot;proposedId&quot;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Changed lease ID is %s%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.changeLeaseWithResponse#String -->
     *
     * @param proposedId A new lease ID in a valid GUID format.
     * @return A reactive response containing the new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> changeLeaseWithResponse(String proposedId) {
        try {
            return withContext(context -> changeLeaseWithResponse(proposedId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<String>> changeLeaseWithResponse(String proposedId, Context context) {
        context = context == null ? Context.NONE : context;

        Mono<Response<String>> response;
        if (this.isShareFile) {
            response = this.client.getFiles().changeLeaseWithResponseAsync(shareName, resourcePath, this.leaseId, null, proposedId,
                null, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsLeaseId()));
        } else {
            response = this.client.getShares().changeLeaseWithResponseAsync(shareName, this.leaseId, null, proposedId, shareSnapshot,
                null, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsLeaseId()));
        }

        response = response.doOnSuccess(r -> this.leaseId = r.getValue());
        return response;
    }

    /**
     * Renews the previously acquired lease on a share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.renewLease -->
     * <pre>
     * client.renewLease&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Renewed lease ID is %s%n&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.renewLease -->
     *
     * @return A reactive response containing the renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> renewLease() {
        return renewLeaseWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Renews the previously acquired lease on a share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.renewLeaseWithResponse -->
     * <pre>
     * client.renewLeaseWithResponse&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Renewed lease ID is %s%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.specialized.ShareLeaseAsyncClient.renewLeaseWithResponse -->
     *
     * @return A reactive response containing the renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> renewLeaseWithResponse() {
        try {
            return withContext(this::renewLeaseWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<String>> renewLeaseWithResponse(Context context) {
        context = context == null ? Context.NONE : context;

        Mono<Response<String>> response;
        if (this.isShareFile) {
            throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
                "Cannot renew a lease on a share file."));
        } else {
            response = this.client.getShares().renewLeaseWithResponseAsync(shareName, this.leaseId, null,
                shareSnapshot, null, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsLeaseId()));
        }

        response = response.doOnSuccess(r -> this.leaseId = r.getValue());
        return response;
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
