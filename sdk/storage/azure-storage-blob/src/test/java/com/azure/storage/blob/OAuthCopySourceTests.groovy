package com.azure.storage.blob

import com.azure.core.experimental.http.HttpAuthorization
import com.azure.core.util.BinaryData
import com.azure.core.util.Context
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.PageRange
import com.azure.storage.blob.options.AppendBlobAppendBlockFromUrlOptions
import com.azure.storage.blob.options.BlobUploadFromUrlOptions
import com.azure.storage.blob.options.BlockBlobStageBlockFromUrlOptions
import com.azure.storage.blob.options.PageBlobUploadPagesFromUrlOptions
import com.azure.storage.blob.specialized.AppendBlobClient
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.blob.specialized.PageBlobClient
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion

@RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_10_02")
class OAuthCopySourceTests extends APISpec {
    BlobClient defaultDataSourceBlobClient
    BlobClient pageBlobDataSourceBlobClient

    AppendBlobClient appendBlobClient
    BlockBlobClient blockBlobClient
    PageBlobClient pageBlobClient

    def setup() {
        defaultDataSourceBlobClient = cc.getBlobClient(generateBlobName())
        defaultDataSourceBlobClient.upload(data.defaultBinaryData)
        pageBlobDataSourceBlobClient = cc.getBlobClient(generateBlobName())
        pageBlobDataSourceBlobClient.upload(BinaryData.fromBytes(getRandomByteArray(PageBlobClient.PAGE_BYTES)))

        appendBlobClient = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        appendBlobClient.create()

        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        pageBlobClient = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        pageBlobClient.create(PageBlobClient.PAGE_BYTES)
    }

    def "Append blob append block from URL source oauth"() {
        setup:
        def sourceBlob = cc.getBlobClient(generateBlobName())
        sourceBlob.upload(data.defaultBinaryData)
        def oauthHeader = getAuthToken()

        when:
        appendBlobClient.appendBlockFromUrlWithResponse(
            new AppendBlobAppendBlockFromUrlOptions(sourceBlob.getBlobUrl())
                .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
            null, Context.NONE)

        then:
        appendBlobClient.downloadContent().toBytes() == data.defaultBytes
    }

    def "Append blob append block from URL source oauth fail"() {
        setup:
        def sourceBlob = cc.getBlobClient(generateBlobName())
        sourceBlob.upload(data.defaultBinaryData)
        def oauthHeader = "garbage"

        when:
        appendBlobClient.appendBlockFromUrlWithResponse(
            new AppendBlobAppendBlockFromUrlOptions(sourceBlob.getBlobUrl())
                .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
            null, Context.NONE)

        then:
        thrown(BlobStorageException)
    }

    def "Block blob upload from URL source oauth"() {
        setup:
        def oauthHeader = getAuthToken()

        when:
        blockBlobClient.uploadFromUrlWithResponse(
            new BlobUploadFromUrlOptions(defaultDataSourceBlobClient.getBlobUrl())
                .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
            null, Context.NONE)

        then:
        blockBlobClient.downloadContent().toBytes() == data.defaultBytes
    }

    def "Block blob upload from URL source oauth fail"() {
        setup:
        def sourceBlob = cc.getBlobClient(generateBlobName())
        sourceBlob.upload(data.defaultBinaryData)
        def oauthHeader = "garbage"

        when:
        blockBlobClient.uploadFromUrlWithResponse(
            new BlobUploadFromUrlOptions(sourceBlob.getBlobUrl())
                .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
            null, Context.NONE)

        then:
        thrown(BlobStorageException)
    }

    def "Block blob stage block from URL source oauth"() {
        setup:
        def oauthHeader = getAuthToken()
        def blockId = Base64.getEncoder().encodeToString("myBlockId".getBytes())

        when:
        blockBlobClient.stageBlockFromUrlWithResponse(
            new BlockBlobStageBlockFromUrlOptions(blockId, defaultDataSourceBlobClient.getBlobUrl())
                .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
            null, Context.NONE)
        blockBlobClient.commitBlockList([blockId], true)

        then:
        blockBlobClient.downloadContent().toBytes() == data.defaultBytes
    }

    def "Block blob stage block from URL source oauth fail"() {
        setup:
        def sourceBlob = cc.getBlobClient(generateBlobName())
        sourceBlob.upload(data.defaultBinaryData)
        def oauthHeader = "garbage"
        def blockId = Base64.getEncoder().encodeToString("myBlockId".getBytes())

        when:
        blockBlobClient.stageBlockFromUrlWithResponse(
            new BlockBlobStageBlockFromUrlOptions(blockId, sourceBlob.getBlobUrl())
                .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
            null, Context.NONE)

        then:
        thrown(BlobStorageException)
    }

    def "Upload pages from URL source oauth"() {
        setup:
        def pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)
        def oauthHeader = getAuthToken()

        when:
        pageBlobClient.uploadPagesFromUrlWithResponse(
            new PageBlobUploadPagesFromUrlOptions(pageRange, pageBlobDataSourceBlobClient.getBlobUrl())
                .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
            null, Context.NONE)

        then:
        pageBlobClient.downloadContent().toBytes() == pageBlobDataSourceBlobClient.downloadContent().toBytes()
    }

    def "Upload pages from URL source oauth fail"() {
        setup:
        def pageRange = new PageRange().setStart(0).setEnd(PageBlobClient.PAGE_BYTES - 1)
        def oauthHeader = "garbage"

        when:
        pageBlobClient.uploadPagesFromUrlWithResponse(
            new PageBlobUploadPagesFromUrlOptions(pageRange, pageBlobDataSourceBlobClient.getBlobUrl())
                .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
            null, Context.NONE)

        then:
        thrown(BlobStorageException)
    }
}
