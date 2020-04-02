/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.management.appservice.AppServicePlan;
import com.azure.management.appservice.CsmPublishingProfileOptions;
import com.azure.management.appservice.CsmSlotEntity;
import com.azure.management.appservice.HostNameBinding;
import com.azure.management.appservice.MSDeploy;
import com.azure.management.appservice.OperatingSystem;
import com.azure.management.appservice.PricingTier;
import com.azure.management.appservice.PublishingProfile;
import com.azure.management.appservice.WebAppBase;
import com.azure.management.appservice.WebAppSourceControl;
import com.azure.management.appservice.models.ConnectionStringDictionaryInner;
import com.azure.management.appservice.models.IdentifierInner;
import com.azure.management.appservice.models.MSDeployStatusInner;
import com.azure.management.appservice.models.SiteAuthSettingsInner;
import com.azure.management.appservice.models.SiteConfigResourceInner;
import com.azure.management.appservice.models.SiteInner;
import com.azure.management.appservice.models.SiteLogsConfigInner;
import com.azure.management.appservice.models.SitePatchResourceInner;
import com.azure.management.appservice.models.SiteSourceControlInner;
import com.azure.management.appservice.models.SlotConfigNamesResourceInner;
import com.azure.management.appservice.models.StringDictionaryInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.model.Creatable;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The base implementation for web apps and function apps.
 *
 * @param <FluentT> the fluent interface, WebApp or FunctionApp
 * @param <FluentImplT> the implementation class for FluentT
 * @param <FluentWithCreateT> the definition stage that derives from Creatable
 * @param <FluentUpdateT> The definition stage that derives from Appliable
 */
abstract class AppServiceBaseImpl<
    FluentT extends WebAppBase,
    FluentImplT extends AppServiceBaseImpl<FluentT, FluentImplT, FluentWithCreateT, FluentUpdateT>,
    FluentWithCreateT,
    FluentUpdateT>
        extends WebAppBaseImpl<FluentT, FluentImplT> {

    protected static final String SETTING_DOCKER_IMAGE = "DOCKER_CUSTOM_IMAGE_NAME";
    protected static final String SETTING_REGISTRY_SERVER = "DOCKER_REGISTRY_SERVER_URL";
    protected static final String SETTING_REGISTRY_USERNAME = "DOCKER_REGISTRY_SERVER_USERNAME";
    protected static final String SETTING_REGISTRY_PASSWORD = "DOCKER_REGISTRY_SERVER_PASSWORD";

    AppServiceBaseImpl(String name, SiteInner innerObject, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig, AppServiceManager manager) {
        super(name, innerObject, siteConfig, logConfig, manager);
    }

    @Override
    Mono<SiteInner> createOrUpdateInner(SiteInner site) {
        return this.manager().inner().webApps().createOrUpdateAsync(resourceGroupName(), name(), site);
    }

    @Override
    Mono<SiteInner> updateInner(SitePatchResourceInner siteUpdate) {
        return this.manager().inner().webApps().updateAsync(resourceGroupName(), name(), siteUpdate);
    }

    @Override
    Mono<SiteInner> getInner() {
        return this.manager().inner().webApps().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    Mono<SiteConfigResourceInner> getConfigInner() {
        return this.manager().inner().webApps().getConfigurationAsync(resourceGroupName(), name());
    }

    @Override
    Mono<SiteConfigResourceInner> createOrUpdateSiteConfig(SiteConfigResourceInner siteConfig) {
        return this.manager().inner().webApps().createOrUpdateConfigurationAsync(resourceGroupName(), name(), siteConfig);
    }

    @Override
    Mono<Void> deleteHostNameBinding(String hostname) {
        return this.manager().inner().webApps().deleteHostNameBindingAsync(resourceGroupName(), name(), hostname);
    }

    @Override
    Mono<StringDictionaryInner> listAppSettings() {
        return this.manager().inner().webApps().listApplicationSettingsAsync(resourceGroupName(), name());
    }

    @Override
    Mono<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner) {
        return this.manager().inner().webApps().updateApplicationSettingsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<ConnectionStringDictionaryInner> listConnectionStrings() {
        return this.manager().inner().webApps().listConnectionStringsAsync(resourceGroupName(), name());
    }

    @Override
    Mono<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner) {
        return this.manager().inner().webApps().updateConnectionStringsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<SlotConfigNamesResourceInner> listSlotConfigurations() {
        return this.manager().inner().webApps().listSlotConfigurationNamesAsync(resourceGroupName(), name());
    }

    @Override
    Mono<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner) {
        return this.manager().inner().webApps().updateSlotConfigurationNamesAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<SiteSourceControlInner> createOrUpdateSourceControl(SiteSourceControlInner inner) {
        return this.manager().inner().webApps().createOrUpdateSourceControlAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<Void> deleteSourceControl() {
        return this.manager().inner().webApps().deleteSourceControlAsync(resourceGroupName(), name());
    }

    @Override
    Mono<SiteAuthSettingsInner> updateAuthentication(SiteAuthSettingsInner inner) {
        return manager().inner().webApps().updateAuthSettingsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<SiteAuthSettingsInner> getAuthentication() {
        return manager().inner().webApps().getAuthSettingsAsync(resourceGroupName(), name());
    }

    @Override
    public Map<String, HostNameBinding> getHostNameBindings() {
        return getHostNameBindingsAsync().block();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, HostNameBinding>> getHostNameBindingsAsync() {
        return this.manager().inner().webApps().listHostNameBindingsAsync(resourceGroupName(), name())
                .mapPage(hostNameBindingInner -> new HostNameBindingImpl<>(hostNameBindingInner, (FluentImplT) AppServiceBaseImpl.this))
                .collectList()
                .map(hostNameBindings -> Collections.<String, HostNameBinding>unmodifiableMap(hostNameBindings.stream()
                        .collect(Collectors.toMap(binding -> binding.name().replace(name() + "/", ""),
                                Function.identity()))));
    }

    @Override
    public PublishingProfile getPublishingProfile() {
        return getPublishingProfileAsync().block();
    }

    public Mono<PublishingProfile> getPublishingProfileAsync() {
        return FluxUtil.collectBytesInByteBufferStream(
                manager().inner().webApps().listPublishingProfileXmlWithSecretsAsync(resourceGroupName(), name(), new CsmPublishingProfileOptions()))
                .map(bytes -> new PublishingProfileImpl(new String(bytes, StandardCharsets.UTF_8), AppServiceBaseImpl.this));
    }

    @Override
    public WebAppSourceControl getSourceControl() {
        return getSourceControlAsync().block();
    }

    @Override
    public Mono<WebAppSourceControl> getSourceControlAsync() {
        return manager().inner().webApps().getSourceControlAsync(resourceGroupName(), name())
                .map(siteSourceControlInner -> new WebAppSourceControlImpl<>(siteSourceControlInner, AppServiceBaseImpl.this));
    }

    @Override
    Mono<MSDeployStatusInner> createMSDeploy(MSDeploy msDeployInner) {
        return manager().inner().webApps()
                .createMSDeployOperationAsync(resourceGroupName(), name(), msDeployInner);
    }

    @Override
    public void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken) {
        verifyDomainOwnershipAsync(certificateOrderName, domainVerificationToken).block();
    }

    @Override
    public Mono<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken) {
        IdentifierInner identifierInner = new IdentifierInner().withValue(domainVerificationToken);
        return this.manager().inner().webApps().createOrUpdateDomainOwnershipIdentifierAsync(resourceGroupName(), name(), certificateOrderName, identifierInner)
                .then(Mono.empty());
    }

    @Override
    public void start() {
        startAsync().block();
    }

    @Override
    public Mono<Void> startAsync() {
        return manager().inner().webApps().startAsync(resourceGroupName(), name())
                .then(refreshAsync())
                .then(Mono.empty());
    }

    @Override
    public void stop() {
        stopAsync().block();
    }

    @Override
    public Mono<Void> stopAsync() {
        return manager().inner().webApps().stopAsync(resourceGroupName(), name())
                .then(refreshAsync())
                .then(Mono.empty());
    }

    @Override
    public void restart() {
        restartAsync().block();
    }

    @Override
    public Mono<Void> restartAsync() {
        return manager().inner().webApps().restartAsync(resourceGroupName(), name())
                .then(refreshAsync())
                .then(Mono.empty());
    }

    @Override
    public void swap(String slotName) {
        swapAsync(slotName).block();
    }

    @Override
    public Mono<Void> swapAsync(String slotName) {
        return manager().inner().webApps().swapSlotWithProductionAsync(resourceGroupName(), name(), new CsmSlotEntity().withTargetSlot(slotName))
                .then(refreshAsync())
                .then(Mono.empty());
    }

    @Override
    public void applySlotConfigurations(String slotName) {
        applySlotConfigurationsAsync(slotName).block();
    }

    @Override
    public Mono<Void> applySlotConfigurationsAsync(String slotName) {
        return manager().inner().webApps().applySlotConfigToProductionAsync(resourceGroupName(), name(), new CsmSlotEntity().withTargetSlot(slotName))
                .then(refreshAsync())
                .then(Mono.empty());
    }

    @Override
    public void resetSlotConfigurations() {
        resetSlotConfigurationsAsync().block();
    }

    @Override
    public Mono<Void> resetSlotConfigurationsAsync() {
        return manager().inner().webApps().resetProductionSlotConfigAsync(resourceGroupName(), name())
                .then(refreshAsync())
                .then(Mono.empty());
    }

    @Override
    public byte[] getContainerLogs() {
        return getContainerLogsAsync().block();
    }

    @Override
    public Mono<byte[]> getContainerLogsAsync() {
        return FluxUtil.collectBytesInByteBufferStream(manager().inner().webApps().getWebSiteContainerLogsAsync(resourceGroupName(), name()));
    }

    @Override
    public byte[] getContainerLogsZip() {
        return getContainerLogsZipAsync().block();
    }

    @Override
    public Mono<byte[]> getContainerLogsZipAsync() {
        return FluxUtil.collectBytesInByteBufferStream(manager().inner().webApps().getContainerLogsZipAsync(resourceGroupName(), name()));
    }

    @Override
    Mono<SiteLogsConfigInner> updateDiagnosticLogsConfig(SiteLogsConfigInner siteLogsConfigInner) {
        return manager().inner().webApps().updateDiagnosticLogsConfigAsync(resourceGroupName(), name(), siteLogsConfigInner);
    }

    private AppServicePlanImpl newDefaultAppServicePlan() {
        String planName = this.manager().getSdkContext().randomResourceName(name() + "plan", 32);
        return newDefaultAppServicePlan(planName);
    }

    private AppServicePlanImpl newDefaultAppServicePlan(String appServicePlanName) {
        AppServicePlanImpl appServicePlan = (AppServicePlanImpl) (this.manager().appServicePlans()
                .define(appServicePlanName))
                .withRegion(regionName());
        if (super.creatableGroup != null && isInCreateMode()) {
            appServicePlan = appServicePlan.withNewResourceGroup(super.creatableGroup);
        } else {
            appServicePlan = appServicePlan.withExistingResourceGroup(resourceGroupName());
        }
        return appServicePlan;
    }

    public FluentImplT withNewFreeAppServicePlan() {
        return withNewAppServicePlan(OperatingSystem.WINDOWS, PricingTier.FREE_F1);
    }

    public FluentImplT withNewSharedAppServicePlan() {
        return withNewAppServicePlan(OperatingSystem.WINDOWS, PricingTier.SHARED_D1);
    }

    FluentImplT withNewAppServicePlan(OperatingSystem operatingSystem, PricingTier pricingTier) {
        return withNewAppServicePlan(newDefaultAppServicePlan().withOperatingSystem(operatingSystem).withPricingTier(pricingTier));
    }

    FluentImplT withNewAppServicePlan(String appServicePlanName, OperatingSystem operatingSystem, PricingTier pricingTier) {
        return withNewAppServicePlan(newDefaultAppServicePlan(appServicePlanName).withOperatingSystem(operatingSystem).withPricingTier(pricingTier));
    }

    public FluentImplT withNewAppServicePlan(PricingTier pricingTier) {
        return withNewAppServicePlan(operatingSystem(), pricingTier);
    }

    public FluentImplT withNewAppServicePlan(String appServicePlanName, PricingTier pricingTier) {
        return withNewAppServicePlan(appServicePlanName, operatingSystem(), pricingTier);
    }

    public FluentImplT withNewAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable) {
        this.addDependency(appServicePlanCreatable);
        String id = ResourceUtils.constructResourceId(this.manager().getSubscriptionId(),
                resourceGroupName(), "Microsoft.Web", "serverFarms", appServicePlanCreatable.name(), "");
        inner().withServerFarmId(id);
        return withOperatingSystem(((AppServicePlanImpl) appServicePlanCreatable).operatingSystem());
    }

    @SuppressWarnings("unchecked")
    private FluentImplT withOperatingSystem(OperatingSystem os) {
        if (os == OperatingSystem.LINUX) {
            inner().withReserved(true);
            inner().withKind(inner().kind() + ",linux");
        }
        return (FluentImplT) this;
    }

    public FluentImplT withExistingAppServicePlan(AppServicePlan appServicePlan) {
        inner().withServerFarmId(appServicePlan.id());
        this.withRegion(appServicePlan.regionName());
        return withOperatingSystem(appServicePlanOperatingSystem(appServicePlan));
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withPublicDockerHubImage(String imageAndTag) {
        cleanUpContainerSettings();
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withLinuxFxVersion(String.format("DOCKER|%s", imageAndTag));
        withAppSetting(SETTING_DOCKER_IMAGE, imageAndTag);
        return (FluentImplT) this;
    }

    public FluentImplT withPrivateDockerHubImage(String imageAndTag) {
        return withPublicDockerHubImage(imageAndTag);
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withPrivateRegistryImage(String imageAndTag, String serverUrl) {
        imageAndTag = Utils.smartCompletionPrivateRegistryImage(imageAndTag, serverUrl);

        cleanUpContainerSettings();
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withLinuxFxVersion(String.format("DOCKER|%s", imageAndTag));
        withAppSetting(SETTING_DOCKER_IMAGE, imageAndTag);
        withAppSetting(SETTING_REGISTRY_SERVER, serverUrl);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withCredentials(String username, String password) {
        withAppSetting(SETTING_REGISTRY_USERNAME, username);
        withAppSetting(SETTING_REGISTRY_PASSWORD, password);
        return (FluentImplT) this;
    }

    protected abstract void cleanUpContainerSettings();

    protected void ensureLinuxPlan() {
        if (OperatingSystem.WINDOWS.equals(operatingSystem())) {
            throw new IllegalArgumentException("Docker container settings only apply to Linux app service plans.");
        }
    }

    protected OperatingSystem appServicePlanOperatingSystem(AppServicePlan appServicePlan) {
        return appServicePlan.operatingSystem();
    }
}