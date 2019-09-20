// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.cryptography;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.Metadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a blob encryption policy that is used to perform envelope encryption of Azure storage blobs.
 * <a href="https://docs.microsoft.com/en-us/azure/storage/common/storage-client-side-encryption-java?toc=%2fazure%2fstorage%2fblobs%2ftoc.json">Azure Docs</a>
 * for more information.
 */
final class BlobEncryption {
    private final ClientLogger logger = new ClientLogger(BlobEncryption.class);

    /**
     * An object of type {@link IKey} that is used to wrap/unwrap the content key during encryption.
     */
    private final IKey keyWrapper;

    /**
     * Initializes a new instance of the {@link BlobEncryption} class with the specified key.
     * <p>
     * If the generated policy is intended to be used for encryption, users are expected to provide a key at the
     * minimum. The absence of key will cause an exception to be thrown during encryption. If the generated policy is
     * intended to be used for decryption, users can provide a keyResolver. The client library will - 1. Invoke the key
     * resolver if specified to get the key. 2. If resolver is not specified but a key is specified, match the key id on
     * the key and use it.
     *
     * @param key An object of type {@link IKey} that is used to wrap/unwrap the content encryption key.
     */
    BlobEncryption(IKey key) {
        this.keyWrapper = key;
    }

    /**
     * Encrypts the given Flux ByteBuffer.
     *
     * @param plainTextFlux The Flux ByteBuffer to be encrypted.
     *
     * @return A {@link EncryptedBlob}
     *
     * @throws InvalidKeyException
     */
    Mono<EncryptedBlob> encryptBlob(Flux<ByteBuffer> plainTextFlux) throws InvalidKeyException {
        Objects.requireNonNull(this.keyWrapper);
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);

            Cipher cipher = Cipher.getInstance(EncryptionConstants.AES_CBC_PKCS5PADDING);

            // Generate content encryption key
            SecretKey aesKey = keyGen.generateKey();
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            Map<String, String> keyWrappingMetadata = new HashMap<>();
            keyWrappingMetadata.put(EncryptionConstants.AGENT_METADATA_KEY, EncryptionConstants.AGENT_METADATA_VALUE);

            return this.keyWrapper.wrapKeyAsync(aesKey.getEncoded(), null /* algorithm */)
                .map(encryptedKey -> {
                    WrappedKey wrappedKey = new WrappedKey(
                        this.keyWrapper.getKid(), encryptedKey.getT1(), encryptedKey.getT2());

                    // Build EncryptionData
                    EncryptionData encryptionData = new EncryptionData()
                        .withEncryptionMode(EncryptionConstants.ENCRYPTION_MODE)
                        .withEncryptionAgent(
                            new EncryptionAgent(EncryptionConstants.ENCRYPTION_PROTOCOL_V1,
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
        }
        // These are hardcoded and guaranteed to work. There is no reason to propogate a checked exception.
        catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
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
        Metadata metadata) {
        try {
            return this.encryptBlob(plainText)
                .flatMap(encryptedBlob -> {
                    try {
                        metadata.put(EncryptionConstants.ENCRYPTION_DATA_KEY,
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
