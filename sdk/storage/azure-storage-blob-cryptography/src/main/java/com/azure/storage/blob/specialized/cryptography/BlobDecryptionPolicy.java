// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobRange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_BLOCK_SIZE;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_METADATA_HEADER;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V1;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.RANGE_HEADER;

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
        String initialRangeHeader = requestHeaders.getValue(RANGE_HEADER);

        if (!isRangeRequest(initialRangeHeader)) {
            // If there is no range, there is nothing to expand, so we can continue with the request
            return next.process().flatMap(httpResponse -> {
                // If it is a download response, check if we need to decrypt
                if (isDownloadResponse(httpResponse)) {
                    HttpHeaders responseHeaders = httpResponse.getHeaders();

                    /*
                     * Deserialize encryption data.
                     * If there is no encryption data set on the blob, then we can return the request as is since we
                     * didn't expand the range at all.
                     */
                    EncryptionData encryptionData = EncryptionData.getAndValidateEncryptionData(
                        httpResponse.getHeaderValue(ENCRYPTION_METADATA_HEADER), requiresEncryption);
                    // If there was no encryption data, it was either an error response or the blob is not encrypted.
                    if (!isEncryptedBlob(encryptionData)) {
                        return Mono.just(httpResponse);
                    }

                    /*
                     * We will need to know the total size of the data to know when to finalize the decryption. If it
                     * was not set originally with the intent of downloading the whole blob, update it here.
                     * If there was no range set on the request, we skipped instantiating a BlobRange as we did not have
                     * encryption data at the time. Instantiate now with a BlobRange that indicates a full blob.
                     */
                    EncryptedBlobRange encryptedRange = new EncryptedBlobRange(new BlobRange(0), encryptionData);
                    encryptedRange.setAdjustedDownloadCount(
                        Long.parseLong(responseHeaders.getValue(HttpHeaderName.CONTENT_LENGTH)));

                    boolean padding = hasPadding(responseHeaders, encryptionData, encryptedRange);

                    Flux<ByteBuffer> plainTextData = this.decryptBlob(httpResponse.getBody(), encryptedRange, padding,
                        encryptionData, httpResponse.getRequest().getUrl());

                    return Mono.just(new BlobDecryptionPolicy.DecryptedResponse(httpResponse, plainTextData));
                } else {
                    return Mono.just(httpResponse);
                }
            });
        } else {
            // If it was a ranged request, we would have already called get properties and set encryption data.
            // Since there is no encryption data, the request is not encrypted
            if (!isEncryptedBlob(context)) {
                return validateEncryptionDataConsistency(next.process());
            }
            EncryptionData encryptionData =
                (EncryptionData) context.getData(CryptographyConstants.ENCRYPTION_DATA_KEY).get();

            EncryptedBlobRange encryptedRange = EncryptedBlobRange.getEncryptedBlobRangeFromHeader(
                initialRangeHeader, encryptionData);
            if (context.getHttpRequest().getHeaders().getValue(RANGE_HEADER) != null) {
                requestHeaders.set(RANGE_HEADER, encryptedRange.toBlobRange().toString());
            }

            // Process the request after expanding the range/
            return next.process().map(httpResponse -> {
                if (isDownloadResponse(httpResponse)) {
                    HttpHeaders responseHeaders = httpResponse.getHeaders();
                    // Checking that encryption data at least exists on the download call even if we didn't use
                    // it for deserialization ensures that the download response was not an error response.
                    if (httpResponse.getHeaderValue(ENCRYPTION_METADATA_HEADER) == null) {
                        return httpResponse;
                    }
                    encryptedRange.setAdjustedDownloadCount(
                        Long.parseLong(responseHeaders.getValue(HttpHeaderName.CONTENT_LENGTH)));

                    /*
                     * We expect padding only if we are at the end of a blob and it is not a multiple of the
                     * encryption block size. Padding is only ever present in track 1.
                     */
                    boolean padding = hasPadding(httpResponse.getHeaders(), encryptionData, encryptedRange);

                    Flux<ByteBuffer> plainTextData = this.decryptBlob(httpResponse.getBody(),
                        encryptedRange, padding, encryptionData,
                        httpResponse.getRequest().getUrl());

                    return new DecryptedResponse(httpResponse, plainTextData);
                } else {
                    return httpResponse;
                }
            });
        }
    }

    private boolean isRangeRequest(String rangeHeader) {
        return rangeHeader != null;
    }

    private boolean isEncryptedBlob(HttpPipelineCallContext context) {
        return context.getData(CryptographyConstants.ENCRYPTION_DATA_KEY).isPresent();
    }

    private boolean isEncryptedBlob(EncryptionData encryptionData) {
        return encryptionData != null;
    }

    private Mono<HttpResponse> validateEncryptionDataConsistency(Mono<HttpResponse> responseMono) {
        return responseMono.map(response -> {
            if (response.getHeaderValue(ENCRYPTION_METADATA_HEADER) != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException("GetProperties did not find"
                    + " encryption data, but download request returned encryption data."));
            }
            return response;
        });
    }

    private boolean hasPadding(HttpHeaders responseHeaders, EncryptionData encryptionData,
            EncryptedBlobRange encryptedRange) {
        /*
         * We expect padding only if we are at the end of a blob and it is not a multiple of the encryption
         * block size. Padding is only ever present in track 1.
         */
        return encryptionData.getEncryptionAgent().getProtocol().equals(ENCRYPTION_PROTOCOL_V1)
                && (encryptedRange.toBlobRange().getOffset()
                + encryptedRange.toBlobRange().getCount()
                > (blobSize(responseHeaders) - ENCRYPTION_BLOCK_SIZE));
    }

    private boolean isDownloadResponse(HttpResponse httpResponse) {
        // Assumption: Download is the only API on an encrypted client that is a get request and has a body in the
        // response
        return httpResponse.getRequest().getHttpMethod() == HttpMethod.GET && httpResponse.getBody() != null;
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
        EncryptionData encryptionData, URL requestUri) {

        String uriToLog = requestUri.getHost() + requestUri.getPath();
        // The number of bytes we have put into the Cipher so far.
        AtomicLong totalInputBytes = new AtomicLong(0);
        // The number of bytes that have been sent to the downstream so far.
        AtomicLong totalOutputBytes = new AtomicLong(0);

        Decryptor decryptor = Decryptor.getDecryptor(keyResolver, keyWrapper, encryptionData);
        Flux<ByteBuffer> dataToTrim = decryptor.getKeyEncryptionKey()
                .flatMapMany(key -> decryptor.decrypt(encryptedFlux, encryptedBlobRange, padding, uriToLog,
                        totalInputBytes, key));

        return trimData(encryptedBlobRange, totalOutputBytes, dataToTrim);
    }

    Flux<ByteBuffer> trimData(EncryptedBlobRange encryptedBlobRange,
            AtomicLong totalOutputBytes, Flux<ByteBuffer> dataToTrim) {
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

    private Long blobSize(HttpHeaders headers) {
        // e.g. 0-5/1024
        if (headers.getValue(HttpHeaderName.CONTENT_RANGE) != null) {
            String range = headers.getValue(HttpHeaderName.CONTENT_RANGE);
            return Long.valueOf(range.split("/")[1]);
        } else {
            // If there was no content range header, we requested a full blob, so the blobSize = contentLength
            return Long.valueOf(headers.getValue(HttpHeaderName.CONTENT_LENGTH));
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
