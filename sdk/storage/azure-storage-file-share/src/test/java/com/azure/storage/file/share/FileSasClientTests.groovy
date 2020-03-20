package com.azure.storage.file.share

import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.sas.SasIpRange
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
        shareName = testResourceName.randomName(methodName, 60)

        primaryFileServiceClient = fileServiceBuilderHelper(interceptorManager).buildClient()
        primaryShareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        primaryFileClient = fileBuilderHelper(interceptorManager, shareName, filePath).buildFileClient()

        data = "test"
        primaryShareClient.create()
        primaryFileClient.create(Constants.KB)
    }

    ShareServiceSasSignatureValues generateValues(ShareFileSasPermission permission) {
        return new ShareServiceSasSignatureValues(getUTCNow().plusDays(1), permission)
            .setStartTime(getUTCNow().minusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
            .setSasIpRange(new SasIpRange()
                .setIpMin("0.0.0.0")
                .setIpMax("255.255.255.255"))
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
        def client = fileBuilderHelper(interceptorManager, shareName, filePath)
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

        def client = fileBuilderHelper(interceptorManager, shareName, filePath)
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
                .setExpiresOn(getUTCNow().plusDays(1)))
        primaryShareClient.setAccessPolicy(Arrays.asList(identifier))

        // Check shareSASPermissions
        ShareSasPermission permissions = new ShareSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setListPermission(true)

        OffsetDateTime expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new ShareServiceSasSignatureValues(identifier.getId())
        def sasWithId = primaryShareClient.generateSas(sasValues)

        ShareClient client1 = shareBuilderHelper(interceptorManager, primaryShareClient.getShareName())
            .endpoint(primaryShareClient.getShareUrl())
            .sasToken(sasWithId)
            .buildClient()

        client1.createDirectory("dir")
        client1.deleteDirectory("dir")

        sasValues = new ShareServiceSasSignatureValues(expiryTime, permissions)
        def sasWithPermissions = primaryShareClient.generateSas(sasValues)
        def client2 = shareBuilderHelper(interceptorManager, primaryShareClient.getShareName())
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
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryFileServiceClient.generateAccountSas(sasValues)
        def scBuilder = fileServiceBuilderHelper(interceptorManager)
        scBuilder.endpoint(primaryFileServiceClient.getFileServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()
        sc.createShare("create")
        sc.deleteShare("create")

        then:
        notThrown(ShareStorageException)
    }
}
