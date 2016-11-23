/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.website.DeploymentSlots;
import com.microsoft.azure.management.website.HostNameBinding;
import com.microsoft.azure.management.website.WebApp;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The implementation for {@link WebApp}.
 */
class WebAppImpl
        extends WebAppBaseImpl<WebApp, WebAppImpl>
        implements
            WebApp,
            WebApp.Definition,
            WebApp.Update {

    private DeploymentSlots deploymentSlots;

    WebAppImpl(String name, SiteInner innerObject, SiteConfigInner configObject, final WebAppsInner client, AppServiceManager manager) {
        super(name, innerObject, configObject, client, manager);
    }

    @Override
    Observable<SiteInner> createOrUpdateInner(SiteInner site) {
        return client.createOrUpdateAsync(resourceGroupName(), name(), site);
    }

    @Override
    Observable<SiteInner> getInner() {
        return client.getAsync(resourceGroupName(), name());
    }

    @Override
    Observable<SiteConfigInner> createOrUpdateSiteConfig(SiteConfigInner siteConfig) {
        return client.createOrUpdateConfigurationAsync(resourceGroupName(), name(), siteConfig);
    }

    @Override
    Observable<Object> deleteHostNameBinding(String hostname) {
        return client.deleteHostNameBindingAsync(resourceGroupName(), name(), hostname);
    }

    @Override
    Observable<StringDictionaryInner> listAppSettings() {
        return client.listApplicationSettingsAsync(resourceGroupName(), name());
    }

    @Override
    Observable<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner) {
        return client.updateApplicationSettingsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<ConnectionStringDictionaryInner> listConnectionStrings() {
        return client.listConnectionStringsAsync(resourceGroupName(), name());
    }

    @Override
    Observable<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner) {
        return client.updateConnectionStringsAsync(resourceGroupName(), name(), inner);
    }

    @Override
    Observable<SlotConfigNamesResourceInner> listSlotConfigurations() {
        return client.listSlotConfigurationNamesAsync(resourceGroupName(), name());
    }

    @Override
    Observable<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner) {
        return client.updateSlotConfigurationNamesAsync(resourceGroupName(), name(), inner);
    }

    @Override
    public DeploymentSlots deploymentSlots() {
        if (deploymentSlots == null) {
            deploymentSlots = new DeploymentSlotsImpl(this, client, myManager);
        }
        return deploymentSlots;
    }

    @Override
    public Map<String, HostNameBinding> getHostNameBindings() {
        List<HostNameBindingInner> collectionInner = client.listHostNameBindings(resourceGroupName(), name());
        List<HostNameBinding> hostNameBindings = new ArrayList<>();
        for (HostNameBindingInner inner : collectionInner) {
            hostNameBindings.add(new HostNameBindingImpl<>(inner, this, client));
        }
        return Collections.unmodifiableMap(Maps.uniqueIndex(hostNameBindings, new Function<HostNameBinding, String>() {
            @Override
            public String apply(HostNameBinding input) {
                return input.name().replace(name() + "/", "");
            }
        }));
    }

    @Override
    public void start() {
        client.start(resourceGroupName(), name());
    }

    @Override
    public void stop() {
        client.stop(resourceGroupName(), name());
    }

    @Override
    public void restart() {
        client.restart(resourceGroupName(), name());
    }

    @Override
    public void swap(String slotName) {
        client.swapSlotWithProduction(resourceGroupName(), name(), new CsmSlotEntityInner().withTargetSlot(slotName));
    }

    @Override
    public void applySlotConfigurations(String slotName) {
        client.applySlotConfigToProduction(resourceGroupName(), name(), new CsmSlotEntityInner().withTargetSlot(slotName));
    }

    @Override
    public void resetSlotConfigurations() {
        client.resetProductionSlotConfig(resourceGroupName(), name());
    }

    @Override
    public WebAppImpl refresh() {
        this.setInner(client.get(resourceGroupName(), name()));
        return this;
    }
}