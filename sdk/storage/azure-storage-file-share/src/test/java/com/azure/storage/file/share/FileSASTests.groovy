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
        shareName = testResourceName.randomName(methodName, 60)

        primaryFileServiceClient = fileServiceBuilderHelper(interceptorManager).buildClient()
        primaryShareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        primaryFileClient = fileBuilderHelper(interceptorManager, shareName, filePath).buildFileClient()
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
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new SasIpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SasProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(connectionString)
        def sas = new ShareServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setSasIpRange(ipRange)
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
        String data = "test"
        primaryShareClient.create()
        primaryFileClient.create(Constants.KB)

        def permissions = new ShareFileSasPermission()
            .setReadPermission(true)
            .setWritePermission(false)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new SasIpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SasProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(connectionString)
        def sas = new ShareServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setSasIpRange(ipRange)
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
            .setShareName(primaryFileClient.getShareName())
            .setFilePath(primaryFileClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode()

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
        primaryShareClient.create()
        primaryShareClient.setAccessPolicy(Arrays.asList(identifier))

        // Check containerSASPermissions
        ShareSasPermission permissions = new ShareSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setListPermission(true)

        OffsetDateTime expiryTime = getUTCNow().plusDays(1)

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(connectionString)
        def sasWithId = new ShareServiceSasSignatureValues()
            .setIdentifier(identifier.getId())
            .setShareName(primaryShareClient.getShareName())
            .generateSasQueryParameters(credential)
            .encode()

        ShareClient client1 = shareBuilderHelper(interceptorManager, primaryShareClient.getShareName())
            .endpoint(primaryShareClient.getShareUrl())
            .sasToken(sasWithId)
            .buildClient()

        client1.createDirectory("dir")
        client1.deleteDirectory("dir")

        def sasWithPermissions = new ShareServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setShareName(primaryFileClient.getShareName())
            .generateSasQueryParameters(credential)
            .encode()

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
        def credential = StorageSharedKeyCredential.fromConnectionString(connectionString)
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
        def scBuilder = fileServiceBuilderHelper(interceptorManager)
        scBuilder.endpoint(primaryFileServiceClient.getFileServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()
        sc.createShare("create")
        sc.deleteShare("create")

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
        def expiryTime = getUTCNow().plusDays(1)

        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(primaryCredential)
            .encode()
        def shareName = testResourceName.randomName(methodName, 60)
        def pathName = testResourceName.randomName(methodName, 60)

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

}
