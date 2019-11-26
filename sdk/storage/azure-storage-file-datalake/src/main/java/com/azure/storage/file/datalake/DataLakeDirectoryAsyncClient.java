// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
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
 * Please refer to the <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction?toc=%2fazure%2fstorage%2fblobs%2ftoc.json>Azure
 * Docs</a> for more information.
 */
public final class DataLakeDirectoryAsyncClient extends DataLakePathAsyncClient {

    private final ClientLogger logger = new ClientLogger(DataLakeDirectoryAsyncClient.class);

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
        String accountName, String fileSystemName, String directoryName, BlockBlobAsyncClient blockBlobAsyncClient) {
        super(pipeline, url, serviceVersion, accountName, fileSystemName, directoryName, PathResourceType.DIRECTORY,
            blockBlobAsyncClient);
    }

    DataLakeDirectoryAsyncClient(DataLakePathAsyncClient dataLakePathAsyncClient) {
        super(dataLakePathAsyncClient.getHttpPipeline(), dataLakePathAsyncClient.getPathUrl(),
            dataLakePathAsyncClient.getServiceVersion(), dataLakePathAsyncClient.getAccountName(),
            dataLakePathAsyncClient.getFileSystemName(), dataLakePathAsyncClient.getObjectPath(),
            PathResourceType.DIRECTORY, dataLakePathAsyncClient.getBlockBlobAsyncClient());
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.delete}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Void> delete() {
        try {
            return deleteWithResponse(false, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteWithResponse#boolean-DataLakeRequestConditions}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param recursive Whether or not to delete all paths beneath the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> deleteWithResponse(boolean recursive, DataLakeRequestConditions requestConditions) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        try {
            return withContext(context -> deleteWithResponse(recursive, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new DataLakeFileAsyncClient object by concatenating fileName to the end of
     * DataLakeDirectoryAsyncClient's URL. The new DataLakeFileAsyncClient uses the same request policy pipeline as the
     * DataLakeDirectoryAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.getFileAsyncClient#String}
     *
     * @param fileName A {@code String} representing the name of the file.
     * @return A new {@link DataLakeFileAsyncClient} object which references the file with the specified name in this
     * file system.
     */
    public DataLakeFileAsyncClient getFileAsyncClient(String fileName) {
        Objects.requireNonNull(fileName, "'fileName' can not be set to null");

        BlockBlobAsyncClient blockBlobAsyncClient = prepareBuilderAppendPath(fileName).buildBlockBlobAsyncClient();

        return new DataLakeFileAsyncClient(getHttpPipeline(),
            StorageImplUtils.appendToUrlPath(getPathUrl(), Utility.urlEncode(Utility.urlDecode(fileName))).toString(),
            getServiceVersion(), getAccountName(), getFileSystemName(), getObjectPath() + "/"
            + Utility.urlDecode(fileName), blockBlobAsyncClient);
    }

    /**
     * Creates a new file within a directory. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String}
     *
     * @param fileName Name of the file to create.
     * @return A {@link Mono} containing a {@link DataLakeFileAsyncClient} used to interact with the file created.
     */
    public Mono<DataLakeFileAsyncClient> createFile(String fileName) {
        try {
            return createFileWithResponse(fileName, null, null, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new file within a directory. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions}
     *
     * @param fileName Name of the file to create.
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the file.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeFileAsyncClient} used to interact with the file created.
     */
    public Mono<Response<DataLakeFileAsyncClient>> createFileWithResponse(String fileName, String permissions,
        String umask, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        try {
            DataLakeFileAsyncClient dataLakeFileAsyncClient = getFileAsyncClient(fileName);

            return dataLakeFileAsyncClient.createWithResponse(permissions, umask, headers, metadata, requestConditions)
                .map(response -> new SimpleResponse<>(response, dataLakeFileAsyncClient));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified file in the file system. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFile#String}
     *
     * @param fileName Name of the file to delete.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> deleteFile(String fileName) {
        try {
            return deleteFileWithResponse(fileName, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified file in the directory. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions}
     *
     * @param fileName Name of the file to delete.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Response<Void>> deleteFileWithResponse(String fileName, DataLakeRequestConditions requestConditions) {
        try {
            return getFileAsyncClient(fileName).deleteWithResponse(requestConditions);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new DataLakeDirectoryAsyncClient object by concatenating subdirectoryName to the end of
     * DataLakeDirectoryAsyncClient's URL. The new DataLakeDirectoryAsyncClient uses the same request policy pipeline
     * as the DataLakeDirectoryAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.getSubdirectoryAsyncClient#String}
     *
     * @param subdirectoryName A {@code String} representing the name of the sub-directory.
     * @return A new {@link DataLakeDirectoryAsyncClient} object which references the directory with the specified name
     * in this file system.
     */
    public DataLakeDirectoryAsyncClient getSubdirectoryAsyncClient(String subdirectoryName) {
        Objects.requireNonNull(subdirectoryName, "'subdirectoryName' can not be set to null");

        BlockBlobAsyncClient blockBlobAsyncClient = prepareBuilderAppendPath(subdirectoryName)
            .buildBlockBlobAsyncClient();

        return new DataLakeDirectoryAsyncClient(getHttpPipeline(),
            StorageImplUtils.appendToUrlPath(getPathUrl(), Utility.urlEncode(Utility.urlDecode(subdirectoryName)))
                .toString(), getServiceVersion(), getAccountName(), getFileSystemName(), getObjectPath() + "/"
            + Utility.urlDecode(subdirectoryName), blockBlobAsyncClient);
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectory#String}
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @return A {@link Mono} containing a {@link DataLakeDirectoryAsyncClient} used to interact with the directory
     * created.
     */
    public Mono<DataLakeDirectoryAsyncClient> createSubdirectory(String subdirectoryName) {
        try {
            return createSubdirectoryWithResponse(subdirectoryName, null, null, null, null, null)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions}
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @param permissions POSIX access permissions for the sub-directory owner, the sub-directory owning group, and
     * others.
     * @param umask Restricts permissions of the sub-directory to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the sub-directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeDirectoryAsyncClient} used to interact with the sub-directory created.
     */
    public Mono<Response<DataLakeDirectoryAsyncClient>> createSubdirectoryWithResponse(String subdirectoryName,
        String permissions, String umask, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        try {
            DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient = getSubdirectoryAsyncClient(subdirectoryName);

            return dataLakeDirectoryAsyncClient.createWithResponse(permissions, umask, headers, metadata,
                requestConditions).map(response -> new SimpleResponse<>(response, dataLakeDirectoryAsyncClient));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified sub-directory in the directory. If the sub-directory doesn't exist or is not empty the
     * operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectory#String}
     *
     * @param subdirectoryName Name of the sub-directory to delete.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> deleteSubdirectory(String subdirectoryName) {
        try {
            return deleteSubdirectoryWithResponse(subdirectoryName, false, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified sub-directory in the directory. If the sub-directory doesn't exist or is not empty the
     * operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryWithResponse#String-boolean-DataLakeRequestConditions}
     *
     * @param directoryName Name of the sub-directory to delete.
     * @param recursive Whether or not to delete all paths beneath the sub-directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Response<Void>> deleteSubdirectoryWithResponse(String directoryName, boolean recursive,
        DataLakeRequestConditions requestConditions) {
        try {
            return getSubdirectoryAsyncClient(directoryName).deleteWithResponse(recursive, requestConditions);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Moves the directory to another location within the file system.
     * For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.rename#String}
     *
     * @param destinationPath Relative path from the file system to rename the directory to, excludes the file system
     * name. For example if you want to move a directory with fileSystem = "myfilesystem", path = "mydir/mysubdir" to
     * another path in myfilesystem (ex: newdir) then set the destinationPath = "newdir"
     * @return A {@link Mono} containing a {@link DataLakeDirectoryAsyncClient} used to interact with the new directory
     * created.
     */
    public Mono<DataLakeDirectoryAsyncClient> rename(String destinationPath) {
        try {
            return renameWithResponse(destinationPath, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Moves the directory to another location within the file system.
     * For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.renameWithResponse#String-DataLakeRequestConditions-DataLakeRequestConditions}
     *
     * @param destinationPath Relative path from the file system to rename the directory to, excludes the file system
     * name. For example if you want to move a directory with fileSystem = "myfilesystem", path = "mydir/mysubdir" to
     * another path in myfilesystem (ex: newdir) then set the destinationPath = "newdir"
     * @param sourceRequestConditions {@link DataLakeRequestConditions} against the source.
     * @param destinationRequestConditions {@link DataLakeRequestConditions} against the destination.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakeDirectoryAsyncClient} used to interact with the directory created.
     */
    public Mono<Response<DataLakeDirectoryAsyncClient>> renameWithResponse(String destinationPath,
        DataLakeRequestConditions sourceRequestConditions, DataLakeRequestConditions destinationRequestConditions) {
        try {
            return withContext(context -> renameWithResponse(destinationPath, sourceRequestConditions,
                destinationRequestConditions, context)).map(response -> new SimpleResponse<>(response,
                    new DataLakeDirectoryAsyncClient(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
            .endpoint(StorageImplUtils.appendToUrlPath(blobUrl, pathName).toString());
    }
}
