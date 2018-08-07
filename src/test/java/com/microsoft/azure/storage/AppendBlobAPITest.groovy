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

import com.microsoft.azure.storage.blob.AppendBlobAccessConditions
import com.microsoft.azure.storage.blob.AppendBlobURL
import com.microsoft.azure.storage.blob.BlobAccessConditions
import com.microsoft.azure.storage.blob.BlobHTTPHeaders
import com.microsoft.azure.storage.blob.ETag
import com.microsoft.azure.storage.blob.HTTPAccessConditions
import com.microsoft.azure.storage.blob.LeaseAccessConditions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.StorageException
import com.microsoft.azure.storage.blob.models.AppendBlobAppendBlockHeaders
import com.microsoft.azure.storage.blob.models.AppendBlobCreateResponse
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesResponse
import com.microsoft.rest.v2.util.FlowableUtil
import spock.lang.Unroll
import java.security.MessageDigest

public class AppendBlobAPITest extends APISpec {
    AppendBlobURL bu

    def setup() {
        bu = cu.createAppendBlobURL(generateBlobName())
        bu.create(null, null, null).blockingGet()
    }

    def "Append blob create defaults"() {
        when:
        AppendBlobCreateResponse createResponse =
                bu.create(null, null, null).blockingGet()

        then:
        createResponse.statusCode() == 201
        validateBasicHeaders(createResponse.headers())
        createResponse.headers().contentMD5() == null
        createResponse.headers().isServerEncrypted()
    }

    def "Append blob create error"() {
        when:
        bu.create(null, null, new BlobAccessConditions(
                new HTTPAccessConditions(null, null,
                        new ETag("garbage"), null),
                null, null, null)).blockingGet()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Append blob create headers"() {
        setup:
        BlobHTTPHeaders headers = new BlobHTTPHeaders(cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, contentType)

        when:
        bu.create(headers, null, null).blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null).blockingGet()

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
    def "Append blob create metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu.create(null, metadata, null).blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        response.headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Append blob create AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)


        expect:
        bu.create(null, null, bac).blockingGet().statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    def "Append blob append block defaults"() {
        setup:
        AppendBlobAppendBlockHeaders headers =
                bu.appendBlock(defaultFlowable, defaultDataSize,
                        null).blockingGet().headers()

        expect:
        FlowableUtil.collectBytesInBuffer(bu.download(null, null, false)
                .blockingGet().body()).blockingGet().compareTo(defaultData) == 0
        validateBasicHeaders(headers)
        headers.contentMD5() != null
        headers.blobAppendOffset() != null
        headers.blobCommittedBlockCount() != null
    }

    /*
    TODO: Negative cases where data size does not equal the passed value for length
    defaultData | defaultDataSize + 1 | defaultData                                        || -1
    defaultData | 2                           | ByteBuffer.wrap(defaultText.substring(0, 3).bytes) || -1/*
    try{
        statusCode = bu.appendBlock(Flowable.just(inputData), dataSize, null)
                    .blockingGet().statusCode()
       }
    catch (TimeoutException | RestException e) {
            statusCode = -1
        }
        statusCode == expectedCode || receivedData == expectedData
     */

    @Unroll
    def "Append blob append block AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                new AppendBlobAccessConditions(appendPosE, maxSizeLTE), null)

        expect:
        bu.appendBlock(defaultFlowable, defaultDataSize, bac)
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

    def "Append blob append block error"() {
        setup:
        bu = cu.createAppendBlobURL(generateBlobName())

        when:
        bu.appendBlock(defaultFlowable, defaultDataSize, null).blockingGet()

        then:
        thrown(StorageException)
    }
}
