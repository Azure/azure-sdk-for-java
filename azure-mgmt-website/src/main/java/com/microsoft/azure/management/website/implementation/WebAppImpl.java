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
import rx.functions.Func1;
import rx.functions.FuncN;

import java.util.ArrayList;
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
    public WebAppImpl refresh() {
        this.setInner(client.get(resourceGroupName(), name()));
        return this;
    }

    @Override
    public Observable<WebApp> createResourceAsync() {
        if (hostNameSslStateMap.size() > 0) {
            inner().withHostNameSslStates(new ArrayList<>(hostNameSslStateMap.values()));
        }
        // Construct web app observable
        inner().siteConfig().withLocation(inner().location());
        return client.createOrUpdateAsync(resourceGroupName(), name(), inner())
                // Submit hostname bindings
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(final SiteInner site) {
                        List<Observable<HostNameBinding>> bindingObservables = new ArrayList<>();
                        for (HostNameBindingImpl<WebApp, WebAppImpl> binding: hostNameBindingsToCreate.values()) {
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
                        return client.getAsync(resourceGroupName(), site.name());
                    }
                })
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(final SiteInner siteInner) {
                        inner().siteConfig().withLocation(inner().location());
                        return client.createOrUpdateConfigurationAsync(resourceGroupName(), name(), inner().siteConfig())
                                .flatMap(new Func1<SiteConfigInner, Observable<SiteInner>>() {
                                    @Override
                                    public Observable<SiteInner> call(SiteConfigInner siteConfigInner) {
                                        siteInner.withSiteConfig(siteConfigInner);
                                        return Observable.just(siteInner);
                                    }
                                });
                    }
                })
                .map(new Func1<SiteInner, WebApp>() {
                    @Override
                    public WebApp call(SiteInner siteInner) {
                        setInner(siteInner);
                        return normalizeProperties();
                    }
                });
    }

}