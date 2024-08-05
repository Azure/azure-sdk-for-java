// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRange;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.implementation.models.PageBlobsClearPagesHeaders;
import com.azure.storage.blob.implementation.models.PageBlobsCopyIncrementalHeaders;
import com.azure.storage.blob.implementation.models.PageBlobsCreateHeaders;
import com.azure.storage.blob.implementation.models.PageBlobsGetPageRangesDiffHeaders;
import com.azure.storage.blob.implementation.models.PageBlobsGetPageRangesHeaders;
import com.azure.storage.blob.implementation.models.PageBlobsResizeHeaders;
import com.azure.storage.blob.implementation.models.PageBlobsUpdateSequenceNumberHeaders;
import com.azure.storage.blob.implementation.models.PageListHelper;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ClearRange;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.PageBlobCopyIncrementalRequestConditions;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageList;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.PageRangeItem;
import com.azure.storage.blob.models.SequenceNumberActionType;
import com.azure.storage.blob.options.ListPageRangesDiffOptions;
import com.azure.storage.blob.options.ListPageRangesOptions;
import com.azure.storage.blob.options.PageBlobCopyIncrementalOptions;
import com.azure.storage.blob.options.PageBlobCreateOptions;
import com.azure.storage.blob.options.PageBlobUploadPagesFromUrlOptions;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.storage.common.implementation.StorageImplUtils.sendRequest;

/**
 * Client to a page blob. It may only be instantiated through a {@link SpecializedBlobClientBuilder} or via the method
 * {@link BlobClient#getPageBlobClient()}. This class does not hold any state about a particular blob, but is instead a
 * convenient way of sending appropriate requests to the resource on the service.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = SpecializedBlobClientBuilder.class)
public final class PageBlobClient extends BlobClientBase {
    private static final ClientLogger LOGGER = new ClientLogger(PageBlobClient.class);
    private final PageBlobAsyncClient pageBlobAsyncClient;

    /**
     * Indicates the number of bytes in a page.
     */
    public static final int PAGE_BYTES = 512;

    /**
     * Indicates the maximum number of bytes that may be sent in a call to putPage.
     */
    public static final int MAX_PUT_PAGES_BYTES = 4 * Constants.MB;

    /**
     * Package-private constructor for use by {@link SpecializedBlobClientBuilder}.
     *
     * @param pageBlobAsyncClient the async page blob client
     */
    PageBlobClient(PageBlobAsyncClient pageBlobAsyncClient) {
        this(pageBlobAsyncClient, pageBlobAsyncClient.getHttpPipeline(), pageBlobAsyncClient.getAccountUrl(),
            pageBlobAsyncClient.getServiceVersion(), pageBlobAsyncClient.getAccountName(),
            pageBlobAsyncClient.getContainerName(), pageBlobAsyncClient.getBlobName(),
            pageBlobAsyncClient.getSnapshotId(), pageBlobAsyncClient.getCustomerProvidedKey(),
            new EncryptionScope().setEncryptionScope(pageBlobAsyncClient.getEncryptionScope()),
            pageBlobAsyncClient.getVersionId());
    }

    /**
     * Package-private constructor for use by {@link SpecializedBlobClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param containerName The container name.
     * @param blobName The blob name.
     * @param snapshot The snapshot identifier for the blob, pass {@code null} to interact with the blob directly.
     * @param customerProvidedKey Customer provided key used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     * @param encryptionScope Encryption scope used during encryption of the blob's data on the server, pass
     * {@code null} to allow the service to use its own encryption.
     * @param versionId The version identifier for the blob, pass {@code null} to interact with the latest blob version.
     */
    PageBlobClient(PageBlobAsyncClient pageBlobAsyncClient, HttpPipeline pipeline, String url,
        BlobServiceVersion serviceVersion, String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey,
        EncryptionScope encryptionScope, String versionId) {
        super(pageBlobAsyncClient, pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey,
            encryptionScope, versionId);
        this.pageBlobAsyncClient = pageBlobAsyncClient;
    }

    /**
     * Creates a new {@link PageBlobClient} with the specified {@code encryptionScope}.
     *
     * @param encryptionScope the encryption scope for the blob, pass {@code null} to use no encryption scope.
     * @return a {@link PageBlobClient} with the specified {@code encryptionScope}.
     */
    @Override
    public PageBlobClient getEncryptionScopeClient(String encryptionScope) {
        EncryptionScope finalEncryptionScope = null;
        if (encryptionScope != null) {
            finalEncryptionScope = new EncryptionScope().setEncryptionScope(encryptionScope);
        }
        PageBlobAsyncClient asyncClient = pageBlobAsyncClient.getEncryptionScopeAsyncClient(encryptionScope);
        return new PageBlobClient(asyncClient, getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), getSnapshotId(), getCustomerProvidedKey(), finalEncryptionScope,
            getVersionId());
    }

    /**
     * Creates a new {@link PageBlobClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the blob,
     * pass {@code null} to use no customer provided key.
     * @return a {@link PageBlobClient} with the specified {@code customerProvidedKey}.
     */
    @Override
    public PageBlobClient getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey) {
        CpkInfo finalCustomerProvidedKey = null;
        if (customerProvidedKey != null) {
            finalCustomerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }
        PageBlobAsyncClient asyncClient = pageBlobAsyncClient.getCustomerProvidedKeyAsyncClient(customerProvidedKey);
        return new PageBlobClient(asyncClient, getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), getSnapshotId(), finalCustomerProvidedKey, encryptionScope,
            getVersionId());
    }

    /**
     * Creates and opens an output stream to write data to the page blob. If the blob already exists on the service, it
     * will be overwritten.
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(PageRange pageRange) {
        return getBlobOutputStream(pageRange, null);
    }

    /**
     * Creates and opens an output stream to write data to the page blob. If the blob already exists on the service, it
     * will be overwritten.
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param requestConditions A {@link BlobRequestConditions} object that represents the access conditions for the
     * blob.
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(PageRange pageRange, BlobRequestConditions requestConditions) {
        return BlobOutputStream.pageBlobOutputStream(pageBlobAsyncClient, pageRange, requestConditions);
    }

    /**
     * Creates a page blob of the specified length. By default this method will not overwrite an existing blob.
     * Call PutPage to upload data data to a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.PageBlobClient.create#long -->
     * <pre>
     * PageBlobItem pageBlob = client.create&#40;size&#41;;
     * System.out.printf&#40;&quot;Created page blob with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.PageBlobClient.create#long -->
     *
     * @param size Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     * @return The information of the created page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PageBlobItem create(long size) {
        return create(size, false);
    }

    /**
     * Creates a page blob of the specified length. Call PutPage to upload data data to a page blob. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.PageBlobClient.create#long-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;&#47; Default value
     * PageBlobItem pageBlob = client.create&#40;size, overwrite&#41;;
     * System.out.printf&#40;&quot;Created page blob with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.PageBlobClient.create#long-boolean -->
     *
     * @param size Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     * @param overwrite Whether or not to overwrite, should data exist on the blob.
     * @return The information of the created page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PageBlobItem create(long size, boolean overwrite) {
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions();
        if (!overwrite) {
            blobRequestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return createWithResponse(size, null, null, null, blobRequestConditions, null, Context.NONE).getValue();
    }

    /**
     * Creates a page blob of the specified length. Call PutPage to upload data data to a page blob. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.createWithResponse#long-Long-BlobHttpHeaders-Map-BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobHttpHeaders headers = new BlobHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * BlobRequestConditions blobRequestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * PageBlobItem pageBlob = client
     *     .createWithResponse&#40;size, sequenceNumber, headers, metadata, blobRequestConditions, timeout, context&#41;
     *     .getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Created page blob with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.createWithResponse#long-Long-BlobHttpHeaders-Map-BlobRequestConditions-Duration-Context -->
     *
     * @param size Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     * @param sequenceNumber A user-controlled value that you can use to track requests. The value of the sequence
     * number must be between 0 and 2^63 - 1.The default value is 0.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The information of the created page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PageBlobItem> createWithResponse(long size, Long sequenceNumber, BlobHttpHeaders headers,
        Map<String, String> metadata, BlobRequestConditions requestConditions, Duration timeout, Context context) {
        return this.createWithResponse(new PageBlobCreateOptions(size).setSequenceNumber(sequenceNumber)
            .setHeaders(headers).setMetadata(metadata).setRequestConditions(requestConditions), timeout,
            context);
    }

    /**
     * Creates a page blob of the specified length. Call PutPage to upload data data to a page blob. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.createWithResponse#PageBlobCreateOptions-Duration-Context -->
     * <pre>
     * BlobHttpHeaders headers = new BlobHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * BlobRequestConditions blobRequestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * PageBlobItem pageBlob = client
     *     .createWithResponse&#40;new PageBlobCreateOptions&#40;size&#41;.setSequenceNumber&#40;sequenceNumber&#41;
     *             .setHeaders&#40;headers&#41;.setMetadata&#40;metadata&#41;.setTags&#40;tags&#41;
     *             .setRequestConditions&#40;blobRequestConditions&#41;, timeout,
     *         context&#41;
     *     .getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Created page blob with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.createWithResponse#PageBlobCreateOptions-Duration-Context -->
     *
     * @param options {@link PageBlobCreateOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The information of the created page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PageBlobItem> createWithResponse(PageBlobCreateOptions options, Duration timeout, Context context) {
//        Mono<Response<PageBlobItem>> response = pageBlobAsyncClient.createWithResponse(options, context);
//        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
        StorageImplUtils.assertNotNull("options", options);
        Context finalContext = context == null ? Context.NONE : context;
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();

        if (options.getSize() % PAGE_BYTES != 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("size must be a multiple of PageBlobAsyncClient.PAGE_BYTES."));
        }
        if (options.getSequenceNumber() != null && options.getSequenceNumber() < 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("SequenceNumber must be greater than or equal to 0."));
        }
        BlobImmutabilityPolicy immutabilityPolicy = options.getImmutabilityPolicy() == null
            ? new BlobImmutabilityPolicy() : options.getImmutabilityPolicy();

        Callable<ResponseBase<PageBlobsCreateHeaders, Void>> operation = () ->
            this.azureBlobStorage.getPageBlobs().createWithResponse(containerName, blobName, 0, options.getSize(), null,
                null, options.getMetadata(), requestConditions.getLeaseId(), requestConditions.getIfModifiedSince(),
                requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
                requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), options.getSequenceNumber(),
                null, ModelHelper.tagsToString(options.getTags()), immutabilityPolicy.getExpiryTime(),
                immutabilityPolicy.getPolicyMode(), options.isLegalHold(), options.getHeaders(),
                getCustomerProvidedKey(), encryptionScope, finalContext);

        ResponseBase<PageBlobsCreateHeaders, Void> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        PageBlobsCreateHeaders hd = response.getDeserializedHeaders();
        PageBlobItem item = new PageBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
            hd.isXMsRequestServerEncrypted(), hd.getXMsEncryptionKeySha256(), hd.getXMsEncryptionScope(),
            null, hd.getXMsVersionId());
        return new SimpleResponse<>(response, item);
    }

    /**
     * Creates a page blob of the specified length if it does not exist.
     * Call PutPage to upload data to a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.PageBlobClient.createIfNotExists#long -->
     * <pre>
     * PageBlobItem pageBlob = client.createIfNotExists&#40;size&#41;;
     * System.out.printf&#40;&quot;Created page blob with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.PageBlobClient.createIfNotExists#long -->
     *
     * @param size Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     * @return {@link PageBlobItem} containing information of the created page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PageBlobItem createIfNotExists(long size) {
        return createIfNotExistsWithResponse(new PageBlobCreateOptions(size), null, null).getValue();
    }

    /**
     * Creates a page blob of the specified length if it does not exist. Call PutPage to upload data to a page blob.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.createIfNotExistsWithResponse#PageBlobCreateOptions-Duration-Context -->
     * <pre>
     * BlobHttpHeaders headers = new BlobHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * Response&lt;PageBlobItem&gt; response = client.createIfNotExistsWithResponse&#40;new PageBlobCreateOptions&#40;size&#41;
     *     .setHeaders&#40;headers&#41;.setMetadata&#40;metadata&#41;.setTags&#40;tags&#41;, timeout, context&#41;;
     *
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.createIfNotExistsWithResponse#PageBlobCreateOptions-Duration-Context -->
     *
     * @param options {@link PageBlobCreateOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A reactive {@link Response} signaling completion, whose {@link Response#getValue() value} contains a
     * {@link PageBlobItem} containing information about the page blob. If {@link Response}'s status code is 201, a new
     * page blob was successfully created. If status code is 409, a page blob already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PageBlobItem> createIfNotExistsWithResponse(PageBlobCreateOptions options, Duration timeout,
        Context context) {
        StorageImplUtils.assertNotNull("options", options);
        options.setRequestConditions(new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD)
            .setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD));
        try {
            return createWithResponse(options, timeout, context);
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 409 && (e.getErrorCode().equals(BlobErrorCode.BLOB_ALREADY_EXISTS)
                || e.getErrorCode().equals(BlobErrorCode.RESOURCE_ALREADY_EXISTS))) {
                HttpResponse res = e.getResponse();
                return new SimpleResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), null);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Writes one or more pages to the page blob. The write size must be a multiple of 512. For more information, see
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.uploadPages#PageRange-InputStream -->
     * <pre>
     * PageRange pageRange = new PageRange&#40;&#41;
     *     .setStart&#40;0&#41;
     *     .setEnd&#40;511&#41;;
     * InputStream dataStream = new ByteArrayInputStream&#40;data.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     *
     * PageBlobItem pageBlob = client.uploadPages&#40;pageRange, dataStream&#41;;
     * System.out.printf&#40;&quot;Uploaded page blob with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.uploadPages#PageRange-InputStream -->
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param body The data to upload. The data must be markable. This is in order to support retries. If
     * the data is not markable, consider using {@link #getBlobOutputStream(PageRange)} and writing to the returned
     * OutputStream. Alternatively, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add
     * mark support.
     * @return The information of the uploaded pages.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PageBlobItem uploadPages(PageRange pageRange, InputStream body) {
        return uploadPagesWithResponse(pageRange, body, null, null, null, Context.NONE).getValue();
    }

    /**
     * Writes one or more pages to the page blob. The write size must be a multiple of 512. For more information, see
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     * <p>
     * Note that the data passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.uploadPagesWithResponse#PageRange-InputStream-byte-PageBlobRequestConditions-Duration-Context -->
     * <pre>
     * byte[] md5 = MessageDigest.getInstance&#40;&quot;MD5&quot;&#41;.digest&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * PageRange pageRange = new PageRange&#40;&#41;
     *     .setStart&#40;0&#41;
     *     .setEnd&#40;511&#41;;
     * InputStream dataStream = new ByteArrayInputStream&#40;data.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * PageBlobRequestConditions pageBlobRequestConditions = new PageBlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * PageBlobItem pageBlob = client
     *     .uploadPagesWithResponse&#40;pageRange, dataStream, md5, pageBlobRequestConditions, timeout, context&#41;.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Uploaded page blob with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.uploadPagesWithResponse#PageRange-InputStream-byte-PageBlobRequestConditions-Duration-Context -->
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param body The data to upload. The data must be markable. This is in order to support retries. If
     * the data is not markable, consider using {@link #getBlobOutputStream(PageRange)} and writing to the returned
     * OutputStream. Alternatively, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add
     * mark support.
     * @param contentMd5 An MD5 hash of the page content. This hash is used to verify the integrity of the page during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     * @param pageBlobRequestConditions {@link PageBlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The information of the uploaded pages.
     * @throws UnexpectedLengthException when the length of data does not match the input {@code length}.
     * @throws NullPointerException if the input data is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PageBlobItem> uploadPagesWithResponse(PageRange pageRange, InputStream body, byte[] contentMd5,
        PageBlobRequestConditions pageBlobRequestConditions, Duration timeout, Context context) {
        Objects.requireNonNull(body, "'body' cannot be null.");
        final long length = pageRange.getEnd() - pageRange.getStart() + 1;
        Flux<ByteBuffer> fbb = Utility.convertStreamToByteBuffer(body, length, PAGE_BYTES, true);

        Mono<Response<PageBlobItem>> response = pageBlobAsyncClient.uploadPagesWithResponse(pageRange, fbb, contentMd5,
            pageBlobRequestConditions, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Writes one or more pages from the source page blob to this page blob. The write size must be a multiple of 512.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromUrl#PageRange-String-Long -->
     * <pre>
     * PageRange pageRange = new PageRange&#40;&#41;
     *     .setStart&#40;0&#41;
     *     .setEnd&#40;511&#41;;
     *
     * PageBlobItem pageBlob = client.uploadPagesFromUrl&#40;pageRange, url, sourceOffset&#41;;
     *
     * System.out.printf&#40;&quot;Uploaded page blob from URL with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromUrl#PageRange-String-Long -->
     *
     * @param range A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceOffset The source offset to copy from.  Pass null or 0 to copy from the beginning of source page
     * blob.
     * @return The information of the uploaded pages.
     * @throws IllegalArgumentException If {@code sourceUrl} is a malformed {@link URL}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PageBlobItem uploadPagesFromUrl(PageRange range, String sourceUrl, Long sourceOffset) {
        return uploadPagesFromUrlWithResponse(range, sourceUrl, sourceOffset, null, null, null, null, Context.NONE)
            .getValue();
    }

    /**
     * Writes one or more pages from the source page blob to this page blob. The write size must be a multiple of 512.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromUrlWithResponse#PageRange-String-Long-byte-PageBlobRequestConditions-BlobRequestConditions-Duration-Context -->
     * <pre>
     * PageRange pageRange = new PageRange&#40;&#41;
     *     .setStart&#40;0&#41;
     *     .setEnd&#40;511&#41;;
     * InputStream dataStream = new ByteArrayInputStream&#40;data.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * byte[] sourceContentMD5 = new byte[512];
     * PageBlobRequestConditions pageBlobRequestConditions = new PageBlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * BlobRequestConditions sourceRequestConditions = new BlobRequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * PageBlobItem pageBlob = client
     *     .uploadPagesFromUrlWithResponse&#40;pageRange, url, sourceOffset, sourceContentMD5, pageBlobRequestConditions,
     *         sourceRequestConditions, timeout, context&#41;.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Uploaded page blob from URL with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromUrlWithResponse#PageRange-String-Long-byte-PageBlobRequestConditions-BlobRequestConditions-Duration-Context -->
     *
     * @param range The destination {@link PageRange} range. Given that pages must be aligned with 512-byte boundaries,
     * the start offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte
     * ranges are 0-511, 512-1023, etc.
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceOffset The source offset to copy from.  Pass null or 0 to copy from the beginning of source blob.
     * @param sourceContentMd5 An MD5 hash of the block content from the source blob. If specified, the service will
     * calculate the MD5 of the received data and fail the request if it does not match the provided MD5.
     * @param destRequestConditions {@link PageBlobRequestConditions}
     * @param sourceRequestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The information of the uploaded pages.
     * @throws IllegalArgumentException If {@code sourceUrl} is a malformed {@link URL}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PageBlobItem> uploadPagesFromUrlWithResponse(PageRange range, String sourceUrl, Long sourceOffset,
        byte[] sourceContentMd5, PageBlobRequestConditions destRequestConditions,
        BlobRequestConditions sourceRequestConditions, Duration timeout, Context context) {

        return uploadPagesFromUrlWithResponse(
            new PageBlobUploadPagesFromUrlOptions(range, sourceUrl).setSourceOffset(sourceOffset)
                .setSourceContentMd5(sourceContentMd5).setDestinationRequestConditions(destRequestConditions)
                .setSourceRequestConditions(sourceRequestConditions),
            timeout, context);
    }

    /**
     * Writes one or more pages from the source page blob to this page blob. The write size must be a multiple of 512.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromUrlWithResponse#PageBlobUploadPagesFromUrlOptions-Duration-Context -->
     * <pre>
     * PageRange pageRange = new PageRange&#40;&#41;
     *     .setStart&#40;0&#41;
     *     .setEnd&#40;511&#41;;
     * InputStream dataStream = new ByteArrayInputStream&#40;data.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     * byte[] sourceContentMD5 = new byte[512];
     * PageBlobRequestConditions pageBlobRequestConditions = new PageBlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * BlobRequestConditions sourceRequestConditions = new BlobRequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * PageBlobItem pageBlob = client
     *     .uploadPagesFromUrlWithResponse&#40;new PageBlobUploadPagesFromUrlOptions&#40;pageRange, url&#41;
     *         .setSourceOffset&#40;sourceOffset&#41;.setSourceContentMd5&#40;sourceContentMD5&#41;
     *         .setDestinationRequestConditions&#40;pageBlobRequestConditions&#41;
     *         .setSourceRequestConditions&#40;sourceRequestConditions&#41;, timeout, context&#41;.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Uploaded page blob from URL with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.uploadPagesFromUrlWithResponse#PageBlobUploadPagesFromUrlOptions-Duration-Context -->
     *
     * @param options Parameters for the operation.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The information of the uploaded pages.
     * @throws IllegalArgumentException If {@code sourceUrl} is a malformed {@link URL}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PageBlobItem> uploadPagesFromUrlWithResponse(PageBlobUploadPagesFromUrlOptions options, Duration timeout,
        Context context) {

        Mono<Response<PageBlobItem>> response = pageBlobAsyncClient.uploadPagesFromUrlWithResponse(options, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Frees the specified pages from the page blob. The size of the range must be a multiple of 512. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.clearPages#PageRange -->
     * <pre>
     * PageRange pageRange = new PageRange&#40;&#41;
     *     .setStart&#40;0&#41;
     *     .setEnd&#40;511&#41;;
     *
     * PageBlobItem pageBlob = client.clearPages&#40;pageRange&#41;;
     *
     * System.out.printf&#40;&quot;Cleared page blob with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.clearPages#PageRange -->
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @return The information of the cleared pages.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PageBlobItem clearPages(PageRange pageRange) {
        return clearPagesWithResponse(pageRange, null, null, Context.NONE).getValue();
    }

    /**
     * Frees the specified pages from the page blob. The size of the range must be a multiple of 512. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.clearPagesWithResponse#PageRange-PageBlobRequestConditions-Duration-Context -->
     * <pre>
     * PageRange pageRange = new PageRange&#40;&#41;
     *     .setStart&#40;0&#41;
     *     .setEnd&#40;511&#41;;
     * PageBlobRequestConditions pageBlobRequestConditions = new PageBlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * PageBlobItem pageBlob = client
     *     .clearPagesWithResponse&#40;pageRange, pageBlobRequestConditions, timeout, context&#41;.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Cleared page blob with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.clearPagesWithResponse#PageRange-PageBlobRequestConditions-Duration-Context -->
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param pageBlobRequestConditions {@link PageBlobRequestConditions}
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The information of the cleared pages.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PageBlobItem> clearPagesWithResponse(PageRange pageRange,
        PageBlobRequestConditions pageBlobRequestConditions, Duration timeout, Context context) {
        PageBlobRequestConditions finalPageBlobRequestConditions = pageBlobRequestConditions == null
            ? new PageBlobRequestConditions() : pageBlobRequestConditions;
        if (pageRange == null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("pageRange cannot be null."));
        }
        String pageRangeStr = ModelHelper.pageRangeToString(pageRange);
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<PageBlobsClearPagesHeaders, Void>> operation = () ->
            this.azureBlobStorage.getPageBlobs().clearPagesWithResponse(containerName, blobName, 0, null, pageRangeStr,
                finalPageBlobRequestConditions.getLeaseId(),
                finalPageBlobRequestConditions.getIfSequenceNumberLessThanOrEqualTo(),
                finalPageBlobRequestConditions.getIfSequenceNumberLessThan(),
                finalPageBlobRequestConditions.getIfSequenceNumberEqualTo(),
                finalPageBlobRequestConditions.getIfModifiedSince(),
                finalPageBlobRequestConditions.getIfUnmodifiedSince(), finalPageBlobRequestConditions.getIfMatch(),
                finalPageBlobRequestConditions.getIfNoneMatch(), finalPageBlobRequestConditions.getTagsConditions(), null,
                getCustomerProvidedKey(), encryptionScope, finalContext);
        ResponseBase<PageBlobsClearPagesHeaders, Void> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        PageBlobsClearPagesHeaders hd = response.getDeserializedHeaders();
        PageBlobItem item = new PageBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
            hd.isXMsRequestServerEncrypted(), hd.getXMsEncryptionKeySha256(), null,
            hd.getXMsBlobSequenceNumber());
        return new SimpleResponse<>(response, item);
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.getPageRanges#BlobRange -->
     * <pre>
     * BlobRange blobRange = new BlobRange&#40;offset&#41;;
     * PageList pageList = client.getPageRanges&#40;blobRange&#41;;
     *
     * System.out.println&#40;&quot;Valid Page Ranges are:&quot;&#41;;
     * for &#40;PageRange pageRange : pageList.getPageRange&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Start: %s, End: %s%n&quot;, pageRange.getStart&#40;&#41;, pageRange.getEnd&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.getPageRanges#BlobRange -->
     *
     * @param blobRange {@link BlobRange}
     * @return The information of the cleared pages.
     * @deprecated See {@link #listPageRanges(BlobRange)}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Deprecated
    public PageList getPageRanges(BlobRange blobRange) {
        return getPageRangesWithResponse(blobRange, null, null, Context.NONE).getValue();
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.getPageRangesWithResponse#BlobRange-BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobRange blobRange = new BlobRange&#40;offset&#41;;
     * BlobRequestConditions blobRequestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * PageList pageList = client
     *     .getPageRangesWithResponse&#40;blobRange, blobRequestConditions, timeout, context&#41;.getValue&#40;&#41;;
     *
     * System.out.println&#40;&quot;Valid Page Ranges are:&quot;&#41;;
     * for &#40;PageRange pageRange : pageList.getPageRange&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Start: %s, End: %s%n&quot;, pageRange.getStart&#40;&#41;, pageRange.getEnd&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.getPageRangesWithResponse#BlobRange-BlobRequestConditions-Duration-Context -->
     *
     * @param blobRange {@link BlobRange}
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return All the page ranges.
     * @deprecated See {@link #listPageRanges(ListPageRangesOptions,Duration,Context)}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Deprecated
    public Response<PageList> getPageRangesWithResponse(BlobRange blobRange, BlobRequestConditions requestConditions,
        Duration timeout, Context context) {
        BlobRange finalBlobRange = blobRange == null ? new BlobRange(0) : blobRange;
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        Context finalContext = context == null ? Context.NONE : context;

        Callable<ResponseBase<PageBlobsGetPageRangesHeaders, PageList>> operation = () ->
            this.azureBlobStorage.getPageBlobs().getPageRangesWithResponse(containerName, blobName,
                getSnapshotId(), null, finalBlobRange.toHeaderValue(), finalRequestConditions.getLeaseId(),
                    finalRequestConditions.getIfModifiedSince(), finalRequestConditions.getIfUnmodifiedSince(),
                    finalRequestConditions.getIfMatch(), finalRequestConditions.getIfNoneMatch(),
                    finalRequestConditions.getTagsConditions(), null, null, null,
                finalContext);
        ResponseBase<PageBlobsGetPageRangesHeaders, PageList> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            response.getValue());
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.listPageRanges#BlobRange -->
     * <pre>
     * BlobRange blobRange = new BlobRange&#40;offset&#41;;
     * String prevSnapshot = &quot;previous snapshot&quot;;
     * PagedIterable&lt;PageRangeItem&gt; iterable = client.listPageRanges&#40;blobRange&#41;;
     *
     * for &#40;PageRangeItem item : iterable&#41; &#123;
     *     System.out.printf&#40;&quot;Offset: %s, Length: %s, isClear: %s%n&quot;, item.getRange&#40;&#41;.getOffset&#40;&#41;,
     *         item.getRange&#40;&#41;.getLength&#40;&#41;, item.isClear&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.listPageRanges#BlobRange -->
     *
     * @param blobRange {@link BlobRange}
     *
     * @return A reactive response containing the information of the cleared pages.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<PageRangeItem> listPageRanges(BlobRange blobRange) {
        return listPageRanges(new ListPageRangesOptions(blobRange), null, null);
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.getPageRangesWithResponse#ListPageRangesOptions-Duration-Context -->
     * <pre>
     * ListPageRangesOptions options = new ListPageRangesOptions&#40;new BlobRange&#40;offset&#41;&#41;
     *     .setRequestConditions&#40;new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;
     *     .setMaxResultsPerPage&#40;1000&#41;;
     *
     * Context context = new Context&#40;key, value&#41;;
     *
     * PagedIterable&lt;PageRangeItem&gt; iter = client
     *     .listPageRanges&#40;options, timeout, context&#41;;
     *
     * System.out.println&#40;&quot;Valid Page Ranges are:&quot;&#41;;
     * for &#40;PageRangeItem item : iter&#41; &#123;
     *     System.out.printf&#40;&quot;Offset: %s, Length: %s, isClear: %s%n&quot;, item.getRange&#40;&#41;.getOffset&#40;&#41;,
     *         item.getRange&#40;&#41;.getLength&#40;&#41;, item.isClear&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.getPageRangesWithResponse#ListPageRangesOptions-Duration-Context -->
     *
     * @param options {@link ListPageRangesOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A reactive response emitting all the page ranges.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<PageRangeItem> listPageRanges(ListPageRangesOptions options, Duration timeout,
        Context context) {
        Objects.requireNonNull(options, "options must not be null");
        Context finalContext = context == null ? Context.NONE : context;

        // Helper function to retrieve a page of items
        BiFunction<String, Integer, PagedResponse<PageRangeItem>> pageRetriever = (continuationToken, pageSize) -> {
            BlobRequestConditions requestConditions = options.getRequestConditions() == null
                ? new BlobRequestConditions() : options.getRequestConditions();
            Integer finalPageSize = pageSize == null ? options.getMaxResultsPerPage() : pageSize;

            // Call the synchronous service method
            Callable<ResponseBase<PageBlobsGetPageRangesHeaders, PageList>> operation = () ->
                this.azureBlobStorage.getPageBlobs().getPageRangesWithResponse(containerName, blobName, getSnapshotId(),
                    null, options.getRange().toHeaderValue(), requestConditions.getLeaseId(),
                    requestConditions.getIfModifiedSince(), requestConditions.getIfUnmodifiedSince(),
                    requestConditions.getIfMatch(), requestConditions.getIfNoneMatch(),
                    requestConditions.getTagsConditions(), null, continuationToken, finalPageSize, finalContext);

            ResponseBase<PageBlobsGetPageRangesHeaders, PageList> response = sendRequest(operation, timeout,
                BlobStorageException.class);
            List<PageRangeItem> value = parsePageRangeItems(response.getValue());

            return new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                value,
                PageListHelper.getNextMarker(response.getValue()),
                response.getDeserializedHeaders());
        };
        return new PagedIterable<>(pageSize -> pageRetriever.apply(null, pageSize), pageRetriever);
    }

    private List<PageRangeItem> parsePageRangeItems(PageList pageList) {
        if (pageList == null) {
            return Collections.emptyList();
        }
        return Stream.concat(
            pageList.getPageRange().stream().map(this::toPageBlobRange),
            pageList.getClearRange().stream().map(this::toPageBlobRange)
        ).collect(Collectors.toList());
    }

    private PageRangeItem toPageBlobRange(PageRange range) {
        return new PageRangeItem(new HttpRange(range.getStart(), range.getEnd() - range.getStart() + 1), false);
    }

    private PageRangeItem toPageBlobRange(ClearRange range) {
        return new PageRangeItem(new HttpRange(range.getStart(), range.getEnd() - range.getStart() + 1), true);
    }

    /**
     * Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.getPageRangesDiff#BlobRange-String -->
     * <pre>
     * BlobRange blobRange = new BlobRange&#40;offset&#41;;
     * final String prevSnapshot = &quot;previous snapshot&quot;;
     * PageList pageList = client.getPageRangesDiff&#40;blobRange, prevSnapshot&#41;;
     *
     * System.out.println&#40;&quot;Valid Page Ranges are:&quot;&#41;;
     * for &#40;PageRange pageRange : pageList.getPageRange&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Start: %s, End: %s%n&quot;, pageRange.getStart&#40;&#41;, pageRange.getEnd&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.getPageRangesDiff#BlobRange-String -->
     *
     * @param blobRange {@link BlobRange}
     * @param prevSnapshot Specifies that the response will contain only pages that were changed between target blob and
     * previous snapshot. Changed pages include both updated and cleared pages. The target blob may be a snapshot, as
     * long as the snapshot specified by prevsnapshot is the older of the two.
     * @return All the different page ranges.
     * @deprecated See {@link #listPageRangesDiff(BlobRange, String)}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Deprecated
    public PageList getPageRangesDiff(BlobRange blobRange, String prevSnapshot) {
        return getPageRangesDiffWithResponse(blobRange, prevSnapshot, null, null, Context.NONE).getValue();
    }

    /**
     * This API only works for managed disk accounts.
     * <p>Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.getPageRangesDiffWithResponse#BlobRange-String-BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobRange blobRange = new BlobRange&#40;offset&#41;;
     * final String prevSnapshot = &quot;previous snapshot&quot;;
     * BlobRequestConditions blobRequestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * PageList pageList = client
     *     .getPageRangesDiffWithResponse&#40;blobRange, prevSnapshot, blobRequestConditions, timeout, context&#41;.getValue&#40;&#41;;
     *
     * System.out.println&#40;&quot;Valid Page Ranges are:&quot;&#41;;
     * for &#40;PageRange pageRange : pageList.getPageRange&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Start: %s, End: %s%n&quot;, pageRange.getStart&#40;&#41;, pageRange.getEnd&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.getPageRangesDiffWithResponse#BlobRange-String-BlobRequestConditions-Duration-Context -->
     *
     * @param blobRange {@link BlobRange}
     * @param prevSnapshot Specifies that the response will contain only pages that were changed between target blob and
     * previous snapshot. Changed pages include both updated and cleared pages. The target blob may be a snapshot, as
     * long as the snapshot specified by prevsnapshot is the older of the two.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return All the different page ranges.
     * @deprecated See {@link #listPageRanges(ListPageRangesOptions,Duration,Context)} )}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Deprecated
    public Response<PageList> getPageRangesDiffWithResponse(BlobRange blobRange, String prevSnapshot,
        BlobRequestConditions requestConditions, Duration timeout, Context context) {
        BlobRange finalBlobRange = blobRange == null ? new BlobRange(0) : blobRange;
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        if (prevSnapshot == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("prevSnapshot cannot be null"));
        }
        Context finalContext = context == null ? Context.NONE : context;

        Callable<ResponseBase<PageBlobsGetPageRangesDiffHeaders, PageList>> operation = () ->
            this.azureBlobStorage.getPageBlobs().getPageRangesDiffWithResponse(containerName, blobName, getSnapshotId(),
                    null, prevSnapshot, null, finalBlobRange.toHeaderValue(), finalRequestConditions.getLeaseId(),
                    finalRequestConditions.getIfModifiedSince(), finalRequestConditions.getIfUnmodifiedSince(),
                    finalRequestConditions.getIfMatch(), finalRequestConditions.getIfNoneMatch(),
                    finalRequestConditions.getTagsConditions(), null, null, null, finalContext);
        ResponseBase<PageBlobsGetPageRangesDiffHeaders, PageList> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            response.getValue());
    }

    /**
     * Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.listPageRangesDiff#BlobRange-String -->
     * <pre>
     * BlobRange blobRange = new BlobRange&#40;offset&#41;;
     * String prevSnapshot = &quot;previous snapshot&quot;;
     * PagedIterable&lt;PageRangeItem&gt; iterable = client.listPageRangesDiff&#40;blobRange, prevSnapshot&#41;;
     *
     * for &#40;PageRangeItem item : iterable&#41; &#123;
     *     System.out.printf&#40;&quot;Offset: %s, Length: %s, isClear: %s%n&quot;, item.getRange&#40;&#41;.getOffset&#40;&#41;,
     *         item.getRange&#40;&#41;.getLength&#40;&#41;, item.isClear&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.listPageRangesDiff#BlobRange-String -->
     *
     * @param blobRange {@link BlobRange}
     * @param prevSnapshot Specifies that the response will contain only pages that were changed between target blob and
     * previous snapshot. Changed pages include both updated and cleared pages. The target blob may be a snapshot, as
     * long as the snapshot specified by prevsnapshot is the older of the two.
     *
     * @return A reactive response emitting all the different page ranges.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<PageRangeItem> listPageRangesDiff(BlobRange blobRange, String prevSnapshot) {
        return listPageRangesDiff(new ListPageRangesDiffOptions(blobRange, prevSnapshot), null, null);
    }

    /**
     * Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.getPageRangesDiffWithResponse#ListPageRangesDiffOptions-Duration-Context -->
     * <pre>
     * ListPageRangesDiffOptions options = new ListPageRangesDiffOptions&#40;new BlobRange&#40;offset&#41;, &quot;previous snapshot&quot;&#41;
     *     .setRequestConditions&#40;new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;
     *     .setMaxResultsPerPage&#40;1000&#41;;
     *
     * Context context = new Context&#40;key, value&#41;;
     *
     * PagedIterable&lt;PageRangeItem&gt; iter = client
     *     .listPageRangesDiff&#40;options, timeout, context&#41;;
     *
     * System.out.println&#40;&quot;Valid Page Ranges are:&quot;&#41;;
     * for &#40;PageRangeItem item : iter&#41; &#123;
     *     System.out.printf&#40;&quot;Offset: %s, Length: %s, isClear: %s%n&quot;, item.getRange&#40;&#41;.getOffset&#40;&#41;,
     *         item.getRange&#40;&#41;.getLength&#40;&#41;, item.isClear&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.getPageRangesDiffWithResponse#ListPageRangesDiffOptions-Duration-Context -->
     *
     * @param options {@link ListPageRangesDiffOptions}.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A reactive response emitting all the different page ranges.
     *
     * @throws IllegalArgumentException If {@code prevSnapshot} is {@code null}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<PageRangeItem> listPageRangesDiff(ListPageRangesDiffOptions options, Duration timeout,
        Context context) {
        Objects.requireNonNull(options, "options must not be null");
        Context finalContext = context == null ? Context.NONE : context;

        BiFunction<String, Integer, PagedResponse<PageRangeItem>> pageRetriever = (continuationToken, pageSize) -> {
            BlobRequestConditions requestConditions = options.getRequestConditions() == null
                ? new BlobRequestConditions() : options.getRequestConditions();

            // Dynamically use pageSize provided during the iteration if available
            Integer finalPageSize = pageSize != null ? pageSize : options.getMaxResultsPerPage();

            Callable<ResponseBase<PageBlobsGetPageRangesDiffHeaders, PageList>> operation = () ->
                this.azureBlobStorage.getPageBlobs().getPageRangesDiffWithResponse(
                    containerName, blobName, getSnapshotId(), null,
                    options.getPreviousSnapshot(), null, options.getRange().toHeaderValue(),
                    requestConditions.getLeaseId(),
                    requestConditions.getIfModifiedSince(), requestConditions.getIfUnmodifiedSince(),
                    requestConditions.getIfMatch(), requestConditions.getIfNoneMatch(),
                    requestConditions.getTagsConditions(), null, continuationToken, finalPageSize, finalContext);

            ResponseBase<PageBlobsGetPageRangesDiffHeaders, PageList> response = sendRequest(operation, timeout,
                BlobStorageException.class);
            List<PageRangeItem> value = parsePageRangeItems(response.getValue());

            return new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                value,
                PageListHelper.getNextMarker(response.getValue()),
                response.getDeserializedHeaders());
        };
        return new PagedIterable<>(pageSize -> pageRetriever.apply(null, pageSize), pageRetriever);
    }

    /**
     * This API only works for managed disk accounts.
     * <p>Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.getManagedDiskPageRangesDiff#BlobRange-String -->
     * <pre>
     * BlobRange blobRange = new BlobRange&#40;offset&#41;;
     * final String prevSnapshotUrl = &quot;previous snapshot url&quot;;
     * PageList pageList = client.getPageRangesDiff&#40;blobRange, prevSnapshotUrl&#41;;
     *
     * System.out.println&#40;&quot;Valid Page Ranges are:&quot;&#41;;
     * for &#40;PageRange pageRange : pageList.getPageRange&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Start: %s, End: %s%n&quot;, pageRange.getStart&#40;&#41;, pageRange.getEnd&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.getManagedDiskPageRangesDiff#BlobRange-String -->
     *
     * @param blobRange {@link BlobRange}
     * @param prevSnapshotUrl Specifies the URL of a previous snapshot of the target blob. Specifies that the
     * response will contain only pages that were changed between target blob and previous snapshot. Changed pages
     * include both updated and cleared pages. The target blob may be a snapshot, as long as the snapshot specified by
     * prevsnapshot is the older of the two.
     * @return All the different page ranges.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PageList getManagedDiskPageRangesDiff(BlobRange blobRange, String prevSnapshotUrl) {
        return getManagedDiskPageRangesDiffWithResponse(blobRange, prevSnapshotUrl, null, null, Context.NONE)
            .getValue();
    }

    /**
     * This API only works for managed disk accounts.
     * <p>Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.getManagedDiskPageRangesDiffWithResponse#BlobRange-String-BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobRange blobRange = new BlobRange&#40;offset&#41;;
     * final String prevSnapshotUrl = &quot;previous snapshot url&quot;;
     * BlobRequestConditions blobRequestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * PageList pageList = client
     *     .getPageRangesDiffWithResponse&#40;blobRange, prevSnapshotUrl, blobRequestConditions, timeout, context&#41;.getValue&#40;&#41;;
     *
     * System.out.println&#40;&quot;Valid Page Ranges are:&quot;&#41;;
     * for &#40;PageRange pageRange : pageList.getPageRange&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Start: %s, End: %s%n&quot;, pageRange.getStart&#40;&#41;, pageRange.getEnd&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.getManagedDiskPageRangesDiffWithResponse#BlobRange-String-BlobRequestConditions-Duration-Context -->
     *
     * @param blobRange {@link BlobRange}
     * @param prevSnapshotUrl Specifies the URL of a previous snapshot of the target blob. Specifies that the
     * response will contain only pages that were changed between target blob and previous snapshot. Changed pages
     * include both updated and cleared pages. The target blob may be a snapshot, as long as the snapshot specified by
     * prevsnapshot is the older of the two.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return All the different page ranges.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PageList> getManagedDiskPageRangesDiffWithResponse(BlobRange blobRange, String prevSnapshotUrl,
        BlobRequestConditions requestConditions, Duration timeout, Context context) {
        BlobRange finalBlobRange = blobRange == null ? new BlobRange(0) : blobRange;
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        if (prevSnapshotUrl == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("prevSnapshot cannot be null"));
        }
        try {
            new URL(prevSnapshotUrl);
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'prevSnapshotUrl' is not a valid url.", ex));
        }
        Context finalContext = context == null ? Context.NONE : context;

        Callable<ResponseBase<PageBlobsGetPageRangesDiffHeaders, PageList>> operation = () ->
            this.azureBlobStorage.getPageBlobs().getPageRangesDiffWithResponse(containerName, blobName, getSnapshotId(),
                    null, null, prevSnapshotUrl, finalBlobRange.toHeaderValue(), finalRequestConditions.getLeaseId(),
                    finalRequestConditions.getIfModifiedSince(), finalRequestConditions.getIfUnmodifiedSince(),
                    finalRequestConditions.getIfMatch(), finalRequestConditions.getIfNoneMatch(),
                    finalRequestConditions.getTagsConditions(), null, null, null, finalContext);
        ResponseBase<PageBlobsGetPageRangesDiffHeaders, PageList> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), response.getValue());
    }

    /**
     * Resizes the page blob to the specified size (which must be a multiple of 512). For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.resize#long -->
     * <pre>
     * PageBlobItem pageBlob = client.resize&#40;size&#41;;
     * System.out.printf&#40;&quot;Page blob resized with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.resize#long -->
     *
     * @param size Resizes a page blob to the specified size. If the specified value is less than the current size of
     * the blob, then all pages above the specified value are cleared.
     * @return The resized page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PageBlobItem resize(long size) {
        return resizeWithResponse(size, null, null, Context.NONE).getValue();
    }

    /**
     * Resizes the page blob to the specified size (which must be a multiple of 512). For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.resizeWithResponse#long-BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobRequestConditions blobRequestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * PageBlobItem pageBlob = client
     *     .resizeWithResponse&#40;size, blobRequestConditions, timeout, context&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Page blob resized with sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.resizeWithResponse#long-BlobRequestConditions-Duration-Context -->
     *
     * @param size Resizes a page blob to the specified size. If the specified value is less than the current size of
     * the blob, then all pages above the specified value are cleared.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The resized page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PageBlobItem> resizeWithResponse(long size, BlobRequestConditions requestConditions,
        Duration timeout, Context context) {
        if (size % PAGE_BYTES != 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("size must be a multiple of PageBlobAsyncClient.PAGE_BYTES."));
        }
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions()
            : requestConditions;
        Context finalContext = context == null ? Context.NONE : context;

        Callable<ResponseBase<PageBlobsResizeHeaders, Void>> operation = () ->
            this.azureBlobStorage.getPageBlobs().resizeWithResponse(containerName, blobName, size, null,
                finalRequestConditions.getLeaseId(), finalRequestConditions.getIfModifiedSince(),
                finalRequestConditions.getIfUnmodifiedSince(), finalRequestConditions.getIfMatch(),
                finalRequestConditions.getIfNoneMatch(), finalRequestConditions.getTagsConditions(), null,
                getCustomerProvidedKey(), encryptionScope, finalContext);
        ResponseBase<PageBlobsResizeHeaders, Void> response = sendRequest(operation, timeout, BlobStorageException.class);

        PageBlobsResizeHeaders hd = response.getDeserializedHeaders();
        PageBlobItem item = new PageBlobItem(hd.getETag(), hd.getLastModified(), null, null, null, null,
            hd.getXMsBlobSequenceNumber());
        return new SimpleResponse<>(response, item);
    }

    /**
     * Sets the page blob's sequence number. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.updateSequenceNumber#SequenceNumberActionType-Long -->
     * <pre>
     * PageBlobItem pageBlob = client.updateSequenceNumber&#40;SequenceNumberActionType.INCREMENT, size&#41;;
     *
     * System.out.printf&#40;&quot;Page blob updated to sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.updateSequenceNumber#SequenceNumberActionType-Long -->
     *
     * @param action Indicates how the service should modify the blob's sequence number.
     * @param sequenceNumber The blob's sequence number. The sequence number is a user-controlled property that you can
     * use to track requests and manage concurrency issues.
     * @return The updated page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PageBlobItem updateSequenceNumber(SequenceNumberActionType action,
        Long sequenceNumber) {
        return updateSequenceNumberWithResponse(action, sequenceNumber, null, null, Context.NONE).getValue();
    }

    /**
     * Sets the page blob's sequence number. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.updateSequenceNumberWithResponse#SequenceNumberActionType-Long-BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobRequestConditions blobRequestConditions = new BlobRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * PageBlobItem pageBlob = client.updateSequenceNumberWithResponse&#40;
     *     SequenceNumberActionType.INCREMENT, size, blobRequestConditions, timeout, context&#41;.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Page blob updated to sequence number %s%n&quot;, pageBlob.getBlobSequenceNumber&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.updateSequenceNumberWithResponse#SequenceNumberActionType-Long-BlobRequestConditions-Duration-Context -->
     *
     * @param action Indicates how the service should modify the blob's sequence number.
     * @param sequenceNumber The blob's sequence number. The sequence number is a user-controlled property that you can
     * use to track requests and manage concurrency issues.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The updated page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PageBlobItem> updateSequenceNumberWithResponse(SequenceNumberActionType action,
        Long sequenceNumber, BlobRequestConditions requestConditions, Duration timeout, Context context) {
        if (sequenceNumber != null && sequenceNumber < 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("SequenceNumber must be greater than or equal to 0."));
        }
        BlobRequestConditions finalRequestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        Long finalSequenceNumber = action == SequenceNumberActionType.INCREMENT ? null : sequenceNumber;
        Context finalContext = context == null ? Context.NONE : context;

        Callable<ResponseBase<PageBlobsUpdateSequenceNumberHeaders, Void>> operation = () ->
            this.azureBlobStorage.getPageBlobs().updateSequenceNumberWithResponse(containerName, blobName, action, null,
                finalRequestConditions.getLeaseId(), finalRequestConditions.getIfModifiedSince(),
                finalRequestConditions.getIfUnmodifiedSince(), finalRequestConditions.getIfMatch(),
                finalRequestConditions.getIfNoneMatch(), finalRequestConditions.getTagsConditions(),
                finalSequenceNumber, null, finalContext);

        ResponseBase<PageBlobsUpdateSequenceNumberHeaders, Void> response = sendRequest(operation, timeout, BlobStorageException.class);
        PageBlobsUpdateSequenceNumberHeaders hd = response.getDeserializedHeaders();
        PageBlobItem item = new PageBlobItem(hd.getETag(), hd.getLastModified(), null, null, null, null,
            hd.getXMsBlobSequenceNumber());
        return new SimpleResponse<>(response, item);
    }

    /**
     * Begins an operation to start an incremental copy from one page blob's snapshot to this page blob. The snapshot is
     * copied such that only the differential changes between the previously copied snapshot are transferred to the
     * destination. The copied snapshots are complete copies of the original snapshot and can be read or copied from as
     * usual. For more information, see the Azure Docs <a href="https://docs.microsoft.com/rest/api/storageservices/incremental-copy-blob">here</a>
     * and
     * <a href="https://docs.microsoft.com/azure/virtual-machines/windows/incremental-snapshots">here</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.copyIncremental#String-String -->
     * <pre>
     * final String snapshot = &quot;copy snapshot&quot;;
     * CopyStatusType statusType = client.copyIncremental&#40;url, snapshot&#41;;
     *
     * switch &#40;statusType&#41; &#123;
     *     case SUCCESS:
     *         System.out.println&#40;&quot;Page blob copied successfully&quot;&#41;;
     *         break;
     *     case FAILED:
     *         System.out.println&#40;&quot;Page blob copied failed&quot;&#41;;
     *         break;
     *     case ABORTED:
     *         System.out.println&#40;&quot;Page blob copied aborted&quot;&#41;;
     *         break;
     *     case PENDING:
     *         System.out.println&#40;&quot;Page blob copied pending&quot;&#41;;
     *         break;
     *     default:
     *         break;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.copyIncremental#String-String -->
     *
     * @param source The source page blob.
     * @param snapshot The snapshot on the copy source.
     * @return The copy status.
     * @throws IllegalArgumentException If {@code source} is a malformed {@link URL}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CopyStatusType copyIncremental(String source, String snapshot) {
        return copyIncrementalWithResponse(source, snapshot, null, null, Context.NONE).getValue();
    }

    /**
     * Begins an operation to start an incremental copy from one page blob's snapshot to this page blob. The snapshot is
     * copied such that only the differential changes between the previously copied snapshot are transferred to the
     * destination. The copied snapshots are complete copies of the original snapshot and can be read or copied from as
     * usual. For more information, see the Azure Docs <a href="https://docs.microsoft.com/rest/api/storageservices/incremental-copy-blob">here</a>
     * and
     * <a href="https://docs.microsoft.com/azure/virtual-machines/windows/incremental-snapshots">here</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.copyIncrementalWithResponse#String-String-RequestConditions-Duration-Context -->
     * <pre>
     * final String snapshot = &quot;copy snapshot&quot;;
     * RequestConditions modifiedRequestConditions = new RequestConditions&#40;&#41;
     *     .setIfNoneMatch&#40;&quot;snapshotMatch&quot;&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * CopyStatusType statusType = client
     *     .copyIncrementalWithResponse&#40;url, snapshot, modifiedRequestConditions, timeout, context&#41;.getValue&#40;&#41;;
     *
     * switch &#40;statusType&#41; &#123;
     *     case SUCCESS:
     *         System.out.println&#40;&quot;Page blob copied successfully&quot;&#41;;
     *         break;
     *     case FAILED:
     *         System.out.println&#40;&quot;Page blob copied failed&quot;&#41;;
     *         break;
     *     case ABORTED:
     *         System.out.println&#40;&quot;Page blob copied aborted&quot;&#41;;
     *         break;
     *     case PENDING:
     *         System.out.println&#40;&quot;Page blob copied pending&quot;&#41;;
     *         break;
     *     default:
     *         break;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.copyIncrementalWithResponse#String-String-RequestConditions-Duration-Context -->
     *
     * @param source The source page blob.
     * @param snapshot The snapshot on the copy source.
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The copy status.
     * @throws IllegalArgumentException If {@code source} is a malformed {@link URL}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CopyStatusType> copyIncrementalWithResponse(String source, String snapshot,
        RequestConditions modifiedRequestConditions, Duration timeout, Context context) {
        return copyIncrementalWithResponse(new PageBlobCopyIncrementalOptions(source, snapshot)
            .setRequestConditions(
                ModelHelper.populateBlobDestinationRequestConditions(modifiedRequestConditions)),
            timeout, context);
    }

    /**
     * Begins an operation to start an incremental copy from one page blob's snapshot to this page blob. The snapshot is
     * copied such that only the differential changes between the previously copied snapshot are transferred to the
     * destination. The copied snapshots are complete copies of the original snapshot and can be read or copied from as
     * usual. For more information, see the Azure Docs <a href="https://docs.microsoft.com/rest/api/storageservices/incremental-copy-blob">here</a>
     * and
     * <a href="https://docs.microsoft.com/azure/virtual-machines/windows/incremental-snapshots">here</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.PageBlobClient.copyIncrementalWithResponse#PageBlobCopyIncrementalOptions-Duration-Context -->
     * <pre>
     * final String snapshot = &quot;copy snapshot&quot;;
     * PageBlobCopyIncrementalRequestConditions destinationRequestConditions = new PageBlobCopyIncrementalRequestConditions&#40;&#41;
     *     .setIfNoneMatch&#40;&quot;snapshotMatch&quot;&#41;;
     * Context context = new Context&#40;key, value&#41;;
     *
     * CopyStatusType statusType = client
     *     .copyIncrementalWithResponse&#40;new PageBlobCopyIncrementalOptions&#40;url, snapshot&#41;
     *         .setRequestConditions&#40;destinationRequestConditions&#41;, timeout, context&#41;.getValue&#40;&#41;;
     *
     * switch &#40;statusType&#41; &#123;
     *     case SUCCESS:
     *         System.out.println&#40;&quot;Page blob copied successfully&quot;&#41;;
     *         break;
     *     case FAILED:
     *         System.out.println&#40;&quot;Page blob copied failed&quot;&#41;;
     *         break;
     *     case ABORTED:
     *         System.out.println&#40;&quot;Page blob copied aborted&quot;&#41;;
     *         break;
     *     case PENDING:
     *         System.out.println&#40;&quot;Page blob copied pending&quot;&#41;;
     *         break;
     *     default:
     *         break;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.PageBlobClient.copyIncrementalWithResponse#PageBlobCopyIncrementalOptions-Duration-Context -->
     *
     * @param options {@link PageBlobCopyIncrementalOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The copy status.
     * @throws IllegalArgumentException If {@code source} is a malformed {@link URL}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CopyStatusType> copyIncrementalWithResponse(PageBlobCopyIncrementalOptions options,
        Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        UrlBuilder builder = UrlBuilder.parse(options.getSource());
        builder.setQueryParameter(Constants.UrlConstants.SNAPSHOT_QUERY_PARAMETER, options.getSnapshot());
        PageBlobCopyIncrementalRequestConditions modifiedRequestConditions = (options.getRequestConditions() == null)
            ? new PageBlobCopyIncrementalRequestConditions() : options.getRequestConditions();

        try {
            builder.toUrl();
        } catch (MalformedURLException e) {
            // We are parsing a valid url and adding a query parameter. If this fails, we can't recover.
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(e));
        }
        Context finalContext = context == null ? Context.NONE : context;

        Callable<ResponseBase<PageBlobsCopyIncrementalHeaders, Void>> operation = () ->
            this.azureBlobStorage.getPageBlobs().copyIncrementalWithResponse(containerName, blobName,
                    builder.toString(), null, modifiedRequestConditions.getIfModifiedSince(),
                modifiedRequestConditions.getIfUnmodifiedSince(), modifiedRequestConditions.getIfMatch(),
                modifiedRequestConditions.getIfNoneMatch(), modifiedRequestConditions.getTagsConditions(), null,
                    finalContext);
        ResponseBase<PageBlobsCopyIncrementalHeaders, Void> response = sendRequest(operation, timeout,
            BlobStorageException.class);
        return new SimpleResponse<>(response, response.getDeserializedHeaders().getXMsCopyStatus());
    }
}
