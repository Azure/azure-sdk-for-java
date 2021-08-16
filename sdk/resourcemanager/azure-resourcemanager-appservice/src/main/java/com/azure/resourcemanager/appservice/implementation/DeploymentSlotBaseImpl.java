// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.resourcemanager.appservice.models.AppSetting;
import com.azure.resourcemanager.appservice.models.ConnectionString;
import com.azure.resourcemanager.appservice.models.CsmPublishingProfileOptions;
import com.azure.resourcemanager.appservice.models.CsmSlotEntity;
import com.azure.resourcemanager.appservice.models.DeploymentSlotBase;
import com.azure.resourcemanager.appservice.models.HostnameBinding;
import com.azure.resourcemanager.appservice.models.MSDeploy;
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
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation for DeploymentSlot. */
abstract class DeploymentSlotBaseImpl<
        FluentT extends WebAppBase,
        FluentImplT extends DeploymentSlotBaseImpl<FluentT, FluentImplT, ParentImplT, FluentWithCreateT, FluentUpdateT>,
        ParentImplT extends AppServiceBaseImpl<?, ?, ?, ?>,
        FluentWithCreateT,
        FluentUpdateT>
    extends WebAppBaseImpl<FluentT, FluentImplT>
    implements DeploymentSlotBase<FluentT>, DeploymentSlotBase.Update<FluentT> {
    private final ParentImplT parent;
    private final String name;
    WebAppBase configurationSource;

    DeploymentSlotBaseImpl(
        String name,
        SiteInner innerObject,
        SiteConfigResourceInner siteConfig,
        SiteLogsConfigInner logConfig,
        final ParentImplT parent) {
        super(name.replaceAll(".*/", ""), innerObject, siteConfig, logConfig, parent.manager());
        this.name = name.replaceAll(".*/", "");
        this.parent = parent;
        innerModel().withServerFarmId(parent.appServicePlanId());
        innerModel().withLocation(regionName());
    }

    @Override
    public String name() {
        return name;
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
            .listHostnameBindingsSlotAsync(resourceGroupName(), parent().name(), name()),
                hostNameBindingInner ->
                    new HostnameBindingImpl<FluentT, FluentImplT>(
                        hostNameBindingInner, (FluentImplT) DeploymentSlotBaseImpl.this))
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
                    .listPublishingProfileXmlWithSecretsSlotAsync(
                        resourceGroupName(), this.parent().name(), name(), new CsmPublishingProfileOptions()))
            .map(bytes -> new PublishingProfileImpl(new String(bytes, StandardCharsets.UTF_8), this));
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
            .startSlotAsync(resourceGroupName(), this.parent().name(), name())
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
            .stopSlotAsync(resourceGroupName(), this.parent().name(), name())
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
            .restartSlotAsync(resourceGroupName(), this.parent().name(), name())
            .then(refreshAsync())
            .then(Mono.empty());
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withBrandNewConfiguration() {
        this.siteConfig = null;
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withConfigurationFromDeploymentSlot(FluentT slot) {
        this.siteConfig = ((WebAppBaseImpl) slot).siteConfig;
        configurationSource = slot;
        return (FluentImplT) this;
    }

    Mono<Indexable> submitAppSettings() {
        return Mono
            .justOrEmpty(configurationSource)
            .flatMap(
                webAppBase -> {
                    if (!isInCreateMode()) {
                        return DeploymentSlotBaseImpl.super.submitAppSettings();
                    }
                    return webAppBase
                        .getAppSettingsAsync()
                        .flatMap(
                            stringAppSettingMap -> {
                                for (AppSetting appSetting : stringAppSettingMap.values()) {
                                    if (appSetting.sticky()) {
                                        withStickyAppSetting(appSetting.key(), appSetting.value());
                                    } else {
                                        withAppSetting(appSetting.key(), appSetting.value());
                                    }
                                }
                                return DeploymentSlotBaseImpl.super.submitAppSettings();
                            });
                })
            .switchIfEmpty(DeploymentSlotBaseImpl.super.submitAppSettings());
    }

    Mono<Indexable> submitConnectionStrings() {
        return Mono
            .justOrEmpty(configurationSource)
            .flatMap(
                webAppBase -> {
                    if (!isInCreateMode()) {
                        return DeploymentSlotBaseImpl.super.submitConnectionStrings();
                    }
                    return webAppBase
                        .getConnectionStringsAsync()
                        .flatMap(
                            stringConnectionStringMap -> {
                                for (ConnectionString connectionString : stringConnectionStringMap.values()) {
                                    if (connectionString.sticky()) {
                                        withStickyConnectionString(
                                            connectionString.name(), connectionString.value(),
                                            connectionString.type());
                                    } else {
                                        withConnectionString(
                                            connectionString.name(), connectionString.value(),
                                            connectionString.type());
                                    }
                                }
                                return DeploymentSlotBaseImpl.super.submitConnectionStrings();
                            });
                })
            .switchIfEmpty(DeploymentSlotBaseImpl.super.submitConnectionStrings());
    }

    public ParentImplT parent() {
        return this.parent;
    }

    @Override
    Mono<SiteInner> createOrUpdateInner(SiteInner site) {
        return manager()
            .serviceClient()
            .getWebApps()
            .createOrUpdateSlotAsync(resourceGroupName(), this.parent().name(), name(), site);
    }

    @Override
    Mono<SiteInner> updateInner(SitePatchResourceInner siteUpdate) {
        return manager()
            .serviceClient()
            .getWebApps()
            .updateSlotAsync(resourceGroupName(), this.parent().name(), name(), siteUpdate);
    }

    @Override
    Mono<SiteInner> getInner() {
        return manager().serviceClient().getWebApps().getSlotAsync(resourceGroupName(), this.parent().name(), name());
    }

    @Override
    Mono<SiteConfigResourceInner> getConfigInner() {
        return manager().serviceClient().getWebApps()
            .getConfigurationSlotAsync(resourceGroupName(), parent().name(), name());
    }

    @Override
    Mono<SiteConfigResourceInner> createOrUpdateSiteConfig(SiteConfigResourceInner siteConfig) {
        return manager()
            .serviceClient()
            .getWebApps()
            .createOrUpdateConfigurationSlotAsync(resourceGroupName(), this.parent().name(), name(), siteConfig);
    }

    @Override
    Mono<Void> deleteHostnameBinding(String hostname) {
        return manager()
            .serviceClient()
            .getWebApps()
            .deleteHostnameBindingSlotAsync(resourceGroupName(), parent().name(), name(), hostname);
    }

    @Override
    Mono<StringDictionaryInner> listAppSettings() {
        return manager()
            .serviceClient()
            .getWebApps()
            .listApplicationSettingsSlotAsync(resourceGroupName(), parent().name(), name());
    }

    @Override
    Mono<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner) {
        return manager()
            .serviceClient()
            .getWebApps()
            .updateApplicationSettingsSlotAsync(resourceGroupName(), parent().name(), name(), inner);
    }

    @Override
    Mono<ConnectionStringDictionaryInner> listConnectionStrings() {
        return manager().serviceClient().getWebApps()
            .listConnectionStringsSlotAsync(resourceGroupName(), parent().name(), name());
    }

    @Override
    Mono<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner) {
        return manager()
            .serviceClient()
            .getWebApps()
            .updateConnectionStringsSlotAsync(resourceGroupName(), parent().name(), name(), inner);
    }

    @Override
    Mono<SlotConfigNamesResourceInner> listSlotConfigurations() {
        return manager().serviceClient().getWebApps()
            .listSlotConfigurationNamesAsync(resourceGroupName(), parent().name());
    }

    @Override
    Mono<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner) {
        return manager()
            .serviceClient()
            .getWebApps()
            .updateSlotConfigurationNamesAsync(resourceGroupName(), parent().name(), inner);
    }

    @Override
    Mono<SiteSourceControlInner> createOrUpdateSourceControl(SiteSourceControlInner inner) {
        return manager()
            .serviceClient()
            .getWebApps()
            .createOrUpdateSourceControlSlotAsync(resourceGroupName(), parent().name(), name(), inner);
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
            .swapSlotAsync(
                resourceGroupName(), this.parent().name(), name(), new CsmSlotEntity().withTargetSlot(slotName))
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
            .applySlotConfigurationSlotAsync(
                resourceGroupName(), this.parent().name(), name(), new CsmSlotEntity().withTargetSlot(slotName))
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
            .resetSlotConfigurationSlotAsync(resourceGroupName(), this.parent().name(), name())
            .then(refreshAsync())
            .then(Mono.empty());
    }

    @Override
    Mono<Void> deleteSourceControl() {
        return manager()
            .serviceClient()
            .getWebApps()
            .deleteSourceControlSlotAsync(resourceGroupName(), parent().name(), name())
            .then(refreshAsync())
            .then(Mono.empty());
    }

    @Override
    Mono<SiteAuthSettingsInner> updateAuthentication(SiteAuthSettingsInner inner) {
        return manager()
            .serviceClient()
            .getWebApps()
            .updateAuthSettingsSlotAsync(resourceGroupName(), parent().name(), name(), inner);
    }

    @Override
    Mono<SiteAuthSettingsInner> getAuthentication() {
        return manager().serviceClient().getWebApps()
            .getAuthSettingsSlotAsync(resourceGroupName(), parent().name(), name());
    }

    @Override
    Mono<MSDeployStatusInner> createMSDeploy(MSDeploy msDeployInner) {
        return parent()
            .manager()
            .serviceClient()
            .getWebApps()
            .createMSDeployOperationAsync(parent().resourceGroupName(), parent().name(), msDeployInner);
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
            .getSourceControlSlotAsync(resourceGroupName(), parent().name(), name())
            .map(
                siteSourceControlInner ->
                    new WebAppSourceControlImpl<>(siteSourceControlInner, DeploymentSlotBaseImpl.this));
    }

    @Override
    public byte[] getContainerLogs() {
        return getContainerLogsAsync().block();
    }

    @Override
    public Mono<byte[]> getContainerLogsAsync() {
        return FluxUtil
            .collectBytesInByteBufferStream(
                manager()
                    .serviceClient()
                    .getWebApps()
                    .getWebSiteContainerLogsSlotAsync(resourceGroupName(), parent().name(), name()));
    }

    @Override
    public byte[] getContainerLogsZip() {
        return getContainerLogsZipAsync().block();
    }

    @Override
    public Mono<byte[]> getContainerLogsZipAsync() {
        return FluxUtil
            .collectBytesInByteBufferStream(
                manager().serviceClient().getWebApps()
                    .getContainerLogsZipSlotAsync(resourceGroupName(), parent().name(), name()));
    }

    @Override
    Mono<SiteLogsConfigInner> updateDiagnosticLogsConfig(SiteLogsConfigInner siteLogsConfigInner) {
        return manager()
            .serviceClient()
            .getWebApps()
            .updateDiagnosticLogsConfigSlotAsync(resourceGroupName(), parent().name(), name(), siteLogsConfigInner);
    }

    @Override
    public void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken) {
        verifyDomainOwnershipAsync(certificateOrderName, domainVerificationToken).block();
    }

    @Override
    public Mono<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken) {
        IdentifierInner identifierInner = new IdentifierInner().withValue(domainVerificationToken);
        return manager()
            .serviceClient()
            .getWebApps()
            .createOrUpdateDomainOwnershipIdentifierSlotAsync(
                resourceGroupName(), parent().name(), name(), certificateOrderName, identifierInner)
            .then(Mono.empty());
    }

    @Override
    public FluentImplT withRuntime(String runtime) {
        return withAppSetting(SETTING_FUNCTIONS_WORKER_RUNTIME, runtime);
    }

    @Override
    public FluentImplT withRuntimeVersion(String version) {
        return withAppSetting(SETTING_FUNCTIONS_EXTENSION_VERSION, version.startsWith("~") ? version : "~" + version);
    }

    @Override
    public FluentImplT withLatestRuntimeVersion() {
        return withRuntimeVersion("latest");
    }

    @Override
    public FluentImplT withPublicDockerHubImage(String imageAndTag) {
        cleanUpContainerSettings();
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        setAppFrameworkVersion(String.format("DOCKER|%s", imageAndTag));
        return withAppSetting(SETTING_DOCKER_IMAGE, imageAndTag);
    }

    @Override
    public FluentImplT withPrivateDockerHubImage(String imageAndTag) {
        return withPublicDockerHubImage(imageAndTag);
    }

    @Override
    public FluentImplT withPrivateRegistryImage(String imageAndTag, String serverUrl) {
        imageAndTag = Utils.smartCompletionPrivateRegistryImage(imageAndTag, serverUrl);

        cleanUpContainerSettings();
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        setAppFrameworkVersion(String.format("DOCKER|%s", imageAndTag));
        withAppSetting(SETTING_DOCKER_IMAGE, imageAndTag);
        return withAppSetting(SETTING_REGISTRY_SERVER, serverUrl);
    }

    @Override
    public FluentImplT withCredentials(String username, String password) {
        withAppSetting(SETTING_REGISTRY_USERNAME, username);
        return withAppSetting(SETTING_REGISTRY_PASSWORD, password);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withStartUpCommand(String startUpCommand) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withAppCommandLine(startUpCommand);
        return (FluentImplT) this;
    }

    protected void cleanUpContainerSettings() {
        if (siteConfig != null && siteConfig.linuxFxVersion() != null) {
            siteConfig.withLinuxFxVersion(null);
        }
        if (siteConfig != null && siteConfig.windowsFxVersion() != null) {
            siteConfig.withWindowsFxVersion(null);
        }
        // Docker Hub
        withoutAppSetting(SETTING_DOCKER_IMAGE);
        withoutAppSetting(SETTING_REGISTRY_SERVER);
        withoutAppSetting(SETTING_REGISTRY_USERNAME);
        withoutAppSetting(SETTING_REGISTRY_PASSWORD);
    }
}
