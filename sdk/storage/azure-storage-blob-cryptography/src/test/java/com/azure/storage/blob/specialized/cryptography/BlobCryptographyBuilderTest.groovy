package com.azure.storage.blob.specialized.cryptography

import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.CustomerProvidedKey

class BlobCryptographyBuilderTest extends APISpec {

    def beac
    def bc
    def cc

    def fakeKey
    def fakeKeyResolver
    def keyId

    def setup() {
        keyId = "keyId"
        fakeKey = new FakeKey(keyId, getRandomByteArray(256))
        fakeKeyResolver = new FakeKeyResolver(fakeKey)

        def sc = getServiceClientBuilder(primaryCredential,
            String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .buildClient()
        def containerName = generateContainerName()
        def blobName = generateBlobName()
        cc = sc.getBlobContainerClient(containerName)
        bc = cc.getBlobClient(blobName)

        beac = new EncryptedBlobClientBuilder()
            .blobName(blobName)
            .key(fakeKey, "keyWrapAlgorithm")
            .keyResolver(fakeKeyResolver)
            .blobClient(bc)
            .buildEncryptedBlobAsyncClient()
    }

    def "Pipeline integrity"() {
        expect:
        // Http pipeline of encrypted client additionally includes decryption policy
        beac.getHttpPipeline().getPolicyCount() == bc.getHttpPipeline().getPolicyCount() + 1

        beac.getBlobUrl() == bc.getBlobUrl()

        // Compare all policies
        for (int i = 0; i < bc.getHttpPipeline().getPolicyCount(); i++) {
            beac.getHttpPipeline().getPolicy(i+1) == bc.getHttpPipeline().getPolicy(i)
        }
    }

    def "Encrypted client integrity"() {
        setup:
        cc.create()
        def file = getRandomFile(KB)

        when:
        beac.uploadFromFile(file.toPath().toString()).block()

        then:
        compareDataToFile(beac.download(), file)
    }

    def "Http pipeline"() {
        when:
        def regularClient = cc.getBlobClient(generateBlobName())
        def encryptedClient = getEncryptedClientBuilder(fakeKey, null, primaryCredential, cc.getBlobContainerUrl())
            .pipeline(regularClient.getHttpPipeline())
            .blobName(regularClient.getBlobName())
            .buildEncryptedBlobClient()

        then:
        // Checks that there is one less policy in a regular client and that the extra policy is a decryption policy
        regularClient.getHttpPipeline().getPolicyCount() == encryptedClient.getHttpPipeline().getPolicyCount() - 1
        encryptedClient.getHttpPipeline().getPolicy(0) instanceof BlobDecryptionPolicy
    }

    def "Customer provided key"() {
        setup:
        cc.create()
        CustomerProvidedKey key = new CustomerProvidedKey(getRandomKey())
        def builder = getEncryptedClientBuilder(fakeKey, null, primaryCredential, cc.getBlobContainerUrl())
            .customerProvidedKey(key)
            .blobName(generateBlobName())
        def encryptedAsyncClient = builder.buildEncryptedBlobAsyncClient()
        def encryptedClient = builder.buildEncryptedBlobClient()

        when:
        def uploadResponse = encryptedAsyncClient.uploadWithResponse(defaultFlux, null, null, null, null, null).block()

        def downloadResult = new ByteArrayOutputStream()

        encryptedClient.download(downloadResult)

        then:
        uploadResponse.getStatusCode() == 201
        uploadResponse.getValue().isServerEncrypted()
        uploadResponse.getValue().getEncryptionKeySha256() == key.getKeySha256()
        downloadResult.toByteArray() == defaultData.array()
    }

    def "Customer provided key not a noop"() {
        setup:
        cc.create()
        CustomerProvidedKey key = new CustomerProvidedKey(getRandomKey())
        def encryptedClientWithCpk = getEncryptedClientBuilder(fakeKey, null, primaryCredential, cc.getBlobContainerUrl())
            .customerProvidedKey(key)
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient()

        def encryptedClientNoCpk = getEncryptedClientBuilder(fakeKey, null, primaryCredential, encryptedClientWithCpk.getBlobUrl())
            .buildEncryptedBlobClient()

        when:
        encryptedClientWithCpk.uploadWithResponse(defaultFlux, null, null, null, null, null).block()

        def datastream = new ByteArrayOutputStream()
        encryptedClientNoCpk.downloadWithResponse(datastream, null, null, null, false, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getStatusCode() == 409
    }

    def "Conflicting encryption info"() {
        when:
        new EncryptedBlobClientBuilder()
            .blobAsyncClient(beac)
            .key(fakeKey, "keywrapalgorithm")
            .buildEncryptedBlobAsyncClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "Key after pipeline"() {
        when:
        new EncryptedBlobClientBuilder()
            .blobClient(bc)
            .key(fakeKey, "keywrapalgorithm")
            .buildEncryptedBlobClient()

        then:
        notThrown(IllegalArgumentException)

    }
}
