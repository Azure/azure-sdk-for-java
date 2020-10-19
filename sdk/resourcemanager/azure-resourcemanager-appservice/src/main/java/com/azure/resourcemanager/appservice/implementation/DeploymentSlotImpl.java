// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteLogsConfigInner;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import reactor.core.publisher.Mono;

/** The implementation for DeploymentSlot. */
class DeploymentSlotImpl
    extends DeploymentSlotBaseImpl<
        DeploymentSlot,
        DeploymentSlotImpl,
        WebAppImpl,
        DeploymentSlot.DefinitionStages.WithCreate,
        DeploymentSlot.Update>
    implements DeploymentSlot, DeploymentSlot.Definition, DeploymentSlot.Update {

    DeploymentSlotImpl(
        String name,
        SiteInner innerObject,
        SiteConfigResourceInner siteConfig,
        SiteLogsConfigInner logConfig,
        WebAppImpl parent) {
        super(name, innerObject, siteConfig, logConfig, parent);
    }

    @Override
    public DeploymentSlotImpl withConfigurationFromParent() {
        return withConfigurationFromWebApp(this.parent());
    }

    @Override
    public DeploymentSlotImpl withConfigurationFromWebApp(WebApp webApp) {
        this.siteConfig = ((WebAppBaseImpl) webApp).siteConfig;
        configurationSource = webApp;
        return this;
    }

    @Override
    public Mono<Void> warDeployAsync(File warFile) {
        return warDeployAsync(warFile, null);
    }

    @Override
    public void warDeploy(File warFile) {
        warDeployAsync(warFile).block();
    }

    @Override
    public Mono<Void> warDeployAsync(InputStream warFile, long length) {
        return warDeployAsync(warFile, length, null);
    }

    @Override
    public void warDeploy(InputStream warFile, long length) {
        warDeployAsync(warFile, length).block();
    }

    @Override
    public Mono<Void> warDeployAsync(File warFile, String appName) {
        try {
            return kuduClient.warDeployAsync(warFile, appName);
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @Override
    public void warDeploy(File warFile, String appName) {
        warDeployAsync(warFile, appName).block();
    }

    @Override
    public Mono<Void> warDeployAsync(InputStream warFile, long length, String appName) {
        return kuduClient.warDeployAsync(warFile, length, appName);
    }

    @Override
    public void warDeploy(InputStream warFile, long length, String appName) {
        warDeployAsync(warFile, length, appName).block();
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
        return kuduClient.zipDeployAsync(zipFile, length).then(stopAsync()).then(startAsync());
    }

    @Override
    public Mono<Void> zipDeployAsync(File zipFile) {
        try {
            return kuduClient.zipDeployAsync(zipFile);
        } catch (IOException e) {
            return Mono.error(e);
        }
    }
}
