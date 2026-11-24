// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureResourceTests extends BlobNioTestBase {
    private Map<String, Object> config;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        config = initializeConfigMap();
    }

    @Test
    public void constructor() throws IOException {
        AzureFileSystem fs = createFS(config);
        AzureResource resource = new AzureResource(fs.getPath(getNonDefaultRootDir(fs), "foo/bar"));

        assertEquals(getNonDefaultRootDir(fs) + "/foo/bar", resource.getPath().toString());
        assertEquals(resource.getPath().toBlobClient().getBlobUrl(), resource.getBlobClient().getBlobUrl());
    }

    @Test
    public void noRoot() {
        assertThrows(IllegalArgumentException.class, () -> new AzureResource(createFS(config).getPath("root:")));
    }

    @Test
    public void instanceType() {
        assertThrows(IllegalArgumentException.class, () -> new AzureResource(Paths.get("")));
    }

    @ParameterizedTest
    @MethodSource("directoryStatusAndExistsSupplier")
    public void directoryStatusAndExists(DirectoryStatus status, boolean isVirtual) throws IOException {
        AzureFileSystem fs = createFS(config);

        // Generate resource names.
        // In root1, the resource will be in the root. In root2, the resource will be several levels deep. Also
        // root1 will be non-default directory and root2 is default directory.
        AzurePath parentPath1
            = (AzurePath) fs.getPath(rootNameToContainerName(getNonDefaultRootDir(fs)), generateBlobName());
        AzurePath parentPath2 = (AzurePath) fs.getPath(getPathWithDepth(3), generateBlobName());

        // Generate clients to resources.
        BlobClient blobClient1 = parentPath1.toBlobClient();
        BlobClient blobClient2 = parentPath2.toBlobClient();
        BlobClient childClient1 = ((AzurePath) parentPath1.resolve(generateBlobName())).toBlobClient();
        BlobClient childClient2 = ((AzurePath) parentPath2.resolve(generateBlobName())).toBlobClient();

        // Create resources as necessary
        if (status == DirectoryStatus.NOT_A_DIRECTORY) {
            blobClient1.upload(DATA.getDefaultBinaryData());
            blobClient2.upload(DATA.getDefaultBinaryData());
        } else if (status == DirectoryStatus.EMPTY) {
            putDirectoryBlob(blobClient1.getBlockBlobClient());
            putDirectoryBlob(blobClient2.getBlockBlobClient());
        } else if (status == DirectoryStatus.NOT_EMPTY) {
            if (!isVirtual) {
                putDirectoryBlob(blobClient1.getBlockBlobClient());
                putDirectoryBlob(blobClient2.getBlockBlobClient());
            }
            childClient1.upload(DATA.getDefaultBinaryData());
            childClient2.upload(DATA.getDefaultBinaryData());
        }

        boolean directoryExists = status == DirectoryStatus.EMPTY || status == DirectoryStatus.NOT_EMPTY;
        assertEquals(status, new AzureResource(parentPath1).getDirectoryStatus());
        assertEquals(status, new AzureResource(parentPath2).getDirectoryStatus());
        assertEquals(directoryExists, new AzureResource(parentPath1).checkDirectoryExists());
        assertEquals(directoryExists, new AzureResource(parentPath2).checkDirectoryExists());
    }

    private static Stream<Arguments> directoryStatusAndExistsSupplier() {
        return Stream.of(Arguments.of(DirectoryStatus.DOES_NOT_EXIST, false),
            Arguments.of(DirectoryStatus.NOT_A_DIRECTORY, false), Arguments.of(DirectoryStatus.EMPTY, false),
            Arguments.of(DirectoryStatus.NOT_EMPTY, true), Arguments.of(DirectoryStatus.NOT_EMPTY, false));
    }

    @Test
    public void directoryStatusFilesWithSamePrefix() throws IOException {
        AzureFileSystem fs = createFS(config);
        // Create two files with same prefix. Both paths should have DirectoryStatus.NOT_A_DIRECTORY
        String pathName = generateBlobName();
        Path path1 = fs.getPath("/foo/bar/" + pathName + ".txt");
        Path path2 = fs.getPath("/foo/bar/" + pathName + ".txt.backup");
        Files.createFile(path1);
        Files.createFile(path2);

        assertEquals(DirectoryStatus.NOT_A_DIRECTORY, new AzureResource(path1).getDirectoryStatus());
        assertEquals(DirectoryStatus.NOT_A_DIRECTORY, new AzureResource(path2).getDirectoryStatus());
    }

    @Test
    public void directoryStatusDirectoriesWithSamePrefix() throws IOException {
        // Create two folders where one is a prefix of the others
        AzureFileSystem fs = createFS(config);
        String pathName = generateBlobName();
        String pathName2 = pathName + '2';
        Files.createDirectory(fs.getPath(pathName));
        Files.createDirectory(fs.getPath(pathName2));

        // Both should be empty
        assertEquals(DirectoryStatus.EMPTY, new AzureResource(fs.getPath(pathName)).getDirectoryStatus());
        assertEquals(DirectoryStatus.EMPTY, new AzureResource(fs.getPath(pathName2)).getDirectoryStatus());
    }

    @Test
    public void directoryStatusFilesBetweenPrefixAndChild() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path dirPath = fs.getPath(generateBlobName());
        Path childPath = fs.getPath(dirPath.toString(), generateBlobName());
        // Under an old listing scheme, it was possible for a file with the same name as a directory but with a trailing
        // '+' to cut in between the parent and child in the listing as we did it and the listing may not register the
        // child and erroneously return that the directory is empty. This ensures that listing is done in such a way as
        // to account for this and return correctly that the directory is not empty.
        Path middlePath = fs.getPath(dirPath + "+");

        Files.createDirectory(dirPath);
        Files.createFile(childPath);
        Files.createFile(middlePath);

        assertEquals(DirectoryStatus.NOT_EMPTY, new AzureResource(dirPath).getDirectoryStatus());
    }

    @Test
    public void parentDirExistsFalse() throws IOException {
        assertFalse(new AzureResource(createFS(config).getPath(generateBlobName(), "bar")).parentDirectoryExists());
    }

    @Test
    public void parentDirExistsVirtual() throws IOException {
        AzureFileSystem fs = createFS(config);
        String fileName = generateBlobName();
        String childName = generateBlobName();
        rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(fileName + fs.getSeparator() + childName)
            .getAppendBlobClient()
            .create();

        assertTrue(new AzureResource(fs.getPath(fileName, childName)).parentDirectoryExists());
    }

    @Test
    public void parentDirExistsConcrete() throws IOException {
        AzureFileSystem fs = createFS(config);
        String fileName = generateBlobName();
        putDirectoryBlob(rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(fileName).getBlockBlobClient());

        assertTrue(new AzureResource(fs.getPath(fileName, "bar")).parentDirectoryExists());
    }

    @Test
    public void parentDirExistsRoot() throws IOException {
        // No parent means the parent is implicitly the default root, which always exists
        assertTrue(new AzureResource(createFS(config).getPath("foo")).parentDirectoryExists());

    }

    @Test
    public void parentDirExistsNonDefaultRoot() throws IOException {
        // Checks for a bug where we would check the wrong root container for existence on a path with depth > 1
        AzureFileSystem fs = createFS(config);
        String rootName = getNonDefaultRootDir(fs);
        rootNameToContainerClient(rootName).getBlobClient("fizz/buzz/bazz").getAppendBlobClient().create();

        assertTrue(new AzureResource(fs.getPath(rootName, "fizz/buzz")).parentDirectoryExists());
    }

    @ParameterizedTest
    @CsvSource(value = { "false,false", "true,false", "false,true", "true,true" })
    public void putDirectoryBlob(boolean metadata, boolean properties) throws IOException, NoSuchAlgorithmException {
        AzureResource resource = new AzureResource(createFS(config).getPath(generateBlobName()));
        byte[] contentMd5 = MessageDigest.getInstance("MD5").digest(new byte[0]);
        List<FileAttribute<?>> attributes = new ArrayList<>();
        if (metadata) {
            attributes.add(new TestFileAttribute<>("fizz", "buzz"));
            attributes.add(new TestFileAttribute<>("foo", "bar"));
        }
        if (properties) {
            attributes.add(new TestFileAttribute<>("Content-Type", "myType"));
            attributes.add(new TestFileAttribute<>("Content-Disposition", "myDisposition"));
            attributes.add(new TestFileAttribute<>("Content-Language", "myLanguage"));
            attributes.add(new TestFileAttribute<>("Content-Encoding", "myEncoding"));
            attributes.add(new TestFileAttribute<>("Cache-Control", "myControl"));
            attributes.add(new TestFileAttribute<>("Content-MD5", contentMd5));
        }

        if (metadata || properties) {
            resource.setFileAttributes(attributes);
        }
        resource.putDirectoryBlob(null);
        checkBlobIsDir(resource.getBlobClient());
        BlobProperties props = resource.getBlobClient().getProperties();

        if (metadata) {
            assertEquals("buzz", props.getMetadata().get("fizz"));
            assertEquals("bar", props.getMetadata().get("foo"));
            assertFalse(props.getMetadata().containsKey("Content-Type"));
            assertFalse(props.getMetadata().containsKey("Content-Disposition"));
            assertFalse(props.getMetadata().containsKey("Content-Language"));
            assertFalse(props.getMetadata().containsKey("Content-Encoding"));
            assertFalse(props.getMetadata().containsKey("Content-MD5"));
            assertFalse(props.getMetadata().containsKey("Cache-Control"));
        }
        if (properties) {
            assertEquals("myType", props.getContentType());
            assertEquals("myDisposition", props.getContentDisposition());
            assertEquals("myLanguage", props.getContentLanguage());
            assertEquals("myEncoding", props.getContentEncoding());
            assertArraysEqual(contentMd5, props.getContentMd5());
            assertEquals("myControl", props.getCacheControl());
        }
    }

    @ParameterizedTest
    @MethodSource("putDirectoryBlobACSupplier")
    public void putDirectoryBlobAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch)
        throws IOException {
        AzureResource resource = new AzureResource(createFS(config).getPath(generateBlobName()));
        resource.getBlobClient().upload(DATA.getDefaultBinaryData());
        match = setupBlobMatchCondition(resource.getBlobClient(), match);
        resource.putDirectoryBlob(new BlobRequestConditions().setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified));

        checkBlobIsDir(resource.getBlobClient());
    }

    private static Stream<Arguments> putDirectoryBlobACSupplier() {
        return Stream.of(Arguments.of(null, null, null, null), Arguments.of(OLD_DATE, null, null, null),
            Arguments.of(null, NEW_DATE, null, null), Arguments.of(null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, GARBAGE_ETAG));
    }

    @ParameterizedTest
    @MethodSource("putDirectoryBlobACFailSupplier")
    public void putDirectoryBlobACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch) throws IOException {
        AzureResource resource = new AzureResource(createFS(config).getPath(generateBlobName()));
        resource.getBlobClient().upload(DATA.getDefaultBinaryData());
        noneMatch = setupBlobMatchCondition(resource.getBlobClient(), noneMatch);
        BlobRequestConditions bac = new BlobRequestConditions().setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        BlobStorageException e = assertThrows(BlobStorageException.class, () -> resource.putDirectoryBlob(bac));
        assertTrue(e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET
            || e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION);
    }

    private static Stream<Arguments> putDirectoryBlobACFailSupplier() {
        return Stream.of(Arguments.of(NEW_DATE, null, null, null), Arguments.of(null, OLD_DATE, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null), Arguments.of(null, null, null, RECEIVED_ETAG));
    }
}
