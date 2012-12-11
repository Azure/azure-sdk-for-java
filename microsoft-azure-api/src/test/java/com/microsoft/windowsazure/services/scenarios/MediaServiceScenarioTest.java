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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.EncryptionOption;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Task;

public class MediaServiceScenarioTest extends ScenarioTestBase {
    private static final String rootTestAssetPrefix = "testAssetPrefix-";
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
    }

    @Test
    public void newAsset() throws Exception {
        AssetInfo asset = wrapper.createAsset(testAssetPrefix + "newAsset", EncryptionOption.None);
        validator.validateAsset(asset, testAssetPrefix + "newAsset", EncryptionOption.None);
    }

    @Test
    public void pageOverAssets() throws ServiceException {
        signalSetupStarting();
        String rootName = testAssetPrefix + "pageOverAssets";
        List<String> assetNames = createListOfAssetNames(rootName, 4);
        for (String assetName : assetNames) {
            wrapper.createAsset(assetName, EncryptionOption.None);
        }
        signalSetupFinished();

        List<ListResult<AssetInfo>> pages = wrapper.getAssetSortedPagedResults(rootName, 3);
        validator.validateAssetSortedPages(pages, assetNames, 3);
    }

    @Test
    public void uploadFiles() throws Exception {
        signalSetupStarting();
        AssetInfo asset = wrapper.createAsset(testAssetPrefix + "uploadFiles", EncryptionOption.None);
        signalSetupFinished();

        wrapper.uploadFilesToAsset(asset, 10, getTestAssetFiles());
        validator.validateAssetFiles(asset, getTestAssetFiles());
    }

    @Test
    public void downloadFiles() throws Exception {
        signalSetupStarting();
        AssetInfo asset = wrapper.createAsset(testAssetPrefix + "downloadFiles", EncryptionOption.None);
        wrapper.uploadFilesToAsset(asset, 10, getTestAssetFiles());
        signalSetupFinished();

        List<URL> fileUrls = wrapper.createFileURLsFromAsset(asset, 10);
        validator.validateAssetFileUrls(fileUrls, getTestAssetFiles());
    }

    @Test
    public void createJob() throws Exception {
        signalSetupStarting();
        AssetInfo asset = wrapper.createAsset(testAssetPrefix + "createJob", EncryptionOption.None);
        wrapper.uploadFilesToAsset(asset, 10, getTestAssetFiles());
        signalSetupFinished();

        String jobName = "my job createJob";
        JobInfo job = wrapper.createJob(jobName, asset, createTasks());
        validator.validateJob(job, jobName, asset, createTasks());
    }

    @Test
    public void transformAsset() throws Exception {
        signalSetupStarting();
        AssetInfo asset = wrapper.createAsset(testAssetPrefix + "transformAsset", EncryptionOption.None);
        wrapper.uploadFilesToAsset(asset, 10, getTestAssetFiles());
        String jobName = "my job transformAsset";
        JobInfo job = wrapper.createJob(jobName, asset, createTasks());
        signalSetupFinished();

        waitForJobToFinish(job);
        List<AssetInfo> outputAssets = wrapper.getJobOutputMediaAssets(job);
        validator.validateOutputAssets(outputAssets);
    }

    private void waitForJobToFinish(JobInfo job) throws InterruptedException, ServiceException {
        for (int counter = 0; !wrapper.isJobFinished(job); counter++) {
            if (counter > 10) {
                fail("Took took long for the job to finish");
            }
            Thread.sleep(10000);
        }
    }

    private List<Task.CreateBatchOperation> createTasks() throws ServiceException {
        List<Task.CreateBatchOperation> tasks = new ArrayList<Task.CreateBatchOperation>();

        tasks.add(wrapper.createTaskOptionsMp4ToSmoothStreams("MP4 to SS", 0, 0));
        tasks.add(wrapper.createTaskOptionsSmoothStreamsToHls("SS to HLS", 0, 1));
        return tasks;
    }

    private Hashtable<String, InputStream> getTestAssetFiles() {
        Hashtable<String, InputStream> inputFiles = new Hashtable<String, InputStream>();
        inputFiles.put("MPEG4-H264.mp4", getClass().getResourceAsStream("/media/MPEG4-H264.mp4"));
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
