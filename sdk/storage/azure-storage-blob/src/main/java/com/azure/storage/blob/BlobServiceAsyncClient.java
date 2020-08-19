// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.credential.TokenCredential;
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
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.models.TaggedBlobItem;
import com.azure.storage.blob.implementation.models.ServiceGetAccountInfoHeaders;
import com.azure.storage.blob.implementation.models.ServicesListBlobContainersSegmentResponse;
import com.azure.storage.blob.models.BlobContainerEncryptionScope;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobContainerListDetails;
import com.azure.storage.blob.models.BlobCorsRule;
import com.azure.storage.blob.models.BlobRetentionPolicy;
import com.azure.storage.blob.models.BlobServiceProperties;
import com.azure.storage.blob.models.BlobServiceStatistics;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.options.FindBlobsOptions;
import com.azure.storage.blob.models.KeyInfo;
import com.azure.storage.blob.models.ListBlobContainersIncludeType;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.AccountSasImplUtil;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;

/**
 * Client to a storage account. It may only be instantiated through a {@link BlobServiceClientBuilder}. This class does
 * not hold any state about a particular storage account but is instead a convenient way of sending off appropriate
 * requests to the resource on the service. It may also be used to construct URLs to blobs and containers.
 *
 * <p>
 * This client contains operations on a blob. Operations on a container are available on {@link
 * BlobContainerAsyncClient} through {@link #getBlobContainerAsyncClient(String)}, and operations on a blob are
 * available on {@link BlobAsyncClient}.
 *
 * <p>
 * Please see <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>here</a> for more
 * information on containers.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core project
 * (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong> start the actual network
 * operation, until {@code .subscribe()} is called on the reactive response. You can simply convert one of these
 * responses to a {@link java.util.concurrent.CompletableFuture} object through {@link Mono#toFuture()}.
 */
@ServiceClient(builder = BlobServiceClientBuilder.class, isAsync = true)
public final class BlobServiceAsyncClient {
    private final ClientLogger logger = new ClientLogger(BlobServiceAsyncClient.class);

    private final AzureBlobStorageImpl azureBlobStorage;

    private final String accountName;
    private final BlobServiceVersion serviceVersion;
    private final CpkInfo customerProvidedKey; // only used to pass down to blob clients
    private final EncryptionScope encryptionScope; // only used to pass down to blob clients
    private final BlobContainerEncryptionScope blobContainerEncryptionScope; // only used to pass down to container
    // clients
    private final boolean anonymousAccess;

    /**
     * Package-private constructor for use by {@link BlobServiceClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param customerProvidedKey Customer provided key used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     * @param encryptionScope Encryption scope used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     * @param anonymousAccess Whether or not the client was built with anonymousAccess
     */
    BlobServiceAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName,
        CpkInfo customerProvidedKey, EncryptionScope encryptionScope,
        BlobContainerEncryptionScope blobContainerEncryptionScope, boolean anonymousAccess) {
        /* Check to make sure the uri is valid. We don't want the error to occur later in the generated layer
           when the sas token has already been applied. */
        try {
            URI.create(url);
        } catch (IllegalArgumentException ex) {
            throw logger.logExceptionAsError(ex);
        }
        this.azureBlobStorage = new AzureBlobStorageBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion.getVersion())
            .build();
        this.serviceVersion = serviceVersion;

        this.accountName = accountName;
        this.customerProvidedKey = customerProvidedKey;
        this.encryptionScope = encryptionScope;
        this.blobContainerEncryptionScope = blobContainerEncryptionScope;
        this.anonymousAccess = anonymousAccess;
    }

    /**
     * Initializes a {@link BlobContainerAsyncClient} object pointing to the specified container. This method does not
     * create a container. It simply constructs the URL to the container and offers access to methods relevant to
     * containers.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getBlobContainerAsyncClient#String}
     *
     * @param containerName The name of the container to point to. A value of null or empty string will be interpreted
     *                      as pointing to the root container and will be replaced by "$root".
     * @return A {@link BlobContainerAsyncClient} object pointing to the specified container
     */
    public BlobContainerAsyncClient getBlobContainerAsyncClient(String containerName) {
        if (CoreUtils.isNullOrEmpty(containerName)) {
            containerName = BlobContainerAsyncClient.ROOT_CONTAINER_NAME;
        }

        return new BlobContainerAsyncClient(getHttpPipeline(),
            StorageImplUtils.appendToUrlPath(getAccountUrl(), containerName).toString(), getServiceVersion(),
            getAccountName(), containerName, customerProvidedKey, encryptionScope, blobContainerEncryptionScope);
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return azureBlobStorage.getHttpPipeline();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public BlobServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainer#String}
     *
     * @param containerName Name of the container to create
     * @return A {@link Mono} containing a {@link BlobContainerAsyncClient} used to interact with the container created.
     */
    public Mono<BlobContainerAsyncClient> createBlobContainer(String containerName) {
        try {
            return createBlobContainerWithResponse(containerName, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainerWithResponse#String-Map-PublicAccessType}
     *
     * @param containerName Name of the container to create
     * @param metadata Metadata to associate with the container
     * @param accessType Specifies how the data in this container is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * BlobContainerAsyncClient} used to interact with the container created.
     */
    public Mono<Response<BlobContainerAsyncClient>> createBlobContainerWithResponse(String containerName,
        Map<String, String> metadata, PublicAccessType accessType) {
        try {
            return withContext(context -> createBlobContainerWithResponse(containerName, metadata, accessType,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlobContainerAsyncClient>> createBlobContainerWithResponse(String containerName,
        Map<String, String> metadata, PublicAccessType accessType, Context context) {
        throwOnAnonymousAccess();
        BlobContainerAsyncClient blobContainerAsyncClient = getBlobContainerAsyncClient(containerName);

        return blobContainerAsyncClient.createWithResponse(metadata, accessType, context)
            .map(response -> new SimpleResponse<>(response, blobContainerAsyncClient));
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainer#String}
     *
     * @param containerName Name of the container to delete
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Void> deleteBlobContainer(String containerName) {
        try {
            return deleteBlobContainerWithResponse(containerName).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes the specified container in the storage account. If the container doesn't exist the operation fails. For
     * more information see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainerWithResponse#String-Context}
     *
     * @param containerName Name of the container to delete
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    public Mono<Response<Void>> deleteBlobContainerWithResponse(String containerName) {
        try {
            return withContext(context -> deleteBlobContainerWithResponse(containerName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteBlobContainerWithResponse(String containerName, Context context) {
        throwOnAnonymousAccess();
        return getBlobContainerAsyncClient(containerName).deleteWithResponse(null, context);
    }

    /**
     * Gets the URL of the storage account represented by this client.
     *
     * @return the URL.
     */
    public String getAccountUrl() {
        return azureBlobStorage.getUrl();
    }

    /**
     * Returns a reactive Publisher emitting all the containers in this account lazily as needed. For more information,
     * see the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.listBlobContainers}
     *
     * @return A reactive response emitting the list of containers.
     */
    public PagedFlux<BlobContainerItem> listBlobContainers() {
        try {
            return this.listBlobContainers(new ListBlobContainersOptions());
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Returns a reactive Publisher emitting all the containers in this account lazily as needed. For more information,
     * see the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.listBlobContainers#ListBlobContainersOptions}
     *
     * @param options A {@link ListBlobContainersOptions} which specifies what data should be returned by the service.
     * @return A reactive response emitting the list of containers.
     */
    public PagedFlux<BlobContainerItem> listBlobContainers(ListBlobContainersOptions options) {
        try {
            return listBlobContainersWithOptionalTimeout(options, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<BlobContainerItem> listBlobContainersWithOptionalTimeout(ListBlobContainersOptions options,
        Duration timeout) {
        throwOnAnonymousAccess();
        Function<String, Mono<PagedResponse<BlobContainerItem>>> func =
            marker -> listBlobContainersSegment(marker, options, timeout)
                .map(response -> new PagedResponseBase<>(
                    response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    response.getValue().getBlobContainerItems(),
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> func.apply(null), func);
    }

    private Mono<ServicesListBlobContainersSegmentResponse> listBlobContainersSegment(String marker,
        ListBlobContainersOptions options, Duration timeout) {
        options = options == null ? new ListBlobContainersOptions() : options;

        return StorageImplUtils.applyOptionalTimeout(
            this.azureBlobStorage.services().listBlobContainersSegmentWithRestResponseAsync(
                options.getPrefix(), marker, options.getMaxResultsPerPage(),
                toIncludeTypes(options.getDetails()),
                null, null, Context.NONE), timeout);
    }

    /**
     * Returns a reactive Publisher emitting the blobs in this account whose tags match the query expression. For more
     * information, including information on the query syntax, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/find-blobs-by-tags">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.findBlobsByTag#String}
     *
     * @param query Filters the results to return only blobs whose tags match the specified expression.
     * @return A reactive response emitting the list of blobs.
     */
    public PagedFlux<TaggedBlobItem> findBlobsByTags(String query) {
        return this.findBlobsByTags(new FindBlobsOptions(query));
    }

    /**
     * Returns a reactive Publisher emitting the blobs in this account whose tags match the query expression. For more
     * information, including information on the query syntax, see the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/find-blobs-by-tags">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobAsyncServiceClient.findBlobsByTag#FindBlobsOptions}
     *
     * @param options {@link FindBlobsOptions}
     * @return A reactive response emitting the list of blobs.
     */
    public PagedFlux<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options) {
        try {
            return findBlobsByTags(options, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options, Duration timeout) {
        throwOnAnonymousAccess();
        return new PagedFlux<>(
            () -> withContext(context -> this.findBlobsByTags(options, null, timeout, context)),
            marker -> withContext(context -> this.findBlobsByTags(options, marker, timeout, context)));
    }

    PagedFlux<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options, Duration timeout, Context context) {
        throwOnAnonymousAccess();
        return new PagedFlux<>(
            () -> this.findBlobsByTags(options, null, timeout, context),
            marker -> this.findBlobsByTags(options, marker, timeout, context));
    }

    private Mono<PagedResponse<TaggedBlobItem>> findBlobsByTags(
        FindBlobsOptions options, String marker,
        Duration timeout, Context context) {
        throwOnAnonymousAccess();
        StorageImplUtils.assertNotNull("options", options);
        return StorageImplUtils.applyOptionalTimeout(
            this.azureBlobStorage.services().filterBlobsWithRestResponseAsync(null, null,
                options.getQuery(), marker, options.getMaxResultsPerPage(),
                context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE)), timeout)
            .map(response -> {
                List<TaggedBlobItem> value = response.getValue().getBlobs() == null
                    ? Collections.emptyList()
                    : response.getValue().getBlobs().stream()
                    .map(filterBlobItem -> new TaggedBlobItem(filterBlobItem.getContainerName(),
                        filterBlobItem.getName()))
                    .collect(Collectors.toList());

                return new PagedResponseBase<>(
                    response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    value,
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders());
            });
    }

    /**
     * Converts {@link BlobContainerListDetails} into list of {@link ListBlobContainersIncludeType}
     * that contains only options selected. If no option is selected then null is returned.
     *
     * @return a list of selected options converted into {@link ListBlobContainersIncludeType}, null if none
     * of options has been selected.
     */
    private List<ListBlobContainersIncludeType> toIncludeTypes(BlobContainerListDetails blobContainerListDetails) {
        boolean hasDetails = blobContainerListDetails != null
            && blobContainerListDetails.getRetrieveMetadata();
        // Add back for container soft delete.
//        boolean hasDetails = blobContainerListDetails != null
//            && (blobContainerListDetails.getRetrieveMetadata() || blobContainerListDetails.getRetrieveDeleted());
        if (hasDetails) {
            List<ListBlobContainersIncludeType> flags = new ArrayList<>(2);
//            if (blobContainerListDetails.getRetrieveDeleted()) {
//                flags.add(ListBlobContainersIncludeType.DELETED);
//            }
            if (blobContainerListDetails.getRetrieveMetadata()) {
                flags.add(ListBlobContainersIncludeType.METADATA);
            }
            return flags;
        } else {
            return null;
        }
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getProperties}
     *
     * @return A reactive response containing the storage account properties.
     */
    public Mono<BlobServiceProperties> getProperties() {
        try {
            return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getPropertiesWithResponse}
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the storage
     * account properties.
     */
    public Mono<Response<BlobServiceProperties>> getPropertiesWithResponse() {
        try {
            return withContext(this::getPropertiesWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlobServiceProperties>> getPropertiesWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        throwOnAnonymousAccess();
        return this.azureBlobStorage.services().getPropertiesWithRestResponseAsync(null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> new SimpleResponse<>(rb, rb.getValue()));
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     * <p>This method checks to ensure the properties being sent follow the specifications indicated in the Azure Docs.
     * If CORS policies are set, CORS parameters that are not set default to the empty string.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.setProperties#BlobServiceProperties}
     *
     * @param properties Configures the service.
     * @return A {@link Mono} containing the storage account properties.
     */
    public Mono<Void> setProperties(BlobServiceProperties properties) {
        try {
            return setPropertiesWithResponse(properties).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     * <p>This method checks to ensure the properties being sent follow the specifications indicated in the Azure Docs.
     * If CORS policies are set, CORS parameters that are not set default to the empty string.</p>
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.setPropertiesWithResponse#BlobServiceProperties}
     *
     * @param properties Configures the service.
     * @return A {@link Mono} containing the storage account properties.
     */
    public Mono<Response<Void>> setPropertiesWithResponse(BlobServiceProperties properties) {
        try {
            return withContext(context -> setPropertiesWithResponse(properties, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> setPropertiesWithResponse(BlobServiceProperties properties, Context context) {
        throwOnAnonymousAccess();
        BlobServiceProperties finalProperties = null;
        if (properties != null) {
            finalProperties = new BlobServiceProperties();

            // Logging
            finalProperties.setLogging(properties.getLogging());
            if (finalProperties.getLogging() != null) {
                StorageImplUtils.assertNotNull("Logging Version", finalProperties.getLogging().getVersion());
                validateRetentionPolicy(finalProperties.getLogging().getRetentionPolicy(), "Logging Retention Policy");
            }

            // Hour Metrics
            finalProperties.setHourMetrics(properties.getHourMetrics());
            if (finalProperties.getHourMetrics() != null) {
                StorageImplUtils.assertNotNull("HourMetrics Version", finalProperties.getHourMetrics().getVersion());
                validateRetentionPolicy(finalProperties.getHourMetrics().getRetentionPolicy(), "HourMetrics Retention "
                    + "Policy");
                if (finalProperties.getHourMetrics().isEnabled()) {
                    StorageImplUtils.assertNotNull("HourMetrics IncludeApis",
                        finalProperties.getHourMetrics().isIncludeApis());
                }
            }

            // Minute Metrics
            finalProperties.setMinuteMetrics(properties.getMinuteMetrics());
            if (finalProperties.getMinuteMetrics() != null) {
                StorageImplUtils.assertNotNull("MinuteMetrics Version",
                    finalProperties.getMinuteMetrics().getVersion());
                validateRetentionPolicy(finalProperties.getMinuteMetrics().getRetentionPolicy(), "MinuteMetrics "
                    + "Retention Policy");
                if (finalProperties.getMinuteMetrics().isEnabled()) {
                    StorageImplUtils.assertNotNull("MinuteMetrics IncludeApis",
                        finalProperties.getHourMetrics().isIncludeApis());
                }
            }

            // CORS
            if (properties.getCors() != null) {
                List<BlobCorsRule> corsRules = new ArrayList<>();
                for (BlobCorsRule rule : properties.getCors()) {
                    corsRules.add(validatedCorsRule(rule));
                }
                finalProperties.setCors(corsRules);
            }

            // Default Service Version
            finalProperties.setDefaultServiceVersion(properties.getDefaultServiceVersion());

            // Delete Retention Policy
            finalProperties.setDeleteRetentionPolicy(properties.getDeleteRetentionPolicy());
            validateRetentionPolicy(finalProperties.getDeleteRetentionPolicy(), "DeleteRetentionPolicy Days");

            // Static Website
            finalProperties.setStaticWebsite(properties.getStaticWebsite());

        }
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.services().setPropertiesWithRestResponseAsync(finalProperties, null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Sets any null fields to "" since the service requires all Cors rules to be set if some are set.
     * @param originalRule {@link BlobCorsRule}
     * @return The validated {@link BlobCorsRule}
     */
    private BlobCorsRule validatedCorsRule(BlobCorsRule originalRule) {
        if (originalRule == null) {
            return null;
        }
        BlobCorsRule validRule = new BlobCorsRule();
        validRule.setAllowedHeaders(StorageImplUtils.emptyIfNull(originalRule.getAllowedHeaders()));
        validRule.setAllowedMethods(StorageImplUtils.emptyIfNull(originalRule.getAllowedMethods()));
        validRule.setAllowedOrigins(StorageImplUtils.emptyIfNull(originalRule.getAllowedOrigins()));
        validRule.setExposedHeaders(StorageImplUtils.emptyIfNull(originalRule.getExposedHeaders()));
        validRule.setMaxAgeInSeconds(originalRule.getMaxAgeInSeconds());
        return validRule;
    }

    /**
     * Validates a {@link BlobRetentionPolicy} according to service specs for set properties.
     * @param retentionPolicy {@link BlobRetentionPolicy}
     * @param policyName The name of the variable for errors.
     */
    private void validateRetentionPolicy(BlobRetentionPolicy retentionPolicy, String policyName) {
        if (retentionPolicy == null) {
            return;
        }
        if (retentionPolicy.isEnabled()) {
            StorageImplUtils.assertInBounds(policyName, retentionPolicy.getDays(), 1, 365);
        }
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime}
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return A {@link Mono} containing the user delegation key.
     * @throws IllegalArgumentException If {@code start} isn't null and is after {@code expiry}.
     * @throws NullPointerException If {@code expiry} is null.
     */
    public Mono<UserDelegationKey> getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry) {
        try {
            return withContext(context -> getUserDelegationKeyWithResponse(start, expiry, context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a user delegation key for use with this account's blob storage. Note: This method call is only valid when
     * using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime}
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} containing the user
     * delegation key.
     * @throws IllegalArgumentException If {@code start} isn't null and is after {@code expiry}.
     * @throws NullPointerException If {@code expiry} is null.
     */
    public Mono<Response<UserDelegationKey>> getUserDelegationKeyWithResponse(OffsetDateTime start,
        OffsetDateTime expiry) {
        try {
            return withContext(context -> getUserDelegationKeyWithResponse(start, expiry, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<UserDelegationKey>> getUserDelegationKeyWithResponse(OffsetDateTime start, OffsetDateTime expiry,
        Context context) {
        StorageImplUtils.assertNotNull("expiry", expiry);
        if (start != null && !start.isBefore(expiry)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("`start` must be null or a datetime before `expiry`."));
        }
        throwOnAnonymousAccess();
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.services().getUserDelegationKeyWithRestResponseAsync(
                new KeyInfo()
                    .setStart(start == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(start))
                    .setExpiry(Constants.ISO_8601_UTC_DATE_FORMATTER.format(expiry)),
                null, null, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> new SimpleResponse<>(rb, rb.getValue()));
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getStatistics}
     *
     * @return A {@link Mono} containing the storage account statistics.
     */
    public Mono<BlobServiceStatistics> getStatistics() {
        try {
            return getStatisticsWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the storage account. For more information, see
     * the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getStatisticsWithResponse}
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} containing the
     * storage account statistics.
     */
    public Mono<Response<BlobServiceStatistics>> getStatisticsWithResponse() {
        try {
            return withContext(this::getStatisticsWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlobServiceStatistics>> getStatisticsWithResponse(Context context) {
        throwOnAnonymousAccess();
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.services().getStatisticsWithRestResponseAsync(null, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> new SimpleResponse<>(rb, rb.getValue()));
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getAccountInfo}
     *
     * @return A {@link Mono} containing containing the storage account info.
     */
    public Mono<StorageAccountInfo> getAccountInfo() {
        try {
            return getAccountInfoWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the sku name and account kind for the account. For more information, please see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-account-information">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.getAccountInfoWithResponse}
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} the storage account
     * info.
     */
    public Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse() {
        try {
            return withContext(this::getAccountInfoWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse(Context context) {
        throwOnAnonymousAccess();
        return this.azureBlobStorage.services().getAccountInfoWithRestResponseAsync(context)
            .map(rb -> {
                ServiceGetAccountInfoHeaders hd = rb.getDeserializedHeaders();
                return new SimpleResponse<>(rb, new StorageAccountInfo(hd.getSkuName(), hd.getAccountKind()));
            });
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
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p>The snippet below generates a SAS that lasts for two days and gives the user read and list access to blob
     * containers and file shares.</p>
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.generateAccountSas#AccountSasSignatureValues}
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues) {
        throwOnAnonymousAccess();
        return new AccountSasImplUtil(accountSasSignatureValues)
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()));
    }

    /**
     * Checks if service client was built with credentials.
     */
    private void throwOnAnonymousAccess() {
        if (anonymousAccess) {
            throw logger.logExceptionAsError(new IllegalStateException("Service client cannot be accessed without "
                + "credentials"));
        }
    }

    /**
     * Restores a previously deleted container.
     * If the container associated with provided <code>deletedContainerName</code>
     * already exists, this call will result in a 409 (conflict).
     * This API is only functional if Container Soft Delete is enabled
     * for the storage account associated with the container.
     * For more information, see the
     * <a href="TBD">Azure Docs</a>. TODO (kasobol-msft) add link to REST API docs
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.undeleteBlobContainer#String-String}
     *
     * @param deletedContainerName The name of the previously deleted container.
     * @param deletedContainerVersion The version of the previously deleted container.
     * @return A {@link Mono} containing a {@link BlobContainerAsyncClient} used
     * to interact with the restored container.
     */
    /*
    public Mono<BlobContainerAsyncClient> undeleteBlobContainer(
        String deletedContainerName, String deletedContainerVersion) {
        return this.undeleteBlobContainerWithResponse(
            new UndeleteBlobContainerOptions(deletedContainerName, deletedContainerVersion)
        ).flatMap(FluxUtil::toMono);
    }
    */

    /**
     * Restores a previously deleted container. The restored container
     * will be renamed to the <code>destinationContainerName</code> if provided in <code>options</code>.
     * Otherwise <code>deletedContainerName</code> is used as destination container name.
     * If the container associated with provided <code>destinationContainerName</code>
     * already exists, this call will result in a 409 (conflict).
     * This API is only functional if Container Soft Delete is enabled
     * for the storage account associated with the container.
     * For more information, see the
     * <a href="TBD">Azure Docs</a>. TODO (kasobol-msft) add link to REST API docs
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.BlobServiceAsyncClient.undeleteBlobContainerWithResponse#UndeleteBlobContainerOptions}
     *
     * @param options {@link UndeleteBlobContainerOptions}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * BlobContainerAsyncClient} used to interact with the restored container.
     */
    /*
    public Mono<Response<BlobContainerAsyncClient>> undeleteBlobContainerWithResponse(
        UndeleteBlobContainerOptions options) {
        try {
            return withContext(context ->
                undeleteBlobContainerWithResponse(
                    options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<BlobContainerAsyncClient>> undeleteBlobContainerWithResponse(
        UndeleteBlobContainerOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        boolean hasOptionalDestinationContainerName = options.getDestinationContainerName() != null;
        String finalDestinationContainerName =
            hasOptionalDestinationContainerName ? options.getDestinationContainerName()
                : options.getDeletedContainerName();
        context = context == null ? Context.NONE : context;
        return this.azureBlobStorage.containers().restoreWithRestResponseAsync(finalDestinationContainerName, null,
            null, options.getDeletedContainerName(), options.getDeletedContainerVersion(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response,
                getBlobContainerAsyncClient(finalDestinationContainerName)));
    }
    */
}
