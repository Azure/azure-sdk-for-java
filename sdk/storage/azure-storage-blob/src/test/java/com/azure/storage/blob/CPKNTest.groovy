// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.storage.blob.models.BlobContainerEncryptionScope
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.CustomerProvidedKey

import com.azure.storage.blob.models.PageRange
import com.azure.storage.blob.sas.BlobSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import com.azure.storage.blob.specialized.AppendBlobClient
import com.azure.storage.blob.specialized.BlobClientBase
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.blob.specialized.PageBlobClient
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder

import java.time.OffsetDateTime

class CPKNTest extends APISpec {

    String scope1 = "testscope1"
    String scope2 = "testscope2"
    String es
    BlobContainerEncryptionScope ces
    BlobContainerClientBuilder builder

    BlobContainerClient cpknContainer
    BlockBlobClient cpknBlockBlob
    PageBlobClient cpknPageBlob
    AppendBlobClient cpknAppendBlob

    def setup() {
        es = scope1
        ces = new BlobContainerEncryptionScope().setDefaultEncryptionScope(scope2).setEncryptionScopeOverridePrevented(true)

        builder = getContainerClientBuilder(cc.getBlobContainerUrl())
            .credential(env.primaryAccount.credential)

        cpknContainer = builder.encryptionScope(es).buildClient()

        cpknBlockBlob = cpknContainer.getBlobClient(generateBlobName()).getBlockBlobClient()
        cpknPageBlob = cpknContainer.getBlobClient(generateBlobName()).getPageBlobClient()
        cpknAppendBlob = cpknContainer.getBlobClient(generateBlobName()).getAppendBlobClient()
    }

    def "Container create"() {
        when:
        BlobContainerClient cpkncesContainer = builder.blobContainerEncryptionScope(ces).encryptionScope(null)
            .containerName(generateContainerName()).buildClient()
        def response = cpkncesContainer.createWithResponse(null, null, null, null)

        then:
        response.getStatusCode() == 201
    }

    def "Container deny encryption scope override"() {
        setup:
        BlobContainerClient cpkncesContainer = builder.blobContainerEncryptionScope(ces)
            .containerName(generateContainerName()).buildClient()
        cpkncesContainer.create()

        cpknAppendBlob = builder.encryptionScope(es)
            .containerName(cpkncesContainer.getBlobContainerName())
            .buildClient()
            .getBlobClient(generateBlobName())
            .getAppendBlobClient()

        when:
        cpknAppendBlob.create()

        then:
        thrown(BlobStorageException)
    }

    def "Container list blobs flat"() {
        setup:
        BlobContainerClient cpkncesContainer = builder
            .blobContainerEncryptionScope(ces)
            .encryptionScope(null)
            .containerName(generateContainerName())
            .buildClient()
        cpkncesContainer.create()
        def cpknAppendBlob = cpkncesContainer.getBlobClient(generateBlobName()).getAppendBlobClient()
        cpknAppendBlob.create()

        when:
        Iterator<BlobItem> items = cpkncesContainer.listBlobs().iterator()

        then:
        def blob = items.next()
        !items.hasNext()
        blob.getProperties().getEncryptionScope() == scope2
    }

    def "Container list blobs hierarchical"() {
        setup:
        BlobContainerClient cpkncesContainer = builder
            .blobContainerEncryptionScope(ces)
            .encryptionScope(null)
            .containerName(generateContainerName())
            .buildClient()
        cpkncesContainer.create()
        def cpknAppendBlob = cpkncesContainer.getBlobClient(generateBlobName()).getAppendBlobClient()
        cpknAppendBlob.create()

        when:
        Iterator<BlobItem> items = cpkncesContainer.listBlobsByHierarchy("").iterator()

        then:
        def blob = items.next()
        !items.hasNext()
        blob.getProperties().getEncryptionScope() == scope2
    }

    def "Append blob create"() {
        when:
        def response = cpknAppendBlob.createWithResponse(null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionScope() == scope1
    }

    def "Append blob append block"() {
        setup:
        cpknAppendBlob.create()

        when:
        def response = cpknAppendBlob.appendBlockWithResponse(data.defaultInputStream, data.defaultDataSize, null, null,
            null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionScope() == scope1
    }

    def "Append blob append block from URL"() {
        setup:
        cpknAppendBlob.create()
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
        def response = cpknAppendBlob.appendBlockFromUrlWithResponse(sourceBlob.getBlobUrl().toString() + "?" + sas,
            null, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionScope() == scope1
    }

    def "Page blob create"() {
        when:
        def response = cpknPageBlob.createWithResponse(1024, null, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionScope() == scope1
    }

    def "Page blob put page"() {
        setup:
        cpknPageBlob.create(PageBlobClient.PAGE_BYTES)

        when:
        def response = cpknPageBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionScope() == scope1
    }

    def "Page blob put page from URL"() {
        setup:
        def blobName = generateBlobName()
        def sourceBlob = cc.getBlobClient(blobName).getPageBlobClient()
        sourceBlob.create(PageBlobClient.PAGE_BYTES)
        sourceBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null)

        cpknPageBlob.create(PageBlobClient.PAGE_BYTES)

        when:
        def sas = new BlobServiceSasSignatureValues()
            .setExpiryTime(OffsetDateTime.now().plusHours(1))
            .setPermissions(new BlobSasPermission().setReadPermission(true))
            .setContainerName(cc.getBlobContainerName())
            .setBlobName(blobName)
            .generateSasQueryParameters(env.primaryAccount.credential)
            .encode()

        def response = cpknPageBlob.uploadPagesFromUrlWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            sourceBlob.getBlobUrl().toString() + "?" + sas, null, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionScope() == scope1
    }

    def "Page blob put multiple pages"() {
        setup:
        cpknPageBlob.create(PageBlobClient.PAGE_BYTES * 2)

        when:
        def response = cpknPageBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES * 2 - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES * 2)), null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionScope() == scope1
    }

    def "Page blob clear page"() {
        setup:
        cpknPageBlob.create(PageBlobClient.PAGE_BYTES * 2)
        cpknPageBlob.uploadPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            new ByteArrayInputStream(getRandomByteArray(PageBlobClient.PAGE_BYTES)), null, null, null, null)

        when:
        def response = cpknPageBlob.clearPagesWithResponse(new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1),
            null, null, null)

        then:
        response.getStatusCode() == 201
    }

    def "Page blob resize"() {
        setup:
        cpknPageBlob.create(PageBlobClient.PAGE_BYTES * 2)
        def response = cpknPageBlob.resizeWithResponse(PageBlobClient.PAGE_BYTES * 2, null, null, null)

        expect:
        response.getStatusCode() == 200
    }

    def "Block blob upload"() {
        setup:
        def response = cpknBlockBlob.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null, null, null,
            null, null)

        expect:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionScope() == scope1
    }

    def "Block blob stage block"() {
        setup:
        cpknBlockBlob.upload(data.defaultInputStream, data.defaultDataSize)
        def response = cpknBlockBlob.stageBlockWithResponse(getBlockID(), data.defaultInputStream, data.defaultDataSize, null, null,
            null, null)
        def headers = response.getHeaders()

        expect:
        response.getStatusCode() == 201
        Boolean.parseBoolean(headers.getValue("x-ms-request-server-encrypted"))
        headers.getValue("x-ms-encryption-scope") == scope1
    }

    def "Block blob commit block list"() {
        setup:
        def blockID = getBlockID()
        cpknBlockBlob.stageBlock(blockID, data.defaultInputStream, data.defaultDataSize)
        def ids = [blockID] as List

        when:
        def response = cpknBlockBlob.commitBlockListWithResponse(ids, null, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        response.getValue().isServerEncrypted()
        response.getValue().getEncryptionScope() == scope1
    }

    def "Service client builder check"() {
        when:
        new BlobServiceClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .buildClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "Container client builder check"() {
        when:
        new BlobContainerClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .buildClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "Blob client builder check"() {
        when:
        new BlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "Append blob client builder check"() {
        when:
        new SpecializedBlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildAppendBlobClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "Block blob client builder check"() {
        when:
        new SpecializedBlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildBlockBlobClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "Page blob client builder check"() {
        when:
        new SpecializedBlobClientBuilder()
            .encryptionScope(es)
            .customerProvidedKey(new CustomerProvidedKey(getRandomKey()))
            .endpoint(cc.getBlobContainerUrl())
            .blobName(generateBlobName())
            .buildPageBlobClient()

        then:
        thrown(IllegalArgumentException)
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

    def "getEncryptionScopeClient"() {
        setup:
        def newEncryptionScope = "newtestscope"

        when: "AppendBlob"
        def newCpknAppendBlob = cpknAppendBlob.getEncryptionScopeClient(newEncryptionScope)

        then:
        newCpknAppendBlob instanceof AppendBlobClient
        newCpknAppendBlob.getEncryptionScope() != cpknAppendBlob.getEncryptionScope()

        when: "BlockBlob"
        def newCpknBlockBlob = cpknBlockBlob.getEncryptionScopeClient(newEncryptionScope)

        then:
        newCpknBlockBlob instanceof BlockBlobClient
        newCpknBlockBlob.getEncryptionScope() != cpknBlockBlob.getEncryptionScope()

        when: "PageBlob"
        def newCpknPageBlob = cpknPageBlob.getEncryptionScopeClient(newEncryptionScope)

        then:
        newCpknPageBlob instanceof PageBlobClient
        newCpknPageBlob.getEncryptionScope() != cpknPageBlob.getEncryptionScope()

        when: "BlobClient"
        def cpkBlobClient = cpknContainer.getBlobClient(generateBlobName()) // Inherits container's CPK
        def newCpknBlobClient = cpkBlobClient.getEncryptionScopeClient(newEncryptionScope)

        then:
        newCpknBlobClient instanceof BlobClient
        newCpknBlobClient.getEncryptionScope() != cpkBlobClient.getEncryptionScope()

        when: "BlobClientBase"
        def newCpknBlobClientBase = ((BlobClientBase) cpkBlobClient).getEncryptionScopeClient(newEncryptionScope)

        then:
        newCpknBlobClientBase instanceof BlobClientBase
        newCpknBlobClientBase.getEncryptionScope() != cpkBlobClient.getEncryptionScope()
    }

}
