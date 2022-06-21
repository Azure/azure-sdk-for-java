// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobQueryAsyncResponse;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobQueryOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.BufferStagingArea;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.UploadUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_CBC_PKCS5PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_GCM_NO_PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_DATA_KEY;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.GCM_ENCRYPTION_REGION_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.NONCE_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;

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
 * For operations on a specific blob type (i.e. append, block, or page) use {@link #getAppendBlobAsyncClient()
 * getAppendBlobAsyncClient}, {@link #getBlockBlobAsyncClient() getBlockBlobAsyncClient}, or {@link
 * #getPageBlobAsyncClient() getPageBlobAsyncClient} to construct a client that allows blob specific operations. Note,
 * these types do not support client-side encryption, though decryption is possible in case the associated
 * block/page/append blob contains encrypted data.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = EncryptedBlobClientBuilder.class, isAsync = true)
public class EncryptedBlobAsyncClient extends BlobAsyncClient {
    static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;
    private static final ClientLogger LOGGER = new ClientLogger(EncryptedBlobAsyncClient.class);

    /**
     * An object of type {@link AsyncKeyEncryptionKey} that is used to wrap/unwrap the content key during encryption.
     */
    private final AsyncKeyEncryptionKey keyWrapper;

    /**
     * A {@link String} that is used to wrap/unwrap the content key during encryption.
     */
    private final String keyWrapAlgorithm;

    private final EncryptionVersion encryptionVersion;

    /**
     * Package-private constructor for use by {@link EncryptedBlobClientBuilder}.
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
     * @param key The key used to encrypt and decrypt data.
     * @param keyWrapAlgorithm The algorithm used to wrap/unwrap the key during encryption.
     * @param versionId The version identifier for the blob, pass {@code null} to interact with the latest blob
     * version.
     */
    EncryptedBlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName,
        String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey,
        EncryptionScope encryptionScope, AsyncKeyEncryptionKey key, String keyWrapAlgorithm, String versionId,
        EncryptionVersion encryptionVersion) {
        super(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey,
            encryptionScope, versionId);

        this.keyWrapper = key;
        this.keyWrapAlgorithm = keyWrapAlgorithm;
        this.encryptionVersion = encryptionVersion;
    }

    /**
     * Creates a new {@link EncryptedBlobAsyncClient} with the specified {@code encryptionScope}.
     *
     * @param encryptionScope the encryption scope for the blob, pass {@code null} to use no encryption scope.
     * @return a {@link EncryptedBlobAsyncClient} with the specified {@code encryptionScope}.
     */
    @Override
    public EncryptedBlobAsyncClient getEncryptionScopeAsyncClient(String encryptionScope) {
        EncryptionScope finalEncryptionScope = null;
        if (encryptionScope != null) {
            finalEncryptionScope = new EncryptionScope().setEncryptionScope(encryptionScope);
        }
        return new EncryptedBlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), getSnapshotId(), getCustomerProvidedKey(), finalEncryptionScope,
            keyWrapper, keyWrapAlgorithm, getVersionId(), encryptionVersion);
    }

    /**
     * Creates a new {@link EncryptedBlobAsyncClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the blob, pass {@code null} to use no customer
     * provided key.
     * @return a {@link EncryptedBlobAsyncClient} with the specified {@code customerProvidedKey}.
     */
    @Override
    public EncryptedBlobAsyncClient getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey) {
        CpkInfo finalCustomerProvidedKey = null;
        if (customerProvidedKey != null) {
            finalCustomerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }
        return new EncryptedBlobAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getContainerName(), getBlobName(), getSnapshotId(), finalCustomerProvidedKey, encryptionScope, keyWrapper,
            keyWrapAlgorithm, getVersionId(), encryptionVersion);
    }

    /**
     * Creates a new block blob. By default, this method will not overwrite an existing blob.
     * <p>
     * Updating an existing block blob overwrites any existing blob metadata. Partial updates are not supported with
     * this method; the content of the existing blob is overwritten with the new content. To perform a partial update of
     * block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
     * BlockBlobAsyncClient#commitBlockList(List)} on a regular blob client. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs for Put Block</a> and the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method should
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
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.upload#Flux-ParallelTransferOptions -->
     * <pre>
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;
     *     .setMaxConcurrency&#40;maxConcurrency&#41;;
     * client.upload&#40;data, parallelTransferOptions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *         Base64.getEncoder&#40;&#41;.encodeToString&#40;response.getContentMd5&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.upload#Flux-ParallelTransferOptions -->
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions) {
        return this.upload(data, parallelTransferOptions, false);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing blob metadata. Partial updates are not supported with
     * this method; the content of the existing blob is overwritten with the new content. To perform a partial update of
     * block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
     * BlockBlobAsyncClient#commitBlockList(List)} on a regular blob client. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs for Put Block</a> and the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method should
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
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.upload#Flux-ParallelTransferOptions-boolean -->
     * <pre>
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;
     *     .setMaxConcurrency&#40;maxConcurrency&#41;;
     * boolean overwrite = false; &#47;&#47; Default behavior
     * client.upload&#40;data, parallelTransferOptions, overwrite&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *         Base64.getEncoder&#40;&#41;.encodeToString&#40;response.getContentMd5&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.upload#Flux-ParallelTransferOptions-boolean -->
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param overwrite Whether to overwrite if the blob exists.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions,
        boolean overwrite) {
        Mono<BlockBlobItem> uploadTask = this.uploadWithResponse(data, parallelTransferOptions, null, null, null,
            null).flatMap(FluxUtil::toMono);

        if (overwrite) {
            return uploadTask;
        } else {
            return exists().flatMap(exists -> exists
                ? monoError(LOGGER, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                : uploadTask);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing blob metadata. Partial updates are not supported with this method; the content of the
     * existing blob is overwritten with the new content. To perform a partial update of a block blob's, use {@link
     * BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link BlockBlobAsyncClient#commitBlockList(List)},
     * which this method uses internally. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure
     * Docs for Put Block</a> and the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure
     * Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method should
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
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions -->
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
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;
     *     .setMaxConcurrency&#40;maxConcurrency&#41;;
     *
     * client.uploadWithResponse&#40;data, parallelTransferOptions, headers, metadata, AccessTier.HOT, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *         Base64.getEncoder&#40;&#41;.encodeToString&#40;response.getValue&#40;&#41;.getContentMd5&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions -->
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any metadata
     * key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @Override
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
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing blob metadata. Partial updates are not supported with this method; the content of the
     * existing blob is overwritten with the new content. To perform a partial update of a block blob's, use {@link
     * BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link BlockBlobAsyncClient#commitBlockList(List)},
     * which this method uses internally. For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure
     * Docs for Put Block</a> and the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block-list">Azure
     * Docs for Put Block List</a>.
     * <p>
     * The data passed need not support multiple subscriptions/be replayable as is required in other upload methods when
     * retries are enabled, and the length of the data need not be known in advance. Therefore, this method should
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
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadWithResponse#BlobParallelUploadOptions -->
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
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;
     *     .setMaxConcurrency&#40;maxConcurrency&#41;;
     *
     * client.uploadWithResponse&#40;new BlobParallelUploadOptions&#40;data&#41;
     *     .setParallelTransferOptions&#40;parallelTransferOptions&#41;.setHeaders&#40;headers&#41;.setMetadata&#40;metadata&#41;
     *     .setTags&#40;tags&#41;.setTier&#40;AccessTier.HOT&#41;.setRequestConditions&#40;requestConditions&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Uploaded BlockBlob MD5 is %s%n&quot;,
     *         Base64.getEncoder&#40;&#41;.encodeToString&#40;response.getValue&#40;&#41;.getContentMd5&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadWithResponse#BlobParallelUploadOptions -->
     * <p>
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     *
     * @param options {@link BlobParallelUploadOptions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlockBlobItem>> uploadWithResponse(BlobParallelUploadOptions options) {
        try {
            StorageImplUtils.assertNotNull("options", options);
            // Can't use a Collections.emptyMap() because we add metadata for encryption.
            final Map<String, String> metadataFinal = options.getMetadata() == null
                ? new HashMap<>() : options.getMetadata();
            Flux<ByteBuffer> data = options.getDataFlux() == null ? Utility.convertStreamToByteBuffer(
                options.getDataStream(), options.getLength(), BLOB_DEFAULT_UPLOAD_BLOCK_SIZE, false)
                : options.getDataFlux();
            Flux<ByteBuffer> dataFinal = prepareToSendEncryptedRequest(data, metadataFinal);
            return super.uploadWithResponse(new BlobParallelUploadOptions(dataFinal)
                .setParallelTransferOptions(options.getParallelTransferOptions()).setHeaders(options.getHeaders())
                .setMetadata(metadataFinal).setTags(options.getTags()).setTier(options.getTier())
                .setRequestConditions(options.getRequestConditions())
                .setComputeMd5(options.isComputeMd5()));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a new block blob with the content of the specified file. By default, this method will not overwrite
     * existing data
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String -->
     * <pre>
     * client.uploadFromFile&#40;filePath&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String -->
     *
     * @param filePath Path to the upload file
     * @return An empty response
     */
    @Override
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
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;&#47; Default behavior
     * client.uploadFromFile&#40;filePath, overwrite&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String-boolean -->
     *
     * @param filePath Path to the upload file
     * @param overwrite Whether to overwrite should the blob exist.
     * @return An empty response
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> uploadFromFile(String filePath, boolean overwrite) {
        Mono<Void> uploadTask = uploadFromFile(filePath, null, null, null, null, null);

        if (overwrite) {
            return uploadTask;
        } else {
            return exists().flatMap(exists -> exists
                ? monoError(LOGGER, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                : uploadTask);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions -->
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
     *
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;;
     *
     * client.uploadFromFile&#40;filePath, parallelTransferOptions, headers, metadata, AccessTier.HOT, requestConditions&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions -->
     *
     * @param filePath Path to the upload file
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob. If there is leading or trailing whitespace in any metadata
     * key or value, it must be removed or encoded.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @return An empty response
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 4000 MB
     * @throws UncheckedIOException If an I/O error occurs
     */
    @Override
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
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFileWithResponse#BlobUploadFromFileOptions -->
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
     *
     * ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions&#40;&#41;
     *     .setBlockSizeLong&#40;blockSize&#41;;
     *
     * client.uploadFromFileWithResponse&#40;new BlobUploadFromFileOptions&#40;filePath&#41;
     *     .setParallelTransferOptions&#40;parallelTransferOptions&#41;.setHeaders&#40;headers&#41;.setMetadata&#40;metadata&#41;.setTags&#40;tags&#41;
     *     .setTier&#40;AccessTier.HOT&#41;.setRequestConditions&#40;requestConditions&#41;&#41;
     *     .doOnError&#40;throwable -&gt; System.err.printf&#40;&quot;Failed to upload from file %s%n&quot;, throwable.getMessage&#40;&#41;&#41;&#41;
     *     .subscribe&#40;completion -&gt; System.out.println&#40;&quot;Upload from file succeeded&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFileWithResponse#BlobUploadFromFileOptions -->
     *
     * @param options {@link BlobUploadFromFileOptions}
     * @return A reactive response containing the information of the uploaded block blob.
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 4000 MB
     * @throws UncheckedIOException If an I/O error occurs
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BlockBlobItem>> uploadFromFileWithResponse(BlobUploadFromFileOptions options) {
        try {
            StorageImplUtils.assertNotNull("options", options);
            return Mono.using(() -> UploadUtils.uploadFileResourceSupplier(options.getFilePath(), LOGGER),
                channel -> this.uploadWithResponse(new BlobParallelUploadOptions(FluxUtil.readFile(channel))
                        .setParallelTransferOptions(options.getParallelTransferOptions()).setHeaders(options.getHeaders())
                        .setMetadata(options.getMetadata()).setTags(options.getTags()).setTier(options.getTier())
                        .setRequestConditions(options.getRequestConditions()))
                    .doOnTerminate(() -> {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
                        }
                    }), channel -> UploadUtils.uploadFileCleanup(channel, LOGGER));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Encrypts the given Flux ByteBuffer.
     *
     * @param plainTextFlux The Flux ByteBuffer to be encrypted.
     * @return A {@link EncryptedBlob}
     */
    Mono<EncryptedBlob> encryptBlob(Flux<ByteBuffer> plainTextFlux) {
        Objects.requireNonNull(this.keyWrapper, "keyWrapper cannot be null");
        try {
            SecretKey aesKey = generateSecretKey();

            Map<String, String> keyWrappingMetadata = new HashMap<>();
            keyWrappingMetadata.put(CryptographyConstants.AGENT_METADATA_KEY,
                CryptographyConstants.AGENT_METADATA_VALUE);

            byte[] keyToWrap;
            switch (this.encryptionVersion) {
                case V2:
                    /*
                     * Prevent a downgrade attack by prepending the protocol version to the key (padded to 8 bytes) before
                     * wrapping.
                     */
                    ByteArrayOutputStream keyStream = new ByteArrayOutputStream((256 / 8) + 8);
                    keyStream.write(CryptographyConstants.ENCRYPTION_PROTOCOL_V2.getBytes(StandardCharsets.UTF_8));
                    for (int i = 0; i < 5; i++) {
                        keyStream.write(0);
                    }
                    keyStream.write(aesKey.getEncoded());
                    keyToWrap = keyStream.toByteArray();
                    break;
                case V1:
                    keyToWrap = aesKey.getEncoded();
                    break;
                default:
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException("Invalid encryption version: "
                        + this.encryptionVersion));
            }
            return keyWrapper.getKeyId().flatMap(keyId -> keyWrapper.wrapKey(keyWrapAlgorithm, keyToWrap)
                .map(encryptedKey -> {
                    WrappedKey wrappedKey = new WrappedKey(keyId, encryptedKey, keyWrapAlgorithm);

                    EncryptionData encryptionData;
                    Flux<ByteBuffer> encryptedTextFlux;
                    switch (this.encryptionVersion) {
                        case V1:
                            Cipher cbcCipher;
                            try {
                                cbcCipher = generateCBCCipher(aesKey);
                            } catch (GeneralSecurityException e) {
                                throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
                            }
                            // Build EncryptionData
                            encryptionData = new EncryptionData()
                                .setEncryptionMode(CryptographyConstants.ENCRYPTION_MODE)
                                .setEncryptionAgent(new EncryptionAgent(CryptographyConstants.ENCRYPTION_PROTOCOL_V1,
                                    EncryptionAlgorithm.AES_CBC_256))
                                .setKeyWrappingMetadata(keyWrappingMetadata)
                                .setContentEncryptionIV(cbcCipher.getIV())
                                .setWrappedContentKey(wrappedKey);

                            encryptedTextFlux = encryptV1(plainTextFlux, cbcCipher);
                            break;
                        case V2:
                            // Build EncryptionData
                            encryptionData = new EncryptionData()
                                .setEncryptionMode(CryptographyConstants.ENCRYPTION_MODE)
                                .setEncryptionAgent(new EncryptionAgent(CryptographyConstants.ENCRYPTION_PROTOCOL_V2,
                                    EncryptionAlgorithm.AES_GCM_256))
                                .setKeyWrappingMetadata(keyWrappingMetadata)
                                .setEncryptedRegionInfo(new EncryptedRegionInfo(
                                    GCM_ENCRYPTION_REGION_LENGTH, NONCE_LENGTH))
                                .setWrappedContentKey(wrappedKey);

                            encryptedTextFlux = encryptV2(plainTextFlux, aesKey);

                            break;
                        default:
                            throw LOGGER.logExceptionAsError(new IllegalStateException("Encryption version not supported"));
                    }

                    return new EncryptedBlob(encryptionData, encryptedTextFlux);
                }));
        } catch (GeneralSecurityException | IOException e) {
            // These are hardcoded and guaranteed to work. There is no reason to propagate a checked exception.
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private Flux<ByteBuffer> encryptV2(Flux<ByteBuffer> plainTextFlux, SecretKey aesKey) {
        Flux<ByteBuffer> encryptedTextFlux;
        BufferStagingArea stagingArea =
            new BufferStagingArea(GCM_ENCRYPTION_REGION_LENGTH, GCM_ENCRYPTION_REGION_LENGTH);

        encryptedTextFlux =
            UploadUtils.chunkSource(plainTextFlux,
                    new com.azure.storage.common.ParallelTransferOptions()
                        .setBlockSizeLong((long) GCM_ENCRYPTION_REGION_LENGTH))
                .flatMapSequential(stagingArea::write)
                .concatWith(Flux.defer(stagingArea::flush))
                .index()
                .flatMapSequential(tuple -> {
                    Cipher gcmCipher;
                    try {
                        // We use the index as the nonce as a counter guarantees each nonce is used
                        // only once with a given key.
                        gcmCipher = generateGCMCipher(aesKey, tuple.getT1());
                    } catch (NoSuchPaddingException | NoSuchAlgorithmException
                        | InvalidAlgorithmParameterException | InvalidKeyException e) {
                        throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
                    }

                    // Expected size of each encryption region after calling doFinal. Last one may
                    // be less, will never be more.
                    ByteBuffer encryptedRegion = ByteBuffer.allocate(
                        GCM_ENCRYPTION_REGION_LENGTH + TAG_LENGTH);

                    // Each flux is at most 1 BufferAggregator of 4mb
                    Flux<ByteBuffer> cipherTextWithTag = tuple.getT2()
                        .asFlux()
                        .map(buffer -> {
                            // Write into the preallocated buffer and always return this buffer.
                            try {
                                gcmCipher.update(buffer, encryptedRegion);
                            } catch (ShortBufferException e) {
                                throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
                            }
                            return encryptedRegion;
                        })
                        .then(Mono.fromCallable(() -> {
                            // We have already written all the data to the cipher. Passing in a final
                            // empty buffer allows us to force completion and return the filled buffer.
                            gcmCipher.doFinal(ByteBuffer.allocate(0), encryptedRegion);
                            encryptedRegion.flip();
                            return encryptedRegion;
                        })).flux();

                    return Flux.concat(Flux.just(ByteBuffer.wrap(gcmCipher.getIV())),
                        cipherTextWithTag);
                }, 1, 1);
        return encryptedTextFlux;
    }

    private Flux<ByteBuffer> encryptV1(Flux<ByteBuffer> plainTextFlux, Cipher cbcCipher) {
        Flux<ByteBuffer> encryptedTextFlux;
        // Encrypt plain text with content encryption key
        encryptedTextFlux = plainTextFlux.map(plainTextBuffer -> {
            int outputSize = cbcCipher.getOutputSize(plainTextBuffer.remaining());

            /*
             * This should be the only place we allocate memory in encryptBlob(). Although there is an
             * overload that can encrypt in place that would save allocations, we do not want to overwrite
             * customer's memory, so we must allocate our own memory. If memory usage becomes unreasonable,
             * we should implement pooling.
             */
            ByteBuffer encryptedTextBuffer = ByteBuffer.allocate(outputSize);

            int encryptedBytes;
            try {
                encryptedBytes = cbcCipher.update(plainTextBuffer, encryptedTextBuffer);
            } catch (ShortBufferException e) {
                throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
            }
            encryptedTextBuffer.position(0);
            encryptedTextBuffer.limit(encryptedBytes);
            return encryptedTextBuffer;
        });

        /*
         * Defer() ensures the contained code is not executed until the Flux is subscribed to, in
         * other words, cipher.doFinal() will not be called until the plainTextFlux has completed
         * and therefore all other data has been encrypted.
         */
        encryptedTextFlux = Flux.concat(encryptedTextFlux,
            Mono.fromCallable(() -> ByteBuffer.wrap(cbcCipher.doFinal())));
        return encryptedTextFlux;
    }

    SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(CryptographyConstants.AES);
        keyGen.init(256);

        return keyGen.generateKey();
    }

    Cipher generateCBCCipher(SecretKey aesKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5PADDING);

        // Generate content encryption key
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        return cipher;
    }

    Cipher generateGCMCipher(SecretKey aesKey, long index) throws NoSuchPaddingException, NoSuchAlgorithmException,
        InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        byte[] iv = ByteBuffer.allocate(12).putLong(index).array(); // standard gcm nonce is 12 bytes per James' doc

        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_LENGTH * 8, iv));
        return cipher;
    }

    /**
     * Encrypt the blob and add the encryption metadata to the customer's metadata.
     *
     * @param plainText The data to encrypt
     * @param metadata The customer's metadata to be updated.
     * @return A Mono containing the cipher text
     */
    private Flux<ByteBuffer> prepareToSendEncryptedRequest(Flux<ByteBuffer> plainText,
        Map<String, String> metadata) {
        return this.encryptBlob(plainText).flatMapMany(encryptedBlob -> {
            try {
                metadata.put(ENCRYPTION_DATA_KEY, encryptedBlob.getEncryptionData().toJsonString());
                return encryptedBlob.getCiphertextFlux();
            } catch (JsonProcessingException e) {
                throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
            }
        });
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public Flux<ByteBuffer> query(String expression) {
        // This is eagerly thrown instead of waiting for the subscription to happen.
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side"));
    }

    /**
     * Unsupported. Cannot query data encrypted on client side.
     */
    @Override
    public Mono<BlobQueryAsyncResponse> queryWithResponse(BlobQueryOptions queryOptions) {
        // This is eagerly thrown instead of waiting for the subscription to happen.
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException(
            "Cannot query data encrypted on client side"));
    }
}
