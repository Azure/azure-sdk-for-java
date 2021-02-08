// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.perf.resources;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.resourcemanager.perf.core.AzureResourceManagerTest;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class ListTenantsTest extends AzureResourceManagerTest<PerfStressOptions> {
    public ListTenantsTest(PerfStressOptions options) throws IOException {
        super(options);
    }

    @Override
    public void run() {
        azureResourceManager.tenants().list();
    }

    @Override
    public Mono<Void> runAsync() {
        return azureResourceManager.tenants().listAsync().then();
    }
}
