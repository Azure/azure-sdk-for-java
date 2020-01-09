package com.azure.storage.blob

import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.test.TestMode
import com.azure.storage.blob.models.BlobContainerEncryptionScope
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.EncryptionScope
import com.azure.storage.blob.specialized.PageBlobClient

class BlobServiceVersionPolicyTest extends APISpec {

    BlobClient bc
    BlobContainerClient cc
    EncryptionScope es
    BlobContainerEncryptionScope ces

    BlobContainerClientBuilder containerBuilder

    def setup() {
        es = new EncryptionScope().setEncryptionScope("testscope1")

        containerBuilder = new BlobContainerClientBuilder()
            .endpoint(cc.getBlobContainerUrl().toString())
            .serviceVersion(BlobServiceVersion.V2019_02_02)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .credential(primaryCredential)

        if (testMode == TestMode.RECORD) {
            containerBuilder.addPolicy(interceptorManager.getRecordPolicy())
        }
    }

    def "Default encryption scope"() {
        when:
        ces = new BlobContainerEncryptionScope()
            .setDefaultEncryptionScope("testscope2")
            .setDenyEncryptionScopeOverride(true)
        BlobContainerClient cpkncesContainer = containerBuilder.blobContainerEncryptionScope(ces).encryptionScope(null)
            .containerName(generateContainerName()).buildClient()
        cpkncesContainer.createWithResponse(null, null, null, null)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage() == "x-ms-default-encryption-scope is not supported for create container in service version 2019-02-02"
    }

    def "Deny encryption scope override"() {
        setup:
        ces = new BlobContainerEncryptionScope()
            .setDefaultEncryptionScope(null)
            .setDenyEncryptionScopeOverride(true)
        BlobContainerClient cpkncesContainer = containerBuilder.blobContainerEncryptionScope(ces)
            .containerName(generateContainerName()).buildClient()

        when:
        cpkncesContainer.createWithResponse(null, null, null, null)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage() == "x-ms-deny-encryption-scope-override is not supported for create container in service version 2019-02-02"
    }

    def "Encryption scope"() {
        setup:
        BlobContainerClient cpknContainer = containerBuilder.encryptionScope(es)
            .containerName(generateContainerName()).buildClient()
        cpknContainer.create()

        def cpknBlockBlob = cpknContainer.getBlobClient(generateBlobName()).getBlockBlobClient()
        def cpknPageBlob = cpknContainer.getBlobClient(generateBlobName()).getPageBlobClient()
        def cpknAppendBlob = cpknContainer.getBlobClient(generateBlobName()).getAppendBlobClient()

        when:
        cpknAppendBlob.create()

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage() == "x-ms-encryption-scope is not supported for any blob API in service version 2019-02-02"

        when:
        cpknPageBlob.create(PageBlobClient.PAGE_BYTES)

        then:
        e = thrown(IllegalArgumentException)
        e.getMessage() == "x-ms-encryption-scope is not supported for any blob API in service version 2019-02-02"

        when:
        cpknBlockBlob.upload(defaultInputStream.get(), defaultDataSize)

        then:
        e = thrown(IllegalArgumentException)
        e.getMessage() == "x-ms-encryption-scope is not supported for any blob API in service version 2019-02-02"
    }

    def "Get page ranges diff prev snapshot url"() {
        setup:
        containerBuilder.endpoint(managedDiskServiceClient.getAccountUrl().toString())
            .containerName(generateContainerName())
            .credential(managedDiskCredential)
        BlobContainerClient managedDiskContainer = containerBuilder.buildClient()
        managedDiskContainer.create()

        PageBlobClient managedDiskBlob = managedDiskContainer.getBlobClient(generateBlobName()).getPageBlobClient()
        managedDiskBlob.create(PageBlobClient.PAGE_BYTES * 2)

        def snapUrl = managedDiskBlob.createSnapshot().getBlobUrl()

        when:
        managedDiskBlob.getManagedDiskRangesDiffWithResponse(new BlobRange(0, PageBlobClient.PAGE_BYTES * 2), snapUrl, null, null, null)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage() == "x-ms-previous-snapshot-url is not supported for any blob API in service version 2019-02-02"
    }

}
