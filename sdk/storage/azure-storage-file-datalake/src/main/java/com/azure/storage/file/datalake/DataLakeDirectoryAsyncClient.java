// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
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
import com.azure.storage.file.datalake.implementation.models.FileSystemsListPathsResponse;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.TransformUtils;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathItem;
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
        AzureSasCredential sasToken) {
        super(pipeline, url, serviceVersion, accountName, fileSystemName, directoryName, PathResourceType.DIRECTORY,
            blockBlobAsyncClient, sasToken);
    }

    DataLakeDirectoryAsyncClient(DataLakePathAsyncClient dataLakePathAsyncClient) {
        super(dataLakePathAsyncClient.getHttpPipeline(), dataLakePathAsyncClient.getAccountUrl(),
            dataLakePathAsyncClient.getServiceVersion(), dataLakePathAsyncClient.getAccountName(),
            dataLakePathAsyncClient.getFileSystemName(), Utility.urlEncode(dataLakePathAsyncClient.pathName),
            PathResourceType.DIRECTORY, dataLakePathAsyncClient.getBlockBlobAsyncClient(),
            dataLakePathAsyncClient.getSasToken());
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

        BlockBlobAsyncClient blockBlobAsyncClient = prepareBuilderAppendPath(fileName).buildBlockBlobAsyncClient();

        String pathPrefix = getObjectPath().isEmpty() ? "" : getObjectPath() + "/";

        return new DataLakeFileAsyncClient(getHttpPipeline(), getAccountUrl(),
            getServiceVersion(), getAccountName(), getFileSystemName(), Utility.urlEncode(pathPrefix
            + Utility.urlDecode(fileName)), blockBlobAsyncClient, this.getSasToken());
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

        return createFileWithResponse(fileName, null, null, null, null, requestConditions).flatMap(FluxUtil::toMono);
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
        DataLakeFileAsyncClient dataLakeFileAsyncClient;
        try {
            dataLakeFileAsyncClient = getFileAsyncClient(fileName);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }

        return dataLakeFileAsyncClient.createWithResponse(permissions, umask, headers, metadata, requestConditions)
            .map(response -> new SimpleResponse<>(response, dataLakeFileAsyncClient));
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

        BlockBlobAsyncClient blockBlobAsyncClient = prepareBuilderAppendPath(subdirectoryName)
            .buildBlockBlobAsyncClient();

        String pathPrefix = getObjectPath().isEmpty() ? "" : getObjectPath() + "/";

        return new DataLakeDirectoryAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(),
            getAccountName(), getFileSystemName(),
            Utility.urlEncode(pathPrefix + Utility.urlDecode(subdirectoryName)), blockBlobAsyncClient,
            this.getSasToken());
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

        return createSubdirectoryWithResponse(subdirectoryName, null, null, null, null, requestConditions)
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
        DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient;
        try {
            dataLakeDirectoryAsyncClient = getSubdirectoryAsyncClient(subdirectoryName);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }

        return dataLakeDirectoryAsyncClient.createWithResponse(permissions, umask, headers, metadata, requestConditions)
            .map(response -> new SimpleResponse<>(response, dataLakeDirectoryAsyncClient));
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

    private Mono<FileSystemsListPathsResponse> listPathsSegment(String marker, boolean recursive,
        boolean userPrincipleNameReturned, Integer maxResults, Duration timeout) {

        return StorageImplUtils.applyOptionalTimeout(
            this.fileSystemDataLakeStorage.getFileSystems().listPathsWithResponseAsync(
                recursive, null, null, marker, getDirectoryPath(), maxResults, userPrincipleNameReturned,
                Context.NONE), timeout);
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
            .endpoint(StorageImplUtils.appendToUrlPath(blobUrl, pathName).toString());
    }
}
