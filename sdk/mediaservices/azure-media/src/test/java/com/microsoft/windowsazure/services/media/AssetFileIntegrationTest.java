/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.AccessPolicy;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetFile;
import com.microsoft.windowsazure.services.media.models.AssetFileInfo;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;

public class AssetFileIntegrationTest extends IntegrationTestBase {

    // Some dummy binary data for uploading
    private static byte[] firstPrimes = new byte[] { 2, 3, 5, 7, 11, 13, 17,
            19, 23, 29 };
    private static byte[] onesAndZeros = new byte[] { 1, 0, 1, 0, 1, 0, 1, 0 };
    private static byte[] countingUp = new byte[] { 3, 4, 5, 6, 7, 8, 9, 10, 11 };

    private static final String BLOB_NAME = "primes.bin";
    private static final String BLOB_NAME_2 = "primes2.bin";

    private static AccessPolicyInfo writePolicy;

    @BeforeClass
    public static void setup() throws Exception {
        IntegrationTestBase.setup();

        writePolicy = createWritePolicy("uploadWritePolicy", 30);
    }

    @Test
    public void canCreateFileForUploadedBlob() throws Exception {
        AssetInfo asset = createTestAsset("createFileForUploadedBlob");
        LocatorInfo locator = createLocator(writePolicy, asset, 5);
        WritableBlobContainerContract blobWriter = service
                .createBlobWriter(locator);

        createAndUploadBlob(blobWriter, BLOB_NAME, firstPrimes);

        service.action(AssetFile.createFileInfos(asset.getId()));

        ListResult<AssetFileInfo> files = service.list(AssetFile.list(asset
                .getAssetFilesLink()));

        assertEquals(1, files.size());
        AssetFileInfo file = files.get(0);
        assertEquals(BLOB_NAME, file.getName());
    }

    @Test
    public void canCreateFileEntityDirectly() throws Exception {
        AssetInfo asset = createTestAsset("createFileEntityDirectly");
        LocatorInfo locator = createLocator(writePolicy, asset, 5);
        WritableBlobContainerContract blobWriter = service
                .createBlobWriter(locator);

        createAndUploadBlob(blobWriter, BLOB_NAME_2, firstPrimes);

        service.create(AssetFile.create(asset.getId(), BLOB_NAME_2));

        ListResult<AssetFileInfo> files = service.list(AssetFile.list(asset
                .getAssetFilesLink()));

        boolean found = false;
        for (AssetFileInfo file : files) {
            if (file.getName().equals(BLOB_NAME_2)) {
                found = true;
                break;
            }
        }

        assertTrue(found);
    }

    @Test
    public void canCreateAssetWithMultipleFiles() throws Exception {
        AssetInfo asset = createTestAsset("createWithMultipleFiles");
        AccessPolicyInfo policy = createWritePolicy("createWithMultipleFiles",
                10);
        LocatorInfo locator = createLocator(policy, asset, 5);

        WritableBlobContainerContract blobWriter = service
                .createBlobWriter(locator);

        createAndUploadBlob(blobWriter, "blob1.bin", firstPrimes);
        createAndUploadBlob(blobWriter, "blob2.bin", onesAndZeros);
        createAndUploadBlob(blobWriter, "blob3.bin", countingUp);

        AssetFileInfo file1 = service.create(AssetFile
                .create(asset.getId(), "blob1.bin").setIsPrimary(true)
                .setIsEncrypted(false)
                .setContentFileSize(new Long(firstPrimes.length)));

        AssetFileInfo file2 = service.create(AssetFile
                .create(asset.getId(), "blob2.bin").setIsPrimary(false)
                .setIsEncrypted(false)
                .setContentFileSize(new Long(onesAndZeros.length)));

        AssetFileInfo file3 = service.create(AssetFile
                .create(asset.getId(), "blob3.bin").setIsPrimary(false)
                .setIsEncrypted(false)
                .setContentFileSize(new Long(countingUp.length))
                .setContentChecksum("1234"));

        ListResult<AssetFileInfo> files = service.list(AssetFile.list(asset
                .getAssetFilesLink()));

        assertEquals(3, files.size());

        ArrayList<AssetFileInfo> results = new ArrayList<AssetFileInfo>(files);
        Collections.sort(results, new Comparator<AssetFileInfo>() {
            @Override
            public int compare(AssetFileInfo o1, AssetFileInfo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        assertAssetFileInfoEquals("results.get(0)", file1, results.get(0));
        assertAssetFileInfoEquals("results.get(1)", file2, results.get(1));
        assertAssetFileInfoEquals("results.get(2)", file3, results.get(2));
    }

    @Test
    public void canCreateFileAndThenUpdateIt() throws Exception {
        AssetInfo asset = createTestAsset("createAndUpdate");
        AccessPolicyInfo policy = createWritePolicy("createAndUpdate", 10);
        LocatorInfo locator = createLocator(policy, asset, 5);
        WritableBlobContainerContract blobWriter = service
                .createBlobWriter(locator);

        createAndUploadBlob(blobWriter, "toUpdate.bin", firstPrimes);

        AssetFileInfo file = service.create(AssetFile.create(asset.getId(),
                "toUpdate.bin"));

        service.update(AssetFile.update(file.getId()).setMimeType(
                "application/octet-stream"));

        AssetFileInfo fromServer = service.get(AssetFile.get(file.getId()));

        assertEquals("application/octet-stream", fromServer.getMimeType());
    }

    @Test
    public void canDeleteFileFromAsset() throws Exception {
        AssetInfo asset = createTestAsset("deleteFile");
        AccessPolicyInfo policy = createWritePolicy("deleteFile", 10);
        LocatorInfo locator = createLocator(policy, asset, 5);
        WritableBlobContainerContract blobWriter = service
                .createBlobWriter(locator);

        createAndUploadBlob(blobWriter, "todelete.bin", firstPrimes);
        createAndUploadBlob(blobWriter, "tokeep.bin", onesAndZeros);

        service.action(AssetFile.createFileInfos(asset.getId()));

        ListResult<AssetFileInfo> originalFiles = service.list(AssetFile
                .list(asset.getAssetFilesLink()));
        assertEquals(2, originalFiles.size());

        for (AssetFileInfo file : originalFiles) {
            if (file.getName().equals("todelete.bin")) {
                service.delete(AssetFile.delete(file.getId()));
                break;
            }
        }

        ListResult<AssetFileInfo> newFiles = service.list(AssetFile.list(asset
                .getAssetFilesLink()));
        assertEquals(1, newFiles.size());
        assertEquals("tokeep.bin", newFiles.get(0).getName());
    }

    //
    // Helper functions to create various media services entities
    //
    private static AssetInfo createTestAsset(String name)
            throws ServiceException {
        return service.create(Asset.create().setName(testAssetPrefix + name));
    }

    private static AccessPolicyInfo createWritePolicy(String name,
            int durationInMinutes) throws ServiceException {
        return service.create(AccessPolicy.create(testPolicyPrefix + name,
                durationInMinutes, EnumSet.of(AccessPolicyPermission.WRITE)));
    }

    private static void createAndUploadBlob(
            WritableBlobContainerContract blobWriter, String blobName,
            byte[] data) throws ServiceException {
        InputStream blobContent = new ByteArrayInputStream(data);
        blobWriter.createBlockBlob(blobName, blobContent);
    }

    //
    // Assertion helpers
    //

    private void assertAssetFileInfoEquals(String message,
            AssetFileInfo expected, AssetFileInfo actual) {
        verifyAssetInfoProperties(message, expected.getId(),
                expected.getName(), expected.getParentAssetId(),
                expected.getIsPrimary(), expected.getIsEncrypted(),
                expected.getEncryptionKeyId(), expected.getEncryptionScheme(),
                expected.getEncryptionVersion(),
                expected.getInitializationVector(), expected.getCreated(),
                expected.getLastModified(), expected.getContentChecksum(),
                expected.getMimeType(), actual);
    }

    private void verifyAssetInfoProperties(String message, String id,
            String name, String parentAssetId, boolean isPrimary,
            boolean isEncrypted, String encryptionKeyId,
            String encryptionScheme, String encryptionVersion,
            String initializationVector, Date created, Date lastModified,
            String contentChecksum, String mimeType, AssetFileInfo assetFile) {
        assertNotNull(message, assetFile);

        assertEquals(message + ".getId", id, assetFile.getId());
        assertEquals(message + ".getName", name, assetFile.getName());
        assertEquals(message + ".getParentAssetId", parentAssetId,
                assetFile.getParentAssetId());
        assertEquals(message + ".getIsPrimary", isPrimary,
                assetFile.getIsPrimary());

        assertEquals(message + ".getIsEncrypted", isEncrypted,
                assetFile.getIsEncrypted());
        assertEquals(message + ".getEncryptionKeyId", encryptionKeyId,
                assetFile.getEncryptionKeyId());
        assertEquals(message + ".getEncryptionScheme", encryptionScheme,
                assetFile.getEncryptionScheme());
        assertEquals(message + ".getEncryptionVersion", encryptionVersion,
                assetFile.getEncryptionVersion());
        assertEquals(message + ".getInitializationVector",
                initializationVector, assetFile.getInitializationVector());

        assertDateApproxEquals(message + ".getCreated", created,
                assetFile.getCreated());
        assertDateApproxEquals(message + ".getLastModified", lastModified,
                assetFile.getLastModified());
        assertEquals(message + ".getContentChecksum", contentChecksum,
                assetFile.getContentChecksum());
        assertEquals(message + ".getMimeType", mimeType,
                assetFile.getMimeType());
    }
}
