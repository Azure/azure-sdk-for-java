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
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.models.CpkInfo;
import com.azure.storage.file.datalake.implementation.models.FileSystemsListPathsHeaders;
import com.azure.storage.file.datalake.implementation.models.PathList;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.ModelHelper;
import com.azure.storage.file.datalake.implementation.util.TransformUtils;
import com.azure.storage.file.datalake.models.CustomerProvidedKey;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client that contains directory operations for Azure Storage Data Lake. Operations provided by
 * this client include creating a directory, deleting a directory, renaming a directory, setting metadata and
 * http headers, setting and retrieving access control, getting properties and creating and deleting files and
 * subdirectories.
 *
 * <p>
 * This client is instantiated through {@link DataLakePathClientBuilder} or retrieved via
 * {@link DataLakeFileSystemAsyncClient#getDirectoryAsyncClient(String) getDirectoryAsyncClient}.
 *
 * <p>
 * Please refer to the
 *
 * <a href="https://docs.microsoft.com/azure/storage/blobs/data-lake-storage-introduction">Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = DataLakePathClientBuilder.class, isAsync = true)
public final class DataLakeDirectoryAsyncClient extends DataLakePathAsyncClient {

    private static final ClientLogger LOGGER = new ClientLogger(DataLakeDirectoryAsyncClient.class);

    /**
     * Package-private constructor for use by {@link DataLakePathClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param fileSystemName The file system name.
     * @param directoryName The directory name.
     * @param blockBlobAsyncClient The underlying {@link BlobContainerAsyncClient}
     */
    DataLakeDirectoryAsyncClient(HttpPipeline pipeline, String url, DataLakeServiceVersion serviceVersion,
        String accountName, String fileSystemName, String directoryName, BlockBlobAsyncClient blockBlobAsyncClient,
        AzureSasCredential sasToken, CpkInfo customerProvidedKey, boolean isTokenCredentialAuthenticated) {
        super(pipeline, url, serviceVersion, accountName, fileSystemName, directoryName, PathResourceType.DIRECTORY,
            blockBlobAsyncClient, sasToken, customerProvidedKey, isTokenCredentialAuthenticated);
    }

    DataLakeDirectoryAsyncClient(DataLakePathAsyncClient dataLakePathAsyncClient) {
        super(dataLakePathAsyncClient.getHttpPipeline(), dataLakePathAsyncClient.getAccountUrl(),
            dataLakePathAsyncClient.getServiceVersion(), dataLakePathAsyncClient.getAccountName(),
            dataLakePathAsyncClient.getFileSystemName(), Utility.urlEncode(dataLakePathAsyncClient.pathName),
            PathResourceType.DIRECTORY, dataLakePathAsyncClient.getBlockBlobAsyncClient(),
            dataLakePathAsyncClient.getSasToken(), dataLakePathAsyncClient.getCpkInfo(),
            dataLakePathAsyncClient.isTokenCredentialAuthenticated());
    }

    /**
     * Gets the URL of the directory represented by this client on the Data Lake service.
     *
     * @return the URL.
     */
    public String getDirectoryUrl() {
        return getPathUrl();
    }

    /**
     * Gets the path of this directory, not including the name of the resource itself.
     *
     * @return The path of the directory.
     */
    public String getDirectoryPath() {
        return getObjectPath();
    }

    /**
     * Gets the name of this directory, not including its full path.
     *
     * @return The name of the directory.
     */
    public String getDirectoryName() {
        return getObjectName();
    }

    /**
     * Creates a new {@link DataLakeDirectoryAsyncClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the directory,
     * pass {@code null} to use no customer provided key.
     * @return a {@link DataLakeDirectoryAsyncClient} with the specified {@code customerProvidedKey}.
     */
    public DataLakeDirectoryAsyncClient getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey) {
        CpkInfo finalCustomerProvidedKey = null;
        if (customerProvidedKey != null) {
            finalCustomerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }
        return new DataLakeDirectoryAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(),
            getAccountName(), getFileSystemName(), getObjectPath(), this.blockBlobAsyncClient, getSasToken(),
            finalCustomerProvidedKey, isTokenCredentialAuthenticated());
    }

    /**
     * Deletes a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.delete -->
     * <pre>
     * client.delete&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.delete -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        return deleteWithResponse(false, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Recursively deletes a directory and all contents within the directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteRecursively -->
     * <pre>
     * client.deleteRecursively&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteRecursively -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRecursively() {
        return deleteRecursivelyWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Recursively deletes a directory and all contents within the directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteWithResponse#boolean-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     *
     * client.deleteWithResponse&#40;recursive, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteWithResponse#boolean-DataLakeRequestConditions -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRecursivelyWithResponse(DataLakeRequestConditions requestConditions) {
        try {
            return withContext(context -> deleteWithResponse(true, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteWithResponse#boolean-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     *
     * client.deleteWithResponse&#40;recursive, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteWithResponse#boolean-DataLakeRequestConditions -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param recursive Whether to delete all paths beneath the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse(boolean recursive, DataLakeRequestConditions requestConditions) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        try {
            return withContext(context -> deleteWithResponse(recursive, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a directory if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteIfExists -->
     * <pre>
     * client.deleteIfExists&#40;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteIfExists -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @return a reactive response signaling completion. {@code true} indicates that the directory was successfully
     * deleted, {@code true} indicates that the directory did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteIfExists() {
        return deleteIfExistsWithResponse(new DataLakePathDeleteOptions()).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a directory if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;recursive&#41;
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
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param options {@link DataLakePathDeleteOptions}
     *
     * @return A reactive response signaling completion. If {@link Response}'s status code is 200, the directory was
     * successfully deleted. If status code is 404, the directory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteIfExistsWithResponse(DataLakePathDeleteOptions options) {
        try {
            options = options == null ? new DataLakePathDeleteOptions() : options;
            return deleteWithResponse(options.getIsRecursive(), options.getRequestConditions())
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorResume(t -> t
                instanceof DataLakeStorageException && ((DataLakeStorageException) t).getStatusCode() == 404,
                    t -> {
                        HttpResponse response = ((DataLakeStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), false));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new DataLakeFileAsyncClient object by concatenating fileName to the end of
     * DataLakeDirectoryAsyncClient's URL. The new DataLakeFileAsyncClient uses the same request policy pipeline as the
     * DataLakeDirectoryAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.getFileAsyncClient#String -->
     * <pre>
     * DataLakeFileAsyncClient dataLakeFileClient = client.getFileAsyncClient&#40;fileName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.getFileAsyncClient#String -->
     *
     * @param fileName A {@code String} representing the name of the file.
     * @return A new {@link DataLakeFileAsyncClient} object which references the file with the specified name in this
     * file system.
     */
    public DataLakeFileAsyncClient getFileAsyncClient(String fileName) {
        Objects.requireNonNull(fileName, "'fileName' can not be set to null");

        String pathPrefix = getObjectPath().isEmpty() ? "" : getObjectPath() + "/";

        BlockBlobAsyncClient blockBlobAsyncClient = prepareBuilderAppendPath(pathPrefix + fileName)
            .buildBlockBlobAsyncClient();

        return new DataLakeFileAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getFileSystemName(), pathPrefix + fileName, blockBlobAsyncClient, this.getSasToken(), getCpkInfo(),
            isTokenCredentialAuthenticated());
    }

    /**
     * Creates a new file within a directory. By default, this method will not overwrite an existing file.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String -->
     * <pre>
     * DataLakeFileAsyncClient fileClient = client.createFile&#40;fileName&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String -->
     *
     * @param fileName Name of the file to create.
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeFileAsyncClient> createFile(String fileName) {
        return createFile(fileName, false);
    }

    /**
     * Creates a new file within a directory. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;* Default value. *&#47;
     * DataLakeFileAsyncClient fClient = client.createFile&#40;fileName, overwrite&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String-boolean -->
     *
     * @param fileName Name of the file to create.
     * @param overwrite Whether to overwrite, should the file exist.
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeFileAsyncClient> createFile(String fileName, boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }

        return createFileWithResponse(fileName, new DataLakePathCreateOptions().setRequestConditions(requestConditions)).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new file within a directory. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * DataLakeFileAsyncClient newFileClient = client.createFileWithResponse&#40;fileName,
     *     permissions, umask, httpHeaders, Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;, requestConditions
     * &#41;.block&#40;&#41;.getValue&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions -->
     *
     * @param fileName Name of the file to create.
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
    public Mono<Response<DataLakeFileAsyncClient>> createFileWithResponse(String fileName, String permissions,
        String umask, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
            .setPathHttpHeaders(headers)
            .setMetadata(metadata)
            .setRequestConditions(requestConditions);

        try {
            return createFileWithResponse(fileName, options);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new file within a directory. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileWithResponse#String-DataLakePathCreateOptions -->
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
     * DataLakeFileAsyncClient newFileClient = client.createFileWithResponse&#40;fileName, options&#41;.block&#40;&#41;.getValue&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileWithResponse#String-DataLakePathCreateOptions -->
     *
     * @param fileName Name of the file to create.
     * @param options {@link DataLakePathCreateOptions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeFileAsyncClient>> createFileWithResponse(String fileName, DataLakePathCreateOptions options) {
        DataLakeFileAsyncClient dataLakeFileAsyncClient;
        try {
            dataLakeFileAsyncClient = getFileAsyncClient(fileName);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }

        return dataLakeFileAsyncClient.createWithResponse(options)
            .map(response -> new SimpleResponse<>(response, dataLakeFileAsyncClient));
    }

    /**
     * Creates a new file within a directory if it does not exist. By default this method will not overwrite an existing
     * file. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileIfNotExists#String -->
     * <pre>
     * DataLakeFileAsyncClient fileClient = client.createFileIfNotExists&#40;fileName&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileIfNotExists#String -->
     *
     * @param fileName Name of the file to create.
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeFileAsyncClient> createFileIfNotExists(String fileName) {
        return createFileIfNotExistsWithResponse(fileName, new DataLakePathCreateOptions()).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new file within a directory if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions-->
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
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions -->
     *
     * @param fileName Name of the file to create.
     * @param options {@link DataLakePathCreateOptions}
     * metadata key or value, it must be removed or encoded.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a
     * {@link DataLakeFileAsyncClient} used to interact with the file created. If {@link Response}'s status code is 201,
     * a new file was successfully created. If status code is 409, a file with the same name already existed
     * at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeFileAsyncClient>> createFileIfNotExistsWithResponse(String fileName,
        DataLakePathCreateOptions options) {
        DataLakeFileAsyncClient dataLakeFileAsyncClient = getFileAsyncClient(fileName);
        try {
            return dataLakeFileAsyncClient.createIfNotExistsWithResponse(options)
                .map(response -> new SimpleResponse<>(response, dataLakeFileAsyncClient));
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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFile#String -->
     * <pre>
     * client.deleteFile&#40;fileName&#41;.subscribe&#40;response -&gt;
     *     System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFile#String -->
     *
     * @param fileName Name of the file to delete.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteFile(String fileName) {
        return deleteFileWithResponse(fileName, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified file in the directory. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     *
     * client.deleteFileWithResponse&#40;fileName, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions -->
     *
     * @param fileName Name of the file to delete.
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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileIfExists#String -->
     * <pre>
     * client.deleteFileIfExists&#40;fileName&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileIfExists#String -->
     *
     * @param fileName Name of the file to delete.
     * @return a reactive response signaling completion. {@code true} indicates that the specified file was successfully
     * deleted, {@code false} indicates that the specified file did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteFileIfExists(String fileName) {
        return deleteFileIfExistsWithResponse(fileName, new DataLakePathDeleteOptions()).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified file in the directory if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions -->
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
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions -->
     *
     * @param fileName Name of the file to delete.
     * @param options {@link DataLakePathDeleteOptions}
     * @return A reactive response signaling completion. If {@link Response}'s status code is 200, the specified file was
     * successfully deleted. If status code is 404, the specified file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteFileIfExistsWithResponse(String fileName, DataLakePathDeleteOptions options) {
        try {
            return withContext(context -> this.deleteFileIfExistsWithResponse(fileName, options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> deleteFileIfExistsWithResponse(String fileName, DataLakePathDeleteOptions options,
        Context context) {
        try {
            return getFileAsyncClient(fileName).deleteIfExistsWithResponse(options, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new DataLakeDirectoryAsyncClient object by concatenating subdirectoryName to the end of
     * DataLakeDirectoryAsyncClient's URL. The new DataLakeDirectoryAsyncClient uses the same request policy pipeline
     * as the DataLakeDirectoryAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.getSubdirectoryAsyncClient#String -->
     * <pre>
     * DataLakeDirectoryAsyncClient dataLakeDirectoryClient = client.getSubdirectoryAsyncClient&#40;directoryName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.getSubdirectoryAsyncClient#String -->
     *
     * @param subdirectoryName A {@code String} representing the name of the sub-directory.
     * @return A new {@link DataLakeDirectoryAsyncClient} object which references the directory with the specified name
     * in this file system.
     */
    public DataLakeDirectoryAsyncClient getSubdirectoryAsyncClient(String subdirectoryName) {
        Objects.requireNonNull(subdirectoryName, "'subdirectoryName' can not be set to null");

        String pathPrefix = getObjectPath().isEmpty() ? "" : getObjectPath() + "/";

        BlockBlobAsyncClient blockBlobAsyncClient = prepareBuilderAppendPath(pathPrefix + subdirectoryName)
            .buildBlockBlobAsyncClient();

        return new DataLakeDirectoryAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(),
            getAccountName(), getFileSystemName(), pathPrefix + subdirectoryName, blockBlobAsyncClient,
            this.getSasToken(), getCpkInfo(), isTokenCredentialAuthenticated());
    }

    /**
     * Creates a new sub-directory within a directory. By default, this method will not overwrite an existing
     * sub-directory. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectory#String -->
     * <pre>
     * DataLakeDirectoryAsyncClient directoryClient = client.createSubdirectory&#40;directoryName&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectory#String -->
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @return A {@link Mono} containing a {@link DataLakeDirectoryAsyncClient} used to interact with the directory
     * created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeDirectoryAsyncClient> createSubdirectory(String subdirectoryName) {
        return createSubdirectory(subdirectoryName, false);
    }

    /**
     * Creates a new sub-directory within a directory. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectory#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;* Default value. *&#47;
     * DataLakeDirectoryAsyncClient dClient = client.createSubdirectory&#40;directoryName, overwrite&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectory#String-boolean -->
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @param overwrite Whether to overwrite, should the subdirectory exist.
     * @return A {@link Mono} containing a {@link DataLakeDirectoryAsyncClient} used to interact with the directory
     * created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeDirectoryAsyncClient> createSubdirectory(String subdirectoryName, boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }

        return createSubdirectoryWithResponse(subdirectoryName, new DataLakePathCreateOptions()
            .setRequestConditions(requestConditions))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * DataLakeDirectoryAsyncClient newDirectoryClient = client.createSubdirectoryWithResponse&#40;
     *     directoryName, permissions, umask, httpHeaders, Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;,
     *     requestConditions
     * &#41;.block&#40;&#41;.getValue&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions -->
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @param permissions POSIX access permissions for the sub-directory owner, the sub-directory owning group, and
     * others.
     * @param umask Restricts permissions of the sub-directory to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeDirectoryAsyncClient} used to interact with the sub-directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeDirectoryAsyncClient>> createSubdirectoryWithResponse(String subdirectoryName,
        String permissions, String umask, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
            .setPathHttpHeaders(headers)
            .setMetadata(metadata)
            .setRequestConditions(requestConditions);

        try {
            return createSubdirectoryWithResponse(subdirectoryName, options);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryWithResponse#String-DataLakePathCreateOptions -->
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
     * DataLakeDirectoryAsyncClient newDirectoryClient = client.createSubdirectoryWithResponse&#40;directoryName, options&#41;
     *     .block&#40;&#41;.getValue&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryWithResponse#String-DataLakePathCreateOptions -->
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @param options {@link DataLakePathCreateOptions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeDirectoryAsyncClient} used to interact with the sub-directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeDirectoryAsyncClient>> createSubdirectoryWithResponse(String subdirectoryName,
        DataLakePathCreateOptions options) {
        DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient;
        try {
            dataLakeDirectoryAsyncClient = getSubdirectoryAsyncClient(subdirectoryName);
            return dataLakeDirectoryAsyncClient.createWithResponse(options)
                .map(response -> new SimpleResponse<>(response, dataLakeDirectoryAsyncClient));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new subdirectory within a directory if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryIfNotExists#String -->
     * <pre>
     * DataLakeDirectoryAsyncClient subdirectoryClient = client.createSubdirectoryIfNotExists&#40;directoryName&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryIfNotExists#String -->
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @return A {@link Mono} containing a {@link DataLakeDirectoryAsyncClient} used to interact with the subdirectory
     * created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeDirectoryAsyncClient> createSubdirectoryIfNotExists(String subdirectoryName) {
        return createSubdirectoryIfNotExistsWithResponse(subdirectoryName, new DataLakePathCreateOptions())
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new sub-directory within a directory if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions -->
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
     * client.createSubdirectoryIfNotExistsWithResponse&#40;directoryName, options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions -->
     *
     * @param subdirectoryName Name of the subdirectory to create.
     * @param options {@link DataLakePathCreateOptions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a
     * {@link DataLakeDirectoryAsyncClient} used to interact with the subdirectory created. If {@link Response}'s status
     * code is 201, a new subdirectory was successfully created. If status code is 409, a subdirectory with the same
     * name already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeDirectoryAsyncClient>> createSubdirectoryIfNotExistsWithResponse(
        String subdirectoryName, DataLakePathCreateOptions options) {
        options = options == null ? new DataLakePathCreateOptions() : options;
        options.setRequestConditions(new DataLakeRequestConditions()
            .setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD));
        try {
            return createSubdirectoryWithResponse(subdirectoryName, options.getPermissions(), options.getUmask(),
                options.getPathHttpHeaders(), options.getMetadata(), options.getRequestConditions())
                .onErrorResume(t -> t instanceof DataLakeStorageException && ((DataLakeStorageException) t)
                    .getStatusCode() == 409,
                    t -> {
                        HttpResponse response = ((DataLakeStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), getSubdirectoryAsyncClient(subdirectoryName)));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes the specified sub-directory in the directory. If the sub-directory doesn't exist or is not empty the
     * operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectory#String -->
     * <pre>
     * client.deleteSubdirectory&#40;directoryName&#41;.subscribe&#40;response -&gt;
     *     System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectory#String -->
     *
     * @param subdirectoryName Name of the sub-directory to delete.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteSubdirectory(String subdirectoryName) {
        return deleteSubdirectoryWithResponse(subdirectoryName, false, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified sub-directory in the directory. If the sub-directory doesn't exist or is not empty the
     * operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryWithResponse#String-boolean-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     *
     * client.deleteSubdirectoryWithResponse&#40;directoryName, recursive, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Delete request completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryWithResponse#String-boolean-DataLakeRequestConditions -->
     *
     * @param directoryName Name of the sub-directory to delete.
     * @param recursive Whether to delete all paths beneath the sub-directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteSubdirectoryWithResponse(String directoryName, boolean recursive,
        DataLakeRequestConditions requestConditions) {
        DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient;
        try {
            dataLakeDirectoryAsyncClient = getSubdirectoryAsyncClient(directoryName);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }

        return dataLakeDirectoryAsyncClient.deleteWithResponse(recursive, requestConditions);
    }

    /**
     * Deletes the specified subdirectory in the directory if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryIfExists#String -->
     * <pre>
     * client.deleteSubdirectoryIfExists&#40;directoryName&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryIfExists#String -->
     *
     * @param subdirectoryName Name of the subdirectory to delete.
     * @return A reactive response signaling completion. {@code true} indicates that the subdirectory was deleted.
     * {@code false} indicates the specified subdirectory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteSubdirectoryIfExists(String subdirectoryName) {
        return deleteSubdirectoryIfExistsWithResponse(subdirectoryName, new DataLakePathDeleteOptions())
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified subdirectory in the directory if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;recursive&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * client.deleteSubdirectoryIfExistsWithResponse&#40;directoryName, options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions -->
     *
     * @param directoryName Name of the subdirectory to delete.
     * @param options {@link DataLakePathDeleteOptions}
     * @return A reactive response signaling completion. If {@link Response}'s status code is 200, the specified subdirectory
     * was successfully deleted. If status code is 404, the specified subdirectory does not exist.
     *
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteSubdirectoryIfExistsWithResponse(String directoryName,
        DataLakePathDeleteOptions options) {
        try {
            return deleteSubdirectoryWithResponse(directoryName, options.getIsRecursive(),
                options.getRequestConditions())
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorResume(t -> t instanceof DataLakeStorageException
                    && ((DataLakeStorageException) t).getStatusCode() == 404,
                    t -> {
                        HttpResponse response = ((DataLakeStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), false));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Moves the directory to another location within the file system.
     * For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.rename#String-String -->
     * <pre>
     * DataLakeDirectoryAsyncClient renamedClient = client.rename&#40;fileSystemName, destinationPath&#41;.block&#40;&#41;;
     * System.out.println&#40;&quot;Directory Client has been renamed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.rename#String-String -->
     *
     * @param destinationFileSystem The file system of the destination within the account.
     * {@code null} for the current file system.
     * @param destinationPath Relative path from the file system to rename the directory to, excludes the file system
     * name. For example if you want to move a directory with fileSystem = "myfilesystem", path = "mydir/mysubdir" to
     * another path in myfilesystem (ex: newdir) then set the destinationPath = "newdir"
     * @return A {@link Mono} containing a {@link DataLakeDirectoryAsyncClient} used to interact with the new directory
     * created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataLakeDirectoryAsyncClient> rename(String destinationFileSystem, String destinationPath) {
        return renameWithResponse(destinationFileSystem, destinationPath, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Moves the directory to another location within the file system.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions sourceRequestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * DataLakeRequestConditions destinationRequestConditions = new DataLakeRequestConditions&#40;&#41;;
     *
     * DataLakeDirectoryAsyncClient newRenamedClient = client.renameWithResponse&#40;fileSystemName, destinationPath,
     *     sourceRequestConditions, destinationRequestConditions&#41;.block&#40;&#41;.getValue&#40;&#41;;
     * System.out.println&#40;&quot;Directory Client has been renamed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions -->
     *
     * @param destinationFileSystem The file system of the destination within the account.
     * {@code null} for the current file system.
     * @param destinationPath Relative path from the file system to rename the directory to, excludes the file system
     * name. For example if you want to move a directory with fileSystem = "myfilesystem", path = "mydir/mysubdir" to
     * another path in myfilesystem (ex: newdir) then set the destinationPath = "newdir"
     * @param sourceRequestConditions {@link DataLakeRequestConditions} against the source.
     * @param destinationRequestConditions {@link DataLakeRequestConditions} against the destination.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeDirectoryAsyncClient} used to interact with the directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataLakeDirectoryAsyncClient>> renameWithResponse(String destinationFileSystem,
        String destinationPath, DataLakeRequestConditions sourceRequestConditions,
        DataLakeRequestConditions destinationRequestConditions) {
        try {
            return withContext(context -> renameWithResponse(destinationFileSystem, destinationPath,
                sourceRequestConditions, destinationRequestConditions, context)).map(
                    response -> new SimpleResponse<>(response, new DataLakeDirectoryAsyncClient(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Returns a reactive Publisher emitting all the files/directories in this directory lazily as needed. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/filesystem/list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.listPaths -->
     * <pre>
     * client.listPaths&#40;&#41;.subscribe&#40;path -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, path.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.listPaths -->
     *
     * @return A reactive response emitting the list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PathItem> listPaths() {
        return this.listPaths(false, false, null);
    }

    /**
     * Returns a reactive Publisher emitting all the files/directories in this directory lazily as needed. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/filesystem/list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.listPaths#boolean-boolean-Integer -->
     * <pre>
     * client.listPaths&#40;false, false, 10&#41;
     *     .subscribe&#40;path -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, path.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.listPaths#boolean-boolean-Integer -->
     *
     * @param recursive Specifies if the call should recursively include all paths.
     * @param userPrincipleNameReturned If "true", the user identity values returned by the x-ms-owner, x-ms-group,
     * and x-ms-acl response headers will be transformed from Azure Active Directory Object IDs to User Principal Names.
     * If "false", the values will be returned as Azure Active Directory Object IDs.
     * The default value is false. Note that group and application Object IDs are not translated because they do not
     * have unique friendly names.
     * @param maxResults Specifies the maximum number of blobs to return per page, including all BlobPrefix elements. If
     * the request does not specify maxResults or specifies a value greater than 5,000, the server will return up to
     * 5,000 items per page.
     * @return A reactive response emitting the list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PathItem> listPaths(boolean recursive, boolean userPrincipleNameReturned, Integer maxResults) {
        try {
            return listPathsWithOptionalTimeout(recursive, userPrincipleNameReturned, maxResults, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    PagedFlux<PathItem> listPathsWithOptionalTimeout(boolean recursive, boolean userPrincipleNameReturned,
        Integer maxResults, Duration timeout) {
        BiFunction<String, Integer, Mono<PagedResponse<PathItem>>> func =
            (marker, pageSize) -> listPathsSegment(marker, recursive, userPrincipleNameReturned,
                pageSize == null ? maxResults : pageSize, timeout)
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

        return new PagedFlux<>(pageSize -> func.apply(null, pageSize), func);
    }

    private Mono<ResponseBase<FileSystemsListPathsHeaders, PathList>> listPathsSegment(String marker, boolean recursive,
        boolean userPrincipleNameReturned, Integer maxResults, Duration timeout) {

        return StorageImplUtils.applyOptionalTimeout(
            this.fileSystemDataLakeStorage.getFileSystems().listPathsWithResponseAsync(
                recursive, null, null, marker, getDirectoryPath(), maxResults, userPrincipleNameReturned,
                Context.NONE), timeout)
            .onErrorMap(ModelHelper::mapToDataLakeStorageException);
    }

    /**
     * Prepares a SpecializedBlobClientBuilder with the pathname appended to the end of the current BlockBlobClient's
     * url
     * @param pathName The name of the path to append
     * @return {@link SpecializedBlobClientBuilder}
     */
    SpecializedBlobClientBuilder prepareBuilderAppendPath(String pathName) {
        String blobUrl = DataLakeImplUtils.endpointToDesiredEndpoint(getPathUrl(), "blob", "dfs");

        return new SpecializedBlobClientBuilder()
            .pipeline(getHttpPipeline())
            .serviceVersion(TransformUtils.toBlobServiceVersion(getServiceVersion()))
            .endpoint(blobUrl)
            .blobName(pathName);
    }
}
