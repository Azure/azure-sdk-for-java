// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.perf.dns;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.resourcemanager.perf.core.ResourceGroupTestBase;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class CreateDnsZonesTest extends ResourceGroupTestBase<PerfStressOptions> {
    public CreateDnsZonesTest(PerfStressOptions options) throws IOException {
        super(options);
    }

    @Override
    public void run() {
        azureResourceManager.dnsZones().define(RESOURCE_NAMER.randomName("dnsstress", 24) + ".com")
            .withExistingResourceGroup(RESOURCE_GROUP_NAME)
            .withETagCheck()
            .create();
    }

    @Override
    public Mono<Void> runAsync() {
        return azureResourceManager.dnsZones().define(RESOURCE_NAMER.randomName("dnsstress", 24) + ".com")
            .withExistingResourceGroup(RESOURCE_GROUP_NAME)
            .withETagCheck()
            .createAsync()
            .then();
    }
}
