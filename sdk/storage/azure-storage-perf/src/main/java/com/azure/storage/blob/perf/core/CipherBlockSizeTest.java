// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.storage.blob.perf.BlobPerfStressOptions;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import static com.azure.perf.test.core.TestDataCreationHelper.generateRandomString;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_GCM_NO_PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.NONCE_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;

public class CipherBlockSizeTest extends BlobTestBase<BlobPerfStressOptions> {
    private static final int SIZE = 10 * Constants.MB;

    private static String randomData;

    public CipherBlockSizeTest(BlobPerfStressOptions options, String blobName) {
        super(options, blobName);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(Mono.fromCallable(() -> {
                randomData = generateRandomString(SIZE);
                return randomData;
            }))
            .then();
    }

    // Need a branch for v1/v2. And then another test that is EncryptionMultiUpdateTest.
    // Run both of those with both versions on 10mb and compare 4 ways

    @Override
    public void run() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");

            keyGen.init(256);
            SecretKey aesKey = keyGen.generateKey();

            if (options.getClientEncryption().equals("1.0")) {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                // Generate content encryption key
                cipher.init(Cipher.ENCRYPT_MODE, aesKey);

                ByteBuffer outBuff = ByteBuffer.allocate(5 * Constants.MB);

                cipher.update(ByteBuffer.wrap(randomData.getBytes(StandardCharsets.UTF_8), 0, 4 * Constants.MB),
                    outBuff);
                outBuff.position(0);
                cipher.update(ByteBuffer.wrap(randomData.getBytes(StandardCharsets.UTF_8), 4 * Constants.MB,
                    4 * Constants.MB), outBuff);
                outBuff.position(0);
                cipher.update(ByteBuffer.wrap(randomData.getBytes(StandardCharsets.UTF_8), 8* Constants.MB,
                    4 * Constants.MB), outBuff);
                cipher.doFinal();
            } else if (options.getClientEncryption().equals("2.0")) {
                ByteBuffer outBuff = ByteBuffer.allocate(5 * Constants.MB);
                byte[] iv = ByteBuffer.allocate(NONCE_LENGTH).putLong(0).array();

                Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
                cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_LENGTH * 8, iv));
                cipher.doFinal(ByteBuffer.wrap(randomData.getBytes(StandardCharsets.UTF_8), 0, 4 * Constants.MB),
                    outBuff);
                outBuff.position(0);

                cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
                cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_LENGTH * 8, iv));
                cipher.doFinal(ByteBuffer.wrap(randomData.getBytes(StandardCharsets.UTF_8), 4 * Constants.MB,
                    4 * Constants.MB), outBuff);
                outBuff.position(0);

                cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
                cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_LENGTH * 8, iv));
                cipher.doFinal(ByteBuffer.wrap(randomData.getBytes(StandardCharsets.UTF_8), 8* Constants.MB,
                    4 * Constants.MB), outBuff);
            } else {
                throw new RuntimeException();
            }
        } catch (GeneralSecurityException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return null;
    }
}
