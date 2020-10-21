// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.perf.core;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.test.utils.AuthFile;

import java.io.File;
import java.io.IOException;

public abstract class AzureResourceManagerTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    protected final AzureResourceManager azureResourceManager;
    public AzureResourceManagerTest(TOptions options) throws IOException {
        super(options);

        String authFilePath = System.getenv("AZURE_AUTH_LOCATION");
        if (CoreUtils.isNullOrEmpty(authFilePath)) {
            System.out.println("Environment variable AZURE_AUTH_LOCATION must be set.");
            System.exit(1);
        }

        AuthFile authFile = AuthFile.parse(new File(authFilePath));
        AzureProfile profile = new AzureProfile(authFile.getTenantId(), authFile.getSubscriptionId(), AzureEnvironment.AZURE);
        azureResourceManager = AzureResourceManager.authenticate(authFile.getCredential(), profile).withDefaultSubscription();
    }
}
