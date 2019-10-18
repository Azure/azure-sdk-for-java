package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.models.PathHTTPHeaders;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import com.azure.storage.file.datalake.models.PathItem;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

public class DirectoryClient extends PathClient {
    private final ClientLogger logger = new ClientLogger(DirectoryClient.class);

    private final DirectoryAsyncClient directoryAsyncClient;

    protected DirectoryClient(DirectoryAsyncClient pathAsyncClient, BlockBlobClient blockBlobClient) {
        super(pathAsyncClient, blockBlobClient);
        this.directoryAsyncClient = pathAsyncClient;
    }

    /**
     * Creates the resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.create}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return Information about the created resource.
     */
    public PathItem create() {
        return createWithResponse(null, null, null, null, null, null, Context.NONE).getValue();
    }

    public Response<PathItem> createWithResponse(PathHTTPHeaders httpHeaders, Map<String, String> metadata,
        String permissions, String umask, PathAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<PathItem>> response = client.createWithResponse(PathResourceType.DIRECTORY, httpHeaders, metadata,
            permissions, umask, accessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    public void delete() {
        deleteWithResponse(false, null, null, null).getValue();
    }

    public Response<Void> deleteWithResponse(boolean recursive, PathAccessConditions accessConditions, Duration timeout,
        Context context) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        Mono<Response<Void>> response = client.deleteWithResponse(recursive, accessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    // Create File
    public FileClient getFile(String fileName) {
        if (ImplUtils.isNullOrEmpty(fileName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'fileName' can not be set to null"));
        }
        return new FileClient(directoryAsyncClient.getFile(fileName), prepareBuilder(fileName).buildBlockBlobClient());
    }

    public String getDataLakeUrl() {
        return client.getDataLakeUrl();
    }

    public FileClient createFile(String fileName) {
        return createFileWithResponse(fileName, null, null, null, null, null, null, null).getValue();
    }

    public Response<FileClient> createFileWithResponse(String fileName, PathHTTPHeaders httpHeaders,
        Map<String, String> metadata, String permissions, String umask, PathAccessConditions accessConditions,
        Duration timeout, Context context) {
        FileClient fileClient = getFile(fileName);
        Response<PathItem> response = fileClient.createWithResponse(httpHeaders, metadata, permissions, umask,
            accessConditions, timeout, context);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            fileClient);
    }

    // Delete File
    public void deleteFile(String fileName) {
        deleteFileWithResponse(fileName, null, null, null);
    }

    public Response<Void> deleteFileWithResponse(String fileName, PathAccessConditions accessConditions,
        Duration timeout, Context context) {
        FileClient fileClient = getFile(fileName);
        return fileClient.deleteWithResponse(accessConditions, timeout, context);
    }

    // Create Directory
    public DirectoryClient getSubDirectory(String directoryName) {
        if (ImplUtils.isNullOrEmpty(directoryName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'directoryName' can not be set to null"));
        }
        return new DirectoryClient(directoryAsyncClient.getSubDirectory(directoryName),
            prepareBuilder(directoryName).buildBlockBlobClient());
    }

    public DirectoryClient createSubDirectory(String directoryName) {
        return createSubDirectoryWithResponse(directoryName, null, null, null, null, null, null, null).getValue();
    }

    public Response<DirectoryClient> createSubDirectoryWithResponse(String directoryName,
        PathHTTPHeaders httpHeaders, Map<String, String> metadata, String permissions, String umask,
        PathAccessConditions accessConditions, Duration timeout, Context context) {
        DirectoryClient directoryClient = getSubDirectory(directoryName);
        Response<PathItem> response = directoryClient.createWithResponse(httpHeaders, metadata, permissions, umask,
            accessConditions, timeout, context);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            directoryClient);
    }

    // Delete SubDirectory
    public void deleteSubDirectory(String directoryName) {
        deleteSubDirectoryWithResponse(directoryName, false, null, null, null);
    }

    public Response<Void> deleteSubDirectoryWithResponse(String directoryName, boolean recursive,
        PathAccessConditions accessConditions, Duration timeout, Context context) {
        DirectoryClient directoryClient = getSubDirectory(directoryName);
        return directoryClient.deleteWithResponse(recursive, accessConditions, timeout, context);
    }

    private SpecializedBlobClientBuilder prepareBuilder(String pathName) {
        return new SpecializedBlobClientBuilder()
            .pipeline(client.getHttpPipeline())
            .endpoint(Utility.appendToURLPath(client.getBlobUrl(), pathName).toString());
    }

//    public DirectoryClient move(String destinationPath) {
//        return moveWithResponse(destinationPath, null, null, null, null, null, null, null, null).getValue();
//    }
//
//    public Response<DirectoryClient> moveWithResponse(String destinationPath,
//        PathHTTPHeaders httpHeaders, Map<String, String> metadata, String permissions, String umask,
//        ModifiedAccessConditions sourceModifiedAccessConditions,
//        PathAccessConditions destAccessConditions, Duration timeout, Context context) {
//
//        Mono<Response<PathAsyncClient>> response = directoryAsyncClient.moveWithResponse(PathResourceType.DIRECTORY,
//            destinationPath, httpHeaders, metadata, permissions, umask, sourceModifiedAccessConditions,
//            destAccessConditions, context);
//
//        Response<PathAsyncClient> resp = Utility.blockWithOptionalTimeout(response, timeout);
//        return new SimpleResponse<>(resp.getRequest(), resp.getStatusCode(), resp.getHeaders(),
//            new DirectoryClient(resp.getValue(), new BlockBlobClient(resp.getValue().blockBlobAsyncClient)));
//
//    }
}
