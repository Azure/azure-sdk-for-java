// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.specialized.AppendBlobClient
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileSystemAlreadyExistsException
import java.nio.file.FileSystemNotFoundException
import java.nio.file.StandardCopyOption
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
        def containerName = rootToContainer(rootName)
        def parent = ""
        for (int i = 0; i < depth; i++) {
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
        def fileName = generateBlobName()
        def containerClient =
            primaryBlobServiceClient.getBlobContainerClient(rootToContainer(fs.getDefaultDirectory().toString()))
        AppendBlobClient blobClient = containerClient.getBlobClient(fileName).getAppendBlobClient()

        when: "Relative paths are resolved against the default directory"
        fs.provider().createDirectory(fs.getPath(fileName))

        then:
        blobClient.getProperties().getMetadata().containsKey(AzureFileSystemProvider.DIR_METADATA_MARKER)
    }

    def "FileSystemProvider createDir file already exists"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def containerClient =
            primaryBlobServiceClient.getBlobContainerClient(rootToContainer(fs.getDefaultDirectory().toString()))
        AppendBlobClient blobClient = containerClient.getBlobClient(fileName).getAppendBlobClient()

        when:
        blobClient.create()
        fs.provider().createDirectory(fs.getPath(fileName)) // Will go to default directory

        then:
        thrown(FileAlreadyExistsException)
    }

    def "FileSystemProvider createDir concrete dir already exists"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def containerClient =
            primaryBlobServiceClient.getBlobContainerClient(rootToContainer(fs.getDefaultDirectory().toString()))
        AppendBlobClient blobClient = containerClient.getBlobClient(fileName).getAppendBlobClient()

        when:
        blobClient.createWithResponse(null, [(AzureFileSystemProvider.DIR_METADATA_MARKER): "true"], null, null, null)
        fs.provider().createDirectory(fs.getPath(fileName))

        then:
        thrown(FileAlreadyExistsException)
    }

    def "FileSystemProvider createDir virtual dir already exists"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def childName = generateBlobName()
        def containerClient =
            primaryBlobServiceClient.getBlobContainerClient(rootToContainer(fs.getDefaultDirectory().toString()))
        AppendBlobClient blobClient = containerClient.getBlobClient(fileName).getAppendBlobClient()

        when:
        AppendBlobClient blobClient2 = containerClient.getBlobClient(fileName + fs.getSeparator() + childName)
            .getAppendBlobClient()
        blobClient2.create()
        fs.provider().createDirectory(fs.getPath(fileName))

        then:
        notThrown(FileAlreadyExistsException)
        blobClient.exists() // We will turn the directory from virtual to concrete
        blobClient.getProperties().getMetadata().containsKey(AzureFileSystemProvider.DIR_METADATA_MARKER)
    }

    def "FileSystemProvider createDir root"() {
        setup:
        def fs = createFS(config)

        when: "Trying to create the root"
        fs.provider().createDirectory(fs.getDefaultDirectory())

        then:
        thrown(IOException)
    }

    def "FileSystemProvider createDir no parent"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def childName = generateBlobName()

        when: "Parent doesn't exist"
        fs.provider().createDirectory(fs.getPath(fileName + fs.getSeparator() + childName))

        then:
        thrown(IOException)
    }

    def "FileSystemProvider createDir invalid root"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()

        when:
        fs.provider().createDirectory(fs.getPath("fakeRoot:" + fs.getSeparator() + fileName))

        then:
        thrown(IOException)
    }

    def "FileSystemProvider createDir attributes"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def containerClient =
            primaryBlobServiceClient.getBlobContainerClient(rootToContainer(fs.getDefaultDirectory().toString()))
        AppendBlobClient blobClient = containerClient.getBlobClient(fileName).getAppendBlobClient()
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
        fs.provider().createDirectory(fs.getPath(fileName), attributes)
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

    @Unroll
    def "FileSystemProvider copy source"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = fs.getRootDirectories().last().toString()
        def containerName = rootToContainer(rootName)
        def sourceName = generateBlobName()
        def sourcePath = fs.getPath(rootName, sourceName)
        def destName = generateBlobName()
        def destPath = fs.getPath(rootName, destName)

        // Generate clients to resources.
        def containerClient = primaryBlobServiceClient
            .getBlobContainerClient(containerName)
        def sourceClient = containerClient.getBlobClient(sourceName)
        def destinationClient = containerClient.getBlobClient(destName)
        def sourceChildClient = null
        def destChildClient = null

        // Create resources as necessary
        if (sourceIsDir) {
            if (!sourceIsVirtual) {
                fs.provider().createDirectory(sourcePath)
            }
            if (!sourceEmpty) {
                def sourceChildName = generateBlobName()
                def sourceChildPath = fs.getPath(rootName, sourceName, sourceChildName)
                sourceChildClient = ((AzurePath) sourceChildPath).toBlobClient().getAppendBlobClient()
                sourceChildClient.create()
                destChildClient = ((AzurePath) fs.getPath(rootName, destName, sourceChildName)).toBlobClient()
                    .getAppendBlobClient()
            }
        } else { // source is file
            sourceClient.upload(defaultInputStream.get(), defaultDataSize)
        }

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES)

        then:
        // Check the source still exists.
        if (!sourceIsVirtual) {
            assert sourceClient.exists()
        } else {
            assert ((AzureFileSystemProvider) fs.provider()).checkDirectoryExists(sourceClient)
        }

        // If the source was a file, check that the destination data matches the source.
        if (!sourceIsDir) {
            def outStream = new ByteArrayOutputStream()
            destinationClient.download(outStream)
            assert ByteBuffer.wrap(outStream.toByteArray()) == defaultData
        } else {
            // Check that the destination directory is concrete.
            assert destinationClient.exists()
            assert destinationClient.getProperties().getMetadata()
                .containsKey(AzureFileSystemProvider.DIR_METADATA_MARKER)
            if (!sourceEmpty) {
                // Check that source child still exists and was not copied to the destination.
                assert sourceChildClient.exists()
                assert !destChildClient.exists()
            }
        }

        where:
        sourceIsDir | sourceIsVirtual | sourceEmpty
        false       | false           | false
        true        | true            | false
        true        | false           | true
        true        | false           | false
        // Can't have an empty virtual dir
    }

    @Unroll
    def "FileSystemProvider copy destination"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = fs.getRootDirectories().last().toString()
        def containerName = rootToContainer(rootName)
        def sourceName = generateBlobName()
        def sourcePath = fs.getPath(rootName, sourceName)
        def destName = generateBlobName()
        def destPath = fs.getPath(rootName, destName)

        // Generate clients to resources.
        def containerClient = primaryBlobServiceClient
            .getBlobContainerClient(containerName)
        def sourceClient = containerClient.getBlobClient(sourceName)
        def destinationClient = containerClient.getBlobClient(destName)

        // Create resources as necessary
        sourceClient.upload(defaultInputStream.get(), defaultDataSize)
        if (destinationExists) {
            if (destinationIsDir) {
                fs.provider().createDirectory(destPath)
            } else { // source is file
                destinationClient.upload(new ByteArrayInputStream(getRandomByteArray(20)), 20)
            }
        }

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES,
            StandardCopyOption.REPLACE_EXISTING)

        then:
        sourceClient.exists()
        def outStream = new ByteArrayOutputStream()
        destinationClient.download(outStream)
        assert ByteBuffer.wrap(outStream.toByteArray()) == defaultData

        where:
        destinationExists | destinationIsDir
        false             | false
        true              | false
        true              | true
        // Can't have an empty virtual directory. Copying to a nonempty directory will fail.
    }

    @Unroll
    def "FileSystemProvider copy non empty dest"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = fs.getRootDirectories().last().toString()
        def containerName = rootToContainer(rootName)
        def sourceName = generateBlobName()
        def sourcePath = fs.getPath(rootName, sourceName)
        def destName = generateBlobName()
        def destPath = fs.getPath(rootName, destName)

        // Generate clients to resources.
        def containerClient = primaryBlobServiceClient
            .getBlobContainerClient(containerName)
        def sourceClient = containerClient.getBlobClient(sourceName)
        def destinationClient = containerClient.getBlobClient(destName)
        def destChildClient

        // Create resources as necessary
        sourceClient.upload(new ByteArrayInputStream(getRandomByteArray(20)), 20)
        if (!destinationIsVirtual) {
            fs.provider().createDirectory(destPath)
        }
        def childName = generateBlobName()
        destChildClient = ((AzurePath) fs.getPath(rootName, destName, childName)).toBlobClient()
        destChildClient.upload(defaultInputStream.get(), defaultDataSize)

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES,
            StandardCopyOption.REPLACE_EXISTING) // Ensure that even when trying to replace_existing, we still fail.

        then:
        thrown(DirectoryNotEmptyException)
        ((AzureFileSystemProvider) fs.provider()).checkDirectoryExists(destinationClient)

        where:
        destinationIsVirtual | _
        true                 | _
        false                | _
    }

    @Unroll
    def "FileSystemProvider copy replace existing fail"() {
        // The success case is tested by the "copy destination" test.
        // Testing replacing a virtual directory is in the "non empty dest" test as there can be no empty virtual dir.
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = fs.getRootDirectories().last().toString()
        def containerName = rootToContainer(rootName)
        def sourceName = generateBlobName()
        def sourcePath = fs.getPath(rootName, sourceName)
        def destName = generateBlobName()
        def destPath = fs.getPath(rootName, destName)

        // Generate clients to resources.
        def containerClient = primaryBlobServiceClient
            .getBlobContainerClient(containerName)
        def sourceClient = containerClient.getBlobClient(sourceName)
        def destinationClient = containerClient.getBlobClient(destName)

        // Create resources as necessary
        sourceClient.upload(new ByteArrayInputStream(getRandomByteArray(20)), 20)
        if (destinationIsDir) {
            fs.provider().createDirectory(destPath)
        } else {
            destinationClient.upload(defaultInputStream.get(), defaultDataSize)
        }

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES)

        then:
        thrown(FileAlreadyExistsException)
        if (destinationIsDir) {
            assert ((AzureFileSystemProvider) fs.provider()).checkDirectoryExists(destinationClient)
        } else {
            def outStream = new ByteArrayOutputStream()
            destinationClient.download(outStream)
            assert ByteBuffer.wrap(outStream.toByteArray()) == defaultData
        }

        where:
        destinationIsDir | _
        true             | _
        false            | _
        // No need to test virtual directories. If they exist, they aren't empty and can't be overwritten anyway.
        // See above.
    }

    def "FileSystemProvider copy options fail"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = fs.getRootDirectories().last().toString()
        def sourceName = generateBlobName()
        def sourcePath = fs.getPath(rootName, sourceName)
        def destName = generateBlobName()
        def destPath = fs.getPath(rootName, destName)

        when: "Missing COPY_ATTRIBUTES"
        fs.provider().copy(sourcePath, destPath)

        then:
        thrown(UnsupportedOperationException)

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.ATOMIC_MOVE)

        then:
        thrown(UnsupportedOperationException)
    }

    @Unroll
    def "FileSystemProvider copy depth"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = fs.getRootDirectories().last().toString()
        def containerName = rootToContainer(rootName)
        def sourceParent = ""
        for (int i = 0; i < sourceDepth; i++) {
            sourceParent += generateBlobName() + AzureFileSystem.PATH_SEPARATOR
        }
        def sourceFileName = generateBlobName()
        def sourceFilePathStr = sourceParent + sourceFileName
        def sourcePath = fs.getPath(rootName, sourceFilePathStr)

        def destParent = ""
        for (int i = 0; i < destDepth; i++) {
            destParent += generateBlobName() + AzureFileSystem.PATH_SEPARATOR
        }
        def destFileName = generateBlobName()
        def destFilePathStr = destParent + destFileName
        def destPath = fs.getPath(rootName, destFilePathStr)

        // Generate clients to resources.
        def containerClient = primaryBlobServiceClient
            .getBlobContainerClient(containerName)
        def sourceClient = containerClient.getBlobClient(sourceFilePathStr)
        def destinationClient = containerClient.getBlobClient(destFilePathStr)
        def destParentClient = containerClient.getBlobClient(destParent)

        // Create resources as necessary
        sourceClient.upload(defaultInputStream.get(), defaultDataSize)
        destParentClient.getBlockBlobClient().commitBlockListWithResponse(Collections.emptyList(), null,
            [(AzureFileSystemProvider.DIR_METADATA_MARKER): "true"], null, null, null, null)

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES)

        then:
        def outStream = new ByteArrayOutputStream()
        destinationClient.download(outStream)
        ByteBuffer.wrap(outStream.toByteArray()) == defaultData

        where:
        sourceDepth | destDepth
        1           | 1
        1           | 2
        1           | 3
        2           | 1
        2           | 2
        2           | 3
        3           | 1
        3           | 2
        3           | 3
    }

    def "FileSystemProvider copy no parent for dest"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = fs.getRootDirectories().last().toString()
        def containerName = rootToContainer(rootName)
        def sourceName = generateBlobName()
        def sourcePath = fs.getPath(rootName, sourceName)
        def destName = generateBlobName() + fs.getSeparator() + generateBlobName()
        def destPath = fs.getPath(rootName, destName)

        // Generate clients to resources.
        def containerClient = primaryBlobServiceClient
            .getBlobContainerClient(containerName)
        def sourceClient = containerClient.getBlobClient(sourceName)
        def destinationClient = containerClient.getBlobClient(destName)

        // Create resources as necessary
        sourceClient.upload(new ByteArrayInputStream(getRandomByteArray(20)), 20)

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES)

        then:
        thrown(IOException)
        !destinationClient.exists()
    }

    def "FileSystemProvider copy source does not exist"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = fs.getRootDirectories().last().toString()
        def sourceName = generateBlobName()
        def sourcePath = fs.getPath(rootName, sourceName)
        def destName = generateBlobName()
        def destPath = fs.getPath(rootName, destName)

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES)

        then:
        thrown(IOException)
    }

    def "FileSystemProvider copy no root dir"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = fs.getRootDirectories().last().toString()
        def sourceName = generateBlobName()
        def sourcePath = fs.getPath(rootName, sourceName)
        def destName = generateBlobName()
        def destPath = fs.getPath(rootName, destName)

        when: "Source root"
        fs.provider().copy(fs.getPath(rootName), destPath, StandardCopyOption.COPY_ATTRIBUTES)

        then:
        thrown(IllegalArgumentException)

        when: "Dest root"
        fs.provider().copy(sourcePath, fs.getPath(rootName), StandardCopyOption.COPY_ATTRIBUTES)

        then:
        thrown(IllegalArgumentException)
    }

    def "FileSystemProvider copy same file no op"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = fs.getRootDirectories().last().toString()
        def sourceName = generateBlobName()
        def sourcePath = fs.getPath(rootName, sourceName)

        when:
        // Even when the source does not exist or COPY_ATTRIBUTES is not specified, this will succeed as no-op
        fs.provider().copy(sourcePath, sourcePath)

        then:
        notThrown(Exception)
    }

    def "FileSystemProvider copy across containers"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        def sourceRootName = fs.getRootDirectories().last().toString()
        def destRootName = fs.getRootDirectories().first().toString()
        def sourceContainerName = rootToContainer(sourceRootName)
        def destContainerName = rootToContainer(destRootName)
        def sourceName = generateBlobName()
        def sourcePath = fs.getPath(sourceRootName, sourceName)
        def destName = generateBlobName()
        def destPath = fs.getPath(destRootName, destName)

        // Generate clients to resources.
        def sourceContainerClient = primaryBlobServiceClient.getBlobContainerClient(sourceContainerName)
        def sourceClient = sourceContainerClient.getBlobClient(sourceName)
        def destContainerClient = primaryBlobServiceClient.getBlobContainerClient(destContainerName)
        def destinationClient = destContainerClient.getBlobClient(destName)

        // Create resources as necessary
        sourceClient.upload(defaultInputStream.get(), defaultDataSize)

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES)

        then:
        sourceClient.exists()
        destinationClient.exists()
    }

    @Unroll
    def "FileSystemProvider directory status"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // In root1, the resource will be in the root. In root2, the resource will be several levels deep. Also
        // root1 will be non-default directory and root2 is default directory.
        def root1 = fs.getRootDirectories().last().toString()
        def root2 = fs.getRootDirectories().first().toString()
        def container1 = rootToContainer(root1)
        def container2 = rootToContainer(root2)
        def name1 = generateBlobName()
        def parent2 = ""
        for (int i = 0; i < 3; i++) {
            parent2 += generateBlobName() + AzureFileSystem.PATH_SEPARATOR
        }
        def name2 = generateBlobName()

        // Generate clients to resources.
        def containerClient1 = primaryBlobServiceClient.getBlobContainerClient(container1)
        def blobClient1 = containerClient1.getBlobClient(name1)
        def containerClient2 = primaryBlobServiceClient.getBlobContainerClient(container2)
        def blobClient2 = containerClient2.getBlobClient(parent2 + name2)
        def childClient1 = containerClient1.getBlobClient(
            blobClient1.getBlobName() + AzureFileSystem.PATH_SEPARATOR + generateBlobName())
        def childClient2 = containerClient2.getBlobClient(
            blobClient2.getBlobName() + AzureFileSystem.PATH_SEPARATOR + generateBlobName())

        // Create resources as necessary
        def dirMetadata = [(AzureFileSystemProvider.DIR_METADATA_MARKER):"true"]
        if (status == DirectoryStatus.NOT_A_DIRECTORY) {
            blobClient1.upload(defaultInputStream.get(), defaultDataSize)
            blobClient2.upload(defaultInputStream.get(), defaultDataSize)
        } else if (status == DirectoryStatus.EMPTY) {
            blobClient1.getBlockBlobClient().commitBlockListWithResponse(Collections.emptyList(), null,
                dirMetadata, null, null, null, null)
            blobClient2.getBlockBlobClient().commitBlockListWithResponse(Collections.emptyList(), null,
                dirMetadata, null, null, null, null)
        } else if (status == DirectoryStatus.NOT_EMPTY) {
            if (!isVirtual) {
                blobClient1.getBlockBlobClient().commitBlockListWithResponse(Collections.emptyList(), null,
                    dirMetadata, null, null, null, null)
                blobClient2.getBlockBlobClient().commitBlockListWithResponse(Collections.emptyList(), null,
                    dirMetadata, null, null, null, null)
            }
            childClient1.upload(defaultInputStream.get(), defaultDataSize)
            childClient2.upload(defaultInputStream.get(), defaultDataSize)
        }

        expect:
        ((AzureFileSystemProvider) fs.provider()).checkDirStatus(blobClient1) == status
        ((AzureFileSystemProvider) fs.provider()).checkDirStatus(blobClient2) == status
        if (status == DirectoryStatus.EMPTY || status == DirectoryStatus.NOT_EMPTY) {
            assert ((AzureFileSystemProvider) fs.provider()).checkDirectoryExists(blobClient1)
            assert ((AzureFileSystemProvider) fs.provider()).checkDirectoryExists(blobClient2)
        } else {
            assert !((AzureFileSystemProvider) fs.provider()).checkDirectoryExists(blobClient1)
            assert !((AzureFileSystemProvider) fs.provider()).checkDirectoryExists(blobClient2)
        }

        where:
        status                          | isVirtual
        DirectoryStatus.DOES_NOT_EXIST  | false
        DirectoryStatus.NOT_A_DIRECTORY | false
        DirectoryStatus.EMPTY           | false
        DirectoryStatus.NOT_EMPTY       | true
        DirectoryStatus.NOT_EMPTY       | false
    }


    def "FileSystemProvider parent dir exists false"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()

        expect:
        !((AzureFileSystemProvider) fs.provider()).checkParentDirectoryExists(fs.getPath(fileName, "bar"))
    }

    def "FileSystemProvider parent dir exists virtual"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def childName = generateBlobName()
        def containerClient =
            primaryBlobServiceClient.getBlobContainerClient(rootToContainer(fs.getDefaultDirectory().toString()))

        when:
        AppendBlobClient blobClient = containerClient.getBlobClient(fileName + fs.getSeparator() + childName)
            .getAppendBlobClient()
        blobClient.create()

        then:
        ((AzureFileSystemProvider) fs.provider()).checkParentDirectoryExists(fs.getPath(fileName, childName))
    }

    def "FileSystemProvider parent dir exists concrete"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def containerClient =
            primaryBlobServiceClient.getBlobContainerClient(rootToContainer(fs.getDefaultDirectory().toString()))

        when:
        AppendBlobClient blobClient = containerClient.getBlobClient(fileName).getAppendBlobClient()
        blobClient.createWithResponse(null, [(AzureFileSystemProvider.DIR_METADATA_MARKER): "true"], null, null, null)

        then:
        ((AzureFileSystemProvider) fs.provider()).checkParentDirectoryExists(fs.getPath(fileName, "bar"))
    }

    def "FileSystemProvider parent dir exists root"() {
        setup:
        def fs = createFS(config)

        expect:
        // No parent means the parent is implicitly the default root, which always exists
        ((AzureFileSystemProvider) fs.provider()).checkParentDirectoryExists(fs.getPath("foo"))

        when: "Non-default root"
        // Checks for a bug where we would check the wrong root container for existence on a path with depth > 1
        blobClient.delete()
        def rootName = fs.getRootDirectories().last().toString()
        containerClient = primaryBlobServiceClient.getBlobContainerClient(rootToContainer(rootName))

        blobClient = containerClient.getBlobClient("fizz/buzz/bazz")
        blobClient.getAppendBlobClient().create()

        then:
        ((AzureFileSystemProvider) fs.provider()).checkParentDirectoryExists(fs.getPath(rootName, "fizz/buzz"))
    }
}
