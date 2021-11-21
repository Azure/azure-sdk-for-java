package com.azure.storage.file.share


import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.sas.SasProtocol
import com.azure.storage.file.share.models.ShareAccessPolicy
import com.azure.storage.file.share.models.ShareSignedIdentifier
import com.azure.storage.file.share.models.ShareStorageException
import com.azure.storage.file.share.sas.ShareFileSasPermission
import com.azure.storage.file.share.sas.ShareSasPermission
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues

import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime

class FileSasClientTests extends APISpec {

    private ShareFileClient primaryFileClient
    private ShareClient primaryShareClient
    private ShareServiceClient primaryFileServiceClient
    private String shareName

    private String filePath = "filename"
    private String data

    def setup() {
        shareName = namer.getRandomName(60)

        primaryFileServiceClient = fileServiceBuilderHelper().buildClient()
        primaryShareClient = shareBuilderHelper(shareName).buildClient()
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient()

        data = "test"
        primaryShareClient.create()
        primaryFileClient.create(Constants.KB)
    }

    ShareServiceSasSignatureValues generateValues(ShareFileSasPermission permission) {
        return new ShareServiceSasSignatureValues(namer.getUtcNow().plusDays(1), permission)
            .setStartTime(namer.getUtcNow().minusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
            .setCacheControl("cache")
            .setContentDisposition("disposition")
            .setContentEncoding("encoding")
            .setContentLanguage("language")
            .setContentType("type")
    }

    def "FileSAS network test download upload"() {
        setup:
        primaryFileClient.upload(getInputStream(data.getBytes()), (long) data.length())
        def permissions = new ShareFileSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def sasValues = generateValues(permissions)

        when:
        def sas = primaryFileClient.generateSas(sasValues)
        def client = fileBuilderHelper(shareName, filePath)
            .endpoint(primaryFileClient.getFileUrl())
            .sasToken(sas)
            .buildFileClient()

        def stream = new ByteArrayOutputStream()
        client.download(stream)

        client.upload(getInputStream(data.getBytes(StandardCharsets.UTF_8)), (long) data.length())

        then:
        notThrown(ShareStorageException)
        Arrays.copyOfRange(stream.toByteArray(), 0, data.length()) == data.getBytes(StandardCharsets.UTF_8)
    }

    def "FileSAS network test upload fails"() {
        setup:
        def permissions = new ShareFileSasPermission()
            .setReadPermission(true)
            .setWritePermission(false)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def sasValues = generateValues(permissions)

        when:
        def sas = primaryFileClient.generateSas(sasValues)

        def client = fileBuilderHelper(shareName, filePath)
            .endpoint(primaryFileClient.getFileUrl())
            .sasToken(sas)
            .buildFileClient()

        client.upload(getInputStream(data.getBytes()), (long) data.length())

        then:
        thrown(ShareStorageException)

        when:
        client.delete()

        then:
        notThrown(ShareStorageException)
    }

    def "ShareSAS network identifier permissions"() {
        setup:
        ShareSignedIdentifier identifier = new ShareSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new ShareAccessPolicy().setPermissions("rcwdl")
                .setExpiresOn(namer.getUtcNow().plusDays(1)))

        primaryShareClient.setAccessPolicy(Arrays.asList(identifier))

        // Sleep 30 seconds if running against the live service as it may take ACLs that long to take effect.
        sleepIfLive(30000)

        // Check shareSASPermissions
        ShareSasPermission permissions = new ShareSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setListPermission(true)

        OffsetDateTime expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def sasValues = new ShareServiceSasSignatureValues(identifier.getId())
        def sasWithId = primaryShareClient.generateSas(sasValues)

        ShareClient client1 = shareBuilderHelper(primaryShareClient.getShareName())
            .endpoint(primaryShareClient.getShareUrl())
            .sasToken(sasWithId)
            .buildClient()

        client1.createDirectory("dir")
        client1.deleteDirectory("dir")

        sasValues = new ShareServiceSasSignatureValues(expiryTime, permissions)
        def sasWithPermissions = primaryShareClient.generateSas(sasValues)
        def client2 = shareBuilderHelper(primaryShareClient.getShareName())
            .endpoint(primaryFileClient.getFileUrl())
            .sasToken(sasWithPermissions)
            .buildClient()

        client2.createDirectory("dir")
        client2.deleteDirectory("dir")

        then:
        notThrown(ShareStorageException)
    }

    def "AccountSAS network create delete share"() {
        setup:
        def service = new AccountSasService()
            .setFileAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryFileServiceClient.generateAccountSas(sasValues)
        def scBuilder = fileServiceBuilderHelper()
        scBuilder.endpoint(primaryFileServiceClient.getFileServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()
        sc.createShare("create")
        sc.deleteShare("create")

        then:
        notThrown(ShareStorageException)
    }

    /**
     * If this test fails it means that non-deprecated string to sign has new components.
     * In that case we should hardcode version used for deprecated string to sign like we did for blobs.
     */
    def "Remember about string to sign deprecation"() {
        setup:
        def client = shareBuilderHelper(shareName).credential(environment.primaryAccount.credential).buildClient()
        def values = new ShareServiceSasSignatureValues(namer.getUtcNow(), new ShareSasPermission())
        values.setShareName(client.getShareName())

        when:
        def deprecatedStringToSign = values.generateSasQueryParameters(environment.primaryAccount.credential).encode()
        def stringToSign = client.generateSas(values)

        then:
        deprecatedStringToSign == stringToSign
    }
}
