// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.models.RegistryTaskRun;
import com.azure.resourcemanager.containerregistry.models.RegistryTaskRuns;
import com.azure.resourcemanager.containerregistry.fluent.models.RunInner;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

public class RegistryTaskRunsImpl implements RegistryTaskRuns {

    private ContainerRegistryManager registryManager;

    public RegistryTaskRunsImpl(ContainerRegistryManager registryManager) {
        this.registryManager = registryManager;
    }

    @Override
    public RegistryTaskRun.DefinitionStages.BlankFromRuns scheduleRun() {
        return new RegistryTaskRunImpl(registryManager, new RunInner());
    }

    @Override
    public PagedFlux<RegistryTaskRun> listByRegistryAsync(String rgName, String acrName) {
        return PagedConverter.mapPage(this.registryManager.serviceClient().getRuns().listAsync(rgName, acrName),
            inner -> wrapModel(inner));
    }

    @Override
    public PagedIterable<RegistryTaskRun> listByRegistry(String rgName, String acrName) {
        return new PagedIterable<>(this.listByRegistryAsync(rgName, acrName));
    }

    @Override
    public Mono<String> getLogSasUrlAsync(String rgName, String acrName, String runId) {
        return this
            .registryManager
            .serviceClient()
            .getRuns()
            .getLogSasUrlAsync(rgName, acrName, runId)
            .map(runGetLogResultInner -> runGetLogResultInner.logLink());
    }

    @Override
    public String getLogSasUrl(String rgName, String acrName, String runId) {
        return this.getLogSasUrlAsync(rgName, acrName, runId).block();
    }

    @Override
    public Mono<Void> cancelAsync(String rgName, String acrName, String runId) {
        return this.registryManager.serviceClient().getRuns().cancelAsync(rgName, acrName, runId);
    }

    @Override
    public void cancel(String rgName, String acrName, String runId) {
        this.cancelAsync(rgName, acrName, runId).block();
    }

    private RegistryTaskRunImpl wrapModel(RunInner innerModel) {
        return new RegistryTaskRunImpl(registryManager, innerModel);
    }
}
