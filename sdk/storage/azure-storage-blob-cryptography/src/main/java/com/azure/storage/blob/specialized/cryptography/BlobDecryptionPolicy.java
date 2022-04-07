// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_BLOCK_SIZE;

/**
 * This is a decryption policy in an {@link com.azure.core.http.HttpPipeline} to decrypt data in an {@link
 * EncryptedBlobAsyncClient} or {@link EncryptedBlobClient} download request. The range will be expanded for decryption
 * purposes and then decrypt the body when the response comes in.
 */
public class BlobDecryptionPolicy implements HttpPipelinePolicy {

    private final ClientLogger logger = new ClientLogger(BlobDecryptionPolicy.class);

    /**
     * The {@link AsyncKeyEncryptionKeyResolver} used to select the correct key for decrypting existing blobs.
     */
    private final AsyncKeyEncryptionKeyResolver keyResolver;

    /**
     * An object of type {@link AsyncKeyEncryptionKey} that is used to wrap/unwrap the content key during encryption.
     */
    private final AsyncKeyEncryptionKey keyWrapper;

    /**
     * Whether or not encryption is enforced by this client. Throws an exception if data is downloaded and it is not
     * encrypted.
     */
    private final boolean requiresEncryption;

    /**
     * Initializes a new instance of the {@link BlobDecryptionPolicy} class with the specified key and resolver.
     * <p>
     * If the generated policy is intended to be used for encryption, users are expected to provide a key at the
     * minimum. The absence of key will cause an exception to be thrown during encryption. If the generated policy is
     * intended to be used for decryption, users can provide a keyResolver. The client library will - 1. Invoke the key
     * resolver if specified to get the key. 2. If resolver is not specified but a key is specified, match the key id on
     * the key and use it.
     *
     * @param key An object of type {@link AsyncKeyEncryptionKey} that is used to wrap/unwrap the content encryption
     * key
     * @param keyResolver The key resolver used to select the correct key for decrypting existing blobs.
     * @param requiresEncryption Whether or not encryption is enforced by this client.
     */
    BlobDecryptionPolicy(AsyncKeyEncryptionKey key, AsyncKeyEncryptionKeyResolver keyResolver,
        boolean requiresEncryption) {
        this.keyWrapper = key;
        this.keyResolver = keyResolver;
        this.requiresEncryption = requiresEncryption;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // 1. Expand the range of download for decryption
        HttpHeaders requestHeaders = context.getHttpRequest().getHeaders();
        EncryptedBlobRange encryptedRange = EncryptedBlobRange.getEncryptedBlobRangeFromHeader(
            requestHeaders.getValue(CryptographyConstants.RANGE_HEADER));

        // Assumption: Download is the only API on an encrypted client that sets x-ms-range
        // Only set the x-ms-range header if it already exists
        if (requestHeaders.getValue(CryptographyConstants.RANGE_HEADER) != null) {
            requestHeaders.put(CryptographyConstants.RANGE_HEADER, encryptedRange.toBlobRange().toString());
        }

        // 2. Replace the body of the response with a decrypted version of the body
        return next.process().flatMap(httpResponse -> {
            // Assumption: Download is the only API on an encrypted client that is a get request and has a body in the
            // response
            if (httpResponse.getRequest().getHttpMethod() == HttpMethod.GET && httpResponse.getBody() != null) {
                HttpHeaders responseHeaders = httpResponse.getHeaders();
                /*
                 * We will need to know the total size of the data to know when to finalize the decryption. If it was
                 * not set originally with the intent of downloading the whole blob, update it here.
                 */
                encryptedRange.setAdjustedDownloadCount(Long.parseLong(responseHeaders.getValue(
                    CryptographyConstants.CONTENT_LENGTH)));
                /*
                 * We expect padding only if we are at the end of a blob and it is not a multiple of the encryption
                 * block size
                 */
                boolean padding = encryptedRange.toBlobRange().getOffset()
                    + encryptedRange.toBlobRange().getCount() > (blobSize(responseHeaders) - ENCRYPTION_BLOCK_SIZE);
                String encryptedDataString = responseHeaders
                    .getValue(Constants.HeaderConstants.X_MS_META + "-"
                        + CryptographyConstants.ENCRYPTION_DATA_KEY);

                Flux<ByteBuffer> plainTextData = this.decryptBlob(encryptedDataString,
                    httpResponse.getBody(), encryptedRange, padding);

                return Mono.just(new BlobDecryptionPolicy.DecryptedResponse(httpResponse, plainTextData));
            } else {
                return Mono.just(httpResponse);
            }
        });
    }

    /**
     * Decrypted all or part of an encrypted Block-, Page- or AppendBlob.
     *
     * @param encryptedDataString The Blob's encrypted data in the metadata
     * @param encryptedFlux The encrypted Flux of ByteBuffer to decrypt
     * @param encryptedBlobRange A {@link EncryptedBlobRange} indicating the range to decrypt
     * @param padding Boolean indicating if the padding mode should be set or not.
     * @return A Flux ByteBuffer that has been decrypted
     */
    Flux<ByteBuffer> decryptBlob(String encryptedDataString, Flux<ByteBuffer> encryptedFlux,
        EncryptedBlobRange encryptedBlobRange, boolean padding) {
        EncryptionData encryptionData = getAndValidateEncryptionData(encryptedDataString);

        // The number of bytes we have put into the Cipher so far.
        AtomicLong totalInputBytes = new AtomicLong(0);
        // The number of bytes that have been sent to the downstream so far.
        AtomicLong totalOutputBytes = new AtomicLong(0);

        Flux<ByteBuffer> dataToTrim;
        // Blob being downloaded is not encrypted
        if (encryptionData == null) {
            dataToTrim = encryptedFlux;
        } else {
            dataToTrim = getKeyEncryptionKey(encryptionData)
                .flatMapMany(contentEncryptionKey -> {
                    /*
                     * Calculate the IV.
                     *
                     * If we are starting at the beginning, we can grab the IV from the encryptionData. Otherwise,
                     * Reactor makes it difficult to grab the first 16 bytes of data to pass as an IV to the cipher.
                     * As a work around, we initialize the cipher with a garbage IV (empty byte array) and attempt to
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
                        iv = new byte[ENCRYPTION_BLOCK_SIZE];
                    }

                    Cipher cipher;
                    try {
                        cipher = getCipher(contentEncryptionKey, encryptionData, iv, padding);
                    } catch (InvalidKeyException e) {
                        throw logger.logExceptionAsError(Exceptions.propagate(e));
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
                            if (totalInputBytes.longValue() + bytesToInput
                                >= encryptedBlobRange.getAdjustedDownloadCount()) {
                                cipher.doFinal(encryptedByteBuffer, plaintextByteBuffer);
                            } else {
                                // We won't reach the end of the downloaded range, update.
                                cipher.update(encryptedByteBuffer, plaintextByteBuffer);
                            }
                        } catch (GeneralSecurityException e) {
                            throw logger.logExceptionAsError(Exceptions.propagate(e));
                        }
                        totalInputBytes.addAndGet(bytesToInput);

                        // Flip the buffer to set the position back to 0 and set the limit to the data size.
                        plaintextByteBuffer.flip();
                        return plaintextByteBuffer;
                    });
                });
        }

        return dataToTrim.map(plaintextByteBuffer -> {
            int decryptedBytes = plaintextByteBuffer.limit();

            // Next, determine and set the position of the output buffer.

            /*
             * The amount of data sent so far has not yet reached customer-requested data. i.e. it starts
             * somewhere in either the IV or the range adjustment to align on a block boundary. We should
             * advance the position so the customer does not read this data.
             */
            if (totalOutputBytes.longValue() <= encryptedBlobRange.getOffsetAdjustment()) {
                /*
                 * Note that the cast is safe because of the bounds on offsetAdjustment (see encryptedBlobRange
                 * for details), which here upper bounds totalInputBytes.
                 * Note that we do not simply set the position to be offsetAdjustment because of the (unlikely)
                 * case that some ByteBuffers were small enough to be entirely contained within the
                 * offsetAdjustment, so when we do reach customer-requested data, advancing the position by
                 * the whole offsetAdjustment would be too much.
                 */
                int remainingAdjustment = encryptedBlobRange.getOffsetAdjustment()
                    - (int) totalOutputBytes.longValue();

                /*
                 * Setting the position past the limit will throw. This is in the case of very small
                 * ByteBuffers that are entirely contained within the offsetAdjustment.
                 */
                int newPosition = Math.min(remainingAdjustment, plaintextByteBuffer.limit());
                plaintextByteBuffer.position(newPosition);
            }

            /*
             * Else: The beginning of this ByteBuffer is somewhere after the offset adjustment. If it is in the
             * middle of customer requested data, let it be. If it starts in the end adjustment, this will
             * be trimmed effectively by setting the limit below.
             */

            // Finally, determine and set the limit of the output buffer.

            long beginningOfEndAdjustment; // read: beginning of end-adjustment.
            /*
             * The user intended to download the whole blob, so the only extra we downloaded was padding, which
             * is trimmed by the cipher automatically; there is effectively no beginning to the end-adjustment.
             */
            if (encryptedBlobRange.getOriginalRange().getCount() == null) {
                beginningOfEndAdjustment = Long.MAX_VALUE;
            } else {
                // Calculate the end of the user-requested data so we can trim anything after.
                beginningOfEndAdjustment = encryptedBlobRange.getOffsetAdjustment()
                    + encryptedBlobRange.getOriginalRange().getCount();
            }

            /*
             * The end of the Cipher output lies after customer requested data (in the end adjustment) and
             * should be trimmed.
             */
            if (decryptedBytes + totalOutputBytes.longValue() > beginningOfEndAdjustment) {
                long amountPastEnd // past the end of user-requested data.
                    = decryptedBytes + totalOutputBytes.longValue() - beginningOfEndAdjustment;
                /*
                 * Note that amountPastEnd can only be up to 16, so the cast is safe. We do not need to worry
                 * about limit() throwing because we allocated at least enough space for decryptedBytes and
                 * the newLimit will be less than that. In the case where this Cipher output starts after the
                 * beginning of the endAdjustment, we don't want to send anything back, so we set limit to be
                 * the same as position.
                 */
                int newLimit = totalOutputBytes.longValue() <= beginningOfEndAdjustment
                    ? decryptedBytes - (int) amountPastEnd : plaintextByteBuffer.position();
                plaintextByteBuffer.limit(newLimit);
            } else if (decryptedBytes + totalOutputBytes.longValue()
                > encryptedBlobRange.getOffsetAdjustment()) {
                /*
                 * The end of this Cipher output is before the end adjustment and after the offset adjustment, so
                 * it will lie somewhere in customer requested data. It is possible we allocated a ByteBuffer that
                 * is slightly too large, so we set the limit equal to exactly the amount we decrypted to be safe.
                 */
                plaintextByteBuffer.limit(decryptedBytes);
            } else {
                /*
                 * Else: The end of this ByteBuffer will not reach the beginning of customer-requested data. Make
                 * it effectively empty.
                 */
                plaintextByteBuffer.limit(plaintextByteBuffer.position());
            }

            totalOutputBytes.addAndGet(decryptedBytes);
            return plaintextByteBuffer;
        });
    }

    /**
     * Gets and validates {@link EncryptionData} from a Blob's metadata
     *
     * @param encryptedDataString {@code String} of encrypted metadata
     * @return {@link EncryptionData}
     */
    private EncryptionData getAndValidateEncryptionData(String encryptedDataString) {
        if (encryptedDataString == null) {
            if (requiresEncryption) {
                throw logger.logExceptionAsError(new IllegalStateException("'requiresEncryption' set to true but "
                    + "downloaded data is not encrypted."));
            }
            return null;
        }

        try {
            EncryptionData encryptionData = EncryptionData.fromJsonString(encryptedDataString);

            // Blob being downloaded is not null.
            if (encryptionData == null) {
                if (requiresEncryption) {
                    throw logger.logExceptionAsError(new IllegalStateException("'requiresEncryption' set to true but "
                        + "downloaded data is not encrypted."));
                }
                return null;
            }

            Objects.requireNonNull(encryptionData.getContentEncryptionIV(), "contentEncryptionIV in encryptionData "
                + "cannot be null");
            Objects.requireNonNull(encryptionData.getWrappedContentKey().getEncryptedKey(), "encryptedKey in "
                + "encryptionData.wrappedContentKey cannot be null");

            // Throw if the encryption protocol on the message doesn't match the version that this client library
            // understands and is able to decrypt.
            if (!CryptographyConstants.ENCRYPTION_PROTOCOL_V1
                .equals(encryptionData.getEncryptionAgent().getProtocol())) {
                throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.ROOT,
                    "Invalid Encryption Agent. This version of the client library does not understand the "
                        + "Encryption Agent set on the blob message: %s",
                    encryptionData.getEncryptionAgent())));
            }
            return encryptionData;
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Returns the key encryption key for blob. First tries to get key encryption key from KeyResolver, then falls back
     * to IKey stored on this EncryptionPolicy.
     *
     * @param encryptionData A {@link EncryptionData}
     * @return Key encryption key as a byte array
     */
    private Mono<byte[]> getKeyEncryptionKey(EncryptionData encryptionData) {
        /*
         * 1. Invoke the key resolver if specified to get the key. If the resolver is specified but does not have a
         * mapping for the key id, an error should be thrown. This is important for key rotation scenario.
         * 2. If resolver is not specified but a key is specified, match the key id on the key and and use it.
         */
        Mono<? extends AsyncKeyEncryptionKey> keyMono;

        if (this.keyResolver != null) {
            keyMono = this.keyResolver.buildAsyncKeyEncryptionKey(encryptionData.getWrappedContentKey().getKeyId())
                .onErrorResume(NullPointerException.class, e -> {
                    /*
                     * keyResolver returns null if it cannot find the key, but Reactor throws on null values
                     * passing through workflows, so we propagate this case with an IllegalArgumentException
                     */
                    throw logger.logExceptionAsError(Exceptions.propagate(e));
                });
        } else {
            keyMono = this.keyWrapper.getKeyId().flatMap(keyId -> {
                if (encryptionData.getWrappedContentKey().getKeyId().equals(keyId)) {
                    return Mono.just(this.keyWrapper);
                } else {
                    throw logger.logExceptionAsError(Exceptions.propagate(new IllegalArgumentException("Key mismatch. "
                        + "The key id stored on the service does not match the specified key.")));
                }
            });
        }

        return keyMono.flatMap(keyEncryptionKey -> keyEncryptionKey.unwrapKey(
            encryptionData.getWrappedContentKey().getAlgorithm(),
            encryptionData.getWrappedContentKey().getEncryptedKey()
        ));
    }

    /**
     * Creates a {@link Cipher} using given content encryption key, encryption data, iv, and padding.
     *
     * @param contentEncryptionKey The content encryption key, used to decrypt the contents of the blob.
     * @param encryptionData {@link EncryptionData}
     * @param iv IV used to initialize the Cipher.  If IV is null, encryptionData
     * @param padding If cipher should use padding. Padding is necessary to decrypt all the way to end of a blob.
     * Otherwise, don't use padding.
     * @return {@link Cipher}
     * @throws InvalidKeyException The key provided is invalid
     */
    private Cipher getCipher(byte[] contentEncryptionKey, EncryptionData encryptionData, byte[] iv, boolean padding)
        throws InvalidKeyException {
        try {
            switch (encryptionData.getEncryptionAgent().getAlgorithm()) {
                case AES_CBC_256:
                    Cipher cipher;
                    if (padding) {
                        cipher = Cipher.getInstance(CryptographyConstants.AES_CBC_PKCS5PADDING);
                    } else {
                        cipher = Cipher.getInstance(CryptographyConstants.AES_CBC_NO_PADDING);
                    }
                    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
                    SecretKey keySpec = new SecretKeySpec(contentEncryptionKey, 0, contentEncryptionKey.length,
                        CryptographyConstants.AES);
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
                    return cipher;
                default:
                    throw logger.logExceptionAsError(new IllegalArgumentException(
                        "Invalid Encryption Algorithm found on the resource. This version of the client library "
                            + "does not support the specified encryption algorithm."));
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw logger.logExceptionAsError(Exceptions.propagate(e));
        }
    }

    private Long blobSize(HttpHeaders headers) {
        // e.g. 0-5/1024
        if (headers.getValue(CryptographyConstants.CONTENT_RANGE) != null) {
            String range = headers.getValue(CryptographyConstants.CONTENT_RANGE);
            return Long.valueOf(range.split("/")[1]);
        } else {
            // If there was no content range header, we requested a full blob, so the blobSize = contentLength
            return Long.valueOf(headers.getValue(CryptographyConstants.CONTENT_LENGTH));
        }
    }

    static class DecryptedResponse extends HttpResponse {

        private final Flux<ByteBuffer> plainTextBody;
        private final HttpHeaders httpHeaders;
        private final int statusCode;

        DecryptedResponse(HttpResponse httpResponse, Flux<ByteBuffer> plainTextBody) {
            super(httpResponse.getRequest());
            this.plainTextBody = plainTextBody;
            this.httpHeaders = httpResponse.getHeaders();
            this.statusCode = httpResponse.getStatusCode();
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getHeaderValue(String name) {
            return httpHeaders.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return httpHeaders;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return plainTextBody;
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesInByteBufferStream(plainTextBody);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return FluxUtil.collectBytesInByteBufferStream(plainTextBody).map(String::new);
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return FluxUtil.collectBytesInByteBufferStream(plainTextBody).map(b -> new String(b, charset));
        }
    }
}
