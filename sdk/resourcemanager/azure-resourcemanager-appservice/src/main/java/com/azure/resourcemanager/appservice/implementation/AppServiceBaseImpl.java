// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
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
import com.azure.resourcemanager.appservice.models.PrivateLinkConnectionApprovalRequestResource;
import com.azure.resourcemanager.appservice.models.PrivateLinkConnectionState;
import com.azure.resourcemanager.appservice.models.PublishingProfile;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.resourcemanager.appservice.models.WebAppSourceControl;
import com.azure.resourcemanager.appservice.fluent.models.ConnectionStringDictionaryInner;
import com.azure.resourcemanager.appservice.fluent.models.IdentifierInner;
import com.azure.resourcemanager.appservice.fluent.models.MSDeployStatusInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteAuthSettingsInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteLogsConfigInner;
import com.azure.resourcemanager.appservice.fluent.models.SitePatchResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteSourceControlInner;
import com.azure.resourcemanager.appservice.fluent.models.SlotConfigNamesResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.StringDictionaryInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingPrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingPrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsUpdatingPrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

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
    extends WebAppBaseImpl<FluentT, FluentImplT>
    implements
    SupportsListingPrivateLinkResource,
    SupportsListingPrivateEndpointConnection,
    SupportsUpdatingPrivateEndpointConnection {

    private final ClientLogger logger = new ClientLogger(getClass());

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
        return this.manager().serviceClient().getWebApps().createOrUpdateAsync(resourceGroupName(), name(), site);
    }

    @Override
    Mono<SiteInner> updateInner(SitePatchResourceInner siteUpdate) {
        return this.manager().serviceClient().getWebApps().updateAsync(resourceGroupName(), name(), siteUpdate);
    }

    @Override
    Mono<SiteInner> getInner() {
        return this.manager().serviceClient().getWebApps().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    Mono<SiteConfigResourceInner> getConfigInner() {
        return this.manager().serviceClient().getWebApps().getConfigurationAsync(resourceGroupName(), name());
    }

    @Override
    Mono<SiteConfigResourceInner> createOrUpdateSiteConfig(SiteConfigResourceInner siteConfig) {
        return this
            .manager()
            .serviceClient()
            .getWebApps()
            .createOrUpdateConfigurationAsync(resourceGroupName(), name(), siteConfig);
    }

    @Override
    Mono<Void> deleteHostnameBinding(String hostname) {
        return this.manager().serviceClient().getWebApps()
            .deleteHostnameBindingAsync(resourceGroupName(), name(), hostname);
    }

    @Override
    Mono<StringDictionaryInner> listAppSettings() {
        return this.manager().serviceClient().getWebApps().listApplicationSettingsAsync(resourceGroupName(), name());
    }

    @Override
    Mono<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner) {
        return this.manager().serviceClient().getWebApps()
            .updateApplicationSettingsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<ConnectionStringDictionaryInner> listConnectionStrings() {
        return this.manager().serviceClient().getWebApps().listConnectionStringsAsync(resourceGroupName(), name());
    }

    @Override
    Mono<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner) {
        return this.manager().serviceClient().getWebApps()
            .updateConnectionStringsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<SlotConfigNamesResourceInner> listSlotConfigurations() {
        return this.manager().serviceClient().getWebApps().listSlotConfigurationNamesAsync(resourceGroupName(), name());
    }

    @Override
    Mono<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner) {
        return this.manager().serviceClient().getWebApps()
            .updateSlotConfigurationNamesAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<SiteSourceControlInner> createOrUpdateSourceControl(SiteSourceControlInner inner) {
        return this.manager().serviceClient().getWebApps()
            .createOrUpdateSourceControlAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<Void> deleteSourceControl() {
        return this.manager().serviceClient().getWebApps().deleteSourceControlAsync(resourceGroupName(), name());
    }

    @Override
    Mono<SiteAuthSettingsInner> updateAuthentication(SiteAuthSettingsInner inner) {
        return manager().serviceClient().getWebApps().updateAuthSettingsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Mono<SiteAuthSettingsInner> getAuthentication() {
        return manager().serviceClient().getWebApps().getAuthSettingsAsync(resourceGroupName(), name());
    }

    @Override
    public Map<String, HostnameBinding> getHostnameBindings() {
        return getHostnameBindingsAsync().block();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, HostnameBinding>> getHostnameBindingsAsync() {
        return PagedConverter.mapPage(this
            .manager()
            .serviceClient()
            .getWebApps()
            .listHostnameBindingsAsync(resourceGroupName(), name()),
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
                    .serviceClient()
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
            .serviceClient()
            .getWebApps()
            .getSourceControlAsync(resourceGroupName(), name())
            .map(
                siteSourceControlInner ->
                    new WebAppSourceControlImpl<>(siteSourceControlInner, AppServiceBaseImpl.this));
    }

    @Override
    Mono<MSDeployStatusInner> createMSDeploy(MSDeploy msDeployInner) {
        return manager().serviceClient().getWebApps()
            .createMSDeployOperationAsync(resourceGroupName(), name(), msDeployInner);
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
            .serviceClient()
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
            .serviceClient()
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
            .serviceClient()
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
            .serviceClient()
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
            .serviceClient()
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
            .serviceClient()
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
            .serviceClient()
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
                manager().serviceClient().getWebApps().getWebSiteContainerLogsAsync(resourceGroupName(), name()));
    }

    @Override
    public byte[] getContainerLogsZip() {
        return getContainerLogsZipAsync().block();
    }

    @Override
    public Mono<byte[]> getContainerLogsZipAsync() {
        return FluxUtil
            .collectBytesInByteBufferStream(
                manager().serviceClient().getWebApps().getContainerLogsZipAsync(resourceGroupName(), name()));
    }

    @Override
    Mono<SiteLogsConfigInner> updateDiagnosticLogsConfig(SiteLogsConfigInner siteLogsConfigInner) {
        return manager()
            .serviceClient()
            .getWebApps()
            .updateDiagnosticLogsConfigAsync(resourceGroupName(), name(), siteLogsConfigInner);
    }

    private AppServicePlanImpl newDefaultAppServicePlan() {
        String planName = this.manager().resourceManager().internalContext().randomResourceName(name() + "plan", 32);
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
        innerModel().withServerFarmId(id);
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
            innerModel().withReserved(true);
            innerModel().withKind(innerModel().kind() + ",linux");
        }
        return (FluentImplT) this;
    }

    public FluentImplT withExistingAppServicePlan(AppServicePlan appServicePlan) {
        innerModel().withServerFarmId(appServicePlan.id());
        this.withRegion(appServicePlan.regionName());
        return withOperatingSystem(appServicePlanOperatingSystem(appServicePlan));
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withPublicDockerHubImage(String imageAndTag) {
        cleanUpContainerSettings();
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        setAppFrameworkVersion(String.format("DOCKER|%s", imageAndTag));
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
        setAppFrameworkVersion(String.format("DOCKER|%s", imageAndTag));
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

    @Override
    public PagedIterable<PrivateLinkResource> listPrivateLinkResources() {
        return new PagedIterable<>(listPrivateLinkResourcesAsync());
    }

    @Override
    public PagedFlux<PrivateLinkResource> listPrivateLinkResourcesAsync() {
        Mono<Response<List<PrivateLinkResource>>> retList = this.manager().serviceClient().getWebApps()
            .getPrivateLinkResourcesWithResponseAsync(this.resourceGroupName(), this.name())
            .map(response -> new SimpleResponse<>(response, response.getValue().value().stream()
                .map(PrivateLinkResourceImpl::new)
                .collect(Collectors.toList())));

        return PagedConverter.convertListToPagedFlux(retList);
    }

    @Override
    public PagedIterable<PrivateEndpointConnection> listPrivateEndpointConnections() {
        return new PagedIterable<>(listPrivateEndpointConnectionsAsync());
    }

    @Override
    public PagedFlux<PrivateEndpointConnection> listPrivateEndpointConnectionsAsync() {
        return PagedConverter.mapPage(this.manager().serviceClient().getWebApps()
            .getPrivateEndpointConnectionListAsync(this.resourceGroupName(), this.name()),
            PrivateEndpointConnectionImpl::new);
    }

    @Override
    public void approvePrivateEndpointConnection(String privateEndpointConnectionName) {
        approvePrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> approvePrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return this.manager().serviceClient().getWebApps()
            .approveOrRejectPrivateEndpointConnectionAsync(this.resourceGroupName(), this.name(),
                privateEndpointConnectionName,
                new PrivateLinkConnectionApprovalRequestResource().withPrivateLinkServiceConnectionState(
                    new PrivateLinkConnectionState()
                        .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED.toString())
                ))
            .then();
    }

    @Override
    public void rejectPrivateEndpointConnection(String privateEndpointConnectionName) {
        rejectPrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> rejectPrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return this.manager().serviceClient().getWebApps()
            .approveOrRejectPrivateEndpointConnectionAsync(this.resourceGroupName(), this.name(),
                privateEndpointConnectionName,
                new PrivateLinkConnectionApprovalRequestResource().withPrivateLinkServiceConnectionState(
                    new PrivateLinkConnectionState()
                        .withStatus(PrivateEndpointServiceConnectionStatus.REJECTED.toString())
                ))
            .then();
    }
}
