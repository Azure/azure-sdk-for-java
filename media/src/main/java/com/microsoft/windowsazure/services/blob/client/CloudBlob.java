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
package com.microsoft.windowsazure.services.blob.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.microsoft.windowsazure.services.blob.core.storage.SharedAccessSignatureHelper;
import com.microsoft.windowsazure.services.core.storage.AccessCondition;
import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.DoesServiceRequest;
import com.microsoft.windowsazure.services.core.storage.LeaseStatus;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.RetryNoRetry;
import com.microsoft.windowsazure.services.core.storage.RetryPolicy;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.windowsazure.services.core.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.StorageExtendedErrorInformation;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.core.storage.utils.PathUtility;
import com.microsoft.windowsazure.services.core.storage.utils.StreamMd5AndLength;
import com.microsoft.windowsazure.services.core.storage.utils.UriQueryBuilder;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseResponse;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ExecutionEngine;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.LeaseAction;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageOperation;

/**
 * 
 * Represents a Windows Azure blob. This is the base class for the {@link CloudBlockBlob} and {@link CloudPageBlob}
 * classes.
 */
public abstract class CloudBlob implements ListBlobItem {
    /**
     * Holds the metadata for the blob.
     */
    HashMap<String, String> metadata;

    /**
     * Holds the properties of the blob.
     */
    BlobProperties properties;

    /**
     * Holds the URI of the blob, Setting this is RESERVED for internal use.
     */
    URI uri;

    /**
     * Holds the snapshot ID.
     */
    String snapshotID;

    /**
     * Represents the state of the most recent or pending copy operation.
     */
    CopyState copyState;

    /**
     * Holds the Blobs container Reference.
     */
    private CloudBlobContainer container;

    /**
     * Represents the blob's directory.
     */
    protected CloudBlobDirectory parent;

    /**
     * Holds the Blobs Name.
     */
    private String name;

    /**
     * Represents the blob client.
     */
    protected CloudBlobClient blobServiceClient;

    /**
     * Creates an instance of the <code>CloudBlob</code> class.
     * 
     * @param type
     *            the type of the blob.
     */
    protected CloudBlob(final BlobType type) {
        this.metadata = new HashMap<String, String>();
        this.properties = new BlobProperties(type);
    }

    /**
     * Creates an instance of the <code>CloudBlob</code> class using the specified URI and cloud blob client.
     * 
     * @param type
     *            the type of the blob.
     * @param uri
     *            A <code>java.net.URI</code> object that represents the URI to the blob, beginning with the container
     *            name.
     * @param client
     *            A {@link CloudBlobClient} object that specifies the endpoint for the Blob service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    protected CloudBlob(final BlobType type, final URI uri, final CloudBlobClient client) throws StorageException {
        this(type);

        Utility.assertNotNull("blobAbsoluteUri", uri);

        this.blobServiceClient = client;
        this.uri = uri;

        this.parseURIQueryStringAndVerify(uri, client, client.isUsePathStyleUris());
    }

    /**
     * Creates an instance of the <code>CloudBlob</code> class using the specified URI, cloud blob client, and cloud
     * blob container.
     * 
     * @param type
     *            the type of the blob.
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI to the blob, beginning with the
     *            container name.
     * @param client
     *            A {@link CloudBlobClient} object that specifies the endpoint for the Blob service.
     * @param container
     *            A {@link CloudBlobContainer} object that represents the container to use for the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    protected CloudBlob(final BlobType type, final URI uri, final CloudBlobClient client,
            final CloudBlobContainer container) throws StorageException {
        this(type, uri, client);
        this.container = container;
    }

    /**
     * Creates an instance of the <code>CloudBlob</code> class using the specified URI, snapshot ID, and cloud blob
     * client.
     * 
     * @param type
     *            the type of the blob.
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI to the blob, beginning with the
     *            container name.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot version, if applicable.
     * @param client
     *            A {@link CloudBlobContainer} object that represents the container to use for the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    protected CloudBlob(final BlobType type, final URI uri, final String snapshotID, final CloudBlobClient client)
            throws StorageException {
        this(type, uri, client);
        if (snapshotID != null) {
            if (this.snapshotID != null) {
                throw new IllegalArgumentException(
                        "Snapshot query parameter is already defined in the blobUri. Either pass in a snapshotTime parameter or use a full URL with a snapshot query parameter.");
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
        this.metadata = new HashMap<String, String>();
        this.properties = new BlobProperties(otherBlob.properties);

        if (otherBlob.metadata != null) {
            this.metadata = new HashMap<String, String>();
            for (final String key : otherBlob.metadata.keySet()) {
                this.metadata.put(key, otherBlob.metadata.get(key));
            }
        }

        this.snapshotID = otherBlob.snapshotID;
        this.uri = otherBlob.uri;
        this.container = otherBlob.container;
        this.parent = otherBlob.parent;
        this.blobServiceClient = otherBlob.blobServiceClient;
        this.name = otherBlob.name;
        this.copyState = otherBlob.copyState;
    }

    /**
     * Acquires a new lease on the blob with the specified lease time and proposed lease ID.
     * 
     * @param leaseTimeInSeconds
     *            Specifies the span of time for which to acquire the lease, in seconds.
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
        return this.acquireLease(leaseTimeInSeconds, proposedLeaseId, null, null, null);
    }

    /**
     * Acquires a new lease on the blob with the specified lease time, proposed lease ID, request
     * options, and operation context.
     * 
     * @param leaseTimeInSeconds
     *            Specifies the span of time for which to acquire the lease, in seconds.
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

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, String> impl = new StorageOperation<CloudBlobClient, CloudBlob, String>(
                options) {
            @Override
            public String execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.lease(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), LeaseAction.ACQUIRE, leaseTimeInSeconds,
                        proposedLeaseId, null, accessCondition, blobOptions, opContext);
                this.setConnection(request);

                this.signRequest(client, request, 0L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.properties.setLeaseStatus(LeaseStatus.LOCKED);

                return BlobResponse.getLeaseID(request, opContext);
            }
        };

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Asserts that the blob has the correct blob type specified in the blob attributes.
     * 
     * @throws StorageException
     *             If an incorrect blob type is used.
     */
    protected final void assertCorrectBlobType() throws StorageException {
        if (this instanceof CloudBlockBlob && this.properties.getBlobType() != BlobType.BLOCK_BLOB) {
            throw new StorageException(
                    StorageErrorCodeStrings.INCORRECT_BLOB_TYPE,
                    String.format(
                            "Incorrect Blob type, please use the correct Blob type to access a blob on the server. Expected %s, actual %s",
                            BlobType.BLOCK_BLOB, this.properties.getBlobType()),
                    Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
        }
        if (this instanceof CloudPageBlob && this.properties.getBlobType() != BlobType.PAGE_BLOB) {
            throw new StorageException(
                    StorageErrorCodeStrings.INCORRECT_BLOB_TYPE,
                    String.format(
                            "Incorrect Blob type, please use the correct Blob type to access a blob on the server. Expected %s, actual %s",
                            BlobType.PAGE_BLOB, this.properties.getBlobType()),
                    Constants.HeaderConstants.HTTP_UNUSED_306, null, null);

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
     * @return The time, in seconds, remaining in the lease period.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final long breakLease(final Integer breakPeriodInSeconds) throws StorageException {
        return this.breakLease(breakPeriodInSeconds, null, null, null);
    }

    /**
     * Breaks the existing lease, using the specified request options and operation context, and ensures that another
     * client cannot acquire a new lease until the current lease period has expired.
     * 
     * @param breakPeriodInSeconds
     *            Specifies the time to wait, in seconds, until the current lease is broken.
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
     * @return The time, in seconds, remaining in the lease period.
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

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Long> impl = new StorageOperation<CloudBlobClient, CloudBlob, Long>(
                options) {
            @Override
            public Long execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.lease(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), LeaseAction.BREAK, null, null,
                        breakPeriodInSeconds, accessCondition, blobOptions, opContext);
                this.setConnection(request);

                this.signRequest(client, request, 0L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return -1L;
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                final String leaseTime = BlobResponse.getLeaseTime(request, opContext);

                blob.properties.setLeaseStatus(LeaseStatus.UNLOCKED);
                return Utility.isNullOrEmpty(leaseTime) ? -1L : Long.parseLong(leaseTime);
            }
        };

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Copies an existing blob's contents, properties, and metadata to this instance of the <code>CloudBlob</code>
     * class.
     * 
     * @param sourceBlob
     *            A <code>CloudBlob</code> object that represents the source blob to copy.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     */
    @DoesServiceRequest
    public final void copyFromBlob(final CloudBlob sourceBlob) throws StorageException, URISyntaxException {
        this.copyFromBlob(sourceBlob, null, null, null, null);
    }

    /**
     * Copies an existing blob's contents, properties, and metadata to this instance of the <code>CloudBlob</code>
     * class, using the specified access conditions, lease ID, request options, and operation context.
     * 
     * @param sourceBlob
     *            A <code>CloudBlob</code> object that represents the source blob to copy.
     * @param sourceAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the source blob.
     * @param destinationAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the destination blob.
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
     * @throws URISyntaxException
     * 
     */
    @DoesServiceRequest
    public final void copyFromBlob(final CloudBlob sourceBlob, final AccessCondition sourceAccessCondition,
            final AccessCondition destinationAccessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException, URISyntaxException {
        this.copyFromBlob(sourceBlob.uri, sourceAccessCondition, destinationAccessCondition, options, opContext);

    }

    /**
     * Copies an existing blob's contents, properties, and metadata to this instance of the <code>CloudBlob</code>
     * class.
     * 
     * @param source
     *            A <code>URI</code> The URI of a source blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void copyFromBlob(final URI source) throws StorageException {
        this.copyFromBlob(source, null, null, null, null);
    }

    /**
     * Copies an existing blob's contents, properties, and metadata to a new blob, using the specified access
     * conditions, lease ID, request options, and operation context.
     * 
     * @param source
     *            A <code>URI</code> The URI of a source blob.
     * @param sourceAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the source blob.
     * @param destinationAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the destination blob.
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
     * 
     */
    @DoesServiceRequest
    public final void copyFromBlob(final URI source, final AccessCondition sourceAccessCondition,
            final AccessCondition destinationAccessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.copyFrom(blob.getTransformedAddress(opContext),
                        blobOptions.getTimeoutIntervalInMs(), source.toString(), blob.snapshotID,
                        sourceAccessCondition, destinationAccessCondition, blobOptions, opContext);
                this.setConnection(request);

                BlobRequest.addMetadata(request, blob.metadata, opContext);

                this.signRequest(client, request, 0, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                blob.copyState = BlobResponse.getCopyState(request);

                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
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
        this.abortCopy(copyId, null, null, null);
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
     * 
     */
    @DoesServiceRequest
    public final void abortCopy(final String copyId, final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.abortCopy(blob.getTransformedAddress(opContext),
                        blobOptions.getTimeoutIntervalInMs(), copyId, accessCondition, blobOptions, opContext);
                this.setConnection(request);

                this.signRequest(client, request, 0, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
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
        return this.createSnapshot(null, null, null);
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
        assertNoWriteOperationForSnapshot();

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, CloudBlob> impl = new StorageOperation<CloudBlobClient, CloudBlob, CloudBlob>(
                options) {
            @Override
            public CloudBlob execute(final CloudBlobClient client, final CloudBlob blob,
                    final OperationContext opContext) throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.snapshot(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), accessCondition, blobOptions, opContext);
                this.setConnection(request);

                this.signRequest(client, request, 0L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }
                CloudBlob snapshot = null;
                final String snapshotTime = BlobResponse.getSnapshotTime(request, opContext);
                if (blob instanceof CloudBlockBlob) {
                    snapshot = new CloudBlockBlob(blob.getUri(), snapshotTime, client);
                }
                else if (blob instanceof CloudPageBlob) {
                    snapshot = new CloudPageBlob(blob.getUri(), snapshotTime, client);
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                return snapshot;
            }
        };

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Deletes the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void delete() throws StorageException {
        this.delete(DeleteSnapshotsOption.NONE, null, null, null);
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
     *            A {@link DeleteSnapshotsOption} object that indicates whether to delete only blobs, only snapshots, or
     *            both.
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

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.delete(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), blob.snapshotID, deleteSnapshotsOption,
                        accessCondition, blobOptions, opContext);
                this.setConnection(request);

                this.signRequest(client, request, -1L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
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
        return this.deleteIfExists(DeleteSnapshotsOption.NONE, null, null, null);
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
     *            A {@link DeleteSnapshotsOption} object that indicates whether to delete only blobs, only snapshots, or
     *            both.
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
     * @return <code>true</code> if the blob was deleted; otherwise, <code>false</code>
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final boolean deleteIfExists(final DeleteSnapshotsOption deleteSnapshotsOption,
            final AccessCondition accessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        Utility.assertNotNull("deleteSnapshotsOption", deleteSnapshotsOption);

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Boolean> impl = new StorageOperation<CloudBlobClient, CloudBlob, Boolean>(
                options) {
            @Override
            public Boolean execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.delete(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), blob.snapshotID, deleteSnapshotsOption,
                        accessCondition, blobOptions, opContext);
                this.setConnection(request);

                this.signRequest(client, request, -1L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_ACCEPTED) {
                    return true;
                }
                else if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return false;
                }
                else {
                    this.setNonExceptionedRetryableFailure(true);

                    // return false instead of null to avoid SCA issues
                    return false;
                }
            }
        };

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Downloads the contents of a blob to a stream.
     * 
     * @param outStream
     *            An <code>OutputStream</code> object that represents the target stream.
     * 
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void download(final OutputStream outStream) throws StorageException, IOException {
        this.download(outStream, null, null, null);
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
     * 
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void download(final OutputStream outStream, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException, IOException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.get(blob.getTransformedAddress(opContext),
                        blobOptions.getTimeoutIntervalInMs(), blob.snapshotID, accessCondition, blobOptions, opContext);
                this.setConnection(request);

                this.signRequest(client, request, -1L, null);

                final InputStream streamRef = ExecutionEngine.getInputStream(request, opContext, this.getResult());

                final String contentMD5 = request.getHeaderField(Constants.HeaderConstants.CONTENT_MD5);
                final Boolean validateMD5 = !blobOptions.getDisableContentMD5Validation()
                        && !Utility.isNullOrEmpty(contentMD5);

                final String contentLength = request.getHeaderField(Constants.HeaderConstants.CONTENT_LENGTH);
                final long expectedLength = Long.parseLong(contentLength);

                final BlobAttributes retrievedAttributes = BlobResponse.getAttributes(request, blob.getUri(),
                        blob.snapshotID, opContext);
                blob.properties = retrievedAttributes.getProperties();
                blob.metadata = retrievedAttributes.getMetadata();
                blob.copyState = retrievedAttributes.getCopyState();
                ExecutionEngine.getResponseCode(this.getResult(), request, opContext);

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                final StreamMd5AndLength descriptor = Utility.writeToOutputStream(streamRef, outStream, -1, false,
                        validateMD5, this.getResult(), opContext);

                if (descriptor.getLength() != expectedLength) {
                    throw new StorageException(
                            StorageErrorCodeStrings.OUT_OF_RANGE_INPUT,
                            "An incorrect number of bytes was read from the connection. The connection may have been closed",
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }

                if (validateMD5 && !contentMD5.equals(descriptor.getMd5())) {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_MD5, String.format(
                            "Blob data corrupted (integrity check failed), Expected value is %s, retrieved %s",
                            contentMD5, descriptor.getMd5()), Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }

                return null;
            }
        };

        try {
            // Executed with no retries so that the first failure will move out
            // to the read Stream.
            ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, RetryNoRetry.getInstance(), opContext);
            opContext.setIntermediateMD5(null);
        }
        catch (final StorageException ex) {
            // Check if users has any retries specified, Or if the exception is retryable
            final RetryPolicy dummyPolicy = options.getRetryPolicyFactory().createInstance(opContext);
            if ((ex.getHttpStatusCode() == Constants.HeaderConstants.HTTP_UNUSED_306 && !ex.getErrorCode().equals(
                    StorageErrorCodeStrings.OUT_OF_RANGE_INPUT))
                    || ex.getHttpStatusCode() == HttpURLConnection.HTTP_PRECON_FAILED
                    || !dummyPolicy.shouldRetry(0, impl.getResult().getStatusCode(), (Exception) ex.getCause(),
                            opContext).isShouldRetry()) {
                opContext.setIntermediateMD5(null);
                throw ex;
            }

            // Continuation, fail gracefully into a read stream. This needs to
            // be outside the operation above as it would get retried resulting
            // in a nested retry

            // Copy access condition, and update etag. This will potentially replace the if match value, but not on the
            // users object.

            AccessCondition etagLockCondition = new AccessCondition();
            etagLockCondition.setIfMatch(this.getProperties().getEtag());
            if (accessCondition != null) {
                etagLockCondition.setLeaseID(accessCondition.getLeaseID());
            }

            // 1. Open Read Stream
            final BlobInputStream streamRef = this.openInputStream(etagLockCondition, options, opContext);

            // Cache value indicating if we need
            final boolean validateMd5 = streamRef.getValidateBlobMd5();

            streamRef.setValidateBlobMd5(false);
            streamRef.mark(Integer.MAX_VALUE);

            try {
                // 2. Seek to current position, this will disable read streams
                // internal content md5 checks.
                if (opContext.getCurrentOperationByteCount() > 0) {
                    // SCA will say this if a failure as we do not validate the
                    // return of skip.
                    // The Blob class repositioning a virtual pointer and will
                    // always skip the correct
                    // number of bytes.
                    streamRef.skip(opContext.getCurrentOperationByteCount());
                }

                // 3. Continue copying
                final StreamMd5AndLength descriptor = Utility.writeToOutputStream(streamRef, outStream, -1, false,
                        validateMd5, null, opContext);

                if (validateMd5 && !this.properties.getContentMD5().equals(descriptor.getMd5())) {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_MD5, String.format(
                            "Blob data corrupted (integrity check failed), Expected value is %s, retrieved %s",
                            this.properties.getContentMD5(), descriptor.getMd5()),
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
            catch (final IOException secondEx) {
                opContext.setIntermediateMD5(null);
                if (secondEx.getCause() != null && secondEx.getCause() instanceof StorageException) {
                    throw (StorageException) secondEx.getCause();
                }
                else {
                    throw secondEx;
                }
            }
        }
    }

    /**
     * Populates a blob's properties and metadata.
     * <p>
     * This method populates the blob's system properties and user-defined metadata. Before reading a blob's properties
     * or metadata, call this method or its overload to retrieve the latest values for the blob's properties and
     * metadata from the Windows Azure storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void downloadAttributes() throws StorageException {
        this.downloadAttributes(null, null, null);
    }

    /**
     * Populates a blob's properties and metadata using the specified request options and operation context.
     * <p>
     * This method populates the blob's system properties and user-defined metadata. Before reading a blob's properties
     * or metadata, call this method or its overload to retrieve the latest values for the blob's properties and
     * metadata from the Windows Azure storage service.
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

        if (options == null) {
            options = new BlobRequestOptions();
        }

        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {

                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.getProperties(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), blob.snapshotID, accessCondition, blobOptions,
                        opContext);
                this.setConnection(request);

                this.signRequest(client, request, -1L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                // Set attributes
                final BlobAttributes retrievedAttributes = BlobResponse.getAttributes(request, blob.getUri(),
                        blob.snapshotID, opContext);

                if (retrievedAttributes.getProperties().getBlobType() != blob.properties.getBlobType()) {
                    throw new StorageException(
                            StorageErrorCodeStrings.INCORRECT_BLOB_TYPE,
                            String.format(
                                    "Incorrect Blob type, please use the correct Blob type to access a blob on the server. Expected %s, actual %s",
                                    blob.properties.getBlobType(), retrievedAttributes.getProperties().getBlobType()),
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }

                blob.properties = retrievedAttributes.getProperties();
                blob.metadata = retrievedAttributes.getMetadata();
                blob.copyState = retrievedAttributes.getCopyState();

                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Downloads a range of bytes from the blob to the given byte buffer.
     * 
     * @param offset
     *            The byte offset to use as the starting point for the source.
     * @param length
     *            The number of bytes to read.
     * @param buffer
     *            The byte buffer, as an array of bytes, to which the blob bytes are downloaded.
     * @param bufferOffet
     *            The byte offset to use as the starting point for the target.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void downloadRange(final long offset, final int length, final byte[] buffer, final int bufferOffet)
            throws StorageException {
        this.downloadRangeInternal(offset, length, buffer, bufferOffet, null, null, null);
    }

    /**
     * Downloads a range of bytes from the blob to the given byte buffer, using the specified request options and
     * operation context.
     * 
     * @param offset
     *            The byte offset to use as the starting point for the source.
     * @param length
     *            The number of bytes to read.
     * @param buffer
     *            The byte buffer, as an array of bytes, to which the blob bytes are downloaded.
     * @param bufferOffet
     *            The byte offset to use as the starting point for the target.
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
    public final void downloadRange(final long offset, final int length, final byte[] buffer, final int bufferOffet,
            final AccessCondition accessCondition, final BlobRequestOptions options, final OperationContext opContext)
            throws StorageException {
        if (offset < 0 || length <= 0) {
            throw new IndexOutOfBoundsException();
        }

        Utility.assertNotNull("buffer", buffer);

        if (length + bufferOffet > buffer.length) {
            throw new IndexOutOfBoundsException();
        }

        opContext.initialize();
        this.downloadRangeInternal(offset, length, buffer, bufferOffet, accessCondition, options, opContext);
    }

    /**
     * Downloads a range of bytes from the blob to the given byte buffer.
     * 
     * @param blobOffset
     *            the offset of the blob to begin downloading at
     * @param length
     *            the number of bytes to read
     * @param buffer
     *            the byte buffer to write to.
     * @param bufferOffset
     *            the offset in the byte buffer to begin writing.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            An object that specifies any additional options for the request
     * @param opContext
     *            an object used to track the execution of the operation
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    protected final void downloadRangeInternal(final long blobOffset, final int length, final byte[] buffer,
            final int bufferOffset, final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (blobOffset < 0 || length <= 0) {
            throw new IndexOutOfBoundsException();
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        options.applyDefaults(this.blobServiceClient);

        if (options.getUseTransactionalContentMD5() && length > 4 * Constants.MB) {
            throw new IllegalArgumentException(
                    "Cannot specify x-ms-range-get-content-md5 header on ranges larger than 4 MB. Either use a BlobReadStream via openRead, or disable TransactionalMD5 checking via the BlobRequestOptions.");
        }

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.get(blob.getTransformedAddress(opContext),
                        blobOptions.getTimeoutIntervalInMs(), blob.snapshotID, blobOffset, length, accessCondition,
                        blobOptions, opContext);
                this.setConnection(request);

                if (blobOptions.getUseTransactionalContentMD5()) {
                    request.setRequestProperty(Constants.HeaderConstants.RANGE_GET_CONTENT_MD5, "true");
                }

                this.signRequest(client, request, -1L, null);

                final InputStream sourceStream = ExecutionEngine.getInputStream(request, opContext, this.getResult());

                int totalRead = 0;
                int nextRead = buffer.length - bufferOffset;
                int count = sourceStream.read(buffer, bufferOffset, nextRead);

                while (count > 0) {
                    totalRead += count;
                    nextRead = buffer.length - (bufferOffset + totalRead);

                    if (nextRead == 0) {
                        // check for case where more data is returned
                        if (sourceStream.read(new byte[1], 0, 1) != -1) {
                            throw new StorageException(
                                    StorageErrorCodeStrings.OUT_OF_RANGE_INPUT,
                                    "An incorrect number of bytes was read from the connection. The connection may have been closed",
                                    Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                        }
                    }
                    count = sourceStream.read(buffer, bufferOffset + totalRead, nextRead);
                }

                ExecutionEngine.getResponseCode(this.getResult(), request, opContext);

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_PARTIAL) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                // Do not update blob length and Content-MD5 in downloadRangeInternal API. 
                final long originalBlobLength = blob.properties.getLength();
                final String originalContentMD5 = blob.properties.getContentMD5();
                final BlobAttributes retrievedAttributes = BlobResponse.getAttributes(request, blob.getUri(),
                        blob.snapshotID, opContext);
                blob.properties = retrievedAttributes.getProperties();
                blob.metadata = retrievedAttributes.getMetadata();
                blob.copyState = retrievedAttributes.getCopyState();
                blob.properties.setContentMD5(originalContentMD5);
                blob.properties.setLength(originalBlobLength);

                final String contentLength = request.getHeaderField(Constants.HeaderConstants.CONTENT_LENGTH);
                final long expectedLength = Long.parseLong(contentLength);
                if (totalRead != expectedLength) {
                    throw new StorageException(
                            StorageErrorCodeStrings.OUT_OF_RANGE_INPUT,
                            "An incorrect number of bytes was read from the connection. The connection may have been closed",
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }

                if (blobOptions.getUseTransactionalContentMD5()) {
                    final String contentMD5 = request.getHeaderField(Constants.HeaderConstants.CONTENT_MD5);

                    try {
                        final MessageDigest digest = MessageDigest.getInstance("MD5");
                        digest.update(buffer, bufferOffset, length);
                        final String calculatedMD5 = Base64.encode(digest.digest());
                        if (!contentMD5.equals(calculatedMD5)) {
                            throw new StorageException(StorageErrorCodeStrings.INVALID_MD5, String.format(
                                    "Blob data corrupted (integrity check failed), Expected value is %s, retrieved %s",
                                    contentMD5, calculatedMD5), Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                        }
                    }
                    catch (final NoSuchAlgorithmException e) {
                        // This wont happen, throw fatal.
                        throw Utility.generateNewUnexpectedStorageException(e);
                    }
                }

                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Checks to see if the blob exists.
     * 
     * @return <code>true</code> if the blob exists, other wise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final boolean exists() throws StorageException {
        return this.exists(null, null, null);
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
     *             f a storage service error occurred.
     */
    @DoesServiceRequest
    public final boolean exists(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Boolean> impl = new StorageOperation<CloudBlobClient, CloudBlob, Boolean>(
                options) {

            @Override
            public Boolean execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.getProperties(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), blob.snapshotID, accessCondition, blobOptions,
                        opContext);
                this.setConnection(request);

                this.signRequest(client, request, -1L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_OK) {
                    final BlobAttributes retrievedAttributes = BlobResponse.getAttributes(request, blob.getUri(),
                            blob.snapshotID, opContext);
                    blob.properties = retrievedAttributes.getProperties();
                    blob.metadata = retrievedAttributes.getMetadata();
                    blob.copyState = retrievedAttributes.getCopyState();
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

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
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

        if (!this.blobServiceClient.getCredentials().canCredentialsSignRequest()) {
            throw new IllegalArgumentException(
                    "Cannot create Shared Access Signature unless the Account Key credentials are used by the BlobServiceClient.");
        }

        if (this.isSnapshot()) {
            throw new IllegalArgumentException(
                    "Cannot create Shared Access Signature for snapshots. Perform the operation on the root blob instead.");
        }

        final String resourceName = this.getCanonicalName(true);

        final String signature = SharedAccessSignatureHelper.generateSharedAccessSignatureHash(policy,
                groupPolicyIdentifier, resourceName, this.blobServiceClient, null);

        final UriQueryBuilder builder = SharedAccessSignatureHelper.generateSharedAccessSignature(policy,
                groupPolicyIdentifier, "b", signature);

        return builder.toString();
    }

    /**
     * Returns the canonical name of the blob in the format of
     * <i>/&lt;account-name&gt;/&lt;container-name&gt;/&lt;blob-name&gt;</i>.
     * <p>
     * This format is used by both Shared Access and Copy blob operations.
     * 
     * @param ignoreSnapshotTime
     *            <code>true</code> if the snapshot time is ignored; otherwise, <code>false</code>.
     * 
     * @return The canonical name in the format of <i>/&lt;account-name&gt;/&lt;container
     *         -name&gt;/&lt;blob-name&gt;</i>.
     */
    String getCanonicalName(final boolean ignoreSnapshotTime) {
        String canonicalName;
        if (this.blobServiceClient.isUsePathStyleUris()) {
            canonicalName = this.getUri().getRawPath();
        }
        else {
            canonicalName = PathUtility.getCanonicalPathFromCredentials(this.blobServiceClient.getCredentials(), this
                    .getUri().getRawPath());
        }

        if (!ignoreSnapshotTime && this.snapshotID != null) {
            canonicalName = canonicalName.concat("?snapshot=");
            canonicalName = canonicalName.concat(this.snapshotID);
        }

        return canonicalName;
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
            final URI containerURI = PathUtility.getContainerURI(this.getUri(),
                    this.blobServiceClient.isUsePathStyleUris());
            this.container = new CloudBlobContainer(containerURI, this.blobServiceClient);
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
        if (Utility.isNullOrEmpty(this.name)) {
            this.name = PathUtility.getBlobNameFromURI(this.getUri(), this.blobServiceClient.isUsePathStyleUris());
        }
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
            final URI parentURI = PathUtility.getParentAddress(this.getUri(),
                    this.blobServiceClient.getDirectoryDelimiter(), this.blobServiceClient.isUsePathStyleUris());
            this.parent = new CloudBlobDirectory(parentURI, null, this.blobServiceClient);
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
        return this.copyState;
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
        else if (this.blobServiceClient.getCredentials() instanceof StorageCredentialsSharedAccessSignature) {
            return this.blobServiceClient.getCredentials().transformUri(this.getUri());
        }
        else {
            return this.getUri();
        }
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
     * @return the Blob Snapshot ID.
     */
    public final String getSnapshotID() {
        return this.snapshotID;
    }

    /**
     * Returns the transformed URI for the resource if the given credentials require transformation.
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A <code>java.net.URI</code> object that represents the transformed URI.
     * 
     * @throws IllegalArgumentException
     *             If the URI is not absolute.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    protected final URI getTransformedAddress(final OperationContext opContext) throws URISyntaxException,
            StorageException {
        if (this.blobServiceClient.getCredentials().doCredentialsNeedTransformUri()) {
            if (this.getUri().isAbsolute()) {
                return this.blobServiceClient.getCredentials().transformUri(this.getUri(), opContext);
            }
            else {
                final StorageException ex = Utility.generateNewUnexpectedStorageException(null);
                ex.getExtendedErrorInformation().setErrorMessage("Blob Object relative URIs not supported.");
                throw ex;
            }
        }
        else {
            return this.getUri();
        }
    }

    /**
     * Returns the URI for this blob.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI for the blob.
     */
    @Override
    public final URI getUri() {
        return this.uri;
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
     * Use {@link CloudBlobClient#setStreamMinimumReadSizeInBytes} to configure the read size.
     * 
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final BlobInputStream openInputStream() throws StorageException {
        return this.openInputStream(null, null, null);
    }

    /**
     * Opens a blob input stream to download the blob using the specified request options and operation context.
     * <p>
     * Use {@link CloudBlobClient#setStreamMinimumReadSizeInBytes} to configure the read size.
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

        if (options == null) {
            options = new BlobRequestOptions();
        }

        assertNoWriteOperationForSnapshot();

        options.applyDefaults(this.blobServiceClient);

        return new BlobInputStream(this, accessCondition, options, opContext);
    }

    /**
     * Parse Uri for SAS (Shared access signature) information.
     * 
     * Validate that no other query parameters are passed in. Any SAS information will be recorded as corresponding
     * credentials instance. If existingClient is passed in, any SAS information found will not be supported. Otherwise
     * a new client is created based on SAS information or as anonymous credentials.
     * 
     * @param completeUri
     *            The complete Uri.
     * @param existingClient
     *            The client to use.
     * @param usePathStyleUris
     *            If true, path style Uris are used.
     * @throws StorageException
     *             If a storage service error occurred.
     * */
    protected void parseURIQueryStringAndVerify(final URI completeUri, final CloudBlobClient existingClient,
            final boolean usePathStyleUris) throws StorageException {
        Utility.assertNotNull("resourceUri", completeUri);

        if (!completeUri.isAbsolute()) {
            final String errorMessage = String.format(
                    "Address '%s' is not an absolute address. Relative addresses are not permitted in here.",
                    completeUri.toString());
            throw new IllegalArgumentException(errorMessage);
        }

        this.uri = PathUtility.stripURIQueryAndFragment(completeUri);

        final HashMap<String, String[]> queryParameters = PathUtility.parseQueryString(completeUri.getQuery());

        final StorageCredentialsSharedAccessSignature sasCreds = SharedAccessSignatureHelper
                .parseQuery(queryParameters);

        final String[] snapshotIDs = queryParameters.get(BlobConstants.SNAPSHOT);
        if (snapshotIDs != null && snapshotIDs.length > 0) {
            this.snapshotID = snapshotIDs[0];
        }

        if (sasCreds == null) {
            return;
        }

        final Boolean sameCredentials = existingClient == null ? false : Utility.areCredentialsEqual(sasCreds,
                existingClient.getCredentials());

        if (existingClient == null || !sameCredentials) {
            try {
                this.blobServiceClient = new CloudBlobClient(new URI(PathUtility.getServiceClientBaseAddress(
                        this.getUri(), usePathStyleUris)), sasCreds);
            }
            catch (final URISyntaxException e) {
                throw Utility.generateNewUnexpectedStorageException(e);
            }
        }

        if (existingClient != null && !sameCredentials) {
            this.blobServiceClient
                    .setPageBlobStreamWriteSizeInBytes(existingClient.getPageBlobStreamWriteSizeInBytes());
            this.blobServiceClient.setSingleBlobPutThresholdInBytes(existingClient.getSingleBlobPutThresholdInBytes());
            this.blobServiceClient.setStreamMinimumReadSizeInBytes(existingClient.getStreamMinimumReadSizeInBytes());
            this.blobServiceClient.setWriteBlockSizeInBytes(existingClient.getWriteBlockSizeInBytes());
            this.blobServiceClient.setConcurrentRequestCount(existingClient.getConcurrentRequestCount());
            this.blobServiceClient.setDirectoryDelimiter(existingClient.getDirectoryDelimiter());
            this.blobServiceClient.setRetryPolicyFactory(existingClient.getRetryPolicyFactory());
            this.blobServiceClient.setTimeoutInMs(existingClient.getTimeoutInMs());
        }
    }

    void updateEtagAndLastModifiedFromResponse(HttpURLConnection request) {
        String tempStr = request.getHeaderField(Constants.HeaderConstants.ETAG);

        // ETag
        if (!Utility.isNullOrEmpty(tempStr)) {
            this.getProperties().setEtag(tempStr);
        }

        // Last Modified
        if (0 != request.getLastModified()) {
            final Calendar lastModifiedCalendar = Calendar.getInstance(Utility.LOCALE_US);
            lastModifiedCalendar.setTimeZone(Utility.UTC_ZONE);
            lastModifiedCalendar.setTime(new Date(request.getLastModified()));
            this.getProperties().setLastModified(lastModifiedCalendar.getTime());
        }

    }

    void updateLengthFromResponse(HttpURLConnection request) {
        final String xContentLengthHeader = request.getHeaderField(BlobConstants.CONTENT_LENGTH_HEADER);
        if (!Utility.isNullOrEmpty(xContentLengthHeader)) {
            this.getProperties().setLength(Long.parseLong(xContentLengthHeader));
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
        this.releaseLease(accessCondition, null, null);
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

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.lease(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), LeaseAction.RELEASE, null, null, null,
                        accessCondition, blobOptions, opContext);
                this.setConnection(request);

                this.signRequest(client, request, 0L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                blob.properties.setLeaseStatus(LeaseStatus.UNLOCKED);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
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
        this.renewLease(accessCondition, null, null);
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

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.lease(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), LeaseAction.RENEW, null, null, null,
                        accessCondition, blobOptions, opContext);
                this.setConnection(request);

                this.signRequest(client, request, 0L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Changes the existing lease ID to the proposed lease ID.
     * 
     * @param proposedLeaseId
     *            A <code>String</code> that represents the proposed lease ID for the new lease,
     *            or null if no lease ID is proposed.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob. The lease ID is
     *            required to be set with an access condition.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void changeLease(final String proposedLeaseId, final AccessCondition accessCondition)
            throws StorageException {
        this.changeLease(proposedLeaseId, accessCondition, null, null);
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
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void changeLease(final String proposedLeaseId, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        Utility.assertNotNull("accessCondition", accessCondition);
        Utility.assertNotNullOrEmpty("leaseID", accessCondition.getLeaseID());

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.lease(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), LeaseAction.CHANGE, null, proposedLeaseId, null,
                        accessCondition, blobOptions, opContext);
                this.setConnection(request);

                this.signRequest(client, request, 0L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
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
     * Reserved for internal use.
     * 
     * @param copyState
     *            the copyState to set
     */
    public void setCopyState(final CopyState copyState) {
        this.copyState = copyState;
    }

    /**
     * Sets the blob snapshot ID.
     * 
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot ID being assigned to the blob.
     */
    public final void setSnapshotID(final String snapshotID) {
        this.snapshotID = snapshotID;
    }

    /**
     * Attempts to break the lease and ensure that another client cannot acquire a new lease until the current lease
     * period has expired.
     * 
     * @return Time, in seconds, remaining in the lease period, or -1 if the lease is already broken.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final long tryBreakLease() throws StorageException {
        return this.tryBreakLease(null, null, null);
    }

    /**
     * Attempts to breaks the lease using the specified request options and operation context, and ensure that another
     * client cannot acquire a new lease until the current lease period has expired.
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
     * @return Time, in seconds, remaining in the lease period, -1 if the lease is already broken.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final long tryBreakLease(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Long> impl = new StorageOperation<CloudBlobClient, CloudBlob, Long>(
                options) {
            @Override
            public Long execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.lease(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), LeaseAction.BREAK, null, null, null,
                        accessCondition, blobOptions, opContext);
                this.setConnection(request);

                this.signRequest(client, request, 0L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_CONFLICT) {
                    final StorageException potentialConflictException = StorageException.translateException(request,
                            null, opContext);

                    StorageExtendedErrorInformation extendedInfo = potentialConflictException
                            .getExtendedErrorInformation();

                    if (extendedInfo == null) {
                        // If we cant validate the error then the error must be surfaced to the user.
                        throw potentialConflictException;
                    }

                    if (!extendedInfo.getErrorCode().equals(StorageErrorCodeStrings.LEASE_ALREADY_BROKEN)) {
                        this.setException(potentialConflictException);
                        this.setNonExceptionedRetryableFailure(true);
                    }

                    return -1L;
                }

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return -1L;
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                final String leaseTime = BlobResponse.getLeaseTime(request, opContext);

                return Utility.isNullOrEmpty(leaseTime) ? -1L : Long.parseLong(leaseTime);
            }
        };

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Uploads the source stream data to the blob.
     * 
     * @param sourceStream
     *            An <code>InputStream</code> object that represents the source stream to upload.
     * 
     * @param length
     *            The length of the stream data in bytes, or -1 if unknown. The length must be greater than zero and a
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
     * Uploads a blob in a single operation.
     * 
     * @param sourceStream
     *            A <code>InputStream</code> object that represents the source stream to upload.
     * @param length
     *            The length, in bytes, of the stream, or -1 if unknown.
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
    protected final void uploadFullBlob(final InputStream sourceStream, final long length,
            final AccessCondition accessCondition, final BlobRequestOptions options, final OperationContext opContext)
            throws StorageException, IOException {
        assertNoWriteOperationForSnapshot();

        // Mark sourceStream for current position.
        sourceStream.mark(Constants.MAX_MARK_LENGTH);

        if (length < 0 || length > BlobConstants.MAX_SINGLE_UPLOAD_BLOB_SIZE_IN_BYTES) {
            throw new IllegalArgumentException(String.format(
                    "Invalid stream length; stream must be between 0 and %s MB in length.",
                    BlobConstants.MAX_SINGLE_UPLOAD_BLOB_SIZE_IN_BYTES / Constants.MB));
        }

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {

            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.put(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), blob.properties, blob.properties.getBlobType(),
                        0, accessCondition, blobOptions, opContext);
                this.setConnection(request);

                BlobRequest.addMetadata(request, blob.metadata, opContext);

                this.signRequest(client, request, length, null);

                final StreamMd5AndLength descriptor = Utility.writeToOutputStream(sourceStream,
                        request.getOutputStream(), length, true, false, null, opContext);

                if (length != descriptor.getLength()) {
                    throw new StorageException(
                            StorageErrorCodeStrings.INVALID_INPUT,
                            "An incorrect stream length was specified, resulting in an authentication failure. Please specify correct length, or -1.",
                            HttpURLConnection.HTTP_FORBIDDEN, null, null);
                }

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Uploads the blob's metadata to the storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void uploadMetadata() throws StorageException {
        this.uploadMetadata(null, null, null);
    }

    /**
     * Uploads the blob's metadata to the storage service using the specified lease ID, request options, and operation
     * context.
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

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.setMetadata(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), accessCondition, blobOptions, opContext);
                this.setConnection(request);

                BlobRequest.addMetadata(request, blob.metadata, opContext);

                this.signRequest(client, request, 0L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Updates the blob's properties to the storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void uploadProperties() throws StorageException {
        this.uploadProperties(null, null, null);
    }

    /**
     * Updates the blob's properties using the specified lease ID, request options, and operation context.
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

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.setProperties(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), blob.properties, null, accessCondition,
                        blobOptions, opContext);
                this.setConnection(request);

                BlobRequest.addMetadata(request, blob.metadata, opContext);

                this.signRequest(client, request, 0L, null);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Asserts that write operation is not done for snapshot.
     */
    protected void assertNoWriteOperationForSnapshot() {
        if (isSnapshot()) {
            throw new IllegalArgumentException("Cannot perform this operation on a blob representing a snapshot.");
        }
    }
}
