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
package com.microsoft.azure.storage.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.IPRange;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.SharedAccessProtocols;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageErrorCode;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageLocation;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.core.Base64;
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
 * Represents a Microsoft Azure File.
 */
public final class CloudFile implements ListFileItem {
    /**
     * Holds the number of bytes to buffer when writing to a {@link FileOutputStream}.
     */
    protected int streamWriteSizeInBytes = Constants.DEFAULT_STREAM_WRITE_IN_BYTES;

    /**
     * Holds the minimum read size when using a {@link FileInputStream}.
     */
    protected int streamMinimumReadSizeInBytes = Constants.DEFAULT_MINIMUM_READ_SIZE_IN_BYTES;

    /**
     * Holds the file's share reference.
     */
    private CloudFileShare share;

    /**
     * Represents the file's directory reference.
     */
    protected CloudFileDirectory parent;

    /**
     * Holds the file's name.
     */
    private String name;

    /**
     * Represents the file service client.
     */
    protected CloudFileClient fileServiceClient;

    /**
     * Holds the metadata for the file.
     */
    private HashMap<String, String> metadata = new HashMap<String, String>();

    /**
     * Holds the properties of the file.
     */
    private FileProperties properties = new FileProperties();

    /**
     * Stores the absolute URI to the file.
     */
    private StorageUri storageUri;

    /**
     * Creates an instance of the <code>CloudFile</code> class using the specified absolute URI.
     * 
     * @param fileAbsoluteUri
     *            A <code>java.net.URI</code> object that represents the absolute URI to the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudFile(final URI fileAbsoluteUri) throws StorageException, URISyntaxException {
        this(new StorageUri(fileAbsoluteUri));
    }

    /**
     * Creates an instance of the <code>CloudFile</code> class using the specified absolute StorageUri.
     * 
     * @param fileAbsoluteUri
     *            A {@link StorageUri} object that represents the absolute URI to the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudFile(final StorageUri fileAbsoluteUri) throws StorageException, URISyntaxException {
        this(fileAbsoluteUri, null);
    }
    
    /**
     * Creates an instance of the <code>CloudFile</code> class using the specified absolute URI 
     * and credentials.
     * 
     * @param fileAbsoluteUri
     *            A <code>java.net.URI</code> object that represents the absolute URI to the file.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudFile(final URI fileAbsoluteUri, final StorageCredentials credentials) throws StorageException {
        this(new StorageUri(fileAbsoluteUri), credentials);
    }

    /**
     * Creates an instance of the <code>CloudFile</code> class using the specified absolute StorageUri 
     * and credentials.
     * 
     * @param fileAbsoluteUri
     *            A {@link StorageUri} object that represents the absolute URI to the file.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudFile(final StorageUri fileAbsoluteUri, final StorageCredentials credentials) throws StorageException {    
        this.parseQueryAndVerify(fileAbsoluteUri, credentials);
    }

    /**
     * Creates an instance of the <code>CloudFile</code> class by copying values from another cloud file.
     * 
     * @param otherFile
     *            A <code>CloudFile</code> object that represents the file to copy.
     */
    public CloudFile(final CloudFile otherFile) {
        this.metadata = new HashMap<String, String>();
        this.properties = new FileProperties(otherFile.properties);
        if (otherFile.metadata != null) {
            for (final String key : otherFile.metadata.keySet()) {
                this.metadata.put(key, otherFile.metadata.get(key));
            }
        }

        this.storageUri = otherFile.storageUri;
        this.share = otherFile.share;
        this.parent = otherFile.parent;
        this.fileServiceClient = otherFile.fileServiceClient;
        this.name = otherFile.name;
        this.setStreamMinimumReadSizeInBytes(otherFile.getStreamMinimumReadSizeInBytes());
        this.setStreamWriteSizeInBytes(otherFile.getStreamWriteSizeInBytes());
    }
    
    /**
     * Creates an instance of the <code>CloudFile</code> class using the specified address, share,
     * and client.
     * 
     * @param uri
     *            A {@link StorageUri} that represents the file directory's address.
     * @param fileName
     *            A <code>String</code> that represents the name of the file.
     * @param share
     *            A {@link CloudFileShare} object that represents the associated file share.
     */
    protected CloudFile(final StorageUri uri, final String fileName, final CloudFileShare share) {
        Utility.assertNotNull("uri", uri);
        Utility.assertNotNull("fileName", fileName);
        Utility.assertNotNull("share", share);

        this.name = fileName;
        this.fileServiceClient = share.getServiceClient();
        this.share = share;
        this.storageUri = uri;
    }

    /**
     * Aborts an ongoing Azure File copy operation.
     *
     * @param copyId
     *            A <code>String</code> object that identifies the copy operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void abortCopy(final String copyId) throws StorageException {
        this.abortCopy(copyId, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Aborts an ongoing Azure File copy operation.
     *
     * @param copyId
     *            A <code>String</code> object that identifies the copy operation.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the Azure File.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void abortCopy(final String copyId, final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.abortCopyImpl(copyId, accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFile, Void> abortCopyImpl(final String copyId,
            final AccessCondition accessCondition, final FileRequestOptions options) {
        Utility.assertNotNull("copyId", copyId);

        final StorageRequest<CloudFileClient, CloudFile, Void> putRequest =
                new StorageRequest<CloudFileClient, CloudFile, Void>(options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFile file, OperationContext context)
                    throws Exception {
                return FileRequest.abortCopy(file.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, copyId);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0, context);
            }

            @Override
            public Void preProcessResponse(CloudFile parentObject, CloudFileClient client, OperationContext context)
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
     * Requests the service to start copying a blob's contents, properties, and metadata to a new file.
     *
     * @param sourceBlob
     *            A <code>CloudBlob</code> object that represents the source blob to copy.
     *
     * @return A <code>String</code> which represents the copy ID associated with the copy operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     */
    @DoesServiceRequest
    public final String startCopy(final CloudBlob sourceBlob) throws StorageException, URISyntaxException {
        return this.startCopy(sourceBlob, null /* sourceAccessCondition */,
                null /* destinationAccessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Requests the service to start copying a file's contents, properties, and metadata to a new file,
     * using the specified access conditions, lease ID, request options, and operation context.
     *
     * @param sourceBlob
     *            A <code>CloudBlob</code> object that represents the source blob to copy.
     * @param sourceAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the source blob.
     * @param destinationAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the destination file.
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
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *
     */
    @DoesServiceRequest
    public final String startCopy(final CloudBlob sourceBlob, final AccessCondition sourceAccessCondition,
            final AccessCondition destinationAccessCondition, FileRequestOptions options, OperationContext opContext)
            throws StorageException, URISyntaxException {
        Utility.assertNotNull("sourceBlob", sourceBlob);
        return this.startCopy(
                sourceBlob.getQualifiedUri(), sourceAccessCondition, destinationAccessCondition, options, opContext);
    }

    /**
     * Requests the service to start copying an Azure File's contents, properties, and metadata to a new Azure File.
     *
     * @param sourceFile
     *            A <code>CloudFile</code> object that represents the source Azure File to copy.
     *
     * @return A <code>String</code> which represents the copy ID associated with the copy operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     */
    @DoesServiceRequest
    public final String startCopy(final CloudFile sourceFile) throws StorageException, URISyntaxException {
        return this.startCopy(sourceFile, null /* sourceAccessCondition */,
                null /* destinationAccessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Requests the service to start copying an Azure File's contents, properties, and metadata to a new Azure File,
     * using the specified access conditions, lease ID, request options, and operation context.
     *
     * @param sourceFile
     *            A <code>CloudFile</code> object that represents the source file to copy.
     * @param sourceAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the source.
     * @param destinationAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the destination.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request.
     *            Specifying <code>null</code> will use the default request options from the associated
     *            service client ({@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation.
     *            This object is used to track requests to the storage service, and to provide additional
     *            runtime information about the operation.
     *
     * @return A <code>String</code> which represents the copy ID associated with the copy operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *
     */
    @DoesServiceRequest
    public final String startCopy(final CloudFile sourceFile, final AccessCondition sourceAccessCondition,
            final AccessCondition destinationAccessCondition, FileRequestOptions options, OperationContext opContext)
            throws StorageException, URISyntaxException {
        Utility.assertNotNull("sourceFile", sourceFile);
        return this.startCopy(sourceFile.getTransformedAddress(opContext).getPrimaryUri(),
                sourceAccessCondition, destinationAccessCondition, options, opContext);

    }

    /**
     * Requests the service to start copying a URI's contents, properties, and metadata to a new Azure File.
     *
     * @param source
     *            The source's <code>java.net.URI</code>.
     *
     * @return A <code>String</code> which represents the copy ID associated with the copy operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final String startCopy(final URI source) throws StorageException {
        return this.startCopy(source, null /* sourceAccessCondition */,
                null /* destinationAccessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Requests the service to start copying a URI's contents, properties, and metadata to a new Azure File,
     * using the specified access conditions, lease ID, request options, and operation context.
     *
     * @param source
     *            The source's <code>java.net.URI</code>.
     * @param sourceAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the source.
     * @param destinationAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the destination.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request.
     *            Specifying <code>null</code> will use the default request options from the associated
     *            service client ({@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation.
     *            This object is used to track requests to the storage service, and to provide additional
     *            runtime information about the operation.
     *
     * @return A <code>String</code> which represents the copy ID associated with the copy operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     *
     */
    @DoesServiceRequest
    public final String startCopy(final URI source, final AccessCondition sourceAccessCondition,
            final AccessCondition destinationAccessCondition, FileRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        return ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.startCopyImpl(source, sourceAccessCondition, destinationAccessCondition, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFile, String> startCopyImpl(final URI source,
            final AccessCondition sourceAccessCondition, final AccessCondition destinationAccessCondition,
            final FileRequestOptions options) {

        if (sourceAccessCondition != null && !Utility.isNullOrEmpty(sourceAccessCondition.getLeaseID())) {
            throw new IllegalArgumentException(SR.LEASE_CONDITION_ON_SOURCE);
        }

        final StorageRequest<CloudFileClient, CloudFile, String> putRequest =
                new StorageRequest<CloudFileClient, CloudFile, String>(options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFile file, OperationContext context)
                    throws Exception {
                return FileRequest.copyFrom(file.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, sourceAccessCondition, destinationAccessCondition, source.toASCIIString());
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudFile file, OperationContext context) {
                FileRequest.addMetadata(connection, file.metadata, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0, context);
            }

            @Override
            public String preProcessResponse(CloudFile file, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                file.updateEtagAndLastModifiedFromResponse(this.getConnection());
                file.properties.setCopyState(FileResponse.getCopyState(this.getConnection()));

                return file.properties.getCopyState().getCopyId();
            }
        };

        return putRequest;
    }

    /**
     * Clears a range from a file.
     * <p>
     * Calling <code>clearRange</code> releases the storage space used by the specified range. Ranges that have been
     * cleared are no longer tracked as part of the file.
     * 
     * @param offset
     *            The offset, in bytes, at which to begin clearing.
     * @param length
     *            The length, in bytes, of the data range to be cleared.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void clearRange(final long offset, final long length) throws StorageException {
        this.clearRange(offset, length, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Clears a range from a file using the specified lease ID, request options, and operation context.
     * <p>
     * Calling <code>clearRange</code> releases the storage space used by the specified range. Ranges that have been
     * cleared are no longer tracked as part of the file.
     * 
     * @param offset
     *            A <code>long</code> which represents the offset, in bytes, at which to begin clearing.
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the data range to be cleared.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void clearRange(final long offset, final long length, final AccessCondition accessCondition,
            FileRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        final FileRange range = new FileRange(offset, offset + length - 1);

        this.putRangeInternal(range, FileRangeOperationType.CLEAR, null, length, null, accessCondition, options,
                opContext);
    }

    /**
     * Creates a file. If the file already exists, this will replace it.
     * 
     * @param size
     *            A <code>long</code> which represents the size, in bytes, of the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void create(final long size) throws StorageException {
        this.create(size, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Creates a file using the specified access condition, request options and operation context. If the file already 
     * exists, this will replace it.
     * 
     * @param size
     *            A <code>long</code> which represents the size, in bytes, of the file.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void create(final long size, final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {

        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this, this.createImpl(size, accessCondition, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFile, Void> createImpl(final long size,
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFile, Void> putRequest =
                new StorageRequest<CloudFileClient, CloudFile, Void>(options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFile file, OperationContext context)
                    throws Exception {
                return FileRequest.putFile(file.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, file.properties, size);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudFile file, OperationContext context) {
                FileRequest.addMetadata(connection, file.metadata, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudFile file, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                file.updateEtagAndLastModifiedFromResponse(this.getConnection());
                return null;
            }

        };

        return putRequest;
    }

    /**
     * Deletes the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void delete() throws StorageException {
        this.delete(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Deletes the file using the specified access condition, request options, and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void delete(final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this, this.deleteImpl(accessCondition, options),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Deletes the file if it exists.
     * 
     * @return <code>true</code> if the file was deleted; otherwise, <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * 
     */
    @DoesServiceRequest
    public final boolean deleteIfExists() throws StorageException {
        return this.deleteIfExists(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Deletes the file if it exists, using the specified access condition, request options, and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>true</code> if the file existed and was deleted; otherwise, <code>false</code>
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final boolean deleteIfExists(final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        boolean exists = this.exists(true, accessCondition, options, opContext);
        if (exists) {
            try {
                this.delete(accessCondition, options, opContext);
                return true;
            }
            catch (StorageException e) {
                if (e.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND
                        && StorageErrorCodeStrings.RESOURCE_NOT_FOUND.equals(e.getErrorCode())) {
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

    private StorageRequest<CloudFileClient, CloudFile, Void> deleteImpl(final AccessCondition accessCondition,
            final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFile, Void> deleteRequest =
                new StorageRequest<CloudFileClient, CloudFile, Void>(options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFile file, OperationContext context)
                    throws Exception {
                return FileRequest.deleteFile(file.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Void preProcessResponse(CloudFile file, CloudFileClient client, OperationContext context)
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
     * Downloads the contents of a file to a stream.
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
     * Downloads the contents of a file to a stream using the specified request options and operation context.
     * 
     * @param outStream
     *            An <code>OutputStream</code> object that represents the target stream.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void download(final OutputStream outStream, final AccessCondition accessCondition,
            FileRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this, this.downloadToStreamImpl(
                null /* fileOffset */, null /* length */, outStream, accessCondition, options, opContext), options
                .getRetryPolicyFactory(), opContext);
    }

    /**
     * Downloads the contents of a file to a stream.
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
     * Downloads the contents of a file to a stream using the specified request options and operation context.
     * 
     * @param offset
     *            A <code>long</code> which represents the offset to use as the starting point for the source.
     * @param length
     *            A <code>Long</code> which represents the number of bytes to read or <code>null</code>.
     * @param outStream
     *            An <code>{@link OutputStream}</code> object that represents the target stream.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void downloadRange(final long offset, final Long length, final OutputStream outStream,
            final AccessCondition accessCondition, FileRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (offset < 0 || (length != null && length <= 0)) {
            throw new IndexOutOfBoundsException();
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.downloadToStreamImpl(offset, length, outStream, accessCondition, options, opContext),
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Downloads a range of bytes from the file to the given byte buffer.
     * 
     * @param fileOffset
     *            A <code>long</code> which represents the offset within the file to begin downloading.
     * @param length
     *            A <code>Long</code> which represents the number of bytes to read.
     * @param buffer
     *            A <code>byte</code> array which represents the buffer to write to.
     * @param bufferOffset
     *            An <code>int</code> which represents the offset in the byte buffer to begin writing.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @throws StorageException
     *             an exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    protected final int downloadRangeInternal(final long fileOffset, final Long length, final byte[] buffer,
            final int bufferOffset, final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {

        if (bufferOffset < 0 || fileOffset < 0 || (length != null && length <= 0)) {
            throw new IndexOutOfBoundsException();
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);
        
        WrappedByteArrayOutputStream outputStream = new WrappedByteArrayOutputStream(buffer, bufferOffset);
        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.downloadToStreamImpl(fileOffset, length, outputStream, accessCondition, options, opContext),
                options.getRetryPolicyFactory(), opContext);
        return outputStream.getPosition();
    }

    /**
     * Downloads a range of bytes from the file to the given byte buffer.
     * 
     * @param offset
     *            A <code>long</code> which represents the byte offset to use as the starting point for the source.
     * @param length
     *            A <code>Long</code> which represents the number of bytes to read or null.
     * @param buffer
     *            A <code>byte</code> array which represents the buffer to which the file bytes are downloaded.
     * @param bufferOffset
     *            An <code>int</code> which represents the byte offset to use as the starting point for the target.
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
     * Downloads a range of bytes from the file to the given byte buffer, using the specified request options and
     * operation context.
     * 
     * @param offset
     *            A <code>long</code> which represents the byte offset to use as the starting point for the source.
     * @param length
     *            A <code>Long</code> which represents the number of bytes to read or <code>null</code>.
     * @param buffer
     *            A <code>byte</code> array which represents the buffer to which the file bytes are downloaded.
     * @param bufferOffset
     *            An <code>int</code> which represents the byte offset to use as the starting point for the target.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final int downloadRangeToByteArray(final long offset, final Long length, final byte[] buffer,
            final int bufferOffset, final AccessCondition accessCondition, FileRequestOptions options,
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
     * Downloads a range of bytes from the file to the given byte buffer.
     * 
     * @param buffer
     *            A <code>byte</code> array which represents the buffer to which the file bytes are downloaded.
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
     * Downloads a range of bytes from the file to the given byte buffer, using the specified request options and
     * operation context.
     * 
     * @param buffer
     *            A <code>byte</code> array which represents the buffer to which the file bytes are downloaded.
     * @param bufferOffset
     *            A <code>long</code> which represents the byte offset to use as the starting point for the target.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
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
            final AccessCondition accessCondition, FileRequestOptions options, OperationContext opContext)
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
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);
        
        WrappedByteArrayOutputStream outputStream = new WrappedByteArrayOutputStream(buffer, bufferOffset);
        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.downloadToStreamImpl(null, null, outputStream, accessCondition, options, opContext),
                options.getRetryPolicyFactory(), opContext);
        return outputStream.getPosition();
    }

    /**
     * Downloads a file.
     * 
     * @param path
     *            A <code>String</code> which represents the path to the file that will be created.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public void downloadToFile(final String path) throws StorageException, IOException {
        downloadToFile(path, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Downloads a file.
     * 
     * @param path
     *            A <code>String</code> which represents the path to the file that will be created.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public void downloadToFile(final String path, final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException, IOException {
        OutputStream outputStream = new BufferedOutputStream(new java.io.FileOutputStream(path));
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
     * Downloads a file to a string using the platform's default encoding.
     * 
     * @return A <code>String</code> which represents the file's contents.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public String downloadText() throws StorageException, IOException {
        return this
                .downloadText(null /* charsetName */, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Downloads a file to a string using the specified encoding.
     * 
     * @param charsetName
     *            A <code>String</code> which represents the name of the charset to use to encode the content.
     *            If null, the platform's default encoding is used.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A <code>String</code> which represents the file's contents.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public String downloadText(final String charsetName, final AccessCondition accessCondition,
            FileRequestOptions options, OperationContext opContext) throws StorageException, IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.download(baos, accessCondition, options, opContext);
        return charsetName == null ? baos.toString() : baos.toString(charsetName);
    }

    /**
     * Returns a collection of file ranges and their starting and ending byte offsets.
     * <p>
     * The start and end byte offsets for each file range are inclusive.
     * 
     * @return An <code>ArrayList</code> object which represents the set of file ranges and their starting and ending
     *         byte offsets.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ArrayList<FileRange> downloadFileRanges() throws StorageException {
        return this.downloadFileRanges(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Returns a collection of file ranges and their starting and ending byte offsets using the specified request
     * options and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return An <code>ArrayList</code> object which represents the set of file ranges and their starting
     *            and ending byte offsets.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ArrayList<FileRange> downloadFileRanges(final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        return ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.downloadFileRangesImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFile, ArrayList<FileRange>> downloadFileRangesImpl(
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFile, ArrayList<FileRange>> getRequest =
                new StorageRequest<CloudFileClient, CloudFile, ArrayList<FileRange>>(options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFile file, OperationContext context)
                    throws Exception {
                return FileRequest.getFileRanges(file.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public ArrayList<FileRange> preProcessResponse(CloudFile parentObject, CloudFileClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }

            @Override
            public ArrayList<FileRange> postProcessResponse(HttpURLConnection connection, CloudFile file,
                    CloudFileClient client, OperationContext context, ArrayList<FileRange> storageObject)
                    throws Exception {
                file.updateEtagAndLastModifiedFromResponse(this.getConnection());
                file.updateLengthFromResponse(this.getConnection());

                return FileRangeHandler.getFileRanges(this.getConnection().getInputStream());
            }

        };

        return getRequest;
    }

    @DoesServiceRequest
    private final StorageRequest<CloudFileClient, CloudFile, Integer> downloadToStreamImpl(final Long fileOffset,
            final Long length, final OutputStream outStream, final AccessCondition accessCondition,
            final FileRequestOptions options, OperationContext opContext) {

        final long startingOffset = fileOffset == null ? 0 : fileOffset;
        final boolean isRangeGet = fileOffset != null;
        final StorageRequest<CloudFileClient, CloudFile, Integer> getRequest = new StorageRequest<CloudFileClient, CloudFile, Integer>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFile file, OperationContext context)
                    throws Exception {

                // The first time this is called, we have to set the length and offset.
                // On retries, these will already have values and need not be called.
                if (this.getOffset() == null) {
                    this.setOffset(fileOffset);
                }

                if (this.getLength() == null) {
                    this.setLength(length);
                }

                // Only do this when we have support from the service.
                // AccessCondition tempCondition = (this.getETagLockCondition() != null) ? this.getETagLockCondition()
                //         : accessCondition;

                return FileRequest.getFile(file.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, this.getOffset(), this.getLength(),
                        (options.getUseTransactionalContentMD5() && !this.getArePropertiesPopulated()));
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Integer preProcessResponse(CloudFile file, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_PARTIAL
                        && this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                if (!this.getArePropertiesPopulated()) {
                    String originalContentMD5 = null;

                    final FileAttributes retrievedAttributes = FileResponse.getFileAttributes(this.getConnection(),
                            file.getStorageUri());

                    // Do not update Content-MD5 if it is a range get. 
                    if (isRangeGet) {
                        originalContentMD5 = file.properties.getContentMD5();
                    }
                    else {
                        originalContentMD5 = retrievedAttributes.getProperties().getContentMD5();
                    }

                    if (!options.getDisableContentMD5Validation() && options.getUseTransactionalContentMD5()
                            && Utility.isNullOrEmpty(retrievedAttributes.getProperties().getContentMD5())) {
                        throw new StorageException(StorageErrorCodeStrings.MISSING_MD5_HEADER, SR.MISSING_MD5,
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }

                    file.properties = retrievedAttributes.getProperties();
                    file.metadata = retrievedAttributes.getMetadata();
                    this.setContentMD5(retrievedAttributes.getProperties().getContentMD5());
                    file.properties.setContentMD5(originalContentMD5);
                    this.setLockedETag(file.properties.getEtag());
                    this.setArePropertiesPopulated(true);
                }
                else {
                    if (this.getLockedETag() != null) {
                        if (!this.getLockedETag().equals(file.properties.getEtag())) {
                            throw new StorageException(StorageErrorCode.CONDITION_FAILED.toString(),
                                    SR.INVALID_CONDITIONAL_HEADERS, HttpURLConnection.HTTP_PRECON_FAILED, null, null);
                        }
                    }
                }

                // If the download fails and we need to resume the download, going to the
                // same storage location is important to prevent a possible ETag mismatch.
                this.setRequestLocationMode((this.getResult().getTargetLocation() == StorageLocation.PRIMARY) ?
                        RequestLocationMode.PRIMARY_ONLY : RequestLocationMode.SECONDARY_ONLY);
                return null;
            }

            @Override
            public Integer postProcessResponse(HttpURLConnection connection, CloudFile file, CloudFileClient client,
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
                                SR.FILE_HASH_MISMATCH, this.getContentMD5(), descriptor.getMd5()),
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
     * Populates a file's properties and metadata.
     * <p>
     * This method populates the file's system properties and user-defined metadata. Before reading or modifying
     * a file's properties or metadata, call this method or its overload to retrieve the latest values for the
     * file's properties and metadata from the Microsoft Azure storage service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void downloadAttributes() throws StorageException {
        this.downloadAttributes(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Populates a file's properties and metadata using the specified request options and operation context.
     * <p>
     * This method populates the file's system properties and user-defined metadata. Before reading or modifying
     * a file's properties or metadata, call this method or its overload to retrieve the latest values for
     * the file's properties and metadata from the Microsoft Azure storage service.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void downloadAttributes(final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.downloadAttributesImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFile, Void> downloadAttributesImpl(
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFile, Void> getRequest =
                new StorageRequest<CloudFileClient, CloudFile, Void>(options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFile file, OperationContext context)
                    throws Exception {
                return FileRequest.getFileProperties(
                        file.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Void preProcessResponse(CloudFile file, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                // Set attributes
                final FileAttributes retrievedAttributes = FileResponse.getFileAttributes(this.getConnection(),
                        file.getStorageUri());
                file.properties = retrievedAttributes.getProperties();
                file.metadata = retrievedAttributes.getMetadata();

                return null;
            }
        };

        return getRequest;
    }

    /**
     * Checks to see if the file exists.
     * 
     * @return <code>true</code> if the file exists, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final boolean exists() throws StorageException {
        return this.exists(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Checks to see if the file exists, using the specified access condition, request options and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>true</code> if the file exists, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final boolean exists(final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        return this.exists(false /* primaryOnly */, accessCondition, options, opContext);
    }

    @DoesServiceRequest
    private final boolean exists(final boolean primaryOnly, final AccessCondition accessCondition,
            FileRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        return ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.existsImpl(primaryOnly, accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFile, Boolean> existsImpl(final boolean primaryOnly,
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFile, Boolean> getRequest =
                new StorageRequest<CloudFileClient, CloudFile, Boolean>(options, this.getStorageUri()) {
            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(primaryOnly ? RequestLocationMode.PRIMARY_ONLY
                        : RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFile file, OperationContext context)
                    throws Exception {
                return FileRequest.getFileProperties(
                        file.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, -1L, context);
            }

            @Override
            public Boolean preProcessResponse(CloudFile file, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_OK) {
                    final FileAttributes retrievedAttributes = FileResponse.getFileAttributes(this.getConnection(),
                            file.getStorageUri());
                    file.properties = retrievedAttributes.getProperties();
                    file.metadata = retrievedAttributes.getMetadata();
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
     * Returns a shared access signature for the file using the specified group policy identifier and
     * shared access file headers. Note this does not contain the leading "?".
     *
     * @param policy
     *            A <code>{@link SharedAccessFilePolicy}</code> object that represents the access policy for the shared
     *            access signature.
     * @param groupPolicyIdentifier
     *            A <code>String</code> that represents the share-level access policy.
     *
     * @return A <code>String</code> that represents the shared access signature.
     *
     * @throws InvalidKeyException
     *             If the credentials are invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public String generateSharedAccessSignature(final SharedAccessFilePolicy policy, final String groupPolicyIdentifier)
            throws InvalidKeyException, StorageException {
        return generateSharedAccessSignature(policy, null /* headers */, groupPolicyIdentifier);
    }
    
    /**
     * Returns a shared access signature for the file using the specified group policy identifier and
     * shared access file headers. Note this does not contain the leading "?".
     *
     * @param policy
     *            A <code>{@link SharedAccessFilePolicy}</code> object that represents the access policy for the shared
     *            access signature.
     * @param headers
     *            A <code>{@link SharedAccessFileHeaders}</code> object that represents the optional header values to
     *            set for a file accessed with this shared access signature.
     * @param groupPolicyIdentifier
     *            A <code>String</code> that represents the share-level access policy.
     *
     * @return A <code>String</code> that represents the shared access signature.
     *
     * @throws IllegalArgumentException
     *             If the credentials are invalid.
     * @throws InvalidKeyException
     *             If the credentials are invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public String generateSharedAccessSignature(
            final SharedAccessFilePolicy policy, final SharedAccessFileHeaders headers, final String groupPolicyIdentifier)
            throws InvalidKeyException, StorageException {

        return this.generateSharedAccessSignature(policy, headers, groupPolicyIdentifier,
                null /* IP range */, null /* protocols */);
    }
    
    /**
     * Returns a shared access signature for the file using the specified group policy identifier and
     * shared access file headers. Note this does not contain the leading "?".
     *
     * @param policy
     *            A <code>{@link SharedAccessFilePolicy}</code> object that represents the access policy for the shared
     *            access signature.
     * @param headers
     *            A <code>{@link SharedAccessFileHeaders}</code> object that represents the optional header values to
     *            set for a file accessed with this shared access signature.
     * @param groupPolicyIdentifier
     *            A <code>String</code> that represents the share-level access policy.
     * @param ipRange
     *            A {@link IPRange} object containing the range of allowed IP addresses.
     * @param protocols
     *            A {@link SharedAccessProtocols} representing the allowed Internet protocols.
     *
     * @return A <code>String</code> that represents the shared access signature.
     *
     * @throws IllegalArgumentException
     *             If the credentials are invalid.
     * @throws InvalidKeyException
     *             If the credentials are invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public String generateSharedAccessSignature(
            final SharedAccessFilePolicy policy, final SharedAccessFileHeaders headers,
            final String groupPolicyIdentifier, final IPRange ipRange, final SharedAccessProtocols protocols)
            throws InvalidKeyException, StorageException {

        if (!StorageCredentialsHelper.canCredentialsSignRequest(this.fileServiceClient.getCredentials())) {
            throw new IllegalArgumentException(SR.CANNOT_CREATE_SAS_WITHOUT_ACCOUNT_KEY);
        }

        final String signature = SharedAccessSignatureHelper.generateSharedAccessSignatureHashForBlobAndFile(
                policy, headers, groupPolicyIdentifier, this.getCanonicalName(),
                ipRange, protocols, this.fileServiceClient);

        final UriQueryBuilder builder = SharedAccessSignatureHelper.generateSharedAccessSignatureForBlobAndFile(
                policy, headers, groupPolicyIdentifier, "f", ipRange, protocols, signature);

        return builder.toString();
    }
    
    /**
     * Returns the canonical name of the file in the format of
     * <i>/&lt;service-name&gt;/&lt;account-name&gt;/&lt;share-name&gt;/&lt;file-name&gt;</i>.
     * <p>
     * This format is used for Shared Access operations.
     *
     * @return The canonical name in the format of <i>/&lt;service-name&gt;/&lt;account-name&gt;
     *         /&lt;share-name&gt;/&lt;file-name&gt;</i>.
     */
    String getCanonicalName() {
        StringBuilder canonicalName = new StringBuilder("/");
        canonicalName.append(SR.FILE);

        String rawPath = this.getUri().getRawPath();
        if (this.fileServiceClient.isUsePathStyleUris()) {
            canonicalName.append(rawPath);
        }
        else {
            canonicalName.append(PathUtility.getCanonicalPathFromCredentials(
                    this.getServiceClient().getCredentials(), rawPath));
        }
        
        return canonicalName.toString();
    }

    /**
     * Returns the Azure File's copy state.
     *
     * @return A {@link CopyState} object that represents the copy state of the file.
     */
    public CopyState getCopyState() {
        return this.properties.getCopyState();
    }

    /**
     * Opens a file input stream to download the file.
     * <p>
     * Use {@link CloudFile#setStreamMinimumReadSizeInBytes(int)} to configure the read size.
     * 
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final FileInputStream openRead() throws StorageException {
        return this.openRead(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Opens a file input stream to download the file using the specified request options and
     * operation context.
     * <p>
     * Use {@link #setStreamMinimumReadSizeInBytes(int)} to configure the read size.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return An <code>InputStream</code> object that represents the stream to use for reading from the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final FileInputStream openRead(final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient, false /* setStartTime */);

        return new FileInputStream(this, accessCondition, options, opContext);
    }

    /**
     * Opens an output stream object to write data to the file. The file must already exist and any existing data may 
     * be overwritten.
     * 
     * @return A {@link FileOutputStream} object used to write data to the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public FileOutputStream openWriteExisting() throws StorageException {
        return this
                .openOutputStreamInternal(null /* length */, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Opens an output stream object to write data to the file, using specified request options and
     * operation context. The file must already exist and any existing data may be overwritten.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link FileOutputStream} object used to write data to the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public FileOutputStream openWriteExisting(AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        return this
                .openOutputStreamInternal(null /* length */, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Opens an output stream object to write data to the file. The file does not yet exist and will
     * be created with the length specified. If the file already exists on the service, it will be overwritten.
     * <p>
     * To avoid overwriting and instead throw an error, please use the
     * {@link #openWriteNew(long, AccessCondition, FileRequestOptions, OperationContext)} overload with the appropriate
     * {@link AccessCondition}.
     * 
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the stream to create.
     * 
     * @return A {@link FileOutputStream} object used to write data to the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public FileOutputStream openWriteNew(final long length) throws StorageException {
        return this
                .openOutputStreamInternal(length, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Opens an output stream object to write data to the file, using the specified lease ID, request options and
     * operation context. The file does not need to yet exist and will be created with the length specified. If the file
     *  already exists on the service, it will be overwritten.
     * <p>
     * To avoid overwriting and instead throw an error, please pass in an {@link AccessCondition} generated using 
     * {@link AccessCondition#generateIfNotExistsCondition()}.
     * 
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the stream to create.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link FileOutputStream} object used to write data to the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public FileOutputStream openWriteNew(final long length, AccessCondition accessCondition,
            FileRequestOptions options, OperationContext opContext) throws StorageException {
        return openOutputStreamInternal(length, accessCondition, options, opContext);
    }

    /**
     * Opens an output stream object to write data to the file, using the specified lease ID, request options and
     * operation context. If the length is specified, a new file will be created with the length specified.
     * Otherwise, the file must already exist and a stream of its current length will be opened.
     * 
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the stream to create. This value must be
     *            null if the file already exists.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link FileOutputStream} object used to write data to the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    private FileOutputStream openOutputStreamInternal(Long length, AccessCondition accessCondition,
            FileRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient, false /* setStartTime */);

        if (length != null) {
            if (options.getStoreFileContentMD5()) {
                throw new IllegalArgumentException(SR.FILE_MD5_NOT_POSSIBLE);
            }

            this.create(length, accessCondition, options, opContext);
        }
        else {
            this.downloadAttributes(accessCondition, options, opContext);
            length = this.getProperties().getLength();
        }

        if (accessCondition != null) {
            accessCondition = AccessCondition.generateLeaseCondition(accessCondition.getLeaseID());
        }

        return new FileOutputStream(this, length, accessCondition, options, opContext);
    }

    /**
     * Uploads a file from data in a byte array. If the file already exists on the service, it will be overwritten.
     * 
     * @param buffer
     *            A <code>byte</code> array which represents the data to write to the file.
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
     * Uploads a file from data in a byte array. If the file already exists on the service, it will be overwritten.
     * 
     * @param buffer
     *            A <code>byte</code> array which represents the data to write to the file.
     * @param offset
     *            A <code>int</code> which represents the offset of the byte array from which to start the data upload.
     * @param length
     *            An <code>int</code> which represents the number of bytes to upload from the input buffer.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
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
            final AccessCondition accessCondition, FileRequestOptions options, OperationContext opContext)
            throws StorageException, IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer, offset, length);
        this.upload(inputStream, length, accessCondition, options, opContext);
        inputStream.close();
    }

    /**
     * Uploads a local file. If the file already exists on the service, it will be overwritten.
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
     * Uploads a file from a local file. If the file already exists on the service, it will be overwritten.
     * 
     * @param path
     *            A <code>String</code> which represents the path to the file to be uploaded.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public void uploadFromFile(final String path, final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException, IOException {
        File file = new File(path);
        long fileLength = file.length();
        InputStream inputStream = new BufferedInputStream(new java.io.FileInputStream(file));
        this.upload(inputStream, fileLength, accessCondition, options, opContext);
        inputStream.close();
    }

    /**
     * Uploads a file from a string using the platform's default encoding. If the file already exists on the service, it
     * will be overwritten.
     * 
     * @param content
     *            A <code>String</code> which represents the content that will be uploaded to the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public void uploadText(final String content) throws StorageException, IOException {
        this.uploadText(content, null /* charsetName */, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Uploads a file from a string using the specified encoding. If the file already exists on the service, it will be 
     * overwritten.
     * 
     * @param content
     *            A <code>String</code> which represents the content that will be uploaded to the file.
     * @param charsetName
     *            A <code>String</code> which represents the name of the charset to use to encode the content.
     *            If null, the platform's default encoding is used.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    public void uploadText(final String content, final String charsetName, final AccessCondition accessCondition,
            FileRequestOptions options, OperationContext opContext) throws StorageException, IOException {
        byte[] bytes = (charsetName == null) ? content.getBytes() : content.getBytes(charsetName);
        this.uploadFromByteArray(bytes, 0, bytes.length, accessCondition, options, opContext);
    }

    /**
     * Uploads a range to a file. 
     * 
     * @param sourceStream
     *            An {@link InputStream} object which represents the input stream to write to the file.
     * @param offset
     *            A <code>long</code> which represents the offset, in number of bytes, at which to begin writing the
     *            data.
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the data to write.
     * 
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadRange(final InputStream sourceStream, final long offset, final long length)
            throws StorageException, IOException {
        this.uploadRange(sourceStream, offset, length, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Uploads a range to a file using the specified lease ID, request options, and operation context.
     * 
     * @param sourceStream
     *            An {@link InputStream} object which represents the input stream to write to the file.
     * @param offset
     *            A <code>long</code> which represents the offset, in number of bytes, at which to begin writing the
     *            data.
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the data to write.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadRange(final InputStream sourceStream, final long offset, final long length,
            final AccessCondition accessCondition, FileRequestOptions options, OperationContext opContext)
            throws StorageException, IOException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        final FileRange range = new FileRange(offset, offset + length - 1);
        final byte[] data = new byte[(int) length];
        String md5 = null;

        int count = 0;
        int total = 0;
        while (total < length) {
            count = sourceStream.read(data, total, (int) Math.min(length - total, Integer.MAX_VALUE));
            total += count;
        }

        if (options.getUseTransactionalContentMD5()) {
            try {
                final MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update(data, 0, data.length);
                md5 = Base64.encode(digest.digest());
            }
            catch (final NoSuchAlgorithmException e) {
                // This wont happen, throw fatal.
                throw Utility.generateNewUnexpectedStorageException(e);
            }
        }

        this.putRangeInternal(range, FileRangeOperationType.UPDATE, data, length, md5, accessCondition, options,
                opContext);
    }

    /**
     * Used for both uploadRange and clearRange.
     * 
     * @param range
     *            A {@link FileRange} object that specifies the file range.
     * @param operationType
     *            A {@link FileRangeOperationType} enumeration value that specifies the operation type.
     * @param data
     *            A <code>byte</code> array which represents the data to write.
     * @param length
     *            A <code>long</code> which represents the number of bytes to write.
     * @param md5
     *            A <code>String</code> which represents the MD5 hash for the data.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    private void putRangeInternal(final FileRange range, final FileRangeOperationType operationType, final byte[] data,
            final long length, final String md5, final AccessCondition accessCondition,
            final FileRequestOptions options, final OperationContext opContext) throws StorageException {
        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                putRangeImpl(range, operationType, data, length, md5, accessCondition, options, opContext),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFile, Void> putRangeImpl(final FileRange range,
            final FileRangeOperationType operationType, final byte[] data, final long length, final String md5,
            final AccessCondition accessCondition, final FileRequestOptions options, final OperationContext opContext) {
        final StorageRequest<CloudFileClient, CloudFile, Void> putRequest =
                new StorageRequest<CloudFileClient, CloudFile, Void>(options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFile file, OperationContext context)
                    throws Exception {
                if (operationType == FileRangeOperationType.UPDATE) {
                    this.setSendStream(new ByteArrayInputStream(data));
                    this.setLength(length);
                }

                return FileRequest.putRange(file.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, opContext, accessCondition, range, operationType);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudFile file, OperationContext context) {
                if (operationType == FileRangeOperationType.UPDATE) {
                    if (options.getUseTransactionalContentMD5()) {
                        connection.setRequestProperty(Constants.HeaderConstants.CONTENT_MD5, md5);
                    }
                }
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (operationType == FileRangeOperationType.UPDATE) {
                    StorageRequest.signBlobQueueAndFileRequest(connection, client, length, context);
                }
                else {
                    StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
                }
            }

            @Override
            public Void preProcessResponse(CloudFile file, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                file.updateEtagAndLastModifiedFromResponse(this.getConnection());
                return null;
            }
        };

        return putRequest;
    }

    /**
     * Uploads the file's metadata to the storage service.
     * <p>
     * Use {@link CloudFile#downloadAttributes} to retrieve the latest values for the file's properties and metadata
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
     * Uploads the file's metadata to the storage service using the access condition, request options, and operation
     * context.
     * <p>
     * Use {@link CloudFile#downloadAttributes} to retrieve the latest values for the file's properties and metadata
     * from the Microsoft Azure storage service.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void uploadMetadata(final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.uploadMetadataImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFile, Void> uploadMetadataImpl(final AccessCondition accessCondition,
            final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFile, Void> putRequest = new StorageRequest<CloudFileClient, CloudFile, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFile file, OperationContext context)
                    throws Exception {
                return FileRequest.setFileMetadata(
                        file.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudFile file, OperationContext context) {
                FileRequest.addMetadata(connection, file.metadata, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudFile file, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                file.updateEtagAndLastModifiedFromResponse(this.getConnection());
                return null;
            }
        };

        return putRequest;
    }

    /**
     * Updates the file's properties to the storage service.
     * <p>
     * Use {@link CloudFile#downloadAttributes} to retrieve the latest values for the file's properties and metadata
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
     * Updates the file's properties using the access condition, request options, and operation context.
     * <p>
     * Use {@link CloudFile#downloadAttributes} to retrieve the latest values for the file's properties and metadata
     * from the Microsoft Azure storage service.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void uploadProperties(final AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this,
                this.uploadPropertiesImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFile, Void> uploadPropertiesImpl(
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFile, Void> putRequest =
                new StorageRequest<CloudFileClient, CloudFile, Void>(options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFile file, OperationContext context)
                    throws Exception {
                return FileRequest.setFileProperties(
                        file.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, file.properties);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);

            }

            @Override
            public Void preProcessResponse(CloudFile file, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                file.updateEtagAndLastModifiedFromResponse(this.getConnection());
                return null;
            }
        };

        return putRequest;
    }

    /**
     * Resizes the file to the specified size.
     * 
     * @param size
     *            A <code>long</code> which represents the size of the file, in bytes.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public void resize(long size) throws StorageException {
        this.resize(size, null /* accessCondition */, null /* options */, null /* operationContext */);
    }

    /**
     * Resizes the file to the specified size.
     * 
     * @param size
     *            A <code>long</code> which represents the size of the file, in bytes.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public void resize(long size, AccessCondition accessCondition, FileRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        ExecutionEngine.executeWithRetry(this.fileServiceClient, this, this.resizeImpl(size, accessCondition, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudFileClient, CloudFile, Void> resizeImpl(final long size,
            final AccessCondition accessCondition, final FileRequestOptions options) {
        final StorageRequest<CloudFileClient, CloudFile, Void> putRequest =
                new StorageRequest<CloudFileClient, CloudFile, Void>(options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudFileClient client, CloudFile file, OperationContext context)
                    throws Exception {
                return FileRequest.resize(file.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, size);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudFileClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudFile file, CloudFileClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                file.getProperties().setLength(size);
                file.updateEtagAndLastModifiedFromResponse(this.getConnection());
                return null;
            }
        };

        return putRequest;
    }

    /**
     * Uploads the source stream data to the file. If the file already exists on the service, it will be overwritten.
     * 
     * @param sourceStream
     *            An {@link InputStream} object to read from.
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the stream data. Must be non zero.
     * 
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void upload(final InputStream sourceStream, final long length) throws StorageException, IOException {
        this.upload(sourceStream, length, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Uploads the source stream data to the file using the specified access condition, request options, and operation
     * context. If the file already exists on the service, it will be overwritten.
     * 
     * @param sourceStream
     *            An {@link InputStream} object to read from.
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the stream data. This must be great than
     *            zero.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the file.
     * @param options
     *            A {@link FileRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudFileClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void upload(final InputStream sourceStream, final long length, final AccessCondition accessCondition,
            FileRequestOptions options, OperationContext opContext) throws StorageException, IOException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = FileRequestOptions.populateAndApplyDefaults(options, this.fileServiceClient);

        if (length <= 0) {
            throw new IllegalArgumentException(SR.INVALID_FILE_LENGTH);
        }

        if (sourceStream.markSupported()) {
            // Mark sourceStream for current position.
            sourceStream.mark(Constants.MAX_MARK_LENGTH);
        }

        final FileOutputStream streamRef = this.openWriteNew(length, accessCondition, options, opContext);
        try {
            streamRef.write(sourceStream, length);
        }
        finally {
            streamRef.close();
        }
    }

    /**
     * Retrieves the parent name for a file URI.
     * 
     * @param resourceAddress
     *            A {@link StorageUri} object which represents the resource URI.
     * @param delimiter
     *            A <code>String</code> which specifies the directory delimiter to use.
     * @param usePathStyleUris
     *            A <code>Boolean</code> which specifies whether path style URIs are used.
     * @param share
     *            A {@link CloudFileShare} object which represents the file share.
     * 
     * @return A <code>String</code> which represents the parent address for a file URI.
     * 
     * @throws URISyntaxException
     */
    protected static String getParentNameFromURI(final StorageUri resourceAddress, final CloudFileShare share)
            throws URISyntaxException {
        Utility.assertNotNull("resourceAddress", resourceAddress);
        Utility.assertNotNull("share", share);

        String delimiter = "/";
        String shareName = share.getName() + delimiter;

        String relativeURIString = Utility.safeRelativize(share.getStorageUri().getPrimaryUri(),
                resourceAddress.getPrimaryUri());

        if (relativeURIString.endsWith(delimiter)) {
            relativeURIString = relativeURIString.substring(0, relativeURIString.length() - delimiter.length());
        }

        String parentName;

        if (Utility.isNullOrEmpty(relativeURIString)) {
            // Case 1 /<ShareName>[Delimiter]*? => /<ShareName>
            // Parent of share is share itself
            parentName = null;
        }
        else {
            final int lastDelimiterDex = relativeURIString.lastIndexOf(delimiter);

            if (lastDelimiterDex < 0) {
                // Case 2 /<Share>/<Directory>
                // Parent of a Directory is Share
                parentName = "";
            }
            else {
                // Case 3 /<Share>/<Directory>/[<subDirectory>/]*<FileName>
                // Parent of CloudFile is CloudFileDirectory
                parentName = relativeURIString.substring(0, lastDelimiterDex);
                if (parentName != null && parentName.equals(shareName)) {
                    parentName = "";
                }
            }
        }

        return parentName;
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
        
        final StorageCredentialsSharedAccessSignature parsedCredentials = 
                SharedAccessSignatureHelper.parseQuery(completeUri);

        if (credentials != null && parsedCredentials != null) {
            throw new IllegalArgumentException(SR.MULTIPLE_CREDENTIALS_PROVIDED);
        }

        try {
            final boolean usePathStyleUris = Utility.determinePathStyleFromUri(this.storageUri.getPrimaryUri());
            this.fileServiceClient = new CloudFileClient(PathUtility.getServiceClientBaseAddress(
                    this.getStorageUri(), usePathStyleUris), credentials != null ? credentials : parsedCredentials);
            this.name = PathUtility.getFileNameFromURI(this.storageUri.getPrimaryUri(), usePathStyleUris);
        }
        catch (final URISyntaxException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
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
        final String xContentLengthHeader = request.getHeaderField(FileConstants.CONTENT_LENGTH_HEADER);
        if (!Utility.isNullOrEmpty(xContentLengthHeader)) {
            this.getProperties().setLength(Long.parseLong(xContentLengthHeader));
        }
    }

    /**
     * Returns the file's share.
     * 
     * @return A {@link CloudFileShare} object that represents the share of the file.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @Override
    public final CloudFileShare getShare() throws StorageException, URISyntaxException {
        if (this.share == null) {
            final StorageUri shareUri = PathUtility.getShareURI(this.getStorageUri(),
                    this.fileServiceClient.isUsePathStyleUris());
            this.share = new CloudFileShare(shareUri, this.fileServiceClient.getCredentials());
        }

        return this.share;
    }

    /**
     * Returns the metadata for the file.
     * 
     * @return A <code>java.util.HashMap</code> object that represents the metadata for the file.
     */
    public final HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Returns the name of the file.
     * 
     * @return A <code>String</code> that represents the name of the file.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Returns the file item's parent.
     * 
     * @return A {@link CloudFileDirectory} object that represents the parent directory for the file.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @Override
    public final CloudFileDirectory getParent() throws URISyntaxException, StorageException {
        if (this.parent == null) {
            final String parentName = getParentNameFromURI(this.getStorageUri(), this.getShare());

            if (parentName != null) {
                StorageUri parentURI = PathUtility.appendPathToUri(this.share.getStorageUri(), parentName);
                this.parent = new CloudFileDirectory(parentURI, this.getServiceClient().getCredentials());
            }
        }
        return this.parent;
    }

    /**
     * Returns the file's properties.
     * 
     * @return A {@link FileProperties} object that represents the properties of the file.
     */
    public final FileProperties getProperties() {
        return this.properties;
    }

    /**
     * Returns the file service client associated with the file.
     * 
     * @return A {@link CloudFileClient} object that represents the client.
     */
    public final CloudFileClient getServiceClient() {
        return this.fileServiceClient;
    }

    /**
     * Gets the number of bytes to buffer when writing to a {@link FileOutputStream}.
     * 
     * @return
     *         A <code>int</code> which represents the number of bytes to buffer.
     */
    public final int getStreamWriteSizeInBytes() {
        return this.streamWriteSizeInBytes;
    }

    /**
     * Returns the minimum read size when using a {@link FileInputStream}.
     * 
     * @return A <code>int</code> which represents the minimum read size, in bytes, when using a {@link FileInputStream}
     *         object.
     */
    public final int getStreamMinimumReadSizeInBytes() {
        return this.streamMinimumReadSizeInBytes;
    }

    /**
     * Returns the list of URIs for all locations.
     * 
     * @return A {@link StorageUri} that represents the list of URIs for all locations.
     */
    @Override
    public final StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * Returns the URI for this file.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI for the file.
     */
    @Override
    public final URI getUri() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Sets the share for the file.
     * 
     * @param share
     *            A {@link CloudFileShare} object that represents the share being assigned to the file.
     */
    protected final void setShare(final CloudFileShare share) {
        this.share = share;
    }

    /**
     * Sets the metadata for the file.
     * 
     * @param metadata
     *            A <code>java.util.HashMap</code> object that contains the metadata being assigned to the file.
     */
    public final void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the properties for the file.
     * 
     * @param properties
     *            A {@link FileProperties} object that represents the properties being assigned to the file.
     */
    protected final void setProperties(final FileProperties properties) {
        this.properties = properties;
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
     * Sets the minimum read size when using a {@link FileInputStream}.
     * 
     * @param minimumReadSize
     *            An <code>int</code> that represents the minimum number of bytes to buffer when reading from
     *            a file while using a {@link FileInputStream} object. Must be greater than or equal to 16 KB.
     * @throws IllegalArgumentException
     *             If <code>minimumReadSize</code> is less than 16 KB.
     */
    public void setStreamMinimumReadSizeInBytes(final int minimumReadSize) {
        if (minimumReadSize < 16 * Constants.KB) {
            throw new IllegalArgumentException("MinimumReadSize");
        }

        this.streamMinimumReadSizeInBytes = minimumReadSize;
    }

    /**
     * Sets the number of bytes to buffer when writing to a {@link FileOutputStream}.
     * 
     * @param streamWriteSizeInBytes
     *            An <code>int</code> which represents the number of bytes to buffer while using a
     *            {@link FileOutputStream} object, ranging from 512 bytes to 4 MB, inclusive.
     * 
     * @throws IllegalArgumentException
     *             If <code>streamWriteSizeInBytes</code> is less than 512 bytes or greater than 4 MB.
     */
    public void setStreamWriteSizeInBytes(final int streamWriteSizeInBytes) {
        if (streamWriteSizeInBytes > Constants.MAX_BLOCK_SIZE || streamWriteSizeInBytes < Constants.PAGE_SIZE) {
            throw new IllegalArgumentException("StreamWriteSizeInBytes");
        }

        this.streamWriteSizeInBytes = streamWriteSizeInBytes;
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
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    protected final StorageUri getTransformedAddress(final OperationContext opContext)
            throws URISyntaxException, StorageException {
        return this.fileServiceClient.getCredentials().transformUri(this.getStorageUri(), opContext);
    }
}
