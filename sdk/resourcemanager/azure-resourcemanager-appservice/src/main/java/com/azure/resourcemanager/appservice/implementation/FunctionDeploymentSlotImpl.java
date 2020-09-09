// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.fluent.inner.SitePatchResourceInner;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionDeploymentSlot;
import com.azure.resourcemanager.appservice.fluent.inner.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteLogsConfigInner;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import reactor.core.publisher.Mono;

/** The implementation for FunctionDeploymentSlot. */
class FunctionDeploymentSlotImpl
    extends DeploymentSlotBaseImpl<
        FunctionDeploymentSlot,
        FunctionDeploymentSlotImpl,
        FunctionAppImpl,
        FunctionDeploymentSlot.DefinitionStages.WithCreate,
        FunctionDeploymentSlot.Update>
    implements FunctionDeploymentSlot, FunctionDeploymentSlot.Definition, FunctionDeploymentSlot.Update {

    FunctionDeploymentSlotImpl(
        String name,
        SiteInner innerObject,
        SiteConfigResourceInner siteConfig,
        SiteLogsConfigInner logConfig,
        FunctionAppImpl parent) {
        super(name, innerObject, siteConfig, logConfig, parent);
    }

    @Override
    public FunctionDeploymentSlot.DefinitionStages.WithCreate withConfigurationFromParent() {
        return withConfigurationFromFunctionApp(this.parent());
    }

    @Override
    public FunctionDeploymentSlot.DefinitionStages.WithCreate withConfigurationFromFunctionApp(FunctionApp app) {
        this.siteConfig = ((WebAppBaseImpl) app).siteConfig;
        configurationSource = app;
        return this;
    }

    @Override
    public void zipDeploy(File zipFile) {
        zipDeployAsync(zipFile).block();
    }

    @Override
    public void zipDeploy(InputStream zipFile) {
        zipDeployAsync(zipFile).block();
    }

    @Override
    public Mono<Void> zipDeployAsync(InputStream zipFile) {
        return kuduClient.zipDeployAsync(zipFile);
    }

    @Override
    public Mono<Void> zipDeployAsync(File zipFile) {
        try {
            return kuduClient.zipDeployAsync(zipFile);
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @Override
    Mono<SiteInner> submitSite(final SiteInner site) {
        return submitSiteWithoutSiteConfig(site);
    }

    @Override
    Mono<SiteInner> submitSite(final SitePatchResourceInner siteUpdate) {
        // PATCH does not work for function app slot
        return submitSiteWithoutSiteConfig(this.inner());
    }
}
