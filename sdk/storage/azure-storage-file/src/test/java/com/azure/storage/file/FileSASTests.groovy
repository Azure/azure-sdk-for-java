package com.azure.storage.file


import com.azure.storage.common.AccountSasPermission
import com.azure.storage.common.AccountSasResourceType
import com.azure.storage.common.AccountSasService
import com.azure.storage.common.AccountSasSignatureValues
import com.azure.storage.common.Constants
import com.azure.storage.common.IpRange
import com.azure.storage.common.SasProtocol
import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.file.models.FileAccessPolicy
import com.azure.storage.file.models.FileSignedIdentifier
import com.azure.storage.file.models.FileStorageException
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime

class FileSASTests extends APISpec {

    private FileClient primaryFileClient
    private ShareClient primaryShareClient
    private FileServiceClient primaryFileServiceClient

    private String shareName = "sharename"
    private String filePath = "filename"

    def setup() {
        primaryFileServiceClient = fileServiceBuilderHelper(interceptorManager).buildClient()
        primaryShareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        primaryFileClient = fileBuilderHelper(interceptorManager, shareName, filePath).buildFileClient()
    }

    @Unroll
    def "FileSASPermissions toString"() {
        setup:
        def perms = new FileSasPermission()
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
        def perms = FileSasPermission.parse(permString)

        then:
        perms.getReadPermission() == read
        perms.getWritePermission() == write
        perms.getDeletePermission() == delete
        perms.getCreatePermission() == create

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
        FileSasPermission.parse("rwaq")

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
        perms.getReadPermission() == read
        perms.getWritePermission() == write
        perms.getDeletePermission() == delete
        perms.getCreatePermission() == create
        perms.getListPermission() == list

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

    def "serviceSASSignatureValues canonicalizedResource"() {
        setup:
        def filePath = primaryFileClient.getFilePath()
        def shareName = primaryFileClient.getShareName()
        def accountName = "account"

        when:
        def serviceSASSignatureValues = new FileServiceSasSignatureValues()
            .setCanonicalName(shareName, filePath, accountName)
            .setResource(Constants.UrlConstants.SAS_FILE_CONSTANT)

        then:
        serviceSASSignatureValues.getCanonicalName() == "/file/" + accountName  + "/" + shareName + "/" + filePath
    }

    def "FileSAS network test download upload"() {
        setup:
        String data = "test"
        primaryFileClient.create(Constants.KB)
        primaryFileClient.upload(getInputStream(data.getBytes()), (long) data.length())

        def permissions = new FileSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new IpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SasProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        when:
        def credential = SharedKeyCredential.fromConnectionString(connectionString)
        def sas = new FileServiceSasSignatureValues()
            .setPermissions(permissions.toString())
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setIpRange(ipRange)
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
            .setCanonicalName(primaryFileClient.getShareName(), primaryFileClient.getFilePath(), credential.getAccountName())
            .setResource(Constants.UrlConstants.SAS_FILE_CONSTANT)
            .generateSASQueryParameters(credential)
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
        notThrown(FileStorageException)
        Arrays.copyOfRange(stream.toByteArray(), 0, data.length()) == data.getBytes(StandardCharsets.UTF_8)
    }

    def "FileSAS network test upload fails"() {
        setup:
        String data = "test"
        primaryFileClient.create(Constants.KB)

        def permissions = new FileSasPermission()
            .setReadPermission(true)
            .setWritePermission(false)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new IpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SasProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        when:
        def credential = SharedKeyCredential.fromConnectionString(connectionString)
        def sas = new FileServiceSasSignatureValues()
            .setPermissions(permissions.toString())
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setIpRange(ipRange)
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
            .setCanonicalName(primaryFileClient.getShareName(), primaryFileClient.getFilePath(), credential.getAccountName())
            .setResource(Constants.UrlConstants.SAS_FILE_CONSTANT)
            .generateSASQueryParameters(credential)
            .encode()

        def client = fileBuilderHelper(interceptorManager, shareName, filePath)
            .endpoint(primaryFileClient.getFileUrl())
            .sasToken(sas)
            .buildFileClient()

        client.upload(getInputStream(data.getBytes()), (long) data.length())

        then:
        thrown(FileStorageException)

        when:
        client.delete()

        then:
        notThrown(FileStorageException)
    }

    def "ShareSAS network test identifier permissions create delete"() {
        setup:
        FileSignedIdentifier identifier = new FileSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new FileAccessPolicy().setPermissions("rcwdl")
                .setExpiresOn(getUTCNow().plusDays(1)))

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
        def credential = SharedKeyCredential.fromConnectionString(connectionString)
        def sasWithId = new FileServiceSasSignatureValues()
            .setIdentifier(identifier.getId())
            .setCanonicalName(primaryShareClient.getShareName(), credential.getAccountName())
            .setResource(Constants.UrlConstants.SAS_SHARE_CONSTANT)
            .generateSASQueryParameters(credential)
            .encode()

        ShareClient client1 = shareBuilderHelper(interceptorManager, primaryShareClient.getShareName())
            .endpoint(primaryShareClient.getShareUrl())
            .sasToken(sasWithId)
            .buildClient()

        client1.createDirectory("dir")
        client1.deleteDirectory("dir")

        def sasWithPermissions = new FileServiceSasSignatureValues()
            .setPermissions(permissions.toString())
            .setExpiryTime(expiryTime)
            .setCanonicalName(primaryShareClient.getShareName(), credential.getAccountName())
            .setResource(Constants.UrlConstants.SAS_SHARE_CONSTANT)
            .generateSASQueryParameters(credential)
            .encode()

        def client2 = shareBuilderHelper(interceptorManager, primaryShareClient.getShareName())
            .endpoint(primaryFileClient.getFileUrl())
            .sasToken(sasWithPermissions)
            .buildClient()

        client2.createDirectory("dir")
        client2.deleteDirectory("dir")

        then:
        notThrown(FileStorageException)
    }

    def "AccountSAS FileService network test create delete share succeeds"() {
        setup:
        def service = new AccountSasService()
            .setFile(true)
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
        def credential = SharedKeyCredential.fromConnectionString(connectionString)
        def sas = AccountSasSignatureValues.generateAccountSas(credential, service, resourceType, permissions, expiryTime, null, null, null, null)

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
        notThrown(FileStorageException)
    }


}
