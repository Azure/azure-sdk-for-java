// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf;

import com.azure.containers.containerregistry.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static com.azure.containers.containerregistry.perf.core.Utils.REPOSITORY_NAME;
import static com.azure.containers.containerregistry.perf.core.Utils.TEST_PERF_TAG1_NAME;
import static com.azure.containers.containerregistry.perf.core.Utils.TEST_PERF_TAG2_NAME;
import static com.azure.containers.containerregistry.perf.core.Utils.TEST_PERF_TAG3_NAME;
import static com.azure.containers.containerregistry.perf.core.Utils.TEST_PERF_TAG4_NAME;

public class ListRepositoryTests extends ServiceTest<PerfStressOptions> {

    public ListRepositoryTests(PerfStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(importImageAsync(REPOSITORY_NAME, Arrays.asList(TEST_PERF_TAG1_NAME, TEST_PERF_TAG2_NAME, TEST_PERF_TAG3_NAME, TEST_PERF_TAG4_NAME)));
    }

    @Override
    public void run() {
        containerRegistryClient.listRepositoryNames();
    }

    @Override
    public Mono<Void> runAsync() {
        return containerRegistryAsyncClient.listRepositoryNames().then();
    }
}
