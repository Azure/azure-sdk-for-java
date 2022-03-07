// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import spock.lang.Unroll

import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes

/**
 * This test class is for testing static helper methods provided by the JDK. Customers often rely on these methods
 * rather than using the file system or provider methods directly, so if a customer reports a scenario leverages one of
 * these methods and we need to add support for it, we should capture that here.
 */
class CompositeTest extends APISpec {
    def config = new HashMap<String, Object>()

    def setup() {
        config = initializeConfigMap()
    }

    def "Files createDirs"() {
        setup:
        def fs = createFS(config)

        when:
        def dirs = fs.getPath('mydir1/mydir2/mydir3')
        Files.createDirectories(dirs)

        then:
        Files.isDirectory(fs.getPath('mydir1'))
        Files.isDirectory(fs.getPath('mydir1/mydir2'))
        Files.isDirectory(fs.getPath('mydir1/mydir2/mydir3'))
    }

    def "Files create"() {
        setup:
        def fs = createFS(config)

        when:
        def path = Files.createFile(fs.getPath(generateBlobName()))

        then:
        fs.provider().checkAccess(path)
    }

    def "Files copy"() {
        setup:
        def fs = createFS(config)
        def dest = fs.getPath("dest")
        def resultArr = new byte[data.defaultDataSize]

        when:
        Files.copy(data.defaultInputStream, dest)
        fs.provider().newInputStream(dest).read(resultArr)

        then:
        resultArr == data.defaultBytes

        when:
        def dest2 = fs.getPath("dest2")
        def outStream = fs.provider().newOutputStream(dest2)
        Files.copy(dest, outStream)
        outStream.close()
        resultArr = new byte[data.defaultDataSize]
        fs.provider().newInputStream(dest2).read(resultArr)

        then:
        resultArr == data.defaultBytes

        when:
        def dest3 = fs.getPath("dest3")
        Files.copy(dest, dest3, StandardCopyOption.COPY_ATTRIBUTES)
        resultArr = new byte[data.defaultDataSize]
        fs.provider().newInputStream(dest3).read(resultArr)

        then:
        resultArr == data.defaultBytes
    }

    // Bug: https://github.com/Azure/azure-sdk-for-java/issues/20325
    def "Files readAllBytes"() {
        setup:
        def fs = createFS(config)
        def pathName = generateBlobName()
        def path1 = fs.getPath("/foo/bar/" + pathName)
        def path2 = fs.getPath("/foo/bar/" + pathName + ".backup")
        Files.createFile(path1)
        Files.createFile(path2)

        when:
        Files.readAllBytes(path1)

        then:
        notThrown(IOException)
    }

    def "Files delete empty directory"() {
        setup: "Create two folders where one is a prefix of the others"
        def fs = createFS(config)
        def pathName = generateBlobName()
        def pathName2 = pathName + '2'
        Files.createDirectory(fs.getPath(pathName))
        Files.createDirectory(fs.getPath(pathName2))

        expect:
        // Delete the one that is a prefix to ensure the other one does not interfere
        Files.delete(fs.getPath(pathName))
    }

    @Unroll
    def "Files exists"() {
        setup:
        def fs = createFS(config)

        // Generate resource names.
        def container1 = rootNameToContainerName(getNonDefaultRootDir(fs))
        def path = (AzurePath) fs.getPath(container1, generateBlobName())

        // Generate clients to resources.
        def blobClient = path.toBlobClient()
        def childClient1 = ((AzurePath) path.resolve(generateBlobName())).toBlobClient()

        // Create resources as necessary
        if (status == DirectoryStatus.NOT_A_DIRECTORY) {
            blobClient.upload(data.defaultInputStream, data.defaultDataSize)
        } else if (status == DirectoryStatus.NOT_EMPTY) {
            if (!isVirtual) {
                putDirectoryBlob(blobClient.getBlockBlobClient())
            }
            childClient1.upload(data.defaultInputStream, data.defaultDataSize)
        }

        expect:
        if (status != DirectoryStatus.DOES_NOT_EXIST) {
            assert Files.exists(path)
        } else {
            assert !Files.exists(path)
        }

        where:
        status                          | isVirtual
        DirectoryStatus.DOES_NOT_EXIST  | false
        DirectoryStatus.NOT_A_DIRECTORY | false
        DirectoryStatus.NOT_EMPTY       | true
        DirectoryStatus.NOT_EMPTY       | false
    }

    def "Files walkFileTree"() {
        setup:
        def fs = createFS(config)
        /*
        file1
        cDir1
        cDir2
        |__file2
        |__cDir3
        |__vDir1
           |__file3
        vDir2
        |__file4
        |__cDir4
        |__vDir3
           |__file5
         */
        def baseDir = "a"
        def file1 = "a/file1"
        def cDir1 = "a/cDir1"
        def cDir2 = "a/cDir2"
        def file2 = "a/cDir2/file2"
        def cDir3 = "a/cDir2/cDir3"
        def vDir1 = "a/cDir2/vDir1"
        def file3 = "a/cDir2/vDir1/file3"
        def vDir2 = "a/vDir2"
        def file4 = "a/vDir2/file4"
        def cDir4 = "a/vDir2/cDir4"
        def vdir3 = "a/vDir2/vDir3"
        def file5 = "a/vDir2/vDir3/file5"

        // Create files and directories
        ((AzurePath) fs.getPath(file1)).toBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        ((AzurePath) fs.getPath(file2)).toBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        ((AzurePath) fs.getPath(file3)).toBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        ((AzurePath) fs.getPath(file4)).toBlobClient().upload(data.defaultInputStream, data.defaultDataSize)
        ((AzurePath) fs.getPath(file5)).toBlobClient().upload(data.defaultInputStream, data.defaultDataSize)

        putDirectoryBlob(((AzurePath) fs.getPath(baseDir)).toBlobClient().getBlockBlobClient())
        putDirectoryBlob(((AzurePath) fs.getPath(cDir1)).toBlobClient().getBlockBlobClient())
        putDirectoryBlob(((AzurePath) fs.getPath(cDir2)).toBlobClient().getBlockBlobClient())
        putDirectoryBlob(((AzurePath) fs.getPath(cDir3)).toBlobClient().getBlockBlobClient())
        putDirectoryBlob(((AzurePath) fs.getPath(cDir4)).toBlobClient().getBlockBlobClient())

        when:
        def visitor = new TestFileVisitor()
        System.out.println(Files.readAttributes(fs.getPath(baseDir), AzureBasicFileAttributes).isDirectory())
        Files.walkFileTree(fs.getPath(baseDir), visitor)

        // might need to make this work on root directories as well, which would probably mean inspecting the path and adding an isRoot method

        then:
        visitor.fileCount == 5
        visitor.directoryCount == 8 // includes baseDir
        visitor.failureCount == 0
        notThrown(Exception)
    }

    class TestFileVisitor<Path> implements FileVisitor<Path> {

        public int fileCount = 0;
        public int directoryCount = 0;
        public int failureCount = 0;

        @Override
        FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE
        }

        @Override
        FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            fileCount++
            return FileVisitResult.CONTINUE
        }

        @Override
        FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            failureCount++
            return FileVisitResult.CONTINUE
        }

        @Override
        FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            directoryCount++
            return FileVisitResult.CONTINUE
        }
    }
}
