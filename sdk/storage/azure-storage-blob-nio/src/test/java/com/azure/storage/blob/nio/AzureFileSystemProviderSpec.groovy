// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import spock.lang.Unroll

import java.nio.file.FileSystemAlreadyExistsException
import java.nio.file.FileSystemNotFoundException

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
        // Root, one, two subdirs
        // for (numSubDirs) blobName = generateBlobName. Create Blob. Then after all, create dir.
        // Call a getBlob and check the metadata

        setup:
        def fs = createFS(config)

        // Generate resource names
        def rootName = fs.getRootDirectories().first().toString()
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


        // TODO: Test on a path that doesn't have a parent. On a path whose parent is just the root (This will test that
        // getting a blobClient to the default directory works and that listing based off it works.
        // Create a root. Create something in the root. Create something in a normal directory. Create something that doesn't have a parent.
        // On a root that is invalid. On a path that already exists. Create something that has no parent.

        where:
        depth | _
        0     | _ // Test putting a blob in the root dir.
        1     | _
        2     | _
    }

    def "FileSystemProvider dir exists"() {
        // No blob. Blob with prefix. Directory blob.
    }

    def "FileSystemProvider createDir file already exists"() {
        // Root. Another blob. Another directory.
    }

    def "FileSystemProvider createDir IOException"() {
        // Parent doesn't exist.
        // Invalid rood
    }

    def "FileSystemProvider createDir attributes"() {
        // null
    }
}
