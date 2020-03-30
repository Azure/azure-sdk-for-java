/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.management.appservice.AppSetting;
import com.azure.management.appservice.ConnectionString;
import com.azure.management.appservice.CsmPublishingProfileOptions;
import com.azure.management.appservice.CsmSlotEntity;
import com.azure.management.appservice.HostNameBinding;
import com.azure.management.appservice.MSDeploy;
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
import com.azure.management.resources.fluentcore.model.Indexable;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The implementation for DeploymentSlot.
 */
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

    DeploymentSlotBaseImpl(String name, SiteInner innerObject, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig, final ParentImplT parent) {
        super(name.replaceAll(".*/", ""), innerObject, siteConfig, logConfig, parent.manager());
        this.name = name.replaceAll(".*/", "");
        this.parent = parent;
        inner().withServerFarmId(parent.appServicePlanId());
        inner().setLocation(regionName());
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Map<String, HostNameBinding> getHostNameBindings() {
        return getHostNameBindingsAsync().block();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Map<String, HostNameBinding>> getHostNameBindingsAsync() {
        return this.manager().inner().webApps().listHostNameBindingsSlotAsync(resourceGroupName(), parent().name(), name())
                .mapPage(hostNameBindingInner -> new HostNameBindingImpl<FluentT, FluentImplT>(hostNameBindingInner, (FluentImplT) DeploymentSlotBaseImpl.this))
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
        return FluxUtil.collectBytesInByteBufferStream(manager().inner().webApps().listPublishingProfileXmlWithSecretsSlotAsync(resourceGroupName(), this.parent().name(), name(), new CsmPublishingProfileOptions()))
                .map(bytes -> new PublishingProfileImpl(new String(bytes, StandardCharsets.UTF_8), this));
    }

    @Override
    public void start() {
        startAsync().block();
    }

    @Override
    public Mono<Void> startAsync() {
        return manager().inner().webApps().startSlotAsync(resourceGroupName(), this.parent().name(), name())
                .then(refreshAsync())
                .then(Mono.empty());
    }

    @Override
    public void stop() {
        stopAsync().block();
    }

    @Override
    public Mono<Void> stopAsync() {
        return manager().inner().webApps().stopSlotAsync(resourceGroupName(), this.parent().name(), name())
                .then(refreshAsync())
                .then(Mono.empty());
    }

    @Override
    public void restart() {
        restartAsync().block();
    }

    @Override
    public Mono<Void> restartAsync() {
        return manager().inner().webApps().restartSlotAsync(resourceGroupName(), this.parent().name(), name())
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
        return Mono.just(configurationSource).flatMap(webAppBase -> {
            if (!isInCreateMode()) {
                return DeploymentSlotBaseImpl.super.submitAppSettings();
            }
            return webAppBase.getAppSettingsAsync().flatMap(stringAppSettingMap -> {
                for (AppSetting appSetting : stringAppSettingMap.values()) {
                    if (appSetting.sticky()) {
                        withStickyAppSetting(appSetting.key(), appSetting.value());
                    } else {
                        withAppSetting(appSetting.key(), appSetting.value());
                    }
                }
                return DeploymentSlotBaseImpl.super.submitAppSettings();
            });
        }).switchIfEmpty(DeploymentSlotBaseImpl.super.submitAppSettings());
    }

    Mono<Indexable> submitConnectionStrings() {
        return Mono.just(configurationSource).flatMap(webAppBase -> {
            if (!isInCreateMode()) {
                return DeploymentSlotBaseImpl.super.submitConnectionStrings();
            }
            return webAppBase.getConnectionStringsAsync().flatMap(stringConnectionStringMap -> {
                for (ConnectionString connectionString : stringConnectionStringMap.values()) {
                    if (connectionString.sticky()) {
                        withStickyConnectionString(connectionString.name(), connectionString.value(), connectionString.type());
                    } else {
                        withConnectionString(connectionString.name(), connectionString.value(), connectionString.type());
                    }
                }
                return DeploymentSlotBaseImpl.super.submitConnectionStrings();
            });
        }).switchIfEmpty(DeploymentSlotBaseImpl.super.submitConnectionStrings());
    }

    public ParentImplT parent() {
        return this.parent;
    }

    @Override
    Mono<SiteInner> createOrUpdateInner(SiteInner site) {
        return manager().inner().webApps().createOrUpdateSlotAsync(resourceGroupName(), this.parent().name(), name(), site);
    }

    @Override
    Mono<SiteInner> updateInner(SitePatchResourceInner siteUpdate) {
        return manager().inner().webApps().updateSlotAsync(resourceGroupName(), this.parent().name(), name(), siteUpdate);
    }

    @Override
    Mono<SiteInner> getInner() {
        return manager().inner().webApps().getSlotAsync(resourceGroupName(), this.parent().name(), name());
    }

    @Override
    Mono<SiteConfigResourceInner> getConfigInner() {
        return manager().inner().webApps().getConfigurationSlotAsync(resourceGroupName(), parent().name(), name());
    }

    @Override
    Mono<SiteConfigResourceInner> createOrUpdateSiteConfig(SiteConfigResourceInner siteConfig) {
        return manager().inner().webApps().createOrUpdateConfigurationSlotAsync(resourceGroupName(), this.parent().name(), name(), siteConfig);
    }

    @Override
    Mono<Void> deleteHostNameBinding(String hostname) {
        return manager().inner().webApps().deleteHostNameBindingSlotAsync(resourceGroupName(), parent().name(), name(), hostname);
    }

    @Override
    Mono<StringDictionaryInner> listAppSettings() {
        return manager().inner().webApps().listApplicationSettingsSlotAsync(resourceGroupName(), parent().name(), name());
    }

    @Override
    Mono<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner) {
        return manager().inner().webApps().updateApplicationSettingsSlotAsync(resourceGroupName(), parent().name(), name(), inner);
    }

    @Override
    Mono<ConnectionStringDictionaryInner> listConnectionStrings() {
        return manager().inner().webApps().listConnectionStringsSlotAsync(resourceGroupName(), parent().name(), name());
    }

    @Override
    Mono<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner) {
        return manager().inner().webApps().updateConnectionStringsSlotAsync(resourceGroupName(), parent().name(), name(), inner);
    }

    @Override
    Mono<SlotConfigNamesResourceInner> listSlotConfigurations() {
        return manager().inner().webApps().listSlotConfigurationNamesAsync(resourceGroupName(), parent().name());
    }

    @Override
    Mono<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner) {
        return manager().inner().webApps().updateSlotConfigurationNamesAsync(resourceGroupName(), parent().name(), inner);
    }

    @Override
    Mono<SiteSourceControlInner> createOrUpdateSourceControl(SiteSourceControlInner inner) {
        return manager().inner().webApps().createOrUpdateSourceControlSlotAsync(resourceGroupName(), parent().name(), name(), inner);
    }

    @Override
    public void swap(String slotName) {
        swapAsync(slotName).block();
    }

    @Override
    public Mono<Void> swapAsync(String slotName) {
        return manager().inner().webApps().swapSlotAsync(resourceGroupName(), this.parent().name(), name(), new CsmSlotEntity().withTargetSlot(slotName))
                .then(refreshAsync())
                .then(Mono.empty());
    }

    @Override
    public void applySlotConfigurations(String slotName) {
        applySlotConfigurationsAsync(slotName).block();
    }

    @Override
    public Mono<Void> applySlotConfigurationsAsync(String slotName) {
        return manager().inner().webApps().applySlotConfigurationSlotAsync(resourceGroupName(), this.parent().name(), name(), new CsmSlotEntity().withTargetSlot(slotName))
                .then(refreshAsync())
                .then(Mono.empty());
    }

    @Override
    public void resetSlotConfigurations() {
        resetSlotConfigurationsAsync().block();
    }

    @Override
    public Mono<Void> resetSlotConfigurationsAsync() {
        return manager().inner().webApps().resetSlotConfigurationSlotAsync(resourceGroupName(), this.parent().name(), name())
                .then(refreshAsync())
                .then(Mono.empty());
    }

    @Override
    Mono<Void> deleteSourceControl() {
        return manager().inner().webApps().deleteSourceControlSlotAsync(resourceGroupName(), parent().name(), name())
                .then(refreshAsync())
                .then(Mono.empty());
    }

    @Override
    Mono<SiteAuthSettingsInner> updateAuthentication(SiteAuthSettingsInner inner) {
        return manager().inner().webApps().updateAuthSettingsSlotAsync(resourceGroupName(), parent().name(), name(), inner);
    }

    @Override
    Mono<SiteAuthSettingsInner> getAuthentication() {
        return manager().inner().webApps().getAuthSettingsSlotAsync(resourceGroupName(), parent().name(), name());
    }

    @Override
    Mono<MSDeployStatusInner> createMSDeploy(MSDeploy msDeployInner) {
        return parent().manager().inner().webApps()
                .createMSDeployOperationAsync(parent().resourceGroupName(), parent().name(), msDeployInner);
    }

    @Override
    public WebAppSourceControl getSourceControl() {
        return getSourceControlAsync().block();
    }

    @Override
    public Mono<WebAppSourceControl> getSourceControlAsync() {
        return manager().inner().webApps().getSourceControlSlotAsync(resourceGroupName(), parent().name(), name())
                .map(siteSourceControlInner -> new WebAppSourceControlImpl<>(siteSourceControlInner, DeploymentSlotBaseImpl.this));
    }

    @Override
    public byte[] getContainerLogs() {
        return getContainerLogsAsync().block();
    }

    @Override
    public Mono<byte[]> getContainerLogsAsync() {
        return FluxUtil.collectBytesInByteBufferStream(manager().inner().webApps().getWebSiteContainerLogsSlotAsync(resourceGroupName(), parent().name(), name()));
    }

    @Override
    public byte[] getContainerLogsZip() {
        return getContainerLogsZipAsync().block();
    }

    @Override
    public Mono<byte[]> getContainerLogsZipAsync() {
        return FluxUtil.collectBytesInByteBufferStream(manager().inner().webApps().getContainerLogsZipSlotAsync(resourceGroupName(), parent().name(), name()));
    }

    @Override
    Mono<SiteLogsConfigInner> updateDiagnosticLogsConfig(SiteLogsConfigInner siteLogsConfigInner) {
        return manager().inner().webApps().updateDiagnosticLogsConfigSlotAsync(resourceGroupName(), parent().name(), name(), siteLogsConfigInner);
    }

    @Override
    public void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken) {
        verifyDomainOwnershipAsync(certificateOrderName, domainVerificationToken).block();
    }

    @Override
    public Mono<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken) {
        IdentifierInner identifierInner = new IdentifierInner().withValue(domainVerificationToken);
        return manager().inner().webApps().createOrUpdateDomainOwnershipIdentifierSlotAsync(resourceGroupName(), parent().name(), name(), certificateOrderName, identifierInner)
                .then(Mono.empty());
    }
}