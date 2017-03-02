/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.DeploymentSlots;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.HostNameBinding;
import com.microsoft.azure.management.appservice.PublishingProfile;
import com.microsoft.azure.management.appservice.WebAppSourceControl;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The implementation for WebApp.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class FunctionAppImpl
        extends WebAppBaseImpl<FunctionApp, FunctionAppImpl>
        implements
            FunctionApp,
            FunctionApp.Definition,
            FunctionApp.Update {

    private DeploymentSlots deploymentSlots;
    private AppServicePlanImpl appServicePlan;

    FunctionAppImpl(String name, SiteInner innerObject, SiteConfigInner configObject, AppServiceManager manager) {
        super(name, innerObject, configObject, manager);
    }

    @Override
    Observable<SiteInner> createOrUpdateInner(SiteInner site) {
        return this.manager().inner().webApps().createOrUpdateAsync(resourceGroupName(), name(), site);
    }

    @Override
    Observable<SiteInner> getInner() {
        return this.manager().inner().webApps().getAsync(resourceGroupName(), name());
    }

    @Override
    Observable<SiteConfigInner> getConfigInner() {
        return this.manager().inner().webApps().getConfigurationAsync(resourceGroupName(), name());
    }

    @Override
    Observable<SiteConfigInner> createOrUpdateSiteConfig(SiteConfigInner siteConfig) {
        return this.manager().inner().webApps().createOrUpdateConfigurationAsync(resourceGroupName(), name(), siteConfig);
    }

    @Override
    Observable<Void> deleteHostNameBinding(String hostname) {
        return this.manager().inner().webApps().deleteHostNameBindingAsync(resourceGroupName(), name(), hostname);
    }

    @Override
    Observable<StringDictionaryInner> listAppSettings() {
        return this.manager().inner().webApps().listApplicationSettingsAsync(resourceGroupName(), name());
    }

    @Override
    Observable<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner) {
        return this.manager().inner().webApps().updateApplicationSettingsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<ConnectionStringDictionaryInner> listConnectionStrings() {
        return this.manager().inner().webApps().listConnectionStringsAsync(resourceGroupName(), name());
    }

    @Override
    Observable<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner) {
        return this.manager().inner().webApps().updateConnectionStringsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<SlotConfigNamesResourceInner> listSlotConfigurations() {
        return this.manager().inner().webApps().listSlotConfigurationNamesAsync(resourceGroupName(), name());
    }

    @Override
    Observable<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner) {
        return this.manager().inner().webApps().updateSlotConfigurationNamesAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<SiteSourceControlInner> createOrUpdateSourceControl(SiteSourceControlInner inner) {
        return this.manager().inner().webApps().createOrUpdateSourceControlAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<Void> deleteSourceControl() {
        return this.manager().inner().webApps().deleteSourceControlAsync(resourceGroupName(), name()).map(new Func1<Object, Void>() {
            @Override
            public Void call(Object o) {
                return null;
            }
        });
    }

    @Override
    public Map<String, HostNameBinding> getHostNameBindings() {
        List<HostNameBindingInner> collectionInner = this.manager().inner().webApps().listHostNameBindings(resourceGroupName(), name());
        List<HostNameBinding> hostNameBindings = new ArrayList<>();
        for (HostNameBindingInner inner : collectionInner) {
            hostNameBindings.add(new HostNameBindingImpl<>(inner, this));
        }
        return Collections.unmodifiableMap(Maps.uniqueIndex(hostNameBindings, new Function<HostNameBinding, String>() {
            @Override
            public String apply(HostNameBinding input) {
                return input.name().replace(name() + "/", "");
            }
        }));
    }

    @Override
    public PublishingProfile getPublishingProfile() {
        InputStream stream = this.manager().inner().webApps().listPublishingProfileXmlWithSecrets(resourceGroupName(), name());
        try {
            String xml = CharStreams.toString(new InputStreamReader(stream));
            return new PublishingProfileImpl(xml, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WebAppSourceControl getSourceControl() {
        SiteSourceControlInner siteSourceControlInner = this.manager().inner().webApps().getSourceControl(resourceGroupName(), name());
        return new WebAppSourceControlImpl<>(siteSourceControlInner, this);
    }

    @Override
    public void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken) {
        verifyDomainOwnershipAsync(certificateOrderName, domainVerificationToken).toBlocking().subscribe();
    }

    @Override
    public Observable<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken) {
        IdentifierInner identifierInner = new IdentifierInner().withIdentifierId(domainVerificationToken);
        identifierInner.withLocation("global");
        return this.manager().inner().webApps().createOrUpdateDomainOwnershipIdentifierAsync(resourceGroupName(), name(), certificateOrderName, identifierInner)
                .map(new Func1<IdentifierInner, Void>() {
                    @Override
                    public Void call(IdentifierInner identifierInner) {
                        return null;
                    }
                });
    }

    @Override
    public void start() {
        this.manager().inner().webApps().start(resourceGroupName(), name());
        refresh();
    }

    @Override
    public void stop() {
        this.manager().inner().webApps().stop(resourceGroupName(), name());
        refresh();
    }

    @Override
    public void restart() {
        this.manager().inner().webApps().restart(resourceGroupName(), name());
        refresh();
    }

    @Override
    public void swap(String slotName) {
        this.manager().inner().webApps().swapSlotWithProduction(resourceGroupName(), name(), new CsmSlotEntityInner().withTargetSlot(slotName));
        refresh();
    }

    @Override
    public void applySlotConfigurations(String slotName) {
        this.manager().inner().webApps().applySlotConfigToProduction(resourceGroupName(), name(), new CsmSlotEntityInner().withTargetSlot(slotName));
        refresh();
    }

    @Override
    public void resetSlotConfigurations() {
        this.manager().inner().webApps().resetProductionSlotConfig(resourceGroupName(), name());
    }
}