// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.loadtestservice;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.resources.ResourceManager;

import java.util.Random;
import org.junit.jupiter.api.Test;

public class TestOrchestrator extends TestBase {

    private static final Random RANDOM = new Random();
    private static final Region Location = Region.US_WEST2;
    private static final String ResourceGroupName = "az-java-sdk-"+RANDOM.nextInt(1000);
    private static final String LoadTestResourceName = "loadtest-resource"+RANDOM.nextInt(1000);
    private static final String QuotaBucketName = "maxEngineInstancesPerTestRun";

    private DefaultAzureCredential Credential;
    private AzureProfile Profile;
    private LoadTestManager LoadTestsManager;

    public void SetupCredential(){
        Credential = new DefaultAzureCredentialBuilder().build();
    }

    public void SetupProfile(){
        Profile = new AzureProfile(AzureEnvironment.AZURE);
    }

    public void PrepareTests(){
        SetupCredential();
        SetupProfile();

        LoadTestsManager = LoadTestManager
        .configure()
        .authenticate(Credential, Profile);
        
        ResourceManager
        .authenticate(Credential, Profile)
        .withDefaultSubscription()
        .resourceGroups()
        .define(ResourceGroupName)
        .withRegion(Location.toString())
        .create();
    }

    public void CleanupTests(){
        ResourceManager
        .authenticate(Credential, Profile)
        .withDefaultSubscription()
        .resourceGroups()
        .beginDeleteByName(ResourceGroupName);
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void StartTest() {
        PrepareTests();

        ResourceOperations resourceOperations = new ResourceOperations(Location.toString(), ResourceGroupName, LoadTestResourceName);
        resourceOperations.Create(LoadTestsManager);
        resourceOperations.Get(LoadTestsManager);
        resourceOperations.Update(LoadTestsManager);
        resourceOperations.Delete(LoadTestsManager);

        QuotaOperations quotaOperations = new QuotaOperations(Location.toString(), QuotaBucketName);
        quotaOperations.ListBuckets(LoadTestsManager);
        quotaOperations.GetBucket(LoadTestsManager);
        quotaOperations.CheckAvailability(LoadTestsManager);

        CleanupTests();
    }
}
