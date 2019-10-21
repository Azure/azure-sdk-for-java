package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientBuilder;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;
import com.azure.storage.file.datalake.implementation.models.PathGetPropertiesAction;
import com.azure.storage.file.datalake.implementation.models.PathHTTPHeaders;
import com.azure.storage.file.datalake.implementation.models.PathRenameMode;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.implementation.models.SourceModifiedAccessConditions;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathItem;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.implementation.util.FluxUtil.withContext;

/**
 * This class provides a client that contains all operations that apply to any path object.
 */
public class PathAsyncClient {
    protected final DataLakeStorageClientImpl dataLakeStorage;
    protected final String accountName;
    protected final String fileSystemName;
    protected final String pathName;
    protected final BlockBlobAsyncClient blockBlobAsyncClient;
    private final ClientLogger logger = new ClientLogger(PathAsyncClient.class);

    /**
     * Package-private constructor for use by {@link PathClientBuilder}.
     *
     * @param dataLakeStorage The API client for data lake storage.
     * @param accountName The account name for storage account.
     */
    protected PathAsyncClient(DataLakeStorageClientImpl dataLakeStorage, String accountName, String fileSystemName,
        String pathName, BlockBlobAsyncClient blockBlobAsyncClient) {
        this.dataLakeStorage = dataLakeStorage;
        this.accountName = accountName;
        this.fileSystemName = fileSystemName;
        this.pathName = pathName;
        this.blockBlobAsyncClient = blockBlobAsyncClient;
    }

    /**
     * Converts the metadata into a string of format "key1=value1, key2=value2" and Base64 encodes the values.
     *
     * @param metadata The metadata.
     *
     * @return The metadata represented as a String.
     */
    protected static String buildMetadataString(Map<String, String> metadata) {
        StringBuilder sb = new StringBuilder();
        if (!ImplUtils.isNullOrEmpty(metadata)) {
            for (final Map.Entry<String, String> entry : metadata.entrySet()) {
                if (Objects.isNull(entry.getKey()) || entry.getKey().isEmpty()) {
                    throw new IllegalArgumentException("The key for one of the metadata key-value pairs is null, "
                        + "empty, or whitespace.");
                } else if (Objects.isNull(entry.getValue()) || entry.getValue().isEmpty()) {
                    throw new IllegalArgumentException("The value for one of the metadata key-value pairs is null, "
                        + "empty, or whitespace.");
                }

                /*
                The service has an internal base64 decode when metadata is copied from ADLS to Storage, so getMetadata
                will work as normal. Doing this encoding for the customers preserves the existing behavior of
                metadata.
                 */
                sb.append(entry.getKey()).append('=')
                    .append(new String(Base64.getEncoder().encode(entry.getValue().getBytes()),
                        Charset.forName("UTF-8"))).append(',');
            }
            sb.deleteCharAt(sb.length() - 1); // Remove the extraneous "," after the last element.
        }
        return sb.toString();
    }

    /**
     * Gets the URL of the object represented by this client on the Data Lake service.
     *
     * @return the URL.
     */
    public String getDataLakeUrl() {
        return dataLakeStorage.getUrl();
    }

    /**
     * Gets the URL of the object represented by this client on the blob service.
     *
     * @return the URL.
     */
    public String getBlobUrl() {
        return blockBlobAsyncClient.getBlobUrl();
    }

    /**
     * Gets the associated account name.
     *
     * @return Account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Gets the name of the File System in which this object lives.
     *
     * @return The name of the File System.
     */
    public String getFileSystemName() {
        return this.fileSystemName;
    }

    /**
     * Gets the path of this object, not including the name of the resource itself.
     *
     * @return The path of the object.
     */
    public final String getObjectPath() {
        return this.pathName;
    }

    /**
     * Gets the name of this object, not including its full path.
     *
     * @return The name of the object.
     */
    public final String getObjectName() {
        String[] pathParts = pathName.split("/");
        return pathParts[pathParts.length - 1];
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return dataLakeStorage.getHttpPipeline();
    }

    /**
     * Gets the inner {@link BlockBlobAsyncClient}.
     *
     * @return The BlockBlobClient.
     */
    public BlockBlobAsyncClient getBlockBlobAsyncClient() {
        return this.blockBlobAsyncClient;
    }

    Mono<Response<PathItem>> createWithResponse(PathResourceType resourceType, PathHTTPHeaders httpHeaders,
        Map<String, String> metadata, String permissions, String umask, PathAccessConditions accessConditions,
        Context context) {
        accessConditions = accessConditions == null ? new PathAccessConditions() : accessConditions;

        return this.dataLakeStorage.paths().createWithRestResponseAsync(resourceType, null, null, null, null,
            buildMetadataString(metadata), permissions, umask, null, null, httpHeaders,
            accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(), null, context)
            .map(response -> new SimpleResponse<>(response, new PathItem(response.getDeserializedHeaders())));
    }

    Mono<Response<Void>> deleteWithResponse(Boolean recursive, PathAccessConditions accessConditions, Context context) {
        accessConditions = accessConditions == null ? new PathAccessConditions() : accessConditions;

        return this.dataLakeStorage.paths().deleteWithRestResponseAsync(recursive, null, null, null,
            accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathAsyncClient.setMetadata#Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource.
     * @return A reactive response signalling completion.
     */
    public Mono<Void> setMetadata(Map<String, String> metadata) {
        return this.setMetadataWithResponse(metadata, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathAsyncClient.setMetadata#Map-PathAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource.
     * @param accessConditions {@link PathAccessConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        PathAccessConditions accessConditions) {
        return this.blockBlobAsyncClient.setMetadataWithResponse(metadata,
            Transforms.toBlobAccessConditions(accessConditions));
    }


    /**
     * Changes a resource's HTTP header properties. if only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathAsyncClient.setHTTPHeaders#PathHTTPHeaders}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHTTPHeaders}
     * @return A reactive response signalling completion.
     */
    public Mono<Void> setHTTPHeaders(PathHTTPHeaders headers) {
        return setHTTPHeadersWithResponse(headers, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Changes a resources's HTTP header properties. if only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.PathAsyncClient.setHTTPHeadersWithResponse#PathHTTPHeaders-PathAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHTTPHeaders}
     * @param accessConditions {@link PathAccessConditions}
     * @return A reactive response signalling completion.
     */
    public Mono<Response<Void>> setHTTPHeadersWithResponse(PathHTTPHeaders headers,
        PathAccessConditions accessConditions) {
        return this.blockBlobAsyncClient.setHTTPHeadersWithResponse(Transforms.toBlobHttpHeaders(headers),
            Transforms.toBlobAccessConditions(accessConditions));
    }

    // Set access control

    public Mono<PathInfo> setAccessControl(PathAccessControl accessControl) {
        return setAccessControlWithResponse(accessControl, null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<PathInfo>> setAccessControlWithResponse(PathAccessControl accessControl,
        PathAccessConditions accessConditions) {
        return withContext(context -> setAccessControlWithResponse(accessControl, accessConditions, context));
    }

    Mono<Response<PathInfo>> setAccessControlWithResponse(PathAccessControl accessControl,
        PathAccessConditions accessConditions, Context context) {

        Objects.requireNonNull(accessControl, "accessControl can not be null");
        accessConditions = accessConditions == null ? new PathAccessConditions() : accessConditions;

        return this.dataLakeStorage.paths().setAccessControlWithRestResponseAsync(null, accessControl.getOwner(),
            accessControl.getGroup(), accessControl.getPermissions(), accessControl.getAcl(), null,
            accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(), context)
            .map(response -> new SimpleResponse<>(response, new PathInfo(response.getDeserializedHeaders())));
    }

    // Get access control

    public Mono<PathAccessControl> getAccessControl() {
        return getAccessControlWithResponse(false, null).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<PathAccessControl>> getAccessControlWithResponse(boolean upn,
        PathAccessConditions accessConditions) {
        return withContext(context -> getAccessControlWithResponse(upn, accessConditions, context));
    }

    Mono<Response<PathAccessControl>> getAccessControlWithResponse(boolean upn, PathAccessConditions accessConditions,
        Context context) {
        accessConditions = accessConditions == null ? new PathAccessConditions() : accessConditions;

        return this.dataLakeStorage.paths().getPropertiesWithRestResponseAsync(
            PathGetPropertiesAction.GET_ACCESS_CONTROL, upn, null, null, accessConditions.getLeaseAccessConditions(),
            accessConditions.getModifiedAccessConditions(), context).map(response ->
            new SimpleResponse<>(response, new PathAccessControl(response.getDeserializedHeaders())));
    }


    // Move

    Mono<Response<PathAsyncClient>> moveWithResponse(String destinationPath,
        PathHTTPHeaders httpHeaders, Map<String, String> metadata, String permissions, String umask,
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

        PathAsyncClient pathAsyncClient = getPathAsyncClient(destinationPath);

        String renameSource = "/" + fileSystemName + "/" + pathName;

        return pathAsyncClient.dataLakeStorage.paths().createWithRestResponseAsync(null /* pathResourceType */,
            null /* continuation */, PathRenameMode.LEGACY, renameSource,
            sourceAccessConditions.getLeaseAccessConditions().getLeaseId(), buildMetadataString(metadata), permissions,
            umask, null /* request id */, null /* timeout */, httpHeaders,
            destAccessConditions.getLeaseAccessConditions(), destAccessConditions.getModifiedAccessConditions(),
            sourceConditions, context).map(response -> new SimpleResponse<>(response, pathAsyncClient));
    }

    /**
     * Takes in a destination path and creates a PathAsyncClient with a new path name
     * @param destinationPath The destination path
     * @return A PathAsyncClient
     */
    PathAsyncClient getPathAsyncClient(String destinationPath) {
        if (ImplUtils.isNullOrEmpty(destinationPath)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'destinationPath' can not be set to null"));
        }
        try {
            // Get current Datalake URL and replace current path with user provided path
            String newDfsEndpoint = BlobUrlParts.parse(getDataLakeUrl(), logger)
                .setBlobName(destinationPath).toURL().toString();

            return new PathAsyncClient(new DataLakeStorageClientBuilder()
                .url(newDfsEndpoint)
                .pipeline(dataLakeStorage.getHttpPipeline())
                .build(), accountName, fileSystemName, destinationPath,
                prepareBuilderReplacePath(destinationPath).buildBlockBlobAsyncClient());
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
    }

    /**
     * Takes in a destination path and creates a SpecializedBlobClientBuilder with a new path name
     * @param destinationPath The destination path
     * @return An updated SpecializedBlobClientBuilder
     */
    SpecializedBlobClientBuilder prepareBuilderReplacePath(String destinationPath) {
        try {
            // Get current Blob URL and replace current path with user provided path
            String newBlobEndpoint = BlobUrlParts.parse(getBlobUrl(), logger)
                .setBlobName(destinationPath).toURL().toString();

            return new SpecializedBlobClientBuilder()
                .pipeline(getHttpPipeline())
                .endpoint(newBlobEndpoint);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
    }

    // Get properties
    /**
     * Returns the blob's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return A reactive response containing the blob properties and metadata.
     */
    public Mono<PathProperties> getProperties() {
        return getPropertiesWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the blob's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.BlobAsyncClientBase.getPropertiesWithResponse#BlobAccessConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param accessConditions {@link PathAccessConditions}
     * @return A reactive response containing the blob properties and metadata.
     */
    public Mono<Response<PathProperties>> getPropertiesWithResponse(PathAccessConditions accessConditions) {
        return blockBlobAsyncClient.getPropertiesWithResponse(Transforms.toBlobAccessConditions(accessConditions))
            .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), Transforms.toPathProperties(response.getValue())));
    }
}
