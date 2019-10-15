package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.SR;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.models.PathHTTPHeaders;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import com.azure.storage.file.datalake.models.PathItem;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a client that contains all operations that apply to any path object.
 */
public abstract class PathAsyncClient {
    protected final DataLakeStorageClientImpl dataLakeStorage;
    protected final String accountName;
    protected final BlockBlobAsyncClient blockBlobAsyncClient;
    private final ClientLogger logger = new ClientLogger(PathAsyncClient.class);

    /**
     * Package-private constructor for use by {@link PathClientBuilder}.
     *
     * @param dataLakeStorage The API client for data lake storage.
     * @param accountName The account name for storage account.
     */
    protected PathAsyncClient(DataLakeStorageClientImpl dataLakeStorage, String accountName,
        BlockBlobAsyncClient blockBlobAsyncClient) {
        this.dataLakeStorage = dataLakeStorage;
        this.accountName = accountName;
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
        if (metadata != null && !metadata.isEmpty()) {
            for (final Map.Entry<String, String> entry : metadata.entrySet()) {
                if (Objects.isNull(entry.getKey()) || entry.getKey().isEmpty()) {
                    throw new IllegalArgumentException(SR.METADATA_KEY_INVALID);
                } else if (Objects.isNull(entry.getValue()) || entry.getValue().isEmpty()) {
                    throw new IllegalArgumentException(SR.METADATA_VALUE_INVALID);
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
        // TODO (rickle-msft): Implement DataLake URL
        return "";
    }

    /**
     * Gets the path of this object, not including the name of the resource itself.
     *
     * @return The path of the object.
     */
    public final String getObjectPath() {
        // TODO (rickle-msft): Implement DataLakeURL
        return "";
    }

    /**
     * Gets the name of this object, not including its full path.
     *
     * @return The name of the object.
     */
    public final String getObjectName() {
        // TODO (rickle-msft): Implement DataLakeURL
        return "";
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

    /**
     * Creates the resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet } TODO (rickle-msft)
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

    /**
     * Creates the resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet } TODO (rickle-msft) after samples folder added.
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     * @param httpHeaders {@link PathHTTPHeaders}
     * @param metadata Metadata to associate with the resource.
     * @param permissions TODO (rickle-msft) (depending on if we add permissions type).
     * @param umask When creating a file or directory and the parent folder does not have a default ACL, the umask
     * restricts the permissions of the file or directory to be created. The resulting permission is given by p & ^u,
     * where p is the permission and u is the umask. For example, if p is 0777 and u is 0057, then the resulting
     * permission is 0720. The default permission is 0777 for a directory and 0666 for a file. The default umask is
     * 0027. The umask must be specified in 4-digit octal notation (e.g. 0766).
     * @param accessConditions {@link PathAccessConditions}
     *
     * @return A reactive response containing the information of the created resource.
     */
    public abstract Mono<Response<PathItem>> createWithResponse(PathHTTPHeaders httpHeaders,
        Map<String, String> metadata, String permissions, String umask, PathAccessConditions accessConditions);

    Mono<Response<PathItem>> createWithResponse(PathResourceType resourceType, PathHTTPHeaders httpHeaders,
        Map<String, String> metadata, String permissions, String umask, PathAccessConditions accessConditions,
        Context context) {
        return this.dataLakeStorage.paths().createWithRestResponseAsync(resourceType, null, null, null, null,
            buildMetadataString(metadata), permissions, umask, null, null, httpHeaders,
            accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(), null, context)
            .map(response -> new SimpleResponse<>(response, new PathItem(response.getDeserializedHeaders())));
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
     * @return A reactive response signalling completion.
     */
    public abstract Mono<Void> delete();

    Mono<Response<Void>> deleteWithResponse(Boolean recursive, PathAccessConditions accessConditions, Context context) {
        return this.dataLakeStorage.paths().deleteWithRestResponseAsync(recursive, null, null, null,
            accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(), context)
            .map(response -> new SimpleResponse<Void>(response, null));
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet TODO (rickle-msft)
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
     * {@codesnippet TODO (rickle-msft)
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource.
     * @param accessConditions {@link PathAccessConditions}
     * @return A reactive response signalling completion.
     */
    // TODO (rickle-msft): Update return type based on api conversation
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        PathAccessConditions accessConditions) {
        return this.blockBlobAsyncClient.setMetadataWithResponse(metadata, toBlobAccessConditions(accessConditions));
    }

    // TODO (rickle-msft): Move these to the transform file when it has been pushed.
    static BlobAccessConditions toBlobAccessConditions(PathAccessConditions
        pathAccessConditions) {
        if (pathAccessConditions == null) {
            return null;
        } else {
            return new BlobAccessConditions()
                .setModifiedAccessConditions(toBlobModifiedAccessConditions(pathAccessConditions
                    .getModifiedAccessConditions()))
                .setLeaseAccessConditions(toBlobLeaseAccessConditions(pathAccessConditions
                    .getLeaseAccessConditions()));
        }
    }

    static com.azure.storage.blob.models.ModifiedAccessConditions toBlobModifiedAccessConditions(
        ModifiedAccessConditions pathModifiedAccessConditions) {
        if (pathModifiedAccessConditions == null) {
            return null;
        } else {
            return new com.azure.storage.blob.models.ModifiedAccessConditions()
                .setIfMatch(pathModifiedAccessConditions.getIfMatch())
                .setIfModifiedSince(pathModifiedAccessConditions.getIfModifiedSince())
                .setIfNoneMatch(pathModifiedAccessConditions.getIfNoneMatch())
                .setIfUnmodifiedSince(pathModifiedAccessConditions.getIfUnmodifiedSince());
        }
    }

    static com.azure.storage.blob.models.LeaseAccessConditions toBlobLeaseAccessConditions(LeaseAccessConditions
        pathLeaseAccessConditions) {
        if (pathLeaseAccessConditions == null) {
            return null;
        } else {
            return new com.azure.storage.blob.models.LeaseAccessConditions()
                .setLeaseId(pathLeaseAccessConditions.getLeaseId());
        }
    }

}
