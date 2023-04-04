// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.implementation.models.BlockBlobsUploadHeaders;
import com.azure.storage.blob.implementation.models.BlockBlobsPutBlobFromUrlHeaders;
import com.azure.storage.blob.implementation.models.BlockBlobsStageBlockHeaders;
import com.azure.storage.blob.implementation.models.BlockBlobsGetBlockListHeaders;
import com.azure.storage.blob.implementation.models.BlockBlobsStageBlockFromURLHeaders;
import com.azure.storage.blob.implementation.models.BlockBlobsCommitBlockListHeaders;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlockLookupList;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobUploadFromUrlOptions;
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.storage.blob.options.BlockBlobListBlocksOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockFromUrlOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockOptions;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;
import static com.azure.storage.common.implementation.StorageImplUtils.enableSyncRestProxy;
import static com.azure.storage.common.implementation.StorageImplUtils.THREAD_POOL;
import static com.azure.storage.common.implementation.StorageImplUtils.executeOperation;


/**
 * Client to a block blob. It may only be instantiated through a {@link SpecializedBlobClientBuilder} or via the method
 * {@link BlobClient#getBlockBlobClient()}. This class does not hold any state about a particular blob, but is instead
 * a convenient way of sending appropriate requests to the resource on the service.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure Docs</a> for more information.
 */
@ServiceClient(builder = SpecializedBlobClientBuilder.class)
public final class BlockBlobClient extends BlobClientBase {
    private static final ClientLogger LOGGER = new ClientLogger(BlockBlobClient.class);
    private final BlockBlobAsyncClient asyncClient;

    //TODO: these variables should bubble to BlobClientBase once all Storage clients support sync stack.
    /**
     * Backing REST client for the blob client.
     */
    final AzureBlobStorageImpl azureBlobStorage;

    private final String snapshot;
    private final String versionId;
    private final CpkInfo customerProvidedKey;

    /**
     * Encryption scope of the blob.
     */
    final EncryptionScope encryptionScope;

    /**
     * Storage account name that contains the blob.
     */
    final String accountName;

    /**
     * Container name that contains the blob.
     */
    final String containerName;

    /**
     * Name of the blob.
     */
    final String blobName;

    /**
     * Storage REST API version used in requests to the Storage service.
     */
    final BlobServiceVersion serviceVersion;


    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     * @deprecated Use {@link #MAX_STAGE_BLOCK_BYTES_LONG}.
     */
    @Deprecated
    public static final int MAX_UPLOAD_BLOB_BYTES = BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES;
    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    public static final long MAX_UPLOAD_BLOB_BYTES_LONG = BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES_LONG;
    /**
     * Indicates the maximum number of bytes that can be sent in a call to stageBlock.
     * @deprecated Use {@link #MAX_STAGE_BLOCK_BYTES_LONG}
     */
    @Deprecated
    public static final int MAX_STAGE_BLOCK_BYTES = BlockBlobAsyncClient.MAX_STAGE_BLOCK_BYTES;
    /**
     * Indicates the maximum number of bytes that can be sent in a call to stageBlock.
     */
    public static final long MAX_STAGE_BLOCK_BYTES_LONG = BlockBlobAsyncClient.MAX_STAGE_BLOCK_BYTES_LONG;
    /**
     * Indicates the maximum number of blocks allowed in a block blob.
     */
    public static final int MAX_BLOCKS = BlockBlobAsyncClient.MAX_BLOCKS;

    /**
     * Protected constructor for use by {@link SpecializedBlobClientBuilder}.
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
    BlockBlobClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion,
                             String accountName, String containerName, String blobName, String snapshot,
                             CpkInfo customerProvidedKey, EncryptionScope encryptionScope, String versionId,
                             BlockBlobAsyncClient blockBlobAsyncClient) {
        super(blockBlobAsyncClient);
        if (snapshot != null && versionId != null) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'snapshot' and 'versionId' cannot be used at the same time."));
        }
        this.azureBlobStorage = new AzureBlobStorageImplBuilder()
            .pipeline(pipeline)
            .url(url)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.serviceVersion = serviceVersion;

        this.accountName = accountName;
        this.containerName = containerName;
        this.blobName = Utility.urlDecode(blobName);
        this.snapshot = snapshot;
        this.customerProvidedKey = customerProvidedKey;
        this.encryptionScope = encryptionScope;
        this.versionId = versionId;
        this.asyncClient = blockBlobAsyncClient;
        /* Check to make sure the uri is valid. We don't want the error to occur later in the generated layer
           when the sas token has already been applied. */
        try {
            URI.create(getBlobUrl());
        } catch (IllegalArgumentException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    /**
     * Creates a new {@link BlockBlobClient} with the specified {@code encryptionScope}.
     *
     * @param encryptionScope the encryption scope for the blob, pass {@code null} to use no encryption scope.
     * @return a {@link BlockBlobClient} with the specified {@code encryptionScope}.
     */
    @Override
    public BlockBlobClient getEncryptionScopeClient(String encryptionScope) {
        EncryptionScope finalEncryptionScope = null;
        if (encryptionScope != null) {
            finalEncryptionScope = new EncryptionScope().setEncryptionScope(encryptionScope);
        }
        return new BlockBlobClient(azureBlobStorage.getHttpPipeline(), azureBlobStorage.getUrl(), serviceVersion,
            accountName, containerName, blobName, snapshot, customerProvidedKey, finalEncryptionScope, versionId,
            asyncClient);
    }

    /**
     * Creates a new {@link BlockBlobClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the blob,
     * pass {@code null} to use no customer provided key.
     * @return a {@link BlockBlobClient} with the specified {@code customerProvidedKey}.
     */
    @Override
    public BlockBlobClient getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey) {
        CpkInfo finalCustomerProvidedKey = null;
        if (customerProvidedKey != null) {
            finalCustomerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }
        return new BlockBlobClient(azureBlobStorage.getHttpPipeline(), azureBlobStorage.getUrl(), serviceVersion,
            accountName, containerName, blobName, snapshot, finalCustomerProvidedKey, encryptionScope,
            versionId, asyncClient);
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
        return getBlobOutputStream(requestConditions);
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
     * @param requestConditions A {@link BlobRequestConditions} object that represents the access conditions for the
     * blob.
     *
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(BlobRequestConditions requestConditions) {
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
            .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata).setTier(tier)
            .setRequestConditions(requestConditions));
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
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * @throws BlobStorageException If a storage service error occurred.
     */
    public BlobOutputStream getBlobOutputStream(BlockBlobOutputStreamOptions options) {
        BlobAsyncClient blobClient = prepareBuilder().buildAsyncClient();

        return BlobOutputStream.blockBlobOutputStream(blobClient, options, null);
    }

    private BlobClientBuilder prepareBuilder() {
        BlobClientBuilder builder = new BlobClientBuilder()
            .pipeline(getHttpPipeline())
            .endpoint(getBlobUrl())
            .snapshot(getSnapshotId())
            .serviceVersion(getServiceVersion());

        CpkInfo cpk = getCustomerProvidedKey();
        if (cpk != null) {
            builder.customerProvidedKey(new CustomerProvidedKey(cpk.getEncryptionKey()));
        }

        return builder;
    }

    /**
     * Creates a new block blob. By default, this method will not overwrite an existing blob. Updating an existing block
     * blob overwrites any existing metadata on the blob. Partial updates are not supported with PutBlob; the content
     * of the existing blob is overwritten with the new content. To perform a partial update of a block blob's, use
     * PutBlock and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.upload#InputStream-long -->
     * <pre>
     * System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *     Base64.getEncoder&#40;&#41;.encodeToString&#40;client.upload&#40;data, length&#41;.getContentMd5&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.upload#InputStream-long -->
     *
     * @param data The data to write to the blob. The data must be markable. This is in order to support retries. If
     * the data is not markable, consider using {@link #getBlobOutputStream()} and writing to the returned OutputStream.
     * Alternatively, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add mark support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @return The information of the uploaded block blob.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlockBlobItem upload(InputStream data, long length) {
        return upload(data, length, false);
    }

    /**
     * Creates a new block blob. By default, this method will not overwrite an existing blob. Updating an existing block
     * blob overwrites any existing metadata on the blob. Partial updates are not supported with PutBlob; the content
     * of the existing blob is overwritten with the new content. To perform a partial update of a block blob's, use
     * PutBlock and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.upload#BinaryData -->
     * <pre>
     * BinaryData binaryData = BinaryData.fromStream&#40;data, length&#41;;
     * System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *     Base64.getEncoder&#40;&#41;.encodeToString&#40;client.upload&#40;binaryData&#41;.getContentMd5&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.upload#BinaryData -->
     *
     * @param data The data to write to the block. Note that this {@code BinaryData} must have defined length
     * and must be replayable if retries are enabled (the default), see {@link BinaryData#isReplayable()}.
     * @return The information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlockBlobItem upload(BinaryData data) {
        return upload(data, false);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing metadata on the blob. Partial updates are not supported with PutBlob; the content of the
     * existing blob is overwritten with the new content. To perform a partial update of a block blob's, use PutBlock
     * and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.upload#InputStream-long-boolean -->
     * <pre>
     * boolean overwrite = false;
     * System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *     Base64.getEncoder&#40;&#41;.encodeToString&#40;client.upload&#40;data, length, overwrite&#41;.getContentMd5&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.upload#InputStream-long-boolean -->
     *
     * @param data The data to write to the blob. The data must be markable. This is in order to support retries. If
     * the data is not markable, consider using {@link #getBlobOutputStream()} and writing to the returned OutputStream.
     * Alternatively, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add mark support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @param overwrite Whether to overwrite, should data exist on the blob.
     * @return The information of the uploaded block blob.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlockBlobItem upload(InputStream data, long length, boolean overwrite) {
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions();
        if (!overwrite) {
            blobRequestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return uploadWithResponse(data, length, null, null, null, null, blobRequestConditions, null, Context.NONE)
            .getValue();
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing metadata on the blob. Partial updates are not supported with PutBlob; the content of the
     * existing blob is overwritten with the new content. To perform a partial update of a block blob's, use PutBlock
     * and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.upload#BinaryData-boolean -->
     * <pre>
     * boolean overwrite = false;
     * BinaryData binaryData = BinaryData.fromStream&#40;data, length&#41;;
     * System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *     Base64.getEncoder&#40;&#41;.encodeToString&#40;client.upload&#40;binaryData, overwrite&#41;.getContentMd5&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.upload#BinaryData-boolean -->
     *
     * @param data The data to write to the block. Note that this {@code BinaryData} must have defined length
     * and must be replayable if retries are enabled (the default), see {@link BinaryData#isReplayable()}.
     * @param overwrite Whether to overwrite, should data exist on the blob.
     * @return The information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlockBlobItem upload(BinaryData data, boolean overwrite) {
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions();
        if (!overwrite) {
            blobRequestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return uploadWithResponse(
            new BlockBlobSimpleUploadOptions(data)
                .setRequestConditions(blobRequestConditions),
            null, Context.NONE)
            .getValue();
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing metadata on the blob. Partial updates are not supported with PutBlob; the content of the
     * existing blob is overwritten with the new content. To perform a partial update of a block blob's, use PutBlock
     * and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.uploadWithResponse#InputStream-long-BlobHttpHeaders-Map-AccessTier-byte-BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobHttpHeaders headers = new BlobHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     *
     * byte[] md5 = MessageDigest.getInstance&#40;&quot;MD5&quot;&#41;.digest&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     *
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;, Base64.getEncoder&#40;&#41;
     *     .encodeToString&#40;client.uploadWithResponse&#40;data, length, headers, metadata, AccessTier.HOT, md5,
     *         requestConditions, timeout, context&#41;
     *         .getValue&#40;&#41;
     *         .getContentMd5&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.uploadWithResponse#InputStream-long-BlobHttpHeaders-Map-AccessTier-byte-BlobRequestConditions-Duration-Context -->
     *
     * @param data The data to write to the blob. The data must be markable. This is in order to support retries. If
     * the data is not markable, consider using {@link #getBlobOutputStream()} and writing to the returned OutputStream.
     * Alternatively, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add mark support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param contentMd5 An MD5 hash of the block content. This hash is used to verify the integrity of the block during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The information of the uploaded block blob.
     *
     * @throws UnexpectedLengthException when the length of data does not match the input {@code length}.
     * @throws NullPointerException if the input data is null.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockBlobItem> uploadWithResponse(InputStream data, long length, BlobHttpHeaders headers,
        Map<String, String> metadata, AccessTier tier, byte[] contentMd5, BlobRequestConditions requestConditions,
        Duration timeout, Context context) {
        return this.uploadWithResponse(new BlockBlobSimpleUploadOptions(data, length).setHeaders(headers)
            .setMetadata(metadata).setTier(tier).setContentMd5(contentMd5).setRequestConditions(requestConditions),
            timeout, context);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing metadata on the blob. Partial updates are not supported with PutBlob; the content of the
     * existing blob is overwritten with the new content. To perform a partial update of a block blob's, use PutBlock
     * and PutBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob">Azure Docs</a>.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.uploadWithResponse#BlockBlobSimpleUploadOptions-Duration-Context -->
     * <pre>
     * BlobHttpHeaders headers = new BlobHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * Map&lt;String, String&gt; tags = Collections.singletonMap&#40;&quot;tag&quot;, &quot;value&quot;&#41;;
     *
     * byte[] md5 = MessageDigest.getInstance&#40;&quot;MD5&quot;&#41;.digest&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     *
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;, Base64.getEncoder&#40;&#41;
     *     .encodeToString&#40;client.uploadWithResponse&#40;new BlockBlobSimpleUploadOptions&#40;data, length&#41;
     *         .setHeaders&#40;headers&#41;.setMetadata&#40;metadata&#41;.setTags&#40;tags&#41;.setTier&#40;AccessTier.HOT&#41;.setContentMd5&#40;md5&#41;
     *         .setRequestConditions&#40;requestConditions&#41;, timeout, context&#41;
     *         .getValue&#40;&#41;
     *         .getContentMd5&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.uploadWithResponse#BlockBlobSimpleUploadOptions-Duration-Context -->
     *
     * @param options {@link BlockBlobSimpleUploadOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The information of the uploaded block blob.
     *
     * @throws UnexpectedLengthException when the length of data does not match the input {@code length}.
     * @throws NullPointerException if the input data is null.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockBlobItem> uploadWithResponse(BlockBlobSimpleUploadOptions options, Duration timeout,
        Context context) {
        StorageImplUtils.assertNotNull("options", options);

        Supplier<Response<BlockBlobItem>> operation = () ->
            uploadWithResponseSync(options, enableSyncRestProxy(context));

        try {
            return timeout != null
                ? THREAD_POOL.submit(() -> operation.get()).get(timeout.toMillis(), TimeUnit.MILLISECONDS)
                : operation.get();
        } catch (UncheckedIOException e) {
            throw LOGGER.logExceptionAsError(e);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    Response<BlockBlobItem> uploadWithResponseSync(BlockBlobSimpleUploadOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        BlobImmutabilityPolicy immutabilityPolicy = options.getImmutabilityPolicy() == null
            ? new BlobImmutabilityPolicy() : options.getImmutabilityPolicy();
        BinaryData data = options.getData();
        if (data == null) {
            if (options.getDataStream() != null) {
                data = BinaryData.fromStream(options.getDataStream());
            } else {
                data = BinaryData.fromFlux(options.getDataFlux()).block();
            }
        }
        ResponseBase<BlockBlobsUploadHeaders, Void> response = this.azureBlobStorage.getBlockBlobs()
            .uploadWithResponse(containerName, blobName,
                options.getLength(), data, null, options.getContentMd5(), options.getMetadata(),
                requestConditions.getLeaseId(), options.getTier(), requestConditions.getIfModifiedSince(),
                requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
                requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null,
                tagsToString(options.getTags()), immutabilityPolicy.getExpiryTime(), immutabilityPolicy.getPolicyMode(),
                options.isLegalHold(), null, options.getHeaders(), getCustomerProvidedKey(),
                encryptionScope, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE));

        BlockBlobsUploadHeaders hd = response.getDeserializedHeaders();
        BlockBlobItem item = new BlockBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
            hd.isXMsRequestServerEncrypted(), hd.getXMsEncryptionKeySha256(), hd.getXMsEncryptionScope(),
            hd.getXMsVersionId());
        return new SimpleResponse<>(response, item);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with PutBlobFromUrl; the content of the existing blob is overwritten with the new content.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob-from-url">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.uploadFromUrl#String -->
     * <pre>
     * System.out.printf&#40;&quot;Uploaded BlockBlob from URL, MD5 is %s%n&quot;,
     *     Base64.getEncoder&#40;&#41;.encodeToString&#40;client.uploadFromUrl&#40;sourceUrl&#41;.getContentMd5&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.uploadFromUrl#String -->
     *
     * @param sourceUrl The source URL to upload from.
     * @return The information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlockBlobItem uploadFromUrl(String sourceUrl) {
        return uploadFromUrl(sourceUrl, false);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with PutBlobFromUrl; the content of the existing blob is overwritten with the new content.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob-from-url">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.uploadFromUrl#String-boolean -->
     * <pre>
     * boolean overwrite = false;
     * System.out.printf&#40;&quot;Uploaded BlockBlob from URL, MD5 is %s%n&quot;,
     *     Base64.getEncoder&#40;&#41;.encodeToString&#40;client.uploadFromUrl&#40;sourceUrl, overwrite&#41;.getContentMd5&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.uploadFromUrl#String-boolean -->
     *
     * @param sourceUrl The source URL to upload from.
     * @param overwrite Whether to overwrite, should data exist on the blob.
     * @return The information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlockBlobItem uploadFromUrl(String sourceUrl, boolean overwrite) {
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions();
        if (!overwrite) {
            blobRequestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return uploadFromUrlWithResponse(
            new BlobUploadFromUrlOptions(sourceUrl).setDestinationRequestConditions(blobRequestConditions),
            null, Context.NONE)
            .getValue();
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with PutBlobFromUrl; the content of the existing blob is overwritten with the new content.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-blob-from-url">Azure Docs</a>.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.uploadFromUrlWithResponse#BlobUploadFromUrlOptions-Duration-Context -->
     * <pre>
     * BlobHttpHeaders headers = new BlobHttpHeaders&#40;&#41;
     *     .setContentMd5&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     *
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * Map&lt;String, String&gt; tags = Collections.singletonMap&#40;&quot;tag&quot;, &quot;value&quot;&#41;;
     *
     * byte[] md5 = MessageDigest.getInstance&#40;&quot;MD5&quot;&#41;.digest&#40;&quot;data&quot;.getBytes&#40;StandardCharsets.UTF_8&#41;&#41;;
     *
     * BlobRequestConditions requestConditions = new BlobRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;, Base64.getEncoder&#40;&#41;
     *     .encodeToString&#40;client.uploadFromUrlWithResponse&#40;new BlobUploadFromUrlOptions&#40;sourceUrl&#41;
     *         .setHeaders&#40;headers&#41;.setTags&#40;tags&#41;.setTier&#40;AccessTier.HOT&#41;.setContentMd5&#40;md5&#41;
     *         .setDestinationRequestConditions&#40;requestConditions&#41;, timeout, context&#41;
     *         .getValue&#40;&#41;
     *         .getContentMd5&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.uploadFromUrlWithResponse#BlobUploadFromUrlOptions-Duration-Context -->
     *
     * @param options {@link BlobUploadFromUrlOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockBlobItem> uploadFromUrlWithResponse(BlobUploadFromUrlOptions options, Duration timeout,
                                                             Context context) {
        StorageImplUtils.assertNotNull("options", options);
        return StorageImplUtils.executeOperation(() -> uploadFromUrlWithResponseSync(options, enableSyncRestProxy(context)),
            timeout);
    }

    Response<BlockBlobItem> uploadFromUrlWithResponseSync(BlobUploadFromUrlOptions options, Context context) {
        BlobRequestConditions destinationRequestConditions =
            options.getDestinationRequestConditions() == null ? new BlobRequestConditions()
                : options.getDestinationRequestConditions();
        BlobRequestConditions sourceRequestConditions =
            options.getSourceRequestConditions() == null ? new BlobRequestConditions()
                : options.getSourceRequestConditions();
        context = context == null ? Context.NONE : context;
        String sourceAuth = options.getSourceAuthorization() == null
            ? null : options.getSourceAuthorization().toString();

        try {
            new URL(options.getSourceUrl());
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url.", ex));
        }

        // TODO (kasobol-msft) add metadata back (https://github.com/Azure/azure-sdk-for-net/issues/15969)
        ResponseBase<BlockBlobsPutBlobFromUrlHeaders, Void> response = this.azureBlobStorage.getBlockBlobs().putBlobFromUrlWithResponse(
            containerName, blobName, 0, options.getSourceUrl(), null, null, null,
            destinationRequestConditions.getLeaseId(), options.getTier(),
            destinationRequestConditions.getIfModifiedSince(), destinationRequestConditions.getIfUnmodifiedSince(),
            destinationRequestConditions.getIfMatch(), destinationRequestConditions.getIfNoneMatch(),
            destinationRequestConditions.getTagsConditions(),
            sourceRequestConditions.getIfModifiedSince(), sourceRequestConditions.getIfUnmodifiedSince(),
            sourceRequestConditions.getIfMatch(), sourceRequestConditions.getIfNoneMatch(),
            sourceRequestConditions.getTagsConditions(),
            null, options.getContentMd5(), tagsToString(options.getTags()),
            options.isCopySourceBlobProperties(), sourceAuth, options.getCopySourceTagsMode(), options.getHeaders(),
            getCustomerProvidedKey(), encryptionScope,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE));

        BlockBlobsPutBlobFromUrlHeaders hd = response.getDeserializedHeaders();
        BlockBlobItem item = new BlockBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
            hd.isXMsRequestServerEncrypted(), hd.getXMsEncryptionKeySha256(), hd.getXMsEncryptionScope(),
            hd.getXMsVersionId());
        return new SimpleResponse<>(response, item);
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.stageBlock#String-InputStream-long -->
     * <pre>
     * client.stageBlock&#40;base64BlockId, data, length&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.stageBlock#String-InputStream-long -->
     *
     * @param base64BlockId A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param data The data to write to the block. The data must be markable. This is in order to support retries. If
     * the data is not markable, consider using {@link #getBlobOutputStream()} and writing to the returned OutputStream.
     * Alternatively, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add mark support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void stageBlock(String base64BlockId, InputStream data, long length) {
        stageBlockWithResponse(base64BlockId, data, length, null, null, null, Context.NONE);
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.stageBlock#String-BinaryData -->
     * <pre>
     * BinaryData binaryData = BinaryData.fromStream&#40;data, length&#41;;
     * client.stageBlock&#40;base64BlockId, binaryData&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.stageBlock#String-BinaryData -->
     *
     * @param base64BlockId A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param data The data to write to the block. Note that this {@code BinaryData} must have defined length
     * and must be replayable if retries are enabled (the default), see {@link BinaryData#isReplayable()}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void stageBlock(String base64BlockId, BinaryData data) {
        stageBlockWithResponse(new BlockBlobStageBlockOptions(base64BlockId, data), null, Context.NONE);
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.stageBlockWithResponse#String-InputStream-long-byte-String-Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;;
     * System.out.printf&#40;&quot;Staging block completed with status %d%n&quot;,
     *     client.stageBlockWithResponse&#40;base64BlockId, data, length, md5, leaseId, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.stageBlockWithResponse#String-InputStream-long-byte-String-Duration-Context -->
     *
     * @param base64BlockId A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param data The data to write to the block. The data must be markable. This is in order to support retries. If
     * the data is not markable, consider using {@link #getBlobOutputStream()} and writing to the returned OutputStream.
     * Alternatively, consider wrapping your data source in a {@link java.io.BufferedInputStream} to add mark support.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     * @param contentMd5 An MD5 hash of the block content. This hash is used to verify the integrity of the block during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers
     *
     * @throws UnexpectedLengthException when the length of data does not match the input {@code length}.
     * @throws NullPointerException if the input data is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stageBlockWithResponse(String base64BlockId, InputStream data, long length, byte[] contentMd5,
        String leaseId, Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("data", data);
        return executeOperation(() -> stageBlockWithResponseSync(base64BlockId,
            BinaryData.fromStream(data, length), contentMd5, leaseId, enableSyncRestProxy(context)), timeout);
    }

    Response<Void> stageBlockWithResponseSync(String base64BlockId, BinaryData data,
                                              byte[] contentMd5, String leaseId, Context context) {
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(data.getLength(), "data must have defined length");
        context = context == null ? Context.NONE : context;
        ResponseBase<BlockBlobsStageBlockHeaders, Void> response = this.azureBlobStorage.getBlockBlobs().stageBlockWithResponse(containerName, blobName,
            base64BlockId, data.getLength(), data, contentMd5, null, null,
            leaseId, null, getCustomerProvidedKey(),
            encryptionScope, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE));
        return new SimpleResponse<>(response, null);
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.stageBlockWithResponse#BlockBlobStageBlockOptions-Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;;
     * BinaryData binaryData = BinaryData.fromStream&#40;data, length&#41;;
     * BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions&#40;base64BlockId, binaryData&#41;
     *     .setContentMd5&#40;md5&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * System.out.printf&#40;&quot;Staging block completed with status %d%n&quot;,
     *     client.stageBlockWithResponse&#40;options, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.stageBlockWithResponse#BlockBlobStageBlockOptions-Duration-Context -->
     *
     * @param options {@link BlockBlobStageBlockOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers
     *
     * @throws UnexpectedLengthException when the length of data does not match the input {@code length}.
     * @throws NullPointerException if the input options is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stageBlockWithResponse(BlockBlobStageBlockOptions options, Duration timeout, Context context) {
        Objects.requireNonNull(options, "options must not be null");

        return executeOperation(() -> stageBlockWithResponseSync(
            options.getBase64BlockId(), options.getData(), options.getContentMd5(), options.getLeaseId(),
                enableSyncRestProxy(context)), timeout);
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-from-url">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromUrl#String-String-BlobRange -->
     * <pre>
     * client.stageBlockFromUrl&#40;base64BlockId, sourceUrl, new BlobRange&#40;offset, count&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromUrl#String-String-BlobRange -->
     *
     * @param base64BlockId A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     * @throws IllegalArgumentException If {@code sourceUrl} is a malformed {@link URL}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void stageBlockFromUrl(String base64BlockId, String sourceUrl, BlobRange sourceRange) {
        stageBlockFromUrlWithResponse(base64BlockId, sourceUrl, sourceRange, null, null, null, null, Context.NONE);
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-from-url">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromUrlWithResponse#String-String-BlobRange-byte-String-BlobRequestConditions-Duration-Context -->
     * <pre>
     * BlobRequestConditions sourceRequestConditions = new BlobRequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Staging block from URL completed with status %d%n&quot;,
     *     client.stageBlockFromUrlWithResponse&#40;base64BlockId, sourceUrl, new BlobRange&#40;offset, count&#41;, null,
     *         leaseId, sourceRequestConditions, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromUrlWithResponse#String-String-BlobRange-byte-String-BlobRequestConditions-Duration-Context -->
     *
     * @param base64BlockId A Base64 encoded {@code String} that specifies the ID for this block. Note that all block
     * ids for a given blob must be the same length.
     * @param sourceUrl The url to the blob that will be the source of the copy.  A source blob in the same storage
     * account can be authenticated via Shared Key. However, if the source is a blob in another account, the source blob
     * must either be public or must be authenticated via a shared access signature. If the source blob is public, no
     * authentication is required to perform the operation.
     * @param sourceRange {@link BlobRange}
     * @param sourceContentMd5 An MD5 hash of the block content. This hash is used to verify the integrity of the block
     * during transport. When this header is specified, the storage service compares the hash of the content that has
     * arrived with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not
     * match, the operation will fail.
     * @param leaseId The lease ID that the active lease on the blob must match.
     * @param sourceRequestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers
     * @throws IllegalArgumentException If {@code sourceUrl} is a malformed {@link URL}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stageBlockFromUrlWithResponse(String base64BlockId, String sourceUrl, BlobRange sourceRange,
            byte[] sourceContentMd5, String leaseId, BlobRequestConditions sourceRequestConditions, Duration timeout,
            Context context) {
        return stageBlockFromUrlWithResponse(new BlockBlobStageBlockFromUrlOptions(base64BlockId, sourceUrl)
            .setSourceRange(sourceRange).setSourceContentMd5(sourceContentMd5).setLeaseId(leaseId)
            .setSourceRequestConditions(sourceRequestConditions), timeout, context);
    }

    /**
     * Creates a new block to be committed as part of a blob where the contents are read from a URL. For more
     * information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-from-url">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromUrlWithResponse#BlockBlobStageBlockFromUrlOptions-Duration-Context -->
     * <pre>
     * BlobRequestConditions sourceRequestConditions = new BlobRequestConditions&#40;&#41;
     *     .setIfUnmodifiedSince&#40;OffsetDateTime.now&#40;&#41;.minusDays&#40;3&#41;&#41;;
     * Context context = new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Staging block from URL completed with status %d%n&quot;,
     *     client.stageBlockFromUrlWithResponse&#40;new BlockBlobStageBlockFromUrlOptions&#40;base64BlockId, sourceUrl&#41;
     *         .setSourceRange&#40;new BlobRange&#40;offset, count&#41;&#41;.setLeaseId&#40;leaseId&#41;
     *         .setSourceRequestConditions&#40;sourceRequestConditions&#41;, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromUrlWithResponse#BlockBlobStageBlockFromUrlOptions-Duration-Context -->
     *
     * @param options Parameters for the operation
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers
     * @throws IllegalArgumentException If {@code sourceUrl} is a malformed {@link URL}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stageBlockFromUrlWithResponse(BlockBlobStageBlockFromUrlOptions options, Duration timeout,
        Context context) {
        return executeOperation(() -> stageBlockFromUrlWithResponseSync(options, enableSyncRestProxy(context)),
            timeout);
    }

    Response<Void> stageBlockFromUrlWithResponseSync(BlockBlobStageBlockFromUrlOptions options, Context context) {
        BlobRange sourceRange = (options.getSourceRange() == null) ? new BlobRange(0) : options.getSourceRange();
        BlobRequestConditions sourceRequestConditions = (options.getSourceRequestConditions() == null)
            ? new BlobRequestConditions() : options.getSourceRequestConditions();

        try {
            new URL(options.getSourceUrl());
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'sourceUrl' is not a valid url.", ex));
        }
        context = context == null ? Context.NONE : context;
        String sourceAuth = options.getSourceAuthorization() == null
            ? null : options.getSourceAuthorization().toString();

        ResponseBase<BlockBlobsStageBlockFromURLHeaders, Void> response = this.azureBlobStorage.getBlockBlobs().stageBlockFromURLWithResponse(containerName, blobName,
            options.getBase64BlockId(), 0, options.getSourceUrl(), sourceRange.toHeaderValue(), options.getSourceContentMd5(), null, null,
            options.getLeaseId(), sourceRequestConditions.getIfModifiedSince(),
            sourceRequestConditions.getIfUnmodifiedSince(), sourceRequestConditions.getIfMatch(),
            sourceRequestConditions.getIfNoneMatch(), null, sourceAuth, getCustomerProvidedKey(),
            encryptionScope, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE));
        return new SimpleResponse<>(response, null);
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list filter.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.listBlocks#BlockListType -->
     * <pre>
     * BlockList block = client.listBlocks&#40;BlockListType.ALL&#41;;
     *
     * System.out.println&#40;&quot;Committed Blocks:&quot;&#41;;
     * block.getCommittedBlocks&#40;&#41;.forEach&#40;b -&gt; System.out.printf&#40;&quot;Name: %s, Size: %d&quot;, b.getName&#40;&#41;, b.getSizeLong&#40;&#41;&#41;&#41;;
     *
     * System.out.println&#40;&quot;Uncommitted Blocks:&quot;&#41;;
     * block.getUncommittedBlocks&#40;&#41;.forEach&#40;b -&gt; System.out.printf&#40;&quot;Name: %s, Size: %d&quot;, b.getName&#40;&#41;, b.getSizeLong&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.listBlocks#BlockListType -->
     *
     * @param listType Specifies which type of blocks to return.
     *
     * @return The list of blocks.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlockList listBlocks(BlockListType listType) {
        return this.listBlocksWithResponse(listType, null, null, Context.NONE).getValue();
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list
     * filter. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.listBlocksWithResponse#BlockListType-String-Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;;
     * BlockList block = client.listBlocksWithResponse&#40;BlockListType.ALL, leaseId, timeout, context&#41;.getValue&#40;&#41;;
     *
     * System.out.println&#40;&quot;Committed Blocks:&quot;&#41;;
     * block.getCommittedBlocks&#40;&#41;.forEach&#40;b -&gt; System.out.printf&#40;&quot;Name: %s, Size: %d&quot;, b.getName&#40;&#41;, b.getSizeLong&#40;&#41;&#41;&#41;;
     *
     * System.out.println&#40;&quot;Uncommitted Blocks:&quot;&#41;;
     * block.getUncommittedBlocks&#40;&#41;.forEach&#40;b -&gt; System.out.printf&#40;&quot;Name: %s, Size: %d&quot;, b.getName&#40;&#41;, b.getSizeLong&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.listBlocksWithResponse#BlockListType-String-Duration-Context -->
     *
     * @param listType Specifies which type of blocks to return.
     * @param leaseId The lease ID the active lease on the blob must match.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The list of blocks.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockList> listBlocksWithResponse(BlockListType listType, String leaseId, Duration timeout,
        Context context) {
        return listBlocksWithResponse(new BlockBlobListBlocksOptions(listType).setLeaseId(leaseId), timeout, context);
    }

    /**
     * Returns the list of blocks that have been uploaded as part of a block blob using the specified block list
     * filter. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/get-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.listBlocksWithResponse#BlockBlobListBlocksOptions-Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;;
     * BlockList block = client.listBlocksWithResponse&#40;new BlockBlobListBlocksOptions&#40;BlockListType.ALL&#41;
     *     .setLeaseId&#40;leaseId&#41;
     *     .setIfTagsMatch&#40;tags&#41;, timeout, context&#41;.getValue&#40;&#41;;
     *
     * System.out.println&#40;&quot;Committed Blocks:&quot;&#41;;
     * block.getCommittedBlocks&#40;&#41;.forEach&#40;b -&gt; System.out.printf&#40;&quot;Name: %s, Size: %d&quot;, b.getName&#40;&#41;, b.getSizeLong&#40;&#41;&#41;&#41;;
     *
     * System.out.println&#40;&quot;Uncommitted Blocks:&quot;&#41;;
     * block.getUncommittedBlocks&#40;&#41;.forEach&#40;b -&gt; System.out.printf&#40;&quot;Name: %s, Size: %d&quot;, b.getName&#40;&#41;, b.getSizeLong&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.listBlocksWithResponse#BlockBlobListBlocksOptions-Duration-Context -->
     *
     * @param options {@link BlockBlobListBlocksOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The list of blocks.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockList> listBlocksWithResponse(BlockBlobListBlocksOptions options, Duration timeout,
        Context context) {
        return executeOperation(() -> listBlocksWithResponseSync(options, enableSyncRestProxy(context)),
            timeout);
    }

    Response<BlockList> listBlocksWithResponseSync(BlockBlobListBlocksOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);

        ResponseBase<BlockBlobsGetBlockListHeaders, BlockList> response = this.azureBlobStorage.getBlockBlobs().getBlockListWithResponse(
            containerName, blobName, options.getType(), getSnapshotId(), null, options.getLeaseId(),
            options.getIfTagsMatch(), null, context);
        return new SimpleResponse<>(response, response.getValue());
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob. In order to be written as part of
     * a blob, a block must have been successfully written to the server in a prior stageBlock operation. You can call
     * commitBlockList to update a blob by uploading only those blocks that have changed, then committing the new and
     * existing blocks together. Any blocks not specified in the block list and permanently deleted. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.commitBlockList#List -->
     * <pre>
     * System.out.printf&#40;&quot;Committing block list completed. Last modified: %s%n&quot;,
     *     client.commitBlockList&#40;Collections.singletonList&#40;base64BlockId&#41;&#41;.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.commitBlockList#List -->
     *
     * @param base64BlockIds A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @return The information of the block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlockBlobItem commitBlockList(List<String> base64BlockIds) {
        return commitBlockList(base64BlockIds, false);
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob. In order to be written as part of
     * a blob, a block must have been successfully written to the server in a prior stageBlock operation. You can call
     * commitBlockList to update a blob by uploading only those blocks that have changed, then committing the new and
     * existing blocks together. Any blocks not specified in the block list and permanently deleted. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.commitBlockList#List-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;&#47; Default behavior
     * System.out.printf&#40;&quot;Committing block list completed. Last modified: %s%n&quot;,
     *     client.commitBlockList&#40;Collections.singletonList&#40;base64BlockId&#41;, overwrite&#41;.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.commitBlockList#List-boolean -->
     *
     * @param base64BlockIds A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @param overwrite Whether to overwrite, should data exist on the blob.
     * @return The information of the block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BlockBlobItem commitBlockList(List<String> base64BlockIds, boolean overwrite) {
        BlobRequestConditions requestConditions = null;
        if (!overwrite) {
            requestConditions = new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return commitBlockListWithResponse(base64BlockIds, null, null, null, requestConditions, null, Context.NONE)
            .getValue();
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob. In order to be written as part
     * of a blob, a block must have been successfully written to the server in a prior stageBlock operation. You can
     * call commitBlockList to update a blob by uploading only those blocks that have changed, then committing the new
     * and existing blocks together. Any blocks not specified in the block list and permanently deleted. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.uploadFromFile#List-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration-Context -->
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
     * Context context = new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Committing block list completed with status %d%n&quot;,
     *     client.commitBlockListWithResponse&#40;Collections.singletonList&#40;base64BlockId&#41;, headers, metadata,
     *         AccessTier.HOT, requestConditions, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.uploadFromFile#List-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration-Context -->
     *
     * @param base64BlockIds A list of base64 encode {@code String}s that specifies the block IDs to be committed.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The information of the block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockBlobItem> commitBlockListWithResponse(List<String> base64BlockIds, BlobHttpHeaders headers,
            Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions, Duration timeout,
            Context context) {
        return this.commitBlockListWithResponse(new BlockBlobCommitBlockListOptions(base64BlockIds)
                .setHeaders(headers).setMetadata(metadata).setTier(tier).setRequestConditions(requestConditions),
                timeout, context);
    }

    /**
     * Writes a blob by specifying the list of block IDs that are to make up the blob. In order to be written as part
     * of a blob, a block must have been successfully written to the server in a prior stageBlock operation. You can
     * call commitBlockList to update a blob by uploading only those blocks that have changed, then committing the new
     * and existing blocks together. Any blocks not specified in the block list and permanently deleted. For more
     * information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs</a>.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlockBlobClient.uploadFromFile#BlockBlobCommitBlockListOptions-Duration-Context -->
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
     * Context context = new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Committing block list completed with status %d%n&quot;,
     *     client.commitBlockListWithResponse&#40;
     *         new BlockBlobCommitBlockListOptions&#40;Collections.singletonList&#40;base64BlockId&#41;&#41;.setHeaders&#40;headers&#41;
     *             .setMetadata&#40;metadata&#41;.setTags&#40;tags&#41;.setTier&#40;AccessTier.HOT&#41;
     *             .setRequestConditions&#40;requestConditions&#41;, timeout, context&#41;
     *         .getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlockBlobClient.uploadFromFile#BlockBlobCommitBlockListOptions-Duration-Context -->
     *
     * @param options {@link BlockBlobCommitBlockListOptions options}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The information of the block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BlockBlobItem> commitBlockListWithResponse(BlockBlobCommitBlockListOptions options,
        Duration timeout, Context context) {
        return executeOperation(() -> commitBlockListWithResponseSync(options, enableSyncRestProxy(context)),
            timeout);
    }

    Response<BlockBlobItem> commitBlockListWithResponseSync(BlockBlobCommitBlockListOptions options,
                                                            Context context) {
        StorageImplUtils.assertNotNull("options", options);
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();
        context = context == null ? Context.NONE : context;
        BlobImmutabilityPolicy immutabilityPolicy = options.getImmutabilityPolicy() == null
            ? new BlobImmutabilityPolicy() : options.getImmutabilityPolicy();

        ResponseBase<BlockBlobsCommitBlockListHeaders, Void> response = this.azureBlobStorage.getBlockBlobs().commitBlockListWithResponse(containerName, blobName,
            new BlockLookupList().setLatest(options.getBase64BlockIds()), null, null, null, options.getMetadata(),
            requestConditions.getLeaseId(), options.getTier(), requestConditions.getIfModifiedSince(),
            requestConditions.getIfUnmodifiedSince(), requestConditions.getIfMatch(),
            requestConditions.getIfNoneMatch(), requestConditions.getTagsConditions(), null,
            tagsToString(options.getTags()), immutabilityPolicy.getExpiryTime(), immutabilityPolicy.getPolicyMode(),
            options.isLegalHold(), options.getHeaders(), getCustomerProvidedKey(),
            encryptionScope, context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE));

        BlockBlobsCommitBlockListHeaders hd = response.getDeserializedHeaders();
        BlockBlobItem item = new BlockBlobItem(hd.getETag(), hd.getLastModified(), hd.getContentMD5(),
            hd.isXMsRequestServerEncrypted(), hd.getXMsEncryptionKeySha256(), hd.getXMsEncryptionScope(),
            hd.getXMsVersionId());
        return new SimpleResponse<>(response, item);
    }

    /**
     * Get the url of the storage account.
     *
     * @return the URL of the storage account
     */
    public String getAccountUrl() {
        return asyncClient.getAccountUrl();
    }

    /**
     * Gets the URL of the blob represented by this client.
     *
     * @return the URL.
     */
    public String getBlobUrl() {
        String blobUrl = azureBlobStorage.getUrl() + "/" + containerName + "/" + Utility.urlEncode(blobName);
        if (this.isSnapshot()) {
            blobUrl = Utility.appendQueryParameter(blobUrl, "snapshot", getSnapshotId());
        }
        if (this.getVersionId() != null) {
            blobUrl = Utility.appendQueryParameter(blobUrl, "versionid", getVersionId());
        }
        return blobUrl;
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Gets a client pointing to the parent container.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.BlobClientBase.getContainerClient -->
     * <pre>
     * BlobContainerClient containerClient = client.getContainerClient&#40;&#41;;
     * System.out.println&#40;&quot;The name of the container is &quot; + containerClient.getBlobContainerName&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.BlobClientBase.getContainerClient -->
     *
     * @return {@link BlobContainerClient}
     */
    public BlobContainerClient getContainerClient() {
        return getContainerClientBuilder().buildClient();
    }

    BlobContainerClientBuilder getContainerClientBuilder() {
        CustomerProvidedKey encryptionKey = this.customerProvidedKey == null ? null
            : new CustomerProvidedKey(this.customerProvidedKey.getEncryptionKey());
        return new BlobContainerClientBuilder()
            .endpoint(this.getBlobUrl())
            .pipeline(this.getHttpPipeline())
            .serviceVersion(this.serviceVersion)
            .customerProvidedKey(encryptionKey)
            .encryptionScope(this.getEncryptionScope());
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
     * Gets the {@link CpkInfo} used to encrypt this blob's content on the server.
     *
     * @return the customer provided key used for encryption.
     */
    public CpkInfo getCustomerProvidedKey() {
        return customerProvidedKey;
    }

    /**
     * Gets the {@code encryption scope} used to encrypt this blob's content on the server.
     *
     * @return the encryption scope used for encryption.
     */
    String getEncryptionScope() {
        if (encryptionScope == null) {
            return null;
        }
        return encryptionScope.getEncryptionScope();
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
     * Gets the snapshotId for a blob resource
     *
     * @return A string that represents the snapshotId of the snapshot blob
     */
    public String getSnapshotId() {
        return this.snapshot;
    }

    /**
     * Gets the versionId for a blob resource
     *
     * @return A string that represents the versionId of the snapshot blob
     */
    public String getVersionId() {
        return this.versionId;
    }

    /**
     * Determines if a blob is a snapshot
     *
     * @return A boolean that indicates if a blob is a snapshot
     */
    public boolean isSnapshot() {
        return this.snapshot != null;
    }

    String tagsToString(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            try {
                sb.append(URLEncoder.encode(entry.getKey(), Charset.defaultCharset().toString()));
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue(), Charset.defaultCharset().toString()));
                sb.append("&");
            } catch (UnsupportedEncodingException e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }
        }

        sb.deleteCharAt(sb.length() - 1); // Remove the last '&'
        return sb.toString();
    }
}
