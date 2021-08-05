// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.options.ShareAcquireLeaseOptions;
import com.azure.storage.file.share.options.ShareBreakLeaseOptions;

import java.net.URL;
import java.time.Duration;

/**
 * This class provides a client that contains all the leasing operations for {@link ShareFileClient files}.
 * This client acts as a supplement to that client and only handles leasing operations.
 *
 * <p><strong>Instantiating a ShareLeaseClient</strong></p>
 *
 * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClientBuilder.syncInstantiation}
 *
 * <p>View {@link ShareLeaseClientBuilder this} for additional ways to construct the client.</p>
 *
 * <p>For more information about leasing see the
 * <a href="https://docs.microsoft.com/rest/api/storageservices/lease-file">Azure Docs</a>.
 *
 * @see ShareLeaseClientBuilder
 */
@ServiceClient(builder =  ShareLeaseClientBuilder.class)
public final class ShareLeaseClient {
    private final ShareLeaseAsyncClient client;

    ShareLeaseClient(ShareLeaseAsyncClient client) {
        this.client = client;
    }

    /**
     * @return URL of the lease client.
     * @deprecated Please use {@link #getResourceUrl()}
     */
    @Deprecated
    public String getFileUrl() {
        return client.getFileUrl();
    }

    /**
     * Gets the {@link URL} of the lease client.
     *
     * <p>The lease will either be a share or share file URL depending on which the lease client is associated.</p>
     *
     * @return URL of the lease client.
     */
    public String getResourceUrl() {
        return this.client.getResourceUrl();
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
     * Acquires an infinite lease for write and delete operations.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClient.acquireLease}
     *
     * @return The lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String acquireLease() {
        return acquireLeaseWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Acquires an infinite lease for write and delete operations.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClient.acquireLeaseWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> acquireLeaseWithResponse(Duration timeout, Context context) {
        return acquireLeaseWithResponse(new ShareAcquireLeaseOptions(), timeout, context);
    }

    /**
     * Acquires a lease for write and delete operations. Note: Share files only support infinite lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClient.acquireLeaseWithResponse#ShareAcquireLeaseOptions-Duration-Context}
     *
     * @param options {@link ShareAcquireLeaseOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> acquireLeaseWithResponse(ShareAcquireLeaseOptions options, Duration timeout,
        Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(this.client.acquireLeaseWithResponse(options, context),
            timeout);
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClient.releaseLease}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void releaseLease() {
        releaseLeaseWithResponse(null, Context.NONE);
    }

    /**
     * Releases the previously acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClient.releaseLeaseWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> releaseLeaseWithResponse(Duration timeout, Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(this.client.releaseLeaseWithResponse(context), timeout);
    }

    /**
     * Breaks the previously acquired lease, if it exists. Leases will break immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClient.breakLease}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void breakLease() {
        breakLeaseWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Breaks the previously acquired lease, if it exists. Leases will break immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClient.breakLeaseWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> breakLeaseWithResponse(Duration timeout, Context context) {
        return breakLeaseWithResponse(new ShareBreakLeaseOptions(), timeout, context);
    }

    /**
     * Breaks the previously acquired lease, if it exists. Leases will break immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClient.breakLeaseWithResponse#ShareBreakLeaseOptions-Duration-Context}
     *
     * @param options {@link ShareBreakLeaseOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> breakLeaseWithResponse(ShareBreakLeaseOptions options, Duration timeout, Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(this.client.breakLeaseWithResponse(options, context), timeout);
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClient.changeLease#String}
     *
     * @param proposedId A new lease ID in a valid GUID format.
     * @return The new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String changeLease(String proposedId) {
        return changeLeaseWithResponse(proposedId, null, Context.NONE).getValue();
    }

    /**
     * Changes the lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClient.changeLeaseWithResponse#String-Duration-Context}
     *
     * @param proposedId A new lease ID in a valid GUID format.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The new lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> changeLeaseWithResponse(String proposedId, Duration timeout, Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(this.client
            .changeLeaseWithResponse(proposedId, context), timeout);
    }

    /**
     * Renews the previously acquired lease on a share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClient.renewLease}
     *
     * @return A response containing the renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String renewLease() {
        return renewLeaseWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Renews the previously acquired lease on a share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.share.specialized.ShareLeaseClient.renewLeaseWithResponse#Duration-Context}
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the renewed lease ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> renewLeaseWithResponse(Duration timeout, Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(this.client.renewLeaseWithResponse(context), timeout);
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
