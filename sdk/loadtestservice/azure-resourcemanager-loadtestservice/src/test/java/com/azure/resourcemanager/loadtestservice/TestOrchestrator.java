// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.loadtestservice;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;

public class TestOrchestrator extends TestBase {

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void createResource() {
        LoadTestManager loadTestManager = LoadTestManager
        .configure()
        .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));

        ResourceOperations.create(loadTestManager);

        ResourceOperations.get(loadTestManager);

        ResourceOperations.update(loadTestManager);

        ResourceOperations.delete(loadTestManager);

        QuotaOperations.listBuckets(loadTestManager);

        QuotaOperations.getBucket(loadTestManager);

        QuotaOperations.checkAvailability(loadTestManager);
    }
}
