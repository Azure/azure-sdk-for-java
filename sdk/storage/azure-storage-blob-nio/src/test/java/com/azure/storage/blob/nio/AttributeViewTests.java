// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AttributeViewTests extends BlobNioTestBase {
    // Get attributes--All properties set;
    private BlobClient bc;
    private AzureFileSystem fs;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        fs = createFS(initializeConfigMap());
        cc = rootNameToContainerClient(getDefaultDir(fs));
        bc = cc.getBlobClient(generateBlobName());
        bc.upload(DATA.getDefaultBinaryData());
    }

    @Test
    public void azureBasicFileAttributeViewReadAttributes() throws IOException {
        AzureBasicFileAttributes attr = new AzureBasicFileAttributeView(fs.getPath(bc.getBlobName())).readAttributes();
        BlobProperties props = bc.getProperties();

        assertEquals(attr.size(), props.getBlobSize());
        assertEquals(attr.lastModifiedTime(), FileTime.from(props.getLastModified().toInstant()));
        assertEquals(attr.creationTime(), FileTime.from(props.getCreationTime().toInstant()));
        assertTrue(attr.isRegularFile());
        assertEquals(attr.fileKey(), bc.getBlobUrl());
        assertFalse(attr.isDirectory());
        assertFalse(attr.isVirtualDirectory());
        assertFalse(attr.isSymbolicLink());
        assertFalse(attr.isOther());
    }

    @Test
    public void azureBasicFileAttributeViewDirectory() throws IOException {
        Path path = fs.getPath(generateBlobName());
        putDirectoryBlob(new AzureResource(path).getBlobClient().getBlockBlobClient());
        AzureBasicFileAttributes attr = new AzureBasicFileAttributeView(path).readAttributes();

        assertTrue(attr.isDirectory());
        assertFalse(attr.isVirtualDirectory());
        assertFalse(attr.isRegularFile());
        assertFalse(attr.isOther());
        assertFalse(attr.isSymbolicLink());
    }

    @Test
    public void azureBasicFileAttributeViewDirectoryVirtual() throws IOException {
        String dirName = generateBlobName();
        BlobClient bc = cc.getBlobClient(dirName + '/' + generateContainerName());
        bc.upload(DATA.getDefaultBinaryData());
        AzureBasicFileAttributes attr = new AzureBasicFileAttributeView(fs.getPath(dirName)).readAttributes();

        assertTrue(attr.isDirectory());
        assertTrue(attr.isVirtualDirectory());
        assertFalse(attr.isRegularFile());
        assertFalse(attr.isOther());
        assertFalse(attr.isSymbolicLink());
    }

    @Test
    public void azureBasicFileAttributeViewNoExist() {
        assertThrows(IOException.class,
            () -> new AzureBasicFileAttributeView(fs.getPath(generateBlobName())).readAttributes());
    }

    @Test
    public void azureBasicFileAttributeViewFSClosed() throws IOException {
        Path path = fs.getPath(generateBlobName());
        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> new AzureBasicFileAttributeView(path).readAttributes());
    }

    @Test
    public void azureBlobFileAttributeViewReadAttributes() throws IOException {
        AzureBlobFileAttributes attr = new AzureBlobFileAttributeView(fs.getPath(bc.getBlobName())).readAttributes();
        Map<String, Supplier<Object>> suppliers = AzureBlobFileAttributes.getAttributeSuppliers(attr);
        BlobProperties props = bc.getProperties();

        // getters
        assertEquals(attr.size(), props.getBlobSize());
        assertEquals(attr.lastModifiedTime(), FileTime.from(props.getLastModified().toInstant()));
        assertEquals(attr.creationTime(), FileTime.from(props.getCreationTime().toInstant()));
        assertTrue(attr.isRegularFile());
        assertEquals(attr.fileKey(), bc.getBlobUrl());
        assertFalse(attr.isDirectory());
        assertFalse(attr.isVirtualDirectory());
        assertFalse(attr.isSymbolicLink());
        assertFalse(attr.isOther());
        assertEquals(attr.eTag(), props.getETag());
        assertEquals(attr.blobHttpHeaders().getContentType(), props.getContentType());
        assertArraysEqual(attr.blobHttpHeaders().getContentMd5(), props.getContentMd5());
        assertEquals(attr.blobHttpHeaders().getContentLanguage(), props.getContentLanguage());
        assertEquals(attr.blobHttpHeaders().getContentEncoding(), props.getContentEncoding());
        assertEquals(attr.blobHttpHeaders().getContentDisposition(), props.getContentDisposition());
        assertEquals(attr.blobHttpHeaders().getCacheControl(), props.getCacheControl());
        assertEquals(attr.blobType(), props.getBlobType());
        assertEquals(attr.copyId(), props.getCopyId());
        assertEquals(attr.copyStatus(), props.getCopyStatus());
        assertEquals(attr.copySource(), props.getCopySource());
        assertEquals(attr.copyProgress(), props.getCopyProgress());
        assertEquals(attr.copyCompletionTime(), props.getCopyCompletionTime());
        assertEquals(attr.copyStatusDescription(), props.getCopyStatusDescription());
        assertEquals(attr.isServerEncrypted(), props.isServerEncrypted());
        assertEquals(attr.accessTier(), props.getAccessTier());
        assertEquals(attr.isAccessTierInferred(), props.isAccessTierInferred());
        assertEquals(attr.archiveStatus(), props.getArchiveStatus());
        assertEquals(attr.accessTierChangeTime(), props.getAccessTierChangeTime());
        assertEquals(attr.metadata(), props.getMetadata());

        // Suppliers, used in FileSystemProvider.readAttributes(String). Unlike the consumers used for setting
        // properties, we test these here rather than on the FileSystemProvider because there are so many of them and
        // it's more feasible this way rather than having a test for each method like the consumers.
        assertEquals(suppliers.get("size").get(), props.getBlobSize());
        assertEquals(suppliers.get("lastModifiedTime").get(), FileTime.from(props.getLastModified().toInstant()));
        assertEquals(suppliers.get("creationTime").get(), FileTime.from(props.getCreationTime().toInstant()));
        assertEquals(suppliers.get("eTag").get(), props.getETag());
        BlobHttpHeaders supplierHeaders = (BlobHttpHeaders) suppliers.get("blobHttpHeaders").get();
        assertEquals(supplierHeaders.getContentType(), props.getContentType());
        assertArraysEqual(supplierHeaders.getContentMd5(), props.getContentMd5());
        assertEquals(supplierHeaders.getContentLanguage(), props.getContentLanguage());
        assertEquals(supplierHeaders.getContentEncoding(), props.getContentEncoding());
        assertEquals(supplierHeaders.getContentDisposition(), props.getContentDisposition());
        assertEquals(supplierHeaders.getCacheControl(), props.getCacheControl());
        assertEquals(suppliers.get("blobType").get(), props.getBlobType());
        assertEquals(suppliers.get("copyId").get(), props.getCopyId());
        assertEquals(suppliers.get("copyStatus").get(), props.getCopyStatus());
        assertEquals(suppliers.get("copySource").get(), props.getCopySource());
        assertEquals(suppliers.get("copyProgress").get(), props.getCopyProgress());
        assertEquals(suppliers.get("copyCompletionTime").get(), props.getCopyCompletionTime());
        assertEquals(suppliers.get("copyStatusDescription").get(), props.getCopyStatusDescription());
        assertEquals(suppliers.get("isServerEncrypted").get(), props.isServerEncrypted());
        assertEquals(suppliers.get("accessTier").get(), props.getAccessTier());
        assertEquals(suppliers.get("isAccessTierInferred").get(), props.isAccessTierInferred());
        assertEquals(suppliers.get("archiveStatus").get(), props.getArchiveStatus());
        assertEquals(suppliers.get("accessTierChangeTime").get(), props.getAccessTierChangeTime());
        assertEquals(suppliers.get("metadata").get(), props.getMetadata());
    }

    @Test
    public void azureBlobFileAttributeViewReadFSClosed() throws IOException {
        Path path = fs.getPath(generateBlobName());
        fs.close();

        assertThrows(ClosedFileSystemException.class, () -> new AzureBlobFileAttributeView(path).readAttributes());
    }

    @ParameterizedTest
    @MethodSource("azureBlobFileAttributeViewSetBlobHttpHeadersSupplier")
    public void azureBlobFileAttributeViewSetBlobHttpHeaders(String cacheControl, String contentDisposition,
        String contentEncoding, String contentLanguage, byte[] contentMD5, String contentType) throws IOException {
        AzureBlobFileAttributeView view = new AzureBlobFileAttributeView(fs.getPath(bc.getBlobName()));
        BlobHttpHeaders headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        view.setBlobHttpHeaders(headers);
        BlobProperties response = bc.getProperties();

        assertEquals(cacheControl, response.getCacheControl());
        assertEquals(contentDisposition, response.getContentDisposition());
        assertEquals(contentEncoding, response.getContentEncoding());
        assertEquals(contentLanguage, response.getContentLanguage());
        assertArraysEqual(contentMD5, response.getContentMd5());
        assertEquals(contentType, response.getContentType());
    }

    private static Stream<Arguments> azureBlobFileAttributeViewSetBlobHttpHeadersSupplier()
        throws NoSuchAlgorithmException {
        return Stream.of(Arguments.of(null, null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language",
                Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes())), "typr"));
    }

    @Test
    public void azureBlobFileAttributeViewSetHeadersFSClosed() throws IOException {
        Path path = fs.getPath(generateBlobName());
        fs.close();

        assertThrows(ClosedFileSystemException.class,
            () -> new AzureBlobFileAttributeView(path).setBlobHttpHeaders(new BlobHttpHeaders()));
    }

    @ParameterizedTest
    @CsvSource(
        value = { "null,null,null,null", "foo,bar,fizz,buzz", "i0,a,i_,a" /* Test culture sensitive word sort */ },
        nullValues = "null")
    public void azureBlobFileAttributeViewSetMetadata(String key1, String value1, String key2, String value2)
        throws IOException {
        AzureBlobFileAttributeView view = new AzureBlobFileAttributeView(fs.getPath(bc.getBlobName()));
        Map<String, String> metadata = new HashMap<String, String>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }

        view.setMetadata(metadata);

        assertEquals(metadata, bc.getProperties().getMetadata());
    }

    @Test
    public void azureBlobFileAttributeViewSetMetadataFSClosed() throws IOException {
        Path path = fs.getPath(generateBlobName());
        fs.close();

        assertThrows(ClosedFileSystemException.class,
            () -> new AzureBlobFileAttributeView(path).setMetadata(Collections.emptyMap()));
    }

    @ParameterizedTest
    @MethodSource("azureBlobFileAttributeViewSetTierSupplier")
    public void azureBlobFileAttributeViewSetTier(AccessTier tier) throws IOException {
        new AzureBlobFileAttributeView(fs.getPath(bc.getBlobName())).setTier(tier);

        assertEquals(tier, bc.getProperties().getAccessTier());
    }

    private static Stream<AccessTier> azureBlobFileAttributeViewSetTierSupplier() {
        // We don't test archive because it takes a while to take effect and testing HOT and COOL demonstrates that the
        // tier is successfully being passed to the underlying client.
        return Stream.of(AccessTier.HOT, AccessTier.COOL);
    }

    @Test
    public void azureBlobFileAttributeViewSetTierFSClosed() throws IOException {
        Path path = fs.getPath(generateBlobName());
        fs.close();

        assertThrows(ClosedFileSystemException.class,
            () -> new AzureBlobFileAttributeView(path).setTier(AccessTier.HOT));
    }

    @ParameterizedTest
    @MethodSource("attributeViewSetTimesUnsupportedSupplier")
    public void attributeViewSetTimesUnsupported(FileTime t1, FileTime t2, FileTime t3) {
        Path path = fs.getPath(bc.getBlobName());
        AzureBlobFileAttributeView blobView = new AzureBlobFileAttributeView(path);
        AzureBasicFileAttributeView basicView = new AzureBasicFileAttributeView(path);

        assertThrows(UnsupportedOperationException.class, () -> blobView.setTimes(t1, t2, t3));
        assertThrows(UnsupportedOperationException.class, () -> basicView.setTimes(t1, t2, t3));
    }

    private static Stream<Arguments> attributeViewSetTimesUnsupportedSupplier() {
        return Stream.of(Arguments.of(FileTime.fromMillis(System.currentTimeMillis()), null, null),
            Arguments.of(null, FileTime.fromMillis(System.currentTimeMillis()), null),
            Arguments.of(null, null, FileTime.fromMillis(System.currentTimeMillis())));
    }
}
