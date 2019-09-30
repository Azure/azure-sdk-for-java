package com.azure.storage.file

import com.azure.storage.common.AccountSASPermission
import com.azure.storage.common.AccountSASResourceType
import com.azure.storage.common.AccountSASService
import com.azure.storage.common.Constants
import com.azure.storage.common.IPRange
import com.azure.storage.common.SASProtocol
import com.azure.storage.common.credentials.SASTokenCredential
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
        def perms = new FileSASPermission()
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
        def perms = FileSASPermission.parse(permString)

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
        FileSASPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "ShareSASPermissions toString"() {
        setup:
        def perms = new ShareSASPermission()
            .setRead(read)
            .setWrite(write)
            .setDelete(delete)
            .setCreate(create)
            .setList(list)

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
        def perms = ShareSASPermission.parse(permString)

        then:
        perms.getRead() == read
        perms.getWrite() == write
        perms.getDelete() == delete
        perms.getCreate() == create
        perms.getList() == list

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
        ShareSASPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    def "serviceSASSignatureValues canonicalizedResource"() {
        setup:
        def fileName = primaryFileClient.fileAsyncClient.filePath
        def shareName = primaryFileClient.fileAsyncClient.shareName
        def accountName = "account"

        when:
        def serviceSASSignatureValues = primaryFileClient.fileAsyncClient.configureServiceSASSignatureValues(new FileServiceSASSignatureValues(), accountName)

        then:
        serviceSASSignatureValues.getCanonicalName() == "/file/" + accountName  + "/" + shareName + "/" + fileName
    }

    def "FileSAS network test download upload"() {
        setup:
        String data = "test"
        primaryFileClient.create(Constants.KB)
        primaryFileClient.upload(ByteBuffer.wrap(data.getBytes()), (long) data.length())

        def permissions = new FileSASPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new IPRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SASProtocol.HTTPS_HTTP
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
            .endpoint(primaryFileClient.getFileUrl().toString())
            .credential(SASTokenCredential.fromSASTokenString(sas))
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

        def permissions = new FileSASPermission()
            .setReadPermission(true)
            .setWritePermission(false)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new IPRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SASProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        when:
        def sas = primaryFileClient.generateSAS(null, permissions, expiryTime, startTime, null, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType)

        def client = fileBuilderHelper(interceptorManager, shareName, filePath)
            .endpoint(primaryFileClient.getFileUrl().toString())
            .credential(SASTokenCredential.fromSASTokenString(sas))
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
        ShareSASPermission permissions = new ShareSASPermission()
            .setRead(true)
            .setWrite(true)
            .setCreate(true)
            .setDelete(true)
            .setList(true)

        OffsetDateTime expiryTime = getUTCNow().plusDays(1)

        when:
        String sasWithId = primaryShareClient.generateSAS(identifier.getId())

        ShareClient client1 = shareBuilderHelper(interceptorManager, primaryShareClient.client.shareName)
            .endpoint(primaryShareClient.getShareUrl().toString())
            .credential(SASTokenCredential.fromSASTokenString(sasWithId))
            .buildClient()

        client1.createDirectory("dir")
        client1.deleteDirectory("dir")

        String sasWithPermissions = primaryShareClient.generateSAS(expiryTime, permissions)

        def client2 = shareBuilderHelper(interceptorManager, primaryShareClient.client.shareName)
            .endpoint(primaryFileClient.getFileUrl().toString())
            .credential(SASTokenCredential.fromSASTokenString(sasWithPermissions))
            .buildClient()

        client2.createDirectory("dir")
        client2.deleteDirectory("dir")

        then:
        notThrown(StorageException)
    }

    def "AccountSAS FileService network test create delete share succeeds"() {
        setup:
        def service = new AccountSASService()
            .setFile(true)
        def resourceType = new AccountSASResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSASPermission()
            .setReadPermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sas = primaryFileServiceClient.generateAccountSAS(service, resourceType, permissions, expiryTime, null, null, null, null)

        then:
        sas != null

        when:
        def scBuilder = fileServiceBuilderHelper(interceptorManager)
        scBuilder.endpoint(primaryFileServiceClient.getFileServiceUrl().toString())
            .credential(SASTokenCredential.fromSASTokenString(sas))
        def sc = scBuilder.buildClient()
        sc.createShare("create")
        sc.deleteShare("create")

        then:
        notThrown(StorageException)
    }


}
