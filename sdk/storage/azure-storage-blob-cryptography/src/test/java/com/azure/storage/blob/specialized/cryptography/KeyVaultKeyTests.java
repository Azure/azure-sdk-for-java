// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.credential.TokenCredential;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.CryptographyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.KeyEncryptionKeyClientBuilder;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class KeyVaultKeyTests extends BlobCryptographyTestBase {
    private static final KeyServiceVersion KEY_SERVICE_VERSION = KeyServiceVersion.V7_2;
    private static final CryptographyServiceVersion CRYPTOGRAPHY_SERVICE_VERSION = CryptographyServiceVersion.V7_2;
    private BlobContainerClient cc;
    private EncryptedBlobClient bec; // encrypted client for download
    private KeyClient keyClient;
    private String keyId;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        String keyVaultUrl = "https://azstoragesdkvault.vault.azure.net/";
        if (getTestMode() != TestMode.PLAYBACK) {
            keyVaultUrl = Configuration.getGlobalConfiguration().get("KEYVAULT_URL");
        }

        keyClient = new KeyClientBuilder()
            .pipeline(getHttpPipeline(KEY_SERVICE_VERSION))
            .httpClient(getHttpClient())
            .vaultUrl(keyVaultUrl)
            .serviceVersion(KEY_SERVICE_VERSION)
            .buildClient();

        keyId = testResourceNamer.randomName(prefix, 50);

        KeyVaultKey keyVaultKey = keyClient.createRsaKey(new CreateRsaKeyOptions(keyId)
            .setExpiresOn(testResourceNamer.now().plusYears(1))
            .setKeySize(2048));

        AsyncKeyEncryptionKey akek = new KeyEncryptionKeyClientBuilder()
            .pipeline(getHttpPipeline(KEY_SERVICE_VERSION))
            .httpClient(getHttpClient())
            .serviceVersion(CRYPTOGRAPHY_SERVICE_VERSION)
            .buildAsyncKeyEncryptionKey(keyVaultKey.getId())
            .block();

        cc = getServiceClientBuilder(ENV.getPrimaryAccount())
            .buildClient()
            .getBlobContainerClient(generateContainerName());
        cc.create();

        bec = getEncryptedClientBuilder(akek, null, ENV.getPrimaryAccount().getCredential(), cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildEncryptedBlobClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        keyClient.beginDeleteKey(keyId);
    }

    @Test
    public void uploadDownload() {
        byte[] inputArray = getRandomByteArray(Constants.KB);
        InputStream stream = new ByteArrayInputStream(inputArray);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        bec.upload(stream, Constants.KB);
        bec.download(os);

        assertArraysEqual(inputArray, os.toByteArray());
    }

    @Test
    public void encryptionNotANoop() {
        byte[] inputArray = getRandomByteArray(Constants.KB);
        InputStream stream = new ByteArrayInputStream(inputArray);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        bec.upload(stream, Constants.KB);
        cc.getBlobClient(bec.getBlobName()).download(os);

        assertFalse(Arrays.equals(inputArray, os.toByteArray()));
    }

    private HttpPipeline getHttpPipeline(KeyServiceVersion serviceVersion) {
        Configuration global = Configuration.getGlobalConfiguration().clone();
        TokenCredential credential;

        credential = StorageCommonTestUtils.getTokenCredential(interceptorManager);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy("client_name", "client_version", global, serviceVersion));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        RetryStrategy strategy = new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16));
        policies.add(new RetryPolicy(strategy));
        policies.add(new BearerTokenAuthenticationPolicy(credential, "https://vault.azure.net/.default"));
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(getHttpClient())
            .build();
    }
}
