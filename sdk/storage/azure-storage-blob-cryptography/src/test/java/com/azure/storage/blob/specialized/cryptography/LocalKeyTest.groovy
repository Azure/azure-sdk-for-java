package com.azure.storage.blob.specialized.cryptography

import com.azure.core.cryptography.AsyncKeyEncryptionKey
import com.azure.core.test.TestMode
import com.azure.core.util.Configuration
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.identity.EnvironmentCredentialBuilder
import com.azure.security.keyvault.keys.KeyClient
import com.azure.security.keyvault.keys.KeyClientBuilder
import com.azure.security.keyvault.keys.cryptography.KeyEncryptionKeyClientBuilder
import com.azure.security.keyvault.keys.cryptography.LocalKeyEncryptionKeyClientBuilder
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions
import com.azure.security.keyvault.keys.models.JsonWebKey
import com.azure.security.keyvault.keys.models.KeyOperation
import com.azure.security.keyvault.keys.models.KeyVaultKey
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.common.implementation.Constants

import javax.crypto.spec.SecretKeySpec
import java.time.OffsetDateTime

class LocalKeyTest extends APISpec {

    BlobContainerClient cc
    EncryptedBlobClient bec // encrypted client for download

    def setup() {

        /* Insecurely generate a local key*/
        def byteKey = getRandomByteArray(256)

        JsonWebKey localKey = JsonWebKey.fromAes(new SecretKeySpec(byteKey, "AES"),
            Arrays.asList(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY))
            .setId("local")
        AsyncKeyEncryptionKey akek = new LocalKeyEncryptionKeyClientBuilder()
            .buildAsyncKeyEncryptionKey(localKey)
            .block();

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
