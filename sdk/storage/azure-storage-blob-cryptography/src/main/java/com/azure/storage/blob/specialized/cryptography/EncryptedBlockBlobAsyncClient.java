// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.Constants;
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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EncryptedBlockBlobAsyncClient extends BlobAsyncClient {

    static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;
    private static final int BLOB_MAX_UPLOAD_BLOCK_SIZE = 100 * Constants.MB;
    private final ClientLogger logger = new ClientLogger(EncryptedBlockBlobAsyncClient.class);

    /**
     * An object of type {@link AsyncKeyEncryptionKey} that is used to wrap/unwrap the content key during encryption.
     */
    private final AsyncKeyEncryptionKey keyWrapper;

    /**
     * A {@link KeyWrapAlgorithm} that is used to wrap/unwrap the content key during encryption.
     */
    private final KeyWrapAlgorithm keyWrapAlgorithm;

    /**
     * Package-private constructor for use by {@link EncryptedBlobClientBuilder}.
     */
    EncryptedBlockBlobAsyncClient(AzureBlobStorageImpl constructImpl, String snapshot, String accountName,
        AsyncKeyEncryptionKey key, KeyWrapAlgorithm keyWrapAlgorithm) {
        super(constructImpl, snapshot, null, accountName);
        this.keyWrapper = key;
        this.keyWrapAlgorithm = keyWrapAlgorithm;
    }

    public BlockBlobAsyncClient getBlockBlobAsyncClient() {
        return new BlobClientBuilder()
            .pipeline(removeDecryptionPolicy(getHttpPipeline(),
                getHttpPipeline().getHttpClient()))
            .endpoint(getBlobUrl())
            .buildAsyncClient()
            .getBlockBlobAsyncClient();
    }

    static HttpPipeline removeDecryptionPolicy(HttpPipeline originalPipeline, HttpClient client) {
        HttpPipelinePolicy[] policies = new HttpPipelinePolicy[originalPipeline.getPolicyCount() - 1];
        int index = 0;
        for (int i = 0; i < originalPipeline.getPolicyCount(); i++) {
            if (!(originalPipeline.getPolicy(i) instanceof BlobDecryptionPolicy)) {
                policies[index] = originalPipeline.getPolicy(i);
            } else {
                index--;
            }
            index++;
        }
        return new HttpPipelineBuilder()
            .httpClient(client)
            .policies(policies)
            .build();
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
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.upload#Flux-ParallelTransferOptions}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @return A reactive response containing the information of the uploaded block blob.
     */
    public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions) {
        return this.uploadWithResponse(data, parallelTransferOptions, null, null, null, null).flatMap(FluxUtil::toMono);
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
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions}
     *
     * @param data The data to write to the blob. Unlike other upload methods, this method does not require that the
     * {@code Flux} be replayable. In other words, it does not have to support multiple subscribers and is not expected
     * to produce the same values across subscriptions.
     * @param parallelTransferOptions {@link ParallelTransferOptions} used to configure buffered uploading.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return A reactive response containing the information of the uploaded block blob.
     */
    // TODO (gapra) : Investigate best way to reuse all the code in the RegularBlobClient.
    public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data,
        ParallelTransferOptions parallelTransferOptions, BlobHTTPHeaders headers, Map<String, String> metadata,
        AccessTier tier, BlobAccessConditions accessConditions) {

        final Map<String, String> metadataFinal = metadata == null ? new HashMap<>() : metadata;
        Mono<Flux<ByteBuffer>> dataFinal = prepareToSendEncryptedRequest(data, metadataFinal);
        return dataFinal.flatMap(df -> super.uploadWithResponse(df, parallelTransferOptions, headers, metadataFinal,
           tier, accessConditions));
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadFromFile#String}
     *
     * @param filePath Path to the upload file
     * @return An empty response
     */
    public Mono<Void> uploadFromFile(String filePath) {
        return uploadFromFile(filePath, null, null, null, null, null);
    }

    /**
     * Creates a new block blob, or updates the content of an existing block blob, with the content of the specified
     * file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions}
     *
     * @param filePath Path to the upload file
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to upload from file. Number of parallel
     *        transfers parameter is ignored.
     * @param headers {@link BlobHTTPHeaders}
     * @param metadata Metadata to associate with the blob.
     * @param tier {@link AccessTier} for the destination blob.
     * @param accessConditions {@link BlobAccessConditions}
     * @return An empty response
     * @throws IllegalArgumentException If {@code blockSize} is less than 0 or greater than 100MB
     * @throws UncheckedIOException If an I/O error occurs
     */
    // TODO (gapra) : Investigate if this is can be parallelized, and include the parallelTransfers parameter.
    public Mono<Void> uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions,
        BlobHTTPHeaders headers, Map<String, String> metadata, AccessTier tier, BlobAccessConditions accessConditions) {
        final Map<String, String> metadataFinal = metadata == null ? new HashMap<>() : metadata;
        final ParallelTransferOptions finalParallelTransferOptions = parallelTransferOptions == null
            ? new ParallelTransferOptions()
            : parallelTransferOptions;

        return Mono.using(() -> uploadFileResourceSupplier(filePath),
            channel -> this.uploadWithResponse(FluxUtil.readFile(channel), finalParallelTransferOptions, headers,
                metadataFinal, tier, accessConditions)
                .then()
                .doOnTerminate(() -> {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        throw logger.logExceptionAsError(new UncheckedIOException(e));
                    }
                }), this::uploadFileCleanup);
    }

    private AsynchronousFileChannel uploadFileResourceSupplier(String filePath) {
        try {
            return AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
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
        Objects.requireNonNull(this.keyWrapper);
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

            return this.keyWrapper.wrapKey(keyWrapAlgorithm.toString(), aesKey.getEncoded())
                .map(encryptedKey -> {
                    WrappedKey wrappedKey = new WrappedKey(
                        this.keyWrapper.getKeyId().block(), encryptedKey, keyWrapAlgorithm.toString());

                    // Build EncryptionData
                    EncryptionData encryptionData = new EncryptionData()
                        .withEncryptionMode(CryptographyConstants.ENCRYPTION_MODE)
                        .withEncryptionAgent(
                            new EncryptionAgent(CryptographyConstants.ENCRYPTION_PROTOCOL_V1,
                                EncryptionAlgorithm.AES_CBC_256))
                        .withKeyWrappingMetadata(keyWrappingMetadata)
                        .withContentEncryptionIV(cipher.getIV())
                        .withWrappedContentKey(wrappedKey);

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
    Mono<Flux<ByteBuffer>> prepareToSendEncryptedRequest(Flux<ByteBuffer> plainText,
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
