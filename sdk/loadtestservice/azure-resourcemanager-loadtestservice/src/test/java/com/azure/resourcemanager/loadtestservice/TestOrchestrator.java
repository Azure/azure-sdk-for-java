// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.loadtestservice;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class TestOrchestrator extends TestBase {

    private static final Random RANDOM = new Random();
    private static final Region LOCATION = Region.US_WEST2;
    private static final String RESOURCE_NAME = "loadtest-resource" + RANDOM.nextInt(1000);
    private static final String QUOTA_BUCKET_NAME = "maxEngineInstancesPerTestRun";

    private DefaultAzureCredential credential;
    private AzureProfile profile;
    private LoadTestManager loadTestManager;
    private String resourceGroupName;

    public void setupCredential() {
        credential = new DefaultAzureCredentialBuilder().build();
    }

    public void setupProfile() {
        profile = new AzureProfile(AzureEnvironment.AZURE);
    }

    public void prepareTests() {
        setupCredential();
        setupProfile();
        resourceGroupName = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        loadTestManager = LoadTestManager
            .configure()
            .authenticate(credential, profile);
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void startTest() {
        prepareTests();

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
}
