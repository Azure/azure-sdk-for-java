// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.storage;

import com.azure.core.http.HttpPipeline;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.fluentcore.profile.AzureProfile;
import com.azure.management.resources.implementation.ResourceManager;
import com.azure.management.storage.implementation.StorageManager;

/** The base for storage manager tests. */
public abstract class StorageManagementTest extends TestBase {
    protected ResourceManager resourceManager;
    protected StorageManager storageManager;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        resourceManager =
            ResourceManager.authenticate(httpPipeline, profile).withSdkContext(sdkContext).withDefaultSubscription();

        storageManager = StorageManager.authenticate(httpPipeline, profile, sdkContext);
    }
}
