package com.azure.storage.blob.specialized.cryptography

import com.azure.core.cryptography.AsyncKeyEncryptionKey
import com.azure.core.test.TestMode
import com.azure.core.util.Configuration
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.identity.EnvironmentCredentialBuilder
import com.azure.security.keyvault.keys.KeyClient
import com.azure.security.keyvault.keys.KeyClientBuilder
import com.azure.security.keyvault.keys.cryptography.KeyEncryptionKeyClientBuilder
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions
import com.azure.security.keyvault.keys.models.KeyVaultKey
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.common.implementation.Constants

import java.time.OffsetDateTime

class KeyvaultKeyTest extends APISpec {

    BlobContainerClient cc
    EncryptedBlobClient bec // encrypted client for download
    KeyClient keyClient
    String keyId

    def setup() {
        def keyVaultUrl = Configuration.getGlobalConfiguration().get("KEYVAULT_URL")

        KeyClientBuilder builder = new KeyClientBuilder()

        if (testMode != TestMode.PLAYBACK) {
            if (testMode == TestMode.RECORD) {
                builder.addPolicy(interceptorManager.getRecordPolicy())
            }
            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            builder.credential(new EnvironmentCredentialBuilder().build())
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build())
        }

        keyClient = builder
            .httpClient(getHttpClient())
            .vaultUrl(keyVaultUrl)
            .buildClient()

        keyId = generateResourceName("keyId", entityNo++)

        KeyVaultKey keyVaultKey = keyClient.createRsaKey(new CreateRsaKeyOptions(keyId)
            .setExpiresOn(OffsetDateTime.now().plusYears(1))
            .setKeySize(2048))

        AsyncKeyEncryptionKey akek = new KeyEncryptionKeyClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
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

}
