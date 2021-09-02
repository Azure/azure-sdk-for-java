package com.azure.storage.file.share

import com.azure.core.http.HttpAuthorization
import com.azure.core.util.Context
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.azure.storage.file.share.models.ShareStorageException
import com.azure.storage.file.share.options.ShareFileUploadRangeFromUrlOptions

@RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_10_02")
class OAuthCopySourceTests extends APISpec {
    ShareFileClient primaryFileClient
    ShareClient shareClient
    String shareName
    String filePath

    BlobContainerClient container
    BlobClient blob

    def setup() {
        shareName = namer.getRandomName(60)
        filePath = namer.getRandomName(60)
        shareClient = shareBuilderHelper(shareName).buildClient()
        shareClient.create()
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient()

        container = getBlobContainer()
        blob = container.getBlobClient(generatePathName())
        blob.upload(data.defaultBinaryData)
    }

    def cleanup() {
        container.delete()
    }

    def getBlobContainer() {
        instrument(new BlobServiceClientBuilder())
            .endpoint(env.primaryAccount.blobEndpoint)
            .credential(env.primaryAccount.credential)
            .buildClient()
            .createBlobContainer(getShareName())
    }

    def "Copy from URL with oauth source"() {
        given:
        def oauthHeader = getAuthToken()
        primaryFileClient.create(data.defaultDataSize)

        when:
        primaryFileClient.uploadRangeFromUrlWithResponse(
            new ShareFileUploadRangeFromUrlOptions(data.defaultDataSize, blob.getBlobUrl())
                .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)),
            null, Context.NONE)

        then:
        def os = new ByteArrayOutputStream(data.defaultDataSize)
        primaryFileClient.download(os)
        os.toByteArray() == data.defaultBytes
    }

    def "Copy from URL with oauth source invalid credential"() {
        given:
        def oauthHeader = "garbage"
        primaryFileClient.create(data.defaultDataSize)

        when:
        primaryFileClient.uploadRangeFromUrlWithResponse(
            new ShareFileUploadRangeFromUrlOptions(data.defaultDataSize, blob.getBlobUrl())
                .setSourceAuthorization(new HttpAuthorization("Bearer", oauthHeader)), null, Context.NONE)

        then:
        thrown(ShareStorageException)
    }
}
