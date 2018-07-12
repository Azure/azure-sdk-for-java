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

import com.microsoft.azure.storage.blob.AppendBlobURL
import com.microsoft.azure.storage.blob.BlobURL
import com.microsoft.azure.storage.blob.BlobListingDetails
import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.ContainerAccessConditions
import com.microsoft.azure.storage.blob.HTTPAccessConditions
import com.microsoft.azure.storage.blob.LeaseAccessConditions
import com.microsoft.azure.storage.blob.ListBlobsOptions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.PageBlobURL
import com.microsoft.azure.storage.blob.PipelineOptions
import com.microsoft.azure.storage.blob.StorageException
import com.microsoft.azure.storage.blob.StorageURL
import com.microsoft.azure.storage.blob.models.AccessPolicy
import com.microsoft.azure.storage.blob.models.AppendBlobCreateResponse
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesResponse
import com.microsoft.azure.storage.blob.models.BlobItem
import com.microsoft.azure.storage.blob.models.BlobType
import com.microsoft.azure.storage.blob.models.ContainerAcquireLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainerBreakLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainerChangeLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainerCreateResponse
import com.microsoft.azure.storage.blob.models.ContainerDeleteResponse
import com.microsoft.azure.storage.blob.models.ContainerGetAccessPolicyResponse
import com.microsoft.azure.storage.blob.models.ContainerGetPropertiesHeaders
import com.microsoft.azure.storage.blob.models.ContainerGetPropertiesResponse
import com.microsoft.azure.storage.blob.models.ContainerListBlobFlatSegmentHeaders
import com.microsoft.azure.storage.blob.models.ContainerListBlobFlatSegmentResponse
import com.microsoft.azure.storage.blob.models.ContainerListBlobHierarchySegmentHeaders
import com.microsoft.azure.storage.blob.models.ContainerListBlobHierarchySegmentResponse
import com.microsoft.azure.storage.blob.models.ContainerReleaseLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainerRenewLeaseHeaders
import com.microsoft.azure.storage.blob.models.ContainerSetAccessPolicyResponse
import com.microsoft.azure.storage.blob.models.ContainerSetMetadataResponse
import com.microsoft.azure.storage.blob.models.CopyStatusType
import com.microsoft.azure.storage.blob.models.LeaseDurationType
import com.microsoft.azure.storage.blob.models.LeaseStateType
import com.microsoft.azure.storage.blob.models.LeaseStatusType
import com.microsoft.azure.storage.blob.models.PublicAccessType
import com.microsoft.azure.storage.blob.models.SignedIdentifier
import com.microsoft.azure.storage.blob.models.StorageErrorCode
import com.microsoft.rest.v2.http.HttpPipeline
import io.reactivex.Flowable
import spock.lang.*

import java.time.OffsetDateTime
import java.time.ZoneId


class ContainerAPITest extends APISpec {

    def "Container create all null"() {
        setup:
        // Overwrite the existing cu, which has already been created
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        ContainerCreateResponse response = cu.create(null, null).blockingGet()

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
        ContainerGetPropertiesResponse response = cu.getProperties(null).blockingGet()

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

    def "Container create exception"() {
        when:
        cu.create(null, null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.response().statusCode() == 409
        e.errorCode() == StorageErrorCode.CONTAINER_ALREADY_EXISTS
        e.message().contains("The specified container already exists.")
    }

    def "Container get properties null"() {
        when:
        ContainerGetPropertiesHeaders headers =
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

    def "Container get properties error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.getProperties(null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Container set metadata"() {
        setup:
        ContainerSetMetadataResponse response = cu.setMetadata(null, null).blockingGet()

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

    def "Container set metadata error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.setMetadata(null, null).blockingGet()

        then:
        thrown(StorageException)
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
                .withAccessPolicy(new AccessPolicy()
                .withStart(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .withExpiry(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                .plusDays(1))
                .withPermission("r"))
        List<SignedIdentifier> ids = new ArrayList<>()
        ids.push(identifier)

        when:
        ContainerSetAccessPolicyResponse response =
                cu.setAccessPolicy(null, ids, null).blockingGet()
        SignedIdentifier receivedIdentifier = cu.getAccessPolicy(null).blockingGet().body().get(0)

        then:
        response.statusCode() == 200
        validateBasicHeaders(response.headers())
        receivedIdentifier.accessPolicy().expiry() == identifier.accessPolicy().expiry()
        receivedIdentifier.accessPolicy().start() == identifier.accessPolicy().start()
        receivedIdentifier.accessPolicy().permission() == identifier.accessPolicy().permission()
    }

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

    def "Container set access policy error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.setAccessPolicy(null, null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Container get access policy"() {
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
        cu.setAccessPolicy(PublicAccessType.BLOB, ids, null).blockingGet()
        ContainerGetAccessPolicyResponse response = cu.getAccessPolicy(null).blockingGet()

        expect:
        response.statusCode() == 200
        response.headers().blobPublicAccess() == PublicAccessType.BLOB
        validateBasicHeaders(response.headers())
        response.body().get(0).accessPolicy().expiry() == identifier.accessPolicy().expiry()
        response.body().get(0).accessPolicy().start() == identifier.accessPolicy().start()
        response.body().get(0).accessPolicy().permission() == identifier.accessPolicy().permission()
    }

    def "Container get access policy lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        expect:
        cu.getAccessPolicy(new LeaseAccessConditions(leaseID)).blockingGet().statusCode() == 200
    }

    def "Container get access policy error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.getAccessPolicy(null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Container delete"() {
        when:
        ContainerDeleteResponse response = cu.delete(null).blockingGet()

        then:
        response.statusCode() == 202
        response.headers().requestId() != null
        response.headers().version() != null
        response.headers().date() != null
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

    def "Container delete error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.delete(null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Container list blobs flat"() {
        setup:
        String name = generateBlobName()
        PageBlobURL bu = cu.createPageBlobURL(name)
        bu.create(512, null, null, null, null).blockingGet()

        when:
        ContainerListBlobFlatSegmentResponse response = cu.listBlobsFlatSegment(null, null)
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
    }

    def setupListBlobsTest(String normalName, String copyName, String metadataName, String uncommittedName) {
        PageBlobURL normal = cu.createPageBlobURL(normalName)
        normal.create(512, null, null, null, null).blockingGet()

        PageBlobURL copyBlob = cu.createPageBlobURL(copyName)
        waitForCopy(copyBlob, copyBlob.startCopyFromURL(normal.toURL(),
                null, null, null).blockingGet())

        PageBlobURL metadataBlob = cu.createPageBlobURL(metadataName)
        Metadata values = new Metadata()
        values.put("foo", "bar")
        metadataBlob.create(512, null, null, values, null).blockingGet()

        String snapshotTime = normal.createSnapshot(null, null)
                .blockingGet().headers().snapshot()

        BlockBlobURL uncommittedBlob = cu.createBlockBlobURL(uncommittedName)

        uncommittedBlob.stageBlock("0000", Flowable.just(defaultData), defaultData.remaining(),
                null).blockingGet()

        return snapshotTime
    }

    def "List blobs flat options copy"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions(new BlobListingDetails(
                true, false, false, false, false),
                null, null)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, options).blockingGet().body().segment().blobItems()

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
        ListBlobsOptions options = new ListBlobsOptions(new BlobListingDetails(
                false, true, false, false, false),
                null, null)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, options).blockingGet().body().segment().blobItems()

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
        ListBlobsOptions options = new ListBlobsOptions(new BlobListingDetails(
                false, false, true, false, false),
                null, null)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        String snapshotTime = setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, options).blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == normalName
        blobs.get(0).snapshot() == snapshotTime
        blobs.get(1).name() == normalName
        blobs.size() == 4 // Normal, snapshot, copy, metadata
    }

    def "List blobs flat options uncommitted"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions(new BlobListingDetails(
                false, false, false, true, false),
                null, null)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, options).blockingGet().body().segment().blobItems()

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
        bu.create(null, null, null).blockingGet()
        bu.delete(null, null).blockingGet()

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, new ListBlobsOptions(new BlobListingDetails(
                false, false, false,false, true), null,
                null)).blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == name
        blobs.size() == 1

        disableSoftDelete() == null // Must produce a true value or test will fail.
    }

    def "List blobs flat options prefix"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions(null, "a", null)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, options).blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == normalName
        blobs.size() == 1 // Normal
    }

    def "List blobs flat options maxResults"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions(new BlobListingDetails(
                true, false, true, true, false),
                null, 2)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, options).blockingGet().body().segment().blobItems()

        then:
        blobs.size() == 2
    }

    def "Container list blobs flat marker"() {
        setup:
        for (int i = 0; i < 10; i++) {
            PageBlobURL bu = cu.createPageBlobURL(generateBlobName())
            bu.create(512, null, null, null, null).blockingGet()
        }

        ContainerListBlobFlatSegmentResponse response = cu.listBlobsFlatSegment(null,
                new ListBlobsOptions(null, null, 6))
                .blockingGet()
        String marker = response.body().nextMarker()
        int firstSegmentSize = response.body().segment().blobItems().size()
        response = cu.listBlobsFlatSegment(marker, null).blockingGet()

        expect:
        firstSegmentSize == 6
        response.body().nextMarker() == null
        response.body().segment().blobItems().size() == 4
    }

    def "Container list blobs flat error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.listBlobsFlatSegment(null,null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Container list blobs hierarchy"() {
        setup:
        String name = generateBlobName()
        PageBlobURL bu = cu.createPageBlobURL(name)
        bu.create(512, null, null, null, null).blockingGet()

        when:
        ContainerListBlobHierarchySegmentResponse response =
                cu.listBlobsHierarchySegment(null, "/", null)
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

    def "List blobs hier options copy"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions(new BlobListingDetails(
                true, false, false, false, false),
                null, null)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsHierarchySegment(null, "", options).blockingGet().body().segment().blobItems()

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
        ListBlobsOptions options = new ListBlobsOptions(new BlobListingDetails(
                false, true, false, false, false),
                null, null)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsHierarchySegment(null, "", options)
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
        ListBlobsOptions options = new ListBlobsOptions(new BlobListingDetails(
                false, false, false, true, false),
                null, null)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsHierarchySegment(null, "", options)
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
        bu.create(null, null, null).blockingGet()
        bu.delete(null, null).blockingGet()

        when:
        List<BlobItem> blobs = cu.listBlobsHierarchySegment(null, "",
                new ListBlobsOptions(new BlobListingDetails(false, false, false,
                        false, true), null,null)).blockingGet()
                .body().segment().blobItems()

        then:
        blobs.get(0).name() == name
        blobs.size() == 1

        disableSoftDelete() == null
    }

    def "List blobs hier options prefix"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions(null, "a", null)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsHierarchySegment(null, "", options)
                .blockingGet().body().segment().blobItems()

        then:
        blobs.get(0).name() == normalName
        blobs.size() == 1 // Normal
    }

    def "List blobs hier options maxResults"() {
        setup:
        ListBlobsOptions options = new ListBlobsOptions(new BlobListingDetails(
                true, false, false, true, false), null,
                1)
        String normalName = "a" + generateBlobName()
        String copyName = "c" + generateBlobName()
        String metadataName = "m" + generateBlobName()
        String uncommittedName = "u" + generateBlobName()
        setupListBlobsTest(normalName, copyName, metadataName, uncommittedName)

        when:
        List<BlobItem> blobs = cu.listBlobsHierarchySegment(null, "", options)
                .blockingGet().body().segment().blobItems()

        then:
        blobs.size() == 1
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
        ContainerListBlobHierarchySegmentResponse response =
                cu.listBlobsHierarchySegment(null, "/", null).blockingGet()

        then:
        response.body().segment().blobPrefixes().size() == 1
        response.body().segment().blobPrefixes().get(0).name() == "b/"
        response.body().segment().blobItems().size() == 1
        response.body().segment().blobItems().get(0).name() == "a"
    }

    def "Container list blobs hier marker"() {
        setup:
        for (int i = 0; i < 10; i++) {
            PageBlobURL bu = cu.createPageBlobURL(generateBlobName())
            bu.create(512, null, null, null, null).blockingGet()
        }

        ContainerListBlobHierarchySegmentResponse response = cu.listBlobsHierarchySegment(null, "/",
                new ListBlobsOptions(null, null, 6))
                .blockingGet()
        String marker = response.body().nextMarker()
        int firstSegmentSize = response.body().segment().blobItems().size()
        response = cu.listBlobsHierarchySegment(marker, "/", null).blockingGet()

        expect:
        firstSegmentSize == 6
        response.body().nextMarker() == null
        response.body().segment().blobItems().size() == 4
    }

    def "Container list blobs hier error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.listBlobsHierarchySegment(null, ".", null).blockingGet()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Container acquire lease"() {
        setup:
        ContainerAcquireLeaseHeaders headers =
                cu.acquireLease(UUID.randomUUID().toString(), leaseTime, null).blockingGet().headers()

        when:
        ContainerGetPropertiesHeaders properties = cu.getProperties(null).blockingGet()
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

    def "Container acquier lease error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.acquireLease(null, 50,null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Container renew lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        Thread.sleep(16000) // Wait for the lease to expire to ensure we are actually renewing it
        ContainerRenewLeaseHeaders headers = cu.renewLease(leaseID, null).blockingGet().headers()

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

    def "Container renew lease error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.renewLease("id", null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Container release lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)

        ContainerReleaseLeaseHeaders headers = cu.releaseLease(leaseID, null).blockingGet().headers()

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

    def "Container release lease error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.releaseLease("id", null).blockingGet()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Container break lease"() {
        setup:
        cu.acquireLease(UUID.randomUUID().toString(), leaseTime, null).blockingGet()

        ContainerBreakLeaseHeaders headers = cu.breakLease(breakPeriod, null).blockingGet().headers()
        LeaseStateType state = cu.getProperties(null).blockingGet().headers().leaseState()

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

    def "Container break lease error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.breakLease(null, null).blockingGet()

        then:
        thrown(StorageException)
    }

    def "Container change lease"() {
        setup:
        String leaseID = setupContainerLeaseCondition(cu, receivedLeaseID)
        ContainerChangeLeaseHeaders headers =
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

    def "Container change lease error"() {
        setup:
        cu = primaryServiceURL.createContainerURL(generateContainerName())

        when:
        cu.changeLease("id", "id",null).blockingGet()

        then:
        thrown(StorageException)
    }

    @Unroll
    def "Container create URL special chars"() {
        setup:
        AppendBlobURL bu2 = cu.createAppendBlobURL(name)
        PageBlobURL bu3 = cu.createPageBlobURL(name + "2")
        BlockBlobURL bu4 = cu.createBlockBlobURL(name + "3")
        BlobURL bu5 = cu.createBlockBlobURL(name)

        expect:
        bu2.create(null, null, null).blockingGet().statusCode() == 201
        bu5.getProperties(null).blockingGet().statusCode() == 200
        bu3.create(512, null, null, null, null).blockingGet()
                .statusCode() == 201
        bu4.upload(defaultFlowable, defaultDataSize,
                null, null, null).blockingGet().statusCode() == 201

        when:
        List<BlobItem> blobs = cu.listBlobsFlatSegment(null, null).blockingGet().body().segment().blobItems()

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

    def "Container root explicit"() {
        setup:
        cu = primaryServiceURL.createContainerURL("\$root")
        BlobURL bu = cu.createAppendBlobURL("rootblob")

        expect:
        bu.create(null, null, null).blockingGet().statusCode() == 201
    }

    def "Container root implicit"() {
        setup:
        PipelineOptions po = new PipelineOptions()
        po.client = getHttpClient()
        HttpPipeline pipeline = StorageURL.createPipeline(primaryCreds, po)
        AppendBlobURL bu = new AppendBlobURL(new URL("http://xclientdev3.blob.core.windows.net/rootblob"), pipeline)

        when:
        AppendBlobCreateResponse createResponse = bu.create(null, null, null)
                .blockingGet()
        BlobGetPropertiesResponse propsResponse = bu.getProperties(null).blockingGet()

        then:
        createResponse.statusCode() == 201
        propsResponse.statusCode() == 200
        propsResponse.headers().blobType() == BlobType.APPEND_BLOB
    }
}
