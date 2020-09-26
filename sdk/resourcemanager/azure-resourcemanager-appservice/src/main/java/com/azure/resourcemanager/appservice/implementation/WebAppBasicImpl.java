// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebAppBasic;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import reactor.core.publisher.Mono;

class WebAppBasicImpl extends WebSiteBaseImpl implements WebAppBasic, HasManager<AppServiceManager> {

    private final AppServiceManager myManager;

    WebAppBasicImpl(SiteInner innerObject, AppServiceManager myManager) {
        super(innerObject);
        this.myManager = myManager;
    }

    @Override
    public WebApp refresh() {
        return this.refreshAsync().block();
    }

    @Override
    public Mono<WebApp> refreshAsync() {
        return this.manager().webApps().getByIdAsync(this.id())
            .doOnNext(site -> this.setInner(site.innerModel()));
    }

    @Override
    public AppServiceManager manager() {
        return myManager;
    }
}
