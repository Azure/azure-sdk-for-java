// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.FileSystemAccessPolicies;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Client to a file system. It may only be instantiated through a {@link DataLakeFileSystemClientBuilder} or via the
 * method {@link DataLakeServiceClient#getFileSystemClient(String)}. This class does not hold any state about a
 * particular file system but is instead a convenient way of sending off appropriate requests to the resource on the
 * service. It may also be used to construct URLs to files/directories.
 *
 * <p>
 * This client contains operations on a file system. Operations on a path are available on {@link DataLakeFileClient}
 * and {@link DataLakeDirectoryClient} through {@link #getFileClient(String)} and {@link #getDirectoryClient(String)}
 * respectively, and operations on the service are available on {@link DataLakeServiceClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction?toc=%2fazure%2fstorage%2fblobs%2ftoc.json>
 *     Azure Docs</a> for more information on file systems.
 */
@ServiceClient(builder = DataLakeFileSystemClientBuilder.class)
public class DataLakeFileSystemClient {
    private final ClientLogger logger = new ClientLogger(DataLakeFileSystemClient.class);

    private final DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient;
    private final BlobContainerClient blobContainerClient;

    public static final String ROOT_FILESYSTEM_NAME = DataLakeFileSystemAsyncClient.ROOT_FILESYSTEM_NAME;

//    public static final String STATIC_WEBSITE_FILESYSTEM_NAME =
//    DataLakeFileSystemAsyncClient.STATIC_WEBSITE_FILESYSTEM_NAME;

//    public static final String LOG_FILESYSTEM_NAME = DataLakeFileSystemAsyncClient.LOG_FILESYSTEM_NAME;

    /**
     * Package-private constructor for use by {@link DataLakeFileSystemClientBuilder}.
     *
     * @param dataLakeFileSystemAsyncClient the async file system client.
     * @param blobContainerClient the sync blob container client.
     */
    DataLakeFileSystemClient(DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient,
        BlobContainerClient blobContainerClient) {
        this.dataLakeFileSystemAsyncClient = dataLakeFileSystemAsyncClient;
        this.blobContainerClient = blobContainerClient;
    }

    /**
     * Initializes a new DataLakeFileClient object by concatenating fileName to the end of DataLakeFileSystemClient's
     * URL. The new DataLakeFileClient uses the same request policy pipeline as the DataLakeFileSystemClient.
     *
     * @param fileName A {@code String} representing the name of the file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.getFileClient#String}
     *
     * @return A new {@link DataLakeFileClient} object which references the file with the specified name in this file
     * system.
     */
    public DataLakeFileClient getFileClient(String fileName) {
        Objects.requireNonNull(fileName, "'fileName' can not be set to null");

        return new DataLakeFileClient(dataLakeFileSystemAsyncClient.getFileAsyncClient(fileName),
            blobContainerClient.getBlobClient(fileName).getBlockBlobClient());
    }

    /**
     * Initializes a new DataLakeDirectoryClient object by concatenating directoryName to the end of
     * DataLakeFileSystemClient's URL. The new DataLakeDirectoryClient uses the same request policy pipeline as the
     * DataLakeFileSystemClient.
     *
     * @param directoryName A {@code String} representing the name of the directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.getDirectoryClient#String}
     *
     * @return A new {@link DataLakeDirectoryClient} object which references the directory with the specified name in
     * this file system.
     */
    public DataLakeDirectoryClient getDirectoryClient(String directoryName) {
        Objects.requireNonNull(directoryName, "'directoryName' can not be set to null");

        return new DataLakeDirectoryClient(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(directoryName),
            blobContainerClient.getBlobClient(directoryName).getBlockBlobClient());
    }

    /**
     * Get the file system name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.getFileSystemName}
     *
     * @return The name of file system.
     */
    public String getFileSystemName() {
        return dataLakeFileSystemAsyncClient.getFileSystemName();
    }

    /**
     * Gets the URL of the file system represented by this client.
     *
     * @return the URL.
     */
    public String getFileSystemUrl() {
        return dataLakeFileSystemAsyncClient.getFileSystemUrl();
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return dataLakeFileSystemAsyncClient.getAccountName();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public DataLakeServiceVersion getServiceVersion() {
        return dataLakeFileSystemAsyncClient.getServiceVersion();
    }


    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return dataLakeFileSystemAsyncClient.getHttpPipeline();
    }


    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.create}
     */
    public void create() {
        createWithResponse(null, null, null, Context.NONE);
    }

    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.createWithResponse#Map-PublicAccessType-Duration-Context}
     *
     * @param metadata Metadata to associate with the file system.
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> createWithResponse(Map<String, String> metadata, PublicAccessType accessType,
        Duration timeout, Context context) {
        return blobContainerClient.createWithResponse(metadata, Transforms.toBlobPublicAccessType(accessType), timeout,
            context);
    }

    /**
     * Marks the specified file system for deletion. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.delete}
     */
    public void delete() {
        deleteWithResponse(null, null, Context.NONE);
    }

    /**
     * Marks the specified file system for deletion. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteWithResponse#DataLakeRequestConditions-Duration-Context}
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> deleteWithResponse(DataLakeRequestConditions requestConditions, Duration timeout,
        Context context) {
        return blobContainerClient.deleteWithResponse(Transforms.toBlobRequestConditions(requestConditions),
             timeout, context);
    }

    /**
     * Returns the file system's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.getProperties}
     *
     * @return The file system properties.
     */
    public FileSystemProperties getProperties() {
        return getPropertiesWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the file system's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.getPropertiesWithResponse#String-Duration-Context}
     *
     * @param leaseId The lease ID the active lease on the file system must match.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the file system properties.
     */
    public Response<FileSystemProperties> getPropertiesWithResponse(String leaseId, Duration timeout, Context context) {
        Response<BlobContainerProperties> response = blobContainerClient.getPropertiesWithResponse(leaseId, timeout,
            context);
        return new SimpleResponse<>(response, Transforms.toFileSystemProperties(response.getValue()));
    }

    /**
     * Sets the file system's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.setMetadata#Map}
     *
     * @param metadata Metadata to associate with the file system.
     */
    public void setMetadata(Map<String, String> metadata) {
        setMetadataWithResponse(metadata, null, null, Context.NONE);
    }

    /**
     * Sets the file system's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.setMetadataWithResponse#Map-DataLakeRequestConditions-Duration-Context}
     * @param metadata Metadata to associate with the file system.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> setMetadataWithResponse(Map<String, String> metadata,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        return blobContainerClient.setMetadataWithResponse(metadata,
            Transforms.toBlobRequestConditions(requestConditions), timeout, context);
    }

    /**
     * Returns a lazy loaded list of files/directories in this account. The returned {@link PagedIterable} can be
     * consumed while new items are automatically retrieved as needed. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/filesystem/list#filesystem">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.listPaths}
     *
     * @return The list of files/directories.
     */
    public PagedIterable<PathItem> listPaths() {
        return this.listPaths(new ListPathsOptions(), null);
    }

    /**
     * Returns a lazy loaded list of files/directories in this account. The returned {@link PagedIterable} can be
     * consumed while new items are automatically retrieved as needed. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/filesystem/list#filesystem">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.listPaths#ListPathsOptions-Duration}
     *
     * @param options A {@link ListPathsOptions} which specifies what data should be returned by the service.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The list of files/directories.
     */
    public PagedIterable<PathItem> listPaths(ListPathsOptions options, Duration timeout) {
        return new PagedIterable<>(dataLakeFileSystemAsyncClient.listPathsWithOptionalTimeout(options, timeout));
    }

    /**
     * Creates a new file within a file system. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.createFile#String}
     *
     * @param fileName Name of the file to create.
     * @return A {@link DataLakeFileClient} used to interact with the file created.
     */
    public DataLakeFileClient createFile(String fileName) {
        return createFileWithResponse(fileName, null, null, null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new file within a file system. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context}
     *
     * @param fileName Name of the file to create.
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the file.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeFileClient} used
     * to interact with the file created.
     */
    public Response<DataLakeFileClient> createFileWithResponse(String fileName, String permissions, String umask,
        PathHttpHeaders headers, Map<String, String> metadata, DataLakeRequestConditions requestConditions,
        Duration timeout, Context context) {
        DataLakeFileClient dataLakeFileClient = getFileClient(fileName);

        return new SimpleResponse<>(dataLakeFileClient.createWithResponse(permissions, umask, headers, metadata,
            requestConditions, timeout, context), dataLakeFileClient);
    }

    /**
     * Deletes the specified file in the file system. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteFile#String}
     *
     * @param fileName Name of the file to delete.
     */
    public void deleteFile(String fileName) {
        deleteFileWithResponse(fileName, null, null, Context.NONE).getValue();
    }

    /**
     * Deletes the specified file in the file system. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteFileWithResponse#String-DataLakeRequestConditions-Duration-Context}
     *
     * @param fileName Name of the file to delete.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> deleteFileWithResponse(String fileName, DataLakeRequestConditions requestConditions,
        Duration timeout, Context context) {
        return getFileClient(fileName).deleteWithResponse(requestConditions, timeout, context);
    }

    /**
     * Creates a new directory within a file system. If a directory with the same name already exists, the directory
     * will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectory#String}
     *
     * @param directoryName Name of the directory to create.
     * @return A {@link DataLakeDirectoryClient} used to interact with the directory created.
     */
    public DataLakeDirectoryClient createDirectory(String directoryName) {
        return createDirectoryWithResponse(directoryName, null, null, null, null, null, null, null).getValue();
    }

    /**
     * Creates a new directory within a file system. If a directory with the same name already exists, the directory
     * will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context}
     *
     * @param directoryName Name of the directory to create.
     * @param permissions POSIX access permissions for the directory owner, the directory owning group, and others.
     * @param umask Restricts permissions of the directory to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains a {@link DataLakeDirectoryClient}
     * used to interact with the directory created.
     */
    public Response<DataLakeDirectoryClient> createDirectoryWithResponse(String directoryName, String permissions,
        String umask, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        DataLakeDirectoryClient dataLakeDirectoryClient = getDirectoryClient(directoryName);

        return new SimpleResponse<>(dataLakeDirectoryClient.createWithResponse(permissions, umask, headers, metadata,
            requestConditions, timeout, context), dataLakeDirectoryClient);
    }

    /**
     * Deletes the specified directory in the file system. If the directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteDirectory#String}
     *
     * @param directoryName Name of the directory to delete.
     */
    public void deleteDirectory(String directoryName) {
        deleteDirectoryWithResponse(directoryName, false, null, null, Context.NONE).getValue();
    }

    /**
     * Deletes the specified directory in the file system. If the directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context}
     *
     * @param directoryName Name of the directory to delete.
     * @param recursive Whether or not to delete all paths beneath the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> deleteDirectoryWithResponse(String directoryName, boolean recursive,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        return getDirectoryClient(directoryName).deleteWithResponse(recursive, requestConditions, timeout, context);
    }

    /**
     * Returns the file system's permissions. The permissions indicate whether file system's paths may be accessed
     * publicly. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.getAccessPolicy}
     *
     * @return The file system access policy.
     */
    public FileSystemAccessPolicies getAccessPolicy() {
        return getAccessPolicyWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the file system's permissions. The permissions indicate whether file system's paths may be accessed
     * publicly. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.getAccessPolicyWithResponse#String-Duration-Context}
     *
     * @param leaseId The lease ID the active lease on the file system must match.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The file system access policy.
     */
    public Response<FileSystemAccessPolicies> getAccessPolicyWithResponse(String leaseId, Duration timeout,
        Context context) {
        Response<BlobContainerAccessPolicies> response = blobContainerClient.getAccessPolicyWithResponse(leaseId,
            timeout, context);
        return new SimpleResponse<>(response, Transforms.toFileSystemAccessPolicies(response.getValue()));
    }

    /**
     * Sets the file system's permissions. The permissions indicate whether paths in a file system may be accessed
     * publicly. Note that, for each signed identifier, we will truncate the start and expiry times to the nearest
     * second to ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.setAccessPolicy#PublicAccessType-List}
     *
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link DataLakeSignedIdentifier} objects that specify the permissions for the file
     * system.
     * Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     */
    public void setAccessPolicy(PublicAccessType accessType, List<DataLakeSignedIdentifier> identifiers) {
        setAccessPolicyWithResponse(accessType, identifiers, null, null, Context.NONE);
    }

    /**
     * Sets the file system's permissions. The permissions indicate whether paths in a file system may be accessed
     * publicly. Note that, for each signed identifier, we will truncate the start and expiry times to the nearest
     * second to ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.setAccessPolicyWithResponse#PublicAccessType-List-DataLakeRequestConditions-Duration-Context}
     *
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link DataLakeSignedIdentifier} objects that specify the permissions for the file
     * system.
     * Please see
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> setAccessPolicyWithResponse(PublicAccessType accessType,
        List<DataLakeSignedIdentifier> identifiers, DataLakeRequestConditions requestConditions,
        Duration timeout, Context context) {
        return blobContainerClient
            .setAccessPolicyWithResponse(Transforms.toBlobPublicAccessType(accessType),
                Transforms.toBlobIdentifierList(identifiers), Transforms.toBlobRequestConditions(requestConditions),
                timeout, context);
    }

    BlobContainerClient getBlobContainerClient() {
        return blobContainerClient;
    }

    /**
     * Generates a user delegation SAS for the file system using the specified
     * {@link DataLakeServiceSasSignatureValues}.
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a user delegation SAS.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * @see DataLakeServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime) for more information on how to
     * get a user delegation key.
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey) {
        return dataLakeFileSystemAsyncClient.generateUserDelegationSas(dataLakeServiceSasSignatureValues,
            userDelegationKey);
    }

    /**
     * Generates a service SAS for the file system using the specified {@link DataLakeServiceSasSignatureValues}
     * Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeFileSystemClient.generateSas#DataLakeServiceSasSignatureValues}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues) {
        return dataLakeFileSystemAsyncClient.generateSas(dataLakeServiceSasSignatureValues);
    }

}
