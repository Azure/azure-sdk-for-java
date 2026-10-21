// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.common.test.shared.ShapedByteBuffer;
import com.azure.storage.common.test.shared.TestBufferShape;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_CBC_PKCS5PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_GCM_NO_PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.NONCE_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Offline tests verifying that client-side encryption encrypts only the bytes between each source buffer's position and
 * limit, and doesn't modify the source buffers, for every kind of buffer (see {@link TestBufferShape}).
 */
public class EncryptorBufferShapeTests {
    // Small regions keep the tests fast; the region logic doesn't depend on the absolute size.
    private static final int REGION_LENGTH = 16;
    // The content encryption key used by the encrypted client uploads.
    private static final byte[] CONTENT_KEY = randomBytes(32);

    @ParameterizedTest
    @MethodSource("encryptV2Supplier")
    public void encryptV2EncryptsOnlyExposedBytes(TestBufferShape shape, int length, int expectedRegions)
        throws Exception {
        byte[] content = randomBytes(length);
        ShapedByteBuffer source = shape.wrap(content);
        SecretKey key = newKey();

        byte[] ciphertext = concat(encryptorV2(key).encrypt(Flux.just(source.getBuffer())).collectList().block());

        TestUtils.assertArraysEqual(content, decryptV2(key, ciphertext, expectedRegions));
        source.assertNoBytesChanged();
    }

    private static Stream<Arguments> encryptV2Supplier() {
        return Stream.of(TestBufferShape.values())
            .flatMap(shape -> Stream.of(
                // shape | length | expected regions
                Arguments.of(shape, REGION_LENGTH / 2, 1), // A single partial region.
                Arguments.of(shape, REGION_LENGTH, 1), // Exactly one region.
                Arguments.of(shape, 2 * REGION_LENGTH + 5, 3), // Several regions, with a partial last one.
                Arguments.of(shape, 3 * REGION_LENGTH, 3))); // Several regions, an exact multiple of the length.
    }

    @Test
    public void encryptV2EncryptsOnlyExposedBytesOfMixedBuffers() throws Exception {
        byte[] content = randomBytes(5 * REGION_LENGTH + 5);
        // Pieces that don't align with regions, so regions span buffers of different kinds.
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, 11);
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        SecretKey key = newKey();

        byte[] ciphertext = concat(encryptorV2(key).encrypt(Flux.fromIterable(buffers)).collectList().block());

        TestUtils.assertArraysEqual(content, decryptV2(key, ciphertext, 6));
        sources.forEach(ShapedByteBuffer::assertNoBytesChanged);
    }

    @ParameterizedTest
    @EnumSource(TestBufferShape.class)
    public void encryptV1EncryptsOnlyExposedBytes(TestBufferShape shape) throws Exception {
        byte[] content = randomBytes(3 * REGION_LENGTH + 5);
        ShapedByteBuffer source = shape.wrap(content);
        SecretKey key = newKey();
        EncryptorV1 encryptor = new EncryptorV1(key);

        byte[] ciphertext = concat(encryptor.encrypt(Flux.just(source.getBuffer())).collectList().block());

        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5PADDING);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(getV1Iv(encryptor)));
        TestUtils.assertArraysEqual(content, cipher.doFinal(ciphertext));
        source.assertNoBytesChanged();
    }

    /**
     * The ways to pass the caller's buffers to an encrypted upload. The sync and async clients share an
     * implementation today, but both are tested so they stay covered if their implementations diverge.
     */
    enum UploadMethod {
        SYNC_FLUX, ASYNC_FLUX, SYNC_BINARY_DATA, ASYNC_BINARY_DATA
    }

    /*
     * End to end through the encrypted clients: the uploaded blob, decrypted with the content encryption key, must be
     * exactly the bytes between the source buffer's position and limit. Client-side encryption splits the data into
     * regions regardless of the upload size, so this applies with the default transfer options.
     */
    @ParameterizedTest
    @MethodSource("encryptedUploadSupplier")
    public void encryptedUploadSendsOnlyExposedBytes(UploadMethod method, TestBufferShape shape, int length,
        int expectedRegions) throws Exception {
        byte[] content = randomBytes(length);
        ShapedByteBuffer source = shape.wrap(content);
        BodyCapturingHttpClient httpClient = new BodyCapturingHttpClient();

        uploadEncrypted(method, httpClient, source.getBuffer());

        TestUtils.assertArraysEqual(content, decryptV2(new SecretKeySpec(CONTENT_KEY, CryptographyConstants.AES),
            httpClient.getBody(), expectedRegions));
        source.assertNoBytesChanged();
    }

    private static Stream<Arguments> encryptedUploadSupplier() {
        return Stream.of(UploadMethod.values())
            .flatMap(method -> Stream.of(TestBufferShape.values())
                .flatMap(shape -> Stream.of(
                    // method | shape | length | expected regions
                    Arguments.of(method, shape, REGION_LENGTH, 1), // Exactly one region.
                    Arguments.of(method, shape, 2 * REGION_LENGTH + 5, 3), // Several regions, partial last one.
                    Arguments.of(method, shape, 3 * REGION_LENGTH, 3)))); // Several regions, exact multiple.
    }

    private static void uploadEncrypted(UploadMethod method, HttpClient httpClient, ByteBuffer buffer) {
        BlobParallelUploadOptions options = (method == UploadMethod.SYNC_FLUX || method == UploadMethod.ASYNC_FLUX)
            ? new BlobParallelUploadOptions(Flux.just(buffer))
            : new BlobParallelUploadOptions(BinaryData.fromByteBuffer(buffer));

        // A fixed content encryption key, so the test can decrypt what was uploaded. Custom region sizes require
        // encryption version 2.1.
        EncryptedBlobAsyncClient asyncClient = new FixedKeyEncryptedBlobAsyncClient(
            new EncryptedBlobClientBuilder(EncryptionVersion.V2_1).endpoint("https://account.blob.core.windows.net")
                .containerName("container")
                .blobName("blob")
                .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build())
                .key(new FakeKey("keyId", randomBytes(256)), "keyWrapAlgorithm")
                .clientSideEncryptionOptions(
                    new BlobClientSideEncryptionOptions().setAuthenticatedRegionDataLengthInBytes(REGION_LENGTH))
                .buildEncryptedBlobAsyncClient());

        if (method == UploadMethod.SYNC_FLUX || method == UploadMethod.SYNC_BINARY_DATA) {
            new EncryptedBlobClient(asyncClient).uploadWithResponse(options, null, null);
        } else {
            asyncClient.uploadWithResponse(options).block();
        }
    }

    /*
     * Captures the body of the single Put Blob request the encrypted upload makes. Reads request bodies through
     * duplicates, as HTTP clients do.
     */
    private static final class BodyCapturingHttpClient implements HttpClient {
        private byte[] body;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (request.getHttpMethod() != HttpMethod.PUT || request.getUrl().getQuery() != null) {
                return Mono.error(new IllegalStateException("Expected a single Put Blob request."));
            }
            return FluxUtil.collectBytesInByteBufferStream(request.getBody().map(ByteBuffer::duplicate)).map(bytes -> {
                synchronized (this) {
                    body = bytes;
                }
                return new MockHttpResponse(request, 201,
                    new HttpHeaders().set(HttpHeaderName.fromString("x-ms-request-server-encrypted"), "true"));
            });
        }

        synchronized byte[] getBody() {
            return body;
        }
    }

    /*
     * Encrypts with a fixed content encryption key. The key is a SecretKeySpec, whose getEncoded() returns a copy, so
     * a cipher clearing the key bytes after use can't change the key seen by other tests running in parallel.
     */
    private static final class FixedKeyEncryptedBlobAsyncClient extends EncryptedBlobAsyncClient {
        FixedKeyEncryptedBlobAsyncClient(EncryptedBlobAsyncClient client) {
            super(client.getHttpPipeline(), client.getAccountUrl(), client.getServiceVersion(), client.getAccountName(),
                client.getContainerName(), client.getBlobName(), client.getSnapshotId(),
                client.getCustomerProvidedKey(), client.getEncryptionScopeInternal(), client.getKeyWrapper(),
                client.getKeyWrapAlgorithm(), client.getVersionId(), client.getEncryptionVersion(),
                client.isRequiresEncryption(), client.getClientSideEncryptionOptions());
        }

        @Override
        SecretKey generateSecretKey() {
            return new SecretKeySpec(CONTENT_KEY, CryptographyConstants.AES);
        }
    }

    private static EncryptorV2 encryptorV2(SecretKey key) {
        return new EncryptorV2(key,
            new BlobClientSideEncryptionOptions().setAuthenticatedRegionDataLengthInBytes(REGION_LENGTH),
            ENCRYPTION_PROTOCOL_V2);
    }

    /*
     * Decrypts V2 ciphertext, where each authenticated region is its nonce, followed by the encrypted region data and
     * its tag.
     */
    private static byte[] decryptV2(SecretKey key, byte[] ciphertext, int expectedRegions) throws Exception {
        ByteArrayOutputStream plaintext = new ByteArrayOutputStream();
        int offset = 0;
        int regions = 0;
        while (offset < ciphertext.length) {
            int regionLength = Math.min(NONCE_LENGTH + REGION_LENGTH + TAG_LENGTH, ciphertext.length - offset);
            byte[] nonce = Arrays.copyOfRange(ciphertext, offset, offset + NONCE_LENGTH);
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH * 8, nonce));
            byte[] region = cipher.doFinal(ciphertext, offset + NONCE_LENGTH, regionLength - NONCE_LENGTH);
            plaintext.write(region, 0, region.length);
            offset += regionLength;
            regions++;
        }
        assertEquals(expectedRegions, regions);
        return plaintext.toByteArray();
    }

    private static byte[] getV1Iv(EncryptorV1 encryptor) throws ReflectiveOperationException {
        Field cipherField = EncryptorV1.class.getDeclaredField("cipher");
        cipherField.setAccessible(true);
        return ((Cipher) cipherField.get(encryptor)).getIV();
    }

    private static SecretKey newKey() {
        byte[] keyBytes = new byte[32];
        new Random(1).nextBytes(keyBytes);
        return new SecretKeySpec(keyBytes, CryptographyConstants.AES);
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        new Random(length).nextBytes(bytes);
        return bytes;
    }

    private static byte[] concat(List<ByteBuffer> buffers) {
        assertNotNull(buffers);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (ByteBuffer buffer : buffers) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            out.write(bytes, 0, bytes.length);
        }
        return out.toByteArray();
    }
}
