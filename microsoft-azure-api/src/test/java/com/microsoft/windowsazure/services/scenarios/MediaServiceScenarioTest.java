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
import org.junit.Ignore;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.EncryptionOption;
import com.microsoft.windowsazure.services.media.models.ListResult;

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

    @Ignore("Needs https://github.com/WindowsAzure/azure-sdk-for-java-pr/issues/277")
    @Test
    public void uploadFiles() throws Exception {
        signalSetupStarting();
        AssetInfo asset = wrapper.createAsset(testAssetPrefix + "uploadFiles", EncryptionOption.None);
        signalSetupFinished();

        wrapper.uploadFilesToAsset(asset, 10, getTestAssetFiles());
        validator.validateAssetFiles(asset, getTestAssetFiles());
    }

    @Ignore("Needs https://github.com/WindowsAzure/azure-sdk-for-java-pr/issues/277")
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

        MediaServiceMocks.JobInfo job = wrapper.createJob("my job createJob", asset, createTasks());
        validator.validateJob(job, "my job", asset, createTasks());
    }

    @Test
    public void transformAsset() throws Exception {
        signalSetupStarting();
        AssetInfo asset = wrapper.createAsset(testAssetPrefix + "transformAsset", EncryptionOption.None);
        wrapper.uploadFilesToAsset(asset, 10, getTestAssetFiles());
        MediaServiceMocks.JobInfo job = wrapper.createJob("my job transformAsset", asset, createTasks());
        signalSetupFinished();

        waitForJobToFinish(job);
        List<AssetInfo> outputAssets = wrapper.getJobOutputMediaAssets(job);
        validator.validateOutputAssets(outputAssets);
    }

    private void waitForJobToFinish(MediaServiceMocks.JobInfo job) throws InterruptedException {
        for (int counter = 0; !wrapper.isJobFinished(job); counter++) {
            if (counter > 10) {
                fail("Took took long for the job to finish");
            }
            Thread.sleep(20000);
        }
    }

    private List<MediaServiceMocks.CreateTaskOptions> createTasks() throws ServiceException {
        List<MediaServiceMocks.CreateTaskOptions> tasks = new ArrayList<MediaServiceMocks.CreateTaskOptions>();
        tasks.add(wrapper.createTaskOptionsMp4ToSmoothStreams("MP4 to SS"));
        tasks.add(wrapper.createTaskOptionsSmoothStreamsToHls("SS to HLS"));
        return tasks;
    }

    private Hashtable<String, InputStream> getTestAssetFiles() {
        Hashtable<String, InputStream> inputFiles = new Hashtable<String, InputStream>();
        inputFiles.put("interview.wmv", getClass().getResourceAsStream("/media/interview.wmv"));
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
