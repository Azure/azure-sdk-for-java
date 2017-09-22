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

package com.microsoft.windowsazure.services.scenarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.AssetOption;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Task;
import com.microsoft.windowsazure.services.scenarios.MediaServiceWrapper.EncoderType;

public class MediaServiceScenarioTest extends ScenarioTestBase {
    private static final String rootTestAssetPrefix = "testAssetPrefix-";
    private static final String testJobPrefix = "testJobPrefix";
    private static String testAssetPrefix;
    private static MediaServiceWrapper wrapper;
    private static MediaServiceValidation validator;

    @BeforeClass
    public static void setup() throws ServiceException {
        ScenarioTestBase.initializeConfig();
        MediaContract service = MediaService.create(config);
        wrapper = new MediaServiceWrapper(service);
        validator = new MediaServiceValidation(service);
        testAssetPrefix = rootTestAssetPrefix + UUID.randomUUID() + "-";
    }

    @AfterClass
    public static void cleanup() throws ServiceException {
        wrapper.removeAllAssetsWithPrefix(rootTestAssetPrefix);
        wrapper.removeAllAccessPoliciesWithPrefix();
        wrapper.removeAllJobWithPrefix(testJobPrefix);
        ScenarioTestBase.cleanupConfig();
    }

    @Test
    public void newAsset() throws Exception {
        AssetInfo asset = wrapper.createAsset(testAssetPrefix + "newAsset",
                AssetOption.None);
        validator.validateAsset(asset, testAssetPrefix + "newAsset",
                AssetOption.None);
    }

    @Test
    public void pageOverAssets() throws ServiceException {
        signalSetupStarting();
        String rootName = testAssetPrefix + "pageOverAssets";
        List<String> assetNames = createListOfAssetNames(rootName, 4);
        for (String assetName : assetNames) {
            wrapper.createAsset(assetName, AssetOption.None);
        }
        signalSetupFinished();

        List<ListResult<AssetInfo>> pages = wrapper.getAssetSortedPagedResults(
                rootName, 3);
        validator.validateAssetSortedPages(pages, assetNames, 3);
    }

    @Test
    public void uploadFiles() throws Exception {
        signalSetupStarting();
        AssetInfo asset = wrapper.createAsset(testAssetPrefix + "uploadFiles",
                AssetOption.None);
        signalSetupFinished();

        wrapper.uploadFilesToAsset(asset, 10, getTestAssetFiles());
        validator.validateAssetFiles(asset, getTestAssetFiles());
    }

    @Test
    public void uploadEncryptedFiles() throws Exception {
        signalSetupStarting();
        byte[] aesKey = getNewAesKey();
        AssetInfo asset = wrapper.createAsset(testAssetPrefix
                + "uploadEncryptedFiles", AssetOption.StorageEncrypted);

        signalSetupFinished();

        wrapper.uploadFilesToAsset(asset, 10, getTestAssetFiles(), aesKey);
        validator.validateAssetFiles(asset, getTestAssetFiles());
    }

    @Test
    public void downloadFiles() throws Exception {
        signalSetupStarting();
        AssetInfo asset = wrapper.createAsset(
                testAssetPrefix + "downloadFiles", AssetOption.None);
        wrapper.uploadFilesToAsset(asset, 10, getTestAssetFiles());
        signalSetupFinished();

        Map<String, InputStream> actualFileStreams = wrapper
                .downloadFilesFromAsset(asset, 10);
        validator.validateAssetFiles(getTestAssetFiles(), actualFileStreams);
    }

    @Test
    public void createJob() throws Exception {
        signalSetupStarting();
        AssetInfo asset = wrapper.createAsset(testAssetPrefix + "createJob",
                AssetOption.None);
        wrapper.uploadFilesToAsset(asset, 10, getTestAssetFiles());
        signalSetupFinished();

        String jobName = testJobPrefix + UUID.randomUUID().toString();
        JobInfo job = wrapper.createJob(jobName, asset, createTasks());
        validator.validateJob(job, jobName, asset, createTasks());
    }

    @Test
    public void transformAsset() throws Exception {
        signalSetupStarting();
        AssetInfo asset = wrapper.createAsset(testAssetPrefix
                + "transformAsset", AssetOption.None);
        wrapper.uploadFilesToAsset(asset, 10, getTestAssetFiles());
        String jobName = testJobPrefix + UUID.randomUUID().toString();

        JobInfo job = wrapper.createJob(jobName, asset, wrapper
                .createTaskOptions("Transform", 0, 0,
                        EncoderType.WindowsAzureMediaEncoder));
        signalSetupFinished();

        waitForJobToFinish(job);
        List<AssetInfo> outputAssets = wrapper.getJobOutputMediaAssets(job);

        validator.validateOutputAssets(outputAssets, getTestAssetFiles().keySet());
    }

    @Test
    public void transformEncryptedAsset() throws Exception {
        signalSetupStarting();
        byte[] aesKey = getNewAesKey();
        AssetInfo asset = wrapper.createAsset(testAssetPrefix
                + "transformEncryptedAsset", AssetOption.StorageEncrypted);

        wrapper.uploadFilesToAsset(asset, 10, getTestAssetFiles(), aesKey);
        String jobName = "my job transformEncryptedAsset"
                + UUID.randomUUID().toString();
        JobInfo job = wrapper.createJob(jobName, asset, wrapper
                .createTaskOptions("Decode", 0, 0,
                        EncoderType.StorageDecryption));
        signalSetupFinished();

        waitForJobToFinish(job);
        List<AssetInfo> outputAssets = wrapper.getJobOutputMediaAssets(job);
        validator.validateOutputAssets(outputAssets, getTestAssetFiles().keySet());

        // Verify output asset files.
        assertEquals("output assets count", 1, outputAssets.size());
        AssetInfo outputAsset = outputAssets.get(0);
        validator.validateAssetFiles(outputAsset, getTestAssetFiles());

        // Verify assets were decoded.
        Map<String, InputStream> actualFileStreams = wrapper.downloadFilesFromAsset(outputAsset, 10);
        validator.validateAssetFiles(getTestAssetFiles(), actualFileStreams);
    }

    private byte[] getNewAesKey() {
        // Media Services requires 256-bit (32-byte) keys for AES encryption.
        Random random = new Random();
        byte[] aesKey = new byte[32];
        random.nextBytes(aesKey);
        return aesKey;
    }

    private void waitForJobToFinish(JobInfo job) throws InterruptedException,
            ServiceException {
        for (int counter = 0; !wrapper.isJobFinished(job); counter++) {
            if (counter > 30) {
                fail("Took took long for the job to finish");
            }
            Thread.sleep(10000);
        }
    }

    private List<Task.CreateBatchOperation> createTasks()
            throws ServiceException {
        List<Task.CreateBatchOperation> tasks = new ArrayList<Task.CreateBatchOperation>();

        tasks.add(wrapper.createTaskOptions("Decryptor", 0, 0,
                EncoderType.StorageDecryption));
        tasks.add(wrapper.createTaskOptions("Processor", 0, 1,
                EncoderType.WindowsAzureMediaEncoder));
        return tasks;
    }

    private Map<String, InputStream> getTestAssetFiles() {
        Map<String, InputStream> inputFiles = new HashMap<String, InputStream>();
        inputFiles.put("MPEG4-H264.mp4",
                getClass().getResourceAsStream("/media/MPEG4-H264.mp4"));
        return inputFiles;
    }

    private List<String> createListOfAssetNames(String rootName, int count) {
        List<String> assetNames = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            assetNames.add(rootName + i);
        }
        return assetNames;
    }
}
