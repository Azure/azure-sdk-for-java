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

package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.*
import com.microsoft.rest.v2.http.HttpPipeline
import com.microsoft.rest.v2.http.UnexpectedLengthException
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest

class AppendBlobAPITest extends APISpec {
    AppendBlobURL bu

    def setup() {
        bu = cu.createAppendBlobURL(generateBlobName())
        bu.create(null, null, null, null).blockingGet()
    }

    def "Create defaults"() {
        when:
        AppendBlobCreateResponse createResponse =
                bu.create(null, null, null, null).blockingGet()

        then:
        createResponse.statusCode() == 201
        validateBasicHeaders(createResponse.headers())
        createResponse.headers().contentMD5() == null
        createResponse.headers().isServerEncrypted()
    }

    def "Create min"() {
        expect:
        bu.create().blockingGet().statusCode() == 201
    }

    def "Create error"() {
        when:
        bu.create(null, null, new BlobAccessConditions().withModifiedAccessConditions(new ModifiedAccessConditions()
                .withIfMatch("garbage")), null).blockingGet()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Create headers"() {
        setup:
        BlobHTTPHeaders headers = new BlobHTTPHeaders().withBlobCacheControl(cacheControl)
                .withBlobContentDisposition(contentDisposition)
                .withBlobContentEncoding(contentEncoding)
                .withBlobContentLanguage(contentLanguage)
                .withBlobContentMD5(contentMD5)
                .withBlobContentType(contentType)

        when:
        bu.create(headers, null, null, null).blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null, null).blockingGet()

        then:
        validateBlobHeaders(response.headers(), cacheControl, contentDisposition, contentEncoding, contentLanguage,
                contentMD5, contentType == null ? "application/octet-stream" : contentType)
        // HTTP default content type is application/octet-stream

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                               | contentType
        null         | null               | null            | null            | null                                                                                     | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())) | "type"
    }

    @Unroll
    def "Create metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu.create(null, metadata, null, null).blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null, null).blockingGet()

        then:
        response.headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Create AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))


        expect:
        bu.create(null, null, bac, null).blockingGet().statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Create AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        bu.create(null, null, bac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Create context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, AppendBlobCreateHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.create(null, null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Append block defaults"() {
        setup:
        AppendBlobAppendBlockHeaders headers =
                bu.appendBlock(defaultFlowable, defaultDataSize,
                        null, null).blockingGet().headers()

        expect:
        FlowableUtil.collectBytesInBuffer(bu.download(null, null, false, null)
                .blockingGet().body(null)).blockingGet().compareTo(defaultData) == 0
        validateBasicHeaders(headers)
        headers.contentMD5() != null
        headers.blobAppendOffset() != null
        headers.blobCommittedBlockCount() != null
        bu.getProperties(null, null).blockingGet().headers().blobCommittedBlockCount() == 1
    }

    def "Append block min"() {
        expect:
        bu.appendBlock(defaultFlowable, defaultDataSize).blockingGet().statusCode() == 201
    }

    @Unroll
    def "Append block IA"() {
        when:
        bu.appendBlock(data, dataSize, null, null).blockingGet()

        then:
        def e = thrown(Exception)
        exceptionType.isInstance(e)

        where:
        data            | dataSize            | exceptionType
        null            | defaultDataSize     | IllegalArgumentException
        defaultFlowable | defaultDataSize + 1 | UnexpectedLengthException
        defaultFlowable | defaultDataSize - 1 | UnexpectedLengthException
    }

    def "Append block empty body"() {
        when:
        bu.appendBlock(Flowable.just(ByteBuffer.wrap(new byte[0])), 0, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Append block null body"() {
        when:
        bu.appendBlock(Flowable.just(null), 0, null, null).blockingGet()

        then:
        thrown(NullPointerException) // Thrown by Flowable.
    }

    @Unroll
    def "Append block AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        AppendBlobAccessConditions bac = new AppendBlobAccessConditions()
                .withModifiedAccessConditions(new ModifiedAccessConditions().withIfModifiedSince(modified)
                .withIfUnmodifiedSince(unmodified).withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
                .withAppendPositionAccessConditions(new AppendPositionAccessConditions()
                .withAppendPosition(appendPosE).withMaxSize(maxSizeLTE))

        expect:
        bu.appendBlock(defaultFlowable, defaultDataSize, bac, null)
                .blockingGet().statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | appendPosE | maxSizeLTE
        null     | null       | null         | null        | null            | null       | null
        oldDate  | null       | null         | null        | null            | null       | null
        null     | newDate    | null         | null        | null            | null       | null
        null     | null       | receivedEtag | null        | null            | null       | null
        null     | null       | null         | garbageEtag | null            | null       | null
        null     | null       | null         | null        | receivedLeaseID | null       | null
        null     | null       | null         | null        | null            | 0          | null
        null     | null       | null         | null        | null            | null       | 100
    }

    @Unroll
    def "Append block AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)

        AppendBlobAccessConditions bac = new AppendBlobAccessConditions()
                .withModifiedAccessConditions(new ModifiedAccessConditions().withIfModifiedSince(modified)
                .withIfUnmodifiedSince(unmodified).withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
                .withAppendPositionAccessConditions(new AppendPositionAccessConditions()
                .withAppendPosition(appendPosE).withMaxSize(maxSizeLTE))

        when:
        bu.appendBlock(defaultFlowable, defaultDataSize, bac, null)
                .blockingGet().statusCode()

        then:
        thrown(StorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | appendPosE | maxSizeLTE
        newDate  | null       | null        | null         | null           | null       | null
        null     | oldDate    | null        | null         | null           | null       | null
        null     | null       | garbageEtag | null         | null           | null       | null
        null     | null       | null        | receivedEtag | null           | null       | null
        null     | null       | null        | null         | garbageLeaseID | null       | null
        null     | null       | null        | null         | null           | 1          | null
        null     | null       | null        | null         | null           | null       | 1
    }

    def "Append block error"() {
        setup:
        bu = cu.createAppendBlobURL(generateBlobName())

        when:
        bu.appendBlock(defaultFlowable, defaultDataSize, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Append block context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, AppendBlobAppendBlockHeaders)))

        bu = bu.withPipeline(pipeline)

        when:
        bu.appendBlock(defaultFlowable, defaultDataSize, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }
}
