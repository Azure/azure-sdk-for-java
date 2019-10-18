package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobProperties;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.implementation.models.PathHTTPHeaders;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathItem;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * This class provides a client that contains all operations that apply to any path object.
 */
public class PathClient {
    private final ClientLogger logger = new ClientLogger(PathClient.class);
    protected final PathAsyncClient client;
    private final BlockBlobClient syncClient;

    protected PathClient(PathAsyncClient pathAsyncClient, BlockBlobClient blockBlobClient) {
        this.client = pathAsyncClient;
        this.syncClient = blockBlobClient;
    }

    Response<PathItem> createWithResponse(PathResourceType resourceType, PathHTTPHeaders httpHeaders,
        Map<String, String> metadata, String permissions, String umask, PathAccessConditions accessConditions,
        Duration timeout, Context context) {
        Mono<Response<PathItem>> response = client.createWithResponse(resourceType, httpHeaders, metadata, permissions,
            umask, accessConditions, context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.setMetadata#Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource.
     * @return A response containing status code and HTTP headers.
     */
    public void setMetadata(Map<String, String> metadata) {
        setMetadataWithResponse(metadata, null, null, Context.NONE);
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.setMetadata#Map-PathAccessConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource.
     * @param accessConditions {@link PathAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> setMetadataWithResponse(Map<String, String> metadata, PathAccessConditions accessConditions,
        Duration timeout, Context context) {
        return syncClient.setMetadataWithResponse(metadata, Transforms.toBlobAccessConditions(accessConditions),
            timeout, context);
    }

    /**
     * Changes a resources's HTTP header properties. if only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.setHTTPHeaders#PathHTTPHeaders}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHTTPHeaders}
     */
    public void setHTTPHeaders(PathHTTPHeaders headers) {
        setHTTPHeadersWithResponse(headers, null, null, Context.NONE);
    }

    /**
     * Changes a resources's HTTP header properties. if only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.setHTTPHeaders.setHTTPHeadersWithResponse#PathHTTPHeaders-PathAccessConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHTTPHeaders}
     * @param accessConditions {@link PathAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> setHTTPHeadersWithResponse(PathHTTPHeaders headers, PathAccessConditions accessConditions,
        Duration timeout, Context context) {
        return syncClient.setHTTPHeadersWithResponse(Transforms.toBlobHttpHeaders(headers),
            Transforms.toBlobAccessConditions(accessConditions), timeout, context);
    }

    // Set access control

    public PathInfo setAccessControl(PathAccessControl accessControl) {
        return setAccessControlWithResponse(accessControl, null, null, null).getValue();
    }

    public Response<PathInfo> setAccessControlWithResponse(PathAccessControl accessControl,
        PathAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<PathInfo>> response = client.setAccessControlWithResponse(accessControl, accessConditions,
            context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    public PathAccessControl getAccessControl() {
        return getAccessControlWithResponse(false, null, null, null).getValue();
    }

    public Response<PathAccessControl> getAccessControlWithResponse(boolean upn,
        PathAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<PathAccessControl>> response = client.getAccessControlWithResponse(upn, accessConditions,
            context);
        return Utility.blockWithOptionalTimeout(response, timeout);
    }

    private PathClient getPathClient(String destinationPath) {
        return new PathClient(client.getPathAsyncClient(destinationPath),
            client.prepareBuilderReplacePath(destinationPath).buildBlockBlobClient());
    }

    /**
     * Returns the file's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.specialized.BlobClientBase.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-properties">Azure Docs</a></p>
     *
     * @return The file properties and metadata.
     */
    public PathProperties getProperties() {
        return getPropertiesWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the file's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.specialized.BlobClientBase.getPropertiesWithResponse#BlobAccessConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-properties">Azure Docs</a></p>
     *
     * @param accessConditions {@link PathAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The file properties and metadata.
     */
    public Response<PathProperties> getPropertiesWithResponse(PathAccessConditions accessConditions, Duration timeout,
        Context context) {
        Response<BlobProperties> response = syncClient.getPropertiesWithResponse(
            Transforms.toBlobAccessConditions(accessConditions), timeout, context);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            Transforms.toPathProperties(response.getValue()));
    }

}
