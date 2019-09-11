// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob


import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.UserDelegationKey
import com.azure.storage.common.AccountSASPermission
import com.azure.storage.common.AccountSASResourceType
import com.azure.storage.common.AccountSASSignatureValues
import com.azure.storage.common.Constants
import com.azure.storage.common.IPRange
import com.azure.storage.common.SASProtocol
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
        response.request() != null
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
        BlobServiceSASSignatureValues v = new BlobServiceSASSignatureValues()
        if (permissions != null) {
            v.setPermissions(new BlobSASPermission().setRead(true).toString())
        } else {
            v.setPermissions("")
        }

        if (snapId != null) {
            v.setResource(Constants.UrlConstants.SAS_BLOB_SNAPSHOT_CONSTANT)
        } else {
            v.setResource(Constants.UrlConstants.SAS_BLOB_CONSTANT)
        }

        v.setStartTime(startTime)
            .setCanonicalName(String.format("/blob/%s/containerName/blobName", primaryCredential.accountName()))
            .setSnapshotId(snapId)

        if (expiryTime == null) {
            v.setExpiryTime(OffsetDateTime.now())
        } else {
            v.setExpiryTime(expiryTime)
        }

        if (ipRange != null) {
            v.setIpRange(new IPRange().ipMin("ip"))
        }

        v.setIdentifier(identifier)
            .setProtocol(protocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(disposition)
            .setContentEncoding(encoding)
            .setContentLanguage(language)
            .setContentType(type)

        BlobServiceSASQueryParameters token = v.generateSASQueryParameters(primaryCredential)

        if (startTime != null) {
            expectedStringToSign = String.format(expectedStringToSign,
                Utility.ISO_8601_UTC_DATE_FORMATTER.format(startTime),
                Utility.ISO_8601_UTC_DATE_FORMATTER.format(expiryTime),
                primaryCredential.accountName())
        } else {
            expectedStringToSign = String.format(expectedStringToSign,
                Utility.ISO_8601_UTC_DATE_FORMATTER.format(expiryTime),
                primaryCredential.accountName())
        }

        then:
        token.signature() == primaryCredential.computeHmac256(expectedStringToSign)

        /*
        We don't test the blob or containerName properties because canonicalized resource is always added as at least
        /blob/accountName. We test canonicalization of resources later. Again, this is not to test a fully functional
        sas but the construction of the string to sign.
        Signed resource is tested elsewhere, as we work some minor magic in choosing which value to use.
         */
        where:
        permissions             | startTime                                             | expiryTime                                           | identifier | ipRange       | protocol               | snapId   | cacheControl | disposition   | encoding   | language   | type   || expectedStringToSign
        new BlobSASPermission() | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n\n%s\n" + "/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | OffsetDateTime.now(ZoneOffset.UTC).minusDays(1) | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | null         | null          | null       | null       | null   || "\n%s\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | "id"       | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\nid\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | new IPRange() | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | SASProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n" + SASProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | "snapId" | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nbs\nsnapId\n\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | "control"    | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | null         | "disposition" | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | null         | null          | "encoding" | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | null         | null          | null       | "language" | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                    | null                                                  | OffsetDateTime.now(ZoneOffset.UTC).plusDays(1) | null       | null          | null                   | null     | null         | null          | null       | null       | "type" || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

    @Unroll
    def "serviceSasSignatures string to sign user delegation key"() {
        when:
        BlobServiceSASSignatureValues v = new BlobServiceSASSignatureValues()
        if (permissions != null) {
            v.setPermissions(new BlobSASPermission().setRead(true).toString())
        } else {
            v.setPermissions("")
        }

        v.setStartTime(startTime)
            .setCanonicalName(String.format("/blob/%s/containerName/blobName", primaryCredential.accountName()))
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
            v.setIpRange(new IPRange().ipMin("ip"))
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

        BlobServiceSASQueryParameters token = v.generateSASQueryParameters(key)

        expectedStringToSign = String.format(expectedStringToSign, Utility.ISO_8601_UTC_DATE_FORMATTER.format(v.getExpiryTime()), primaryCredential.accountName())

        then:
        token.signature() == Utility.computeHMac256(key.getValue(), expectedStringToSign)

        /*
        We test string to sign functionality directly related to user delegation sas specific parameters
         */
        where:
        permissions             | startTime                                                 | expiryTime                                                | keyOid                                 | keyTid                                 | keyStart                                                              | keyExpiry                                                             | keyService | keyVersion   | keyValue                                       | ipRange       | protocol               | snapId   | cacheControl | disposition   | encoding   | language   | type   || expectedStringToSign
        new BlobSASPermission() | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "r\n\n%s\n" + "/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | "11111111-1111-1111-1111-111111111111" | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | "22222222-2222-2222-2222-222222222222" | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | "b"        | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\nb\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | "2018-06-17" | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n2018-06-17\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | new IPRange() | null                   | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | SASProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n" + SASProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | "snapId" | null         | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nbs\nsnapId\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | "control"    | null          | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | "disposition" | null       | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | "encoding" | null       | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | "language" | null   || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                    | null                                                      | null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null          | null                   | null     | null         | null          | null       | null       | "type" || "\n\n%s\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

    @Unroll
    def "serviceSASSignatureValues canonicalizedResource"() {
        setup:
        BlobServiceSASSignatureValues v = new BlobServiceSASSignatureValues()
            .setExpiryTime(expiryTime)
            .setPermissions(new BlobSASPermission().toString())
            .setResource(expectedResource)
            .setCanonicalName(String.format("/blob/%s/%s", primaryCredential.accountName(), containerName))
            .setSnapshotId(snapId)

        if (blobName != null) {
            v.setCanonicalName(v.getCanonicalName() + "/" + blobName)
        }

        expectedStringToSign = String.format(expectedStringToSign,
            Utility.ISO_8601_UTC_DATE_FORMATTER.format(expiryTime),
            primaryCredential.accountName())

        when:
        BlobServiceSASQueryParameters token = v.generateSASQueryParameters(primaryCredential)

        then:
        token.signature() == primaryCredential.computeHmac256(expectedStringToSign)
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
        BlobServiceSASSignatureValues v = new BlobServiceSASSignatureValues()
            .setPermissions(new AccountSASPermission().toString())
            .setExpiryTime(OffsetDateTime.now())
            .setResource(containerName)
            .setCanonicalName(blobName)
            .setSnapshotId("2018-01-01T00:00:00.0000000Z")
            .setVersion(version)

        when:
        v.generateSASQueryParameters((SharedKeyCredential)creds)

        then:
        def e = thrown(IllegalArgumentException)
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
        BlobSASPermission perms = new BlobSASPermission()
            .setRead(read)
            .setWrite(write)
            .setDelete(delete)
            .setCreate(create)
            .setAdd(add)

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
        BlobSASPermission perms = BlobSASPermission.parse(permString)

        then:
        perms.getRead() == read
        perms.getWrite() == write
        perms.getDelete() == delete
        perms.getCreate() == create
        perms.getAdd() == add

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
        ContainerSASPermission perms = new ContainerSASPermission()
            .setRead(read)
            .setWrite(write)
            .setDelete(delete)
            .setCreate(create)
            .setAdd(add)
            .setList(list)

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
        ContainerSASPermission perms = ContainerSASPermission.parse(permString)

        then:
        perms.getRead() == read
        perms.getWrite() == write
        perms.getDelete() == delete
        perms.getCreate() == create
        perms.getAdd() == add
        perms.getList() == list

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
        IPRange ip = IPRange.parse(rangeStr)

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
        AccountSASSignatureValues v = new AccountSASSignatureValues()
            .setPermissions(new AccountSASPermission().setRead(true).toString())
            .setServices("b")
            .setResourceTypes("o")
            .setStartTime(startTime)
            .setExpiryTime(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
            .setProtocol(protocol)

        if (ipRange != null) {
            v.setIpRange(new IPRange().ipMin("ip"))
        }

        def token = v.generateSASQueryParameters(primaryCredential)

        expectedStringToSign = String.format(expectedStringToSign, primaryCredential.accountName())

        then:
        token.signature() == primaryCredential.computeHmac256(expectedStringToSign)

        where:
        startTime                                                 | ipRange       | protocol               || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null          | null                   || "%s\nr\nb\no\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
        null                                                      | new IPRange() | null                   || "%s\nr\nb\no\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
        null                                                      | null          | SASProtocol.HTTPS_ONLY || "%s\nr\nb\no\n\n" + Utility.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n" + SASProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
    }

    @Unroll
    def "accountSasSignatureValues IA"() {
        setup:
        AccountSASSignatureValues v = new AccountSASSignatureValues()
            .setPermissions(permissions)
            .setServices(service)
            .setResourceTypes(resourceType)
            .setExpiryTime(expiryTime)
            .setVersion(version)

        when:
        v.generateSASQueryParameters(creds)

        then:
        def e = thrown(IllegalArgumentException)
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
        AccountSASPermission perms = new AccountSASPermission()
            .setRead(read)
            .setWrite(write)
            .setDelete(delete)
            .setList(list)
            .setAdd(add)
            .setCreate(create)
            .setUpdate(update)
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
        AccountSASPermission perms = AccountSASPermission.parse(permString)

        then:
        perms.getRead() == read
        perms.getWrite() == write
        perms.getDelete() == delete
        perms.getList() == list
        perms.getAdd() == add
        perms.getCreate() == create
        perms.getUpdate() == update
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
        AccountSASPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "AccountSASResourceType toString"() {
        setup:
        AccountSASResourceType resourceTypes = new AccountSASResourceType()
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
        AccountSASResourceType resourceTypes = AccountSASResourceType.parse(resourceTypeString)

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
        BlobURLParts parts = new BlobURLParts()
            .setScheme("http")
            .setHost("host")
            .setContainerName("container")
            .setBlobName("blob")
            .setSnapshot("snapshot")

        BlobServiceSASSignatureValues sasValues = new BlobServiceSASSignatureValues()
            .setExpiryTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1))
            .setPermissions("r")
            .setCanonicalName(String.format("/blob/%s/container/blob", primaryCredential.accountName()))
            .setResource(Constants.UrlConstants.SAS_BLOB_SNAPSHOT_CONSTANT)

        parts.setSasQueryParameters(sasValues.generateSASQueryParameters(primaryCredential))

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

    def "URLParser"() {
        when:
        BlobURLParts parts = URLParser.parse(new URL("http://host/container/blob?snapshot=snapshot&sv=" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "&sr=c&sp=r&sig=Ee%2BSodSXamKSzivSdRTqYGh7AeMVEk3wEoRZ1yzkpSc%3D"))

        then:
        parts.getScheme() == "http"
        parts.getHost() == "host"
        parts.getContainerName() == "container"
        parts.getBlobName() == "blob"
        parts.getSnapshot() == "snapshot"
        parts.getSasQueryParameters().permissions() == "r"
        parts.getSasQueryParameters().getKeyVersion() == Constants.HeaderConstants.TARGET_STORAGE_VERSION
        parts.getSasQueryParameters().getResource() == "c"
        parts.getSasQueryParameters().signature() == Utility.urlDecode("Ee%2BSodSXamKSzivSdRTqYGh7AeMVEk3wEoRZ1yzkpSc%3D")
    }
}
