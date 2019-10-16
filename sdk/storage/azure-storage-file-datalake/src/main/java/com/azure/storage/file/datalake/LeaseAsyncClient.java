// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import reactor.core.publisher.Mono;

import java.net.URL;


/**
 * This class provides a client that contains all the leasing operations for {@link FileSystemAsyncClient file systems}
 * and {@link PathAsyncClient files and directoies}. This client acts as a supplement to those clients and only handles
 * leasing operations.
 *
 * <p><strong>Instantiating a LeaseAsyncClient</strong></p>
 *
 * {@codesnippet com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithBlob}
 *
 * {@codesnippet com.azure.storage.blob.specialized.LeaseClientBuilder.asyncInstantiationWithContainer}
 *
 * <p>View {@link LeaseClientBuilder this} for additional ways to construct the client.</p>
 *
 * <p>For more information about leasing see the
 * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-container">container leasing</a> or
 * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">blob leasing</a> documentation.</p>
 *
 * @see LeaseClientBuilder
 */
@ServiceClient(builder = LeaseClientBuilder.class, isAsync = true)
public final class LeaseAsyncClient {

    private final com.azure.storage.blob.specialized.LeaseAsyncClient blobLeaseAsyncClient;

    LeaseAsyncClient(com.azure.storage.blob.specialized.LeaseAsyncClient blobLeaseAsyncClient) {
        this.blobLeaseAsyncClient = blobLeaseAsyncClient;
    }

    /**
     * Gets the {@link URL} of the lease client.
     *
     * <p>The lease will either be a container or blob URL depending on which the lease client is associated.</p>
     *
     * @return URL of the lease client.
     */
    public String getResourceUrl() {
        return this.blobLeaseAsyncClient.getResourceUrl();
    }

    /**
     * Get the lease ID for this lease.
     *
     * @return the lease ID.
     */
    public String getLeaseId() {
        return this.blobLeaseAsyncClient.getLeaseId();
    }

    /**
     * Acquires a lease for write and delete operations. The lease duration must be between 15 to 60 seconds or
     * -1 for an infinite duration.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.acquireLease#int}
     *
     * @param duration The duration of the lease between 15 to 60 seconds or -1 for an infinite duration.
     * @return A reactive response containing the lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> acquireLease(int duration) {
        return acquireLeaseWithResponse(duration, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Acquires a lease for write and delete operations. The lease duration must be between 15 to 60 seconds, or
     * -1 for an infinite duration.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.acquireLeaseWithResponse#int-ModifiedAccessConditions}
     *
     * @param duration The duration of the lease between 15 to 60 seconds or -1 for an infinite duration.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> acquireLeaseWithResponse(int duration,
        ModifiedAccessConditions modifiedAccessConditions) {
        return this.blobLeaseAsyncClient.acquireLeaseWithResponse(duration,
            Transforms.toBlobModifiedAccessConditions(modifiedAccessConditions));
    }

    /**
     * Renews the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.renewLease}
     *
     * @return A reactive response containing the renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> renewLease() {
        return renewLeaseWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Renews the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.renewLeaseWithResponse#ModifiedAccessConditions}
     *
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> renewLeaseWithResponse(ModifiedAccessConditions modifiedAccessConditions) {
        return blobLeaseAsyncClient.renewLeaseWithResponse(
            Transforms.toBlobModifiedAccessConditions(modifiedAccessConditions));
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.releaseLease}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> releaseLease() {
        return releaseLeaseWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.releaseLeaseWithResponse#ModifiedAccessConditions}
     *
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> releaseLeaseWithResponse(ModifiedAccessConditions modifiedAccessConditions) {
        return blobLeaseAsyncClient.releaseLeaseWithResponse(
            Transforms.toBlobModifiedAccessConditions(modifiedAccessConditions));
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.breakLease}
     *
     * @return A reactive response containing the remaining time in the broken lease in seconds.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Integer> breakLease() {
        return breakLeaseWithResponse(null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     *
     * <p>If {@code null} is passed for {@code breakPeriodInSeconds} a fixed duration lease will break after the
     * remaining lease period elapses and an infinite lease will break immediately.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.breakLeaseWithResponse#Integer-ModifiedAccessConditions}
     *
     * @param breakPeriodInSeconds An optional duration, between 0 and 60 seconds, that the lease should continue before
     * it is broken. If the break period is longer than the time remaining on the lease the remaining time on the lease
     * is used. A new lease will not be available before the break period has expired, but the lease may be held for
     * longer than the break period.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the remaining time in the broken lease in seconds.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Integer>> breakLeaseWithResponse(Integer breakPeriodInSeconds,
        ModifiedAccessConditions modifiedAccessConditions) {
        return blobLeaseAsyncClient.breakLeaseWithResponse(breakPeriodInSeconds,
            Transforms.toBlobModifiedAccessConditions(modifiedAccessConditions));
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.changeLease#String}
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
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.changeLeaseWithResponse#String-ModifiedAccessConditions}
     *
     * @param proposedId A new lease ID in a valid GUID format.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> changeLeaseWithResponse(String proposedId,
        ModifiedAccessConditions modifiedAccessConditions) {
        return blobLeaseAsyncClient.changeLeaseWithResponse(proposedId,
            Transforms.toBlobModifiedAccessConditions(modifiedAccessConditions));
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.blobLeaseAsyncClient.getAccountName();
    }
}
