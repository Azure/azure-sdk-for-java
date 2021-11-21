package com.azure.storage.blob.specialized.cryptography

import com.azure.core.cryptography.AsyncKeyEncryptionKey
import com.azure.security.keyvault.keys.cryptography.CryptographyServiceVersion
import com.azure.security.keyvault.keys.cryptography.KeyEncryptionKeyClientBuilder
import com.azure.security.keyvault.keys.models.JsonWebKey
import com.azure.security.keyvault.keys.models.KeyOperation
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.common.implementation.Constants

import javax.crypto.spec.SecretKeySpec

class LocalKeyTest extends APISpec {
    CryptographyServiceVersion cryptographyServiceVersion = CryptographyServiceVersion.V7_2
    BlobContainerClient cc
    EncryptedBlobClient bec // encrypted client for download

    def setup() {

        /* Insecurely generate a local key*/
        def byteKey = getRandomByteArray(256)

        JsonWebKey localKey = JsonWebKey.fromAes(new SecretKeySpec(byteKey, "AES"),
            Arrays.asList(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY))
            .setId("local")
        AsyncKeyEncryptionKey akek = new KeyEncryptionKeyClientBuilder()
            .serviceVersion(cryptographyServiceVersion)
            .buildAsyncKeyEncryptionKey(localKey)
            .block()

        cc = getServiceClientBuilder(environment.primaryAccount)
            .buildClient()
            .getBlobContainerClient(generateContainerName())
        cc.create()

        bec = getEncryptedClientBuilder(akek, null, environment.primaryAccount.credential,
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
