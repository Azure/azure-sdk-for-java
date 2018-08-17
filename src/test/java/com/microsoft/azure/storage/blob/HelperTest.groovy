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
import com.microsoft.azure.storage.blob.models.AccessPolicy
import com.microsoft.azure.storage.blob.models.SignedIdentifier
import com.microsoft.azure.storage.blob.models.StorageErrorCode
import com.microsoft.azure.storage.blob.models.StorageServiceProperties
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

    def "serviceSASSignatureValues network test blob"() {
        setup:
        def containerName = generateContainerName()
        def blobName = generateBlobName()
        def cu = primaryServiceURL.createContainerURL(containerName)
        cu.create(null, null).blockingGet()

        def v = new ServiceSASSignatureValues()
        def p = new BlobSASPermission()
        p.read = true
        p.write = true
        p.create = true
        p.delete = true
        p.add = true
        v.permissions = p.toString();
        v.startTime = OffsetDateTime.now().minusDays(1)
        v.expiryTime = OffsetDateTime.now().plusDays(1)
        v.containerName = containerName
        v.blobName = blobName
        def ipR = new IPRange()
        ipR.ipMin = "0.0.0.0"
        ipR.ipMax = "255.255.255.255"
        v.ipRange = ipR
        v.protocol = SASProtocol.HTTPS_ONLY
        v.cacheControl = "cache"
        v.contentDisposition = "disposition"
        v.contentEncoding = "encoding"
        v.contentLanguage = "language"
        v.contentType = "type"

        when:
        def parts = URLParser.parse(cu.createBlobURL(blobName).toURL())
        parts.sasQueryParameters = v.generateSASQueryParameters(primaryCreds)
        parts.scheme = "https"
        def bu = new AppendBlobURL(parts.toURL(), StorageURL.createPipeline(new AnonymousCredentials(),
                new PipelineOptions()))

        then:
        bu.create(null, null, null).blockingGet()

        and:
        def properties = bu.getProperties(null).blockingGet().headers()

        then:
        properties.cacheControl() == "cache"
        properties.contentDisposition() == "disposition"
        properties.contentEncoding() == "encoding"
        properties.contentLanguage() == "language"
        properties.contentType() == "type"
    }

    def "serviceSASSignatureValues network test container"(){
        setup:
        def containerName = generateContainerName()
        def cu = primaryServiceURL.createContainerURL(containerName)
        cu.create(null, null).blockingGet()
        def id = new SignedIdentifier().withId("0000").withAccessPolicy(new AccessPolicy().withPermission("racwdl")
                .withExpiry(OffsetDateTime.now().plusDays(1)))
        cu.setAccessPolicy(null, Arrays.asList(id), null).blockingGet()

        // Check id field
        def v = new ServiceSASSignatureValues()
        v.identifier = "0000"
        v.containerName = containerName
        v.protocol = SASProtocol.HTTPS_ONLY

        // Check containerSASPermissions
        def v2 = new ServiceSASSignatureValues()
        def p = new ContainerSASPermission()
        p.read = true
        p.write = true
        p.create = true
        p.delete = true
        p.add = true
        p.list = true
        v2.permissions = p.toString();
        v2.expiryTime = OffsetDateTime.now().plusDays(1)
        v2.containerName = containerName

        when:
        def parts = URLParser.parse(cu.toURL())
        parts.sasQueryParameters = v.generateSASQueryParameters(primaryCreds)
        parts.scheme = "https"
        def cuSAS = new ContainerURL(parts.toURL(), StorageURL.createPipeline(new AnonymousCredentials(),
                new PipelineOptions()))

        parts.sasQueryParameters = v2.generateSASQueryParameters(primaryCreds)
        def cuSAS2 = new ContainerURL(parts.toURL(), StorageURL.createPipeline(new AnonymousCredentials(),
                new PipelineOptions()))

        then:
        cuSAS.listBlobsFlatSegment(null, null).blockingGet()
        cuSAS2.listBlobsFlatSegment(null, null).blockingGet()
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
        p.read = true
        v.permissions = p.toString();
        v.services = "b"
        v.resourceTypes = "o"
        v.startTime = startTime
        v.expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        if (ipRange != null) {
            def ipR = new IPRange()
            ipR.ipMin = "ip"
            v.ipRange = ipR
        }
        v.protocol = protocol

        def token = v.generateSASQueryParameters(primaryCreds)

        then:
        token.signature == primaryCreds.computeHmac256(expectedStringToSign)

        where:
        startTime                                                 | ipRange       | protocol               || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null          | null                   || primaryCreds.getAccountName() + "\nr\nb\no\n" + Utility.ISO8601UTCDateFormatter.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Utility.ISO8601UTCDateFormatter.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
        null                                                      | new IPRange() | null                   || primaryCreds.getAccountName() + "\nr\nb\no\n\n" + Utility.ISO8601UTCDateFormatter.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
        null                                                      | null          | SASProtocol.HTTPS_ONLY || primaryCreds.getAccountName() + "\nr\nb\no\n\n" + Utility.ISO8601UTCDateFormatter.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n" + SASProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
    }

    @Unroll
    def "accountSasSignatureValues IA"() {
        setup:
        def v = new AccountSASSignatureValues()
        v.permissions = permissions
        v.services = service
        v.resourceTypes = resourceType
        v.expiryTime = expiryTime
        v.version = version

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
        perms.read = read
        perms.write = write
        perms.delete = delete
        perms.list = list
        perms.add = add
        perms.create = create
        perms.update = update
        perms.processMessages = process

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
        perms.read == read
        perms.write == write
        perms.delete == delete
        perms.list == list
        perms.add == add
        perms.create == create
        perms.update == update
        perms.processMessages == process

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
        resourceTypes.service = service
        resourceTypes.container = container
        resourceTypes.object = object

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
        resourceTypes.service == service
        resourceTypes.container == container
        resourceTypes.object == object

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
        parts.scheme = "http"
        parts.host = "host"
        parts.containerName = "container"
        parts.blobName = "blob"
        parts.snapshot = "snapshot"
        def sasValues = new ServiceSASSignatureValues()
        sasValues.permissions = "r"
        sasValues.containerName = "container"
        parts.sasQueryParameters = sasValues.generateSASQueryParameters(primaryCreds)

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
        def parts = URLParser.parse(new URL("http://host/container/blob?snapshot=snapshot&sv=2018-03-28&sr=c&sp=r&sig=Ee%2BSodSXamKSzivSdRTqYGh7AeMVEk3wEoRZ1yzkpSc%3D"))

        then:
        parts.scheme == "http"
        parts.host == "host"
        parts.containerName == "container"
        parts.blobName == "blob"
        parts.snapshot == "snapshot"
        parts.sasQueryParameters.getPermissions() == "r"
        parts.sasQueryParameters.getVersion() == Constants.HeaderConstants.TARGET_STORAGE_VERSION
        parts.sasQueryParameters.getResource() == "c"
        parts.sasQueryParameters.getSignature() ==
                Utility.safeURLDecode("Ee%2BSodSXamKSzivSdRTqYGh7AeMVEk3wEoRZ1yzkpSc%3D")
    }
}
