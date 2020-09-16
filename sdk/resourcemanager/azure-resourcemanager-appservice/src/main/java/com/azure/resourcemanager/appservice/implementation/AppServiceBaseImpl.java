// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.CsmPublishingProfileOptions;
import com.azure.resourcemanager.appservice.models.CsmSlotEntity;
import com.azure.resourcemanager.appservice.models.HostnameBinding;
import com.azure.resourcemanager.appservice.models.MSDeploy;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.PublishingProfile;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.resourcemanager.appservice.models.WebAppSourceControl;
import com.azure.resourcemanager.appservice.fluent.inner.ConnectionStringDictionaryInner;
import com.azure.resourcemanager.appservice.fluent.inner.IdentifierInner;
import com.azure.resourcemanager.appservice.fluent.inner.MSDeployStatusInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteAuthSettingsInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteLogsConfigInner;
import com.azure.resourcemanager.appservice.fluent.inner.SitePatchResourceInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteSourceControlInner;
import com.azure.resourcemanager.appservice.fluent.inner.SlotConfigNamesResourceInner;
import com.azure.resourcemanager.appservice.fluent.inner.StringDictionaryInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

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

    private final ClientLogger logger = new ClientLogger(getClass());

    protected static final String SETTING_DOCKER_IMAGE = "DOCKER_CUSTOM_IMAGE_NAME";
    protected static final String SETTING_REGISTRY_SERVER = "DOCKER_REGISTRY_SERVER_URL";
    protected static final String SETTING_REGISTRY_USERNAME = "DOCKER_REGISTRY_SERVER_USERNAME";
    protected static final String SETTING_REGISTRY_PASSWORD = "DOCKER_REGISTRY_SERVER_PASSWORD";

    AppServiceBaseImpl(
        String name,
        SiteInner innerObject,
        SiteConfigResourceInner siteConfig,
        SiteLogsConfigInner logConfig,
        AppServiceManager manager) {
        super(name, innerObject, siteConfig, logConfig, manager);
    }

    @Override
    Mono<SiteInner> createOrUpdateInner(SiteInner site) {
        return this.manager().inner().getWebApps().createOrUpdateAsync(resourceGroupName(), name(), site);
    }

    @Override
    Mono<SiteInner> updateInner(SitePatchResourceInner siteUpdate) {
        return this.manager().inner().getWebApps().updateAsync(resourceGroupName(), name(), siteUpdate);
    }

    @Override
    Mono<SiteInner> getInner() {
        return this.manager().inner().getWebApps().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    Mono<SiteConfigResourceInner> getConfigInner() {
        return this.manager().inner().getWebApps().getConfigurationAsync(resourceGroupName(), name());
    }

    @Override
    Mono<SiteConfigResourceInner> createOrUpdateSiteConfig(SiteConfigResourceInner siteConfig) {
        return this
            .manager()
            .inner()
            .getWebApps()
            .createOrUpdateConfigurationAsync(resourceGroupName(), name(), siteConfig);
    }

    @Override
    Mono<Void> deleteHostnameBinding(String hostname) {
        return this.manager().inner().getWebApps().deleteHostnameBindingAsync(resourceGroupName(), name(), hostname);
    }

    @Override
    Mono<StringDictionaryInner> listAppSettings() {
        return this.manager().inner().getWebApps().listApplicationSettingsAsync(resourceGroupName(), name());
    }

    @Override
    Mono<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner) {
        return this.manager().inner().getWebApps().updateApplicationSettingsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<ConnectionStringDictionaryInner> listConnectionStrings() {
        return this.manager().inner().getWebApps().listConnectionStringsAsync(resourceGroupName(), name());
    }

    @Override
    Mono<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner) {
        return this.manager().inner().getWebApps().updateConnectionStringsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<SlotConfigNamesResourceInner> listSlotConfigurations() {
        return this.manager().inner().getWebApps().listSlotConfigurationNamesAsync(resourceGroupName(), name());
    }

    @Override
    Mono<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner) {
        return this.manager().inner().getWebApps()
            .updateSlotConfigurationNamesAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<SiteSourceControlInner> createOrUpdateSourceControl(SiteSourceControlInner inner) {
        return this.manager().inner().getWebApps().createOrUpdateSourceControlAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<Void> deleteSourceControl() {
        return this.manager().inner().getWebApps().deleteSourceControlAsync(resourceGroupName(), name());
    }

    @Override
    Mono<SiteAuthSettingsInner> updateAuthentication(SiteAuthSettingsInner inner) {
        return manager().inner().getWebApps().updateAuthSettingsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<SiteAuthSettingsInner> getAuthentication() {
        return manager().inner().getWebApps().getAuthSettingsAsync(resourceGroupName(), name());
    }

    @Override
    public Map<String, HostnameBinding> getHostnameBindings() {
        return getHostnameBindingsAsync().block();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, HostnameBinding>> getHostnameBindingsAsync() {
        return this
            .manager()
            .inner()
            .getWebApps()
            .listHostnameBindingsAsync(resourceGroupName(), name())
            .mapPage(
                hostNameBindingInner ->
                    new HostnameBindingImpl<>(hostNameBindingInner, (FluentImplT) AppServiceBaseImpl.this))
            .collectList()
            .map(
                hostNameBindings ->
                    Collections
                        .<String, HostnameBinding>unmodifiableMap(
                            hostNameBindings
                                .stream()
                                .collect(
                                    Collectors
                                        .toMap(
                                            binding -> binding.name().replace(name() + "/", ""),
                                            Function.identity()))));
    }

    @Override
    public PublishingProfile getPublishingProfile() {
        return getPublishingProfileAsync().block();
    }

    public Mono<PublishingProfile> getPublishingProfileAsync() {
        return FluxUtil
            .collectBytesInByteBufferStream(
                manager()
                    .inner()
                    .getWebApps()
                    .listPublishingProfileXmlWithSecretsAsync(
                        resourceGroupName(), name(), new CsmPublishingProfileOptions()))
            .map(
                bytes -> new PublishingProfileImpl(new String(bytes, StandardCharsets.UTF_8), AppServiceBaseImpl.this));
    }

    @Override
    public WebAppSourceControl getSourceControl() {
        return getSourceControlAsync().block();
    }

    @Override
    public Mono<WebAppSourceControl> getSourceControlAsync() {
        return manager()
            .inner()
            .getWebApps()
            .getSourceControlAsync(resourceGroupName(), name())
            .map(
                siteSourceControlInner ->
                    new WebAppSourceControlImpl<>(siteSourceControlInner, AppServiceBaseImpl.this));
    }

    @Override
    Mono<MSDeployStatusInner> createMSDeploy(MSDeploy msDeployInner) {
        return manager().inner().getWebApps().createMSDeployOperationAsync(resourceGroupName(), name(), msDeployInner);
    }

    @Override
    public void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken) {
        verifyDomainOwnershipAsync(certificateOrderName, domainVerificationToken).block();
    }

    @Override
    public Mono<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken) {
        IdentifierInner identifierInner = new IdentifierInner().withValue(domainVerificationToken);
        return this
            .manager()
            .inner()
            .getWebApps()
            .createOrUpdateDomainOwnershipIdentifierAsync(
                resourceGroupName(), name(), certificateOrderName, identifierInner)
            .then(Mono.empty());
    }

    @Override
    public void start() {
        startAsync().block();
    }

    @Override
    public Mono<Void> startAsync() {
        return manager()
            .inner()
            .getWebApps()
            .startAsync(resourceGroupName(), name())
            .then(refreshAsync())
            .then(Mono.empty());
    }

    @Override
    public void stop() {
        stopAsync().block();
    }

    @Override
    public Mono<Void> stopAsync() {
        return manager()
            .inner()
            .getWebApps()
            .stopAsync(resourceGroupName(), name())
            .then(refreshAsync())
            .then(Mono.empty());
    }

    @Override
    public void restart() {
        restartAsync().block();
    }

    @Override
    public Mono<Void> restartAsync() {
        return manager()
            .inner()
            .getWebApps()
            .restartAsync(resourceGroupName(), name())
            .then(refreshAsync())
            .then(Mono.empty());
    }

    @Override
    public void swap(String slotName) {
        swapAsync(slotName).block();
    }

    @Override
    public Mono<Void> swapAsync(String slotName) {
        return manager()
            .inner()
            .getWebApps()
            .swapSlotWithProductionAsync(resourceGroupName(), name(), new CsmSlotEntity().withTargetSlot(slotName))
            .then(refreshAsync())
            .then(Mono.empty());
    }

    @Override
    public void applySlotConfigurations(String slotName) {
        applySlotConfigurationsAsync(slotName).block();
    }

    @Override
    public Mono<Void> applySlotConfigurationsAsync(String slotName) {
        return manager()
            .inner()
            .getWebApps()
            .applySlotConfigToProductionAsync(resourceGroupName(), name(), new CsmSlotEntity().withTargetSlot(slotName))
            .then(refreshAsync())
            .then(Mono.empty());
    }

    @Override
    public void resetSlotConfigurations() {
        resetSlotConfigurationsAsync().block();
    }

    @Override
    public Mono<Void> resetSlotConfigurationsAsync() {
        return manager()
            .inner()
            .getWebApps()
            .resetProductionSlotConfigAsync(resourceGroupName(), name())
            .then(refreshAsync())
            .then(Mono.empty());
    }

    @Override
    public byte[] getContainerLogs() {
        return getContainerLogsAsync().block();
    }

    @Override
    public Mono<byte[]> getContainerLogsAsync() {
        return FluxUtil
            .collectBytesInByteBufferStream(
                manager().inner().getWebApps().getWebSiteContainerLogsAsync(resourceGroupName(), name()));
    }

    @Override
    public byte[] getContainerLogsZip() {
        return getContainerLogsZipAsync().block();
    }

    @Override
    public Mono<byte[]> getContainerLogsZipAsync() {
        return FluxUtil
            .collectBytesInByteBufferStream(
                manager().inner().getWebApps().getContainerLogsZipAsync(resourceGroupName(), name()));
    }

    @Override
    Mono<SiteLogsConfigInner> updateDiagnosticLogsConfig(SiteLogsConfigInner siteLogsConfigInner) {
        return manager()
            .inner()
            .getWebApps()
            .updateDiagnosticLogsConfigAsync(resourceGroupName(), name(), siteLogsConfigInner);
    }

    private AppServicePlanImpl newDefaultAppServicePlan() {
        String planName = this.manager().sdkContext().randomResourceName(name() + "plan", 32);
        return newDefaultAppServicePlan(planName);
    }

    private AppServicePlanImpl newDefaultAppServicePlan(String appServicePlanName) {
        AppServicePlanImpl appServicePlan =
            (AppServicePlanImpl) (this.manager().appServicePlans().define(appServicePlanName)).withRegion(regionName());
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
        return withNewAppServicePlan(
            newDefaultAppServicePlan().withOperatingSystem(operatingSystem).withPricingTier(pricingTier));
    }

    FluentImplT withNewAppServicePlan(
        String appServicePlanName, OperatingSystem operatingSystem, PricingTier pricingTier) {
        return withNewAppServicePlan(
            newDefaultAppServicePlan(appServicePlanName)
                .withOperatingSystem(operatingSystem)
                .withPricingTier(pricingTier));
    }

    public FluentImplT withNewAppServicePlan(PricingTier pricingTier) {
        return withNewAppServicePlan(operatingSystem(), pricingTier);
    }

    public FluentImplT withNewAppServicePlan(String appServicePlanName, PricingTier pricingTier) {
        return withNewAppServicePlan(appServicePlanName, operatingSystem(), pricingTier);
    }

    public FluentImplT withNewAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable) {
        this.addDependency(appServicePlanCreatable);
        String id =
            ResourceUtils
                .constructResourceId(
                    this.manager().subscriptionId(),
                    resourceGroupName(),
                    "Microsoft.Web",
                    "serverFarms",
                    appServicePlanCreatable.name(),
                    "");
        inner().withServerFarmId(id);
        if (appServicePlanCreatable instanceof AppServicePlanImpl) {
            return withOperatingSystem(((AppServicePlanImpl) appServicePlanCreatable).operatingSystem());
        } else {
            throw logger.logExceptionAsError(
                new IllegalStateException("Internal error, appServicePlanCreatable must be class AppServicePlanImpl"));
        }
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
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Docker container settings only apply to Linux app service plans."));
        }
    }

    protected OperatingSystem appServicePlanOperatingSystem(AppServicePlan appServicePlan) {
        return appServicePlan.operatingSystem();
    }
}
