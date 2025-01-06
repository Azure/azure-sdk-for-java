// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.FluxUtil;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.CryptographyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.KeyEncryptionKeyClientBuilder;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class KeyVaultKeyAsyncTests extends BlobCryptographyTestBase {
    private static final KeyServiceVersion KEY_SERVICE_VERSION = KeyServiceVersion.V7_2;
    private static final CryptographyServiceVersion CRYPTOGRAPHY_SERVICE_VERSION = CryptographyServiceVersion.V7_2;
    private BlobContainerAsyncClient cca;
    private EncryptedBlobAsyncClient beac; // encrypted async client for download
    private KeyClient keyClient;
    private String keyId;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        String keyVaultUrl = "https://azstoragesdkvault.vault.azure.net/";
        if (getTestMode() != TestMode.PLAYBACK) {
            keyVaultUrl = Configuration.getGlobalConfiguration().get("KEYVAULT_URL");
        }

        keyClient = new KeyClientBuilder().pipeline(getHttpPipeline(KEY_SERVICE_VERSION))
            .httpClient(getHttpClient())
            .vaultUrl(keyVaultUrl)
            .serviceVersion(KEY_SERVICE_VERSION)
            .buildClient();

        keyId = testResourceNamer.randomName(prefix, 50);

        KeyVaultKey keyVaultKey = keyClient.createRsaKey(
            new CreateRsaKeyOptions(keyId).setExpiresOn(testResourceNamer.now().plusYears(1)).setKeySize(2048));

        AsyncKeyEncryptionKey akek = new KeyEncryptionKeyClientBuilder().pipeline(getHttpPipeline(KEY_SERVICE_VERSION))
            .httpClient(getHttpClient())
            .serviceVersion(CRYPTOGRAPHY_SERVICE_VERSION)
            .buildAsyncKeyEncryptionKey(keyVaultKey.getId())
            .block();

        cca = getServiceClientBuilder(ENV.getPrimaryAccount()).buildAsyncClient()
            .getBlobContainerAsyncClient(generateContainerName());
        cca.create().block();

        beac = getEncryptedClientBuilder(akek, null, ENV.getPrimaryAccount().getCredential(), cca.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        keyClient.beginDeleteKey(keyId);
    }

    @Test
    public void uploadDownload() {
        byte[] inputArray = getRandomByteArray(Constants.KB);

        StepVerifier
            .create(beac.upload(Flux.just(ByteBuffer.wrap(inputArray)), null)
                .then(FluxUtil.collectBytesInByteBufferStream(beac.downloadStream())))
            .assertNext(r -> assertArraysEqual(inputArray, r))
            .verifyComplete();
    }

    @Test
    public void encryptionNotANoop() {
        byte[] inputArray = getRandomByteArray(Constants.KB);

        StepVerifier
            .create(beac.upload(Flux.just(ByteBuffer.wrap(inputArray)), null)
                .then(FluxUtil
                    .collectBytesInByteBufferStream(cca.getBlobAsyncClient(beac.getBlobName()).downloadStream())))
            .assertNext(r -> assertFalse(Arrays.equals(inputArray, r)))
            .verifyComplete();
    }
}
