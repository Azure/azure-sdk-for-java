// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.implementation.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * This class provides a client side encryption client that contains generic blob operations for Azure Storage Blobs.
 * Operations allowed by the client are uploading, downloading and copying a blob, retrieving and setting metadata,
 * retrieving and setting HTTP headers, and deleting and un-deleting a blob. The upload and download operation allow for
 * encryption and decryption of the data client side. Note: setting metadata in particular is unsafe and should only be
 * done so with caution.
 * <p> Please refer to the
 * <a href=https://docs.microsoft.com/en-us/azure/storage/common/storage-client-side-encryption-java>Azure
 * Docs For Client-Side Encryption</a> for more information.
 *
 * <p>
 * This client is instantiated through {@link EncryptedBlobClientBuilder}
 *
 * <p>
 * For operations on a specific blob type (i.e append, block, or page) use
 * {@link #getAppendBlobAsyncClient() getAppendBlobAsyncClient}, {@link #getBlockBlobAsyncClient()
 * getBlockBlobAsyncClient}, or {@link #getPageBlobAsyncClient() getPageBlobAsyncClient} to construct a client that
 * allows blob specific operations. Note, these types do not support client-side encryption, though decryption is
 * possible in case the associated block/page/append blob contains encrypted data.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = EncryptedBlobClientBuilder.class, isAsync = true)
public class EncryptedBlobAsyncClient extends BlobAsyncClient {

    static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;
    private static final int BLOB_MAX_UPLOAD_BLOCK_SIZE = 100 * Constants.MB;
    private final ClientLogger logger = new ClientLogger(EncryptedBlobAsyncClient.class);

    /**
     * An object of type {@link AsyncKeyEncryptionKey} that is used to wrap/unwrap the content key during encryption.
     */
    private final AsyncKeyEncryptionKey keyWrapper;

    /**
     * A {@link String} that is used to wrap/unwrap the content key during encryption.
     */
    private final String keyWrapAlgorithm;

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
     * @param key The key used to encrypt and decrypt data.
     * @param keyWrapAlgorithm The algorithm used to wrap/unwrap the key during encryption.
     */
    EncryptedBlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName,
        String containerName, String blobName, String snapshot, AsyncKeyEncryptionKey key, String keyWrapAlgorithm) {
        super(pipeline, url, serviceVersion, accountName, containerName, blobName, snapshot, null);

        this.keyWrapper = key;
        this.keyWrapAlgorithm = keyWrapAlgorithm;
    }

    /**
     * Creates a new block blob. By default this method will not overwrite an existing blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with this method; the content of the existing blob is overwritten with the new content. To perform a partial
     * update of block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
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
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.upload#Flux-ParallelTransferOptions}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions) {
        try {
            return this.upload(data, parallelTransferOptions, false);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob.
     * <p>
     * Updating an existing block blob overwrites any existing metadata on the blob. Partial updates are not supported
     * with this method; the content of the existing blob is overwritten with the new content. To perform a partial
     * update of block blob's, use {@link BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and {@link
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
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.upload#Flux-ParallelTransferOptions-boolean}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param overwrite Whether or not to overwrite, should data exist on the blob.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions,
        boolean overwrite) {
        try {
            Mono<BlockBlobItem> uploadTask = this.uploadWithResponse(data, parallelTransferOptions, null, null, null,
                null).flatMap(FluxUtil::toMono);

            if (overwrite) {
                return uploadTask;
            } else {
                return exists()
                    .flatMap(exists -> exists
                        ? monoError(logger, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                        : uploadTask);
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob. Updating an existing block blob
     * overwrites any existing metadata on the blob. Partial updates are not supported with this method; the content of
     * the existing blob is overwritten with the new content. To perform a partial update of a block blob's, use {@link
     * BlockBlobAsyncClient#stageBlock(String, Flux, long) stageBlock} and
     * {@link BlockBlobAsyncClient#commitBlockList(List)}, which this method uses internally. For more information,
     * see the <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure
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
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data,
        ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers, Map<String, String> metadata,
        AccessTier tier, BlobRequestConditions requestConditions) {
        try {
            final Map<String, String> metadataFinal = metadata == null ? new HashMap<>() : metadata;
            Mono<Flux<ByteBuffer>> dataFinal = prepareToSendEncryptedRequest(data, metadataFinal);
            return dataFinal.flatMap(df -> super.uploadWithResponse(df, parallelTransferOptions, headers, metadataFinal,
                tier, requestConditions));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob with the content of the specified file. By default this method will not overwrite
     * existing data
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String}
     *
     * @param filePath Path to the upload file
     * @return An empty response
     */
    public Mono<Void> uploadFromFile(String filePath) {
        try {
            return uploadFromFile(filePath, false);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String-boolean}
     *
     * @param filePath Path to the upload file
     * @param overwrite Whether or not to overwrite should data exist on the blob.
     * @return An empty response
     */
    public Mono<Void> uploadFromFile(String filePath, boolean overwrite) {
        try {
            Mono<Void> uploadTask = uploadFromFile(filePath, null, null, null, null, null);

            if (overwrite) {
                return uploadTask;
            } else {
                return exists()
                    .flatMap(exists -> exists
                        ? monoError(logger, new IllegalArgumentException(Constants.BLOB_ALREADY_EXISTS))
                        : uploadTask);
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions}
     *
     * @param filePath Path to the upload file
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file.
     * @param headers {@link BlobHttpHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param requestConditions {@link BlobRequestConditions}
     * @return An empty response
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB
     * @throws UncheckedIOException If an I/O error occurs
     */
    public Mono<Void> uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier,
        BlobRequestConditions requestConditions) {
        try {
            final Map<String, String> metadataFinal = metadata == null ? new HashMap<>() : metadata;

            return Mono.using(() -> super.uploadFileResourceSupplier(filePath),
                channel -> this.uploadWithResponse(FluxUtil.readFile(channel), parallelTransferOptions, headers,
                    metadataFinal, tier, requestConditions)
                    .then()
                    .doOnTerminate(() -> {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            throw logger.logExceptionAsError(new UncheckedIOException(e));
                        }
                    }), this::uploadFileCleanup);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private void uploadFileCleanup(AsynchronousFileChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Encrypts the given Flux ByteBuffer.
     *
     * @param plainTextFlux The Flux ByteBuffer to be encrypted.
     *
     * @return A {@link EncryptedBlob}
     *
     * @throws InvalidKeyException If the key provided is invalid
     */
    Mono<EncryptedBlob> encryptBlob(Flux<ByteBuffer> plainTextFlux) throws InvalidKeyException {
        Objects.requireNonNull(this.keyWrapper, "keyWrapper cannot be null");
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(CryptographyConstants.AES);
            keyGen.init(256);

            Cipher cipher = Cipher.getInstance(CryptographyConstants.AES_CBC_PKCS5PADDING);

            // Generate content encryption key
            SecretKey aesKey = keyGen.generateKey();
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            Map<String, String> keyWrappingMetadata = new HashMap<>();
            keyWrappingMetadata.put(CryptographyConstants.AGENT_METADATA_KEY,
                CryptographyConstants.AGENT_METADATA_VALUE);

            return this.keyWrapper.wrapKey(keyWrapAlgorithm, aesKey.getEncoded())
                .map(encryptedKey -> {
                    WrappedKey wrappedKey = new WrappedKey(
                        this.keyWrapper.getKeyId().block(), encryptedKey, keyWrapAlgorithm);

                    // Build EncryptionData
                    EncryptionData encryptionData = new EncryptionData()
                        .setEncryptionMode(CryptographyConstants.ENCRYPTION_MODE)
                        .setEncryptionAgent(
                            new EncryptionAgent(CryptographyConstants.ENCRYPTION_PROTOCOL_V1,
                                EncryptionAlgorithm.AES_CBC_256))
                        .setKeyWrappingMetadata(keyWrappingMetadata)
                        .setContentEncryptionIV(cipher.getIV())
                        .setWrappedContentKey(wrappedKey);

                    // Encrypt plain text with content encryption key
                    Flux<ByteBuffer> encryptedTextFlux = plainTextFlux.map(plainTextBuffer -> {
                        int outputSize = cipher.getOutputSize(plainTextBuffer.remaining());

                        /*
                        This should be the only place we allocate memory in encryptBlob(). Although there is an
                        overload that can encrypt in place that would save allocations, we do not want to overwrite
                        customer's memory, so we must allocate our own memory. If memory usage becomes unreasonable,
                        we should implement pooling.
                         */
                        ByteBuffer encryptedTextBuffer = ByteBuffer.allocate(outputSize);

                        int encryptedBytes;
                        try {
                            encryptedBytes = cipher.update(plainTextBuffer, encryptedTextBuffer);
                        } catch (ShortBufferException e) {
                            throw logger.logExceptionAsError(Exceptions.propagate(e));
                        }
                        encryptedTextBuffer.position(0);
                        encryptedTextBuffer.limit(encryptedBytes);
                        return encryptedTextBuffer;
                    });

                    /*
                    Defer() ensures the contained code is not executed until the Flux is subscribed to, in
                    other words, cipher.doFinal() will not be called until the plainTextFlux has completed
                    and therefore all other data has been encrypted.
                     */
                    encryptedTextFlux = Flux.concat(encryptedTextFlux, Flux.defer(() -> {
                        try {
                            return Flux.just(ByteBuffer.wrap(cipher.doFinal()));
                        } catch (GeneralSecurityException e) {
                            throw logger.logExceptionAsError(Exceptions.propagate(e));
                        }
                    }));
                    return new EncryptedBlob(encryptionData, encryptedTextFlux);
                });
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            // These are hardcoded and guaranteed to work. There is no reason to propogate a checked exception.
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Encrypt the blob and add the encryption metadata to the customer's metadata.
     *
     * @param plainText The data to encrypt
     * @param metadata The customer's metadata to be updated.
     *
     * @return A Mono containing the cipher text
     */
    private Mono<Flux<ByteBuffer>> prepareToSendEncryptedRequest(Flux<ByteBuffer> plainText,
        Map<String, String> metadata) {
        try {
            return this.encryptBlob(plainText)
                .flatMap(encryptedBlob -> {
                    try {
                        metadata.put(CryptographyConstants.ENCRYPTION_DATA_KEY,
                            encryptedBlob.getEncryptionData().toJsonString());
                        return Mono.just(encryptedBlob.getCiphertextFlux());
                    } catch (JsonProcessingException e) {
                        throw logger.logExceptionAsError(Exceptions.propagate(e));
                    }
                });
        } catch (InvalidKeyException e) {
            throw logger.logExceptionAsError(Exceptions.propagate(e));
        }
    }
}
