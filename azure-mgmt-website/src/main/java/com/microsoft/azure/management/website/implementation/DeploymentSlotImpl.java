/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.website.DeploymentSlot;
import com.microsoft.azure.management.website.HostNameBinding;
import com.microsoft.azure.management.website.WebApp;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The implementation for {@link DeploymentSlot}.
 */
class DeploymentSlotImpl
        extends WebAppBaseImpl<DeploymentSlot, DeploymentSlotImpl>
        implements
            DeploymentSlot,
            DeploymentSlot.Definition,
            DeploymentSlot.Update {
    private final WebAppImpl parent;
    private final String name;

    DeploymentSlotImpl(String name, SiteInner innerObject, SiteConfigInner configObject, final WebAppImpl parent, final WebAppsInner client, AppServiceManager manager) {
        super(name.replaceAll(".*/", ""), innerObject, configObject, client, manager);
        this.name = name.replaceAll(".*/", "");
        this.parent = parent;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DeploymentSlotImpl refresh() {
        SiteInner inner = client.getSlot(resourceGroupName(), parent.name(), name());
        inner.withSiteConfig(client.getConfigurationSlot(resourceGroupName(), parent.name(), name()));
        setInner(inner);
        return this;
    }

    @Override
    public Map<String, HostNameBinding> getHostNameBindings() {
        List<HostNameBindingInner> collectionInner = client.listHostNameBindingsSlot(resourceGroupName(), parent.name(), name());
        List<HostNameBinding> hostNameBindings = new ArrayList<>();
        for (HostNameBindingInner inner : collectionInner) {
            hostNameBindings.add(new HostNameBindingImpl<>(inner, this, client));
        }
        return Maps.uniqueIndex(hostNameBindings, new Function<HostNameBinding, String>() {
            @Override
            public String apply(HostNameBinding input) {
                return input.name().replace(name() + "/", "");
            }
        });
    }

    @Override
    public void start() {
        client.startSlot(resourceGroupName(), parent.name(), name());
    }

    @Override
    public void stop() {
        client.stopSlot(resourceGroupName(), parent.name(), name());
    }

    @Override
    public void restart() {
        client.restartSlot(resourceGroupName(), parent.name(), name());
    }

    @Override
    public DeploymentSlotImpl withBrandNewConfiguration() {
        SiteConfigInner configInner = new SiteConfigInner();
        configInner.withLocation(regionName());
        inner().withSiteConfig(configInner);
        return this;
    }

    @Override
    public DeploymentSlotImpl withConfigurationFromParent() {
        return withConfigurationFromWebApp(parent);
    }

    @Override
    public DeploymentSlotImpl withConfigurationFromWebApp(WebApp webApp) {
        inner().withSiteConfig(webApp.inner().siteConfig());
        return this;
    }

    @Override
    public DeploymentSlotImpl withConfigurationFromDeploymentSlot(DeploymentSlot deploymentSlot) {
        inner().withSiteConfig(deploymentSlot.inner().siteConfig());
        return this;
    }

    @Override
    public WebAppImpl parent() {
        return parent;
    }

    @Override
    Observable<SiteInner> createOrUpdateInner(String resourceGroupName, String name, SiteInner site) {
        return client.createOrUpdateSlotAsync(resourceGroupName, parent.name(), name, site);
    }

    @Override
    Observable<SiteInner> getInner(String resourceGroupName, String name) {
        return client.getSlotAsync(resourceGroupName, parent.name(), name);
    }

    @Override
    Observable<SiteConfigInner> createOrUpdateSiteConfig(String resourceGroupName, String name, SiteConfigInner siteConfig) {
        return client.createOrUpdateConfigurationSlotAsync(resourceGroupName, parent.name(), name, siteConfig);
    }
}