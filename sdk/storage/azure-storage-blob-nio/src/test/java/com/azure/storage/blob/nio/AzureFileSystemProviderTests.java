// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("resource")
public class AzureFileSystemProviderTests extends BlobNioTestBase {
    Map<String, Object> config;
    private AzureFileSystemProvider provider;

    // The following are common among a large number of copy tests
    private AzurePath sourcePath;
    private AzurePath destPath;
    private BlobClient sourceClient;
    private BlobClient destinationClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        config = initializeConfigMap();
        provider = new AzureFileSystemProvider();
    }

    @Test
    public void createFileSystem() throws IOException {
        config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL, ENV.getPrimaryAccount().getCredential());
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, generateContainerName());
        URI uri = getFileSystemUri();
        provider.newFileSystem(uri, config);

        assertTrue(provider.getFileSystem(uri).isOpen());
        assertEquals(primaryBlobServiceClient.getAccountUrl(),
            ((AzureFileSystem) provider.getFileSystem(uri)).getFileSystemUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = { "azc://path", "azb://path", "azb://?foo=bar", "azb://?account=" })
    public void createFileSystemInvalidUri(String uri) {
        assertThrows(IllegalArgumentException.class, () -> provider.newFileSystem(new URI(uri), config));
    }

    @Test
    public void createFileSystemDuplicate() throws IOException {
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, generateContainerName());
        config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL, ENV.getPrimaryAccount().getCredential());
        provider.newFileSystem(getFileSystemUri(), config);

        assertThrows(FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(getFileSystemUri(), config));
    }

    @Test
    public void createFileSystemInitialCheckFail() {
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, generateContainerName());
        byte[] badKey = ENV.getPrimaryAccount().getKey().getBytes(StandardCharsets.UTF_8);
        badKey[0]++;
        config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL,
            new StorageSharedKeyCredential(ENV.getPrimaryAccount().getName(), new String(badKey)));

        assertThrows(IOException.class, () -> provider.newFileSystem(getFileSystemUri(), config));
        assertThrows(FileSystemNotFoundException.class, () -> provider.getFileSystem(getFileSystemUri()));
    }

    @Test
    public void getFileSystemNotFound() {
        assertThrows(FileSystemNotFoundException.class, () -> provider.getFileSystem(getFileSystemUri()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "azc://path", "azb://path", "azb://?foo=bar", "azb://?account=" })
    public void getFileSystemIa(String uri) {
        assertThrows(IllegalArgumentException.class, () -> provider.getFileSystem(new URI(uri)));
    }

    // TODO: Be sure to test operating on containers that already have data
    // all apis should have a test that tries them after the FileSystem is closed to ensure they throw.
    @Test
    public void getScheme() {
        assertEquals("azb", provider.getScheme());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    public void createDirParentExists(int depth) throws IOException {
        AzureFileSystem fs = createFS(config);

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        String rootName = getNonDefaultRootDir(fs);
        String parent = getPathWithDepth(depth);
        String dirPathStr = parent + generateBlobName();

        Path dirPath = fs.getPath(rootName, dirPathStr);

        // Generate clients to resources. Create resources as necessary
        BlobContainerClient containerClient = rootNameToContainerClient(rootName);
        /*
        In this case, we are putting the blob in the root directory, i.e. directly in the container, so no need to
        create a blob.
         */
        if (!"".equals(parent)) {
            containerClient.getBlobClient(parent).getAppendBlobClient().create();
        }
        BlobClient dirClient = containerClient.getBlobClient(dirPathStr);
        fs.provider().createDirectory(dirPath);

        checkBlobIsDir(dirClient);
    }

    @Test
    public void createDirRelativePath() throws IOException {
        AzureFileSystem fs = createFS(config);
        String fileName = generateBlobName();
        BlobClient blobClient = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(fileName);

        // Relative paths are resolved against the default directory
        fs.provider().createDirectory(fs.getPath(fileName));

        checkBlobIsDir(blobClient);
    }

    @Test
    public void createDirFileAlreadyExists() {
        AzureFileSystem fs = createFS(config);
        String fileName = generateBlobName();
        BlockBlobClient blobClient
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(fileName).getBlockBlobClient();
        blobClient.commitBlockList(Collections.emptyList(), false);

        // Will go to default directory
        assertThrows(FileAlreadyExistsException.class, () -> fs.provider().createDirectory(fs.getPath(fileName)));
    }

    @Test
    public void createDirConcreteDirAlreadyExists() {
        AzureFileSystem fs = createFS(config);
        String fileName = generateBlobName();
        BlockBlobClient blobClient
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(fileName).getBlockBlobClient();
        putDirectoryBlob(blobClient);

        assertThrows(FileAlreadyExistsException.class, () -> fs.provider().createDirectory(fs.getPath(fileName)));
    }

    @Test
    public void createDirVirtualDirAlreadyExists() throws IOException {
        AzureFileSystem fs = createFS(config);
        String fileName = generateBlobName();
        BlobContainerClient containerClient = rootNameToContainerClient(getDefaultDir(fs));
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        AppendBlobClient blobClient2
            = containerClient.getBlobClient(fileName + fs.getSeparator() + generateBlobName()).getAppendBlobClient();
        blobClient2.create();
        fs.provider().createDirectory(fs.getPath(fileName));

        assertTrue(blobClient.exists()); // We will turn the directory from virtual to concrete
        checkBlobIsDir(blobClient);
    }

    @Test
    public void createDirRoot() {
        AzureFileSystem fs = createFS(config);

        assertThrows(IllegalArgumentException.class, () -> fs.provider().createDirectory(fs.getDefaultDirectory()));
    }

    @Test
    public void createDirNoParent() {
        AzureFileSystem fs = createFS(config);

        // Parent doesn't exists.
        assertThrows(IOException.class, () -> fs.provider()
            .createDirectory(fs.getPath(generateBlobName() + fs.getSeparator() + generateBlobName())));
    }

    @Test
    public void createDirInvalidRoot() {
        AzureFileSystem fs = createFS(config);

        assertThrows(IOException.class,
            () -> fs.provider().createDirectory(fs.getPath("fakeRoot:" + fs.getSeparator() + generateBlobName())));
    }

    @Test
    public void createDirAttributes() throws NoSuchAlgorithmException, IOException {
        AzureFileSystem fs = createFS(config);
        String fileName = generateBlobName();
        AppendBlobClient blobClient
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(fileName).getAppendBlobClient();
        byte[] contentMd5 = MessageDigest.getInstance("MD5").digest(new byte[0]);
        FileAttribute<?>[] attributes = new FileAttribute<?>[] {
            new TestFileAttribute<>("fizz", "buzz"),
            new TestFileAttribute<>("foo", "bar"),
            new TestFileAttribute<>("Content-Type", "myType"),
            new TestFileAttribute<>("Content-Disposition", "myDisposition"),
            new TestFileAttribute<>("Content-Language", "myLanguage"),
            new TestFileAttribute<>("Content-Encoding", "myEncoding"),
            new TestFileAttribute<>("Cache-Control", "myControl"),
            new TestFileAttribute<>("Content-MD5", contentMd5) };

        fs.provider().createDirectory(fs.getPath(fileName), attributes);
        BlobProperties props = blobClient.getProperties();

        assertEquals("buzz", props.getMetadata().get("fizz"));
        assertEquals("bar", props.getMetadata().get("foo"));
        assertFalse(props.getMetadata().containsKey("Content-Type"));
        assertFalse(props.getMetadata().containsKey("Content-Disposition"));
        assertFalse(props.getMetadata().containsKey("Content-Language"));
        assertFalse(props.getMetadata().containsKey("Content-Encoding"));
        assertFalse(props.getMetadata().containsKey("Content-MD5"));
        assertFalse(props.getMetadata().containsKey("Cache-Control"));
        assertEquals("myType", props.getContentType());
        assertEquals("myDisposition", props.getContentDisposition());
        assertEquals("myLanguage", props.getContentLanguage());
        assertEquals("myEncoding", props.getContentEncoding());
        assertArraysEqual(contentMd5, props.getContentMd5());
        assertEquals("myControl", props.getCacheControl());
    }

    @Test
    public void createDirFSClosed() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> fs.provider().createDirectory(path));
    }

    @ParameterizedTest
    @CsvSource(value = { "false,false,false", "true,true,false", "true,false,true", "true,false,false" })
    public void copySource(boolean sourceIsDir, boolean sourceIsVirtual, boolean sourceEmpty) throws IOException {
        AzureFileSystem fs = createFS(config);
        basicSetupForCopyTest(fs);

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        AppendBlobClient sourceChildClient = null;
        AppendBlobClient destChildClient = null;

        // Create resources as necessary
        if (sourceIsDir) {
            if (!sourceIsVirtual) {
                fs.provider().createDirectory(sourcePath);
            }
            if (!sourceEmpty) {
                String sourceChildName = generateBlobName();
                sourceChildClient
                    = ((AzurePath) sourcePath.resolve(sourceChildName)).toBlobClient().getAppendBlobClient();
                sourceChildClient.create();
                destChildClient = ((AzurePath) destPath.resolve(sourceChildName)).toBlobClient().getAppendBlobClient();
            }
        } else { // source is file
            sourceClient.upload(DATA.getDefaultBinaryData());
        }

        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES);

        // Check the source still exists.
        if (!sourceIsVirtual) {
            assertTrue(sourceClient.exists());
        } else {
            assertTrue(new AzureResource(sourcePath).checkDirectoryExists());
        }

        // If the source was a file, check that the destination data matches the source.
        if (!sourceIsDir) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            destinationClient.download(outStream);
            assertArraysEqual(DATA.getDefaultBytes(), outStream.toByteArray());
        } else {
            // Check that the destination directory is concrete.
            assertTrue(destinationClient.exists());
            checkBlobIsDir(destinationClient);
            if (!sourceEmpty) {
                // Check that source child still exists and was not copied to the destination.
                assertTrue(sourceChildClient.exists());
                assertFalse(destChildClient.exists());
            }
        }
    }

    @Test
    public void copySourceWithSas() throws IOException {
        AzureSasCredential cred = new AzureSasCredential(primaryBlobServiceClient
            .generateAccountSas(new AccountSasSignatureValues(testResourceNamer.now().plusDays(2),
                AccountSasPermission.parse("rwcdl"), new AccountSasService().setBlobAccess(true),
                new AccountSasResourceType().setContainer(true).setObject(true))));

        config.put(AzureFileSystem.AZURE_STORAGE_SAS_TOKEN_CREDENTIAL, cred);
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, generateContainerName() + "," + generateContainerName());

        AzureFileSystem fs
            = new AzureFileSystem(new AzureFileSystemProvider(), ENV.getPrimaryAccount().getBlobEndpoint(), config);
        basicSetupForCopyTest(fs);

        fs.provider().createDirectory(sourcePath);
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES);

        assertTrue(destinationClient.exists());
        checkBlobIsDir(destinationClient);
    }

    @ParameterizedTest
    @CsvSource(value = { "false,false", "true,false", "true,true" })
    public void copyDestination(boolean destinationExists, boolean destinationIsDir) throws IOException {
        AzureFileSystem fs = createFS(config);
        basicSetupForCopyTest(fs);

        // Create resources as necessary
        sourceClient.upload(DATA.getDefaultBinaryData());
        if (destinationExists) {
            if (destinationIsDir) {
                fs.provider().createDirectory(destPath);
            } else { // source is file
                destinationClient.upload(new ByteArrayInputStream(getRandomByteArray(20)), 20);
            }
        }
        fs.provider()
            .copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);

        assertTrue(sourceClient.exists());
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        destinationClient.download(outStream);
        assertArraysEqual(DATA.getDefaultBytes(), outStream.toByteArray());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void copyNonEmptyDest(boolean destinationIsVirtual) throws IOException {
        AzureFileSystem fs = createFS(config);
        basicSetupForCopyTest(fs);

        // Create resources as necessary
        sourceClient.upload(new ByteArrayInputStream(getRandomByteArray(20)), 20);
        if (!destinationIsVirtual) {
            fs.provider().createDirectory(destPath);
        }
        BlobClient destChildClient = ((AzurePath) destPath.resolve(generateBlobName())).toBlobClient();
        destChildClient.upload(DATA.getDefaultBinaryData());

        // Ensure that even when trying to replace_existing, we still fail.
        assertThrows(DirectoryNotEmptyException.class, () -> fs.provider()
            .copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING));
        assertTrue(new AzureResource(destPath).checkDirectoryExists());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void copyReplaceExistingFail(boolean destinationIsDir) throws IOException {
        // The success case is tested by the "copy destination" test.
        // Testing replacing a virtual directory is in the "non empty dest" test as there can be no empty virtual dir.
        AzureFileSystem fs = createFS(config);
        basicSetupForCopyTest(fs);

        // Create resources as necessary
        sourceClient.upload(new ByteArrayInputStream(getRandomByteArray(20)), 20);
        if (destinationIsDir) {
            fs.provider().createDirectory(destPath);
        } else {
            destinationClient.upload(DATA.getDefaultBinaryData());
        }

        assertThrows(FileAlreadyExistsException.class,
            () -> fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES));
        if (destinationIsDir) {
            assertTrue(new AzureResource(destPath).checkDirectoryExists());
        } else {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            destinationClient.download(outStream);
            assertArraysEqual(DATA.getDefaultBytes(), outStream.toByteArray());
        }
    }

    @Test
    public void copyOptionsFail() {
        AzureFileSystem fs = createFS(config);
        basicSetupForCopyTest(fs);

        assertThrows(UnsupportedOperationException.class, () -> fs.provider().copy(sourcePath, destPath));
        assertThrows(UnsupportedOperationException.class, () -> fs.provider()
            .copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.ATOMIC_MOVE));
    }

    @ParameterizedTest
    @CsvSource(value = { "1,1", "1,2", "1,3", "2,1", "2,2", "2,3", "3,1", "3,2", "3,3" })
    public void copyDepth(int sourceDepth, int destDepth) throws IOException {
        AzureFileSystem fs = createFS(config);

        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        String rootName = getNonDefaultRootDir(fs);
        AzurePath sourcePath = (AzurePath) fs.getPath(rootName, getPathWithDepth(sourceDepth), generateBlobName());

        String destParent = getPathWithDepth(destDepth);
        AzurePath destPath = (AzurePath) fs.getPath(rootName, destParent, generateBlobName());

        // Generate clients to resources.
        BlobClient sourceClient = sourcePath.toBlobClient();
        BlobClient destinationClient = destPath.toBlobClient();
        BlobClient destParentClient = ((AzurePath) destPath.getParent()).toBlobClient();

        // Create resources as necessary
        sourceClient.upload(DATA.getDefaultBinaryData());
        putDirectoryBlob(destParentClient.getBlockBlobClient());

        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        destinationClient.download(outStream);
        assertArraysEqual(DATA.getDefaultBytes(), outStream.toByteArray());
    }

    @Test
    public void copyNoParentForDest() throws IOException {
        AzureFileSystem fs = createFS(config);
        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        String rootName = getNonDefaultRootDir(fs);
        AzurePath sourcePath = (AzurePath) fs.getPath(rootName, generateBlobName());
        AzurePath destPath = (AzurePath) fs.getPath(rootName, generateBlobName(), generateBlobName());

        // Generate clients to resources.
        BlobClient sourceClient = sourcePath.toBlobClient();
        BlobClient destinationClient = destPath.toBlobClient();

        // Create resources as necessary
        sourceClient.upload(new ByteArrayInputStream(getRandomByteArray(20)), 20);

        assertThrows(IOException.class,
            () -> fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES));
        assertFalse(destinationClient.exists());
    }

    @Test
    public void copySourceDoesNotExist() {
        AzureFileSystem fs = createFS(config);
        basicSetupForCopyTest(fs);

        assertThrows(IOException.class,
            () -> fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES));
    }

    @Test
    public void copyNoRootDir() {
        AzureFileSystem fs = createFS(config);
        basicSetupForCopyTest(fs);

        // Source root
        assertThrows(IllegalArgumentException.class,
            () -> fs.provider().copy(sourcePath.getRoot(), destPath, StandardCopyOption.COPY_ATTRIBUTES));

        // Dest root
        assertThrows(IllegalArgumentException.class,
            () -> fs.provider().copy(sourcePath, destPath.getRoot(), StandardCopyOption.COPY_ATTRIBUTES));
    }

    @Test
    public void copySameFileNoop() {
        AzureFileSystem fs = createFS(config);
        basicSetupForCopyTest(fs);

        // Even when the source does not exist or COPY_ATTRIBUTES is not specified, this will succeed as no-op
        assertDoesNotThrow(() -> fs.provider().copy(sourcePath, sourcePath));
    }

    @Test
    public void copyAcrossContainers() throws IOException {
        AzureFileSystem fs = createFS(config);

        // Generate resource names.
        AzurePath sourcePath = (AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName());
        AzurePath destPath = (AzurePath) fs.getPath(getDefaultDir(fs), generateBlobName());

        // Generate clients to resources.
        BlobClient sourceClient = sourcePath.toBlobClient();
        BlobClient destinationClient = destPath.toBlobClient();

        // Create resources as necessary
        sourceClient.upload(DATA.getDefaultBinaryData());
        fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES);

        assertTrue(sourceClient.exists());
        assertTrue(destinationClient.exists());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void copyClosedFS(boolean sourceClosed) throws IOException {
        AzureFileSystem fs = createFS(config);
        basicSetupForCopyTest(fs);
        AzureFileSystem fsDest = createFS(config);
        Path destPath = fsDest.getPath(getDefaultDir(fsDest), generateBlobName());
        sourceClient.upload(DATA.getDefaultBinaryData());

        if (sourceClosed) {
            fs.close();
        } else {
            fsDest.close();
        }

        assertThrows(ClosedFileSystemException.class,
            () -> fs.provider().copy(sourcePath, destPath, StandardCopyOption.COPY_ATTRIBUTES));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void delete(boolean isDir) throws IOException {
        AzureFileSystem fs = createFS(config);
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName()));
        BlockBlobClient blobClient = path.toBlobClient().getBlockBlobClient();

        if (isDir) {
            putDirectoryBlob(blobClient);
        } else {
            blobClient.upload(DATA.getDefaultBinaryData());
        }

        fs.provider().delete(path);

        assertFalse(blobClient.exists());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void deleteNonEmptyDir(boolean virtual) throws IOException {
        AzureFileSystem fs = createFS(config);
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName()));
        BlockBlobClient blobClient = path.toBlobClient().getBlockBlobClient();
        BlobClient childClient = ((AzurePath) path.resolve(generateBlobName())).toBlobClient();

        childClient.upload(DATA.getDefaultBinaryData());
        if (!virtual) {
            putDirectoryBlob(blobClient);
        }

        assertThrows(DirectoryNotEmptyException.class, () -> fs.provider().delete(path));
        assertTrue(new AzureResource(path).checkDirectoryExists());
    }

    @Test
    public void deleteNoTarget() {
        AzureFileSystem fs = createFS(config);
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName()));

        assertThrows(NoSuchFileException.class, () -> fs.provider().delete(path));
    }

    @Test
    public void deleteDefaultDir() throws IOException {
        AzureFileSystem fs = createFS(config);
        AzurePath path = ((AzurePath) fs.getPath(generateBlobName()));
        BlobClient client = path.toBlobClient();

        client.upload(DATA.getDefaultBinaryData());
        fs.provider().delete(path);

        assertFalse(client.exists());
    }

    @Test
    public void deleteClosedFS() throws IOException {
        AzureFileSystem fs = createFS(config);
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName()));
        BlockBlobClient blobClient = path.toBlobClient().getBlockBlobClient();
        putDirectoryBlob(blobClient);

        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> fs.provider().delete(path));
    }

    @Test
    public void directoryStream() throws IOException {
        AzureFileSystem fs = createFS(config);
        AzureResource resource = new AzureResource(fs.getPath("a" + generateBlobName()));
        resource.getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList());
        resource = new AzureResource(fs.getPath("b" + generateBlobName()));
        resource.getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList());

        Iterator<Path> iterator = fs.provider()
            .newDirectoryStream(fs.getPath(getDefaultDir(fs)), path -> path.getFileName().toString().startsWith("a"))
            .iterator();

        assertTrue(iterator.hasNext());
        assertTrue(iterator.next().getFileName().toString().startsWith("a"));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void directoryStreamInvalidRoot() {
        AzureFileSystem fs = createFS(config);

        assertThrows(IOException.class, () -> fs.provider().newDirectoryStream(fs.getPath("fakeRoot:"), path -> true));
    }

    @Test
    public void directoryStreamClosedFS() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(getDefaultDir(fs));
        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> fs.provider().newDirectoryStream(path, null));
    }

    @Test
    public void inputStreamDefault() throws IOException {
        AzureFileSystem fs = createFS(config);
        sourcePath = (AzurePath) fs.getPath(generateBlobName());
        sourceClient = sourcePath.toBlobClient();
        sourceClient.upload(DATA.getDefaultBinaryData());

        compareInputStreams(fs.provider().newInputStream(sourcePath), DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize());
    }

    @ParameterizedTest
    @EnumSource(
        value = StandardOpenOption.class,
        names = {
            "APPEND",
            "CREATE",
            "CREATE_NEW",
            "DELETE_ON_CLOSE",
            "DSYNC",
            "SPARSE",
            "SYNC",
            "TRUNCATE_EXISTING",
            "WRITE" })
    public void inputStreamOptionsFail(StandardOpenOption option) {
        AzureFileSystem fs = createFS(config);

        // Options are validated before path is validated.
        assertThrows(UnsupportedOperationException.class,
            () -> fs.provider().newInputStream(fs.getPath("foo"), option));
    }

    @Test
    public void inputStreamNonFileFailRoot() {
        AzureFileSystem fs = createFS(config);

        assertThrows(IllegalArgumentException.class, () -> fs.provider().newInputStream(fs.getPath(getDefaultDir(fs))));
    }

    @Test
    public void inputStreamNonFileFailDir() {
        AzureFileSystem fs = createFS(config);
        BlockBlobClient bc
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(generateBlobName()).getBlockBlobClient();
        putDirectoryBlob(bc);

        assertThrows(IOException.class, () -> fs.provider().newInputStream(fs.getPath(bc.getBlobName())));
    }

    @Test
    public void inputStreamNonFileFailNoFile() {
        AzureFileSystem fs = createFS(config);

        assertThrows(IOException.class, () -> fs.provider().newInputStream(fs.getPath("foo")));
    }

    @Test
    public void inputStreamFSClosed() throws IOException {
        AzureFileSystem fs = createFS(config);
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName()));
        BlockBlobClient blobClient = path.toBlobClient().getBlockBlobClient();
        blobClient.upload(DATA.getDefaultBinaryData());

        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> fs.provider().newInputStream(path));
    }

    @Test
    public void outputStreamOptionsDefault() throws IOException {
        AzureFileSystem fs = createFS(config);
        BlockBlobClient bc
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(generateBlobName()).getBlockBlobClient();
        OutputStream nioStream = fs.provider().newOutputStream(fs.getPath(bc.getBlobName()));

        // Defaults should allow us to create a new file.
        nioStream.write(DATA.getDefaultBytes());
        nioStream.close();

        compareInputStreams(bc.openInputStream(), DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        // Defaults should allow us to open to an existing file and overwrite the destination.
        byte[] randomData = getRandomByteArray(100);
        nioStream = fs.provider().newOutputStream(fs.getPath(bc.getBlobName()));
        nioStream.write(randomData);
        nioStream.close();

        compareInputStreams(bc.openInputStream(), new ByteArrayInputStream(randomData), 100);
    }

    @Test
    public void outputStreamOptionsCreate() {
        // Create works both on creating new and opening existing. We test these scenarios above.
        // Here we assert that we cannot create without this option (i.e. you are only allowed to overwrite, not create)
        AzureFileSystem fs = createFS(config);

        // Explicitly exclude a create option.
        assertThrows(IOException.class,
            () -> fs.provider()
                .newOutputStream(fs.getPath(generateBlobName()), StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING));
    }

    @Test
    public void outputStreamOptionsCreateNew() throws IOException {
        AzureFileSystem fs = createFS(config);
        BlockBlobClient bc
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(generateBlobName()).getBlockBlobClient();

        // Succeed in creating a new file
        OutputStream nioStream = fs.provider()
            .newOutputStream(fs.getPath(bc.getBlobName()), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING);
        nioStream.write(DATA.getDefaultBytes());
        nioStream.close();

        compareInputStreams(bc.openInputStream(), DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        // Fail in overwriting an existing
        assertThrows(IOException.class,
            () -> fs.provider()
                .newOutputStream(fs.getPath(bc.getBlobName()), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING));
    }

    @Test
    public void outputStreamOptionsMissingRequired() {
        AzureFileSystem fs = createFS(config);

        // Missing WRITE
        assertThrows(IllegalArgumentException.class,
            () -> fs.provider().newOutputStream(fs.getPath(generateBlobName()), StandardOpenOption.TRUNCATE_EXISTING));

        // Missing TRUNCATE_EXISTING and CREATE_NEW
        assertThrows(IllegalArgumentException.class,
            () -> fs.provider().newOutputStream(fs.getPath(generateBlobName()), StandardOpenOption.WRITE));

        // Missing only TRUNCATE_EXISTING
        assertDoesNotThrow(() -> fs.provider()
            .newOutputStream(fs.getPath(generateBlobName()), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW));

        // Missing only CREATE_NEW
        assertDoesNotThrow(() -> fs.provider()
            .newOutputStream(fs.getPath(generateBlobName()), StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE));
    }

    @ParameterizedTest
    @EnumSource(
        value = StandardOpenOption.class,
        names = { "APPEND", "DELETE_ON_CLOSE", "DSYNC", "READ", "SPARSE", "SYNC" })
    public void outputStreamOptionsInvalid(StandardOpenOption option) {
        AzureFileSystem fs = createFS(config);

        assertThrows(UnsupportedOperationException.class,
            () -> fs.provider()
                .newOutputStream(fs.getPath(generateBlobName()), option, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING));
    }

    @LiveOnly
    @ParameterizedTest
    @CsvSource(value = { "60,0", "150,3" })
    public void outputStreamFileSystemConfig(int dataSize, int blockCount) throws IOException {
        config.put(AzureFileSystem.AZURE_STORAGE_UPLOAD_BLOCK_SIZE, 50L);
        config.put(AzureFileSystem.AZURE_STORAGE_PUT_BLOB_THRESHOLD, 100L);
        AzureFileSystem fs = createFS(config);
        BlockBlobClient bc
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(generateBlobName()).getBlockBlobClient();
        OutputStream nioStream = fs.provider().newOutputStream(fs.getPath(bc.getBlobName()));
        byte[] data = getRandomByteArray(dataSize);

        nioStream.write(data);
        nioStream.close();

        assertEquals(blockCount, bc.listBlocks(BlockListType.COMMITTED).getCommittedBlocks().size());
    }

    @Test
    public void outputSteamOpenDirectoryFail() {
        AzureFileSystem fs = createFS(config);
        BlockBlobClient bc
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(generateBlobName()).getBlockBlobClient();
        putDirectoryBlob(bc);

        assertThrows(IOException.class, () -> fs.provider().newOutputStream(fs.getPath(bc.getBlobName())));
    }

    @Test
    public void outputStreamClosedFS() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());

        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> fs.provider().newOutputStream(path));
    }

    @Test
    public void byteChannelDefault() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        Files.createFile(path);

        SeekableByteChannel channel = fs.provider().newByteChannel(path, null);

        // This indicates the channel is open in read mode, which is the default
        assertDoesNotThrow(() -> channel.read(ByteBuffer.allocate(1)));
    }

    @ParameterizedTest
    @EnumSource(value = StandardOpenOption.class, names = { "APPEND", "DELETE_ON_CLOSE", "DSYNC", "SPARSE", "SYNC" })
    public void byteChannelOptionsFail(StandardOpenOption option) {
        AzureFileSystem fs = createFS(config);

        // Options are validated before path is validated.
        assertThrows(UnsupportedOperationException.class,
            () -> fs.provider().newByteChannel(fs.getPath("foo"), new HashSet<>(Arrays.asList(option))));
    }

    @Test
    public void byteChannelReadNonFileFailRoot() {
        AzureFileSystem fs = createFS(config);

        assertThrows(IllegalArgumentException.class,
            () -> fs.provider().newByteChannel(fs.getPath(getDefaultDir(fs)), null));
    }

    @Test
    public void byteChannelReadFileFailDir() {
        AzureFileSystem fs = createFS(config);
        BlockBlobClient bc
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(generateBlobName()).getBlockBlobClient();
        putDirectoryBlob(bc);

        assertThrows(IOException.class, () -> fs.provider().newByteChannel(fs.getPath(bc.getBlobName()), null));
    }

    @Test
    public void byteChannelReadNonFileFailNoFile() {
        AzureFileSystem fs = createFS(config);

        assertThrows(IOException.class, () -> fs.provider().newByteChannel(fs.getPath("foo"), null));
    }

    @Test
    public void byteChannelFSClosed() throws IOException {
        AzureFileSystem fs = createFS(config);
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName()));
        path.toBlobClient().getBlockBlobClient().upload(DATA.getDefaultBinaryData());

        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> fs.provider().newByteChannel(path, null));
    }

    @Test
    public void byteChannelOptionsCreate() throws IOException {
        AzureFileSystem fs = createFS(config);
        BlockBlobClient bc
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(generateBlobName()).getBlockBlobClient();

        // There are no default options for write as read is the default for channel. We must specify all required.
        SeekableByteChannel nioChannel = fs.provider()
            .newByteChannel(fs.getPath(bc.getBlobName()), new HashSet<>(Arrays.asList(StandardOpenOption.WRITE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)));

        // Create should allow us to create a new file.
        nioChannel.write(DATA.getDefaultData().duplicate());
        nioChannel.close();

        compareInputStreams(bc.openInputStream(), DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        // Explicitly exclude a create option.
        assertThrows(IOException.class,
            () -> fs.provider()
                .newOutputStream(fs.getPath(generateBlobName()), StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING));
    }

    @Test
    public void byteChannelOptionsCreateNew() throws IOException {
        AzureFileSystem fs = createFS(config);
        BlockBlobClient bc
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(generateBlobName()).getBlockBlobClient();

        // Succeed in creating a new file
        SeekableByteChannel nioChannel = fs.provider()
            .newByteChannel(fs.getPath(bc.getBlobName()), new HashSet<>(Arrays.asList(StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)));
        nioChannel.write(DATA.getDefaultData().duplicate());
        nioChannel.close();

        compareInputStreams(bc.openInputStream(), DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        // Fail in overwriting an existing file
        assertThrows(IOException.class,
            () -> fs.provider()
                .newByteChannel(fs.getPath(bc.getBlobName()), new HashSet<>(Arrays.asList(StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))));
    }

    @Test
    public void byteChannelFileAttributes() throws NoSuchAlgorithmException, IOException {
        AzureFileSystem fs = createFS(config);
        BlockBlobClient bc
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(generateBlobName()).getBlockBlobClient();
        byte[] contentMd5 = MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes());
        FileAttribute<?>[] attributes = new FileAttribute<?>[] {
            new TestFileAttribute<>("fizz", "buzz"),
            new TestFileAttribute<>("foo", "bar"),
            new TestFileAttribute<>("Content-Type", "myType"),
            new TestFileAttribute<>("Content-Disposition", "myDisposition"),
            new TestFileAttribute<>("Content-Language", "myLanguage"),
            new TestFileAttribute<>("Content-Encoding", "myEncoding"),
            new TestFileAttribute<>("Cache-Control", "myControl"),
            new TestFileAttribute<>("Content-MD5", contentMd5) };

        SeekableByteChannel nioChannel = fs.provider()
            .newByteChannel(fs.getPath(bc.getBlobName()), new HashSet<>(Arrays.asList(StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)), attributes);
        nioChannel.write(DATA.getDefaultData().duplicate());
        nioChannel.close();
        BlobProperties props = bc.getProperties();

        compareInputStreams(bc.openInputStream(), DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        assertEquals("buzz", props.getMetadata().get("fizz"));
        assertEquals("bar", props.getMetadata().get("foo"));
        assertFalse(props.getMetadata().containsKey("Content-Type"));
        assertFalse(props.getMetadata().containsKey("Content-Disposition"));
        assertFalse(props.getMetadata().containsKey("Content-Language"));
        assertFalse(props.getMetadata().containsKey("Content-Encoding"));
        assertFalse(props.getMetadata().containsKey("Content-MD5"));
        assertFalse(props.getMetadata().containsKey("Cache-Control"));
        assertEquals("myType", props.getContentType());
        assertEquals("myDisposition", props.getContentDisposition());
        assertEquals("myLanguage", props.getContentLanguage());
        assertEquals("myEncoding", props.getContentEncoding());
        assertArraysEqual(contentMd5, props.getContentMd5());
        assertEquals("myControl", props.getCacheControl());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void byteChannelFileAttrNullEmpty(boolean isNull) throws IOException {
        AzureFileSystem fs = createFS(config);
        BlockBlobClient bc
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(generateBlobName()).getBlockBlobClient();
        ByteBuffer data = DATA.getDefaultData().duplicate();

        SeekableByteChannel nioChannel = fs.provider()
            .newByteChannel(
                fs.getPath(bc.getBlobName()), new HashSet<>(Arrays.asList(StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)),
                isNull ? null : new FileAttribute<?>[0]);
        assertDoesNotThrow(() -> nioChannel.write(data));
        assertDoesNotThrow(nioChannel::close);
    }

    @Test
    public void byteChannelWriteOptionsMissingRequired() {
        AzureFileSystem fs = createFS(config);

        // Missing WRITE
        assertThrows(UnsupportedOperationException.class,
            () -> fs.provider()
                .newByteChannel(fs.getPath(generateBlobName()),
                    new HashSet<>(Arrays.asList(StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING))));

        // Missing TRUNCATE_EXISTING and CREATE_NEW
        assertThrows(IllegalArgumentException.class,
            () -> fs.provider()
                .newByteChannel(fs.getPath(generateBlobName()),
                    new HashSet<>(Arrays.asList(StandardOpenOption.WRITE, StandardOpenOption.CREATE))));

        // Missing TRUNCATE_EXISTING
        assertDoesNotThrow(() -> fs.provider()
            .newByteChannel(fs.getPath(generateBlobName()),
                new HashSet<>(Arrays.asList(StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))));

        assertDoesNotThrow(() -> fs.provider()
            .newByteChannel(fs.getPath(generateBlobName()), new HashSet<>(Arrays.asList(StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE))));
    }

    @ParameterizedTest
    @EnumSource(
        value = StandardOpenOption.class,
        names = { "APPEND", "DELETE_ON_CLOSE", "DSYNC", "READ", "SPARSE", "SYNC" })
    public void byteChannelOptionsInvalid(StandardOpenOption option) {
        AzureFileSystem fs = createFS(config);

        assertThrows(UnsupportedOperationException.class,
            () -> fs.provider()
                .newOutputStream(fs.getPath(generateBlobName()), option, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING));
    }

    @LiveOnly
    @ParameterizedTest
    @CsvSource(value = { "60,0", "150,3" })
    public void byteChannelFileSystemConfig(int dataSize, int blockCount) throws IOException {
        config.put(AzureFileSystem.AZURE_STORAGE_UPLOAD_BLOCK_SIZE, 50L);
        config.put(AzureFileSystem.AZURE_STORAGE_PUT_BLOB_THRESHOLD, 100L);
        AzureFileSystem fs = createFS(config);
        BlockBlobClient bc
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(generateBlobName()).getBlockBlobClient();
        SeekableByteChannel nioChannel = fs.provider()
            .newByteChannel(fs.getPath(bc.getBlobName()), new HashSet<>(Arrays.asList(StandardOpenOption.WRITE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)));

        nioChannel.write(getRandomData(dataSize));
        nioChannel.close();

        assertEquals(blockCount, bc.listBlocks(BlockListType.COMMITTED).getCommittedBlocks().size());
    }

    @Test
    public void byteChannelOpenDirectoryFail() {
        AzureFileSystem fs = createFS(config);
        BlockBlobClient bc
            = rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(generateBlobName()).getBlockBlobClient();
        putDirectoryBlob(bc);

        assertThrows(IOException.class,
            () -> fs.provider()
                .newByteChannel(fs.getPath(bc.getBlobName()),
                    new HashSet<>(Arrays.asList(StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))));
    }

    @Test
    public void byteChannelClosedFS() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());

        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> fs.provider().newByteChannel(path, null));
    }

    @Test
    public void checkAccess() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        fs.provider().newOutputStream(path).close();

        assertDoesNotThrow(() -> fs.provider().checkAccess(path));
    }

    @Test
    public void checkAccessRoot() {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(getDefaultDir(fs));

        assertDoesNotThrow(() -> fs.provider().checkAccess(path));
    }

    @ParameterizedTest
    @EnumSource(value = AccessMode.class, names = { "READ", "WRITE", "EXECUTE" })
    public void checkAccessAccessDenied(AccessMode mode) throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        fs.provider().newOutputStream(path).close();

        assertThrows(AccessDeniedException.class, () -> fs.provider().checkAccess(path, mode));
    }

    @Test
    public void checkAccessIOException() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        fs.provider().newOutputStream(path).close();
        fs.close();

        config = initializeConfigMap(new CheckAccessIoExceptionPolicy());
        fs = createFS(config);
        path = fs.getPath(path.toString());

        AzureFileSystem finalFs = fs;
        Path finalPath = path;
        IOException e = assertThrows(IOException.class, () -> finalFs.provider().checkAccess(finalPath));
        assertFalse(e instanceof NoSuchFileException);
    }

    class CheckAccessIoExceptionPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext,
            HttpPipelineNextPolicy httpPipelineNextPolicy) {
            HttpRequest request = httpPipelineCallContext.getHttpRequest();
            // GetProperties call to blob
            if (request.getUrl().getPath().split("/").length == 3 && request.getHttpMethod() == (HttpMethod.HEAD)) {
                return Mono.just(new MockHttpResponse(request, 403,
                    new HttpHeaders().set("x-ms-error-code", BlobErrorCode.AUTHORIZATION_FAILURE.toString())));
            } else {
                return httpPipelineNextPolicy.process();
            }
        }
    }

    @Test
    public void checkAccessNoFile() {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());

        assertThrows(NoSuchFileException.class, () -> fs.provider().checkAccess(path));
    }

    @Test
    public void checkAccessFSClosed() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        fs.provider().newOutputStream(path).close();
        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> fs.provider().checkAccess(path));
    }

    @ParameterizedTest
    @ValueSource(
        classes = { BasicFileAttributeView.class, AzureBasicFileAttributeView.class, AzureBlobFileAttributeView.class })
    public void getAttributeView(Class<? extends BasicFileAttributeView> type) {
        Class<?> expected = type == AzureBlobFileAttributeView.class
            ? AzureBlobFileAttributeView.class
            : BasicFileAttributeView.class;
        AzureFileSystem fs = createFS(config);

        // No path validation is expected for getting the view
        assertInstanceOf(expected, fs.provider().getFileAttributeView(fs.getPath("path"), type));
    }

    @Test
    public void getAttributeViewFail() {
        AzureFileSystem fs = createFS(config);

        // No path validation is expected for getting the view
        assertNull(fs.provider().getFileAttributeView(fs.getPath("path"), DosFileAttributeView.class));
    }

    @ParameterizedTest
    @ValueSource(classes = { BasicFileAttributes.class, AzureBasicFileAttributes.class, AzureBlobFileAttributes.class })
    public void readAttributes(Class<? extends BasicFileAttributes> type) throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        fs.provider().newOutputStream(path).close();

        Class<?> expected = type.equals(AzureBlobFileAttributes.class)
            ? AzureBlobFileAttributes.class
            : AzureBasicFileAttributes.class;

        assertInstanceOf(expected, fs.provider().readAttributes(path, type));
    }

    @Test
    public void readAttributesDirectory() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        putDirectoryBlob(new AzureResource(path).getBlobClient().getBlockBlobClient());

        assertDoesNotThrow(() -> fs.provider().readAttributes(path, BasicFileAttributes.class));
    }

    @Test
    public void readAttributesUnsupported() {
        AzureFileSystem fs = createFS(config);

        assertThrows(UnsupportedOperationException.class,
            () -> fs.provider().readAttributes(fs.getPath("path"), DosFileAttributes.class));
    }

    @Test
    public void readAttributesIOException() {
        AzureFileSystem fs = createFS(config);

        // Path doesn't exist.
        assertThrows(IOException.class,
            () -> fs.provider().readAttributes(fs.getPath("path"), BasicFileAttributes.class));
    }

    @Test
    public void readAttributesFSClosed() throws IOException {
        AzureFileSystem fs = createFS(config);
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName()));
        path.toBlobClient().getBlockBlobClient().upload(DATA.getDefaultBinaryData());

        fs.close();

        assertThrows(ClosedFileSystemException.class,
            () -> fs.provider().readAttributes(path, AzureBasicFileAttributes.class));
    }

    @ParameterizedTest
    @MethodSource("readAttributesStrParsingSupplier")
    public void readAttributesStrParsing(String attrStr, List<String> attrList) throws IOException {
        // This test checks that we correctly parse the attribute string and that all the requested attributes are
        // represented in the return value. We can also just test a subset of attributes for parsing logic.
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        fs.provider().newOutputStream(path).close();

        Map<String, Object> result = fs.provider().readAttributes(path, attrStr);
        for (String attr : attrList) {
            assertTrue(result.containsKey(attr));
        }
        assertEquals(attrList.size(), result.keySet().size());
    }

    private static Stream<Arguments> readAttributesStrParsingSupplier() {
        List<String> basic = Arrays.asList("lastModifiedTime", "creationTime", "isRegularFile", "isDirectory",
            "isVirtualDirectory", "isSymbolicLink", "isOther", "size");
        return Stream.of(Arguments.of("*", basic), Arguments.of("basic:*", basic), Arguments.of("azureBasic:*", basic),
            Arguments.of("azureBlob:*",
                Arrays.asList("lastModifiedTime", "creationTime", "eTag", "blobHttpHeaders", "blobType", "copyId",
                    "copyStatus", "copySource", "copyProgress", "copyCompletionTime", "copyStatusDescription",
                    "isServerEncrypted", "accessTier", "isAccessTierInferred", "archiveStatus", "accessTierChangeTime",
                    "metadata", "isRegularFile", "isDirectory", "isVirtualDirectory", "isSymbolicLink", "isOther",
                    "size")),
            Arguments.of("lastModifiedTime,creationTime", Arrays.asList("lastModifiedTime", "creationTime")),
            Arguments.of("basic:isRegularFile,isDirectory,isVirtualDirectory",
                Arrays.asList("isRegularFile", "isDirectory", "isVirtualDirectory")),
            Arguments.of("azureBasic:size", Collections.singletonList("size")),
            Arguments.of("azureBlob:eTag,blobHttpHeaders,blobType,copyId",
                Arrays.asList("eTag", "blobHttpHeaders", "blobType", "copyId")));
    }

    @Test
    public void readAttributesStrDirectory() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        putDirectoryBlob(new AzureResource(path).getBlobClient().getBlockBlobClient());

        assertDoesNotThrow(() -> fs.provider().readAttributes(path, "creationTime"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "azureBlob:size:foo", "", "azureBasic:foo" })
    public void readAttributesStrIA(String attrStr) {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());

        assertThrows(IllegalArgumentException.class, () -> fs.provider().readAttributes(path, attrStr));
    }

    @Test
    public void readAttributesStrInvalidView() {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());

        assertThrows(UnsupportedOperationException.class, () -> fs.provider().readAttributes(path, "foo:size"));
    }

    @Test
    public void readAttributesStrIOException() {
        AzureFileSystem fs = createFS(config);

        // Path doesn't exist
        assertThrows(IOException.class, () -> fs.provider().readAttributes(fs.getPath("path"), "basic:creationTime"));
    }

    @Test
    public void readAtrributesStrClosedFS() throws IOException {
        AzureFileSystem fs = createFS(config);
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName()));
        BlockBlobClient blobClient = path.toBlobClient().getBlockBlobClient();
        blobClient.upload(DATA.getDefaultBinaryData());

        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> fs.provider().readAttributes(path, "basic:*"));
    }

    @ParameterizedTest
    @MethodSource("setAttributesHeadersSupplier")
    public void setAttributesHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, byte[] contentMD5, String contentType) throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        fs.provider().newOutputStream(path).close();
        BlobHttpHeaders headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        fs.provider().setAttribute(path, "azureBlob:blobHttpHeaders", headers);
        headers = fs.provider().readAttributes(path, AzureBlobFileAttributes.class).blobHttpHeaders();

        assertEquals(cacheControl, headers.getCacheControl());
        assertEquals(contentDisposition, headers.getContentDisposition());
        assertEquals(contentEncoding, headers.getContentEncoding());
        assertEquals(contentLanguage, headers.getContentLanguage());
        assertArraysEqual(contentMD5, headers.getContentMd5());
        assertEquals(contentType, headers.getContentType());
    }

    private static Stream<Arguments> setAttributesHeadersSupplier() throws NoSuchAlgorithmException {
        return Stream.of(Arguments.of(null, null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language",
                Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes())), "type"));
    }

    @ParameterizedTest
    @CsvSource(value = { "null,null,null,null,200", "foo,bar,fizz,buzz,200", "i0,a,i_,a,200" }, nullValues = "null")
    public void setAttributesMetadata(String key1, String value1, String key2, String value2) throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        OutputStream os = fs.provider().newOutputStream(path);
        os.close();

        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }

        fs.provider().setAttribute(path, "azureBlob:metadata", metadata);

        assertEquals(metadata, fs.provider().readAttributes(path, AzureBlobFileAttributes.class).metadata());
    }

    @ParameterizedTest
    @MethodSource("setAttributesTierSupplier")
    public void setAttributesTier(AccessTier tier) throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        OutputStream os = fs.provider().newOutputStream(path);
        os.close();

        fs.provider().setAttribute(path, "azureBlob:tier", tier);

        assertEquals(tier, fs.provider().readAttributes(path, AzureBlobFileAttributes.class).accessTier());
    }

    private static Stream<AccessTier> setAttributesTierSupplier() {
        return Stream.of(AccessTier.HOT, AccessTier.COOL);
    }

    @Test
    public void setAttributesDirectory() throws IOException {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());
        putDirectoryBlob(new AzureResource(path).getBlobClient().getBlockBlobClient());

        assertDoesNotThrow(() -> fs.provider().setAttribute(path, "azureBlob:tier", AccessTier.COOL));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "azureBlob:metadata:foo", // Invalid format
            "", // empty
            "azureBasic:foo" // Invalid property
        })
    public void setAttribuesIA(String attrStr) {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());

        assertThrows(IllegalArgumentException.class, () -> fs.provider().setAttribute(path, attrStr, "Foo"));
    }

    @Test
    public void setAttributesInvalidView() {
        AzureFileSystem fs = createFS(config);
        Path path = fs.getPath(generateBlobName());

        assertThrows(UnsupportedOperationException.class, () -> fs.provider().setAttribute(path, "foo:size", "foo"));
    }

    @Test
    public void setAttributesIOException() {
        AzureFileSystem fs = createFS(config);

        // Path does not exist
        // Covers virtual directory, too
        assertThrows(IOException.class,
            () -> fs.provider().setAttribute(fs.getPath("path"), "azureBlob:metadata", Collections.emptyMap()));
    }

    @Test
    public void setAttributesFSClosed() throws IOException {
        AzureFileSystem fs = createFS(config);
        AzurePath path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), generateBlobName()));
        BlockBlobClient blobClient = path.toBlobClient().getBlockBlobClient();
        blobClient.upload(DATA.getDefaultBinaryData());

        fs.close();

        assertThrows(ClosedFileSystemException.class,
            () -> fs.provider().setAttribute(path, "azureBlob:blobHttpHeaders", new BlobHttpHeaders()));
    }

    private void basicSetupForCopyTest(FileSystem fs) {
        // Generate resource names.
        // Don't use default directory to ensure we honor the root.
        String rootName = getNonDefaultRootDir(fs);
        sourcePath = (AzurePath) fs.getPath(rootName, generateBlobName());
        destPath = (AzurePath) fs.getPath(rootName, generateBlobName());

        // Generate clients to resources.
        try {
            sourceClient = sourcePath.toBlobClient();
            destinationClient = destPath.toBlobClient();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
