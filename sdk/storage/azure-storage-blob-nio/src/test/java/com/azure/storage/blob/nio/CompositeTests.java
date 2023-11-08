// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.storage.blob.BlobClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompositeTests extends BlobNioTestBase {
    private Map<String, Object> config;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        config = initializeConfigMap();
    }

    @Test
    public void filesCreateDirs() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path dirs = fs.getPath("mydir1/mydir2/mydir3");
        Files.createDirectories(dirs);

        assertTrue(Files.isDirectory(fs.getPath("mydir1")));
        assertTrue(Files.isDirectory(fs.getPath("mydir1/mydir2")));
        assertTrue(Files.isDirectory(fs.getPath("mydir1/mydir2/mydir3")));
    }

    @Test
    public void filesCreate() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = Files.createFile(fs.getPath(generateBlobName()));

        assertDoesNotThrow(() -> fs.provider().checkAccess(path));
    }

    @Test
    public void filesCopy() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path dest = fs.getPath("dest");
        byte[] resultArr = new byte[DATA.getDefaultDataSize()];
        Files.copy(DATA.getDefaultInputStream(), dest);
        fs.provider().newInputStream(dest).read(resultArr);

        assertArraysEqual(DATA.getDefaultBytes(), resultArr);

        Path dest2 = fs.getPath("dest2");
        OutputStream outStream = fs.provider().newOutputStream(dest2);
        Files.copy(dest, outStream);
        outStream.close();
        resultArr = new byte[DATA.getDefaultDataSize()];
        fs.provider().newInputStream(dest2).read(resultArr);

        assertArraysEqual(DATA.getDefaultBytes(), resultArr);

        Path dest3 = fs.getPath("dest3");
        Files.copy(dest, dest3, StandardCopyOption.COPY_ATTRIBUTES);
        resultArr = new byte[DATA.getDefaultDataSize()];
        fs.provider().newInputStream(dest3).read(resultArr);

        assertArraysEqual(DATA.getDefaultBytes(), resultArr);
    }

    // Bug: https://github.com/Azure/azure-sdk-for-java/issues/20325
    @Test
    public void filesReadAllBytes() throws IOException {
        AzureFileSystem fs = createFS(config);
        String pathName = generateBlobName();
        Path path1 = fs.getPath("/foo/bar/" + pathName);
        Path path2 = fs.getPath("/foo/bar/" + pathName + ".backup");
        Files.createFile(path1);
        Files.createFile(path2);

        assertDoesNotThrow(() -> Files.readAllBytes(path1));
    }

    @Test
    public void filesDeleteEmptyDirectory() throws IOException {
        // Create two folders where one is a prefix of the others
        AzureFileSystem fs = createFS(config);
        String pathName = generateBlobName();
        String pathName2 = pathName + '2';
        Files.createDirectory(fs.getPath(pathName));
        Files.createDirectory(fs.getPath(pathName2));

        // Delete the one that is a prefix to ensure the other one does not interfere
        assertDoesNotThrow(() -> Files.delete(fs.getPath(pathName)));
    }

    @ParameterizedTest
    @MethodSource("filesExistsSupplier")
    public void filesExists(DirectoryStatus status, boolean isVirtual) throws IOException {
        AzureFileSystem fs = createFS(config);

        // Generate resource names.
        AzurePath path = (AzurePath) fs.getPath(rootNameToContainerName(getNonDefaultRootDir(fs)), generateBlobName());

        // Generate clients to resources.
        BlobClient blobClient = path.toBlobClient();
        BlobClient childClient1 = ((AzurePath) path.resolve(generateBlobName())).toBlobClient();

        // Create resources as necessary
        if (status == DirectoryStatus.NOT_A_DIRECTORY) {
            blobClient.upload(DATA.getDefaultBinaryData());
        } else if (status == DirectoryStatus.NOT_EMPTY) {
            if (!isVirtual) {
                putDirectoryBlob(blobClient.getBlockBlobClient());
            }
            childClient1.upload(DATA.getDefaultBinaryData());
        }

        assertEquals(status != DirectoryStatus.DOES_NOT_EXIST, Files.exists(path));
    }

    private static Stream<Arguments> filesExistsSupplier() {
        return Stream.of(Arguments.of(DirectoryStatus.DOES_NOT_EXIST, false),
            Arguments.of(DirectoryStatus.NOT_A_DIRECTORY, false), Arguments.of(DirectoryStatus.NOT_EMPTY, true),
            Arguments.of(DirectoryStatus.NOT_EMPTY, false));
    }

    @Test
    public void filesWalkFileTree() throws IOException {
        AzureFileSystem fs = createFS(config);
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
        String baseDir = "a";

        // Create files and directories
        ((AzurePath) fs.getPath("a/file1")).toBlobClient().upload(DATA.getDefaultBinaryData());
        ((AzurePath) fs.getPath("a/cDir2/file2")).toBlobClient().upload(DATA.getDefaultBinaryData());
        ((AzurePath) fs.getPath("a/cDir2/vDir1/file3")).toBlobClient().upload(DATA.getDefaultBinaryData());
        ((AzurePath) fs.getPath("a/vDir2/file4")).toBlobClient().upload(DATA.getDefaultBinaryData());
        ((AzurePath) fs.getPath("a/vDir2/vDir3/file5")).toBlobClient().upload(DATA.getDefaultBinaryData());

        putDirectoryBlob(((AzurePath) fs.getPath(baseDir)).toBlobClient().getBlockBlobClient());
        putDirectoryBlob(((AzurePath) fs.getPath("a/cDir1")).toBlobClient().getBlockBlobClient());
        putDirectoryBlob(((AzurePath) fs.getPath("a/cDir2")).toBlobClient().getBlockBlobClient());
        putDirectoryBlob(((AzurePath) fs.getPath("a/cDir2/cDir3")).toBlobClient().getBlockBlobClient());
        putDirectoryBlob(((AzurePath) fs.getPath("a/vDir2/cDir4")).toBlobClient().getBlockBlobClient());

        TestFileVisitor<Path> visitor = new TestFileVisitor<>();
        // System.out.println(Files.readAttributes(fs.getPath(baseDir), AzureBasicFileAttributes.class).isDirectory());
        Files.walkFileTree(fs.getPath(baseDir), visitor);

        // might need to make this work on root directories as well, which would probably mean inspecting the path and
        // adding an isRoot method
        assertEquals(5, visitor.fileCount);
        assertEquals(8, visitor.directoryCount); // includes baseDir
    }

    static class TestFileVisitor<Path> extends SimpleFileVisitor<Path> {
        private int fileCount = 0;
        private int directoryCount = 0;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            fileCount++;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            directoryCount++;
            return FileVisitResult.CONTINUE;
        }
    }
}
