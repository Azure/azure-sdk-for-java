// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.UploadUtils;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a client that contains generic blob operations for Azure Storage Blobs. Operations allowed by
 * the client are uploading and downloading, copying a blob, retrieving and setting metadata, retrieving and setting
 * HTTP headers, and deleting and un-deleting a blob.
 *
 * <p>
 * This client is instantiated through {@link BlobClientBuilder} or retrieved via
 * {@link BlobContainerClient#getBlobClient(String) getBlobClient}.
 *
 * <p>
 * For operations on a specific blob type (i.e append, block, or page) use
 * {@link #getAppendBlobClient() getAppendBlobClient}, {@link #getBlockBlobClient() getBlockBlobClient}, or
 * {@link #getPageBlobClient() getPageBlobClient} to construct a client that allows blob specific operations.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = BlobClientBuilder.class)
public class BlobClient extends BlobClientBase {
    private static final ClientLogger LOGGER = new ClientLogger(BlobClient.class);

    /**
     * The block size to use if none is specified in parallel operations.
     */
    public static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = BlobAsyncClient.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;

    /**
     * The number of buffers to use if none is specied on the buffered upload method.
     */
    public static final int BLOB_DEFAULT_NUMBER_OF_BUFFERS = BlobAsyncClient.BLOB_DEFAULT_NUMBER_OF_BUFFERS;
    /**
     * If a blob  is known to be greater than 100MB, using a larger block size will trigger some server-side
     * optimizations. If the block size is not set and the size of the blob is known to be greater than 100MB, this
     * value will be used.
     */
    public static final int BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE = BlobAsyncClient.BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE;

    private final BlobAsyncClient client;

    private BlockBlobClient blockBlobClient;
    private AppendBlobClient appendBlobClient;
    private PageBlobClient pageBlobClient;

    /**
     * Protected constructor for use by {@link BlobClientBuilder}.
     * @param client the async blob client
     */
    protected BlobClient(BlobAsyncClient client) {
        super(client);
        this.client = client;
    }

    /**
     * Creates a new {@link BlobClient} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return A {@link BlobClient} used to interact with the specific snapshot.
     */
    @Override
    public BlobClient getSnapshotClient(String snapshot) {
        return new BlobClient(client.getSnapshotClient(snapshot));
    }

    /**
     * Creates a new {@link BlobClient} linked to the {@code version} of this blob resource.
     *
     * @param versionId the identifier for a specific version of this blob,
     * pass {@code null} to interact with the latest blob version.
     * @return A {@link BlobClient} used to interact with the specific version.
     */
    @Override
    public BlobClient getVersionClient(String versionId) {
        return new BlobClient(client.getVersionClient(versionId));
    }

    /**
     * Creates a new {@link BlobClient} with the specified {@code encryptionScope}.
     *
     * @param encryptionScope the encryption scope for the blob, pass {@code null} to use no encryption scope.
     * @return a {@link BlobClient} with the specified {@code encryptionScope}.
     */
    @Override
    public BlobClient getEncryptionScopeClient(String encryptionScope) {
        return new BlobClient(client.getEncryptionScopeAsyncClient(encryptionScope));
    }

    /**
     * Creates a new {@link BlobClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the blob,
     * pass {@code null} to use no customer provided key.
     * @return a {@link BlobClient} with the specified {@code customerProvidedKey}.
     */
    @Override
    public BlobClient getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey) {
        return new BlobClient(client.getCustomerProvidedKeyAsyncClient(customerProvidedKey));
    }

    /**
     * Creates a new {@link AppendBlobClient} associated with this blob.
     *
     * @return A {@link AppendBlobClient} associated with this blob.
     */
    public AppendBlobClient getAppendBlobClient() {
        if (appendBlobClient == null) {
            appendBlobClient = new SpecializedBlobClientBuilder()
                .blobClient(this)
                .buildAppendBlobClient();
        }
        return appendBlobClient;
    }

    /**
     * Creates a new {@link BlockBlobClient} associated with this blob.
     *
     * @return A {@link BlockBlobClient} associated with this blob.
     */
    public BlockBlobClient getBlockBlobClient() {
        if (blockBlobClient == null) {
            blockBlobClient = new SpecializedBlobClientBuilder()
                .blobClient(this)
                .buildBlockBlobClient();
        }
        return blockBlobClient;
    }

    /**
     * Creates a new {@link PageBlobClient} associated with this blob.
     *
     * @return A {@link PageBlobClient} associated with this blob.
     */
    public PageBlobClient getPageBlobClient() {
        if (pageBlobClient == null) {
            pageBlobClient = new SpecializedBlobClientBuilder()
                .blobClient(this)
                .buildPageBlobClient();
        }
        return pageBlobClient;
    }

    /**
     * Creates a new blob. By default this method will not overwrite an existing blob.
     *
     * @param data The data to write to the blob. The data must be markable. This is in order to support retries. If
     * the data is not markable, consider opening a {@link com.azure.storage.blob.specialized.BlobOutputStream} and
     * writing to the returned stream. Alternatively, consider wrapping your data source in a
     * {@link java.io.BufferedInputStream} to add mark support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upload(InputStream data, long length) {
        upload(data, length, false);
    }

    /**
     * Creates a new blob, or updates the content of an existing blob.
     *
     * @param data The data to write to the blob. The data must be markable. This is in order to support retries. If
     * the data is not markable, consider opening a {@link com.azure.storage.blob.specialized.BlobOutputStream} and
     * writing to the returned stream. Alternatively, consider wrapping your data source in a
     * {@link java.io.BufferedInputStream} to add mark support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @param overwrite Whether or not to overwrite, should data exist on the blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upload(InputStream data, long length, boolean overwrite) {
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions();
        if (!overwrite) {
            blobRequestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        uploadWithResponse(data, length, null, null, null, null, blobRequestConditions, null, Context.NONE);
    }

    /**
     * Creates a new blob. By default this method will not overwrite an existing blob.
     *
     * @param data The data to write to the blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upload(BinaryData data) {
        upload(data, false);
    }

    /**
     * Creates a new blob, or updates the content of an existing blob.
     *
     * @param data The data to write to the blob.
     * @param overwrite Whether or not to overwrite, should data exist on the blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upload(BinaryData data, boolean overwrite) {
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions();
        if (!overwrite) {
            blobRequestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        uploadWithResponse(new BlobParallelUploadOptions(data).setRequestConditions(blobRequestConditions),
            null, Context.NONE);
    }

    /**
     * Creates a new blob, or updates the content of an existing blob.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * @param data The data to write to the blob. The data must be markable. This is in order to support retries. If
     * the data is not markable, consider opening a {@link com.azure.storage.blob.specialized.BlobOutputStream} and
     * writing to the returned stream. Alternatively, consider wrapping your data source in a
     * {@link java.io.BufferedInputStream} to add mark support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @deprecated See {@link #uploadWithResponse(BlobParallelUploadOptions, Duration, Context)} instead
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    @Deprecated
    public void uploadWithResponse(InputStream data, long length, ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions,
        Duration timeout, Context context) {
        this.uploadWithResponse(new BlobParallelUploadOptions(data, length)
            .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata).setTier(tier)
            .setRequestConditions(requestConditions), timeout, context);
    }

    /**
     * Creates a new blob, or updates the content of an existing blob.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     * @param options {@link BlobParallelUploadOptions}
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Information about the uploaded block blob.
     *
     * @deprecated Use {@link BlobClient#uploadWithResponse(BlobParallelUploadOptions, Duration, Context)}
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockBlobItem> uploadWithResponse(BlobParallelUploadOptions options, Context context) {
        Objects.requireNonNull(options);
        return this.uploadWithResponse(options, options.getTimeout(), context);
    }

    /**
     * Creates a new blob, or updates the content of an existing blob.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     * @param options {@link BlobParallelUploadOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Information about the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockBlobItem> uploadWithResponse(BlobParallelUploadOptions options, Duration timeout,
        Context context) {
        Objects.requireNonNull(options);
        Mono<Response<BlockBlobItem>> upload = client.uploadWithResponse(options)
            .contextWrite(FluxUtil.toReactorContext(context));

        try {
            return StorageImplUtils.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Creates a new block blob. By default this method will not overwrite an existing blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobClient.uploadFromFile#String -->
     * <pre>
     * try &#123;
     *     client.uploadFromFile&#40;filePath&#41;;
     *     System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;;
     * &#125; catch &#40;UncheckedIOException ex&#41; &#123;
     *     System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobClient.uploadFromFile#String -->
     *
     * @param filePath Path of the file to upload
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath) {
        uploadFromFile(filePath, false);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobClient.uploadFromFile#String-boolean -->
     * <pre>
     * try &#123;
     *     boolean overwrite = false;
     *     client.uploadFromFile&#40;filePath, overwrite&#41;;
     *     System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;;
     * &#125; catch &#40;UncheckedIOException ex&#41; &#123;
     *     System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobClient.uploadFromFile#String-boolean -->
     *
     * @param filePath Path of the file to upload
     * @param overwrite Whether or not to overwrite, should the blob already exist
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath, boolean overwrite) {
        BlobRequestConditions requestConditions = null;

        if (!overwrite) {
            // Note we only want to make the exists call if we will be uploading in stages. Otherwise it is superfluous.
            //
            // Default behavior is to use uploading in chunks when the file size is greater than 256 MB.
            if (UploadUtils.shouldUploadInChunks(filePath, ModelHelper.BLOB_DEFAULT_MAX_SINGLE_UPLOAD_SIZE, LOGGER)
                && exists()) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS));
            }
            requestConditions = new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        uploadFromFile(filePath, null, null, null, null, requestConditions, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration -->
     * <pre>
     * BlobHttpHeaders headers = new BlobHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Long blockSize = 100L * 1024L * 1024L; &#47;&#47; 100 MB;
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;blockSize&#41;;
     *
     * try &#123;
     *     client.uploadFromFile&#40;filePath, parallelTransferOptions, headers, metadata,
     *         AccessTier.HOT, requestConditions, timeout&#41;;
     *     System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;;
     * &#125; catch &#40;UncheckedIOException ex&#41; &#123;
     *     System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration -->
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions,
        Duration timeout) {
        this.uploadFromFileWithResponse(new BlobUploadFromFileOptions(filePath)
            .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
            .setTier(tier).setRequestConditions(requestConditions), timeout, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobClient.uploadFromFileWithResponse#BlobUploadFromFileOptions-Duration-Context -->
     * <pre>
     * BlobHttpHeaders headers = new BlobHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * Map&lt;String, String&gt; tags = Collections.singletonMap&#40;&quot;tag&quot;, &quot;value&quot;&#41;;
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Long blockSize = 100 * 1024 * 1024L; &#47;&#47; 100 MB;
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;blockSize&#41;;
     *
     * try &#123;
     *     client.uploadFromFileWithResponse&#40;new BlobUploadFromFileOptions&#40;filePath&#41;
     *         .setParallelTransferOptions&#40;parallelTransferOptions&#41;.setHeaders&#40;headers&#41;.setMetadata&#40;metadata&#41;
     *         .setTags&#40;tags&#41;.setTier&#40;AccessTier.HOT&#41;.setRequestConditions&#40;requestConditions&#41;, timeout,
     *         new Context&#40;key2, value2&#41;&#41;;
     *     System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;;
     * &#125; catch &#40;UncheckedIOException ex&#41; &#123;
     *     System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, ex.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobClient.uploadFromFileWithResponse#BlobUploadFromFileOptions-Duration-Context -->
     *
     * @param options {@link BlobUploadFromFileOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Information about the uploaded block blob.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockBlobItem> uploadFromFileWithResponse(BlobUploadFromFileOptions options, Duration timeout,
        Context context) {
        Mono<Response<BlockBlobItem>> upload =
            this.client.uploadFromFileWithResponse(options)
                .contextWrite(FluxUtil.toReactorContext(context));

        try {
            return StorageImplUtils.blockWithOptionalTimeout(upload, timeout);
        } catch (UncheckedIOException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }
}
