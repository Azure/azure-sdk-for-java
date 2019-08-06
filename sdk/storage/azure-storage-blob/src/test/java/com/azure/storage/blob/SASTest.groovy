// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.storage.blob.models.AccessPolicy
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.SignedIdentifier
import com.azure.storage.blob.models.UserDelegationKey
import com.azure.storage.common.credentials.SASTokenCredential
import com.azure.storage.common.credentials.SharedKeyCredential
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SASTest extends APISpec {
    /*
    This test is to validate the workaround for the autorest bug that forgets to set the request property on the
    response.
     */
    def "Request property"() {
        when:
        def response = cu.delete()

        then:
        response.request() != null
    }

    @Unroll
    def "Blob range"() {
        expect:
        if (count == null) {
            assert new BlobRange(offset).toHeaderValue() == result
        } else {
            assert new BlobRange(offset, count).toHeaderValue() == result
        }


        where:
        offset | count || result
        0      | null  || null
        0      | 5     || "bytes=0-4"
        5      | 10    || "bytes=5-14"
    }

    @Unroll
    def "Blob range IA"() {
        when:
        new BlobRange(offset, count)

        then:
        thrown(IllegalArgumentException)

        where:
        offset | count
        -1     | 5
        0      | -1
    }

    def "BlobClient getSnapshot"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cu.getBlockBlobClient(blobName)
        bu.upload(new ByteArrayInputStream(data), data.length)
        def snapshotId = bu.createSnapshot().value().getSnapshotId()

        when:
        def snapshotBlob = cu.getBlockBlobClient(blobName, snapshotId)

        then:
        snapshotBlob.getSnapshotId() == snapshotId
        bu.getSnapshotId() == null
    }

    def "BlobClient isSnapshot"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cu.getBlockBlobClient(blobName)
        bu.upload(new ByteArrayInputStream(data), data.length)
        def snapshotId = bu.createSnapshot().value().getSnapshotId()

        when:
        def snapshotBlob = cu.getBlockBlobClient(blobName, snapshotId)

        then:
        snapshotBlob.isSnapshot()
        !bu.isSnapshot()

    }

    def "serviceSASSignatureValues network test blob"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cu.getBlockBlobClient(blobName)
        bu.upload(new ByteArrayInputStream(data), data.length)

        def permissions = new BlobSASPermission()
            .read(true)
            .write(true)
            .create(true)
            .delete(true)
            .add(true)
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
        def sas = bu.generateSAS(null, permissions, expiryTime, startTime, null, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType)

        def client = new BlobClientBuilder()
            .endpoint(cu.getContainerUrl().toString())
            .blobName(blobName)
            .credential(SASTokenCredential.fromSASTokenString(sas))
            .httpClient(getHttpClient())
            .buildBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties().value()

        then:
        os.toString() == new String(data)
        properties.cacheControl() == "cache"
        properties.contentDisposition() == "disposition"
        properties.contentEncoding() == "encoding"
        properties.contentLanguage() == "language"
        notThrown(StorageException)
    }

    def "serviceSASSignatureValues network test blob snapshot"() {
        setup:

        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cu.getBlockBlobClient(blobName)
        bu.upload(new ByteArrayInputStream(data), data.length)
        String snapshotId = bu.createSnapshot().value().getSnapshotId()

        def snapshotBlob = cu.getBlockBlobClient(blobName, snapshotId)

        def permissions = new BlobSASPermission()
            .read(true)
            .write(true)
            .create(true)
            .delete(true)
            .add(true)
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
        def sas = snapshotBlob.generateSAS(null, permissions, expiryTime, startTime, null, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType)

        def client = new BlobClientBuilder()
            .endpoint(cu.getContainerUrl().toString())
            .blobName(blobName)
            .snapshot(snapshotId)
            .credential(SASTokenCredential.fromSASTokenString(sas))
            .httpClient(getHttpClient())
            .buildBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties().value()

        then:
        os.toString() == new String(data)
        properties.cacheControl() == "cache"
        properties.contentDisposition() == "disposition"
        properties.contentEncoding() == "encoding"
        properties.contentLanguage() == "language"
    }

    def "serviceSASSignatureValues network test container"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
            .id("0000")
            .accessPolicy(new AccessPolicy().permission("racwdl")
                .expiry(OffsetDateTime.now().plusDays(1)))
        cu.setAccessPolicy(null, Arrays.asList(identifier), null, null)

        // Check containerSASPermissions
        ContainerSASPermission permissions = new ContainerSASPermission()
            .read(true)
            .write(true)
            .create(true)
            .delete(true)
            .add(true)

        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1)

        when:
        String sasWithId = cu.generateSAS(identifier.id())

        ContainerClient client1 = new ContainerClientBuilder()
            .endpoint(cu.getContainerUrl().toString())
            .credential(SASTokenCredential.fromSASTokenString(sasWithId))
            .httpClient(getHttpClient())
            .buildClient()

        client1.listBlobsFlat().iterator().hasNext()

        String sasWithPermissions = cu.generateSAS(permissions, expiryTime)

        ContainerClient client2 = new ContainerClientBuilder()
            .endpoint(cu.getContainerUrl().toString())
            .credential(SASTokenCredential.fromSASTokenString(sasWithPermissions))
            .httpClient(getHttpClient())
            .buildClient()

        client2.listBlobsFlat().iterator().hasNext()

        then:
        notThrown(StorageException)
    }


    def "serviceSASSignatureValues network test blob user delegation"() {
        setup:
        byte[] data = "test".getBytes()
        String blobName = generateBlobName()
        BlockBlobClient bu = cu.getBlockBlobClient(blobName)
        bu.upload(new ByteArrayInputStream(data), data.length)

        BlobSASPermission permissions = new BlobSASPermission()
            .read(true)
            .write(true)
            .create(true)
            .delete(true)
            .add(true)

        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1)
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1)

        IPRange ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255")

        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP
        String cacheControl = "cache"
        String contentDisposition = "disposition"
        String contentEncoding = "encoding"
        String contentLanguage = "language"
        String contentType = "type"

        UserDelegationKey key = getOAuthServiceURL().getUserDelegationKey(null, OffsetDateTime.now().plusDays(1)).value()

        when:
        String sas = bu.generateUserDelegationSAS(key, primaryCreds.accountName(), permissions, expiryTime, startTime, null, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType)

        BlockBlobClient client = new BlobClientBuilder()
            .endpoint(cu.getContainerUrl().toString())
            .blobName(blobName)
            .credential(SASTokenCredential.fromSASTokenString(sas))
            .httpClient(getHttpClient())
            .buildBlockBlobClient()

        OutputStream os = new ByteArrayOutputStream()
        client.download(os)
        BlobProperties properties = client.getProperties().value()

        then:
        os.toString() == new String(data)
        properties.cacheControl() == "cache"
        properties.contentDisposition() == "disposition"
        properties.contentEncoding() == "encoding"
        properties.contentLanguage() == "language"
        notThrown(StorageException)
    }

    def "serviceSASSignatureValues network test blob snapshot user delegation"() {
        setup:
        byte[] data = "test".getBytes()
        String blobName = generateBlobName()
        BlockBlobClient bu = cu.getBlockBlobClient(blobName)
        bu.upload(new ByteArrayInputStream(data), data.length)
        BlockBlobClient snapshotBlob = bu.createSnapshot().value().asBlockBlobClient()
        String snapshotId = snapshotBlob.getSnapshotId()

        BlobSASPermission permissions = new BlobSASPermission()
            .read(true)
            .write(true)
            .create(true)
            .delete(true)
            .add(true)

        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1)
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1)

        IPRange ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255")

        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP
        String cacheControl = "cache"
        String contentDisposition = "disposition"
        String contentEncoding = "encoding"
        String contentLanguage = "language"
        String contentType = "type"

        UserDelegationKey key = getOAuthServiceURL().getUserDelegationKey(startTime, expiryTime).value()

        when:

        String sas = snapshotBlob.generateUserDelegationSAS(key, primaryCreds.accountName(), permissions, expiryTime, startTime, null, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType)

        // base blob with snapshot SAS
        BlockBlobClient client1 = new BlobClientBuilder()
            .endpoint(cu.getContainerUrl().toString())
            .blobName(blobName)
            .credential(SASTokenCredential.fromSASTokenString(sas))
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .buildBlockBlobClient()
        client1.download(new ByteArrayOutputStream())

        then:
        // snapshot-level SAS shouldn't be able to access base blob
        thrown(StorageException)

        when:
        // blob snapshot with snapshot SAS
        BlockBlobClient client2 = new BlobClientBuilder()
            .endpoint(cu.getContainerUrl().toString())
            .blobName(blobName)
            .snapshot(snapshotId)
            .credential(SASTokenCredential.fromSASTokenString(sas))
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .buildBlockBlobClient()

        OutputStream os = new ByteArrayOutputStream()
        client2.download(os)

        then:
        notThrown(StorageException)
        os.toString() == new String(data)

        and:
        def properties = client2.getProperties().value()

        then:
        properties.cacheControl() == "cache"
        properties.contentDisposition() == "disposition"
        properties.contentEncoding() == "encoding"
        properties.contentLanguage() == "language"
    }

    def "serviceSASSignatureValues network test container user delegation"() {
        setup:
        ContainerSASPermission permissions = new ContainerSASPermission()
            .read(true)
            .write(true)
            .create(true)
            .delete(true)
            .add(true)

        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1)

        UserDelegationKey key = getOAuthServiceURL().getUserDelegationKey(null, OffsetDateTime.now().plusDays(1)).value()

        when:

        String sasWithPermissions = cu.generateUserDelegationSAS(key, primaryCreds.accountName(), permissions, expiryTime)

        ContainerClient client = new ContainerClientBuilder()
            .endpoint(cu.getContainerUrl().toString())
            .credential(SASTokenCredential.fromSASTokenString(sasWithPermissions))
            .httpClient(getHttpClient())
            .buildClient()

        client.listBlobsFlat().iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    def "accountSAS network test blob read"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cu.getBlockBlobClient(blobName)
        bu.upload(new ByteArrayInputStream(data), data.length)

        def service = new AccountSASService()
            .blob(true)
        def resourceType = new AccountSASResourceType()
            .container(true)
            .service(true)
            .object(true)
        def permissions = new AccountSASPermission()
            .read(true)
        def expiryTime = OffsetDateTime.now().plusDays(1)

        when:
        def sas = primaryServiceURL.generateAccountSAS(service, resourceType, permissions, expiryTime, null, null, null, null)

        def client = new BlobClientBuilder()
            .endpoint(cu.getContainerUrl().toString())
            .blobName(blobName)
            .credential(SASTokenCredential.fromSASTokenString(sas))
            .httpClient(getHttpClient())
            .buildBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)

        then:
        os.toString() == new String(data)
    }

    def "accountSAS network test blob delete fails"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cu.getBlockBlobClient(blobName)
        bu.upload(new ByteArrayInputStream(data), data.length)

        def service = new AccountSASService()
            .blob(true)
        def resourceType = new AccountSASResourceType()
            .container(true)
            .service(true)
            .object(true)
        def permissions = new AccountSASPermission()
            .read(true)
        def expiryTime = OffsetDateTime.now().plusDays(1)

        when:
        def sas = primaryServiceURL.generateAccountSAS(service, resourceType, permissions, expiryTime, null, null, null, null)

        def client = new BlobClientBuilder()
            .endpoint(cu.getContainerUrl().toString())
            .blobName(blobName)
            .credential(SASTokenCredential.fromSASTokenString(sas))
            .httpClient(getHttpClient())
            .buildBlockBlobClient()
        client.delete()

        then:
        thrown(StorageException)
    }

    def "accountSAS network create container fails"() {
        setup:
        def service = new AccountSASService()
            .blob(true)
        def resourceType = new AccountSASResourceType()
            .container(true)
            .service(true)
            .object(true)
        def permissions = new AccountSASPermission()
            .read(true)
            .create(false)
        def expiryTime = OffsetDateTime.now().plusDays(1)

        when:
        def sas = primaryServiceURL.generateAccountSAS(service, resourceType, permissions, expiryTime, null, null, null, null)

        def scBuilder = new BlobServiceClientBuilder()
        scBuilder.endpoint(primaryServiceURL.getAccountUrl().toString())
            .httpClient(getHttpClient())
            .credential(SASTokenCredential.fromSASTokenString(sas))
        def sc = scBuilder.buildClient()
        sc.createContainer("container")

        then:
        thrown(StorageException)
    }

    def "accountSAS network create container succeeds"() {
        setup:
        def service = new AccountSASService()
            .blob(true)
        def resourceType = new AccountSASResourceType()
            .container(true)
            .service(true)
            .object(true)
        def permissions = new AccountSASPermission()
            .read(true)
            .create(true)
        def expiryTime = OffsetDateTime.now().plusDays(1)

        when:
        def sas = primaryServiceURL.generateAccountSAS(service, resourceType, permissions, expiryTime, null, null, null, null)

        def scBuilder = new BlobServiceClientBuilder()
        scBuilder.endpoint(primaryServiceURL.getAccountUrl().toString())
            .httpClient(getHttpClient())
            .credential(SASTokenCredential.fromSASTokenString(sas))
        def sc = scBuilder.buildClient()
        sc.createContainer("container")

        then:
        notThrown(StorageException)
    }

    /*
     This test will ensure that each field gets placed into the proper location within the string to sign and that null
     values are handled correctly. We will validate the whole SAS with service calls as well as correct serialization of
     individual parts later.
     */

    @Unroll
    def "serviceSasSignatures string to sign"() {
        when:
        def v = new ServiceSASSignatureValues()
        def p = new BlobSASPermission()
        p.read(true)
        v.permissions(p.toString())

        v.startTime(startTime)
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        v.expiryTime(e)

        v.canonicalName("containerName/blobName")
            .snapshotId(snapId)
        if (ipRange != null) {
            def ipR = new IPRange()
            ipR.ipMin("ip")
            v.ipRange(ipR)
        }
        v.identifier(identifier)
            .protocol(protocol)
            .cacheControl(cacheControl)
            .contentDisposition(disposition)
            .contentEncoding(encoding)
            .contentLanguage(language)
            .contentType(type)

        def token = v.generateSASQueryParameters(primaryCreds)
        then:
        token.signature() == primaryCreds.computeHmac256(expectedStringToSign)

        /*
        We don't test the blob or containerName properties because canonicalized resource is always added as at least
        /blob/accountName. We test canonicalization of resources later. Again, this is not to test a fully functional
        sas but the construction of the string to sign.
        Signed resource is tested elsewhere, as we work some minor magic in choosing which value to use.
         */
        where:
        startTime                                                 | identifier | ipRange       | protocol               | snapId   | cacheControl | disposition   | encoding   | language   | type   || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null       | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | "id"       | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\nid\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | null       | new IPRange() | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | null       | null          | SASProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n" + SASProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | null       | null          | null                   | "snapId" | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\nsnapId\n\n\n\n\n"
        null                                                      | null       | null          | null                   | null     | "control"    | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\ncontrol\n\n\n\n"
        null                                                      | null       | null          | null                   | null     | null         | "disposition" | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\ndisposition\n\n\n"
        null                                                      | null       | null          | null                   | null     | null         | null          | "encoding" | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\nencoding\n\n"
        null                                                      | null       | null          | null                   | null     | null         | null          | null       | "language" | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\nlanguage\n"
        null                                                      | null       | null          | null                   | null     | null         | null          | null       | null       | "type" || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\ntype"
    }

    @Unroll
    def "serviceSasSignatures string to sign user delegation key"() {
        when:
        def v = new ServiceSASSignatureValues()

        def p = new BlobSASPermission()
        p.read(true)
        v.permissions(p.toString())

        v.startTime(startTime)
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        v.expiryTime(e)

        v.canonicalName("containerName/blobName")
            .snapshotId(snapId)
        if (ipRange != null) {
            def ipR = new IPRange()
            ipR.ipMin("ip")
            v.ipRange(ipR)
        }
        v.protocol(protocol)
            .cacheControl(cacheControl)
            .contentDisposition(disposition)
            .contentEncoding(encoding)
            .contentLanguage(language)
            .contentType(type)
        def key = new UserDelegationKey()
            .signedOid(keyOid)
            .signedTid(keyTid)
            .signedStart(keyStart)
            .signedExpiry(keyExpiry)
            .signedService(keyService)
            .signedVersion(keyVersion)
            .value(keyValue)
        def token = v.generateSASQueryParameters(key)

        then:
        token.signature() == Utility.delegateComputeHmac256(key, expectedStringToSign)

        /*
        We test string to sign functionality directly related to user delegation sas specific parameters
         */
        where:
        startTime                                                 | keyOid                                 | keyTid                                 | keyStart                                                              | keyExpiry                                                             | keyService | keyVersion   | keyValue                                       | ipRange       | protocol               | snapId   | cacheControl | disposition   | encoding   | language   | type   || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | "11111111-1111-1111-1111-111111111111" | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | null                                   | "22222222-2222-2222-2222-222222222222" | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | "b"        | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n\nb\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | "2018-06-17" | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n\n\n2018-06-17\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | new IPRange() | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n\n\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | SASProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n\n\n\n\n" + SASProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | "snapId" | null         | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\nsnapId\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | "control"    | null          | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\ncontrol\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | "disposition" | null       | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\ndisposition\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | "encoding" | null       | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\nencoding\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | "language" | null   || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\nlanguage\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | "type" || "r\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\ncontainerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n\n\ntype"
    }

    @Unroll
    def "serviceSASSignatureValues canonicalizedResource"() {
        setup:
        def blobName = generateBlobName()
        def accountName = "account"
        def bu = cu.getBlockBlobClient(blobName)

        when:
        def serviceSASSignatureValues = bu.blockBlobAsyncClient.configureServiceSASSignatureValues(new ServiceSASSignatureValues(), accountName)

        then:
        serviceSASSignatureValues.canonicalName() == "/blob/" + accountName + cu.containerUrl.path + "/" + blobName
    }

    @Unroll
    def "serviceSasSignatureValues IA"() {
        setup:
        def v = new ServiceSASSignatureValues()
            .snapshotId("2018-01-01T00:00:00.0000000Z")
            .version(version)

        when:
        v.generateSASQueryParameters((SharedKeyCredential) creds)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains(parameter)

        where:
        version | creds       || parameter
        null    | primaryCreds | "version"
        "v"     | null         | "sharedKeyCredentials"
    }

    @Unroll
    def "BlobSASPermissions toString"() {
        setup:
        def perms = new BlobSASPermission()
            .read(read)
            .write(write)
            .delete(delete)
            .create(create)
            .add(add)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | create | add   || expectedString
        true  | false | false  | false  | false || "r"
        false | true  | false  | false  | false || "w"
        false | false | true   | false  | false || "d"
        false | false | false  | true   | false || "c"
        false | false | false  | false  | true  || "a"
        true  | true  | true   | true   | true  || "racwd"
    }

    @Unroll
    def "BlobSASPermissions parse"() {
        when:
        def perms = BlobSASPermission.parse(permString)

        then:
        perms.read() == read
        perms.write() == write
        perms.delete() == delete
        perms.create() == create
        perms.add() == add

        where:
        permString || read  | write | delete | create | add
        "r"        || true  | false | false  | false  | false
        "w"        || false | true  | false  | false  | false
        "d"        || false | false | true   | false  | false
        "c"        || false | false | false  | true   | false
        "a"        || false | false | false  | false  | true
        "racwd"    || true  | true  | true   | true   | true
        "dcwra"    || true  | true  | true   | true   | true
    }

    def "BlobSASPermissions parse IA"() {
        when:
        BlobSASPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "ContainerSASPermissions toString"() {
        setup:
        def perms = new ContainerSASPermission()
            .read(read)
            .write(write)
            .delete(delete)
            .create(create)
            .add(add)
            .list(list)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | create | add   | list  || expectedString
        true  | false | false  | false  | false | false || "r"
        false | true  | false  | false  | false | false || "w"
        false | false | true   | false  | false | false || "d"
        false | false | false  | true   | false | false || "c"
        false | false | false  | false  | true  | false || "a"
        false | false | false  | false  | false | true  || "l"
        true  | true  | true   | true   | true  | true  || "racwdl"
    }

    @Unroll
    def "ContainerSASPermissions parse"() {
        when:
        def perms = ContainerSASPermission.parse(permString)

        then:
        perms.read() == read
        perms.write() == write
        perms.delete() == delete
        perms.create() == create
        perms.add() == add
        perms.list() == list

        where:
        permString || read  | write | delete | create | add   | list
        "r"        || true  | false | false  | false  | false | false
        "w"        || false | true  | false  | false  | false | false
        "d"        || false | false | true   | false  | false | false
        "c"        || false | false | false  | true   | false | false
        "a"        || false | false | false  | false  | true  | false
        "l"        || false | false | false  | false  | false | true
        "racwdl"   || true  | true  | true   | true   | true  | true
        "dcwrla"   || true  | true  | true   | true   | true  | true
    }

    def "ContainerSASPermissions parse IA"() {
        when:
        ContainerSASPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "IPRange toString"() {
        setup:
        def ip = new IPRange()
            .ipMin(min)
            .ipMax(max)

        expect:
        ip.toString() == expectedString

        where:
        min  | max  || expectedString
        "a"  | "b"  || "a-b"
        "a"  | null || "a"
        null | "b"  || ""
    }

    @Unroll
    def "IPRange parse"() {
        when:
        def ip = IPRange.parse(rangeStr)

        then:
        ip.ipMin() == min
        ip.ipMax() == max

        where:
        rangeStr || min | max
        "a-b"    || "a" | "b"
        "a"      || "a" | null
        ""       || ""  | null
    }

    @Unroll
    def "SASProtocol parse"() {
        expect:
        SASProtocol.parse(protocolStr) == protocol

        where:
        protocolStr  || protocol
        "https"      || SASProtocol.HTTPS_ONLY
        "https,http" || SASProtocol.HTTPS_HTTP
    }

    @Unroll
    def "ServiceSASSignatureValues assertGenerateOk"() {
        when:
        def serviceSASSignatureValues = new ServiceSASSignatureValues()
        serviceSASSignatureValues.version(version)
        serviceSASSignatureValues.canonicalName(canonicalName)
        serviceSASSignatureValues.expiryTime(expiryTime)
        serviceSASSignatureValues.permissions(permissions)
        serviceSASSignatureValues.identifier(identifier)
        serviceSASSignatureValues.resource(resource)
        serviceSASSignatureValues.snapshotId(snapshotId)

        if (usingUserDelegation) {
            serviceSASSignatureValues.generateSASQueryParameters(new UserDelegationKey())
        } else {
            serviceSASSignatureValues.generateSASQueryParameters(new SharedKeyCredential("", ""))
        }

        then:

        thrown(IllegalArgumentException)

        where:
        usingUserDelegation | version                                          | canonicalName            | expiryTime                                                | permissions                                   | identifier | resource | snapshotId
        false               | null                                             | null                     | null                                                      | null                                          | null       | null     | null
        false               | Constants.HeaderConstants.TARGET_STORAGE_VERSION | null                     | null                                                      | null                                          | null       | null     | null
        false               | Constants.HeaderConstants.TARGET_STORAGE_VERSION | "containerName/blobName" | null                                                      | null                                          | null       | null     | null
        false               | Constants.HeaderConstants.TARGET_STORAGE_VERSION | "containerName/blobName" | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                          | null       | null     | null
        false               | Constants.HeaderConstants.TARGET_STORAGE_VERSION | "containerName/blobName" | null                                                      | new BlobSASPermission().read(true).toString() | null       | null     | null
        false               | null                                             | null                     | null                                                      | null                                          | "0000"     | "c"      | "id"
    }

    /*
     This test will ensure that each field gets placed into the proper location within the string to sign and that null
     values are handled correctly. We will validate the whole SAS with service calls as well as correct serialization of
     individual parts later.
     */

    @Unroll
    def "accountSasSignatures string to sign"() {
        when:
        def v = new AccountSASSignatureValues()
        def p = new AccountSASPermission()
            .read(true)
        v.permissions(p.toString())
            .services("b")
            .resourceTypes("o")
            .startTime(startTime)
            .expiryTime(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
        if (ipRange != null) {
            def ipR = new IPRange()
            ipR.ipMin("ip")
            v.ipRange(ipR)
        }
        v.protocol(protocol)

        def token = v.generateSASQueryParameters(primaryCreds)

        then:
        token.signature() == primaryCreds.computeHmac256(String.format(expectedStringToSign, primaryCreds.accountName()))

        where:
        startTime                                                 | ipRange       | protocol               || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null          | null                   || "%s" + "\nr\nb\no\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
        null                                                      | new IPRange() | null                   || "%s" + "\nr\nb\no\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
        null                                                      | null          | SASProtocol.HTTPS_ONLY || "%s" + "\nr\nb\no\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n" + SASProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
    }

    @Unroll
    def "accountSasSignatureValues IA"() {
        setup:
        def v = new AccountSASSignatureValues()
            .permissions(permissions)
            .services(service)
            .resourceTypes(resourceType)
            .expiryTime(expiryTime)
            .version(version)

        when:
        v.generateSASQueryParameters(creds)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains(parameter)

        where:
        permissions | service | resourceType | expiryTime           | version | creds        || parameter
        null        | "b"     | "c"          | OffsetDateTime.now() | "v"     | primaryCreds || "permissions"
        "c"         | null    | "c"          | OffsetDateTime.now() | "v"     | primaryCreds || "services"
        "c"         | "b"     | null         | OffsetDateTime.now() | "v"     | primaryCreds || "resourceTypes"
        "c"         | "b"     | "c"          | null                 | "v"     | primaryCreds || "expiryTime"
        "c"         | "b"     | "c"          | OffsetDateTime.now() | null    | primaryCreds || "version"
        "c"         | "b"     | "c"          | OffsetDateTime.now() | "v"     | null         || "SharedKeyCredential"
    }

    @Unroll
    def "AccountSASPermissions toString"() {
        setup:
        def perms = new AccountSASPermission()
        perms.read(read)
            .write(write)
            .delete(delete)
            .list(list)
            .add(add)
            .create(create)
            .update(update)
            .processMessages(process)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | list  | add   | create | update | process || expectedString
        true  | false | false  | false | false | false  | false  | false   || "r"
        false | true  | false  | false | false | false  | false  | false   || "w"
        false | false | true   | false | false | false  | false  | false   || "d"
        false | false | false  | true  | false | false  | false  | false   || "l"
        false | false | false  | false | true  | false  | false  | false   || "a"
        false | false | false  | false | false | true   | false  | false   || "c"
        false | false | false  | false | false | false  | true   | false   || "u"
        false | false | false  | false | false | false  | false  | true    || "p"
        true  | true  | true   | true  | true  | true   | true   | true    || "rwdlacup"
    }

    @Unroll
    def "AccountSASPermissions parse"() {
        when:
        def perms = AccountSASPermission.parse(permString)

        then:
        perms.read() == read
        perms.write() == write
        perms.delete() == delete
        perms.list() == list
        perms.add() == add
        perms.create() == create
        perms.update() == update
        perms.processMessages() == process

        where:
        permString || read  | write | delete | list  | add   | create | update | process
        "r"        || true  | false | false  | false | false | false  | false  | false
        "w"        || false | true  | false  | false | false | false  | false  | false
        "d"        || false | false | true   | false | false | false  | false  | false
        "l"        || false | false | false  | true  | false | false  | false  | false
        "a"        || false | false | false  | false | true  | false  | false  | false
        "c"        || false | false | false  | false | false | true   | false  | false
        "u"        || false | false | false  | false | false | false  | true   | false
        "p"        || false | false | false  | false | false | false  | false  | true
        "rwdlacup" || true  | true  | true   | true  | true  | true   | true   | true
        "lwrupcad" || true  | true  | true   | true  | true  | true   | true   | true
    }

    def "AccountSASPermissions parse IA"() {
        when:
        AccountSASPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "AccountSASResourceType toString"() {
        setup:
        def resourceTypes = new AccountSASResourceType()
            .service(service)
            .container(container)
            .object(object)

        expect:
        resourceTypes.toString() == expectedString

        where:
        service | container | object || expectedString
        true    | false     | false  || "s"
        false   | true      | false  || "c"
        false   | false     | true   || "o"
        true    | true      | true   || "sco"
    }

    @Unroll
    def "AccountSASResourceType parse"() {
        when:
        def resourceTypes = AccountSASResourceType.parse(resourceTypeString)

        then:
        resourceTypes.service() == service
        resourceTypes.container() == container
        resourceTypes.object() == object

        where:
        resourceTypeString || service | container | object
        "s"                || true    | false     | false
        "c"                || false   | true      | false
        "o"                || false   | false     | true
        "sco"              || true    | true      | true
    }

    @Unroll
    def "AccountSASResourceType IA"() {
        when:
        AccountSASResourceType.parse("scq")

        then:
        thrown(IllegalArgumentException)
    }

    def "BlobURLParts"() {
        setup:
        def parts = new BlobURLParts()
        parts.scheme("http")
            .host("host")
            .containerName("container")
            .blobName("blob")
            .snapshot("snapshot")
        def sasValues = new ServiceSASSignatureValues()
            .permissions("r")
            .canonicalName("/containerName/blobName")
            .expiryTime(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
        parts.sasQueryParameters(sasValues.generateSASQueryParameters(primaryCreds))

        when:
        def splitParts = parts.toURL().toString().split("\\?")

        then:
        splitParts.size() == 2 // Ensure that there is only one question mark even when sas and snapshot are present
        splitParts[0] == "http://host/container/blob"
        splitParts[1].contains("snapshot=snapshot")
        splitParts[1].contains("sp=r")
        splitParts[1].contains("sig=")
        splitParts[1].split("&").size() == 5 // snapshot & sv & sr & sp & sig
    }

    def "URLParser"() {
        when:
        def parts = URLParser.parse(new URL("http://host/container/blob?snapshot=snapshot&sv=" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "&sr=c&sp=r&sig=Ee%2BSodSXamKSzivSdRTqYGh7AeMVEk3wEoRZ1yzkpSc%3D"))

        then:
        parts.scheme() == "http"
        parts.host() == "host"
        parts.containerName() == "container"
        parts.blobName() == "blob"
        parts.snapshot() == "snapshot"
        parts.sasQueryParameters().permissions() == "r"
        parts.sasQueryParameters().version() == Constants.HeaderConstants.TARGET_STORAGE_VERSION
        parts.sasQueryParameters().resource() == "c"
        parts.sasQueryParameters().signature() == Utility.safeURLDecode("Ee%2BSodSXamKSzivSdRTqYGh7AeMVEk3wEoRZ1yzkpSc%3D")
    }
}
