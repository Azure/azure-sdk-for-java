// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob


import com.azure.core.http.rest.Response
import com.azure.core.http.rest.VoidResponse
import com.azure.storage.blob.models.*
import spock.lang.Unroll

import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId

class ContainerAPITest extends APISpec {

    def "Create all null"() {
        setup:
        // Overwrite the existing cu, which has already been created
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        VoidResponse response = cu.create()

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
    }

    def "Create min"() {
        expect:
        primaryServiceURL.createContainer(generateContainerName()).statusCode() == 201
    }

    @Unroll
    def "Create metadata"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        cu.create(metadata, null, null)
        Response<ContainerProperties> response = cu.getProperties()

        then:
        getMetadataFromHeaders(response.headers()) == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Create publicAccess"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.create(null, publicAccess, null)
        PublicAccessType access = cu.getProperties().value().blobPublicAccess()

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
        cu.create()

        then:
        def e = thrown(StorageException)
        e.response().statusCode() == 409
        e.errorCode() == StorageErrorCode.CONTAINER_ALREADY_EXISTS
        e.message().contains("The specified container already exists.")
    }

    def "Get properties null"() {
        when:
        Response<ContainerProperties> response = cu.getProperties()

        then:
        validateBasicHeaders(response.headers())
        response.value().blobPublicAccess() == null
        !response.value().hasImmutabilityPolicy()
        !response.value().hasLegalHold()
        response.headers().value("x-ms-lease-duration") == null
        response.headers().value("x-ms-lease-state") == LeaseStateType.AVAILABLE.toString()
        response.headers().value("x-ms-lease-status") == LeaseStatusType.UNLOCKED.toString()
        getMetadataFromHeaders(response.headers()).size() == 0
    }

    def "Get properties min"() {
        expect:
        cu.getProperties().statusCode() == 200
    }

    def "Get properties lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.getProperties(new LeaseAccessConditions().leaseId(leaseID), null).statusCode() == 200
    }

    def "Get properties lease fail"() {
        when:
        cu.getProperties(new LeaseAccessConditions().leaseId("garbage"), null)

        then:
        thrown(StorageException)
    }

    def "Get properties error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.getProperties(null, null)

        then:
        thrown(StorageException)
    }

    def "Set metadata"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())
        Metadata metadata = new Metadata()
        metadata.put("key", "value")
        cu.create(metadata, null, null)
        VoidResponse response = cu.setMetadata(null)

        expect:
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        getMetadataFromHeaders(cu.getProperties().headers()).size() == 0
    }

    def "Set metadata min"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")

        when:
        cu.setMetadata(metadata)

        then:
        getMetadataFromHeaders(cu.getProperties().headers()) == metadata
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
        cu.setMetadata(metadata).statusCode() == 200
        getMetadataFromHeaders(cu.getProperties().headers()) == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Set metadata AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cu, leaseID)
        ContainerAccessConditions cac = new ContainerAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified))

        expect:
        cu.setMetadata(null, cac, null).statusCode() == 200

        where:
        modified | leaseID
        null     | null
        oldDate  | null
        null     | receivedLeaseID
    }

    @Unroll
    def "Set metadata AC fail"() {
        setup:
        ContainerAccessConditions cac = new ContainerAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified))

        when:
        cu.setMetadata(null, cac, null)

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
        ModifiedAccessConditions mac = new ModifiedAccessConditions()
            .ifUnmodifiedSince(unmodified)
            .ifMatch(match)
            .ifNoneMatch(noneMatch)

        when:
        cu.setMetadata(null, new ContainerAccessConditions().modifiedAccessConditions(mac), null)

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
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.setMetadata(null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Set access policy"() {
        setup:
        def response = cu.setAccessPolicy(access, null, null, null)

        expect:
        validateBasicHeaders(response.headers())
        cu.getProperties().value().blobPublicAccess() == access

        where:
        access                     | _
        PublicAccessType.BLOB      | _
        PublicAccessType.CONTAINER | _
        null                       | _
    }

    def "Set access policy min access"() {
        when:
        cu.setAccessPolicy(PublicAccessType.CONTAINER, null)

        then:
        cu.getProperties().value().blobPublicAccess() == PublicAccessType.CONTAINER
    }

    def "Set access policy min ids"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
                .id("0000")
                .accessPolicy(new AccessPolicy()
                    .start(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                    .expiry(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                        .plusDays(1))
                    .permission("r"))

        List<SignedIdentifier> ids = new ArrayList<>()
        ids.push(identifier)

        when:
        cu.setAccessPolicy(null, ids)

        then:
        cu.getAccessPolicy().value().getIdentifiers().get(0).id() == "0000"
    }

    def "Set access policy ids"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
                .id("0000")
                .accessPolicy(new AccessPolicy()
                .start(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .expiry(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                .plusDays(1))
                .permission("r"))
        SignedIdentifier identifier2 = new SignedIdentifier()
                .id("0001")
                .accessPolicy(new AccessPolicy()
                .start(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .expiry(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                .plusDays(2))
                .permission("w"))
        List<SignedIdentifier> ids = new ArrayList<>()
        ids.push(identifier)
        ids.push(identifier2)

        when:
        VoidResponse response = cu.setAccessPolicy(null, ids, null, null)
        List<SignedIdentifier> receivedIdentifiers = cu.getAccessPolicy().value().getIdentifiers()

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
        ContainerAccessConditions cac = new ContainerAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified))

        expect:
        cu.setAccessPolicy(null, null, cac, null).statusCode() == 200

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
        ContainerAccessConditions cac = new ContainerAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified))

        when:
        cu.setAccessPolicy(null, null, cac, null)

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
        ModifiedAccessConditions mac = new ModifiedAccessConditions().ifMatch(match).ifNoneMatch(noneMatch)

        when:
        cu.setAccessPolicy(null, null, new ContainerAccessConditions().modifiedAccessConditions(mac), null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Set access policy error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.setAccessPolicy(null, null, null, null)

        then:
        thrown(StorageException)
    }

    def "Get access policy"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
                .id("0000")
                .accessPolicy(new AccessPolicy()
                .start(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .expiry(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                .plusDays(1))
                .permission("r"))
        List<SignedIdentifier> ids = new ArrayList<>()
        ids.push(identifier)
        cu.setAccessPolicy(PublicAccessType.BLOB, ids)
        Response<ContainerAccessPolicies> response = cu.getAccessPolicy()

        expect:
        response.statusCode() == 200
        response.value().getBlobAccessType() == PublicAccessType.BLOB
        validateBasicHeaders(response.headers())
        response.value().getIdentifiers().get(0).accessPolicy().expiry() == identifier.accessPolicy().expiry()
        response.value().getIdentifiers().get(0).accessPolicy().start() == identifier.accessPolicy().start()
        response.value().getIdentifiers().get(0).accessPolicy().permission() == identifier.accessPolicy().permission()
    }

    def "Get access policy lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.getAccessPolicy(new LeaseAccessConditions().leaseId(leaseID), null).statusCode() == 200
    }

    def "Get access policy lease fail"() {
        when:
        cu.getAccessPolicy(new LeaseAccessConditions().leaseId(garbageLeaseID), null)

        then:
        thrown(StorageException)
    }

    def "Get access policy error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.getAccessPolicy()

        then:
        thrown(StorageException)
    }

    def "Delete"() {
        when:
        VoidResponse response = cu.delete()

        then:
        response.statusCode() == 202
        response.headers().value("x-ms-request-id") != null
        response.headers().value("x-ms-version") != null
        response.headers().value("Date") != null
    }

    def "Delete min"() {
        expect:
        cu.delete().statusCode() == 202
    }

    @Unroll
    def "Delete AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cu, leaseID)
        ContainerAccessConditions cac = new ContainerAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified))

        expect:
        cu.delete(cac, null).statusCode() == 202

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
        ContainerAccessConditions cac = new ContainerAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified))

        when:
        cu.delete(cac, null)

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
        ModifiedAccessConditions mac = new ModifiedAccessConditions().ifMatch(match).ifNoneMatch(noneMatch)

        when:
        cu.delete(new ContainerAccessConditions().modifiedAccessConditions(mac), null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Delete error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.delete(null, null)

        then:
        thrown(StorageException)
    }

    def "List blobs flat"() {
        setup:
        String name = generateBlobName()
        PageBlobClient bu = cu.getPageBlobClient(name)
        bu.create(512)

        when:
        Iterator<BlobItem> blobs = cu.listBlobsFlat().iterator()

        //ContainerListBlobFlatSegmentHeaders headers = response.headers()
        //List<BlobItem> blobs = responseiterator()()

        then:
//        response.statusCode() == 200
//        headers.contentType() != null
//        headers.requestId() != null
//        headers.version() != null
//        headers.date() != null
        BlobItem blob = blobs.next()
        !blobs.hasNext()
        blob.name() == name
        blob.properties().blobType() == BlobType.PAGE_BLOB
        blob.properties().copyCompletionTime() == null
        blob.properties().copyStatusDescription() == null
        blob.properties().copyId() == null
        blob.properties().copyProgress() == null
        blob.properties().copySource() == null
        blob.properties().copyStatus() == null
        blob.properties().incrementalCopy() == null
        blob.properties().destinationSnapshot() == null
        blob.properties().leaseDuration() == null
        blob.properties().leaseState() == LeaseStateType.AVAILABLE
        blob.properties().leaseStatus() == LeaseStatusType.UNLOCKED
        blob.properties().contentLength() != null
        blob.properties().contentType() != null
        blob.properties().contentMD5() == null
        blob.properties().contentEncoding() == null
        blob.properties().contentDisposition() == null
        blob.properties().contentLanguage() == null
        blob.properties().cacheControl() == null
        blob.properties().blobSequenceNumber() == 0
        blob.properties().serverEncrypted()
        blob.properties().accessTierInferred()
        blob.properties().accessTier() == AccessTier.HOT
        blob.properties().archiveStatus() == null
        blob.properties().creationTime() != null
    }

    def "List blobs flat min"() {
        when:
        cu.listBlobsFlat().iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    def setupListBlobsTest(String normalName, String copyName, String metadataName, String uncommittedName) {
        PageBlobClient normal = cu.getPageBlobClient(normalName)
        normal.create(512)

        PageBlobClient copyBlob = cu.getPageBlobClient(copyName)

        String status = copyBlob.startCopyFromURL(normal.getBlobUrl()).value()
        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS.toString()) {
            status = copyBlob.getProperties().headers().value("x-ms-copy-status")
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED.toString() || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleep(1000)
        }

        PageBlobClient metadataBlob = cu.getPageBlobClient(metadataName)
        Metadata values = new Metadata()
        values.put("foo", "bar")
        metadataBlob.create(512, null, null, values, null, null)

        String snapshotTime = normal.createSnapshot().value().getSnapshotId()

        BlockBlobClient uncommittedBlob = cu.getBlockBlobClient(uncommittedName)

        uncommittedBlob.stageBlock("0000", defaultInputStream.get(), defaultData.remaining())

        return snapshotTime
    }

    def blobListResponseToList(Iterator<BlobItem> blobs) {
        ArrayList<BlobItem> blobQueue = new ArrayList<>()
        while (blobs.hasNext()) {
            blobQueue.add(blobs.next())
        }

        return blobQueue
    }

    def "List blobs flat options copy"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().details(new BlobListDetails().copy(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = blobListResponseToList(cu.listBlobsFlat(options, null).iterator())

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
        ListBlobsOptions options = new ListBlobsOptions().details(new BlobListDetails().metadata(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = blobListResponseToList(cu.listBlobsFlat(options, null).iterator())

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
        ListBlobsOptions options = new ListBlobsOptions().details(new BlobListDetails().snapshots(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        String snapshotTime = setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = blobListResponseToList(cu.listBlobsFlat(options, null).iterator())

        then:
        blobs.get(0).name() == normalName
        blobs.get(0).snapshot() == snapshotTime
        blobs.get(1).name() == normalName
        blobs.size() == 4 // Normal, snapshot, copy, metadata
    }

    def "List blobs flat options uncommitted"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().details(new BlobListDetails().uncommittedBlobs(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = blobListResponseToList(cu.listBlobsFlat(options, null).iterator())

        then:
        blobs.get(0).name() == normalName
        blobs.get(3).name() == uncommittedName
        blobs.size() == 4 // Normal, copy, metadata, uncommitted
    }

    def "List blobs flat options deleted"() {
        setup:
        enableSoftDelete()
        String name = generateBlobName()
        AppendBlobClient bu = cu.getAppendBlobClient(name)
        bu.create()
        bu.delete()

        when:
        ListBlobsOptions options = new ListBlobsOptions().details(new BlobListDetails().deletedBlobs(true))
        Iterator<BlobItem> blobs = cu.listBlobsFlat(options, null).iterator()

        then:
        blobs.next().name() == name
        !blobs.hasNext()

        disableSoftDelete() == null // Must produce a true value or test will fail.
    }

    def "List blobs flat options prefix"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().prefix("a")
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        Iterator<BlobItem> blobs = cu.listBlobsFlat(options, null).iterator()

        then:
        blobs.next().name() == normalName
        !blobs.hasNext() // Normal
    }

    // TODO (alzimmer): Turn this on once paged responses are available
    /*def "List blobs flat options maxResults"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().details(new BlobListDetails().copy(true)
                .snapshots(true).uncommittedBlobs(true)).maxResults(2)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        Iterator<BlobItem> blobs = cu.listBlobsFlat(options, null).iterator()

        then:
        blobs.size() == 2
    }*/

    def "List blobs flat options fail"() {
        when:
        new ListBlobsOptions().maxResults(0)

        then:
        thrown(IllegalArgumentException)
    }

    // TODO (alzimmer): Turn this on once paged responses are available
    /*def "List blobs flat marker"() {
        setup:
        for (int i = 0; i < 10; i++) {
            PageBlobClient bu = cu.getPageBlobClient(generateBlobName())
            bu.create(512)
        }

        Iterator<BlobItem> response = cu.listBlobsFlat(new ListBlobsOptions().maxResults(6), null)

        String marker = response.body().nextMarker()
        int firstSegmentSize = response.iterator().size()
        response = cu.listBlobsFlat(marker, null, null)

        expect:
        firstSegmentSize == 6
        response.body().nextMarker() == null
        responseiterator()().size() == 4
    }*/

    def "List blobs flat error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.listBlobsFlat().iterator().hasNext()

        then:
        thrown(StorageException)
    }

    def "List blobs hierarchy"() {
        setup:
        String name = generateBlobName()
        PageBlobClient bu = cu.getPageBlobClient(name)
        bu.create(512)

        when:
        Iterator<BlobItem> blobs = cu.listBlobsHierarchy(null).iterator()

        then:
//        response.statusCode() == 200
//        headers.contentType() != null
//        headers.requestId() != null
//        headers.version() != null
//        headers.date() != null
        blobs.next().name() == name
        !blobs.hasNext()
    }

    def "List blobs hierarchy min"() {
        when:
        cu.listBlobsHierarchy("/").iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    def "List blobs hier options copy"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().details(new BlobListDetails().copy(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = blobListResponseToList(cu.listBlobsHierarchy("", options, null).iterator())

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
        ListBlobsOptions options = new ListBlobsOptions().details(new BlobListDetails().metadata(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = blobListResponseToList(cu.listBlobsHierarchy("", options, null).iterator())

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
        ListBlobsOptions options = new ListBlobsOptions().details(new BlobListDetails().uncommittedBlobs(true))
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = blobListResponseToList(cu.listBlobsHierarchy("", options, null).iterator())

        then:
        blobs.get(0).name() == normalName
        blobs.get(3).name() == uncommittedName
        blobs.size() == 4 // Normal, copy, metadata, uncommitted
    }

    def "List blobs hier options deleted"() {
        setup:
        enableSoftDelete()
        String name = generateBlobName()
        AppendBlobClient bu = cu.getAppendBlobClient(name)
        bu.create()
        bu.delete()

        when:
        ListBlobsOptions options = new ListBlobsOptions().details(new BlobListDetails().deletedBlobs(true))
        Iterator<BlobItem> blobs = cu.listBlobsHierarchy("", options, null).iterator()

        then:
        blobs.next().name() == name
        !blobs.hasNext()

        disableSoftDelete() == null
    }

    def "List blobs hier options prefix"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().prefix("a")
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        Iterator<BlobItem> blobs = cu.listBlobsHierarchy("", options, null).iterator()

        then:
        blobs.next().name() == normalName
        !blobs.hasNext() // Normal
    }

    // TODO (alzimmer): Turn this on when paged responses becomes available
    /*def "List blobs hier options maxResults"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions().details(new BlobListDetails().copy(true)
                .uncommittedBlobs(true)).maxResults(1)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        Iterator<BlobItem> blobs = cu.listBlobsHierarchy("", options, null).iterator()

        then:
        blobs.size() == 1
    }*/

    @Unroll
    def "List blobs hier options fail"() {
        when:
        def options = new ListBlobsOptions().details(new BlobListDetails().snapshots(snapshots))
                .maxResults(maxResults)
        cu.listBlobsHierarchy(null, options, null).iterator().hasNext()

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
            def bu = cu.getAppendBlobClient(blobName)
            bu.create()
        }

        when:
        Iterator<BlobItem> blobs = cu.listBlobsHierarchy(null).iterator()

        and:
        ArrayDeque<String> expectedBlobs = new ArrayDeque<>()
        expectedBlobs.add("a")
        expectedBlobs.add("c")
        expectedBlobs.add("e")
        expectedBlobs.add("f")

        ArrayDeque<String> expectedPrefixes = new ArrayDeque<>()
        expectedPrefixes.add("b/")
        expectedPrefixes.add("d/")
        expectedPrefixes.add("g/")

        then:
        while (blobs.hasNext()) {
            BlobItem blob = blobs.next()

            if (blob.isPrefix()) {
                blob.name() == expectedPrefixes.pop()
            } else {
                blob.name() == expectedBlobs.pop()
            }
        }

        expectedPrefixes.isEmpty()
        expectedBlobs.isEmpty()
    }

    // TODO (alzimmer): Turn this on when paged response become available
    /*def "List blobs hier marker"() {
        setup:
        for (int i = 0; i < 10; i++) {
            PageBlobClient bu = cu.getPageBlobClient(generateBlobName())
            bu.create(512)
        }

        ContainerListBlobHierarchySegmentResponse response = cu.listBlobsHierarchySegment(null, "/",
                new ListBlobsOptions().maxResults(6), null)

        String marker = response.body().nextMarker()
        int firstSegmentSize = responseiterator()().size()
        response = cu.listBlobsHierarchySegment(marker, "/", null, null)

        expect:
        firstSegmentSize == 6
        response.body().nextMarker() == null
        response.iterator().size() == 4
    }*/

    def "List blobs flat simple"() {
        setup:
        // Create 10 page blobs in the container
        for (int i = 0; i < 10; i++) {
            PageBlobClient bu = cu.getPageBlobClient(generateBlobName())
            bu.create(512)
        }
        // Setting maxResult limits the number of items per page, this way we validate
        // that blob.size() make multiple calls - one call per page to retrieve all 10 blobs.
        //
        Iterable<BlobItem> blobs = cu.listBlobsFlat(new ListBlobsOptions().maxResults(3), null)
        int size = blobs.size()

        expect:
        size == 10
    }

    def "List blobs hier error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.listBlobsHierarchy(".").iterator().hasNext()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Acquire lease"() {
        setup:
        Response<String> leaseResponse = cu.acquireLease(proposedID, leaseTime)

        when:
        Response<ContainerProperties> propertiesResponse = cu.getProperties()

        then:
        leaseResponse.value() != null
        validateBasicHeaders(leaseResponse.headers())
        propertiesResponse.headers().value("x-ms-lease-state") == leaseState.toString()
        propertiesResponse.headers().value("x-ms-lease-duration") == leaseDuration.toString()

        where:
        proposedID                   | leaseTime || leaseState            | leaseDuration
        null                         | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
        null                         | 25        || LeaseStateType.LEASED | LeaseDurationType.FIXED
        UUID.randomUUID().toString() | -1        || LeaseStateType.LEASED | LeaseDurationType.INFINITE
    }

    def "Acquire lease min"() {
        expect:
        cu.acquireLease(null, -1).statusCode() == 201
    }

    @Unroll
    def "Acquire lease AC"() {
        setup:
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        expect:
        cu.acquireLease(null, -1, mac, null).statusCode() == 201

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Acquire lease AC fail"() {
        setup:
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        when:
        cu.acquireLease(null, -1, mac, null)

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
        ModifiedAccessConditions mac = new ModifiedAccessConditions().ifMatch(match).ifNoneMatch(noneMatch)

        when:
        cu.acquireLease(null, -1, mac, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Acquire lease error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.acquireLease(null, 50, null, null)

        then:
        thrown(StorageException)
    }

    def "Renew lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        Thread.sleep(16000) // Wait for the lease to expire to ensure we are actually renewing it
        Response<String> renewLeaseResponse = cu.renewLease(leaseID)

        expect:
        renewLeaseResponse.value() != null
        cu.getProperties().headers().value("x-ms-lease-state") == LeaseStateType.LEASED.toString()
        validateBasicHeaders(renewLeaseResponse.headers())
    }

    def "Renew lease min"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.renewLease(leaseID).statusCode() == 200
    }

    @Unroll
    def "Renew lease AC"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        expect:
        cu.renewLease(leaseID, mac, null).statusCode() == 200

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
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        when:
        cu.renewLease(leaseID, mac, null)

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
        ModifiedAccessConditions mac = new ModifiedAccessConditions().ifMatch(match).ifNoneMatch(noneMatch)

        when:
        cu.renewLease(receivedLeaseID, mac, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Renew lease error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.renewLease("id")

        then:
        thrown(StorageException)
    }

    def "Release lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        VoidResponse releaseLeaseResponse = cu.releaseLease(leaseID)

        expect:
        cu.getProperties().headers().value("x-ms-lease-state") == LeaseStateType.AVAILABLE.toString()
        validateBasicHeaders(releaseLeaseResponse.headers())
    }

    def "Release lease min"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.releaseLease(leaseID).statusCode() == 200
    }

    @Unroll
    def "Release lease AC"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        expect:
        cu.releaseLease(leaseID, mac, null).statusCode() == 200

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
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        when:
        cu.releaseLease(leaseID, mac, null)

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
        ModifiedAccessConditions mac = new ModifiedAccessConditions().ifMatch(match).ifNoneMatch(noneMatch)

        when:
        cu.releaseLease(receivedLeaseID, mac, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Release lease error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.releaseLease("id")

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Break lease"() {
        setup:
        cu.acquireLease(UUID.randomUUID().toString(), leaseTime)

        Response<Duration> breakLeaseResponse = cu.breakLease(breakPeriod, null, null)
        LeaseStateType state = LeaseStateType.fromString(cu.getProperties().headers().value("x-ms-lease-state"))

        expect:
        state == LeaseStateType.BROKEN || state == LeaseStateType.BREAKING
        breakLeaseResponse.value().getSeconds() <= remainingTime
        validateBasicHeaders(breakLeaseResponse.headers())
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
        cu.breakLease().statusCode() == 202
    }

    @Unroll
    def "Break lease AC"() {
        setup:
        setupContainerLeaseCondition(cu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        expect:
        cu.breakLease(null, mac, null).statusCode() == 202

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
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        when:
        cu.breakLease(null, mac, null)

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
        ModifiedAccessConditions mac = new ModifiedAccessConditions().ifMatch(match).ifNoneMatch(noneMatch)

        when:
        cu.breakLease(null, mac, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Break lease error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.breakLease()

        then:
        thrown(StorageException)
    }

    def "Change lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        Response<String> changeLeaseResponse = cu.changeLease(leaseID, UUID.randomUUID().toString())
        leaseID = changeLeaseResponse.value()

        expect:
        cu.releaseLease(leaseID).statusCode() == 200
        validateBasicHeaders(changeLeaseResponse.headers())
    }

    def "Change lease min"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.changeLease(leaseID, UUID.randomUUID().toString()).statusCode() == 200
    }

    @Unroll
    def "Change lease AC"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        expect:
        cu.changeLease(leaseID, UUID.randomUUID().toString(), mac, null).statusCode() == 200

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
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        when:
        cu.changeLease(leaseID, UUID.randomUUID().toString(), mac, null)

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
        ModifiedAccessConditions mac = new ModifiedAccessConditions().ifMatch(match).ifNoneMatch(noneMatch)

        when:
        cu.changeLease(receivedLeaseID, garbageLeaseID, mac, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Change lease error"() {
        setup:
        cu = primaryServiceURL.getContainerClient(generateContainerName())

        when:
        cu.changeLease("id", "id")

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Create URL special chars"() {
        // This test checks that we encode special characters in blob names correctly.
        setup:
        AppendBlobClient bu2 = cu.getAppendBlobClient(name)
        PageBlobClient bu3 = cu.getPageBlobClient(name + "2")
        BlockBlobClient bu4 = cu.getBlockBlobClient(name + "3")
        BlockBlobClient bu5 = cu.getBlockBlobClient(name)

        expect:
        bu2.create().statusCode() == 201
        bu5.getProperties().statusCode() == 200
        bu3.create(512).statusCode() == 201
        bu4.upload(defaultInputStream.get(), defaultDataSize).statusCode() == 201

        when:
        Iterator<BlobItem> blobs = cu.listBlobsFlat().iterator()

        then:
        blobs.next().name() == name
        blobs.next().name() == name + "2"
        blobs.next().name() == name + "3"

        where:
        name                  | _
        // "中文"                 | _  TODO: requires blob name to be url encoded, deferred for post preview-1, storage team to decide on encoding story across SDKS
        "az[]"                | _
        // "hello world"         | _  TODO: see previous TODO
        "hello/world"         | _
        "hello&world"         | _
        // "!*'();:@&=+\$,/?#[]" | _  TODO: see previous TODO
    }

    def "Root explicit"() {
        setup:
        cu = primaryServiceURL.getContainerClient(ContainerClient.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        if (!cu.exists().value()) {
            cu.create()
        }

        AppendBlobClient bu = cu.getAppendBlobClient("rootblob")

        expect:
        bu.create().statusCode() == 201
    }

    def "Root explicit in endpoint"() {
        setup:
        cu = primaryServiceURL.getContainerClient(ContainerClient.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        if (!cu.exists().value()) {
            cu.create()
        }

        AppendBlobClient bu = new BlobClientBuilder()
                .credential(primaryCreds)
                .endpoint("http://" + primaryCreds.accountName() + ".blob.core.windows.net/\$root/rootblob")
                .httpClient(getHttpClient())
                .buildAppendBlobClient()

        when:
        Response<AppendBlobItem> createResponse = bu.create()

        Response<BlobProperties> propsResponse = bu.getProperties()

        then:
        createResponse.statusCode() == 201
        propsResponse.statusCode() == 200
        propsResponse.value().blobType() == BlobType.APPEND_BLOB
    }

    /*
    def "Root implicit"() {
        setup:
        cu = primaryServiceURL.getContainerClient(ContainerClient.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        if (!cu.exists().value()) {
            cu.create()
        }

        AppendBlobClient bu = new BlobClientBuilder()
            .credential(primaryCreds)
            .endpoint("http://" + primaryCreds.accountName() + ".blob.core.windows.net/rootblob")
            .httpClient(getHttpClient())
            .buildAppendBlobClient()

        when:
        Response<AppendBlobItem> createResponse = bu.create()

        Response<BlobProperties> propsResponse = bu.getProperties()

        then:
        createResponse.statusCode() == 201
        propsResponse.statusCode() == 200
        propsResponse.value().blobType() == BlobType.APPEND_BLOB
    }
    */

    def "Web container"() {
        setup:
        cu = primaryServiceURL.getContainerClient(ContainerClient.STATIC_WEBSITE_CONTAINER_NAME)
        // Create root container if not exist.
        try {
            cu.create(null, null, null)
        }
        catch (StorageException se) {
            if (se.errorCode() != StorageErrorCode.CONTAINER_ALREADY_EXISTS) {
                throw se
            }
        }
        def webContainer = primaryServiceURL.getContainerClient(ContainerClient.STATIC_WEBSITE_CONTAINER_NAME)

        when:
        // Validate some basic operation.
        webContainer.setAccessPolicy(null, null)

        then:
        notThrown(StorageException)
    }

    def "Get account info"() {
        when:
        Response<StorageAccountInfo> response = primaryServiceURL.getAccountInfo()

        then:
        response.headers().value("Date") != null
        response.headers().value("x-ms-version") != null
        response.headers().value("x-ms-request-id") != null
        response.value().accountKind() != null
        response.value().skuName() != null
    }

    def "Get account info min"() {
        expect:
        primaryServiceURL.getAccountInfo().statusCode() == 200
    }

    def "Get account info error"() {
        when:
        BlobServiceClient serviceURL = new BlobServiceClientBuilder()
            .endpoint(primaryServiceURL.getAccountUrl().toString())
            .buildClient()

        serviceURL.getContainerClient(generateContainerName()).getAccountInfo()

        then:
        thrown(StorageException)
    }
}
