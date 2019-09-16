// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.ContainerAsyncClient;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.StorageErrorException;
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.common.Utility;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

import static com.azure.core.implementation.util.FluxUtil.withContext;

/**
 *
 */
public final class LeaseAsyncClient {
    private final ClientLogger logger = new ClientLogger(LeaseAsyncClient.class);

    private final boolean isBlob;
    private final String leaseId;
    private final AzureBlobStorageImpl client;

    LeaseAsyncClient(HttpPipeline pipeline, URL url, String leaseId, boolean isBlob) {
        this.isBlob = isBlob;
        this.leaseId = leaseId;
        this.client = new AzureBlobStorageBuilder()
            .pipeline(pipeline)
            .url(url.toString())
            .build();
    }

    /**
     * Gets the {@link URL} of the lease client. If the lease client is using a {@link BlobAsyncClient} it will be the
     * URL of the blob, otherwise the URL will be the {@link ContainerAsyncClient} URL.
     *
     * @return URL of the lease client.
     */
    public URL getLeaseAsyncClientUrl() {
        try {
            return new URL(this.client.getUrl());
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new RuntimeException("Unable to parse URL"));
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
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.acquireLease#int}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param duration The  duration of the lease, in seconds, or negative one (-1) for a lease that never expires. A
     * non-infinite lease can be between 15 and 60 seconds.
     * @return A reactive response containing the lease ID.
     */
    public Mono<String> acquireLease(int duration) {
        return acquireLeaseWithResponse(duration, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.acquireLeaseWithResponse#int-ModifiedAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param duration The  duration of the lease, in seconds, or negative one (-1) for a lease that never expires. A
     * non-infinite lease can be between 15 and 60 seconds.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the lease ID.
     * @throws IllegalArgumentException If {@code duration} is outside the bounds of 15 to 60 or isn't -1.
     */
    public Mono<Response<String>> acquireLeaseWithResponse(int duration,
        ModifiedAccessConditions modifiedAccessConditions) {
        return withContext(context -> acquireLeaseWithResponse(duration, modifiedAccessConditions, context));
    }

    Mono<Response<String>> acquireLeaseWithResponse(int duration, ModifiedAccessConditions modifiedAccessConditions,
        Context context) {
        if (this.isBlob) {
            return postProcessResponse(this.client.blobs().acquireLeaseWithRestResponseAsync(
                null, null, null, duration, this.leaseId, null, modifiedAccessConditions, context))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        } else {
            return postProcessResponse(this.client.containers().acquireLeaseWithRestResponseAsync(
                null, null, duration, this.leaseId, null, modifiedAccessConditions, context))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        }
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.renewLease}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @return A reactive response containing the renewed lease ID.
     */
    public Mono<String> renewLease() {
        return renewLeaseWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.renewLeaseWithResponse#ModifiedAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the renewed lease ID.
     */
    public Mono<Response<String>> renewLeaseWithResponse(ModifiedAccessConditions modifiedAccessConditions) {
        return withContext(context -> renewLeaseWithResponse(modifiedAccessConditions, context));
    }

    Mono<Response<String>> renewLeaseWithResponse(ModifiedAccessConditions modifiedAccessConditions, Context context) {
        if (this.isBlob) {
            return postProcessResponse(this.client.blobs().renewLeaseWithRestResponseAsync(
                null, null, this.leaseId, null, null, modifiedAccessConditions, context))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        } else {
            return postProcessResponse(this.client.containers().renewLeaseWithRestResponseAsync(
                null, this.leaseId, null, null, modifiedAccessConditions, context))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        }
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.releaseLease}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> releaseLease() {
        return releaseLeaseWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.releaseLeaseWithResponse#ModifiedAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response signalling completion.
     */
    public Mono<VoidResponse> releaseLeaseWithResponse(ModifiedAccessConditions modifiedAccessConditions) {
        return withContext(context -> releaseLeaseWithResponse(modifiedAccessConditions, context));
    }

    Mono<VoidResponse> releaseLeaseWithResponse(ModifiedAccessConditions modifiedAccessConditions, Context context) {
        if (this.isBlob) {
            return postProcessResponse(this.client.blobs().releaseLeaseWithRestResponseAsync(
                null, null, this.leaseId, null, null, modifiedAccessConditions, context))
                .map(VoidResponse::new);
        } else {
            return postProcessResponse(this.client.containers().releaseLeaseWithRestResponseAsync(
                null, this.leaseId, null, null, modifiedAccessConditions, context))
                .map(VoidResponse::new);
        }
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.breakLease}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @return A reactive response containing the remaining time in the broken lease in seconds.
     */
    public Mono<Integer> breakLease() {
        return breakLeaseWithResponse(null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.breakLeaseWithResponse#Integer-ModifiedAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param breakPeriodInSeconds An optional {@code Integer} representing the proposed duration of seconds that the
     * lease should continue before it is broken, between 0 and 60 seconds. This break period is only used if it is
     * shorter than the time remaining on the lease. If longer, the time remaining on the lease is used. A new lease
     * will not be available before the break period has expired, but the lease may be held for longer than the break
     * period.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the remaining time in the broken lease in seconds.
     */
    public Mono<Response<Integer>> breakLeaseWithResponse(Integer breakPeriodInSeconds,
        ModifiedAccessConditions modifiedAccessConditions) {
        return withContext(context -> breakLeaseWithResponse(breakPeriodInSeconds, modifiedAccessConditions, context));
    }

    Mono<Response<Integer>> breakLeaseWithResponse(Integer breakPeriodInSeconds,
        ModifiedAccessConditions modifiedAccessConditions, Context context) {
        if (this.isBlob) {
            return postProcessResponse(this.client.blobs().breakLeaseWithRestResponseAsync(
                null, null, null, breakPeriodInSeconds, null, modifiedAccessConditions, context))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseTime()));
        } else {
            return postProcessResponse(this.client.containers().breakLeaseWithRestResponseAsync(
                null, null, breakPeriodInSeconds, null, modifiedAccessConditions, context))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseTime()));
        }
    }

    /**
     * ChangeLease changes the blob's lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.changeLease#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param proposedId A {@code String} in any valid GUID format.
     * @return A reactive response containing the new lease ID.
     */
    public Mono<String> changeLease(String proposedId) {
        return changeLeaseWithResponse(proposedId, null).flatMap(FluxUtil::toMono);
    }

    /**
     * ChangeLease changes the blob's lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseAsyncClient.changeLeaseWithResponse#String-ModifiedAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param proposedId A {@code String} in any valid GUID format.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the new lease ID.
     */
    public Mono<Response<String>> changeLeaseWithResponse(String proposedId,
        ModifiedAccessConditions modifiedAccessConditions) {
        return withContext(context -> changeLeaseWithResponse(proposedId, modifiedAccessConditions, context));
    }

    Mono<Response<String>> changeLeaseWithResponse(String proposedId, ModifiedAccessConditions modifiedAccessConditions,
        Context context) {
        if (this.isBlob) {
            return postProcessResponse(this.client.blobs().changeLeaseWithRestResponseAsync(
                null, null, this.leaseId, proposedId, null, null, modifiedAccessConditions, context))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        } else {
            return postProcessResponse(this.client.containers().changeLeaseWithRestResponseAsync(
                null, this.leaseId, proposedId, null, null, modifiedAccessConditions, context))
                .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getLeaseId()));
        }
    }

    private static <T> Mono<T> postProcessResponse(Mono<T> response) {
        return Utility.postProcessResponse(response, (errorResponse) ->
            errorResponse.onErrorResume(StorageErrorException.class, resume ->
                resume.getResponse()
                    .getBodyAsString()
                    .switchIfEmpty(Mono.just(""))
                    .flatMap(body -> Mono.error(new StorageException(resume, body)))
            ));
    }
}
