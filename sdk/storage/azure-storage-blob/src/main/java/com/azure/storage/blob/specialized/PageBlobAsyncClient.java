// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.implementation.models.PageBlobsClearPagesHeaders;
import com.azure.storage.blob.implementation.models.PageBlobsCreateHeaders;
import com.azure.storage.blob.implementation.models.PageBlobsResizeHeaders;
import com.azure.storage.blob.implementation.models.PageBlobsUpdateSequenceNumberHeaders;
import com.azure.storage.blob.implementation.models.PageBlobsUploadPagesFromURLHeaders;
import com.azure.storage.blob.implementation.models.PageBlobsUploadPagesHeaders;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.PageBlobCopyIncrementalRequestConditions;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageList;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.SequenceNumberActionType;
import com.azure.storage.blob.options.PageBlobCopyIncrementalOptions;
import com.azure.storage.blob.options.PageBlobCreateOptions;
import com.azure.storage.blob.options.PageBlobUploadPagesFromUrlOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;

/**
 * Client to a page blob. It may only be instantiated through a {@link SpecializedBlobClientBuilder} or via the method
 * {@link BlobAsyncClient#getPageBlobAsyncClient()}. This class does not hold any state about a particular blob, but is
 * instead a convenient way of sending appropriate requests to the resource on the service.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a> for more information.
 *
 * <p>
 * Note this client is an async client that returns reactive responses from Spring Reactor Core project
 * (https://projectreactor.io/). Calling the methods in this client will <strong>NOT</strong> start the actual network
 * operation, until {@code .subscribe()} is called on the reactive response. You can simply convert one of these
 * responses to a {@link java.util.concurrent.CompletableFuture} object through {@link Mono#toFuture()}.
 */
@ServiceClient(builder = SpecializedBlobClientBuilder.class, isAsync = true)
public final class PageBlobAsyncClient extends BlobAsyncClientBase {
    /**
     * Indicates the number of bytes in a page.
     */
    public static final int PAGE_BYTES = 512;

    /**
     * Indicates the maximum number of bytes that may be sent in a call to putPage.
     */
    public static final int MAX_PUT_PAGES_BYTES = 4 * Constants.MB;

    private final ClientLogger logger = new ClientLogger(PageBlobAsyncClient.class);

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
     * @param versionId The version identifier for the blob, pass {@code null} to interact with the latest blob version
     * @param serializerAdapter serializer adapter.
     */
    PageBlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion,
        String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey,
        EncryptionScope encryptionScope, String versionId, SerializerAdapter serializerAdapter) {
        super(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey,
            encryptionScope, versionId, serializerAdapter);
    }

    /**
     * Creates a new {@link PageBlobAsyncClient} with the specified {@code encryptionScope}.
     *
     * @param encryptionScope the encryption scope for the blob, pass {@code null} to use no encryption scope.
     * @return a {@link PageBlobAsyncClient} with the specified {@code encryptionScope}.
     */
    @Override
    public PageBlobAsyncClient getEncryptionScopeAsyncClient(String encryptionScope) {
        EncryptionScope finalEncryptionScope = null;
        if (encryptionScope != null) {
            finalEncryptionScope = new EncryptionScope().setEncryptionScope(encryptionScope);
        }
        return new PageBlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), getSnapshotId(), getCustomerProvidedKey(), finalEncryptionScope,
            getVersionId(), getSerializerAdapter());
    }

    /**
     * Creates a new {@link PageBlobAsyncClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the blob,
     * pass {@code null} to use no customer provided key.
     * @return a {@link PageBlobAsyncClient} with the specified {@code customerProvidedKey}.
     */
    @Override
    public PageBlobAsyncClient getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey) {
        CpkInfo finalCustomerProvidedKey = null;
        if (customerProvidedKey != null) {
            finalCustomerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }
        return new PageBlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), getSnapshotId(), finalCustomerProvidedKey, encryptionScope,
            getVersionId(), getSerializerAdapter());
    }

    private static String pageRangeToString(PageRange pageRange) {
        if (pageRange.getStart() < 0 || pageRange.getEnd() <= 0) {
            throw new IllegalArgumentException("PageRange's start and end values must be greater than or equal to "
                + "0 if specified.");
        }
        if (pageRange.getStart() % PAGE_BYTES != 0) {
            throw new IllegalArgumentException("PageRange's start value must be a multiple of 512.");
        }
        if (pageRange.getEnd() % PAGE_BYTES != PAGE_BYTES - 1) {
            throw new IllegalArgumentException("PageRange's end value must be 1 less than a multiple of 512.");
        }
        if (pageRange.getEnd() <= pageRange.getStart()) {
            throw new IllegalArgumentException("PageRange's End value must be after the start.");
        }
        return "bytes=" + pageRange.getStart() + '-' + pageRange.getEnd();
    }

    /**
     * Creates a page blob of the specified length. By default this method will not overwrite an existing blob.
     * Call PutPage to upload data data to a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.PageBlobAsyncClient.create#long}
     *
     * @param size Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     *
     * @return A reactive response containing the information of the created page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PageBlobItem> create(long size) {
        try {
            return create(size, false);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a page blob of the specified length. Call PutPage to upload data data to a page blob. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.PageBlobAsyncClient.create#long-boolean}
     *
     * @param size Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     * @param overwrite Whether or not to overwrite, should data exist on the blob.
     * @return A reactive response containing the information of the created page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PageBlobItem> create(long size, boolean overwrite) {
        try {
            BlobRequestConditions blobRequestConditions = new BlobRequestConditions();
            if (!overwrite) {
                blobRequestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
            }
            return createWithResponse(size, null, null, null, blobRequestConditions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.createWithResponse#long-Long-BlobHttpHeaders-Map-BlobRequestConditions}
     *
     * @param size Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     * @param sequenceNumber A user-controlled value that you can use to track requests. The value of the sequence
     * number must be between 0 and 2^63 - 1.The default value is 0.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the information of the created page blob.
     *
     * @throws IllegalArgumentException If {@code size} isn't a multiple of {@link PageBlobAsyncClient#PAGE_BYTES} or
     * {@code sequenceNumber} isn't null and is less than 0.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PageBlobItem>> createWithResponse(long size, Long sequenceNumber, BlobHttpHeaders headers,
        Map<String, String> metadata, BlobRequestConditions requestConditions) {
        return this.createWithResponse(new PageBlobCreateOptions(size).setSequenceNumber(sequenceNumber)
            .setHeaders(headers).setMetadata(metadata).setRequestConditions(requestConditions));
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
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.createWithResponse#PageBlobCreateOptions}
     *
     * @param options {@link PageBlobCreateOptions}
     * @return A reactive response containing the information of the created page blob.
     *
     * @throws IllegalArgumentException If {@code size} isn't a multiple of {@link PageBlobAsyncClient#PAGE_BYTES} or
     * {@code sequenceNumber} isn't null and is less than 0.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PageBlobItem>> createWithResponse(PageBlobCreateOptions options) {
        try {
            return withContext(context ->
                createWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageBlobItem>> createWithResponse(PageBlobCreateOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();

        if (options.getSize() % PAGE_BYTES != 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(
                new IllegalArgumentException("size must be a multiple of PageBlobAsyncClient.PAGE_BYTES."));
        }
        if (options.getSequenceNumber() != null && options.getSequenceNumber() < 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(
                new IllegalArgumentException("SequenceNumber must be greater than or equal to 0."));
        }
        context = context == null ? Context.NONE : context;
        BlobImmutabilityPolicy immutabilityPolicy = options.getImmutabilityPolicy() == null
            ? new BlobImmutabilityPolicy() : options.getImmutabilityPolicy();

        return this.azureBlobStorage.getPageBlobs().createWithResponseAsync(containerName, blobName, 0,
            options.getSize(), null, null, options.getMetadata(), requestConditions.getLeaseId(),
            requestConditions.getIfModifiedSince(), requestConditions.getIfUnmodifiedSince(),
            requestConditions.getIfMatch(), requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(),
            options.getSequenceNumber(), null, tagsToString(options.getTags()),
            immutabilityPolicy.getExpiryTime(), immutabilityPolicy.getPolicyMode(), options.isLegalHold(),
            options.getHeaders(), getCustomerProvidedKey(), encryptionScope,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> {
                PageBlobsCreateHeaders hd = rb.getDeserializedHeaders();
                PageBlobItem item = new PageBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
                    hd.isXMsRequestServerEncrypted(), hd.getXMsEncryptionKeySha256(), hd.getXMsEncryptionScope(),
                    null, hd.getXMsVersionId());
                return new SimpleResponse<>(rb, item);
            });
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
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPages#PageRange-Flux}
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param body The data to upload. Note that this {@code Flux} must be replayable if retries are enabled (the
     * default). In other words, the Flowable must produce the same data each time it is subscribed to.
     *
     * @return A reactive response containing the information of the uploaded pages.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PageBlobItem> uploadPages(PageRange pageRange, Flux<ByteBuffer> body) {
        try {
            return uploadPagesWithResponse(pageRange, body, null, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
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
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesWithResponse#PageRange-Flux-byte-PageBlobRequestConditions}
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param body The data to upload. Note that this {@code Flux} must be replayable if retries are enabled (the
     * default). In other words, the Flowable must produce the same data each time it is subscribed to.
     * @param contentMd5 An MD5 hash of the page content. This hash is used to verify the integrity of the page during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     * @param pageBlobRequestConditions {@link PageBlobRequestConditions}
     * @return A reactive response containing the information of the uploaded pages.
     *
     * @throws IllegalArgumentException If {@code pageRange} is {@code null}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PageBlobItem>> uploadPagesWithResponse(PageRange pageRange, Flux<ByteBuffer> body,
        byte[] contentMd5, PageBlobRequestConditions pageBlobRequestConditions) {
        try {
            return withContext(context -> uploadPagesWithResponse(pageRange, body, contentMd5,
                pageBlobRequestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageBlobItem>> uploadPagesWithResponse(PageRange pageRange, Flux<ByteBuffer> body, byte[] contentMd5,
        PageBlobRequestConditions pageBlobRequestConditions, Context context) {
        pageBlobRequestConditions = pageBlobRequestConditions == null
            ? new PageBlobRequestConditions()
            : pageBlobRequestConditions;

        if (pageRange == null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(new IllegalArgumentException("pageRange cannot be null."));
        }
        String pageRangeStr = pageRangeToString(pageRange);
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getPageBlobs().uploadPagesWithResponseAsync(containerName, blobName,
            pageRange.getEnd() - pageRange.getStart() + 1, body, contentMd5, null, null, pageRangeStr,
            pageBlobRequestConditions.getLeaseId(),
            pageBlobRequestConditions.getIfSequenceNumberLessThanOrEqualTo(),
            pageBlobRequestConditions.getIfSequenceNumberLessThan(),
            pageBlobRequestConditions.getIfSequenceNumberEqualTo(), pageBlobRequestConditions.getIfModifiedSince(),
            pageBlobRequestConditions.getIfUnmodifiedSince(), pageBlobRequestConditions.getIfMatch(),
            pageBlobRequestConditions.getIfNoneMatch(), pageBlobRequestConditions.getTagsConditions(), null,
            getCustomerProvidedKey(), encryptionScope,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> {
                PageBlobsUploadPagesHeaders hd = rb.getDeserializedHeaders();
                PageBlobItem item = new PageBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
                    hd.isXMsRequestServerEncrypted(), hd.getXMsEncryptionKeySha256(), hd.getXMsEncryptionScope(),
                    hd.getXMsBlobSequenceNumber());
                return new SimpleResponse<>(rb, item);
            });
    }

    /**
     * Writes one or more pages from the source page blob to this page blob. The write size must be a multiple of 512.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromUrl#PageRange-String-Long}
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
     *
     * @return A reactive response containing the information of the uploaded pages.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PageBlobItem> uploadPagesFromUrl(PageRange range, String sourceUrl, Long sourceOffset) {
        try {
            return uploadPagesFromUrlWithResponse(range, sourceUrl, sourceOffset, null, null, null)
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Writes one or more pages from the source page blob to this page blob. The write size must be a multiple of 512.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromUrlWithResponse#PageRange-String-Long-byte-PageBlobRequestConditions-BlobRequestConditions}
     *
     * @param range The destination {@link PageRange} range. Given that pages must be aligned with 512-byte boundaries,
     * the start offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte
     * ranges are 0-511, 512-1023, etc.
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceOffset The source offset to copy from.  Pass null or 0 to copy from the beginning of source blob.
     * @param sourceContentMd5 An MD5 hash of the page content. This hash is used to verify the integrity of the page
     * during transport. When this header is specified, the storage service compares the hash of the content that has
     * arrived with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not
     * match, the operation will fail.
     * @param destRequestConditions {@link PageBlobRequestConditions}
     * @param sourceRequestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the information of the uploaded pages.
     *
     * @throws IllegalArgumentException If {@code range} is {@code null}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PageBlobItem>> uploadPagesFromUrlWithResponse(PageRange range, String sourceUrl,
            Long sourceOffset, byte[] sourceContentMd5, PageBlobRequestConditions destRequestConditions,
            BlobRequestConditions sourceRequestConditions) {
        try {
            return withContext(context -> uploadPagesFromUrlWithResponse(new PageBlobUploadPagesFromUrlOptions(range, sourceUrl)
                .setSourceOffset(sourceOffset).setSourceContentMd5(sourceContentMd5)
                .setDestinationRequestConditions(destRequestConditions)
                .setSourceRequestConditions(sourceRequestConditions), context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Writes one or more pages from the source page blob to this page blob. The write size must be a multiple of 512.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.uploadPagesFromUrlWithResponse#PageBlobUploadPagesFromUrlOptions}
     *
     * @param options Parameters for the operation.
     * @return A reactive response containing the information of the uploaded pages.
     *
     * @throws IllegalArgumentException If {@code range} is {@code null}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PageBlobItem>> uploadPagesFromUrlWithResponse(PageBlobUploadPagesFromUrlOptions options) {
        try {
            return withContext(context -> uploadPagesFromUrlWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageBlobItem>> uploadPagesFromUrlWithResponse(PageBlobUploadPagesFromUrlOptions options, Context context) {
        if (options.getRange() == null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(new IllegalArgumentException("range cannot be null."));
        }

        String rangeString = pageRangeToString(options.getRange());

        long sourceOffset = options.getSourceOffset() == null ? 0L : options.getSourceOffset();

        String sourceRangeString = pageRangeToString(new PageRange()
            .setStart(sourceOffset)
            .setEnd(sourceOffset + (options.getRange().getEnd() - options.getRange().getStart())));

        PageBlobRequestConditions destRequestConditions = (options.getDestinationRequestConditions() == null)
            ? new PageBlobRequestConditions() : options.getDestinationRequestConditions();
        BlobRequestConditions sourceRequestConditions = (options.getSourceRequestConditions() == null)
            ? new BlobRequestConditions() : options.getSourceRequestConditions();

        try {
            new URL(options.getSourceUrl());
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url."));
        }
        context = context == null ? Context.NONE : context;
        String sourceAuth = options.getSourceAuthorization() == null
            ? null : options.getSourceAuthorization().toString();

        return this.azureBlobStorage.getPageBlobs().uploadPagesFromURLWithResponseAsync(
            containerName, blobName, options.getSourceUrl(), sourceRangeString, 0, rangeString, options.getSourceContentMd5(), null, null,
            destRequestConditions.getLeaseId(), destRequestConditions.getIfSequenceNumberLessThanOrEqualTo(),
            destRequestConditions.getIfSequenceNumberLessThan(), destRequestConditions.getIfSequenceNumberEqualTo(),
            destRequestConditions.getIfModifiedSince(), destRequestConditions.getIfUnmodifiedSince(),
            destRequestConditions.getIfMatch(), destRequestConditions.getIfNoneMatch(),
            destRequestConditions.getTagsConditions(), sourceRequestConditions.getIfModifiedSince(),
            sourceRequestConditions.getIfUnmodifiedSince(), sourceRequestConditions.getIfMatch(),
            sourceRequestConditions.getIfNoneMatch(), null, sourceAuth, getCustomerProvidedKey(),
            encryptionScope, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> {
                PageBlobsUploadPagesFromURLHeaders hd = rb.getDeserializedHeaders();
                PageBlobItem item = new PageBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
                    hd.isXMsRequestServerEncrypted(), hd.getXMsEncryptionKeySha256(), hd.getXMsEncryptionScope(), null);
                return new SimpleResponse<>(rb, item);
            });
    }

    /**
     * Frees the specified pages from the page blob. The size of the range must be a multiple of 512. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.clearPages#PageRange}
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     *
     * @return A reactive response containing the information of the cleared pages.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PageBlobItem> clearPages(PageRange pageRange) {
        try {
            return clearPagesWithResponse(pageRange, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Frees the specified pages from the page blob. The size of the range must be a multiple of 512. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-page">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.clearPagesWithResponse#PageRange-PageBlobRequestConditions}
     *
     * @param pageRange A {@link PageRange} object. Given that pages must be aligned with 512-byte boundaries, the start
     * offset must be a modulus of 512 and the end offset must be a modulus of 512 - 1. Examples of valid byte ranges
     * are 0-511, 512-1023, etc.
     * @param pageBlobRequestConditions {@link PageBlobRequestConditions}
     * @return A reactive response containing the information of the cleared pages.
     *
     * @throws IllegalArgumentException If {@code pageRange} is {@code null}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PageBlobItem>> clearPagesWithResponse(PageRange pageRange,
        PageBlobRequestConditions pageBlobRequestConditions) {
        try {
            return withContext(context -> clearPagesWithResponse(pageRange, pageBlobRequestConditions,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageBlobItem>> clearPagesWithResponse(PageRange pageRange,
        PageBlobRequestConditions pageBlobRequestConditions, Context context) {
        pageBlobRequestConditions = pageBlobRequestConditions == null
            ? new PageBlobRequestConditions()
            : pageBlobRequestConditions;
        if (pageRange == null) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(new IllegalArgumentException("pageRange cannot be null."));
        }
        String pageRangeStr = pageRangeToString(pageRange);
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getPageBlobs().clearPagesWithResponseAsync(containerName, blobName, 0,
            null, pageRangeStr, pageBlobRequestConditions.getLeaseId(),
            pageBlobRequestConditions.getIfSequenceNumberLessThanOrEqualTo(),
            pageBlobRequestConditions.getIfSequenceNumberLessThan(),
            pageBlobRequestConditions.getIfSequenceNumberEqualTo(), pageBlobRequestConditions.getIfModifiedSince(),
            pageBlobRequestConditions.getIfUnmodifiedSince(), pageBlobRequestConditions.getIfMatch(),
            pageBlobRequestConditions.getIfNoneMatch(), pageBlobRequestConditions.getTagsConditions(), null,
            getCustomerProvidedKey(), encryptionScope,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> {
                PageBlobsClearPagesHeaders hd = rb.getDeserializedHeaders();
                PageBlobItem item = new PageBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
                    hd.isXMsRequestServerEncrypted(), hd.getXMsEncryptionKeySha256(), null,
                    hd.getXMsBlobSequenceNumber());
                return new SimpleResponse<>(rb, item);
            });
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRanges#BlobRange}
     *
     * @param blobRange {@link BlobRange}
     *
     * @return A reactive response containing the information of the cleared pages.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PageList> getPageRanges(BlobRange blobRange) {
        try {
            return getPageRangesWithResponse(blobRange, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the list of valid page ranges for a page blob or snapshot of a page blob. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesWithResponse#BlobRange-BlobRequestConditions}
     *
     * @param blobRange {@link BlobRange}
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response emitting all the page ranges.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PageList>> getPageRangesWithResponse(BlobRange blobRange,
        BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> getPageRangesWithResponse(blobRange, requestConditions,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageList>> getPageRangesWithResponse(BlobRange blobRange, BlobRequestConditions requestConditions,
        Context context) {
        blobRange = blobRange == null ? new BlobRange(0) : blobRange;
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getPageBlobs().getPageRangesWithResponseAsync(containerName, blobName, getSnapshotId(), null,
            blobRange.toHeaderValue(), requestConditions.getLeaseId(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesDiff#BlobRange-String}
     *
     * @param blobRange {@link BlobRange}
     * @param prevSnapshot Specifies that the response will contain only pages that were changed between target blob and
     * previous snapshot. Changed pages include both updated and cleared pages. The target blob may be a snapshot, as
     * long as the snapshot specified by prevsnapshot is the older of the two.
     *
     * @return A reactive response emitting all the different page ranges.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PageList> getPageRangesDiff(BlobRange blobRange, String prevSnapshot) {
        try {
            return getPageRangesDiffWithResponse(blobRange, prevSnapshot, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.getPageRangesDiffWithResponse#BlobRange-String-BlobRequestConditions}
     *
     * @param blobRange {@link BlobRange}
     * @param prevSnapshot Specifies that the response will contain only pages that were changed between target blob and
     * previous snapshot. Changed pages include both updated and cleared pages. The target blob may be a snapshot, as
     * long as the snapshot specified by prevsnapshot is the older of the two.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response emitting all the different page ranges.
     *
     * @throws IllegalArgumentException If {@code prevSnapshot} is {@code null}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PageList>> getPageRangesDiffWithResponse(BlobRange blobRange, String prevSnapshot,
        BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> getPageRangesDiffWithResponse(blobRange, prevSnapshot, null,
                requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * This API only works for managed disk accounts.
     * <p>Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.getManagedDiskPageRangesDiff#BlobRange-String}
     *
     * @param blobRange {@link BlobRange}
     * @param prevSnapshotUrl Specifies the URL of a previous snapshot of the target blob. Specifies that the
     * response will contain only pages that were changed between target blob and previous snapshot. Changed pages
     * include both updated and cleared pages. The target blob may be a snapshot, as long as the snapshot specified by
     * prevsnapshot is the older of the two.
     *
     * @return A reactive response emitting all the different page ranges.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PageList> getManagedDiskPageRangesDiff(BlobRange blobRange, String prevSnapshotUrl) {
        try {
            return getManagedDiskPageRangesDiffWithResponse(blobRange, prevSnapshotUrl, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * This API only works for managed disk accounts.
     * <p>Gets the collection of page ranges that differ between a specified snapshot and this page blob. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-page-ranges">Azure
     * Docs</a>.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.getManagedDiskPageRangesDiffWithResponse#BlobRange-String-BlobRequestConditions}
     *
     * @param blobRange {@link BlobRange}
     * @param prevSnapshotUrl Specifies the URL of a previous snapshot of the target blob. Specifies that the
     * response will contain only pages that were changed between target blob and previous snapshot. Changed pages
     * include both updated and cleared pages. The target blob may be a snapshot, as long as the snapshot specified by
     * prevsnapshot is the older of the two.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response emitting all the different page ranges.
     *
     * @throws IllegalArgumentException If {@code prevSnapshot} is {@code null}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PageList>> getManagedDiskPageRangesDiffWithResponse(BlobRange blobRange,
        String prevSnapshotUrl, BlobRequestConditions requestConditions) {
        try {
            return withContext(context ->
                getPageRangesDiffWithResponse(blobRange, null, prevSnapshotUrl, requestConditions,
                    context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageList>> getPageRangesDiffWithResponse(BlobRange blobRange, String prevSnapshot,
        String prevSnapshotUrl, BlobRequestConditions requestConditions, Context context) {
        blobRange = blobRange == null ? new BlobRange(0) : blobRange;
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        if (prevSnapshotUrl == null && prevSnapshot == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("prevSnapshot cannot be null"));
        }
        if (prevSnapshotUrl != null) {
            try {
                new URL(prevSnapshotUrl);
            } catch (MalformedURLException ex) {
                throw logger.logExceptionAsError(new IllegalArgumentException("'prevSnapshotUrl' is not a valid url."));
            }
        }
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getPageBlobs().getPageRangesDiffWithResponseAsync(containerName, blobName,
            getSnapshotId(), null, prevSnapshot, prevSnapshotUrl, blobRange.toHeaderValue(), requestConditions.getLeaseId(),
            requestConditions.getIfModifiedSince(), requestConditions.getIfUnmodifiedSince(),
            requestConditions.getIfMatch(), requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(),
            null, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Resizes the page blob to the specified size (which must be a multiple of 512). For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.resize#long}
     *
     * @param size Resizes a page blob to the specified size. If the specified value is less than the current size of
     * the blob, then all pages above the specified value are cleared.
     *
     * @return A reactive response emitting the resized page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PageBlobItem> resize(long size) {
        try {
            return resizeWithResponse(size, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resizes the page blob to the specified size (which must be a multiple of 512). For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.resizeWithResponse#long-BlobRequestConditions}
     *
     * @param size Resizes a page blob to the specified size. If the specified value is less than the current size of
     * the blob, then all pages above the specified value are cleared.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response emitting the resized page blob.
     *
     * @throws IllegalArgumentException If {@code size} isn't a multiple of {@link PageBlobAsyncClient#PAGE_BYTES}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PageBlobItem>> resizeWithResponse(long size, BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> resizeWithResponse(size, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageBlobItem>> resizeWithResponse(long size, BlobRequestConditions requestConditions,
        Context context) {
        if (size % PAGE_BYTES != 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(
                new IllegalArgumentException("size must be a multiple of PageBlobAsyncClient.PAGE_BYTES."));
        }
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getPageBlobs().resizeWithResponseAsync(containerName, blobName, size, null,
            requestConditions.getLeaseId(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null,
            getCustomerProvidedKey(), encryptionScope,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> {
                PageBlobsResizeHeaders hd = rb.getDeserializedHeaders();
                PageBlobItem item = new PageBlobItem(hd.getETag(), hd.getLastModified(), null, null, null, null,
                    hd.getXMsBlobSequenceNumber());
                return new SimpleResponse<>(rb, item);
            });
    }

    /**
     * Sets the page blob's sequence number. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.updateSequenceNumber#SequenceNumberActionType-Long}
     *
     * @param action Indicates how the service should modify the blob's sequence number.
     * @param sequenceNumber The blob's sequence number. The sequence number is a user-controlled property that you can
     * use to track requests and manage concurrency issues.
     *
     * @return A reactive response emitting the updated page blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PageBlobItem> updateSequenceNumber(SequenceNumberActionType action, Long sequenceNumber) {
        try {
            return updateSequenceNumberWithResponse(action, sequenceNumber, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sets the page blob's sequence number. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.updateSequenceNumberWithResponse#SequenceNumberActionType-Long-BlobRequestConditions}
     *
     * @param action Indicates how the service should modify the blob's sequence number.
     * @param sequenceNumber The blob's sequence number. The sequence number is a user-controlled property that you can
     * use to track requests and manage concurrency issues.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response emitting the updated page blob.
     *
     * @throws IllegalArgumentException If {@code sequenceNumber} isn't null and is less than 0
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PageBlobItem>> updateSequenceNumberWithResponse(SequenceNumberActionType action,
        Long sequenceNumber, BlobRequestConditions requestConditions) {
        try {
            return withContext(context -> updateSequenceNumberWithResponse(action, sequenceNumber, requestConditions,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PageBlobItem>> updateSequenceNumberWithResponse(SequenceNumberActionType action, Long sequenceNumber,
        BlobRequestConditions requestConditions, Context context) {
        if (sequenceNumber != null && sequenceNumber < 0) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw logger.logExceptionAsError(
                new IllegalArgumentException("SequenceNumber must be greater than or equal to 0."));
        }
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        sequenceNumber = action == SequenceNumberActionType.INCREMENT ? null : sequenceNumber;
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getPageBlobs().updateSequenceNumberWithResponseAsync(containerName, blobName, action, null,
            requestConditions.getLeaseId(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), sequenceNumber, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> {
                PageBlobsUpdateSequenceNumberHeaders hd = rb.getDeserializedHeaders();
                PageBlobItem item = new PageBlobItem(hd.getETag(), hd.getLastModified(), null, null, null, null,
                    hd.getXMsBlobSequenceNumber());
                return new SimpleResponse<>(rb, item);
            });
    }

    /**
     * Begins an operation to start an incremental copy from one page blob's snapshot to this page blob. The snapshot
     * is
     * copied such that only the differential changes between the previously copied snapshot are transferred to the
     * destination. The copied snapshots are complete copies of the original snapshot and can be read or copied from as
     * usual. For more information, see the Azure Docs
     * <a href="https://docs.microsoft.com/rest/api/storageservices/incremental-copy-blob">here</a>
     * and
     * <a href="https://docs.microsoft.com/azure/virtual-machines/windows/incremental-snapshots">here</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncremental#String-String}
     *
     * @param source The source page blob.
     * @param snapshot The snapshot on the copy source.
     *
     * @return A reactive response emitting the copy status.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CopyStatusType> copyIncremental(String source, String snapshot) {
        try {
            return copyIncrementalWithResponse(source, snapshot, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Begins an operation to start an incremental copy from one page blob's snapshot to this page blob. The snapshot
     * is
     * copied such that only the differential changes between the previously copied snapshot are transferred to the
     * destination. The copied snapshots are complete copies of the original snapshot and can be read or copied from as
     * usual. For more information, see the Azure Docs
     * <a href="https://docs.microsoft.com/rest/api/storageservices/incremental-copy-blob">here</a>
     * and
     * <a href="https://docs.microsoft.com/azure/virtual-machines/windows/incremental-snapshots">here</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncrementalWithResponse#String-String-RequestConditions}
     *
     * @param source The source page blob.
     * @param snapshot The snapshot on the copy source.
     * @param modifiedRequestConditions Standard HTTP Access conditions related to the modification of data. ETag and
     * LastModifiedTime are used to construct conditions related to when the blob was changed relative to the given
     * request. The request will fail if the specified condition is not satisfied.
     *
     * @return A reactive response emitting the copy status.
     *
     * @throws IllegalStateException If {@code source} and {@code snapshot} form a malformed URL.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CopyStatusType>> copyIncrementalWithResponse(String source, String snapshot,
        RequestConditions modifiedRequestConditions) {
        try {
            return copyIncrementalWithResponse(new PageBlobCopyIncrementalOptions(source, snapshot)
                .setRequestConditions(
                    ModelHelper.populateBlobDestinationRequestConditions(modifiedRequestConditions)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Begins an operation to start an incremental copy from one page blob's snapshot to this page blob. The snapshot
     * is
     * copied such that only the differential changes between the previously copied snapshot are transferred to the
     * destination. The copied snapshots are complete copies of the original snapshot and can be read or copied from as
     * usual. For more information, see the Azure Docs
     * <a href="https://docs.microsoft.com/rest/api/storageservices/incremental-copy-blob">here</a>
     * and
     * <a href="https://docs.microsoft.com/azure/virtual-machines/windows/incremental-snapshots">here</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.PageBlobAsyncClient.copyIncrementalWithResponse#PageBlobCopyIncrementalOptions}
     *
     * @param options {@link PageBlobCopyIncrementalOptions}
     *
     * @return A reactive response emitting the copy status.
     *
     * @throws IllegalStateException If {@code source} and {@code snapshot} form a malformed URL.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CopyStatusType>> copyIncrementalWithResponse(PageBlobCopyIncrementalOptions options) {
        try {
            return withContext(context -> copyIncrementalWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CopyStatusType>> copyIncrementalWithResponse(PageBlobCopyIncrementalOptions options,
        Context context) {
        StorageImplUtils.assertNotNull("options", options);
        UrlBuilder builder = UrlBuilder.parse(options.getSource());
        builder.setQueryParameter(Constants.UrlConstants.SNAPSHOT_QUERY_PARAMETER, options.getSnapshot());
        PageBlobCopyIncrementalRequestConditions modifiedRequestConditions = (options.getRequestConditions() == null)
            ? new PageBlobCopyIncrementalRequestConditions() : options.getRequestConditions();

        try {
            builder.toUrl();
        } catch (MalformedURLException e) {
            // We are parsing a valid url and adding a query parameter. If this fails, we can't recover.
            throw logger.logExceptionAsError(new IllegalArgumentException(e));
        }
        context = context == null ? Context.NONE : context;

        return this.azureBlobStorage.getPageBlobs().copyIncrementalWithResponseAsync(containerName, blobName, builder.toString(), null,
            modifiedRequestConditions.getIfModifiedSince(), modifiedRequestConditions.getIfUnmodifiedSince(),
            modifiedRequestConditions.getIfMatch(), modifiedRequestConditions.getIfNoneMatch(),
            modifiedRequestConditions.getTagsConditions(), null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(rb -> new SimpleResponse<>(rb, rb.getDeserializedHeaders().getXMsCopyStatus()));
    }
}
