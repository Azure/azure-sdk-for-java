// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob


import com.azure.core.http.rest.Response
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.AppendBlobItem
import com.azure.storage.blob.models.BlobAccessPolicy
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobListDetails
import com.azure.storage.blob.models.BlobProperties
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobSignedIdentifier
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.CustomerProvidedKey
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.LeaseStatusType
import com.azure.storage.blob.models.ListBlobsOptions
import com.azure.storage.blob.models.ObjectReplicationPolicy
import com.azure.storage.blob.models.ObjectReplicationStatus
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.models.RehydratePriority
import com.azure.storage.blob.options.BlobSetAccessTierOptions
import com.azure.storage.blob.options.PageBlobCreateOptions
import com.azure.storage.blob.specialized.AppendBlobClient
import com.azure.storage.blob.specialized.BlobClientBase
import com.azure.storage.common.Utility
import com.azure.storage.common.test.shared.extensions.PlaybackOnly
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import reactor.test.StepVerifier
import spock.lang.Unroll

import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.stream.Collectors

class ContainerAPITest extends APISpec {

    String tagKey
    String tagValue

    def setup() {
        tagKey = namer.getRandomName(20)
        tagValue = namer.getRandomName(20)
    }

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
        key1      | value1    | key2       | value2
        null      | null      | null       | null
        "foo"     | "bar"     | "fizz"     | "buzz"
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
        def e = thrown(BlobStorageException)
        e.getResponse().getStatusCode() == 409
        e.getErrorCode() == BlobErrorCode.CONTAINER_ALREADY_EXISTS
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
        !response.getValue().isEncryptionScopeOverridePrevented()
        response.getValue().getDefaultEncryptionScope()

    }

    def "Get properties min"() {
        expect:
        cc.getProperties() != null
    }

    def "Get properties lease"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        cc.getPropertiesWithResponse(leaseID, null, null).getStatusCode() == 200
    }

    def "Get properties lease fail"() {
        when:
        cc.getPropertiesWithResponse("garbage", null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Get properties error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        cc.getProperties()

        then:
        thrown(BlobStorageException)
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
        def cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)

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
        def cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)

        when:
        cc.setMetadataWithResponse(null, cac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | leaseID
        newDate  | null
        null     | garbageLeaseID
    }

    @Unroll
    def "Set metadata AC illegal"() {
        setup:
        def mac = new BlobRequestConditions()
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        cc.setMetadataWithResponse(null, mac, null, null)

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
        thrown(BlobStorageException)
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
        def identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"))

        def ids = [identifier] as List

        when:
        cc.setAccessPolicy(null, ids)

        then:
        cc.getAccessPolicy().getIdentifiers().get(0).getId() == "0000"
    }

    def "Set access policy ids"() {
        setup:
        def identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(namer.getUtcNow())
                .setExpiresOn(namer.getUtcNow().plusDays(1))
                .setPermissions("r"))
        def identifier2 = new BlobSignedIdentifier()
            .setId("0001")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(namer.getUtcNow())
                .setExpiresOn(namer.getUtcNow().plusDays(2))
                .setPermissions("w"))
        def ids = [identifier, identifier2] as List

        when:
        def response = cc.setAccessPolicyWithResponse(null, ids, null, null, null)
        def receivedIdentifiers = cc.getAccessPolicyWithResponse(null, null, null).getValue().getIdentifiers()

        then:
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
        receivedIdentifiers.get(0).getAccessPolicy().getExpiresOn() == identifier.getAccessPolicy().getExpiresOn()
        receivedIdentifiers.get(0).getAccessPolicy().getStartsOn() == identifier.getAccessPolicy().getStartsOn()
        receivedIdentifiers.get(0).getAccessPolicy().getPermissions() == identifier.getAccessPolicy().getPermissions()
        receivedIdentifiers.get(1).getAccessPolicy().getExpiresOn() == identifier2.getAccessPolicy().getExpiresOn()
        receivedIdentifiers.get(1).getAccessPolicy().getStartsOn() == identifier2.getAccessPolicy().getStartsOn()
        receivedIdentifiers.get(1).getAccessPolicy().getPermissions() == identifier2.getAccessPolicy().getPermissions()
    }

    @Unroll
    def "Set access policy AC"() {
        setup:
        leaseID = setupContainerLeaseCondition(cc, leaseID)
        def cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

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
        def cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        cc.setAccessPolicyWithResponse(null, null, cac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | leaseID
        newDate  | null       | null
        null     | oldDate    | null
        null     | null       | garbageLeaseID
    }

    @Unroll
    def "Set access policy AC illegal"() {
        setup:
        def mac = new BlobRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        cc.setAccessPolicyWithResponse(null, null, mac, null, null)

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
        thrown(BlobStorageException)
    }

    def "Get access policy"() {
        setup:
        def identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(namer.getUtcNow())
                .setExpiresOn(namer.getUtcNow().plusDays(1))
                .setPermissions("r"))
        def ids = [identifier] as List
        cc.setAccessPolicy(PublicAccessType.BLOB, ids)
        def response = cc.getAccessPolicyWithResponse(null, null, null)

        expect:
        response.getStatusCode() == 200
        response.getValue().getBlobAccessType() == PublicAccessType.BLOB
        validateBasicHeaders(response.getHeaders())
        response.getValue().getIdentifiers().get(0).getAccessPolicy().getExpiresOn() == identifier.getAccessPolicy().getExpiresOn()
        response.getValue().getIdentifiers().get(0).getAccessPolicy().getStartsOn() == identifier.getAccessPolicy().getStartsOn()
        response.getValue().getIdentifiers().get(0).getAccessPolicy().getPermissions() == identifier.getAccessPolicy().getPermissions()
    }

    def "Get access policy lease"() {
        setup:
        def leaseID = setupContainerLeaseCondition(cc, receivedLeaseID)

        expect:
        cc.getAccessPolicyWithResponse(leaseID, null, null).getStatusCode() == 200
    }

    def "Get access policy lease fail"() {
        when:
        cc.getAccessPolicyWithResponse(garbageLeaseID, null, null)

        then:
        thrown(BlobStorageException)
    }

    def "Get access policy error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        cc.getAccessPolicy()

        then:
        thrown(BlobStorageException)
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
        def cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

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
        def cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        cc.deleteWithResponse(cac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | leaseID
        newDate  | null       | null
        null     | oldDate    | null
        null     | null       | garbageLeaseID
    }

    @Unroll
    def "Delete AC illegal"() {
        setup:
        def mac = new BlobRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch)

        when:
        cc.deleteWithResponse(mac, null, null)

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
        thrown(BlobStorageException)
    }

    def "List block blobs flat"() {
        setup:
        def name = generateBlobName()
        def bu = cc.getBlobClient(name).getBlockBlobClient()
        bu.upload(data.defaultInputStream, 7)

        when:
        def blobs = cc.listBlobs(new ListBlobsOptions().setPrefix(namer.getResourcePrefix()), null).iterator()

        then:
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
        blob.getProperties().getContentMd5() != null
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "List append blobs flat"() {
        setup:
        def name = generateBlobName()
        def bu = cc.getBlobClient(name).getAppendBlobClient()
        bu.create()
        bu.seal()

        when:
        def blobs = cc.listBlobs(new ListBlobsOptions().setPrefix(namer.getResourcePrefix()), null).iterator()

        then:
        def blob = blobs.next()
        !blobs.hasNext()
        blob.getName() == name
        blob.getProperties().getBlobType() == BlobType.APPEND_BLOB
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
        blob.getProperties().getContentMd5() == null
        blob.getProperties().getContentEncoding() == null
        blob.getProperties().getContentDisposition() == null
        blob.getProperties().getContentLanguage() == null
        blob.getProperties().getCacheControl() == null
        blob.getProperties().getBlobSequenceNumber() == null
        blob.getProperties().isServerEncrypted()
        blob.getProperties().isAccessTierInferred() == null
        blob.getProperties().getAccessTier() == null
        blob.getProperties().getArchiveStatus() == null
        blob.getProperties().getCreationTime() != null
        blob.getProperties().isSealed()
    }

    def "List page blobs flat"() {
        setup:
        ccPremium = premiumBlobServiceClient.getBlobContainerClient(containerName)
        ccPremium.create()
        def name = generateBlobName()
        def bu = ccPremium.getBlobClient(name).getPageBlobClient()
        bu.create(512)

        when:
        def blobs = ccPremium.listBlobs(new ListBlobsOptions().setPrefix(namer.getResourcePrefix()), null).iterator()

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
        blob.getProperties().getContentMd5() == null
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
        cc.listBlobs().iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    def setupListBlobsTest(String normalName, String copyName, String metadataName, String tagsName,
                           String uncommittedName) {
        def normal = cc.getBlobClient(normalName).getPageBlobClient()
        normal.create(512)

        def copyBlob = cc.getBlobClient(copyName).getPageBlobClient()
        copyBlob.beginCopy(normal.getBlobUrl(), getPollingDuration(5000)).waitForCompletion()

        def metadataBlob = cc.getBlobClient(metadataName).getPageBlobClient()
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")
        metadataBlob.createWithResponse(512, null, null, metadata, null, null, null)

        def tagsBlob = cc.getBlobClient(tagsName).getPageBlobClient()
        def tags = new HashMap<String, String>()
        tags.put(tagKey, tagValue)
        tagsBlob.createWithResponse(new PageBlobCreateOptions(512).setTags(tags), null, null)

        def uncommittedBlob = cc.getBlobClient(uncommittedName).getBlockBlobClient()
        uncommittedBlob.stageBlock(getBlockID(), data.defaultInputStream, data.defaultData.remaining())

        return normal.createSnapshot().getSnapshotId()
    }

    def "List blobs flat options copy"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        when:
        def blobs = cc.listBlobs(options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(1).getName() == copyName
        blobs.get(1).getProperties().getCopyId() != null
        // Comparing the urls isn't reliable because the service may use https.
        blobs.get(1).getProperties().getCopySource().contains(normalName)
        blobs.get(1).getProperties().getCopyStatus() == CopyStatusType.SUCCESS // We waited for the copy to complete.
        blobs.get(1).getProperties().getCopyProgress() != null
        blobs.get(1).getProperties().getCopyCompletionTime() != null
        blobs.size() == 4 // Normal, copy, metadata, tags
    }

    def "List blobs flat options metadata"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveMetadata(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        when:
        def blobs = cc.listBlobs(options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(1).getName() == copyName
        blobs.get(1).getProperties().getCopyCompletionTime() == null
        blobs.get(2).getName() == metadataName
        blobs.get(2).getMetadata().get("foo") == "bar"
        blobs.size() == 4 // Normal, copy, metadata, tags
    }

    @PlaybackOnly
    def "List blobs flat options last access time"() {
        when:
        def b = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        b.upload(data.defaultInputStream, data.defaultData.remaining())
        def blob = cc.listBlobs().iterator().next()

        then:
        blob.getProperties().getLastAccessedTime()
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "List blobs flat options tags"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveTags(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        when:
        def blobs = cc.listBlobs(options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(1).getName() == copyName
        blobs.get(1).getProperties().getCopyCompletionTime() == null
        blobs.get(2).getName() == metadataName
        blobs.get(2).getMetadata() == null
        blobs.get(3).getTags().get(tagKey) == tagValue
        blobs.get(3).getProperties().getTagCount() == 1
        blobs.size() == 4 // Normal, copy, metadata, tags
    }

    def "List blobs flat options snapshots"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveSnapshots(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        def snapshotTime = setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        when:
        def blobs = cc.listBlobs(options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(0).getSnapshot() == snapshotTime
        blobs.get(1).getName() == normalName
        blobs.size() == 5 // Normal, snapshot, copy, metadata, tags
    }

    def "List blobs flat options uncommitted"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveUncommittedBlobs(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        when:
        def blobs = cc.listBlobs(options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(4).getName() == uncommittedName
        blobs.size() == 5 // Normal, copy, metadata, tags, uncommitted
    }

    def "List blobs flat options prefix"() {
        setup:
        def options = new ListBlobsOptions().setPrefix("a")
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        when:
        def blobs = cc.listBlobs(options, null).iterator()

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
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        expect: "Get first page of blob listings (sync and async)"
        cc.listBlobs(options, null).iterableByPage().iterator().next().getValue().size() == PAGE_SIZE
        StepVerifier.create(ccAsync.listBlobs(options).byPage().limitRequest(1))
            .assertNext({ assert it.getValue().size() == PAGE_SIZE })
            .verifyComplete()
    }

    def "List blobs flat options maxResults by page"() {
        setup:
        def PAGE_SIZE = 2
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true)
            .setRetrieveSnapshots(true).setRetrieveUncommittedBlobs(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        expect: "Get first page of blob listings (sync and async)"
        for (def page : cc.listBlobs(options, null).iterableByPage(PAGE_SIZE)) {
            assert page.value.size() <= PAGE_SIZE
        }
        StepVerifier.create(ccAsync.listBlobs(options).byPage(PAGE_SIZE).limitRequest(1))
            .assertNext({ assert it.getValue().size() == PAGE_SIZE })
            .verifyComplete()
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_10_02")
    def "list blobs flat options deleted with versions"() {
        setup:
        def blobName = generateBlobName()
        def blob = cc.getBlobClient(blobName).getAppendBlobClient()
        blob.create()
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")
        blob.setMetadata(metadata)
        blob.delete()
        def options = new ListBlobsOptions().setPrefix(blobName)
            .setDetails(new BlobListDetails().setRetrieveDeletedBlobsWithVersions(true))

        when:
        def blobs = cc.listBlobs(options, null).iterator()

        then:
        def b = blobs.next()
        !blobs.hasNext()
        b.getName() == blobName
        b.hasVersionsOnly()
    }

    def "List blobs prefix with comma"() {
        setup:
        def prefix = generateBlobName() + ", " + generateBlobName()
        def b = cc.getBlobClient(prefix).getBlockBlobClient()
        b.upload(data.defaultInputStream, data.defaultData.remaining())

        when:
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(prefix)
        def blob = cc.listBlobs(options, null).iterator().next()

        then:
        blob.getName() == prefix
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
        for (int i = 0; i < NUM_BLOBS; i++) {
            def bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()
            bc.create(512)
        }

        when: "list blobs with sync client"
        def pagedIterable = cc.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(PAGE_SIZE), null)
        def pagedSyncResponse1 = pagedIterable.iterableByPage().iterator().next()
        def pagedSyncResponse2 = pagedIterable.iterableByPage(pagedSyncResponse1.getContinuationToken()).iterator().next()

        then:
        pagedSyncResponse1.getValue().size() == PAGE_SIZE
        pagedSyncResponse2.getValue().size() == NUM_BLOBS - PAGE_SIZE
        pagedSyncResponse2.getContinuationToken() == null


        when: "list blobs with async client"
        def pagedFlux = ccAsync.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(PAGE_SIZE))
        def pagedResponse1 = pagedFlux.byPage().blockFirst()
        def pagedResponse2 = pagedFlux.byPage(pagedResponse1.getContinuationToken()).blockFirst()

        then:
        pagedResponse1.getValue().size() == PAGE_SIZE
        pagedResponse2.getValue().size() == NUM_BLOBS - PAGE_SIZE
        pagedResponse2.getContinuationToken() == null
    }

    def "List blobs flat marker overload"() {
        setup:
        def NUM_BLOBS = 10
        def PAGE_SIZE = 6
        for (int i = 0; i < NUM_BLOBS; i++) {
            def bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()
            bc.create(512)
        }

        when: "list blobs with sync client"
        def pagedIterable = cc.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(PAGE_SIZE), null)
        def pagedSyncResponse1 = pagedIterable.iterableByPage().iterator().next()

        pagedIterable = cc.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(PAGE_SIZE),
            pagedSyncResponse1.getContinuationToken(), null)
        def pagedSyncResponse2 = pagedIterable.iterableByPage().iterator().next()

        then:
        pagedSyncResponse1.getValue().size() == PAGE_SIZE
        pagedSyncResponse2.getValue().size() == NUM_BLOBS - PAGE_SIZE
        pagedSyncResponse2.getContinuationToken() == null


        when: "list blobs with async client"
        def pagedFlux = ccAsync.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(PAGE_SIZE))
        def pagedResponse1 = pagedFlux.byPage().blockFirst()
        pagedFlux = ccAsync.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(PAGE_SIZE),
            pagedResponse1.getContinuationToken())
        def pagedResponse2 = pagedFlux.byPage().blockFirst()

        then:
        pagedResponse1.getValue().size() == PAGE_SIZE
        pagedResponse2.getValue().size() == NUM_BLOBS - PAGE_SIZE
        pagedResponse2.getContinuationToken() == null
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "List blobs flat rehydrate priority"() {
        setup:
        def name = generateBlobName()
        def bc = cc.getBlobClient(name).getBlockBlobClient()
        bc.upload(data.defaultInputStream, 7)

        if (rehydratePriority != null) {
            bc.setAccessTier(AccessTier.ARCHIVE)

            bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT).setPriority(rehydratePriority), null, null)
        }

        when:
        def item = cc.listBlobs().iterator().next()

        then:
        item.getProperties().getRehydratePriority() == rehydratePriority

        where:
        rehydratePriority          || _
        null                       || _
        RehydratePriority.STANDARD || _
        RehydratePriority.HIGH     || _
    }

    def "List blobs flat error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        cc.listBlobs().iterator().hasNext()

        then:
        thrown(BlobStorageException)
    }

    def "List blobs flat with timeout still backed by PagedFlux"() {
        setup:
        def NUM_BLOBS = 5
        def PAGE_RESULTS = 3

        def blobs = [] as Collection<BlobClientBase>
        for (i in (1..NUM_BLOBS)) {
            def blob = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
            blob.upload(data.defaultInputStream, data.defaultDataSize)
            blobs << blob
        }

        when: "Consume results by page"
        cc.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(PAGE_RESULTS), Duration.ofSeconds(10)).streamByPage().count()

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
            blob.upload(data.defaultInputStream, data.defaultDataSize)
            blobs << blob
        }

        when: "Consume results by page"
        cc.listBlobsByHierarchy("/", new ListBlobsOptions().setMaxResultsPerPage(PAGE_RESULTS), Duration.ofSeconds(10)).streamByPage().count()

        then: "Still have paging functionality"
        notThrown(Exception)
    }

    /*
    This test requires two accounts that are configured in a very specific way. It is not feasible to setup that
    relationship programmatically, so we have recorded a successful interaction and only test recordings.
    */
    @PlaybackOnly
    def "List blobs flat ORS"() {
        setup:
        def sourceContainer = primaryBlobServiceClient.getBlobContainerClient("test1")
        def destContainer = alternateBlobServiceClient.getBlobContainerClient("test2")

        when:
        def sourceBlobs = sourceContainer.listBlobs().stream().collect(Collectors.toList())
        def destBlobs = destContainer.listBlobs().stream().collect(Collectors.toList())

        then:
        int i = 0
        for (def blob : sourceBlobs) {
            if (i == 1) {
                assert blob.getObjectReplicationSourcePolicies() == null
            } else {
                assert validateOR(blob.getObjectReplicationSourcePolicies(), "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80", "105f9aad-f39b-4064-8e47-ccd7937295ca")
            }
            i++
        }

        /* Service specifies no ors metadata on the dest blobs. */
        for (def blob : destBlobs) {
            assert blob.getObjectReplicationSourcePolicies() == null
        }
    }

    def validateOR(List<ObjectReplicationPolicy> policies, String policyId, String ruleId) {
        return policies.stream()
            .filter({ policy -> policyId.equals(policy.getPolicyId()) })
            .findFirst()
            .get()
            .getRules()
            .stream()
            .filter({ rule -> ruleId.equals(rule.getRuleId()) })
            .findFirst()
            .get()
            .getStatus() == ObjectReplicationStatus.COMPLETE
    }

    def "List blobs hierarchy"() {
        setup:
        def name = generateBlobName()
        def bu = cc.getBlobClient(name).getPageBlobClient()
        bu.create(512)

        when:
        def blobs = cc.listBlobsByHierarchy(null).iterator()

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
        cc.listBlobsByHierarchy("/").iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    def "List blobs hier options copy"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        when:
        def blobs = cc.listBlobsByHierarchy("", options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(1).getName() == copyName
        blobs.get(1).getProperties().getCopyId() != null
        // Comparing the urls isn't reliable because the service may use https.
        blobs.get(1).getProperties().getCopySource().contains(normalName)
        blobs.get(1).getProperties().getCopyStatus() == CopyStatusType.SUCCESS // We waited for the copy to complete.
        blobs.get(1).getProperties().getCopyProgress() != null
        blobs.get(1).getProperties().getCopyCompletionTime() != null
        blobs.size() == 4 // Normal, copy, metadata, tags
    }

    def "List blobs hier options metadata"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveMetadata(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        when:
        def blobs = cc.listBlobsByHierarchy("", options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(1).getName() == copyName
        blobs.get(1).getProperties().getCopyCompletionTime() == null
        blobs.get(2).getName() == metadataName
        blobs.get(2).getMetadata().get("foo") == "bar"
        blobs.size() == 4 // Normal, copy, metadata, tags
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "List blobs hier options tags"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveTags(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        when:
        def blobs = cc.listBlobsByHierarchy("", options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(1).getName() == copyName
        blobs.get(1).getProperties().getCopyCompletionTime() == null
        blobs.get(2).getName() == metadataName
        blobs.get(2).getMetadata() == null
        blobs.get(3).getTags().get(tagKey) == tagValue
        blobs.get(3).getProperties().getTagCount() == 1
        blobs.size() == 4 // Normal, copy, metadata, tags
    }

    def "List blobs hier options uncommitted"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveUncommittedBlobs(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        when:
        def blobs = cc.listBlobsByHierarchy("", options, null).stream().collect(Collectors.toList())

        then:
        blobs.get(0).getName() == normalName
        blobs.get(4).getName() == uncommittedName
        blobs.size() == 5 // Normal, copy, metadata, tags, uncommitted
    }

    def "List blobs hier options prefix"() {
        setup:
        def options = new ListBlobsOptions().setPrefix("a")
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        when:
        def blobs = cc.listBlobsByHierarchy("", options, null).iterator()

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
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        expect:
        StepVerifier.create(ccAsync.listBlobsByHierarchy("", options).byPage().limitRequest(1))
            .assertNext({ assert it.getValue().size() == 1 })
            .verifyComplete()
    }

    def "List blobs hier options maxResults by page"() {
        setup:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true)
            .setRetrieveUncommittedBlobs(true))
        def normalName = "a" + generateBlobName()
        def copyName = "c" + generateBlobName()
        def metadataName = "m" + generateBlobName()
        def tagsName = "t" + generateBlobName()
        def uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName)

        expect:
        def pagedIterable = cc.listBlobsByHierarchy("", options, null);

        def iterableByPage = pagedIterable.iterableByPage(1)
        for (def page : iterableByPage) {
            assert page.value.size() == 1
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_10_02")
    def "list blobs hier options deleted with versions"() {
        setup:
        def blobName = generateBlobName()
        def blob = cc.getBlobClient(blobName).getAppendBlobClient()
        blob.create()
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")
        blob.setMetadata(metadata)
        blob.delete()
        def options = new ListBlobsOptions().setPrefix(blobName)
            .setDetails(new BlobListDetails().setRetrieveDeletedBlobsWithVersions(true))

        when:
        def blobs = cc.listBlobsByHierarchy("", options, null).iterator()

        then:
        def b = blobs.next()
        !blobs.hasNext()
        b.getName() == blobName
        b.hasVersionsOnly()
    }

    @Unroll
    def "List blobs hier options fail"() {
        when:
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveSnapshots(snapshots))
            .setMaxResultsPerPage(maxResults)
        cc.listBlobsByHierarchy(null, options, null).iterator().hasNext()

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
        cc.listBlobsByHierarchy(null).stream().collect(Collectors.toList())
            .forEach { blobItem ->
                if (blobItem.isPrefix()) {
                    foundPrefixes << blobItem.getName()
                } else {
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

        def blobs = cc.listBlobsByHierarchy("/", new ListBlobsOptions().setMaxResultsPerPage(PAGE_SIZE), null)

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

    /*
    This test requires two accounts that are configured in a very specific way. It is not feasible to setup that
    relationship programmatically, so we have recorded a successful interaction and only test recordings.
    */
    @PlaybackOnly
    def "List blobs hier ORS"() {
        setup:
        def sourceContainer = primaryBlobServiceClient.getBlobContainerClient("test1")
        def destContainer = alternateBlobServiceClient.getBlobContainerClient("test2")

        when:
        def sourceBlobs = sourceContainer.listBlobsByHierarchy("/").stream().collect(Collectors.toList())
        def destBlobs = destContainer.listBlobsByHierarchy("/").stream().collect(Collectors.toList())

        then:
        int i = 0
        for (def blob : sourceBlobs) {
            if (i == 1) {
                assert blob.getObjectReplicationSourcePolicies() == null
            } else {
                assert validateOR(blob.getObjectReplicationSourcePolicies(), "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80", "105f9aad-f39b-4064-8e47-ccd7937295ca")
            }
            i++
        }

        /* Service specifies no ors metadata on the dest blobs. */
        for (def blob : destBlobs) {
            assert blob.getObjectReplicationSourcePolicies() == null
        }
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
        cc.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(PAGE_SIZE), null).stream().count() == NUM_BLOBS
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    def "List blobs hier rehydrate priority"() {
        setup:
        def name = generateBlobName()
        def bc = cc.getBlobClient(name).getBlockBlobClient()
        bc.upload(data.defaultInputStream, 7)

        if (rehydratePriority != null) {
            bc.setAccessTier(AccessTier.ARCHIVE)

            bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT).setPriority(rehydratePriority), null, null)
        }

        when:
        def item = cc.listBlobsByHierarchy(null).iterator().next()

        then:
        item.getProperties().getRehydratePriority() == rehydratePriority

        where:
        rehydratePriority          || _
        null                       || _
        RehydratePriority.STANDARD || _
        RehydratePriority.HIGH     || _
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "List append blobs hier"() {
        setup:
        def name = generateBlobName()
        def bu = cc.getBlobClient(name).getAppendBlobClient()
        bu.create()
        bu.seal()

        when:
        def blobs = cc.listBlobsByHierarchy(null, new ListBlobsOptions().setPrefix(namer.getResourcePrefix()), null).iterator()

        then:
        def blob = blobs.next()
        !blobs.hasNext()
        blob.getName() == name
        blob.getProperties().getBlobType() == BlobType.APPEND_BLOB
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
        blob.getProperties().getContentMd5() == null
        blob.getProperties().getContentEncoding() == null
        blob.getProperties().getContentDisposition() == null
        blob.getProperties().getContentLanguage() == null
        blob.getProperties().getCacheControl() == null
        blob.getProperties().getBlobSequenceNumber() == null
        blob.getProperties().isServerEncrypted()
        blob.getProperties().isAccessTierInferred() == null
        blob.getProperties().getAccessTier() == null
        blob.getProperties().getArchiveStatus() == null
        blob.getProperties().getCreationTime() != null
        blob.getProperties().isSealed()
    }

    def "List blobs hier error"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())

        when:
        cc.listBlobsByHierarchy(".").iterator().hasNext()

        then:
        thrown(BlobStorageException)
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
        bu4.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null, null, null, null, null)
            .getStatusCode() == 201

        when:
        def blobs = cc.listBlobs().iterator()

        then:
        blobs.next().getName() == name
        blobs.next().getName() == name + "2"
        blobs.next().getName() == name + "3"

        where:
        name                  | _
        "中文"                | _
        "az[]"                | _
        "hello world"         | _
        "hello/world"         | _
        "hello&world"         | _
        "!*'();:@&=+\$,/?#[]" | _
    }

    @Unroll
    def "Create URL special chars encoded"() {
        // This test checks that we handle blob names with encoded special characters correctly.
        setup:
        def bu2 = cc.getBlobClient(name).getAppendBlobClient()
        def bu3 = cc.getBlobClient(name + "2").getPageBlobClient()
        def bu4 = cc.getBlobClient(name + "3").getBlockBlobClient()
        def bu5 = cc.getBlobClient(name).getBlockBlobClient()

        expect:
        bu2.createWithResponse(null, null, null, null, null).getStatusCode() == 201
        bu5.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
        bu3.createWithResponse(512, null, null, null, null, null, null).getStatusCode() == 201
        bu4.uploadWithResponse(data.defaultInputStream, data.defaultDataSize, null, null, null, null, null, null, null)
            .getStatusCode() == 201

        when:
        def blobs = cc.listBlobs().iterator()

        then:
        blobs.next().getName() == Utility.urlDecode(name)
        blobs.next().getName() == Utility.urlDecode(name) + "2"
        blobs.next().getName() == Utility.urlDecode(name) + "3"

        where:
        name                                                     | _
        "%E4%B8%AD%E6%96%87"                                     | _
        "az%5B%5D"                                               | _
        "hello%20world"                                          | _
        "hello%2Fworld"                                          | _
        "hello%26world"                                          | _
        "%21%2A%27%28%29%3B%3A%40%26%3D%2B%24%2C%2F%3F%23%5B%5D" | _
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

        AppendBlobClient bc = instrument(new BlobClientBuilder()
            .credential(env.primaryAccount.credential)
            .endpoint(env.primaryAccount.blobEndpoint)
            .blobName("rootblob"))
            .buildClient().getAppendBlobClient()

        when:
        Response<AppendBlobItem> createResponse = bc.createWithResponse(null, null, null, null, null)

        Response<BlobProperties> propsResponse = bc.getPropertiesWithResponse(null, null, null)

        then:
        createResponse.getStatusCode() == 201
        propsResponse.getStatusCode() == 200
        propsResponse.getValue().getBlobType() == BlobType.APPEND_BLOB
    }

    def "ContainerClientBuilder root implicit"() {
        setup:
        cc = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.ROOT_CONTAINER_NAME)
        // Create root container if not exist.
        if (!cc.exists()) {
            cc.create()
        }

        when:
        cc = instrument(new BlobContainerClientBuilder()
            .credential(env.primaryAccount.credential)
            .endpoint(env.primaryAccount.blobEndpoint)
            .containerName(null))
            .buildClient()

        then:
        cc.getProperties() != null
        cc.getBlobContainerName() == BlobContainerAsyncClient.ROOT_CONTAINER_NAME

        when:
        def bc = cc.getBlobClient("rootblob").getAppendBlobClient()
        bc.create(true)

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
        catch (BlobStorageException se) {
            if (se.getErrorCode() != BlobErrorCode.CONTAINER_ALREADY_EXISTS) {
                throw se
            }
        }
        def webContainer = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.STATIC_WEBSITE_CONTAINER_NAME)

        when:
        // Validate some basic operation.
        webContainer.setAccessPolicy(null, null)

        then:
        notThrown(BlobStorageException)
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

    def "Get Container Name"() {
        given:
        def containerName = generateContainerName()
        def newcc = primaryBlobServiceClient.getBlobContainerClient(containerName)
        expect:
        containerName == newcc.getBlobContainerName()
    }

    def "Builder cpk validation"() {
        setup:
        String endpoint = BlobUrlParts.parse(cc.getBlobContainerUrl()).setScheme("http").toUrl()
        def builder = new BlobContainerClientBuilder()
            .customerProvidedKey(new CustomerProvidedKey(Base64.getEncoder().encodeToString(getRandomByteArray(256))))
            .endpoint(endpoint)

        when:
        builder.buildClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "Builder bearer token validation"() {
        setup:
        String endpoint = BlobUrlParts.parse(cc.getBlobContainerUrl()).setScheme("http").toUrl()
        def builder = new BlobContainerClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)

        when:
        builder.buildClient()

        then:
        thrown(IllegalArgumentException)
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        setup:
        def cc = getContainerClientBuilder(cc.getBlobContainerUrl())
            .credential(env.primaryAccount.credential)
            .addPolicy(getPerCallVersionPolicy())
            .buildClient()

        when:
        def response = cc.getPropertiesWithResponse(null, null, null)

        then:
        notThrown(BlobStorageException)
        response.getHeaders().getValue("x-ms-version") == "2017-11-09"
    }

//    def "Rename"() {
//        setup:
//        def newName = generateContainerName()
//
//        when:
//        def renamedContainer = cc.rename(newName)
//
//        then:
//        renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
//
//        cleanup:
//        renamedContainer.delete()
//    }
//
//    def "Rename sas"() {
//        setup:
//        def newName = generateContainerName()
//        def sas = primaryBlobServiceClient.generateAccountSas(new AccountSasSignatureValues(namer.getUtcNow().plusHours(1), AccountSasPermission.parse("rwdxlacuptf"), AccountSasService.parse("b"), AccountSasResourceType.parse("c")))
//        def sasClient = getContainerClient(sas, cc.getBlobContainerUrl())
//
//        when:
//        def renamedContainer = sasClient.rename(newName)
//
//        then:
//        renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
//
//        cleanup:
//        renamedContainer.delete()
//    }
//
//    @Unroll
//    def "Rename AC"() {
//        setup:
//        leaseID = setupContainerLeaseCondition(cc, leaseID)
//        def cac = new BlobRequestConditions()
//            .setLeaseId(leaseID)
//
//        expect:
//        cc.renameWithResponse(new BlobContainerRenameOptions(generateContainerName()).setRequestConditions(cac),
//            null, null).getStatusCode() == 200
//
//        where:
//        leaseID         || _
//        null            || _
//        receivedLeaseID || _
//    }
//
//    @Unroll
//    def "Rename AC fail"() {
//        setup:
//        def cac = new BlobRequestConditions()
//            .setLeaseId(leaseID)
//
//        when:
//        cc.renameWithResponse(new BlobContainerRenameOptions(generateContainerName()).setRequestConditions(cac),
//            null, null)
//
//        then:
//        thrown(BlobStorageException)
//
//        where:
//        leaseID         || _
//        garbageLeaseID  || _
//    }
//
//    @Unroll
//    def "Rename AC illegal"() {
//        setup:
//        def ac = new BlobRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch).setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified).setTagsConditions(tags)
//
//        when:
//        cc.renameWithResponse(new BlobContainerRenameOptions(generateContainerName()).setRequestConditions(ac),
//            null, null)
//
//        then:
//        thrown(UnsupportedOperationException)
//
//        where:
//        modified | unmodified | match        | noneMatch    | tags
//        oldDate  | null       | null         | null         | null
//        null     | newDate    | null         | null         | null
//        null     | null       | receivedEtag | null         | null
//        null     | null       | null         | garbageEtag  | null
//        null     | null       | null         | null         | "tags"
//    }
//
//    def "Rename error"() {
//        setup:
//        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())
//        def newName = generateContainerName()
//
//        when:
//        cc.rename(newName)
//
//        then:
//        thrown(BlobStorageException)
//    }
}
