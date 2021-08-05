package com.azure.storage.file.share

import com.azure.core.credential.AzureSasCredential
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.TestMode
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
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime

class FileSASTests extends APISpec {

    private ShareFileClient primaryFileClient
    private ShareClient primaryShareClient
    private ShareServiceClient primaryFileServiceClient
    private String shareName

    private String filePath = "filename"

    def setup() {
        shareName = namer.getRandomName(60)

        primaryFileServiceClient = fileServiceBuilderHelper().buildClient()
        primaryShareClient = shareBuilderHelper(shareName).buildClient()
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient()
    }

    @Unroll
    def "FileSASPermissions toString"() {
        setup:
        def perms = new ShareFileSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | create || expectedString
        true  | false | false  | false  || "r"
        false | true  | false  | false  || "w"
        false | false | true   | false  || "d"
        false | false | false  | true   || "c"
        true  | true  | true   | true   || "rcwd"
    }

    @Unroll
    def "FileSASPermissions parse"() {
        when:
        def perms = ShareFileSasPermission.parse(permString)

        then:
        perms.hasReadPermission() == read
        perms.hasWritePermission() == write
        perms.hasDeletePermission() == delete
        perms.hasCreatePermission() == create

        where:
        permString || read  | write | delete | create
        "r"        || true  | false | false  | false
        "w"        || false | true  | false  | false
        "d"        || false | false | true   | false
        "c"        || false | false | false  | true
        "rcwd"     || true  | true  | true   | true
        "dcwr"     || true  | true  | true   | true
    }

    def "FileSASPermissions parse IA"() {
        when:
        ShareFileSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "ShareSASPermissions toString"() {
        setup:
        def perms = new ShareSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setListPermission(list)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | create | list  || expectedString
        true  | false | false  | false  | false || "r"
        false | true  | false  | false  | false || "w"
        false | false | true   | false  | false || "d"
        false | false | false  | true   | false || "c"
        false | false | false  | false  | true  || "l"
        true  | true  | true   | true   | true  || "rcwdl"
    }

    @Unroll
    def "ShareSASPermissions parse"() {
        when:
        def perms = ShareSasPermission.parse(permString)

        then:
        perms.hasReadPermission() == read
        perms.hasWritePermission() == write
        perms.hasDeletePermission() == delete
        perms.hasCreatePermission() == create
        perms.hasListPermission() == list

        where:
        permString || read  | write | delete | create | list
        "r"        || true  | false | false  | false  | false
        "w"        || false | true  | false  | false  | false
        "d"        || false | false | true   | false  | false
        "c"        || false | false | false  | true   | false
        "l"        || false | false | false  | false  | true
        "rcwdl"    || true  | true  | true   | true   | true
        "dcwrl"    || true  | true  | true   | true   | true
    }

    def "ShareSASPermissions parse IA"() {
        when:
        ShareSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    def "FileSAS network test download upload"() {
        setup:
        String data = "test"
        primaryShareClient.create()
        primaryFileClient.create(Constants.KB)
        primaryFileClient.upload(getInputStream(data.getBytes()), (long) data.length())

        def permissions = new ShareFileSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def startTime = namer.getUtcNow().minusDays(1)
        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasProtocol = SasProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString)
        def sas = new ShareServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
            .setShareName(primaryFileClient.getShareName())
            .setFilePath(primaryFileClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode()

        then:
        sas != null

        when:
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
        String data = "test"
        primaryShareClient.create()
        primaryFileClient.create(Constants.KB)

        def permissions = new ShareFileSasPermission()
            .setReadPermission(true)
            .setWritePermission(false)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def startTime = namer.getUtcNow().minusDays(1)
        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasProtocol = SasProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString)
        def sas = new ShareServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
            .setShareName(primaryFileClient.getShareName())
            .setFilePath(primaryFileClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode()

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
        primaryShareClient.create()
        primaryShareClient.setAccessPolicy(Arrays.asList(identifier))

        // Check containerSASPermissions
        ShareSasPermission permissions = new ShareSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setListPermission(true)

        OffsetDateTime expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString)
        def sasWithId = new ShareServiceSasSignatureValues()
            .setIdentifier(identifier.getId())
            .setShareName(primaryShareClient.getShareName())
            .generateSasQueryParameters(credential)
            .encode()

        ShareClient client1 = shareBuilderHelper(primaryShareClient.getShareName())
            .endpoint(primaryShareClient.getShareUrl())
            .sasToken(sasWithId)
            .buildClient()

        def dirName = namer.getRandomName(60)
        client1.createDirectory(dirName)
        client1.deleteDirectory(dirName)

        def sasWithPermissions = new ShareServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setShareName(primaryFileClient.getShareName())
            .generateSasQueryParameters(credential)
            .encode()

        def client2 = shareBuilderHelper(primaryShareClient.getShareName())
            .endpoint(primaryFileClient.getFileUrl())
            .sasToken(sasWithPermissions)
            .buildClient()

        def dirName2 = namer.getRandomName(60)
        client2.createDirectory(dirName2)
        client2.deleteDirectory(dirName2)

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
        def credential = StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString)
        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(credential)
            .encode()

        then:
        sas != null

        when:
        def scBuilder = fileServiceBuilderHelper()
        scBuilder.endpoint(primaryFileServiceClient.getFileServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()
        def shareName = namer.getRandomName(60)
        sc.createShare(shareName)
        sc.deleteShare(shareName)

        then:
        notThrown(ShareStorageException)
    }

    def "accountSAS network account sas token on endpoint"() {
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
        def expiryTime = namer.getUtcNow().plusDays(1)

        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(env.primaryAccount.credential)
            .encode()
        def shareName = namer.getRandomName(60)
        def pathName = namer.getRandomName(60)

        when:
        def sc = getServiceClientBuilder(null, primaryFileServiceClient.getFileServiceUrl() + "?" + sas, null).buildClient()
        sc.createShare(shareName)

        def sharec = getShareClientBuilder(primaryFileServiceClient.getFileServiceUrl() + "/" + shareName + "?" + sas).buildClient()
        sharec.createFile(pathName, 1024)

        def fc = getFileClient(null, primaryFileServiceClient.getFileServiceUrl() + "/" + shareName + "/" + pathName + "?" + sas)
        fc.download(new ByteArrayOutputStream())

        then:
        notThrown(Exception)
    }

    def "Parse protocol"() {
        setup:
        primaryShareClient.create()
        primaryFileClient.create(100)
        def sas = primaryFileServiceClient.generateAccountSas(new AccountSasSignatureValues(
            namer.getUtcNow().plusDays(1),
            AccountSasPermission.parse("r"), new AccountSasService().setFileAccess(true),
            new AccountSasResourceType().setService(true).setContainer(true).setObject(true))
            .setProtocol(SasProtocol.HTTPS_HTTP))

        when:
        def sasClient = instrument(new ShareFileClientBuilder()
            .endpoint(primaryFileClient.getFileUrl() + "?" + sas))
            .buildFileClient()

        and:
        sasClient.getProperties()

        then:
        notThrown(ShareStorageException)


        when:
        def sasShareClient = instrument(new ShareClientBuilder()
            .endpoint(primaryShareClient.getShareUrl() + "?" + sas))
            .buildClient()

        and:
        sasShareClient.getProperties()

        then:
        notThrown(ShareStorageException)


        when:
        def sasServiceClient = instrument(new ShareServiceClientBuilder()
            .endpoint(primaryFileServiceClient.getFileServiceUrl() + "?" + sas))
            .buildClient()

        and:
        sasServiceClient.getProperties()

        then:
        notThrown(ShareStorageException)
    }

    def "can use sas to authenticate"() {
        setup:
        def service = new AccountSasService()
            .setFileAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryFileServiceClient.generateAccountSas(sasValues)
        def pathName = generatePathName()
        primaryShareClient.create()
        primaryShareClient.createDirectory(pathName)

        when:
        instrument(new ShareClientBuilder()
            .endpoint(primaryShareClient.getShareUrl())
            .sasToken(sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new ShareClientBuilder()
            .endpoint(primaryShareClient.getShareUrl())
            .credential(new AzureSasCredential(sas)))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new ShareClientBuilder()
            .endpoint(primaryShareClient.getShareUrl() + "?" + sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new ShareFileClientBuilder()
            .endpoint(primaryShareClient.getShareUrl())
            .resourcePath(pathName)
            .sasToken(sas))
            .buildDirectoryClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new ShareFileClientBuilder()
            .endpoint(primaryShareClient.getShareUrl())
            .resourcePath(pathName)
            .credential(new AzureSasCredential(sas)))
            .buildDirectoryClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new ShareFileClientBuilder()
            .endpoint(primaryShareClient.getShareUrl() + "?" + sas)
            .resourcePath(pathName))
            .buildDirectoryClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new ShareServiceClientBuilder()
            .endpoint(primaryShareClient.getShareUrl())
            .sasToken(sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new ShareServiceClientBuilder()
            .endpoint(primaryShareClient.getShareUrl())
            .credential(new AzureSasCredential(sas)))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new ShareServiceClientBuilder()
            .endpoint(primaryShareClient.getShareUrl() + "?" + sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()
    }
}
