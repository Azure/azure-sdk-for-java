package com.azure.storage.blob

import com.azure.storage.blob.implementation.util.BlobSasImplUtil
import com.azure.storage.blob.models.BlobAccessPolicy
import com.azure.storage.blob.models.BlobProperties
import com.azure.storage.blob.models.BlobSignedIdentifier
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.UserDelegationKey
import com.azure.storage.blob.sas.BlobContainerSasPermission
import com.azure.storage.blob.sas.BlobSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.implementation.StorageImplUtils
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.sas.SasIpRange
import com.azure.storage.common.sas.SasProtocol
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SasClientTests extends APISpec {

    BlockBlobClient sasClient
    String blobName

    def setup() {
        blobName = generateBlobName()
        sasClient = getBlobClient(primaryCredential, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        sasClient.upload(new ByteArrayInputStream(defaultData.array()), defaultDataSize)
    }

    def "network test blob sas"() {
        setup:
        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateSas(sasValues)

        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(defaultData.array())
        validateSasProperties(properties)
        notThrown(BlobStorageException)
    }

    def "network test blob snapshot"() {
        setup:

        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = getBlobClient(primaryCredential, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        bu.upload(new ByteArrayInputStream(data), data.length)
        def snapshotId = bu.createSnapshot().getSnapshotId()
        def snapshotBlob = cc.getBlobClient(blobName, snapshotId).getBlockBlobClient()

        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)

        def sasValues = generateValues(permissions)

        when:
        def sas = snapshotBlob.generateSas(sasValues)

        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName, snapshotId).getBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(data)
        validateSasProperties(properties)
    }

    def "serviceSASSignatureValues network test container"() {
        setup:
        def identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy().setPermissions("racwdl")
                .setExpiresOn(getUTCNow().plusDays(1)))
        cc.setAccessPolicy(null, Arrays.asList(identifier))

        // Check containerSASPermissions
        def permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setListPermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new BlobServiceSasSignatureValues(identifier.getId())
        def sasWithId = cc.generateSas(sasValues)
        def client1 = getContainerClient(sasWithId, cc.getBlobContainerUrl())
        client1.listBlobs().iterator().hasNext()

        and:
        sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
        def sasWithPermissions = cc.generateSas(sasValues)
        def client2 = getContainerClient(sasWithPermissions, cc.getBlobContainerUrl())
        client2.listBlobs().iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    def "network test blob user delegation"() {
        setup:
        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateUserDelegationSas(sasValues, getUserDelegationInfo())

        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(defaultData.array())
        validateSasProperties(properties)
        notThrown(BlobStorageException)
    }

    def "BlobServiceSAS network test blob snapshot"() {
        setup:
        def snapshotBlob = new SpecializedBlobClientBuilder().blobClient(sasClient.createSnapshot()).buildBlockBlobClient()
        def snapshotId = snapshotBlob.getSnapshotId()
        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
        def sasValues = generateValues(permissions)
        def sas = snapshotBlob.generateSas(sasValues)

        when:
        // base blob with snapshot SAS
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getAppendBlobClient()
        client.download(new ByteArrayOutputStream())

        then:
        // snapshot-level SAS shouldn't be able to access base blob
        thrown(BlobStorageException)

        when:
        // blob snapshot with snapshot SAS
        def snapClient = getBlobClient(sas, cc.getBlobContainerUrl(), blobName, snapshotId).getAppendBlobClient()

        def os = new ByteArrayOutputStream()
        snapClient.download(os)

        def properties = snapClient.getProperties()

        then:
        notThrown(BlobStorageException)
        os.toByteArray() == defaultData.array()

        then:
        validateSasProperties(properties)
    }

    def "network test blob snapshot user delegation"() {
        setup:
        def snapshotBlob = new SpecializedBlobClientBuilder().blobClient(sasClient.createSnapshot()).buildBlockBlobClient()
        def snapshotId = snapshotBlob.getSnapshotId()

        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
        def sasValues = generateValues(permissions)
        def sas = snapshotBlob.generateUserDelegationSas(sasValues, getUserDelegationInfo())

        when:
        // base blob with snapshot SAS
        def client1 = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        client1.download(new ByteArrayOutputStream())

        then:
        // snapshot-level SAS shouldn't be able to access base blob
        thrown(BlobStorageException)

        when:
        // blob snapshot with snapshot SAS
        def client2 = getBlobClient(sas, cc.getBlobContainerUrl(), blobName, snapshotId).getBlockBlobClient()
        def os = new ByteArrayOutputStream()
        client2.download(os)

        def properties = client2.getProperties()

        then:
        notThrown(BlobStorageException)
        os.toString() == new String(defaultData.array())
        validateSasProperties(properties)
    }

    def "network test container user delegation"() {
        setup:
        def permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
        def sasWithPermissions = cc.generateUserDelegationSas(sasValues, getUserDelegationInfo())

        def client = getContainerClient(sasWithPermissions, cc.getBlobContainerUrl())
        client.listBlobs().iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    def "accountSAS network test blob read"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        def os = new ByteArrayOutputStream()
        client.download(os)

        then:
        os.toString() == new String(defaultData.array())
    }

    def "accountSAS network test blob delete fails new API"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        client.delete()

        then:
        thrown(BlobStorageException)
    }

    def "accountSAS network create container fails"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(false)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
        def sc = getServiceClient(sas, primaryBlobServiceClient.getAccountUrl())
        sc.createBlobContainer(generateContainerName())

        then:
        thrown(BlobStorageException)
    }

    def "accountSAS network create container succeeds"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
        def sc = getServiceClient(sas, primaryBlobServiceClient.getAccountUrl())
        sc.createBlobContainer(generateContainerName())

        then:
        notThrown(BlobStorageException)
    }

    BlobServiceSasSignatureValues generateValues(BlobSasPermission permission) {
        return new BlobServiceSasSignatureValues(getUTCNow().plusDays(1), permission)
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

    def validateSasProperties(BlobProperties properties) {
        boolean ret = true
        ret &= properties.getCacheControl() == "cache"
        ret &= properties.getContentDisposition() == "disposition"
        ret &= properties.getContentEncoding() == "encoding"
        ret &= properties.getContentLanguage() == "language"
        return ret
    }

    UserDelegationKey getUserDelegationInfo() {
        def key = getOAuthServiceClient().getUserDelegationKey(getUTCNow().minusDays(1), getUTCNow().plusDays(1))
        def keyOid = getConfigValue(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)
        def keyTid = getConfigValue(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)
        return key
    }

    /* Blob SAS Impl Util Tests */
    def "ensure state version"() {
        when:
        BlobSasImplUtil implUtil = new BlobSasImplUtil(new BlobServiceSasSignatureValues("id"), "container")
        implUtil.version = null
        implUtil.ensureState()

        then:
        implUtil.version // Version is set
            implUtil.resource == "c" // Default resource is container
        !implUtil.permissions // Identifier was used so permissions is null
    }

    def "ensure state illegal argument"() {
        when:
        BlobSasImplUtil implUtil = new BlobSasImplUtil(new BlobServiceSasSignatureValues(), null)

        implUtil.ensureState()

        then:
        thrown(IllegalStateException)
    }

    @Unroll
    def "ensure state resource and permission"() {
        setup:
        def expiryTime = OffsetDateTime.now().plusDays(1)

        expect:
        BlobSasImplUtil implUtil = new BlobSasImplUtil(new BlobServiceSasSignatureValues(expiryTime, permission), container, blob, snapshot)
        implUtil.ensureState()
        implUtil.resource == resource
        implUtil.permissions ==  permissionString

        where:
        container    | blob    | snapshot    | permission                                                                       || resource | permissionString
        "container"  |  null   | null        | new BlobContainerSasPermission().setReadPermission(true).setListPermission(true) || "c"      | "rl"
        "container"  | "blob"  | null        | new BlobSasPermission().setReadPermission(true)                                  || "b"      | "r"
        "container"  | "blob"  | "snapshot"  | new BlobSasPermission().setReadPermission(true)                                  || "bs"     | "r"
    }

    /*
     This test will ensure that each field gets placed into the proper location within the string to sign and that null
     values are handled correctly. We will validate the whole SAS with service calls as well as correct serialization of
     individual parts later.
     */

    @Unroll
    def "blob sas impl util string to sign"() {
        when:
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def p = new BlobSasPermission()
        p.setReadPermission(true)
        def v = new BlobServiceSasSignatureValues(e, p)

        def expected = String.format(expectedStringToSign, primaryCredential.getAccountName())

        v.setStartTime(startTime)

        if (ipRange != null) {
            def ipR = new SasIpRange()
            ipR.setIpMin("ip")
            v.setSasIpRange(ipR)
        }
        v.setIdentifier(identifier)
            .setProtocol(protocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(disposition)
            .setContentEncoding(encoding)
            .setContentLanguage(language)
            .setContentType(type)

        def implUtil = new BlobSasImplUtil(v, "containerName", "blobName", snapId)

        def sasToken = implUtil.generateSas(primaryCredential)

        def token = BlobUrlParts.parse(cc.getBlobContainerUrl() + "?" + sasToken).getSasQueryParameters()

        then:
        token.getSignature() == primaryCredential.computeHmac256(expected)

        /*
        We don't test the blob or containerName properties because canonicalized resource is always added as at least
        /blob/accountName. We test canonicalization of resources later. Again, this is not to test a fully functional
        sas but the construction of the string to sign.
        Signed resource is tested elsewhere, as we work some minor magic in choosing which value to use.
         */
        where:
        startTime                                                 | identifier | ipRange          | protocol               | snapId   | cacheControl | disposition   | encoding   | language   | type   || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null       | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | "id"       | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\nid\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | new SasIpRange() | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | null             | SasProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | null             | null                   | "snapId" | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nbs\nsnapId\n\n\n\n\n"
        null                                                      | null       | null             | null                   | null     | "control"    | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                                                      | null       | null             | null                   | null     | null         | "disposition" | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                                                      | null       | null             | null                   | null     | null         | null          | "encoding" | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                                                      | null       | null             | null                   | null     | null         | null          | null       | "language" | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                                                      | null       | null             | null                   | null     | null         | null          | null       | null       | "type" || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

    @Unroll
    def "blob sas impl util string to sign user delegation key"() {
        when:
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def p = new BlobSasPermission()
        p.setReadPermission(true)
        def v = new BlobServiceSasSignatureValues(e, p)

        def expected = String.format(expectedStringToSign, primaryCredential.getAccountName())

        v.setStartTime(startTime)

        if (ipRange != null) {
            def ipR = new SasIpRange()
            ipR.setIpMin("ip")
            v.setSasIpRange(ipR)
        }
        v.setProtocol(protocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(disposition)
            .setContentEncoding(encoding)
            .setContentLanguage(language)
            .setContentType(type)
        def key = new UserDelegationKey()
            .setSignedObjectId(keyOid)
            .setSignedTenantId(keyTid)
            .setSignedStart(keyStart)
            .setSignedExpiry(keyExpiry)
            .setSignedService(keyService)
            .setSignedVersion(keyVersion)
            .setValue(keyValue)

        def implUtil = new BlobSasImplUtil(v, "containerName", "blobName", snapId)

        def sasToken = implUtil.generateUserDelegationSas(key, primaryCredential.getAccountName())

        def token = BlobUrlParts.parse(cc.getBlobContainerUrl() + "?" + sasToken).getSasQueryParameters()

        then:
        token.getSignature() == StorageImplUtils.computeHMac256(key.getValue(), expected)

        /*
        We test string to sign functionality directly related to user delegation sas specific parameters
         */
        where:
        startTime                                                 | keyOid                                 | keyTid                                 | keyStart                                                              | keyExpiry | keyService | keyVersion | keyValue                                       | ipRange | protocol | snapId | cacheControl | disposition | encoding | language | type || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | "11111111-1111-1111-1111-111111111111" | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | "22222222-2222-2222-2222-222222222222" | null                                                                  | null      | null       | null       | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null    | null     | null   | null         | null        | null     | null     | null || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null      | null       | null       | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null    | null     | null   | null         | null        | null     | null     | null || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | "b"        | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\nb\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | "2018-06-17" | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n2018-06-17\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | new SasIpRange() | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | SasProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | "snapId" | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nbs\nsnapId\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | "control"    | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | "disposition" | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | "encoding" | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | "language" | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | "type" || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

}
