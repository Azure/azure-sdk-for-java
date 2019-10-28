package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.models.PathHttpHeaders;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathInfo;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * This class provides a client that contains directory operations for Azure Storage Data Lake. Operations provided by
 * this client include creating a directory, deleting a directory, renaming a directory, setting metadata and
 * http headers, setting and retrieving access control, getting properties and creating and deleting files and
 * subdirectories.
 *
 * <p>
 * This client is instantiated through {@link PathClientBuilder} or retrieved via
 * {@link FileSystemClient#getDirectoryClient(String) getDirectoryClient}.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction?toc=%2fazure%2fstorage%2fblobs%2ftoc.json>Azure
 * Docs</a> for more information.
 */
public class DataLakeDirectoryClient extends PathClient {
    private final ClientLogger logger = new ClientLogger(DataLakeDirectoryClient.class);

    private DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient;

    DataLakeDirectoryClient(DataLakeDirectoryAsyncClient pathAsyncClient, BlockBlobClient blockBlobClient) {
        super(pathAsyncClient, blockBlobClient);
        this.dataLakeDirectoryAsyncClient = pathAsyncClient;
    }

    private DataLakeDirectoryClient(PathClient pathClient) {
        super(pathClient.pathAsyncClient, pathClient.blockBlobClient);
        this.dataLakeDirectoryAsyncClient = new DataLakeDirectoryAsyncClient(pathClient.pathAsyncClient);
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
     * Creates a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.create}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return Information about the created directory.
     */
    public PathInfo create() {
        return createWithResponse(null, null, null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.createWithResponse#PathHttpHeaders-Map-DataLakeRequestConditions-String-String-Duration-Context}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource.
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @return A response containing information about the created directory.
     */
    public Response<PathInfo> createWithResponse(PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions accessConditions, String permissions, String umask, Duration timeout, Context context) {
        Mono<Response<PathInfo>> response = pathAsyncClient.createWithResponse(PathResourceType.DIRECTORY, headers,
            metadata, accessConditions, permissions, umask, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteWithResponse#boolean-DataLakeRequestConditions-Duration-Context}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param recursive Whether or not to delete all paths beneath the directory.
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A reactive response signalling completion.
     */
    public Response<Void> deleteWithResponse(boolean recursive, DataLakeRequestConditions accessConditions, Duration timeout,
        Context context) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        Mono<Response<Void>> response = pathAsyncClient.deleteWithResponse(recursive, accessConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Initializes a new DataLakeFileClient object by concatenating fileName to the end of DataLakeDirectoryClient's URL. The new
     * DataLakeFileClient uses the same request policy pipeline as the DataLakeDirectoryClient.
     *
     * @param fileName A {@code String} representing the name of the file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.getFileClient#String}
     *
     * @return A new {@link DataLakeFileClient} object which references the file with the specified name in this directory.
     */
    public DataLakeFileClient getFileClient(String fileName) {
        if (ImplUtils.isNullOrEmpty(fileName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'fileName' can not be set to null"));
        }
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
     * {@codesnippet com.azure.storage.file.datalake.FileSystemClient.createFile#String}
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String}
     *
     * @param fileName Name of the file to create.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the file.
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeFileClient} used
     * to interact with the file created.
     */
    public Response<DataLakeFileClient> createFileWithResponse(String fileName, PathHttpHeaders headers,
        Map<String, String> metadata, DataLakeRequestConditions accessConditions, String permissions, String umask,
        Duration timeout, Context context) {
        DataLakeFileClient dataLakeFileClient = getFileClient(fileName);
        Response<PathInfo> response = dataLakeFileClient.createWithResponse(headers, metadata, accessConditions, permissions,
            umask, timeout, context);
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
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> deleteFileWithResponse(String fileName, DataLakeRequestConditions accessConditions,
        Duration timeout, Context context) {
        return getFileClient(fileName).deleteWithResponse(accessConditions, timeout, context);
    }


    /**
     * Initializes a new DataLakeDirectoryClient object by concatenating directoryName to the end of DataLakeDirectoryClient's URL.
     * The new DataLakeDirectoryClient uses the same request policy pipeline as the DataLakeDirectoryClient.
     *
     * @param subDirectoryName A {@code String} representing the name of the sub-directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.getDirectoryClient#String}
     *
     * @return A new {@link DataLakeDirectoryClient} object which references the sub-directory with the specified name in this
     * directory
     */
    public DataLakeDirectoryClient getSubDirectoryClient(String subDirectoryName) {
        if (ImplUtils.isNullOrEmpty(subDirectoryName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'subDirectoryName' can not be set to null"));
        }
        return new DataLakeDirectoryClient(dataLakeDirectoryAsyncClient.getSubDirectoryAsyncClient(subDirectoryName),
            dataLakeDirectoryAsyncClient.prepareBuilderAppendPath(subDirectoryName).buildBlockBlobClient());
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubDirectory#String}
     *
     * @param subDirectoryName Name of the sub-directory to create.
     * @return A {@link DataLakeDirectoryClient} used to interact with the sub-directory created.
     */
    public DataLakeDirectoryClient createSubDirectory(String subDirectoryName) {
        return createSubDirectoryWithResponse(subDirectoryName, null, null, null, null, null, null, null).getValue();
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.Directory.createSubDirectoryWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String}
     *
     * @param subDirectoryName Name of the sub-directory to create.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the sub-directory.
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param permissions POSIX access permissions for the sub-directory owner, the sub-directory owning group, and
     * others.
     * @param umask Restricts permissions of the sub-directory to be created.
     * @return A {@link Response} whose {@link Response#getValue() value} contains a {@link DataLakeDirectoryClient} used to
     * interact with the sub-directory created.
     */
    public Response<DataLakeDirectoryClient> createSubDirectoryWithResponse(String subDirectoryName,
        PathHttpHeaders headers, Map<String, String> metadata, DataLakeRequestConditions accessConditions,
        String permissions, String umask, Duration timeout, Context context) {
        DataLakeDirectoryClient dataLakeDirectoryClient = getSubDirectoryClient(subDirectoryName);
        Response<PathInfo> response = dataLakeDirectoryClient.createWithResponse(headers, metadata, accessConditions, permissions, umask,
            timeout, context);
        return new SimpleResponse<>(response, dataLakeDirectoryClient);
    }

    /**
     * Deletes the specified sub-directory in the directory. If the directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubDirectory#String}
     *
     * @param subDirectoryName Name of the sub-directory to delete.
     */
    public void deleteSubDirectory(String subDirectoryName) {
        deleteSubDirectoryWithResponse(subDirectoryName, false, null, null, null);
    }

    /**
     * Deletes the specified sub-directory in the directory. If the sub-directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubDirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context}
     *
     * @param subDirectoryName Name of the sub-directory to delete.
     * @param recursive Whether or not to delete all paths beneath the sub-directory.
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> deleteSubDirectoryWithResponse(String subDirectoryName, boolean recursive,
        DataLakeRequestConditions accessConditions, Duration timeout, Context context) {
        DataLakeDirectoryClient dataLakeDirectoryClient = getSubDirectoryClient(subDirectoryName);
        return dataLakeDirectoryClient.deleteWithResponse(recursive, accessConditions, timeout, context);
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
     * @param destinationPath Relative path from the file system to rename the directory to.
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
     * {@codesnippet com.azure.storage.file.datalake.DataLakeDirectoryClient.renameWithResponse#String-PathHTTPHeaders-Map-String-String-DataLakeRequestConditions-DataLakeRequestConditions}
     *
     * @param destinationPath Relative path from the file system to rename the directory to.
     * @param sourceAccessConditions {@link DataLakeRequestConditions} against the source.
     * @param destAccessConditions {@link DataLakeRequestConditions} against the destination.
     * @return A {@link Response} whose {@link Response#getValue() value} that contains a {@link DataLakeDirectoryClient} used
     * to interact with the directory created.
     */
    public Response<DataLakeDirectoryClient> renameWithResponse(String destinationPath,
        DataLakeRequestConditions sourceAccessConditions, DataLakeRequestConditions destAccessConditions, Duration timeout,
        Context context) {

        Mono<Response<PathClient>> response = renameWithResponse(destinationPath, sourceAccessConditions,
            destAccessConditions, context);

        Response<PathClient> resp = StorageImplUtils.blockWithOptionalTimeout(response, timeout);
        return new SimpleResponse<>(resp, new DataLakeDirectoryClient(resp.getValue()));
    }
}
