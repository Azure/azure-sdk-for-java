package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.file.datalake.implementation.DataLakeStorageClientImpl;

/**
 * This class provides a client that contains all operations that apply to any path object.
 */
public class PathAsyncClient {
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
}
