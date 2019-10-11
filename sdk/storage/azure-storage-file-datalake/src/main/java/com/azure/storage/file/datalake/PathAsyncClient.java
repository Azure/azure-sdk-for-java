package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.SR;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;
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
    private final ClientLogger logger = new ClientLogger(PathAsyncClient.class);

    protected final DataLakeStorageClientImpl dataLakeStorage;
    protected final String accountName;
    protected final BlockBlobAsyncClient blockBlobAsyncClient;

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
     * Converts the metadata into a string of format "key1=value1, key2=value2" and Base64 encodes the values.
     *
     * @param metadata The metadata.
     * @return The metadata represented as a String.
     */
    protected static String buildMetadataString(Map<String, String> metadata){
        StringBuilder sb = new StringBuilder();
        if (metadata != null && !metadata.isEmpty()) {
            for (final Map.Entry<String, String> entry : metadata.entrySet()) {
                if (Objects.isNull(entry.getKey()) || entry.getKey().isEmpty()) {
                    throw new IllegalArgumentException(SR.METADATA_KEY_INVALID);
                }
                else if (Objects.isNull(entry.getValue()) || entry.getValue().isEmpty()) {
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
            sb.deleteCharAt(sb.length()-1); // Remove the extraneous "," after the last element.
        }
        return sb.toString();
    }

    Mono<Response<PathItem>> createWithResponse(PathResourceType resourceType, PathHTTPHeaders httpHeaders,
        Map<String, String> metadata, String permissions, String umask, PathAccessConditions accessConditions,
        Context context) {
        return this.dataLakeStorage.paths().createWithRestResponseAsync(resourceType, null, null, null, null,
            buildMetadataString(metadata), permissions, umask, null, null, httpHeaders,
            accessConditions.getLeaseAccessConditions(), accessConditions.getModifiedAccessConditions(), null, context)
            .map(response -> new SimpleResponse<>(response, new PathItem(response.getDeserializedHeaders())));
    }
}
