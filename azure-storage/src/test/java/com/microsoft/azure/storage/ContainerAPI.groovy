package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.AppendBlobURL
import com.microsoft.azure.storage.blob.BlobAccessConditions
import com.microsoft.azure.storage.blob.BlobListingDetails
import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.ContainerAccessConditions
import com.microsoft.azure.storage.blob.ETag
import com.microsoft.azure.storage.blob.HTTPAccessConditions
import com.microsoft.azure.storage.blob.LeaseAccessConditions
import com.microsoft.azure.storage.blob.ListBlobsOptions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.PageBlobURL
import com.microsoft.azure.storage.blob.models.AccessPolicy
import com.microsoft.azure.storage.blob.models.Blob
import com.microsoft.azure.storage.blob.models.BlobList
import com.microsoft.azure.storage.blob.models.BlobPrefix
import com.microsoft.azure.storage.blob.models.BlobsGetPropertiesResponse
import com.microsoft.azure.storage.blob.models.ContainersAcquireLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainersBreakLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainersChangeLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainersCreateResponse
import com.microsoft.azure.storage.blob.models.ContainersDeleteHeaders
import com.microsoft.azure.storage.blob.models.ContainersDeleteResponse
import com.microsoft.azure.storage.blob.models.ContainersGetAccessPolicyResponse
import com.microsoft.azure.storage.blob.models.ContainersGetPropertiesHeaders
import com.microsoft.azure.storage.blob.models.ContainersGetPropertiesResponse
import com.microsoft.azure.storage.blob.models.ContainersListBlobFlatSegmentHeaders
import com.microsoft.azure.storage.blob.models.ContainersListBlobFlatSegmentResponse
import com.microsoft.azure.storage.blob.models.ContainersListBlobHierarchySegmentHeaders
import com.microsoft.azure.storage.blob.models.ContainersListBlobHierarchySegmentResponse
import com.microsoft.azure.storage.blob.models.ContainersReleaseLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainersRenewLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainersSetAccessPolicyResponse
import com.microsoft.azure.storage.blob.models.ContainersSetMetadataResponse
import com.microsoft.azure.storage.blob.models.CopyStatusType
import com.microsoft.azure.storage.blob.models.LeaseDurationType
import com.microsoft.azure.storage.blob.models.LeaseStateType
import com.microsoft.azure.storage.blob.models.LeaseStatusType
import com.microsoft.azure.storage.blob.models.ListBlobsResponse
import com.microsoft.azure.storage.blob.models.PublicAccessType
import com.microsoft.azure.storage.blob.models.SignedIdentifier
import com.microsoft.rest.v2.RestException
import com.microsoft.rest.v2.http.HttpPipelineLogger
import io.reactivex.Flowable
import spock.lang.*

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

class ContainerAPI extends APISpec {

    def "Container create all null"() {
        setup:
        // Overwrite the existing cu, which has already been created
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        ContainersCreateResponse response = cu.create(null, null).blockingGet()

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
    }

    @Unroll
    def "Container create metadata"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        cu.create(metadata, null).blockingGet()
        ContainersGetPropertiesResponse response = cu.getProperties(null).blockingGet()

        then:
        response.headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Container create publicAccess"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        int statusCode = cu.create(null, publicAccess).blockingGet().statusCode()
        PublicAccessType access =
                cu.getProperties(null).blockingGet().headers().blobPublicAccess()

        then:
        access.toString() == publicAccess.toString()

        where:
        publicAccess               | _
        PublicAccessType.BLOB      | _
        PublicAccessType.CONTAINER | _
        null                       | _
    }

    def "Container get properties null"() {
        when:
        ContainersGetPropertiesHeaders headers =
                cu.getProperties(null).blockingGet().headers()

        then:
        validateBasicHeaders(headers)
        headers.blobPublicAccess() == null
        headers.leaseDuration() == null
        headers.leaseState() == LeaseStateType.AVAILABLE
        headers.leaseStatus() == LeaseStatusType.UNLOCKED
    }

    def "Container get properties lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.getProperties(new LeaseAccessConditions(leaseID)).blockingGet().statusCode() == 200
    }

    def "Container set metadata"() {
        setup:
        ContainersSetMetadataResponse response = cu.setMetadata(null, null).blockingGet()

        expect:
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
    }

    @Unroll
    def "Container set metadata metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        expect:
        cu.setMetadata(metadata, null).blockingGet().statusCode() == 200
        cu.getProperties(null).blockingGet().headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
        //TODO: invalid characters. empty metadata
    }

    @Unroll
    def "Container set metadata AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cu, leaseID)
        ContainerAccessConditions cac = new ContainerAccessConditions(
                new HTTPAccessConditions(modified, null, null, null),
                new LeaseAccessConditions(leaseID))

        expect:
        cu.setMetadata(null, cac).blockingGet().statusCode() == 200

        where:
        modified | leaseID
        null     | null
        oldDate  | null
        null     | receivedLeaseID
    }

    @Unroll
    def "Container set access policy"() {
        setup:
        cu.setAccessPolicy(access, null, null).blockingGet()

        expect:
        cu.getProperties(null).blockingGet()
                .headers().blobPublicAccess() == access

        where:
        access                     | _
        PublicAccessType.BLOB      | _
        PublicAccessType.CONTAINER | _
        null                       | _
    }

    def "Container set access policy ids"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
                .withId("0000")
                .withAccessPolicy(new AccessPolicy().withStart(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .withExpiry(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(1))
                .withPermission("r"))
        List<SignedIdentifier> ids = new ArrayList<>()
        ids.push(identifier)

        when:
        ContainersSetAccessPolicyResponse response =
                cu.setAccessPolicy(null, ids, null).blockingGet()
        SignedIdentifier receivedIdentifier = cu.getAccessPolicy(null).blockingGet().body().get(0)

        then:
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        receivedIdentifier.accessPolicy().expiry().compareTo(identifier.accessPolicy().expiry())
        receivedIdentifier.accessPolicy().start().compareTo(identifier.accessPolicy().start())
        receivedIdentifier.accessPolicy().permission() == identifier.accessPolicy().permission()
    }
    // TODO: Fix nanosecond resolution issue

    @Unroll
    def "Container set access policy AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cu, leaseID)
        ContainerAccessConditions cac = new ContainerAccessConditions(
                new HTTPAccessConditions(modified, unmodified, null, null),
                new LeaseAccessConditions(leaseID))

        expect:
        cu.setAccessPolicy(null, null, cac).blockingGet().statusCode() == 200

        where:
        modified | unmodified | leaseID
        null     | null       | null
        oldDate  | null       | null
        null     | newDate    | null
        null     | null       | receivedLeaseID
    }

    def "Container get access policy"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
                .withId("0000")
                .withAccessPolicy(new AccessPolicy().withStart(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .withExpiry(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(1))
                .withPermission("r"))
        List<SignedIdentifier> ids = new ArrayList<>()
        ids.push(identifier)
        cu.setAccessPolicy(PublicAccessType.BLOB, ids, null).blockingGet()
        ContainersGetAccessPolicyResponse response = cu.getAccessPolicy(null).blockingGet()

        expect:
        response.statusCode() == 200
        response.headers().blobPublicAccess() == PublicAccessType.BLOB
        validateBasicHeaders(response.headers())
        response.body().get(0).accessPolicy().expiry().compareTo(identifier.accessPolicy().expiry())
        response.body().get(0).accessPolicy().start().compareTo(identifier.accessPolicy().start())
        response.body().get(0).accessPolicy().permission() == identifier.accessPolicy().permission()
    }

    def "Container get access policy lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.getAccessPolicy(new LeaseAccessConditions(leaseID)).blockingGet().statusCode() == 200
    }

    def "Container delete"() {
        when:
        ContainersDeleteResponse response = cu.delete(null).blockingGet()

        then:
        response.statusCode() == 202
        response.headers().requestId() != null
        response.headers().version() != null
        response.headers().dateProperty() != null
    }

    @Unroll
    def "Container delete AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cu, leaseID)
        ContainerAccessConditions cac = new ContainerAccessConditions(
                new HTTPAccessConditions(modified, unmodified, null, null),
                new LeaseAccessConditions(leaseID))

        expect:
        cu.delete(cac).blockingGet().statusCode() == 202

        where:
        modified | unmodified | leaseID
        null     | null       | null
        oldDate  | null       | null
        null     | newDate    | null
        null     | null       | receivedLeaseID
    }

    def "Container list blobs flat"() {
        setup:
        String name = generateBlobName()
        PageBlobURL bu = cu.createPageBlobURL(name)
        bu.create(512, null, null, null, null).blockingGet()

        when:
        ContainersListBlobFlatSegmentResponse response = cu.listBlobsFlatSegment(null, null)
                .blockingGet()
        ContainersListBlobFlatSegmentHeaders headers = response.headers()
        List<Blob> blobs = response.body().blobs().blob()

        then:
        response.statusCode() == 200
        headers.contentType() != null
        headers.requestId() != null
        headers.version() != null
        headers.dateProperty() != null
        blobs.size() == 1
        blobs.get(0).name() == name
    }

    @Unroll
    def "Container list blobs flat options"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions(new BlobListingDetails(copy, metadata, snapshots, uncommitted),
                prefix, maxResults)

        String normalName = "a" + generateBlobName()
        PageBlobURL normal = cu.createPageBlobURL(normalName)
        normal.create(512, null, null, null, null).blockingGet()

        String copyName = "c" + generateBlobName()
        PageBlobURL copyBlob = cu.createPageBlobURL(copyName)
        waitForCopy(copyBlob, copyBlob.startCopyFromURL(normal.toURL(),
                null, null, null).blockingGet())

        String metadataName = "m" + generateBlobName()
        PageBlobURL metadataBlob = cu.createPageBlobURL(metadataName)
        Metadata values = new Metadata()
        values.put("foo", "bar")
        metadataBlob.create(512, null, null, values, null).blockingGet()

        String snapshotTime = normal.createSnapshot(null, null)
                .blockingGet().headers().snapshot()

        String uncommittedName = "u" + generateBlobName()
        BlockBlobURL uncommittedBlob = cu.createBlockBlobURL(uncommittedName)
        uncommittedBlob.stageBlock("0000", Flowable.just(defaultData), defaultData.remaining(),
                null).blockingGet()

        when:
        List<Blob> blobs = cu.listBlobsFlatSegment(null, options).blockingGet().body().blobs().blob()

        then:
        if (copy) {
            blobs.get(0).name() == normalName
            blobs.get(1).name() == copyName
            blobs.get(1).properties().copyId() != null
            blobs.get(1).properties().copySource() == normalName
            blobs.get(1).properties().copyStatus() == CopyStatusType.SUCCESS // We waited for the copy to complete.
            blobs.get(1).properties().copyProgress() != null
            blobs.get(1).properties().copyCompletionTime() != null
            blobs.size() == 3 // Normal, copy, metadata
        }
        else if (metadata) {
            blobs.get(0).name() == normalName
            blobs.get(1).name() == copyName
            blobs.get(1).properties().copyCompletionTime() == null
            blobs.get(2).name() == metadataName
            blobs.get(2).metadata().get("foo") == "bar"
            blobs.size() == 3 // Normal, copy, metadata
        }
        else if (snapshots) {
            blobs.get(0).name() == normalName
            blobs.get(1).name() == normalName
            blobs.get(1).snapshot() == snapshotTime
            blobs.size() == 4 // Normal, snapshot, copy, metadata
        }
        else if (uncommitted) {
            blobs.get(0).name() == normalName
            blobs.get(2).name() == uncommittedName
            blobs.size() == 4 // Normal, copy, metadata, uncommitted
        }
        else if (prefix != null) {
            blobs.get(0).name() == normalName
            blobs.size() == 1 // Normal
        }

        if (maxResults != null) {
            blobs.size() == maxResults
        }

        where:
        copy  | metadata | snapshots | uncommitted | prefix | maxResults
        true  | false    | false     | false       | null   | null
        false | true     | false     | false       | null   | null
        false | false    | true      | false       | null   | null
        false | false    | false     | true        | null   | null
        false | false    | false     | false       | "a"    | null
        true  | false    | true      | true        | null   | 2
    }

    def "Container list blobs flat marker"() {
        setup:
        for (int i=0; i<10; i++) {
            PageBlobURL bu = cu.createPageBlobURL(generateBlobName())
            bu.create(512, null, null, null, null).blockingGet()
        }

        ContainersListBlobFlatSegmentResponse response = cu.listBlobsFlatSegment(null,
                new ListBlobsOptions(null, null, 6))
                .blockingGet()
        String marker = response.body().nextMarker()
        int firstSegmentSize = response.body().blobs().blob().size()
        response = cu.listBlobsFlatSegment(marker, null).blockingGet()

        expect:
        firstSegmentSize == 6
        response.body().nextMarker() == null
        response.body().blobs().blob().size() == 4
    }

    def "Container list blobs hierarchy"() {
        setup:
        String name = generateBlobName()
        PageBlobURL bu = cu.createPageBlobURL(name)
        bu.create(512, null, null, null, null).blockingGet()

        when:
        ContainersListBlobHierarchySegmentResponse response =
                cu.listBlobsHierarchySegment(null, "/", null)
                .blockingGet()
        ContainersListBlobHierarchySegmentHeaders headers = response.headers()
        List<Blob> blobs = response.body().blobs().blob()

        then:
        response.statusCode() == 200
        headers.contentType() != null
        headers.requestId() != null
        headers.version() != null
        headers.dateProperty() != null
        blobs.size() == 1
        blobs.get(0).name() == name
    }

    @Unroll
    def "Container list blobs hier options"(){
        setup:
        ListBlobsOptions options = new ListBlobsOptions(
                new BlobListingDetails(copy, metadata, false, uncommitted),
                prefix, maxResults)

        String normalName = "a" + generateBlobName()
        PageBlobURL normal = cu.createPageBlobURL(normalName)
        normal.create(512, null, null, null, null).blockingGet()

        String copyName = "c" + generateBlobName()
        PageBlobURL copyBlob = cu.createPageBlobURL(copyName)
        waitForCopy(copyBlob, copyBlob.startCopyFromURL(normal.toURL(),
                null, null, null).blockingGet())

        String metadataName = "m" + generateBlobName()
        PageBlobURL metadataBlob = cu.createPageBlobURL(metadataName)
        Metadata values = new Metadata()
        values.put("foo", "bar")
        metadataBlob.create(512, null, null, values, null).blockingGet()

        String snapshotTime = normal.createSnapshot(null, null)
                .blockingGet().headers().snapshot()

        String uncommittedName = "u" + generateBlobName()
        BlockBlobURL uncommittedBlob = cu.createBlockBlobURL(uncommittedName)
        uncommittedBlob.stageBlock("0000", Flowable.just(defaultData), defaultData.remaining(),
                null).blockingGet()

        when:
        List<Blob> blobs = cu.listBlobsHierarchySegment(null, "none", options).blockingGet()
                .body().blobs().blob()

        then:
        if (copy) {
            blobs.get(0).name() == normalName
            blobs.get(1).name() == copyName
            blobs.get(1).properties().copyId() != null
            blobs.get(1).properties().copySource() == normalName
            blobs.get(1).properties().copyStatus() == CopyStatusType.SUCCESS // We waited for the copy to complete.
            blobs.get(1).properties().copyProgress() != null
            blobs.get(1).properties().copyCompletionTime() != null
            blobs.size() == 3 // Normal, copy, metadata
        }
        else if (metadata) {
            blobs.get(0).name() == normalName
            blobs.get(1).name() == copyName
            blobs.get(1).properties().copyCompletionTime() == null
            blobs.get(2).name() == metadataName
            blobs.get(2).metadata().get("foo") == "bar"
            blobs.size() == 3 // Normal, copy, metadata
        }
        else if (uncommitted) {
            blobs.get(0).name() == normalName
            blobs.get(2).name() == uncommittedName
            blobs.size() == 4 // Normal, copy, metadata, uncommitted
        }
        else if (prefix != null) {
            blobs.get(0).name() == normalName
            blobs.size() == 1 // Normal
        }

        if (maxResults != null) {
            blobs.size() == maxResults
        }

        where:
        copy  | metadata | uncommitted | prefix | maxResults
        true  | false    | false       | null   | null
        false | true     | false       | null   | null
        false | false    | false       | null   | null
        false | false    | true        | null   | null
        false | false    | false       | "a"    | null
        true  | false    | true        | null   | 2
    }

    def "Container list blobs hier delim"() {
        setup:
        AppendBlobURL blob = cu.createAppendBlobURL("a")
        blob.create(null, null, null).blockingGet()
        AppendBlobURL dir = cu.createAppendBlobURL("b/")
        dir.create(null, null, null).blockingGet()
        AppendBlobURL subBlob = cu.createAppendBlobURL("b/c")
        subBlob.create(null, null, null).blockingGet()

        when:
        ContainersListBlobHierarchySegmentResponse response =
                cu.listBlobsHierarchySegment(null, "/", null).blockingGet()

        then:
        response.body().blobs().blobPrefix().size() == 1
        response.body().blobs().blobPrefix().get(0).name() == "b/"
        response.body().blobs().blob().size() == 1
        response.body().blobs().blob().get(0).name() == "a"
    }

    def "Container list blobs hier marker"() {
        setup:
        for (int i=0; i<10; i++) {
            PageBlobURL bu = cu.createPageBlobURL(generateBlobName())
            bu.create(512, null, null, null, null).blockingGet()
        }

        ContainersListBlobHierarchySegmentResponse response = cu.listBlobsHierarchySegment(null, "/",
                new ListBlobsOptions(null, null, 6))
                .blockingGet()
        String marker = response.body().nextMarker()
        int firstSegmentSize = response.body().blobs().blob().size()
        response = cu.listBlobsHierarchySegment(marker, "/", null).blockingGet()

        expect:
        firstSegmentSize == 6
        response.body().nextMarker() == null
        response.body().blobs().blob().size() == 4
    }

    @Unroll
    def "Container acquire lease"() {
        setup:
        ContainersAcquireLeaseHeaders headers =
                cu.acquireLease(UUID.randomUUID().toString(), leaseTime, null).blockingGet().headers()

        when:
        ContainersGetPropertiesHeaders properties = cu.getProperties(null).blockingGet()
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

    @Unroll
    def "Container acquire lease AC"() {
        setup:
        HTTPAccessConditions hac = new HTTPAccessConditions(modified, unmodified, null, null)

        expect:
        cu.acquireLease(null, -1, hac).blockingGet().statusCode() == 201

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    def "Container renew lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        Thread.sleep(16000) // Wait for the lease to expire to ensure we are actually renewing it
        ContainersRenewLeaseHeaders headers = cu.renewLease(leaseID, null).blockingGet().headers()

        expect:
        cu.getProperties(null).blockingGet().headers().leaseState() == LeaseStateType.LEASED
        validateBasicHeaders(headers)
        headers.leaseId() != null
    }

    @Unroll
    def "Container renew lease AC"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        HTTPAccessConditions hac = new HTTPAccessConditions(modified, unmodified, null, null)

        expect:
        cu.renewLease(leaseID, hac).blockingGet().statusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    def "Container release lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        ContainersReleaseLeaseHeaders headers = cu.releaseLease(leaseID, null).blockingGet().headers()

        expect:
        cu.getProperties(null).blockingGet().headers().leaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(headers)
    }

    @Unroll
    def "Container release lease AC"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        HTTPAccessConditions hac = new HTTPAccessConditions(modified, unmodified, null, null)

        expect:
        cu.releaseLease(leaseID, hac).blockingGet().statusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Container break lease"() {
        setup:
        cu.acquireLease(UUID.randomUUID().toString(), leaseTime, null).blockingGet()

        ContainersBreakLeaseHeaders headers = cu.breakLease(breakPeriod, null).blockingGet().headers()
        LeaseStateType state = cu.getProperties(null).blockingGet().headers().leaseState()

        expect:
        state == LeaseStateType.BROKEN || state == LeaseStateType.BREAKING
        headers.leaseTime() <= remainingTime
        validateBasicHeaders(headers)
        if (breakPeriod != null) {
            sleep(breakPeriod*1000) // so we can delete the container after the test completes
        }

        where:
        leaseTime | breakPeriod | remainingTime
        -1        | null        | 0
        -1        | 20          | 25
        20        | 15          | 16

    }

    @Unroll
    def "Container break lease AC"() {
        setup:
        setupContainerLeaseCondition(cu, receivedLeaseID)
        HTTPAccessConditions hac = new HTTPAccessConditions(modified, unmodified, null, null)

        expect:
        cu.breakLease(null, hac).blockingGet().statusCode() == 202

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    def "Container change lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        ContainersChangeLeaseHeaders headers =
                cu.changeLease(leaseID, UUID.randomUUID().toString(), null)
                .blockingGet().headers()
        leaseID = headers.leaseId()

        expect:
        cu.releaseLease(leaseID, null).blockingGet().statusCode() == 200
        validateBasicHeaders(headers)
    }

    @Unroll
    def "Container change lease AC"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        HTTPAccessConditions hac = new HTTPAccessConditions(modified, unmodified, null, null)

        expect:
        cu.changeLease(leaseID, UUID.randomUUID().toString(), hac).blockingGet().statusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }
}
