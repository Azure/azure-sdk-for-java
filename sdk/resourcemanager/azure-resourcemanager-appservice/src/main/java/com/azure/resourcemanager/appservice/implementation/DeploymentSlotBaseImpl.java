// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.resourcemanager.appservice.models.AppSetting;
import com.azure.resourcemanager.appservice.models.ConnectionString;
import com.azure.resourcemanager.appservice.models.CsmPublishingProfileOptions;
import com.azure.resourcemanager.appservice.models.CsmSlotEntity;
import com.azure.resourcemanager.appservice.models.HostnameBinding;
import com.azure.resourcemanager.appservice.models.MSDeploy;
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
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

/** The implementation for DeploymentSlot. */
abstract class DeploymentSlotBaseImpl<
        FluentT extends WebAppBase,
        FluentImplT extends DeploymentSlotBaseImpl<FluentT, FluentImplT, ParentImplT, FluentWithCreateT, FluentUpdateT>,
        ParentImplT extends AppServiceBaseImpl<?, ?, ?, ?>,
        FluentWithCreateT,
        FluentUpdateT>
    extends WebAppBaseImpl<FluentT, FluentImplT> {
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
        inner().withServerFarmId(parent.appServicePlanId());
        inner().withLocation(regionName());
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
        return this
            .manager()
            .inner()
            .getWebApps()
            .listHostnameBindingsSlotAsync(resourceGroupName(), parent().name(), name())
            .mapPage(
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
                    .inner()
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
            .inner()
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
            .inner()
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
            .inner()
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
            .inner()
            .getWebApps()
            .createOrUpdateSlotAsync(resourceGroupName(), this.parent().name(), name(), site);
    }

    @Override
    Mono<SiteInner> updateInner(SitePatchResourceInner siteUpdate) {
        return manager()
            .inner()
            .getWebApps()
            .updateSlotAsync(resourceGroupName(), this.parent().name(), name(), siteUpdate);
    }

    @Override
    Mono<SiteInner> getInner() {
        return manager().inner().getWebApps().getSlotAsync(resourceGroupName(), this.parent().name(), name());
    }

    @Override
    Mono<SiteConfigResourceInner> getConfigInner() {
        return manager().inner().getWebApps().getConfigurationSlotAsync(resourceGroupName(), parent().name(), name());
    }

    @Override
    Mono<SiteConfigResourceInner> createOrUpdateSiteConfig(SiteConfigResourceInner siteConfig) {
        return manager()
            .inner()
            .getWebApps()
            .createOrUpdateConfigurationSlotAsync(resourceGroupName(), this.parent().name(), name(), siteConfig);
    }

    @Override
    Mono<Void> deleteHostnameBinding(String hostname) {
        return manager()
            .inner()
            .getWebApps()
            .deleteHostnameBindingSlotAsync(resourceGroupName(), parent().name(), name(), hostname);
    }

    @Override
    Mono<StringDictionaryInner> listAppSettings() {
        return manager()
            .inner()
            .getWebApps()
            .listApplicationSettingsSlotAsync(resourceGroupName(), parent().name(), name());
    }

    @Override
    Mono<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner) {
        return manager()
            .inner()
            .getWebApps()
            .updateApplicationSettingsSlotAsync(resourceGroupName(), parent().name(), name(), inner);
    }

    @Override
    Mono<ConnectionStringDictionaryInner> listConnectionStrings() {
        return manager().inner().getWebApps()
            .listConnectionStringsSlotAsync(resourceGroupName(), parent().name(), name());
    }

    @Override
    Mono<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner) {
        return manager()
            .inner()
            .getWebApps()
            .updateConnectionStringsSlotAsync(resourceGroupName(), parent().name(), name(), inner);
    }

    @Override
    Mono<SlotConfigNamesResourceInner> listSlotConfigurations() {
        return manager().inner().getWebApps().listSlotConfigurationNamesAsync(resourceGroupName(), parent().name());
    }

    @Override
    Mono<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner) {
        return manager()
            .inner()
            .getWebApps()
            .updateSlotConfigurationNamesAsync(resourceGroupName(), parent().name(), inner);
    }

    @Override
    Mono<SiteSourceControlInner> createOrUpdateSourceControl(SiteSourceControlInner inner) {
        return manager()
            .inner()
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
            .inner()
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
            .inner()
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
            .inner()
            .getWebApps()
            .resetSlotConfigurationSlotAsync(resourceGroupName(), this.parent().name(), name())
            .then(refreshAsync())
            .then(Mono.empty());
    }

    @Override
    Mono<Void> deleteSourceControl() {
        return manager()
            .inner()
            .getWebApps()
            .deleteSourceControlSlotAsync(resourceGroupName(), parent().name(), name())
            .then(refreshAsync())
            .then(Mono.empty());
    }

    @Override
    Mono<SiteAuthSettingsInner> updateAuthentication(SiteAuthSettingsInner inner) {
        return manager()
            .inner()
            .getWebApps()
            .updateAuthSettingsSlotAsync(resourceGroupName(), parent().name(), name(), inner);
    }

    @Override
    Mono<SiteAuthSettingsInner> getAuthentication() {
        return manager().inner().getWebApps().getAuthSettingsSlotAsync(resourceGroupName(), parent().name(), name());
    }

    @Override
    Mono<MSDeployStatusInner> createMSDeploy(MSDeploy msDeployInner) {
        return parent()
            .manager()
            .inner()
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
            .inner()
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
                    .inner()
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
                manager().inner().getWebApps()
                    .getContainerLogsZipSlotAsync(resourceGroupName(), parent().name(), name()));
    }

    @Override
    Mono<SiteLogsConfigInner> updateDiagnosticLogsConfig(SiteLogsConfigInner siteLogsConfigInner) {
        return manager()
            .inner()
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
            .inner()
            .getWebApps()
            .createOrUpdateDomainOwnershipIdentifierSlotAsync(
                resourceGroupName(), parent().name(), name(), certificateOrderName, identifierInner)
            .then(Mono.empty());
    }
}
