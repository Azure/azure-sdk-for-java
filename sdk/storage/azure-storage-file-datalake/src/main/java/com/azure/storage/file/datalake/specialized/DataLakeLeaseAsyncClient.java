// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.specialized.BlobLeaseAsyncClient;
import com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import reactor.core.publisher.Mono;

import java.net.URL;


/**
 * This class provides a client that contains all the leasing operations for {@link DataLakeFileSystemAsyncClient
 * file systems}, {@link DataLakeFileAsyncClient files} and {@link DataLakeDirectoryAsyncClient directories}.
 * This client acts as a supplement to those clients and only handles leasing operations.
 *
 * <p><strong>Instantiating a DataLakeLeaseAsyncClient</strong></p>
 *
 * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithFile}
 *
 * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithDirectory}
 *
 * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.asyncInstantiationWithFileSystem}
 *
 * <p>View {@link DataLakeLeaseClientBuilder this} for additional ways to construct the client.</p>
 *
 * <p>For more information about leasing see the
 * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">file system leasing</a> or
 * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">path leasing</a> documentation.</p>
 *
 * @see DataLakeLeaseClientBuilder
 */
@ServiceClient(builder = DataLakeLeaseClientBuilder.class, isAsync = true)
public final class DataLakeLeaseAsyncClient {

    private final BlobLeaseAsyncClient blobLeaseAsyncClient;

    DataLakeLeaseAsyncClient(BlobLeaseAsyncClient blobLeaseAsyncClient) {
        this.blobLeaseAsyncClient = blobLeaseAsyncClient;
    }

    /**
     * Gets the {@link URL} of the lease client.
     *
     * <p>The lease will either be a file system or path URL depending on which the lease client is associated.</p>
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
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.acquireLease#int}
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
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.acquireLeaseWithResponse#int-RequestConditions}
     *
     * @param duration The duration of the lease between 15 to 60 seconds or -1 for an infinite duration.
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> acquireLeaseWithResponse(int duration,
        RequestConditions modifiedRequestConditions) {
        return this.blobLeaseAsyncClient.acquireLeaseWithResponse(duration, modifiedRequestConditions)
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Renews the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.renewLease}
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
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.renewLeaseWithResponse#RequestConditions}
     *
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response containing the renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> renewLeaseWithResponse(RequestConditions modifiedRequestConditions) {
        return blobLeaseAsyncClient.renewLeaseWithResponse(modifiedRequestConditions)
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.releaseLease}
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
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.releaseLeaseWithResponse#RequestConditions}
     *
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the resource was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> releaseLeaseWithResponse(RequestConditions modifiedRequestConditions) {
        return blobLeaseAsyncClient.releaseLeaseWithResponse(modifiedRequestConditions)
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.breakLease}
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
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.breakLeaseWithResponse#Integer-RequestConditions}
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
        return blobLeaseAsyncClient.breakLeaseWithResponse(breakPeriodInSeconds, modifiedRequestConditions)
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.changeLease#String}
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
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseAsyncClient.changeLeaseWithResponse#String-RequestConditions}
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
        return blobLeaseAsyncClient.changeLeaseWithResponse(proposedId, modifiedRequestConditions)
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
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
