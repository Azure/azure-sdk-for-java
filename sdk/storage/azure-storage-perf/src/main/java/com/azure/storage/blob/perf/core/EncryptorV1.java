// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Map;

class EncryptorV1 extends Encryptor {
    private static final ClientLogger LOGGER = new ClientLogger(EncryptorV1.class);

    private final Cipher cipher;

    /**
     * Constructs a new encryptor.
     *
     * @param aesKey The aes key that serves as the CEK.
     * @throws GeneralSecurityException If cipher initialization fails.
     */
    protected EncryptorV1(SecretKey aesKey) throws GeneralSecurityException {
        super(aesKey);
        cipher = this.getCipher();
    }

    @Override
    byte[] getKeyToWrap() {
        return aesKey.getEncoded();
    }

    @Override
    protected EncryptionData buildEncryptionData(Map<String, String> keyWrappingMetadata, WrappedKey wrappedKey) {
        return super.buildEncryptionData(keyWrappingMetadata, wrappedKey)
            .setEncryptionAgent(new EncryptionAgent("1.0",
                EncryptionAlgorithm.AES_CBC_256))
            .setContentEncryptionIV(cipher.getIV());
    }

    private Cipher getCipher() throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Generate content encryption key
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        return cipher;
    }

    @Override
    protected Flux<ByteBuffer> encrypt(Flux<ByteBuffer> plainTextFlux) throws GeneralSecurityException {
        Flux<ByteBuffer> encryptedTextFlux;
        // Encrypt plain text with content encryption key
        encryptedTextFlux = plainTextFlux.map(plainTextBuffer -> {
            int outputSize = cipher.getOutputSize(plainTextBuffer.remaining());

            /*
             * This should be the only place we allocate memory in encryptBlob(). Although there is an
             * overload that can encrypt in place that would save allocations, we do not want to overwrite
             * customer's memory, so we must allocate our own memory. If memory usage becomes unreasonable,
             * we should implement pooling.
             */
            ByteBuffer encryptedTextBuffer = ByteBuffer.allocate(outputSize);

            int encryptedBytes;
            try {
                encryptedBytes = cipher.update(plainTextBuffer, encryptedTextBuffer);
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
            Mono.fromCallable(() -> ByteBuffer.wrap(cipher.doFinal())));
        return encryptedTextFlux;
    }


}
