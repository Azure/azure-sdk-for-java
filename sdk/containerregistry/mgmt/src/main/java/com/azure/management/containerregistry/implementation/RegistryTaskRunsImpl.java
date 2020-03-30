/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerregistry.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.containerregistry.RegistryTaskRun;
import com.azure.management.containerregistry.RegistryTaskRuns;
import com.azure.management.containerregistry.models.RunInner;
import reactor.core.publisher.Mono;

class RegistryTaskRunsImpl implements RegistryTaskRuns {

    private ContainerRegistryManager registryManager;

    RegistryTaskRunsImpl(ContainerRegistryManager registryManager) {
        this.registryManager = registryManager;
    }


    @Override
    public RegistryTaskRun.DefinitionStages.BlankFromRuns scheduleRun() {
        return new RegistryTaskRunImpl(registryManager, new RunInner());
    }

    @Override
    public PagedFlux<RegistryTaskRun> listByRegistryAsync(String rgName, String acrName) {
        return this.registryManager.inner().runs().listAsync(rgName, acrName)
                .mapPage(inner -> wrapModel(inner));
    }

    @Override
    public PagedIterable<RegistryTaskRun> listByRegistry(String rgName, String acrName) {
        return new PagedIterable<>(this.listByRegistryAsync(rgName, acrName));
    }

    @Override
    public Mono<String> getLogSasUrlAsync(String rgName, String acrName, String runId) {
        return this.registryManager.inner().runs().getLogSasUrlAsync(rgName, acrName, runId)
            .map(runGetLogResultInner -> runGetLogResultInner.logLink());
    }

    @Override
    public String getLogSasUrl(String rgName, String acrName, String runId) {
        return this.getLogSasUrlAsync(rgName, acrName, runId).block();
    }

    @Override
    public Mono<Void> cancelAsync(String rgName, String acrName, String runId) {
        return this.registryManager.inner().runs().cancelAsync(rgName, acrName, runId);
    }

    @Override
    public void cancel(String rgName, String acrName, String runId) {
        this.cancelAsync(rgName, acrName, runId).block();
    }

    private RegistryTaskRunImpl wrapModel(RunInner innerModel) {
        return new RegistryTaskRunImpl(registryManager, innerModel);
    }
}
