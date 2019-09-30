// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;
import com.azure.storage.file.datalake.models.FileSystemAccessConditions;
import com.azure.storage.file.datalake.models.FileSystemInfo;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.azure.core.implementation.util.FluxUtil.withContext;

public class FileSystemAsyncClient {

    private final ClientLogger logger = new ClientLogger(FileSystemAsyncClient.class);

    private final AzureBlobStorageImpl blobImpl;
    private final DataLakeStorageClientImpl dataLakeImpl;
    private final String fileSystemName;

    public FileSystemAsyncClient(AzureBlobStorageImpl blobImpl, DataLakeStorageClientImpl dataLakeImpl,
        String fileSystemName) {
        this.blobImpl = blobImpl;
        this.dataLakeImpl = dataLakeImpl;
        this.fileSystemName = fileSystemName;
    }

    public Mono<FileSystemInfo> create() {
        return null;
    }

    public Mono<Response<FileSystemInfo>> createWithResponse(Map<String, String> metadata) {
        return withContext(context ->  createWithResponse(metadata, context));
    }

    Mono<Response<FileSystemInfo>> createWithResponse(Map<String, String> metadata, Context context) {
        return null;
    }

    public Mono<Void> delete() {
        return null;
    }

    public Mono<Response<Void>> deleteWithResponse(PathAccessConditions pathAccessConditions) {
        return withContext(context ->  deleteWithResponse(pathAccessConditions, context));
    }

    Mono<Response<Void>> deleteWithResponse(PathAccessConditions pathAccessConditions, Context context) {
        return null;
    }

    public BlobContainerAsyncClient getBlobContainerAsyncClient() {
        return null;
    }

    public DirectoryAsyncClient getDirectoryAsyncClient() {
        return null;
    }

    public FileAsyncClient getFileAsyncClient() {
        return null;
    }

    // TODO (gapra) : GetPaths

    public Mono<FileSystemProperties> getProperties() {
        return null;
    }

    public Mono<Response<FileSystemProperties>> getPropertiesWithResponse() {
        return withContext(this::getPropertiesWithResponse);
    }

    Mono<Response<FileSystemProperties>> getPropertiesWithResponse(Context context) {
        return null;
    }

    public Mono<Void> setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata, null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        FileSystemAccessConditions accessConditions) {
        return withContext(context -> setMetadataWithResponse(metadata, accessConditions, context));
    }

    Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        FileSystemAccessConditions accessConditions, Context context) {
        return null;
    }
}
