// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a client that contains directory operations for Azure Storage Data Lake. Operations provided by
 * this client include creating a directory, deleting a directory, renaming a directory, setting metadata and
 * http headers, setting and retrieving access control, getting properties and creating and deleting files and
 * subdirectories.
 *
 * <p>
 * This client is instantiated through {@link DataLakePathClientBuilder} or retrieved via
 * {@link DataLakeFileSystemClient#getDirectoryClient(String) getDirectoryClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction?toc=%2fazure%2fstorage%2fblobs%2ftoc.json>Azure
 * Docs</a> for more information.
 */
public class DataLakeDirectoryClient extends DataLakePathClient {
    private final ClientLogger logger = new ClientLogger(DataLakeDirectoryClient.class);

    private final DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient;

    DataLakeDirectoryClient(DataLakeDirectoryAsyncClient pathAsyncClient, BlockBlobClient blockBlobClient) {
        super(pathAsyncClient, blockBlobClient);
        this.dataLakeDirectoryAsyncClient = pathAsyncClient;
    }

    private DataLakeDirectoryClient(DataLakePathClient dataLakePathClient) {
        super(dataLakePathClient.dataLakePathAsyncClient, dataLakePathClient.blockBlobClient);
        this.dataLakeDirectoryAsyncClient = new DataLakeDirectoryAsyncClient(
            dataLakePathClient.dataLakePathAsyncClient);
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.delete}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     */
    public void delete() {
        deleteWithResponse(false, null, null, null).getValue();
    }

    /**
     * Deletes a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteWithResponse#boolean-DataLakeRequestConditions-Duration-Context}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param recursive Whether or not to delete all paths beneath the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A reactive response signalling completion.
     */
    public Response<Void> deleteWithResponse(boolean recursive, DataLakeRequestConditions requestConditions,
        Duration timeout, Context context) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        Mono<Response<Void>> response = dataLakePathAsyncClient.deleteWithResponse(recursive, requestConditions,
            context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Initializes a new DataLakeFileClient object by concatenating fileName to the end of DataLakeDirectoryClient's
     * URL. The new DataLakeFileClient uses the same request policy pipeline as the DataLakeDirectoryClient.
     *
     * @param fileName A {@code String} representing the name of the file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.getFileClient#String}
     *
     * @return A new {@link DataLakeFileClient} object which references the file with the specified name in this
     * directory.
     */
    public DataLakeFileClient getFileClient(String fileName) {
        Objects.requireNonNull(fileName, "'fileName' can not be set to null");

        return new DataLakeFileClient(dataLakeDirectoryAsyncClient.getFileAsyncClient(fileName),
            dataLakeDirectoryAsyncClient.prepareBuilderAppendPath(fileName).buildBlockBlobClient());
    }

    /**
     * Creates a new file within a directory. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.createFile#String}
     *
     * @param fileName Name of the file to create.
     * @return A {@link DataLakeFileClient} used to interact with the file created.
     */
    public DataLakeFileClient createFile(String fileName) {
        return createFileWithResponse(fileName, null, null, null, null, null, null, null).getValue();
    }

    /**
     * Creates a new file within a directory. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context}
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
        Response<PathInfo> response = dataLakeFileClient.createWithResponse(permissions, umask, headers, metadata,
            requestConditions, timeout, context);
        return new SimpleResponse<>(response, dataLakeFileClient);
    }

    /**
     * Deletes the specified file in the directory. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFile#String}
     *
     * @param fileName Name of the file to delete.
     */
    public void deleteFile(String fileName) {
        deleteFileWithResponse(fileName, null, null, null);
    }

    /**
     * Deletes the specified file in the directory. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileWithResponse#String-DataLakeRequestConditions-Duration-Context}
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
     * Initializes a new DataLakeDirectoryClient object by concatenating directoryName to the end of
     * DataLakeDirectoryClient's URL. The new DataLakeDirectoryClient uses the same request policy pipeline as the
     * DataLakeDirectoryClient.
     *
     * @param subdirectoryName A {@code String} representing the name of the sub-directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.getSubdirectoryClient#String}
     *
     * @return A new {@link DataLakeDirectoryClient} object which references the sub-directory with the specified name
     * in this directory
     */
    public DataLakeDirectoryClient getSubdirectoryClient(String subdirectoryName) {
        Objects.requireNonNull(subdirectoryName, "'subdirectoryName' can not be set to null");

        return new DataLakeDirectoryClient(dataLakeDirectoryAsyncClient.getSubdirectoryAsyncClient(subdirectoryName),
            dataLakeDirectoryAsyncClient.prepareBuilderAppendPath(subdirectoryName).buildBlockBlobClient());
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectory#String}
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @return A {@link DataLakeDirectoryClient} used to interact with the sub-directory created.
     */
    public DataLakeDirectoryClient createSubdirectory(String subdirectoryName) {
        return createSubdirectoryWithResponse(subdirectoryName, null, null, null, null, null, null, null).getValue();
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context}
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @param permissions POSIX access permissions for the sub-directory owner, the sub-directory owning group, and
     * others.
     * @param umask Restricts permissions of the sub-directory to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the sub-directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains a {@link DataLakeDirectoryClient}
     * used to interact with the sub-directory created.
     */
    public Response<DataLakeDirectoryClient> createSubdirectoryWithResponse(String subdirectoryName,
        String permissions, String umask, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        DataLakeDirectoryClient dataLakeDirectoryClient = getSubdirectoryClient(subdirectoryName);
        Response<PathInfo> response = dataLakeDirectoryClient.createWithResponse(permissions, umask, headers, metadata,
            requestConditions, timeout, context);
        return new SimpleResponse<>(response, dataLakeDirectoryClient);
    }

    /**
     * Deletes the specified sub-directory in the directory. If the sub-directory doesn't exist or is not empty the
     * operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectory#String}
     *
     * @param subdirectoryName Name of the sub-directory to delete.
     */
    public void deleteSubdirectory(String subdirectoryName) {
        deleteSubdirectoryWithResponse(subdirectoryName, false, null, null, null);
    }

    /**
     * Deletes the specified sub-directory in the directory. If the sub-directory doesn't exist or is not empty the
     * operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context}
     *
     * @param subdirectoryName Name of the sub-directory to delete.
     * @param recursive Whether or not to delete all paths beneath the sub-directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> deleteSubdirectoryWithResponse(String subdirectoryName, boolean recursive,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        DataLakeDirectoryClient dataLakeDirectoryClient = getSubdirectoryClient(subdirectoryName);
        return dataLakeDirectoryClient.deleteWithResponse(recursive, requestConditions, timeout, context);
    }

    /**
     * Moves the directory to another location within the file system.
     * For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.rename#String}
     *
     * @param destinationPath Relative path from the file system to rename the directory to, excludes the file system
     * name. For example if you want to move a directory with fileSystem = "myfilesystem", path = "mydir/mysubdir" to
     * another path in myfilesystem (ex: newdir) then set the destinationPath = "newdir"
     * @return A {@link DataLakeDirectoryClient} used to interact with the new directory created.
     */
    public DataLakeDirectoryClient rename(String destinationPath) {
        return renameWithResponse(destinationPath, null, null, null, null).getValue();
    }

    /**
     * Moves the directory to another location within the file system.
     * For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.renameWithResponse#String-DataLakeRequestConditions-DataLakeRequestConditions-Duration-Context}
     *
     * @param destinationPath Relative path from the file system to rename the directory to, excludes the file system
     * name. For example if you want to move a directory with fileSystem = "myfilesystem", path = "mydir/mysubdir" to
     * another path in myfilesystem (ex: newdir) then set the destinationPath = "newdir"
     * @param sourceRequestConditions {@link DataLakeRequestConditions} against the source.
     * @param destinationRequestConditions {@link DataLakeRequestConditions} against the destination.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} that contains a
     * {@link DataLakeDirectoryClient} used to interact with the directory created.
     */
    public Response<DataLakeDirectoryClient> renameWithResponse(String destinationPath,
        DataLakeRequestConditions sourceRequestConditions, DataLakeRequestConditions destinationRequestConditions,
        Duration timeout, Context context) {

        Mono<Response<DataLakePathClient>> response = renameWithResponse(destinationPath, sourceRequestConditions,
            destinationRequestConditions, context);

        Response<DataLakePathClient> resp = StorageImplUtils.blockWithOptionalTimeout(response, timeout);
        return new SimpleResponse<>(resp, new DataLakeDirectoryClient(resp.getValue()));
    }
}
