// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.test.TestMode;
import com.azure.core.util.FluxUtil;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_DATA_KEY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("deprecation")
public class EncryptedBlockBlobAsyncApiTests  extends BlobCryptographyTestBase {
    private static final String KEY_ID = "keyId";
    private EncryptedBlobAsyncClient bec; // encrypted async client
    private BlobContainerAsyncClient cc;
    private FakeKey fakeKey;
    private FakeKeyResolver fakeKeyResolver;
    private static final HttpHeaderName X_MS_META_ENCRYPTIONDATA = HttpHeaderName.fromString("x-ms-meta-encryptiondata");

    @Override
    protected void beforeTest() {
        super.beforeTest();
        fakeKey = new FakeKey(KEY_ID, (getTestMode() == TestMode.LIVE) ? getRandomByteArray(256) : MOCK_RANDOM_DATA);
        fakeKeyResolver = new FakeKeyResolver(fakeKey);

        cc = getServiceClientBuilder(ENV.getPrimaryAccount())
            .buildAsyncClient()
            .getBlobContainerAsyncClient(generateContainerName());
        cc.create().block();

        bec = getEncryptionAsyncClient(EncryptionVersion.V1);
    }

    private EncryptedBlobAsyncClient getEncryptionAsyncClient(EncryptionVersion version) {
        return mockAesKey(getEncryptedClientBuilder(fakeKey, null, ENV.getPrimaryAccount().getCredential(),
            cc.getBlobContainerUrl(), version)
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient());
    }

    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {3000, 5 * 1024 * 1024 - 10, 20 * 1024 * 1024 - 10, 4 * 1024 * 1024, 4 * 1024 * 1024 - 10,
        8 * 1024 * 1024})
    public void v2DownloadTest(int dataSize) {
        ByteBuffer data = getRandomData(dataSize);
        bec = mockAesKey(getEncryptedClientBuilder(fakeKey, null, ENV.getPrimaryAccount().getCredential(),
            cc.getBlobContainerUrl(), EncryptionVersion.V2)
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient());


        StepVerifier.create(bec.uploadWithResponse(new BlobParallelUploadOptions(Flux.just(data.duplicate())))
            .then(FluxUtil.collectBytesInByteBufferStream(bec.downloadStream())))
                .assertNext(r -> assertArraysEqual(data.array(), r))
                .verifyComplete();
    }

    // Key and key resolver null
    @Test
    public void createEncryptionClientFails() {
        assertThrows(IllegalArgumentException.class, () -> getEncryptedClientBuilder(null, null,
            ENV.getPrimaryAccount().getCredential(), cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient());
    }

    // Check that all valid ways to specify the key and keyResolver work
    @ParameterizedTest
    @CsvSource(value = {"true,false", "false,true", "true,true"})
    public void createEncryptionClientSucceeds(boolean passKey, boolean passKeyResolver) {
        FakeKey key = passKey ? fakeKey : null;
        FakeKeyResolver keyResolver = passKeyResolver ? fakeKeyResolver : null;

        assertDoesNotThrow(() -> getEncryptedClientBuilder(key, keyResolver, ENV.getPrimaryAccount().getCredential(),
            cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient());
    }

    // This test checks that encryption is not just a no-op
    @ParameterizedTest
    @EnumSource(EncryptionVersion.class)
    public void encryptionNoANoop(EncryptionVersion version) {
        bec = getEncryptionAsyncClient(version);
        ByteBuffer byteBuffer = getRandomData(Constants.KB);

        StepVerifier.create(bec.upload(Flux.just(byteBuffer), null)
            .then(FluxUtil.collectBytesInByteBufferStream(cc.getBlobAsyncClient(bec.getBlobName()).download())))
                .assertNext(r -> assertFalse(Arrays.equals(byteBuffer.array(), r)))
                .verifyComplete();
    }

    // This test uses an encrypted client to encrypt and decrypt data
    // Tests buffered upload with different bytebuffer sizes
    @ParameterizedTest
    @CsvSource(value = {
        "5,2", // 0 Two buffers smaller than an encryption block.
        "8,2", // 1 Two buffers that equal an encryption block.
        "10,1", // 2 One buffer smaller than an encryption block.
        "10,2", // 3 A buffer that spans an encryption block.
        "16,1", // 4 A buffer exactly the same size as an encryption block.
        "16,2", // 5 Two buffers the same size as an encryption block.
        "20,1", // 6 One buffer larger than an encryption block.
        "20,2", // 7 Two buffers larger than an encryption block.
        "100,1" // 8 One buffer containing multiple encryption blocks
    })
    public void encryption(int size, int byteBufferCount) {
        encryptionTestHelper(size, byteBufferCount);
    }

    @LiveOnly
    @ParameterizedTest
    @CsvSource(value = {"5120,1024", "10485760,2"})
    public void encryptionLarge(int size, int byteBufferCount) {
        encryptionTestHelper(size, byteBufferCount);
    }

    private void encryptionTestHelper(int size, int byteBufferCount) {
        List<ByteBuffer> byteBufferList = new ArrayList<>();

        // Sending a sequence of buffers allows us to test encryption behavior in different cases when the buffers do
        // or do not align on encryption boundaries.
        for (int i = 0; i < byteBufferCount; i++) {
            byteBufferList.add(getRandomData(size));
        }
        Flux<ByteBuffer> flux = Flux.fromIterable(byteBufferList);


        // Test buffered upload.
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong((long) size)
            .setMaxConcurrency(2);

        StepVerifier.create(bec.upload(flux, parallelTransferOptions)
            .then(FluxUtil.collectBytesInByteBufferStream(bec.downloadStream()).map(ByteBuffer::wrap)))
                .assertNext(outputByteBuffer -> compareListToBuffer(byteBufferList, outputByteBuffer))
                .verifyComplete();
    }

    @LiveOnly
    @ParameterizedTest
    @CsvSource(value = {
        "5,2", // 0 Two buffers smaller than an encryption block.
        "2097152,2", // 1 Two buffers that equal an encryption block.
        "1024,1", // 2 One buffer smaller than an encryption block.
        "4193280,2", // 3 A buffer that spans an encryption block.
        "4194304,1", // 4 A buffer exactly the same size as an encryption block.
        "4194304,2", // 5 Two buffers the same size as an encryption block.
        "4194314,1", // 6 One buffer larger than an encryption block.
        "4194314,2", // 7 Two buffers larger than an encryption block.
        "4194334,1", // 8 One buffer containing multiple encryption blocks
        "5120,4096", // 9 Large number of small buffers.
        "20971520,2" // 10 Small number of large buffers.
    })
    public void encryptionV2(int size, int byteBufferCount) {
        bec = getEncryptionAsyncClient(EncryptionVersion.V2);

        encryptionTestHelper(size, byteBufferCount);
    }

    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {3000, 5 * 1024 * 1024 - 10, 20 * 1024 * 1024 - 10})
    public void encryptionV2ManualDecryption(int dataSize) throws IOException, GeneralSecurityException {
        bec = getEncryptionAsyncClient(EncryptionVersion.V2);

        ByteBuffer data = getRandomData(dataSize);

        Mono<Tuple2<byte[], byte[]>> response = bec.uploadWithResponse(new BlobParallelUploadOptions(Flux.just(data)))
            .then(cc.getBlobAsyncClient(bec.getBlobName())
                .downloadStreamWithResponse(null, null, null, false))
            .flatMap(downloadResponse ->
                FluxUtil.collectBytesInByteBufferStream(downloadResponse.getValue())
                    .flatMap(ciphertextRawBites -> {
                        ByteArrayInputStream ciphertextInputStream = new ByteArrayInputStream(ciphertextRawBites);
                        byte[] plaintextOriginal = data.array();
                        ByteArrayOutputStream plaintextOutputStream = new ByteArrayOutputStream();

                        EncryptionData encryptionData;
                        try (JsonReader jsonReader = JsonProviders.createReader(downloadResponse.getDeserializedHeaders().getMetadata()
                            .get(ENCRYPTION_DATA_KEY))) {
                            encryptionData = EncryptionData.fromJson(jsonReader);
                        }
                         catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        byte[] cek = fakeKey.unwrapKey(encryptionData.getWrappedContentKey().getAlgorithm(),
                            encryptionData.getWrappedContentKey().getEncryptedKey()).block();
                        ByteArrayInputStream keyStream = new ByteArrayInputStream(cek);
                        byte[] protocolBytes = new byte[3];
                        try {
                            keyStream.read(protocolBytes);
                            for (int i = 0; i < 5; i++) {
                                keyStream.read();
                            }
                            byte[] strippedKeyBytes = new byte[256 / 8];
                            keyStream.read(strippedKeyBytes);
                            SecretKeySpec keySpec = new SecretKeySpec(strippedKeyBytes, CryptographyConstants.AES);

                            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

                            int numChunks = (int) Math.ceil(data.array().length / (4 * 1024 * 1024.0));

                            for (int i = 0; i < numChunks; i++) {
                                byte[] iv = new byte[12];
                                ciphertextInputStream.read(iv);
                                GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, iv);

                                cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

                                int bufferSize = Math.min(ciphertextInputStream.available(), (4 * 1024 * 1024) + 16);
                                byte[] buffer = new byte[bufferSize];
                                ciphertextInputStream.read(buffer);
                                plaintextOutputStream.write(cipher.doFinal(buffer));
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return Mono.zip(Mono.just(plaintextOriginal), Mono.just(plaintextOutputStream.toByteArray()));
                    }));

        StepVerifier.create(response)
            .assertNext(r -> assertArrayEquals(r.getT1(), r.getT2()))
            .verifyComplete();
    }
}
