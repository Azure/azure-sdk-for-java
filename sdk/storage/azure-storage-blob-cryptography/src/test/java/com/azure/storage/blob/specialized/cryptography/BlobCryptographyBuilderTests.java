// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.implementation.util.BlobUserAgentModificationPolicy;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobCryptographyBuilderTests extends BlobCryptographyTestBase {
    private static final String KEY_ID = "keyId";
    private EncryptedBlobAsyncClient beac;
    private BlobClient bc;
    private BlobContainerClient cc;
    private FakeKey fakeKey;

    @Override
    protected void beforeTest() {
        super.beforeTest();

        fakeKey = new FakeKey(KEY_ID, (getTestMode() == TestMode.LIVE) ? getRandomByteArray(256) : MOCK_RANDOM_DATA);
        AsyncKeyEncryptionKeyResolver fakeKeyResolver = new FakeKeyResolver(fakeKey);

        BlobServiceClient sc = getServiceClientBuilder(ENV.getPrimaryAccount()).buildClient();
        cc = sc.getBlobContainerClient(generateContainerName());
        bc = cc.getBlobClient(generateBlobName());

        beac = mockAesKey(new EncryptedBlobClientBuilder().blobName(bc.getBlobName())
            .key(fakeKey, "keyWrapAlgorithm")
            .keyResolver(fakeKeyResolver)
            .blobClient(bc)
            .buildEncryptedBlobAsyncClient());
    }

    @Test
    public void pipelineIntegrity() {
        // Http pipeline of encrypted client additionally includes decryption policy,
        // blob user agent modification policy, and fetch encryption version policy
        assertEquals(bc.getHttpPipeline().getPolicyCount() + 2, beac.getHttpPipeline().getPolicyCount());
        assertEquals(bc.getBlobUrl(), beac.getBlobUrl());

        List<HttpPipelinePolicy> originalPolicies = getAllPolicies(bc.getHttpPipeline());
        List<HttpPipelinePolicy> encryptionPolicies = getAllPolicies(beac.getHttpPipeline());

        assertTrue(encryptionPolicies.removeAll(originalPolicies));
        assertEquals(2, encryptionPolicies.size());
        assertTrue(encryptionPolicies.stream().anyMatch(policy -> policy instanceof BlobDecryptionPolicy));
        assertTrue(encryptionPolicies.stream().anyMatch(policy -> policy instanceof BlobUserAgentModificationPolicy));
    }

    private static List<HttpPipelinePolicy> getAllPolicies(HttpPipeline pipeline) {
        List<HttpPipelinePolicy> policies = new ArrayList<>(pipeline.getPolicyCount());
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            policies.add(pipeline.getPolicy(i));
        }

        return policies;
    }

    @Test
    public void encryptedClientIntegrity() throws IOException {
        cc.create();
        File file = getRandomFile(Constants.KB);
        beac.uploadFromFile(file.toPath().toString()).block();

        compareDataToFile(beac.download(), file);
    }

    @Test
    public void httpPipeline() {
        BlobClient regularClient = cc.getBlobClient(generateBlobName());
        EncryptedBlobClient encryptedClient = new EncryptedBlobClient(mockAesKey(
            getEncryptedClientBuilder(fakeKey, null, ENV.getPrimaryAccount().getCredential(), cc.getBlobContainerUrl())
                .pipeline(regularClient.getHttpPipeline())
                .blobName(regularClient.getBlobName())
                .buildEncryptedBlobAsyncClient()));

        // Checks that there is one less policy in a regular client and that the extra policy is a decryption policy
        // and a blob user agent modification policy
        assertEquals(encryptedClient.getHttpPipeline().getPolicyCount() - 2,
            regularClient.getHttpPipeline().getPolicyCount());
        assertInstanceOf(BlobDecryptionPolicy.class, encryptedClient.getHttpPipeline().getPolicy(0));
        assertInstanceOf(BlobUserAgentModificationPolicy.class, encryptedClient.getHttpPipeline().getPolicy(2));
    }

    @Test
    public void customerProvidedKey() {
        cc.create();
        CustomerProvidedKey key = new CustomerProvidedKey(getRandomKey(null));
        EncryptedBlobClientBuilder builder
            = getEncryptedClientBuilder(fakeKey, null, ENV.getPrimaryAccount().getCredential(),
                cc.getBlobContainerUrl()).customerProvidedKey(key).blobName(generateBlobName());
        if (getTestMode() == TestMode.PLAYBACK) {
            // Needed to solve echo header validation.
            builder.addPolicy(new PlaybackKeySha256Policy());
        }

        EncryptedBlobAsyncClient encryptedAsyncClient = mockAesKey(builder.buildEncryptedBlobAsyncClient());
        EncryptedBlobClient encryptedClient
            = new EncryptedBlobClient(mockAesKey(builder.buildEncryptedBlobAsyncClient()));

        Response<BlockBlobItem> uploadResponse
            = encryptedAsyncClient.uploadWithResponse(DATA.getDefaultFlux(), null, null, null, null, null).block();
        ByteArrayOutputStream downloadResult = new ByteArrayOutputStream();
        encryptedClient.download(downloadResult);

        assertEquals(201, uploadResponse.getStatusCode());
        assertTrue(uploadResponse.getValue().isServerEncrypted());
        assertEquals((getTestMode() == TestMode.PLAYBACK) ? "REDACTEDREDACTED" : key.getKeySha256(),
            uploadResponse.getValue().getEncryptionKeySha256());
        assertArraysEqual(DATA.getDefaultBytes(), downloadResult.toByteArray());
    }

    @Test
    public void customerProvidedKeyNotANoop() {
        cc.create();
        CustomerProvidedKey key = new CustomerProvidedKey(getRandomKey(null));
        EncryptedBlobClientBuilder builder
            = getEncryptedClientBuilder(fakeKey, null, ENV.getPrimaryAccount().getCredential(),
                cc.getBlobContainerUrl()).customerProvidedKey(key).blobName(generateBlobName());
        if (getTestMode() == TestMode.PLAYBACK) {
            // Needed to solve echo header validation.
            builder.addPolicy(new PlaybackKeySha256Policy());
        }

        EncryptedBlobAsyncClient encryptedAsyncClientWithCpk = mockAesKey(builder.buildEncryptedBlobAsyncClient());
        EncryptedBlobClient encryptedClientNoCpk
            = new EncryptedBlobClient(mockAesKey(builder.customerProvidedKey(null).buildEncryptedBlobAsyncClient()));

        encryptedAsyncClientWithCpk.uploadWithResponse(DATA.getDefaultFlux(), null, null, null, null, null).block();
        ByteArrayOutputStream datastream = new ByteArrayOutputStream();

        BlobStorageException e = assertThrows(BlobStorageException.class,
            () -> encryptedClientNoCpk.downloadWithResponse(datastream, null, null, null, false, null, null));
        assertEquals(409, e.getStatusCode());
    }

    @Test
    public void encryptionScope() {
        String scope = "testscope1";
        cc.create();
        EncryptedBlobClientBuilder builder
            = getEncryptedClientBuilder(fakeKey, null, ENV.getPrimaryAccount().getCredential(),
                cc.getBlobContainerUrl()).encryptionScope(scope).blobName(generateBlobName());
        EncryptedBlobAsyncClient encryptedAsyncClient = mockAesKey(builder.buildEncryptedBlobAsyncClient());
        EncryptedBlobClient encryptedClient
            = new EncryptedBlobClient(mockAesKey(builder.buildEncryptedBlobAsyncClient()));

        Response<BlockBlobItem> uploadResponse
            = encryptedAsyncClient.uploadWithResponse(DATA.getDefaultFlux(), null, null, null, null, null).block();
        ByteArrayOutputStream downloadResult = new ByteArrayOutputStream();
        encryptedClient.download(downloadResult);

        assertEquals(201, uploadResponse.getStatusCode());
        assertArraysEqual(DATA.getDefaultBytes(), downloadResult.toByteArray());
        assertEquals(scope, encryptedClient.getProperties().getEncryptionScope());
    }

    @Test
    public void conflictingEncryptionInfo() {
        assertThrows(IllegalArgumentException.class,
            () -> new EncryptedBlobClientBuilder().blobAsyncClient(beac)
                .blobName("blob")
                .key(fakeKey, "keywrapalgorithm")
                .buildEncryptedBlobAsyncClient());
    }

    @Test
    public void keyAfterPipeline() {
        assertDoesNotThrow(() -> new EncryptedBlobClientBuilder().blobClient(bc)
            .key(fakeKey, "keywrapalgorithm")
            .buildEncryptedBlobClient());
    }

    @Test
    public void getCustomerProvidedKeyClient() {
        CustomerProvidedKey originalKey = new CustomerProvidedKey(getRandomKey(null));
        EncryptedBlobClient client = getEncryptedClientBuilder(fakeKey, null, ENV.getPrimaryAccount().getCredential(),
            cc.getBlobContainerUrl()).customerProvidedKey(originalKey)
                .blobName(generateBlobName())
                .buildEncryptedBlobClient();
        CustomerProvidedKey newCpk = new CustomerProvidedKey(getRandomKey(null));
        EncryptedBlobClient newClient = client.getCustomerProvidedKeyClient(newCpk);

        assertNotEquals(client.getCustomerProvidedKey(), newClient.getCustomerProvidedKey());
    }

    @Test
    public void getEncryptionScopeClient() {
        String originalScope = "testscope1";
        EncryptedBlobClient client = getEncryptedClientBuilder(fakeKey, null, ENV.getPrimaryAccount().getCredential(),
            cc.getBlobContainerUrl()).encryptionScope(originalScope)
                .blobName(generateBlobName())
                .buildEncryptedBlobClient();
        String newEncryptionScope = "newtestscope";

        EncryptedBlobClient newClient = client.getEncryptionScopeClient(newEncryptionScope);

        assertNotEquals(client.encryptedBlobAsyncClient.getEncryptionScopeInternal().getEncryptionScope(),
            newClient.encryptedBlobAsyncClient.getEncryptionScopeInternal().getEncryptionScope());
    }

    private static final class PlaybackKeySha256Policy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            // Needed to solve echo header validation.
            if (context.getHttpRequest().getHeaders().get("x-ms-encryption-key-sha256") != null) {
                context.getHttpRequest().setHeader("x-ms-encryption-key-sha256", "REDACTEDREDACTED");
            }
            return next.process();
        }
    }
}
