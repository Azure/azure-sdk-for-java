// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.specialized.BlobLeaseClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.DataLakePathClient;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;

import java.net.URL;
import java.time.Duration;

/**
 * This class provides a client that contains all the leasing operations for {@link DataLakeFileSystemClient
 * file systems} and {@link DataLakePathClient files and directories}. This client acts as a supplement to those
 * clients and only handles leasing operations.
 *
 * <p><strong>Instantiating a DataLakeLeaseClient</strong></p>
 *
 * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithFile}
 *
 * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithDirectory}
 *
 * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder.syncInstantiationWithFileSystem}
 *
 * <p>View {@link DataLakeLeaseClientBuilder this} for additional ways to construct the client.</p>
 *
 * <p>For more information about leasing see the
 * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-container">file system leasing</a> or
 * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-blob">path leasing</a> documentation.</p>
 *
 * @see DataLakeLeaseClientBuilder
 */
@ServiceClient(builder =  DataLakeLeaseClientBuilder.class)
public final class DataLakeLeaseClient {

    private final ClientLogger logger = new ClientLogger(DataLakeLeaseClient.class);
    private final BlobLeaseClient blobLeaseClient;

    DataLakeLeaseClient(BlobLeaseClient blobLeaseClient) {
        this.blobLeaseClient = blobLeaseClient;
    }

    /**
     * Gets the {@link URL} of the lease client.
     *
     * <p>The lease will either be a file system or path URL depending on which the lease client is associated.</p>
     *
     * @return URL of the lease client.
     */
    public String getResourceUrl() {
        return blobLeaseClient.getResourceUrl();
    }

    /**
     * Get the lease ID for this lease.
     *
     * @return the lease ID.
     */
    public String getLeaseId() {
        return blobLeaseClient.getLeaseId();
    }

    /**
     * Acquires a lease for write and delete operations. The lease duration must be between 15 to 60 seconds or
     * -1 for an infinite duration.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClient.acquireLease#int}
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
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClient.acquireLeaseWithResponse#int-RequestConditions-Duration-Context}
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
        return DataLakeImplUtils.returnOrConvertException(() ->
            blobLeaseClient.acquireLeaseWithResponse(duration, modifiedRequestConditions, timeout, context), logger);
    }

    /**
     * Renews the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClient.renewLease}
     *
     * @return The renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String renewLease() {
        return renewLeaseWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Renews the previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClient.renewLeaseWithResponse#RequestConditions-Duration-Context}
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
        return DataLakeImplUtils.returnOrConvertException(() ->
            blobLeaseClient.renewLeaseWithResponse(modifiedRequestConditions, timeout, context), logger);
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClient.releaseLease}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void releaseLease() {
        releaseLeaseWithResponse(null, null, Context.NONE);
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClient.releaseLeaseWithResponse#RequestConditions-Duration-Context}
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
        return DataLakeImplUtils.returnOrConvertException(() ->
            blobLeaseClient.releaseLeaseWithResponse(modifiedRequestConditions, timeout, context), logger);
    }

    /**
     * Breaks the previously acquired lease, if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClient.breakLease}
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
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClient.breakLeaseWithResponse#Integer-RequestConditions-Duration-Context}
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
        return DataLakeImplUtils.returnOrConvertException(() ->
            blobLeaseClient.breakLeaseWithResponse(breakPeriodInSeconds, modifiedRequestConditions, timeout,
                context), logger);
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClient.changeLease#String}
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
     * {@codesnippet com.azure.storage.file.datalake.specialized.DataLakeLeaseClient.changeLeaseWithResponse#String-RequestConditions-Duration-Context}
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
        return DataLakeImplUtils.returnOrConvertException(() ->
            blobLeaseClient.changeLeaseWithResponse(proposedId, modifiedRequestConditions, timeout, context), logger);
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return blobLeaseClient.getAccountName();
    }

}
