package com.azure.storage.blob.cryptography;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.Metadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.storage.blob.cryptography.EncryptionConstants.ENCRYPTION_BLOCK_SIZE;

public class BlobDecryptionPolicy implements HttpPipelinePolicy {

    private final ClientLogger logger = new ClientLogger(BlobDecryptionPolicy.class);

    /**
     * The {@link IKeyResolver} used to select the correct key for decrypting existing blobs.
     */
    private final IKeyResolver keyResolver;

    /**
     * An object of type {@link IKey} that is used to wrap/unwrap the content key during encryption.
     */
    private final IKey keyWrapper;

    /**
     * Initializes a new instance of the {@link BlobDecryptionPolicy} class with the specified key and resolver.
     * <p>
     * If the generated policy is intended to be used for encryption, users are expected to provide a key at the
     * minimum. The absence of key will cause an exception to be thrown during encryption. If the generated policy is
     * intended to be used for decryption, users can provide a keyResolver. The client library will - 1. Invoke the key
     * resolver if specified to get the key. 2. If resolver is not specified but a key is specified, match the key id on
     * the key and use it.
     *
     * @param key An object of type {@link IKey} that is used to wrap/unwrap the content encryption key.
     * @param keyResolver  The key resolver used to select the correct key for decrypting existing blobs.
     */
    BlobDecryptionPolicy(IKey key, IKeyResolver keyResolver) {
        this.keyWrapper = key;
        this.keyResolver = keyResolver;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // 1. Expand the range of download for decryption
        String RANGE_HEADER = "x-ms-range";
        EncryptedBlobRange encryptedRange = EncryptedBlobRange.getEncryptedBlobRangeFromHeader(
            context.getHttpRequest().getHeaders().getValue(RANGE_HEADER));

        // Assumption: Download is the only API on an encrypted client that sets x-ms-range
        // Only set the x-ms-range header if it already exists
        if(context.getHttpRequest().getHeaders().getValue(RANGE_HEADER) != null) {
            context.getHttpRequest().getHeaders().put(RANGE_HEADER, encryptedRange.toBlobRange().toString());
        }

        // 2. Replace the body of the response with a decrypted version of the body
        return next.process().flatMap(httpResponse -> {
            // Assumption: Download is the only API on an encrypted client that is a get request and has a body in the
            // response
            if (httpResponse.getRequest().getHttpMethod() == HttpMethod.GET && httpResponse.getBody() != null) {
                /*
                We will need to know the total size of the data to know when to finalize the decryption. If it was
                not set originally with the intent of downloading the whole blob, update it here.
                 */
                encryptedRange.withAdjustedDownloadCount(Long.valueOf(httpResponse.getHeaders()
                    .getValue("Content-Length")));
                boolean padding = encryptedRange.toBlobRange().getOffset() +
                    encryptedRange.toBlobRange().getCount() > (blobSize(httpResponse.getHeaders()) - 16);
                Metadata metadata = extractMetadataFromResponse(httpResponse.getHeaders());
                Flux<ByteBuffer> plainTextData = this.decryptBlob(metadata,
                    httpResponse.getBody(), encryptedRange, padding);

                return Mono.just(new DecryptedResponse(httpResponse, plainTextData));
            } else {
                return Mono.just(httpResponse);
            }
        });
    }

    /**
     * Decrypted all or part of an encrypted Block-, Page- or AppendBlob.
     *
     * @param metadata The Blob's {@link com.azure.storage.blob.models.Metadata}
     * @param encryptedFlux The encrypted Flux of ByteBuffer to decrypt
     * @param encryptedBlobRange A {@link EncryptedBlobRange} indicating the range to decrypt
     * @param padding Boolean indicating if the padding mode should be set or not.
     *
     * @return A Flux ByteBuffer that has been decrypted
     */
    Flux<ByteBuffer> decryptBlob(Map<String, String> metadata, Flux<ByteBuffer> encryptedFlux,
        EncryptedBlobRange encryptedBlobRange, boolean padding) {
        if (this.keyWrapper == null && this.keyResolver == null) {
            throw new IllegalArgumentException("Key and KeyResolver cannot both be null");
        }

        EncryptionData encryptionData = getAndValidateEncryptionData(metadata);
        if (encryptionData == null) {
            throw new IllegalStateException("Encryption client is being used but the blob is not encrypted.");
        }

        // The number of bytes we have put into the Cipher so far.
        AtomicLong totalInputBytes = new AtomicLong(0);
        // The number of bytes that have been sent to the downstream so far.
        AtomicLong totalOutputBytes = new AtomicLong(0);

        return getKeyEncryptionKey(encryptionData)
            .flatMapMany(contentEncryptionKey -> {

                /*
                Calculate the IV.

                If we are starting at the beginning, we can grab the IV from the encryptionData. Otherwise,
                Rx makes it difficult to grab the first 16 bytes of data to pass as an IV to the cipher.
                As a work around, we initialize the cipher with a garbage IV (empty byte array) and attempt to
                decrypt the first 16 bytes (the actual IV for the relevant data). We throw away this "decrypted"
                data. Now, though, because each block of 16 is used as the IV for the next, the original 16 bytes
                of downloaded data are in position to be used as the IV for the data actually requested and we are
                in the desired state.
                 */
                byte[] IV = new byte[ENCRYPTION_BLOCK_SIZE];
                /*
                Adjusting the range by <= 16 means we only adjusted to align on an encryption block boundary
                (padding will add 1-16 bytes as it will prefer to pad 16 bytes instead of 0) and therefore the key
                is in the metadata.
                 */
                if (encryptedBlobRange.offsetAdjustment() <= ENCRYPTION_BLOCK_SIZE) {
                    IV = encryptionData.contentEncryptionIV();
                }

                Cipher cipher;
                try {
                    cipher = getCipher(contentEncryptionKey, encryptionData, IV, padding);
                } catch (InvalidKeyException e) {
                    throw logger.logExceptionAsError(Exceptions.propagate(e));
                }

                return encryptedFlux.map(encryptedByteBuffer -> {
                    ByteBuffer plaintextByteBuffer;
                    /*
                    If we could potentially decrypt more bytes than encryptedByteBuffer can hold, allocate more
                    room. Note that, because getOutputSize returns the size needed to store
                    max(updateOutputSize, finalizeOutputSize), this is likely to produce a ByteBuffer slightly
                    larger than what the real outputSize is. This is accounted for below.
                     */
                    int outputSize = cipher.getOutputSize(encryptedByteBuffer.remaining());
                    if (outputSize > encryptedByteBuffer.remaining()) {
                        plaintextByteBuffer = ByteBuffer.allocate(outputSize);
                    } else {
                        /*
                        This is an ok optimization on download as the underlying buffer is not the customer's but
                        the protocol layer's, which does not expect to be able to reuse it.
                         */
                        plaintextByteBuffer = encryptedByteBuffer.duplicate();
                    }

                    // First, determine if we should update or finalize and fill the output buffer.

                    // We will have reached the end of the downloaded range. Finalize.
                    int decryptedBytes;
                    int bytesToInput = encryptedByteBuffer.remaining();
                    if (totalInputBytes.longValue() + bytesToInput >= encryptedBlobRange.adjustedDownloadCount()) {
                        try {
                            decryptedBytes = cipher.doFinal(encryptedByteBuffer, plaintextByteBuffer);
                        } catch (GeneralSecurityException e) {
                            throw logger.logExceptionAsError(Exceptions.propagate(e));
                        }
                    }
                    // We will not have reached the end of the downloaded range. Update.
                    else {
                        try {
                            decryptedBytes = cipher.update(encryptedByteBuffer, plaintextByteBuffer);
                        } catch (ShortBufferException e) {
                            throw logger.logExceptionAsError(Exceptions.propagate(e));
                        }
                    }
                    totalInputBytes.addAndGet(bytesToInput);
                    plaintextByteBuffer.position(0); // Reset the position after writing.

                    // Next, determine and set the position of the output buffer.

                    /*
                    The amount of data sent so far has not yet reached customer-requested data. i.e. it starts
                    somewhere in either the IV or the range adjustment to align on a block boundary. We should
                    advance the position so the customer does not read this data.
                     */
                    if (totalOutputBytes.longValue() <= encryptedBlobRange.offsetAdjustment()) {
                        /*
                        Note that the cast is safe because of the bounds on offsetAdjustment (see encryptedBlobRange
                        for details), which here upper bounds totalInputBytes.
                        Note that we do not simply set the position to be offsetAdjustment because of the (unlikely)
                        case that some ByteBuffers were small enough to be entirely contained within the
                        offsetAdjustment, so when we do reach customer-requested data, advancing the position by
                        the whole offsetAdjustment would be too much.
                         */
                        int remainingAdjustment = encryptedBlobRange.offsetAdjustment() -
                            (int) totalOutputBytes.longValue();

                        /*
                        Setting the position past the limit will throw. This is in the case of very small
                        ByteBuffers that are entirely contained within the offsetAdjustment.
                         */
                        int newPosition = remainingAdjustment <= plaintextByteBuffer.limit() ?
                            remainingAdjustment : plaintextByteBuffer.limit();

                        plaintextByteBuffer.position(newPosition);

                    }
                    /*
                    Else: The beginning of this ByteBuffer is somewhere after the offset adjustment. If it is in the
                    middle of customer requested data, let it be. If it starts in the end adjustment, this will
                    be trimmed effectively by setting the limit below.
                     */

                    // Finally, determine and set the limit of the output buffer.

                    long beginningOfEndAdjustment; // read: beginning of end-adjustment.
                    /*
                    The user intended to download the whole blob, so the only extra we downloaded was padding, which
                    is trimmed by the cipher automatically; there is effectively no beginning to the end-adjustment.
                     */
                    if (encryptedBlobRange.originalRange().getCount() == null) {
                        beginningOfEndAdjustment = Long.MAX_VALUE;
                    }
                    // Calculate the end of the user-requested data so we can trim anything after.
                    else {
                        beginningOfEndAdjustment = encryptedBlobRange.offsetAdjustment() +
                            encryptedBlobRange.originalRange().getCount();
                    }

                    /*
                    The end of the Cipher output lies after customer requested data (in the end adjustment) and
                    should be trimmed.
                     */
                    if (decryptedBytes + totalOutputBytes.longValue() > beginningOfEndAdjustment) {
                        long amountPastEnd // past the end of user-requested data.
                            = decryptedBytes + totalOutputBytes.longValue() - beginningOfEndAdjustment;
                        /*
                        Note that amountPastEnd can only be up to 16, so the cast is safe. We do not need to worry
                        about limit() throwing because we allocated at least enough space for decryptedBytes and
                        the newLimit will be less than that. In the case where this Cipher output starts after the
                        beginning of the endAdjustment, we don't want to send anything back, so we set limit to be
                        the same as position.
                         */
                        int newLimit = totalOutputBytes.longValue() <= beginningOfEndAdjustment ?
                            decryptedBytes - (int) amountPastEnd : plaintextByteBuffer.position();
                        plaintextByteBuffer.limit(newLimit);
                    }
                    /*
                    The end of this Cipher output is before the end adjustment and after the offset adjustment, so
                    it will lie somewhere in customer requested data. It is possible we allocated a ByteBuffer that
                    is slightly too large, so we set the limit equal to exactly the amount we decrypted to be safe.
                     */
                    else if (decryptedBytes + totalOutputBytes.longValue() >
                        encryptedBlobRange.offsetAdjustment()) {
                        plaintextByteBuffer.limit(decryptedBytes);
                    }
                    /*
                    Else: The end of this ByteBuffer will not reach the beginning of customer-requested data. Make
                    it effectively empty.
                     */
                    else {
                        plaintextByteBuffer.limit(plaintextByteBuffer.position());
                    }

                    totalOutputBytes.addAndGet(decryptedBytes);
                    return plaintextByteBuffer;
                });
            });
    }

    /**
     * Gets and validates {@link EncryptionData} from a Blob's metadata
     *
     * @param metadata {@code Map} of String -> String
     *
     * @return {@link EncryptionData}
     */
    private EncryptionData getAndValidateEncryptionData(Map<String, String> metadata) {
        Objects.requireNonNull(metadata);
        String encryptedDataString = metadata.get(EncryptionConstants.ENCRYPTION_DATA_KEY);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            EncryptionData encryptionData = objectMapper.readValue(encryptedDataString, EncryptionData.class);

            if (encryptionData == null) {
                throw new IllegalArgumentException(
                    "Encryption data does not exist. If you do not want to decrypt the data, please do not " +
                        "set the require encryption flag to true");
            }

            Objects.requireNonNull(encryptionData.contentEncryptionIV());
            Objects.requireNonNull(encryptionData.wrappedContentKey().encryptedKey());

            // Throw if the encryption protocol on the message doesn't match the version that this client library
            // understands
            // and is able to decrypt.
            if (!EncryptionConstants.ENCRYPTION_PROTOCOL_V1.equals(encryptionData.encryptionAgent().protocol())) {
                throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.ROOT,
                    "Invalid Encryption Agent. This version of the client library does not understand the " +
                        "Encryption Agent set on the blob message: %s",
                    encryptionData.encryptionAgent())));
            }
            return encryptionData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the key encryption key for blob. First tries to get key encryption key from KeyResolver, then
     * falls back to IKey stored on this EncryptionPolicy.
     *
     * @param encryptionData A {@link EncryptionData}
     *
     * @return Key encryption key as a byte array
     */
    private Mono<byte[]> getKeyEncryptionKey(EncryptionData encryptionData) {
        /*
        1. Invoke the key resolver if specified to get the key. If the resolver is specified but does not have a
        mapping for the key id, an error should be thrown. This is important for key rotation scenario.
        2. If resolver is not specified but a key is specified, match the key id on the key and and use it.
        */

        Mono<IKey> keyMono;

        if (this.keyResolver != null) {
            keyMono = this.keyResolver.resolveKeyAsync(encryptionData.wrappedContentKey().keyId())
                .onErrorResume(NullPointerException.class, e -> {
                    /*
                    keyResolver returns null if it cannot find the key, but RX throws on null values
                    passing through workflows, so we propagate this case with an IllegalArgumentException
                     */
                    throw logger.logExceptionAsError(Exceptions.propagate(e));
                });
        } else {
            if (encryptionData.wrappedContentKey().keyId().equals(this.keyWrapper.getKid())) {
                keyMono = Mono.just(this.keyWrapper);
            } else {
                throw logger.logExceptionAsError(new IllegalArgumentException("Key mismatch. The key id stored on " +
                    "the service does not match the specified key."));
            }
        }

        return keyMono.flatMap(keyEncryptionKey -> {
            try {
                return this.keyWrapper.unwrapKeyAsync(
                    encryptionData.wrappedContentKey().encryptedKey(),
                    encryptionData.wrappedContentKey().algorithm()
                );
            } catch (NoSuchAlgorithmException e) {
                throw logger.logExceptionAsError(Exceptions.propagate(e));
            }
        });
    }

    /**
     * Creates a {@link Cipher} using given content encryption key, encryption data, iv, and padding.
     *
     * @param contentEncryptionKey The content encryption key, used to decrypt the contents of the blob.
     * @param encryptionData {@link EncryptionData}
     * @param iv IV used to initialize the Cipher.  If IV is null, encryptionData
     * @param padding If cipher should use padding. Padding is necessary to decrypt all the way to end of a blob.
     *         Otherwise, don't use padding.
     *
     * @return {@link Cipher}
     *
     * @throws InvalidKeyException
     */
    private Cipher getCipher(byte[] contentEncryptionKey, EncryptionData encryptionData, byte[] iv, boolean padding)
        throws InvalidKeyException {
        try {
            switch (encryptionData.encryptionAgent().algorithm()) {
                case AES_CBC_256:
                    Cipher cipher;
                    if (padding) {
                        cipher = Cipher.getInstance(EncryptionConstants.AES_CBC_PKCS5PADDING);
                    } else {
                        cipher = Cipher.getInstance(EncryptionConstants.AES_CBC_NO_PADDING);
                    }
                    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
                    SecretKey keySpec = new SecretKeySpec(contentEncryptionKey, 0, contentEncryptionKey.length,
                        EncryptionConstants.AES);
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
                    return cipher;
                default:
                    throw logger.logExceptionAsError(new IllegalArgumentException(
                        "Invalid Encryption Algorithm found on the resource. This version of the client library " +
                            "does not support the specified encryption algorithm."));
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw logger.logExceptionAsError(Exceptions.propagate(e));
        }
    }

    private Long blobSize(HttpHeaders headers) {
        // e.g. 0-5/1024
        if (headers.getValue("Content-Range") != null) {
            String range = headers.getValue("Content-Range");
            return Long.valueOf(range.split("/")[1]);
        }
        else {
            // If there was no content range header, we requested a full blob, so the blobSize = contentLength
            return Long.valueOf(headers.getValue("Content-Length"));
        }
    }

    private Metadata extractMetadataFromResponse(HttpHeaders headers) {
        Metadata metadata = new Metadata();
        for (HttpHeader header : headers) {
            String key = header.getName();
            String METADATA_HEADER = "x-ms-meta-";
            if (key.startsWith(METADATA_HEADER)) {
                metadata.put(key.substring(METADATA_HEADER.length()), header.getValue());
            }
        }
        return metadata;
    }

    private class DecryptedResponse extends HttpResponse {

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
