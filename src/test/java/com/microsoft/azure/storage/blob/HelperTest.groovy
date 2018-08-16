/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage.blob

import com.microsoft.azure.storage.APISpec
import com.microsoft.azure.storage.blob.models.StorageErrorCode
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.ZoneOffset

class HelperTest extends APISpec {

    def "responseError"() {
        when:
        cu.listBlobsFlatSegment("garbage", null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.INVALID_QUERY_PARAMETER_VALUE
        e.statusCode() == 400
        e.message().contains("Value for one of the query parameters specified in the request URI is invalid.")
        e.getMessage().contains("<?xml") // Ensure that the details in the payload are printable
    }

    @Unroll
    def "Blob range"() {
        expect:
        new BlobRange(offset, count).toString() == result

        where:
        offset | count || result
        0      | null  || "bytes=0-"
        0      | 5     || "bytes=0-4"
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
        def v = new ServiceSASSignatureValues()
        if (permissions != null) {
            def p = new BlobSASPermission()
            p.read = true
            v.permissions = p.toString();
        }
        v.startTime = startTime
        v.expiryTime = expiryTime
        v.containerName = "c"
        if (ipRange != null) {
            def ipR = new IPRange()
            ipR.ipMin = "ip"
            v.ipRange = ipR
        }
        v.identifier = identifier
        v.protocol = protocol
        v.cacheControl = cacheControl
        v.contentDisposition = disposition
        v.contentEncoding = encoding
        v.contentLanguage = language
        v.contentType = type

        def token = v.generateSASQueryParameters(primaryCreds)

        then:
        token.signature == primaryCreds.computeHmac256(expectedStringToSign)

        /*
        We don't test the blob or containerName properties because canonicalized resource is always added as at least
        /blob/accountName. We test canonicalization of resources later. Again, this is not to test a fully functional
        sas but the construction of the string to sign.
         */
        where:
        permissions             | startTime                                                 | expiryTime                                                | identifier | ipRange       | protocol               | cacheControl | disposition   | encoding   | language   | type   || expectedStringToSign
        new BlobSASPermission() | null                                                      | null                                                      | null       | null          | null                   | null         | null          | null       | null       | null   || "r\n\n\n" + "/blob/" + primaryCreds.getAccountName() + "/c\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n"
        null                    | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                                      | null       | null          | null                   | null         | null          | null       | null       | null   || "\n" + Utility.ISO8601UTCDateFormatter.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n/blob/" + primaryCreds.getAccountName() + "/c\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n"
        null                    | null                                                      | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null       | null          | null                   | null         | null          | null       | null       | null   || "\n\n" + Utility.ISO8601UTCDateFormatter.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/" + primaryCreds.getAccountName() + "/c\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n"
        null                    | null                                                      | null                                                      | "id"       | null          | null                   | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/c\nid\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null       | new IPRange() | null                   | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/c\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null       | null          | SASProtocol.HTTPS_ONLY | null         | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/c\n\n\n" + SASProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n"
        null                    | null                                                      | null                                                      | null       | null          | null                   | "control"    | null          | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/c\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\ncontrol\n\n\n\n"
        null                    | null                                                      | null                                                      | null       | null          | null                   | null         | "disposition" | null       | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/c\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\ndisposition\n\n\n"
        null                    | null                                                      | null                                                      | null       | null          | null                   | null         | null          | "encoding" | null       | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/c\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\nencoding\n\n"
        null                    | null                                                      | null                                                      | null       | null          | null                   | null         | null          | null       | "language" | null   || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/c\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\nlanguage\n"
        null                    | null                                                      | null                                                      | null       | null          | null                   | null         | null          | null       | null       | "type" || "\n\n\n/blob/" + primaryCreds.getAccountName() + "/c\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\ntype"
    }

    @Unroll
    def "serviceSASSignatureValues canonicalizedResource"() {
        setup:
        def v = new ServiceSASSignatureValues()
        v.containerName = containerName
        v.blobName = blobName

        when:
        def token = v.generateSASQueryParameters(primaryCreds)

        then:
        token.signature == primaryCreds.computeHmac256(expectedStringToSign)
        token.getResource() == expectedResource

        where:
        containerName | blobName || expectedResource | expectedStringToSign
        "c"           | "b"      || "b"              | "\n\n\n" + "/blob/" + primaryCreds.getAccountName() + "/c/b\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n"
        "c"           | null     || "c"              | "\n\n\n" + "/blob/" + primaryCreds.getAccountName() + "/c\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n\n\n\n\n"

    }

    @Unroll
    def "serviceSasSignatureValues IA"() {
        setup:
        def v = new ServiceSASSignatureValues()
        v.containerName = containerName
        v.version = version

        when:
        v.generateSASQueryParameters(creds)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage().contains(parameter)

        where:
        containerName | version | creds       || parameter
        null          | "v"     | primaryCreds | "container"
        "c"           | null    | primaryCreds | "version"
        "c"           | "v"     | null         | "sharedKeyCredentials"
    }

    @Unroll
    def "BlobSASPermissions toString"() {
        setup:
        def perms = new BlobSASPermission()
        perms.read = read
        perms.write = write
        perms.delete = delete
        perms.create = create
        perms.add = add

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
        perms.read == read
        perms.write == write
        perms.delete == delete
        perms.create == create
        perms.add == add

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
        perms.read = read
        perms.write = write
        perms.delete = delete
        perms.create = create
        perms.add = add
        perms.list = list

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
        perms.read == read
        perms.write == write
        perms.delete == delete
        perms.create == create
        perms.add == add
        perms.list == list

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
        ip.ipMin = min
        ip.ipMax = max

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
        ip.ipMin == min
        ip.ipMax == max

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

    def "accountSasSignatures"() {
        when:
        def v = new AccountSASSignatureValues()
        v.expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        v.protocol = SASProtocol.HTTPS_ONLY
        def p = new ContainerSASPermission()
        p.read = true
        v.permissions = p.toString()
        def s = new AccountSASService()
        s.blob = true
        v.services = s.toString()
        def t = new AccountSASResourceType()
        t.container = true
        t.object = true
        v.resourceTypes = t.toString()
        def expectedStringToSign = "${primaryCreds.accountName}\n${p.toString()}\n${v.services}\n${v.resourceTypes}\n\n${Utility.ISO8601UTCDateFormatter.format(v.expiryTime)}\n${IPRange.DEFAULT.toString()}\n${SASProtocol.HTTPS_ONLY}\n${v.version}\n"
        def token = v.generateSASQueryParameters(primaryCreds)

        then:
        token.signature == primaryCreds.computeHmac256(expectedStringToSign)

    }

    def "AccountSASPermissions"() {

    }
}
