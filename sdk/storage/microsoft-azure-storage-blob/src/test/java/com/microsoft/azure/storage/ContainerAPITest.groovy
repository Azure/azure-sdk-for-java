// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.*
import com.microsoft.rest.v2.http.HttpPipeline
import com.microsoft.rest.v2.http.HttpRequest
import com.microsoft.rest.v2.http.HttpResponse
import com.microsoft.rest.v2.policy.RequestPolicy
import com.microsoft.rest.v2.policy.RequestPolicyFactory
import com.microsoft.rest.v2.policy.RequestPolicyOptions
import io.reactivex.Flowable
import io.reactivex.Single
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.ZoneId

class ContainerAPITest extends APISpec {

    def "Create all null"() {
        setup:
        // Overwrite the existing cu, which has already been created
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        ContainerCreateResponse response = cu.create(null, null, null).blockingGet()

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
    }

    def "Create min"() {
        expect:
        primaryServiceURL.createContainerURL(generateContainerName()).create().blockingGet().statusCode() == 201
    }

    @Unroll
    def "Create metadata"() {
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
        cu.create(metadata, null, null).blockingGet()
        ContainerGetPropertiesResponse response = cu.getProperties(null, null).blockingGet()

        then:
        response.headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Create publicAccess"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.create(null, publicAccess, null).blockingGet()
        PublicAccessType access =
                cu.getProperties(null, null).blockingGet().headers().blobPublicAccess()

        then:
        access.toString() == publicAccess.toString()

        where:
        publicAccess               | _
        PublicAccessType.BLOB      | _
        PublicAccessType.CONTAINER | _
        null                       | _
    }

    def "Create error"() {
        when:
        cu.create(null, null, null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.response().statusCode() == 409
        e.errorCode() == StorageErrorCode.CONTAINER_ALREADY_EXISTS
        e.message().contains("The specified container already exists.")
    }

    def "Create context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(201, ContainerCreateHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.create(null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Get properties null"() {
        when:
        ContainerGetPropertiesHeaders headers =
                cu.getProperties(null, null).blockingGet().headers()

        then:
        validateBasicHeaders(headers)
        headers.blobPublicAccess() == null
        headers.leaseDuration() == null
        headers.leaseState() == LeaseStateType.AVAILABLE
        headers.leaseStatus() == LeaseStatusType.UNLOCKED
        headers.metadata().size() == 0
        !headers.hasImmutabilityPolicy()
        !headers.hasLegalHold()
    }

    def "Get properties min"() {
        expect:
        cu.getProperties().blockingGet().statusCode() == 200
    }

    def "Get properties lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.getProperties(new LeaseAccessConditions().withLeaseId(leaseID), null).blockingGet().statusCode() == 200
    }

    def "Get properties lease fail"() {
        when:
        cu.getProperties(new LeaseAccessConditions().withLeaseId("garbage"), null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get properties error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.getProperties(null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get properties context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ContainerGetPropertiesHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.getProperties(null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Set metadata"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())
        Metadata metadata = new Metadata()
        metadata.put("key", "value")
        cu.create(metadata, null, null).blockingGet()
        ContainerSetMetadataResponse response = cu.setMetadata(null, null, null).blockingGet()

        expect:
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        cu.getProperties(null, null).blockingGet().headers().metadata().size() == 0
    }

    def "Set metadata min"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")

        when:
        cu.setMetadata(metadata).blockingGet()

        then:
        cu.getProperties().blockingGet().headers().metadata() == metadata
    }

    @Unroll
    def "Set metadata metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        expect:
        cu.setMetadata(metadata, null, null).blockingGet().statusCode() == 200
        cu.getProperties(null, null).blockingGet().headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Set metadata AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cu, leaseID)
        ContainerAccessConditions cac = new ContainerAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        cu.setMetadata(null, cac, null).blockingGet().statusCode() == 200

        where:
        modified | leaseID
        null     | null
        oldDate  | null
        null     | receivedLeaseID
    }

    @Unroll
    def "Set metadata AC fail"() {
        setup:
        ContainerAccessConditions cac = new ContainerAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        cu.setMetadata(null, cac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | leaseID
        newDate  | null
        null     | garbageLeaseID
    }

    @Unroll
    def "Set metadata AC illegal"() {
        setup:
        ModifiedAccessConditions mac = new ModifiedAccessConditions().withIfUnmodifiedSince(unmodified)
                .withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        cu.setMetadata(null, new ContainerAccessConditions().withModifiedAccessConditions(mac), null)

        then:
        thrown(UnsupportedOperationException)

        where:
        unmodified | match        | noneMatch
        newDate    | null         | null
        null       | receivedEtag | null
        null       | null         | garbageEtag
    }

    def "Set metadata error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.setMetadata(null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Set metadata context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ContainerSetMetadataHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.setMetadata(null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    @Unroll
    def "Set access policy"() {
        setup:
        def response = cu.setAccessPolicy(access, null, null, null).blockingGet()

        expect:
        validateBasicHeaders(response.headers())
        cu.getProperties(null, null).blockingGet()
                .headers().blobPublicAccess() == access

        where:
        access                     | _
        PublicAccessType.BLOB      | _
        PublicAccessType.CONTAINER | _
        null                       | _
    }

    def "Set access policy min access"() {
        when:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null).blockingGet()

        then:
        cu.getProperties().blockingGet().headers().blobPublicAccess() == PublicAccessType.CONTAINER
    }

    def "Set access policy min ids"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
                .withId("0000")
                .withAccessPolicy(new AccessPolicy()
                .withStart(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .withExpiry(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                .plusDays(1))
                .withPermission("r"))

        List<SignedIdentifier> ids = new ArrayList<>()
        ids.push(identifier)

        when:
        cu.setAccessPolicy(null, ids).blockingGet()

        then:
        cu.getAccessPolicy(null, null).blockingGet().body().get(0).id() == "0000"
    }

    def "Set access policy ids"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
                .withId("0000")
                .withAccessPolicy(new AccessPolicy()
                .withStart(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .withExpiry(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                .plusDays(1))
                .withPermission("r"))
        SignedIdentifier identifier2 = new SignedIdentifier()
                .withId("0001")
                .withAccessPolicy(new AccessPolicy()
                .withStart(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .withExpiry(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                .plusDays(2))
                .withPermission("w"))
        List<SignedIdentifier> ids = new ArrayList<>()
        ids.push(identifier)
        ids.push(identifier2)

        when:
        ContainerSetAccessPolicyResponse response =
                cu.setAccessPolicy(null, ids, null, null).blockingGet()
        List<SignedIdentifier> receivedIdentifiers = cu.getAccessPolicy(null, null).blockingGet().body()

        then:
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        receivedIdentifiers.get(0).accessPolicy().expiry() == identifier.accessPolicy().expiry()
        receivedIdentifiers.get(0).accessPolicy().start() == identifier.accessPolicy().start()
        receivedIdentifiers.get(0).accessPolicy().permission() == identifier.accessPolicy().permission()
        receivedIdentifiers.get(1).accessPolicy().expiry() == identifier2.accessPolicy().expiry()
        receivedIdentifiers.get(1).accessPolicy().start() == identifier2.accessPolicy().start()
        receivedIdentifiers.get(1).accessPolicy().permission() == identifier2.accessPolicy().permission()
    }

    @Unroll
    def "Set access policy AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cu, leaseID)
        ContainerAccessConditions cac = new ContainerAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        cu.setAccessPolicy(null, null, cac, null).blockingGet().statusCode() == 200

        where:
        modified | unmodified | leaseID
        null     | null       | null
        oldDate  | null       | null
        null     | newDate    | null
        null     | null       | receivedLeaseID
    }

    @Unroll
    def "Set access policy AC fail"() {
        setup:
        ContainerAccessConditions cac = new ContainerAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        cu.setAccessPolicy(null, null, cac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified | leaseID
        newDate  | null       | null
        null     | oldDate    | null
        null     | null       | garbageLeaseID
    }

    @Unroll
    def "Set access policy AC illegal"() {
        setup:
        ModifiedAccessConditions mac = new ModifiedAccessConditions().withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        cu.setAccessPolicy(null, null, new ContainerAccessConditions().withModifiedAccessConditions(mac), null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Set access policy error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.setAccessPolicy(null, null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Set access policy context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ContainerSetAccessPolicyHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.setAccessPolicy(null, null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Get access policy"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
                .withId("0000")
                .withAccessPolicy(new AccessPolicy()
                .withStart(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .withExpiry(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                .plusDays(1))
                .withPermission("r"))
        List<SignedIdentifier> ids = new ArrayList<>()
        ids.push(identifier)
        cu.setAccessPolicy(PublicAccessType.BLOB, ids, null, null).blockingGet()
        ContainerGetAccessPolicyResponse response = cu.getAccessPolicy(null, null).blockingGet()

        expect:
        response.statusCode() == 200
        response.headers().blobPublicAccess() == PublicAccessType.BLOB
        validateBasicHeaders(response.headers())
        response.body().get(0).accessPolicy().expiry() == identifier.accessPolicy().expiry()
        response.body().get(0).accessPolicy().start() == identifier.accessPolicy().start()
        response.body().get(0).accessPolicy().permission() == identifier.accessPolicy().permission()
    }

    def "Get access policy lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.getAccessPolicy(new LeaseAccessConditions().withLeaseId(leaseID), null).blockingGet().statusCode() == 200
    }

    def "Get access policy lease fail"() {
        when:
        cu.getAccessPolicy(new LeaseAccessConditions().withLeaseId(garbageLeaseID), null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get access policy error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.getAccessPolicy(null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get access policy context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ContainerGetAccessPolicyHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.getAccessPolicy(null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Delete"() {
        when:
        ContainerDeleteResponse response = cu.delete(null, null).blockingGet()

        then:
        response.statusCode() == 202
        response.headers().requestId() != null
        response.headers().version() != null
        response.headers().date() != null
    }

    def "Delete min"() {
        expect:
        cu.delete().blockingGet().statusCode() == 202
    }

    @Unroll
    def "Delete AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cu, leaseID)
        ContainerAccessConditions cac = new ContainerAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))


        expect:
        cu.delete(cac, null).blockingGet().statusCode() == 202

        where:
        modified | unmodified | leaseID
        null     | null       | null
        oldDate  | null       | null
        null     | newDate    | null
        null     | null       | receivedLeaseID
    }

    @Unroll
    def "Delete AC fail"() {
        setup:
        ContainerAccessConditions cac = new ContainerAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        cu.delete(cac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified | leaseID
        newDate  | null       | null
        null     | oldDate    | null
        null     | null       | garbageLeaseID
    }

    @Unroll
    def "Delete AC illegal"() {
        setup:
        ModifiedAccessConditions mac = new ModifiedAccessConditions().withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        cu.delete(new ContainerAccessConditions().withModifiedAccessConditions(mac), null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Delete error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.delete(null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Delete context"() {
        setup:
        def pipeline = HttpPipeline.build(getStubFactory(getContextStubPolicy(202, ContainerDeleteHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.delete(null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "List blobs flat"() {
        setup:
        String name = generateBlobName()
        PageBlobURL bu = cu.createPageBlobURL(name)
        bu.create(512, null, null, null, null, null).blockingGet()

        when:
        ContainerListBlobFlatSegmentResponse response = cu.listBlobsFlatSegment(null, null, null)
                .blockingGet()
        ContainerListBlobFlatSegmentHeaders headers = response.headers()
        List<BlobItem> blobs = response.body().segment().blobItems()

        then:
        response.statusCode() == 200
        headers.contentType() != null
        headers.requestId() != null
        headers.version() != null
        headers.date() != null
        blobs.size() == 1
        blobs.get(0).name() == name
        blobs.get(0).properties().blobType() == BlobType.PAGE_BLOB
        blobs.get(0).properties().copyCompletionTime() == null
        blobs.get(0).properties().copyStatusDescription() == null
        blobs.get(0).properties().copyId() == null
        blobs.get(0).properties().copyProgress() == null
        blobs.get(0).properties().copySource() == null
        blobs.get(0).properties().copyStatus() == null
        blobs.get(0).properties().incrementalCopy() == null
        blobs.get(0).properties().destinationSnapshot() == null
        blobs.get(0).properties().leaseDuration() == null
        blobs.get(0).properties().leaseState() == LeaseStateType.AVAILABLE
        blobs.get(0).properties().leaseStatus() == LeaseStatusType.UNLOCKED
        blobs.get(0).properties().contentLength() != null
        blobs.get(0).properties().contentType() != null
        blobs.get(0).properties().contentMD5() == null
        blobs.get(0).properties().contentEncoding() == null
        blobs.get(0).properties().contentDisposition() == null
        blobs.get(0).properties().contentLanguage() == null
        blobs.get(0).properties().cacheControl() == null
        blobs.get(0).properties().blobSequenceNumber() == 0
        blobs.get(0).properties().serverEncrypted()
        blobs.get(0).properties().accessTierInferred()
        blobs.get(0).properties().accessTier() == AccessTier.HOT
        blobs.get(0).properties().archiveStatus() == null
        blobs.get(0).properties().creationTime() != null
    }

    def "List blobs flat min"() {
        expect:
        cu.listBlobsFlatSegment(null, null).blockingGet().statusCode() == 200
    }

    def setupListBlobsTest(String normalName, String copyName, String metadataName, String uncommittedName) {
        PageBlobURL normal = cu.createPageBlobURL(normalName)
        normal.create(512, null, null, null, null, null).blockingGet()

        PageBlobURL copyBlob = cu.createPageBlobURL(copyName)
        waitForCopy(copyBlob, copyBlob.startCopyFromURL(normal.toURL(),
                null, null, null, null).blockingGet().headers().copyStatus())

        PageBlobURL metadataBlob = cu.createPageBlobURL(metadataName)
        Metadata values = new Metadata()
        values.put("foo", "bar")
        metadataBlob.create(512, null, null, values, null, null).blockingGet()

        String snapshotTime = normal.createSnapshot(null, null, null)
                .blockingGet().headers().snapshot()

        BlockBlobURL uncommittedBlob = cu.createBlockBlobURL(uncommittedName)

        uncommittedBlob.stageBlock("0000", Flowable.just(defaultData), defaultData.remaining(),
                null, null).blockingGet()

        return snapshotTime
    }

    def "List blobs flat options copy"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().withDetails(new BlobListDetails().withCopy(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, options, null).blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == normalName
        blobs.get(1).name() == copyName
        blobs.get(1).properties().copyId() != null
        // Comparing the urls isn't reliable because the service may use https.
        blobs.get(1).properties().copySource().contains(normalName)
        blobs.get(1).properties().copyStatus() == CopyStatusType.SUCCESS // We waited for the copy to complete.
        blobs.get(1).properties().copyProgress() != null
        blobs.get(1).properties().copyCompletionTime() != null
        blobs.size() == 3 // Normal, copy, metadata
    }

    def "List blobs flat options metadata"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().withDetails(new BlobListDetails().withMetadata(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, options, null).blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == normalName
        blobs.get(1).name() == copyName
        blobs.get(1).properties().copyCompletionTime() == null
        blobs.get(2).name() == metadataName
        blobs.get(2).metadata().get("foo") == "bar"
        blobs.size() == 3 // Normal, copy, metadata
    }

    def "List blobs flat options snapshots"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().withDetails(new BlobListDetails().withSnapshots(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        String snapshotTime = setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, options, null).blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == normalName
        blobs.get(0).snapshot() == snapshotTime
        blobs.get(1).name() == normalName
        blobs.size() == 4 // Normal, snapshot, copy, metadata
    }

    def "List blobs flat options uncommitted"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().withDetails(new BlobListDetails()
                .withUncommittedBlobs(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, options, null).blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == normalName
        blobs.get(3).name() == uncommittedName
        blobs.size() == 4 // Normal, copy, metadata, uncommitted
    }

    def "List blobs flat options deleted"() {
        setup:
        enableSoftDelete()
        String name = generateBlobName()
        AppendBlobURL bu = cu.createAppendBlobURL(name)
        bu.create(null, null, null, null).blockingGet()
        bu.delete(null, null, null).blockingGet()

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, new ListBlobsOptions().withDetails(new BlobListDetails()
                .withDeletedBlobs(true)), null).blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == name
        blobs.size() == 1

        disableSoftDelete() == null // Must produce a true value or test will fail.
    }

    def "List blobs flat options prefix"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().withPrefix("a")
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, options, null).blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == normalName
        blobs.size() == 1 // Normal
    }

    def "List blobs flat options maxResults"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().withDetails(new BlobListDetails().withCopy(true)
                .withSnapshots(true).withUncommittedBlobs(true)).withMaxResults(2)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, options, null).blockingGet().body().segment().blobItems()

        then:
        blobs.size() == 2
    }

    def "List blobs flat options fail"() {
        when:
        new ListBlobsOptions().withMaxResults(0)

        then:
        thrown(IllegalArgumentException)
    }

    def "List blobs flat marker"() {
        setup:
        for (int i = 0; i < 10; i++) {
            PageBlobURL bu = cu.createPageBlobURL(generateBlobName())
            bu.create(512, null, null, null, null, null).blockingGet()
        }

        ContainerListBlobFlatSegmentResponse response = cu.listBlobsFlatSegment(null,
                new ListBlobsOptions().withMaxResults(6), null)
                .blockingGet()
        String marker = response.body().nextMarker()
        int firstSegmentSize = response.body().segment().blobItems().size()
        response = cu.listBlobsFlatSegment(marker, null, null).blockingGet()

        expect:
        firstSegmentSize == 6
        response.body().nextMarker() == null
        response.body().segment().blobItems().size() == 4
    }

    def "List blobs flat error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.listBlobsFlatSegment(null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "List blobs flat context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ContainerListBlobFlatSegmentHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.listBlobsFlatSegment(null, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "List blobs hierarchy"() {
        setup:
        String name = generateBlobName()
        PageBlobURL bu = cu.createPageBlobURL(name)
        bu.create(512, null, null, null, null, null).blockingGet()

        when:
        ContainerListBlobHierarchySegmentResponse response =
                cu.listBlobsHierarchySegment(null, "/", null, null)
                        .blockingGet()
        ContainerListBlobHierarchySegmentHeaders headers = response.headers()
        List<BlobItem> blobs = response.body().segment().blobItems()

        then:
        response.statusCode() == 200
        headers.contentType() != null
        headers.requestId() != null
        headers.version() != null
        headers.date() != null
        blobs.size() == 1
        blobs.get(0).name() == name
    }

    def "List blobs hierarchy min"() {
        expect:
        cu.listBlobsHierarchySegment(null, "/", null).blockingGet().statusCode() == 200
    }

    def "List blobs hier options copy"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().withDetails(new BlobListDetails().withCopy(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsHierarchySegment(null, "", options, null)
                .blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == normalName
        blobs.get(1).name() == copyName
        blobs.get(1).properties().copyId() != null
        // Comparing the urls isn't reliable because the service may use https.
        blobs.get(1).properties().copySource().contains(normalName)
        blobs.get(1).properties().copyStatus() == CopyStatusType.SUCCESS // We waited for the copy to complete.
        blobs.get(1).properties().copyProgress() != null
        blobs.get(1).properties().copyCompletionTime() != null
        blobs.size() == 3 // Normal, copy, metadata
    }

    def "List blobs hier options metadata"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().withDetails(new BlobListDetails().withMetadata(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsHierarchySegment(null, "", options, null)
                .blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == normalName
        blobs.get(1).name() == copyName
        blobs.get(1).properties().copyCompletionTime() == null
        blobs.get(2).name() == metadataName
        blobs.get(2).metadata().get("foo") == "bar"
        blobs.size() == 3 // Normal, copy, metadata
    }

    def "List blobs hier options uncommitted"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().withDetails(new BlobListDetails()
                .withUncommittedBlobs(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsHierarchySegment(null, "", options, null)
                .blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == normalName
        blobs.get(3).name() == uncommittedName
        blobs.size() == 4 // Normal, copy, metadata, uncommitted
    }

    def "List blobs hier options deleted"() {
        setup:
        enableSoftDelete()
        String name = generateBlobName()
        AppendBlobURL bu = cu.createAppendBlobURL(name)
        bu.create(null, null, null, null).blockingGet()
        bu.delete(null, null, null).blockingGet()

        when:
        List<BlobItem> blobs = cu.listBlobsHierarchySegment(null, "",
                new ListBlobsOptions().withDetails(new BlobListDetails().withDeletedBlobs(true)), null).blockingGet()
                .body().segment().blobItems()

        then:
        blobs.get(0).name() == name
        blobs.size() == 1

        disableSoftDelete() == null
    }

    def "List blobs hier options prefix"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().withPrefix("a")
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsHierarchySegment(null, "", options, null)
                .blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == normalName
        blobs.size() == 1 // Normal
    }

    def "List blobs hier options maxResults"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().withDetails(new BlobListDetails().withCopy(true)
                .withUncommittedBlobs(true)).withMaxResults(1)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsHierarchySegment(null, "", options, null)
                .blockingGet().body().segment().blobItems()

        then:
        blobs.size() == 1
    }

    @Unroll
    def "List blobs hier options fail"() {
        when:
        def options = new ListBlobsOptions().withDetails(new BlobListDetails().withSnapshots(snapshots))
                .withMaxResults(maxResults)
        cu.listBlobsHierarchySegment(null, null, options, null)

        then:
        def e = thrown(Exception)
        exceptionType.isInstance(e)

        where:
        snapshots | maxResults | exceptionType
        true      | 5          | UnsupportedOperationException
        false     | 0          | IllegalArgumentException
    }

    def "List blobs hier delim"() {
        setup:
        def blobNames = Arrays.asList("a", "b/a", "c", "d/a", "e", "f", "g/a")
        for (String blobName : blobNames) {
            def bu = cu.createAppendBlobURL(blobName)
            bu.create().blockingGet()
        }

        when:
        ContainerListBlobHierarchySegmentResponse response =
                cu.listBlobsHierarchySegment(null, "/", null, null).blockingGet()

        and:
        def expectedBlobs = Arrays.asList("a", "c", "e", "f")
        def expectedPrefixes = Arrays.asList("b/", "d/", "g/")

        then:
        response.body().segment().blobItems().size() == 4
        for (int i=0; i<expectedBlobs.size(); i++) {
            assert expectedBlobs.get(i) == response.body().segment().blobItems().get(i).name()
        }
        for (int i=0; i<expectedPrefixes.size(); i++) {
            assert expectedPrefixes.get(i) == response.body().segment().blobPrefixes().get(i).name()
        }
        response.body().segment().blobPrefixes().size() == 3
    }

    def "List blobs hier marker"() {
        setup:
        for (int i = 0; i < 10; i++) {
            PageBlobURL bu = cu.createPageBlobURL(generateBlobName())
            bu.create(512, null, null, null, null, null).blockingGet()
        }

        ContainerListBlobHierarchySegmentResponse response = cu.listBlobsHierarchySegment(null, "/",
                new ListBlobsOptions().withMaxResults(6), null)
                .blockingGet()
        String marker = response.body().nextMarker()
        int firstSegmentSize = response.body().segment().blobItems().size()
        response = cu.listBlobsHierarchySegment(marker, "/", null, null).blockingGet()

        expect:
        firstSegmentSize == 6
        response.body().nextMarker() == null
        response.body().segment().blobItems().size() == 4
    }

    def "List blobs hier error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.listBlobsHierarchySegment(null, ".", null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "List blobs hier context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ContainerListBlobHierarchySegmentHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.listBlobsHierarchySegment(null, "/", null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    @Unroll
    def "Acquire lease"() {
        setup:
        ContainerAcquireLeaseHeaders headers =
                cu.acquireLease(proposedID, leaseTime, null, null).blockingGet().headers()

        when:
        ContainerGetPropertiesHeaders properties = cu.getProperties(null, null).blockingGet()
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

    def "Acquire lease min"() {
        expect:
        cu.acquireLease(null, -1).blockingGet().statusCode() == 201
    }

    @Unroll
    def "Acquire lease AC"() {
        setup:
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)

        expect:
        cu.acquireLease(null, -1, mac, null).blockingGet().statusCode() == 201

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Acquire lease AC fail"() {
        setup:
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)

        when:
        cu.acquireLease(null, -1, mac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Acquire lease AC illegal"() {
        setup:
        ModifiedAccessConditions mac = new ModifiedAccessConditions().withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        cu.acquireLease(null, -1, mac, null).blockingGet()

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Acquire lease error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.acquireLease(null, 50, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Acquire lease context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(201, ContainerAcquireLeaseHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.acquireLease(null, 20, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Renew lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        Thread.sleep(16000) // Wait for the lease to expire to ensure we are actually renewing it
        ContainerRenewLeaseHeaders headers = cu.renewLease(leaseID, null, null).blockingGet().headers()

        expect:
        cu.getProperties(null, null).blockingGet().headers().leaseState() == LeaseStateType.LEASED
        validateBasicHeaders(headers)
        headers.leaseId() != null
    }

    def "Renew lease min"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.renewLease(leaseID).blockingGet().statusCode() == 200
    }

    @Unroll
    def "Renew lease AC"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)

        expect:
        cu.renewLease(leaseID, mac, null).blockingGet().statusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Renew lease AC fail"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)

        when:
        cu.renewLease(leaseID, mac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Renew lease AC illegal"() {
        setup:
        ModifiedAccessConditions mac = new ModifiedAccessConditions().withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        cu.renewLease(receivedLeaseID, mac, null).blockingGet()

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Renew lease error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.renewLease("id", null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Renew lease context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ContainerRenewLeaseHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.renewLease("id", null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Release lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        ContainerReleaseLeaseHeaders headers = cu.releaseLease(leaseID, null, null).blockingGet().headers()

        expect:
        cu.getProperties(null, null).blockingGet().headers().leaseState() == LeaseStateType.AVAILABLE
        validateBasicHeaders(headers)
    }

    def "Release lease min"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.releaseLease(leaseID).blockingGet().statusCode() == 200
    }

    @Unroll
    def "Release lease AC"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)

        expect:
        cu.releaseLease(leaseID, mac, null).blockingGet().statusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Release lease AC fail"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)

        when:
        cu.releaseLease(leaseID, mac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Release lease AC illegal"() {
        setup:
        ModifiedAccessConditions mac = new ModifiedAccessConditions().withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        cu.releaseLease(receivedLeaseID, mac, null).blockingGet()

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Release lease error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.releaseLease("id", null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Release lease context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ContainerReleaseLeaseHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.releaseLease("id", null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    @Unroll
    def "Break lease"() {
        setup:
        cu.acquireLease(UUID.randomUUID().toString(), leaseTime, null, null).blockingGet()

        ContainerBreakLeaseHeaders headers = cu.breakLease(breakPeriod, null, null).blockingGet().headers()
        LeaseStateType state = cu.getProperties(null, null).blockingGet().headers().leaseState()

        expect:
        state == LeaseStateType.BROKEN || state == LeaseStateType.BREAKING
        headers.leaseTime() <= remainingTime
        validateBasicHeaders(headers)
        if (breakPeriod != null) {
            sleep(breakPeriod * 1000) // so we can delete the container after the test completes
        }

        where:
        leaseTime | breakPeriod | remainingTime
        -1        | null        | 0
        -1        | 20          | 25
        20        | 15          | 16

    }

    def "Break lease min"() {
        setup:
        setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.breakLease().blockingGet().statusCode() == 202
    }

    @Unroll
    def "Break lease AC"() {
        setup:
        setupContainerLeaseCondition(cu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)

        expect:
        cu.breakLease(null, mac, null).blockingGet().statusCode() == 202

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Break lease AC fail"() {
        setup:
        setupContainerLeaseCondition(cu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)

        when:
        cu.breakLease(null, mac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Break lease AC illegal"() {
        setup:
        ModifiedAccessConditions mac = new ModifiedAccessConditions().withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        cu.breakLease(null, mac, null).blockingGet()

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Break lease error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.breakLease(null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Break lease context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(202, ContainerBreakLeaseHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.breakLease(20, null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    def "Change lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        ContainerChangeLeaseHeaders headers =
                cu.changeLease(leaseID, UUID.randomUUID().toString(), null, null)
                        .blockingGet().headers()
        leaseID = headers.leaseId()

        expect:
        cu.releaseLease(leaseID, null, null).blockingGet().statusCode() == 200
        validateBasicHeaders(headers)
    }

    def "Change lease min"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.changeLease(leaseID, UUID.randomUUID().toString()).blockingGet().statusCode() == 200
    }

    @Unroll
    def "Change lease AC"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)

        expect:
        cu.changeLease(leaseID, UUID.randomUUID().toString(), mac, null).blockingGet().statusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Change lease AC fail"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)

        when:
        cu.changeLease(leaseID, UUID.randomUUID().toString(), mac, null).blockingGet()

        then:
        thrown(StorageException)

        where:
        modified | unmodified
        newDate  | null
        null     | oldDate
    }

    @Unroll
    def "Change lease AC illegal"() {
        setup:
        ModifiedAccessConditions mac = new ModifiedAccessConditions().withIfMatch(match).withIfNoneMatch(noneMatch)

        when:
        cu.changeLease(receivedLeaseID, garbageLeaseID, mac, null).blockingGet()

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Change lease error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.changeLease("id", "id", null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Change lease context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ContainerChangeLeaseHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.changeLease("id", "id", null, defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }

    @Unroll
    def "Create URL special chars"() {
        // This test checks that we encode special characters in blob names correctly.
        setup:
        AppendBlobURL bu2 = cu.createAppendBlobURL(name)
        PageBlobURL bu3 = cu.createPageBlobURL(name + "2")
        BlockBlobURL bu4 = cu.createBlockBlobURL(name + "3")
        BlobURL bu5 = cu.createBlockBlobURL(name)

        expect:
        bu2.create(null, null, null, null).blockingGet().statusCode() == 201
        bu5.getProperties(null, null).blockingGet().statusCode() == 200
        bu3.create(512, null, null, null, null, null).blockingGet()
                .statusCode() == 201
        bu4.upload(defaultFlowable, defaultDataSize,
                null, null, null, null).blockingGet().statusCode() == 201

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, null, null).blockingGet()
                .body().segment().blobItems()

        then:
        blobs.get(0).name() == name
        blobs.get(1).name() == name + "2"
        blobs.get(2).name() == name + "3"

        where:
        name                  | _
        "中文"                  | _
        "az[]"                | _
        "hello world"         | _
        "hello/world"         | _
        "hello&world"         | _
        "!*'();:@&=+\$,/?#[]" | _
    }

    def "Root explicit"() {
        setup:
        cu = primaryServiceURL.createContainerURL(ContainerURL.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        try {
            cu.create(null, null, null).blockingGet()
        }
        catch (StorageException se) {
            if (se.errorCode() != StorageErrorCode.CONTAINER_ALREADY_EXISTS) {
                throw se
            }
        }
        BlobURL bu = cu.createAppendBlobURL("rootblob")

        expect:
        bu.create(null, null, null, null).blockingGet().statusCode() == 201
    }

    def "Root implicit"() {
        setup:
        cu = primaryServiceURL.createContainerURL(ContainerURL.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        try {
            cu.create(null, null, null).blockingGet()
        }
        catch (StorageException se) {
            if (se.errorCode() != StorageErrorCode.CONTAINER_ALREADY_EXISTS) {
                throw se
            }
        }
        PipelineOptions po = new PipelineOptions()
        po.withClient(getHttpClient())
        HttpPipeline pipeline = StorageURL.createPipeline(primaryCreds, po)
        AppendBlobURL bu = new AppendBlobURL(new URL("http://" + primaryCreds.getAccountName() + ".blob.core.windows.net/rootblob"),
                pipeline)

        when:
        AppendBlobCreateResponse createResponse = bu.create(null, null, null, null)
                .blockingGet()
        BlobGetPropertiesResponse propsResponse = bu.getProperties(null, null).blockingGet()

        then:
        createResponse.statusCode() == 201
        propsResponse.statusCode() == 200
        propsResponse.headers().blobType() == BlobType.APPEND_BLOB
    }

    def "Web container"() {
        setup:
        cu = primaryServiceURL.createContainerURL(ContainerURL.STATIC_WEBSITE_CONTAINER_NAME)
        // Create root container if not exist.
        try {
            cu.create(null, null, null).blockingGet()
        }
        catch (StorageException se) {
            if (se.errorCode() != StorageErrorCode.CONTAINER_ALREADY_EXISTS) {
                throw se
            }
        }
        def webContainer = primaryServiceURL.createContainerURL(ContainerURL.STATIC_WEBSITE_CONTAINER_NAME)

        when:
        // Validate some basic operation.
        webContainer.setAccessPolicy(null, null, null, null).blockingGet()

        then:
        notThrown(StorageException)
    }

    def "With pipeline"() {
        setup:
        ContainerURL withPipeline = cu.withPipeline(HttpPipeline.build(new RequestPolicyFactory() {
            @Override
            RequestPolicy create(RequestPolicy requestPolicy, RequestPolicyOptions requestPolicyOptions) {
                return new RequestPolicy() {
                    @Override
                    Single<HttpResponse> sendAsync(HttpRequest httpRequest) {
                        return Single.error(new Exception("Expected error"))
                    }
                }
            }
        }))

        when:
        withPipeline.create(null, null, null).blockingGet()

        then:
        def e = thrown(Exception)
        e.getMessage().contains("Expected error")
    }

    def "Get account info"() {
        when:
        def response = primaryServiceURL.getAccountInfo(null).blockingGet()

        then:
        response.headers().date() != null
        response.headers().version() != null
        response.headers().requestId() != null
        response.headers().accountKind() != null
        response.headers().skuName() != null
    }

    def "Get account info min"() {
        expect:
        primaryServiceURL.getAccountInfo().blockingGet().statusCode() == 200
    }

    def "Get account info error"() {
        when:
        ServiceURL serviceURL = new ServiceURL(primaryServiceURL.toURL(),
                StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()))
        serviceURL.createContainerURL(generateContainerName()).getAccountInfo(null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Get account info context"() {
        setup:
        def pipeline =
                HttpPipeline.build(getStubFactory(getContextStubPolicy(200, ContainerGetAccountInfoHeaders)))

        cu = cu.withPipeline(pipeline)

        when:
        // No service call is made. Just satisfy the parameters.
        cu.getAccountInfo(defaultContext).blockingGet()

        then:
        notThrown(RuntimeException)
    }
}
