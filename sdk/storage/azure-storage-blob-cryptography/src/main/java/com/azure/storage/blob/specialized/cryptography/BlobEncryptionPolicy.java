// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
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
final class BlobEncryptionPolicy {
    private final ClientLogger logger = new ClientLogger(BlobEncryptionPolicy.class);

    /**
     * An object of type {@link AsyncKeyEncryptionKey} that is used to wrap/unwrap the content key during encryption.
     */
    private final AsyncKeyEncryptionKey keyWrapper;

    /**
     * A {@link KeyWrapAlgorithm} that is used to wrap/unwrap the content key during encryption.
     */
    private final KeyWrapAlgorithm keyWrapAlgorithm;

    /**
     * Initializes a new instance of the {@link BlobEncryptionPolicy} class with the specified key.
     * <p>
     * If the generated policy is intended to be used for encryption, users are expected to provide a key at the
     * minimum. The absence of key will cause an exception to be thrown during encryption. If the generated policy is
     * intended to be used for decryption, users can provide a keyResolver. The client library will - 1. Invoke the key
     * resolver if specified to get the key. 2. If resolver is not specified but a key is specified, match the key id on
     * the key and use it.
     *
     * @param key An object of type {@link AsyncKeyEncryptionKey} that is used to wrap/unwrap the content encryption key
     * @param keyWrapAlgorithm A {@link KeyWrapAlgorithm} that is used to wrap/unwrap the content key during encryption.
     */
    BlobEncryptionPolicy(AsyncKeyEncryptionKey key, KeyWrapAlgorithm keyWrapAlgorithm) {
        this.keyWrapper = key;
        this.keyWrapAlgorithm = keyWrapAlgorithm;
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
        Metadata metadata) {
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
