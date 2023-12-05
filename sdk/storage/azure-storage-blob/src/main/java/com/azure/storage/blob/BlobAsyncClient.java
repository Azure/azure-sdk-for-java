// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Contexts;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressListener;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockOptions;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.implementation.BufferStagingArea;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.UploadUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class provides a client that contains generic blob operations for Azure Storage Blobs. Operations allowed by the
 * client are uploading and downloading, copying a blob, retrieving and setting metadata, retrieving and setting HTTP
 * headers, and deleting and un-deleting a blob.
 *
 * <p>
 * This client is instantiated through {@link BlobClientBuilder} or retrieved via {@link
 * BlobContainerAsyncClient#getBlobAsyncClient(String) getBlobAsyncClient}.
 *
 * <p>
 * For operations on a specific blob type (i.e. append, block, or page) use {@link #getAppendBlobAsyncClient()
 * getAppendBlobAsyncClient}, {@link #getBlockBlobAsyncClient() getBlockBlobAsyncClient}, or {@link
 * #getPageBlobAsyncClient() getPageBlobAsyncClient} to construct a client that allows blob specific operations.
 *
 * <p>
 * Please refer to the
 * <a href=https://docs.microsoft.com/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
public class BlobAsyncClient extends BlobAsyncClientBase {
    /**
     * The block size to use if none is specified in parallel operations.
     */
    public static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;

    /**
     * The number of buffers to use if none is specified on the buffered upload method.
     */
    public static final int BLOB_DEFAULT_NUMBER_OF_BUFFERS = 8;

    /**
     * If a blob is known to be greater than 100MB, using a larger block size will trigger some server-side
     * optimizations. If the block size is not set and the size of the blob is known to be greater than 100MB, this
     * value will be used.
     */
    public static final int BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE = 8 * Constants.MB;

    static final long BLOB_MAX_UPLOAD_BLOCK_SIZE = 4000L * Constants.MB;

    /**
     * The default block size used in {@link FluxUtil#readFile(AsynchronousFileChannel)}.
     * This is to make sure we're using same size when using {@link BinaryData#fromFile(Path, int)}
     * and {@link BinaryData#fromFile(Path, Long, Long, int)}
     * to represent the content.
     */
    private static final int DEFAULT_FILE_READ_CHUNK_SIZE = 1024 * 64;

    private static final ClientLogger LOGGER = new ClientLogger(BlobAsyncClient.class);

    private BlockBlobAsyncClient blockBlobAsyncClient;
    private AppendBlobAsyncClient appendBlobAsyncClient;
    private PageBlobAsyncClient pageBlobAsyncClient;

    /**
     * Protected constructor for use by {@link BlobClientBuilder}.
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
     */
    protected BlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName,
        String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey) {
        super(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey);
    }

    /**
     * Protected constructor for use by {@link BlobClientBuilder}.
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
     */
    protected BlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName,
        String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey,
        EncryptionScope encryptionScope) {
        super(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey,
            encryptionScope);
    }

    /**
     * Protected constructor for use by {@link BlobClientBuilder}.
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
    protected BlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName,
                              String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey,
                              EncryptionScope encryptionScope, String versionId) {
        super(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey,
            encryptionScope, versionId);
    }

    /**
     * Creates a new {@link BlobAsyncClient} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return A {@link BlobAsyncClient} used to interact with the specific snapshot.
     */
    @Override
    public BlobAsyncClient getSnapshotClient(String snapshot) {
        return new BlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), snapshot, getCustomerProvidedKey(), encryptionScope, getVersionId());
    }

    /**
     * Creates a new {@link BlobAsyncClient} linked to the {@code versionId} of this blob resource.
     *
     * @param versionId the identifier for a specific version of this blob,
     * pass {@code null} to interact with the latest blob version.
     * @return A {@link BlobAsyncClient} used to interact with the specific version.
     */
    @Override
    public BlobAsyncClient getVersionClient(String versionId) {
        return new BlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), getSnapshotId(), getCustomerProvidedKey(), encryptionScope, versionId);
    }

    /**
     * Creates a new {@link BlobAsyncClient} with the specified {@code encryptionScope}.
     *
     * @param encryptionScope the encryption scope for the blob, pass {@code null} to use no encryption scope.
     * @return a {@link BlobAsyncClient} with the specified {@code encryptionScope}.
     */
    @Override
    public BlobAsyncClient getEncryptionScopeAsyncClient(String encryptionScope) {
        EncryptionScope finalEncryptionScope = null;
        if (encryptionScope != null) {
            finalEncryptionScope = new EncryptionScope().setEncryptionScope(encryptionScope);
        }
        return new BlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), getSnapshotId(), getCustomerProvidedKey(), finalEncryptionScope,
            getVersionId());
    }

    /**
     * Creates a new {@link BlobAsyncClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the blob,
     * pass {@code null} to use no customer provided key.
     * @return a {@link BlobAsyncClient} with the specified {@code customerProvidedKey}.
     */
    @Override
    public BlobAsyncClient getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey) {
        CpkInfo finalCustomerProvidedKey = null;
        if (customerProvidedKey != null) {
            finalCustomerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }
        return new BlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), getSnapshotId(), finalCustomerProvidedKey, encryptionScope,
            getVersionId());
    }

    /**
     * Creates a new {@link AppendBlobAsyncClient} associated with this blob.
     *
     * @return A {@link AppendBlobAsyncClient} associated with this blob.
     */
    public AppendBlobAsyncClient getAppendBlobAsyncClient() {
        if (appendBlobAsyncClient == null) {
            appendBlobAsyncClient = prepareBuilder().buildAppendBlobAsyncClient();
        }
        return appendBlobAsyncClient;
    }

    /**
     * Creates a new {@link BlockBlobAsyncClient} associated with this blob.
     *
     * @return A {@link BlockBlobAsyncClient} associated with this blob.
     */
    public BlockBlobAsyncClient getBlockBlobAsyncClient() {
        if (blockBlobAsyncClient == null) {
            blockBlobAsyncClient = prepareBuilder().buildBlockBlobAsyncClient();
        }
        return blockBlobAsyncClient;
    }

    /**
     * Creates a new {@link PageBlobAsyncClient} associated with this blob.
     *
     * @return A {@link PageBlobAsyncClient} associated with this blob.
     */
    public PageBlobAsyncClient getPageBlobAsyncClient() {
        if (pageBlobAsyncClient == null) {
            pageBlobAsyncClient = prepareBuilder().buildPageBlobAsyncClient();
        }
        return pageBlobAsyncClient;
    }

    private SpecializedBlobClientBuilder prepareBuilder() {
        SpecializedBlobClientBuilder builder = new SpecializedBlobClientBuilder()
            .pipeline(getHttpPipeline())
            .endpoint(getBlobUrl())
            .snapshot(getSnapshotId())
            .serviceVersion(getServiceVersion());

        CpkInfo cpk = getCustomerProvidedKey();
        if (cpk != null) {
            builder.customerProvidedKey(new CustomerProvidedKey(cpk.getEncryptionKey()));
        }

        if (encryptionScope != null) {
            builder.encryptionScope(encryptionScope.getEncryptionScope());
        }

        return builder;
    }

    /**
     * Creates a new block blob. By default, this method will not overwrite an existing blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with this method; the content of the existing blob is overwritten with the new content. To perform a partial
     * update of a block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
     * BlockBlobAsyncClient#commitBlockList(List) commitBlockList}. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs for Put Block</a> and the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method does
     * support uploading any arbitrary data source, including network streams. This behavior is possible because this
     * method will perform some internal buffering as configured by the blockSize and numBuffers parameters, so while
     * this method may offer additional convenience, it will not be as performant as other options, which should be
     * preferred when possible.
     * <p>
     * Typically, the greater the number of buffers used, the greater the possible parallelism when transferring the
     * data. Larger buffers means we will have to stage fewer blocks and therefore require fewer IO operations. The
     * trade-offs between these values are context-dependent, so some experimentation may be required to optimize inputs
     * for a given scenario.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncClient.upload#Flux-ParallelTransferOptions -->
     * <pre>
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;
     *     .setMaxConcurrency&#40;maxConcurrency&#41;;
     * client.upload&#40;data, parallelTransferOptions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *         Base64.getEncoder&#40;&#41;.encodeToString&#40;response.getContentMd5&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncClient.upload#Flux-ParallelTransferOptions -->
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions) {
        return upload(data, parallelTransferOptions, false);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with this method; the content of the existing blob is overwritten with the new content. To perform a partial
     * update of a block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
     * BlockBlobAsyncClient#commitBlockList(List) commitBlockList}. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs for Put Block</a> and the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method does
     * support uploading any arbitrary data source, including network streams. This behavior is possible because this
     * method will perform some internal buffering as configured by the blockSize and numBuffers parameters, so while
     * this method may offer additional convenience, it will not be as performant as other options, which should be
     * preferred when possible.
     * <p>
     * Typically, the greater the number of buffers used, the greater the possible parallelism when transferring the
     * data. Larger buffers means we will have to stage fewer blocks and therefore require fewer IO operations. The
     * trade-offs between these values are context-dependent, so some experimentation may be required to optimize inputs
     * for a given scenario.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncClient.upload#Flux-ParallelTransferOptions-boolean -->
     * <pre>
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;
     *     .setMaxConcurrency&#40;maxConcurrency&#41;;
     * boolean overwrite = false; &#47;&#47; Default behavior
     * client.upload&#40;data, parallelTransferOptions, overwrite&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *         Base64.getEncoder&#40;&#41;.encodeToString&#40;response.getContentMd5&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncClient.upload#Flux-ParallelTransferOptions-boolean -->
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param overwrite Whether to overwrite, should the blob already exist.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions,
        boolean overwrite) {
        Mono<Void> overwriteCheck;
        BlobRequestConditions requestConditions;

        if (overwrite) {
            overwriteCheck = Mono.empty();
            requestConditions = null;
        } else {
            overwriteCheck = exists().flatMap(exists -> exists
                ? monoError(LOGGER, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                : Mono.empty());
            requestConditions = new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }

        return overwriteCheck
            .then(uploadWithResponse(data, parallelTransferOptions, null, null, null, requestConditions))
            .flatMap(FluxUtil::toMono);
    }


    /**
     * Creates a new block blob. By default, this method will not overwrite an existing blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncClient.upload#BinaryData -->
     * <pre>
     * client.upload&#40;BinaryData.fromString&#40;&quot;Data!&quot;&#41;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *         Base64.getEncoder&#40;&#41;.encodeToString&#40;response.getContentMd5&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncClient.upload#BinaryData -->
     *
     * @param data The data to write to the blob.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockBlobItem> upload(BinaryData data) {
        return upload(data, false);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncClient.upload#BinaryData-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;&#47; Default behavior
     * client.upload&#40;BinaryData.fromString&#40;&quot;Data!&quot;&#41;, overwrite&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *         Base64.getEncoder&#40;&#41;.encodeToString&#40;response.getContentMd5&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncClient.upload#BinaryData-boolean -->
     *
     * @param data The data to write to the blob.
     * @param overwrite Whether to overwrite, should the blob already exist.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockBlobItem> upload(BinaryData data, boolean overwrite) {
        Mono<Void> overwriteCheck;
        BlobRequestConditions requestConditions;

        if (overwrite) {
            overwriteCheck = Mono.empty();
            requestConditions = null;
        } else {
            overwriteCheck = exists().flatMap(exists -> exists
                ? monoError(LOGGER, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                : Mono.empty());
            requestConditions = new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }

        return overwriteCheck
            .then(uploadWithResponse(data.toFluxByteBuffer(), null, null, null, null, requestConditions))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with this method; the content of the existing blob is overwritten with the new content. To perform a partial
     * update of a block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
     * BlockBlobAsyncClient#commitBlockList(List) commitBlockList}. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs for Put Block</a> and the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method does
     * support uploading any arbitrary data source, including network streams. This behavior is possible because this
     * method will perform some internal buffering as configured by the blockSize and numBuffers parameters, so while
     * this method may offer additional convenience, it will not be as performant as other options, which should be
     * preferred when possible.
     * <p>
     * Typically, the greater the number of buffers used, the greater the possible parallelism when transferring the
     * data. Larger buffers means we will have to stage fewer blocks and therefore require fewer IO operations. The
     * trade-offs between these values are context-dependent, so some experimentation may be required to optimize inputs
     * for a given scenario.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions -->
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
     *
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;
     *     .setMaxConcurrency&#40;maxConcurrency&#41;;
     *
     * client.uploadWithResponse&#40;data, parallelTransferOptions, headers, metadata, AccessTier.HOT, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *         Base64.getEncoder&#40;&#41;.encodeToString&#40;response.getValue&#40;&#41;.getContentMd5&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions -->
     *
     * <p><strong>Using Progress Reporting</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions.ProgressReporter -->
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
     *
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;
     *     .setMaxConcurrency&#40;maxConcurrency&#41;
     *     .setProgressListener&#40;bytesTransferred -&gt; System.out.printf&#40;&quot;Upload progress: %s bytes sent&quot;, bytesTransferred&#41;&#41;;
     *
     * client.uploadWithResponse&#40;data, parallelTransferOptions, headers, metadata, AccessTier.HOT, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *         Base64.getEncoder&#40;&#41;.encodeToString&#40;response.getValue&#40;&#41;.getContentMd5&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions.ProgressReporter -->
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data,
        ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers, Map<String, String> metadata,
        AccessTier tier, BlobRequestConditions requestConditions) {
        try {
            return this.uploadWithResponse(new BlobParallelUploadOptions(data)
                .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
                .setTier(tier).setRequestConditions(requestConditions));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with this method; the content of the existing blob is overwritten with the new content. To perform a partial
     * update of a block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
     * BlockBlobAsyncClient#commitBlockList(List) commitBlockList}. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs for Put Block</a> and the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method does
     * support uploading any arbitrary data source, including network streams. This behavior is possible because this
     * method will perform some internal buffering as configured by the blockSize and numBuffers parameters, so while
     * this method may offer additional convenience, it will not be as performant as other options, which should be
     * preferred when possible.
     * <p>
     * Typically, the greater the number of buffers used, the greater the possible parallelism when transferring the
     * data. Larger buffers means we will have to stage fewer blocks and therefore require fewer IO operations. The
     * trade-offs between these values are context-dependent, so some experimentation may be required to optimize inputs
     * for a given scenario.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#BlobParallelUploadOptions -->
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
     *
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;blockSize&#41;
     *     .setMaxConcurrency&#40;maxConcurrency&#41;.setProgressListener&#40;bytesTransferred -&gt;
     *         System.out.printf&#40;&quot;Upload progress: %s bytes sent&quot;, bytesTransferred&#41;&#41;;
     *
     * client.uploadWithResponse&#40;new BlobParallelUploadOptions&#40;data&#41;
     *     .setParallelTransferOptions&#40;parallelTransferOptions&#41;.setHeaders&#40;headers&#41;.setMetadata&#40;metadata&#41;.setTags&#40;tags&#41;
     *     .setTier&#40;AccessTier.HOT&#41;.setRequestConditions&#40;requestConditions&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *         Base64.getEncoder&#40;&#41;.encodeToString&#40;response.getValue&#40;&#41;.getContentMd5&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#BlobParallelUploadOptions -->
     *
     * <p><strong>Using Progress Reporting</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#BlobParallelUploadOptions -->
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
     *
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;blockSize&#41;
     *     .setMaxConcurrency&#40;maxConcurrency&#41;.setProgressListener&#40;bytesTransferred -&gt;
     *         System.out.printf&#40;&quot;Upload progress: %s bytes sent&quot;, bytesTransferred&#41;&#41;;
     *
     * client.uploadWithResponse&#40;new BlobParallelUploadOptions&#40;data&#41;
     *     .setParallelTransferOptions&#40;parallelTransferOptions&#41;.setHeaders&#40;headers&#41;.setMetadata&#40;metadata&#41;.setTags&#40;tags&#41;
     *     .setTier&#40;AccessTier.HOT&#41;.setRequestConditions&#40;requestConditions&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *         Base64.getEncoder&#40;&#41;.encodeToString&#40;response.getValue&#40;&#41;.getContentMd5&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#BlobParallelUploadOptions -->
     *
     * @param options {@link BlobParallelUploadOptions}. Unlike other upload methods, this method does not require that
     * the {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not
     * expected to produce the same values across subscriptions.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlockBlobItem>> uploadWithResponse(BlobParallelUploadOptions options) {
        /*
         * The following is catalogue of all the places we allocate memory/copy in any upload method a justification for
         * that case current as of 1/13/21.
         *
         * - Async buffered upload chunked upload: We used an UploadBufferPool. This will allocate memory as needed up
         *   to the configured maximum. This is necessary to support replayability on retires. Each flux to come out of
         *   the pool is a Flux.just() of up to two deep copied buffers, so it is replayable. It also allows us to
         *   optimize the upload by uploading the maximum amount per block. Finally, in the case of chunked uploading,
         *   it allows the customer to pass data without knowing the size. Note that full upload does not need a deep
         *   copy because the Flux emitted by the PayloadSizeGate in the full upload case is already replayable and the
         *   length is maintained by the gate.
         *
         * - Sync buffered upload: converting the input stream to a flux involves creating a buffer for each stream
         *   read. Using a new buffer per read ensures that the reads are safe and not overwriting data in buffers that
         *   were passed to the async upload but have not yet been sent. This covers both full and chunked uploads in
         *   the sync case.
         *
         * - BlobOutputStream: A deep copy is made of any buffer passed to write. While async copy does streamline our
         *   code and allow for some potential parallelization, this extra copy is necessary to ensure that customers
         *   writing to the stream in a tight loop are not overwriting data previously given to the stream before it has
         *   been sent.
         *
         * Taken together, these should support retries and protect against data being overwritten in all upload
         * scenarios.
         *
         * One note is that there is no deep copy in the uploadFull method. This is unnecessary as explained in
         * uploadFullOrChunked because the Flux coming out of the size gate in that case is already replayable and
         * reusing buffers is not a common scenario for async like it is in sync (and we already buffer in sync to
         * convert from a stream).
         */
        try {
            StorageImplUtils.assertNotNull("options", options);

            final ParallelTransferOptions parallelTransferOptions =
                ModelHelper.populateAndApplyDefaults(options.getParallelTransferOptions());
            final BlobHttpHeaders headers = options.getHeaders();
            final Map<String, String> metadata = options.getMetadata();
            final Map<String, String> tags = options.getTags();
            final AccessTier tier = options.getTier();
            final BlobRequestConditions requestConditions = options.getRequestConditions() == null
                ? new BlobRequestConditions() : options.getRequestConditions();
            final boolean computeMd5 = options.isComputeMd5();
            final BlobImmutabilityPolicy immutabilityPolicy = options.getImmutabilityPolicy() == null
                ? new BlobImmutabilityPolicy() : options.getImmutabilityPolicy();
            final Boolean legalHold = options.isLegalHold();

            BlockBlobAsyncClient blockBlobAsyncClient = getBlockBlobAsyncClient();

            Function<Flux<ByteBuffer>, Mono<Response<BlockBlobItem>>> uploadInChunksFunction = (stream) ->
                uploadInChunks(blockBlobAsyncClient, stream, parallelTransferOptions, headers, metadata, tags,
                    tier, requestConditions, computeMd5, immutabilityPolicy, legalHold);

            BiFunction<Flux<ByteBuffer>, Long, Mono<Response<BlockBlobItem>>> uploadFullBlobFunction =
                (stream, length) -> uploadFullBlob(blockBlobAsyncClient, stream, length, parallelTransferOptions,
                    headers, metadata, tags, tier, requestConditions, computeMd5, immutabilityPolicy, legalHold);

            Flux<ByteBuffer> data = options.getDataFlux();
            data = UploadUtils.extractByteBuffer(data, options.getOptionalLength(),
                parallelTransferOptions.getBlockSizeLong(), options.getDataStream());

            return UploadUtils.uploadFullOrChunked(data, ModelHelper.wrapBlobOptions(parallelTransferOptions),
                uploadInChunksFunction, uploadFullBlobFunction);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    private Mono<Response<BlockBlobItem>> uploadFullBlob(BlockBlobAsyncClient blockBlobAsyncClient,
        Flux<ByteBuffer> data, long length, ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers,
        Map<String, String> metadata, Map<String, String> tags, AccessTier tier,
        BlobRequestConditions requestConditions, boolean computeMd5, BlobImmutabilityPolicy immutabilityPolicy,
        Boolean legalHold) {

        /*
         * Note that there is no need to buffer here as the flux returned by the size gate in this case is created
         * from an iterable and is therefore replayable.
         */
        return UploadUtils.computeMd5(data, computeMd5, LOGGER)
            .map(fluxMd5Wrapper -> new BlockBlobSimpleUploadOptions(fluxMd5Wrapper.getData(), length)
                .setHeaders(headers)
                .setMetadata(metadata)
                .setTags(tags)
                .setTier(tier)
                .setRequestConditions(requestConditions)
                .setContentMd5(fluxMd5Wrapper.getMd5())
                .setImmutabilityPolicy(immutabilityPolicy)
                .setLegalHold(legalHold))
            .flatMap(options -> {
                Mono<Response<BlockBlobItem>> responseMono = blockBlobAsyncClient.uploadWithResponse(options);
                if (parallelTransferOptions.getProgressListener() != null) {
                    ProgressReporter progressReporter = ProgressReporter.withProgressListener(
                        parallelTransferOptions.getProgressListener());
                    responseMono = responseMono.contextWrite(
                        FluxUtil.toReactorContext(
                            Contexts.empty()
                                .setHttpRequestProgressReporter(progressReporter).getContext()));
                }
                return responseMono;
            });
    }

    private Mono<Response<BlockBlobItem>> uploadInChunks(BlockBlobAsyncClient blockBlobAsyncClient,
        Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers,
        Map<String, String> metadata, Map<String, String> tags, AccessTier tier,
        BlobRequestConditions requestConditions, boolean computeMd5, BlobImmutabilityPolicy immutabilityPolicy,
        Boolean legalHold) {
        // TODO: Sample/api reference

        ProgressListener progressListener = parallelTransferOptions.getProgressListener();
        ProgressReporter progressReporter = progressListener == null ? null : ProgressReporter.withProgressListener(
            progressListener);

        // Validation done in the constructor.
        BufferStagingArea stagingArea = new BufferStagingArea(parallelTransferOptions.getBlockSizeLong(),
            BlockBlobClient.MAX_STAGE_BLOCK_BYTES_LONG);

        Flux<ByteBuffer> chunkedSource = UploadUtils.chunkSource(data,
            ModelHelper.wrapBlobOptions(parallelTransferOptions));

        /*
         * Write to the pool and upload the output.
         * maxConcurrency = 1 when writing means only 1 BufferAggregator will be accumulating at a time.
         * parallelTransferOptions.getMaxConcurrency() appends will be happening at once, so we guarantee buffering of
         * only concurrency + 1 chunks at a time.
         */
        return chunkedSource.flatMapSequential(stagingArea::write, 1, 1)
            .concatWith(Flux.defer(stagingArea::flush))
            .flatMapSequential(bufferAggregator -> {
                Flux<ByteBuffer> chunkData = bufferAggregator.asFlux();

                final String blockId = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(UTF_8));
                return UploadUtils.computeMd5(chunkData, computeMd5, LOGGER)
                    .flatMap(fluxMd5Wrapper -> {
                        Mono<Response<Void>> responseMono = blockBlobAsyncClient.stageBlockWithResponse(blockId,
                            fluxMd5Wrapper.getData(), bufferAggregator.length(), fluxMd5Wrapper.getMd5(),
                            requestConditions.getLeaseId());
                        if (progressReporter != null) {
                            responseMono = responseMono.contextWrite(
                                FluxUtil.toReactorContext(Contexts.empty()
                                    .setHttpRequestProgressReporter(progressReporter.createChild()).getContext())
                            );
                        }
                        return responseMono;
                    })
                    // We only care about the stageBlock insofar as it was successful, but we need to collect the ids.
                    .map(x -> blockId);
            }, parallelTransferOptions.getMaxConcurrency(), 1)
            .collect(Collectors.toList())
            .flatMap(ids ->
                blockBlobAsyncClient.commitBlockListWithResponse(new BlockBlobCommitBlockListOptions(ids)
                    .setHeaders(headers).setMetadata(metadata).setTags(tags).setTier(tier)
                    .setRequestConditions(requestConditions).setImmutabilityPolicy(immutabilityPolicy)
                    .setLegalHold(legalHold)));
    }

    /**
     * Creates a new block blob with the content of the specified file. By default, this method will not overwrite an
     * existing blob.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String -->
     * <pre>
     * client.uploadFromFile&#40;filePath&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String -->
     *
     * @param filePath Path to the upload file
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> uploadFromFile(String filePath) {
        return uploadFromFile(filePath, false);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;&#47; Default behavior
     * client.uploadFromFile&#40;filePath, overwrite&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-boolean -->
     *
     * @param filePath Path to the upload file
     * @param overwrite Whether to overwrite, should the blob already exist.
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> uploadFromFile(String filePath, boolean overwrite) {
        Mono<Void> overwriteCheck = Mono.empty();
        BlobRequestConditions requestConditions = null;

        // Note that if the file will be uploaded using a putBlob, we also can skip the exists check.
        //
        // Default behavior is to use uploading in chunks when the file size is greater than 256 MB.
        if (!overwrite) {
            if (UploadUtils.shouldUploadInChunks(filePath, ModelHelper.BLOB_DEFAULT_MAX_SINGLE_UPLOAD_SIZE, LOGGER)) {
                overwriteCheck = exists().flatMap(exists -> exists
                    ? monoError(LOGGER, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                    : Mono.empty());
            }

            requestConditions = new BlobRequestConditions().setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }

        return overwriteCheck.then(uploadFromFile(filePath, null, null, null, null, requestConditions));
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions -->
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
     *
     * client.uploadFromFile&#40;filePath,
     *     new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;BlockBlobClient.MAX_STAGE_BLOCK_BYTES_LONG&#41;,
     *     headers, metadata, AccessTier.HOT, requestConditions&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions -->
     *
     * @param filePath Path to the upload file
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     * transfers parameter is ignored.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @return An empty response
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier,
        BlobRequestConditions requestConditions) {
        try {
            return this.uploadFromFileWithResponse(new BlobUploadFromFileOptions(filePath)
                    .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata)
                    .setTier(tier).setRequestConditions(requestConditions))
                .then();
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     * <p>
     * To avoid overwriting, pass "*" to {@link BlobRequestConditions#setIfNoneMatch(String)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.BlobAsyncClient.uploadFromFileWithResponse#BlobUploadFromFileOptions -->
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
     *
     * client.uploadFromFileWithResponse&#40;new BlobUploadFromFileOptions&#40;filePath&#41;
     *     .setParallelTransferOptions&#40;
     *         new ParallelTransferOptions&#40;&#41;.setBlockSizeLong&#40;BlobAsyncClient.BLOB_MAX_UPLOAD_BLOCK_SIZE&#41;&#41;
     *     .setHeaders&#40;headers&#41;.setMetadata&#40;metadata&#41;.setTags&#40;tags&#41;.setTier&#40;AccessTier.HOT&#41;
     *     .setRequestConditions&#40;requestConditions&#41;&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.BlobAsyncClient.uploadFromFileWithResponse#BlobUploadFromFileOptions -->
     *
     * @param options {@link BlobUploadFromFileOptions}
     * @return A reactive response containing the information of the uploaded block blob.
     * @throws UncheckedIOException If an I/O error occurs
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlockBlobItem>> uploadFromFileWithResponse(BlobUploadFromFileOptions options) {
        StorageImplUtils.assertNotNull("options", options);
        Long originalBlockSize = (options.getParallelTransferOptions() == null)
            ? null
            : options.getParallelTransferOptions().getBlockSizeLong();
        final ParallelTransferOptions finalParallelTransferOptions =
            ModelHelper.populateAndApplyDefaults(options.getParallelTransferOptions());
        try {
            Path filePath = Paths.get(options.getFilePath());
            BlockBlobAsyncClient blockBlobAsyncClient = getBlockBlobAsyncClient();
            // This will retrieve file length but won't read file body.
            BinaryData fullFileData = BinaryData.fromFile(filePath, DEFAULT_FILE_READ_CHUNK_SIZE);
            long fileSize = fullFileData.getLength();

            // By default, if the file is larger than 256MB chunk it and stage it as blocks.
            // But, this is configurable by the user passing options with max single upload size configured.
            if (fileSize > finalParallelTransferOptions.getMaxSingleUploadSizeLong()) {
                return uploadFileChunks(fileSize, finalParallelTransferOptions, originalBlockSize,
                    options.getHeaders(), options.getMetadata(), options.getTags(),
                    options.getTier(), options.getRequestConditions(), filePath,
                    blockBlobAsyncClient);
            } else {
                // Otherwise, we know it can be sent in a single request reducing network overhead.
                Mono<Response<BlockBlobItem>> responseMono = blockBlobAsyncClient.uploadWithResponse(
                    new BlockBlobSimpleUploadOptions(fullFileData).setHeaders(options.getHeaders())
                        .setMetadata(options.getMetadata()).setTags(options.getTags())
                        .setTier(options.getTier())
                        .setRequestConditions(options.getRequestConditions()));
                if (finalParallelTransferOptions.getProgressListener() != null) {
                    ProgressReporter progressReporter = ProgressReporter.withProgressListener(
                        finalParallelTransferOptions.getProgressListener());
                    responseMono = responseMono.contextWrite(
                        FluxUtil.toReactorContext(
                            Contexts.empty()
                                .setHttpRequestProgressReporter(progressReporter).getContext()));
                }
                return responseMono;
            }
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    private Mono<Response<BlockBlobItem>> uploadFileChunks(
        long fileSize, ParallelTransferOptions parallelTransferOptions,
        Long originalBlockSize, BlobHttpHeaders headers, Map<String, String> metadata, Map<String, String> tags,
        AccessTier tier, BlobRequestConditions requestConditions, Path filePath,
        BlockBlobAsyncClient client) {
        final BlobRequestConditions finalRequestConditions = (requestConditions == null)
            ? new BlobRequestConditions() : requestConditions;
        // parallelTransferOptions are finalized in the calling method.

        ProgressListener progressListener = parallelTransferOptions.getProgressListener();
        ProgressReporter progressReporter = progressListener == null ? null : ProgressReporter.withProgressListener(
            progressListener);

        final SortedMap<Long, String> blockIds = new TreeMap<>();
        return Flux.fromIterable(sliceFile(fileSize, originalBlockSize, parallelTransferOptions.getBlockSizeLong()))
            .flatMap(chunk -> {
                String blockId = getBlockID();
                blockIds.put(chunk.getOffset(), blockId);

                BinaryData data = BinaryData.fromFile(
                    filePath, chunk.getOffset(), chunk.getCount(), DEFAULT_FILE_READ_CHUNK_SIZE);

                Mono<Response<Void>> responseMono = client.stageBlockWithResponse(
                    new BlockBlobStageBlockOptions(blockId, data)
                        .setLeaseId(finalRequestConditions.getLeaseId()));
                if (progressReporter != null) {
                    responseMono = responseMono.contextWrite(
                        FluxUtil.toReactorContext(Contexts.empty().setHttpRequestProgressReporter(
                            progressReporter.createChild()).getContext())
                    );
                }
                return responseMono;
            }, parallelTransferOptions.getMaxConcurrency())
            .then(Mono.defer(() -> client.commitBlockListWithResponse(
                new BlockBlobCommitBlockListOptions(new ArrayList<>(blockIds.values()))
                    .setHeaders(headers).setMetadata(metadata).setTags(tags).setTier(tier)
                    .setRequestConditions(finalRequestConditions))));
    }

    /**
     * RESERVED FOR INTERNAL USE.
     *
     * Resource Supplier for UploadFile.
     *
     * @param filePath The path for the file
     * @return {@code AsynchronousFileChannel}
     * @throws UncheckedIOException an input output exception.
     * @deprecated due to refactoring code to be in the common storage library.
     */
    @Deprecated
    protected AsynchronousFileChannel uploadFileResourceSupplier(String filePath) {
        return UploadUtils.uploadFileResourceSupplier(filePath, LOGGER);
    }

    private String getBlockID() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

    private List<BlobRange> sliceFile(long fileSize, Long originalBlockSize, long blockSize) {
        List<BlobRange> ranges = new ArrayList<>();
        if (fileSize > 100 * Constants.MB && originalBlockSize == null) {
            blockSize = BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE;
        }
        for (long pos = 0; pos < fileSize; pos += blockSize) {
            long count = blockSize;
            if (pos + count > fileSize) {
                count = fileSize - pos;
            }
            ranges.add(new BlobRange(pos, count));
        }
        return ranges;
    }
}
