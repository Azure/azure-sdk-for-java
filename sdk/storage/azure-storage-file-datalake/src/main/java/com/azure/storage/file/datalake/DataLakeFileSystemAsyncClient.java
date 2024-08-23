// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.options.BlobContainerCreateOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.AzureDataLakeStorageRestAPIImpl;
import com.azure.storage.file.datalake.implementation.AzureDataLakeStorageRestAPIImplBuilder;
import com.azure.storage.file.datalake.implementation.models.FileSystemsListBlobHierarchySegmentHeaders;
import com.azure.storage.file.datalake.implementation.models.FileSystemsListPathsHeaders;
import com.azure.storage.file.datalake.implementation.models.ListBlobsHierarchySegmentResponse;
import com.azure.storage.file.datalake.implementation.models.ListBlobsShowOnly;
import com.azure.storage.file.datalake.implementation.models.PathList;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.DataLakeSasImplUtil;
import com.azure.storage.file.datalake.implementation.util.ModelHelper;
import com.azure.storage.file.datalake.implementation.util.TransformUtils;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileSystemAccessPolicies;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathDeletedItem;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Client to a file system. It may only be instantiated through a {@link DataLakeFileSystemClientBuilder} or via the
 * method {@link DataLakeServiceAsyncClient#getFileSystemAsyncClient(String)}. This class does not hold any state about
 * a particular blob but is instead a convenient way of sending off appropriate requests to the resource on the service.
 * It may also be used to construct clients for files/directories.
 *
 * <p>
 * This client contains operations on a file system. Operations on a path are available on
 * {@link DataLakeFileAsyncClient} and {@link DataLakeDirectoryAsyncClient} through {@link #getFileAsyncClient(String)}
 * and {@link #getDirectoryAsyncClient(String)} respectively, and operations on the service are available on
 * {@link DataLakeServiceAsyncClient}.
 *
 * <p>
 * Please refer to the
 *
 * <a href="https://docs.microsoft.com/azure/storage/blobs/data-lake-storage-introduction">Azure Docs</a> for more
 * information on file systems.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core project
 * (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong> start the actual network
 * operation, until {@code .subscribe()} is called on the reactive response. You can simply convert one of these
 * responses to a {@link java.util.concurrent.CompletableFuture} object through {@link Mono#toFuture()}.
 */
@ServiceClient(builder = DataLakeFileSystemClientBuilder.class, isAsync = true)
public class DataLakeFileSystemAsyncClient {
    /**
     * Special file system name for the root file system in the Storage account.
     */
    public static final String ROOT_FILESYSTEM_NAME = "$root";

    /**
     * Special directory name for the root directory of the file system.
     * <p>
     * This should only be used while getting the root directory from the file system client.
     */
    public static final String ROOT_DIRECTORY_NAME = "";

    private static final ClientLogger LOGGER = new ClientLogger(DataLakeFileSystemAsyncClient.class);
    private final AzureDataLakeStorageRestAPIImpl azureDataLakeStorage;
    private final AzureDataLakeStorageRestAPIImpl blobDataLakeStorageFs; // Just for list deleted paths
    private final BlobContainerAsyncClient blobContainerAsyncClient;

    private final String accountName;
    private final String fileSystemName;
    private final DataLakeServiceVersion serviceVersion;
    private final AzureSasCredential sasToken;
    private final boolean isTokenCredentialAuthenticated;

    /**
     * Package-private constructor for use by {@link DataLakeFileSystemClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param fileSystemName The file system name.
     * @param blobContainerAsyncClient The underlying {@link BlobContainerAsyncClient}
     */
    DataLakeFileSystemAsyncClient(HttpPipeline pipeline, String url, DataLakeServiceVersion serviceVersion,
        String accountName, String fileSystemName, BlobContainerAsyncClient blobContainerAsyncClient,
        AzureSasCredential sasToken, boolean isTokenCredentialAuthenticated) {
        this.azureDataLakeStorage = new AzureDataLakeStorageRestAPIImplBuilder()
            .pipeline(pipeline)
            .url(url)
            .fileSystem(fileSystemName)
            .version(serviceVersion.getVersion())
            .buildClient();
        String blobUrl = DataLakeImplUtils.endpointToDesiredEndpoint(url, "blob", "dfs");
        this.blobDataLakeStorageFs = new AzureDataLakeStorageRestAPIImplBuilder()
            .pipeline(pipeline)
            .url(blobUrl)
            .fileSystem(fileSystemName)
            .version(serviceVersion.getVersion())
            .buildClient();

        this.serviceVersion = serviceVersion;

        this.accountName = accountName;
        this.fileSystemName = fileSystemName;
        this.blobContainerAsyncClient = blobContainerAsyncClient;
        this.sasToken = sasToken;
        this.isTokenCredentialAuthenticated = isTokenCredentialAuthenticated;
    }

    /**
     * Initializes a new DataLakeFileAsyncClient object by concatenating fileName to the end of
     * DataLakeFileSystemAsyncClient's URL. The new DataLakeFileAsyncClient uses the same request policy pipeline as
     * the DataLakeFileSystemAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getFileAsyncClient#String -->
     * <pre>
     * DataLakeFileAsyncClient dataLakeFileAsyncClient = client.getFileAsyncClient&#40;fileName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getFileAsyncClient#String -->
     *
     * @param fileName A {@code String} representing the name of the file. If the path name contains special characters,
     * pass in the url encoded version of the path name.
     * @return A new {@link DataLakeFileAsyncClient} object which references the file with the specified name in this
     * file system.
     */
    public DataLakeFileAsyncClient getFileAsyncClient(String fileName) {
        Objects.requireNonNull(fileName, "'fileName' can not be set to null");

        BlockBlobAsyncClient blockBlobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(fileName,
            null).getBlockBlobAsyncClient();

        return new DataLakeFileAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getFileSystemName(), fileName, blockBlobAsyncClient, sasToken,
            Transforms.fromBlobCpkInfo(blobContainerAsyncClient.getCustomerProvidedKey()), isTokenCredentialAuthenticated);
    }

    /**
     * Initializes a new DataLakeDirectoryAsyncClient object by concatenating directoryName to the end of
     * DataLakeFileSystemAsyncClient's URL. The new DataLakeDirectoryAsyncClient uses the same request policy pipeline
     * as the DataLakeFileSystemAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getDirectoryAsyncClient#String -->
     * <pre>
     * DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient = client.getDirectoryAsyncClient&#40;directoryName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getDirectoryAsyncClient#String -->
     *
     * @param directoryName A {@code String} representing the name of the directory. If the path name contains special
     * characters, pass in the url encoded version of the path name.
     * @return A new {@link DataLakeDirectoryAsyncClient} object which references the directory with the specified name
     * in this file system.
     */
    public DataLakeDirectoryAsyncClient getDirectoryAsyncClient(String directoryName) {
        Objects.requireNonNull(directoryName, "'directoryName' can not be set to null");

        BlockBlobAsyncClient blockBlobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(directoryName,
            null).getBlockBlobAsyncClient();
        return new DataLakeDirectoryAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(),
            getAccountName(), getFileSystemName(), directoryName, blockBlobAsyncClient, sasToken,
            Transforms.fromBlobCpkInfo(blobContainerAsyncClient.getCustomerProvidedKey()),
            isTokenCredentialAuthenticated);
    }

    /**
     * Initializes a new DataLakeDirectoryAsyncClient object by concatenating {@code ""} to the end of
     * DataLakeFileSystemAsyncClient's URL. The new DataLakeDirectoryAsyncClient uses the same request policy pipeline
     * as the DataLakeFileSystemAsyncClient.
     *
     * Note: this should only be used while getting the root directory from the file system client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient -->
     * <pre>
     * DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient =
     *     client.getDirectoryAsyncClient&#40;DataLakeFileSystemAsyncClient.ROOT_DIRECTORY_NAME&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient -->
     *
     * @return A new {@link DataLakeDirectoryAsyncClient} object which references the root directory
     * in this file system.
     */
    DataLakeDirectoryAsyncClient getRootDirectoryAsyncClient() {
        return getDirectoryAsyncClient(DataLakeFileSystemAsyncClient.ROOT_DIRECTORY_NAME);
    }

    /**
     * Get the url of the storage account.
     *
     * @return the URL of the storage account
     */
    public String getAccountUrl() {
        return azureDataLakeStorage.getUrl();
    }

    /**
     * Gets the URL of the file system represented by this client.
     *
     * @return the URL.
     */
    public String getFileSystemUrl() {
        return azureDataLakeStorage.getUrl() + "/" + fileSystemName;
    }

    /**
     * Get the file system name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getFileSystemName -->
     * <pre>
     * String fileSystemName = client.getFileSystemName&#40;&#41;;
     * System.out.println&#40;&quot;The name of the file system is &quot; + fileSystemName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getFileSystemName -->
     *
     * @return The name of file system.
     */
    public String getFileSystemName() {
        return fileSystemName;
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public DataLakeServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return azureDataLakeStorage.getHttpPipeline();
    }

    BlobContainerAsyncClient getBlobContainerAsyncClient() {
        return blobContainerAsyncClient;
    }

    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.create -->
     * <pre>
     * client.create&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Create completed%n&quot;&#41;,
     *     error -&gt; System.out.printf&#40;&quot;Error while creating file system %s%n&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.create -->
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> create() {
        return createWithResponse(null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createWithResponse#Map-PublicAccessType -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * client.createWithResponse&#40;metadata, PublicAccessType.CONTAINER&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createWithResponse#Map-PublicAccessType -->
     *
     * @param metadata Metadata to associate with the file system. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createWithResponse(Map<String, String> metadata, PublicAccessType accessType) {
        return blobContainerAsyncClient.createWithResponse(metadata, Transforms.toBlobPublicAccessType(accessType))
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Creates a new file system within a storage account if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createIfNotExists -->
     * <pre>
     * client.createIfNotExists&#40;&#41;.subscribe&#40;created -&gt; &#123;
     *     if &#40;created&#41; &#123;
     *         System.out.println&#40;&quot;Successfully created.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createIfNotExists -->
     *
     * @return A reactive response signalling completion. {@code true} indicates that a new file system was created.
     * {@code false} indicates that a file system already existed at this location.
     *
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> createIfNotExists() {
        return createIfNotExistsWithResponse(null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new file system within a storage account if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createIfNotExistsWithResponse#Map-PublicAccessType -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * client.createIfNotExistsWithResponse&#40;metadata, PublicAccessType.CONTAINER&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createIfNotExistsWithResponse#Map-PublicAccessType -->
     *
     * @param metadata Metadata to associate with the file system. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @return A reactive response signaling completion. If {@link Response}'s status code is 201, a new file system was
     * successfully created. If status code is 409, a file system already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> createIfNotExistsWithResponse(Map<String, String> metadata,
        PublicAccessType accessType) {
        try {
            BlobContainerCreateOptions options = new BlobContainerCreateOptions().setMetadata(metadata)
                .setPublicAccessType(Transforms.toBlobPublicAccessType(accessType));
            return blobContainerAsyncClient.createIfNotExistsWithResponse(options)
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Marks the specified file system for deletion. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.delete -->
     * <pre>
     * client.delete&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Delete completed%n&quot;&#41;,
     *     error -&gt; System.out.printf&#40;&quot;Delete failed: %s%n&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.delete -->
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        return deleteWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Marks the specified file system for deletion. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteWithResponse#DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * client.deleteWithResponse&#40;requestConditions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteWithResponse#DataLakeRequestConditions -->
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If either {@link DataLakeRequestConditions#getIfMatch()} or
     * {@link DataLakeRequestConditions#getIfNoneMatch()} is set.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse(DataLakeRequestConditions requestConditions) {
        return blobContainerAsyncClient.deleteWithResponse(Transforms.toBlobRequestConditions(requestConditions))
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Marks the specified file system for deletion if it exists. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteIfExists -->
     * <pre>
     * client.deleteIfExists&#40;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteIfExists -->
     *
     * @return a reactive response signaling completion. {@code true} indicates that the file system was successfully
     * deleted, {@code false} indicates that the file system did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteIfExists() {
        return deleteIfExistsWithResponse(new DataLakePathDeleteOptions()).flatMap(FluxUtil::toMono);
    }

    /**
     * Marks the specified file system for deletion if it exists. The file system and any files/directories contained within it are
     * later deleted during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;false&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * client.deleteIfExistsWithResponse&#40;options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions -->
     *
     * @param options {@link DataLakePathDeleteOptions}
     * @return A reactive response signaling completion. If {@link Response}'s status code is 202, the file system was
     * successfully deleted. If status code is 404, the file system does not exist.
     * @throws UnsupportedOperationException If either {@link DataLakeRequestConditions#getIfMatch()} or
     * {@link DataLakeRequestConditions#getIfNoneMatch()} is set.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteIfExistsWithResponse(DataLakePathDeleteOptions options) {
        try {
            options = options == null ? new DataLakePathDeleteOptions() : options;
            return blobContainerAsyncClient.deleteIfExistsWithResponse(
                    Transforms.toBlobRequestConditions(options.getRequestConditions()))
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Returns the file system's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getProperties -->
     * <pre>
     * client.getProperties&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Public Access Type: %s, Legal Hold? %b, Immutable? %b%n&quot;,
     *         response.getDataLakePublicAccess&#40;&#41;,
     *         response.hasLegalHold&#40;&#41;,
     *         response.hasImmutabilityPolicy&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getProperties -->
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} containing the
     * file system properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<FileSystemProperties> getProperties() {
        return getPropertiesWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the file system's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getPropertiesWithResponse#String -->
     * <pre>
     *
     * client.getPropertiesWithResponse&#40;leaseId&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Public Access Type: %s, Legal Hold? %b, Immutable? %b%n&quot;,
     *         response.getValue&#40;&#41;.getDataLakePublicAccess&#40;&#41;,
     *         response.getValue&#40;&#41;.hasLegalHold&#40;&#41;,
     *         response.getValue&#40;&#41;.hasImmutabilityPolicy&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getPropertiesWithResponse#String -->
     *
     * @param leaseId The lease ID the active lease on the file system must match.
     * @return A reactive response containing the file system properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<FileSystemProperties>> getPropertiesWithResponse(String leaseId) {
        return blobContainerAsyncClient.getPropertiesWithResponse(leaseId)
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException)
            .map(response -> new SimpleResponse<>(response, Transforms.toFileSystemProperties(response.getValue())));
    }

    /**
     * Determines if the file system exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.exists -->
     * <pre>
     * client.exists&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Exists? %b%n&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.exists -->
     *
     * @return true if the path exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> exists() {
        return existsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Determines if the file system exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.existsWithResponse -->
     * <pre>
     * client.existsWithResponse&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Exists? %b%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.existsWithResponse -->
     *
     * @return true if the path exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> existsWithResponse() {
        return blobContainerAsyncClient.existsWithResponse().onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Sets the file system's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setMetadata#Map -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * client.setMetadata&#40;metadata&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Set metadata completed%n&quot;&#41;,
     *     error -&gt; System.out.printf&#40;&quot;Set metadata failed: %s%n&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setMetadata#Map -->
     *
     * @param metadata Metadata to associate with the file system. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the file system's metadata. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-metadata">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setMetadataWithResponse#Map-DataLakeRequestConditions -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfModifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     *
     * client.setMetadataWithResponse&#40;metadata, requestConditions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Set metadata completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setMetadataWithResponse#Map-DataLakeRequestConditions -->
     *
     * @param metadata Metadata to associate with the file system. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains signalling
     * completion.
     * @throws UnsupportedOperationException If one of {@link DataLakeRequestConditions#getIfMatch()},
     * {@link DataLakeRequestConditions#getIfNoneMatch()}, or {@link DataLakeRequestConditions#getIfUnmodifiedSince()}
     * is set.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        return blobContainerAsyncClient.setMetadataWithResponse(metadata,
                Transforms.toBlobRequestConditions(requestConditions))
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Returns a reactive Publisher emitting all the files/directories in this account lazily as needed. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/filesystem/list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listPaths -->
     * <pre>
     * client.listPaths&#40;&#41;.subscribe&#40;path -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, path.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listPaths -->
     *
     * @return A reactive response emitting the list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PathItem> listPaths() {
        return this.listPaths(new ListPathsOptions());
    }

    /**
     * Returns a reactive Publisher emitting all the files/directories in this account lazily as needed. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/filesystem/list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listPaths#ListPathsOptions -->
     * <pre>
     * ListPathsOptions options = new ListPathsOptions&#40;&#41;
     *     .setPath&#40;&quot;PathNamePrefixToMatch&quot;&#41;
     *     .setMaxResults&#40;10&#41;;
     *
     * client.listPaths&#40;options&#41;.subscribe&#40;path -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, path.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listPaths#ListPathsOptions -->
     *
     * @param options A {@link ListPathsOptions} which specifies what data should be returned by the service.
     * @return A reactive response emitting the list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PathItem> listPaths(ListPathsOptions options) {
        try {
            return listPathsWithOptionalTimeout(options, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    PagedFlux<PathItem> listPathsWithOptionalTimeout(ListPathsOptions options,
        Duration timeout) {
        BiFunction<String, Integer, Mono<PagedResponse<PathItem>>> func =
            (marker, pageSize) -> {
                ListPathsOptions finalOptions;
                if (pageSize != null) {
                    if (options == null) {
                        finalOptions = new ListPathsOptions().setMaxResults(pageSize);
                    } else {
                        finalOptions = new ListPathsOptions()
                            .setMaxResults(pageSize)
                            .setPath(options.getPath())
                            .setRecursive(options.isRecursive())
                            .setUserPrincipalNameReturned(options.isUserPrincipalNameReturned());
                    }
                } else {
                    finalOptions = options;
                }
                return listPathsSegment(marker, finalOptions, timeout)
                    .map(response -> {
                        List<PathItem> value = response.getValue() == null
                            ? Collections.emptyList()
                            : response.getValue().getPaths().stream()
                            .map(Transforms::toPathItem)
                            .collect(Collectors.toList());

                        return new PagedResponseBase<>(
                            response.getRequest(),
                            response.getStatusCode(),
                            response.getHeaders(),
                            value,
                            response.getDeserializedHeaders().getXMsContinuation(),
                            response.getDeserializedHeaders());
                    });
            };
        return new PagedFlux<>(pageSize -> func.apply(null, pageSize), func);
    }

    private Mono<ResponseBase<FileSystemsListPathsHeaders, PathList>> listPathsSegment(String marker,
        ListPathsOptions options, Duration timeout) {
        options = options == null ? new ListPathsOptions() : options;

        return StorageImplUtils.applyOptionalTimeout(
            this.azureDataLakeStorage.getFileSystems().listPathsWithResponseAsync(options.isRecursive(), null, null,
                marker, options.getPath(), options.getMaxResults(),
                options.isUserPrincipalNameReturned(),  Context.NONE), timeout)
            .onErrorMap(ModelHelper::mapToDataLakeStorageException);
    }

    /**
     * Returns a reactive Publisher emitting all the files/directories in this filesystem that have been recently soft
     * deleted lazily as needed. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listDeletedPaths -->
     * <pre>
     * client.listDeletedPaths&#40;&#41;.subscribe&#40;path -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, path.getPath&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listDeletedPaths -->
     *
     * @return A reactive response emitting the list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PathDeletedItem> listDeletedPaths() {
        return this.listDeletedPaths(null);
    }

    /**
     * Returns a reactive Publisher emitting all the files/directories in this account lazily as needed. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">Azure Docs</a>.
     *
     * Note: You can specify the page size by using byPaged methods that accept an integer such as
     * {@link PagedFlux#byPage(int)}. Please refer to the REST docs above for limitations on page size
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listDeletedPaths#String -->
     * <pre>
     * int pageSize = 10;
     * client.listDeletedPaths&#40;&quot;PathNamePrefixToMatch&quot;&#41;
     *     .byPage&#40;pageSize&#41;
     *     .subscribe&#40;page -&gt;
     *         page.getValue&#40;&#41;.forEach&#40;path -&gt;
     *             System.out.printf&#40;&quot;Name: %s%n&quot;, path.getPath&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listDeletedPaths#String -->
     *
     * @param prefix Specifies the path to filter the results to.
     * @return A reactive response emitting the list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PathDeletedItem> listDeletedPaths(String prefix) {
        try {
            return new PagedFlux<>(pageSize -> withContext(context -> listDeletedPaths(null, pageSize, prefix,
                null, context)),
                (marker, pageSize) -> withContext(context -> listDeletedPaths(marker, pageSize, prefix, null,
                    context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    PagedFlux<PathDeletedItem> listDeletedPathsWithOptionalTimeout(String prefix, Duration timeout, Context context) {
        return new PagedFlux<>(pageSize -> listDeletedPaths(null, pageSize, prefix, timeout, context),
            (marker, pageSize) -> listDeletedPaths(marker, pageSize, prefix, timeout, context));
    }

    private Mono<PagedResponse<PathDeletedItem>> listDeletedPaths(String marker, Integer pageSize,
        String prefix, Duration timeout, Context context) {
        return listDeletedPathsSegment(marker, prefix, pageSize, timeout, context)
            .map(response -> {
                List<PathDeletedItem> value = response.getValue().getSegment() == null
                    ? Collections.emptyList()
                    : Stream.concat(
                    response.getValue().getSegment().getBlobItems().stream().map(Transforms::toPathDeletedItem),
                    response.getValue().getSegment().getBlobPrefixes().stream()
                        .map(Transforms::toPathDeletedItem)
                ).collect(Collectors.toList());
                return new PagedResponseBase<>(
                    response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    value,
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders());
            });
    }

    private Mono<ResponseBase<FileSystemsListBlobHierarchySegmentHeaders, ListBlobsHierarchySegmentResponse>>
        listDeletedPathsSegment(String marker, String prefix, Integer maxResults, Duration timeout, Context context) {
        context = context == null ? Context.NONE : context;

        return StorageImplUtils.applyOptionalTimeout(
            this.blobDataLakeStorageFs.getFileSystems().listBlobHierarchySegmentWithResponseAsync(
                prefix, null, marker, maxResults,
                null, ListBlobsShowOnly.DELETED, null, null, context), timeout)
            .onErrorMap(ModelHelper::mapToDataLakeStorageException);
    }

    /**
     * Creates a new file within a file system. By default, this method will not overwrite an existing file. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String -->
     * <pre>
     * Mono&lt;DataLakeFileAsyncClient&gt; fileClient = client.createFile&#40;fileName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String -->
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeFileAsyncClient> createFile(String fileName) {
        return createFile(fileName, false);
    }

    /**
     * Creates a new file within a file system. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;* Default value. *&#47;
     * Mono&lt;DataLakeFileAsyncClient&gt; fClient = client.createFile&#40;fileName, overwrite&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String-boolean -->
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param overwrite Whether to overwrite, should a file exist.
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeFileAsyncClient> createFile(String fileName, boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }

        return createFileWithResponse(fileName, new DataLakePathCreateOptions().setRequestConditions(requestConditions))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new file within a file system. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * Mono&lt;Response&lt;DataLakeFileAsyncClient&gt;&gt; newFileClient = client.createFileWithResponse&#40;fileName, permissions,
     *     umask, httpHeaders, Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;, requestConditions&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions -->
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the file. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeFileAsyncClient>> createFileWithResponse(String fileName,
        String permissions, String umask, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
            .setPathHttpHeaders(headers)
            .setMetadata(metadata)
            .setRequestConditions(requestConditions);

        return createFileWithResponse(fileName, options);
    }

    /**
     * Creates a new file within a file system. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileWithResponse#String-DataLakePathCreateOptions -->
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
     * String leaseId = CoreUtils.randomUuid&#40;&#41;.toString&#40;&#41;;
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
     * Mono&lt;Response&lt;DataLakeFileAsyncClient&gt;&gt; newFileClient = client.createFileWithResponse&#40;fileName, options&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileWithResponse#String-DataLakePathCreateOptions -->
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param options {@link DataLakePathCreateOptions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeFileAsyncClient>> createFileWithResponse(String fileName, DataLakePathCreateOptions options) {
        DataLakeFileAsyncClient dataLakeFileAsyncClient;
        try {
            dataLakeFileAsyncClient = getFileAsyncClient(fileName);
            return dataLakeFileAsyncClient.createWithResponse(options).map(response -> new SimpleResponse<>(response, dataLakeFileAsyncClient));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new file within a file system if it does not exist. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileIfNotExists#String -->
     * <pre>
     * DataLakeFileAsyncClient fileAsyncClient = client.createFileIfNotExists&#40;fileName&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileIfNotExists#String -->
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeFileAsyncClient> createFileIfNotExists(String fileName) {
        return createFileIfNotExistsWithResponse(fileName, new DataLakePathCreateOptions()).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new file within a file system if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions -->
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
     * client.createFileIfNotExistsWithResponse&#40;fileName, options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions -->
     *
     * @param fileName Name of the file to create. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param options {@link DataLakePathCreateOptions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a
     * {@link DataLakeFileAsyncClient} used to interact with the file created. If {@link Response}'s status code is 201,
     * a new file was successfully created. If status code is 409, a file with the same name already existed
     * at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeFileAsyncClient>> createFileIfNotExistsWithResponse(String fileName,
        DataLakePathCreateOptions options) {
        options = options == null ? new DataLakePathCreateOptions() : options;
        options.setRequestConditions(new DataLakeRequestConditions()
            .setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD));
        try {
            return createFileWithResponse(fileName, options)
                .onErrorResume(t -> t instanceof DataLakeStorageException && ((DataLakeStorageException) t)
                    .getStatusCode() == 409,
                    t -> {
                        HttpResponse response = ((DataLakeStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), getFileAsyncClient(fileName)));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes the specified file in the file system. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFile#String -->
     * <pre>
     * client.deleteFile&#40;fileName&#41;.subscribe&#40;response -&gt;
     *     System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFile#String -->
     *
     * @param fileName Name of the file to delete. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteFile(String fileName) {
        return deleteFileWithResponse(fileName, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified file in the file system. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     *
     * client.deleteFileWithResponse&#40;fileName, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions -->
     *
     * @param fileName Name of the file to delete. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteFileWithResponse(String fileName, DataLakeRequestConditions requestConditions) {
        DataLakeFileAsyncClient dataLakeFileAsyncClient;
        try {
            dataLakeFileAsyncClient = getFileAsyncClient(fileName);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }

        return dataLakeFileAsyncClient.deleteWithResponse(requestConditions);
    }

    /**
     * Deletes the specified file in the file system if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileIfExists#String -->
     * <pre>
     * client.deleteFileIfExists&#40;fileName&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileIfExists#String -->
     *
     * @param fileName Name of the file to delete. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @return a reactive response signaling completion. {@code true} indicates that the specified file was successfully
     * deleted, {@code false} indicates that the specified file did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteFileIfExists(String fileName) {
        return deleteFileIfExistsWithResponse(fileName, new DataLakePathDeleteOptions()).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified file in the file system if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;false&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * client.deleteFileIfExistsWithResponse&#40;fileName, options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions -->
     *
     * @param fileName Name of the file to delete. If the path name contains special characters, pass in the url encoded
     * version of the path name.
     * @param options {@link DataLakePathDeleteOptions}
     * @return A reactive response signaling completion. If {@link Response}'s status code is 200, the file was
     * successfully deleted. If status code is 404, the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteFileIfExistsWithResponse(String fileName, DataLakePathDeleteOptions options) {
        try {
            return getFileAsyncClient(fileName).deleteIfExistsWithResponse(options);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new directory within a file system. By default, this method will not overwrite an existing directory.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String -->
     * <pre>
     * Mono&lt;DataLakeDirectoryAsyncClient&gt; directoryClient = client.createDirectory&#40;directoryName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String -->
     *
     * @param directoryName Name of the directory to create. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @return A {@link Mono} containing a {@link DataLakeDirectoryAsyncClient} used to interact with the directory
     * created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeDirectoryAsyncClient> createDirectory(String directoryName) {
        return createDirectory(directoryName, false);
    }

    /**
     * Creates a new directory within a file system. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;* Default value. *&#47;
     * Mono&lt;DataLakeDirectoryAsyncClient&gt; dClient = client.createDirectory&#40;directoryName, overwrite&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String-boolean -->
     *
     * @param directoryName Name of the directory to create. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param overwrite Whether to overwrite, should a directory exist.
     * @return A {@link Mono} containing a {@link DataLakeDirectoryAsyncClient} used to interact with the directory
     * created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeDirectoryAsyncClient> createDirectory(String directoryName, boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }

        return createDirectoryWithResponse(directoryName, new DataLakePathCreateOptions()
            .setRequestConditions(requestConditions))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new directory within a file system. If a directory with the same name already exists, the directory
     * will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * Mono&lt;Response&lt;DataLakeDirectoryAsyncClient&gt;&gt; newDirectoryClient = client.createDirectoryWithResponse&#40;
     *     directoryName, permissions, umask, httpHeaders, Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;,
     *     requestConditions&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions -->
     *
     * @param directoryName Name of the directory to create. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param permissions POSIX access permissions for the directory owner, the directory owning group, and others.
     * @param umask Restricts permissions of the directory to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeDirectoryAsyncClient} used to interact with the directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeDirectoryAsyncClient>> createDirectoryWithResponse(String directoryName,
        String permissions, String umask, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
            .setPathHttpHeaders(headers)
            .setMetadata(metadata)
            .setRequestConditions(requestConditions);
        try {
            return createDirectoryWithResponse(directoryName, options);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new directory within a file system. If a directory with the same name already exists, the directory
     * will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryWithResponse#String-DataLakePathCreateOptions -->
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
     * String leaseId = CoreUtils.randomUuid&#40;&#41;.toString&#40;&#41;;
     * Integer duration = 15;
     *
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
     * Mono&lt;Response&lt;DataLakeDirectoryAsyncClient&gt;&gt; newDirectoryClient = client.createDirectoryWithResponse&#40;
     *     directoryName, options&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryWithResponse#String-DataLakePathCreateOptions -->
     *
     * @param directoryName Name of the directory to create. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param options {@link DataLakePathCreateOptions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeDirectoryAsyncClient} used to interact with the directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeDirectoryAsyncClient>> createDirectoryWithResponse(String directoryName, DataLakePathCreateOptions options) {
        DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient;
        try {
            dataLakeDirectoryAsyncClient = getDirectoryAsyncClient(directoryName);
            return dataLakeDirectoryAsyncClient.createWithResponse(options).map(response ->
                new SimpleResponse<>(response, dataLakeDirectoryAsyncClient));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new directory within a file system if it does not exist.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryIfNotExists#String -->
     * <pre>
     * DataLakeDirectoryAsyncClient directoryAsyncClient = client.createDirectoryIfNotExists&#40;directoryName&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryIfNotExists#String -->
     *
     * @param directoryName Name of the directory to create. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @return A {@link Mono} containing a {@link DataLakeDirectoryAsyncClient} used to interact with the directory
     * created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeDirectoryAsyncClient> createDirectoryIfNotExists(String directoryName) {
        return createDirectoryIfNotExistsWithResponse(directoryName, new DataLakePathCreateOptions())
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new directory within a file system if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions -->
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
     * client.createDirectoryIfNotExistsWithResponse&#40;directoryName, options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions -->
     *
     * @param directoryName Name of the directory to create. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param options {@link DataLakePathCreateOptions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a
     * {@link DataLakeDirectoryAsyncClient} used to interact with the directory created. If {@link Response}'s status
     * code is 201, a new directory was successfully created. If status code is 409, a directory with the same name
     * already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeDirectoryAsyncClient>> createDirectoryIfNotExistsWithResponse(String directoryName,
        DataLakePathCreateOptions options) {
        options = options == null ? new DataLakePathCreateOptions() : options;
        options.setRequestConditions(new DataLakeRequestConditions()
            .setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD));
        try {
            return createDirectoryWithResponse(directoryName, options)
                .onErrorResume(t -> t instanceof DataLakeStorageException && ((DataLakeStorageException) t)
                    .getStatusCode() == 409,
                    t -> {
                        HttpResponse response = ((DataLakeStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), getDirectoryAsyncClient(directoryName)));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes the specified directory in the file system. If the directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectory#String -->
     * <pre>
     * client.deleteDirectory&#40;directoryName&#41;.subscribe&#40;response -&gt;
     *     System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectory#String -->
     *
     * @param directoryName Name of the directory to delete. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDirectory(String directoryName) {
        return deleteDirectoryWithResponse(directoryName, false, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified directory in the file system. If the directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     *
     * client.deleteDirectoryWithResponse&#40;directoryName, recursive, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions -->
     *
     * @param directoryName Name of the directory to delete. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param recursive Whether to delete all paths beneath the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDirectoryWithResponse(String directoryName, boolean recursive,
        DataLakeRequestConditions requestConditions) {
        DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient;
        try {
            dataLakeDirectoryAsyncClient = getDirectoryAsyncClient(directoryName);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }

        return dataLakeDirectoryAsyncClient.deleteWithResponse(recursive, requestConditions);
    }

    /**
     * Deletes the specified directory in the file system if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryIfExists#String -->
     * <pre>
     * client.deleteDirectoryIfExists&#40;fileName&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryIfExists#String -->
     *
     * @param directoryName Name of the directory to delete. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @return a reactive response signaling completion. {@code true} indicates that the specified directory was
     * successfully deleted, {@code false} indicates that the specified directory did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteDirectoryIfExists(String directoryName) {
        return deleteDirectoryIfExistsWithResponse(directoryName, new DataLakePathDeleteOptions())
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified directory in the file system if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;recursive&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * client.deleteDirectoryIfExistsWithResponse&#40;directoryName, options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions -->
     *
     * @param directoryName Name of the directory to delete. If the path name contains special characters, pass in the
     * url encoded version of the path name.
     * @param options {@link DataLakePathDeleteOptions}
     * @return A reactive response signaling completion. If {@link Response}'s status code is 200, the file was
     * successfully deleted. If status code is 404, the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteDirectoryIfExistsWithResponse(String directoryName,
        DataLakePathDeleteOptions options) {
        try {
            return getDirectoryAsyncClient(directoryName).deleteIfExistsWithResponse(options);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Restores a soft deleted path in the file system. For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.undeletePath#String-String -->
     * <pre>
     * client.undeletePath&#40;deletedPath, deletionId&#41;.doOnSuccess&#40;response -&gt; System.out.println&#40;&quot;Completed undelete&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.undeletePath#String-String -->
     *
     * @param deletedPath The deleted path
     * @param deletionId deletion ID associated with the soft deleted path that uniquely identifies a resource if
     * multiple have been soft deleted at this location.
     * You can get soft deleted paths and their associated deletion IDs with {@link #listDeletedPaths()}.
     * @return A reactive response signalling completion.
     * @throws NullPointerException if deletedPath or deletionId is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakePathAsyncClient> undeletePath(String deletedPath, String deletionId) {
        return undeletePathWithResponse(deletedPath, deletionId).flatMap(FluxUtil::toMono);
    }

    /**
     * Restores a soft deleted path in the file system. For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.undeletePathWithResponse#String-String -->
     * <pre>
     * client.undeletePathWithResponse&#40;deletedPath, deletionId&#41;
     *     .doOnSuccess&#40;response -&gt; System.out.println&#40;&quot;Completed undelete&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.undeletePathWithResponse#String-String -->
     *
     * @param deletedPath The deleted path
     * @param deletionId deletion ID associated with the soft deleted path that uniquely identifies a resource if
     * multiple have been soft deleted at this location.
     * You can get soft deleted paths and their associated deletion IDs with {@link #listDeletedPaths()}.
     * @return A reactive response signalling completion.
     * @throws NullPointerException if deletedPath or deletionId is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakePathAsyncClient>> undeletePathWithResponse(String deletedPath, String deletionId) {
        try {
            return withContext(context -> undeletePathWithResponse(deletedPath, deletionId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<DataLakePathAsyncClient>> undeletePathWithResponse(String deletedPath, String deletionId,
        Context context) {
        Objects.requireNonNull(deletedPath);
        Objects.requireNonNull(deletionId);

        context = context == null ? Context.NONE : context;
        String blobUrl = DataLakeImplUtils.endpointToDesiredEndpoint(blobDataLakeStorageFs.getUrl(), "blob", "dfs");

        // This instance is to have a datalake impl that points to the blob endpoint
        AzureDataLakeStorageRestAPIImpl blobDataLakeStoragePath = new AzureDataLakeStorageRestAPIImplBuilder()
            .pipeline(blobDataLakeStorageFs.getHttpPipeline())
            .url(blobUrl)
            .fileSystem(blobDataLakeStorageFs.getFileSystem())
            .path(deletedPath)
            .version(serviceVersion.getVersion())
            .buildClient();

        // Initial rest call
        return blobDataLakeStoragePath.getPaths().undeleteWithResponseAsync(null,
            String.format("?%s=%s", Constants.UrlConstants.DELETIONID_QUERY_PARAMETER, deletionId), null, context)
                .onErrorMap(ModelHelper::mapToDataLakeStorageException)
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException)
                // Construct the new client and final response from the undelete + getProperties responses
                .map(response -> {
                    DataLakePathAsyncClient client = new DataLakePathAsyncClient(getHttpPipeline(), getAccountUrl(),
                        serviceVersion, accountName, fileSystemName, deletedPath,
                        PathResourceType.fromString(response.getDeserializedHeaders().getXMsResourceType()),
                        blobContainerAsyncClient.getBlobAsyncClient(deletedPath, null)
                            .getBlockBlobAsyncClient(), sasToken,
                            Transforms.fromBlobCpkInfo(blobContainerAsyncClient.getCustomerProvidedKey()),
                        isTokenCredentialAuthenticated);
                    if (PathResourceType.DIRECTORY.equals(client.pathResourceType)) {
                        return new SimpleResponse<>(response, new DataLakeDirectoryAsyncClient(client));
                    } else if (PathResourceType.FILE.equals(client.pathResourceType)) {
                        return new SimpleResponse<>(response, new DataLakeFileAsyncClient(client));
                    } else {
                        throw LOGGER.logExceptionAsError(new IllegalStateException("'pathClient' expected to be either "
                            + "a file or directory client."));
                    }
                });
    }

    /**
     * Sets the file system's permissions. The permissions indicate whether paths in a file system may be accessed
     * publicly. Note that, for each signed identifier, we will truncate the start and expiry times to the nearest
     * second to ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setAccessPolicy#PublicAccessType-List -->
     * <pre>
     * DataLakeSignedIdentifier identifier = new DataLakeSignedIdentifier&#40;&#41;
     *     .setId&#40;&quot;name&quot;&#41;
     *     .setAccessPolicy&#40;new DataLakeAccessPolicy&#40;&#41;
     *         .setStartsOn&#40;OffsetDateTime.now&#40;&#41;&#41;
     *         .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;7&#41;&#41;
     *         .setPermissions&#40;&quot;permissionString&quot;&#41;&#41;;
     *
     * client.setAccessPolicy&#40;PublicAccessType.CONTAINER, Collections.singletonList&#40;identifier&#41;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Set access policy completed%n&quot;&#41;,
     *     error -&gt; System.out.printf&#40;&quot;Set access policy failed: %s%n&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setAccessPolicy#PublicAccessType-List -->
     *
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link DataLakeSignedIdentifier} objects that specify the permissions for the file
     * system.
     * Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setAccessPolicy(PublicAccessType accessType, List<DataLakeSignedIdentifier> identifiers) {
        return setAccessPolicyWithResponse(accessType, identifiers, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the file system's permissions. The permissions indicate whether paths in a file system may be accessed
     * publicly. Note that, for each signed identifier, we will truncate the start and expiry times to the nearest
     * second to ensure the time formatting is compatible with the service. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-DataLakeRequestConditions -->
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
     * client.setAccessPolicyWithResponse&#40;PublicAccessType.CONTAINER, Collections.singletonList&#40;identifier&#41;, requestConditions&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Set access policy completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-DataLakeRequestConditions -->
     *
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param identifiers A list of {@link DataLakeSignedIdentifier} objects that specify the permissions for the file
     * system.
     * Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information. Passing null will clear all access policies.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response signalling completion.
     * @throws UnsupportedOperationException If either {@link DataLakeRequestConditions#getIfMatch()} or
     * {@link DataLakeRequestConditions#getIfNoneMatch()} is set.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setAccessPolicyWithResponse(PublicAccessType accessType,
        List<DataLakeSignedIdentifier> identifiers, DataLakeRequestConditions requestConditions) {
        return blobContainerAsyncClient.setAccessPolicyWithResponse(Transforms.toBlobPublicAccessType(accessType),
                Transforms.toBlobIdentifierList(identifiers), Transforms.toBlobRequestConditions(requestConditions))
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Returns the file system's permissions. The permissions indicate whether file system's paths may be accessed
     * publicly. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getAccessPolicy -->
     * <pre>
     * client.getAccessPolicy&#40;&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Data Lake Access Type: %s%n&quot;, response.getDataLakeAccessType&#40;&#41;&#41;;
     *
     *     for &#40;DataLakeSignedIdentifier identifier : response.getIdentifiers&#40;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Identifier Name: %s, Permissions %s%n&quot;,
     *             identifier.getId&#40;&#41;,
     *             identifier.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getAccessPolicy -->
     *
     * @return A reactive response containing the file system access policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<FileSystemAccessPolicies> getAccessPolicy() {
        return getAccessPolicyWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the file system's permissions. The permissions indicate whether file system's paths may be accessed
     * publicly. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-acl">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getAccessPolicyWithResponse#String -->
     * <pre>
     * client.getAccessPolicyWithResponse&#40;leaseId&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Data Lake Access Type: %s%n&quot;, response.getValue&#40;&#41;.getDataLakeAccessType&#40;&#41;&#41;;
     *
     *     for &#40;DataLakeSignedIdentifier identifier : response.getValue&#40;&#41;.getIdentifiers&#40;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Identifier Name: %s, Permissions %s%n&quot;,
     *             identifier.getId&#40;&#41;,
     *             identifier.getAccessPolicy&#40;&#41;.getPermissions&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getAccessPolicyWithResponse#String -->
     *
     * @param leaseId The lease ID the active lease on the file system must match.
     * @return A reactive response containing the file system access policy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<FileSystemAccessPolicies>> getAccessPolicyWithResponse(String leaseId) {
        return blobContainerAsyncClient.getAccessPolicyWithResponse(leaseId)
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException)
            .map(response -> new SimpleResponse<>(response,
                Transforms.toFileSystemAccessPolicies(response.getValue())));
    }

    // TODO: Reintroduce this API once service starts supporting it.
//    Mono<DataLakeFileSystemAsyncClient> rename(String destinationContainerName) {
//        return renameWithResponse(new FileSystemRenameOptions(destinationContainerName)).flatMap(FluxUtil::toMono);
//    }

    // TODO: Reintroduce this API once service starts supporting it.
//    Mono<Response<DataLakeFileSystemAsyncClient>> renameWithResponse(FileSystemRenameOptions options) {
//        try {
//            return blobContainerAsyncClient.renameWithResponse(Transforms.toBlobContainerRenameOptions(options))
//                .onErrorMap(DataLakeImplUtils::transformBlobStorageException)
//                .map(response -> new SimpleResponse<>(response,
//                    this.getFileSystemAsyncClient(options.getDestinationFileSystemName())));
//        } catch (RuntimeException ex) {
//            return monoError(LOGGER, ex);
//        }
//    }

//    /**
//     * Takes in a destination and creates a DataLakeFileSystemAsyncClient with a new path
//     * @param destinationFileSystem The destination file system
//     * @return A DataLakeFileSystemAsyncClient
//     */
//    DataLakeFileSystemAsyncClient getFileSystemAsyncClient(String destinationFileSystem) {
//        if (CoreUtils.isNullOrEmpty(destinationFileSystem)) {
//            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'destinationFileSystem' can not be set to null"));
//        }
//        // Get current Datalake URL and replace current filesystem with user provided filesystem
//        String newDfsEndpoint = BlobUrlParts.parse(getFileSystemUrl())
//            .setContainerName(destinationFileSystem).toUrl().toString();
//
//        return new DataLakeFileSystemAsyncClient(getHttpPipeline(), newDfsEndpoint, serviceVersion, accountName,
//            destinationFileSystem, prepareBuilderReplacePath(destinationFileSystem).buildAsyncClient(), sasToken);
//    }

    /**
     * Takes in a destination path and creates a ContainerClientBuilder with a new path name
     * @param destinationFileSystem The destination file system
     * @return An updated SpecializedBlobClientBuilder
     */
    BlobContainerClientBuilder prepareBuilderReplacePath(String destinationFileSystem) {
        if (CoreUtils.isNullOrEmpty(destinationFileSystem)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'destinationFileSystem' can not be set to null"));
        }
        // Get current Blob URL and replace current filesystem with user provided filesystem
        String newBlobEndpoint = BlobUrlParts.parse(DataLakeImplUtils.endpointToDesiredEndpoint(getFileSystemUrl(),
            "blob", "dfs")).setContainerName(destinationFileSystem).toUrl().toString();

        return new BlobContainerClientBuilder()
            .pipeline(getHttpPipeline())
            .endpoint(newBlobEndpoint)
            .serviceVersion(TransformUtils.toBlobServiceVersion(getServiceVersion()));
    }

//    BlobContainerAsyncClient getBlobContainerAsyncClient() {
//        return blobContainerAsyncClient;
//    }

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
     * See {@link DataLakeServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information
     * on how to get a user delegation key.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey) {
        return generateUserDelegationSas(dataLakeServiceSasSignatureValues, userDelegationKey, getAccountName(),
            Context.NONE);
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
     * See {@link DataLakeServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information
     * on how to get a user delegation key.
     * @param accountName The account name.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey, String accountName, Context context) {
        return generateUserDelegationSas(dataLakeServiceSasSignatureValues, userDelegationKey, accountName,
            null, context);
    }

    /**
     * Generates a user delegation SAS for the file system using the specified
     * {@link DataLakeServiceSasSignatureValues}.
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a user delegation SAS.
     * </p>
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link DataLakeServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information
     * on how to get a user delegation key.
     * @param accountName The account name.
     * @param stringToSignHandler For debugging purposes only. Returns the string to sign that was used to generate the
     * signature.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey, String accountName, Consumer<String> stringToSignHandler, Context context) {
        return new DataLakeSasImplUtil(dataLakeServiceSasSignatureValues, getFileSystemName())
            .generateUserDelegationSas(userDelegationKey, accountName, stringToSignHandler, context);
    }

    /**
     * Generates a service SAS for the file system using the specified {@link DataLakeServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateSas#DataLakeServiceSasSignatureValues -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * FileSystemSasPermission permission = new FileSystemSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateSas&#40;values&#41;; &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateSas#DataLakeServiceSasSignatureValues -->
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues) {
        return generateSas(dataLakeServiceSasSignatureValues, Context.NONE);
    }

    /**
     * Generates a service SAS for the file system using the specified {@link DataLakeServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateSas#DataLakeServiceSasSignatureValues-Context -->
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
     * <!-- end com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateSas#DataLakeServiceSasSignatureValues-Context -->
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues, Context context) {
        return generateSas(dataLakeServiceSasSignatureValues, null, context);
    }

    /**
     * Generates a service SAS for the file system using the specified {@link DataLakeServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param stringToSignHandler For debugging purposes only. Returns the string to sign that was used to generate the
     * signature.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        Consumer<String> stringToSignHandler, Context context) {
        return new DataLakeSasImplUtil(dataLakeServiceSasSignatureValues, getFileSystemName())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), stringToSignHandler, context);
    }
}
