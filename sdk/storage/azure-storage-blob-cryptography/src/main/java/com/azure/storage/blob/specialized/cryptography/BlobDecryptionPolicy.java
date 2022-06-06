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
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.common.implementation.BufferStagingArea;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.UploadUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.*;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_BLOCK_SIZE;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V1;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;

/**
 * This is a decryption policy in an {@link com.azure.core.http.HttpPipeline} to decrypt data in an {@link
 * EncryptedBlobAsyncClient} or {@link EncryptedBlobClient} download request. The range will be expanded for decryption
 * purposes and then decrypt the body when the response comes in.
 */
public class BlobDecryptionPolicy implements HttpPipelinePolicy {

    private static final ClientLogger LOGGER = new ClientLogger(BlobDecryptionPolicy.class);

    /**
     * The {@link AsyncKeyEncryptionKeyResolver} used to select the correct key for decrypting existing blobs.
     */
    private final AsyncKeyEncryptionKeyResolver keyResolver;

    /**
     * An object of type {@link AsyncKeyEncryptionKey} that is used to wrap/unwrap the content key during encryption.
     */
    private final AsyncKeyEncryptionKey keyWrapper;

    /**
     * Whether encryption is enforced by this client. Throws an exception if data is downloaded and it is not
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
     * @param requiresEncryption Whether encryption is enforced by this client.
     */
    BlobDecryptionPolicy(AsyncKeyEncryptionKey key, AsyncKeyEncryptionKeyResolver keyResolver,
        boolean requiresEncryption) {
        this.keyWrapper = key;
        this.keyResolver = keyResolver;
        this.requiresEncryption = requiresEncryption;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpHeaders requestHeaders = context.getHttpRequest().getHeaders();

        // Get the encryption data put in the pipeline by the FetchEncryptionDataPolicy.
        // This will be present if there was a range set on the request. It will not be present if this is
        // not a download or there was no range set.
        EncryptionData encryptionData = (EncryptionData) context.getData(ENCRYPTION_DATA_KEY)
            .orElse(null);

        // 1. Expand the range of download for decryption if a range was present. If the range was present, we know the
        // policy would have already fetched the encryption data. If there was no encryptiondata, do not mess with the
        // range.
        EncryptedBlobRange encryptedRange = EncryptedBlobRange.getEncryptedBlobRangeFromHeader(
            requestHeaders.getValue(RANGE_HEADER), encryptionData);
        if (context.getHttpRequest().getHeaders().getValue(RANGE_HEADER) != null
            && encryptionData != null) {
            requestHeaders.set(RANGE_HEADER, encryptedRange.toBlobRange().toString());
        }

        // 2. Replace the body of the response with a decrypted version of the body
        return next.process().flatMap(httpResponse -> {
            // Assumption: Download is the only API on an encrypted client that is a get request and has a body in the
            // response
            if (httpResponse.getRequest().getHttpMethod() == HttpMethod.GET && httpResponse.getBody() != null) {
                HttpHeaders responseHeaders = httpResponse.getHeaders();

                /*
                 * If there was no range set on the request, we skipped doing a get properties first as we are getting
                 * the whole blob anyway, so we didn't have to know the version a priori to adjust the range.
                 * In that case, we deserialize the encryption data here.
                 * If there is no encryption data set on the blob, then we would have skipped adjusting the range above
                 * and we can just return the response as is.
                 */
                EncryptionData encryptionDataFinal =
                    encryptionData == null ? EncryptionData.getAndValidateEncryptionData(
                        httpResponse.getHeaderValue(Constants.HeaderConstants.X_MS_META + "-" + ENCRYPTION_DATA_KEY),
                        requiresEncryption) : encryptionData;
                // Checking that encryption data at least exists on the download call even if we didn't use it for
                // deserialization ensures that the download response was not an error response.
                if (encryptionDataFinal == null
                    || httpResponse.getHeaderValue(Constants.HeaderConstants.X_MS_META + "-" + ENCRYPTION_DATA_KEY)
                    == null) {
                    return Mono.just(httpResponse);
                }

                /*
                 * We will need to know the total size of the data to know when to finalize the decryption. If it was
                 * not set originally with the intent of downloading the whole blob, update it here.
                 * If there was no range set on the request, we skipped instantiating a BlobRange as we did not have
                 * encryption data at the time. Instantiate now with a BlobRange that indicates a full blob.
                 */
                EncryptedBlobRange encryptedRangeFinal = encryptedRange == null
                    ? new EncryptedBlobRange(new BlobRange(0), encryptionDataFinal) : encryptedRange;
                encryptedRangeFinal.setAdjustedDownloadCount(
                    Long.parseLong(responseHeaders.getValue(CONTENT_LENGTH)));

                /*
                 * We expect padding only if we are at the end of a blob and it is not a multiple of the encryption
                 * block size. Padding is only ever present in track 1.
                 */
                boolean padding = encryptionDataFinal.getEncryptionAgent().getProtocol().equals(ENCRYPTION_PROTOCOL_V1)
                    && (encryptedRangeFinal.toBlobRange().getOffset()
                    + encryptedRangeFinal.toBlobRange().getCount() >
                    (blobSize(responseHeaders) - ENCRYPTION_BLOCK_SIZE));

                Flux<ByteBuffer> plainTextData = this.decryptBlob(httpResponse.getBody(), encryptedRangeFinal, padding,
                    encryptionDataFinal, httpResponse.getRequest().getUrl().toString());

                return Mono.just(new BlobDecryptionPolicy.DecryptedResponse(httpResponse, plainTextData));
            } else {
                return Mono.just(httpResponse);
            }
        });
    }

    /**
     * Decrypted all or part of an encrypted Block-, Page- or AppendBlob.
     *
     * @param encryptedFlux The encrypted Flux of ByteBuffer to decrypt
     * @param encryptedBlobRange A {@link EncryptedBlobRange} indicating the range to decrypt
     * @param padding Boolean indicating if the padding mode should be set or not.
     * @param encryptionData The {@link EncryptionData}
     * @return A Flux ByteBuffer that has been decrypted
     */
    Flux<ByteBuffer> decryptBlob(Flux<ByteBuffer> encryptedFlux, EncryptedBlobRange encryptedBlobRange, boolean padding,
        EncryptionData encryptionData, String requestUri) {

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
                    switch (encryptionData.getEncryptionAgent().getProtocol()) {
                        case ENCRYPTION_PROTOCOL_V1:
                            return decryptV1(encryptedFlux, encryptedBlobRange, padding,
                                (EncryptionDataV1) encryptionData, requestUri, totalInputBytes, contentEncryptionKey);
                        case ENCRYPTION_PROTOCOL_V2:
                            return decryptV2(encryptedFlux, encryptionData, contentEncryptionKey);
                        default:
                            throw LOGGER.logExceptionAsError(
                                new IllegalStateException("Encryption protocol not recognized"));
                    }
                });
        }

        return trimData(encryptedBlobRange, totalOutputBytes, dataToTrim);
    }

    private Flux<ByteBuffer> trimData(EncryptedBlobRange encryptedBlobRange, AtomicLong totalOutputBytes, Flux<ByteBuffer> dataToTrim) {
        return dataToTrim.map(plaintextByteBuffer -> {
            int decryptedBytes = plaintextByteBuffer.limit();

            // Next, determine and set the position of the output buffer.

            /*
             * The amount of data sent so far has not yet reached customer-requested data. i.e. it starts
             * somewhere in either the IV or the range adjustment to align on a block boundary. We should
             * advance the position so the customer does not read this data.
             */
            if (totalOutputBytes.longValue() <= encryptedBlobRange.getAmountPlaintextToSkip()) {
                /*
                 * Note that the cast is safe because of the bounds on offsetAdjustment (see encryptedBlobRange
                 * for details), which here upper bounds totalInputBytes.
                 * Note that we do not simply set the position to be offsetAdjustment because of the (unlikely)
                 * case that some ByteBuffers were small enough to be entirely contained within the
                 * offsetAdjustment, so when we do reach customer-requested data, advancing the position by
                 * the whole offsetAdjustment would be too much.
                 */
                int remainingAdjustment = encryptedBlobRange.getAmountPlaintextToSkip()
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
                beginningOfEndAdjustment = encryptedBlobRange.getAmountPlaintextToSkip()
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
                 * Note that amountPastEnd can only be up to 16 for v1 or 4mb for v2, so the cast is safe. We do not
                 * need to worry about limit() throwing because we allocated at least enough space for decryptedBytes
                 * and the newLimit will be less than that. In the case where this Cipher output starts after the
                 * beginning of the endAdjustment, we don't want to send anything back, so we set limit to be
                 * the same as position.
                 */
                int newLimit = totalOutputBytes.longValue() <= beginningOfEndAdjustment
                    ? decryptedBytes - (int) amountPastEnd : plaintextByteBuffer.position();
                plaintextByteBuffer.limit(newLimit);
            } else if (decryptedBytes + totalOutputBytes.longValue() > encryptedBlobRange.getAmountPlaintextToSkip()) {
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

    private Flux<ByteBuffer> decryptV2(Flux<ByteBuffer> encryptedFlux, EncryptionData encryptionData,
        byte[] contentEncryptionKey) {
        // Buffer an exact region with the nonce and tag
        EncryptionDataV2 encryptionDataV2 = (EncryptionDataV2) encryptionData;
        final int GCM_ENCRYPTION_REGION_LENGTH = Integer.parseInt(encryptionDataV2
            .getAuthenticationRegionInfo()
            .getEncryptionRegionLength());
        final int NONCE_LENGTH =
            Integer.parseInt(encryptionDataV2
                .getAuthenticationRegionInfo()
                .getNonceLength());
        BufferStagingArea stagingArea =
            new BufferStagingArea(GCM_ENCRYPTION_REGION_LENGTH + TAG_LENGTH + NONCE_LENGTH,
            GCM_ENCRYPTION_REGION_LENGTH + TAG_LENGTH + NONCE_LENGTH);

        return UploadUtils.chunkSource(encryptedFlux,
                new com.azure.storage.common.ParallelTransferOptions()
                    .setBlockSizeLong((long) GCM_ENCRYPTION_REGION_LENGTH
                        + TAG_LENGTH + NONCE_LENGTH))
            .flatMapSequential(stagingArea::write)
            .concatWith(Flux.defer(stagingArea::flush))
            .flatMapSequential(aggregator -> {
                // Get the IV out of the beginning of the aggregator
                byte[] gmcIv = aggregator.getFirstNBytes(NONCE_LENGTH);

                Cipher gmcCipher;
                try {
                    gmcCipher = getCipher(contentEncryptionKey, encryptionData, gmcIv, false);
                } catch (InvalidKeyException e) {
                    return Mono.error(LOGGER.logExceptionAsError(Exceptions.propagate(e)));
                }

                ByteBuffer decryptedRegion = ByteBuffer.allocate(GCM_ENCRYPTION_REGION_LENGTH);
                return aggregator.asFlux()
                    .map(buffer -> {
                        // Write into the preallocated buffer and always return this buffer.
                        try {
                            gmcCipher.update(buffer, decryptedRegion);
                        } catch (ShortBufferException e) {
                            return Mono.error(LOGGER.logExceptionAsError(Exceptions.propagate(e)));
                        }
                        return decryptedRegion;
                    }).then(Mono.fromCallable(() -> {
                        // We have already written all the data to the cipher. Passing in a final
                        // empty buffer allows us to force completion and return the filled buffer.
                        gmcCipher.doFinal(ByteBuffer.allocate(0), decryptedRegion);
                        decryptedRegion.flip();
                        return decryptedRegion;
                    })).flux();
            });
    }

    private Flux<ByteBuffer> decryptV1(Flux<ByteBuffer> encryptedFlux, EncryptedBlobRange encryptedBlobRange,
        boolean padding, EncryptionDataV1 encryptionData, String requestUri,
        AtomicLong totalInputBytes, byte[] contentEncryptionKey) {
        LOGGER.warning("Downloaded data found to be encrypted with v1 encryption, "
            + "which is no longer secure. Uri: " + requestUri);
        EncryptionDataV1 encryptionDataV1 = encryptionData;
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
            iv = encryptionDataV1.getContentEncryptionIV();
        } else {
            // Rather than try to buffer just the 16 bytes of the iv, we "decrypt" them with this garbage iv.
            // This makes counting easier.
            // We end up throwing that garbage decrypted data away when we trim the cipher uses the ciphertext as the iv
            // for the data we actually want.
            iv = new byte[ENCRYPTION_BLOCK_SIZE];
        }

        Cipher cipher;
        try {
            cipher = getCipher(contentEncryptionKey, encryptionDataV1, iv, padding);
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

    /**
     * Returns the key encryption key for blob. First tries to get key encryption key from KeyResolver, then falls back
     * to IKey stored on this EncryptionPolicy.
     *
     * @param encryptionData A {@link EncryptionDataV1}
     * @return Key encryption key as a byte array
     */
    private Mono<byte[]> getKeyEncryptionKey(EncryptionData encryptionData) {
        /*
         * 1. Invoke the key resolver if specified to get the key. If the resolver is specified but does not have a
         * mapping for the key id, an error should be thrown. This is important for key rotation scenario.
         * 2. If resolver is not specified but a key is specified, match the key id on the key and use it.
         */
        Mono<? extends AsyncKeyEncryptionKey> keyMono;

        if (this.keyResolver != null) {
            keyMono = this.keyResolver.buildAsyncKeyEncryptionKey(encryptionData.getWrappedContentKey().getKeyId())
                .onErrorResume(NullPointerException.class, e -> {
                    /*
                     * keyResolver returns null if it cannot find the key, but Reactor throws on null values
                     * passing through workflows, so we propagate this case with an IllegalArgumentException
                     */
                    throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
                });
        } else {
            keyMono = this.keyWrapper.getKeyId().flatMap(keyId -> {
                if (encryptionData.getWrappedContentKey().getKeyId().equals(keyId)) {
                    return Mono.just(this.keyWrapper);
                } else {
                    throw LOGGER.logExceptionAsError(Exceptions.propagate(new IllegalArgumentException("Key mismatch. "
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
     * @param encryptionData {@link EncryptionDataV1}
     * @param iv IV used to initialize the Cipher.  If IV is null, encryptionData
     * @param padding If cipher should use padding. Padding is necessary to decrypt all the way to end of a blob.
     * Otherwise, don't use padding.
     * @return {@link Cipher}
     * @throws InvalidKeyException The key provided is invalid
     */
    private Cipher getCipher(byte[] contentEncryptionKey, EncryptionData encryptionData,
        byte[] iv, boolean padding) throws InvalidKeyException {
        SecretKey keySpec = new SecretKeySpec(contentEncryptionKey, 0, contentEncryptionKey.length,
            AES);
        try {
            switch (encryptionData.getEncryptionAgent().getAlgorithm()) {
                case AES_CBC_256:
                    Cipher cipher;
                    if (padding) {
                        cipher = Cipher.getInstance(AES_CBC_PKCS5PADDING);
                    } else {
                        cipher = Cipher.getInstance(AES_CBC_NO_PADDING);
                    }
                    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

                    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
                    return cipher;
                case AES_GCM_256:
                    cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH * 8, iv));
                    return cipher;
                default:
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "Invalid Encryption Algorithm found on the resource. This version of the client library "
                            + "does not support the specified encryption algorithm."));
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
        }
    }

    private Long blobSize(HttpHeaders headers) {
        // e.g. 0-5/1024
        if (headers.getValue(CONTENT_RANGE) != null) {
            String range = headers.getValue(CONTENT_RANGE);
            return Long.valueOf(range.split("/")[1]);
        } else {
            // If there was no content range header, we requested a full blob, so the blobSize = contentLength
            return Long.valueOf(headers.getValue(CONTENT_LENGTH));
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
