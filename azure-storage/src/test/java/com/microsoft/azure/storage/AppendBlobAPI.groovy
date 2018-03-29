package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.AppendBlobURL
import com.microsoft.azure.storage.blob.BlobAccessConditions
import com.microsoft.azure.storage.blob.BlobHTTPHeaders
import com.microsoft.azure.storage.blob.ETag
import com.microsoft.azure.storage.blob.HTTPAccessConditions
import com.microsoft.azure.storage.blob.LeaseAccessConditions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.models.AppendBlobsCreateResponse
import com.microsoft.azure.storage.blob.models.BlobsAcquireLeaseHeaders
import com.microsoft.azure.storage.blob.models.BlobsGetPropertiesHeaders
import com.microsoft.azure.storage.blob.models.BlobsGetPropertiesResponse
import com.microsoft.rest.v2.RestException
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import io.reactivex.SingleSource
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Function
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.concurrent.TimeoutException

class AppendBlobAPI extends APISpec {
    AppendBlobURL bu

    def setup() {
        bu = cu.createAppendBlobURL(generateBlobName())
        bu.create(null, null, null).blockingGet()
    }

    def "Append blob create defaults"() {
        setup:
        bu = cu.createAppendBlobURL(generateBlobName())

        when:
        AppendBlobsCreateResponse createResponse =
                bu.create(null, null, null).blockingGet()

        then:
        createResponse.statusCode() == 201
        createResponse.headers().eTag() != null
        createResponse.headers().dateProperty() != null
        createResponse.headers().lastModified() != null
        createResponse.headers().requestId() != null
        createResponse.headers().version() != null
    }

    @Unroll
    def "Append blob create headers"() {
        setup:
        bu = cu.createAppendBlobURL(generateBlobName())
        BlobHTTPHeaders headers = new BlobHTTPHeaders(cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, contentType)

        when:
        bu.create(headers, null, null).blockingGet()
        BlobsGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        response.statusCode() == 200
        response.headers().cacheControl() == cacheControl
        response.headers().contentDisposition() == contentDisposition
        response.headers().contentEncoding() == contentEncoding
        response.headers().contentMD5() == contentMD5
        // HTTP default content type is application/octet-stream
        contentType == null ? response.headers().contentType() == "application/octet-stream" :
                response.headers().contentType() == contentType

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                               | contentType
        null         | null               | null            | null            | null                                                                                     | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())) | "type"
    }

    @Unroll
    def "Append blob create metadata"() {
        setup:
        bu = cu.createAppendBlobURL(generateBlobName())
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu.create(null, metadata, null).blockingGet()
        BlobsGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        response.statusCode() == 200
        response.headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Append blob create AC"() {
        setup:
        match = setupMatchCondition(bu, match)
        leaseID = setupLeaseCondition(bu, leaseID)
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


    @Unroll
    def "Append blob append block defaults"() {
        setup:
        int statusCode
        ByteBuffer receivedData = null
        try {
            statusCode = bu.appendBlock(Flowable.just(inputData), dataSize, null)
                    .blockingGet().statusCode()
            receivedData = FlowableUtil.collectBytesInBuffer(
                    bu.download(null, null, false)
                            .blockingGet().body()).blockingGet()
        }
        catch (TimeoutException | RestException e) {
            statusCode = -1
        }

        expect:
        statusCode == expectedCode || receivedData == expectedData

        where:
        inputData   | dataSize                    | expectedData                                       || expectedCode
        defaultData | defaultData.remaining()     | defaultData                                        || 201
        defaultData | defaultData.remaining() + 1 | defaultData                                        || -1
        defaultData | 2                           | ByteBuffer.wrap(defaultText.substring(0, 3).bytes) || -1

    }

    @Unroll
    def "Append blob append block AC"() {
        setup:
        match = setupMatchCondition(bu, match)
        leaseID = setupLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.appendBlock(Flowable.just(defaultData), defaultData.remaining(), bac).blockingGet().statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | appendPosE | appendPosLTE
        null     | null       | null         | null        | null            | null       | null
        oldDate  | null       | null         | null        | null            | null       | null
        null     | newDate    | null         | null        | null            | null       | null
        null     | null       | receivedEtag | null        | null            | null       | null
        null     | null       | null         | garbageEtag | null            | null       | null
        null     | null       | null         | null        | receivedLeaseID | null       | null
        null     | null       | null         | null        | null            | 0          | null
        null     | null       | null         | null        | null            | null       | 0
    }
}
