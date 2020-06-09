// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.DeploymentSlots;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RuntimeStack;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebAppRuntimeStack;
import com.azure.resourcemanager.appservice.fluent.inner.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteLogsConfigInner;
import com.azure.resourcemanager.appservice.fluent.inner.StringDictionaryInner;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import reactor.core.publisher.Mono;

/** The implementation for WebApp. */
class WebAppImpl extends AppServiceBaseImpl<WebApp, WebAppImpl, WebApp.DefinitionStages.WithCreate, WebApp.Update>
    implements WebApp,
        WebApp.Definition,
        WebApp.DefinitionStages.ExistingWindowsPlanWithGroup,
        WebApp.DefinitionStages.ExistingLinuxPlanWithGroup,
        WebApp.DefinitionStages.WithWindowsRuntimeStack,
        WebApp.Update,
        WebApp.UpdateStages.WithCredentials,
        WebApp.UpdateStages.WithStartUpCommand {

    private DeploymentSlots deploymentSlots;
    private WebAppRuntimeStack runtimeStackOnWindowsOSToUpdate;

    WebAppImpl(
        String name,
        SiteInner innerObject,
        SiteConfigResourceInner siteConfig,
        SiteLogsConfigInner logConfig,
        AppServiceManager manager) {
        super(name, innerObject, siteConfig, logConfig, manager);
    }

    @Override
    public WebAppImpl update() {
        runtimeStackOnWindowsOSToUpdate = null;
        return super.update();
    }

    @Override
    public DeploymentSlots deploymentSlots() {
        if (deploymentSlots == null) {
            deploymentSlots = new DeploymentSlotsImpl(this);
        }
        return deploymentSlots;
    }

    @Override
    public WebAppImpl withBuiltInImage(RuntimeStack runtimeStack) {
        ensureLinuxPlan();
        cleanUpContainerSettings();
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withLinuxFxVersion(String.format("%s|%s", runtimeStack.stack(), runtimeStack.version()));
        if (runtimeStack.stack().equals("NODE")) {
            siteConfig.withNodeVersion(runtimeStack.version());
        }
        if (runtimeStack.stack().equals("PHP")) {
            siteConfig.withPhpVersion(runtimeStack.version());
        }
        if (runtimeStack.stack().equals("DOTNETCORE")) {
            siteConfig.withNetFrameworkVersion(runtimeStack.version());
        }
        return this;
    }

    @Override
    protected void cleanUpContainerSettings() {
        if (siteConfig != null && siteConfig.linuxFxVersion() != null) {
            siteConfig.withLinuxFxVersion(null);
        }
        // PHP
        if (siteConfig != null && siteConfig.phpVersion() != null) {
            siteConfig.withPhpVersion(null);
        }
        // Node
        if (siteConfig != null && siteConfig.nodeVersion() != null) {
            siteConfig.withNodeVersion(null);
        }
        // .NET
        if (siteConfig != null && siteConfig.netFrameworkVersion() != null) {
            siteConfig.withNetFrameworkVersion("v4.0");
        }
        // Docker Hub
        withoutAppSetting(SETTING_DOCKER_IMAGE);
        withoutAppSetting(SETTING_REGISTRY_SERVER);
        withoutAppSetting(SETTING_REGISTRY_USERNAME);
        withoutAppSetting(SETTING_REGISTRY_PASSWORD);
    }

    @Override
    public WebAppImpl withStartUpCommand(String startUpCommand) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withAppCommandLine(startUpCommand);
        return this;
    }

    @Override
    public WebAppImpl withExistingWindowsPlan(AppServicePlan appServicePlan) {
        return super.withExistingAppServicePlan(appServicePlan);
    }

    @Override
    public WebAppImpl withExistingLinuxPlan(AppServicePlan appServicePlan) {
        return super.withExistingAppServicePlan(appServicePlan);
    }

    @Override
    public WebAppImpl withNewWindowsPlan(PricingTier pricingTier) {
        return super.withNewAppServicePlan(OperatingSystem.WINDOWS, pricingTier);
    }

    @Override
    public WebAppImpl withNewWindowsPlan(String appServicePlanName, PricingTier pricingTier) {
        return super.withNewAppServicePlan(appServicePlanName, OperatingSystem.WINDOWS, pricingTier);
    }

    @Override
    public WebAppImpl withNewWindowsPlan(Creatable<AppServicePlan> appServicePlanCreatable) {
        return super.withNewAppServicePlan(appServicePlanCreatable);
    }

    @Override
    public WebAppImpl withNewLinuxPlan(PricingTier pricingTier) {
        return super.withNewAppServicePlan(OperatingSystem.LINUX, pricingTier);
    }

    @Override
    public WebAppImpl withNewLinuxPlan(String appServicePlanName, PricingTier pricingTier) {
        return super.withNewAppServicePlan(appServicePlanName, OperatingSystem.LINUX, pricingTier);
    }

    @Override
    public WebAppImpl withNewLinuxPlan(Creatable<AppServicePlan> appServicePlanCreatable) {
        return super.withNewAppServicePlan(appServicePlanCreatable);
    }

    @Override
    public WebAppImpl withRuntimeStack(WebAppRuntimeStack runtimeStack) {
        runtimeStackOnWindowsOSToUpdate = runtimeStack;
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
    public Mono<Void> warDeployAsync(InputStream warFile) {
        return warDeployAsync(warFile, null);
    }

    @Override
    public void warDeploy(InputStream warFile) {
        warDeployAsync(warFile).block();
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
    public void warDeploy(InputStream warFile, String appName) {
        warDeployAsync(warFile, appName).block();
    }

    @Override
    public Mono<Void> warDeployAsync(InputStream warFile, String appName) {
        return kuduClient.warDeployAsync(warFile, appName);
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
    public void zipDeploy(File zipFile) {
        zipDeployAsync(zipFile).block();
    }

    @Override
    public Mono<Void> zipDeployAsync(InputStream zipFile) {
        return kuduClient.zipDeployAsync(zipFile).then(WebAppImpl.this.stopAsync()).then(WebAppImpl.this.startAsync());
    }

    @Override
    public void zipDeploy(InputStream zipFile) {
        zipDeployAsync(zipFile).block();
    }

    @Override
    Mono<Indexable> submitMetadata() {
        Mono<Indexable> observable = super.submitMetadata();
        if (runtimeStackOnWindowsOSToUpdate != null) {
            observable =
                observable
                    // list metadata
                    .then(listMetadata())
                    // merge with change, then update
                    .switchIfEmpty(Mono.just(new StringDictionaryInner()))
                    .flatMap(
                        stringDictionaryInner -> {
                            if (stringDictionaryInner.properties() == null) {
                                stringDictionaryInner.withProperties(new HashMap<String, String>());
                            }
                            stringDictionaryInner
                                .properties()
                                .put("CURRENT_STACK", runtimeStackOnWindowsOSToUpdate.runtime());
                            return updateMetadata(stringDictionaryInner);
                        })
                    // clean up
                    .then(
                        Mono
                            .fromCallable(
                                () -> {
                                    runtimeStackOnWindowsOSToUpdate = null;
                                    return WebAppImpl.this;
                                }));
        }
        return observable;
    }

    Mono<StringDictionaryInner> listMetadata() {
        return this.manager().inner().getWebApps().listMetadataAsync(resourceGroupName(), name());
    }

    Mono<StringDictionaryInner> updateMetadata(StringDictionaryInner inner) {
        return this.manager().inner().getWebApps().updateMetadataAsync(resourceGroupName(), name(), inner);
    }
}
