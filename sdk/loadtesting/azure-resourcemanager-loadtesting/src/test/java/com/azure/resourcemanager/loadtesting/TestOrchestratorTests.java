// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.loadtesting;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import java.util.Random;

import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Test;

public class TestOrchestratorTests extends TestBase {

    private static final Random RANDOM = new Random();
    private static final Region LOCATION = Region.US_WEST2;
    private static final String RESOURCE_NAME = "loadtest-resource" + RANDOM.nextInt(1000);
    private static final String QUOTA_BUCKET_NAME = "maxEngineInstancesPerTestRun";
    private String resourceGroupName = "rg" + randomPadding();
    private LoadTestManager loadTestManager;
    private ResourceManager resourceManager = null;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        loadTestManager = LoadTestManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(LOCATION)
                .create();
        }
    }
    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @LiveOnly
    public void startTest() {
        ResourceOperations resourceOperations = new ResourceOperations(LOCATION.toString(), resourceGroupName, RESOURCE_NAME);
        resourceOperations.create(loadTestManager);
        resourceOperations.get(loadTestManager);
        resourceOperations.update(loadTestManager);
        resourceOperations.delete(loadTestManager);

        QuotaOperations quotaOperations = new QuotaOperations(LOCATION.toString(), QUOTA_BUCKET_NAME);
        quotaOperations.listBuckets(loadTestManager);
        quotaOperations.getBucket(loadTestManager);
        quotaOperations.checkAvailability(loadTestManager);
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
