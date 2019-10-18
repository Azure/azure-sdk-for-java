package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientBuilder;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;
import com.azure.storage.file.datalake.implementation.models.PathHTTPHeaders;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import com.azure.storage.file.datalake.models.PathItem;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.azure.core.implementation.util.FluxUtil.withContext;

public final class DirectoryAsyncClient extends PathAsyncClient {
    private final ClientLogger logger = new ClientLogger(DirectoryAsyncClient.class);
// TODO : Add comments

    /**
     * Package-private constructor for use by {@link PathClientBuilder}.
     *
     * @param dataLakeStorage The API client for data lake storage.
     * @param accountName The account name for storage account.
     * @param blockBlobAsyncClient
     */
    protected DirectoryAsyncClient(DataLakeStorageClientImpl dataLakeStorage, String accountName, String fileSystemName,
        String pathName, BlockBlobAsyncClient blockBlobAsyncClient) {
        super(dataLakeStorage, accountName, fileSystemName, pathName, blockBlobAsyncClient);
    }

    private DirectoryAsyncClient(PathAsyncClient pathAsyncClient) {
        super(pathAsyncClient.dataLakeStorage, pathAsyncClient.accountName, pathAsyncClient.fileSystemName,
            pathAsyncClient.pathName, pathAsyncClient.blockBlobAsyncClient);
    }


    /**
     * Creates the resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathAsyncClient.create}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return A reactive response containing the information of the created resource.
     */
    public Mono<PathItem> create() {
        return createWithResponse(null, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<PathItem>> createWithResponse(PathHTTPHeaders httpHeaders, Map<String, String> metadata,
        String permissions, String umask, PathAccessConditions accessConditions) {
        return withContext(context -> createWithResponse(PathResourceType.DIRECTORY, httpHeaders, metadata,
            permissions, umask, accessConditions, context));
    }

    public Mono<Void> delete() {
        return this.deleteWithResponse(false, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * TODO (rickle-msft): code snippet
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param recursive If "true", all paths beneath the directory will be deleted. If "false" and the directory is
     * non-empty, an error occurs.
     * @param accessConditions {@link PathAccessConditions}
     *
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> deleteWithResponse(boolean recursive, PathAccessConditions accessConditions) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        return withContext(context -> deleteWithResponse(recursive, accessConditions, context));
    }

    // Create File
    public FileAsyncClient getFile(String fileName) {
        if (ImplUtils.isNullOrEmpty(fileName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'fileName' can not be set to null"));
        }
        return new FileAsyncClient(new DataLakeStorageClientBuilder()
            .url(Utility.appendToURLPath(getDataLakeUrl(), fileName).toString())
            .pipeline(dataLakeStorage.getHttpPipeline())
            .build(), accountName, fileSystemName, pathName + "/"+ fileName,
            prepareBuilderAppendPath(fileName).buildBlockBlobAsyncClient());
    }

    public Mono<FileAsyncClient> createFile(String fileName) {
        return createFileWithResponse(fileName, null, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<FileAsyncClient>> createFileWithResponse(String fileName, PathHTTPHeaders httpHeaders,
        Map<String, String> metadata, String permissions, String umask, PathAccessConditions accessConditions) {
        FileAsyncClient fileAsyncClient = getFile(fileName);
        return fileAsyncClient.createWithResponse(httpHeaders, metadata, permissions, umask, accessConditions)
            .map(response -> new SimpleResponse<>(response, fileAsyncClient));
    }

    // Delete File
    public Mono<Void> deleteFile(String fileName) {
        return deleteFileWithResponse(fileName, null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<Void>> deleteFileWithResponse(String fileName, PathAccessConditions accessConditions) {
        FileAsyncClient fileAsyncClient = getFile(fileName);
        return fileAsyncClient.deleteWithResponse(accessConditions);
    }

    // Create SubDirectory
    public DirectoryAsyncClient getSubDirectory(String directoryName) {
        if (ImplUtils.isNullOrEmpty(directoryName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'directoryName' can not be set to null"));
        }
        return new DirectoryAsyncClient(new DataLakeStorageClientBuilder()
            .url(Utility.appendToURLPath(getDataLakeUrl(), directoryName).toString())
            .pipeline(dataLakeStorage.getHttpPipeline())
            .build(), accountName, fileSystemName, pathName + "/"+ directoryName,
            prepareBuilderAppendPath(directoryName).buildBlockBlobAsyncClient());
    }

    public Mono<DirectoryAsyncClient> createSubDirectory(String directoryName) {
        return createSubDirectoryWithResponse(directoryName, null, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<DirectoryAsyncClient>> createSubDirectoryWithResponse(String directoryName,
        PathHTTPHeaders httpHeaders, Map<String, String> metadata, String permissions, String umask,
        PathAccessConditions accessConditions) {
        DirectoryAsyncClient directoryAsyncClient = getSubDirectory(directoryName);
        return directoryAsyncClient.createWithResponse(httpHeaders, metadata, permissions, umask, accessConditions)
            .map(response -> new SimpleResponse<>(response, directoryAsyncClient));
    }

    // Delete SubDirectory
    public Mono<Void> deleteSubDirectory(String directoryName) {
        return deleteSubDirectoryWithResponse(directoryName, false, null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<Void>> deleteSubDirectoryWithResponse(String directoryName, boolean recursive,
        PathAccessConditions accessConditions) {
        DirectoryAsyncClient directoryAsyncClient = getSubDirectory(directoryName);
        return directoryAsyncClient.deleteWithResponse(recursive, accessConditions);
    }

    private SpecializedBlobClientBuilder prepareBuilderAppendPath(String pathName) {
        return new SpecializedBlobClientBuilder()
            .pipeline(getHttpPipeline())
            .endpoint(Utility.appendToURLPath(getBlobUrl(), pathName).toString());
    }

    public Mono<DirectoryAsyncClient> move(String destinationPath) {
        return moveWithResponse(destinationPath, null, null, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<DirectoryAsyncClient>> moveWithResponse(String destinationPath,
        PathHTTPHeaders httpHeaders, Map<String, String> metadata, String permissions, String umask,
        PathAccessConditions sourceAccessConditions, PathAccessConditions destAccessConditions) {
        return withContext(context -> moveWithResponse(destinationPath, httpHeaders,
            metadata, permissions, umask, sourceAccessConditions, destAccessConditions, context))
            .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), new DirectoryAsyncClient(response.getValue())));
    }
}
