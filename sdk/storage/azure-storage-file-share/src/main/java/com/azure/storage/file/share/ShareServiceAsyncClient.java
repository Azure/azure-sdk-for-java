// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.AccountSasImplUtil;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.models.DeleteSnapshotsOptionType;
import com.azure.storage.file.share.implementation.models.ListSharesIncludeType;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.ListSharesOptions;
import com.azure.storage.file.share.models.ShareCorsRule;
import com.azure.storage.file.share.models.ShareItem;
import com.azure.storage.file.share.models.ShareServiceProperties;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareCreateOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;


/**
 * This class provides a azureFileStorageClient that contains all the operations for interacting with a file account in
 * Azure Storage. Operations allowed by the azureFileStorageClient are creating, listing, and deleting shares and
 * retrieving and updating properties of the account.
 *
 * <p><strong>Instantiating an Asynchronous File Service Client</strong></p>
 *
 * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.instantiation -->
 * <pre>
 * ShareAsyncClient client = new ShareClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;$&#123;connectionString&#125;&quot;&#41;
 *     .endpoint&#40;&quot;$&#123;endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.instantiation -->
 *
 * <p>View {@link ShareServiceClientBuilder this} for additional ways to construct the azureFileStorageClient.</p>
 *
 * @see ShareServiceClientBuilder
 * @see ShareServiceClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareServiceClientBuilder.class, isAsync = true)
public final class ShareServiceAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(ShareServiceAsyncClient.class);
    private final AzureFileStorageImpl azureFileStorageClient;
    private final String accountName;
    private final ShareServiceVersion serviceVersion;
    private final AzureSasCredential sasToken;

    /**
     * Creates a ShareServiceClient from the passed {@link AzureFileStorageImpl implementation client}.
     *
     * @param azureFileStorage Client that interacts with the service interfaces.
     */
    ShareServiceAsyncClient(AzureFileStorageImpl azureFileStorage, String accountName,
        ShareServiceVersion serviceVersion, AzureSasCredential sasToken) {
        this.azureFileStorageClient = azureFileStorage;
        this.accountName = accountName;
        this.serviceVersion = serviceVersion;
        this.sasToken = sasToken;
    }

    /**
     * Get the url of the storage file service client.
     *
     * @return the url of the Storage File service.
     */
    public String getFileServiceUrl() {
        return azureFileStorageClient.getUrl();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public ShareServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Constructs a ShareAsyncClient that interacts with the specified share.
     *
     * <p>If the share doesn't exist in the storage account {@link ShareAsyncClient#create() create} in the
     * azureFileStorageClient will
     * need to be called before interaction with the share can happen.</p>
     *
     * @param shareName Name of the share
     * @return a ShareAsyncClient that interacts with the specified share
     */
    public ShareAsyncClient getShareAsyncClient(String shareName) {
        return this.getShareAsyncClient(shareName, null);
    }

    /**
     * Constructs a ShareAsyncClient that interacts with the specified share.
     *
     * <p>If the share doesn't exist in the storage account {@link ShareAsyncClient#create() create} in the
     * azureFileStorageClient will
     * need to be called before interaction with the share can happen.</p>
     *
     * @param shareName Name of the share
     * @param snapshot Snapshot ID of the share
     * @return a ShareAsyncClient that interacts with the specified share
     */
    public ShareAsyncClient getShareAsyncClient(String shareName, String snapshot) {
        return new ShareAsyncClient(azureFileStorageClient, shareName, snapshot, accountName, serviceVersion, sasToken);
    }

    /**
     * Lists all shares in the storage account without their metadata or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares in the account</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.listShares -->
     * <pre>
     * fileServiceAsyncClient.listShares&#40;&#41;.subscribe&#40;
     *     shareItem -&gt; System.out.printf&#40;&quot;Share %s exists in the account&quot;, shareItem.getName&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete listing the shares!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.listShares -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
     *
     * @return {@link ShareItem Shares} in the storage account without their metadata or snapshots
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ShareItem> listShares() {
        return listShares(null);
    }

    /**
     * Lists the shares in the Storage account that pass the options filter.
     *
     * <p>Set starts with name filter using {@link ListSharesOptions#setPrefix(String) prefix} to filter shares that
     * are
     * listed.</p>
     *
     * <p>Pass true to {@link ListSharesOptions#setIncludeMetadata(boolean) includeMetadata} to have metadata returned
     * for
     * the shares.</p>
     *
     * <p>Pass true to {@link ListSharesOptions#setIncludeSnapshots(boolean) includeSnapshots} to have snapshots of the
     * shares listed.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares that begin with "azure"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.listShares#ListSharesOptions.prefix -->
     * <pre>
     * fileServiceAsyncClient.listShares&#40;new ListSharesOptions&#40;&#41;.setPrefix&#40;&quot;azure&quot;&#41;&#41;.subscribe&#40;
     *     shareItem -&gt; System.out.printf&#40;&quot;Share %s exists in the account&quot;, shareItem.getName&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete listing the shares!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.listShares#ListSharesOptions.prefix -->
     *
     * <p>List all shares including their snapshots and metadata</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.listShares#ListSharesOptions.metadata.snapshot -->
     * <pre>
     * fileServiceAsyncClient.listShares&#40;new ListSharesOptions&#40;&#41;.setIncludeMetadata&#40;true&#41;.setIncludeSnapshots&#40;true&#41;&#41;
     *     .subscribe&#40;
     *         shareItem -&gt; System.out.printf&#40;&quot;Share %s exists in the account&quot;, shareItem.getName&#40;&#41;&#41;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete listing the shares!&quot;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.listShares#ListSharesOptions.metadata.snapshot -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
     *
     * @param options Options for listing shares
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ShareItem> listShares(ListSharesOptions options) {
        try {
            return listSharesWithOptionalTimeout(null, options, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Lists the shares in the storage account that pass the filter starting at the specified marker.
     *
     * @param marker Starting point to list the shares
     * @param options Options for listing shares
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     */
    PagedFlux<ShareItem> listSharesWithOptionalTimeout(String marker, ListSharesOptions options, Duration timeout,
        Context context) {
        final String prefix = (options != null) ? options.getPrefix() : null;
        final Integer maxResultsPerPage = (options != null) ? options.getMaxResultsPerPage() : null;
        List<ListSharesIncludeType> include = new ArrayList<>();

        if (options != null) {
            if (options.isIncludeDeleted()) {
                include.add(ListSharesIncludeType.DELETED);
            }

            if (options.isIncludeMetadata()) {
                include.add(ListSharesIncludeType.METADATA);
            }

            if (options.isIncludeSnapshots()) {
                include.add(ListSharesIncludeType.SNAPSHOTS);
            }
        }

        BiFunction<String, Integer, Mono<PagedResponse<ShareItem>>> retriever =
            (nextMarker, pageSize) -> StorageImplUtils.applyOptionalTimeout(this.azureFileStorageClient.getServices()
                    .listSharesSegmentSinglePageAsync(
                        prefix, nextMarker, pageSize == null ? maxResultsPerPage : pageSize, include, null, context)
                    .onErrorMap(ModelHelper::mapToShareStorageException)
                    .map(response -> {
                        List<ShareItem> value = response.getValue() == null
                            ? Collections.emptyList()
                            : response.getValue().stream()
                            .map(ModelHelper::populateShareItem)
                            .collect(Collectors.toList());

                        return new PagedResponseBase<>(response.getRequest(),
                            response.getStatusCode(),
                            response.getHeaders(),
                            value,
                            response.getContinuationToken(),
                            ModelHelper.transformListSharesHeaders(response.getHeaders()));
                    }), timeout);
        return new PagedFlux<>(pageSize -> retriever.apply(marker, pageSize), retriever);
    }

    /**
     * Retrieves the properties of the storage account's File service. The properties range from storage analytics and
     * metrics to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve File service properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.getProperties -->
     * <pre>
     * fileServiceAsyncClient.getProperties&#40;&#41;
     *     .subscribe&#40;properties -&gt; &#123;
     *         System.out.printf&#40;&quot;Hour metrics enabled: %b, Minute metrics enabled: %b&quot;,
     *             properties.getHourMetrics&#40;&#41;.isEnabled&#40;&#41;, properties.getMinuteMetrics&#40;&#41;.isEnabled&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.getProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @return Storage account {@link ShareServiceProperties File service properties}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareServiceProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the properties of the storage account's File service. The properties range from storage analytics and
     * metrics to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve File service properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.getPropertiesWithResponse -->
     * <pre>
     * fileServiceAsyncClient.getPropertiesWithResponse&#40;&#41;
     *     .subscribe&#40;properties -&gt; System.out.printf&#40;&quot;Hour metrics enabled: %b, Minute metrics enabled: %b&quot;,
     *         properties.getValue&#40;&#41;.getHourMetrics&#40;&#41;.isEnabled&#40;&#41;,
     *         properties.getValue&#40;&#41;.getMinuteMetrics&#40;&#41;.isEnabled&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.getPropertiesWithResponse -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @return A response containing the Storage account {@link ShareServiceProperties File service properties}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareServiceProperties>> getPropertiesWithResponse() {
        try {
            return withContext(this::getPropertiesWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareServiceProperties>> getPropertiesWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getServices().getPropertiesWithResponseAsync(null, context)
            .onErrorMap(ModelHelper::mapToShareStorageException)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Sets the properties for the storage account's File service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link ShareServiceProperties#getCors()
     * CORS}. To disable all CORS in the Queue service pass an empty list for {@link ShareServiceProperties#getCors()
     * CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.setProperties#fileServiceProperties -->
     * <pre>
     * fileServiceAsyncClient.getProperties&#40;&#41;.subscribe&#40;properties -&gt; &#123;
     *     properties.getMinuteMetrics&#40;&#41;.setEnabled&#40;true&#41;;
     *     properties.getHourMetrics&#40;&#41;.setEnabled&#40;true&#41;;
     *
     *     fileServiceAsyncClient.setProperties&#40;properties&#41;
     *         .subscribe&#40;r -&gt; System.out.println&#40;&quot;Setting File service properties completed.&quot;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.setProperties#fileServiceProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account File service properties
     * @return An empty response
     * @throws ShareStorageException When one of the following is true
     * <ul>
     * <li>A CORS rule is missing one of its fields</li>
     * <li>More than five CORS rules will exist for the Queue service</li>
     * <li>Size of all CORS rules exceeds 2KB</li>
     * <li>
     * Length of {@link ShareCorsRule#getAllowedHeaders() allowed headers}, {@link ShareCorsRule#getExposedHeaders()
     * exposed headers}, or {@link ShareCorsRule#getAllowedOrigins() allowed origins} exceeds 256 characters.
     * </li>
     * <li>{@link ShareCorsRule#getAllowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or
     * PUT</li>
     * </ul>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setProperties(ShareServiceProperties properties) {
        return setPropertiesWithResponse(properties).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the properties for the storage account's File service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     *
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link ShareServiceProperties#getCors()
     * CORS}. To disable all CORS in the Queue service pass an empty list for {@link ShareServiceProperties#getCors()
     * CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Clear CORS in the File service</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.setPropertiesWithResponse#fileServiceProperties.clearCORS -->
     * <pre>
     * fileServiceAsyncClient.getProperties&#40;&#41;.subscribe&#40;properties -&gt; &#123;
     *     properties.setCors&#40;Collections.emptyList&#40;&#41;&#41;;
     *
     *     fileServiceAsyncClient.setPropertiesWithResponse&#40;properties&#41;.subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Setting File service properties completed with status code %d&quot;,
     *             response.getStatusCode&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.setPropertiesWithResponse#fileServiceProperties.clearCORS -->
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.setPropertiesWithResponseAsync#fileServiceProperties -->
     * <pre>
     * fileServiceAsyncClient.getPropertiesWithResponse&#40;&#41;.subscribe&#40;response -&gt; &#123;
     *     ShareServiceProperties properties = response.getValue&#40;&#41;;
     *     properties.getMinuteMetrics&#40;&#41;.setEnabled&#40;true&#41;;
     *     properties.getHourMetrics&#40;&#41;.setEnabled&#40;true&#41;;
     *
     *     fileServiceAsyncClient.setPropertiesWithResponse&#40;properties&#41;.subscribe&#40;r -&gt;
     *         System.out.printf&#40;&quot;Setting File service properties completed with status code %d&quot;, r.getStatusCode&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.setPropertiesWithResponseAsync#fileServiceProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account File service properties
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException When one of the following is true
     * <ul>
     * <li>A CORS rule is missing one of its fields</li>
     * <li>More than five CORS rules will exist for the Queue service</li>
     * <li>Size of all CORS rules exceeds 2KB</li>
     * <li>
     * Length of {@link ShareCorsRule#getAllowedHeaders() allowed headers}, {@link ShareCorsRule#getExposedHeaders()
     * exposed headers}, or {@link ShareCorsRule#getAllowedOrigins() allowed origins} exceeds 256 characters.
     * </li>
     * <li>{@link ShareCorsRule#getAllowedMethods() Allowed methods} isn't DELETE, GET, HEAD, MERGE, POST, OPTIONS, or
     * PUT</li>
     * </ul>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setPropertiesWithResponse(ShareServiceProperties properties) {
        try {
            return withContext(context -> setPropertiesWithResponse(properties, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> setPropertiesWithResponse(ShareServiceProperties properties, Context context) {
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getServices()
            .setPropertiesNoCustomHeadersWithResponseAsync(properties, null, context)
            .onErrorMap(ModelHelper::mapToShareStorageException);
    }

    /**
     * Creates a share in the storage account with the specified name and returns a ShareAsyncClient to interact with
     * it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.createShare#string -->
     * <pre>
     * fileServiceAsyncClient.createShare&#40;&quot;myshare&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123; &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the share!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.createShare#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @return The {@link ShareAsyncClient ShareAsyncClient}
     * @throws ShareStorageException If a share with the same name already exists
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareAsyncClient> createShare(String shareName) {
        return createShareWithResponse(shareName, (ShareCreateOptions) null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a share in the storage account with the specified name, metadata, and quota and returns a
     * ShareAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test" with metadata "share:metadata"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.createShareWithResponse#string-map-integer.metadata -->
     * <pre>
     * fileServiceAsyncClient.createShareWithResponse&#40;&quot;test&quot;, Collections.singletonMap&#40;&quot;share&quot;, &quot;metadata&quot;&#41;, null&#41;
     *     .subscribe&#40;
     *         response -&gt; System.out.printf&#40;&quot;Creating the share completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the share!&quot;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.createShareWithResponse#string-map-integer.metadata -->
     *
     * <p>Create the share "test" with a quota of 10 GB</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.createShareWithResponse#string-map-integer.quota -->
     * <pre>
     * fileServiceAsyncClient.createShareWithResponse&#40;&quot;test&quot;, null, 10&#41;
     *     .subscribe&#40;
     *         response -&gt; System.out.printf&#40;&quot;Creating the share completed with status code %d&quot;,
     *             response.getStatusCode&#40;&#41;&#41;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the share!&quot;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.createShareWithResponse#string-map-integer.quota -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * <p>For more information on updated max file share size values, see the
     * <a href="https://learn.microsoft.com/azure/storage/files/storage-files-scale-targets#azure-file-share-scale-targets">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB.
     * The default value is 5120. Refer to the Azure Docs for updated values.
     * @return A response containing the {@link ShareAsyncClient ShareAsyncClient} and the status of creating the share.
     * @throws ShareStorageException If a share with the same name already exists or {@code quotaInGB} is outside the
     * allowed range.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareAsyncClient>> createShareWithResponse(String shareName, Map<String, String> metadata,
        Integer quotaInGB) {
        return createShareWithResponse(shareName, new ShareCreateOptions().setMetadata(metadata)
            .setQuotaInGb(quotaInGB));
    }

    /**
     * Creates a share in the storage account with the specified name, and options and returns a
     * ShareAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.createShareWithResponse#String-ShareCreateOptions -->
     * <pre>
     * fileServiceAsyncClient.createShareWithResponse&#40;&quot;test&quot;, new ShareCreateOptions&#40;&#41;
     *     .setMetadata&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;metadata&quot;&#41;&#41;.setQuotaInGb&#40;1&#41;
     *     .setAccessTier&#40;ShareAccessTier.HOT&#41;&#41;.subscribe&#40;
     *         response -&gt; System.out.printf&#40;&quot;Creating the share completed with status code %d&quot;,
     *             response.getStatusCode&#40;&#41;&#41;,
     *         error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Complete creating the share!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.createShareWithResponse#String-ShareCreateOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param options {@link ShareCreateOptions}
     * @return A response containing the {@link ShareAsyncClient ShareAsyncClient} and the status of creating the share.
     * @throws ShareStorageException If a share with the same name already exists or {@code quotaInGB} is outside the
     * allowed range.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareAsyncClient>> createShareWithResponse(String shareName, ShareCreateOptions options) {
        try {
            return withContext(context -> createShareWithResponse(shareName, options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareAsyncClient>> createShareWithResponse(String shareName, ShareCreateOptions options,
        Context context) {
        ShareAsyncClient shareAsyncClient = new ShareAsyncClient(azureFileStorageClient, shareName, null,
            accountName, serviceVersion, sasToken);

        return shareAsyncClient.createWithResponse(options, context).map(response ->
            new SimpleResponse<>(response, shareAsyncClient));
    }

    /**
     * Deletes the share in the storage account with the given name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share "test"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.deleteShare#string -->
     * <pre>
     * fileServiceAsyncClient.deleteShare&#40;&quot;test&quot;&#41;.doOnSuccess&#40;
     *     response -&gt; System.out.println&#40;&quot;Deleting the share completed.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.deleteShare#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @return An empty response
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteShare(String shareName) {
        return deleteShareWithResponse(shareName, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specific snapshot of the share in the storage account with the given name. Snapshot are identified by
     * the time they were created.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the snapshot of share "test" that was created at current time.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.deleteShareWithResponse#string-string -->
     * <pre>
     * OffsetDateTime midnight = OffsetDateTime.of&#40;LocalDateTime.now&#40;&#41;, ZoneOffset.UTC&#41;;
     * fileServiceAsyncClient.deleteShareWithResponse&#40;&quot;test&quot;, midnight.toString&#40;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Deleting the snapshot completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.deleteShareWithResponse#string-string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param snapshot Identifier of the snapshot
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist or the snapshot doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteShareWithResponse(String shareName, String snapshot) {
        try {
            return withContext(context -> deleteShareWithResponse(shareName, snapshot, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteShareWithResponse(String shareName, String snapshot, Context context) {
        DeleteSnapshotsOptionType deleteSnapshots = null;
        if (CoreUtils.isNullOrEmpty(snapshot)) {
            deleteSnapshots = DeleteSnapshotsOptionType.INCLUDE;
        }
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getShares()
            .deleteNoCustomHeadersWithResponseAsync(shareName, snapshot, null, deleteSnapshots, null, context)
            .onErrorMap(ModelHelper::mapToShareStorageException);
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return this.azureFileStorageClient.getHttpPipeline();
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p>The snippet below generates a SAS that lasts for two days and gives the user read and list access to blob
     * containers and file shares.</p>
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.generateAccountSas#AccountSasSignatureValues -->
     * <pre>
     * AccountSasPermission permissions = new AccountSasPermission&#40;&#41;
     *     .setListPermission&#40;true&#41;
     *     .setReadPermission&#40;true&#41;;
     * AccountSasResourceType resourceTypes = new AccountSasResourceType&#40;&#41;.setContainer&#40;true&#41;;
     * AccountSasService services = new AccountSasService&#40;&#41;.setBlobAccess&#40;true&#41;.setFileAccess&#40;true&#41;;
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plus&#40;Duration.ofDays&#40;2&#41;&#41;;
     *
     * AccountSasSignatureValues sasValues =
     *     new AccountSasSignatureValues&#40;expiryTime, permissions, services, resourceTypes&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * String sas = fileServiceAsyncClient.generateAccountSas&#40;sasValues&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.generateAccountSas#AccountSasSignatureValues -->
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues) {
        return generateAccountSas(accountSasSignatureValues, Context.NONE);
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p>The snippet below generates a SAS that lasts for two days and gives the user read and list access to blob
     * containers and file shares.</p>
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.generateAccountSas#AccountSasSignatureValues-Context -->
     * <pre>
     * AccountSasPermission permissions = new AccountSasPermission&#40;&#41;
     *     .setListPermission&#40;true&#41;
     *     .setReadPermission&#40;true&#41;;
     * AccountSasResourceType resourceTypes = new AccountSasResourceType&#40;&#41;.setContainer&#40;true&#41;;
     * AccountSasService services = new AccountSasService&#40;&#41;.setBlobAccess&#40;true&#41;.setFileAccess&#40;true&#41;;
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plus&#40;Duration.ofDays&#40;2&#41;&#41;;
     *
     * AccountSasSignatureValues sasValues =
     *     new AccountSasSignatureValues&#40;expiryTime, permissions, services, resourceTypes&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * String sas = fileServiceAsyncClient.generateAccountSas&#40;sasValues, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.generateAccountSas#AccountSasSignatureValues-Context -->
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues, Context context) {
        return generateAccountSas(accountSasSignatureValues, null, context);
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     * @param stringToSignHandler For debugging purposes only. Returns the string to sign that was used to generate the
     * signature.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues,
        Consumer<String> stringToSignHandler, Context context) {
        return new AccountSasImplUtil(accountSasSignatureValues, null)
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), stringToSignHandler, context);
    }

    /**
     * Restores a previously deleted share.
     * <p>
     * If the share associated with provided <code>deletedShareName</code>
     * already exists, this call will result in a 409 (conflict).
     * </p>
     * <p>
     * This API is only functional if Share Soft Delete is enabled
     * for the storage account associated with the share.
     * For more information, see the
     * <a href="TBD">Azure Docs</a>.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.undeleteShare#String-String -->
     * <pre>
     * ListSharesOptions listSharesOptions = new ListSharesOptions&#40;&#41;;
     * listSharesOptions.setIncludeDeleted&#40;true&#41;;
     * fileServiceAsyncClient.listShares&#40;listSharesOptions&#41;.flatMap&#40;
     *     deletedShare -&gt; &#123;
     *         Mono&lt;ShareAsyncClient&gt; shareAsyncClient = fileServiceAsyncClient.undeleteShare&#40;
     *             deletedShare.getName&#40;&#41;, deletedShare.getVersion&#40;&#41;&#41;;
     *         return shareAsyncClient;
     *     &#125;
     * &#41;.blockFirst&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.undeleteShare#String-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/restore-share">Azure Docs</a>.</p>
     *
     * @param deletedShareName The name of the previously deleted share.
     * @param deletedShareVersion The version of the previously deleted share.
     * @return A {@link Mono} containing a {@link ShareAsyncClient} used
     * to interact with the restored share.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareAsyncClient> undeleteShare(String deletedShareName, String deletedShareVersion) {
        return this.undeleteShareWithResponse(deletedShareName, deletedShareVersion).flatMap(FluxUtil::toMono);
    }

    /**
     * Restores a previously deleted share.
     * <p>
     * If the share associated with provided <code>deletedShareName</code>
     * already exists, this call will result in a 409 (conflict).
     * </p>
     * <p>
     * This API is only functional if Share Soft Delete is enabled
     * for the storage account associated with the share.
     * For more information, see the
     * <a href="TBD">Azure Docs</a>.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceAsyncClient.undeleteShareWithResponse#String-String -->
     * <pre>
     * ListSharesOptions listSharesOptions = new ListSharesOptions&#40;&#41;;
     * listSharesOptions.setIncludeDeleted&#40;true&#41;;
     * fileServiceAsyncClient.listShares&#40;listSharesOptions&#41;.flatMap&#40;
     *     deletedShare -&gt; &#123;
     *         Mono&lt;ShareAsyncClient&gt; shareAsyncClient = fileServiceAsyncClient.undeleteShareWithResponse&#40;
     *             deletedShare.getName&#40;&#41;, deletedShare.getVersion&#40;&#41;&#41;.map&#40;Response::getValue&#41;;
     *         return shareAsyncClient;
     *     &#125;
     * &#41;.blockFirst&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceAsyncClient.undeleteShareWithResponse#String-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/restore-share">Azure Docs</a>.</p>
     *
     * @param deletedShareName The name of the previously deleted share.
     * @param deletedShareVersion The version of the previously deleted share.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * ShareAsyncClient} used to interact with the restored share.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareAsyncClient>> undeleteShareWithResponse(String deletedShareName,
        String deletedShareVersion) {
        try {
            return withContext(context -> undeleteShareWithResponse(deletedShareName, deletedShareVersion, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareAsyncClient>> undeleteShareWithResponse(
        String deletedShareName, String deletedShareVersion, Context context) {
        return this.azureFileStorageClient.getShares().restoreWithResponseAsync(
            deletedShareName, null, null, deletedShareName, deletedShareVersion, context)
            .onErrorMap(ModelHelper::mapToShareStorageException)
            .map(response -> new SimpleResponse<>(response, getShareAsyncClient(deletedShareName)));
    }
}
