package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathProperties;
import reactor.core.publisher.Mono;

import static com.azure.core.implementation.util.FluxUtil.withContext;

public class DirectoryAsyncClient {

    public DirectoryAsyncClient() {
    }

    public Mono<PathInfo> create() {
        return null;
    }

    public Mono<Response<PathInfo>> createWithResponse(PathProperties pathProperties, String permissions, String uMask) {
        return withContext(context -> createWithResponse(pathProperties, permissions, uMask));
    }

    Mono<Response<PathInfo>> createWithResponse(PathProperties pathProperties, String permissions, String uMask,
        Context context) {
        return null;
    }

}
