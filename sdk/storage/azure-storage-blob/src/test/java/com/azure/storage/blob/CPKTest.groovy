// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.storage.blob.models.CustomerProvidedKey
import com.azure.storage.blob.models.PageRange
import com.azure.storage.blob.sas.BlobSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import com.azure.storage.blob.specialized.AppendBlobClient
import com.azure.storage.blob.specialized.BlobClientBase
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.blob.specialized.PageBlobClient
import com.azure.storage.common.implementation.Constants

import java.time.OffsetDateTime

class CPKTest extends APISpec {

    CustomerProvidedKey key
    BlobContainerClient cpkContainer
    BlockBlobClient cpkBlockBlob
    PageBlobClient cpkPageBlob
    AppendBlobClient cpkAppendBlob
    BlobClientBase cpkExistingBlob

    def setup() {
        key = new CustomerProvidedKey(getRandomKey())
        def builder = instrument(new BlobContainerClientBuilder()
            .endpoint(cc.getBlobContainerUrl().toString())
            .customerProvidedKey(key)
            .credential(env.primaryAccount.credential))

        cpkContainer = builder.buildClient()
        cpkBlockBlob = cpkContainer.getBlobClient(generateBlobName()).getBlockBlobClient()
        cpkPageBlob = cpkContainer.getBlobClient(generateBlobName()).getPageBlobClient()
        cpkAppendBlob = cpkContainer.getBlobClient(generateBlobName()).getAppendBlobClient()

        def existingBlobSetup = cpkContainer.getBlobClient(generateBlobName()).getBlockBlobClient()
        existingBlobSetup.upload(data.defaultInputStream, data.defaultDataSize)
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
        def response = cpkBlockBlob.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null,
            null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionKeySha256() == key.getKeySha256()
    }

    def "Get blob with CPK"() {
        setup:
        cpkBlockBlob.upload(data.defaultInputStream, data.defaultDataSize)
        def datastream = new ByteArrayOutputStream()

        when:
        def response = cpkBlockBlob.downloadWithResponse(datastream, null, null, null, false, null, null)

        then:
        response.getStatusCode() == 200
        datastream.toByteArray() == data.defaultBytes
    }

    def "Put block with CPK"() {
        when:
        def response = cpkBlockBlob.stageBlockWithResponse(getBlockID(), data.defaultInputStream, data.defaultDataSize,
            null, null, null, null)

        then:
        response.getStatusCode() == 201
        Boolean.parseBoolean(response.getHeaders().getValue(Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED))
    }

    def "Put block from URL with CPK"() {
        setup:
        def blobName = generateBlobName()
        def sourceBlob = cc.getBlobClient(blobName).getBlockBlobClient()
        sourceBlob.upload(data.defaultInputStream, data.defaultDataSize)

        when:
        def sas = new BlobServiceSasSignatureValues()
            .setExpiryTime(OffsetDateTime.now().plusHours(1))
            .setPermissions(new BlobSasPermission().setReadPermission(true))
            .setContainerName(cc.getBlobContainerName())
            .setBlobName(blobName)
            .generateSasQueryParameters(env.primaryAccount.credential)
            .encode()

        def response = cpkBlockBlob.stageBlockFromUrlWithResponse(getBlockID(), sourceBlob.getBlobUrl().toString() + "?" + sas,
            null, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        Boolean.parseBoolean(response.getHeaders().getValue(Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED))
    }

    def "Put block list with CPK"() {
        setup:
        def blockIDList = [getBlockID(), getBlockID()]
        for (def blockId in blockIDList) {
            cpkBlockBlob.stageBlock(blockId, data.defaultInputStream, data.defaultDataSize)
        }

        when:
        def response = cpkBlockBlob.commitBlockListWithResponse(blockIDList, null, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionKeySha256() == key.getKeySha256()
    }

    def "Put page with CPK"() {
        setup:
        cpkPageBlob.create(PageBlobClient.PAGE_BYTES)

        when:
        def response = cpkPageBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionKeySha256() == key.getKeySha256()
    }

    def "Put page from URL wih CPK"() {
        setup:
        def blobName = generateBlobName()
        def sourceBlob = cc.getBlobClient(blobName).getPageBlobClient()
        sourceBlob.create(PageBlobClient.PAGE_BYTES)
        sourceBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null)

        cpkPageBlob.create(PageBlobClient.PAGE_BYTES)

        when:
        def sas = new BlobServiceSasSignatureValues()
            .setExpiryTime(OffsetDateTime.now().plusHours(1))
            .setPermissions(new BlobSasPermission().setReadPermission(true))
            .setContainerName(cc.getBlobContainerName())
            .setBlobName(blobName)
            .generateSasQueryParameters(env.primaryAccount.credential)
            .encode()

        def response = cpkPageBlob.uploadPagesFromUrlWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            sourceBlob.getBlobUrl().toString() + "?" + sas, null, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        //TODO uncomment when swagger is fixed so PageBlobUploadPagesFromURLHeaders contains the encryption SHA
        //response.getValue().setEncryptionKeySha256() == key.getKeySHA256()
    }

    def "Put multiple pages with CPK"() {
        setup:
        cpkPageBlob.create(PageBlobClient.PAGE_BYTES * 2)

        when:
        def response = cpkPageBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES * 2)), null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionKeySha256() == key.getKeySha256()
    }

    def "Append block with CPK"() {
        setup:
        cpkAppendBlob.create()

        when:
        def response = cpkAppendBlob.appendBlockWithResponse(data.defaultInputStream, data.defaultDataSize, null, null,
            null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionKeySha256() == key.getKeySha256()
    }

    def "Append block from URL with CPK"() {
        setup:
        cpkAppendBlob.create()
        def blobName = generateBlobName()
        def sourceBlob = cc.getBlobClient(blobName).getBlockBlobClient()
        sourceBlob.upload(data.defaultInputStream, data.defaultDataSize)

        when:
        def sas = new BlobServiceSasSignatureValues()
            .setExpiryTime(OffsetDateTime.now().plusHours(1))
            .setPermissions(new BlobSasPermission().setReadPermission(true))
            .setContainerName(cc.getBlobContainerName())
            .setBlobName(blobName)
            .generateSasQueryParameters(env.primaryAccount.credential)
            .encode()
        def response = cpkAppendBlob.appendBlockFromUrlWithResponse(sourceBlob.getBlobUrl().toString() + "?" + sas,
            null, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        //TODO uncomment when swagger is fixed so AppendBlobAppendBLockFromURLHeaders contains isrequestserverencrypted
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionKeySha256() == key.getKeySha256()
    }

    def "Set blob metadata with CPK"() {
        setup:
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")

        when:
        def response = cpkExistingBlob.setMetadataWithResponse(metadata, null, null, null)

        then:
        response.getStatusCode() == 200
        Boolean.parseBoolean(response.getHeaders().getValue(Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED))
        response.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256) == key.getKeySha256()
    }

    def "Get blob properties and metadata with CPK"() {
        when:
        def response = cpkExistingBlob.getPropertiesWithResponse(null, null, null)

        then:
        response.getStatusCode() == 200
        Boolean.parseBoolean(response.getHeaders().getValue(Constants.HeaderConstants.SERVER_ENCRYPTED))
        response.getHeaders().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256) == key.getKeySha256()
    }

//    TODO unignore when swagger is resolved with service team
//    def "Set blob tier with CPK"() {
//        when:
//        def response = cpkExistingBlob.setTierWithResponse(AccessTier.COOL, null, null, null)
//
//        then:
//        response.getStatusCode() == 200
//        Boolean.parseBoolean(response.headers().getValue(Constants.HeaderConstants.SERVER_ENCRYPTED))
//        response.headers().getValue(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256) == key.getKeySHA256()
//    }

    def "Snapshot blob with CPK"() {
        setup:
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")

        when:
        def response = cpkExistingBlob.createSnapshotWithResponse(null, null, null, null)

        then:
        response.getStatusCode() == 201
    }

    //TODO add tests for copy blob CPK tests once generated code supports it

    def "getCustomerProvidedKeyClient"() {
        setup:
        def newCpk = new CustomerProvidedKey(getRandomKey())

        when: "AppendBlob"
        def newCpkAppendBlob = cpkAppendBlob.getCustomerProvidedKeyClient(newCpk)

        then:
        newCpkAppendBlob instanceof AppendBlobClient
        newCpkAppendBlob.getCustomerProvidedKey() != cpkAppendBlob.getCustomerProvidedKey()

        when: "BlockBlob"
        def newCpkBlockBlob = cpkBlockBlob.getCustomerProvidedKeyClient(newCpk)

        then:
        newCpkBlockBlob instanceof BlockBlobClient
        newCpkBlockBlob.getCustomerProvidedKey() != cpkBlockBlob.getCustomerProvidedKey()

        when: "PageBlob"
        def newCpkPageBlob = cpkPageBlob.getCustomerProvidedKeyClient(newCpk)

        then:
        newCpkPageBlob instanceof PageBlobClient
        newCpkPageBlob.getCustomerProvidedKey() != cpkPageBlob.getCustomerProvidedKey()

        when: "BlobClientBase"
        def newCpkBlobClientBase = cpkExistingBlob.getCustomerProvidedKeyClient(newCpk)

        then:
        newCpkBlobClientBase instanceof BlobClientBase
        newCpkBlobClientBase.getCustomerProvidedKey() != cpkExistingBlob.getCustomerProvidedKey()

        when: "BlobClient"
        def cpkBlobClient = cpkContainer.getBlobClient(generateBlobName()) // Inherits container's CPK
        def newCpkBlobClient = cpkBlobClient.getCustomerProvidedKeyClient(newCpk)

        then:
        newCpkBlobClient instanceof BlobClient
        newCpkBlobClient.getCustomerProvidedKey() != cpkBlobClient.getCustomerProvidedKey()
    }

    def "Exists without CPK"() {
        setup:
        def clientWithoutCpk = cpkExistingBlob.getCustomerProvidedKeyClient(null)

        expect:
        clientWithoutCpk.exists()
    }
}
