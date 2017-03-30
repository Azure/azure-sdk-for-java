/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.DeploymentSlots;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.PricingTier;
import com.microsoft.azure.management.appservice.RuntimeStack;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

/**
 * The implementation for WebApp.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class WebAppImpl
        extends AppServiceBaseImpl<WebApp, WebAppImpl, WebApp.DefinitionStages.WithCreate, WebApp.Update>
        implements
            WebApp,
            WebApp.Definition,
            WebApp.DefinitionStages.ExistingWindowsPlanWithGroup,
            WebApp.DefinitionStages.ExistingLinuxPlanWithGroup,
            WebApp.Update,
            WebApp.UpdateStages.WithCredentials,
            WebApp.UpdateStages.WithStartUpCommand {

    private static final String SETTING_DOCKER_IMAGE = "DOCKER_CUSTOM_IMAGE_NAME";
    private static final String SETTING_REGISTRY_SERVER = "DOCKER_REGISTRY_SERVER_URL";
    private static final String SETTING_REGISTRY_USERNAME = "DOCKER_REGISTRY_SERVER_USERNAME";
    private static final String SETTING_REGISTRY_PASSWORD = "DOCKER_REGISTRY_SERVER_PASSWORD";

    private DeploymentSlots deploymentSlots;

    WebAppImpl(String name, SiteInner innerObject, SiteConfigResourceInner configObject, AppServiceManager manager) {
        super(name, innerObject, configObject, manager);
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
    public WebAppImpl withPublicDockerHubImage(String imageAndTag) {
        ensureLinuxPlan();
        cleanUpContainerSettings();
        withBuiltInImage(RuntimeStack.NODEJS_6_6_0);
        withAppSetting(SETTING_DOCKER_IMAGE, imageAndTag);
        return this;
    }

    @Override
    public WebAppImpl withPrivateDockerHubImage(String imageAndTag) {
        return withPublicDockerHubImage(imageAndTag);
    }

    @Override
    public WebAppImpl withPrivateRegistryImage(String imageAndTag, String serverUrl) {
        ensureLinuxPlan();
        cleanUpContainerSettings();
        withBuiltInImage(RuntimeStack.NODEJS_6_6_0);
        withAppSetting(SETTING_DOCKER_IMAGE, imageAndTag);
        withAppSetting(SETTING_REGISTRY_SERVER, serverUrl);
        return this;
    }

    @Override
    public WebAppImpl withCredentials(String username, String password) {
        withAppSetting(SETTING_REGISTRY_USERNAME, username);
        withAppSetting(SETTING_REGISTRY_PASSWORD, password);
        return this;
    }

    private void ensureLinuxPlan() {
        if (OperatingSystem.WINDOWS.equals(operatingSystem())) {
            throw new IllegalArgumentException("Docker container settings only apply to Linux app service plans.");
        }
    }

    private void cleanUpContainerSettings() {
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
    public WebAppImpl withNewWindowsPlan(Creatable<AppServicePlan> appServicePlanCreatable) {
        return super.withNewAppServicePlan(appServicePlanCreatable);
    }

    @Override
    public WebAppImpl withNewLinuxPlan(PricingTier pricingTier) {
        return super.withNewAppServicePlan(OperatingSystem.LINUX, pricingTier);
    }

    @Override
    public WebAppImpl withNewLinuxPlan(Creatable<AppServicePlan> appServicePlanCreatable) {
        return super.withNewAppServicePlan(appServicePlanCreatable);
    }
}