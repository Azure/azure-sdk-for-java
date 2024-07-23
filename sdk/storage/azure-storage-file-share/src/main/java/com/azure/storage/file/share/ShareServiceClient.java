// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.SharedExecutorService;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.AccountSasImplUtil;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.models.DeleteSnapshotsOptionType;
import com.azure.storage.file.share.implementation.models.ListSharesIncludeType;
import com.azure.storage.file.share.implementation.models.ShareItemInternal;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.ListSharesOptions;
import com.azure.storage.file.share.models.ShareCorsRule;
import com.azure.storage.file.share.models.ShareItem;
import com.azure.storage.file.share.models.ShareServiceProperties;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareCreateOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.storage.common.implementation.StorageImplUtils.sendRequest;

/**
 * This class provides a shareServiceAsyncClient that contains all the operations for interacting with a file account in
 * Azure Storage. Operations allowed by the shareServiceAsyncClient are creating, listing, and deleting shares and
 * retrieving and updating properties of the account.
 *
 * <p><strong>Instantiating a Synchronous File Service Client</strong></p>
 *
 * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.instantiation -->
 * <pre>
 * ShareServiceClient client = new ShareServiceClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;$&#123;connectionString&#125;&quot;&#41;
 *     .endpoint&#40;&quot;$&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareServiceClient.instantiation -->
 *
 * <p>View {@link ShareServiceClientBuilder this} for additional ways to construct the shareServiceAsyncClient.</p>
 *
 * @see ShareServiceClientBuilder
 * @see ShareServiceAsyncClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareServiceClientBuilder.class)
public final class ShareServiceClient {
    private static final ClientLogger LOGGER = new ClientLogger(ShareServiceClient.class);
    private final AzureFileStorageImpl azureFileStorageClient;
    private final String accountName;
    private final ShareServiceVersion serviceVersion;
    private final AzureSasCredential sasToken;

    /**
     * Creates a ShareServiceClient.
     * @param azureFileStorage Client that interacts with the service interfaces
     * @param accountName Name of the account
     * @param serviceVersion The version of the service to be used when making requests.
     * @param sasToken The SAS token used to authenticate the request
     */
    ShareServiceClient(AzureFileStorageImpl azureFileStorage, String accountName, ShareServiceVersion serviceVersion,
        AzureSasCredential sasToken) {
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
     * Constructs a ShareClient that interacts with the specified share.
     *
     * <p>If the share doesn't exist in the storage account {@link ShareClient#create() create} in the
     * shareServiceAsyncClient will
     * need to be called before interaction with the share can happen.</p>
     *
     * @param shareName Name of the share
     * @return a ShareClient that interacts with the specified share
     */
    public ShareClient getShareClient(String shareName) {
        return new ShareClient(azureFileStorageClient, shareName, null, accountName, serviceVersion, sasToken);
    }

    /**
     * Lists all shares in the storage account without their metadata or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all shares in the account</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.listShares -->
     * <pre>
     * fileServiceClient.listShares&#40;&#41;.forEach&#40;
     *     shareItem -&gt; System.out.printf&#40;&quot;Share %s exists in the account&quot;, shareItem.getName&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.listShares -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
     *
     * @return {@link ShareItem Shares} in the storage account without their metadata or snapshots
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareItem> listShares() {
        return listShares(null, null, null);
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
     * <!-- src_embed ShareServiceClient.listShares#ListSharesOptions-Duration-Context1 -->
     * <pre>
     * fileServiceClient.listShares&#40;new ListSharesOptions&#40;&#41;.setPrefix&#40;&quot;azure&quot;&#41;, Duration.ofSeconds&#40;1&#41;,
     *     new Context&#40;key1, value1&#41;&#41;.forEach&#40;
     *         shareItem -&gt; System.out.printf&#40;&quot;Share %s exists in the account&quot;, shareItem.getName&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end ShareServiceClient.listShares#ListSharesOptions-Duration-Context1 -->
     *
     * <p>List all shares including their snapshots and metadata</p>
     *
     * <!-- src_embed ShareServiceClient.listShares#ListSharesOptions-Duration-Context2 -->
     * <pre>
     * fileServiceClient.listShares&#40;new ListSharesOptions&#40;&#41;.setIncludeMetadata&#40;true&#41;
     *     .setIncludeSnapshots&#40;true&#41;, Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;.forEach&#40;
     *         shareItem -&gt; System.out.printf&#40;&quot;Share %s exists in the account&quot;, shareItem.getName&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end ShareServiceClient.listShares#ListSharesOptions-Duration-Context2 -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-shares">Azure Docs</a>.</p>
     *
     * @param options Options for listing shares. If iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over the value set on these options.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link ShareItem Shares} in the storage account that satisfy the filter requirements
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareItem> listShares(ListSharesOptions options, Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
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

        BiFunction<String, Integer, PagedResponse<ShareItem>> retriever =
            (nextMarker, pageSize) -> {
                Supplier<PagedResponse<ShareItemInternal>> operation = () -> this.azureFileStorageClient.getServices()
                    .listSharesSegmentNoCustomHeadersSinglePage(prefix, nextMarker,
                        pageSize == null ? maxResultsPerPage : pageSize, include, null, finalContext);

                try {
                    PagedResponse<ShareItemInternal> response = timeout != null
                        ? CoreUtils.getResultWithTimeout(SharedExecutorService.getInstance().submit(operation::get), timeout)
                        : operation.get();

                    List<ShareItem> value = response.getValue() == null ? Collections.emptyList()
                        : response.getValue().stream().map(ModelHelper::populateShareItem).collect(Collectors.toList());

                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), value, response.getContinuationToken(),
                        ModelHelper.transformListSharesHeaders(response.getHeaders()));
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    throw LOGGER.logExceptionAsError(new RuntimeException("Failed to retrieve shares with timeout.", e));
                }
            };

        return new PagedIterable<>(pageSize -> retriever.apply(null, pageSize), retriever);
    }

    /**
     * Retrieves the properties of the storage account's File service. The properties range from storage analytics and
     * metrics to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve File service properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.getProperties -->
     * <pre>
     * ShareServiceProperties properties = fileServiceClient.getProperties&#40;&#41;;
     * System.out.printf&#40;&quot;Hour metrics enabled: %b, Minute metrics enabled: %b&quot;, properties.getHourMetrics&#40;&#41;.isEnabled&#40;&#41;,
     *     properties.getMinuteMetrics&#40;&#41;.isEnabled&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.getProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @return Storage account {@link ShareServiceProperties File service properties}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareServiceProperties getProperties() {
        return getPropertiesWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves the properties of the storage account's File service. The properties range from storage analytics and
     * metrics to CORS (Cross-Origin Resource Sharing).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve File service properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.getPropertiesWithResponse#duration-context -->
     * <pre>
     * ShareServiceProperties properties = fileServiceClient.getPropertiesWithResponse&#40;
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Hour metrics enabled: %b, Minute metrics enabled: %b&quot;, properties.getHourMetrics&#40;&#41;.isEnabled&#40;&#41;,
     *     properties.getMinuteMetrics&#40;&#41;.isEnabled&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.getPropertiesWithResponse#duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the Storage account {@link ShareServiceProperties File service properties} with
     * headers and response status code
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareServiceProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<Response<ShareServiceProperties>> operation = () -> this.azureFileStorageClient.getServices()
            .getPropertiesNoCustomHeadersWithResponse(null, finalContext);

        Response<ShareServiceProperties> response = sendRequest(operation, timeout, ShareStorageException.class);
        return new SimpleResponse<>(response, response.getValue());
    }

    /**
     * Sets the properties for the storage account's File service. The properties range from storage analytics and
     * metric to CORS (Cross-Origin Resource Sharing).
     * <p>
     * To maintain the CORS in the Queue service pass a {@code null} value for {@link ShareServiceProperties#getCors()
     * CORS}. To disable all CORS in the Queue service pass an empty list for {@link ShareServiceProperties#getCors()
     * CORS}.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <p>Clear CORS in the File service</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.setPropertiesWithResponse#fileServiceProperties-Context.clearCORS -->
     * <pre>
     * ShareServiceProperties properties = fileServiceClient.getProperties&#40;&#41;;
     * properties.setCors&#40;Collections.emptyList&#40;&#41;&#41;;
     *
     * Response&lt;Void&gt; response = fileServiceClient.setPropertiesWithResponse&#40;properties,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Setting File service properties completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.setPropertiesWithResponse#fileServiceProperties-Context.clearCORS -->
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.setProperties#fileServiceProperties -->
     * <pre>
     * ShareServiceProperties properties = fileServiceClient.getProperties&#40;&#41;;
     *
     * properties.getMinuteMetrics&#40;&#41;.setEnabled&#40;true&#41;;
     * properties.getHourMetrics&#40;&#41;.setEnabled&#40;true&#41;;
     *
     * fileServiceClient.setProperties&#40;properties&#41;;
     * System.out.println&#40;&quot;Setting File service properties completed.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.setProperties#fileServiceProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account File service properties
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
    public void setProperties(ShareServiceProperties properties) {
        setPropertiesWithResponse(properties, null, Context.NONE);
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
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.setPropertiesWithResponse#fileServiceProperties-Context.clearCORS -->
     * <pre>
     * ShareServiceProperties properties = fileServiceClient.getProperties&#40;&#41;;
     * properties.setCors&#40;Collections.emptyList&#40;&#41;&#41;;
     *
     * Response&lt;Void&gt; response = fileServiceClient.setPropertiesWithResponse&#40;properties,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Setting File service properties completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.setPropertiesWithResponse#fileServiceProperties-Context.clearCORS -->
     *
     * <p>Enable Minute and Hour Metrics</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.setPropertiesWithResponse#fileServiceProperties-Context -->
     * <pre>
     * ShareServiceProperties properties = fileServiceClient.getPropertiesWithResponse&#40;
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;;
     *
     * properties.getMinuteMetrics&#40;&#41;.setEnabled&#40;true&#41;;
     * properties.getHourMetrics&#40;&#41;.setEnabled&#40;true&#41;;
     *
     * Response&lt;Void&gt; response = fileServiceClient.setPropertiesWithResponse&#40;properties,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Setting File service properties completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.setPropertiesWithResponse#fileServiceProperties-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-service-properties">Azure
     * Docs</a>.</p>
     *
     * @param properties Storage account File service properties
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
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
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setPropertiesWithResponse(ShareServiceProperties properties, Duration timeout,
        Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<Response<Void>> operation = () -> this.azureFileStorageClient.getServices()
            .setPropertiesNoCustomHeadersWithResponse(properties, null, finalContext);

        return sendRequest(operation, timeout, ShareStorageException.class);
    }

    /**
     * Creates a share in the storage account with the specified name and returns a ShareClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share with share name of "myshare"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.createShare#string -->
     * <pre>
     * fileServiceClient.createShare&#40;&quot;myshare&quot;&#41;;
     * System.out.println&#40;&quot;Creating the share completed.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.createShare#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @return The {@link ShareClient ShareClient}
     * @throws ShareStorageException If a share with the same name already exists
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareClient createShare(String shareName) {
        return createShareWithResponse(shareName, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a share in the storage account with the specified name and metadata and returns a ShareClient to interact
     * with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the share "test" with a quota of 10 GB</p>
     *
     * <!-- src_embed ShareServiceClient.createShareWithResponse#string-map-integer-duration-context -->
     * <pre>
     * Response&lt;ShareClient&gt; response = fileServiceClient.createShareWithResponse&#40;&quot;test&quot;,
     *     Collections.singletonMap&#40;&quot;share&quot;, &quot;metadata&quot;&#41;, null, Duration.ofSeconds&#40;5&#41;,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Creating the share completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end ShareServiceClient.createShareWithResponse#string-map-integer-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * <p>For more information on updated max file share size values, see the
     * <a href="https://learn.microsoft.com/azure/storage/files/storage-files-scale-targets#azure-file-share-scale-targets">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param metadata Optional metadata to associate with the share
     * @param quotaInGB Optional maximum size the share is allowed to grow to in GB. The default value is 5120.
     * Refer to the Azure Docs for updated values.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareClient ShareClient} and the status of creating the share.
     * @throws ShareStorageException If a share with the same name already exists or {@code quotaInGB} is outside the
     * allowed range.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareClient> createShareWithResponse(String shareName, Map<String, String> metadata,
        Integer quotaInGB, Duration timeout, Context context) {
        ShareClient shareClient = getShareClient(shareName);
        return new SimpleResponse<>(shareClient.createWithResponse(metadata, quotaInGB, timeout, context), shareClient);
    }

    /**
     * Creates a share in the storage account with the specified name and options and returns a ShareClient to interact
     * with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed ShareServiceClient.createShareWithResponse#String-ShareCreateOptions-Duration-Context -->
     * <pre>
     * Response&lt;ShareClient&gt; response = fileServiceClient.createShareWithResponse&#40;&quot;test&quot;,
     *     new ShareCreateOptions&#40;&#41;.setMetadata&#40;Collections.singletonMap&#40;&quot;share&quot;, &quot;metadata&quot;&#41;&#41;.setQuotaInGb&#40;1&#41;
     *     .setAccessTier&#40;ShareAccessTier.HOT&#41;, Duration.ofSeconds&#40;5&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Creating the share completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end ShareServiceClient.createShareWithResponse#String-ShareCreateOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param options {@link ShareCreateOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the {@link ShareClient ShareClient} and the status of creating the share.
     * @throws ShareStorageException If a share with the same name already exists or {@code quotaInGB} is outside the
     * allowed range.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareClient> createShareWithResponse(String shareName, ShareCreateOptions options,
        Duration timeout, Context context) {
        ShareClient shareClient = getShareClient(shareName);
        return new SimpleResponse<>(shareClient.createWithResponse(options, timeout, context), shareClient);
    }

    /**
     * Deletes the share in the storage account with the given name
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the share "test"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.deleteShare#string -->
     * <pre>
     * fileServiceClient.deleteShare&#40;&quot;myshare&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.deleteShare#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteShare(String shareName) {
        deleteShareWithResponse(shareName, null, null, Context.NONE);
    }

    /**
     * Deletes the specific snapshot of the share in the storage account with the given name. Snapshot are identified by
     * the time they were created.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the snapshot of share "test" that was created at current time. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.deleteShareWithResponse#string-string-duration-context -->
     * <pre>
     * OffsetDateTime midnight = OffsetDateTime.of&#40;LocalDateTime.now&#40;&#41;, ZoneOffset.UTC&#41;;
     * Response&lt;Void&gt; response = fileServiceClient.deleteShareWithResponse&#40;&quot;test&quot;, midnight.toString&#40;&#41;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Deleting the snapshot completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.deleteShareWithResponse#string-string-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-share">Azure Docs</a>.</p>
     *
     * @param shareName Name of the share
     * @param snapshot Identifier of the snapshot
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist or the snapshot doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteShareWithResponse(String shareName, String snapshot, Duration timeout,
        Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        DeleteSnapshotsOptionType deleteSnapshots = CoreUtils.isNullOrEmpty(snapshot)
            ? DeleteSnapshotsOptionType.INCLUDE : null;
        Callable<Response<Void>> operation = () -> this.azureFileStorageClient.getShares()
            .deleteNoCustomHeadersWithResponse(shareName, snapshot, null, deleteSnapshots, null, finalContext);

        return sendRequest(operation, timeout, ShareStorageException.class);
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
     * <p><strong>Generating an account SAS</strong></p>
     * <p>The snippet below generates an AccountSasSignatureValues object that lasts for two days and gives the user
     * read and list access to blob  and file shares.</p>
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.generateAccountSas#AccountSasSignatureValues -->
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
     * String sas = fileServiceClient.generateAccountSas&#40;sasValues&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.generateAccountSas#AccountSasSignatureValues -->
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues) {
        return this.generateAccountSas(accountSasSignatureValues, Context.NONE);
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p>The snippet below generates a SAS that lasts for two days and gives the user read and list access to blob
     * containers and file shares.</p>
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.generateAccountSas#AccountSasSignatureValues-Context -->
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
     * String sas = fileServiceClient.generateAccountSas&#40;sasValues, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.generateAccountSas#AccountSasSignatureValues-Context -->
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues, Context context) {
        return new AccountSasImplUtil(accountSasSignatureValues, null)
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }

    /**
     * For debugging purposes only.
     * Returns the string to sign that will be used to generate the signature for the SAS URL.

     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return The string to sign that will be used to generate the signature for the SAS URL.
     */
    @Deprecated
    public String generateAccountSasStringToSign(AccountSasSignatureValues accountSasSignatureValues, Context context) {
        return new AccountSasImplUtil(accountSasSignatureValues, null)
            .generateSasStringToSign(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
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
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.undeleteShare#String-String -->
     * <pre>
     * ListSharesOptions listSharesOptions = new ListSharesOptions&#40;&#41;;
     * listSharesOptions.setIncludeDeleted&#40;true&#41;;
     * fileServiceClient.listShares&#40;listSharesOptions, Duration.ofSeconds&#40;1&#41;, context&#41;.forEach&#40;
     *     deletedShare -&gt; &#123;
     *         ShareClient shareClient = fileServiceClient.undeleteShare&#40;
     *             deletedShare.getName&#40;&#41;, deletedShare.getVersion&#40;&#41;&#41;;
     *     &#125;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.undeleteShare#String-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/restore-share">Azure Docs</a>.</p>
     *
     * @param deletedShareName The name of the previously deleted share.
     * @param deletedShareVersion The version of the previously deleted share.
     * @return A {@link ShareClient} used
     * to interact with the restored share.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareClient undeleteShare(String deletedShareName, String deletedShareVersion) {
        return this.undeleteShareWithResponse(deletedShareName, deletedShareVersion, null, Context.NONE)
            .getValue();
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
     * <!-- src_embed com.azure.storage.file.share.ShareServiceClient.undeleteShareWithResponse#String-String-Duration-Context -->
     * <pre>
     * ListSharesOptions listSharesOptions = new ListSharesOptions&#40;&#41;;
     * listSharesOptions.setIncludeDeleted&#40;true&#41;;
     * fileServiceClient.listShares&#40;listSharesOptions, Duration.ofSeconds&#40;1&#41;, context&#41;.forEach&#40;
     *     deletedShare -&gt; &#123;
     *         ShareClient shareClient = fileServiceClient.undeleteShareWithResponse&#40;
     *             deletedShare.getName&#40;&#41;, deletedShare.getVersion&#40;&#41;, Duration.ofSeconds&#40;1&#41;, context&#41;.getValue&#40;&#41;;
     *     &#125;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareServiceClient.undeleteShareWithResponse#String-String-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/restore-share">Azure Docs</a>.</p>
     *
     * @param deletedShareName The name of the previously deleted share.
     * @param deletedShareVersion The version of the previously deleted share.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link ShareClient} used
     * to interact with the restored share.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareClient> undeleteShareWithResponse(String deletedShareName, String deletedShareVersion,
        Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<Response<Void>> operation = () -> this.azureFileStorageClient.getShares()
            .restoreNoCustomHeadersWithResponse(deletedShareName, null, null, deletedShareName, deletedShareVersion,
                finalContext);

        return new SimpleResponse<>(sendRequest(operation, timeout, ShareStorageException.class),
            getShareClient(deletedShareName));
    }
}
