package com.azure.storage.file.spock

import com.azure.storage.common.AccountSASPermission
import com.azure.storage.common.AccountSASResourceType
import com.azure.storage.common.AccountSASService
import com.azure.storage.common.Constants
import com.azure.storage.common.IPRange
import com.azure.storage.common.SASProtocol
import com.azure.storage.common.credentials.SASTokenCredential
import com.azure.storage.file.FileClient
import com.azure.storage.file.FileClientBuilder
import com.azure.storage.file.FileSASPermission
import com.azure.storage.file.FileServiceClient
import com.azure.storage.file.FileServiceClientBuilder
import com.azure.storage.file.FileServiceSASSignatureValues
import com.azure.storage.file.ShareClient
import com.azure.storage.file.ShareClientBuilder
import com.azure.storage.file.ShareSASPermission
import com.azure.storage.file.models.AccessPolicy
import com.azure.storage.file.models.SignedIdentifier
import com.azure.storage.file.models.StorageErrorException
import spock.lang.Ignore
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
        primaryFileClient = fileBuilderHelper(interceptorManager, shareName, filePath).buildClient()
    }

    @Unroll
    def "FileSASPermissions toString"() {
        setup:
        def perms = new FileSASPermission()
            .read(read)
            .write(write)
            .delete(delete)
            .create(create)

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
        perms.read() == read
        perms.write() == write
        perms.delete() == delete
        perms.create() == create

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
            .read(read)
            .write(write)
            .delete(delete)
            .create(create)
            .list(list)

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
        perms.read() == read
        perms.write() == write
        perms.delete() == delete
        perms.create() == create
        perms.list() == list

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
        serviceSASSignatureValues.canonicalName() == "/file/" + accountName  + "/" + shareName + "/" + fileName
    }

    def "FileSAS network test download upload"() {
        setup:
        String data = "test"
        primaryFileClient.create(Constants.KB)
        primaryFileClient.upload(ByteBuffer.wrap(data.getBytes()), (long) data.length())

        def permissions = new FileSASPermission()
            .read(true)
            .write(true)
            .create(true)
            .delete(true)
        def startTime = OffsetDateTime.now().minusDays(1)
        def expiryTime = OffsetDateTime.now().plusDays(1)
        def ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255")
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
        def client = new FileClientBuilder()
            .endpoint(primaryFileClient.getFileUrl().toString())
            .filePath(filePath)
            .shareName(shareName)
            .credential(SASTokenCredential.fromSASTokenString(sas))
            .httpClient(getHttpClient())
            .buildClient()

        def downloadResponse = client.downloadWithProperties()

        def responseBody = downloadResponse.body().toIterable().iterator().next()

        client.upload(ByteBuffer.wrap(data.getBytes()), (long) data.length())

        then:
        notThrown(StorageErrorException)
        for(int i = 0; i < data.length(); i++) {
            responseBody.get(i) == data.getBytes()[i]
        }
    }

    def "FileSAS network test upload fails"() {
        setup:
        String data = "test"
        primaryFileClient.create(Constants.KB)

        def permissions = new FileSASPermission()
            .read(true)
            .write(false)
            .create(true)
            .delete(true)
        def startTime = OffsetDateTime.now().minusDays(1)
        def expiryTime = OffsetDateTime.now().plusDays(1)
        def ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255")
        def sasProtocol = SASProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        when:
        def sas = primaryFileClient.generateSAS(null, permissions, expiryTime, startTime, null, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType)

        def client = new FileClientBuilder()
            .endpoint(primaryFileClient.getFileUrl().toString())
            .filePath(filePath)
            .shareName(shareName)
            .credential(SASTokenCredential.fromSASTokenString(sas))
            .httpClient(getHttpClient())
            .buildClient()

        client.upload(ByteBuffer.wrap(data.getBytes()), (long) data.length())

        then:
        thrown(StorageErrorException)

        when:
        client.delete()

        then:
        notThrown(StorageErrorException)
    }

    def "ShareSAS network test identifier permissions create delete"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
            .id("0000")
            .accessPolicy(new AccessPolicy().permission("rcwdl")
                .expiry(OffsetDateTime.now().plusDays(1)))

        primaryShareClient.setAccessPolicy(Arrays.asList(identifier))

        // Check containerSASPermissions
        ShareSASPermission permissions = new ShareSASPermission()
            .read(true)
            .write(true)
            .create(true)
            .delete(true)
            .list(true)

        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1)

        when:
        String sasWithId = primaryShareClient.generateSAS(identifier.id())

        ShareClient client1 = new ShareClientBuilder()
            .endpoint(primaryShareClient.getShareUrl().toString())
            .shareName(primaryShareClient.client.shareName)
            .credential(SASTokenCredential.fromSASTokenString(sasWithId))
            .httpClient(getHttpClient())
            .buildClient()

        client1.createDirectory("dir")
        client1.deleteDirectory("dir")

        String sasWithPermissions = primaryShareClient.generateSAS(expiryTime, permissions)

        ShareClient client2 = new ShareClientBuilder()
            .endpoint(primaryShareClient.getShareUrl().toString())
            .shareName(primaryShareClient.client.shareName)
            .credential(SASTokenCredential.fromSASTokenString(sasWithPermissions))
            .httpClient(getHttpClient())
            .buildClient()

        client2.createDirectory("dir")
        client2.deleteDirectory("dir")

        then:
        notThrown(StorageErrorException)
    }

    def "AccountSAS FileService network test create delete share succeeds"() {
        setup:
        def service = new AccountSASService()
            .file(true)
        def resourceType = new AccountSASResourceType()
            .container(true)
            .service(true)
            .object(true)
        def permissions = new AccountSASPermission()
            .read(true)
            .create(true)
            .delete(true)
        def expiryTime = OffsetDateTime.now().plusDays(1)

        when:
        def sas = primaryFileServiceClient.generateAccountSAS(service, resourceType, permissions, expiryTime, null, null, null, null)

        then:
        sas != null

        when:
        def scBuilder = new FileServiceClientBuilder()
        scBuilder.endpoint(primaryFileServiceClient.getFileServiceUrl().toString())
            .httpClient(getHttpClient())
            .credential(SASTokenCredential.fromSASTokenString(sas))
        def sc = scBuilder.buildClient()
        sc.createShare("create")
        sc.deleteShare("create")

        then:
        notThrown(StorageErrorException)
    }


}
