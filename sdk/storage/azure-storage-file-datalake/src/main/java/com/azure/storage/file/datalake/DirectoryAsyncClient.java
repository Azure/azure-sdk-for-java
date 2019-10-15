package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.storage.file.datalake.implementation.models.PathHTTPHeaders;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import com.azure.storage.file.datalake.models.PathItem;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.azure.core.implementation.util.FluxUtil.withContext;

public class DirectoryAsyncClient extends PathAsyncClient {

    @Override
    public Mono<Response<PathItem>> createWithResponse(PathHTTPHeaders httpHeaders, Map<String, String> metadata,
        String permissions, String umask, PathAccessConditions accessConditions) {
        return withContext(context -> createWithResponse(PathResourceType.DIRECTORY, httpHeaders, metadata,
            permissions, umask, accessConditions, context));
    }

    @Override
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
        return withContext(context -> deleteWithResponse(recursive, null, accessConditions, context));
    }
}
