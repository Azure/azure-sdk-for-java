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
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import com.microsoft.windowsazure.services.blob.core.storage.SharedAccessSignatureHelper;
import com.microsoft.windowsazure.services.core.storage.AccessCondition;
import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.DoesServiceRequest;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.ResultContinuation;
import com.microsoft.windowsazure.services.core.storage.ResultContinuationType;
import com.microsoft.windowsazure.services.core.storage.ResultSegment;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.windowsazure.services.core.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.StorageExtendedErrorInformation;
import com.microsoft.windowsazure.services.core.storage.utils.PathUtility;
import com.microsoft.windowsazure.services.core.storage.utils.UriQueryBuilder;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ExecutionEngine;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.LazySegmentedIterable;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.LeaseAction;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.SegmentedStorageOperation;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageOperation;

/**
 * Represents a container in the Windows Azure Blob service.
 * <p>
 * Containers hold directories, which are encapsulated as {@link CloudBlobDirectory} objects, and directories hold block
 * blobs and page blobs. Directories can also contain sub-directories.
 */
public final class CloudBlobContainer {

    /**
     * Converts the ACL string to a BlobContainerPermissions object.
     * 
     * @param aclString
     *            the string to convert.
     * @return The resulting BlobContainerPermissions object.
     */
    static BlobContainerPermissions getContainerAcl(final String aclString) {
        BlobContainerPublicAccessType accessType = BlobContainerPublicAccessType.OFF;

        if (!Utility.isNullOrEmpty(aclString)) {
            final String lowerAclString = aclString.toLowerCase();
            if ("container".equals(lowerAclString)) {
                accessType = BlobContainerPublicAccessType.CONTAINER;
            }
            else if ("blob".equals(lowerAclString)) {
                accessType = BlobContainerPublicAccessType.BLOB;
            }
            else {
                throw new IllegalArgumentException(String.format(
                        "Invalid acl public access type returned '%s'. Expected blob or container.", aclString));
            }
        }

        final BlobContainerPermissions retVal = new BlobContainerPermissions();
        retVal.setPublicAccess(accessType);

        return retVal;
    }

    /**
     * Represents the container metadata.
     */
    protected HashMap<String, String> metadata;

    /**
     * Holds the container properties.
     */
    BlobContainerProperties properties;

    /**
     * Holds the name of the container.
     */
    String name;

    /**
     * Holds the URI of the container.
     */
    URI uri;

    /**
     * Holds a reference to the associated service client.
     */
    private CloudBlobClient blobServiceClient;

    /**
     * Initializes a new instance of the CloudBlobContainer class.
     * 
     * @param client
     *            the reference to the associated service client.
     */
    private CloudBlobContainer(final CloudBlobClient client) {
        this.metadata = new HashMap<String, String>();
        this.properties = new BlobContainerProperties();
        this.blobServiceClient = client;
    }

    /**
     * Creates an instance of the <code>CloudBlobContainer</code> class using the specified address and client.
     * 
     * @param containerAddress
     *            A <code>String</code> that represents either the absolute URI to the container, or the container name.
     * @param client
     *            A {@link CloudBlobClient} object that represents the associated service client, and that specifies the
     *            endpoint for the Blob service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlobContainer(final String containerAddress, final CloudBlobClient client) throws URISyntaxException,
            StorageException {
        this(client);

        final URI resURI = PathUtility.appendPathToUri(client.getEndpoint(), containerAddress);

        this.uri = resURI;
        this.name = PathUtility.getContainerNameFromUri(resURI, client.isUsePathStyleUris());

        this.parseQueryAndVerify(this.uri, client, client.isUsePathStyleUris());
    }

    /**
     * Creates an instance of the <code>CloudBlobContainer</code> class using the specified URI and client.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the URI of the container.
     * @param client
     *            A {@link CloudBlobClient} object that represents the associated service client, and that specifies the
     *            endpoint for the Blob service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlobContainer(final URI uri, final CloudBlobClient client) throws URISyntaxException, StorageException {
        this(client);

        this.uri = uri;
        this.name = PathUtility.getContainerNameFromUri(uri, client.isUsePathStyleUris());

        this.parseQueryAndVerify(this.uri, client, client.isUsePathStyleUris());
    }

    /**
     * Creates the container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void create() throws StorageException {
        this.create(null, null);
    }

    /**
     * Creates the container using the specified options and operation context.
     * 
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
    public void create(BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlobContainer, Void> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, Void>(
                options) {

            @Override
            public Void execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {
                final HttpURLConnection request = ContainerRequest.create(container.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), opContext);

                ContainerRequest.addMetadata(request, container.metadata, opContext);
                client.getCredentials().signRequest(request, 0L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                // Set attributes
                final BlobContainerAttributes attributes = ContainerResponse.getAttributes(request,
                        client.isUsePathStyleUris());
                container.properties = attributes.getProperties();
                container.name = attributes.getName();
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Creates the container if it does not exist.
     * 
     * @return <code>true</code> if the container did not already exist and was created; otherwise, <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean createIfNotExist() throws StorageException {
        return this.createIfNotExist(null, null);
    }

    /**
     * Creates the container if it does not exist, using the specified request options and operation context.
     * 
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>true</code> if the container did not already exist and was created; otherwise, <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean createIfNotExist(BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlobContainer, Boolean> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, Boolean>(
                options) {
            @Override
            public Boolean execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {
                final HttpURLConnection request = ContainerRequest.create(container.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), opContext);

                ContainerRequest.addMetadata(request, container.metadata, opContext);
                client.getCredentials().signRequest(request, 0L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                // Validate response code here
                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_CREATED) {
                    // Set attributes
                    final BlobContainerAttributes attributes = ContainerResponse.getAttributes(request,
                            client.isUsePathStyleUris());
                    container.properties = attributes.getProperties();
                    container.name = attributes.getName();
                    return true;
                }
                else if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_CONFLICT) {
                    final StorageException potentialConflictException = StorageException.translateException(request,
                            null, opContext);

                    StorageExtendedErrorInformation extendedInfo = potentialConflictException
                            .getExtendedErrorInformation();
                    if (extendedInfo == null) {
                        // If we cant validate the error then the error must be surfaced to the user.
                        throw potentialConflictException;
                    }

                    if (!extendedInfo.getErrorCode().equals(StorageErrorCodeStrings.CONTAINER_ALREADY_EXISTS)) {
                        this.setException(potentialConflictException);
                        this.setNonExceptionedRetryableFailure(true);

                        // return false instead of null to avoid SCA issues
                        return false;
                    }

                    return false;
                }
                else {
                    throw StorageException.translateException(request, null, opContext);
                }
            }
        };

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Deletes the container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void delete() throws StorageException {
        this.delete(null, null);
    }

    /**
     * Deletes the container using the specified request options and operation context.
     * 
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
    public void delete(BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlobContainer, Void> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {

                final HttpURLConnection request = ContainerRequest.delete(container.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), opContext);

                client.getCredentials().signRequest(request, -1L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);

    }

    /**
     * Deletes the container if it exists.
     * 
     * @return <code>true</code> if the container did not already exist and was created; otherwise, <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean deleteIfExists() throws StorageException {
        return this.deleteIfExists(null, null);
    }

    /**
     * Deletes the container if it exists using the specified request options and operation context.
     * 
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>true</code> if the container did not already exist and was created; otherwise, <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean deleteIfExists(BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlobContainer, Boolean> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, Boolean>(
                options) {

            @Override
            public Boolean execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {

                final HttpURLConnection request = ContainerRequest.delete(container.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), opContext);

                client.getCredentials().signRequest(request, -1L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_ACCEPTED) {
                    container.updatePropertiesFromResponse(request);
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
     * Downloads the container's attributes, which consist of metadata and properties.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void downloadAttributes() throws StorageException {
        this.downloadAttributes(null, null);
    }

    /**
     * Downloads the container's attributes, which consist of metadata and properties, using the specified request
     * options and operation context.
     * 
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
    public void downloadAttributes(BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlobContainer, Void> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, Void>(
                options) {

            @Override
            public Void execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {
                final HttpURLConnection request = ContainerRequest.getProperties(container.uri, this
                        .getRequestOptions().getTimeoutIntervalInMs(), opContext);

                client.getCredentials().signRequest(request, -1L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                // Set attributes
                final BlobContainerAttributes attributes = ContainerResponse.getAttributes(request,
                        client.isUsePathStyleUris());
                container.metadata = attributes.getMetadata();
                container.properties = attributes.getProperties();
                container.name = attributes.getName();
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Downloads the permission settings for the container.
     * 
     * @return A {@link BlobContainerPermissions} object that represents the container's permissions.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public BlobContainerPermissions downloadPermissions() throws StorageException {
        return this.downloadPermissions(null, null);
    }

    /**
     * Downloads the permissions settings for the container using the specified request options and operation context.
     * 
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link BlobContainerPermissions} object that represents the container's permissions.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public BlobContainerPermissions downloadPermissions(BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlobContainer, BlobContainerPermissions> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, BlobContainerPermissions>(
                options) {

            @Override
            public BlobContainerPermissions execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {

                final HttpURLConnection request = ContainerRequest.getAcl(container.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), opContext);

                client.getCredentials().signRequest(request, -1L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                container.updatePropertiesFromResponse(request);
                final String aclString = ContainerResponse.getAcl(request);
                final BlobContainerPermissions containerAcl = getContainerAcl(aclString);

                final BlobAccessPolicyResponse response = new BlobAccessPolicyResponse(request.getInputStream());

                for (final String key : response.getAccessIdentifiers().keySet()) {
                    containerAcl.getSharedAccessPolicies().put(key, response.getAccessIdentifiers().get(key));
                }

                return containerAcl;
            }
        };

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Returns a value that indicates whether the container exists.
     * 
     * @return <code>true</code> if the container exists, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean exists() throws StorageException {
        return this.exists(null, null);
    }

    /**
     * Returns a value that indicates whether the container exists, using the specified request options and operation
     * context.
     * 
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return <code>true</code> if the container exists, otherwise <code>false</code>.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public boolean exists(BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlobContainer, Boolean> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, Boolean>(
                options) {

            @Override
            public Boolean execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {
                final HttpURLConnection request = ContainerRequest.getProperties(container.uri, this
                        .getRequestOptions().getTimeoutIntervalInMs(), opContext);

                client.getCredentials().signRequest(request, -1L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() == HttpURLConnection.HTTP_OK) {
                    container.updatePropertiesFromResponse(request);
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
     * Returns a shared access signature for the container. Note this does not contain the leading "?".
     * 
     * @param policy
     *            The access policy for the shared access signature.
     * @param groupPolicyIdentifier
     *            A container-level access policy.
     * @return a shared access signature for the container.
     * @throws InvalidKeyException
     * @throws StorageException
     * @throws IllegalArgumentException
     */
    public String generateSharedAccessSignature(final SharedAccessBlobPolicy policy, final String groupPolicyIdentifier)
            throws InvalidKeyException, StorageException {

        if (!this.blobServiceClient.getCredentials().canCredentialsSignRequest()) {
            final String errorMessage = "Cannot create Shared Access Signature unless the Account Key credentials are used by the BlobServiceClient.";
            throw new IllegalArgumentException(errorMessage);
        }

        final String resourceName = this.getSharedAccessCanonicalName();

        final String signature = SharedAccessSignatureHelper.generateSharedAccessSignatureHash(policy,
                groupPolicyIdentifier, resourceName, this.blobServiceClient, null);

        final UriQueryBuilder builder = SharedAccessSignatureHelper.generateSharedAccessSignature(policy,
                groupPolicyIdentifier, "c", signature);

        return builder.toString();
    }

    /**
     * Returns a reference to a {@link CloudBlockBlob} object that represents a block blob in this container.
     * 
     * @param blobAddressUri
     *            A <code>String</code> that represents the name of the blob, or the absolute URI to the blob.
     * 
     * @return A {@link CloudBlockBlob} object that represents a reference to the specified block blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlockBlob getBlockBlobReference(final String blobAddressUri) throws URISyntaxException,
            StorageException {
        Utility.assertNotNullOrEmpty("blobAddressUri", blobAddressUri);

        final URI address = PathUtility.appendPathToUri(this.uri, blobAddressUri);

        return new CloudBlockBlob(address, this.blobServiceClient, this);
    }

    /**
     * Returns a reference to a {@link CloudBlockBlob} object that represents a block blob in this container, using the
     * specified snapshot ID.
     * 
     * @param blobAddressUri
     *            A <code>String</code> that represents the name of the blob, or the absolute URI to the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot ID of the blob.
     * 
     * @return A {@link CloudBlockBlob} object that represents a reference to the specified block blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlockBlob getBlockBlobReference(final String blobAddressUri, final String snapshotID)
            throws URISyntaxException, StorageException {
        Utility.assertNotNullOrEmpty("blobAddressUri", blobAddressUri);

        final URI address = PathUtility.appendPathToUri(this.uri, blobAddressUri);

        final CloudBlockBlob retBlob = new CloudBlockBlob(address, snapshotID, this.blobServiceClient);
        retBlob.setContainer(this);
        return retBlob;
    }

    /**
     * Returns a reference to a {@link CloudBlobDirectory} object that represents a virtual blob directory within this
     * container.
     * 
     * @param relativeAddress
     *            A <code>String</code> that represents the name of the virtual blob directory, or the absolute URI to
     *            the virtual blob directory.
     * 
     * @return A {@link CloudBlobDirectory} that represents a virtual blob directory within this container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlobDirectory getDirectoryReference(final String relativeAddress) throws URISyntaxException,
            StorageException {
        Utility.assertNotNullOrEmpty("relativeAddress", relativeAddress);
        final URI address = PathUtility.appendPathToUri(this.uri, relativeAddress);

        return new CloudBlobDirectory(address, null, this.blobServiceClient);
    }

    /**
     * Returns the metadata for the container.
     * 
     * @return A <code>java.util.HashMap</code> object that represents the metadata for the container.
     */
    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Returns the name of the container.
     * 
     * @return A <code>String</code> that represents the name of the container.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a reference to a {@link CloudPageBlob} object that represents a page blob in this container.
     * 
     * @param blobAddressUri
     *            A <code>String</code> that represents the name of the blob, or the absolute URI to the blob.
     * 
     * @return A {@link CloudPageBlob} object that represents a reference to the specified page blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudPageBlob getPageBlobReference(final String blobAddressUri) throws URISyntaxException, StorageException {
        Utility.assertNotNullOrEmpty("blobAddressUri", blobAddressUri);

        final URI address = PathUtility.appendPathToUri(this.uri, blobAddressUri);

        return new CloudPageBlob(address, this.blobServiceClient, this);
    }

    /**
     * Returns a reference to a {@link CloudPageBlob} object that represents a page blob in the directory, using the
     * specified snapshot ID.
     * 
     * @param blobAddressUri
     *            A <code>String</code> that represents the name of the blob, or the absolute URI to the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot ID of the blob.
     * 
     * @return A {@link CloudPageBlob} object that represents a reference to the specified page blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudPageBlob getPageBlobReference(final String blobAddressUri, final String snapshotID)
            throws URISyntaxException, StorageException {
        Utility.assertNotNullOrEmpty("blobAddressUri", blobAddressUri);

        final URI address = PathUtility.appendPathToUri(this.uri, blobAddressUri);

        final CloudPageBlob retBlob = new CloudPageBlob(address, snapshotID, this.blobServiceClient);
        retBlob.setContainer(this);
        return retBlob;
    }

    /**
     * Returns the properties for the container.
     * 
     * @return A {@link BlobContainerProperties} object that represents the properties for the container.
     */
    public BlobContainerProperties getProperties() {
        return this.properties;
    }

    /**
     * Returns the Blob service client associated with this container.
     * 
     * @return A {@link CloudBlobClient} object that represents the service client associated with this container.
     */
    public CloudBlobClient getServiceClient() {
        return this.blobServiceClient;
    }

    /**
     * Returns the canonical name for shared access.
     * 
     * @return the canonical name for shared access.
     */
    private String getSharedAccessCanonicalName() {
        if (this.blobServiceClient.isUsePathStyleUris()) {
            return this.getUri().getPath();
        }
        else {
            return PathUtility.getCanonicalPathFromCredentials(this.blobServiceClient.getCredentials(), this.getUri()
                    .getPath());
        }
    }

    /**
     * Returns the URI after applying the authentication transformation.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI after applying the authentication
     *         transformation.
     * 
     * @throws IllegalArgumentException
     *             If an unexpected value is passed.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    protected URI getTransformedAddress() throws URISyntaxException, StorageException {
        if (this.blobServiceClient.getCredentials().doCredentialsNeedTransformUri()) {
            if (this.uri.isAbsolute()) {
                return this.blobServiceClient.getCredentials().transformUri(this.uri);
            }
            else {
                final StorageException ex = Utility.generateNewUnexpectedStorageException(null);
                ex.getExtendedErrorInformation().setErrorMessage("Blob Object relative URIs not supported.");
                throw ex;
            }
        }
        else {
            return this.uri;
        }
    }

    /**
     * Returns the URI for this container.
     * 
     * @return The absolute URI to the container.
     */
    public URI getUri() {
        return this.uri;
    }

    /**
     * Returns an enumerable collection of blob items for the container.
     * 
     * @return An enumerable collection of {@link ListBlobItem} objects that represents the items in this container.
     */
    @DoesServiceRequest
    public Iterable<ListBlobItem> listBlobs() {
        return this.listBlobs(null, false, EnumSet.noneOf(BlobListingDetails.class), null, null);
    }

    /**
     * Returns an enumerable collection of blob items for the container whose names begin with the specified prefix.
     * 
     * @param prefix
     *            A <code>String</code> that represents the blob name prefix. This value must be preceded either by the
     *            name of the container or by the absolute path to the container.
     * 
     * @return An enumerable collection of {@link ListBlobItem} objects that represents the items whose names begin with
     *         the specified prefix in this container.
     */
    @DoesServiceRequest
    public Iterable<ListBlobItem> listBlobs(final String prefix) {
        return this.listBlobs(prefix, false, EnumSet.noneOf(BlobListingDetails.class), null, null);
    }

    /**
     * Returns an enumerable collection of blob items whose names begin with the specified prefix, using the specified
     * flat or hierarchical option, listing details options, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the blob name prefix. This value must be preceded either by the
     *            name of the container or by the absolute path to the container.
     * @param useFlatBlobListing
     *            <code>true</code> to indicate that the returned list will be flat; <code>false</code> to indicate that
     *            the returned list will be hierarchical.
     * @param listingDetails
     *            A <code>java.util.EnumSet</code> object that contains {@link BlobListingDetails} values that indicate
     *            whether snapshots, metadata, and/or uncommitted blocks are returned. Committed blocks are always
     *            returned.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return An enumerable collection of {@link ListBlobItem} objects that represent the block items whose names begin
     *         with the specified prefix in this directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @DoesServiceRequest
    public Iterable<ListBlobItem> listBlobs(final String prefix, final boolean useFlatBlobListing,
            final EnumSet<BlobListingDetails> listingDetails, BlobRequestOptions options, OperationContext opContext) {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        if (!useFlatBlobListing && listingDetails != null && listingDetails.contains(BlobListingDetails.SNAPSHOTS)) {
            throw new IllegalArgumentException(
                    "Listing snapshots is only supported in flat mode (no delimiter). Consider setting useFlatBlobListing to true.");
        }

        final SegmentedStorageOperation<CloudBlobClient, CloudBlobContainer, ResultSegment<ListBlobItem>> impl = new SegmentedStorageOperation<CloudBlobClient, CloudBlobContainer, ResultSegment<ListBlobItem>>(
                options, null) {

            @Override
            public ResultSegment<ListBlobItem> execute(final CloudBlobClient client,
                    final CloudBlobContainer container, final OperationContext opContext) throws Exception {

                final ResultSegment<ListBlobItem> result = CloudBlobContainer.this.listBlobsCore(prefix,
                        useFlatBlobListing, listingDetails, -1, this.getToken(),
                        (BlobRequestOptions) this.getRequestOptions(), this, opContext);

                // Note, setting the token on the SegmentedStorageOperation is
                // key, this is how the iterator
                // will share the token across executions
                if (result != null) {
                    this.setToken(result.getContinuationToken());
                }
                return result;
            }
        };

        return new LazySegmentedIterable<CloudBlobClient, CloudBlobContainer, ListBlobItem>(impl,
                this.blobServiceClient, this, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Returns a result segment containing a collection of blob items whose names begin with the specified prefix.
     * 
     * @param prefix
     *            The blob name prefix. This value must be preceded either by the name of the container or by the
     *            absolute path to the container.
     * @param useFlatBlobListing
     *            a value indicating whether the blob listing operation will list all blobs in a container in a flat
     *            listing, or whether it will list blobs hierarchically, by virtual directory.
     * @param listingDetails
     *            a EnumSet BlobListingDetails that indicates what items a listing operation will return.
     * @param maxResults
     *            the maximum results to retrieve.
     * @param continuationToken
     *            A continuation token returned by a previous listing operation.
     * @param options
     *            An object that specifies any additional options for the request
     * @param taskReference
     *            a reference to the encapsulating task
     * @param opContext
     *            a tracking object for the operation
     * @return a result segment containing a collection of blob items whose names begin with the specified
     * @throws IllegalArgumentException
     * @throws URISyntaxException
     * @throws IOException
     * @throws StorageException
     * @throws InvalidKeyException
     * @throws XMLStreamException
     */
    @DoesServiceRequest
    ResultSegment<ListBlobItem> listBlobsCore(final String prefix, final boolean useFlatBlobListing,
            EnumSet<BlobListingDetails> listingDetails, final int maxResults,
            final ResultContinuation continuationToken, final BlobRequestOptions options,
            final StorageOperation<CloudBlobClient, CloudBlobContainer, ResultSegment<ListBlobItem>> taskReference,
            final OperationContext opContext) throws URISyntaxException, IOException, StorageException,
            InvalidKeyException, XMLStreamException {
        Utility.assertContinuationType(continuationToken, ResultContinuationType.BLOB);
        Utility.assertNotNull("options", options);

        if (listingDetails == null) {
            listingDetails = EnumSet.noneOf(BlobListingDetails.class);
        }

        if (!useFlatBlobListing && listingDetails.contains(BlobListingDetails.SNAPSHOTS)) {
            throw new IllegalArgumentException(
                    "Listing snapshots is only supported in flat mode (no delimiter). Consider setting useFlatBlobListing to true.");
        }

        final String delimiter = useFlatBlobListing ? null : this.blobServiceClient.getDirectoryDelimiter();

        final BlobListingContext listingContext = new BlobListingContext(prefix, maxResults, delimiter, listingDetails);
        listingContext.setMarker(continuationToken != null ? continuationToken.getNextMarker() : null);

        final HttpURLConnection listBlobsRequest = BlobRequest.list(this.getTransformedAddress(),
                options.getTimeoutIntervalInMs(), listingContext, options, opContext);

        this.blobServiceClient.getCredentials().signRequest(listBlobsRequest, -1L);

        ExecutionEngine.processRequest(listBlobsRequest, opContext, taskReference.getResult());

        if (taskReference.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
            taskReference.setNonExceptionedRetryableFailure(true);
            return null;
        }

        final ListBlobsResponse response = new ListBlobsResponse(listBlobsRequest.getInputStream());
        response.parseResponse(this.blobServiceClient, this);

        ResultContinuation newToken = null;

        if (response.getNextMarker() != null) {
            newToken = new ResultContinuation();
            newToken.setNextMarker(response.getNextMarker());
            newToken.setContinuationType(ResultContinuationType.BLOB);
        }

        final ResultSegment<ListBlobItem> resSegment = new ResultSegment<ListBlobItem>(response.getBlobs(
                this.blobServiceClient, this), maxResults, newToken);

        return resSegment;
    }

    /**
     * Returns a result segment of an enumerable collection of blob items in the container.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link ListBlobItem} objects that represent the blob items in the container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<ListBlobItem> listBlobsSegmented() throws StorageException {
        return this.listBlobsSegmented(null, false, EnumSet.noneOf(BlobListingDetails.class), -1, null, null, null);
    }

    /**
     * Returns a result segment containing a collection of blob items whose names begin with the specified prefix.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the blob name.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link ListBlobItem} objects that represent the blob items whose names begin with the specified prefix in
     *         the container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<ListBlobItem> listBlobsSegmented(final String prefix) throws StorageException {
        return this.listBlobsSegmented(prefix, false, EnumSet.noneOf(BlobListingDetails.class), -1, null, null, null);
    }

    /**
     * Returns a result segment containing a collection of blob items whose names begin with the specified prefix, using
     * the specified flat or hierarchical option, listing details options, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the blob name.
     * @param useFlatBlobListing
     *            <code>true</code> to indicate that the returned list will be flat; <code>false</code> to indicate that
     *            the returned list will be hierarchical.
     * @param listingDetails
     *            A <code>java.util.EnumSet</code> object that contains {@link BlobListingDetails} values that indicate
     *            whether snapshots, metadata, and/or uncommitted blocks are returned. Committed blocks are always
     *            returned.
     * @param maxResults
     *            The maximum number of results to retrieve.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a continuation token returned by a previous
     *            listing operation.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link ListBlobItem} objects that represent the block items whose names begin with the specified prefix
     *         in the container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<ListBlobItem> listBlobsSegmented(final String prefix, final boolean useFlatBlobListing,
            final EnumSet<BlobListingDetails> listingDetails, final int maxResults,
            final ResultContinuation continuationToken, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        Utility.assertContinuationType(continuationToken, ResultContinuationType.BLOB);

        if (!useFlatBlobListing && listingDetails != null && listingDetails.contains(BlobListingDetails.SNAPSHOTS)) {
            throw new IllegalArgumentException(
                    "Listing snapshots is only supported in flat mode (no delimiter). Consider setting useFlatBlobListing to true.");
        }

        final StorageOperation<CloudBlobClient, CloudBlobContainer, ResultSegment<ListBlobItem>> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, ResultSegment<ListBlobItem>>(
                options) {

            @Override
            public ResultSegment<ListBlobItem> execute(final CloudBlobClient client, final CloudBlobContainer parent,
                    final OperationContext opContext) throws Exception {

                return CloudBlobContainer.this.listBlobsCore(prefix, useFlatBlobListing, listingDetails, maxResults,
                        continuationToken, (BlobRequestOptions) this.getRequestOptions(), this, opContext);
            }
        };

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Returns an enumerable collection of containers for the service client associated with this container.
     * 
     * @return An enumerable collection of {@link CloudBlobContainer} objects that represent the containers for the
     *         service client associated with this container.
     */
    @DoesServiceRequest
    public Iterable<CloudBlobContainer> listContainers() {
        return this.blobServiceClient.listContainers();
    }

    /**
     * Returns an enumerable collection of containers whose names begin with the specified prefix for the service client
     * associated with this container.
     * 
     * @param prefix
     *            A <code>String</code> that represents the container name prefix.
     * 
     * @return An enumerable collection of {@link CloudBlobContainer} objects that represent the containers whose names
     *         begin with the specified prefix for the service client associated with this container.
     */
    @DoesServiceRequest
    public Iterable<CloudBlobContainer> listContainers(final String prefix) {
        return this.blobServiceClient.listContainers(prefix);
    }

    /**
     * Returns an enumerable collection of containers whose names begin with the specified prefix for the service client
     * associated with this container, using the specified details setting, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the container name prefix.
     * @param detailsIncluded
     *            A {@link ContainerListingDetails} value that indicates whether container metadata will be returned.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return An enumerable collection of {@link CloudBlobContainer} objects that represents the containers for the
     *         service client associated with this container.
     */
    @DoesServiceRequest
    public Iterable<CloudBlobContainer> listContainers(final String prefix,
            final ContainerListingDetails detailsIncluded, final BlobRequestOptions options,
            final OperationContext opContext) {
        return this.blobServiceClient.listContainers(prefix, detailsIncluded, options, opContext);
    }

    /**
     * Returns a result segment of an enumerable collection of containers for the service client associated with this
     * container.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link CloudBlobContainer} objects that represent the containers for the service client associated with
     *         this container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudBlobContainer> listContainersSegmented() throws StorageException {
        return this.blobServiceClient.listContainersSegmented();
    }

    /**
     * Returns a result segment of an enumerable collection of containers whose names begin with the specified prefix
     * for the service client associated with this container.
     * 
     * @param prefix
     *            A <code>String</code> that represents the blob name prefix.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link CloudBlobContainer} objects that represent the containers whose names begin with the specified
     *         prefix for the service client associated with this container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudBlobContainer> listContainersSegmented(final String prefix) throws StorageException {
        return this.blobServiceClient.listContainersSegmented(prefix);
    }

    /**
     * Returns a result segment containing a collection of containers whose names begin with the specified prefix for
     * the service client associated with this container, using the specified listing details options, request options,
     * and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the container name.
     * @param detailsIncluded
     *            A {@link ContainerListingDetails} object that indicates whether metadata is included.
     * @param maxResults
     *            The maximum number of results to retrieve.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a continuation token returned by a previous
     *            listing operation.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link CloudBlobContainer} objects that represent the containers whose names begin with the specified
     *         prefix for the service client associated with this container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * 
     */
    @DoesServiceRequest
    public ResultSegment<CloudBlobContainer> listContainersSegmented(final String prefix,
            final ContainerListingDetails detailsIncluded, final int maxResults,
            final ResultContinuation continuationToken, final BlobRequestOptions options,
            final OperationContext opContext) throws StorageException {
        return this.blobServiceClient.listContainersSegmented(prefix, detailsIncluded, maxResults, continuationToken,
                options, opContext);
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
     * @throws URISyntaxException
     * @throws StorageException
     */
    private void parseQueryAndVerify(final URI completeUri, final CloudBlobClient existingClient,
            final boolean usePathStyleUris) throws URISyntaxException, StorageException {
        Utility.assertNotNull("completeUri", completeUri);

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

        if (sasCreds == null) {
            return;
        }

        final Boolean sameCredentials = existingClient == null ? false : Utility.areCredentialsEqual(sasCreds,
                existingClient.getCredentials());

        if (existingClient == null || !sameCredentials) {
            this.blobServiceClient = new CloudBlobClient(new URI(PathUtility.getServiceClientBaseAddress(this.getUri(),
                    usePathStyleUris)), sasCreds);
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

    void updatePropertiesFromResponse(HttpURLConnection request) {
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

    /**
     * Sets the metadata for the container.
     * 
     * @param metadata
     *            A <code>java.util.HashMap</code> object that represents the metadata being assigned to the container.
     */
    public void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the name of the container.
     * 
     * @param name
     *            A <code>String</code> that represents the name being assigned to the container.
     */
    protected void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the properties for the container.
     * 
     * @param properties
     *            A {@link BlobContainerProperties} object that represents the properties being assigned to the
     *            container.
     */
    protected void setProperties(final BlobContainerProperties properties) {
        this.properties = properties;
    }

    /**
     * Sets the URI of the container.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the URI being assigned to the container.
     */
    protected void setUri(final URI uri) {
        this.uri = uri;
    }

    /**
     * Uploads the container's metadata.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadMetadata() throws StorageException {
        this.uploadMetadata(null, null);
    }

    /**
     * Uploads the container's metadata using the specified request options and operation context.
     * 
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
    public void uploadMetadata(BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlobContainer, Void> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, Void>(
                options) {

            @Override
            public Void execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {

                final HttpURLConnection request = ContainerRequest.setMetadata(container.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), opContext);

                ContainerRequest.addMetadata(request, container.metadata, opContext);
                client.getCredentials().signRequest(request, 0L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                container.updatePropertiesFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);

    }

    /**
     * Uploads the container's permissions.
     * 
     * @param permissions
     *            A {@link BlobContainerPermissions} object that represents the permissions to upload.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadPermissions(final BlobContainerPermissions permissions) throws StorageException {
        this.uploadPermissions(permissions, null, null);
    }

    /**
     * Uploads the container's permissions using the specified request options and operation context.
     * 
     * @param permissions
     *            A {@link BlobContainerPermissions} object that represents the permissions to upload.
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
    public void uploadPermissions(final BlobContainerPermissions permissions, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlobContainer, Void> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, Void>(
                options) {

            @Override
            public Void execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {

                final HttpURLConnection request = ContainerRequest.setAcl(container.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), permissions.getPublicAccess(), opContext);

                final StringWriter outBuffer = new StringWriter();

                ContainerRequest.writeSharedAccessIdentifiersToStream(permissions.getSharedAccessPolicies(), outBuffer);

                final byte[] aclBytes = outBuffer.toString().getBytes("UTF8");
                client.getCredentials().signRequest(request, aclBytes.length);
                final OutputStream outStreamRef = request.getOutputStream();
                outStreamRef.write(aclBytes);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                container.updatePropertiesFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Acquires a new lease on the container with the specified lease time and proposed lease ID.
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
     * Acquires a new lease on the container with the specified lease time, proposed lease ID, request
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
     *            An {@link AccessCondition} object that represents the access conditions for the container.
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

        final StorageOperation<CloudBlobClient, CloudBlobContainer, String> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, String>(
                options) {
            @Override
            public String execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = ContainerRequest.lease(container.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), LeaseAction.ACQUIRE, leaseTimeInSeconds, proposedLeaseId, null,
                        accessCondition, blobOptions, opContext);

                client.getCredentials().signRequest(request, 0L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                container.updatePropertiesFromResponse(request);

                return BlobResponse.getLeaseID(request, opContext);
            }
        };

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Renews an existing lease with the specified access conditions.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the container. The lease
     *            ID is required to be set with an access condition.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void renewLease(final AccessCondition accessCondition) throws StorageException {
        this.renewLease(accessCondition, null, null);
    }

    /**
     * Renews an existing lease with the specified access conditions, request options, and operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob. The lease ID is
     *            required to be set with an access condition.
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

        final StorageOperation<CloudBlobClient, CloudBlobContainer, Void> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = ContainerRequest.lease(container.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), LeaseAction.RENEW, null, null, null, accessCondition, blobOptions,
                        opContext);

                client.getCredentials().signRequest(request, 0L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                container.updatePropertiesFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Releases the lease on the container.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob. The lease ID is
     *            required to be set with an access condition.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public final void releaseLease(final AccessCondition accessCondition) throws StorageException {
        this.releaseLease(accessCondition, null, null);
    }

    /**
     * Releases the lease on the container using the specified access conditions, request options, and operation
     * context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob. The lease ID is
     *            required to be set with an access condition.
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

        final StorageOperation<CloudBlobClient, CloudBlobContainer, Void> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = ContainerRequest.lease(container.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), LeaseAction.RELEASE, null, null, null, accessCondition, blobOptions,
                        opContext);

                client.getCredentials().signRequest(request, 0L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                container.updatePropertiesFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Breaks the lease and ensures that another client cannot acquire a new lease until the current lease
     * period has expired.
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
     * Breaks the existing lease, using the specified request options and operation context, and ensures that
     * another client cannot acquire a new lease until the current lease period has expired.
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

        final StorageOperation<CloudBlobClient, CloudBlobContainer, Long> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, Long>(
                options) {
            @Override
            public Long execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = ContainerRequest.lease(container.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), LeaseAction.BREAK, null, null, breakPeriodInSeconds,
                        accessCondition, blobOptions, opContext);

                client.getCredentials().signRequest(request, 0L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return -1L;
                }

                container.updatePropertiesFromResponse(request);
                final String leaseTime = BlobResponse.getLeaseTime(request, opContext);
                return Utility.isNullOrEmpty(leaseTime) ? -1L : Long.parseLong(leaseTime);
            }
        };

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
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

        final StorageOperation<CloudBlobClient, CloudBlobContainer, Void> impl = new StorageOperation<CloudBlobClient, CloudBlobContainer, Void>(
                options) {
            @Override
            public Void execute(final CloudBlobClient client, final CloudBlobContainer container,
                    final OperationContext opContext) throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = ContainerRequest.lease(container.uri, this.getRequestOptions()
                        .getTimeoutIntervalInMs(), LeaseAction.CHANGE, null, proposedLeaseId, null, accessCondition,
                        blobOptions, opContext);

                client.getCredentials().signRequest(request, 0L);

                ExecutionEngine.processRequest(request, opContext, this.getResult());

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                container.updatePropertiesFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

}
