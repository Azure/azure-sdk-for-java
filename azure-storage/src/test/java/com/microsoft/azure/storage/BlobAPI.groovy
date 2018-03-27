package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.BlobHTTPHeaders
import com.microsoft.azure.storage.blob.BlobURL

import com.microsoft.azure.storage.blob.ContainerURL
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.models.BlobType
import com.microsoft.azure.storage.blob.models.BlobsGetPropertiesHeaders
import com.microsoft.azure.storage.blob.models.CopyStatusType
import com.microsoft.azure.storage.blob.models.LeaseStateType
import com.microsoft.azure.storage.blob.models.PublicAccessType
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest


class BlobAPI extends APISpec{
    BlobURL bu

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(Flowable.just(defaultData), defaultText.length(), null, null, null)
                .blockingGet()
    }

    def "Blob get all null"() {
        when:
        ByteBuffer body = FlowableUtil.collectBytesInBuffer(
                bu.download(null, null, false).blockingGet().body())
                .blockingGet()

        then:
        body.compareTo(defaultData) == 0
    }

    def "Blob get properties all null"() {
        when:
        BlobsGetPropertiesHeaders headers = bu.getProperties(null).blockingGet().headers()

        then:
        headers.blobType() == BlobType.BLOCK_BLOB
        headers.eTag() != null
        headers.version() != null
        headers.leaseState().toString() == "available"
    }

    @Unroll
    def "Blob set HTTP headers"() {
        setup:
        BlobHTTPHeaders putHeaders = new BlobHTTPHeaders(cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, contentType)
        bu.setHTTPHeaders(putHeaders, null).blockingGet()
        BlobsGetPropertiesHeaders receivedHeaders =
                bu.getProperties(null).blockingGet().headers()

        expect:
        receivedHeaders.cacheControl() == cacheControl
        receivedHeaders.contentDisposition() == contentDisposition
        receivedHeaders.contentEncoding() == contentEncoding
        receivedHeaders.contentLanguage() == contentLanguage
        receivedHeaders.contentMD5() == contentMD5
        receivedHeaders.contentType() == contentType

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                                                     | contentType
        null         | null               | null            | null            | null                                                                                                           | null
        "control"    | "disposition"      | "encoding"      | "language"      | new String(Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array()))) | "type"

    }

    @Unroll
    def "Blob set get metadata"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put(key1, value1)
        metadata.put(key2, value2)

        int initialCode = bu.setMetadata(metadata, null).blockingGet().statusCode()
        Map<String,String> receivedMetadata = bu.getProperties(null).blockingGet().headers()
                .metadata()

        expect:
        initialCode == statusCode
        receivedMetadata.get(key1).equals(value1)
        receivedMetadata.get(key2).equals(value2)

        where:
        key1    | value1     | key2     | value2    || statusCode
        "foo"   | "bar"      | "fizz"   | "buzz"    || 200
    }

    def "Blob acquire lease"() {
        setup:
        bu.acquireLease(UUID.randomUUID().toString(), -1, null).blockingGet()

        expect:
        bu.getProperties(null).blockingGet()
                .headers().leaseState().equals(LeaseStateType.LEASED)
    }

    def "Blob renew lease"() {
        setup:
        String leaseID =
                bu.acquireLease(UUID.randomUUID().toString(), 15, null).blockingGet()
                        .headers().leaseId()
        Thread.sleep(16000)
        bu.renewLease(leaseID, null).blockingGet()

        expect:
        bu.getProperties(null).blockingGet().headers().leaseState()
                .equals(LeaseStateType.LEASED)
    }

    def "Blob release lease"() {
        setup:
        String leaseID =
                bu.acquireLease(UUID.randomUUID().toString(), 15, null).blockingGet()
                        .headers().leaseId()
        bu.releaseLease(leaseID, null).blockingGet()

        expect:
        bu.getProperties(null).blockingGet().headers().leaseState()
                .equals(LeaseStateType.AVAILABLE)
    }

    def "Blob break lease"() {
        setup:
        bu.acquireLease(UUID.randomUUID().toString(), -1, null).blockingGet()

        bu.breakLease(null, null).blockingGet()

        expect:
        bu.getProperties(null).blockingGet().headers().leaseState()
                .equals(LeaseStateType.BROKEN)

    }

    def "Blob change lease"() {
        setup:
        String leaseID =
                bu.acquireLease(UUID.randomUUID().toString(), 15, null).blockingGet()
                        .headers().leaseId()
        leaseID = bu.changeLease(leaseID, UUID.randomUUID().toString(), null).blockingGet()
                .headers().leaseId()

        expect:
        bu.releaseLease(leaseID, null).blockingGet().statusCode() == 200
    }

    def "Blob snapshot"() {
        when:
        String snapshot = bu.createSnapshot(null, null).blockingGet().headers().snapshot()

        then:
        bu.withSnapshot(snapshot).getProperties(null).blockingGet().statusCode() == 200
    }

    def "Blob copy"() {
        setup:
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        bu2.startCopyFromURL(bu.toURL(), null, null, null).blockingGet()

        when:
        CopyStatusType status = bu2.getProperties(null).blockingGet().headers().copyStatus()

        then:
        status.equals(CopyStatusType.SUCCESS) || status.equals(CopyStatusType.PENDING)

    }

    def "Blob abort copy"() {
        setup:
        ByteBuffer data = getRandomData(8*1024*1024)
        bu.toBlockBlobURL().upload(Flowable.just(data), 8*1024*1024, null, null, null)
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null).blockingGet()

        ContainerURL cu2 = alternateServiceURL.createContainerURL(generateBlobName())
        cu2.create(null, null).blockingGet()
        BlobURL bu2 = cu2.createBlobURL(generateBlobName())

        when:
        String copyID = bu2.startCopyFromURL(bu.toURL(), null, null, null)
                .blockingGet().headers().copyId()

        then:
        bu2.abortCopyFromURL(copyID, null).blockingGet().statusCode() == 204
    }

    def "Blob delete"() {
        expect:
        bu.delete(null, null).blockingGet().statusCode() == 202
    }
}
