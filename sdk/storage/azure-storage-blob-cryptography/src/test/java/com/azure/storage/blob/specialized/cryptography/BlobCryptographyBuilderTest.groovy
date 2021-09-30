package com.azure.storage.blob.specialized.cryptography

import com.azure.core.cryptography.AsyncKeyEncryptionKey
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver
import com.azure.core.test.TestMode
import com.azure.storage.blob.implementation.util.BlobUserAgentModificationPolicy
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.CustomerProvidedKey
import com.azure.storage.common.implementation.Constants

class BlobCryptographyBuilderTest extends APISpec {
    def beac
    def bc
    def cc

    AsyncKeyEncryptionKey fakeKey
    AsyncKeyEncryptionKeyResolver fakeKeyResolver
    def keyId

    def setup() {
        keyId = "keyId"

        fakeKey = new FakeKey(keyId, (getEnvironment().getTestMode() == TestMode.LIVE) ? getRandomByteArray(256) : mockRandomData)
        fakeKeyResolver = new FakeKeyResolver(fakeKey)

        def sc = getServiceClientBuilder(environment.primaryAccount)
            .buildClient()
        def containerName = generateContainerName()
        def blobName = generateBlobName()
        cc = sc.getBlobContainerClient(containerName)
        bc = cc.getBlobClient(blobName)

        beac = mockAesKey(new EncryptedBlobClientBuilder()
            .blobName(blobName)
            .key(fakeKey, "keyWrapAlgorithm")
            .keyResolver(fakeKeyResolver)
            .blobClient(bc)
            .buildEncryptedBlobAsyncClient())
    }

    def "Pipeline integrity"() {
        expect:
        // Http pipeline of encrypted client additionally includes decryption policy and blob user agent modification policy
        beac.getHttpPipeline().getPolicyCount() == bc.getHttpPipeline().getPolicyCount() + 2

        beac.getBlobUrl() == bc.getBlobUrl()

        // Compare all policies
        for (int i = 0; i < bc.getHttpPipeline().getPolicyCount(); i++) {
            beac.getHttpPipeline().getPolicy(i + 1) == bc.getHttpPipeline().getPolicy(i)
        }
    }

    def "Encrypted client integrity"() {
        setup:
        cc.create()
        def file = getRandomFile(Constants.KB)

        when:
        beac.uploadFromFile(file.toPath().toString()).block()

        then:
        compareDataToFile(beac.download(), file)
    }

    def "Http pipeline"() {
        when:
        def regularClient = cc.getBlobClient(generateBlobName())
        def encryptedClient = new EncryptedBlobClient(mockAesKey(getEncryptedClientBuilder(fakeKey, null, environment.primaryAccount.credential, cc.getBlobContainerUrl())
            .pipeline(regularClient.getHttpPipeline())
            .blobName(regularClient.getBlobName())
            .buildEncryptedBlobAsyncClient()))

        then:
        // Checks that there is one less policy in a regular client and that the extra policy is a decryption policy and a blob user agent modification policy
        regularClient.getHttpPipeline().getPolicyCount() == encryptedClient.getHttpPipeline().getPolicyCount() - 2
        encryptedClient.getHttpPipeline().getPolicy(0) instanceof BlobDecryptionPolicy
        encryptedClient.getHttpPipeline().getPolicy(2) instanceof BlobUserAgentModificationPolicy
    }

    def "Customer provided key"() {
        setup:
        cc.create()
        CustomerProvidedKey key = new CustomerProvidedKey(getRandomKey())
        def builder = getEncryptedClientBuilder(fakeKey, null, environment.primaryAccount.credential, cc.getBlobContainerUrl())
            .customerProvidedKey(key)
            .blobName(generateBlobName())
        def encryptedAsyncClient = mockAesKey(builder.buildEncryptedBlobAsyncClient())
        def encryptedClient = new EncryptedBlobClient(mockAesKey(builder.buildEncryptedBlobAsyncClient()))

        when:
        def uploadResponse = encryptedAsyncClient.uploadWithResponse(data.defaultFlux, null, null, null, null, null).block()

        def downloadResult = new ByteArrayOutputStream()

        encryptedClient.download(downloadResult)

        then:
        uploadResponse.getStatusCode() == 201
        uploadResponse.getValue().isServerEncrypted()
        uploadResponse.getValue().getEncryptionKeySha256() == key.getKeySha256()
        downloadResult.toByteArray() == data.defaultBytes
    }

    def "Customer provided key not a noop"() {
        setup:
        cc.create()
        CustomerProvidedKey key = new CustomerProvidedKey(getRandomKey())
        def encryptedClientWithCpk = mockAesKey(getEncryptedClientBuilder(fakeKey, null, environment.primaryAccount.credential, cc.getBlobContainerUrl())
            .customerProvidedKey(key)
            .blobName(generateBlobName())
            .buildEncryptedBlobAsyncClient())

        def encryptedClientNoCpk = new EncryptedBlobClient(mockAesKey(getEncryptedClientBuilder(fakeKey, null, environment.primaryAccount.credential, encryptedClientWithCpk.getBlobUrl())
            .buildEncryptedBlobAsyncClient()))

        when:
        encryptedClientWithCpk.uploadWithResponse(data.defaultFlux, null, null, null, null, null).block()

        def datastream = new ByteArrayOutputStream()
        encryptedClientNoCpk.downloadWithResponse(datastream, null, null, null, false, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getStatusCode() == 409
    }

    def "Encryption scope"() {
        setup:
        def scope = "testscope1"
        cc.create()
        def builder = getEncryptedClientBuilder(fakeKey, null, environment.primaryAccount.credential, cc.getBlobContainerUrl())
            .encryptionScope(scope)
            .blobName(generateBlobName())
        def encryptedAsyncClient = mockAesKey(builder.buildEncryptedBlobAsyncClient())
        def encryptedClient = new EncryptedBlobClient(mockAesKey(builder.buildEncryptedBlobAsyncClient()))

        when:
        def uploadResponse = encryptedAsyncClient.uploadWithResponse(data.defaultFlux, null, null, null, null, null).block()

        def downloadResult = new ByteArrayOutputStream()

        encryptedClient.download(downloadResult)

        then:
        uploadResponse.getStatusCode() == 201
        downloadResult.toByteArray() == data.defaultBytes
        encryptedClient.getProperties().getEncryptionScope() == scope
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

    def "getCustomerProvidedKeyClient"() {
        setup:
        CustomerProvidedKey originalKey = new CustomerProvidedKey(getRandomKey())
        def client = getEncryptedClientBuilder(fakeKey, null, environment.primaryAccount.credential, cc.getBlobContainerUrl())
            .customerProvidedKey(originalKey)
            .blobName(generateBlobName())
            .buildEncryptedBlobClient()
        def newCpk = new CustomerProvidedKey(getRandomKey())

        when:
        def newClient = client.getCustomerProvidedKeyClient(newCpk)

        then:
        newClient instanceof EncryptedBlobClient
        newClient.getCustomerProvidedKey() != client.getCustomerProvidedKey()
    }

    def "getEncryptionScopeClient"() {
        setup:
        def originalScope = "testscope1"
        def client = getEncryptedClientBuilder(fakeKey, null, environment.primaryAccount.credential, cc.getBlobContainerUrl())
            .encryptionScope(originalScope)
            .blobName(generateBlobName())
            .buildEncryptedBlobClient()
        def newEncryptionScope = "newtestscope"

        when:
        def newClient = client.getEncryptionScopeClient(newEncryptionScope)

        then:
        newClient instanceof EncryptedBlobClient
        newClient.getEncryptionScope() != client.getEncryptionScope()

    }
}
