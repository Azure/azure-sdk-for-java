package com.azure.storage.blob.specialized.cryptography

class BlobCryptographyBuilderTest extends APISpec {

    def beac
    def bc
    def cc

    def setup() {
        def keyId = "keyId"
        def fakeKey = new FakeKey(keyId, getRandomByteArray(256))
        def fakeKeyResolver = new FakeKeyResolver(fakeKey)

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


}
