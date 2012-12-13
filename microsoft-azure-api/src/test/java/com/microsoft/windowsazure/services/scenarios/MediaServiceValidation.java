/**
 * Copyright 2012 Microsoft Corporation
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

package com.microsoft.windowsazure.services.scenarios;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetFile;
import com.microsoft.windowsazure.services.media.models.AssetFileInfo;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.AssetState;
import com.microsoft.windowsazure.services.media.models.AssetOption;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Task;

class MediaServiceValidation {
    private final MediaContract service;

    public MediaServiceValidation(MediaContract service) {
        this.service = service;
    }

    public void validateAsset(AssetInfo asset, String name, AssetOption encryption) throws ServiceException {
        // Check the asset state.
        assertNotNull("asset", asset);
        assertNotNull("asset.getId", asset.getId());
        assertFalse("asset.getId != ''", "".equals(asset.getId()));
        assertEquals("asset.state", AssetState.Initialized, asset.getState());
        assertEquals("asset.getOptions", encryption, asset.getOptions());

        // Verify no files by default.
        List<AssetFileInfo> initialFiles = service.list(AssetFile.list(asset.getId()));
        assertNotNull("initialFiles", initialFiles);
        assertEquals("initialFiles.size", 0, initialFiles.size());

        // Reload asset from server for ID
        AssetInfo reloadedAsset = service.get(Asset.get(asset.getId()));

        // Verify names match
        assertNotNull("reloadedAsset", reloadedAsset);
        assertNotNull("reloadedAsset.getId", reloadedAsset.getId());
        assertEquals("reloadedAsset.getId, asset.getId", asset.getId(), reloadedAsset.getId());
        assertEquals("reloadedAsset.state", AssetState.Initialized, reloadedAsset.getState());
        assertEquals("reloadedAsset.getOptions", encryption, reloadedAsset.getOptions());
    }

    public void validateAssetSortedPages(List<ListResult<AssetInfo>> pages, List<String> assetNames, int pageSize) {
        int sumSizeOfPages = 0;
        List<String> actualAssetNames = new ArrayList<String>();

        for (ListResult<AssetInfo> page : pages) {
            sumSizeOfPages += page.size();
            for (AssetInfo asset : page) {
                actualAssetNames.add(asset.getName());
            }
        }

        assertEquals("sumSizeOfPages", assetNames.size(), sumSizeOfPages);
        assertEquals("size of last page", 0, pages.get(pages.size() - 1).size());

        // Do not worry about comparing the details of the sorted pages,
        // because Media Services splits on the internal order, then sorts
        // each page individually.
    }

    public void validateAssetFiles(AssetInfo asset, Hashtable<String, InputStream> inputFiles) throws ServiceException,
            IOException, NoSuchAlgorithmException {
        List<AssetFileInfo> assetFiles = service.list(AssetFile.list(asset.getId()));

        assertNotNull("assetFiles", assetFiles);
        assertEquals("assetFiles.size", inputFiles.size(), assetFiles.size());

        // More general verifications:
        // * Verify that the asset count on the server increments for each asset added.
        // * Verify that the file count on the server increments for each file added.
        // * Verify that can query the server for assets matching
        //   * The created asset ID, and get only that one item
        //   * The created asset name, get only that one.
        // * If Encrypted, verify file and content key
        // * Get the asset encryption info and compare with the file's encryption info
        // * Compare these properties: IsEncrypted, InitializationVector, EncryptionKeyId, EncryptionScheme, EncryptionVersion

        // Compare the asset files with all files
        List<AssetFileInfo> allFiles = service.list(AssetFile.list());
        for (AssetFileInfo assetFile : assetFiles) {
            assertEquals("fi.getParentAssetId", asset.getId(), assetFile.getParentAssetId());
            AssetFileInfo match = null;
            for (AssetFileInfo aFile : allFiles) {
                if (aFile.getId().equals(assetFile.getId())) {
                    match = aFile;
                    break;
                }
            }

            assertFileInfosEqual("match from all files", assetFile, match);
        }
    }

    public void validateAssetFileUrls(List<URL> fileUrls, Hashtable<String, InputStream> inputFiles)
            throws IOException, InterruptedException {
        assertEquals("fileUrls count", inputFiles.size(), fileUrls.size());
        for (URL file : fileUrls) {
            InputStream expected = inputFiles.get(inputFiles.keySet().toArray()[0]);
            InputStream actual = getInputStreamWithRetry(file);
            assertStreamsEqual(expected, actual);
        }
    }

    public void validateJob(JobInfo job, String name, AssetInfo asset, List<Task.CreateBatchOperation> createTasks) {
        assertDateApproxEquals("getEndTime", new Date(), job.getCreated());
        assertEquals("job.getName", name, job.getName());

        // TODO: Uncomment when fixed:
        // https://github.com/WindowsAzure/azure-sdk-for-java-pr/issues/508
        //        List<String> inputAssets = job.getInputMediaAssets();
        //        assertNotNull("inputAssets", inputAssets);
        //        assertEquals("inputAssets.size()", 1, inputAssets.size());
        //        assertEquals("inputAssets.get(0)", asset.getId(), inputAssets.get(0));
        //
        //        List<String> outputAssets = job.getOutputMediaAssets();
        //        assertNotNull("outputAssets", outputAssets);
        //        assertEquals("outputAssets.size()", createTasks.size(), outputAssets.size());
    }

    public void validateOutputAssets(List<AssetInfo> outputAssets) throws ServiceException {
        // TODO: Uncomment when fixed:
        // https://github.com/WindowsAzure/azure-sdk-for-java-pr/issues/508

        //        assertNotNull("outputAssets", outputAssets);
        //        for (AssetInfo asset : outputAssets) {
        //            this.validateAsset(asset, null, null);
        //        }
    }

    // This method is needed because there can be a delay before a new read locator
    // is applied for the asset files. 
    private InputStream getInputStreamWithRetry(URL file) throws InterruptedException, IOException {
        InputStream reader = null;
        for (int counter = 0; true; counter++) {
            try {
                reader = file.openConnection().getInputStream();
                break;
            }
            catch (IOException e) {
                System.out.println("Got error, wait a bit and try again");
                if (counter < 6) {
                    Thread.sleep(10000);
                }
                else {
                    // No more retries. 
                    throw e;
                }
            }
        }

        return reader;
    }

    public void assertFileInfosEqual(String message, AssetFileInfo fi, AssetFileInfo match) {
        assertNotNull(message + ":fi", fi);
        assertNotNull(message + ":match", match);
        assertEquals(message + ":getContentChecksum", fi.getContentChecksum(), match.getContentChecksum());
        assertEquals(message + ":getContentFileSize", fi.getContentFileSize(), match.getContentFileSize());
        assertEquals(message + ":getCreated", fi.getCreated(), match.getCreated());
        assertEquals(message + ":getEncryptionKeyId", fi.getEncryptionKeyId(), match.getEncryptionKeyId());
        assertEquals(message + ":getEncryptionScheme", fi.getEncryptionScheme(), match.getEncryptionScheme());
        assertEquals(message + ":getEncryptionVersion", fi.getEncryptionVersion(), match.getEncryptionVersion());
        assertEquals(message + ":getId", fi.getId(), match.getId());
        assertEquals(message + ":getIsEncrypted", fi.getIsEncrypted(), match.getIsEncrypted());
        assertEquals(message + ":getIsPrimary", fi.getIsPrimary(), match.getIsPrimary());
        assertEquals(message + ":getLastModified", fi.getLastModified(), match.getLastModified());
        assertEquals(message + ":getMimeType", fi.getMimeType(), match.getMimeType());
        assertEquals(message + ":getName", fi.getName(), match.getName());
        assertEquals(message + ":getParentAssetId", fi.getParentAssetId(), match.getParentAssetId());
    }

    protected void assertDateApproxEquals(String message, Date expected, Date actual) {
        // Default allows for a 30 seconds difference in dates, for clock skew, network delays, etc.
        long deltaInMilliseconds = 30000;

        if (expected == null || actual == null) {
            assertEquals(message, expected, actual);
        }
        else {
            long diffInMilliseconds = Math.abs(expected.getTime() - actual.getTime());

            if (diffInMilliseconds > deltaInMilliseconds) {
                assertEquals(message, expected, actual);
            }
        }
    }

    private void assertStreamsEqual(InputStream inputStream1, InputStream inputStream2) throws IOException {
        byte[] buffer1 = new byte[1024];
        byte[] buffer2 = new byte[1024];
        try {
            while (true) {
                int n1 = inputStream1.read(buffer1);
                int n2 = inputStream2.read(buffer2);
                assertEquals("number of bytes read from streams", n1, n2);
                if (n1 == -1) {
                    break;
                }
                for (int i = 0; i < n1; i++) {
                    assertEquals("byte " + i + " read from streams", buffer1[i], buffer2[i]);
                }
            }
        }
        finally {
            inputStream1.close();
            inputStream2.close();
        }
    }
}
