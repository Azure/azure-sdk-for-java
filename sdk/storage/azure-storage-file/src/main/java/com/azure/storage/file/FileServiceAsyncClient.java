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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class provides a azureFileStorageClient that contains all the operations for interacting with a file account in Azure Storage.
 * Operations allowed by the azureFileStorageClient are creating, listing, and deleting shares and retrieving and updating properties
 * of the account.
 *
 * <p><strong>Instantiating an Asynchronous File Service Client</strong></p>
 *
 * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.instantiation}
 *
 * <p>View {@link FileServiceClientBuilder this} for additional ways to construct the azureFileStorageClient.</p>
 *
 * @see FileServiceClientBuilder
 * @see FileServiceClient
 * @see SharedKeyCredential
 * @see SASTokenCredential
 */
public final class FileServiceAsyncClient {
    private final AzureFileStorageImpl azureFileStorageClient;

    /**
     * Creates a FileServiceClient that sends requests to the storage account at {@code endpoint}.
     * Each service call goes through the {@code httpPipeline}.
     *
     * @param endpoint URL for the Storage File service
     * @param httpPipeline HttpPipeline that the HTTP requests and responses flow through
     */
    FileServiceAsyncClient(URL endpoint, HttpPipeline httpPipeline) {
        this.azureFileStorageClient = new AzureFileStorageBuilder().pipeline(httpPipeline)
            .url(endpoint.toString())
            .build();
    }

    /**
     * Get the url of the storage file service client.
     * @return the url of the Storage File service.
     * @throws RuntimeException If the file service is using a malformed URL.
     */
    public URL getFileServiceUrl() {
        try {
            return new URL(azureFileStorageClient.getUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Invalid URL on %s: %s" + getClass().getSimpleName(),
                azureFileStorageClient.getUrl()), e);
        }
    }

    /**
     * Constructs a ShareAsyncClient that interacts with the specified share.
     *
     * <p>If the share doesn't exist in the storage account {@link ShareAsyncClient#create() create} in the azureFileStorageClient will
     * need to be called before interaction with the share can happen.</p>
     *
     * @param shareName Name of the share
     * @return a ShareAsyncClient that interacts with the specified share
     */
    public ShareAsyncClient getShareAsyncClient(String shareName) {
        return new ShareAsyncClient(azureFileStorageClient, shareName);
    }

    /**
     * Lists all shares in the storage account without their metadata or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares in the account</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.listShares}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
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
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.listShares#ListSharesOptions.prefix}
     *
     * <p>List all shares including their snapshots and metadata</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.listShares#ListSharesOptions.metadata.snapshot}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
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

        return azureFileStorageClient.services().listSharesSegmentWithRestResponseAsync(prefix, marker, maxResults, include, null, Context.NONE)
            .flatMapMany(response -> Flux.fromIterable(response.value().shareItems()));
    }

    /*
     * Helper function used to auto-enumerate through paged responses
     */
    private Flux<ShareItem> listShares(ServicesListSharesSegmentResponse response, List<ListSharesIncludeType> include, Context context) {
        ListSharesResponse value = response.value();
        Mono<ServicesListSharesSegmentResponse> result = azureFileStorageClient.services()
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
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-file-service-properties">Azure Docs</a>.</p>
     *
     * @return Storage account File service properties
     */
    public Mono<Response<FileServiceProperties>> getProperties() {
        return azureFileStorageClient.services().getPropertiesWithRestResponseAsync(Context.NONE)
            .map(response -> new SimpleResponse<>(response, response.value()));
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
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.setProperties#fileServiceProperties.clearCORS}
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.setProperties#fileServiceProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-service-properties">Azure Docs</a>.</p>
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
        return azureFileStorageClient.services().setPropertiesWithRestResponseAsync(properties, Context.NONE)
            .map(VoidResponse::new);
    }

    /**
     * Creates a share in the storage account with the specified name and returns a ShareAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test"</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.createShare#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @return A response containing the ShareAsyncClient and the status of creating the share.
     * @throws StorageErrorException If a share with the same name already exists
     */
    public Mono<Response<ShareAsyncClient>> createShare(String shareName) {
        return createShare(shareName, null, null);
    }

    /**
     * Creates a share in the storage account with the specified name, metadata, and quota and returns a ShareAsyncClient to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test" with metadata "share:metadata"</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.createShare#string-map-integer.metadata}
     *
     * <p>Create the share "test" with a quota of 10 GB</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.createShare#string-map-integer.quota}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. This must be greater than 0 and
     * less than or equal to 5120. The default value is 5120.
     * @return A response containing the ShareAsyncClient and the status of creating the share.
     * @throws StorageErrorException If a share with the same name already exists or {@code quotaInGB} is outside the
     * allowed range.
     */
    public Mono<Response<ShareAsyncClient>> createShare(String shareName, Map<String, String> metadata, Integer quotaInGB) {
        ShareAsyncClient shareAsyncClient = new ShareAsyncClient(azureFileStorageClient, shareName);

        return shareAsyncClient.create(metadata, quotaInGB)
            .map(response -> new SimpleResponse<>(response, shareAsyncClient));
    }

    /**
     * Deletes the share in the storage account with the given name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share "test"</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.deleteShare#string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
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
     * <p>Delete the snapshot of share "test" that was created at current time.</p>
     *
     * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.deleteShare#string-string}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param snapshot Identifier of the snapshot
     * @return A response that only contains headers and response status code
     * @throws StorageErrorException If the share doesn't exist or the snapshot doesn't exist
     */
    public Mono<VoidResponse> deleteShare(String shareName, String snapshot) {
        DeleteSnapshotsOptionType deleteSnapshots = null;
        if (ImplUtils.isNullOrEmpty(snapshot)) {
            deleteSnapshots = DeleteSnapshotsOptionType.fromString(DeleteSnapshotsOptionType.INCLUDE.toString());
        }
        return azureFileStorageClient.shares().deleteWithRestResponseAsync(shareName, snapshot, null, deleteSnapshots, Context.NONE)
            .map(VoidResponse::new);
    }

}
