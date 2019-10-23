package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import com.azure.storage.file.datalake.models.PathItem;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.azure.core.implementation.util.FluxUtil.monoError;
import static com.azure.core.implementation.util.FluxUtil.withContext;

public final class DirectoryAsyncClient extends PathAsyncClient {

    private final ClientLogger logger = new ClientLogger(DirectoryAsyncClient.class);

    /**
     * Package-private constructor for use by {@link PathClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param fileSystemName The file system name.
     * @param directoryName The directory name.
     * @param blockBlobAsyncClient The underlying {@link BlobContainerAsyncClient}
     */
    DirectoryAsyncClient(HttpPipeline pipeline, String url, DataLakeServiceVersion serviceVersion,
        String accountName, String fileSystemName, String directoryName, BlockBlobAsyncClient blockBlobAsyncClient) {
        super(pipeline, url, serviceVersion, accountName, fileSystemName, directoryName, blockBlobAsyncClient);
    }

    private DirectoryAsyncClient(PathAsyncClient pathAsyncClient) {
        super(pathAsyncClient.getHttpPipeline(), pathAsyncClient.getPathUrl(), pathAsyncClient.getServiceVersion(),
            pathAsyncClient.getAccountName(), pathAsyncClient.getFileSystemName(), pathAsyncClient.getObjectPath(),
            pathAsyncClient.getBlockBlobAsyncClient());
    }

    /**
     * Creates a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.create}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return A reactive response containing information about the created directory.
     */
    public Mono<PathItem> create() {
        try {
            return createWithResponse(null, null, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.createWithResponse#PathHttpHeaders-Map-PathAccessConditions-String-String}
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
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * PathItem}.
     */
    public Mono<Response<PathItem>> createWithResponse(PathHttpHeaders headers, Map<String, String> metadata,
        PathAccessConditions accessConditions, String permissions, String umask) {
        try {
            return withContext(context -> createWithResponse(PathResourceType.DIRECTORY, headers, metadata,
                accessConditions, permissions, umask, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.delete}
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
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.deleteWithResponse#boolean-PathAccessConditions}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param recursive Whether or not to delete all paths beneath the directory.
     * @param accessConditions {@link PathAccessConditions}
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> deleteWithResponse(boolean recursive, PathAccessConditions accessConditions) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        try {
            return withContext(context -> deleteWithResponse(recursive, accessConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new FileAsyncClient object by concatenating fileName to the end of DirectoryAsyncClient's URL. The new
     * FileAsyncClient uses the same request policy pipeline as the DirectoryAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.getFileAsyncClient#String}
     *
     * @param fileName A {@code String} representing the name of the file.
     * @return A new {@link FileAsyncClient} object which references the file with the specified name in this file
     * system.
     */
    public FileAsyncClient getFileAsyncClient(String fileName) {
        if (ImplUtils.isNullOrEmpty(fileName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'fileName' can not be set to null"));
        }
        BlockBlobAsyncClient blockBlobAsyncClient = prepareBuilderAppendPath(fileName).buildBlockBlobAsyncClient();

        return new FileAsyncClient(getHttpPipeline(),
            StorageImplUtils.appendToUrlPath(getPathUrl(), fileName).toString(), getServiceVersion(),
            getAccountName(), getFileSystemName(), getObjectPath() + "/"+ fileName, blockBlobAsyncClient);
    }

    /**
     * Creates a new file within a directory. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.createFile#String}
     *
     * @param fileName Name of the file to create.
     * @return A {@link Mono} containing a {@link FileAsyncClient} used to interact with the file created.
     */
    public Mono<FileAsyncClient> createFile(String fileName) {
        try {
            return createFileWithResponse(fileName, null, null, null, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new file within a directory. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.createFileWithResponse#String-PathHttpHeaders-Map-PathAccessConditions-String-String}
     *
     * @param fileName Name of the file to create.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the file.
     * @param accessConditions {@link PathAccessConditions}
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * FileAsyncClient} used to interact with the file created.
     */
    public Mono<Response<FileAsyncClient>> createFileWithResponse(String fileName, PathHttpHeaders headers,
        Map<String, String> metadata, PathAccessConditions accessConditions, String permissions, String umask) {
        try {
            FileAsyncClient fileAsyncClient = getFileAsyncClient(fileName);

            return fileAsyncClient.createWithResponse(headers, metadata, accessConditions, permissions, umask)
                .map(response -> new SimpleResponse<>(response, fileAsyncClient));
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
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.deleteFile#String}
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
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.deleteFileWithResponse#String-PathAccessConditions}
     *
     * @param fileName Name of the file to delete.
     * @param accessConditions {@link PathAccessConditions}
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Response<Void>> deleteFileWithResponse(String fileName, PathAccessConditions accessConditions) {
        try {
            return getFileAsyncClient(fileName).deleteWithResponse(accessConditions);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new DirectoryAsyncClient object by concatenating directoryName to the end of DirectoryAsyncClient's
     * URL. The new DirectoryAsyncClient uses the same request policy pipeline as the DirectoryAsyncClient.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.getDirectoryAsyncClient#String}
     *
     * @param directoryName A {@code String} representing the name of the sub-directory.
     * @return A new {@link DirectoryAsyncClient} object which references the directory with the specified name in this
     * file system.
     */
    public DirectoryAsyncClient getSubDirectoryAsyncClient(String directoryName) {
        if (ImplUtils.isNullOrEmpty(directoryName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'directoryName' can not be set to null"));
        }
        BlockBlobAsyncClient blockBlobAsyncClient = prepareBuilderAppendPath(directoryName).buildBlockBlobAsyncClient();

        return new DirectoryAsyncClient(getHttpPipeline(),
            StorageImplUtils.appendToUrlPath(getPathUrl(), directoryName).toString(), getServiceVersion(),
            getAccountName(), getFileSystemName(), getObjectPath() + "/"+ directoryName, blockBlobAsyncClient);
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.createSubDirectory#String}
     *
     * @param directoryName Name of the sub-directory to create.
     * @return A {@link Mono} containing a {@link DirectoryAsyncClient} used to interact with the directory created.
     */
    public Mono<DirectoryAsyncClient> createSubDirectory(String directoryName) {
        try {
            return createSubDirectoryWithResponse(directoryName, null, null, null, null, null)
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
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.createSubDirectoryWithResponse#String-PathHttpHeaders-Map-PathAccessConditions-String-String}
     *
     * @param directoryName Name of the sub-directory to create.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the sub-directory.
     * @param accessConditions {@link PathAccessConditions}
     * @param permissions POSIX access permissions for the sub-directory owner, the sub-directory owning group, and
     * others.
     * @param umask Restricts permissions of the sub-directory to be created.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DirectoryAsyncClient} used to interact with the sub-directory created.
     */
    public Mono<Response<DirectoryAsyncClient>> createSubDirectoryWithResponse(String directoryName,
        PathHttpHeaders headers, Map<String, String> metadata, PathAccessConditions accessConditions,
        String permissions, String umask) {
        try {
            DirectoryAsyncClient directoryAsyncClient = getSubDirectoryAsyncClient(directoryName);

            return directoryAsyncClient.createWithResponse(headers, metadata, accessConditions, permissions, umask)
                .map(response -> new SimpleResponse<>(response, directoryAsyncClient));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified sub-directory in the directory. If the sub-directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.deleteSubDirectory#String}
     *
     * @param directoryName Name of the sub-directory to delete.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> deleteSubDirectory(String directoryName) {
        try {
            return deleteSubDirectoryWithResponse(directoryName, false, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified sub-directory in the directory. If the sub-directory doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.deleteSubDirectoryWithResponse#String-boolean-PathAccessConditions}
     *
     * @param directoryName Name of the sub-directory to delete.
     * @param recursive Whether or not to delete all paths beneath the sub-directory.
     * @param accessConditions {@link PathAccessConditions}
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Response<Void>> deleteSubDirectoryWithResponse(String directoryName, boolean recursive,
        PathAccessConditions accessConditions) {
        try {
            return getSubDirectoryAsyncClient(directoryName).deleteWithResponse(recursive, accessConditions);
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
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.move#String}
     *
     * @param destinationPath Relative path from the file system to move the directory to.
     * @return A {@link Mono} containing a {@link DirectoryAsyncClient} used to interact with the new directory created.
     */
    public Mono<DirectoryAsyncClient> move(String destinationPath) {
        try {
            return moveWithResponse(destinationPath, null, null, null, null, null, null).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.storage.file.datalake.DirectoryAsyncClient.moveWithResponse#String-PathHttpHeaders-Map-String-String-PathAccessConditions-PathAccessConditions}
     *
     * @param destinationPath Relative path from the file system to move the directory to.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the directory.
     * @param permissions POSIX access permissions for the directory owner, the directory owning group, and others.
     * @param umask Restricts permissions of the sdirectory to be created.
     * @param sourceAccessConditions {@link PathAccessConditions} against the source.
     * @param destAccessConditions {@link PathAccessConditions} against the destination.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DirectoryAsyncClient} used to interact with the directory created.
     */
    public Mono<Response<DirectoryAsyncClient>> moveWithResponse(String destinationPath,
        PathHttpHeaders headers, Map<String, String> metadata, String permissions, String umask,
        PathAccessConditions sourceAccessConditions, PathAccessConditions destAccessConditions) {
        try {
            return withContext(context -> moveWithResponse(destinationPath, headers,
                metadata, permissions, umask, sourceAccessConditions, destAccessConditions, context))
                .map(response -> new SimpleResponse<>(response,
                    new DirectoryAsyncClient(response.getValue())));
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
        String blobUrl = Transforms.endpointToDesiredEndpoint(getPathUrl(), "blob", "dfs");

        return new SpecializedBlobClientBuilder()
            .pipeline(getHttpPipeline())
            .endpoint(StorageImplUtils.appendToUrlPath(blobUrl, pathName).toString());
    }
}
