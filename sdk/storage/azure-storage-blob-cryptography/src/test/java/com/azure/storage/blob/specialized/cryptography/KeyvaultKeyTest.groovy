package com.azure.storage.blob.specialized.cryptography


import com.azure.core.credential.TokenCredential
import com.azure.core.cryptography.AsyncKeyEncryptionKey
import com.azure.core.http.HttpClient
import com.azure.core.http.HttpPipeline
import com.azure.core.http.HttpPipelineBuilder
import com.azure.core.http.policy.*
import com.azure.core.test.TestMode
import com.azure.core.util.Configuration
import com.azure.identity.ClientSecretCredentialBuilder
import com.azure.security.keyvault.keys.KeyClient
import com.azure.security.keyvault.keys.KeyClientBuilder
import com.azure.security.keyvault.keys.KeyServiceVersion
import com.azure.security.keyvault.keys.cryptography.KeyEncryptionKeyClientBuilder
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions
import com.azure.security.keyvault.keys.models.KeyVaultKey
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.common.implementation.Constants

import java.time.Duration
import java.time.OffsetDateTime

class KeyvaultKeyTest extends APISpec {

    BlobContainerClient cc
    EncryptedBlobClient bec // encrypted client for download
    KeyClient keyClient
    String keyId

    def setup() {
        def keyVaultUrl = "https://azstoragesdkvault.vault.azure.net/"
        if (testMode != TestMode.PLAYBACK) {
            keyVaultUrl = Configuration.getGlobalConfiguration().get("KEYVAULT_URL")
        }

        keyClient = new KeyClientBuilder()
            .pipeline(getHttpPipeline(getHttpClient(), KeyServiceVersion.V7_1))
            .httpClient(getHttpClient())
            .vaultUrl(keyVaultUrl)
            .buildClient()

        keyId = generateResourceName("keyId", entityNo++)

        KeyVaultKey keyVaultKey = keyClient.createRsaKey(new CreateRsaKeyOptions(keyId)
            .setExpiresOn(OffsetDateTime.now().plusYears(1))
            .setKeySize(2048))

        AsyncKeyEncryptionKey akek = new KeyEncryptionKeyClientBuilder()
            .pipeline(getHttpPipeline(getHttpClient(), KeyServiceVersion.V7_1))
            .httpClient(getHttpClient())
            .buildAsyncKeyEncryptionKey(keyVaultKey.getId())
            .block()

        cc = getServiceClientBuilder(primaryCredential,
            String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .buildClient()
            .getBlobContainerClient(generateContainerName())
        cc.create()

        bec = getEncryptedClientBuilder(akek, null, primaryCredential,
            cc.getBlobContainerUrl().toString())
            .blobName(generateBlobName())
            .buildEncryptedBlobClient()
    }

    def cleanup() {
        keyClient.beginDeleteKey(keyId)
    }

    def "upload download"() {
        setup:
        def inputArray = getRandomByteArray(Constants.KB)
        InputStream stream = new ByteArrayInputStream(inputArray)
        def os = new ByteArrayOutputStream()

        when:
        bec.upload(stream, Constants.KB)
        bec.download(os)

        then:
        inputArray == os.toByteArray()
    }


    def "encryption not a noop"() {
        setup:
        def inputArray = getRandomByteArray(Constants.KB)
        InputStream stream = new ByteArrayInputStream(inputArray)
        def os = new ByteArrayOutputStream()

        when:
        bec.upload(stream, Constants.KB)
        cc.getBlobClient(bec.getBlobName()).download(os)

        then:
        inputArray != os.toByteArray()
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            String clientId = System.getenv("AZURE_CLIENT_ID");
            String clientKey = System.getenv("AZURE_CLIENT_SECRET");
            String tenantId = System.getenv("AZURE_TENANT_ID");
            Objects.requireNonNull(clientId, "The client id cannot be null");
            Objects.requireNonNull(clientKey, "The client key cannot be null");
            Objects.requireNonNull(tenantId, "The tenant id cannot be null");
            credential = new ClientSecretCredentialBuilder()
                .clientSecret(clientKey)
                .clientId(clientId)
                .tenantId(tenantId)
                .build();
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy("client_name", "client_version",  Configuration.getGlobalConfiguration().clone(), serviceVersion));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        RetryStrategy strategy = new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16));
        policies.add(new RetryPolicy(strategy));
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, "https://vault.azure.net/.default"));
        }
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (!interceptorManager.isPlaybackMode()) {
            if (testMode == TestMode.RECORD) {
                policies.add(interceptorManager.getRecordPolicy());
            }
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build()

        return pipeline;
    }

}
