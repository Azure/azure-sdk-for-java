/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage;

import com.azure.management.RestClient;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.implementation.ResourceManager;
import com.azure.management.storage.implementation.StorageManager;

/**
 * The base for storage manager tests.
 */
public abstract class StorageManagementTest extends TestBase {
    protected ResourceManager resourceManager;
    protected StorageManager storageManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSdkContext(sdkContext)
                .withSubscription(defaultSubscription);

        storageManager = StorageManager
                .authenticate(restClient, defaultSubscription, sdkContext);
    }

}
