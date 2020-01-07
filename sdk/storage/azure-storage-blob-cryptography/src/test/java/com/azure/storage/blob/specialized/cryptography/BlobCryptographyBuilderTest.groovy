package com.azure.storage.blob.specialized.cryptography

import spock.lang.Requires

class BlobCryptographyBuilderTest extends APISpec {

    def beac
    def cc

    def setup() {
        def keyId = "keyId"
        def fakeKey = new FakeKey(keyId, 256)
        def fakeKeyResolver = new FakeKeyResolver(fakeKey)

        def sc = getServiceClientBuilder(primaryCredential,
            String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .buildClient()
        def containerName = generateContainerName()
        def blobName = generateBlobName()
        cc = sc.getBlobContainerClient(containerName)

        beac = new EncryptedBlobClientBuilder()
            .blobName(blobName)
            .key(fakeKey, "keyWrapAlgorithm")
            .keyResolver(fakeKeyResolver)
            .containerClient(cc)
            .buildEncryptedBlobAsyncClient()
    }

    def "Pipeline integrity"() {
        expect:
        // Http pipeline of encrypted client additionally includes decryption policy
        beac.getHttpPipeline().getPolicyCount() == cc.getHttpPipeline().getPolicyCount() + 1

        // Compare all policies
        for (int i = 0; i < cc.getHttpPipeline().getPolicyCount(); i++) {
            beac.getHttpPipeline().getPolicy(i+1) == cc.getHttpPipeline().getPolicy(i)
        }
    }

    @Requires({ liveMode() })
    def "Encrypted client integrity"() {
        setup:
        cc.create()
        def file = getRandomFile(KB)

        when:
        beac.uploadFromFile(file.toPath().toString()).block()

        then:
        compareDataToFile(beac.download(), file)
    }


}
