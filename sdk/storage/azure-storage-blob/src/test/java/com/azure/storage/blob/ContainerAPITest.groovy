// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.rest.Response
import com.azure.storage.blob.models.AccessPolicy
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.AppendBlobItem
import com.azure.storage.blob.models.BlobListDetails
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.BlobContainerAccessConditions
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.LeaseAccessConditions
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.LeaseStatusType
import com.azure.storage.blob.models.ListBlobsOptions

import com.azure.storage.blob.models.ModifiedAccessConditions
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.models.SignedIdentifier
import com.azure.storage.blob.models.StorageErrorCode
import com.azure.storage.blob.models.StorageException
import com.azure.storage.blob.specialized.AppendBlobClient
import com.azure.storage.blob.specialized.BlobClientBase
import spock.lang.Unroll

import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.stream.Collectors

class ContainerAPITest extends APISpec {

    def "Create all null"() {
        setup:
        // Overwrite the existing cc, which has already been created
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        def response = cc.createWithResponse(null, null, null, null)

        then:
        response.getStatusCode() == 201
        validateBasicHeaders(response.getHeaders())
    }

    def "Create min"() {
        when:
        def cc = primaryBlobServiceClient.createBlobContainer(generateContainerName())

        then:
        cc.exists()
    }

    @Unroll
    def "Create metadata"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())
        def metadata = new HashMap<String, String>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        cc.createWithResponse(metadata, null, null, null)
        def response = cc.getPropertiesWithResponse(null, null, null)

        then:
        response.getValue().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
        "testFoo" | "testBar" | "testFizz" | "testBuzz"
    }

    @Unroll
    def "Create publicAccess"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        cc.createWithResponse(null, publicAccess, null, null)
        def access = cc.getProperties().getBlobPublicAccess()

        then:
        access == publicAccess

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
        e.getResponse().getStatusCode() == 409
        e.getErrorCode() == StorageErrorCode.CONTAINER_ALREADY_EXISTS
        e.getServiceMessage().contains("The specified container already exists.")
    }

    def "Get properties null"() {
        when:
        def response = cc.getPropertiesWithResponse(null, null, null)

        then:
        validateBasicHeaders(response.getHeaders())
        response.getValue().getBlobPublicAccess() == null
        !response.getValue().hasImmutabilityPolicy()
        !response.getValue().hasLegalHold()
        response.getValue().getLeaseDuration() == null
        response.getValue().getLeaseState() == LeaseStateType.AVAILABLE
        response.getValue().getLeaseStatus() == LeaseStatusType.UNLOCKED
        response.getValue().getMetadata().size() == 0
    }

    def "Get properties min"() {
        expect:
        cc.getProperties() != null
    }

    def "Get properties lease"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        cc.getPropertiesWithResponse(new LeaseAccessConditions().setLeaseId(leaseID), null, null).getStatusCode() == 200
    }

    def "Get properties lease fail"() {
        when:
        cc.getPropertiesWithResponse(new LeaseAccessConditions().setLeaseId("garbage"), null, null)

        then:
        thrown(StorageException)
    }

    def "Get properties error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        cc.getProperties()

        then:
        thrown(StorageException)
    }

    def "Set metadata"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())
        def metadata = new HashMap<String, String>()
        metadata.put("key", "value")
        cc.createWithResponse(metadata, null, null, null)

        when:
        def response = cc.setMetadataWithResponse(null, null, null, null)

        then:
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
        cc.getPropertiesWithResponse(null, null, null).getValue().getMetadata().size() == 0
    }

    def "Set metadata min"() {
        setup:
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")

        when:
        cc.setMetadata(metadata)

        then:
        cc.getPropertiesWithResponse(null, null, null).getValue().getMetadata() == metadata
    }

    @Unroll
    def "Set metadata metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        expect:
        cc.setMetadataWithResponse(metadata, null, null, null).getStatusCode() == 200
        cc.getPropertiesWithResponse(null, null, null).getValue().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Set metadata AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cc, leaseID)
        def cac = new BlobContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified))

        expect:
        cc.setMetadataWithResponse(null, cac, null, null).getStatusCode() == 200

        where:
        modified | leaseID
        null     | null
        oldDate  | null
        null     | receivedLeaseID
    }

    @Unroll
    def "Set metadata AC fail"() {
        setup:
        def cac = new BlobContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified))

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
        def mac = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        cc.setMetadataWithResponse(null, new BlobContainerAccessConditions().setModifiedAccessConditions(mac), null, null)

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
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

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
        validateBasicHeaders(response.getHeaders())
        cc.getProperties().getBlobPublicAccess() == access

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
        cc.getProperties().getBlobPublicAccess() == PublicAccessType.CONTAINER
    }

    def "Set access policy min ids"() {
        setup:
        def identifier = new SignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new AccessPolicy()
                .setStart(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiry(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermission("r"))

        def ids = [ identifier ] as List

        when:
        cc.setAccessPolicy(null, ids)

        then:
        cc.getAccessPolicy().getIdentifiers().get(0).getId() == "0000"
    }

    def "Set access policy ids"() {
        setup:
        def identifier = new SignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new AccessPolicy()
                .setStart(getUTCNow())
                .setExpiry(getUTCNow().plusDays(1))
                .setPermission("r"))
        def identifier2 = new SignedIdentifier()
            .setId("0001")
            .setAccessPolicy(new AccessPolicy()
                .setStart(getUTCNow())
                .setExpiry(getUTCNow().plusDays(2))
                .setPermission("w"))
        def ids = [ identifier, identifier2 ] as List

        when:
        def response = cc.setAccessPolicyWithResponse(null, ids, null, null, null)
        def receivedIdentifiers = cc.getAccessPolicyWithResponse(null, null, null).getValue().getIdentifiers()

        then:
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
        receivedIdentifiers.get(0).getAccessPolicy().getExpiry() == identifier.getAccessPolicy().getExpiry()
        receivedIdentifiers.get(0).getAccessPolicy().getStart() == identifier.getAccessPolicy().getStart()
        receivedIdentifiers.get(0).getAccessPolicy().getPermission() == identifier.getAccessPolicy().getPermission()
        receivedIdentifiers.get(1).getAccessPolicy().getExpiry() == identifier2.getAccessPolicy().getExpiry()
        receivedIdentifiers.get(1).getAccessPolicy().getStart() == identifier2.getAccessPolicy().getStart()
        receivedIdentifiers.get(1).getAccessPolicy().getPermission() == identifier2.getAccessPolicy().getPermission()
    }

    @Unroll
    def "Set access policy AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cc, leaseID)
        def cac = new BlobContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified))

        expect:
        cc.setAccessPolicyWithResponse(null, null, cac, null, null).getStatusCode() == 200

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
        def cac = new BlobContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified))

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
        def mac = new ModifiedAccessConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        cc.setAccessPolicyWithResponse(null, null, new BlobContainerAccessConditions().setModifiedAccessConditions(mac), null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Set access policy error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        cc.setAccessPolicy(null, null)

        then:
        thrown(StorageException)
    }

    def "Get access policy"() {
        setup:
        def identifier = new SignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new AccessPolicy()
                .setStart(getUTCNow())
                .setExpiry(getUTCNow().plusDays(1))
                .setPermission("r"))
        def ids = [ identifier ] as List
        cc.setAccessPolicy(PublicAccessType.BLOB, ids)
        def response = cc.getAccessPolicyWithResponse(null, null, null)

        expect:
        response.getStatusCode() == 200
        response.getValue().getBlobAccessType() == PublicAccessType.BLOB
        validateBasicHeaders(response.getHeaders())
        response.getValue().getIdentifiers().get(0).getAccessPolicy().getExpiry() == identifier.getAccessPolicy().getExpiry()
        response.getValue().getIdentifiers().get(0).getAccessPolicy().getStart() == identifier.getAccessPolicy().getStart()
        response.getValue().getIdentifiers().get(0).getAccessPolicy().getPermission() == identifier.getAccessPolicy().getPermission()
    }

    def "Get access policy lease"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        cc.getAccessPolicyWithResponse(new LeaseAccessConditions().setLeaseId(leaseID), null, null).getStatusCode() == 200
    }

    def "Get access policy lease fail"() {
        when:
        cc.getAccessPolicyWithResponse(new LeaseAccessConditions().setLeaseId(garbageLeaseID), null, null)

        then:
        thrown(StorageException)
    }

    def "Get access policy error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        cc.getAccessPolicy()

        then:
        thrown(StorageException)
    }

    def "Delete"() {
        when:
        def response = cc.deleteWithResponse(null, null, null)

        then:
        response.getStatusCode() == 202
        response.getHeaders().getValue("x-ms-request-id") != null
        response.getHeaders().getValue("x-ms-version") != null
        response.getHeaders().getValue("Date") != null
    }

    def "Delete min"() {
        when:
        cc.delete()

        then:
        !cc.exists()
    }

    @Unroll
    def "Delete AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cc, leaseID)
        def cac = new BlobContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified))

        expect:
        cc.deleteWithResponse(cac, null, null).getStatusCode() == 202

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
        def cac = new BlobContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseID))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfModifiedSince(modified)
                .setIfUnmodifiedSince(unmodified))

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
        def mac = new ModifiedAccessConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        cc.deleteWithResponse(new BlobContainerAccessConditions().setModifiedAccessConditions(mac), null, null)

        then:
        thrown(UnsupportedOperationException)

        where:
        match        | noneMatch
        receivedEtag | null
        null         | garbageEtag
    }

    def "Delete error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        cc.delete()

        then:
        thrown(StorageException)
    }

    def "List block blobs flat"() {
        setup:
        def name = generateBlobName()
        def bu = cc.getBlobClient(name).getBlockBlobClient()
        bu.upload(defaultInputStream.get(), 7)

        when:
        def blobs = cc.listBlobsFlat(new ListBlobsOptions().setPrefix(blobPrefix), null).iterator()

        //ContainerListBlobFlatSegmentHeaders headers = response.headers()
        //List<BlobItem> blobs = responseiterator()()

        then:
//        response.getStatusCode() == 200
//        headers.contentType() != null
//        headers.requestId() != null
//        headers.getVersion() != null
//        headers.date() != null
        def blob = blobs.next()
        !blobs.hasNext()
        blob.getName() == name
        blob.getProperties().getBlobType() == BlobType.BLOCK_BLOB
        blob.getProperties().getCopyCompletionTime() == null
        blob.getProperties().getCopyStatusDescription() == null
        blob.getProperties().getCopyId() == null
        blob.getProperties().getCopyProgress() == null
        blob.getProperties().getCopySource() == null
        blob.getProperties().getCopyStatus() == null
        blob.getProperties().isIncrementalCopy() == null
        blob.getProperties().getDestinationSnapshot() == null
        blob.getProperties().getLeaseDuration() == null
        blob.getProperties().getLeaseState() == LeaseStateType.AVAILABLE
        blob.getProperties().getLeaseStatus() == LeaseStatusType.UNLOCKED
        blob.getProperties().getContentLength() != null
        blob.getProperties().getContentType() != null
        blob.getProperties().getContentMD5() != null
        blob.getProperties().getContentEncoding() == null
        blob.getProperties().getContentDisposition() == null
        blob.getProperties().getContentLanguage() == null
        blob.getProperties().getCacheControl() == null
        blob.getProperties().getBlobSequenceNumber() == null
        blob.getProperties().isServerEncrypted()
        blob.getProperties().isAccessTierInferred()
        blob.getProperties().getAccessTier() == AccessTier.HOT
        blob.getProperties().getArchiveStatus() == null
        blob.getProperties().getCreationTime() != null
    }

    def "List page blobs flat"() {
        setup:
        ccPremium = premiumBlobServiceClient.getBlobContainerClient(containerName)
        ccPremium.create()
        def name = generateBlobName()
        def bu = ccPremium.getBlobClient(name).getPageBlobClient()
        bu.create(512)

        when:
        def blobs = ccPremium.listBlobsFlat(new ListBlobsOptions().setPrefix(blobPrefix), null).iterator()

        //ContainerListBlobFlatSegmentHeaders headers = response.headers()
        //List<BlobItem> blobs = responseiterator()()

        then:
//        response.getStatusCode() == 200
//        headers.contentType() != null
//        headers.requestId() != null
//        headers.getVersion() != null
//        headers.date() != null
        def blob = blobs.next()
        !blobs.hasNext()
        blob.getName() == name
        blob.getProperties().getBlobType() == BlobType.PAGE_BLOB
        blob.getProperties().getCopyCompletionTime() == null
        blob.getProperties().getCopyStatusDescription() == null
        blob.getProperties().getCopyId() == null
        blob.getProperties().getCopyProgress() == null
        blob.getProperties().getCopySource() == null
        blob.getProperties().getCopyStatus() == null
        blob.getProperties().isIncrementalCopy() == null
        blob.getProperties().getDestinationSnapshot() == null
        blob.getProperties().getLeaseDuration() == null
        blob.getProperties().getLeaseState() == LeaseStateType.AVAILABLE
        blob.getProperties().getLeaseStatus() == LeaseStatusType.UNLOCKED
        blob.getProperties().getContentLength() != null
        blob.getProperties().getContentType() != null
        blob.getProperties().getContentMD5() == null
        blob.getProperties().getContentEncoding() == null
        blob.getProperties().getContentDisposition() == null
        blob.getProperties().getContentLanguage() == null
        blob.getProperties().getCacheControl() == null
        blob.getProperties().getBlobSequenceNumber() == 0
        blob.getProperties().isServerEncrypted()
        blob.getProperties().isAccessTierInferred()
        blob.getProperties().getAccessTier() == AccessTier.P10
        blob.getProperties().getArchiveStatus() == null
        blob.getProperties().getCreationTime() != null

        cleanup:
        ccPremium.delete()
    }

    def "List blobs flat min"() {
        when:
        cc.listBlobsFlat().iterator().hasNext()

        then:
        notThrown(StorageException)
    }

    def setupListBlobsTest(String normalName, String copyName, String metadataName, String uncommittedName) {
        def normal = cc.getBlobClient(normalName).getPageBlobClient()
        normal.create(512)

        def copyBlob = cc.getBlobClient(copyName).getPageBlobClient()

        copyBlob.startCopyFromURL(new URL(normal.getBlobUrl()))
        def start = OffsetDateTime.now()
        def status = CopyStatusType.PENDING
        while (status != CopyStatusType.SUCCESS) {
            status = copyBlob.getProperties().getCopyStatus()
            OffsetDateTime currentTime = OffsetDateTime.now()
            if (status == CopyStatusType.FAILED || currentTime.minusMinutes(1) == start) {
                throw new Exception("Copy failed or took too long")
            }
            sleepIfRecord(1000)
        }

        def metadataBlob = cc.getBlobClient(metadataName).getPageBlobClient()
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")
        metadataBlob.createWithResponse(512, null, null, metadata, null, null, null)

        def snapshotTime = normal.createSnapshot().getSnapshotId()

        def uncommittedBlob = cc.getBlobClient(uncommittedName).getBlockBlobClient()

        uncommittedBlob.stageBlock("0000", defaultInputStream.get(), defaultData.remaining())

        return snapshotTime
    }

    def "List blobs flat options copy"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        def blobs = cc.listBlobsFlat(options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(1).getName() == copyName
        blobs.get(1).getProperties().getCopyId() != null
        // Comparing the urls isn't reliable because the service may use https.
        blobs.get(1).getProperties().getCopySource().contains(normalName)
        blobs.get(1).getProperties().getCopyStatus() == CopyStatusType.SUCCESS // We waited for the copy to complete.
        blobs.get(1).getProperties().getCopyProgress() != null
        blobs.get(1).getProperties().getCopyCompletionTime() != null
        blobs.size() == 3 // Normal, copy, metadata
    }

    def "List blobs flat options metadata"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveMetadata(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        def blobs = cc.listBlobsFlat(options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(1).getName() == copyName
        blobs.get(1).getProperties().getCopyCompletionTime() == null
        blobs.get(2).getName() == metadataName
        blobs.get(2).getMetadata().get("foo") == "bar"
        blobs.size() == 3 // Normal, copy, metadata
    }

    def "List blobs flat options snapshots"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveSnapshots(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        def snapshotTime = setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        def blobs = cc.listBlobsFlat(options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(0).getSnapshot() == snapshotTime
        blobs.get(1).getName() == normalName
        blobs.size() == 4 // Normal, snapshot, copy, metadata
    }

    def "List blobs flat options uncommitted"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveUncommittedBlobs(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        def blobs = cc.listBlobsFlat(options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(3).getName() == uncommittedName
        blobs.size() == 4 // Normal, copy, metadata, uncommitted
    }

    def "List blobs flat options deleted"() {
        setup:
        enableSoftDelete()
        def name = generateBlobName()
        def bu = cc.getBlobClient(name).getAppendBlobClient()
        bu.create()
        bu.delete()

        when:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveDeletedBlobs(true))
        def blobs = cc.listBlobsFlat(options, null).iterator()

        then:
        blobs.next().getName() == name
        !blobs.hasNext()

        disableSoftDelete() == null // Must produce a true value or test will fail.
    }

    def "List blobs flat options prefix"() {
        setup:
        def options = new ListBlobsOptions().setPrefix("a")
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        def blobs = cc.listBlobsFlat(options, null).iterator()

        then:
        blobs.next().getName() == normalName
        !blobs.hasNext() // Normal
    }

    def "List blobs flat options maxResults"() {
        setup:
        def PAGE_SIZE = 2
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true)
            .setRetrieveSnapshots(true).setRetrieveUncommittedBlobs(true)).setMaxResultsPerPage(PAGE_SIZE)
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        expect: "Get first page of blob listings (sync and async)"
        cc.listBlobsFlat(options, null).iterableByPage().iterator().next().getValue().size() == PAGE_SIZE
        ccAsync.listBlobsFlat(options).byPage().blockFirst().getValue().size() == PAGE_SIZE
    }

    def "List blobs flat options fail"() {
        when:
        new ListBlobsOptions().setMaxResultsPerPage(0)

        then:
        thrown(IllegalArgumentException)
    }

    def "List blobs flat marker"() {
        setup:
        def NUM_BLOBS = 10
        def PAGE_SIZE = 6
        for (int i = 0; i < NUM_BLOBS ; i++) {
            def bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()
            bc.create(512)
        }

        when: "list blobs with sync client"
        def pagedIterable = cc.listBlobsFlat(new ListBlobsOptions().setMaxResultsPerPage(PAGE_SIZE), null)
        def pagedSyncResponse1 = pagedIterable.iterableByPage().iterator().next()
        def pagedSyncResponse2 = pagedIterable.iterableByPage(pagedSyncResponse1.getContinuationToken()).iterator().next()

        then:
        pagedSyncResponse1.getValue().size() == PAGE_SIZE
        pagedSyncResponse2.getValue().size() == NUM_BLOBS - PAGE_SIZE
        pagedSyncResponse2.getContinuationToken() == null


        when: "list blobs with async client"
        def pagedFlux = ccAsync.listBlobsFlat(new ListBlobsOptions().setMaxResultsPerPage(PAGE_SIZE))
        def pagedResponse1 = pagedFlux.byPage().blockFirst()
        def pagedResponse2 = pagedFlux.byPage(pagedResponse1.getContinuationToken()).blockFirst()

        then:
        pagedResponse1.getValue().size() == PAGE_SIZE
        pagedResponse2.getValue().size() == NUM_BLOBS - PAGE_SIZE
        pagedResponse2.getContinuationToken() == null
    }

    def "List blobs flat error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        cc.listBlobsFlat().iterator().hasNext()

        then:
        thrown(StorageException)
    }

    def "List blobs flat with timeout still backed by PagedFlux"() {
        setup:
        def NUM_BLOBS = 5
        def PAGE_RESULTS = 3

        def blobs = [] as Collection<BlobClientBase>
        for (i in (1..NUM_BLOBS)) {
            def blob = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
            blob.upload(defaultInputStream.get(), defaultDataSize)
            blobs << blob
        }

        when: "Consume results by page"
        cc.listBlobsFlat(new ListBlobsOptions().setMaxResultsPerPage(PAGE_RESULTS), Duration.ofSeconds(10)).streamByPage().count()

        then: "Still have paging functionality"
        notThrown(Exception)
    }

    def "List blobs hier with timeout still backed by PagedFlux"() {
        setup:
        def NUM_BLOBS = 5
        def PAGE_RESULTS = 3

        def blobs = [] as Collection<BlobClientBase>
        for (i in (1..NUM_BLOBS)) {
            def blob = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
            blob.upload(defaultInputStream.get(), defaultDataSize)
            blobs << blob
        }

        when: "Consume results by page"
        cc.listBlobsHierarchy("/", new ListBlobsOptions().setMaxResultsPerPage(PAGE_RESULTS), Duration.ofSeconds(10)).streamByPage().count()

        then: "Still have paging functionality"
        notThrown(Exception)
    }

    def "List blobs hierarchy"() {
        setup:
        def name = generateBlobName()
        def bu = cc.getBlobClient(name).getPageBlobClient()
        bu.create(512)

        when:
        def blobs = cc.listBlobsHierarchy(null).iterator()

        then:
//        response.getStatusCode() == 200
//        headers.contentType() != null
//        headers.requestId() != null
//        headers.getVersion() != null
//        headers.date() != null
        blobs.next().getName() == name
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
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        def blobs = cc.listBlobsHierarchy("", options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(1).getName() == copyName
        blobs.get(1).getProperties().getCopyId() != null
        // Comparing the urls isn't reliable because the service may use https.
        blobs.get(1).getProperties().getCopySource().contains(normalName)
        blobs.get(1).getProperties().getCopyStatus() == CopyStatusType.SUCCESS // We waited for the copy to complete.
        blobs.get(1).getProperties().getCopyProgress() != null
        blobs.get(1).getProperties().getCopyCompletionTime() != null
        blobs.size() == 3 // Normal, copy, metadata
    }

    def "List blobs hier options metadata"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveMetadata(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        def blobs = cc.listBlobsHierarchy("", options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(1).getName() == copyName
        blobs.get(1).getProperties().getCopyCompletionTime() == null
        blobs.get(2).getName() == metadataName
        blobs.get(2).getMetadata().get("foo") == "bar"
        blobs.size() == 3 // Normal, copy, metadata
    }

    def "List blobs hier options uncommitted"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveUncommittedBlobs(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        def blobs = cc.listBlobsHierarchy("", options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(3).getName() == uncommittedName
        blobs.size() == 4 // Normal, copy, metadata, uncommitted
    }

    def "List blobs hier options deleted"() {
        setup:
        enableSoftDelete()
        def name = generateBlobName()
        def bc = cc.getBlobClient(name).getAppendBlobClient()
        bc.create()
        bc.delete()

        when:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveDeletedBlobs(true))
        def blobs = cc.listBlobsHierarchy("", options, null).iterator()

        then:
        blobs.next().getName() == name
        !blobs.hasNext()

        disableSoftDelete() == null
    }

    def "List blobs hier options prefix"() {
        setup:
        def options = new ListBlobsOptions().setPrefix("a")
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        def blobs = cc.listBlobsHierarchy("", options, null).iterator()

        then:
        blobs.next().getName() == normalName
        !blobs.hasNext() // Normal
    }


    def "List blobs hier options maxResults"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true)
                .setRetrieveUncommittedBlobs(true)).setMaxResultsPerPage(1)
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        // use async client, as there is no paging functionality for sync yet
        def blobs = ccAsync.listBlobsHierarchy("", options).byPage().blockFirst()

        then:
        blobs.getValue().size() == 1
    }

    @Unroll
    def "List blobs hier options fail"() {
        when:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveSnapshots(snapshots))
                .setMaxResultsPerPage(maxResults)
        cc.listBlobsHierarchy(null, options, null).iterator().hasNext()

        then:
        thrown(exceptionType)

        where:
        snapshots | maxResults | exceptionType
        true      | 5          | UnsupportedOperationException
        false     | 0          | IllegalArgumentException
    }

    def "List blobs hier delim"() {
        setup:
        def blobNames = ["a", "b/a", "c", "d/a", "e", "f", "g/a"]
        for (def blobName : blobNames) {
            def bu = cc.getBlobClient(blobName).getAppendBlobClient()
            bu.create()
        }

        when:
        def foundBlobs = [] as Set
        def foundPrefixes = [] as Set
        cc.listBlobsHierarchy(null).stream().collect(Collectors.toList())
            .forEach { blobItem ->
            if (blobItem.isPrefix()) {
                foundPrefixes << blobItem.getName()
            }
            else {
                foundBlobs << blobItem.getName()
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
            def bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()
            bc.create(512)
        }

        def blobs = cc.listBlobsHierarchy("/", new ListBlobsOptions().setMaxResultsPerPage(PAGE_SIZE), null)

        when:
        def firstPage = blobs.iterableByPage().iterator().next()

        then:
        firstPage.getValue().size() == PAGE_SIZE
        firstPage.getContinuationToken() != null

        when:
        def secondPage = blobs.iterableByPage(firstPage.getContinuationToken()).iterator().next()

        then:
        secondPage.getValue().size() == NUM_BLOBS - PAGE_SIZE
        secondPage.getContinuationToken() == null
    }

    def "List blobs flat simple"() {
        setup: "Create 10 page blobs in the container"
        def NUM_BLOBS = 10
        def PAGE_SIZE = 3
        for (int i = 0; i < NUM_BLOBS; i++) {
            def bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()
            bc.create(512)
        }

        expect: "listing operation will fetch all 10 blobs, despite page size being smaller than 10"
        cc.listBlobsFlat(new ListBlobsOptions().setMaxResultsPerPage(PAGE_SIZE), null).stream().count() == NUM_BLOBS
    }

    def "List blobs hier error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        cc.listBlobsHierarchy(".").iterator().hasNext()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Create URL special chars"() {
        // This test checks that we encode special characters in blob names correctly.
        setup:
        def bu2 = cc.getBlobClient(name).getAppendBlobClient()
        def bu3 = cc.getBlobClient(name + "2").getPageBlobClient()
        def bu4 = cc.getBlobClient(name + "3").getBlockBlobClient()
        def bu5 = cc.getBlobClient(name).getBlockBlobClient()

        expect:
        bu2.createWithResponse(null, null, null, null, null).getStatusCode() == 201
        bu5.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
        bu3.createWithResponse(512, null, null, null, null, null, null).getStatusCode() == 201
        bu4.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, null, null, null, null).getStatusCode() == 201

        when:
        def blobs = cc.listBlobsFlat().iterator()

        then:
        blobs.next().getName() == name
        blobs.next().getName() == name + "2"
        blobs.next().getName() == name + "3"

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
        cc = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        if (!cc.exists()) {
            cc.create()
        }

        def bu = cc.getBlobClient("rootblob").getAppendBlobClient()

        expect:
        bu.createWithResponse(null, null, null, null, null).getStatusCode() == 201
    }

    def "Root explicit in endpoint"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        if (!cc.exists()) {
            cc.create()
        }

        def bu = cc.getBlobClient("rootblob").getAppendBlobClient()

        when:
        def createResponse = bu.createWithResponse(null, null, null, null, null)
        def propsResponse = bu.getPropertiesWithResponse(null, null, null)

        then:
        createResponse.getStatusCode() == 201
        propsResponse.getStatusCode() == 200
        propsResponse.getValue().getBlobType() == BlobType.APPEND_BLOB
    }

    def "BlobClientBuilder Root implicit"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        if (!cc.exists()) {
            cc.create()
        }

        AppendBlobClient bc = new BlobClientBuilder()
            .credential(primaryCredential)
            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .blobName("rootblob")
            .httpClient(getHttpClient())
            .pipeline(cc.getHttpPipeline())
            .buildClient().getAppendBlobClient()

        when:
        Response<AppendBlobItem> createResponse = bc.createWithResponse(null, null, null, null, null)

        Response<BlobProperties> propsResponse = bc.getPropertiesWithResponse(null, null, null)

        then:
        createResponse.getStatusCode() == 201
        propsResponse.getStatusCode() == 200
        propsResponse.getValue().getBlobType() == BlobType.APPEND_BLOB
    }

    def "ContainerClientBuilder root implicit"(){
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        if (!cc.exists()) {
            cc.create()
        }

        when:
        cc = new BlobContainerClientBuilder()
            .credential(primaryCredential)
            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .containerName(null)
            .pipeline(cc.getHttpPipeline())
            .buildClient()

        then:
        cc.getProperties() != null
        cc.getBlobContainerName() == BlobContainerAsyncClient.ROOT_CONTAINER_NAME

        when:
        def bc = cc.getBlobClient("rootblob").getAppendBlobClient()
        bc.create()

        then:
        bc.exists()
    }

    def "ServiceClient implicit root"() {
        expect:
        primaryBlobServiceClient.getBlobContainerClient(null).getBlobContainerName() ==
            BlobContainerAsyncClient.ROOT_CONTAINER_NAME
        primaryBlobServiceClient.getBlobContainerClient("").getBlobContainerName() ==
            BlobContainerAsyncClient.ROOT_CONTAINER_NAME
    }

    def "Web container"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.STATIC_WEBSITE_CONTAINER_NAME)
        // Create root container if not exist.
        try {
            cc.create()
        }
        catch (StorageException se) {
            if (se.getErrorCode() != StorageErrorCode.CONTAINER_ALREADY_EXISTS) {
                throw se
            }
        }
        def webContainer = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.STATIC_WEBSITE_CONTAINER_NAME)

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
        response.getHeaders().getValue("Date") != null
        response.getHeaders().getValue("x-ms-version") != null
        response.getHeaders().getValue("x-ms-request-id") != null
        response.getValue().getAccountKind() != null
        response.getValue().getSkuName() != null
    }

    def "Get account info min"() {
        expect:
        primaryBlobServiceClient.getAccountInfoWithResponse(null, null).getStatusCode() == 200
    }

    def "Get account info error"() {
        when:
        def serviceURL = getServiceClient(primaryBlobServiceClient.getAccountUrl())

        serviceURL.getBlobContainerClient(generateContainerName()).getAccountInfo()

        then:
        thrown(IllegalArgumentException)
    }

    def "Get Container Name"() {
        given:
        def containerName = generateContainerName()
        def newcc = primaryBlobServiceClient.getBlobContainerClient(containerName)
        expect:
        containerName == newcc.getBlobContainerName()
    }
}
