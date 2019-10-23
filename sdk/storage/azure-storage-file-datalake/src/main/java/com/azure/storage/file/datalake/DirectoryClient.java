package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import com.azure.storage.file.datalake.models.PathItem;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

public class DirectoryClient extends PathClient {
    private final ClientLogger logger = new ClientLogger(DirectoryClient.class);

    private DirectoryAsyncClient directoryAsyncClient;

    DirectoryClient(DirectoryAsyncClient pathAsyncClient, BlockBlobClient blockBlobClient) {
        super(pathAsyncClient, blockBlobClient);
        this.directoryAsyncClient = pathAsyncClient;
    }

    DirectoryClient(PathClient pathClient) {
        super(pathClient.pathAsyncClient, pathClient.blockBlobClient);
        this.directoryAsyncClient = (DirectoryAsyncClient) pathClient.pathAsyncClient;
    }

    /**
     * Creates a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryClient.create}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return Information about the created directory.
     */
    public PathItem create() {
        return createWithResponse(null, null, null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryClient.createWithResponse#PathHttpHeaders-Map-PathAccessConditions-String-String-Duration-Context}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource.
     * @param accessConditions {@link PathAccessConditions}
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @return A response containing information about the created directory.
     */
    public Response<PathItem> createWithResponse(PathHttpHeaders headers, Map<String, String> metadata,
        String permissions, String umask, PathAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<PathItem>> response = pathAsyncClient.createWithResponse(PathResourceType.DIRECTORY, headers,
            metadata, accessConditions, permissions, umask, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Deletes a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryClient.delete}
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
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.deleteWithResponse#boolean-PathAccessConditions-Duration-Context}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param recursive Whether or not to delete all paths beneath the directory.
     * @param accessConditions {@link PathAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A reactive response signalling completion.
     */
    public Response<Void> deleteWithResponse(boolean recursive, PathAccessConditions accessConditions, Duration timeout,
        Context context) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        Mono<Response<Void>> response = pathAsyncClient.deleteWithResponse(recursive, accessConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Initializes a new FileClient object by concatenating fileName to the end of DirectoryClient's URL. The new
     * FileClient uses the same request policy pipeline as the DirectoryClient.
     *
     * @param fileName A {@code String} representing the name of the file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryClient.getFileClient#String}
     *
     * @return A new {@link FileClient} object which references the file with the specified name in this directory.
     */
    public FileClient getFileClient(String fileName) {
        if (ImplUtils.isNullOrEmpty(fileName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'fileName' can not be set to null"));
        }
        return new FileClient(directoryAsyncClient.getFileAsyncClient(fileName),
            directoryAsyncClient.prepareBuilderAppendPath(fileName).buildBlockBlobClient());
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
     * @return A {@link FileClient} used to interact with the file created.
     */
    public FileClient createFile(String fileName) {
        return createFileWithResponse(fileName, null, null, null, null, null, null, null).getValue();
    }

    /**
     * Creates a new file within a directory. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryClient.createFileWithResponse#String-PathHttpHeaders-Map-PathAccessConditions-String-String}
     *
     * @param fileName Name of the file to create.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the file.
     * @param accessConditions {@link PathAccessConditions}
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link FileClient} used
     * to interact with the file created.
     */
    public Response<FileClient> createFileWithResponse(String fileName, PathHttpHeaders headers,
        Map<String, String> metadata, PathAccessConditions accessConditions, String permissions, String umask,
        Duration timeout, Context context) {
        FileClient fileClient = getFileClient(fileName);
        Response<PathItem> response = fileClient.createWithResponse(headers, metadata, accessConditions, permissions,
            umask, timeout, context);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            fileClient);
    }

    /**
     * Deletes the specified file in the directory. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryClient.deleteFile#String}
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
     * {@codesnippet com.azure.storage.file.datalake.DirectoryClient.deleteFileWithResponse#String-PathAccessConditions-Duration-Context}
     *
     * @param fileName Name of the file to delete.
     * @param accessConditions {@link PathAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> deleteFileWithResponse(String fileName, PathAccessConditions accessConditions,
        Duration timeout, Context context) {
        return getFileClient(fileName).deleteWithResponse(accessConditions, timeout, context);
    }


    /**
     * Initializes a new DirectoryClient object by concatenating directoryName to the end of DirectoryClient's URL.
     * The new DirectoryClient uses the same request policy pipeline as the DirectoryClient.
     *
     * @param directoryName A {@code String} representing the name of the sub-directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryClient.getDirectoryClient#String}
     *
     * @return A new {@link DirectoryClient} object which references the sub-directory with the specified name in this
     * directory
     */
    public DirectoryClient getSubDirectoryClient(String directoryName) {
        if (ImplUtils.isNullOrEmpty(directoryName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'directoryName' can not be set to null"));
        }
        return new DirectoryClient(directoryAsyncClient.getSubDirectoryAsyncClient(directoryName),
            directoryAsyncClient.prepareBuilderAppendPath(directoryName).buildBlockBlobClient());
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryClient.createSubDirectory#String}
     *
     * @param directoryName Name of the sub-directory to create.
     * @return A {@link DirectoryClient} used to interact with the sub-directory created.
     */
    public DirectoryClient createSubDirectory(String directoryName) {
        return createSubDirectoryWithResponse(directoryName, null, null, null, null, null, null, null).getValue();
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.Directory.createSubDirectoryWithResponse#String-PathHttpHeaders-Map-PathAccessConditions-String-String}
     *
     * @param directoryName Name of the sub-directory to create.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the sub-directory.
     * @param accessConditions {@link PathAccessConditions}
     * @param permissions POSIX access permissions for the sub-directory owner, the sub-directory owning group, and
     * others.
     * @param umask Restricts permissions of the sub-directory to be created.
     * @return A {@link Response} whose {@link Response#getValue() value} contains a {@link DirectoryClient} used to
     * interact with the sub-directory created.
     */
    public Response<DirectoryClient> createSubDirectoryWithResponse(String directoryName,
        PathHttpHeaders headers, Map<String, String> metadata, PathAccessConditions accessConditions,
        String permissions, String umask, Duration timeout, Context context) {
        DirectoryClient directoryClient = getSubDirectoryClient(directoryName);
        Response<PathItem> response = directoryClient.createWithResponse(headers, metadata, permissions, umask,
            accessConditions, timeout, context);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            directoryClient);
    }

    /**
     * Deletes the specified sub-directory in the directory. If the directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryClient.deleteSubDirectory#String}
     *
     * @param directoryName Name of the sub-directory to delete.
     */
    public void deleteSubDirectory(String directoryName) {
        deleteSubDirectoryWithResponse(directoryName, false, null, null, null);
    }

    /**
     * Deletes the specified sub-directory in the directory. If the sub-directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryClient.deleteSubDirectoryWithResponse#String-boolean-PathAccessConditions-Duration-Context}
     *
     * @param directoryName Name of the sub-directory to delete.
     * @param recursive Whether or not to delete all paths beneath the sub-directory.
     * @param accessConditions {@link PathAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    public Response<Void> deleteSubDirectoryWithResponse(String directoryName, boolean recursive,
        PathAccessConditions accessConditions, Duration timeout, Context context) {
        DirectoryClient directoryClient = getSubDirectoryClient(directoryName);
        return directoryClient.deleteWithResponse(recursive, accessConditions, timeout, context);
    }

    /**
     * Moves the directory to another location within the file system.
     * For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.move#String}
     *
     * @param destinationPath Relative path from the file system to move the directory to.
     * @return A {@link DirectoryClient} used to interact with the new directory created.
     */
    public DirectoryClient move(String destinationPath) {
        return moveWithResponse(destinationPath, null, null, null, null, null, null, null, null).getValue();
    }

    /**
     * Moves the directory to another location within the file system.
     * For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryClient.moveWithResponse#String-PathHttpHeaders-Map-String-String-PathAccessConditions-PathAccessConditions}
     *
     * @param destinationPath Relative path from the file system to move the directory to.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the directory.
     * @param permissions POSIX access permissions for the directory owner, the directory owning group, and others.
     * @param umask Restricts permissions of the sdirectory to be created.
     * @param sourceAccessConditions {@link PathAccessConditions} against the source.
     * @param destAccessConditions {@link PathAccessConditions} against the destination.
     * @return A {@link Response} whose {@link Response#getValue() value} that contains a {@link DirectoryClient} used
     * to interact with the directory created.
     */
    public Response<DirectoryClient> moveWithResponse(String destinationPath, PathHttpHeaders headers,
        Map<String, String> metadata, String permissions, String umask, PathAccessConditions sourceAccessConditions,
        PathAccessConditions destAccessConditions, Duration timeout, Context context) {

        Mono<Response<PathClient>> response = moveWithResponse(destinationPath, headers,
            metadata, permissions, umask, sourceAccessConditions,
            destAccessConditions, context);

        Response<PathClient> resp = StorageImplUtils.blockWithOptionalTimeout(response, timeout);
        return new SimpleResponse<>(resp, new DirectoryClient(resp.getValue()));
    }
}
