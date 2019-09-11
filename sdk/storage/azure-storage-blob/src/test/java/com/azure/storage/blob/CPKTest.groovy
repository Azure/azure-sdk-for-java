package com.azure.storage.blob

import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.storage.blob.models.CustomerProvidedKey
import com.azure.storage.blob.models.Metadata
import com.azure.storage.blob.models.PageRange
import com.azure.storage.common.Constants

import java.time.OffsetDateTime

class CPKTest extends APISpec {

    CustomerProvidedKey key
    ContainerClient cpkContainer
    BlockBlobClient cpkBlockBlob
    PageBlobClient cpkPageBlob
    AppendBlobClient cpkAppendBlob
    BlobClient cpkExistingBlob

    def setup() {
        key = new CustomerProvidedKey(getRandomKey())
        def builder = new ContainerClientBuilder()
            .endpoint(cc.getContainerUrl().toString())
            .customerProvidedKey(key)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .credential(primaryCredential)

        addOptionalRecording(builder)

        cpkContainer = builder.buildClient()
        cpkBlockBlob = cpkContainer.getBlockBlobClient(generateBlobName())
        cpkPageBlob = cpkContainer.getPageBlobClient(generateBlobName())
        cpkAppendBlob = cpkContainer.getAppendBlobClient(generateBlobName())

        def existingBlobSetup = cpkContainer.getBlockBlobClient(generateBlobName())
        existingBlobSetup.upload(defaultInputStream.get(), defaultDataSize)
        cpkExistingBlob = existingBlobSetup
    }

    /**
     * Insecurely and quickly generates a random AES256 key for the purpose of unit tests. No one should ever make a
     * real key this way.
     */
    def getRandomKey(long seed = new Random().nextLong()) {
        def key = new byte[32] // 256 bit key
        new Random(seed).nextBytes(key)
        return key
    }

    def "Put blob with CPK"() {
        when:
        def response = cpkBlockBlob.uploadWithResponse(defaultInputStream.get(), defaultDataSize,
            null, null, null, null, null, null)

        then:
        response.statusCode() == 201
        response.value().isServerEncrypted()
        response.value().getEncryptionKeySha256() == key.getKeySHA256()
    }

    def "Get blob with CPK"() {
        setup:
        cpkBlockBlob.upload(defaultInputStream.get(), defaultDataSize)
        def datastream = new ByteArrayOutputStream()

        when:
        def response = cpkBlockBlob.downloadWithResponse(datastream, null, null, null, false, null, null)

        then:
        response.statusCode() == 200
        datastream.toByteArray() == defaultData.array()
    }

    def "Put block with CPK"() {
        when:
        def response = cpkBlockBlob.stageBlockWithResponse(getBlockID(), defaultInputStream.get(), defaultDataSize,
            null, null, null)

        then:
        response.statusCode() == 201
        Boolean.parseBoolean(response.headers().value(Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED))
    }

    def "Put block from URL with CPK"() {
        setup:
        def sourceBlob = cc.getBlockBlobClient(generateBlobName())
        sourceBlob.upload(defaultInputStream.get(), defaultDataSize)

        when:
        def response = cpkBlockBlob.stageBlockFromURLWithResponse(getBlockID(),
            new URL(sourceBlob.getBlobUrl().toString() + "?" + sourceBlob.generateSAS(OffsetDateTime.now().plusHours(1), new BlobSASPermission().setRead(true))),
            null, null, null, null, null, null)

        then:
        response.statusCode() == 201
        Boolean.parseBoolean(response.headers().value(Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED))
    }

    def "Put block list with CPK"() {
        setup:
        def blockIDList = [getBlockID(), getBlockID()]
        for (def blockId in blockIDList) {
            cpkBlockBlob.stageBlock(blockId, defaultInputStream.get(), defaultDataSize)
        }

        when:
        def response = cpkBlockBlob.commitBlockListWithResponse(blockIDList, null, null, null, null, null, null)

        then:
        response.statusCode() == 201
        response.value().isServerEncrypted()
        response.value().getEncryptionKeySha256() == key.getKeySHA256()
    }

    def "Put page with CPK"() {
        setup:
        cpkPageBlob.setCreate(PageBlobClient.PAGE_BYTES)

        when:
        def response = cpkPageBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null)

        then:
        response.statusCode() == 201
        response.value().isServerEncrypted()
        response.value().getEncryptionKeySha256() == key.getKeySHA256()
    }

    def "Put page from URL wih CPK"() {
        setup:
        def sourceBlob = cc.getPageBlobClient(generateBlobName())
        sourceBlob.setCreate(PageBlobClient.PAGE_BYTES)
        sourceBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null)

        cpkPageBlob.setCreate(PageBlobClient.PAGE_BYTES)

        when:
        def response = cpkPageBlob.uploadPagesFromURLWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new URL(sourceBlob.getBlobUrl().toString() + "?" + sourceBlob.generateSAS(OffsetDateTime.now().plusHours(1), new BlobSASPermission().setRead(true))),
            null, null, null, null, null, null)

        then:
        response.statusCode() == 201
        response.value().isServerEncrypted()
        //TODO uncomment when swagger is fixed so PageBlobUploadPagesFromURLHeaders contains the encryption SHA
        //response.value().setEncryptionKeySha256() == key.getKeySHA256()
    }

    def "Put multiple pages with CPK"() {
        setup:
        cpkPageBlob.setCreate(PageBlobClient.PAGE_BYTES * 2)

        when:
        def response = cpkPageBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES * 2)), null, null, null)

        then:
        response.statusCode() == 201
        response.value().isServerEncrypted()
        response.value().getEncryptionKeySha256() == key.getKeySHA256()
    }

    def "Append block with CPK"() {
        setup:
        cpkAppendBlob.create()

        when:
        def response = cpkAppendBlob.appendBlockWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null)

        then:
        response.statusCode() == 201
        response.value().isServerEncrypted()
        response.value().getEncryptionKeySha256() == key.getKeySHA256()
    }

    def "Append block from URL with CPK"() {
        setup:
        cpkAppendBlob.create()
        def sourceBlob = cc.getBlockBlobClient(generateBlobName())
        sourceBlob.upload(defaultInputStream.get(), defaultDataSize)

        when:
        def response = cpkAppendBlob.appendBlockFromUrlWithResponse(
            new URL(sourceBlob.getBlobUrl().toString() + "?" + sourceBlob.generateSAS(OffsetDateTime.now().plusHours(1), new BlobSASPermission().setRead(true))),
            null, null, null, null, null, null)

        then:
        response.statusCode() == 201
        //TODO uncomment when swagger is fixed so AppendBlobAppendBLockFromURLHeaders contains isrequestserverencrypted
        //response.value().isServerEncrypted()
        response.value().getEncryptionKeySha256() == key.getKeySHA256()
    }

    def "Set blob metadata with CPK"() {
        setup:
        def metadata = new Metadata()
        metadata.put("foo", "bar")

        when:
        def response = cpkExistingBlob.setMetadataWithResponse(metadata, null, null, null)

        then:
        response.statusCode() == 200
        Boolean.parseBoolean(response.headers().value(Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED))
        response.headers().value(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256) == key.getKeySHA256()
    }

    def "Get blob properties and metadata with CPK"() {
        when:
        def response = cpkExistingBlob.getPropertiesWithResponse(null, null, null)

        then:
        response.statusCode() == 200
        Boolean.parseBoolean(response.headers().value(Constants.HeaderConstants.SERVER_ENCRYPTED))
        response.headers().value(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256) == key.getKeySHA256()
    }

//    TODO unignore when swagger is resolved with service team
//    def "Set blob tier with CPK"() {
//        when:
//        def response = cpkExistingBlob.setTierWithResponse(AccessTier.COOL, null, null, null)
//
//        then:
//        response.getStatusCode() == 200
//        Boolean.parseBoolean(response.headers().value(Constants.HeaderConstants.SERVER_ENCRYPTED))
//        response.headers().value(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256) == key.getKeySHA256()
//    }

    def "Snapshot blob with CPK"() {
        setup:
        def metadata = new Metadata()
        metadata.put("foo", "bar")

        when:
        def response = cpkExistingBlob.createSnapshotWithResponse(null, null, null, null)

        then:
        response.statusCode() == 201
    }

    //TODO add tests for copy blob CPK tests once generated code supports it
}
