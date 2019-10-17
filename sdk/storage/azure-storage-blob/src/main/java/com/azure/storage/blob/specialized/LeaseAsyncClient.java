// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import reactor.core.publisher.Mono;

import java.net.URL;

import static com.azure.core.implementation.util.FluxUtil.monoError;
import static com.azure.core.implementation.util.FluxUtil.withContext;

/**
 * This class provides a client that contains all the leasing operations for {@link BlobContainerAsyncClient containers}
 * and {@link BlobAsyncClient blobs}. This client acts as a supplement to those clients and only handles leasing
 * operations.
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
    private final ClientLogger logger = new ClientLogger(LeaseAsyncClient.class);

    private final boolean isBlob;
    private final String leaseId;
    private final AzureBlobStorageImpl client;
    private final String accountName;

    LeaseAsyncClient(HttpPipeline pipeline, String url, String leaseId, boolean isBlob, String accountName,
        String serviceVersion) {
        this.isBlob = isBlob;
        this.leaseId = leaseId;
        this.client = new AzureBlobStorageBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion)
            .build();
        this.accountName = accountName;
    }

    /**
     * Gets the {@link URL} of the lease client.
     *
     * <p>The lease will either be a container or blob URL depending on which the lease client is associated.</p>
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
        try {
            return acquireLeaseWithResponse(duration, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return withContext(context -> acquireLeaseWithResponse(duration, modifiedAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<String>> acquireLeaseWithResponse(int duration, ModifiedAccessConditions modifiedAccessConditions,
        Context context) {
        if (this.isBlob) {
            return this.client.blobs().acquireLeaseWithRestResponseAsync(
                null, null, null, duration, this.leaseId, null, modifiedAccessConditions, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        } else {
            return this.client.containers().acquireLeaseWithRestResponseAsync(
                null, null, duration, this.leaseId, null, modifiedAccessConditions, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        }
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
        try {
            return renewLeaseWithResponse(null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return withContext(context -> renewLeaseWithResponse(modifiedAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<String>> renewLeaseWithResponse(ModifiedAccessConditions modifiedAccessConditions, Context context) {
        if (this.isBlob) {
            return this.client.blobs().renewLeaseWithRestResponseAsync(
                null, null, this.leaseId, null, null, modifiedAccessConditions, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        } else {
            return this.client.containers().renewLeaseWithRestResponseAsync(
                null, this.leaseId, null, null, modifiedAccessConditions, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        }
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
        try {
            return releaseLeaseWithResponse(null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return withContext(context -> releaseLeaseWithResponse(modifiedAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> releaseLeaseWithResponse(ModifiedAccessConditions modifiedAccessConditions, Context context) {
        if (this.isBlob) {
            return this.client.blobs().releaseLeaseWithRestResponseAsync(
                null, null, this.leaseId, null, null, modifiedAccessConditions, context)
                .map(response -> new SimpleResponse<>(response, null));
        } else {
            return this.client.containers().releaseLeaseWithRestResponseAsync(
                null, this.leaseId, null, null, modifiedAccessConditions, context)
                .map(response -> new SimpleResponse<>(response, null));
        }
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
        try {
            return breakLeaseWithResponse(null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return withContext(context -> breakLeaseWithResponse(breakPeriodInSeconds, modifiedAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Integer>> breakLeaseWithResponse(Integer breakPeriodInSeconds,
        ModifiedAccessConditions modifiedAccessConditions, Context context) {
        if (this.isBlob) {
            return this.client.blobs().breakLeaseWithRestResponseAsync(
                null, null, null, breakPeriodInSeconds, null, modifiedAccessConditions, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseTime()));
        } else {
            return this.client.containers().breakLeaseWithRestResponseAsync(
                null, null, breakPeriodInSeconds, null, modifiedAccessConditions, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseTime()));
        }
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
        try {
            return changeLeaseWithResponse(proposedId, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return withContext(context -> changeLeaseWithResponse(proposedId, modifiedAccessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<String>> changeLeaseWithResponse(String proposedId, ModifiedAccessConditions modifiedAccessConditions,
        Context context) {
        if (this.isBlob) {
            return this.client.blobs().changeLeaseWithRestResponseAsync(
                null, null, this.leaseId, proposedId, null, null, modifiedAccessConditions, context)
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        } else {
            return this.client.containers().changeLeaseWithRestResponseAsync(
                null, this.leaseId, proposedId, null, null, modifiedAccessConditions, context)
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
