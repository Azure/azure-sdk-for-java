// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.CorsRule;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesOptions;
import com.azure.storage.file.models.ShareItem;
import com.azure.storage.file.models.StorageErrorException;

import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting with a file account in Azure Storage.
 * Operations allowed by the client are creating, listing, and deleting shares and retrieving and updating properties
 * of the account.
 *
 * <p><strong>Instantiating a Synchronous File Service Client</strong></p>
 *
 * <pre>
 * FileServiceClient client = FileServiceClient.builder()
 *     .connectionString(connectionString)
 *     .endpoint(endpoint)
 *     .buildSync();
 * </pre>
 *
 * <p>View {@link FileServiceClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see FileServiceClientBuilder
 * @see FileServiceAsyncClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public final class FileServiceClient {
    private final FileServiceAsyncClient client;

    /**
     * Creates a FileServiceClient that wraps a FileServiceAsyncClient and blocks requests.
     *
     * @param client FileServiceAsyncClient that is used to send requests
     */
    FileServiceClient(FileServiceAsyncClient client) {
        this.client = client;
    }

    /**
     * Creates a builder that can configure options for the FileServiceClient before creating an instance of it.
     *
     * @return A new {@link FileServiceClientBuilder} used to create FileServiceClient instances.
     */
    public static FileServiceClientBuilder builder() {
        return new FileServiceClientBuilder();
    }

    /**
     * @return the URL of the Storage File service
     */
    public String url() {
        return client.url();
    }

    /**
     * Constructs a ShareClient that interacts with the specified share.
     *
     * <p>If the share doesn't exist in the storage account {@link ShareClient#create() create} in the client will
     * need to be called before interaction with the share can happen.</p>
     *
     * @param shareName Name of the share
     * @return a ShareClient that interacts with the specified share
     */
    public ShareClient getShareClient(String shareName) {
        return new ShareClient(client.getShareAsyncClient(shareName));
    }

    /**
     * Lists all shares in the storage account without their metadata or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares in the account</p>
     *
     * <pre>
     * for (ShareItem result : client.listShares()) {
     *     System.out.printf("Share %s exists in the account", result.name());
     * }
     * </pre>
     *
     * @return {@link ShareItem Shares} in the storage account without their metadata or snapshots
     */
    public Iterable<ShareItem> listShares() {
        return listShares(null);
    }

    /**
     * Lists the shares in the Storage account that pass the options filter.
     *
     * <p>Set starts with name filter using {@link ListSharesOptions#prefix(String) prefix} to filter shares that are
     * listed.</p>
     *
     * <p>Pass true to {@link ListSharesOptions#includeMetadata(boolean) includeMetadata} to have metadata returned for
     * the shares.</p>
     *
     * <p>Pass true to {@link ListSharesOptions#includeSnapshots(boolean) includeSnapshots} to have snapshots of the
     * shares listed.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares that begin with "azure"</p>
     *
     * <pre>
     * for (ShareItem result : client.listShares(new ListSharesOptions().prefix("azure"))) {
     *     System.out.printf("Share %s exists in the account", result.name());
     * }
     * </pre>
     *
     * <p>List all shares including their snapshots and metadata</p>
     *
     * <pre>
     * for (ShareItem result : client.listShares(new ListSharesOptions().includeMetadata(true).includeSnapshots(true))) {
     *     System.out.printf("Share %s, Is Snapshot? %b, Metadata: %s", result.name(), result.snapshot() != null, result.metadata());
     * }
     * </pre>
     *
     * @param options Options for listing shares
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     */
    public Iterable<ShareItem> listShares(ListSharesOptions options) {
        return client.listShares(options).toIterable();
    }

    /**
     * Retrieves the properties of the storage account's File service. The properties range from storage analytics and
     * metrics to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve File service properties</p>
     *
     * <pre>
     * FileServiceProperties properties = client.getProperties().value();
     * System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b", properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
     * </pre>
     *
     * @return Storage account File service properties
     */
    public Response<FileServiceProperties> getProperties() {
        return client.getProperties().block();
    }

    /**
     * Sets the properties for the storage account's File service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link FileServiceProperties#cors() CORS}.
     * To disable all CORS in the Queue service pass an empty list for {@link FileServiceProperties#cors() CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Clear CORS in the File service</p>
     *
     * <pre>
     * FileServiceProperties properties = client.getProperties().value();
     * properties.cors(Collections.emptyList());
     *
     * VoidResponse response = client.setProperties(properties);
     * System.out.printf("Setting File service properties completed with status code %d", response.statusCode());
     * </pre>
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * <pre>
     * FileServiceProperties properties = client.getProperties().value();
     * properties.minuteMetrics().enabled(true);
     * properties.hourMetrics().enabled(true);
     *
     * VoidResponse respone = client.setProperties(properties);
     * System.out.printf("Setting File service properties completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param properties Storage account File service properties
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException When one of the following is true
     * <ul>
     *     <li>A CORS rule is missing one of its fields</li>
     *     <li>More than five CORS rules will exist for the Queue service</li>
     *     <li>Size of all CORS rules exceeds 2KB</li>
     *     <li>
     *         Length of {@link CorsRule#allowedHeaders() allowed headers}, {@link CorsRule#exposedHeaders() exposed headers},
     *         or {@link CorsRule#allowedOrigins() allowed origins} exceeds 256 characters.
     *     </li>
     *     <li>{@link CorsRule#allowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or PUT</li>
     * </ul>
     */
    public VoidResponse setProperties(FileServiceProperties properties) {
        return client.setProperties(properties).block();
    }

    /**
     * Creates a share in the storage account with the specified name and returns a ShareClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test"</p>
     *
     * <pre>
     * Response&lt;ShareClient&gt; response = client.createShare("test");
     * System.out.printf("Creating the share completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param shareName Name of the share
     * @return A response containing the ShareClient and the status of creating the share.
     * @throws StorageErrorException If a share with the same name already exists
     */
    public Response<ShareClient> createShare(String shareName) {
        return createShare(shareName, null, null);
    }

    /**
     * Creates a share in the storage account with the specified name and metadata and returns a ShareClient to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test" with metadata "share:metadata"</p>
     *
     * <pre>
     * Response&lt;ShareClient&gt; response = client.createShare("test", Collections.singletonMap("share", "metadata"), null);
     * System.out.printf("Creating the share completed with status code %d", response.statusCode());
     * </pre>
     *
     * <p>Create the share "test" with a quota of 10 GB</p>
     *
     * <pre>
     * Response&lt;ShareClient&gt; response = client.createShare("test", null, 10)
     * System.out.printf("Creating the share completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param shareName Name of the share
     * @param metadata Optional. Metadata to associate with the share
     * @param quotaInGB Optional. Maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @return A response containing the ShareClient and the status of creating the share.
     * @throws StorageErrorException If a share with the same name already exists or {@code quotaInGB} is outside the
     * allowed range.
     */
    public Response<ShareClient> createShare(String shareName, Map<String, String> metadata, Integer quotaInGB) {
        Response<ShareAsyncClient> response = client.createShare(shareName, metadata, quotaInGB).block();
        return FileServiceAsyncClient.mapToResponse(response, new ShareClient(response.value()));
    }

    /**
     * Deletes the share in the storage account with the given name
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share "test"</p>
     *
     * <pre>
     * VoidResponse response = client.deleteShare("test");
     * System.out.printf("Deleting the share completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param shareName Name of the share
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist
     */
    public VoidResponse deleteShare(String shareName) {
        return deleteShare(shareName, null);
    }

    /**
     * Deletes the specific snapshot of the share in the storage account with the given name. Snapshot are identified
     * by the time they were created.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the snapshot of share "test" that was created at midnight</p>
     *
     * <pre>
     * OffsetDateTime midnight = OffsetDateTime.of(LocalTime.MIDNIGHT, ZoneOffset.UTC));
     * VoidResponse response = client.deleteShare("test", midnight.toString());
     * System.out.printf("Deleting the snapshot completed with status code %d", response.statusCode());
     * </pre>
     *
     * @param shareName Name of the share
     * @param shareSnapshot Identifier of the snapshot
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist or the snapshot doesn't exist
     */
    public VoidResponse deleteShare(String shareName, String shareSnapshot) {
        return client.deleteShare(shareName, shareSnapshot).block();
    }
}
