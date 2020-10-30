// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.perf.storage;

import com.azure.core.management.Region;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.resourcemanager.perf.core.ResourceGroupTestBase;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class CreateStorageAccountsTest extends ResourceGroupTestBase<PerfStressOptions> {
    public CreateStorageAccountsTest(PerfStressOptions options) throws IOException {
        super(options);
    }

    @Override
    public void run() {
        azureResourceManager.storageAccounts().define(RESOURCE_NAMER.randomName("sastress", 24))
            .withRegion(Region.US_WEST)
            .withExistingResourceGroup(RESOURCE_GROUP_NAME)
            .create();
    }

    @Override
    public Mono<Void> runAsync() {
        return azureResourceManager.storageAccounts().define(RESOURCE_NAMER.randomName("sastress", 24))
            .withRegion(Region.US_WEST)
            .withExistingResourceGroup(RESOURCE_GROUP_NAME)
            .createAsync()
            .then();
    }
}
