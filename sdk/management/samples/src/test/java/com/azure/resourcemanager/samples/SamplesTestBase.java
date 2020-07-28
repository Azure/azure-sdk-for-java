// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;

public class SamplesTestBase extends TestBase {
    protected Azure azure;

    public SamplesTestBase() {
        super(RunCondition.BOTH);
    }

    public SamplesTestBase(RunCondition runCondition) {
        super(runCondition);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        azure = Azure
                .authenticate(httpPipeline, profile)
                .withSdkContext(sdkContext)
                .withDefaultSubscription();
    }

    @Override
    protected void cleanUpResources() {
    }
}
