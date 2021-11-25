// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionAppBasic;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import reactor.core.publisher.Mono;

class FunctionAppBasicImpl extends WebSiteBaseImpl implements FunctionAppBasic, HasManager<AppServiceManager> {

    private final AppServiceManager myManager;

    FunctionAppBasicImpl(SiteInner innerObject, AppServiceManager myManager) {
        super(innerObject);
        this.myManager = myManager;
    }

    @Override
    public FunctionApp refresh() {
        return this.refreshAsync().block();
    }

    @Override
    public Mono<FunctionApp> refreshAsync() {
        return this.manager().functionApps().getByIdAsync(this.id())
            .doOnNext(site -> this.setInner(site.innerModel()));
    }

    @Override
    public AppServiceManager manager() {
        return myManager;
    }
}
