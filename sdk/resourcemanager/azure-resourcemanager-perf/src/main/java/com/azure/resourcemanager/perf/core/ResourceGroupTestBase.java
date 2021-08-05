// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.perf.core;

import com.azure.core.management.Region;
import com.azure.core.test.utils.ResourceNamer;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.UUID;

public abstract class ResourceGroupTestBase<TOptions extends PerfStressOptions> extends AzureResourceManagerTest<TOptions> {
    protected final String RESOURCE_GROUP_NAME;
    protected final ResourceNamer RESOURCE_NAMER;

    public ResourceGroupTestBase(TOptions options) throws IOException {
        super(options);
        RESOURCE_GROUP_NAME = "perfstress-" + UUID.randomUUID().toString();
        RESOURCE_NAMER = new ResourceNamer("");
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(azureResourceManager.resourceGroups()
                .define(RESOURCE_GROUP_NAME)
                .withRegion(Region.US_WEST)
                .createAsync().then()
            );
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return azureResourceManager.resourceGroups().deleteByNameAsync(RESOURCE_GROUP_NAME)
            .then(super.cleanupAsync());
    }
}
