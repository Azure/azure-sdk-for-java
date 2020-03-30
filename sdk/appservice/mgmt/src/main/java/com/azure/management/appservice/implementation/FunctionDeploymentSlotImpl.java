/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.management.appservice.FunctionApp;
import com.azure.management.appservice.FunctionDeploymentSlot;
import com.azure.management.appservice.models.SiteConfigResourceInner;
import com.azure.management.appservice.models.SiteInner;
import com.azure.management.appservice.models.SiteLogsConfigInner;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The implementation for FunctionDeploymentSlot.
 */
class FunctionDeploymentSlotImpl
        extends DeploymentSlotBaseImpl<
        FunctionDeploymentSlot,
            FunctionDeploymentSlotImpl,
            FunctionAppImpl,
            FunctionDeploymentSlot.DefinitionStages.WithCreate,
            FunctionDeploymentSlot.Update>
        implements
        FunctionDeploymentSlot,
        FunctionDeploymentSlot.Definition,
        FunctionDeploymentSlot.Update {

    FunctionDeploymentSlotImpl(String name, SiteInner innerObject, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig, FunctionAppImpl parent) {
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
            return zipDeployAsync(new FileInputStream(zipFile));
        } catch (IOException e) {
            return Mono.error(e);
        }
    }
}