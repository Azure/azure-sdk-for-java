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

import com.google.common.escape.ArrayBasedCharEscaper
import com.microsoft.azure.storage.blob.BlobAccessConditions
import com.microsoft.azure.storage.blob.BlobHTTPHeaders
import com.microsoft.azure.storage.blob.BlobRange
import com.microsoft.azure.storage.blob.BlobURL
import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.ContainerURL
import com.microsoft.azure.storage.blob.DownloadResponse
import com.microsoft.azure.storage.blob.HTTPAccessConditions
import com.microsoft.azure.storage.blob.LeaseAccessConditions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.PageBlobURL
import com.microsoft.azure.storage.blob.StorageException
import com.microsoft.azure.storage.blob.models.AccessTier
import com.microsoft.azure.storage.blob.models.ArchiveStatus
import com.microsoft.azure.storage.blob.models.BlobSetTierResponse
import com.microsoft.azure.storage.blob.models.BlobType
import com.microsoft.azure.storage.blob.models.BlobAbortCopyFromURLHeaders
import com.microsoft.azure.storage.blob.models.BlobAbortCopyFromURLResponse
import com.microsoft.azure.storage.blob.models.BlobAcquireLeaseHeaders
import com.microsoft.azure.storage.blob.models.BlobBreakLeaseHeaders
import com.microsoft.azure.storage.blob.models.BlobChangeLeaseHeaders
import com.microsoft.azure.storage.blob.models.BlobCreateSnapshotHeaders
import com.microsoft.azure.storage.blob.models.BlobCreateSnapshotResponse
import com.microsoft.azure.storage.blob.models.BlobDeleteHeaders
import com.microsoft.azure.storage.blob.models.BlobDeleteResponse
import com.microsoft.azure.storage.blob.models.BlobDownloadHeaders
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesHeaders
import com.microsoft.azure.storage.blob.models.BlobReleaseLeaseHeaders
import com.microsoft.azure.storage.blob.models.BlobRenewLeaseHeaders
import com.microsoft.azure.storage.blob.models.BlobSetHTTPHeadersResponse
import com.microsoft.azure.storage.blob.models.BlobSetMetadataResponse
import com.microsoft.azure.storage.blob.models.BlobStartCopyFromURLHeaders
import com.microsoft.azure.storage.blob.models.BlobStartCopyFromURLResponse
import com.microsoft.azure.storage.blob.models.CopyStatusType
import com.microsoft.azure.storage.blob.models.DeleteSnapshotsOptionType
import com.microsoft.azure.storage.blob.models.LeaseDurationType
import com.microsoft.azure.storage.blob.models.LeaseStateType
import com.microsoft.azure.storage.blob.models.LeaseStatusType
import com.microsoft.azure.storage.blob.models.PublicAccessType
import com.microsoft.azure.storage.blob.models.StorageErrorCode
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest

class BlobAPITest extends APISpec {
    BlobURL bu

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(defaultFlowable, defaultDataSize, null, null,
                null).blockingGet()
    }

    def "Blob download all null"() {
        when:
        DownloadResponse response = bu.download(null, null, false)
                .blockingGet()
        ByteBuffer body = FlowableUtil.collectBytesInBuffer(response.body()).blockingGet()
        BlobDownloadHeaders headers = response.headers()

        then:
        validateBasicHeaders(headers)
        body == defaultData
        headers.metadata().isEmpty()
        headers.contentLength() != null
        headers.contentType() != null
        headers.contentRange() != null
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
        headers.acceptRanges() == "bytes"
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
    //TODO: offset negative. Count null or less than offset. offset greater than blob length, count + offset greater than blob length, count negative, count + offset > maxint

    @Unroll
    def "Blob download AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
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
                .headers().contentMD5() ==
                MessageDigest.getInstance("MD5").digest(defaultText.substring(0, 3).getBytes())
    }

    def "Blob download error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.download(null, null, false).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Blob get properties all null"() {
        when:
        BlobGetPropertiesHeaders headers = bu.getProperties(null).blockingGet().headers()

        then:
        validateBasicHeaders(headers)
        headers.metadata().isEmpty()
        headers.blobType() == BlobType.BLOCK_BLOB
        headers.copyCompletionTime() == null
        headers.copyStatusDescription() == null
        headers.copyId() == null
        headers.copyProgress() == null
        headers.copySource() == null
        headers.copyStatus() == null
        headers.isIncrementalCopy() == null
        headers.destinationSnapshot() == null
        headers.leaseDuration() == null
        headers.leaseState() == LeaseStateType.AVAILABLE
        headers.leaseStatus() == LeaseStatusType.UNLOCKED
        headers.contentLength() != null
        headers.contentType() != null
        headers.contentMD5() != null
        headers.contentEncoding() == null
        headers.contentDisposition() == null
        headers.contentLanguage() == null
        headers.cacheControl() == null
        headers.blobSequenceNumber() == null
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
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
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

    def "Blob get properties error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.getProperties(null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Blob set HTTP headers null"() {
        setup:
        BlobSetHTTPHeadersResponse response = bu.setHTTPHeaders(null, null).blockingGet()

        expect:
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        response.headers().blobSequenceNumber() == null
        // TODO: Try with a pageBlob to check sequence number
    }

    @Unroll
    def "Blob set HTTP headers headers"() {
        setup:
        BlobHTTPHeaders putHeaders = new BlobHTTPHeaders(cacheControl, contentDisposition, contentEncoding,
                contentLanguage, contentMD5, contentType)
        bu.setHTTPHeaders(putHeaders, null).blockingGet()
        BlobGetPropertiesHeaders receivedHeaders =
                bu.getProperties(null).blockingGet().headers()

        expect:
        validateBlobHeaders(receivedHeaders, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                               | contentType
        null         | null               | null            | null            | null                                                                                     | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())) | "type"

    }

    @Unroll
    def "Blob set HTTP headers AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
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

    def "Blob set Http headers error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.setHTTPHeaders(null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Blob set metadata all null"() {
        setup:
        BlobSetMetadataResponse response = bu.setMetadata(null, null).blockingGet()

        expect:
        bu.getProperties(null).blockingGet().headers().metadata().size() == 0
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        response.headers().isServerEncrypted()
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

        expect:
        bu.setMetadata(metadata, null).blockingGet().statusCode() == statusCode
        bu.getProperties(null).blockingGet().headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2 || statusCode
        null  | null   | null   | null   || 200
        "foo" | "bar"  | "fizz" | "buzz" || 200
        // TODO: Support: null  | "bar"  | null   | null   || 200?
    }

    @Unroll
    def "Blob set metadata AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
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

    def "Blob set metadata error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.setMetadata(null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Blob acquire lease"() {
        setup:
        BlobAcquireLeaseHeaders headers =
                bu.acquireLease(UUID.randomUUID().toString(), leaseTime, null)
                        .blockingGet().headers()

        when:
        BlobGetPropertiesHeaders properties = bu.getProperties(null).blockingGet()
                .headers()

        then:
        properties.leaseState() == leaseState
        properties.leaseDuration() == leaseDuration
        headers.leaseId() != null
        validateBasicHeaders(headers)

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
        match = setupBlobMatchCondition(bu, match)
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

    def "Blob acquire lease error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.acquireLease(null, 20, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Blob renew lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        Thread.sleep(16000) // Wait for the lease to expire to ensure we are actually renewing it
        BlobRenewLeaseHeaders headers = bu.renewLease(leaseID, null).blockingGet().headers()

        expect:
        bu.getProperties(null).blockingGet().headers().leaseState()
                .equals(LeaseStateType.LEASED)
        validateBasicHeaders(headers)
        headers.leaseId() != null
    }

    @Unroll
    def "Blob renew lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
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

    def "Blob renew lease error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.renewLease("id", null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Blob release lease"() {
        setup:
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)

        BlobReleaseLeaseHeaders headers = bu.releaseLease(leaseID, null).blockingGet().headers()

        expect:
        bu.getProperties(null).blockingGet().headers().leaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(headers)
    }

    @Unroll
    def "Blob release leaseAC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
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

    def "blob release lease error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.releaseLease("id", null).blockingGet()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Blob break lease"() {
        setup:
        bu.acquireLease(UUID.randomUUID().toString(), leaseTime, null).blockingGet()

        BlobBreakLeaseHeaders headers = bu.breakLease(breakPeriod, null).blockingGet().headers()
        LeaseStateType state = bu.getProperties(null).blockingGet().headers().leaseState()

        expect:
        state == LeaseStateType.BROKEN || state == LeaseStateType.BREAKING
        headers.leaseTime() <= remainingTime
        validateBasicHeaders(headers)

        where:
        leaseTime | breakPeriod | remainingTime
        -1        | null        | 0
        -1        | 20          | 25
        20        | 15          | 16

    }

    @Unroll
    def "Blob break lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        setupBlobLeaseCondition(bu, receivedLeaseID)
        HTTPAccessConditions hac = new HTTPAccessConditions(modified, unmodified, match, noneMatch)

        expect:
        bu.breakLease(null, hac).blockingGet().statusCode() == 202

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    def "Blob break lease error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.breakLease(null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Blob change lease"() {
        setup:
        String leaseID =
                bu.acquireLease(UUID.randomUUID().toString(), 15, null).blockingGet()
                        .headers().leaseId()
        BlobChangeLeaseHeaders headers = bu.changeLease(leaseID, UUID.randomUUID().toString(), null)
                .blockingGet().headers()
        leaseID = headers.leaseId()

        expect:
        bu.releaseLease(leaseID, null).blockingGet().statusCode() == 200
        validateBasicHeaders(headers)
    }

    @Unroll
    def "Blob change lease AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        String leaseID = setupBlobLeaseCondition(bu, receivedLeaseID)
        HTTPAccessConditions hac = new HTTPAccessConditions(modified, unmodified, match, noneMatch)

        expect:
        bu.changeLease(leaseID, UUID.randomUUID().toString(), hac).blockingGet().statusCode() == 200

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    def "Blob change lease error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.changeLease("id", "id", null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Blob snapshot"() {
        when:
        BlobCreateSnapshotHeaders headers = bu.createSnapshot(null, null)
                .blockingGet().headers()

        then:
        bu.withSnapshot(headers.snapshot()).getProperties(null).blockingGet().statusCode() == 200
        validateBasicHeaders(headers)
    }

    @Unroll
    def "Blob snapshot metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        BlobCreateSnapshotResponse response = bu.createSnapshot(metadata, null).blockingGet()

        expect:
        response.statusCode() == 201
        bu.withSnapshot(response.headers().snapshot())
                .getProperties(null).blockingGet().headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2 || statusCode
        null  | null   | null   | null   || 200
        "foo" | "bar"  | "fizz" | "buzz" || 200
        // TODO: Support: null  | "bar"  | null   | null   || 200?
    }

    @Unroll
    def "Blob snapshot AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.createSnapshot(null, bac).blockingGet().statusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    def "Blob snapshot error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.createSnapshot(null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Blob copy"() {
        setup:
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        BlobStartCopyFromURLHeaders headers =
                bu2.startCopyFromURL(bu.toURL(), null, null, null)
                        .blockingGet().headers()

        when:
        CopyStatusType status = bu2.getProperties(null).blockingGet().headers().copyStatus()

        then:
        status == CopyStatusType.SUCCESS || status == CopyStatusType.PENDING
        validateBasicHeaders(headers)
        headers.copyId() != null
    }

    @Unroll
    def "Blob copy metadata"() {
        setup:
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        Metadata metadata = new Metadata()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        BlobStartCopyFromURLResponse response =
                bu2.startCopyFromURL(bu.toURL(), metadata, null, null)
                        .blockingGet()
        waitForCopy(bu2, response)

        expect:
        bu2.getProperties(null).blockingGet().headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2 || statusCode
        null  | null   | null   | null   || 200
        "foo" | "bar"  | "fizz" | "buzz" || 200
    }

    @Unroll
    def "Blob copy source AC"() {
        setup:
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu2.startCopyFromURL(bu.toURL(), null, bac, null).blockingGet().statusCode() == 202

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
    def "Blob copy dest AC"() {
        setup:
        BlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        bu2.upload(defaultFlowable, defaultDataSize, null, null,
                null).blockingGet()
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu2.startCopyFromURL(bu.toURL(), null, null, bac).blockingGet().statusCode() == 202

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    def "Blob copy error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.startCopyFromURL(new URL("http://www.error.com"),
                null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Blob abort copy"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        ByteBuffer data = getRandomData(8 * 1024 * 1024)
        bu.toBlockBlobURL()
                .upload(Flowable.just(data), 8 * 1024 * 1024, null, null, null)
                .blockingGet()
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null).blockingGet()

        ContainerURL cu2 = alternateServiceURL.createContainerURL(generateBlobName())
        cu2.create(null, null).blockingGet()
        BlobURL bu2 = cu2.createBlobURL(generateBlobName())

        when:
        String copyID =
                bu2.startCopyFromURL(bu.toURL(), null, null, null)
                        .blockingGet().headers().copyId()
        BlobAbortCopyFromURLResponse response = bu2.abortCopyFromURL(copyID, null).blockingGet()
        BlobAbortCopyFromURLHeaders headers = response.headers()

        then:
        response.statusCode() == 204
        headers.requestId() != null
        headers.version() != null
        headers.date() != null
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete(null).blockingGet().statusCode() == 202
    }

    def "Blob abort copy lease"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        ByteBuffer data = getRandomData(8 * 1024 * 1024)
        bu.toBlockBlobURL()
                .upload(Flowable.just(data), 8 * 1024 * 1024, null, null, null)
                .blockingGet()
        // So we don't have to create a SAS.
        cu.setAccessPolicy(PublicAccessType.BLOB, null, null).blockingGet()

        ContainerURL cu2 = alternateServiceURL.createContainerURL(generateBlobName())
        cu2.create(null, null).blockingGet()
        BlockBlobURL bu2 = cu2.createBlockBlobURL(generateBlobName())
        bu2.upload(defaultFlowable, defaultDataSize, null, null, null)
                .blockingGet()
        String leaseID = setupBlobLeaseCondition(bu2, receivedLeaseID)

        when:
        String copyID =
                bu2.startCopyFromURL(bu.toURL(), null, null,
                        new BlobAccessConditions(null, new LeaseAccessConditions(leaseID),
                                null, null))
                        .blockingGet().headers().copyId()

        then:
        bu2.abortCopyFromURL(copyID, new LeaseAccessConditions(leaseID)).blockingGet().statusCode() == 204
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete(null).blockingGet()
    }

    def "Blob abort copy error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.abortCopyFromURL("id", null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Blob delete"() {
        when:
        BlobDeleteResponse response = bu.delete(null, null).blockingGet()
        BlobDeleteHeaders headers = response.headers()

        then:
        response.statusCode() == 202
        headers.requestId() != null
        headers.version() != null
        headers.date() != null
    }

    @Unroll
    def "Blob delete options"() {
        setup:
        bu.createSnapshot(null, null).blockingGet()
        // Create an extra blob so the list isn't empty (null) when we delete base blob, too
        BlockBlobURL bu2 = cu.createBlockBlobURL(generateBlobName())
        bu2.upload(defaultFlowable, defaultDataSize, null, null, null)
                .blockingGet()

        when:
        bu.delete(option, null).blockingGet()

        then:
        cu.listBlobsFlatSegment(null, null).blockingGet()
                .body().segment().blobItems().size()== blobsRemaining

        where:
        option                            | blobsRemaining
        DeleteSnapshotsOptionType.INCLUDE | 1
        DeleteSnapshotsOptionType.ONLY    | 2
    }

    @Unroll
    def "Blob delete AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        bu.delete(DeleteSnapshotsOptionType.INCLUDE, bac).blockingGet().statusCode() == 202

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    def "Blob delete error"() {
        setup:
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.delete(null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Set tier block blob"() {
        setup:
        ContainerURL cu = blobStorageServiceURL.createContainerURL(generateContainerName())
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        cu.create(null, null).blockingGet()
        bu.upload(defaultFlowable, defaultData.remaining(), null, null, null)
                .blockingGet()

        when:
        BlobSetTierResponse initialResponse = bu.setTier(tier).blockingGet()

        then:
        initialResponse.statusCode() == 201
        initialResponse.headers().version() != null
        initialResponse.headers().requestId() != null
        bu.getProperties(null).blockingGet().headers().accessTier() == tier.toString()
        cu.listBlobsFlatSegment(null, null).blockingGet().body().segment().blobItems().get(0)
                .properties().accessTier() == tier

        where:
        tier               | _
        AccessTier.HOT     | _
        AccessTier.COOL    | _
        AccessTier.ARCHIVE | _
    }

    @Unroll
    def "Set tier page blob"() {
        setup:
        ContainerURL cu = premiumServiceURL.createContainerURL(generateContainerName())
        PageBlobURL bu = cu.createPageBlobURL(generateBlobName())
        cu.create(null, null).blockingGet()
        bu.create(512, null, null, null, null).blockingGet()

        when:
        bu.setTier(tier).blockingGet()

        then:
        bu.getProperties(null).blockingGet().headers().accessTier() == tier.toString()
        cu.listBlobsFlatSegment(null, null).blockingGet().body().segment().blobItems().get(0)
                .properties().accessTier() == tier

        where:
        tier           | _
        AccessTier.P4  | _
        AccessTier.P6  | _
        AccessTier.P10 | _
        AccessTier.P20 | _
        AccessTier.P30 | _
        AccessTier.P40 | _
        AccessTier.P50 | _
    }

    def "Set tier inferred"() {
        setup:
        ContainerURL cu = blobStorageServiceURL.createContainerURL(generateBlobName())
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        cu.create(null, null).blockingGet()
        bu.upload(defaultFlowable, defaultDataSize, null, null, null).blockingGet()

        when:
        boolean inferred1 = bu.getProperties(null).blockingGet().headers().accessTierInferred()
        Boolean inferredList1 = cu.listBlobsFlatSegment(null, null).blockingGet().body().segment()
                .blobItems().get(0).properties().accessTierInferred()

        bu.setTier(AccessTier.HOT).blockingGet()

        BlobGetPropertiesHeaders headers = bu.getProperties(null).blockingGet().headers()
        Boolean inferred2 = headers.accessTierInferred()
        Boolean inferredList2 = cu.listBlobsFlatSegment(null, null).blockingGet().body().segment()
                .blobItems().get(0).properties().accessTierInferred()

        then:
        inferred1
        inferredList1
        inferred2 == null
        inferredList2 == null
    }

    @Unroll
    def "Set tier archive status"() {
        setup:
        ContainerURL cu = blobStorageServiceURL.createContainerURL(generateBlobName())
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        cu.create(null, null).blockingGet()
        bu.upload(defaultFlowable, defaultDataSize, null, null, null).blockingGet()

        when:
        bu.setTier(sourceTier).blockingGet()
        bu.setTier(destTier).blockingGet()

        then:
        bu.getProperties(null).blockingGet().headers().archiveStatus() == status.toString()
        cu.listBlobsFlatSegment(null, null).blockingGet().body().segment().blobItems()
                .get(0).properties().archiveStatus()

        where:
        sourceTier         | destTier        | status
        AccessTier.ARCHIVE | AccessTier.COOL | ArchiveStatus.REHYDRATE_PENDING_TO_COOL
        AccessTier.ARCHIVE | AccessTier.HOT  | ArchiveStatus.REHYDRATE_PENDING_TO_HOT
    }

    def "Set tier error"() {
        setup:
        ContainerURL cu = blobStorageServiceURL.createContainerURL(generateBlobName())
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        cu.create(null, null).blockingGet()
        bu.upload(defaultFlowable, defaultDataSize, null, null, null).blockingGet()

        when:
        bu.setTier(AccessTier.fromString("garbage")).blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.INVALID_HEADER_VALUE
    }

    def "Set tier illegal argument"() {
        when:
        bu.setTier(null)

        then:
        thrown(IllegalArgumentException)
    }

    def "Undelete"() {
        setup:
        enableSoftDelete()
        bu.delete(null, null).blockingGet()

        when:
        def response =  bu.undelete().blockingGet()
        bu.getProperties(null).blockingGet()

        then:
        notThrown(StorageException)
        response.headers().requestId() != null
        response.headers().version() != null
        response.headers().date() != null

        disableSoftDelete() == null
    }

    def "Undelete error"() {
        bu = cu.createBlockBlobURL(generateBlobName())

        when:
        bu.undelete().blockingGet()

        then:
        thrown(StorageException)
    }
}
