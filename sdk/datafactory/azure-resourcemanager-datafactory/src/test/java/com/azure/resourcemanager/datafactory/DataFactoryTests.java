// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.datafactory;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.datafactory.models.AzureBlobDataset;
import com.azure.resourcemanager.datafactory.models.AzureStorageLinkedService;
import com.azure.resourcemanager.datafactory.models.BlobSink;
import com.azure.resourcemanager.datafactory.models.BlobSource;
import com.azure.resourcemanager.datafactory.models.CopyActivity;
import com.azure.resourcemanager.datafactory.models.CreateRunResponse;
import com.azure.resourcemanager.datafactory.models.DatasetReference;
import com.azure.resourcemanager.datafactory.models.Factory;
import com.azure.resourcemanager.datafactory.models.LinkedServiceReference;
import com.azure.resourcemanager.datafactory.models.PipelineResource;
import com.azure.resourcemanager.datafactory.models.PipelineRun;
import com.azure.resourcemanager.datafactory.models.TextFormat;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DataFactoryTests extends TestBase {

    private static final Random RANDOM = new Random();

    private static final Region REGION = Region.US_WEST2;
    private static final String STORAGE_ACCOUNT = "sa" + randomPadding();
    private static final String DATA_FACTORY = "df" + randomPadding();

    private static String resourceGroup = "rg" + randomPadding();

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void dataFactoryTest() {
        StorageManager storageManager = StorageManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));

        DataFactoryManager manager = DataFactoryManager
            .configure().withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));

        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        boolean testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroup = testResourceGroup;
        } else {
            storageManager.resourceManager().resourceGroups().define(resourceGroup)
                .withRegion(REGION)
                .create();
        }

        try {
            // @embedmeStart
            // storage account
            StorageAccount storageAccount = storageManager.storageAccounts().define(STORAGE_ACCOUNT)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .create();
            final String storageAccountKey = storageAccount.getKeys().iterator().next().value();
            final String connectionString = getStorageConnectionString(STORAGE_ACCOUNT, storageAccountKey, storageManager.environment());

            // container
            final String containerName = "adf";
            storageManager.blobContainers().defineContainer(containerName)
                .withExistingStorageAccount(resourceGroup, STORAGE_ACCOUNT)
                .withPublicAccess(PublicAccess.NONE)
                .create();

            // blob as input
            BlobClient blobClient = new BlobClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .blobName("input/data.txt")
                .buildClient();
            blobClient.upload(BinaryData.fromString("data"));

            // data factory
            Factory dataFactory = manager.factories().define(DATA_FACTORY)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroup)
                .create();

            // linked service
            final Map<String, String> connectionStringProperty = new HashMap<>();
            connectionStringProperty.put("type", "SecureString");
            connectionStringProperty.put("value", connectionString);

            final String linkedServiceName = "LinkedService";
            manager.linkedServices().define(linkedServiceName)
                .withExistingFactory(resourceGroup, DATA_FACTORY)
                .withProperties(new AzureStorageLinkedService()
                    .withConnectionString(connectionStringProperty))
                .create();

            // input dataset
            final String inputDatasetName = "InputDataset";
            manager.datasets().define(inputDatasetName)
                .withExistingFactory(resourceGroup, DATA_FACTORY)
                .withProperties(new AzureBlobDataset()
                    .withLinkedServiceName(new LinkedServiceReference().withReferenceName(linkedServiceName))
                    .withFolderPath(containerName)
                    .withFileName("input/data.txt")
                    .withFormat(new TextFormat()))
                .create();

            // output dataset
            final String outputDatasetName = "OutputDataset";
            manager.datasets().define(outputDatasetName)
                .withExistingFactory(resourceGroup, DATA_FACTORY)
                .withProperties(new AzureBlobDataset()
                    .withLinkedServiceName(new LinkedServiceReference().withReferenceName(linkedServiceName))
                    .withFolderPath(containerName)
                    .withFileName("output/data.txt")
                    .withFormat(new TextFormat()))
                .create();

            // pipeline
            PipelineResource pipeline = manager.pipelines().define("CopyBlobPipeline")
                .withExistingFactory(resourceGroup, DATA_FACTORY)
                .withActivities(Collections.singletonList(new CopyActivity()
                    .withName("CopyBlob")
                    .withSource(new BlobSource())
                    .withSink(new BlobSink())
                    .withInputs(Collections.singletonList(new DatasetReference().withReferenceName(inputDatasetName)))
                    .withOutputs(Collections.singletonList(new DatasetReference().withReferenceName(outputDatasetName)))))
                .create();

            // run pipeline
            CreateRunResponse createRun = pipeline.createRun();

            // wait for completion
            PipelineRun pipelineRun = manager.pipelineRuns().get(resourceGroup, DATA_FACTORY, createRun.runId());
            String runStatus = pipelineRun.status();
            while ("InProgress".equals(runStatus)) {
                sleepIfRunningAgainstService(10 * 1000);    // wait 10 seconds
                pipelineRun = manager.pipelineRuns().get(resourceGroup, DATA_FACTORY, createRun.runId());
                runStatus = pipelineRun.status();
            }
            // @embedmeEnd

            manager.linkedServices().listByFactory(resourceGroup, DATA_FACTORY).stream().count();
            manager.datasets().listByFactory(resourceGroup, DATA_FACTORY).stream().count();
            manager.pipelines().listByFactory(resourceGroup, DATA_FACTORY).stream().count();

            manager.factories().deleteById(dataFactory.id());
            storageManager.storageAccounts().deleteById(storageAccount.id());
        } finally {
            if (!testEnv) {
                storageManager.resourceManager().resourceGroups().beginDeleteByName(resourceGroup);
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }

    private static String getStorageConnectionString(String accountName, String accountKey,
                                                     AzureEnvironment environment) {
        if (environment == null || environment.getStorageEndpointSuffix() == null) {
            environment = AzureEnvironment.AZURE;
        }
        String suffix = environment.getStorageEndpointSuffix().replaceAll("^\\.*", "");
        return String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=%s",
            accountName, accountKey, suffix);
    }
}
