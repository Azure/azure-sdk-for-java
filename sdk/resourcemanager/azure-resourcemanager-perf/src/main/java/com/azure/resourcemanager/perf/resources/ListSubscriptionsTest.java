// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.perf.resources;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.resourcemanager.perf.core.AzureResourceManagerTest;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class ListSubscriptionsTest extends AzureResourceManagerTest<PerfStressOptions> {
    public ListSubscriptionsTest(PerfStressOptions options) throws IOException {
        super(options);
    }

    @Override
    public void run() {
        azureResourceManager.subscriptions().list();
    }

    @Override
    public Mono<Void> runAsync() {
        return azureResourceManager.subscriptions().listAsync().then();
    }
}
