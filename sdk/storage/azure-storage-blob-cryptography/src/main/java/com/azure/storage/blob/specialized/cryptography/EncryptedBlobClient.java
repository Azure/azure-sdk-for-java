// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobDownloadContentResponse;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlobQueryOptions;
import com.azure.storage.blob.models.BlobQueryResponse;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_DATA_KEY;

/**
 * This class provides a client side encryption client that contains generic blob operations for Azure Storage Blobs.
 * Operations allowed by the client are uploading, downloading and copying a blob, retrieving and setting metadata,
 * retrieving and setting HTTP headers, and deleting and un-deleting a blob. The upload and download operation allow for
 * encryption and decryption of the data client side. Note: setting metadata in particular is unsafe and should only be
 * done so with caution.
 * <p> Please refer to the
 * <a href=https://docs.microsoft.com/azure/storage/common/storage-client-side-encryption-java>Azure
 * Docs For Client-Side Encryption</a> for more information.
 *
 * <p>
 * This client is instantiated through {@link EncryptedBlobClientBuilder}
 *
 * <p>
 * For operations on a specific blob type (i.e. append, block, or page) use
 * {@link #getAppendBlobClient() getAppendBlobClient}, {@link #getBlockBlobClient()
 * getBlockBlobClient}, or {@link #getPageBlobClient() getPageBlobAsyncClient} to construct a client that
 * allows blob specific operations. Note, these types do not support client-side encryption, though decryption is
 * possible in case the associated block/page/append blob contains encrypted data.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = EncryptedBlobClientBuilder.class)
public class EncryptedBlobClient extends BlobClient {
    private static final ClientLogger LOGGER = new ClientLogger(EncryptedBlobClient.class);
    private final EncryptedBlobAsyncClient encryptedBlobAsyncClient;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     */
    EncryptedBlobClient(EncryptedBlobAsyncClient encryptedBlobAsyncClient) {
        super(encryptedBlobAsyncClient);
        this.encryptedBlobAsyncClient = encryptedBlobAsyncClient;
    }

    /**
     * Creates a new {@link EncryptedBlobClient} with the specified {@code encryptionScope}.
     *
     * @param encryptionScope the encryption scope for the blob, pass {@code null} to use no encryption scope.
     * @return a {@link EncryptedBlobClient} with the specified {@code encryptionScope}.
     */
    @Override
    public EncryptedBlobClient getEncryptionScopeClient(String encryptionScope) {
        return new EncryptedBlobClient(encryptedBlobAsyncClient.getEncryptionScopeAsyncClient(encryptionScope));
    }

    /**
     * Creates a new {@link EncryptedBlobClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the blob,
     * pass {@code null} to use no customer provided key.
     * @return a {@link EncryptedBlobClient} with the specified {@code customerProvidedKey}.
     */
    @Override
    public EncryptedBlobClient getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey) {
        return new EncryptedBlobClient(encryptedBlobAsyncClient.getCustomerProvidedKeyAsyncClient(customerProvidedKey));
    }

    /**
     * Creates and opens an output stream to write data to the block blob.
     * <p>
     * Note: We recommend you call write with reasonably sized buffers, you can do so by wrapping the BlobOutputStream
     * obtained below with a {@link java.io.BufferedOutputStream}.
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream() {
        return getBlobOutputStream(false);
    }

    /**
     * Creates and opens an output stream to write data to the block blob.
     * <p>
     * Note: We recommend you call write with reasonably sized buffers, you can do so by wrapping the BlobOutputStream
     * obtained below with a {@link java.io.BufferedOutputStream}.
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @param overwrite Whether to overwrite, should data exist on the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(boolean overwrite) {
        BlobRequestConditions requestConditions = null;
        if (!overwrite) {
            if (exists()) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS));
            }
            requestConditions = new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return getBlobOutputStream(null, null, null, null, requestConditions);
    }

    /**
     * Creates and opens an output stream to write data to the block blob. If the blob already exists on the service, it
     * will be overwritten.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     * <p>
     * Note: We recommend you call write with reasonably sized buffers, you can do so by wrapping the BlobOutputStream
     * obtained below with a {@link java.io.BufferedOutputStream}.
     *
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier,
        BlobRequestConditions requestConditions) {
        return this.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
            .setTier(tier).setRequestConditions(requestConditions));
    }

    /**
     * Creates and opens an output stream to write data to the block blob. If the blob already exists on the service, it
     * will be overwritten.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     * <p>
     * Note: We recommend you call write with reasonably sized buffers, you can do so by wrapping the BlobOutputStream
     * obtained below with a {@link java.io.BufferedOutputStream}.
     *
     * @param options {@link BlockBlobOutputStreamOptions}
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(BlockBlobOutputStreamOptions options) {

        return BlobOutputStream.blockBlobOutputStream(encryptedBlobAsyncClient, options, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String -->
     * <pre>
     * try &#123;
     *     client.uploadFromFile&#40;filePath&#41;;
     *     System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;;
     * &#125; catch &#40;UncheckedIOException ex&#41; &#123;
     *     System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String -->
     *
     * @param filePath Path of the file to upload
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath) {
        uploadFromFile(filePath, false);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-boolean -->
     * <pre>
     * try &#123;
     *     boolean overwrite = false; &#47;&#47; Default value
     *     client.uploadFromFile&#40;filePath, overwrite&#41;;
     *     System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;;
     * &#125; catch &#40;UncheckedIOException ex&#41; &#123;
     *     System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-boolean -->
     *
     * @param filePath Path of the file to upload
     * @param overwrite Whether to overwrite should data already exist on the blob
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath, boolean overwrite) {
        if (!overwrite && exists()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS));
        }
        uploadFromFile(filePath, null, null, null, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration -->
     * <pre>
     * BlobHttpHeaders headers = new BlobHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = new HashMap&lt;&gt;&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;&#41;;
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * long blockSize = 100 * 1024 * 1024; &#47;&#47; 100 MB;
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;blockSize&#41;;
     *
     * try &#123;
     *     client.uploadFromFile&#40;filePath, parallelTransferOptions, headers, metadata, AccessTier.HOT,
     *         requestConditions, timeout&#41;;
     *     System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;;
     * &#125; catch &#40;UncheckedIOException ex&#41; &#123;
     *     System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration -->
     *
     * @param filePath Path of the file to upload
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     *        transfers parameter is ignored.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the uploaded blob
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions,
        Duration timeout) throws UncheckedIOException {
        this.uploadFromFileWithResponse(new BlobUploadFromFileOptions(filePath)
                .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
                .setTier(tier).setRequestConditions(requestConditions), timeout,
            null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFileWithResponse#BlobUploadFromFileOptions-Duration-Context -->
     * <pre>
     * BlobHttpHeaders headers = new BlobHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = new HashMap&lt;&gt;&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;&#41;;
     * Map&lt;String, String&gt; tags = new HashMap&lt;&gt;&#40;Collections.singletonMap&#40;&quot;tag&quot;, &quot;value&quot;&#41;&#41;;
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * long blockSize = 100 * 1024 * 1024; &#47;&#47; 100 MB;
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;blockSize&#41;;
     *
     * try &#123;
     *     client.uploadFromFileWithResponse&#40;new BlobUploadFromFileOptions&#40;filePath&#41;
     *         .setParallelTransferOptions&#40;parallelTransferOptions&#41;.setHeaders&#40;headers&#41;.setMetadata&#40;metadata&#41;
     *         .setTags&#40;tags&#41;.setTier&#40;AccessTier.HOT&#41;.setRequestConditions&#40;requestConditions&#41;, timeout,
     *         Context.NONE&#41;;
     *     System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;;
     * &#125; catch &#40;UncheckedIOException ex&#41; &#123;
     *     System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFileWithResponse#BlobUploadFromFileOptions-Duration-Context -->
     *
     * @param options {@link BlobUploadFromFileOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws UncheckedIOException If an I/O error occurs
     * @return Information about the uploaded block blob.
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockBlobItem> uploadFromFileWithResponse(BlobUploadFromFileOptions options,
        Duration timeout, Context context)
        throws UncheckedIOException {
        Mono<Response<BlockBlobItem>> upload =
            this.encryptedBlobAsyncClient.uploadFromFileWithResponse(options)
                .contextWrite(FluxUtil.toReactorContext(context));

        try {
            return StorageImplUtils.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public BlobProperties downloadToFile(String filePath) {
        return this.downloadToFile(filePath, false);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public BlobProperties downloadToFile(String filePath, boolean overwrite) {
        Set<OpenOption> openOptions = null;
        if (overwrite) {
            openOptions = new HashSet<>();
            openOptions.add(StandardOpenOption.CREATE);
            openOptions.add(StandardOpenOption.TRUNCATE_EXISTING); // If the file already exists and it is opened
            // for WRITE access, then its length is truncated to 0.
            openOptions.add(StandardOpenOption.READ);
            openOptions.add(StandardOpenOption.WRITE);
        }
        return this.downloadToFileWithResponse(filePath, null, null, null, null, false, openOptions, null, Context.NONE)
            .getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public Response<BlobProperties> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions downloadRetryOptions,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Duration timeout, Context context) {
        return this.downloadToFileWithResponse(filePath, range, parallelTransferOptions, downloadRetryOptions,
            requestConditions, rangeGetContentMd5, null, timeout, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public Response<BlobProperties> downloadToFileWithResponse(String filePath, BlobRange range,
        ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions downloadRetryOptions,
        BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Set<OpenOption> openOptions,
        Duration timeout, Context context) {
        final com.azure.storage.common.ParallelTransferOptions finalParallelTransferOptions =
            ModelHelper.wrapBlobOptions(ModelHelper.populateAndApplyDefaults(parallelTransferOptions));
        return this.downloadToFileWithResponse(new BlobDownloadToFileOptions(filePath).setRange(range)
            .setParallelTransferOptions(finalParallelTransferOptions)
            .setDownloadRetryOptions(downloadRetryOptions).setRequestConditions(requestConditions)
            .setRetrieveContentRangeMd5(rangeGetContentMd5).setOpenOptions(openOptions), timeout, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public Response<BlobProperties> downloadToFileWithResponse(BlobDownloadToFileOptions options, Duration timeout,
        Context context) {
        context = context == null ? Context.NONE : context;
        options.setRequestConditions(options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions());
        context = populateRequestConditionsAndContext(options.getRequestConditions(), timeout, context);
        return super.downloadToFileWithResponse(options, timeout, context);
    }

    @Override
    public BlobInputStream openInputStream() {
        return openInputStream((BlobRange) null, null);
    }

    @Override
    public BlobInputStream openInputStream(BlobRange range, BlobRequestConditions requestConditions) {
        return openInputStream(new BlobInputStreamOptions().setRange(range).setRequestConditions(requestConditions));
    }

    @Override
    public BlobInputStream openInputStream(BlobInputStreamOptions options) {
        return openInputStream(options, null);
    }

    @Override
    public BlobInputStream openInputStream(BlobInputStreamOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        options.setRequestConditions(options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions());
        context = populateRequestConditionsAndContext(options.getRequestConditions(), null, context);
        return super.openInputStream(options, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    @Deprecated
    @Override
    public void download(OutputStream stream) {
        downloadStream(stream);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public void downloadStream(OutputStream stream) {
        downloadStreamWithResponse(stream, null, null, null, false, null, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public BinaryData downloadContent() {
        return this.downloadContentWithResponse(null, null, null, null).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    @Deprecated
    @Override
    public BlobDownloadResponse downloadWithResponse(OutputStream stream, BlobRange range,
        DownloadRetryOptions options, BlobRequestConditions requestConditions, boolean getRangeContentMd5,
        Duration timeout, Context context) {
        return downloadStreamWithResponse(stream, range,
            options, requestConditions, getRangeContentMd5, timeout, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public BlobDownloadResponse downloadStreamWithResponse(OutputStream stream, BlobRange range,
        DownloadRetryOptions options, BlobRequestConditions requestConditions, boolean getRangeContentMd5,
        Duration timeout, Context context) {

        if (isRangeRequest(range)) {
            context = context == null ? Context.NONE : context;
            requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
            context = populateRequestConditionsAndContext(requestConditions, timeout, context);
        }

        return super.downloadStreamWithResponse(stream, range, options, requestConditions, getRangeContentMd5,
            timeout, context);
    }

    static boolean isRangeRequest(BlobRange range) {
        return range != null && (range.getOffset() != 0 || range.getCount() != null);
    }

    private Context populateRequestConditionsAndContext(BlobRequestConditions requestConditions,
        Duration timeout, Context context) {
        BlobProperties initialProperties =
            this.getPropertiesWithResponse(requestConditions, timeout, context).getValue();

        requestConditions.setIfMatch(initialProperties.getETag());
        if (initialProperties.getMetadata().get(ENCRYPTION_DATA_KEY) != null) {
            context = context.addData(ENCRYPTION_DATA_KEY, EncryptionData.getAndValidateEncryptionData(
                initialProperties.getMetadata().get(ENCRYPTION_DATA_KEY),
                encryptedBlobAsyncClient.isEncryptionRequired()));
        }

        return context;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    @Override
    public BlobDownloadContentResponse downloadContentWithResponse(
        DownloadRetryOptions options, BlobRequestConditions requestConditions, Duration timeout, Context context) {
        context = context == null ? Context.NONE : context;
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        context = populateRequestConditionsAndContext(requestConditions, timeout, context);
        return super.downloadContentWithResponse(options, requestConditions, timeout, context);
    }

    /**
     * Unsupported.
     */
    @Override
    public AppendBlobClient getAppendBlobClient() {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Cannot get an encrypted client as an append"
            + " blob client"));
    }

    /**
     * Unsupported.
     */
    @Override
    public BlockBlobClient getBlockBlobClient() {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Cannot get an encrypted client as a block"
            + " blob client"));
    }

    /**
     * Unsupported.
     */
    @Override
    public PageBlobClient getPageBlobClient() {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException("Cannot get an encrypted client as an page"
            + " blob client"));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public InputStream openQueryInputStream(String expression) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side."));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public Response<InputStream> openQueryInputStreamWithResponse(BlobQueryOptions queryOptions) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side."));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public void query(OutputStream stream, String expression) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side."));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public BlobQueryResponse queryWithResponse(BlobQueryOptions queryOptions,
        Duration timeout, Context context) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side."));
    }

}
