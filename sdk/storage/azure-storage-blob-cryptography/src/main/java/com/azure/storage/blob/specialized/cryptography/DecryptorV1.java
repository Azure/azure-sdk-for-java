// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_CBC_NO_PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_CBC_PKCS5PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_BLOCK_SIZE;

class DecryptorV1 extends Decryptor {
    private static final ClientLogger LOGGER = new ClientLogger(DecryptorV1.class);
    private LogLevel v1UsageLogLevel;

    protected DecryptorV1(AsyncKeyEncryptionKeyResolver keyResolver, AsyncKeyEncryptionKey keyWrapper,
        EncryptionData encryptionData) {
        super(keyResolver, keyWrapper, encryptionData);
        this.v1UsageLogLevel = LogLevel.WARNING;
    }

    @Override
    Flux<ByteBuffer> decrypt(Flux<ByteBuffer> encryptedFlux, EncryptedBlobRange encryptedBlobRange,
        boolean padding, String requestUri, AtomicLong totalInputBytes, byte[] contentEncryptionKey) {
        LOGGER.log(this.v1UsageLogLevel, () -> "Downloaded data found to be encrypted with v1 encryption, "
            + "which is no longer secure. Uri: " + requestUri);
        this.v1UsageLogLevel = LogLevel.INFORMATIONAL; // Log subsequently at a lower level to not pollute logs
        /*
         * Calculate the IV.
         *
         * If we are starting at the beginning, we can grab the IV from the encryptionData. Otherwise,
         * Reactor makes it difficult to grab the first 16 bytes of data to pass as an IV to the cipher.
         * As a workaround, we initialize the cipher with a garbage IV (empty byte array) and attempt to
         * decrypt the first 16 bytes (the actual IV for the relevant data). We throw away this "decrypted"
         * data. Now, though, because each block of 16 is used as the IV for the next, the original 16 bytes
         * of downloaded data are in position to be used as the IV for the data actually requested and we
         * are in the desired state.
         */
        byte[] iv;

        /*
         * Adjusting the range by <= 16 means we only adjusted to align on an encryption block boundary
         * (padding will add 1-16 bytes as it will prefer to pad 16 bytes instead of 0) and therefore the
         * key is in the metadata.
         */
        if (encryptedBlobRange.getOffsetAdjustment() <= ENCRYPTION_BLOCK_SIZE) {
            iv = encryptionData.getContentEncryptionIV();
        } else {
            // Rather than try to buffer just the 16 bytes of the iv, we "decrypt" them with this garbage iv.
            // This makes counting easier.
            // We end up throwing that garbage decrypted data away when we trim the cipher uses the ciphertext as the iv
            // for the data we actually want.
            iv = new byte[ENCRYPTION_BLOCK_SIZE];
        }

        Cipher cipher;
        try {
            cipher = getCipher(contentEncryptionKey, iv, padding);
        } catch (InvalidKeyException e) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
        }

        return encryptedFlux.map(encryptedByteBuffer -> {
            /*
             * If we could potentially decrypt more bytes than encryptedByteBuffer can hold, allocate more
             * room. Note that, because getOutputSize returns the size needed to store
             * max(updateOutputSize, finalizeOutputSize), this is likely to produce a ByteBuffer slightly
             * larger than what the real outputSize is. This is accounted for below.
             */
            ByteBuffer plaintextByteBuffer = ByteBuffer
                .allocate(cipher.getOutputSize(encryptedByteBuffer.remaining()));

            // First, determine if we should update or finalize and fill the output buffer.
            int bytesToInput = encryptedByteBuffer.remaining();
            try {
                // We will have reached the end of the downloaded range, finalize.
                if (totalInputBytes.longValue() + bytesToInput >= encryptedBlobRange.getAdjustedDownloadCount()) {
                    cipher.doFinal(encryptedByteBuffer, plaintextByteBuffer);
                } else {
                    // We won't reach the end of the downloaded range, update.
                    cipher.update(encryptedByteBuffer, plaintextByteBuffer);
                }
            } catch (GeneralSecurityException e) {
                throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
            }
            totalInputBytes.addAndGet(bytesToInput);

            // Flip the buffer to set the position back to 0 and set the limit to the data size.
            plaintextByteBuffer.flip();
            return plaintextByteBuffer;
        });
    }

    @Override
    protected Cipher getCipher(byte[] contentEncryptionKey, byte[] iv, boolean padding)
        throws InvalidKeyException {
        // validate encryption data
        try {
            SecretKey keySpec = new SecretKeySpec(contentEncryptionKey, 0, contentEncryptionKey.length,
                AES);
            Cipher cipher;
            if (padding) {
                cipher = Cipher.getInstance(AES_CBC_PKCS5PADDING);
            } else {
                cipher = Cipher.getInstance(AES_CBC_NO_PADDING);
            }
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
            return cipher;
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
        }
    }
}
