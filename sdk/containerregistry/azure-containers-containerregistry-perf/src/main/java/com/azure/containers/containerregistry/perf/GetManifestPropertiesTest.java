// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf;

;

import com.azure.containers.containerregistry.ContainerRepository;
import com.azure.containers.containerregistry.ContainerRepositoryAsync;
import com.azure.containers.containerregistry.RegistryArtifact;
import com.azure.containers.containerregistry.RegistryArtifactAsync;
import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static com.azure.containers.containerregistry.perf.core.Utils.REPOSITORY_NAME;
import static com.azure.containers.containerregistry.perf.core.Utils.TEST_PERF_TAG1_NAME;
import static com.azure.containers.containerregistry.perf.core.Utils.TEST_PERF_TAG2_NAME;
import static com.azure.containers.containerregistry.perf.core.Utils.TEST_PERF_TAG3_NAME;
import static com.azure.containers.containerregistry.perf.core.Utils.TEST_PERF_TAG4_NAME;

public class GetManifestPropertiesTest extends ServiceTest<PerfStressOptions> {
    private final RegistryArtifactAsync registryArtifactAsync;
    private final RegistryArtifact registryArtifact;
    private final List<String> tags;

    public GetManifestPropertiesTest(PerfStressOptions options) {
        super(options);

        tags = Arrays.asList(TEST_PERF_TAG1_NAME, TEST_PERF_TAG2_NAME, TEST_PERF_TAG3_NAME, TEST_PERF_TAG4_NAME);
        registryArtifact = containerRegistryClient.getArtifact(REPOSITORY_NAME, TEST_PERF_TAG1_NAME);
        registryArtifactAsync = containerRegistryAsyncClient.getArtifact(REPOSITORY_NAME, TEST_PERF_TAG1_NAME);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(importImageAsync(REPOSITORY_NAME, tags));
    }

    @Override
    public void run() {
        registryArtifact.getManifestProperties();
    }

    @Override
    public Mono<Void> runAsync() {
        return registryArtifactAsync.getManifestProperties()
            .then();
    }
}
