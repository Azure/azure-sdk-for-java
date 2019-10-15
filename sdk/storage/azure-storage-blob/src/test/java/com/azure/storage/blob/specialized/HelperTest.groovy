// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.BlobSasPermission
import com.azure.storage.blob.BlobContainerSasPermission
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.UserDelegationKey
import com.azure.storage.common.AccountSasPermission
import com.azure.storage.common.AccountSasResourceType
import com.azure.storage.common.AccountSasSignatureValues
import com.azure.storage.common.Constants
import com.azure.storage.common.IpRange
import com.azure.storage.common.SasProtocol
import com.azure.storage.common.Utility
import com.azure.storage.common.credentials.SharedKeyCredential
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class HelperTest extends APISpec {

    // TODO (alzimmer): Turn this on when nextPageLink can be passed into listing
    /*def "responseError"() {
        when:
        cc.listBlobsFlat().iterator().hasNext()

        then:
        def e = thrown(StorageException)
        e.getErrorCode() == StorageErrorCode.INVALID_QUERY_PARAMETER_VALUE
        e.getStatusCode() == 400
        e.message().contains("Value for one of the query parameters specified in the request URI is invalid.")
        e.getServiceMessage().contains("<?xml") // Ensure that the details in the payload are printable
    }*/

    /*
    This test is to validate the workaround for the autorest bug that forgets to set the request property on the
    response.
     */
    def "Request property"() {
        when:
        def response = cc.deleteWithResponse(null, null, null)

        then:
        response.getRequest() != null
    }

    @Unroll
    def "Blob range"() {
        expect:
        new BlobRange(offset, count).toHeaderValue() == result

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

    /*
     This test will ensure that each field gets placed into the proper location within the string to sign and that null
     values are handled correctly. We will validate the whole SAS with service calls as well as correct serialization of
     individual parts later.
     */

    @Unroll
    def "serviceSasSignatures string to sign"() {
        when:
        BlobServiceSasSignatureValues v = new BlobServiceSasSignatureValues()
        if (permissions != null) {
            v.setPermissions(new BlobSasPermission().setReadPermission(true))
        } else {
            v.setPermissions(new BlobSasPermission())
        }

        if (snapId != null) {
            v.setResource(Constants.UrlConstants.SAS_BLOB_SNAPSHOT_CONSTANT)
        } else {
            v.setResource(Constants.UrlConstants.SAS_BLOB_CONSTANT)
        }

        v.setStartTime(startTime)
            .setCanonicalName(String.format("/blob/%s/containerName/blobName", primaryCredential.getAccountName()))
            .setSnapshotId(snapId)

        if (expiryTime == null) {
            v.setExpiryTime(OffsetDateTime.now())
        } else {
            v.setExpiryTime(expiryTime)
        }

        if (ipRange != null) {
            v.setIpRange(new IpRange().setIpMin("ip"))
        }

        v.setIdentifier(identifier)
            .setProtocol(protocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(disposition)
            .setContentEncoding(encoding)
            .setContentLanguage(language)
            .setContentType(type)

        BlobServiceSasQueryParameters token = v.generateSasQueryParameters(primaryCredential)

        if (startTime != null) {
            expectedStringToSign = String.format(expectedStringToSign,
                Utility.ISO_8601_UTC_DATE_FORMATTER.format(startTime),
                Utility.ISO_8601_UTC_DATE_FORMATTER.format(expiryTime),
                primaryCredential.getAccountName())
        } else {
            expectedStringToSign = String.format(expectedStringToSign,
                Utility.ISO_8601_UTC_DATE_FORMATTER.format(expiryTime),
                primaryCredential.getAccountName())
        }

        then:
        token.getSignature() == primaryCredential.computeHmac256(expectedStringToSign)

        /*
        We don't test the blob or containerName properties because canonicalized resource is always added as at least
        /blob/accountName. We test canonicalization of resources later. Again, this is not to test a fully functional
        sas but the construction of the string to sign.
        Signed resource is tested elsewhere, as we work some minor magic in choosing which value to use.
         */
        where:
        permissions             | startTime                                       | expiryTime                                     | identifier | ipRange | protocol | snapId | cacheControl | disposition | encoding | language | type   || expectedStringToSign
        new BlobSasPermission() | null                                            | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null    | null     | null   | null         | null        | null     | null     | null   || "r\n\n%s\n" + "/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | OffsetDateTime.now(ZoneOffset.UTC).minusDays(1) | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null    | null     | null   | null         | null        | null     | null     | null   || "\n%s\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | "id"       | null          | null                   | null | null | null | null | null | null || "\n\n%s\n/blob/%s/containerName/blobName\nid\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | new IpRange() | null                   | null     | null      | null | null | null | null || "\n\n%s\n/blob/%s/containerName/blobName\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | SasProtocol.HTTPS_ONLY | null     | null      | null | null | null | null || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | "snapId" | null      | null | null | null | null || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nbs\nsnapId\n\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | "control" | null | null | null | null || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | null         | "disposition" | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | null         | null          | "encoding" | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | null         | null          | null       | "language" | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                    | null                                            | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null    | null     | null   | null         | null        | null     | null     | "type" || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

    @Unroll
    def "serviceSasSignatures string to sign user delegation key"() {
        when:
        BlobServiceSasSignatureValues v = new BlobServiceSasSignatureValues()
        if (permissions != null) {
            v.setPermissions(new BlobSasPermission().setReadPermission(true))
        } else {
            v.setPermissions(new BlobSasPermission())
        }

        v.setStartTime(startTime)
            .setCanonicalName(String.format("/blob/%s/containerName/blobName", primaryCredential.getAccountName()))
            .setSnapshotId(snapId)

        if (expiryTime == null) {
            v.setExpiryTime(OffsetDateTime.now())
        } else {
            v.setExpiryTime(expiryTime)
        }

        if (snapId != null) {
            v.setResource(Constants.UrlConstants.SAS_BLOB_SNAPSHOT_CONSTANT)
        } else {
            v.setResource(Constants.UrlConstants.SAS_BLOB_CONSTANT)
        }

        if (ipRange != null) {
            v.setIpRange(new IpRange().setIpMin("ip"))
        }

        v.setProtocol(protocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(disposition)
            .setContentEncoding(encoding)
            .setContentLanguage(language)
            .setContentType(type)

        UserDelegationKey key = new UserDelegationKey()
            .setSignedOid(keyOid)
            .setSignedTid(keyTid)
            .setSignedStart(keyStart)
            .setSignedExpiry(keyExpiry)
            .setSignedService(keyService)
            .setSignedVersion(keyVersion)
            .setValue(keyValue)

        BlobServiceSasQueryParameters token = v.generateSasQueryParameters(key)

        expectedStringToSign = String.format(expectedStringToSign, Utility.ISO_8601_UTC_DATE_FORMATTER.format(v.getExpiryTime()), primaryCredential.getAccountName())

        then:
        token.getSignature() == Utility.computeHMac256(key.getValue(), expectedStringToSign)

        /*
        We test string to sign functionality directly related to user delegation sas specific parameters
         */
        where:
        permissions             | startTime | expiryTime | keyOid | keyTid | keyStart | keyExpiry | keyService | keyVersion | keyValue                                       | ipRange | protocol | snapId | cacheControl | disposition | encoding | language | type   || expectedStringToSign
        new BlobSasPermission() | null      | null       | null   | null   | null     | null      | null       | null       | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null    | null     | null   | null         | null        | null     | null     | null   || "r\n\n%s\n" + "/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | "11111111-1111-1111-1111-111111111111" | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | "22222222-2222-2222-2222-222222222222" | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | "b"        | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\nb\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | "2018-06-17" | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null | null | null | null | null | null || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n2018-06-17\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | new IpRange() | null                   | null     | null | null | null | null | null || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | SasProtocol.HTTPS_ONLY | null     | null | null | null | null | null || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | "snapId" | null | null | null | null | null || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nbs\nsnapId\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | "control"    | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | "disposition" | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | "encoding" | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | "language" | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                    | null      | null       | null   | null   | null     | null      | null       | null       | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null    | null     | null   | null         | null        | null     | null     | "type" || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

    @Unroll
    def "serviceSASSignatureValues canonicalizedResource"() {
        setup:
        BlobServiceSasSignatureValues v = new BlobServiceSasSignatureValues()
            .setExpiryTime(expiryTime)
            .setPermissions(new BlobSasPermission())
            .setResource(expectedResource)
            .setCanonicalName(String.format("/blob/%s/%s", primaryCredential.getAccountName(), containerName))
            .setSnapshotId(snapId)

        if (blobName != null) {
            v.setCanonicalName(v.getCanonicalName() + "/" + blobName)
        }

        expectedStringToSign = String.format(expectedStringToSign,
            Utility.ISO_8601_UTC_DATE_FORMATTER.format(expiryTime),
            primaryCredential.getAccountName())

        when:
        BlobServiceSasQueryParameters token = v.generateSasQueryParameters(primaryCredential)

        then:
        token.getSignature() == primaryCredential.computeHmac256(expectedStringToSign)
        token.getResource() == expectedResource

        where:
        containerName | blobName | snapId | expiryTime           || expectedResource | expectedStringToSign
        "c"           | "b"      | "id"   | OffsetDateTime.now() || "bs"             | "\n\n%s\n" + "/blob/%s/c/b\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nbs\nid\n\n\n\n\n"
        "c"           | "b"      | null   | OffsetDateTime.now() || "b"              | "\n\n%s\n" + "/blob/%s/c/b\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        "c"           | null     | null   | OffsetDateTime.now() || "c"              | "\n\n%s\n" + "/blob/%s/c\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nc\n\n\n\n\n\n"

    }

    @Unroll
    def "serviceSasSignatureValues IA"() {
        setup:
        def v = new BlobServiceSasSignatureValues()
            .setPermissions(new BlobSasPermission())
            .setExpiryTime(OffsetDateTime.now())
            .setResource(containerName)
            .setCanonicalName(blobName)
            .setSnapshotId("2018-01-01T00:00:00.0000000Z")
            .setVersion(version)

        when:
        v.generateSasQueryParameters((SharedKeyCredential)creds)

        then:
        def e = thrown(NullPointerException)
        e.getMessage().contains(parameter)

        where:
        containerName | version | creds             | blobName || parameter
        "c"           | null    | primaryCredential | "b"       | "version"
        "c"           | "v"     | null              | "b"       | "sharedKeyCredentials"
        "c"           | "v"     | primaryCredential | null      | "canonicalName"
    }

    @Unroll
    def "BlobSASPermissions toString"() {
        setup:
        BlobSasPermission perms = new BlobSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setAddPermission(add)

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
        BlobSasPermission perms = BlobSasPermission.parse(permString)

        then:
        perms.getReadPermission() == read
        perms.getWritePermission() == write
        perms.getDeletePermission() == delete
        perms.getCreatePermission() == create
        perms.getAddPermission() == add

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
        BlobSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "ContainerSASPermissions toString"() {
        setup:
        BlobContainerSasPermission perms = new BlobContainerSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setAddPermission(add)
            .setListPermission(list)

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
        BlobContainerSasPermission perms = BlobContainerSasPermission.parse(permString)

        then:
        perms.getReadPermission() == read
        perms.getWritePermission() == write
        perms.getDeletePermission() == delete
        perms.getCreatePermission() == create
        perms.getAddPermission() == add
        perms.getListPermission() == list

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
        BlobContainerSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "IPRange toString"() {
        setup:
        def ip = new IpRange()
            .setIpMin(min)
            .setIpMax(max)

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
        IpRange ip = IpRange.parse(rangeStr)

        then:
        ip.getIpMin() == min
        ip.getIpMax() == max

        where:
        rangeStr || min | max
        "a-b"    || "a" | "b"
        "a"      || "a" | null
        ""       || ""  | null
    }

    @Unroll
    def "SASProtocol parse"() {
        expect:
        SasProtocol.parse(protocolStr) == protocol

        where:
        protocolStr  || protocol
        "https"      || SasProtocol.HTTPS_ONLY
        "https,http" || SasProtocol.HTTPS_HTTP
    }

    /*
     This test will ensure that each field gets placed into the proper location within the string to sign and that null
     values are handled correctly. We will validate the whole SAS with service calls as well as correct serialization of
     individual parts later.
     */

    @Unroll
    def "accountSasSignatures string to sign"() {
        when:
        AccountSasSignatureValues v = new AccountSasSignatureValues()
            .setPermissions(new AccountSasPermission().setReadPermission(true).toString())
            .setServices("b")
            .setResourceTypes("o")
            .setStartTime(startTime)
            .setExpiryTime(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
            .setProtocol(protocol)

        if (ipRange != null) {
            v.setIpRange(new IpRange().setIpMin("ip"))
        }

        def token = v.generateSasQueryParameters(primaryCredential)

        expectedStringToSign = String.format(expectedStringToSign, primaryCredential.getAccountName())

        then:
        token.getSignature() == primaryCredential.computeHmac256(expectedStringToSign)

        where:
        startTime                                                 | ipRange       | protocol               || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null          | null                   || "%s\nr\nb\no\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
        null                                                      | new IpRange() | null                   || "%s\nr\nb\no\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
        null                                                      | null          | SasProtocol.HTTPS_ONLY || "%s\nr\nb\no\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
    }

    @Unroll
    def "accountSasSignatureValues IA"() {
        setup:
        AccountSasSignatureValues v = new AccountSasSignatureValues()
            .setPermissions(permissions)
            .setServices(service)
            .setResourceTypes(resourceType)
            .setExpiryTime(expiryTime)
            .setVersion(version)

        when:
        v.generateSasQueryParameters(creds)

        then:
        def e = thrown(NullPointerException)
        e.getMessage().contains(parameter)

        where:
        permissions | service | resourceType | expiryTime           | version | creds             || parameter
        null        | "b"     | "c"          | OffsetDateTime.now() | "v"     | primaryCredential || "permissions"
        "c"         | null    | "c"          | OffsetDateTime.now() | "v"     | primaryCredential || "services"
        "c"         | "b"     | null         | OffsetDateTime.now() | "v"     | primaryCredential || "resourceTypes"
        "c"         | "b"     | "c"          | null                 | "v"     | primaryCredential || "expiryTime"
        "c"         | "b"     | "c"          | OffsetDateTime.now() | null    | primaryCredential || "version"
        "c"         | "b"     | "c"          | OffsetDateTime.now() | "v"     | null              || "SharedKeyCredential"
    }

    @Unroll
    def "AccountSASPermissions toString"() {
        setup:
        AccountSasPermission perms = new AccountSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setListPermission(list)
            .setAddPermission(add)
            .setCreatePermission(create)
            .setUpdatePermission(update)
            .setProcessMessages(process)

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
        AccountSasPermission perms = AccountSasPermission.parse(permString)

        then:
        perms.getReadPermission() == read
        perms.getWritePermission() == write
        perms.getDeletePermission() == delete
        perms.getListPermission() == list
        perms.getAddPermission() == add
        perms.getCreatePermission() == create
        perms.getUpdatePermission() == update
        perms.getProcessMessages() == process

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
        AccountSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "AccountSASResourceType toString"() {
        setup:
        AccountSasResourceType resourceTypes = new AccountSasResourceType()
            .setService(service)
            .setContainer(container)
            .setObject(object)

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
        AccountSasResourceType resourceTypes = AccountSasResourceType.parse(resourceTypeString)

        then:
        resourceTypes.isService() == service
        resourceTypes.isContainer() == container
        resourceTypes.isObject() == object

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
        AccountSasResourceType.parse("scq")

        then:
        thrown(IllegalArgumentException)
    }

    def "BlobURLParts"() {
        setup:
        BlobUrlParts parts = new BlobUrlParts()
            .setScheme("http")
            .setHost("host")
            .setContainerName("container")
            .setBlobName("blob")
            .setSnapshot "snapshot"

        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues()
            .setExpiryTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1))
            .setPermissions(new BlobSasPermission().setReadPermission(true))
            .setCanonicalName(String.format("/blob/%s/container/blob", primaryCredential.getAccountName()))
            .setResource(Constants.UrlConstants.SAS_BLOB_SNAPSHOT_CONSTANT)

        parts.setSasQueryParameters(sasValues.generateSasQueryParameters(primaryCredential))

        when:
        String[] splitParts = parts.toURL().toString().split("\\?")

        then:
        splitParts.size() == 2 // Ensure that there is only one question mark even when sas and snapshot are present
        splitParts[0] == "http://host/container/blob"
        splitParts[1].contains("snapshot=snapshot")
        splitParts[1].contains("sp=r")
        splitParts[1].contains("sig=")
        splitParts[1].split("&").size() == 6 // snapshot & sv & sr & sp & sig & se
    }

    def "BlobURLParts implicit root"() {
        when:
        def bup = new BlobUrlParts()
            .setScheme("http")
            .setHost("host")
            .setBlobName("blob")

        then:
        new BlobUrlParts().parse(bup.toURL()).getBlobContainerName() == BlobContainerAsyncClient.ROOT_CONTAINER_NAME
    }

    def "URLParser"() {
        when:
        BlobUrlParts parts = BlobUrlParts.parse(new URL("http://host/container/blob?snapshot=snapshot&sv=" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "&sr=c&sp=r&sig=Ee%2BSodSXamKSzivSdRTqYGh7AeMVEk3wEoRZ1yzkpSc%3D"))

        then:
        parts.getScheme() == "http"
        parts.getHost() == "host"
        parts.getBlobContainerName() == "container"
        parts.getBlobName() == "blob"
        parts.getSnapshot() == "snapshot"
        parts.getSasQueryParameters().getPermissions() == "r"
        parts.getSasQueryParameters().getVersion() == Constants.HeaderConstants.TARGET_STORAGE_VERSION
        parts.getSasQueryParameters().getResource() == "c"
        parts.getSasQueryParameters().getSignature() == Utility.urlDecode("Ee%2BSodSXamKSzivSdRTqYGh7AeMVEk3wEoRZ1yzkpSc%3D")
    }
}
