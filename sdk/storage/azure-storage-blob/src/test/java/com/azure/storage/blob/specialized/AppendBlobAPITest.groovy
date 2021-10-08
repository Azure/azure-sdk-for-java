// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.core.exception.UnexpectedLengthException
import com.azure.core.util.Context
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobServiceVersion
import com.azure.storage.blob.options.AppendBlobCreateOptions
import com.azure.storage.blob.models.AppendBlobRequestConditions
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.options.AppendBlobSealOptions
import com.azure.storage.blob.options.BlobGetTagsOptions
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import spock.lang.IgnoreIf
import spock.lang.Unroll

import java.security.MessageDigest

class AppendBlobAPITest extends APISpec {
    AppendBlobClient bc
    String blobName

    def setup() {
        blobName = generateBlobName()
        bc = cc.getBlobClient(blobName).getAppendBlobClient()
        bc.create()
    }

    def "Create defaults"() {
        when:
        def createResponse = bc.createWithResponse(null, null, null, null, null)

        then:
        createResponse.getStatusCode() == 201
        validateBasicHeaders(createResponse.getHeaders())
        createResponse.getValue().getContentMd5() == null
        createResponse.getValue().isServerEncrypted()
    }

    def "Create min"() {
        expect:
        bc.createWithResponse(null, null, null, null, null).getStatusCode() == 201
    }

    def "Create error"() {
        when:
        bc.createWithResponse(null, null, new BlobRequestConditions().setIfMatch("garbage"), null, Context.NONE)

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Create headers"() {
        setup:
        def headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

        when:
        bc.createWithResponse(headers, null, null, null, null)
        def response = bc.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType

        then:
        validateBlobProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                                       | contentType
        null         | null               | null            | null            | null                                                                                             | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(data.defaultText.getBytes())) | "type"
    }

    @Unroll
    def "Create metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bc.createWithResponse(null, metadata, null, null, Context.NONE)
        def response = bc.getProperties()

        then:
        response.getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Create tags"() {
        setup:
        def tags = new HashMap<String, String>()
        if (key1 != null) {
            tags.put(key1, value1)
        }
        if (key2 != null) {
            tags.put(key2, value2)
        }

        when:
        bc.createWithResponse(new AppendBlobCreateOptions().setTags(tags), null, Context.NONE)
        def response = bc.getTagsWithResponse(new BlobGetTagsOptions(), null, null)

        then:
        response.getValue() == tags

        where:
        key1                | value1     | key2   | value2
        null                | null       | null   | null
        "foo"               | "bar"      | "fizz" | "buzz"
        " +-./:=_  +-./:=_" | " +-./:=_" | null   | null
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Create AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        expect:
        bc.createWithResponse(null, null, bac, null, null).getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | tags
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Create AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)

        when:
        bc.createWithResponse(null, null, bac, null, Context.NONE)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null         | null        | null           | "\"notfoo\" = 'notbar'"
    }

    def "Append block defaults"() {
        setup:
        def appendResponse = bc.appendBlockWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null,
            null)

        when:
        def downloadStream = new ByteArrayOutputStream()
        bc.download(downloadStream)

        then:
        downloadStream.toByteArray() == data.defaultBytes
        validateBasicHeaders(appendResponse.getHeaders())
        appendResponse.getHeaders().getValue("x-ms-content-crc64") != null
        appendResponse.getValue().getBlobAppendOffset() != null
        appendResponse.getValue().getBlobCommittedBlockCount() != null

        expect:
        bc.getProperties().getCommittedBlockCount() == 1
    }

    def "Append block min"() {
        bc.appendBlockWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null,
            null).getStatusCode() == 201
    }

    @Unroll
    def "Append block IA"() {
        when:
        bc.appendBlock(stream, dataSize)

        then:
        thrown(exceptionType)

        where:
        stream                   | dataSize                 | exceptionType
        null                     | data.defaultDataSize     | NullPointerException
        data.defaultInputStream | data.defaultDataSize + 1 | UnexpectedLengthException
        data.defaultInputStream | data.defaultDataSize - 1 | UnexpectedLengthException
    }

    def "Append block empty body"() {
        when:
        bc.appendBlock(new ByteArrayInputStream(new byte[0]), 0)

        then:
        thrown(BlobStorageException)
    }

    def "Append block null body"() {
        when:
        bc.appendBlock(new ByteArrayInputStream(null), 0)

        then:
        thrown(NullPointerException)
    }

    def "Append block transactionalMD5"() {
        setup:
        byte[] md5 = MessageDigest.getInstance("MD5").digest(data.defaultBytes)

        expect:
        bc.appendBlockWithResponse(data.defaultInputStream, data.defaultDataSize, md5, null, null, null).statusCode == 201
    }

    def "Append block transactionalMD5 fail"() {
        when:
        bc.appendBlockWithResponse(data.defaultInputStream, data.defaultDataSize,
            MessageDigest.getInstance("MD5").digest("garbage".getBytes()), null, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.MD5MISMATCH
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Append block AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)
            .setTagsConditions(tags)

        expect:
        bc.appendBlockWithResponse(data.defaultInputStream, data.defaultDataSize, null, bac, null, null)
            .getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | appendPosE | maxSizeLTE  | tags
        null     | null       | null         | null        | null            | null       | null        | null
        oldDate  | null       | null         | null        | null            | null       | null        | null
        null     | newDate    | null         | null        | null            | null       | null        | null
        null     | null       | receivedEtag | null        | null            | null       | null        | null
        null     | null       | null         | garbageEtag | null            | null       | null        | null
        null     | null       | null         | null        | receivedLeaseID | null       | null        | null
        null     | null       | null         | null        | null            | 0          | null        | null
        null     | null       | null         | null        | null            | null       | 100         | null
        null     | null       | null         | null        | null            | null       | null        | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Append block AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)

        def bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)
            .setTagsConditions(tags)

        when:
        bc.appendBlockWithResponse(data.defaultInputStream, data.defaultDataSize, null, bac, null, null)

        then:
        thrown(BlobStorageException)

        cleanup:
        data.defaultInputStream.reset()

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | appendPosE | maxSizeLTE | tags
        newDate  | null       | null        | null         | null           | null       | null       | null
        null     | oldDate    | null        | null         | null           | null       | null       | null
        null     | null       | garbageEtag | null         | null           | null       | null       | null
        null     | null       | null        | receivedEtag | null           | null       | null       | null
        null     | null       | null        | null         | garbageLeaseID | null       | null       | null
        null     | null       | null        | null         | null           | 1          | null       | null
        null     | null       | null        | null         | null           | null       | 1          | null
        null     | null       | null        | null         | null           | null       | null       | "\"notfoo\" = 'notbar'"
    }

    def "Append block error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getAppendBlobClient()

        when:
        bc.appendBlock(data.defaultInputStream, data.defaultDataSize)

        then:
        thrown(BlobStorageException)
    }

    def "Append block retry on transient failure"() {
        setup:
        def clientWithFailure = getBlobClient(
            environment.primaryAccount.credential,
            bc.getBlobUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        ).getAppendBlobClient()

        when:
        clientWithFailure.appendBlock(data.defaultInputStream, data.defaultDataSize)

        then:
        def os = new ByteArrayOutputStream()
        bc.download(os)
        os.toByteArray() == data.defaultBytes
    }

    def "Append block from URL min"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def data = getRandomByteArray(1024)
        bc.appendBlock(new ByteArrayInputStream(data), data.length)

        def destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        destURL.create()

        def blobRange = new BlobRange(0, (long) PageBlobClient.PAGE_BYTES)

        when:
        def response = destURL.appendBlockFromUrlWithResponse(bc.getBlobUrl(), blobRange, null, null, null, null, null)

        then:
        response.getStatusCode() == 201
        validateBasicHeaders(response.getHeaders())
    }

    def "Append block from URL range"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def data = getRandomByteArray(4 * 1024)
        bc.appendBlock(new ByteArrayInputStream(data), data.length)

        def destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        destURL.create()

        when:
        destURL.appendBlockFromUrl(bc.getBlobUrl(), new BlobRange(2 * 1024, 1024))

        then:
        def downloadStream = new ByteArrayOutputStream(1024)
        destURL.download(downloadStream)
        downloadStream.toByteArray() == Arrays.copyOfRange(data, 2 * 1024, 3 * 1024)
    }

    def "Append block from URL MD5"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def data = getRandomByteArray(1024)
        bc.appendBlock(new ByteArrayInputStream(data), data.length)

        def destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        destURL.create()

        when:
        destURL.appendBlockFromUrlWithResponse(bc.getBlobUrl(), null, MessageDigest.getInstance("MD5").digest(data),
            null, null, null, Context.NONE)

        then:
        notThrown(BlobStorageException)
    }

    def "Append block from URL MD5 fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def data = getRandomByteArray(1024)
        bc.appendBlock(new ByteArrayInputStream(data), data.length)

        def destURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        destURL.create()

        when:
        destURL.appendBlockFromUrlWithResponse(bc.getBlobUrl(), null, MessageDigest.getInstance("MD5").digest("garbage".getBytes()),
            null, null, null, Context.NONE)

        then:
        thrown(BlobStorageException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Append block from URL destination AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)
            .setTagsConditions(tags)

        def sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        sourceURL.create()
        sourceURL.appendBlockWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null, null)
            .getStatusCode()

        expect:
        bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null, null, bac, null, null, null).getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | appendPosE | maxSizeLTE  | tags
        null     | null       | null         | null        | null            | null       | null        | null
        oldDate  | null       | null         | null        | null            | null       | null        | null
        null     | newDate    | null         | null        | null            | null       | null        | null
        null     | null       | receivedEtag | null        | null            | null       | null        | null
        null     | null       | null         | garbageEtag | null            | null       | null        | null
        null     | null       | null         | null        | receivedLeaseID | null       | null        | null
        null     | null       | null         | null        | null            | 0          | null        | null
        null     | null       | null         | null        | null            | null       | 100         | null
        null     | null       | null         | null        | null            | null       | null        | "\"foo\" = 'bar'"
    }

    @Unroll
    def "Append block from URL AC destination fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)

        def bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)
            .setMaxSize(maxSizeLTE)
            .setTagsConditions(tags)

        def sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        sourceURL.create()
        sourceURL.appendBlockWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null, null)
            .getStatusCode()

        when:
        bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null, null, bac, null, null, Context.NONE)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | appendPosE | maxSizeLTE | tags
        newDate  | null       | null        | null         | null           | null       | null       | null
        null     | oldDate    | null        | null         | null           | null       | null       | null
        null     | null       | garbageEtag | null         | null           | null       | null       | null
        null     | null       | null        | receivedEtag | null           | null       | null       | null
        null     | null       | null        | null         | garbageLeaseID | null       | null       | null
        null     | null       | null        | null         | null           | 1          | null       | null
        null     | null       | null        | null         | null           | null       | 1          | null
        null     | null       | null        | null         | null           | null       | null       | "\"notfoo\" = 'notbar'"
    }

    @Unroll
    def "Append block from URL source AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        def sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        sourceURL.create()
        sourceURL.appendBlockWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null, null)
            .getStatusCode()

        def smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(setupBlobMatchCondition(sourceURL, sourceIfMatch))
            .setIfNoneMatch(sourceIfNoneMatch)

        expect:
        bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null, null, null, smac, null, null).getStatusCode() == 201

        where:
        sourceIfModifiedSince | sourceIfUnmodifiedSince | sourceIfMatch | sourceIfNoneMatch
        null                  | null                    | null          | null
        oldDate               | null                    | null          | null
        null                  | newDate                 | null          | null
        null                  | null                    | receivedEtag  | null
        null                  | null                    | null          | garbageEtag
    }

    @Unroll
    def "Append block from URL AC source fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        def sourceURL = cc.getBlobClient(generateBlobName()).getAppendBlobClient()
        sourceURL.create()
        sourceURL.appendBlockWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null, null)
            .getStatusCode()

        def smac = new BlobRequestConditions()
            .setIfModifiedSince(sourceIfModifiedSince)
            .setIfUnmodifiedSince(sourceIfUnmodifiedSince)
            .setIfMatch(sourceIfMatch)
            .setIfNoneMatch(setupBlobMatchCondition(sourceURL, sourceIfNoneMatch))

        when:
        bc.appendBlockFromUrlWithResponse(sourceURL.getBlobUrl(), null, null, null, smac, null, Context.NONE)

        then:
        thrown(BlobStorageException)

        where:
        sourceIfModifiedSince | sourceIfUnmodifiedSince | sourceIfMatch | sourceIfNoneMatch
        newDate               | null                    | null          | null
        null                  | oldDate                 | null          | null
        null                  | null                    | garbageEtag   | null
        null                  | null                    | null          | receivedEtag
    }

    def "Get Container Name"() {
        expect:
        containerName == bc.getContainerName()
    }

    def "Get Append Blob Name"() {
        expect:
        blobName == bc.getBlobName()
    }

    def "Create overwrite false"() {
        when:
        bc.create()

        then:
        thrown(BlobStorageException)
    }

    def "Create overwrite true"() {
        when:
        bc.create(true)

        then:
        notThrown(Throwable)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Seal defaults"() {
        when:
        def sealResponse = bc.sealWithResponse(null, null, null)

        then:
        sealResponse.getStatusCode() == 200
        sealResponse.getHeaders().getValue("x-ms-blob-sealed")
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Seal min"() {
        when:
        bc.seal()

        then:
        bc.getProperties().isSealed()
        bc.downloadWithResponse(new ByteArrayOutputStream(), null, null, null, false, null, null).getDeserializedHeaders().isSealed()
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "Seal error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName()).getAppendBlobClient()

        when:
        bc.seal()

        then:
        thrown(BlobStorageException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Seal AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)

        expect:
        bc.sealWithResponse(new AppendBlobSealOptions().setRequestConditions(bac), null, null)
            .getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | appendPosE
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | 0
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "Seal AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)

        def bac = new AppendBlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setAppendPosition(appendPosE)

        when:
        bc.sealWithResponse(new AppendBlobSealOptions().setRequestConditions(bac), null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | appendPosE
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null         | null           | 1
    }

    @IgnoreIf( { getEnvironment().serviceVersion != null } )
    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        setup:
        def specialBlob = getSpecializedBuilder(environment.primaryAccount.credential, bc.getBlobUrl(), getPerCallVersionPolicy())
            .buildAppendBlobClient()

        when:
        def response = specialBlob.getPropertiesWithResponse(null, null, null)

        then:
        notThrown(BlobStorageException)
        response.getHeaders().getValue("x-ms-version") == "2017-11-09"
    }

}
