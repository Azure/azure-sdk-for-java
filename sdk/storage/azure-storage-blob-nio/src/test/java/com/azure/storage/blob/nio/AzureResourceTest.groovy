/// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.specialized.AppendBlobClient
import spock.lang.Unroll

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.attribute.FileAttribute
import java.security.MessageDigest

class AzureResourceTest extends APISpec {
    def config = new HashMap<String, String>()
    AzureFileSystemProvider provider

    def setup() {
        config = initializeConfigMap()
        provider = new AzureFileSystemProvider()
    }

    def "Constructor"() {
        setup:
        def fs = createFS(config)

        when:
        def resource = new AzureResource(fs.getPath(getNonDefaultRootDir(fs), "foo/bar"))

        then:
        resource.getPath().toString() == getNonDefaultRootDir(fs) + "/foo/bar"
        resource.getBlobClient().getBlobUrl() == resource.getPath().toBlobClient().getBlobUrl()
    }

    def "No root"() {
        when:
        new AzureResource(createFS(config).getPath("root:"))

        then:
        thrown(IllegalArgumentException)
    }

    def "Instance type"() {
        when:
        new AzureResource(FileSystems.getDefault().getPath("foo"))

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "Directory status and exists"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // In root1, the resource will be in the root. In root2, the resource will be several levels deep. Also
        // root1 will be non-default directory and root2 is default directory.
        def container1 = rootNameToContainerName(getNonDefaultRootDir(fs))
        def parentPath1 = (AzurePath) fs.getPath(container1, generateBlobName())
        def parentPath2 = (AzurePath) fs.getPath(getPathWithDepth(3), generateBlobName())

        // Generate clients to resources.
        def blobClient1 = parentPath1.toBlobClient()
        def blobClient2 = parentPath2.toBlobClient()
        def childClient1 = ((AzurePath) parentPath1.resolve(generateBlobName())).toBlobClient()
        def childClient2 = ((AzurePath) parentPath2.resolve(generateBlobName())).toBlobClient()

        // Create resources as necessary
        if (status == DirectoryStatus.NOT_A_DIRECTORY) {
            blobClient1.upload(data.defaultInputStream, data.defaultDataSize)
            blobClient2.upload(data.defaultInputStream, data.defaultDataSize)
        } else if (status == DirectoryStatus.EMPTY) {
            putDirectoryBlob(blobClient1.getBlockBlobClient())
            putDirectoryBlob(blobClient2.getBlockBlobClient())
        } else if (status == DirectoryStatus.NOT_EMPTY) {
            if (!isVirtual) {
                putDirectoryBlob(blobClient1.getBlockBlobClient())
                putDirectoryBlob(blobClient2.getBlockBlobClient())
            }
            childClient1.upload(data.defaultInputStream, data.defaultDataSize)
            childClient2.upload(data.defaultInputStream, data.defaultDataSize)
        }

        expect:
        new AzureResource(parentPath1).checkDirStatus() == status
        new AzureResource(parentPath2).checkDirStatus() == status
        if (status == DirectoryStatus.EMPTY || status == DirectoryStatus.NOT_EMPTY) {
            assert new AzureResource(parentPath1).checkDirectoryExists()
            assert new AzureResource(parentPath2).checkDirectoryExists()
        } else {
            assert !new AzureResource(parentPath1).checkDirectoryExists()
            assert !new AzureResource(parentPath2).checkDirectoryExists()
        }

        where:
        status                          | isVirtual
        DirectoryStatus.DOES_NOT_EXIST  | false
        DirectoryStatus.NOT_A_DIRECTORY | false
        DirectoryStatus.EMPTY           | false
        DirectoryStatus.NOT_EMPTY       | true
        DirectoryStatus.NOT_EMPTY       | false
    }

    @Unroll
    def "Directory status files with same prefix"() {
        setup:
        def fs = createFS(config)
        // Create two files with same prefix. Both paths should have DirectoryStatus.NOT_A_DIRECTORY
        def pathName = generateBlobName()
        def path1 = fs.getPath("/foo/bar/" + pathName + ".txt")
        def path2 = fs.getPath("/foo/bar/" + pathName + ".txt.backup")
        Files.createFile(path1)
        Files.createFile(path2)

        expect:
        new AzureResource(path1).checkDirStatus() == DirectoryStatus.NOT_A_DIRECTORY
        new AzureResource(path2).checkDirStatus() == DirectoryStatus.NOT_A_DIRECTORY
    }

    def "Parent dir exists false"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()

        expect:
        !new AzureResource(fs.getPath(fileName, "bar")).checkParentDirectoryExists()
    }

    def "Parent dir exists virtual"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def childName = generateBlobName()
        def containerClient = rootNameToContainerClient(getDefaultDir(fs))

        when:
        AppendBlobClient blobClient = containerClient.getBlobClient(fileName + fs.getSeparator() + childName)
            .getAppendBlobClient()
        blobClient.create()

        then:
        new AzureResource(fs.getPath(fileName, childName)).checkParentDirectoryExists()
    }

    def "Parent dir exists concrete"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def containerClient = rootNameToContainerClient(getDefaultDir(fs))

        when:
        def blobClient = containerClient.getBlobClient(fileName).getBlockBlobClient()
        putDirectoryBlob(blobClient)

        then:
        new AzureResource(fs.getPath(fileName, "bar")).checkParentDirectoryExists()
    }

    def "Parent dir exists root"() {
        setup:
        def fs = createFS(config)

        expect:
        // No parent means the parent is implicitly the default root, which always exists
        new AzureResource(fs.getPath("foo")).checkParentDirectoryExists()

    }

    def "Parent dir exists non default root"() {
        // Checks for a bug where we would check the wrong root container for existence on a path with depth > 1
        setup:
        def fs = createFS(config)
        def rootName = getNonDefaultRootDir(fs)
        def containerClient = rootNameToContainerClient(rootName)

        when:
        def blobClient = containerClient.getBlobClient("fizz/buzz/bazz")
        blobClient.getAppendBlobClient().create()

        then:
        new AzureResource(fs.getPath(rootName, "fizz/buzz")).checkParentDirectoryExists()
    }

    @Unroll
    def "PutDirectoryBlob"() {
        setup:
        def fs = createFS(config)
        def resource = new AzureResource(fs.getPath(generateBlobName()))
        def contentMd5 = MessageDigest.getInstance("MD5").digest(new byte[0])
        ArrayList<FileAttribute<?>> attributes = null
        if (metadata) {
            attributes = new ArrayList<>()
            attributes.add(new TestFileAttribute<String>("fizz", "buzz"))
            attributes.add(new TestFileAttribute<String>("foo", "bar"))
        }
        if (properties) {
            attributes = attributes == null ? new ArrayList<FileAttribute<?>>() : attributes
            attributes.add(new TestFileAttribute<String>("Content-Type", "myType"))
            attributes.add(new TestFileAttribute<String>("Content-Disposition", "myDisposition"))
            attributes.add(new TestFileAttribute<String>("Content-Language", "myLanguage"))
            attributes.add(new TestFileAttribute<String>("Content-Encoding", "myEncoding"))
            attributes.add(new TestFileAttribute<String>("Cache-Control", "myControl"))
            attributes.add(new TestFileAttribute<byte[]>("Content-MD5", contentMd5))
        }

        when:
        if (metadata || properties) {
            resource.setFileAttributes(attributes)
        }
        resource.putDirectoryBlob(null)

        then:
        checkBlobIsDir(resource.getBlobClient())
        def props = resource.getBlobClient().getProperties()

        if (metadata) {
            assert props.getMetadata()["fizz"] == "buzz"
            assert props.getMetadata()["foo"] == "bar"
            assert !props.getMetadata().containsKey("Content-Type")
            assert !props.getMetadata().containsKey("Content-Disposition")
            assert !props.getMetadata().containsKey("Content-Language")
            assert !props.getMetadata().containsKey("Content-Encoding")
            assert !props.getMetadata().containsKey("Content-MD5")
            assert !props.getMetadata().containsKey("Cache-Control")
        }
        if (properties) {
            assert props.getContentType() == "myType"
            assert props.getContentDisposition() == "myDisposition"
            assert props.getContentLanguage() == "myLanguage"
            assert props.getContentEncoding() == "myEncoding"
            assert props.getContentMd5() == contentMd5
            assert props.getCacheControl() == "myControl"
        }

        where:
        metadata | properties
        false    | false
        true     | false
        false    | true
        true     | true
    }

    @Unroll
    def "PutDirectoryBlob AC"() {
        setup:
        def fs = createFS(config)
        def resource = new AzureResource(fs.getPath(generateBlobName()))
        resource.getBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        match = setupBlobMatchCondition(resource.getBlobClient(), match)
        def bac = new BlobRequestConditions()
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        resource.putDirectoryBlob(bac)

        then:
        checkBlobIsDir(resource.getBlobClient())

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "PutDirectoryBlob AC fail"() {
        setup:
        def fs = createFS(config)
        def resource = new AzureResource(fs.getPath(generateBlobName()))
        resource.getBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        noneMatch = setupBlobMatchCondition(resource.getBlobClient(), noneMatch)
        def bac = new BlobRequestConditions()
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        resource.putDirectoryBlob(bac)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }
}
