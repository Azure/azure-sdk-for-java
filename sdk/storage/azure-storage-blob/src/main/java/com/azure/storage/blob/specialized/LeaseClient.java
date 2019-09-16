// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.ContainerClient;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.common.Utility;

import java.net.URL;
import java.time.Duration;

/**
 *
 */
public final class LeaseClient {
    private final LeaseAsyncClient client;

    LeaseClient(LeaseAsyncClient client) {
        this.client = client;
    }

    /**
     * Gets the {@link URL} of the lease client. If the lease client is using a {@link BlobClient} it will be the URL of
     * the blob, otherwise the URL will be the {@link ContainerClient} URL.
     *
     * @return URL of the lease client.
     */
    public URL getLeaseClientUrl() {
        return client.getLeaseAsyncClientUrl();
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
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseClient.acquireLease#int}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param duration The  duration of the lease, in seconds, or negative one (-1) for a lease that never expires. A
     * non-infinite lease can be between 15 and 60 seconds.
     * @return The lease ID.
     */
    public String acquireLease(int duration) {
        return acquireLeaseWithResponse(duration, null, null, Context.NONE).getValue();
    }

    /**
     * Acquires a lease on the blob for write and delete operations. The lease duration must be between 15 to 60
     * seconds, or infinite (-1).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseClient.acquireLeaseWithResponse#int-ModifiedAccessConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param duration The  duration of the lease, in seconds, or negative one (-1) for a lease that never expires. A
     * non-infinite lease can be between 15 and 60 seconds.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The lease ID.
     */
    public Response<String> acquireLeaseWithResponse(int duration, ModifiedAccessConditions modifiedAccessConditions,
        Duration timeout, Context context) {
        return Utility.blockWithOptionalTimeout(this.client
            .acquireLeaseWithResponse(duration, modifiedAccessConditions, context), timeout);
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseClient.renewLease}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @return The renewed lease ID.
     */
    public String renewLease() {
        return renewLeaseWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Renews the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseClient.renewLeaseWithResponse#ModifiedAccessConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The renewed lease ID.
     */
    public Response<String> renewLeaseWithResponse(ModifiedAccessConditions modifiedAccessConditions, Duration timeout,
        Context context) {
        return Utility.blockWithOptionalTimeout(this.client
            .renewLeaseWithResponse(modifiedAccessConditions, context), timeout);
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseClient.releaseLease}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     */
    public void releaseLease() {
        releaseLeaseWithResponse(null, null, Context.NONE);
    }

    /**
     * Releases the blob's previously-acquired lease.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseClient.releaseLeaseWithResponse#ModifiedAccessConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public VoidResponse releaseLeaseWithResponse(ModifiedAccessConditions modifiedAccessConditions, Duration timeout,
        Context context) {
        return Utility.blockWithOptionalTimeout(this.client
            .releaseLeaseWithResponse(modifiedAccessConditions, context), timeout);
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseClient.breakLease}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @return The remaining time in the broken lease in seconds.
     */
    public Integer breakLease() {
        return breakLeaseWithResponse(null, null, null, Context.NONE).getValue();
    }

    /**
     * BreakLease breaks the blob's previously-acquired lease (if it exists). Pass the LeaseBreakDefault (-1) constant
     * to break a fixed-duration lease when it expires or an infinite lease immediately.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseClient.breakLeaseWithResponse#Integer-ModifiedAccessConditions-Duration-Context}
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
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The remaining time in the broken lease in seconds.
     */
    public Response<Integer> breakLeaseWithResponse(Integer breakPeriodInSeconds,
        ModifiedAccessConditions modifiedAccessConditions, Duration timeout, Context context) {
        return Utility.blockWithOptionalTimeout(this.client
            .breakLeaseWithResponse(breakPeriodInSeconds, modifiedAccessConditions, context), timeout);
    }

    /**
     * ChangeLease changes the blob's lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseClient.changeLease#String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param proposedId A {@code String} in any valid GUID format.
     * @return The new lease ID.
     */
    public String changeLease(String proposedId) {
        return changeLeaseWithResponse(proposedId, null, null, Context.NONE).getValue();
    }

    /**
     * ChangeLease changes the blob's lease ID.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.LeaseClient.changeLeaseWithResponse#String-ModifiedAccessConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a></p>
     *
     * @param proposedId A {@code String} in any valid GUID format.
     * @param modifiedAccessConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The new lease ID.
     */
    public Response<String> changeLeaseWithResponse(String proposedId,
        ModifiedAccessConditions modifiedAccessConditions, Duration timeout, Context context) {
        return Utility.blockWithOptionalTimeout(this.client
            .changeLeaseWithResponse(proposedId, modifiedAccessConditions, context), timeout);
    }
}
