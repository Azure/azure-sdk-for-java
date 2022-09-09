// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.options.BlobContainerCreateOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.FileSystemAccessPolicies;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathDeletedItem;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import reactor.core.publisher.Mono;

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
 * Please refer to the
 *
 * <a href="https://docs.microsoft.com/azure/storage/blobs/data-lake-storage-introduction">
 *     Azure Docs</a> for more information on file systems.
 */
@ServiceClient(builder = DataLakeFileSystemClientBuilder.class)
public class DataLakeFileSystemClient {
    private static final ClientLogger LOGGER = new ClientLogger(DataLakeFileSystemClient.class);

    private final DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient;
    private final BlobContainerClient blobContainerClient;

    /**
     * Special file system name for the root file system in the Storage account.
     */
    public static final String ROOT_FILESYSTEM_NAME = DataLakeFileSystemAsyncClient.ROOT_FILESYSTEM_NAME;

    private static final String ROOT_DIRECTORY_NAME = "";

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
     * @param fileName A {@code String} representing the name of the file. If the path name contains special characters,
     * pass in the url encoded version of the path name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.getFileClient#String -->
     * <pre>
     * DataLakeFileClient dataLakeFileClient = client.getFileClient&#40;fileName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.getFileClient#String -->
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
     * @param directoryName A {@code String} representing the name of the directory. If the path name contains special
     * characters, pass in the url encoded version of the path name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.getDirectoryClient#String -->
     * <pre>
     * DataLakeDirectoryClient dataLakeDirectoryClient = client.getDirectoryClient&#40;directoryName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.getDirectoryClient#String -->
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
     * Initializes a new DataLakeDirectoryClient object by concatenating {@code ""} to the end of
     * DataLakeFileSystemClient's URL. The new DataLakeDirectoryClient uses the same request policy pipeline as the
     * DataLakeFileSystemClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.getRootDirectoryClient -->
     * <pre>
     * DataLakeDirectoryClient dataLakeDirectoryClient = client.getRootDirectoryClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.getRootDirectoryClient -->
     *
     * @return A new {@link DataLakeDirectoryClient} object which references the root directory in this file system.
     */
    DataLakeDirectoryClient getRootDirectoryClient() {
        return getDirectoryClient(DataLakeFileSystemClient.ROOT_DIRECTORY_NAME);
    }

    /**
     * Get the file system name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.getFileSystemName -->
     * <pre>
     * String fileSystemName = client.getFileSystemName&#40;&#41;;
     * System.out.println&#40;&quot;The name of the file system is &quot; + fileSystemName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.getFileSystemName -->
     *
     * @return The name of file system.
     */
    public String getFileSystemName() {
        return dataLakeFileSystemAsyncClient.getFileSystemName();
    }


    /**
     * Get the url of the storage account.
     *
     * @return the URL of the storage account
     */
    public String getAccountUrl() {
        return dataLakeFileSystemAsyncClient.getAccountUrl();
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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.create -->
     * <pre>
     * try &#123;
     *     client.create&#40;&#41;;
     *     System.out.printf&#40;&quot;Create completed%n&quot;&#41;;
     * &#125; catch &#40;BlobStorageException error&#41; &#123;
     *     if &#40;error.getErrorCode&#40;&#41;.equals&#40;BlobErrorCode.CONTAINER_ALREADY_EXISTS&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Can't create file system. It already exists %n&quot;&#41;;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.create -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createWithResponse#Map-PublicAccessType-Duration-Context -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Create completed with status %d%n&quot;,
     *     client.createWithResponse&#40;metadata, PublicAccessType.CONTAINER, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createWithResponse#Map-PublicAccessType-Duration-Context -->
     *
     * @param metadata Metadata to associate with the file system. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> createWithResponse(Map<String, String> metadata, PublicAccessType accessType,
        Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() ->
            blobContainerClient.createWithResponse(metadata, Transforms.toBlobPublicAccessType(accessType), timeout,
                context), LOGGER);
    }

    /**
     * Creates a new file system within a storage account if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createIfNotExists -->
     * <pre>
     * boolean result = client.createIfNotExists&#40;&#41;;
     * System.out.println&#40;&quot;file system created: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createIfNotExists -->
     * @return {@code true} if file system is successfully created, {@code false} if file system already exists.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean createIfNotExists() {
        return createIfNotExistsWithResponse(null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new file system within a storage account if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createIfNotExistsWithResponse#Map-PublicAccessType-Duration-Context -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * Response&lt;Boolean&gt; response = client.createIfNotExistsWithResponse&#40;metadata, PublicAccessType.CONTAINER, timeout,
     *     context&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createIfNotExistsWithResponse#Map-PublicAccessType-Duration-Context -->
     *
     * @param metadata Metadata to associate with the file system. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 201, a new
     * file system was successfully created. If status code is 409, a file system already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> createIfNotExistsWithResponse(Map<String, String> metadata, PublicAccessType accessType,
        Duration timeout, Context context) {
        BlobContainerCreateOptions options = new BlobContainerCreateOptions().setMetadata(metadata)
            .setPublicAccessType(Transforms.toBlobPublicAccessType(accessType));
        return DataLakeImplUtils.returnOrConvertException(() -> blobContainerClient.createIfNotExistsWithResponse(
            options, timeout, context), LOGGER);
    }

    /**
     * Marks the specified file system for deletion. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.delete -->
     * <pre>
     * try &#123;
     *     client.delete&#40;&#41;;
     *     System.out.printf&#40;&quot;Delete completed%n&quot;&#41;;
     * &#125; catch &#40;BlobStorageException error&#41; &#123;
     *     if &#40;error.getErrorCode&#40;&#41;.equals&#40;BlobErrorCode.CONTAINER_NOT_FOUND&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Delete failed. File System was not found %n&quot;&#41;;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.delete -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteWithResponse#DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, client.deleteWithResponse&#40;
     *     requestConditions, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteWithResponse#DataLakeRequestConditions-Duration-Context -->
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(DataLakeRequestConditions requestConditions, Duration timeout,
        Context context) {
        return DataLakeImplUtils.returnOrConvertException(() ->
            blobContainerClient.deleteWithResponse(Transforms.toBlobRequestConditions(requestConditions), timeout,
                context), LOGGER);
    }

    /**
     * Marks the specified file system for deletion if it exists. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteIfExists -->
     * <pre>
     * client.deleteIfExists&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteIfExists -->
     * @return {@code true} if file system is successfully deleted, {@code false} if the file system does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteIfExists() {
        return deleteIfExistsWithResponse(new DataLakePathDeleteOptions(), null, Context.NONE).getValue();
    }

    /**
     * Marks the specified file system for deletion if it exists. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;false&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * Response&lt;Boolean&gt; response = client.deleteIfExistsWithResponse&#40;options, timeout, context&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions-Duration-Context -->
     *
     * @param options {@link DataLakePathDeleteOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. The presence of a {@link Response} indicates the
     * file system was deleted successfully, {@code null} indicates the file system does not exist at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteIfExistsWithResponse(DataLakePathDeleteOptions options, Duration timeout,
        Context context) {
        DataLakeRequestConditions requestConditions = options == null ? new DataLakeRequestConditions()
            : options.getRequestConditions();
        return DataLakeImplUtils.returnOrConvertException(() -> blobContainerClient.deleteIfExistsWithResponse(
            Transforms.toBlobRequestConditions(requestConditions), timeout, context), LOGGER);

    }

    /**
     * Returns the file system's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.getProperties -->
     * <pre>
     * FileSystemProperties properties = client.getProperties&#40;&#41;;
     * System.out.printf&#40;&quot;Public Access Type: %s, Legal Hold? %b, Immutable? %b%n&quot;,
     *     properties.getDataLakePublicAccess&#40;&#41;,
     *     properties.hasLegalHold&#40;&#41;,
     *     properties.hasImmutabilityPolicy&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.getProperties -->
     *
     * @return The file system properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public FileSystemProperties getProperties() {
        return getPropertiesWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the file system's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.getPropertiesWithResponse#String-Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * FileSystemProperties properties = client.getPropertiesWithResponse&#40;leaseId, timeout, context&#41;
     *     .getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Public Access Type: %s, Legal Hold? %b, Immutable? %b%n&quot;,
     *     properties.getDataLakePublicAccess&#40;&#41;,
     *     properties.hasLegalHold&#40;&#41;,
     *     properties.hasImmutabilityPolicy&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.getPropertiesWithResponse#String-Duration-Context -->
     *
     * @param leaseId The lease ID the active lease on the file system must match.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the file system properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<FileSystemProperties> getPropertiesWithResponse(String leaseId, Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() -> {
            Response<BlobContainerProperties> response = blobContainerClient.getPropertiesWithResponse(leaseId, timeout,
                context);
            return new SimpleResponse<>(response, Transforms.toFileSystemProperties(response.getValue()));
        }, LOGGER);
    }

    /**
     * Sets the file system's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.setMetadata#Map -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * try &#123;
     *     client.setMetadata&#40;metadata&#41;;
     *     System.out.printf&#40;&quot;Set metadata completed with status %n&quot;&#41;;
     * &#125; catch &#40;UnsupportedOperationException error&#41; &#123;
     *     System.out.printf&#40;&quot;Fail while setting metadata %n&quot;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.setMetadata#Map -->
     *
     * @param metadata Metadata to associate with the file system. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setMetadata(Map<String, String> metadata) {
        setMetadataWithResponse(metadata, null, null, Context.NONE);
    }

    /**
     * Sets the file system's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.setMetadataWithResponse#Map-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Set metadata completed with status %d%n&quot;,
     *     client.setMetadataWithResponse&#40;metadata, requestConditions, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.setMetadataWithResponse#Map-DataLakeRequestConditions-Duration-Context -->
     * @param metadata Metadata to associate with the file system. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setMetadataWithResponse(Map<String, String> metadata,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() ->
            blobContainerClient.setMetadataWithResponse(metadata, Transforms.toBlobRequestConditions(requestConditions),
                timeout, context), LOGGER);
    }

    /**
     * Returns a lazy loaded list of files/directories in this account. The returned {@link PagedIterable} can be
     * consumed while new items are automatically retrieved as needed. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/filesystem/list#filesystem">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.listPaths -->
     * <pre>
     * client.listPaths&#40;&#41;.forEach&#40;path -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, path.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.listPaths -->
     *
     * @return The list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PathItem> listPaths() {
        return this.listPaths(new ListPathsOptions(), null);
    }

    /**
     * Returns a lazy loaded list of files/directories in this account. The returned {@link PagedIterable} can be
     * consumed while new items are automatically retrieved as needed. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/filesystem/list#filesystem">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.listPaths#ListPathsOptions-Duration -->
     * <pre>
     * ListPathsOptions options = new ListPathsOptions&#40;&#41;
     *     .setPath&#40;&quot;pathPrefixToMatch&quot;&#41;
     *     .setMaxResults&#40;10&#41;;
     *
     * client.listPaths&#40;options, timeout&#41;.forEach&#40;path -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, path.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.listPaths#ListPathsOptions-Duration -->
     *
     * @param options A {@link ListPathsOptions} which specifies what data should be returned by the service. If
     * iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over the value set on these options.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PathItem> listPaths(ListPathsOptions options, Duration timeout) {
        return new PagedIterable<>(dataLakeFileSystemAsyncClient.listPathsWithOptionalTimeout(options, timeout));
    }

    /**
     * Returns a lazy loaded list of files/directories recently soft deleted in this file system. The returned
     * {@link PagedIterable} can be consumed while new items are automatically retrieved as needed. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/filesystem/list#filesystem">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.listDeletedPaths -->
     * <pre>
     * client.listDeletedPaths&#40;&#41;.forEach&#40;path -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, path.getPath&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.listDeletedPaths -->
     *
     * @return The list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PathDeletedItem> listDeletedPaths() {
        return this.listDeletedPaths(null, null, null);
    }

    /**
     * Returns a lazy loaded list of files/directories recently soft deleted in this account. The returned
     * {@link PagedIterable} can be consumed while new items are automatically retrieved as needed. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/filesystem/list#filesystem">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.listDeletedPaths#String-Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * int pageSize = 10;
     *
     * client.listDeletedPaths&#40;&quot;PathPrefixToMatch&quot;, timeout, context&#41;
     *     .iterableByPage&#40;pageSize&#41;
     *     .forEach&#40;page -&gt;
     *         page.getValue&#40;&#41;.forEach&#40;path -&gt;
     *             System.out.printf&#40;&quot;Name: %s%n&quot;, path.getPath&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.listDeletedPaths#String-Duration-Context -->
     *
     * @param prefix Specifies the path to filter the results to.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PathDeletedItem> listDeletedPaths(String prefix, Duration timeout,
        Context context) {
        return new PagedIterable<>(dataLakeFileSystemAsyncClient.listDeletedPathsWithOptionalTimeout(prefix, timeout,
            context));
    }

    /**
     * Creates a new file within a file system. By default, this method will not overwrite an existing file. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createFile#String -->
     * <pre>
     * DataLakeFileClient fileClient = client.createFile&#40;fileName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createFile#String -->
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     *  version of the path name.
     * @return A {@link DataLakeFileClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeFileClient createFile(String fileName) {
        return createFile(fileName, false);
    }

    /**
     * Creates a new file within a file system. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createFile#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;* Default value. *&#47;
     * DataLakeFileClient fClient = client.createFile&#40;fileName, overwrite&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createFile#String-boolean -->
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param overwrite Whether to overwrite, should a file exist.
     * @return A {@link DataLakeFileClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeFileClient createFile(String fileName, boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return createFileWithResponse(fileName, new DataLakePathCreateOptions().setRequestConditions(requestConditions),
            null, Context.NONE)
            .getValue();
    }

    /**
     * Creates a new file within a file system. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * Response&lt;DataLakeFileClient&gt; newFileClient = client.createFileWithResponse&#40;fileName, permissions, umask, httpHeaders,
     *     Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;, requestConditions,
     *     timeout, new Context&#40;key1, value1&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context -->
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the file. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeFileClient} used
     * to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeFileClient> createFileWithResponse(String fileName, String permissions, String umask,
        PathHttpHeaders headers, Map<String, String> metadata, DataLakeRequestConditions requestConditions,
                                                               Duration timeout, Context context) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
            .setPathHttpHeaders(headers)
            .setMetadata(metadata)
            .setRequestConditions(requestConditions);

        return createFileWithResponse(fileName, options, timeout, context);
    }

    /**
     * Creates a new file within a file system. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createFileWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * String owner = &quot;rwx&quot;;
     * String group = &quot;r--&quot;;
     * String leaseId = UUID.randomUUID&#40;&#41;.toString&#40;&#41;;
     * Integer duration = 15;
     * DataLakePathCreateOptions options = new DataLakePathCreateOptions&#40;&#41;
     *     .setPermissions&#40;permissions&#41;
     *     .setUmask&#40;umask&#41;
     *     .setOwner&#40;owner&#41;
     *     .setGroup&#40;group&#41;
     *     .setPathHttpHeaders&#40;httpHeaders&#41;
     *     .setRequestConditions&#40;requestConditions&#41;
     *     .setMetadata&#40;metadata&#41;
     *     .setProposedLeaseId&#40;leaseId&#41;
     *     .setLeaseDuration&#40;duration&#41;;
     *
     * Response&lt;DataLakeFileClient&gt; newFileClient = client.createFileWithResponse&#40;fileName, options, timeout,
     *     new Context&#40;key1, value1&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createFileWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param options {@link DataLakePathCreateOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeFileClient} used
     * to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeFileClient> createFileWithResponse(String fileName, DataLakePathCreateOptions options,
        Duration timeout, Context context) {
        DataLakeFileClient dataLakeFileClient = getFileClient(fileName);

        return new SimpleResponse<>(dataLakeFileClient.createWithResponse(options, timeout, context), dataLakeFileClient);
    }

    /**
     * Creates a new file within a file system if it does not exist. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createFileIfNotExists#String -->
     * <pre>
     * DataLakeFileClient fileClient = client.createFile&#40;fileName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createFileIfNotExists#String -->
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     *  version of the path name.
     * @return A {@link DataLakeFileClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeFileClient createFileIfNotExists(String fileName) {
        return createFileIfNotExistsWithResponse(fileName, new DataLakePathCreateOptions(), null, null).getValue();
    }

    /**
     * Creates a new file within a file system if it does not exist.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     * <pre>
     *
     * PathHttpHeaders headers = new PathHttpHeaders&#40;&#41;.setContentLanguage&#40;&quot;en-US&quot;&#41;.setContentType&#40;&quot;binary&quot;&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * DataLakePathCreateOptions options = new DataLakePathCreateOptions&#40;&#41;
     *     .setPermissions&#40;permissions&#41;
     *     .setUmask&#40;umask&#41;
     *     .setPathHttpHeaders&#40;headers&#41;
     *     .setMetadata&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;&#41;;
     *
     * Response&lt;DataLakeFileClient&gt; response = client.createFileIfNotExistsWithResponse&#40;fileName, options, timeout,
     *     new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param options {@link DataLakePathCreateOptions}
     * metadata key or value, it must be removed or encoded.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeFileClient} used
     * to interact with the file created. If {@link Response}'s status code is 201, a new file was successfully created.
     * If status code is 409, a file with the same name already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeFileClient> createFileIfNotExistsWithResponse(String fileName,
        DataLakePathCreateOptions options, Duration timeout, Context context) {
        DataLakeFileClient dataLakeFileClient = getFileClient(fileName);
        Response<PathInfo> response = dataLakeFileClient.createIfNotExistsWithResponse(options, timeout, context);
        return new SimpleResponse<>(response, dataLakeFileClient);
    }

    /**
     * Deletes the specified file in the file system. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteFile#String -->
     * <pre>
     * client.deleteFile&#40;fileName&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteFile#String -->
     *
     * @param fileName Name of the file to delete. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteFile(String fileName) {
        deleteFileWithResponse(fileName, null, null, Context.NONE).getValue();
    }

    /**
     * Deletes the specified file in the file system. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteFileWithResponse#String-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     *
     * client.deleteFileWithResponse&#40;fileName, requestConditions, timeout, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteFileWithResponse#String-DataLakeRequestConditions-Duration-Context -->
     *
     * @param fileName Name of the file to delete. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteFileWithResponse(String fileName, DataLakeRequestConditions requestConditions,
        Duration timeout, Context context) {
        return getFileClient(fileName).deleteWithResponse(requestConditions, timeout, context);
    }

    /**
     * Deletes the specified file in the file system if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteFileIfExists#String -->
     * <pre>
     * boolean result = client.deleteFileIfExists&#40;fileName&#41;;
     * System.out.println&#40;&quot;Delete request completed: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteFileIfExists#String -->
     *
     * @param fileName Name of the file to delete. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @return {@code true} if the file is successfully deleted, {@code false} if the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteFileIfExists(String fileName) {
        return deleteFileIfExistsWithResponse(fileName, new DataLakePathDeleteOptions(), null,
            Context.NONE).getValue();
    }

    /**
     * Deletes the specified file in the file system if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setRequestConditions&#40;requestConditions&#41;;
     *
     * Response&lt;Boolean&gt; response = client.deleteFileIfExistsWithResponse&#40;fileName, options, timeout,
     *     new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions-Duration-Context -->
     *
     * @param fileName Name of the file to delete. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param options {@link DataLakePathDeleteOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 200, the file
     * was successfully deleted. If status code is 404, the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteFileIfExistsWithResponse(String fileName, DataLakePathDeleteOptions options,
        Duration timeout, Context context) {
        return getFileClient(fileName).deleteIfExistsWithResponse(options, timeout, context);
    }
            /**
             * Creates a new directory within a file system. By default, this method will not overwrite an existing directory.
             * For more information, see the
             * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
             *
             * <p><strong>Code Samples</strong></p>
             *
             * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectory#String -->
             * <pre>
             * DataLakeDirectoryClient directoryClient = client.createDirectory&#40;directoryName&#41;;
             * </pre>
             * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectory#String -->
             *
             * @param directoryName Name of the directory to create. If the path name contains special characters, pass in the
             * url encoded version of the path name.
             * @return A {@link DataLakeDirectoryClient} used to interact with the directory created.
             */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeDirectoryClient createDirectory(String directoryName) {
        return createDirectory(directoryName, false);
    }

    /**
     * Creates a new directory within a file system. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectory#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;* Default value. *&#47;
     * DataLakeDirectoryClient dClient = client.createDirectory&#40;fileName, overwrite&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectory#String-boolean -->
     *
     * @param directoryName Name of the directory to create. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param overwrite Whether to overwrite, should a directory exist.
     * @return A {@link DataLakeDirectoryClient} used to interact with the directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeDirectoryClient createDirectory(String directoryName, boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return createDirectoryWithResponse(directoryName, null, null, null, null, requestConditions, null, Context.NONE)
            .getValue();
    }

    /**
     * Creates a new directory within a file system. If a directory with the same name already exists, the directory
     * will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * Response&lt;DataLakeDirectoryClient&gt; newDirectoryClient = client.createDirectoryWithResponse&#40;directoryName,
     *     permissions, umask, httpHeaders, Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;, requestConditions,
     *     timeout, new Context&#40;key1, value1&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context -->
     *
     * @param directoryName Name of the directory to create.  If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param permissions POSIX access permissions for the directory owner, the directory owning group, and others.
     * @param umask Restricts permissions of the directory to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains a {@link DataLakeDirectoryClient}
     * used to interact with the directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeDirectoryClient> createDirectoryWithResponse(String directoryName, String permissions,
        String umask, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
            .setPathHttpHeaders(headers)
            .setMetadata(metadata)
            .setRequestConditions(requestConditions);

        return createDirectoryWithResponse(directoryName, options, timeout, context);
    }

    /**
     * Creates a new directory within a file system. If a directory with the same name already exists, the directory
     * will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectoryWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * String owner = &quot;rwx&quot;;
     * String group = &quot;r--&quot;;
     * String leaseId = UUID.randomUUID&#40;&#41;.toString&#40;&#41;;
     * Integer duration = 15;
     * DataLakePathCreateOptions options = new DataLakePathCreateOptions&#40;&#41;
     *     .setPermissions&#40;permissions&#41;
     *     .setUmask&#40;umask&#41;
     *     .setOwner&#40;owner&#41;
     *     .setGroup&#40;group&#41;
     *     .setPathHttpHeaders&#40;httpHeaders&#41;
     *     .setRequestConditions&#40;requestConditions&#41;
     *     .setMetadata&#40;metadata&#41;
     *     .setProposedLeaseId&#40;leaseId&#41;
     *     .setLeaseDuration&#40;duration&#41;;
     *
     * Response&lt;DataLakeDirectoryClient&gt; newDirectoryClient = client.createDirectoryWithResponse&#40;directoryName,
     *     options, timeout, new Context&#40;key1, value1&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectoryWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     *
     * @param directoryName Name of the directory to create.  If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param options {@link DataLakePathCreateOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains a {@link DataLakeDirectoryClient}
     * used to interact with the directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeDirectoryClient> createDirectoryWithResponse(String directoryName, DataLakePathCreateOptions options,
        Duration timeout, Context context) {
        DataLakeDirectoryClient dataLakeDirectoryClient = getDirectoryClient(directoryName);

        return new SimpleResponse<>(dataLakeDirectoryClient.createWithResponse(options, timeout, context), dataLakeDirectoryClient);
    }

    /**
     * Creates a new directory within a file system if it does not exist.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectoryIfNotExists#String -->
     * <pre>
     * DataLakeDirectoryClient directoryClient = client.createDirectoryIfNotExists&#40;directoryName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectoryIfNotExists#String -->
     *
     * @param directoryName Name of the directory to create. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @return A {@link DataLakeDirectoryClient} used to interact with the subdirectory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeDirectoryClient createDirectoryIfNotExists(String directoryName) {
        return createDirectoryIfNotExistsWithResponse(directoryName, new DataLakePathCreateOptions(), null, null)
            .getValue();
    }

    /**
     * Creates a new directory within a file system if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     * <pre>
     * PathHttpHeaders headers = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * DataLakePathCreateOptions options = new DataLakePathCreateOptions&#40;&#41;
     *     .setPermissions&#40;permissions&#41;
     *     .setUmask&#40;umask&#41;
     *     .setPathHttpHeaders&#40;headers&#41;
     *     .setMetadata&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;&#41;;
     *
     * Response&lt;DataLakeDirectoryClient&gt; response = client.createDirectoryIfNotExistsWithResponse&#40;directoryName,
     *     options, timeout, new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     *
     * @param directoryName Name of the directory to create.  If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param options {@link DataLakePathCreateOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeDirectoryClient}
     * used to interact with the directory created. If {@link Response}'s status code is 201, a new directory was
     * successfully created. If status code is 409, a directory with the same name already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeDirectoryClient> createDirectoryIfNotExistsWithResponse(String directoryName,
        DataLakePathCreateOptions options, Duration timeout, Context context) {
        DataLakeDirectoryClient dataLakeDirectoryClient = getDirectoryClient(directoryName);
        Response<PathInfo> response = dataLakeDirectoryClient
            .createIfNotExistsWithResponse(options, timeout, context);
        return new SimpleResponse<>(response, dataLakeDirectoryClient);
    }

    /**
     * Deletes the specified directory in the file system. If the directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteDirectory#String -->
     * <pre>
     * client.deleteDirectory&#40;directoryName&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteDirectory#String -->
     *
     * @param directoryName Name of the directory to delete.  If the path name contains special characters, pass in the
     * url encoded version of the path name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDirectory(String directoryName) {
        deleteDirectoryWithResponse(directoryName, false, null, null, Context.NONE).getValue();
    }

    /**
     * Deletes the specified directory in the file system. If the directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     *
     * client.deleteDirectoryWithResponse&#40;directoryName, recursive, requestConditions, timeout,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context -->
     *
     * @param directoryName Name of the directory to delete. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param recursive Whether to delete all paths beneath the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDirectoryWithResponse(String directoryName, boolean recursive,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        return getDirectoryClient(directoryName).deleteWithResponse(recursive, requestConditions, timeout, context);
    }

    /**
     * Deletes the specified directory in the file system if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteDirectoryIfExists#String -->
     * <pre>
     * boolean result = client.deleteDirectoryIfExists&#40;directoryName&#41;;
     * System.out.println&#40;&quot;Delete request completed: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteDirectoryIfExists#String -->
     *
     * @param directoryName Name of the directory to delete.  If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @return {@code true} if the directory is successfully deleted, {@code false} if the directory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteDirectoryIfExists(String directoryName) {
        return deleteDirectoryIfExistsWithResponse(directoryName,  new DataLakePathDeleteOptions(), null, Context.NONE).getValue();
    }

    /**
     * Deletes the specified directory in the file system if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;recursive&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * Response&lt;Boolean&gt; response = client.deleteDirectoryIfExistsWithResponse&#40;directoryName, options,
     *     timeout, new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions-Duration-Context -->
     *
     * @param directoryName Name of the directory to delete. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param options {@link DataLakePathDeleteOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 200, the directory
     * was successfully deleted. If status code is 404, the directory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteDirectoryIfExistsWithResponse(String directoryName, DataLakePathDeleteOptions options,
        Duration timeout, Context context) {
        return getDirectoryClient(directoryName).deleteIfExistsWithResponse(options, timeout, context);
    }

    /**
     * Restores a soft deleted path in the file system. For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.undeletePath#String-String -->
     * <pre>
     * client.undeletePath&#40;deletedPath, deletionId&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.undeletePath#String-String -->
     *
     * @param deletedPath The deleted path
     * @param deletionId deletion ID associated with the soft deleted path that uniquely identifies a resource if
     * multiple have been soft deleted at this location.
     * You can get soft deleted paths and their associated deletion IDs with {@link #listDeletedPaths()}.
     * @return A client pointing to the restored resource.
     * @throws NullPointerException if deletedPath or deletionId is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakePathClient undeletePath(String deletedPath, String deletionId) {
        return undeletePathWithResponse(deletedPath, deletionId, null, Context.NONE).getValue();
    }

    /**
     * Restores a soft deleted path in the file system. For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.undeletePathWithResponse#String-String-Duration-Context -->
     * <pre>
     * client.undeletePathWithResponse&#40;deletedPath, deletionId, timeout, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.undeletePathWithResponse#String-String-Duration-Context -->
     *
     * @param deletedPath The deleted path
     * @param deletionId deletion ID associated with the soft deleted path that uniquely identifies a resource if
     * multiple have been soft deleted at this location.
     * You can get soft deleted paths and their associated deletion IDs with {@link #listDeletedPaths()}.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing a client pointing to the restored resource.
     * @throws NullPointerException if deletedPath or deletionId is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakePathClient> undeletePathWithResponse(String deletedPath, String deletionId,
        Duration timeout, Context context) {
        Mono<Response<DataLakePathAsyncClient>> response =
            dataLakeFileSystemAsyncClient.undeletePathWithResponse(deletedPath, deletionId, context);

        Response<DataLakePathAsyncClient> asyncClientResponse =
            StorageImplUtils.blockWithOptionalTimeout(response, timeout);
        DataLakePathAsyncClient pathAsyncClient = asyncClientResponse.getValue();
        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(deletedPath).getBlockBlobClient();
        if (pathAsyncClient instanceof DataLakeDirectoryAsyncClient) {
            return new SimpleResponse<>(asyncClientResponse.getRequest(), asyncClientResponse.getStatusCode(),
                asyncClientResponse.getHeaders(),
                new DataLakeDirectoryClient((DataLakeDirectoryAsyncClient) pathAsyncClient, blockBlobClient));
        } else if (pathAsyncClient instanceof DataLakeFileAsyncClient) {
            return new SimpleResponse<>(asyncClientResponse.getRequest(), asyncClientResponse.getStatusCode(),
                asyncClientResponse.getHeaders(),
                new DataLakeFileClient((DataLakeFileAsyncClient) pathAsyncClient, blockBlobClient));
        } else {
            throw LOGGER.logExceptionAsError(new IllegalStateException("'pathClient' expected to be either a file "
                + "or directory client."));
        }
    }

    /**
     * Returns the file system's permissions. The permissions indicate whether file system's paths may be accessed
     * publicly. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.getAccessPolicy -->
     * <pre>
     * FileSystemAccessPolicies accessPolicies = client.getAccessPolicy&#40;&#41;;
     * System.out.printf&#40;&quot;Data Lake Access Type: %s%n&quot;, accessPolicies.getDataLakeAccessType&#40;&#41;&#41;;
     *
     * for &#40;DataLakeSignedIdentifier identifier : accessPolicies.getIdentifiers&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Identifier Name: %s, Permissions %s%n&quot;,
     *         identifier.getId&#40;&#41;,
     *         identifier.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.getAccessPolicy -->
     *
     * @return The file system access policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.getAccessPolicyWithResponse#String-Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * FileSystemAccessPolicies accessPolicies = client.getAccessPolicyWithResponse&#40;leaseId, timeout, context&#41;
     *     .getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Data Lake Access Type: %s%n&quot;, accessPolicies.getDataLakeAccessType&#40;&#41;&#41;;
     *
     * for &#40;DataLakeSignedIdentifier identifier : accessPolicies.getIdentifiers&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Identifier Name: %s, Permissions %s%n&quot;,
     *         identifier.getId&#40;&#41;,
     *         identifier.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.getAccessPolicyWithResponse#String-Duration-Context -->
     *
     * @param leaseId The lease ID the active lease on the file system must match.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The file system access policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<FileSystemAccessPolicies> getAccessPolicyWithResponse(String leaseId, Duration timeout,
        Context context) {
        return DataLakeImplUtils.returnOrConvertException(() -> {
            Response<BlobContainerAccessPolicies> response = blobContainerClient.getAccessPolicyWithResponse(leaseId,
                timeout, context);
            return new SimpleResponse<>(response, Transforms.toFileSystemAccessPolicies(response.getValue()));
        }, LOGGER);
    }

    /**
     * Sets the file system's permissions. The permissions indicate whether paths in a file system may be accessed
     * publicly. Note that, for each signed identifier, we will truncate the start and expiry times to the nearest
     * second to ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.setAccessPolicy#PublicAccessType-List -->
     * <pre>
     * DataLakeSignedIdentifier identifier = new DataLakeSignedIdentifier&#40;&#41;
     *     .setId&#40;&quot;name&quot;&#41;
     *     .setAccessPolicy&#40;new DataLakeAccessPolicy&#40;&#41;
     *         .setStartsOn&#40;OffsetDateTime.now&#40;&#41;&#41;
     *         .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;7&#41;&#41;
     *         .setPermissions&#40;&quot;permissionString&quot;&#41;&#41;;
     *
     * try &#123;
     *     client.setAccessPolicy&#40;PublicAccessType.CONTAINER, Collections.singletonList&#40;identifier&#41;&#41;;
     *     System.out.printf&#40;&quot;Set Access Policy completed %n&quot;&#41;;
     * &#125; catch &#40;UnsupportedOperationException error&#41; &#123;
     *     System.out.printf&#40;&quot;Set Access Policy completed %s%n&quot;, error&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.setAccessPolicy#PublicAccessType-List -->
     *
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link DataLakeSignedIdentifier} objects that specify the permissions for the file
     * system.
     * Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.setAccessPolicyWithResponse#PublicAccessType-List-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeSignedIdentifier identifier = new DataLakeSignedIdentifier&#40;&#41;
     *     .setId&#40;&quot;name&quot;&#41;
     *     .setAccessPolicy&#40;new DataLakeAccessPolicy&#40;&#41;
     *         .setStartsOn&#40;OffsetDateTime.now&#40;&#41;&#41;
     *         .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;7&#41;&#41;
     *         .setPermissions&#40;&quot;permissionString&quot;&#41;&#41;;
     *
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Set access policy completed with status %d%n&quot;,
     *     client.setAccessPolicyWithResponse&#40;PublicAccessType.CONTAINER,
     *         Collections.singletonList&#40;identifier&#41;,
     *         requestConditions,
     *         timeout,
     *         context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.setAccessPolicyWithResponse#PublicAccessType-List-DataLakeRequestConditions-Duration-Context -->
     *
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link DataLakeSignedIdentifier} objects that specify the permissions for the file
     * system.
     * Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setAccessPolicyWithResponse(PublicAccessType accessType,
        List<DataLakeSignedIdentifier> identifiers, DataLakeRequestConditions requestConditions,
        Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() ->
            blobContainerClient
            .setAccessPolicyWithResponse(Transforms.toBlobPublicAccessType(accessType),
                Transforms.toBlobIdentifierList(identifiers), Transforms.toBlobRequestConditions(requestConditions),
                timeout, context), LOGGER);
    }

//    /**
//     * Renames an existing file system.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.rename#String -->
//     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.rename#String -->
//     *
//     * @param destinationFileSystemName The new name of the file system.
//     * @return A {@link DataLakeFileSystemClient} used to interact with the renamed file system.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public DataLakeFileSystemClient rename(String destinationFileSystemName) {
//        return this.renameWithResponse(new FileSystemRenameOptions(destinationFileSystemName), null, Context.NONE).getValue();
//    }
//
//    /**
//     * Renames an existing file system.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.renameWithResponse#FileSystemRenameOptions-Duration-Context -->
//     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.renameWithResponse#FileSystemRenameOptions-Duration-Context -->
//     *
//     * @param options {@link FileSystemRenameOptions}
//     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
//     * @param context Additional context that is passed through the Http pipeline during the service call.
//     * @return A {@link Response} whose {@link Response#getValue() value} contains a
//     * {@link DataLakeFileSystemClient} used to interact with the renamed file system.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DataLakeFileSystemClient> renameWithResponse(FileSystemRenameOptions options,
//        Duration timeout, Context context) {
//        return DataLakeImplUtils.returnOrConvertException(() -> {
//            Response<com.azure.storage.blob.BlobContainerClient> response = blobContainerClient
//                .renameWithResponse(Transforms.toBlobContainerRenameOptions(options), timeout, context);
//            return new SimpleResponse<>(response, getFileSystemClient(options.getDestinationFileSystemName()));
//        }, logger);
//    }
//
//    private DataLakeFileSystemClient getFileSystemClient(String destinationFileSystem) {
//        return new DataLakeFileSystemClient(
//            dataLakeFileSystemAsyncClient.getFileSystemAsyncClient(destinationFileSystem),
//            dataLakeFileSystemAsyncClient.prepareBuilderReplacePath(destinationFileSystem).buildClient());
//    }

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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey -->
     * <pre>
     * OffsetDateTime myExpiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * FileSystemSasPermission myPermission = new FileSystemSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues myValues = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateUserDelegationSas&#40;values, userDelegationKey&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey -->
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link DataLakeServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information
     * on how to get a user delegation key.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey) {
        return dataLakeFileSystemAsyncClient.generateUserDelegationSas(dataLakeServiceSasSignatureValues,
            userDelegationKey);
    }

    /**
     * Generates a user delegation SAS for the file system using the specified
     * {@link DataLakeServiceSasSignatureValues}.
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a user delegation SAS.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context -->
     * <pre>
     * OffsetDateTime myExpiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * FileSystemSasPermission myPermission = new FileSystemSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues myValues = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateUserDelegationSas&#40;values, userDelegationKey, accountName, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context -->
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link DataLakeServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information
     * on how to get a user delegation key.
     * @param accountName The account name.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey, String accountName, Context context) {
        return dataLakeFileSystemAsyncClient.generateUserDelegationSas(dataLakeServiceSasSignatureValues,
            userDelegationKey, accountName, context);
    }

    /**
     * Generates a service SAS for the file system using the specified {@link DataLakeServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.generateSas#DataLakeServiceSasSignatureValues -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * FileSystemSasPermission permission = new FileSystemSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateSas&#40;values&#41;; &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.generateSas#DataLakeServiceSasSignatureValues -->
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues) {
        return dataLakeFileSystemAsyncClient.generateSas(dataLakeServiceSasSignatureValues);
    }

    /**
     * Generates a service SAS for the file system using the specified {@link DataLakeServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemClient.generateSas#DataLakeServiceSasSignatureValues-Context -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * FileSystemSasPermission permission = new FileSystemSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * client.generateSas&#40;values, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemClient.generateSas#DataLakeServiceSasSignatureValues-Context -->
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues, Context context) {
        return dataLakeFileSystemAsyncClient.generateSas(dataLakeServiceSasSignatureValues, context);
    }

}
