package com.azure.storage.file


import com.azure.storage.common.AccountSasResourceType
import com.azure.storage.common.AccountSasService
import com.azure.storage.common.AccountSasPermission
import com.azure.storage.common.Constants
import com.azure.storage.common.IpRange
import com.azure.storage.common.SasProtocol
import com.azure.storage.common.credentials.SasTokenCredential
import com.azure.storage.file.models.AccessPolicy
import com.azure.storage.file.models.SignedIdentifier
import com.azure.storage.file.models.StorageException
import spock.lang.Unroll

import java.nio.ByteBuffer
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
        def fileName = primaryFileClient.fileAsyncClient.filePath
        def shareName = primaryFileClient.fileAsyncClient.shareName
        def accountName = "account"

        when:
        def serviceSASSignatureValues = primaryFileClient.fileAsyncClient.configureServiceSASSignatureValues(new FileServiceSasSignatureValues(), accountName)

        then:
        serviceSASSignatureValues.getCanonicalName() == "/file/" + accountName  + "/" + shareName + "/" + fileName
    }

    def "FileSAS network test download upload"() {
        setup:
        String data = "test"
        primaryFileClient.create(Constants.KB)
        primaryFileClient.upload(ByteBuffer.wrap(data.getBytes()), (long) data.length())

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
        def sas = primaryFileClient.generateSAS(null, permissions, expiryTime, startTime, null, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType)

        then:
        sas != null

        when:
        def client = fileBuilderHelper(interceptorManager, shareName, filePath)
            .endpoint(primaryFileClient.getFileUrl())
            .credential(SasTokenCredential.fromSasTokenString(sas))
            .buildFileClient()

        def downloadResponse = client.downloadWithProperties()

        def responseBody = downloadResponse.getBody().toIterable().iterator().next()

        client.upload(ByteBuffer.wrap(data.getBytes()), (long) data.length())

        then:
        notThrown(StorageException)
        for(int i = 0; i < data.length(); i++) {
            responseBody.get(i) == data.getBytes()[i]
        }
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
        def sas = primaryFileClient.generateSAS(null, permissions, expiryTime, startTime, null, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType)

        def client = fileBuilderHelper(interceptorManager, shareName, filePath)
            .endpoint(primaryFileClient.getFileUrl())
            .credential(SasTokenCredential.fromSasTokenString(sas))
            .buildFileClient()

        client.upload(ByteBuffer.wrap(data.getBytes()), (long) data.length())

        then:
        thrown(StorageException)

        when:
        client.delete()

        then:
        notThrown(StorageException)
    }

    def "ShareSAS network test identifier permissions create delete"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new AccessPolicy().setPermission("rcwdl")
                .setExpiry(getUTCNow().plusDays(1)))

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
        String sasWithId = primaryShareClient.generateSas(identifier.getId())

        ShareClient client1 = shareBuilderHelper(interceptorManager, primaryShareClient.client.shareName)
            .endpoint(primaryShareClient.getShareUrl())
            .credential(SasTokenCredential.fromSasTokenString(sasWithId))
            .buildClient()

        client1.createDirectory("dir")
        client1.deleteDirectory("dir")

        String sasWithPermissions = primaryShareClient.generateSas(expiryTime, permissions)

        def client2 = shareBuilderHelper(interceptorManager, primaryShareClient.client.shareName)
            .endpoint(primaryFileClient.getFileUrl())
            .credential(SasTokenCredential.fromSasTokenString(sasWithPermissions))
            .buildClient()

        client2.createDirectory("dir")
        client2.deleteDirectory("dir")

        then:
        notThrown(StorageException)
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
        def sas = primaryFileServiceClient.generateAccountSas(service, resourceType, permissions, expiryTime, null, null, null, null)

        then:
        sas != null

        when:
        def scBuilder = fileServiceBuilderHelper(interceptorManager)
        scBuilder.endpoint(primaryFileServiceClient.getFileServiceUrl())
            .credential(SasTokenCredential.fromSasTokenString(sas))
        def sc = scBuilder.buildClient()
        sc.createShare("create")
        sc.deleteShare("create")

        then:
        notThrown(StorageException)
    }


}
