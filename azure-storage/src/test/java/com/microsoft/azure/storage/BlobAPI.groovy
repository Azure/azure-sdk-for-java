package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.BlobAccessConditions
import com.microsoft.azure.storage.blob.BlobHTTPHeaders
import com.microsoft.azure.storage.blob.BlobRange
import com.microsoft.azure.storage.blob.BlobURL

import com.microsoft.azure.storage.blob.ContainerURL
import com.microsoft.azure.storage.blob.HTTPAccessConditions
import com.microsoft.azure.storage.blob.LeaseAccessConditions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.models.AccessTier
import com.microsoft.azure.storage.blob.models.BlobType
import com.microsoft.azure.storage.blob.models.BlobsAcquireLeaseHeaders
import com.microsoft.azure.storage.blob.models.BlobsDownloadHeaders
import com.microsoft.azure.storage.blob.models.BlobsDownloadResponse
import com.microsoft.azure.storage.blob.models.BlobsGetPropertiesHeaders
import com.microsoft.azure.storage.blob.models.BlobsRenewLeaseHeaders
import com.microsoft.azure.storage.blob.models.BlobsSetHTTPHeadersHeaders
import com.microsoft.azure.storage.blob.models.BlobsSetHTTPHeadersResponse
import com.microsoft.azure.storage.blob.models.BlobsSetMetadataHeaders
import com.microsoft.azure.storage.blob.models.BlobsSetMetadataResponse
import com.microsoft.azure.storage.blob.models.CopyStatusType
import com.microsoft.azure.storage.blob.models.LeaseDurationType
import com.microsoft.azure.storage.blob.models.LeaseStateType
import com.microsoft.azure.storage.blob.models.LeaseStatusType
import com.microsoft.azure.storage.blob.models.PublicAccessType
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest


class BlobAPI extends APISpec {
    BlobURL bu

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(Flowable.just(defaultData), defaultText.length(), null, null, null)
                .blockingGet()
    }

    def "Blob download all null"() {
        when:
        BlobsDownloadResponse response = bu.download(null, null, false)
                .blockingGet()
        ByteBuffer body = FlowableUtil.collectBytesInBuffer(response.body()).blockingGet()
        BlobsDownloadHeaders headers = response.headers()

        then:
        body == defaultData
        headers.lastModified() != null
        headers.metadata().isEmpty()
        headers.contentLength() != null
        headers.contentType() != null
        headers.contentRange() != null
        headers.eTag() != null
        headers.contentMD5() == null
        headers.contentEncoding() == null
        headers.cacheControl() == null
        headers.contentDisposition() == null
        headers.contentLanguage() == null
        headers.blobSequenceNumber() == null
        headers.blobType() == BlobType.BLOCK_BLOB
        headers.copyCompletionTime() == null
        headers.copyStatusDescription() == null
        headers.copyId() == null
        headers.copyProgress() == null
        headers.copySource() == null
        headers.copyStatus() == null
        headers.leaseDuration() == null
        headers.leaseState() == LeaseStateType.AVAILABLE
        headers.leaseStatus() == LeaseStatusType.UNLOCKED
        headers.requestId() != null
        headers.version() != null
        headers.acceptRanges() == "bytes"
        headers.dateProperty() != null
        headers.blobCommittedBlockCount() == null
        headers.serverEncrypted
        headers.blobContentMD5() != null
        //TODO: Add in tests that cover non-null values for these headers
    }

    @Unroll
    def "Blob download range"() {
        setup:
        BlobRange range = new BlobRange(offset, count)

        when:
        ByteBuffer body = FlowableUtil.collectBytesInBuffer(
                bu.download(range, null, false).blockingGet().body()).blockingGet()
        String bodyStr = new String(body.array())

        then:
        bodyStr == expectedData

        where:
        offset | count | expectedData
        0      | null  | defaultText
        0      | 5     | defaultText.substring(0, 5)
        3      | 2     | defaultText.substring(3, 3 + 2)
    }
    //TODO: offset negative. Count null or less than offset.

    @Unroll
    def "Blob download AC"() {
        setup:
        match = setupMatchCondition(bu, match)
        leaseID = setupLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.download(null, bac, false).blockingGet().statusCode() == 206

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    def "Blob download md5"() {
        expect:
        bu.download(new BlobRange(0, 3), null, true).blockingGet()
                .headers().contentMD5() != null
    }

    def "Blob get properties all null"() {
        when:
        BlobsGetPropertiesHeaders headers = bu.getProperties(null).blockingGet().headers()

        then:
        headers.lastModified() != null
        headers.metadata().isEmpty()
        headers.blobType() == BlobType.BLOCK_BLOB
        headers.copyCompletionTime() == null
        headers.copyStatusDescription() == null
        headers.copyId() == null
        headers.copyProgress() == null
        headers.copySource() == null
        headers.copyStatus() == null
        headers.incrementalCopy == null
        headers.destinationSnapshot() == null
        headers.leaseDuration() == null
        headers.leaseState() == LeaseStateType.AVAILABLE
        headers.leaseStatus() == LeaseStatusType.UNLOCKED
        headers.contentLength() != null
        headers.contentType() != null
        headers.eTag() != null
        headers.contentMD5() != null
        headers.contentEncoding() == null
        headers.contentDisposition() == null
        headers.contentLanguage() == null
        headers.cacheControl() == null
        headers.blobSequenceNumber() == null
        headers.requestId() != null
        headers.version() != null
        headers.dateProperty() != null
        headers.acceptRanges() == "bytes"
        headers.blobCommittedBlockCount() == null
        headers.isServerEncrypted()
        headers.accessTier() == AccessTier.HOT.toString()
        headers.accessTierInferred()
        headers.archiveStatus() == null
        // TODO: Test where most of these values will not be null
    }

    @Unroll
    def "Blob get properties AC"() {
        setup:
        match = setupMatchCondition(bu, match)
        leaseID = setupLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.getProperties(bac).blockingGet().statusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    def "Blob set HTTP headers null"() {
        setup:
        BlobsSetHTTPHeadersResponse response = bu.setHTTPHeaders(null, null).blockingGet()
        BlobsSetHTTPHeadersHeaders headers = response.headers()

        expect:
        response.statusCode() == 200
        headers.eTag() != null
        headers.lastModified() != null
        headers.blobSequenceNumber() == null
        headers.requestId() != null
        headers.version() != null
        headers.dateProperty() != null
    }

    @Unroll
    def "Blob set HTTP headers headers"() {
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
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                               | contentType
        null         | null               | null            | null            | null                                                                                     | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())) | "type"

    }

    @Unroll
    def "Blob set HTTP headers AC"() {
        setup:
        match = setupMatchCondition(bu, match)
        leaseID = setupLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.setHTTPHeaders(null, bac).blockingGet().statusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    def "Blob set metadata all null"() {
        setup:
        BlobsSetMetadataResponse response = bu.setMetadata(null, null).blockingGet()
        BlobsSetMetadataHeaders headers = response.headers()

        expect:
        response.statusCode() == 200
        headers.eTag() != null
        headers.lastModified() != null
        headers.requestId() != null
        headers.version() != null
        headers.dateProperty() != null
        headers.isServerEncrypted()
    }

    @Unroll
    def "Blob set metadata metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        int initialCode = bu.setMetadata(metadata, null).blockingGet().statusCode()
        Map<String, String> receivedMetadata = bu.getProperties(null).blockingGet().headers()
                .metadata()

        expect:
        initialCode == statusCode
        receivedMetadata.get(key1).equals(value1)
        receivedMetadata.get(key2).equals(value2)

        where:
        key1  | value1 | key2   | value2 || statusCode
        null  | null   | null   | null   || 200
        "foo" | "bar"  | "fizz" | "buzz" || 200
        // TODO: Support: null  | "bar"  | null   | null   || 200?
    }


    @Unroll
    def "Blob set metadata AC"() {
        setup:
        match = setupMatchCondition(bu, match)
        leaseID = setupLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.setMetadata(null, bac).blockingGet().statusCode() == 200

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
    def "Blob acquire lease"() {
        setup:
        BlobsAcquireLeaseHeaders headers = bu.acquireLease(UUID.randomUUID().toString(), -1, null)
                .blockingGet().headers()

        when:
        BlobsGetPropertiesHeaders properties = bu.getProperties(null).blockingGet()
                .headers()

        then:
        properties.leaseState() == LeaseStateType.LEASED
        properties.leaseDuration() == LeaseDurationType.INFINITE
        headers.eTag() != null
        headers.lastModified() != null
        headers.leaseId() != null
        headers.requestId() != null
        headers.version() != null
        headers.dateProperty() != null

        where:
        proposedID                   | leaseTime || leaseState            | leaseDuration
        null                         | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
        null                         | 25        || LeaseStateType.LEASED | LeaseDurationType.FIXED
        UUID.randomUUID().toString() | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
    }
    // TODO: Invalid lease times. Invalid proposed ID format

    @Unroll
    def "Blob acquire lease AC"() {
        setup:
        match = setupMatchCondition(bu, match)
        HTTPAccessConditions hac = new HTTPAccessConditions(modified, unmodified, match, noneMatch)

        expect:
        bu.acquireLease(null, -1, hac).blockingGet().statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    def "Blob renew lease"() {
        setup:
        String leaseID =
                bu.acquireLease(UUID.randomUUID().toString(), 15, null).blockingGet()
                        .headers().leaseId()
        Thread.sleep(16000) // Wait for the lease to expire to ensure we are actually renewing it
        BlobsRenewLeaseHeaders headers = bu.renewLease(leaseID, null).blockingGet().headers()

        expect:
        bu.getProperties(null).blockingGet().headers().leaseState()
                .equals(LeaseStateType.LEASED)
        headers.eTag() != null
        headers.lastModified() != null
        headers.leaseId() != null
        headers.requestId() != null
        headers.version() != null
        headers.dateProperty() != null
    }

    @Unroll
    def "Blob renew lease AC"() {
        setup:
        match = setupMatchCondition(bu, match)
        String leaseID = setupLeaseCondition(bu, receivedLeaseID)
        HTTPAccessConditions hac = new HTTPAccessConditions(modified, unmodified, match, noneMatch)

        expect:
        bu.renewLease(leaseID, hac).blockingGet().statusCode() == 200

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
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

    @Unroll
    def "Blob release leaseAC"() {
        setup:
        match = setupMatchCondition(bu, match)
        String leaseID = setupLeaseCondition(bu, receivedLeaseID)
        HTTPAccessConditions hac = new HTTPAccessConditions(modified, unmodified, match, noneMatch)

        expect:
        bu.releaseLease(leaseID, hac).blockingGet().statusCode() == 200

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
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
        ByteBuffer data = getRandomData(8 * 1024 * 1024)
        bu.toBlockBlobURL().upload(Flowable.just(data), 8 * 1024 * 1024, null, null, null)
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
