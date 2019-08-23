// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.rest.Response
import com.azure.core.http.rest.VoidResponse
import com.azure.storage.blob.models.AccessPolicy
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.AppendBlobItem
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.BlobListDetails
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.ContainerAccessConditions
import com.azure.storage.blob.models.ContainerAccessPolicies
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.LeaseAccessConditions
import com.azure.storage.blob.models.LeaseDurationType
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.LeaseStatusType
import com.azure.storage.blob.models.ListBlobsOptions
import com.azure.storage.blob.models.Metadata
import com.azure.storage.blob.models.ModifiedAccessConditions
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.models.SignedIdentifier
import com.azure.storage.blob.models.StorageErrorCode
import com.azure.storage.blob.models.StorageException
import spock.lang.Unroll

import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.stream.Collectors

class ContainerAPITest extends APISpec {

    def "Create all null"() {
        setup:
        // Overwrite the existing cc, which has already been created
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        def response = cc.createWithResponse(null, null, null, null)

        then:
        response.statusCode() == 201
        validateBasicHeaders(response.headers())
    }

    def "Create min"() {
        expect:
        primaryBlobServiceClient.createContainerWithResponse(generateContainerName(), null, null, null).statusCode() == 201
    }

    @Unroll
    def "Create metadata"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        cc.createWithResponse(metadata, null, null, null)
        Response<ContainerProperties> response = cc.getPropertiesWithResponse(null, null, null)

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
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.createWithResponse(null, publicAccess, null, null)
        def access = cc.getProperties().blobPublicAccess()

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
        cc.create()

        then:
        def e = thrown(StorageException)
        e.response().statusCode() == 409
        e.errorCode() == StorageErrorCode.CONTAINER_ALREADY_EXISTS
        e.serviceMessage().contains("The specified container already exists.")
    }

    def "Get properties null"() {
        when:
        def response = cc.getPropertiesWithResponse(null, null, null)

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
        cc.getPropertiesWithResponse(null, null, null).statusCode() == 200
    }

    def "Get properties lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        cc.getPropertiesWithResponse(new LeaseAccessConditions().leaseId(leaseID), null, null).statusCode() == 200
    }

    def "Get properties lease fail"() {
        when:
        cc.getPropertiesWithResponse(new LeaseAccessConditions().leaseId("garbage"), null, null)

        then:
        thrown(StorageException)
    }

    def "Get properties error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.getProperties()

        then:
        thrown(StorageException)
    }

    def "Set metadata"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())
        Metadata metadata = new Metadata()
        metadata.put("key", "value")
        cc.createWithResponse(metadata, null, null, null)

        when:
        def response = cc.setMetadataWithResponse(null, null, null, null)

        then:
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        getMetadataFromHeaders(cc.getPropertiesWithResponse(null, null, null).headers()).size() == 0
    }

    def "Set metadata min"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("foo", "bar")

        when:
        cc.setMetadata(metadata)

        then:
        getMetadataFromHeaders(cc.getPropertiesWithResponse(null, null, null).headers()) == metadata
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
        cc.setMetadataWithResponse(metadata, null, null, null).statusCode() == 200
        getMetadataFromHeaders(cc.getPropertiesWithResponse(null, null, null).headers()) == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Set metadata AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cc, leaseID)
        ContainerAccessConditions cac = new ContainerAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified))

        expect:
        cc.setMetadataWithResponse(null, cac, null, null).statusCode() == 200

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
        cc.setMetadataWithResponse(null, cac, null, null)

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
        cc.setMetadataWithResponse(null, new ContainerAccessConditions().modifiedAccessConditions(mac), null, null)

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
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.setMetadata(null)

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Set access policy"() {
        setup:
        def response = cc.setAccessPolicyWithResponse(access, null, null, null, null)

        expect:
        validateBasicHeaders(response.headers())
        cc.getPropertiesWithResponse(null, null, null).value().blobPublicAccess() == access

        where:
        access                     | _
        PublicAccessType.BLOB      | _
        PublicAccessType.CONTAINER | _
        null                       | _
    }

    def "Set access policy min access"() {
        when:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)

        then:
        cc.getProperties().blobPublicAccess() == PublicAccessType.CONTAINER
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

        def ids = []
        ids.push(identifier)

        when:
        cc.setAccessPolicy(null, ids)

        then:
        cc.getAccessPolicy().getIdentifiers().get(0).id() == "0000"
    }

    def "Set access policy ids"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
            .id("0000")
            .accessPolicy(new AccessPolicy()
                .start(getUTCNow())
                .expiry(getUTCNow().plusDays(1))
                .permission("r"))
        SignedIdentifier identifier2 = new SignedIdentifier()
            .id("0001")
            .accessPolicy(new AccessPolicy()
                .start(getUTCNow())
                .expiry(getUTCNow().plusDays(2))
                .permission("w"))
        List<SignedIdentifier> ids = new ArrayList<>()
        ids.push(identifier)
        ids.push(identifier2)

        when:
        def response = cc.setAccessPolicyWithResponse(null, ids, null, null, null)
        def receivedIdentifiers = cc.getAccessPolicyWithResponse(null, null, null).value().getIdentifiers()

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
        leaseID = setupContainerLeaseCondition(cc, leaseID)
        ContainerAccessConditions cac = new ContainerAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified))

        expect:
        cc.setAccessPolicyWithResponse(null, null, cac, null, null).statusCode() == 200

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
        cc.setAccessPolicyWithResponse(null, null, cac, null, null)

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
        cc.setAccessPolicyWithResponse(null, null, new ContainerAccessConditions().modifiedAccessConditions(mac), null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Set access policy error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.setAccessPolicy(null, null)

        then:
        thrown(StorageException)
    }

    def "Get access policy"() {
        setup:
        SignedIdentifier identifier = new SignedIdentifier()
            .id("0000")
            .accessPolicy(new AccessPolicy()
                .start(getUTCNow())
                .expiry(getUTCNow().plusDays(1))
                .permission("r"))
        List<SignedIdentifier> ids = new ArrayList<>()
        ids.push(identifier)
        cc.setAccessPolicy(PublicAccessType.BLOB, ids)
        Response<ContainerAccessPolicies> response = cc.getAccessPolicyWithResponse(null, null, null)

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
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        cc.getAccessPolicyWithResponse(new LeaseAccessConditions().leaseId(leaseID), null, null).statusCode() == 200
    }

    def "Get access policy lease fail"() {
        when:
        cc.getAccessPolicyWithResponse(new LeaseAccessConditions().leaseId(garbageLeaseID), null, null)

        then:
        thrown(StorageException)
    }

    def "Get access policy error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.getAccessPolicy()

        then:
        thrown(StorageException)
    }

    def "Delete"() {
        when:
        VoidResponse response = cc.deleteWithResponse(null, null, null)

        then:
        response.statusCode() == 202
        response.headers().value("x-ms-request-id") != null
        response.headers().value("x-ms-version") != null
        response.headers().value("Date") != null
    }

    def "Delete min"() {
        expect:
        cc.deleteWithResponse(null, null, null).statusCode() == 202
    }

    @Unroll
    def "Delete AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cc, leaseID)
        ContainerAccessConditions cac = new ContainerAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseID))
            .modifiedAccessConditions(new ModifiedAccessConditions()
                .ifModifiedSince(modified)
                .ifUnmodifiedSince(unmodified))

        expect:
        cc.deleteWithResponse(cac, null, null).statusCode() == 202

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
        cc.deleteWithResponse(cac, null, null)

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
        cc.deleteWithResponse(new ContainerAccessConditions().modifiedAccessConditions(mac), null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Delete error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.delete()

        then:
        thrown(StorageException)
    }

    def "List blobs flat"() {
        setup:
        String name = generateBlobName()
        PageBlobClient bu = cc.getPageBlobClient(name)
        bu.create(512)

        when:
        Iterator<BlobItem> blobs = cc.listBlobsFlat().iterator()

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
        cc.listBlobsFlat().iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    def setupListBlobsTest(String normalName, String copyName, String metadataName, String uncommittedName) {
        PageBlobClient normal = cc.getPageBlobClient(normalName)
        normal.create(512)

        PageBlobClient copyBlob = cc.getPageBlobClient(copyName)

        String status = copyBlob.startCopyFromURL(normal.getBlobUrl())
        OffsetDateTime start = OffsetDateTime.now()
        while (status != CopyStatusType.SUCCESS.toString()) {
            status = copyBlob.getPropertiesWithResponse(null, null, null).headers().value("x-ms-copy-status")
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED.toString() || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleepIfRecord(1000)
        }

        PageBlobClient metadataBlob = cc.getPageBlobClient(metadataName)
        Metadata values = new Metadata()
        values.put("foo", "bar")
        metadataBlob.createWithResponse(512, null, null, values, null, null, null)

        String snapshotTime = normal.createSnapshot().getSnapshotId()

        BlockBlobClient uncommittedBlob = cc.getBlockBlobClient(uncommittedName)

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
        List<BlobItem> blobs = blobListResponseToList(cc.listBlobsFlat(options, null).iterator())

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
        List<BlobItem> blobs = blobListResponseToList(cc.listBlobsFlat(options, null).iterator())

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
        List<BlobItem> blobs = blobListResponseToList(cc.listBlobsFlat(options, null).iterator())

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
        List<BlobItem> blobs = blobListResponseToList(cc.listBlobsFlat(options, null).iterator())

        then:
        blobs.get(0).name() == normalName
        blobs.get(3).name() == uncommittedName
        blobs.size() == 4 // Normal, copy, metadata, uncommitted
    }

    def "List blobs flat options deleted"() {
        setup:
        enableSoftDelete()
        String name = generateBlobName()
        AppendBlobClient bu = cc.getAppendBlobClient(name)
        bu.create()
        bu.delete()

        when:
        ListBlobsOptions options = new ListBlobsOptions().details(new BlobListDetails().deletedBlobs(true))
        Iterator<BlobItem> blobs = cc.listBlobsFlat(options, null).iterator()

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
        Iterator<BlobItem> blobs = cc.listBlobsFlat(options, null).iterator()

        then:
        blobs.next().name() == normalName
        !blobs.hasNext() // Normal
    }

    def "List blobs flat options maxResults"() {
        setup:
        def PAGE_SIZE = 2
        def options = new ListBlobsOptions().details(new BlobListDetails().copy(true)
            .snapshots(true).uncommittedBlobs(true)).maxResults(PAGE_SIZE)
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        expect: "Get first page of blob listings (sync and async)"
        cc.listBlobsFlat(options, null).iterableByPage().iterator().next().value().size() == PAGE_SIZE
        ccAsync.listBlobsFlat(options).byPage().blockFirst().value().size() == PAGE_SIZE
    }

    def "List blobs flat options fail"() {
        when:
        new ListBlobsOptions().maxResults(0)

        then:
        thrown(IllegalArgumentException)
    }

    def "List blobs flat marker"() {
        setup:
        def NUM_BLOBS = 10
        def PAGE_SIZE = 6
        for (int i = 0; i < NUM_BLOBS ; i++) {
            PageBlobClient bc = cc.getPageBlobClient(generateBlobName())
            bc.create(512)
        }

        when: "list blobs with sync client"
        def pagedIterable = cc.listBlobsFlat(new ListBlobsOptions().maxResults(PAGE_SIZE), null)
        def pagedSyncResponse1 = pagedIterable.iterableByPage().iterator().next()
        def pagedSyncResponse2 = pagedIterable.iterableByPage(pagedSyncResponse1.nextLink()).iterator().next()

        then:
        pagedSyncResponse1.value().size() == PAGE_SIZE
        pagedSyncResponse2.value().size() == NUM_BLOBS - PAGE_SIZE
        pagedSyncResponse2.nextLink() == null


        when: "list blobs with async client"
        def pagedFlux = ccAsync.listBlobsFlat(new ListBlobsOptions().maxResults(PAGE_SIZE))
        def pagedResponse1 = pagedFlux.byPage().blockFirst()
        def pagedResponse2 = pagedFlux.byPage(pagedResponse1.nextLink()).blockFirst()

        then:
        pagedResponse1.value().size() == PAGE_SIZE
        pagedResponse2.value().size() == NUM_BLOBS - PAGE_SIZE
        pagedResponse2.nextLink() == null
    }

    def "List blobs flat error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.listBlobsFlat().iterator().hasNext()

        then:
        thrown(StorageException)
    }

    def "List blobs flat with timeout still backed by PagedFlux"() {
        setup:
        def NUM_BLOBS = 5
        def PAGE_RESULTS = 3

        def blobs = [] as Collection<BlobClient>
        for (i in (1..NUM_BLOBS)) {
            def blob = cc.getBlockBlobClient(generateBlobName())
            blob.upload(defaultInputStream.get(), defaultDataSize)
            blobs << blob
        }

        when: "Consume results by page"
        cc.listBlobsFlat(new ListBlobsOptions().maxResults(PAGE_RESULTS), Duration.ofSeconds(10)).streamByPage().count()

        then: "Still have paging functionality"
        notThrown(Exception)
    }

    def "List blobs hier with timeout still backed by PagedFlux"() {
        setup:
        def NUM_BLOBS = 5
        def PAGE_RESULTS = 3

        def blobs = [] as Collection<BlobClient>
        for (i in (1..NUM_BLOBS)) {
            def blob = cc.getBlockBlobClient(generateBlobName())
            blob.upload(defaultInputStream.get(), defaultDataSize)
            blobs << blob
        }

        when: "Consume results by page"
        cc.listBlobsHierarchy("/", new ListBlobsOptions().maxResults(PAGE_RESULTS), Duration.ofSeconds(10)).streamByPage().count()

        then: "Still have paging functionality"
        notThrown(Exception)
    }

    def "List blobs hierarchy"() {
        setup:
        String name = generateBlobName()
        PageBlobClient bu = cc.getPageBlobClient(name)
        bu.create(512)

        when:
        Iterator<BlobItem> blobs = cc.listBlobsHierarchy(null).iterator()

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
        cc.listBlobsHierarchy("/").iterator().hasNext()

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
        List<BlobItem> blobs = blobListResponseToList(cc.listBlobsHierarchy("", options, null).iterator())

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
        List<BlobItem> blobs = blobListResponseToList(cc.listBlobsHierarchy("", options, null).iterator())

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
        List<BlobItem> blobs = blobListResponseToList(cc.listBlobsHierarchy("", options, null).iterator())

        then:
        blobs.get(0).name() == normalName
        blobs.get(3).name() == uncommittedName
        blobs.size() == 4 // Normal, copy, metadata, uncommitted
    }

    def "List blobs hier options deleted"() {
        setup:
        enableSoftDelete()
        def name = generateBlobName()
        def bc = cc.getAppendBlobClient(name)
        bc.create()
        bc.delete()

        when:
        def options = new ListBlobsOptions().details(new BlobListDetails().deletedBlobs(true))
        def blobs = cc.listBlobsHierarchy("", options, null).iterator()

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
        Iterator<BlobItem> blobs = cc.listBlobsHierarchy("", options, null).iterator()

        then:
        blobs.next().name() == normalName
        !blobs.hasNext() // Normal
    }


    def "List blobs hier options maxResults"() {
        setup:
        def options = new ListBlobsOptions().details(new BlobListDetails().copy(true)
                .uncommittedBlobs(true)).maxResults(1)
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        // use async client, as there is no paging functionality for sync yet
        def blobs = ccAsync.listBlobsHierarchy("", options).byPage().blockFirst()

        then:
        blobs.value().size() == 1
    }

    @Unroll
    def "List blobs hier options fail"() {
        when:
        def options = new ListBlobsOptions().details(new BlobListDetails().snapshots(snapshots))
                .maxResults(maxResults)
        cc.listBlobsHierarchy(null, options, null).iterator().hasNext()

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
        def blobNames = ["a", "b/a", "c", "d/a", "e", "f", "g/a"]
        for (def blobName : blobNames) {
            def bu = cc.getAppendBlobClient(blobName)
            bu.create()
        }

        when:
        def foundBlobs = [] as Set
        def foundPrefixes = [] as Set
        cc.listBlobsHierarchy(null).stream().collect(Collectors.toList())
            .forEach { blobItem ->
            if (blobItem.isPrefix()) {
                foundPrefixes << blobItem.name()
            }
            else {
                foundBlobs << blobItem.name()
            }
        }

        and:
        def expectedBlobs = ["a", "c", "e", "f"] as Set
        def expectedPrefixes = ["b/", "d/", "g/"] as Set

        then:
        expectedBlobs == foundBlobs
        expectedPrefixes == foundPrefixes
    }

    def "List blobs hier marker"() {
        setup:
        def NUM_BLOBS = 10
        def PAGE_SIZE = 6
        for (int i = 0; i < NUM_BLOBS; i++) {
            PageBlobClient bc = cc.getPageBlobClient(generateBlobName())
            bc.create(512)
        }

        def blobs = cc.listBlobsHierarchy("/", new ListBlobsOptions().maxResults(PAGE_SIZE), null)

        when:
        def firstPage = blobs.iterableByPage().iterator().next()

        then:
        firstPage.value().size() == PAGE_SIZE
        firstPage.nextLink() != null

        when:
        def secondPage = blobs.iterableByPage(firstPage.nextLink()).iterator().next()

        then:
        secondPage.value().size() == NUM_BLOBS - PAGE_SIZE
        secondPage.nextLink() == null
    }

    def "List blobs flat simple"() {
        setup: "Create 10 page blobs in the container"
        def NUM_BLOBS = 10
        def PAGE_SIZE = 3
        for (int i = 0; i < NUM_BLOBS; i++) {
            def bc = cc.getPageBlobClient(generateBlobName())
            bc.create(512)
        }

        expect: "listing operation will fetch all 10 blobs, despite page size being smaller than 10"
        cc.listBlobsFlat(new ListBlobsOptions().maxResults(PAGE_SIZE), null).stream().count() == NUM_BLOBS
    }

    def "List blobs hier error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.listBlobsHierarchy(".").iterator().hasNext()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Acquire lease"() {
        setup:
        def leaseResponse = cc.acquireLeaseWithResponse(proposedID, leaseTime, null, null, null)

        when:
        def propertiesResponse = cc.getPropertiesWithResponse(null, null, null)

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
        cc.acquireLeaseWithResponse(null, -1, null, null, null).statusCode() == 201
    }

    @Unroll
    def "Acquire lease AC"() {
        setup:
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        expect:
        cc.acquireLeaseWithResponse(null, -1, mac, null, null).statusCode() == 201

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
        cc.acquireLeaseWithResponse(null, -1, mac, null, null)

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
        def mac = new ModifiedAccessConditions().ifMatch(match).ifNoneMatch(noneMatch)

        when:
        cc.acquireLeaseWithResponse(null, -1, mac, null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Acquire lease error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.acquireLease(null, 50)

        then:
        thrown(StorageException)
    }

    def "Renew lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        // If running in live mode wait for the lease to expire to ensure we are actually renewing it
        sleepIfRecord(16000)
        Response<String> renewLeaseResponse = cc.renewLeaseWithResponse(leaseID, null, null, null)

        expect:
        renewLeaseResponse.value() != null
        cc.getPropertiesWithResponse(null, null, null).headers().value("x-ms-lease-state") == LeaseStateType.LEASED.toString()
        validateBasicHeaders(renewLeaseResponse.headers())
    }

    def "Renew lease min"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        cc.renewLeaseWithResponse(leaseID, null, null, null).statusCode() == 200
    }

    @Unroll
    def "Renew lease AC"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        expect:
        cc.renewLeaseWithResponse(leaseID, mac, null, null).statusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Renew lease AC fail"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        when:
        cc.renewLease(leaseID, mac, null)

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
        cc.renewLease(receivedLeaseID, mac, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Renew lease error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.renewLease("id")

        then:
        thrown(StorageException)
    }

    def "Release lease"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        def releaseLeaseResponse = cc.releaseLeaseWithResponse(leaseID, null, null, null)

        expect:
        cc.getPropertiesWithResponse(null, null, null).headers().value("x-ms-lease-state") == LeaseStateType.AVAILABLE.toString()
        validateBasicHeaders(releaseLeaseResponse.headers())
    }

    def "Release lease min"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        cc.releaseLeaseWithResponse(leaseID, null, null, null).statusCode() == 200
    }

    @Unroll
    def "Release lease AC"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        expect:
        cc.releaseLeaseWithResponse(leaseID, mac, null, null).statusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Release lease AC fail"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        when:
        cc.releaseLeaseWithResponse(leaseID, mac, null, null)

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
        cc.releaseLeaseWithResponse(receivedLeaseID, mac, null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Release lease error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.releaseLease("id")

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Break lease"() {
        setup:
        cc.acquireLease(getRandomUUID(), leaseTime)

        def breakLeaseResponse = cc.breakLeaseWithResponse(breakPeriod, null, null, null)
        def state = LeaseStateType.fromString(cc.getPropertiesWithResponse(null, null, null).headers().value("x-ms-lease-state"))

        expect:
        state == LeaseStateType.BROKEN || state == LeaseStateType.BREAKING
        breakLeaseResponse.value().getSeconds() <= remainingTime
        validateBasicHeaders(breakLeaseResponse.headers())
        if (breakPeriod != null) {
            // If running in live mode wait for the lease to break so we can delete the container after the test completes
            sleepIfRecord(breakPeriod * 1000)
        }

        where:
        leaseTime | breakPeriod | remainingTime
        -1        | null        | 0
        -1        | 20          | 25
        20        | 15          | 16

    }

    def "Break lease min"() {
        setup:
        setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        cc.breakLeaseWithResponse(null, null, null, null).statusCode() == 202
    }

    @Unroll
    def "Break lease AC"() {
        setup:
        setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        expect:
        cc.breakLeaseWithResponse(null, mac, null, null).statusCode() == 202

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Break lease AC fail"() {
        setup:
        setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        when:
        cc.breakLeaseWithResponse(null, mac, null, null)

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
        cc.breakLeaseWithResponse(null, mac, null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Break lease error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.breakLease()

        then:
        thrown(StorageException)
    }

    def "Change lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        Response<String> changeLeaseResponse = cc.changeLeaseWithResponse(leaseID, getRandomUUID(), null, null, null)
        leaseID = changeLeaseResponse.value()

        expect:
        cc.releaseLeaseWithResponse(leaseID, null, null, null).statusCode() == 200
        validateBasicHeaders(changeLeaseResponse.headers())
    }

    def "Change lease min"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        cc.changeLeaseWithResponse(leaseID, getRandomUUID(), null, null, null).statusCode() == 200
    }

    @Unroll
    def "Change lease AC"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        expect:
        cc.changeLeaseWithResponse(leaseID, getRandomUUID(), mac, null, null).statusCode() == 200

        where:
        modified | unmodified
        null     | null
        oldDate  | null
        null     | newDate
    }

    @Unroll
    def "Change lease AC fail"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)
        def mac = new ModifiedAccessConditions().ifModifiedSince(modified).ifUnmodifiedSince(unmodified)

        when:
        cc.changeLeaseWithResponse(leaseID, getRandomUUID(), mac, null, null)

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
        cc.changeLeaseWithResponse(receivedLeaseID, garbageLeaseID, mac, null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Change lease error"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(generateContainerName())

        when:
        cc.changeLease("id", "id")

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Create URL special chars"() {
        // This test checks that we encode special characters in blob names correctly.
        setup:
        AppendBlobClient bu2 = cc.getAppendBlobClient(name)
        PageBlobClient bu3 = cc.getPageBlobClient(name + "2")
        BlockBlobClient bu4 = cc.getBlockBlobClient(name + "3")
        BlockBlobClient bu5 = cc.getBlockBlobClient(name)

        expect:
        bu2.createWithResponse(null, null, null, null, null).statusCode() == 201
        bu5.getPropertiesWithResponse(null, null, null).statusCode() == 200
        bu3.createWithResponse(512, null, null, null, null, null, null).statusCode() == 201
        bu4.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null, null).statusCode() == 201

        when:
        Iterator<BlobItem> blobs = cc.listBlobsFlat().iterator()

        then:
        blobs.next().name() == name
        blobs.next().name() == name + "2"
        blobs.next().name() == name + "3"

        where:
        name          | _
        // "中文"                 | _  TODO: requires blob name to be url encoded, deferred for post preview-1, storage team to decide on encoding story across SDKS
        "az[]"        | _
        // "hello world"         | _  TODO: see previous TODO
        "hello/world" | _
        "hello&world" | _
        // "!*'();:@&=+\$,/?#[]" | _  TODO: see previous TODO
    }

    def "Root explicit"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(ContainerClient.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        if (!cc.exists()) {
            cc.create()
        }

        AppendBlobClient bu = cc.getAppendBlobClient("rootblob")

        expect:
        bu.createWithResponse(null, null, null, null, null).statusCode() == 201
    }

    def "Root explicit in endpoint"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(ContainerClient.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        if (!cc.exists()) {
            cc.create()
        }

        AppendBlobClient bu = getBlobClient(primaryCredential,
            String.format("http://%s.blob.core.windows.net/%s/rootblob", primaryCredential.accountName(), ContainerClient.ROOT_CONTAINER_NAME))
            .asAppendBlobClient()

        when:
        Response<AppendBlobItem> createResponse = bu.createWithResponse(null, null, null, null, null)

        Response<BlobProperties> propsResponse = bu.getPropertiesWithResponse(null, null, null)

        then:
        createResponse.statusCode() == 201
        propsResponse.statusCode() == 200
        propsResponse.value().blobType() == BlobType.APPEND_BLOB
    }

    /*
    def "Root implicit"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(ContainerClient.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        if (!cc.exists().value()) {
            cc.create()
        }

        AppendBlobClient bc = new BlobClientBuilder()
            .credential(primaryCreds)
            .endpoint("http://" + primaryCreds.accountName() + ".blob.core.windows.net/rootblob")
            .httpClient(getHttpClient())
            .buildAppendBlobClient()

        when:
        Response<AppendBlobItem> createResponse = bc.create()

        Response<BlobProperties> propsResponse = bc.getProperties()

        then:
        createResponse.statusCode() == 201
        propsResponse.statusCode() == 200
        propsResponse.value().blobType() == BlobType.APPEND_BLOB
    }
    */

    def "Web container"() {
        setup:
        cc = primaryBlobServiceClient.getContainerClient(ContainerClient.STATIC_WEBSITE_CONTAINER_NAME)
        // Create root container if not exist.
        try {
            cc.create()
        }
        catch (StorageException se) {
            if (se.errorCode() != StorageErrorCode.CONTAINER_ALREADY_EXISTS) {
                throw se
            }
        }
        def webContainer = primaryBlobServiceClient.getContainerClient(ContainerClient.STATIC_WEBSITE_CONTAINER_NAME)

        when:
        // Validate some basic operation.
        webContainer.setAccessPolicy(null, null)

        then:
        notThrown(StorageException)
    }

    def "Get account info"() {
        when:
        def response = primaryBlobServiceClient.getAccountInfoWithResponse(null, null)

        then:
        response.headers().value("Date") != null
        response.headers().value("x-ms-version") != null
        response.headers().value("x-ms-request-id") != null
        response.value().accountKind() != null
        response.value().skuName() != null
    }

    def "Get account info min"() {
        expect:
        primaryBlobServiceClient.getAccountInfoWithResponse(null, null).statusCode() == 200
    }

    def "Get account info error"() {
        when:
        BlobServiceClient serviceURL = getServiceClient(primaryBlobServiceClient.getAccountUrl().toString())

        serviceURL.getContainerClient(generateContainerName()).getAccountInfo()

        then:
        thrown(StorageException)
    }
}
