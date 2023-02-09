// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.options.BlobAcquireLeaseOptions;
import com.azure.storage.blob.options.BlobBreakLeaseOptions;
import com.azure.storage.blob.options.BlobChangeLeaseOptions;
import com.azure.storage.blob.options.BlobReleaseLeaseOptions;
import com.azure.storage.blob.options.BlobRenewLeaseOptions;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.net.URL;
import java.time.Duration;

/**
 * This class provides a client that contains all the leasing operations for {@link BlobContainerClient containers} and
 * {@link BlobClient blobs}. This client acts as a supplement to those clients and only handles leasing operations.
 *
 * <p><strong>Instantiating a BlobLeaseClient</strong></p>
 *
 * <!-- src_embed com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithBlob -->
 * <pre>
 * BlobLeaseClient blobLeaseClient = new BlobLeaseClientBuilder&#40;&#41;
 *     .blobClient&#40;blobClient&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithBlob -->
 *
 * <!-- src_embed com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithContainer -->
 * <pre>
 * BlobLeaseClient blobLeaseClient = new BlobLeaseClientBuilder&#40;&#41;
 *     .containerClient&#40;blobContainerClient&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.blob.specialized.LeaseClientBuilder.syncInstantiationWithContainer -->
 *
 * <p>View {@link BlobLeaseClientBuilder this} for additional ways to construct the client.</p>
 *
 * <p>For more information about leasing see the
 * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">container leasing</a> or
 * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">blob leasing</a> documentation.</p>
 *
 * @see BlobLeaseClientBuilder
 */
@ServiceClient(builder =  BlobLeaseClientBuilder.class)
public final class BlobLeaseClient {
    private final BlobLeaseAsyncClient client;

    BlobLeaseClient(BlobLeaseAsyncClient client) {
        this.client = client;
    }

    /**
     * Gets the {@link URL} of the lease client.
     *
     * <p>The lease will either be a container or blob URL depending on which the lease client is associated.</p>
     *
     * @return URL of the lease client.
     */
    public String getResourceUrl() {
        return client.getResourceUrl();
    }

    /**
     * Get the lease ID for this lease.
     *
     * @return the lease ID.
     */
    public String getLeaseId() {
        return client.getLeaseId();
    }

    /**
     * Acquires a lease for write and delete operations. The lease duration must be between 15 to 60 seconds or
     * -1 for an infinite duration.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.acquireLease#int -->
     * <pre>
     * System.out.printf&#40;&quot;Lease ID is %s%n&quot;, client.acquireLease&#40;60&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.acquireLease#int -->
     *
     * @param duration The duration of the lease between 15 to 60 seconds or -1 for an infinite duration.
     * @return The lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String acquireLease(int duration) {
        return acquireLeaseWithResponse(duration, null, null, Context.NONE).getValue();
    }

    /**
     * Acquires a lease for write and delete operations. The lease duration must be between 15 to 60 seconds or
     * -1 for an infinite duration.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.acquireLeaseWithResponse#int-RequestConditions-Duration-Context -->
     * <pre>
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfModifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Lease ID is %s%n&quot;, client
     *     .acquireLeaseWithResponse&#40;60, modifiedRequestConditions, timeout, new Context&#40;key, value&#41;&#41;
     *     .getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.acquireLeaseWithResponse#int-RequestConditions-Duration-Context -->
     *
     * @param duration The duration of the lease between 15 to 60 seconds or -1 for an infinite duration.
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> acquireLeaseWithResponse(int duration, RequestConditions modifiedRequestConditions,
        Duration timeout, Context context) {
        return acquireLeaseWithResponse(new BlobAcquireLeaseOptions(duration)
        .setRequestConditions(ModelHelper.populateBlobLeaseRequestConditions(modifiedRequestConditions)), timeout,
            context);
    }

    /**
     * Acquires a lease for write and delete operations. The lease duration must be between 15 to 60 seconds or
     * -1 for an infinite duration.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.acquireLeaseWithResponse#BlobAcquireLeaseOptions-Duration-Context -->
     * <pre>
     * BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions&#40;&#41;
     *     .setIfModifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * BlobAcquireLeaseOptions options = new BlobAcquireLeaseOptions&#40;60&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * System.out.printf&#40;&quot;Lease ID is %s%n&quot;, client
     *     .acquireLeaseWithResponse&#40;options, timeout, new Context&#40;key, value&#41;&#41;
     *     .getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.acquireLeaseWithResponse#BlobAcquireLeaseOptions-Duration-Context -->
     *
     * @param options {@link BlobAcquireLeaseOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> acquireLeaseWithResponse(BlobAcquireLeaseOptions options, Duration timeout,
        Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(this.client.acquireLeaseWithResponse(options, context),
            timeout);
    }

    /**
     * Renews the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.renewLease -->
     * <pre>
     * System.out.printf&#40;&quot;Renewed lease ID is %s%n&quot;, client.renewLease&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.renewLease -->
     *
     * @return The renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String renewLease() {
        return renewLeaseWithResponse((RequestConditions) null, null, Context.NONE).getValue();
    }

    /**
     * Renews the previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.renewLeaseWithResponse#RequestConditions-Duration-Context -->
     * <pre>
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Renewed lease ID is %s%n&quot;,
     *     client.renewLeaseWithResponse&#40;modifiedRequestConditions, timeout, new Context&#40;key, value&#41;&#41;
     *         .getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.renewLeaseWithResponse#RequestConditions-Duration-Context -->
     *
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> renewLeaseWithResponse(RequestConditions modifiedRequestConditions, Duration timeout,
        Context context) {
        return renewLeaseWithResponse(new BlobRenewLeaseOptions()
                .setRequestConditions(ModelHelper.populateBlobLeaseRequestConditions(modifiedRequestConditions)),
            timeout, context);
    }

    /**
     * Renews the previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.renewLeaseWithResponse#BlobRenewLeaseOptions-Duration-Context -->
     * <pre>
     * BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions&#40;&#41;
     *     .setIfModifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * BlobRenewLeaseOptions options = new BlobRenewLeaseOptions&#40;&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * System.out.printf&#40;&quot;Renewed lease ID is %s%n&quot;,
     *     client.renewLeaseWithResponse&#40;options, timeout, new Context&#40;key, value&#41;&#41;
     *         .getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.renewLeaseWithResponse#BlobRenewLeaseOptions-Duration-Context -->
     *
     * @param options {@link BlobRenewLeaseOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> renewLeaseWithResponse(BlobRenewLeaseOptions options, Duration timeout,
        Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(this.client.renewLeaseWithResponse(options, context), timeout);
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.releaseLease -->
     * <pre>
     * client.releaseLease&#40;&#41;;
     * System.out.println&#40;&quot;Release lease completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.releaseLease -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void releaseLease() {
        releaseLeaseWithResponse((RequestConditions) null, null, Context.NONE);
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.releaseLeaseWithResponse#RequestConditions-Duration-Context -->
     * <pre>
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Release lease completed with status %d%n&quot;,
     *     client.releaseLeaseWithResponse&#40;modifiedRequestConditions, timeout, new Context&#40;key, value&#41;&#41;
     *         .getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.releaseLeaseWithResponse#RequestConditions-Duration-Context -->
     *
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> releaseLeaseWithResponse(RequestConditions modifiedRequestConditions, Duration timeout,
        Context context) {
        return releaseLeaseWithResponse(new BlobReleaseLeaseOptions()
                .setRequestConditions(ModelHelper.populateBlobLeaseRequestConditions(modifiedRequestConditions)),
            timeout, context);
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.releaseLeaseWithResponse#BlobReleaseLeaseOptions-Duration-Context -->
     * <pre>
     * BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions&#40;&#41;
     *     .setIfModifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * BlobReleaseLeaseOptions options = new BlobReleaseLeaseOptions&#40;&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * System.out.printf&#40;&quot;Release lease completed with status %d%n&quot;,
     *     client.releaseLeaseWithResponse&#40;options, timeout, new Context&#40;key, value&#41;&#41;
     *         .getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.releaseLeaseWithResponse#BlobReleaseLeaseOptions-Duration-Context -->
     *
     * @param options {@link BlobReleaseLeaseOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> releaseLeaseWithResponse(BlobReleaseLeaseOptions options, Duration timeout,
        Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(this.client.releaseLeaseWithResponse(options, context),
            timeout);
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.breakLease -->
     * <pre>
     * System.out.printf&#40;&quot;The broken lease has %d seconds remaining on the lease&quot;, client.breakLease&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.breakLease -->
     *
     * @return The remaining time in the broken lease in seconds.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Integer breakLease() {
        return breakLeaseWithResponse(null, null, null, Context.NONE).getValue();
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     *
     * <p>If {@code null} is passed for {@code breakPeriodInSeconds} a fixed duration lease will break after the
     * remaining lease period elapses and an infinite lease will break immediately.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.breakLeaseWithResponse#Integer-RequestConditions-Duration-Context -->
     * <pre>
     * Integer retainLeaseInSeconds = 5;
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * System.out.printf&#40;&quot;The broken lease has %d seconds remaining on the lease&quot;, client
     *     .breakLeaseWithResponse&#40;retainLeaseInSeconds, modifiedRequestConditions, timeout, new Context&#40;key, value&#41;&#41;
     *     .getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.breakLeaseWithResponse#Integer-RequestConditions-Duration-Context -->
     *
     * @param breakPeriodInSeconds An optional duration, between 0 and 60 seconds, that the lease should continue before
     * it is broken. If the break period is longer than the time remaining on the lease the remaining time on the lease
     * is used. A new lease will not be available before the break period has expired, but the lease may be held for
     * longer than the break period.
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The remaining time in the broken lease in seconds.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Integer> breakLeaseWithResponse(Integer breakPeriodInSeconds,
        RequestConditions modifiedRequestConditions, Duration timeout, Context context) {
        return breakLeaseWithResponse(new BlobBreakLeaseOptions()
                .setBreakPeriod(breakPeriodInSeconds == null ? null : Duration.ofSeconds(breakPeriodInSeconds))
                .setRequestConditions(ModelHelper.populateBlobLeaseRequestConditions(modifiedRequestConditions)),
            timeout, context);
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     *
     * <p>If {@code null} is passed for {@code breakPeriodInSeconds} a fixed duration lease will break after the
     * remaining lease period elapses and an infinite lease will break immediately.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.breakLeaseWithResponse#BlobBreakLeaseOptions-Duration-Context -->
     * <pre>
     * Integer retainLeaseInSeconds = 5;
     * BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * BlobBreakLeaseOptions options = new BlobBreakLeaseOptions&#40;&#41;
     *     .setBreakPeriod&#40;Duration.ofSeconds&#40;retainLeaseInSeconds&#41;&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * System.out.printf&#40;&quot;The broken lease has %d seconds remaining on the lease&quot;, client
     *     .breakLeaseWithResponse&#40;options, timeout, new Context&#40;key, value&#41;&#41;
     *     .getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.breakLeaseWithResponse#BlobBreakLeaseOptions-Duration-Context -->
     *
     * @param options {@link BlobBreakLeaseOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The remaining time in the broken lease in seconds.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Integer> breakLeaseWithResponse(BlobBreakLeaseOptions options, Duration timeout, Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(this.client.breakLeaseWithResponse(options, context), timeout);
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.changeLease#String -->
     * <pre>
     * System.out.printf&#40;&quot;Changed lease ID is %s%n&quot;, client.changeLease&#40;&quot;proposedId&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.changeLease#String -->
     *
     * @param proposedId A new lease ID in a valid GUID format.
     * @return The new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String changeLease(String proposedId) {
        return changeLeaseWithResponse(proposedId, null, null, Context.NONE).getValue();
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.changeLeaseWithResponse#String-RequestConditions-Duration-Context -->
     * <pre>
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Changed lease ID is %s%n&quot;,
     *     client.changeLeaseWithResponse&#40;&quot;proposedId&quot;, modifiedRequestConditions, timeout, new Context&#40;key, value&#41;&#41;
     *         .getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.changeLeaseWithResponse#String-RequestConditions-Duration-Context -->
     *
     * @param proposedId A new lease ID in a valid GUID format.
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> changeLeaseWithResponse(String proposedId,
        RequestConditions modifiedRequestConditions, Duration timeout, Context context) {
        return changeLeaseWithResponse(new BlobChangeLeaseOptions(proposedId)
            .setRequestConditions(ModelHelper.populateBlobLeaseRequestConditions(modifiedRequestConditions)), timeout,
            context);
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobLeaseClient.changeLeaseWithResponse#BlobChangeLeaseOptions-Duration-Context -->
     * <pre>
     * BlobLeaseRequestConditions requestConditions = new BlobLeaseRequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * BlobChangeLeaseOptions options = new BlobChangeLeaseOptions&#40;&quot;proposedId&quot;&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * System.out.printf&#40;&quot;Changed lease ID is %s%n&quot;,
     *     client.changeLeaseWithResponse&#40;options, timeout, new Context&#40;key, value&#41;&#41;
     *         .getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobLeaseClient.changeLeaseWithResponse#BlobChangeLeaseOptions-Duration-Context -->
     *
     * @param options {@link BlobChangeLeaseOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> changeLeaseWithResponse(BlobChangeLeaseOptions options, Duration timeout, Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(this.client.changeLeaseWithResponse(options, context),
            timeout);
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return client.getAccountName();
    }

}
