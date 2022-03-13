// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.fluent.models.HostKeysInner;
import com.azure.resourcemanager.appservice.fluent.models.SitePatchResourceInner;
import com.azure.resourcemanager.appservice.models.DeploymentSlotBase;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionDeploymentSlot;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteLogsConfigInner;
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
        DeploymentSlotBase<FunctionDeploymentSlot>>
    implements FunctionDeploymentSlot, FunctionDeploymentSlot.Definition {

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
    public void zipDeploy(InputStream zipFile, long length) {
        zipDeployAsync(zipFile, length).block();
    }

    @Override
    public Mono<Void> zipDeployAsync(InputStream zipFile, long length) {
        return kuduClient.zipDeployAsync(zipFile, length);
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
        return submitSiteWithoutSiteConfig(this.innerModel());
    }

    @Override
    public String getMasterKey() {
        return this.getMasterKeyAsync().block();
    }

    @Override
    public Mono<String> getMasterKeyAsync() {
        return this.manager().serviceClient().getWebApps().listHostKeysSlotAsync(
            this.resourceGroupName(), this.parent().name(), this.name()).map(HostKeysInner::masterKey);
    }
}
