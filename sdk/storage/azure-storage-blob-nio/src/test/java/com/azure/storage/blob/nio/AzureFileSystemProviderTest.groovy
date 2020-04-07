// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.specialized.AppendBlobClient
import com.azure.storage.blob.specialized.BlockBlobClient
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileSystem
import java.nio.file.FileSystemAlreadyExistsException
import java.nio.file.FileSystemNotFoundException
import java.nio.file.NoSuchFileException
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileAttribute
import java.security.MessageDigest

class AzureFileSystemProviderTest extends APISpec {
    def config = new HashMap<String, Object>()
    AzureFileSystemProvider provider

    // The following are are common among a large number of copy tests
    AzurePath sourcePath
    AzurePath destPath
    BlobClient sourceClient
    BlobClient destinationClient

    def setup() {
        config = initializeConfigMap()
        provider = new AzureFileSystemProvider()
    }

    def "CreateFileSystem"() {
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
    def "CreateFileSystem invalid uri"() {
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

    def "CreateFileSystem duplicate"() {
        setup:
        config[AzureFileSystem.AZURE_STORAGE_FILE_STORES] = generateContainerName()
        config[AzureFileSystem.AZURE_STORAGE_ACCOUNT_KEY] = getAccountKey(PRIMARY_STORAGE)
        provider.newFileSystem(getAccountUri(), config)

        when:
        provider.newFileSystem(getAccountUri(), config)

        then:
        thrown(FileSystemAlreadyExistsException)
    }

    def "CreateFileSystem initial check fail"() {
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

    def "GetFileSystem not found"() {
        when:
        provider.getFileSystem(getAccountUri())

        then:
        thrown(FileSystemNotFoundException)
    }

    @Unroll
    def "GetFileSystem IA"() {
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

    // TODO: Be sure to test operating on containers that already have data
    // all apis should have a test that tries them after the FileSystem is closed to ensure they throw.

    def "GetScheme"() {
        expect:
        provider.getScheme() == "azb"
    }

    @Unroll
    def "CreateDir parent exists"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = getNonDefaultRootDir(fs)
        def parent = getPathWithDepth(depth)
        def dirName = generateBlobName()
        def dirPathStr = parent + dirName

        def dirPath = fs.getPath(rootName, dirPathStr)

        // Generate clients to resources. Create resources as necessary
        def containerClient = rootNameToContainerClient(rootName)
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
        checkBlobIsDir(dirClient)

        where:
        depth | _
        0     | _ // Test putting a blob in the root dir.
        1     | _
        2     | _
    }

    def "CreateDir relativePath"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def containerClient = rootNameToContainerClient(getDefaultDir(fs))
        def blobClient = containerClient.getBlobClient(fileName)

        when: "Relative paths are resolved against the default directory"
        fs.provider().createDirectory(fs.getPath(fileName))

        then:
        checkBlobIsDir(blobClient)
    }

    def "CreateDir file already exists"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def containerClient = rootNameToContainerClient(getDefaultDir(fs))
        BlockBlobClient blobClient = containerClient.getBlobClient(fileName).getBlockBlobClient()

        when:
        blobClient.commitBlockList(Collections.emptyList(), false)
        fs.provider().createDirectory(fs.getPath(fileName)) // Will go to default directory

        then:
        thrown(FileAlreadyExistsException)
    }

    def "CreateDir concrete dir already exists"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def containerClient = rootNameToContainerClient(getDefaultDir(fs))
        def blobClient = containerClient.getBlobClient(fileName).getBlockBlobClient()

        when:
        putDirectoryBlob(blobClient)
        fs.provider().createDirectory(fs.getPath(fileName))

        then:
        thrown(FileAlreadyExistsException)
    }

    def "CreateDir virtual dir already exists"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def childName = generateBlobName()
        def containerClient = rootNameToContainerClient(getDefaultDir(fs))
        def blobClient = containerClient.getBlobClient(fileName)

        when:
        AppendBlobClient blobClient2 = containerClient.getBlobClient(fileName + fs.getSeparator() + childName)
            .getAppendBlobClient()
        blobClient2.create()
        fs.provider().createDirectory(fs.getPath(fileName))

        then:
        notThrown(FileAlreadyExistsException)
        blobClient.exists() // We will turn the directory from virtual to concrete
        checkBlobIsDir(blobClient)
    }

    def "CreateDir root"() {
        setup:
        def fs = createFS(config)

        when: "Trying to create the root"
        fs.provider().createDirectory(fs.getDefaultDirectory())

        then:
        thrown(IllegalArgumentException)
    }

    def "CreateDir no parent"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def childName = generateBlobName()

        when: "Parent doesn't exist"
        fs.provider().createDirectory(fs.getPath(fileName + fs.getSeparator() + childName))

        then:
        thrown(IOException)
    }

    def "CreateDir invalid root"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()

        when:
        fs.provider().createDirectory(fs.getPath("fakeRoot:" + fs.getSeparator() + fileName))

        then:
        thrown(IOException)
    }

    def "CreateDir attributes"() {
        setup:
        def fs = createFS(config)
        def fileName = generateBlobName()
        def containerClient = rootNameToContainerClient(getDefaultDir(fs))
        AppendBlobClient blobClient = containerClient.getBlobClient(fileName).getAppendBlobClient()
        def contentMd5 = MessageDigest.getInstance("MD5").digest(new byte[0])
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
    def "Copy source"() {
        setup:
        def fs = createFS(config)
        basicSetupForCopyTest(fs)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def sourceChildClient = null
        def destChildClient = null

        // Create resources as necessary
        if (sourceIsDir) {
            if (!sourceIsVirtual) {
                fs.provider().createDirectory(sourcePath)
            }
            if (!sourceEmpty) {
                def sourceChildName = generateBlobName()
                sourceChildClient = ((AzurePath) sourcePath.resolve(sourceChildName)).toBlobClient()
                    .getAppendBlobClient()
                sourceChildClient.create()
                destChildClient = ((AzurePath) destPath.resolve(sourceChildName)).toBlobClient()
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
            assert new AzureResource(sourcePath).checkDirectoryExists()
        }

        // If the source was a file, check that the destination data matches the source.
        if (!sourceIsDir) {
            def outStream = new ByteArrayOutputStream()
            destinationClient.download(outStream)
            assert ByteBuffer.wrap(outStream.toByteArray()) == defaultData
        } else {
            // Check that the destination directory is concrete.
            assert destinationClient.exists()
            assert checkBlobIsDir(destinationClient)
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
    def "Copy destination"() {
        setup:
        def fs = createFS(config)
        basicSetupForCopyTest(fs)

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
    def "Copy non empty dest"() {
        setup:
        def fs = createFS(config)
        basicSetupForCopyTest(fs)

        def destChildClient

        // Create resources as necessary
        sourceClient.upload(new ByteArrayInputStream(getRandomByteArray(20)), 20)
        if (!destinationIsVirtual) {
            fs.provider().createDirectory(destPath)
        }
        destChildClient = ((AzurePath) destPath.resolve(generateBlobName())).toBlobClient()
        destChildClient.upload(defaultInputStream.get(), defaultDataSize)

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES,
            StandardCopyOption.REPLACE_EXISTING) // Ensure that even when trying to replace_existing, we still fail.

        then:
        thrown(DirectoryNotEmptyException)
        new AzureResource(destPath).checkDirectoryExists()

        where:
        destinationIsVirtual | _
        true                 | _
        false                | _
    }

    @Unroll
    def "Copy replace existing fail"() {
        // The success case is tested by the "copy destination" test.
        // Testing replacing a virtual directory is in the "non empty dest" test as there can be no empty virtual dir.
        setup:
        def fs = createFS(config)
        basicSetupForCopyTest(fs)

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
            assert new AzureResource(destPath).checkDirectoryExists()
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

    def "Copy options fail"() {
        setup:
        def fs = createFS(config)
        basicSetupForCopyTest(fs)

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
    def "Copy depth"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = getNonDefaultRootDir(fs)
        def sourcePath = (AzurePath) fs.getPath(rootName, getPathWithDepth(sourceDepth), generateBlobName())

        def destParent = getPathWithDepth(destDepth)
        def destPath = (AzurePath) fs.getPath(rootName, destParent, generateBlobName())

        // Generate clients to resources.
        def sourceClient = sourcePath.toBlobClient()
        def destinationClient = destPath.toBlobClient()
        def destParentClient = ((AzurePath) destPath.getParent()).toBlobClient()

        // Create resources as necessary
        sourceClient.upload(defaultInputStream.get(), defaultDataSize)
        putDirectoryBlob(destParentClient.getBlockBlobClient())

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

    def "Copy no parent for dest"() {
        setup:
        def fs = createFS(config)
        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = getNonDefaultRootDir(fs)
        def sourcePath = (AzurePath) fs.getPath(rootName, generateBlobName())
        def destPath = (AzurePath) fs.getPath(rootName, generateBlobName(), generateBlobName())

        // Generate clients to resources.
        def sourceClient = sourcePath.toBlobClient()
        def destinationClient = destPath.toBlobClient()

        // Create resources as necessary
        sourceClient.upload(new ByteArrayInputStream(getRandomByteArray(20)), 20)

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES)

        then:
        thrown(IOException)
        !destinationClient.exists()
    }

    def "Copy source does not exist"() {
        setup:
        def fs = createFS(config)
        basicSetupForCopyTest(fs)

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES)

        then:
        thrown(IOException)
    }

    def "Copy no root dir"() {
        setup:
        def fs = createFS(config)
        basicSetupForCopyTest(fs)

        when: "Source root"
        fs.provider().copy(sourcePath.getRoot(), destPath, StandardCopyOption.COPY_ATTRIBUTES)

        then:
        thrown(IllegalArgumentException)

        when: "Dest root"
        fs.provider().copy(sourcePath, destPath.getRoot(), StandardCopyOption.COPY_ATTRIBUTES)

        then:
        thrown(IllegalArgumentException)
    }

    def "Copy same file no op"() {
        setup:
        def fs = createFS(config)
        basicSetupForCopyTest(fs)

        when:
        // Even when the source does not exist or COPY_ATTRIBUTES is not specified, this will succeed as no-op
        fs.provider().copy(sourcePath, sourcePath)

        then:
        notThrown(Exception)
    }

    def "Copy across containers"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        def sourceRootName = getNonDefaultRootDir(fs)
        def destRootName = getDefaultDir(fs)
        def sourcePath = (AzurePath) fs.getPath(sourceRootName, generateBlobName())
        def destPath = (AzurePath) fs.getPath(destRootName, generateBlobName())

        // Generate clients to resources.
        def sourceClient = sourcePath.toBlobClient()
        def destinationClient = destPath.toBlobClient()

        // Create resources as necessary
        sourceClient.upload(defaultInputStream.get(), defaultDataSize)

        when:
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES)

        then:
        sourceClient.exists()
        destinationClient.exists()
    }

    @Unroll
    def "Delete"() {
        setup:
        def fs = createFS(config)

        def path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName()))
        def blobClient = path.toBlobClient().getBlockBlobClient()

        if (isDir) {
            putDirectoryBlob(blobClient)
        } else {
            blobClient.upload(defaultInputStream.get(), defaultDataSize)
        }

        when:
        fs.provider().delete(path)

        then:
        !blobClient.exists()

        where:
        isDir | _
        true  | _
        false | _
    }

    @Unroll
    def "Delete nonempty dir"() {
        setup:
        def fs = createFS(config)

        def path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName()))
        def blobClient = path.toBlobClient().getBlockBlobClient()
        def childClient = ((AzurePath) path.resolve(generateBlobName())).toBlobClient()

        childClient.upload(defaultInputStream.get(), defaultDataSize)
        if (!virtual) {
            putDirectoryBlob(blobClient)
        }

        when:
        fs.provider().delete(path)

        then:
        thrown(DirectoryNotEmptyException)
        new AzureResource(path).checkDirectoryExists()

        where:
        virtual | _
        false   | _
        true    | _
    }

    def "Delete no target"() {
        setup:
        def fs = createFS(config)
        def path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName()))

        when:
        fs.provider().delete(path)

        then:
        thrown(NoSuchFileException)
    }

    def "Delete default dir"() {
        setup:
        def fs = createFS(config)
        def path = ((AzurePath) fs.getPath(generateBlobName()))
        def client = path.toBlobClient()

        client.upload(defaultInputStream.get(), defaultDataSize)

        when:
        fs.provider().delete(path)

        then:
        !client.exists()
    }

    def "DirectoryStream"() {
        setup:
        def fs = createFS(config)
        def resource = new AzureResource(fs.getPath("a" + generateBlobName()))
        resource.getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList())
        resource = new AzureResource(fs.getPath("b" + generateBlobName()))
        resource.getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList())

        when:
        def it = fs.provider().newDirectoryStream(fs.getPath(getDefaultDir(fs)),
            {path -> path.getFileName().toString().startsWith("a")}).iterator()

        then:
        it.hasNext()
        it.next().getFileName().toString().startsWith("a")
        !it.hasNext()
    }

    def "DirectoryStream invalid root"() {
        setup:
        def fs = createFS(config)

        when:
        fs.provider().newDirectoryStream(fs.getPath("fakeRoot:"), {path -> true})

        then:
        thrown(IOException)
    }

    def basicSetupForCopyTest(FileSystem fs) {
        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        def rootName = getNonDefaultRootDir(fs)
        sourcePath = (AzurePath) fs.getPath(rootName, generateBlobName())
        destPath = (AzurePath) fs.getPath(rootName, generateBlobName())

        // Generate clients to resources.
        sourceClient = sourcePath.toBlobClient()
        destinationClient = destPath.toBlobClient()
    }
}
