/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.IPRange;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.SharedAccessPolicy;
import com.microsoft.azure.storage.SharedAccessProtocols;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageLocation;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.core.ExecutionEngine;
import com.microsoft.azure.storage.core.Logger;
import com.microsoft.azure.storage.core.NetworkInputStream;
import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.core.RequestLocationMode;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.SharedAccessSignatureHelper;
import com.microsoft.azure.storage.core.StorageCredentialsHelper;
import com.microsoft.azure.storage.core.StorageRequest;
import com.microsoft.azure.storage.core.StreamMd5AndLength;
import com.microsoft.azure.storage.core.UriQueryBuilder;
import com.microsoft.azure.storage.core.Utility;
import com.microsoft.azure.storage.core.WrappedByteArrayOutputStream;

/**
 * Represents a Microsoft Azure blob. This is the base class for the {@link CloudBlockBlob} and {@link CloudPageBlob}
 * classes.
 */
public abstract class CloudBlob implements ListBlobItem {
    /**
     * Holds the metadata for the blob.
     */
    HashMap<String, String> metadata = new HashMap<String, String>();

    /**
     * Holds the properties of the blob.
     */
    BlobProperties properties;

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * Holds the snapshot ID.
     */
    String snapshotID;

    /**
     * Holds the blob's container reference.
     */
    private CloudBlobContainer container;

    /**
     * Represents the blob's directory.
     */
    protected CloudBlobDirectory parent;

    /**
     * Holds the blob's name.
     */
    private String name;

    /**
     * Holds the number of bytes to buffer when writing to a {@link BlobOutputStream} (block and page blobs).
     */
    protected int streamWriteSizeInBytes = Constants.DEFAULT_STREAM_WRITE_IN_BYTES;

    /**
     * Holds the minimum read size when using a {@link BlobInputStream}.
     */
    protected int streamMinimumReadSizeInBytes = Constants.DEFAULT_MINIMUM_READ_SIZE_IN_BYTES;

    /**
     * Represents the blob client.
     */
    protected CloudBlobClient blobServiceClient;
    
    /**
     * Creates an instance of the <code>CloudBlob</code> class using the specified type, name, snapshot ID, and
     * container.
     *
     * @param type
     *            A {@link BlobType} value which represents the type of the blob.
     * @param blobName
     *            Name of the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot version, if applicable.
     * @param container
     *            The reference to the parent container.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    protected CloudBlob(final BlobType type, String blobName, String snapshotID, CloudBlobContainer container)
            throws URISyntaxException {
        Utility.assertNotNullOrEmpty("blobName", blobName);
        Utility.assertNotNull("container", container);

        this.storageUri = PathUtility.appendPathToUri(container.getStorageUri(), blobName);
        this.name = blobName;
        this.blobServiceClient = container.getServiceClient();
        this.container = container;
        this.snapshotID = snapshotID;
        this.properties = new BlobProperties(type);
    }
    
    /**
     * Creates an instance of the <code>CloudBlob</code> class using the specified URI, snapshot ID, and cloud blob
     * client.
     *
     * @param type
     *            A {@link BlobType} value which represents the type of the blob.
     * @param uri
     *            A {@link StorageUri} object that represents the URI to the blob, beginning with the container name.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot version, if applicable.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    protected CloudBlob(final BlobType type, final StorageUri uri, final String snapshotID,
            final StorageCredentials credentials) throws StorageException {
        this.properties = new BlobProperties(type);
        this.parseQueryAndVerify(uri, credentials);

        if (snapshotID != null) {
            if (this.snapshotID != null) {
                throw new IllegalArgumentException(SR.SNAPSHOT_QUERY_OPTION_ALREADY_DEFINED);
            }
            else {
                this.snapshotID = snapshotID;
            }
        }
    }

    /**
     * Creates an instance of the <code>CloudBlob</code> class by copying values from another blob.
     *
     * @param otherBlob
     *            A <code>CloudBlob</code> object that represents the blob to copy.
     */
    protected CloudBlob(final CloudBlob otherBlob) {
        this.properties = new BlobProperties(otherBlob.properties);

        if (otherBlob.metadata != null) {
            this.metadata = new HashMap<String, String>();
            for (final String key : otherBlob.metadata.keySet()) {
                this.metadata.put(key, otherBlob.metadata.get(key));
            }
        }

        this.snapshotID = otherBlob.snapshotID;
        this.storageUri = otherBlob.storageUri;
        this.container = otherBlob.container;
        this.parent = otherBlob.parent;
        this.blobServiceClient = otherBlob.blobServiceClient;
        this.name = otherBlob.name;
        this.setStreamMinimumReadSizeInBytes(otherBlob.getStreamMinimumReadSizeInBytes());
        this.setStreamWriteSizeInBytes(otherBlob.getStreamWriteSizeInBytes());
    }

    /**
     * Aborts an ongoing blob copy operation.
     *
     * @param copyId
     *            A <code>String</code> object that identifying the copy operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void abortCopy(final String copyId) throws StorageException {
        this.abortCopy(copyId, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Aborts an ongoing blob copy operation.
     *
     * @param copyId
     *            A <code>String</code> object that identifying the copy operation.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void abortCopy(final String copyId, final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.abortCopyImpl(copyId, accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Void> abortCopyImpl(final String copyId,
            final AccessCondition accessCondition, final BlobRequestOptions options) {
        Utility.assertNotNull("copyId", copyId);

        final StorageRequest<CloudBlobClient, CloudBlob, Void> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.abortCopy(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, copyId);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0, context);
            }

            @Override
            public Void preProcessResponse(CloudBlob parentObject, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                return null;
            }
        };

        return putRequest;
    }

    /**
     * Acquires a new infinite lease on the blob.
     *
     * @return A <code>String</code> that represents the lease ID.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final String acquireLease() throws StorageException {
        return this.acquireLease(null /* leaseTimeInSeconds */, null /* proposedLeaseId */);
    }

    /**
     * Acquires a new lease on the blob with the specified lease time and proposed lease ID.
     *
     * @param leaseTimeInSeconds
     *            An <code>Integer</code> which specifies the span of time for which to acquire the lease, in seconds.
     *            If null, an infinite lease will be acquired. If not null, the value must be greater than
     *            zero.
     *
     * @param proposedLeaseId
     *            A <code>String</code> that represents the proposed lease ID for the new lease,
     *            or null if no lease ID is proposed.
     *
     * @return A <code>String</code> that represents the lease ID.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final String acquireLease(final Integer leaseTimeInSeconds, final String proposedLeaseId)
            throws StorageException {
        return this.acquireLease(leaseTimeInSeconds, proposedLeaseId, null /* accessCondition */, null /* options */,
                null /* opContext */);
    }

    /**
     * Acquires a new lease on the blob with the specified lease time, proposed lease ID, request
     * options, and operation context.
     *
     * @param leaseTimeInSeconds
     *            An <code>Integer</code> which specifies the span of time for which to acquire the lease, in seconds.
     *            If null, an infinite lease will be acquired. If not null, the value must be greater than
     *            zero.
     *
     * @param proposedLeaseId
     *            A <code>String</code> that represents the proposed lease ID for the new lease,
     *            or null if no lease ID is proposed.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     *
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client
     *            ({@link CloudBlobClient}).
     *
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. The context
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return A <code>String</code> that represents the lease ID.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final String acquireLease(final Integer leaseTimeInSeconds, final String proposedLeaseId,
            final AccessCondition accessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.acquireLeaseImpl(leaseTimeInSeconds, proposedLeaseId, accessCondition, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, String> acquireLeaseImpl(final Integer leaseTimeInSeconds,
            final String proposedLeaseId, final AccessCondition accessCondition, final BlobRequestOptions options) {
        final StorageRequest<CloudBlobClient, CloudBlob, String> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, String>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.leaseBlob(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, LeaseAction.ACQUIRE, leaseTimeInSeconds, proposedLeaseId,
                        null);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public String preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                updateEtagAndLastModifiedFromResponse(this.getConnection());
                blob.properties.setLeaseStatus(LeaseStatus.LOCKED);

                return BlobResponse.getLeaseID(this.getConnection());
            }
        };

        return putRequest;
    }

    /**
     * Asserts that the blob has the correct blob type specified in the blob attributes.
     *
     * @throws StorageException
     *             If an incorrect blob type is used.
     */
    protected final void assertCorrectBlobType() throws StorageException {
        if (this instanceof CloudBlockBlob && this.properties.getBlobType() != BlobType.BLOCK_BLOB) {
            throw new StorageException(StorageErrorCodeStrings.INCORRECT_BLOB_TYPE, String.format(SR.INVALID_BLOB_TYPE,
                    BlobType.BLOCK_BLOB, this.properties.getBlobType()), Constants.HeaderConstants.HTTP_UNUSED_306,
                    null, null);
        } 
        else if (this instanceof CloudPageBlob && this.properties.getBlobType() != BlobType.PAGE_BLOB) {
            throw new StorageException(StorageErrorCodeStrings.INCORRECT_BLOB_TYPE, String.format(SR.INVALID_BLOB_TYPE,
                    BlobType.PAGE_BLOB, this.properties.getBlobType()), Constants.HeaderConstants.HTTP_UNUSED_306,
                    null, null);
        } 
        else if (this instanceof CloudAppendBlob && this.properties.getBlobType() != BlobType.APPEND_BLOB) {
            throw new StorageException(StorageErrorCodeStrings.INCORRECT_BLOB_TYPE, String.format(SR.INVALID_BLOB_TYPE,
                    BlobType.APPEND_BLOB, this.properties.getBlobType()), Constants.HeaderConstants.HTTP_UNUSED_306,
                    null, null);
        }
    }

    /**
     * Asserts that write operation is not done for snapshot.
     */
    protected void assertNoWriteOperationForSnapshot() {
        if (isSnapshot()) {
            throw new IllegalArgumentException(SR.INVALID_OPERATION_FOR_A_SNAPSHOT);
        }
    }

    /**
     * Breaks the lease and ensures that another client cannot acquire a new lease until the current lease period
     * has expired.
     *
     * @param breakPeriodInSeconds
     *            Specifies the time to wait, in seconds, until the current lease is broken.
     *            If null, the break period is the remainder of the current lease, or zero for infinite leases.
     *
     * @return An <code>long</code> which specifies the time, in seconds, remaining in the lease period.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final long breakLease(final Integer breakPeriodInSeconds) throws StorageException {
        return this
                .breakLease(breakPeriodInSeconds, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Breaks the existing lease, using the specified request options and operation context, and ensures that another
     * client cannot acquire a new lease until the current lease period has expired.
     *
     * @param breakPeriodInSeconds
     *            An <code>Integer</code> which specifies the time to wait, in seconds, until the current lease is
     *            broken.
     *            If null, the break period is the remainder of the current lease, or zero for infinite leases.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client
     *            ({@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. The context
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return An <code>long</code> which represents the time, in seconds, remaining in the lease period.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final long breakLease(final Integer breakPeriodInSeconds, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (breakPeriodInSeconds != null) {
            Utility.assertGreaterThanOrEqual("breakPeriodInSeconds", breakPeriodInSeconds, 0);
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.breakLeaseImpl(breakPeriodInSeconds, accessCondition, options), options.getRetryPolicyFactory(),
                opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Long> breakLeaseImpl(final Integer breakPeriodInSeconds,
            final AccessCondition accessCondition, final BlobRequestOptions options) {

        final StorageRequest<CloudBlobClient, CloudBlob, Long> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, Long>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.leaseBlob(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, LeaseAction.BREAK, null, null, breakPeriodInSeconds);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Long preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return -1L;
                }

                updateEtagAndLastModifiedFromResponse(this.getConnection());

                final String leaseTime = BlobResponse.getLeaseTime(this.getConnection());

                blob.properties.setLeaseStatus(LeaseStatus.UNLOCKED);
                return Utility.isNullOrEmpty(leaseTime) ? -1L : Long.parseLong(leaseTime);
            }
        };

        return putRequest;
    }

    /**
     * Changes the existing lease ID to the proposed lease ID.
     *
     * @param proposedLeaseId
     *            A <code>String</code> that represents the proposed lease ID for the new lease,
     *            or null if no lease ID is proposed.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob. The lease ID is
     *            required to be set with an access condition.
     *
     * @return A <code>String</code> that represents the new lease ID.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final String changeLease(final String proposedLeaseId, final AccessCondition accessCondition)
            throws StorageException {
        return this.changeLease(proposedLeaseId, accessCondition, null /* options */, null /* opContext */);
    }

    /**
     * Changes the existing lease ID to the proposed lease Id with the specified access conditions, request options,
     * and operation context.
     *
     * @param proposedLeaseId
     *            A <code>String</code> that represents the proposed lease ID for the new lease,
     *            or null if no lease ID is proposed.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob. The lease ID is
     *            required to be set with an access condition.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client
     *            ({@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. The context
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return A <code>String</code> that represents the new lease ID.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final String changeLease(final String proposedLeaseId, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        Utility.assertNotNull("accessCondition", accessCondition);
        Utility.assertNotNullOrEmpty("leaseID", accessCondition.getLeaseID());

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.changeLeaseImpl(proposedLeaseId, accessCondition, options), options.getRetryPolicyFactory(),
                opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, String> changeLeaseImpl(final String proposedLeaseId,
            final AccessCondition accessCondition, final BlobRequestOptions options) {
        final StorageRequest<CloudBlobClient, CloudBlob, String> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, String>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.leaseBlob(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, LeaseAction.CHANGE, null, proposedLeaseId, null);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public String preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                updateEtagAndLastModifiedFromResponse(this.getConnection());

                return BlobResponse.getLeaseID(this.getConnection());
            }
        };

        return putRequest;
    }

    /**
     * Requests the service to start copying a URI's contents, properties, and metadata to a new blob.
     *
     * @param source
     *            A <code>java.net.URI</code> The source URI. URIs for resources outside of Azure
     *            may only be copied into block blobs.
     *
     * @return A <code>String</code> which represents the copy ID associated with the copy operation.
     *
     * @throws StorageException
     *            If a storage service error occurred.
     */
    @DoesServiceRequest
    public final String startCopy(final URI source) throws StorageException {
        return this.startCopy(source, null /* sourceAccessCondition */, null /* destinationAccessCondition */,
                null /* options */, null /* opContext */);
    }

    /**
     * Requests the service to start copying a URI's contents, properties, and metadata to a new blob, using the
     * specified access conditions, lease ID, request options, and operation context.
     *
     * @param source
     *            A <code>java.net.URI</code> The source URI.  URIs for resources outside of Azure
     *            may only be copied into block blobs.
     * @param sourceAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the source.
     * @param destinationAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the destination.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request.
     *            Specifying <code>null</code> will use the default request options from the associated
     *            service client ({@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation.
     *            This object is used to track requests to the storage service, and to provide additional
     *            runtime information about the operation.
     *
     * @return A <code>String</code> which represents the copy ID associated with the copy operation.
     *
     * @throws StorageException
     *            If a storage service error occurred.
     *
     */
    @DoesServiceRequest
    public final String startCopy(final URI source, final AccessCondition sourceAccessCondition,
            final AccessCondition destinationAccessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.startCopyImpl(source, sourceAccessCondition, destinationAccessCondition, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, String> startCopyImpl(
            final URI source, final AccessCondition sourceAccessCondition,
            final AccessCondition destinationAccessCondition, final BlobRequestOptions options) {

        final StorageRequest<CloudBlobClient, CloudBlob, String> putRequest =
                new StorageRequest<CloudBlobClient, CloudBlob, String>(options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                // toASCIIString() must be used in order to appropriately encode the URI
                return BlobRequest.copyFrom(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, sourceAccessCondition, destinationAccessCondition, source.toASCIIString(),
                        blob.snapshotID);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudBlob blob, OperationContext context) {
                BlobRequest.addMetadata(connection, blob.metadata, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0, context);
            }

            @Override
            public String preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(this.getConnection());
                blob.properties.setCopyState(BlobResponse.getCopyState(this.getConnection()));

                return blob.properties.getCopyState().getCopyId();
            }
        };

        return putRequest;
    }

    /**
     * Creates a snapshot of the blob.
     *
     * @return A <code>CloudBlob</code> object that represents the snapshot of the blob.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final CloudBlob createSnapshot() throws StorageException {
        return this
                .createSnapshot(null /* metadata */, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Creates a snapshot of the blob using the specified request options and operation context.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return A <code>CloudBlob</code> object that represents the snapshot of the blob.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final CloudBlob createSnapshot(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        return this.createSnapshot(null /* metadata */, accessCondition, options, opContext);
    }

    /**
     * Creates a snapshot of the blob using the specified request options and operation context.
     *
     * @param metadata
     *            A collection of name-value pairs defining the metadata of the snapshot, or null.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return A <code>CloudBlob</code> object that represents the snapshot of the blob.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final CloudBlob createSnapshot(final HashMap<String, String> metadata,
            final AccessCondition accessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        assertNoWriteOperationForSnapshot();

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        return ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this,
                        this.createSnapshotImpl(metadata, accessCondition, options), options.getRetryPolicyFactory(),
                        opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, CloudBlob> createSnapshotImpl(
            final HashMap<String, String> metadata, final AccessCondition accessCondition,
            final BlobRequestOptions options) {
        final StorageRequest<CloudBlobClient, CloudBlob, CloudBlob> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, CloudBlob>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.snapshot(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudBlob blob, OperationContext context) {
                if (metadata != null) {
                    BlobRequest.addMetadata(connection, metadata, context);
                }
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public CloudBlob preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }
                CloudBlob snapshot = null;
                final String snapshotTime = BlobResponse.getSnapshotTime(this.getConnection());
                if (blob instanceof CloudBlockBlob) {
                    snapshot = new CloudBlockBlob(blob.getName(), snapshotTime, CloudBlob.this.getContainer());
                }
                else if (blob instanceof CloudPageBlob) {
                    snapshot = new CloudPageBlob(blob.getName(), snapshotTime, CloudBlob.this.getContainer());
                }
                else if (blob instanceof CloudAppendBlob) {
                    snapshot = new CloudAppendBlob(blob.getName(), snapshotTime, CloudBlob.this.getContainer());
                }
                snapshot.setProperties(blob.properties);

                // use the specified metadata if not null : otherwise blob's metadata
                snapshot.setMetadata(metadata != null ? metadata : blob.metadata);

                snapshot.updateEtagAndLastModifiedFromResponse(this.getConnection());

                return snapshot;
            }
        };

        return putRequest;
    }

    /**
     * Deletes the blob.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void delete() throws StorageException {
        this.delete(DeleteSnapshotsOption.NONE, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Deletes the blob using the specified snapshot and request options, and operation context.
     * <p>
     * A blob that has snapshots cannot be deleted unless the snapshots are also deleted. If a blob has snapshots, use
     * the {@link DeleteSnapshotsOption#DELETE_SNAPSHOTS_ONLY} or {@link DeleteSnapshotsOption#INCLUDE_SNAPSHOTS} value
     * in the <code>deleteSnapshotsOption</code> parameter to specify how the snapshots should be handled when the blob
     * is deleted.
     *
     * @param deleteSnapshotsOption
     *            A {@link DeleteSnapshotsOption} object that indicates whether to delete only snapshots, or the blob
     *            and its snapshots.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void delete(final DeleteSnapshotsOption deleteSnapshotsOption, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        Utility.assertNotNull("deleteSnapshotsOption", deleteSnapshotsOption);

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.deleteImpl(deleteSnapshotsOption, accessCondition, options), options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Deletes the blob if it exists.
     * <p>
     * A blob that has snapshots cannot be deleted unless the snapshots are also deleted. If a blob has snapshots, use
     * the {@link DeleteSnapshotsOption#DELETE_SNAPSHOTS_ONLY} or {@link DeleteSnapshotsOption#INCLUDE_SNAPSHOTS} value
     * in the <code>deleteSnapshotsOption</code> parameter to specify how the snapshots should be handled when the blob
     * is deleted.
     *
     * @return <code>true</code> if the blob was deleted; otherwise, <code>false</code>.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     *
     */
    @DoesServiceRequest
    public final boolean deleteIfExists() throws StorageException {
        return this
                .deleteIfExists(DeleteSnapshotsOption.NONE, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Deletes the blob if it exists, using the specified snapshot and request options, and operation context.
     * <p>
     * A blob that has snapshots cannot be deleted unless the snapshots are also deleted. If a blob has snapshots, use
     * the {@link DeleteSnapshotsOption#DELETE_SNAPSHOTS_ONLY} or {@link DeleteSnapshotsOption#INCLUDE_SNAPSHOTS} value
     * in the <code>deleteSnapshotsOption</code> parameter to specify how the snapshots should be handled when the blob
     * is deleted.
     *
     * @param deleteSnapshotsOption
     *            A {@link DeleteSnapshotsOption} object that indicates whether to delete only snapshots, or the blob
     *            and its snapshots.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return <code>true</code> if the blob existed and was deleted; otherwise, <code>false</code>
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final boolean deleteIfExists(final DeleteSnapshotsOption deleteSnapshotsOption,
            final AccessCondition accessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        boolean exists = this.exists(true, accessCondition, options, opContext);
        if (exists) {
            try {
                this.delete(deleteSnapshotsOption, accessCondition, options, opContext);
                return true;
            }
            catch (StorageException e) {
                if (e.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND
                        && StorageErrorCodeStrings.BLOB_NOT_FOUND.equals(e.getErrorCode())) {
                    return false;
                }
                else {
                    throw e;
                }
            }

        }
        else {
            return false;
        }
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Void> deleteImpl(
            final DeleteSnapshotsOption deleteSnapshotsOption, final AccessCondition accessCondition,
            final BlobRequestOptions options) {
        final StorageRequest<CloudBlobClient, CloudBlob, Void> deleteRequest = new StorageRequest<CloudBlobClient, CloudBlob, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.deleteBlob(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, blob.snapshotID, deleteSnapshotsOption);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Void preProcessResponse(CloudBlob parentObject, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                return null;
            }
        };

        return deleteRequest;
    }

    /**
     * Downloads the contents of a blob to a stream.
     *
     * @param outStream
     *            An <code>{@link OutputStream}</code> object that represents the target stream.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void download(final OutputStream outStream) throws StorageException {
        this.download(outStream, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Downloads the contents of a blob to a stream using the specified request options and operation context.
     *
     * @param outStream
     *            An <code>OutputStream</code> object that represents the target stream.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void download(final OutputStream outStream, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        ExecutionEngine.executeWithRetry(this.blobServiceClient, this, this.downloadToStreamImpl(
                null /* blobOffset */, null /* length */, outStream, accessCondition, options, opContext), options
                .getRetryPolicyFactory(), opContext);
    }

    /**
     * Downloads the contents of a blob to a stream.
     *
     * @param offset
     *            A <code>long</code> which represents the offset to use as the starting point for the source.
     * @param length
     *            A <code>Long</code> which represents the number of bytes to read or <code>null</code>.
     * @param outStream
     *            An <code>{@link OutputStream}</code> object that represents the target stream.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void downloadRange(final long offset, final Long length, final OutputStream outStream)
            throws StorageException {
        this.downloadRange(offset, length, outStream, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Downloads the contents of a blob to a stream using the specified request options and operation context.
     *
     * @param offset
     *            A <code>long</code> which represents the offset to use as the starting point for the source.
     * @param length
     *            A <code>Long</code> which represents the number of bytes to read or <code>null</code>.
     * @param outStream
     *            An <code>{@link OutputStream}</code> object that represents the target stream.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void downloadRange(final long offset, final Long length, final OutputStream outStream,
            final AccessCondition accessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (offset < 0 || (length != null && length <= 0)) {
            throw new IndexOutOfBoundsException();
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        if (options.getUseTransactionalContentMD5() && (length != null && length > 4 * Constants.MB)) {
            throw new IllegalArgumentException(SR.INVALID_RANGE_CONTENT_MD5_HEADER);
        }

        ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.downloadToStreamImpl(offset, length, outStream, accessCondition, options, opContext),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Populates a blob's properties and metadata.
     * <p>
     * This method populates the blob's system properties and user-defined metadata. Before reading or modifying a
     * blob's properties or metadata, call this method or its overload to retrieve the latest values for the blob's
     * properties and metadata from the Microsoft Azure storage service.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void downloadAttributes() throws StorageException {
        this.downloadAttributes(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Populates a blob's properties and metadata using the specified request options and operation context.
     * <p>
     * This method populates the blob's system properties and user-defined metadata. Before reading or modifying a
     * blob's properties or metadata, call this method or its overload to retrieve the latest values for the blob's
     * properties and metadata from the Microsoft Azure storage service.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void downloadAttributes(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.downloadAttributesImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Void> downloadAttributesImpl(
            final AccessCondition accessCondition, final BlobRequestOptions options) {
        final StorageRequest<CloudBlobClient, CloudBlob, Void> getRequest = new StorageRequest<CloudBlobClient, CloudBlob, Void>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.getBlobProperties(
                        blob.getTransformedAddress(context).getUri(this.getCurrentLocation()), options, context,
                        accessCondition, blob.snapshotID);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Void preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                // Set attributes
                final BlobAttributes retrievedAttributes = BlobResponse.getBlobAttributes(this.getConnection(),
                        blob.getStorageUri(), blob.snapshotID);

                if (retrievedAttributes.getProperties().getBlobType() != blob.properties.getBlobType()) {
                    throw new StorageException(StorageErrorCodeStrings.INCORRECT_BLOB_TYPE, String.format(
                            SR.INVALID_BLOB_TYPE, blob.properties.getBlobType(), retrievedAttributes.getProperties()
                                    .getBlobType()), Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }

                blob.properties = retrievedAttributes.getProperties();
                blob.metadata = retrievedAttributes.getMetadata();

                return null;
            }
        };

        return getRequest;
    }

    @DoesServiceRequest
    private final StorageRequest<CloudBlobClient, CloudBlob, Integer> downloadToStreamImpl(final Long blobOffset,
            final Long length, final OutputStream outStream, final AccessCondition accessCondition,
            final BlobRequestOptions options, OperationContext opContext) {

        final long startingOffset = blobOffset == null ? 0 : blobOffset;
        final boolean isRangeGet = blobOffset != null;
        final StorageRequest<CloudBlobClient, CloudBlob, Integer> getRequest = new StorageRequest<CloudBlobClient, CloudBlob, Integer>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {

                // The first time this is called, we have to set the length and blob offset. On retries, these will already have values and need not be called.
                if (this.getOffset() == null) {
                    this.setOffset(blobOffset);
                }

                if (this.getLength() == null) {
                    this.setLength(length);
                }

                AccessCondition tempCondition = (this.getETagLockCondition() != null) ? this.getETagLockCondition()
                        : accessCondition;

                return BlobRequest.getBlob(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, tempCondition, blob.snapshotID, this.getOffset(), this.getLength(),
                        (options.getUseTransactionalContentMD5() && !this.getArePropertiesPopulated()));
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Integer preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_PARTIAL
                        && this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                if (!this.getArePropertiesPopulated()) {
                    String originalContentMD5 = null;

                    final BlobAttributes retrievedAttributes = BlobResponse.getBlobAttributes(this.getConnection(),
                            blob.getStorageUri(), blob.snapshotID);

                    // Do not update Content-MD5 if it is a range get.
                    if (isRangeGet) {
                        originalContentMD5 = blob.properties.getContentMD5();
                    }
                    else {
                        originalContentMD5 = retrievedAttributes.getProperties().getContentMD5();
                    }

                    if (!options.getDisableContentMD5Validation() && options.getUseTransactionalContentMD5()
                            && Utility.isNullOrEmpty(retrievedAttributes.getProperties().getContentMD5())) {
                        throw new StorageException(StorageErrorCodeStrings.MISSING_MD5_HEADER, SR.MISSING_MD5,
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }

                    blob.properties = retrievedAttributes.getProperties();
                    blob.metadata = retrievedAttributes.getMetadata();
                    this.setContentMD5(retrievedAttributes.getProperties().getContentMD5());
                    blob.properties.setContentMD5(originalContentMD5);
                    this.setLockedETag(blob.properties.getEtag());
                    this.setArePropertiesPopulated(true);
                }

                // If the download fails and Get Blob needs to resume the download, going to the
                // same storage location is important to prevent a possible ETag mismatch.
                this.setRequestLocationMode(this.getResult().getTargetLocation() == StorageLocation.PRIMARY ? RequestLocationMode.PRIMARY_ONLY
                        : RequestLocationMode.SECONDARY_ONLY);
                return null;
            }

            @Override
            public Integer postProcessResponse(HttpURLConnection connection, CloudBlob blob, CloudBlobClient client,
                    OperationContext context, Integer storageObject) throws Exception {
                final Boolean validateMD5 = !options.getDisableContentMD5Validation()
                        && !Utility.isNullOrEmpty(this.getContentMD5());
                final String contentLength = connection.getHeaderField(Constants.HeaderConstants.CONTENT_LENGTH);
                final long expectedLength = Long.parseLong(contentLength);

                Logger.info(context, String.format(SR.CREATING_NETWORK_STREAM, expectedLength));
                final NetworkInputStream streamRef = new NetworkInputStream(connection.getInputStream(), expectedLength);

                try {
                    // writeToOutputStream will update the currentRequestByteCount on this request in case a retry
                    // is needed and download should resume from that point
                    final StreamMd5AndLength descriptor = Utility.writeToOutputStream(streamRef, outStream, -1, false,
                            validateMD5, context, options, this);

                    // length was already checked by the NetworkInputStream, now check Md5
                    if (validateMD5 && !this.getContentMD5().equals(descriptor.getMd5())) {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_MD5, String.format(
                                SR.BLOB_HASH_MISMATCH, this.getContentMD5(), descriptor.getMd5()),
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }
                }
                finally {
                    // Close the stream and return. Closing an already closed stream is harmless. So its fine to try
                    // to drain the response and close the stream again in the executor.
                    streamRef.close();
                }

                return null;
            }

            @Override
            public void recoveryAction(OperationContext context) throws IOException {
                if (this.getETagLockCondition() == null && (!Utility.isNullOrEmpty(this.getLockedETag()))) {
                    AccessCondition etagLockCondition = new AccessCondition();
                    etagLockCondition.setIfMatch(this.getLockedETag());
                    if (accessCondition != null) {
                        etagLockCondition.setLeaseID(accessCondition.getLeaseID());
                    }
                    this.setETagLockCondition(etagLockCondition);
                }

                if (this.getCurrentRequestByteCount() > 0) {
                    this.setOffset(startingOffset + this.getCurrentRequestByteCount());
                    if (length != null) {
                        this.setLength(length - this.getCurrentRequestByteCount());
                    }
                }
            }
        };

        return getRequest;

    }

    /**
     * Downloads a range of bytes from the blob to the given byte buffer.
     *
     * @param blobOffset
     *            A <code>long</code> which represents the offset within the blob to begin downloading.
     * @param length
     *            A <code>Long</code> which represents the number of bytes to read.
     * @param buffer
     *            A <code>byte</code> array which represents the buffer to write to.
     * @param bufferOffset
     *            An <code>int</code> which represents the offset in the byte buffer to begin writing.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @returns The total number of bytes read into the buffer.
     * 
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    protected final int downloadRangeInternal(final long blobOffset, final Long length, final byte[] buffer,
            final int bufferOffset, final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {

        if (bufferOffset < 0 || blobOffset < 0 || (length != null && length <= 0)) {
            throw new IndexOutOfBoundsException();
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        if (options.getUseTransactionalContentMD5() && (length != null && length > 4 * Constants.MB)) {
            throw new IllegalArgumentException(SR.INVALID_RANGE_CONTENT_MD5_HEADER);
        }

        WrappedByteArrayOutputStream outputStream = new WrappedByteArrayOutputStream(buffer, bufferOffset);
        ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.downloadToStreamImpl(blobOffset, length, outputStream, accessCondition, options, opContext),
                options.getRetryPolicyFactory(), opContext);
        return outputStream.getPosition();
    }

    /**
     * Downloads a range of bytes from the blob to the given byte buffer.
     *
     * @param offset
     *            A <code>long</code> which represents the byte offset to use as the starting point for the source.
     * @param length
     *            A <code>Long</code> which represents the number of bytes to read or null.
     * @param buffer
     *            A <code>byte</code> array which represents the buffer to which the blob bytes are downloaded.
     * @param bufferOffset
     *            An <code>int</code> which represents the byte offset to use as the starting point for the target.
     * @returns The total number of bytes read into the buffer.
     * 
     * @throws StorageException
     */
    @DoesServiceRequest
    public final int downloadRangeToByteArray(final long offset, final Long length, final byte[] buffer,
            final int bufferOffset) throws StorageException {
        return this.downloadRangeToByteArray(offset, length, buffer, bufferOffset, null /* accessCondition */,
                null /* options */, null /* opContext */);
    }

    /**
     * Downloads a range of bytes from the blob to the given byte buffer, using the specified request options and
     * operation context.
     *
     * @param offset
     *            A <code>long</code> which represents the byte offset to use as the starting point for the source.
     * @param length
     *            A <code>Long</code> which represents the number of bytes to read or <code>null</code>.
     * @param buffer
     *            A <code>byte</code> array which represents the buffer to which the blob bytes are downloaded.
     * @param bufferOffset
     *            An <code>int</code> which represents the byte offset to use as the starting point for the target.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @returns The total number of bytes read into the buffer.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final int downloadRangeToByteArray(final long offset, final Long length, final byte[] buffer,
            final int bufferOffset, final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {

        Utility.assertNotNull("buffer", buffer);

        if (length != null) {
            if (length + bufferOffset > buffer.length) {
                throw new IndexOutOfBoundsException();
            }
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();

        return this.downloadRangeInternal(offset, length, buffer, bufferOffset, accessCondition, options, opContext);
    }

    /**
     * Downloads a range of bytes from the blob to the given byte buffer.
     *
     * @param buffer
     *            A <code>byte</code> array which represents the buffer to which the blob bytes are downloaded.
     * @param bufferOffset
     *            An <code>int</code> which represents the byte offset to use as the starting point for the target.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final int downloadToByteArray(final byte[] buffer, final int bufferOffset) throws StorageException {
        return this
                .downloadToByteArray(buffer, bufferOffset, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Downloads a range of bytes from the blob to the given byte buffer, using the specified request options and
     * operation context.
     *
     * @param buffer
     *            A <code>byte</code> array which represents the buffer to which the blob bytes are downloaded.
     * @param bufferOffset
     *            A <code>long</code> which represents the byte offset to use as the starting point for the target.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final int downloadToByteArray(final byte[] buffer, final int bufferOffset,
            final AccessCondition accessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {

        Utility.assertNotNull("buffer", buffer);
        if (bufferOffset < 0) {
            throw new IndexOutOfBoundsException();
        }

        if (bufferOffset >= buffer.length) {
            throw new IndexOutOfBoundsException();
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        WrappedByteArrayOutputStream outputStream = new WrappedByteArrayOutputStream(buffer, bufferOffset);
        ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.downloadToStreamImpl(null, null, outputStream, accessCondition, options, opContext),
                options.getRetryPolicyFactory(), opContext);
        return outputStream.getPosition();
    }

    /**
     * Uploads a blob from data in a byte array. If the blob already exists on the service, it will be overwritten.
     *
     * @param buffer
     *            A <code>byte</code> array which represents the data to write to the blob.
     * @param offset
     *            A <code>int</code> which represents the offset of the byte array from which to start the data upload.
     * @param length
     *            An <code>int</code> which represents the number of bytes to upload from the input buffer.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public void uploadFromByteArray(final byte[] buffer, final int offset, final int length) throws StorageException,
            IOException {
        uploadFromByteArray(buffer, offset, length, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Uploads a blob from data in a byte array. If the blob already exists on the service, it will be overwritten.
     *
     * @param buffer
     *            A <code>byte</code> array which represents the data to write to the blob.
     * @param offset
     *            A <code>int</code> which represents the offset of the byte array from which to start the data upload.
     * @param length
     *            An <code>int</code> which represents the number of bytes to upload from the input buffer.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public void uploadFromByteArray(final byte[] buffer, final int offset, final int length,
            final AccessCondition accessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException, IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer, offset, length);
        this.upload(inputStream, length, accessCondition, options, opContext);
        inputStream.close();
    }

    /**
     * Uploads a blob from a file. If the blob already exists on the service, it will be overwritten.
     *
     * @param path
     *            A <code>String</code> which represents the path to the file to be uploaded.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public void uploadFromFile(final String path) throws StorageException, IOException {
        uploadFromFile(path, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Uploads a blob from a file. If the blob already exists on the service, it will be overwritten.
     *
     * @param path
     *            A <code>String</code> which represents the path to the file to be uploaded.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public void uploadFromFile(final String path, final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException, IOException {
        File file = new File(path);
        long fileLength = file.length();
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        this.upload(inputStream, fileLength, accessCondition, options, opContext);
        inputStream.close();
    }

    /**
     * Downloads a blob, storing the contents in a file.
     *
     * @param path
     *            A <code>String</code> which represents the path to the file that will be created with the contents of
     *            the blob.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public void downloadToFile(final String path) throws StorageException, IOException {
        downloadToFile(path, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Downloads a blob, storing the contents in a file.
     *
     * @param path
     *            A <code>String</code> which represents the path to the file that will be created with the contents of
     *            the blob.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public void downloadToFile(final String path, final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException, IOException {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(path));
        try {
            this.download(outputStream, accessCondition, options, opContext);
            outputStream.close();
        }
        catch (StorageException e) {
            deleteEmptyFileOnException(outputStream, path);
            throw e;
        }
        catch (IOException e) {
            deleteEmptyFileOnException(outputStream, path);
            throw e;
        }
    }

    /**
     * Helper to delete an empty file in the case of an exception
     * 
     * @param outputStream
     * @param path
     * @throws IOException
     */
    private void deleteEmptyFileOnException(OutputStream outputStream, String path) {
        try {
            outputStream.close();
            File fileToDelete = new File(path);
            fileToDelete.delete();
        }
        catch (Exception e) {
            // Best effort delete.
        }
    }

    /**
     * Checks to see if the blob exists.
     *
     * @return <code>true</code> if the blob exists, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final boolean exists() throws StorageException {
        return this.exists(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Checks to see if the blob exists, using the specified request options and operation context.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return <code>true</code> if the blob exists, other wise <code>false</code>.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final boolean exists(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        return this.exists(false /* primaryOnly */, accessCondition, options, opContext);
    }

    @DoesServiceRequest
    private final boolean exists(final boolean primaryOnly, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.existsImpl(primaryOnly, accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Boolean> existsImpl(final boolean primaryOnly,
            final AccessCondition accessCondition, final BlobRequestOptions options) {
        final StorageRequest<CloudBlobClient, CloudBlob, Boolean> getRequest = new StorageRequest<CloudBlobClient, CloudBlob, Boolean>(
                options, this.getStorageUri()) {
            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(primaryOnly ? RequestLocationMode.PRIMARY_ONLY
                        : RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.getBlobProperties(
                        blob.getTransformedAddress(context).getUri(this.getCurrentLocation()), options, context,
                        accessCondition, blob.snapshotID);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Boolean preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_OK) {
                    final BlobAttributes retrievedAttributes = BlobResponse.getBlobAttributes(this.getConnection(),
                            blob.getStorageUri(), blob.snapshotID);
                    blob.properties = retrievedAttributes.getProperties();
                    blob.metadata = retrievedAttributes.getMetadata();
                    return Boolean.valueOf(true);
                }
                else if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return Boolean.valueOf(false);
                }
                else {
                    this.setNonExceptionedRetryableFailure(true);

                    // return false instead of null to avoid SCA issues
                    return false;
                }
            }
        };

        return getRequest;
    }

    /**
     * Returns a shared access signature for the blob using the specified group policy identifier and operation context.
     * Note this does not contain the leading "?".
     *
     * @param policy
     *            A <code>SharedAccessPolicy</code> object that represents the access policy for the shared access
     *            signature.
     * @param groupPolicyIdentifier
     *            A <code>String</code> that represents the container-level access policy.
     *
     * @return A <code>String</code> that represents the shared access signature.
     *
     * @throws IllegalArgumentException
     *             If the credentials are invalid or the blob is a snapshot.
     * @throws InvalidKeyException
     *             If the credentials are invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public String generateSharedAccessSignature(final SharedAccessBlobPolicy policy, final String groupPolicyIdentifier)
            throws InvalidKeyException, StorageException {

        return this.generateSharedAccessSignature(policy, null /* headers */, groupPolicyIdentifier);
    }
    
    /**
     * Returns a shared access signature for the blob using the specified group policy identifier and operation context.
     * Note this does not contain the leading "?".
     *
     * @param policy
     *            A <code>SharedAccessPolicy</code> object that represents the access policy for the shared access
     *            signature.
     * @param headers
     *            A <code>{@link SharedAccessBlobHeaders}</code> object that represents the optional header values to
     *            set for a blob accessed with this shared access signature.
     * @param groupPolicyIdentifier
     *            A <code>String</code> that represents the container-level access policy.
     *
     * @return A <code>String</code> that represents the shared access signature.
     *
     * @throws IllegalArgumentException
     *             If the credentials are invalid or the blob is a snapshot.
     * @throws InvalidKeyException
     *             If the credentials are invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public String generateSharedAccessSignature(final SharedAccessBlobPolicy policy, final SharedAccessBlobHeaders headers,
            final String groupPolicyIdentifier)
            throws InvalidKeyException, StorageException {

        return this.generateSharedAccessSignature(policy, headers, groupPolicyIdentifier,
                null /* IP range */, null /* protocols */);
    }
    
    /**
     * Returns a shared access signature for the blob using the specified group policy identifier and operation context.
     * Note this does not contain the leading "?".
     *
     * @param policy
     *            A <code>{@link SharedAccessPolicy}</code> object that represents the access policy for the shared
     *            access signature.
     * @param headers
     *            A <code>{@link SharedAccessBlobHeaders}</code> object that represents the optional header values to
     *            set for a blob accessed with this shared access signature.
     * @param groupPolicyIdentifier
     *            A <code>String</code> that represents the container-level access policy.
     * @param ipRange
     *            A {@link IPRange} object containing the range of allowed IP addresses.
     * @param protocols
     *            A {@link SharedAccessProtocols} representing the allowed Internet protocols.
     *
     * @return A <code>String</code> that represents the shared access signature.
     *
     * @throws IllegalArgumentException
     *             If the credentials are invalid or the blob is a snapshot.
     * @throws InvalidKeyException
     *             If the credentials are invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public String generateSharedAccessSignature(
            final SharedAccessBlobPolicy policy, final SharedAccessBlobHeaders headers,
            final String groupPolicyIdentifier, final IPRange ipRange, final SharedAccessProtocols protocols)
            throws InvalidKeyException, StorageException {
        
        if (!StorageCredentialsHelper.canCredentialsSignRequest(this.blobServiceClient.getCredentials())) {
            throw new IllegalArgumentException(SR.CANNOT_CREATE_SAS_WITHOUT_ACCOUNT_KEY);
        }
        
        if (this.isSnapshot()) {
            throw new IllegalArgumentException(SR.CANNOT_CREATE_SAS_FOR_SNAPSHOTS);
        }

        final String resourceName = this.getCanonicalName(true);

        final String signature = SharedAccessSignatureHelper.generateSharedAccessSignatureHashForBlobAndFile(
                policy, headers, groupPolicyIdentifier, resourceName, ipRange, protocols, this.blobServiceClient);

        final UriQueryBuilder builder = SharedAccessSignatureHelper.generateSharedAccessSignatureForBlobAndFile(
                policy, headers, groupPolicyIdentifier, "b", ipRange, protocols, signature);

        return builder.toString();
    }

    /**
     * Returns the canonical name of the blob in the format of
     * <i>/&lt;service-name&gt;/&lt;account-name&gt;/&lt;container-name&gt;/&lt;blob-name&gt;</i>.
     * <p>
     * This format is used for Shared Access operations.
     *
     * @param ignoreSnapshotTime
     *            <code>true</code> if the snapshot time is ignored; otherwise, <code>false</code>.
     *
     * @return The canonical name in the format of <i>/&lt;service-name&gt;/&lt;account-name&gt;
     *         /&lt;container-name&gt;/&lt;blob-name&gt;</i>.
     */
    String getCanonicalName(final boolean ignoreSnapshotTime) {
        StringBuilder canonicalName = new StringBuilder("/");
        canonicalName.append(SR.BLOB);
        
        if (this.blobServiceClient.isUsePathStyleUris()) {
            canonicalName.append(this.getUri().getRawPath());
        }
        else {
            canonicalName.append(PathUtility.getCanonicalPathFromCredentials(
                    this.blobServiceClient.getCredentials(), this.getUri().getRawPath()));
        }

        if (!ignoreSnapshotTime && this.snapshotID != null) {
            canonicalName.append("?snapshot=");
            canonicalName.append(this.snapshotID);
        }

        return canonicalName.toString();
    }

    /**
     * Returns the blob's container.
     *
     * @return A {@link CloudBlobContainer} object that represents the container of the blob.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @Override
    public final CloudBlobContainer getContainer() throws StorageException, URISyntaxException {
        if (this.container == null) {
            final StorageUri containerURI = PathUtility.getContainerURI(this.getStorageUri(),
                    this.blobServiceClient.isUsePathStyleUris());
            this.container = new CloudBlobContainer(containerURI, this.blobServiceClient.getCredentials());
        }

        return this.container;
    }

    /**
     * Returns the metadata for the blob.
     *
     * @return A <code>java.util.HashMap</code> object that represents the metadata for the blob.
     */
    public final HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Returns the name of the blob.
     *
     * @return A <code>String</code> that represents the name of the blob.
     *
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public final String getName() throws URISyntaxException {
        return this.name;
    }

    /**
     * Returns the blob item's parent.
     *
     * @return A {@link CloudBlobDirectory} object that represents the parent directory for the blob.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @Override
    public final CloudBlobDirectory getParent() throws URISyntaxException, StorageException {
        if (this.parent == null) {
            final String parentName = getParentNameFromURI(this.getStorageUri(),
                    this.blobServiceClient.getDirectoryDelimiter(), this.getContainer());

            if (parentName != null) {
                StorageUri parentURI = PathUtility.appendPathToUri(this.container.getStorageUri(), parentName);
                this.parent = new CloudBlobDirectory(parentURI, parentName, this.blobServiceClient, this.getContainer());
            }
        }
        return this.parent;
    }

    /**
     * Returns the blob's properties.
     *
     * @return A {@link BlobProperties} object that represents the properties of the blob.
     */
    public final BlobProperties getProperties() {
        return this.properties;
    }

    /**
     * Returns the blob's copy state.
     *
     * @return A {@link CopyState} object that represents the copy state of the blob.
     */
    public CopyState getCopyState() {
        return this.properties.getCopyState();
    }

    /**
     * Returns the snapshot or shared access signature qualified URI for this blob.
     *
     * @return A {@link StorageUri} object that represents the snapshot or shared access signature.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public final StorageUri getQualifiedStorageUri() throws URISyntaxException, StorageException {
        if (this.isSnapshot()) {
            StorageUri snapshotQualifiedUri = PathUtility.addToQuery(this.getStorageUri(),
                    String.format("snapshot=%s", this.snapshotID));
            return this.blobServiceClient.getCredentials().transformUri(snapshotQualifiedUri);
        }
        return this.blobServiceClient.getCredentials().transformUri(this.getStorageUri());
    }

    /**
     * Returns the snapshot or shared access signature qualified URI for this blob.
     *
     * @return A <code>java.net.URI</code> object that represents the snapshot or shared access signature.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public final URI getQualifiedUri() throws URISyntaxException, StorageException {
        if (this.isSnapshot()) {
            return PathUtility.addToQuery(this.getUri(), String.format("snapshot=%s", this.snapshotID));
        }
        return this.blobServiceClient.getCredentials().transformUri(this.getUri());
    }

    /**
     * Returns the Blob service client associated with the blob.
     *
     * @return A {@link CloudBlobClient} object that represents the client.
     */
    public final CloudBlobClient getServiceClient() {
        return this.blobServiceClient;
    }

    /**
     * Gets the Blob Snapshot ID.
     *
     * @return A <code>String</code> which represents the Blob Snapshot ID.
     */
    public final String getSnapshotID() {
        return this.snapshotID;
    }

    /**
     * Returns the list of URIs for all locations.
     *
     * @return A {@link StorageUri} that represents the list of URIs for all locations..
     */
    @Override
    public final StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * Gets the number of bytes to buffer when writing to a {@link BlobOutputStream} (block and page blobs).
     *
     * @return
     *         A <code>int</code> which represents the number of bytes to buffer or the size of a block, in bytes.
     */
    public final int getStreamWriteSizeInBytes() {
        return this.streamWriteSizeInBytes;
    }

    /**
     * Returns the minimum read size when using a {@link BlobInputStream}.
     *
     * @return A <code>int</code> which represents the minimum read size, in bytes, when using a {@link BlobInputStream}
     *         object.
     */
    public final int getStreamMinimumReadSizeInBytes() {
        return this.streamMinimumReadSizeInBytes;
    }

    /**
     * Returns the transformed URI for the resource if the given credentials require transformation.
     *
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return A {@link StorageUri} object that represents the transformed URI.
     *
     * @throws IllegalArgumentException
     *             If the URI is not absolute.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    protected final StorageUri getTransformedAddress(final OperationContext opContext) throws URISyntaxException,
            StorageException {
        return this.blobServiceClient.getCredentials().transformUri(this.getStorageUri(), opContext);
    }

    /**
     * Returns the URI for this blob.
     *
     * @return A <code>java.net.URI</code> object that represents the URI for the blob.
     */
    @Override
    public final URI getUri() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Indicates whether this blob is a snapshot.
     *
     * @return <code>true</code> if the blob is a snapshot, otherwise <code>false</code>.
     *
     * @see DeleteSnapshotsOption
     */
    public final boolean isSnapshot() {
        return this.snapshotID != null;
    }

    /**
     * Opens a blob input stream to download the blob.
     * <p>
     * Use {@link #setStreamMinimumReadSizeInBytes(int)} to configure the read size.
     *
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final BlobInputStream openInputStream() throws StorageException {
        return this.openInputStream(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Opens a blob input stream to download the blob using the specified request options and operation context.
     * <p>
     * Use {@link #setStreamMinimumReadSizeInBytes(int)} to configure the read size.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final BlobInputStream openInputStream(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        assertNoWriteOperationForSnapshot();

        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient, 
                false /* setStartTime */);

        return new BlobInputStream(this, accessCondition, options, opContext);
    }
    
    /**
     * Verifies the passed in URI. Then parses it and uses its components to populate this resource's properties.
     * 
     * @param completeUri
     *            A {@link StorageUri} object which represents the complete URI.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    private void parseQueryAndVerify(final StorageUri completeUri, final StorageCredentials credentials)
            throws StorageException {
       Utility.assertNotNull("completeUri", completeUri);

        if (!completeUri.isAbsolute()) {
            throw new IllegalArgumentException(String.format(SR.RELATIVE_ADDRESS_NOT_PERMITTED, completeUri.toString()));
        }

        this.storageUri = PathUtility.stripURIQueryAndFragment(completeUri);
        
        final HashMap<String, String[]> queryParameters = PathUtility.parseQueryString(completeUri.getQuery());

        final String[] snapshotIDs = queryParameters.get(BlobConstants.SNAPSHOT);
        if (snapshotIDs != null && snapshotIDs.length > 0) {
            this.snapshotID = snapshotIDs[0];
        }
        
        final StorageCredentialsSharedAccessSignature parsedCredentials = 
                SharedAccessSignatureHelper.parseQuery(queryParameters);

        if (credentials != null && parsedCredentials != null) {
            throw new IllegalArgumentException(SR.MULTIPLE_CREDENTIALS_PROVIDED);
        }

        try {
            final boolean usePathStyleUris = Utility.determinePathStyleFromUri(this.storageUri.getPrimaryUri());
            this.blobServiceClient = new CloudBlobClient(PathUtility.getServiceClientBaseAddress(
                    this.getStorageUri(), usePathStyleUris), credentials != null ? credentials : parsedCredentials);
            this.name = PathUtility.getBlobNameFromURI(this.storageUri.getPrimaryUri(), usePathStyleUris);
        }
        catch (final URISyntaxException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
    }

    /**
     * Releases the lease on the blob.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob. The LeaseID is
     *            required to be set on the AccessCondition.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void releaseLease(final AccessCondition accessCondition) throws StorageException {
        this.releaseLease(accessCondition, null /* options */, null /* opContext */);
    }

    /**
     * Releases the lease on the blob using the specified request options and operation context.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.The LeaseID is
     *            required to be set on the AccessCondition.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void releaseLease(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        Utility.assertNotNull("accessCondition", accessCondition);
        Utility.assertNotNullOrEmpty("leaseID", accessCondition.getLeaseID());

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        ExecutionEngine.executeWithRetry(this.blobServiceClient, this, this.releaseLeaseImpl(accessCondition, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Void> releaseLeaseImpl(final AccessCondition accessCondition,
            final BlobRequestOptions options) {
        final StorageRequest<CloudBlobClient, CloudBlob, Void> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.leaseBlob(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, LeaseAction.RELEASE, null /* leaseTimeInSeconds */,
                        null /* proposedLeaseId */, null /*breakPeriodInSeconds */);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                updateEtagAndLastModifiedFromResponse(this.getConnection());

                blob.properties.setLeaseStatus(LeaseStatus.UNLOCKED);
                return null;
            }
        };

        return putRequest;
    }

    /**
     * Renews an existing lease.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob. The LeaseID is
     *            required to be set on the AccessCondition.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void renewLease(final AccessCondition accessCondition) throws StorageException {
        this.renewLease(accessCondition, null /* options */, null /* opContext */);
    }

    /**
     * Renews an existing lease using the specified request options and operation context.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob. The LeaseID is
     *            required to be set on the AccessCondition.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void renewLease(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        Utility.assertNotNull("accessCondition", accessCondition);
        Utility.assertNotNullOrEmpty("leaseID", accessCondition.getLeaseID());

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        ExecutionEngine.executeWithRetry(this.blobServiceClient, this, this.renewLeaseImpl(accessCondition, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Void> renewLeaseImpl(final AccessCondition accessCondition,
            final BlobRequestOptions options) {
        final StorageRequest<CloudBlobClient, CloudBlob, Void> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, Void>(
                options, this.getStorageUri()) {
            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.leaseBlob(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, LeaseAction.RENEW, null /* leaseTimeInSeconds */,
                        null /* proposedLeaseId */, null /*breakPeriodInSeconds */);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                updateEtagAndLastModifiedFromResponse(this.getConnection());

                return null;
            }
        };

        return putRequest;
    }

    /**
     * Sets the container for the blob.
     *
     * @param container
     *            A {@link CloudBlobContainer} object that represents the container being assigned to the blob.
     */
    protected final void setContainer(final CloudBlobContainer container) {
        this.container = container;
    }

    /**
     * Sets the metadata for the blob.
     *
     * @param metadata
     *            A <code>java.util.HashMap</code> object that contains the metadata being assigned to the blob.
     */
    public final void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the properties for the blob.
     *
     * @param properties
     *            A {@link BlobProperties} object that represents the properties being assigned to the blob.
     */
    protected final void setProperties(final BlobProperties properties) {
        this.properties = properties;
    }

    /**
     * Sets the blob snapshot ID.
     *
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot ID being assigned to the blob.
     */
    protected final void setSnapshotID(final String snapshotID) {
        this.snapshotID = snapshotID;
    }

    /**
     * Sets the list of URIs for all locations.
     *
     * @param storageUri
     *            A {@link StorageUri} that represents the list of URIs for all locations.
     */
    protected void setStorageUri(final StorageUri storageUri) {
        this.storageUri = storageUri;
    }

    /**
     * Sets the number of bytes to buffer when writing to a {@link BlobOutputStream} (block and page blobs).
     *
     * @param streamWriteSizeInBytes
     *            An <code>int</code> that represents the number of bytes to buffer or the size of a block, in bytes.
     */
    public abstract void setStreamWriteSizeInBytes(int streamWriteSizeInBytes);

    /**
     * Sets the minimum read size when using a {@link BlobInputStream}.
     *
     * @param minimumReadSize
     *            An <code>int</code> that represents the minimum block size, in bytes, for reading from a blob while
     *            using a {@link BlobInputStream} object. Must be greater than or equal to 16 KB.
     * @throws IllegalArgumentException
     *             If <code>minimumReadSize</code> is less than 16 KB.
     */
    public void setStreamMinimumReadSizeInBytes(final int minimumReadSize) {
        if (minimumReadSize < 16 * Constants.KB) {
            throw new IllegalArgumentException("MinimumReadSize");
        }

        this.streamMinimumReadSizeInBytes = minimumReadSize;
    }

    protected void updateEtagAndLastModifiedFromResponse(HttpURLConnection request) {
        // ETag
        this.getProperties().setEtag(request.getHeaderField(Constants.HeaderConstants.ETAG));

        // Last Modified
        if (0 != request.getLastModified()) {
            final Calendar lastModifiedCalendar = Calendar.getInstance(Utility.LOCALE_US);
            lastModifiedCalendar.setTimeZone(Utility.UTC_ZONE);
            lastModifiedCalendar.setTime(new Date(request.getLastModified()));
            this.getProperties().setLastModified(lastModifiedCalendar.getTime());
        }
    }

    protected void updateLengthFromResponse(HttpURLConnection request) {
        final String xContentLengthHeader = request.getHeaderField(BlobConstants.CONTENT_LENGTH_HEADER);
        if (!Utility.isNullOrEmpty(xContentLengthHeader)) {
            this.getProperties().setLength(Long.parseLong(xContentLengthHeader));
        }
    }

    /**
     * Uploads the source stream data to the blob. If the blob already exists on the service, it will be overwritten.
     *
     * @param sourceStream
     *            An <code>InputStream</code> object that represents the source stream to upload.
     *
     * @param length
     *            An <code>long</code> that represents the length of the stream data in bytes, or -1 if unknown. The
     *            length must be greater than zero and a
     *            multiple of 512 for page blobs.
     *
     * @throws IOException
     *             If an I/O exception occurred.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     *
     */
    @DoesServiceRequest
    public abstract void upload(InputStream sourceStream, long length) throws StorageException, IOException;

    /**
     * Uploads the source stream data to the blob using the specified lease ID, request options, and operation context.
     * If the blob already exists on the service, it will be overwritten.
     * 
     * @param sourceStream
     *            An <code>InputStream</code> object that represents the source stream to upload.
     * @param length
     *            The length of the stream data in bytes, or -1 if unknown. The length must be greater than zero and a
     *            multiple of 512 for page blobs.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public abstract void upload(InputStream sourceStream, long length, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException, IOException;


    /**
     * Uploads the blob's metadata to the storage service.
     * <p>
     * Use {@link CloudBlob#downloadAttributes} to retrieve the latest values for the blob's properties and metadata
     * from the Microsoft Azure storage service.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void uploadMetadata() throws StorageException {
        this.uploadMetadata(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Uploads the blob's metadata to the storage service using the specified lease ID, request options, and operation
     * context.
     * <p>
     * Use {@link CloudBlob#downloadAttributes} to retrieve the latest values for the blob's properties and metadata
     * from the Microsoft Azure storage service.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void uploadMetadata(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        assertNoWriteOperationForSnapshot();

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.uploadMetadataImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Void> uploadMetadataImpl(final AccessCondition accessCondition,
            final BlobRequestOptions options) {
        final StorageRequest<CloudBlobClient, CloudBlob, Void> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.setBlobMetadata(blob.getTransformedAddress(context)
                        .getUri(this.getCurrentLocation()), options, context, accessCondition);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudBlob blob, OperationContext context) {
                BlobRequest.addMetadata(connection, blob.metadata, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(this.getConnection());
                return null;
            }
        };

        return putRequest;
    }

    /**
     * Updates the blob's properties to the storage service.
     * <p>
     * Use {@link CloudBlob#downloadAttributes} to retrieve the latest values for the blob's properties and metadata
     * from the Microsoft Azure storage service.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void uploadProperties() throws StorageException {
        this.uploadProperties(null /* accessCondition */, null /* options */, null /*opContext */);
    }

    /**
     * Updates the blob's properties using the specified lease ID, request options, and operation context.
     * <p>
     * Use {@link CloudBlob#downloadAttributes} to retrieve the latest values for the blob's properties and metadata
     * from the Microsoft Azure storage service.
     *
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void uploadProperties(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        assertNoWriteOperationForSnapshot();

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.populateAndApplyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.uploadPropertiesImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Void> uploadPropertiesImpl(
            final AccessCondition accessCondition, final BlobRequestOptions options) {
        final StorageRequest<CloudBlobClient, CloudBlob, Void> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.setBlobProperties(
                        blob.getTransformedAddress(context).getUri(this.getCurrentLocation()), options, context,
                        accessCondition, blob.properties);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);

            }

            @Override
            public Void preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(this.getConnection());
                return null;
            }
        };

        return putRequest;
    }

    /**
     * Retrieves the parent name for a blob URI.
     *
     * @param resourceAddress
     *            A {@link StorageUri} object which represents the resource URI.
     * @param delimiter
     *            A <code>String</code> which specifies the directory delimiter to use.
     * @param usePathStyleUris
     *            A {@link CloudBlobContainer} object which represents the blob container.
     *
     * @return A <code>String</code> which represents the parent address for a blob URI.
     *
     * @throws URISyntaxException
     */
    protected static String getParentNameFromURI(final StorageUri resourceAddress, final String delimiter,
            final CloudBlobContainer container) throws URISyntaxException {
        Utility.assertNotNull("resourceAddress", resourceAddress);
        Utility.assertNotNull("container", container);
        Utility.assertNotNullOrEmpty("delimiter", delimiter);

        String containerName = container.getName() + "/";

        String relativeURIString = Utility.safeRelativize(container.getStorageUri().getPrimaryUri(),
                resourceAddress.getPrimaryUri());

        if (relativeURIString.endsWith(delimiter)) {
            relativeURIString = relativeURIString.substring(0, relativeURIString.length() - delimiter.length());
        }

        String parentName;

        if (Utility.isNullOrEmpty(relativeURIString)) {
            // Case 1 /<ContainerName>[Delimiter]*? => /<ContainerName>
            // Parent of container is container itself
            parentName = null;
        }
        else {
            final int lastDelimiterDex = relativeURIString.lastIndexOf(delimiter);

            if (lastDelimiterDex < 0) {
                // Case 2 /<Container>/<folder>
                // Parent of a folder is container
                parentName = "";
            }
            else {
                // Case 3 /<Container>/<folder>/[<subfolder>/]*<BlobName>
                // Parent of blob is folder
                parentName = relativeURIString.substring(0, lastDelimiterDex + delimiter.length());
                if (parentName != null && parentName.equals(containerName)) {
                    parentName = "";
                }
            }
        }

        return parentName;
    }
}
