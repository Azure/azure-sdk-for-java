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
import rx.functions.Func1;
import rx.functions.FuncN;

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

    DeploymentSlotImpl(String name, SiteInner innerObject, SiteConfigInner configObject, final WebAppImpl parent, final WebAppsInner client, AppServiceManager manager) {
        super(name.replaceAll(".*/", ""), innerObject, configObject, client, manager);
        this.parent = parent;
    }

    @Override
    public DeploymentSlotImpl refresh() {
        SiteInner inner = client.getSlot(resourceGroupName(), parent.name(), name());
        inner.withSiteConfig(client.getConfigurationSlot(resourceGroupName(), parent.name(), name()));
        setInner(inner);
        return this;
    }

    @Override
    public Observable<DeploymentSlot> createResourceAsync() {
        if (hostNameSslStateMap.size() > 0) {
            inner().withHostNameSslStates(new ArrayList<>(hostNameSslStateMap.values()));
        }
        // Construct web app observable
        inner().siteConfig().withLocation(inner().location());
        final SiteConfigInner localConfig = inner().siteConfig();
        return client.createOrUpdateSlotAsync(resourceGroupName(), parent.name(), name(), inner())
                // Submit hostname bindings
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(final SiteInner site) {
                        List<Observable<HostNameBinding>> bindingObservables = new ArrayList<>();
                        for (HostNameBindingImpl<DeploymentSlot, DeploymentSlotImpl> binding: hostNameBindingsToCreate.values()) {
                            bindingObservables.add(binding.createAsync());
                        }
                        hostNameBindingsToCreate.clear();
                        if (bindingObservables.isEmpty()) {
                            return Observable.just(site);
                        } else {
                            return Observable.zip(bindingObservables, new FuncN<SiteInner>() {
                                @Override
                                public SiteInner call(Object... args) {
                                    return site;
                                }
                            });
                        }
                    }
                })
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(SiteInner site) {
                        return client.getSlotAsync(resourceGroupName(), parent.name(), name());
                    }
                })
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(final SiteInner siteInner) {
                        siteInner.withSiteConfig(localConfig);
                        return client.createOrUpdateConfigurationSlotAsync(resourceGroupName(), parent.name(), name(), localConfig)
                                .flatMap(new Func1<SiteConfigInner, Observable<SiteInner>>() {
                                    @Override
                                    public Observable<SiteInner> call(SiteConfigInner siteConfigInner) {
                                        siteInner.withSiteConfig(siteConfigInner);
                                        return Observable.just(siteInner);
                                    }
                                });
                    }
                })
                .map(new Func1<SiteInner, DeploymentSlot>() {
                    @Override
                    public DeploymentSlot call(SiteInner siteInner) {
                        setInner(siteInner);
                        return normalizeProperties();
                    }
                });
    }

    @Override
    public Map<String, HostNameBinding> getHostNameBindings() {
        List<HostNameBindingInner> collectionInner = client.listHostNameBindingsSlot(resourceGroupName(), parent.name(), name());
        List<HostNameBinding> hostNameBindings = new ArrayList<>();
        for (HostNameBindingInner inner : collectionInner) {
            hostNameBindings.add(new HostNameBindingImpl<>(inner.name(), inner, this, client));
        }
        return Maps.uniqueIndex(hostNameBindings, new Function<HostNameBinding, String>() {
            @Override
            public String apply(HostNameBinding input) {
                return input.name().replace(name() + "/", "");
            }
        });
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
}