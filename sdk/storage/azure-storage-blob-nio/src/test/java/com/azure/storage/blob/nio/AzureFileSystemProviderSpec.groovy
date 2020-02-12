// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import com.azure.storage.blob.specialized.AppendBlobClient
import spock.lang.Unroll

import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileSystemAlreadyExistsException
import java.nio.file.FileSystemNotFoundException
import java.nio.file.attribute.FileAttribute

class AzureFileSystemProviderSpec extends APISpec {
    def config = new HashMap<String, String>()
    AzureFileSystemProvider provider

    def setup() {
        config = initializeConfigMap()
        provider = new AzureFileSystemProvider()
    }

    def "FileSystemProvider createFileSystem"() {
        setup:
        config[AzureFileSystem.AZURE_STORAGE_ACCOUNT_KEY] = getAccountKey(PRIMARY_STORAGE)
        config[AzureFileSystem.AZURE_STORAGE_FILE_STORES] = generateContainerName()
        def uri = getAccountUri()

        when:
        provider.newFileSystem(uri, config)

        then:
        provider.getFileSystem(uri).isOpen()
        ((AzureFileSystem) provider.getFileSystem(uri)).getFileSystemName() == getAccountName(PRIMARY_STORAGE)
    }

    @Unroll
    def "FileSystemProvider createFileSystem invalid uri"() {
        when:
        provider.newFileSystem(uri, config)

        then:
        thrown(IllegalArgumentException)

        where:
        uri                        | _
        new URI("azc://path")      | _
        new URI("azb://path")      | _
        new URI("azb://?foo=bar")  | _
        new URI("azb://?account=") | _
    }

    def "FileSystemProvider createFileSystem duplicate"() {
        setup:
        config[AzureFileSystem.AZURE_STORAGE_FILE_STORES] = generateContainerName()
        config[AzureFileSystem.AZURE_STORAGE_ACCOUNT_KEY] = getAccountKey(PRIMARY_STORAGE)
        provider.newFileSystem(getAccountUri(), config)

        when:
        provider.newFileSystem(getAccountUri(), config)

        then:
        thrown(FileSystemAlreadyExistsException)
    }

    def "FileSystemProvider createFileSystem initial check fail"() {
        when:
        config[AzureFileSystem.AZURE_STORAGE_FILE_STORES] = generateContainerName()
        def badKey = getAccountKey(PRIMARY_STORAGE).getBytes()
        badKey[0]++
        config[AzureFileSystem.AZURE_STORAGE_ACCOUNT_KEY] = new String(badKey)
        provider.newFileSystem(getAccountUri(), config)

        then:
        thrown(IOException)

        when:
        provider.getFileSystem(getAccountUri())

        then:
        thrown(FileSystemNotFoundException)
    }

    def "FileSystemProvider getFileSystem not found"() {
        when:
        provider.getFileSystem(getAccountUri())

        then:
        thrown(FileSystemNotFoundException)
    }

    @Unroll
    def "FileSystemProvider getFileSystem IA"() {
        when:
        provider.getFileSystem(uri)

        then:
        thrown(IllegalArgumentException)

        where:
        uri                        | _
        new URI("azc://path")      | _
        new URI("azb://path")      | _
        new URI("azb://?foo=bar")  | _
        new URI("azb://?account=") | _
    }

    // TODO: Be sure to test directories
    // TODO: Be sure to test operating on containers that already have data

    // all apis should have a test that tries them after the FileSystem is closed to ensure they throw.

    def "FileSystemProvider getScheme"() {
        expect:
        provider.getScheme() == "azb"
    }

    @Unroll
    def "FileSystemProvider createDir parent exists"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = fs.getRootDirectories().last().toString()
        def containerName = rootName.substring(0, rootName.length() - 1)
        def parent = ""
        for (int i=0; i < depth; i++) {
            parent += generateBlobName() + AzureFileSystem.PATH_SEPARATOR
        }
        def dirName = generateBlobName()
        def dirPathStr = parent + dirName

        def dirPath = fs.getPath(rootName, dirPathStr)

        // Generate clients to resources. Create resources as necessary
        def containerClient = primaryBlobServiceClient
            .getBlobContainerClient(containerName)
        /*
        In this case, we are putting the blob in the root directory, i.e. directly in the container, so no need to
        create a blob.
         */
        if (parent != "") {
            def parentClient = containerClient.getBlobClient(parent)
            parentClient.getAppendBlobClient().create()
        }
        def dirClient = containerClient.getBlobClient(dirPathStr)

        when:
        fs.provider().createDirectory(dirPath)

        then:
        dirClient.getPropertiesWithResponse(null, null, null).getValue().getMetadata()
            .containsKey(AzureFileSystemProvider.DIR_METADATA_MARKER)

        where:
        depth | _
        0     | _ // Test putting a blob in the root dir.
        1     | _
        2     | _
    }

    def "FileSystemProvider createDir relativePath"() {
        setup:
        def fs = createFS(config)
        def containerClient =
            primaryBlobServiceClient.getBlobContainerClient(rootToContainer(fs.getDefaultDirectory().toString()))
        AppendBlobClient blobClient = containerClient.getBlobClient("foo").getAppendBlobClient()

        when: "Relative paths are resolved against the default directory"
        fs.provider().createDirectory(fs.getPath("foo"))

        then:
        blobClient.getProperties().getMetadata().containsKey(AzureFileSystemProvider.DIR_METADATA_MARKER)
    }

    def "FileSystemProvider createDir file already exists"() {
        setup:
        def fs = createFS(config)
        def containerClient =
            primaryBlobServiceClient.getBlobContainerClient(rootToContainer(fs.getDefaultDirectory().toString()))
        AppendBlobClient blobClient = containerClient.getBlobClient("foo").getAppendBlobClient()

        when: "A file already exists at the location"
        blobClient.create()
        fs.provider().createDirectory(fs.getPath("foo")) // Will go to default directory

        then:
        thrown(FileAlreadyExistsException)

        when: "A directory with marker already exists"
        blobClient.createWithResponse(null, [(AzureFileSystemProvider.DIR_METADATA_MARKER):"true"], null, null, null)
        fs.provider().createDirectory(fs.getPath("foo"))

        then:
        thrown(FileAlreadyExistsException)

        when: "A virtual directory without marker already exists--no failure"
        blobClient.delete()
        AppendBlobClient blobClient2 = containerClient.getBlobClient("foo/bar").getAppendBlobClient()
        blobClient2.create()
        fs.provider().createDirectory(fs.getPath("foo"))

        then:
        notThrown(FileAlreadyExistsException)
        blobClient.exists()
        blobClient.getProperties().getMetadata().containsKey(AzureFileSystemProvider.DIR_METADATA_MARKER)
    }

    def "FileSystemProvider createDir IOException"() {
        setup:
        def fs = createFS(config)

        when: "Trying to create the root"
        fs.provider().createDirectory(fs.getDefaultDirectory())

        then:
        thrown(IOException)

        when: "Parent doesn't exist"
        fs.provider().createDirectory(fs.getPath("foo/bar"))

        then:
        thrown(IOException)

        when: "Invalid root"
        fs.provider().createDirectory(fs.getPath("fakeRoot:/foo"))

        then:
        thrown(IOException)
    }

    def "FileSystemProvider createDir attributes"() {
        setup:
        def fs = createFS(config)
        def containerClient =
            primaryBlobServiceClient.getBlobContainerClient(rootToContainer(fs.getDefaultDirectory().toString()))
        AppendBlobClient blobClient = containerClient.getBlobClient("foo").getAppendBlobClient()
        def contentMd5 = getRandomByteArray(10)
        FileAttribute<?>[] attributes = [new TestFileAttribute<String>("fizz", "buzz"),
                                         new TestFileAttribute<String>("foo", "bar"),
                                         new TestFileAttribute<String>("Content-Type", "myType"),
                                         new TestFileAttribute<String>("Content-Disposition", "myDisposition"),
                                         new TestFileAttribute<String>("Content-Language", "myLanguage"),
                                         new TestFileAttribute<String>("Content-Encoding", "myEncoding"),
                                         new TestFileAttribute<String>("Cache-Control", "myControl"),
                                         new TestFileAttribute<byte[]>("Content-MD5", contentMd5)]

        when:
        fs.provider().createDirectory(fs.getPath("foo"), attributes)
        def props = blobClient.getProperties()

        then:
        props.getMetadata()["fizz"] == "buzz"
        props.getMetadata()["foo"] == "bar"
        !props.getMetadata().containsKey("Content-Type")
        !props.getMetadata().containsKey("Content-Disposition")
        !props.getMetadata().containsKey("Content-Language")
        !props.getMetadata().containsKey("Content-Encoding")
        !props.getMetadata().containsKey("Content-MD5")
        !props.getMetadata().containsKey("Cache-Control")
        props.getContentType() == "myType"
        props.getContentDisposition() == "myDisposition"
        props.getContentLanguage() == "myLanguage"
        props.getContentEncoding() == "myEncoding"
        props.getContentMd5() == contentMd5
        props.getCacheControl() == "myControl"
    }

    def "FileSystemProvider parent dir exists"() {
        setup:
        def fs = createFS(config)
        def containerClient =
            primaryBlobServiceClient.getBlobContainerClient(rootToContainer(fs.getDefaultDirectory().toString()))
        AppendBlobClient blobClient

        when: "If nothing present, no directory"
        blobClient = containerClient.getBlobClient("foo/bar").getAppendBlobClient()

        then:
        !((AzureFileSystemProvider) fs.provider()).checkParentDirectoryExists(containerClient, fs.getPath("foo"))

        when: "Virtual directories (a blob that has the dir path as a prefix) count as directory existence"
        blobClient.create()

        then:
        ((AzureFileSystemProvider) fs.provider()).checkParentDirectoryExists(containerClient, fs.getPath("foo"))

        when: "Marker blobs cont as directory existence"
        blobClient.delete()
        blobClient = containerClient.getBlobClient("foo").getAppendBlobClient()
        blobClient.createWithResponse(null, [(AzureFileSystemProvider.DIR_METADATA_MARKER):"true"], null, null, null)

        then:
        ((AzureFileSystemProvider) fs.provider()).checkParentDirectoryExists(containerClient, fs.getPath("foo"))

        expect: "Null directory means the path is targeting the root directory"
        ((AzureFileSystemProvider) fs.provider()).checkParentDirectoryExists(containerClient, null)
    }

    def rootToContainer(String root) {
        return root.substring(0, root.length() - 1)
    }
}
