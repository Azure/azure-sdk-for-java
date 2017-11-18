/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.batch;

import com.microsoft.azure.management.batch.implementation.BatchManager;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.RestClient;

public abstract class BatchManagementTest extends TestBase {
    protected static ResourceManager resourceManager;
    protected static BatchManager batchManager;
    protected static String RG_NAME;
    protected static String BATCH_NAME;
    protected static String SA_NAME;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javabatchrg", 20);
        BATCH_NAME = generateRandomResourceName("javabatch", 15);
        SA_NAME = generateRandomResourceName("javasa", 12);

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);

        batchManager = BatchManager
                .authenticate(restClient, defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }
}
