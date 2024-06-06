// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.BlobLeaseRequestConditions;
import com.azure.storage.blob.options.BlobAcquireLeaseOptions;
import com.azure.storage.blob.options.BlobBreakLeaseOptions;
import com.azure.storage.blob.options.BlobChangeLeaseOptions;
import com.azure.storage.blob.options.BlobReleaseLeaseOptions;
import com.azure.storage.blob.options.BlobRenewLeaseOptions;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client that contains all the leasing operations for {@link BlobContainerAsyncClient containers}
 * and {@link BlobAsyncClient blobs}. This client acts as a supplement to those clients and only handles leasing
 * operations.
 *
 * <p><strong>Instantiating a BlobLeaseAsyncClient</strong></p>
 *
 * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithBlob -->
 * <pre>
 * BlobLeaseAsyncClient blobLeaseAsyncClient = new BlobLeaseClientBuilder&#40;&#41;
 *     .blobAsyncClient&#40;blobAsyncClient&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithBlob -->
 *
 * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithContainer -->
 * <pre>
 * BlobLeaseAsyncClient blobLeaseAsyncClient = new BlobLeaseClientBuilder&#40;&#41;
 *     .containerAsyncClient&#40;blobContainerAsyncClient&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.blob.specialized.BlobLeaseClientBuilder.asyncInstantiationWithContainer -->
 *
 * <p>View {@link BlobLeaseClientBuilder this} for additional ways to construct the client.</p>
 *
 * <p>For more information about leasing see the
 * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">container leasing</a> or
 * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">blob leasing</a> documentation.</p>
 *
 * @see BlobLeaseClientBuilder
 */
@ServiceClient(builder = BlobLeaseClientBuilder.class, isAsync = true)
public final class BlobLeaseAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(BlobLeaseAsyncClient.class);

    private final String containerName;
    private final String blobName;
    private final boolean isBlob;
    private final AzureBlobStorageImpl client;
    private final String accountName;

    private volatile String leaseId;

    BlobLeaseAsyncClient(HttpPipeline pipeline, String url, String containerName, String blobName, String leaseId,
        boolean isBlob, String accountName, String serviceVersion) {
        this.isBlob = isBlob;
        this.leaseId = leaseId;
        this.client = new AzureBlobStorageImplBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion)
            .buildClient();
        this.accountName = accountName;
        this.containerName = containerName;
        this.blobName = blobName;
    }

    /**
     * Gets the {@link URL} of the lease client.
     *
     * <p>The lease will either be a container or blob URL depending on which the lease client is associated.</p>
     *
     * @return URL of the lease client.
     */
    public String getResourceUrl() {
        if (this.isBlob) {
            return this.client.getUrl() + "/" + containerName + "/" + blobName;
        } else {
            return this.client.getUrl() + "/" + containerName;
        }
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
     * Acquires a lease for write and delete operations. The lease duration must be between 15 and 60 seconds or -1 for
     * an infinite duration.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.acquireLease#int -->
     * <pre>
     * client.acquireLease&#40;60&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Lease ID is %s%n&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.acquireLease#int -->
     *
     * @param durationInSeconds The duration of the lease between 15 and 60 seconds or -1 for an infinite duration.
     * @return A reactive response containing the lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> acquireLease(int durationInSeconds) {
        return acquireLeaseWithResponse(durationInSeconds, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Acquires a lease for write and delete operations. The lease duration must be between 15 and 60 seconds, or -1 for
     * an infinite duration.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.acquireLeaseWithResponse#int-RequestConditions -->
     * <pre>
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfModifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * client.acquireLeaseWithResponse&#40;60, modifiedRequestConditions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Lease ID is %s%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.acquireLeaseWithResponse#int-RequestConditions -->
     *
     * @param durationInSeconds The duration of the lease between 15 and 60 seconds or -1 for an infinite duration.
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> acquireLeaseWithResponse(int durationInSeconds, RequestConditions modifiedRequestConditions) {
        return acquireLeaseWithResponse(new BlobAcquireLeaseOptions(durationInSeconds)
            .setRequestConditions(ModelHelper.populateBlobLeaseRequestConditions(modifiedRequestConditions)));
    }

    /**
     * Acquires a lease for write and delete operations. The lease duration must be between 15 and 60 seconds, or -1 for
     * an infinite duration.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.acquireLeaseWithResponse#BlobAcquireLeaseOptions -->
     * <pre>
     * BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions&#40;&#41;
     *     .setIfModifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * BlobAcquireLeaseOptions options = new BlobAcquireLeaseOptions&#40;60&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * client.acquireLeaseWithResponse&#40;options&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Lease ID is %s%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.acquireLeaseWithResponse#BlobAcquireLeaseOptions -->
     *
     * @param options {@link BlobAcquireLeaseOptions}
     * @return A reactive response containing the lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> acquireLeaseWithResponse(BlobAcquireLeaseOptions options) {
        try {
            return withContext(context -> acquireLeaseWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<String>> acquireLeaseWithResponse(BlobAcquireLeaseOptions options,
        Context context) {
        StorageImplUtils.assertNotNull("options", options);
        BlobLeaseRequestConditions requestConditions = (options.getRequestConditions() == null)
            ? new BlobLeaseRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;

        Mono<Response<String>> response;
        if (this.isBlob) {
            response = this.client.getBlobs().acquireLeaseWithResponseAsync(containerName, blobName, null,
                options.getDuration(), this.leaseId, requestConditions.getIfModifiedSince(),
                requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
                requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null,
                context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsLeaseId()));
        } else {
            response = this.client.getContainers().acquireLeaseWithResponseAsync(containerName, null,
                options.getDuration(), this.leaseId, requestConditions.getIfModifiedSince(),
                requestConditions.getIfUnmodifiedSince(), null, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsLeaseId()));
        }

        response = response.doOnSuccess(r -> this.leaseId = r.getValue());
        return response;
    }

    /**
     * Renews the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.renewLease -->
     * <pre>
     * client.renewLease&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Renewed lease ID is %s%n&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.renewLease -->
     *
     * @return A reactive response containing the renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> renewLease() {
        return renewLeaseWithResponse((RequestConditions) null).flatMap(FluxUtil::toMono);
    }

    /**
     * Renews the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.renewLeaseWithResponse#RequestConditions -->
     * <pre>
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * client.renewLeaseWithResponse&#40;modifiedRequestConditions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Renewed lease ID is %s%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.renewLeaseWithResponse#RequestConditions -->
     *
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> renewLeaseWithResponse(RequestConditions modifiedRequestConditions) {
        return renewLeaseWithResponse(new BlobRenewLeaseOptions()
            .setRequestConditions(ModelHelper.populateBlobLeaseRequestConditions(modifiedRequestConditions)));
    }

    /**
     * Renews the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.renewLeaseWithResponse#BlobRenewLeaseOptions -->
     * <pre>
     * BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions&#40;&#41;
     *     .setIfModifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * BlobRenewLeaseOptions options = new BlobRenewLeaseOptions&#40;&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * client.renewLeaseWithResponse&#40;options&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Lease ID is %s%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.renewLeaseWithResponse#BlobRenewLeaseOptions -->
     *
     * @param options {@link BlobRenewLeaseOptions}
     * @return A reactive response containing the renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> renewLeaseWithResponse(BlobRenewLeaseOptions options) {
        try {
            return withContext(context -> renewLeaseWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<String>> renewLeaseWithResponse(BlobRenewLeaseOptions options, Context context) {
        options = (options == null) ? new BlobRenewLeaseOptions() : options;
        BlobLeaseRequestConditions requestConditions = (options.getRequestConditions() == null)
            ? new BlobLeaseRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;

        Mono<Response<String>> response;
        if (this.isBlob) {
            response = this.client.getBlobs().renewLeaseWithResponseAsync(containerName, blobName, this.leaseId, null,
                requestConditions.getIfModifiedSince(), requestConditions.getIfUnmodifiedSince(),
                requestConditions.getIfMatch(), requestConditions.getIfNoneMatch(),
                requestConditions.getTagsConditions(), null,
                context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsLeaseId()));
        } else {
            response = this.client.getContainers().renewLeaseWithResponseAsync(containerName, this.leaseId, null,
                requestConditions.getIfModifiedSince(), requestConditions.getIfUnmodifiedSince(),
                null, context)
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
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.releaseLease -->
     * <pre>
     * client.releaseLease&#40;&#41;.subscribe&#40;response -&gt; System.out.println&#40;&quot;Completed release lease&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.releaseLease -->
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> releaseLease() {
        return releaseLeaseWithResponse((RequestConditions) null).flatMap(FluxUtil::toMono);
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.releaseLeaseWithResponse#RequestConditions -->
     * <pre>
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * client.releaseLeaseWithResponse&#40;modifiedRequestConditions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Release lease completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.releaseLeaseWithResponse#RequestConditions -->
     *
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> releaseLeaseWithResponse(RequestConditions modifiedRequestConditions) {
        return releaseLeaseWithResponse(new BlobReleaseLeaseOptions()
            .setRequestConditions(ModelHelper.populateBlobLeaseRequestConditions(modifiedRequestConditions)));
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.releaseLeaseWithResponse#BlobReleaseLeaseOptions -->
     * <pre>
     * BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions&#40;&#41;
     *     .setIfModifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * BlobReleaseLeaseOptions options = new BlobReleaseLeaseOptions&#40;&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * client.releaseLeaseWithResponse&#40;options&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Release lease completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.releaseLeaseWithResponse#BlobReleaseLeaseOptions -->
     *
     * @param options {@link BlobReleaseLeaseOptions}
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> releaseLeaseWithResponse(BlobReleaseLeaseOptions options) {
        try {
            return withContext(context -> releaseLeaseWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> releaseLeaseWithResponse(BlobReleaseLeaseOptions options, Context context) {
        options = (options == null) ? new BlobReleaseLeaseOptions() : options;
        BlobLeaseRequestConditions requestConditions = (options.getRequestConditions() == null)
            ? new BlobLeaseRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;

        if (this.isBlob) {
            return this.client.getBlobs().releaseLeaseNoCustomHeadersWithResponseAsync(containerName, blobName,
                this.leaseId, null, requestConditions.getIfModifiedSince(), requestConditions.getIfUnmodifiedSince(),
                requestConditions.getIfMatch(), requestConditions.getIfNoneMatch(),
                requestConditions.getTagsConditions(), null, context);
        } else {
            return this.client.getContainers().releaseLeaseNoCustomHeadersWithResponseAsync(containerName, this.leaseId,
                null, requestConditions.getIfModifiedSince(), requestConditions.getIfUnmodifiedSince(), null, context);
        }
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.breakLease -->
     * <pre>
     * client.breakLease&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;The broken lease has %d seconds remaining on the lease&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.breakLease -->
     *
     * @return A reactive response containing the remaining time in the broken lease in seconds.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Integer> breakLease() {
        return breakLeaseWithResponse((Integer) null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     *
     * <p>If {@code null} is passed for {@code breakPeriodInSeconds} a fixed duration lease will break after the
     * remaining lease period elapses and an infinite lease will break immediately.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.breakLeaseWithResponse#Integer-RequestConditions -->
     * <pre>
     * Integer retainLeaseInSeconds = 5;
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * client.breakLeaseWithResponse&#40;retainLeaseInSeconds, modifiedRequestConditions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;The broken lease has %d seconds remaining on the lease&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.breakLeaseWithResponse#Integer-RequestConditions -->
     *
     * @param breakPeriodInSeconds An optional duration, between 0 and 60 seconds, that the lease should continue before
     * it is broken. If the break period is longer than the time remaining on the lease the remaining time on the lease
     * is used. A new lease will not be available before the break period has expired, but the lease may be held for
     * longer than the break period.
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the remaining time in the broken lease in seconds.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Integer>> breakLeaseWithResponse(Integer breakPeriodInSeconds,
        RequestConditions modifiedRequestConditions) {
        return breakLeaseWithResponse(new BlobBreakLeaseOptions()
            .setBreakPeriod(breakPeriodInSeconds == null ? null : Duration.ofSeconds(breakPeriodInSeconds))
            .setRequestConditions(ModelHelper.populateBlobLeaseRequestConditions(modifiedRequestConditions)));
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     *
     * <p>If {@code null} is passed for {@code breakPeriodInSeconds} a fixed duration lease will break after the
     * remaining lease period elapses and an infinite lease will break immediately.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.breakLeaseWithResponse#BlobBreakLeaseOptions -->
     * <pre>
     * Integer retainLeaseInSeconds = 5;
     * BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * BlobBreakLeaseOptions options = new BlobBreakLeaseOptions&#40;&#41;
     *     .setBreakPeriod&#40;Duration.ofSeconds&#40;retainLeaseInSeconds&#41;&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * client.breakLeaseWithResponse&#40;options&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;The broken lease has %d seconds remaining on the lease&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.breakLeaseWithResponse#BlobBreakLeaseOptions -->
     *
     * @param options {@link BlobBreakLeaseOptions}
     * @return A reactive response containing the remaining time in the broken lease in seconds.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Integer>> breakLeaseWithResponse(BlobBreakLeaseOptions options) {
        try {
            return withContext(context -> breakLeaseWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Integer>> breakLeaseWithResponse(BlobBreakLeaseOptions options, Context context) {
        options = (options == null) ? new BlobBreakLeaseOptions() : options;
        BlobLeaseRequestConditions requestConditions = (options.getRequestConditions() == null)
            ? new BlobLeaseRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        Integer breakPeriod = options.getBreakPeriod() == null ? null
            : Math.toIntExact(options.getBreakPeriod().getSeconds());

        if (this.isBlob) {
            return this.client.getBlobs().breakLeaseWithResponseAsync(containerName, blobName, null,
                breakPeriod, requestConditions.getIfModifiedSince(),
                requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
                requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null,
                context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsLeaseTime()));
        } else {
            return this.client.getContainers().breakLeaseWithResponseAsync(containerName, null,
                breakPeriod, requestConditions.getIfModifiedSince(),
                requestConditions.getIfUnmodifiedSince(), null, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsLeaseTime()));
        }
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.changeLease#String -->
     * <pre>
     * client.changeLease&#40;&quot;proposedId&quot;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Changed lease ID is %s%n&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.changeLease#String -->
     *
     * @param proposedId A new lease ID in a valid GUID format.
     * @return A reactive response containing the new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> changeLease(String proposedId) {
        return changeLeaseWithResponse(proposedId, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.changeLeaseWithResponse#String-RequestConditions -->
     * <pre>
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * client.changeLeaseWithResponse&#40;&quot;proposedId&quot;, modifiedRequestConditions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Changed lease ID is %s%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.changeLeaseWithResponse#String-RequestConditions -->
     *
     * @param proposedId A new lease ID in a valid GUID format.
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> changeLeaseWithResponse(String proposedId,
        RequestConditions modifiedRequestConditions) {
        return changeLeaseWithResponse(new BlobChangeLeaseOptions(proposedId)
            .setRequestConditions(ModelHelper.populateBlobLeaseRequestConditions(modifiedRequestConditions)));
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseAsyncClient.changeLeaseWithResponse#BlobChangeLeaseOptions -->
     * <pre>
     * BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * BlobChangeLeaseOptions options = new BlobChangeLeaseOptions&#40;&quot;proposedId&quot;&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * client.changeLeaseWithResponse&#40;options&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Changed lease ID is %s%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseAsyncClient.changeLeaseWithResponse#BlobChangeLeaseOptions -->
     *
     * @param options {@link BlobChangeLeaseOptions}
     * @return A reactive response containing the new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> changeLeaseWithResponse(BlobChangeLeaseOptions options) {
        try {
            return withContext(context -> changeLeaseWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<String>> changeLeaseWithResponse(BlobChangeLeaseOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        BlobLeaseRequestConditions requestConditions = (options.getRequestConditions() == null)
            ? new BlobLeaseRequestConditions() : options.getRequestConditions();
        context = context == null ? Context.NONE : context;

        Mono<Response<String>> response;
        if (this.isBlob) {
            response = this.client.getBlobs().changeLeaseWithResponseAsync(containerName, blobName, this.leaseId,
                options.getProposedId(), null, requestConditions.getIfModifiedSince(),
                requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
                requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null,
                context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsLeaseId()));
        } else {
            response = this.client.getContainers().changeLeaseWithResponseAsync(containerName, this.leaseId,
                options.getProposedId(), null, requestConditions.getIfModifiedSince(),
                requestConditions.getIfUnmodifiedSince(), null,
                context)
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
