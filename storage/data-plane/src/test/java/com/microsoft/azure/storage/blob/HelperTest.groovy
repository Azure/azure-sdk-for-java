// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob

import com.microsoft.azure.storage.APISpec
import com.microsoft.azure.storage.blob.models.AccessPolicy
import com.microsoft.azure.storage.blob.models.SignedIdentifier
import com.microsoft.azure.storage.blob.models.StorageErrorCode
import com.microsoft.azure.storage.blob.models.UserDelegationKey
import com.microsoft.rest.v2.util.FlowableUtil
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class HelperTest extends APISpec {

    def "responseError"() {
        when:
        cu.listBlobsFlatSegment("garbage", null, null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.INVALID_QUERY_PARAMETER_VALUE
        e.statusCode() == 400
        e.message().contains("Value for one of the query parameters specified in the request URI is invalid.")
        e.getMessage().contains("<?xml") // Ensure that the details in the payload are printable
    }

    /*
    This test is to validate the workaround for the autorest bug that forgets to set the request property on the
    response.
     */
    def "Request property"() {
        when:
        def response = cu.delete().blockingGet()

        then:
        response.request() != null
    }

    @Unroll
    def "Blob range"() {
        expect:
        new BlobRange().withOffset(offset).withCount(count).toHeaderValue() == result

        where:
        offset | count || result
        0      | null  || null
        0      | 5     || "bytes=0-4"
        5      | 10    || "bytes=5-14"
    }

    @Unroll
    def "Blob range IA"() {
        when:
        new BlobRange().withOffset(offset).withCount(count)

        then:
        thrown(IllegalArgumentException)

        where:
        offset | count
        -1     | 5
        0      | -1
    }

    def "serviceSASSignatureValues network test blob"() {
        setup:
        def containerName = generateContainerName()
        def blobName = generateBlobName()
        def cu = primaryServiceURL.createContainerURL(containerName)
        cu.create(null, null, null).blockingGet()

        def v = new ServiceSASSignatureValues()
        def p = new BlobSASPermission()
                .withRead(true)
                .withWrite(true)
                .withCreate(true)
                .withDelete(true)
                .withAdd(true)
        v.withPermissions(p.toString())
                .withStartTime(OffsetDateTime.now().minusDays(1))
                .withExpiryTime(OffsetDateTime.now().plusDays(1))
                .withContainerName(containerName)
                .withBlobName(blobName)
        def ipR = new IPRange()
                .withIpMin("0.0.0.0")
                .withIpMax("255.255.255.255")
        v.withIpRange(ipR)
                .withProtocol(SASProtocol.HTTPS_ONLY)
                .withCacheControl("cache")
                .withContentDisposition("disposition")
                .withContentEncoding("encoding")
                .withContentLanguage("language")
                .withContentType("type")

        when:
        def parts = URLParser.parse(cu.createBlobURL(blobName).toURL())
        parts.withSasQueryParameters(v.generateSASQueryParameters(primaryCreds)).withScheme("https")
        def bu = new AppendBlobURL(parts.toURL(), StorageURL.createPipeline(new AnonymousCredentials(),
                new PipelineOptions()))

        then:
        bu.create(null, null, null, null).blockingGet()

        and:
        def properties = bu.getProperties(null, null).blockingGet().headers()

        then:
        properties.cacheControl() == "cache"
        properties.contentDisposition() == "disposition"
        properties.contentEncoding() == "encoding"
        properties.contentLanguage() == "language"
        properties.contentType() == "type"
    }

    def "serviceSASSignatureValues network test blob snapshot"() {
        setup:
        def containerName = generateContainerName()
        def blobName = generateBlobName()
        def cu = primaryServiceURL.createContainerURL(containerName)
        cu.create(null, null, null).blockingGet()
        def bu = cu.createBlockBlobURL(blobName)
        bu.upload(defaultFlowable, defaultDataSize).blockingGet() // need something to snapshot
        def snapshotId = bu.createSnapshot().blockingGet().headers().snapshot()


        def v = new ServiceSASSignatureValues()
        def p = new BlobSASPermission()
                .withRead(true)
                .withWrite(true)
                .withCreate(true)
                .withDelete(true)
                .withAdd(true)
        v.withPermissions(p.toString())
                .withStartTime(OffsetDateTime.now().minusDays(1))
                .withExpiryTime(OffsetDateTime.now().plusDays(1))
                .withContainerName(containerName)
                .withBlobName(blobName)
                .withSnapshotId(snapshotId)
        def ipR = new IPRange()
                .withIpMin("0.0.0.0")
                .withIpMax("255.255.255.255")
        v.withIpRange(ipR)
                .withProtocol(SASProtocol.HTTPS_ONLY)
                .withCacheControl("cache")
                .withContentDisposition("disposition")
                .withContentEncoding("encoding")
                .withContentLanguage("language")
                .withContentType("type")

        when:
        def parts = URLParser.parse(bu.toURL())
        parts.withSasQueryParameters(v.generateSASQueryParameters(primaryCreds)).withScheme("https")
        // base blob with snapshot SAS
        def bsu = new AppendBlobURL(parts.toURL(), StorageURL.createPipeline(new AnonymousCredentials(),
                new PipelineOptions()))
        bsu.download().blockingGet()

        then:
        // snapshot-level SAS shouldn't be able to access base blob
        thrown(StorageException)

        when:
        // blob snapshot with snapshot SAS
        parts.withSnapshot(snapshotId)
        bsu = new AppendBlobURL(parts.toURL(), StorageURL.createPipeline(new AnonymousCredentials(),
                new PipelineOptions()))
        def data = FlowableUtil.collectBytesInBuffer(bsu.download().blockingGet().body(null)).blockingGet()

        then:
        notThrown(StorageException)
        data == defaultData

        and:
        def properties = bsu.getProperties(null, null).blockingGet().headers()

        then:
        properties.cacheControl() == "cache"
        properties.contentDisposition() == "disposition"
        properties.contentEncoding() == "encoding"
        properties.contentLanguage() == "language"
        properties.contentType() == "type"
    }

    def "serviceSASSignatureValues network test container"() {
        setup:
        def containerName = generateContainerName()
        def cu = primaryServiceURL.createContainerURL(containerName)
        cu.create(null, null, null).blockingGet()
        def id = new SignedIdentifier().withId("0000").withAccessPolicy(new AccessPolicy().withPermission("racwdl")
                .withExpiry(OffsetDateTime.now().plusDays(1)))
        cu.setAccessPolicy(null, Arrays.asList(id), null, null).blockingGet()

        // Check id field
        def v = new ServiceSASSignatureValues()
                .withIdentifier("0000")
                .withContainerName(containerName)
                .withProtocol(SASProtocol.HTTPS_ONLY)

        // Check containerSASPermissions
        def v2 = new ServiceSASSignatureValues()
        def p = new ContainerSASPermission()
                .withRead(true)
                .withWrite(true)
                .withCreate(true)
                .withDelete(true)
                .withAdd(true)
                .withList(true)
        v2.withPermissions(p.toString())
                .withExpiryTime(OffsetDateTime.now().plusDays(1))
                .withContainerName(containerName)

        when:
        def parts = URLParser.parse(cu.toURL())
                .withSasQueryParameters(v.generateSASQueryParameters(primaryCreds))
                .withScheme("https")
        def cuSAS = new ContainerURL(parts.toURL(), StorageURL.createPipeline(new AnonymousCredentials(),
                new PipelineOptions()))

        parts.withSasQueryParameters(v2.generateSASQueryParameters(primaryCreds))
        def cuSAS2 = new ContainerURL(parts.toURL(), StorageURL.createPipeline(new AnonymousCredentials(),
                new PipelineOptions()))

        then:
        cuSAS.listBlobsFlatSegment(null, null, null).blockingGet()
        cuSAS2.listBlobsFlatSegment(null, null, null).blockingGet()
        notThrown(StorageException)
    }

    def "serviceSASSignatureValues network test blob user delegation"() {
        setup:
        def containerName = generateContainerName()
        def blobName = generateBlobName()
        def cu = primaryServiceURL.createContainerURL(containerName)
        cu.create(null, null, null).blockingGet()

        def v = new ServiceSASSignatureValues()
        def p = new BlobSASPermission()
                .withRead(true)
                .withWrite(true)
                .withCreate(true)
                .withDelete(true)
                .withAdd(true)
        v.withPermissions(p.toString())
                .withStartTime(OffsetDateTime.now().minusDays(1))
                .withExpiryTime(OffsetDateTime.now().plusDays(1))
                .withContainerName(containerName)
                .withBlobName(blobName)
        def ipR = new IPRange()
                .withIpMin("0.0.0.0")
                .withIpMax("255.255.255.255")
        v.withIpRange(ipR)
                .withProtocol(SASProtocol.HTTPS_ONLY)
                .withCacheControl("cache")
                .withContentDisposition("disposition")
                .withContentEncoding("encoding")
                .withContentLanguage("language")
                .withContentType("type")
        def key = getOAuthServiceURL().getUserDelegationKey(null, OffsetDateTime.now().plusDays(1)).blockingGet().body()

        when:
        def parts = URLParser.parse(cu.createBlobURL(blobName).toURL())
        parts.withSasQueryParameters(v.generateSASQueryParameters(key, primaryCreds.accountName)).withScheme("https")
        def bu = new AppendBlobURL(parts.toURL(), StorageURL.createPipeline(new AnonymousCredentials(),
                new PipelineOptions()))

        then:
        bu.create(null, null, null, null).blockingGet()

        and:
        def properties = bu.getProperties(null, null).blockingGet().headers()

        then:
        properties.cacheControl() == "cache"
        properties.contentDisposition() == "disposition"
        properties.contentEncoding() == "encoding"
        properties.contentLanguage() == "language"
        properties.contentType() == "type"
    }

    def "serviceSASSignatureValues network test blob snapshot user delegation"() {
        setup:
        def containerName = generateContainerName()
        def blobName = generateBlobName()
        def cu = primaryServiceURL.createContainerURL(containerName)
        cu.create(null, null, null).blockingGet()
        def bu = cu.createBlockBlobURL(blobName)
        bu.upload(defaultFlowable, defaultDataSize).blockingGet() // need something to snapshot
        def snapshotId = bu.createSnapshot().blockingGet().headers().snapshot()


        def v = new ServiceSASSignatureValues()
        def p = new BlobSASPermission()
                .withRead(true)
                .withWrite(true)
                .withCreate(true)
                .withDelete(true)
                .withAdd(true)
        v.withPermissions(p.toString())
                .withStartTime(OffsetDateTime.now().minusDays(1))
                .withExpiryTime(OffsetDateTime.now().plusDays(1))
                .withContainerName(containerName)
                .withBlobName(blobName)
                .withSnapshotId(snapshotId)
        def ipR = new IPRange()
                .withIpMin("0.0.0.0")
                .withIpMax("255.255.255.255")
        v.withIpRange(ipR)
                .withProtocol(SASProtocol.HTTPS_ONLY)
                .withCacheControl("cache")
                .withContentDisposition("disposition")
                .withContentEncoding("encoding")
                .withContentLanguage("language")
                .withContentType("type")
        def key = getOAuthServiceURL().getUserDelegationKey(null, OffsetDateTime.now().plusDays(1)).blockingGet().body()

        when:
        def parts = URLParser.parse(bu.toURL())
        parts.withSasQueryParameters(v.generateSASQueryParameters(key, primaryCreds.accountName)).withScheme("https")
        // base blob with snapshot SAS
        def bsu = new AppendBlobURL(parts.toURL(), StorageURL.createPipeline(new AnonymousCredentials(),
                new PipelineOptions()))
        bsu.download().blockingGet()

        then:
        // snapshot-level SAS shouldn't be able to access base blob
        thrown(StorageException)

        when:
        // blob snapshot with snapshot SAS
        parts.withSnapshot(snapshotId)
        bsu = new AppendBlobURL(parts.toURL(), StorageURL.createPipeline(new AnonymousCredentials(),
                new PipelineOptions()))
        def data = FlowableUtil.collectBytesInBuffer(bsu.download().blockingGet().body(null)).blockingGet()

        then:
        notThrown(StorageException)
        data == defaultData

        and:
        def properties = bsu.getProperties(null, null).blockingGet().headers()

        then:
        properties.cacheControl() == "cache"
        properties.contentDisposition() == "disposition"
        properties.contentEncoding() == "encoding"
        properties.contentLanguage() == "language"
        properties.contentType() == "type"
    }

    def "serviceSASSignatureValues network test container user delegation"() {
        setup:
        def containerName = generateContainerName()
        def cu = primaryServiceURL.createContainerURL(containerName)
        cu.create(null, null, null).blockingGet()

        def p = new ContainerSASPermission()
                .withRead(true)
                .withWrite(true)
                .withCreate(true)
                .withDelete(true)
                .withAdd(true)
                .withList(true)
        def v = new ServiceSASSignatureValues()
                .withContainerName(containerName)
                .withProtocol(SASProtocol.HTTPS_HTTP)
                .withExpiryTime(OffsetDateTime.now().plusHours(5))
                .withPermissions(p.toString())

        def key = getOAuthServiceURL().getUserDelegationKey(null, OffsetDateTime.now().plusDays(1)).blockingGet().body()

        when:
        def parts = URLParser.parse(cu.toURL())
                .withSasQueryParameters(v.generateSASQueryParameters(key, primaryCreds.accountName))
                .withScheme("http")
        def cuSAS = new ContainerURL(parts.toURL(), StorageURL.createPipeline(new AnonymousCredentials(),
                new PipelineOptions()))

        then:
        cuSAS.listBlobsFlatSegment(null, null, null).blockingGet()
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
        if (permissions != null) {
            def p = new BlobSASPermission()
            p.withRead(true)
            v.withPermissions(p.toString())
        }
        v.withStartTime(startTime)
                .withExpiryTime(expiryTime)
                .withContainerName("containerName")
                .withBlobName("blobName")
                .withSnapshotId(snapId)
        if (ipRange != null) {
            def ipR = new IPRange()
            ipR.withIpMin("ip")
            v.withIpRange(ipR)
        }
        v.withIdentifier(identifier)
                .withProtocol(protocol)
                .withCacheControl(cacheControl)
                .withContentDisposition(disposition)
                .withContentEncoding(encoding)
                .withContentLanguage(language)
                .withContentType(type)

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
        permissions             | startTime                                                 | expiryTime                                                | identifier | ipRange       | protocol               | snapId   | cacheControl | disposition   | encoding   | language   | type   || expectedStringToSign
        new BlobSASPermission() | null                                                      | null                                                      | null       | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n\n\n" + "/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                                      | null       | null          | null                   | null     | null         | null          | null       | null       | null   || "\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null       | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | "id"       | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\nid\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null       | new IPRange() | null                   | null     | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null       | null          | SASProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n" + SASProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null       | null          | null                   | "snapId" | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nbs\nsnapId\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null       | null          | null                   | null     | "control"    | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                    | null                                                      | null                                                      | null       | null          | null                   | null     | null         | "disposition" | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                    | null                                                      | null                                                      | null       | null          | null                   | null     | null         | null          | "encoding" | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                    | null                                                      | null                                                      | null       | null          | null                   | null     | null         | null          | null       | "language" | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                    | null                                                      | null                                                      | null       | null          | null                   | null     | null         | null          | null       | null       | "type" || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

    @Unroll
    def "serviceSasSignatures string to sign user delegation key"() {
        when:
        def v = new ServiceSASSignatureValues()
        if (permissions != null) {
            def p = new BlobSASPermission()
            p.withRead(true)
            v.withPermissions(p.toString())
        }
        v.withStartTime(startTime)
                .withExpiryTime(expiryTime)
                .withContainerName("containerName")
                .withBlobName("blobName")
                .withSnapshotId(snapId)
        if (ipRange != null) {
            def ipR = new IPRange()
            ipR.withIpMin("ip")
            v.withIpRange(ipR)
        }
        v.withProtocol(protocol)
                .withCacheControl(cacheControl)
                .withContentDisposition(disposition)
                .withContentEncoding(encoding)
                .withContentLanguage(language)
                .withContentType(type)
        def key = new UserDelegationKey()
                .withSignedOid(keyOid)
                .withSignedTid(keyTid)
                .withSignedStart(keyStart)
                .withSignedExpiry(keyExpiry)
                .withSignedService(keyService)
                .withSignedVersion(keyVersion)
                .withValue(keyValue)
        def token = v.generateSASQueryParameters(key, primaryCreds.getAccountName())

        then:
        token.signature() == Utility.delegateComputeHmac256(key, expectedStringToSign)

        /*
        We test string to sign functionality directly related to user delegation sas specific parameters
         */
        where:
        permissions             | startTime                                                 | expiryTime                                                | keyOid                                 | keyTid                                 | keyStart                                                              | keyExpiry                                                             | keyService | keyVersion   | keyValue                                       | ipRange       | protocol               | snapId   | cacheControl | disposition   | encoding   | language   | type   || expectedStringToSign
        new BlobSASPermission() | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n\n\n" + "/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | "11111111-1111-1111-1111-111111111111" | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | "22222222-2222-2222-2222-222222222222" | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | "b"        | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\nb\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | "2018-06-17" | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\n\n2018-06-17\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | new IPRange() | null                   | null     | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\n\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | SASProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\n\n\n\n" + SASProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | "snapId" | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nbs\nsnapId\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | "control"    | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | "disposition" | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | "encoding" | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | "language" | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | "type" || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

    @Unroll
    def "serviceSASSignatureValues canonicalizedResource"() {
        setup:
        def v = new ServiceSASSignatureValues()
                .withContainerName(containerName)
                .withBlobName(blobName)
                .withSnapshotId(snapId)

        when:
        def token = v.generateSASQueryParameters(primaryCreds)

        then:
        token.signature() == primaryCreds.computeHmac256(expectedStringToSign)
        token.resource() == expectedResource

        where:
        containerName | blobName | snapId || expectedResource | expectedStringToSign
        "c"           | "b"      | "id"   || "bs"             | "\n\n\n" + "/blob/" + primaryCreds.getAccountName() + "/c/b\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nbs\nid\n\n\n\n\n"
        "c"           | "b"      | null   || "b"              | "\n\n\n" + "/blob/" + primaryCreds.getAccountName() + "/c/b\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        "c"           | null     | null   || "c"              | "\n\n\n" + "/blob/" + primaryCreds.getAccountName() + "/c\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nc\n\n\n\n\n\n"

    }

    @Unroll
    def "serviceSasSignatureValues IA"() {
        setup:
        def v = new ServiceSASSignatureValues()
                .withContainerName(containerName)
                .withBlobName(blobName)
                .withSnapshotId("2018-01-01T00:00:00.0000000Z")
                .withVersion(version)

        when:
        v.generateSASQueryParameters(creds)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains(parameter)

        where:
        containerName | version | creds        | blobName || parameter
        null          | "v"     | primaryCreds | "b"       | "container"
        "c"           | null    | primaryCreds | "b"       | "version"
        "c"           | "v"     | null         | "b"       | "sharedKeyCredentials"
        "c"           | "v"     | primaryCreds | null      | "blobName"
    }

    @Unroll
    def "BlobSASPermissions toString"() {
        setup:
        def perms = new BlobSASPermission()
                .withRead(read)
                .withWrite(write)
                .withDelete(delete)
                .withCreate(create)
                .withAdd(add)

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
                .withRead(read)
                .withWrite(write)
                .withDelete(delete)
                .withCreate(create)
                .withAdd(add)
                .withList(list)

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
                .withIpMin(min)
                .withIpMax(max)

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
                .withRead(true)
        v.withPermissions(p.toString())
                .withServices("b")
                .withResourceTypes("o")
                .withStartTime(startTime)
                .withExpiryTime(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
        if (ipRange != null) {
            def ipR = new IPRange()
            ipR.withIpMin("ip")
            v.withIpRange(ipR)
        }
        v.withProtocol(protocol)

        def token = v.generateSASQueryParameters(primaryCreds)

        then:
        token.signature() == primaryCreds.computeHmac256(expectedStringToSign)

        where:
        startTime                                                 | ipRange       | protocol               || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null          | null                   || primaryCreds.getAccountName() + "\nr\nb\no\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
        null                                                      | new IPRange() | null                   || primaryCreds.getAccountName() + "\nr\nb\no\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
        null                                                      | null          | SASProtocol.HTTPS_ONLY || primaryCreds.getAccountName() + "\nr\nb\no\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n" + SASProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
    }

    @Unroll
    def "accountSasSignatureValues IA"() {
        setup:
        def v = new AccountSASSignatureValues()
                .withPermissions(permissions)
                .withServices(service)
                .withResourceTypes(resourceType)
                .withExpiryTime(expiryTime)
                .withVersion(version)

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
        "c"         | "b"     | "c"          | OffsetDateTime.now() | "v"     | null         || "SharedKeyCredentials"
    }

    @Unroll
    def "AccountSASPermissions toString"() {
        setup:
        def perms = new AccountSASPermission()
        perms.withRead(read)
                .withWrite(write)
                .withDelete(delete)
                .withList(list)
                .withAdd(add)
                .withCreate(create)
                .withUpdate(update)
                .withProcessMessages(process)

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
                .withService(service)
                .withContainer(container)
                .withObject(object)

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
        parts.withScheme("http")
                .withHost("host")
                .withContainerName("container")
                .withBlobName("blob")
                .withSnapshot("snapshot")
        def sasValues = new ServiceSASSignatureValues()
                .withPermissions("r")
                .withContainerName("container")
        parts.withSasQueryParameters(sasValues.generateSASQueryParameters(primaryCreds))

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
