// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.implementation.AzureFileStorageImpl;
import com.azure.storage.file.models.CorsRule;
import com.azure.storage.file.models.DeleteSnapshotsOptionType;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesIncludeType;
import com.azure.storage.file.models.ListSharesOptions;
import com.azure.storage.file.models.ListSharesResponse;
import com.azure.storage.file.models.ServicesListSharesSegmentResponse;
import com.azure.storage.file.models.ShareItem;
import com.azure.storage.file.models.StorageErrorException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides a client that contains all the operations for interacting with a file account in Azure Storage.
 * Operations allowed by the client are creating, listing, and deleting shares and retrieving and updating properties
 * of the account.
 *
 * <p><strong>Instantiating an Asynchronous File Service Client</strong></p>
 *
 * <pre>
 * FileServiceAsyncClient client = FileServiceAsyncClient.builder()
 *     .connectionString(connectionString)
 *     .endpoint(endpoint)
 *     .buildAsync();
 * </pre>
 *
 * <p>View {@link FileServiceClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see FileServiceClientBuilder
 * @see FileServiceClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public final class FileServiceAsyncClient {
    private final AzureFileStorageImpl client;

    /**
     * Creates a FileServiceClient that sends requests to the storage account at {@code endpoint}.
     * Each service call goes through the {@code httpPipeline}.
     *
     * @param endpoint URL for the Storage File service
     * @param httpPipeline HttpPipeline that the HTTP requests and responses flow through
     */
    FileServiceAsyncClient(URL endpoint, HttpPipeline httpPipeline) {
        this.client = new AzureFileStorageBuilder().pipeline(httpPipeline)
            .url(endpoint.toString())
            .build();
    }

    /**
     * Creates a builder that can configure options for the FileServiceAsyncClient before creating an instance of it.
     *
     * @return A new {@link FileServiceClientBuilder} used to create FileServiceAsyncClient instances.
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
     * Constructs a ShareAsyncClient that interacts with the specified share.
     *
     * <p>If the share doesn't exist in the storage account {@link ShareAsyncClient#create() create} in the client will
     * need to be called before interaction with the share can happen.</p>
     *
     * @param shareName Name of the share
     * @return a ShareAsyncClient that interacts with the specified share
     */
    public ShareAsyncClient getShareAsyncClient(String shareName) {
        return new ShareAsyncClient(client, shareName);
    }

    /**
     * Lists all shares in the storage account without their metadata or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares in the account</p>
     *
     * <pre>
     * client.listShares()
     *     .subscribe(result -&gt; System.out.printf("Share %s exists in the account", result.name()));
     * </pre>
     *
     * @return {@link ShareItem Shares} in the storage account without their metadata or snapshots
     */
    public Flux<ShareItem> listShares() {
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
     * client.listShares(new ListSharesOptions().prefix("azure"))
     *     .subscribe(result -&gt; System.out.printf("Share %s exists in the account", result.name()));
     * </pre>
     *
     * <p>List all shares including their snapshots and metadata</p>
     *
     * <pre>
     * client.listShares(new ListSharesOptions().includeMetadata(true).includeSnapshots(true))
     *     .subscribe(result -&gt; System.out.printf("Share %s, Is Snapshot? %b, Metadata: %s", result.name(), result.snapshot() != null, result.metadata()));
     * </pre>
     *
     * @param options Options for listing shares
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     */
    public Flux<ShareItem> listShares(ListSharesOptions options) {
        return listShares(null, options);
    }

    /**
     * Lists the shares in the storage account that pass the filter starting at the specified marker.
     *
     * @param marker Starting point to list the shares
     * @param options Options for listing shares
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     */
    private Flux<ShareItem> listShares(String marker, ListSharesOptions options) {
        String prefix = null;
        Integer maxResults = null;
        List<ListSharesIncludeType> include = new ArrayList<>();

        if (options != null) {
            prefix = options.prefix();
            maxResults = options.maxResults();

            if (options.includeMetadata()) {
                include.add(ListSharesIncludeType.fromString(ListSharesIncludeType.METADATA.toString()));
            }

            if (options.includeSnapshots()) {
                include.add(ListSharesIncludeType.fromString(ListSharesIncludeType.SNAPSHOTS.toString()));
            }
        }

        return client.services().listSharesSegmentWithRestResponseAsync(prefix, marker, maxResults, include, null, Context.NONE)
            .flatMapMany(response -> Flux.fromIterable(response.value().shareItems()));
    }

    /*
     * Helper function used to auto-enumerate through paged responses
     */
    private Flux<ShareItem> listShares(ServicesListSharesSegmentResponse response, List<ListSharesIncludeType> include, Context context) {
        ListSharesResponse value = response.value();
        Mono<ServicesListSharesSegmentResponse> result = client.services()
            .listSharesSegmentWithRestResponseAsync(value.prefix(), value.marker(), value.maxResults(), include, null, context);

        return result.flatMapMany(r -> extractAndFetchShares(r, include, context));
    }

    /*
     * Helper function used to auto-enumerate through paged responses
     */
    private Publisher<ShareItem> extractAndFetchShares(ServicesListSharesSegmentResponse response, List<ListSharesIncludeType> include, Context context) {
        String nextPageLink = response.value().nextMarker();
        if (ImplUtils.isNullOrEmpty(nextPageLink)) {
            return Flux.fromIterable(response.value().shareItems());
        }

        return Flux.fromIterable(response.value().shareItems()).concatWith(listShares(response, include, context));
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
     * client.getProperties()
     *    .subscribe(response -&gt; {
     *        FileServiceProperties properties = response.value();
     *        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b", properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
     *    });
     * </pre>
     *
     * @return Storage account File service properties
     */
    public Mono<Response<FileServiceProperties>> getProperties() {
        return client.services().getPropertiesWithRestResponseAsync(Context.NONE)
            .map(response -> mapToResponse(response, response.value()));
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
     * FileServiceProperties properties = client.getProperties().block().value();
     * properties.cors(Collections.emptyList());
     *
     * client.setProperties(properties)
     *     .subscribe(response -&gt; System.out.printf("Setting File service properties completed with status code %d", response.statusCode()));
     * </pre>
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * <pre>
     * FileServiceProperties properties = client.getProperties().block().value();
     * properties.minuteMetrics().enabled(true);
     * properties.hourMetrics().enabled(true);
     *
     * client.setProperties(properties)
     *     .subscribe(response -&gt; System.out.printf("Setting File service properties completed with status code %d", response.statusCode()));
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
    public Mono<VoidResponse> setProperties(FileServiceProperties properties) {
        return client.services().setPropertiesWithRestResponseAsync(properties, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * Creates a share in the storage account with the specified name and returns a ShareAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test"</p>
     *
     * <pre>
     * client.createShare("test")
     *     .subscribe(response -&gt; System.out.printf("Creating the share completed with status code %d", response.statusCode()));
     * </pre>
     *
     * @param shareName Name of the share
     * @return A response containing the ShareAsyncClient and the status of creating the share.
     * @throws StorageErrorException If a share with the same name already exists
     */
    public Mono<Response<ShareAsyncClient>> createShare(String shareName) {
        return createShare(shareName, null, null);
    }

    /**
     * Creates a share in the storage account with the specified name and metadata and returns a ShareAsyncClient to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test" with metadata "share:metadata"</p>
     *
     * <pre>
     * client.createShare("test", Collections.singletonMap("share", "metadata"), null)
     *     .subscribe(response -&gt; System.out.printf("Creating the share completed with status code %d", response.statusCode()));
     * </pre>
     *
     * <p>Create the share "test" with a quota of 10 GB</p>
     *
     * <pre>
     * client.createShare("test", null, 10)
     *     .subscribe(response -&gt; System.out.printf("Creating the share completed with status code %d", response.statusCode()));
     * </pre>
     *
     * @param shareName Name of the share
     * @param metadata Optional. Metadata to associate with the share
     * @param quotaInGB Optional. Maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @return A response containing the ShareAsyncClient and the status of creating the share.
     * @throws StorageErrorException If a share with the same name already exists or {@code quotaInGB} is outside the
     * allowed range.
     */
    public Mono<Response<ShareAsyncClient>> createShare(String shareName, Map<String, String> metadata, Integer quotaInGB) {
        ShareAsyncClient shareAsyncClient = new ShareAsyncClient(client, shareName);

        return shareAsyncClient.create(metadata, quotaInGB)
            .map(response -> mapToResponse(response, shareAsyncClient));
    }

    /**
     * Deletes the share in the storage account with the given name
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share "test"</p>
     *
     * <pre>
     * client.deleteShare("test")
     *     .subscribe(response -&gt; System.out.printf("Deleting the share completed with status code %d", response.statusCode()));
     * </pre>
     *
     * @param shareName Name of the share
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist
     */
    public Mono<VoidResponse> deleteShare(String shareName) {
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
     * client.deleteShare("test", midnight.toString())
     *     .subscribe(response -&gt; System.out.printf("Deleting the snapshot completed with status code %d", response.statusCode()));
     * </pre>
     *
     * @param shareName Name of the share
     * @param shareSnapshot Identifier of the snapshot
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist or the snapshot doesn't exist
     */
    public Mono<VoidResponse> deleteShare(String shareName, String shareSnapshot) {
        DeleteSnapshotsOptionType deleteSnapshots = null;
        if (ImplUtils.isNullOrEmpty(shareSnapshot)) {
            deleteSnapshots = DeleteSnapshotsOptionType.fromString(DeleteSnapshotsOptionType.INCLUDE.toString());
        }
        return client.shares().deleteWithRestResponseAsync(shareName, shareSnapshot, null, deleteSnapshots, Context.NONE)
            .map(VoidResponse::new);
    }

    static <T> Response<T> mapToResponse(Response<?> response, T value) {
        return new SimpleResponse<>(response.request(), response.statusCode(), response.headers(), value);
    }
}
