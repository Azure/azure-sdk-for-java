package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobProperties;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.models.PathRenameMode;
import com.azure.storage.file.datalake.implementation.models.SourceModifiedAccessConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathInfo;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * This class provides a client that contains all operations that apply to any path object.
 */
public class PathClient {
    private final ClientLogger logger = new ClientLogger(PathClient.class);
    protected final PathAsyncClient pathAsyncClient;
    protected final BlockBlobClient blockBlobClient;

    protected PathClient(PathAsyncClient pathAsyncClient, BlockBlobClient blockBlobClient) {
        this.pathAsyncClient = pathAsyncClient;
        this.blockBlobClient = blockBlobClient;
    }

    /**
     * Gets the URL of the object represented by this client on the Data Lake service.
     *
     * @return the URL.
     */
    protected String getPathUrl() {
        return pathAsyncClient.getPathUrl();
    }

    /**
     * Gets the associated account name.
     *
     * @return Account name associated with this storage resource.
     */
    public String getAccountName() {
        return pathAsyncClient.getAccountName();
    }

    /**
     * Gets the name of the File System in which this object lives.
     *
     * @return The name of the File System.
     */
    public String getFileSystemName() {
        return pathAsyncClient.getFileSystemName();
    }

    /**
     * Gets the path of this object, not including the name of the resource itself.
     *
     * @return The path of the object.
     */
    protected String getObjectPath() {
        return pathAsyncClient.getObjectPath();
    }

    /**
     * Gets the name of this object, not including its full path.
     *
     * @return The name of the object.
     */
    public String getObjectName() {
        return pathAsyncClient.getObjectName();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return pathAsyncClient.getHttpPipeline();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public DataLakeServiceVersion getServiceVersion() {
        return pathAsyncClient.getServiceVersion();
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
        return blockBlobClient.setMetadataWithResponse(metadata, Transforms.toBlobAccessConditions(accessConditions),
            timeout, context);
    }

    /**
     * Changes a resources's HTTP header properties. if only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.setHttpHeaders#PathHttpHeaders}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHttpHeaders}
     */
    public void setHttpHeaders(PathHttpHeaders headers) {
        setHttpHeadersWithResponse(headers, null, null, Context.NONE);
    }

    /**
     * Changes a resources's HTTP header properties. if only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.setHttpHeadersWithResponse#PathHttpHeaders-PathAccessConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHttpHeaders}
     * @param accessConditions {@link PathAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> setHttpHeadersWithResponse(PathHttpHeaders headers, PathAccessConditions accessConditions,
        Duration timeout, Context context) {
        return blockBlobClient.setHttpHeadersWithResponse(Transforms.toBlobHttpHeaders(headers),
            Transforms.toBlobAccessConditions(accessConditions), timeout, context);
    }

    /**
     * Changes the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.setAccessControl#PathAccessControl}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControl {@link PathAccessControl}
     * @return The resource info.
     */
    public PathInfo setAccessControl(PathAccessControl accessControl) {
        return setAccessControlWithResponse(accessControl, null, null, null).getValue();
    }

    /**
     * Changes the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.setAccessControlWithResponse#PathAccessControl-PathAccessConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControl {@link PathAccessControl}
     * @param accessConditions {@link PathAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource info.
     */
    public Response<PathInfo> setAccessControlWithResponse(PathAccessControl accessControl,
        PathAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<PathInfo>> response = pathAsyncClient.setAccessControlWithResponse(accessControl, accessConditions,
            context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.getAccessControl}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/getproperties">Azure Docs</a></p>
     *
     * @return The resource access control.
     */
    public PathAccessControl getAccessControl() {
        return getAccessControlWithResponse(false, null, null, null).getValue();
    }

    /**
     * Returns the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathAsyncClient.getAccessControl#boolean-PathAccessConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/getproperties">Azure Docs</a></p>
     *
     * @param returnUpn When true, user identity values returned as User Principal Names. When false, user identity
     * values returned as Azure Active Directory Object IDs. Default value is false.
     * @param accessConditions {@link PathAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource access control.
     */
    public Response<PathAccessControl> getAccessControlWithResponse(boolean returnUpn,
        PathAccessConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<PathAccessControl>> response = pathAsyncClient.getAccessControlWithResponse(returnUpn, accessConditions,
            context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    private PathClient getPathClient(String destinationPath) {
        return new PathClient(pathAsyncClient.getPathAsyncClient(destinationPath),
            pathAsyncClient.prepareBuilderReplacePath(destinationPath).buildBlockBlobClient());
    }

    /**
     * Returns the resources's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return The resource properties and metadata.
     */
    public PathProperties getProperties() {
        return getPropertiesWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the file's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathClient.getPropertiesWithResponse#PathAccessConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param accessConditions {@link PathAccessConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource properties and metadata.
     */
    public Response<PathProperties> getPropertiesWithResponse(PathAccessConditions accessConditions, Duration timeout,
        Context context) {
        Response<BlobProperties> response = blockBlobClient.getPropertiesWithResponse(
            Transforms.toBlobAccessConditions(accessConditions), timeout, context);
        return new SimpleResponse<>(response, Transforms.toPathProperties(response.getValue()));
    }

    /**
     * Package-private move method for use by {@link FileClient} and {@link DirectoryClient}
     *
     * @param destinationPath The path of the destination relative to the file system name
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource.
     * @param permissions POSIX access permissions for the directory owner, the directory owning group, and others.
     * @param umask Restricts permissions of the directory to be created.
     * @param sourceAccessConditions {@link PathAccessConditions} against the source.
     * @param destAccessConditions {@link PathAccessConditions} against the destination.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * PathClient} used to interact with the path created.
     */
    Mono<Response<PathClient>> moveWithResponse(String destinationPath,
        PathHttpHeaders headers, Map<String, String> metadata, String permissions, String umask,
        PathAccessConditions sourceAccessConditions, PathAccessConditions destAccessConditions, Context context) {

        destAccessConditions = destAccessConditions == null ? new PathAccessConditions() : destAccessConditions;
        sourceAccessConditions = sourceAccessConditions == null ? new PathAccessConditions()
            : sourceAccessConditions;

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        SourceModifiedAccessConditions sourceConditions = sourceAccessConditions.getModifiedAccessConditions() == null
            ? new SourceModifiedAccessConditions()
            : new SourceModifiedAccessConditions()
            .setSourceIfModifiedSince(sourceAccessConditions.getModifiedAccessConditions().getIfModifiedSince())
            .setSourceIfUnmodifiedSince(sourceAccessConditions.getModifiedAccessConditions().getIfUnmodifiedSince())
            .setSourceIfMatch(sourceAccessConditions.getModifiedAccessConditions().getIfMatch())
            .setSourceIfNoneMatch(sourceAccessConditions.getModifiedAccessConditions().getIfNoneMatch());

        PathClient pathClient = getPathClient(destinationPath);

        String renameSource = "/" + pathAsyncClient.getFileSystemName() + "/" + pathAsyncClient.getObjectPath();

        return pathAsyncClient.dataLakeStorage.paths().createWithRestResponseAsync(null /* pathResourceType */,
            null /* continuation */, PathRenameMode.LEGACY, renameSource,
            sourceAccessConditions.getLeaseAccessConditions().getLeaseId(),
            PathAsyncClient.buildMetadataString(metadata), permissions, umask, null /* request id */,
            null /* timeout */, headers, destAccessConditions.getLeaseAccessConditions(),
            destAccessConditions.getModifiedAccessConditions(), sourceConditions, context)
            .map(response -> new SimpleResponse<>(response, pathClient));
    }

}
