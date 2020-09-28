// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebDeploymentSlotBasic;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import reactor.core.publisher.Mono;

class WebDeploymentSlotBasicImpl extends WebSiteBaseImpl implements WebDeploymentSlotBasic, HasParent<WebApp> {

    private final WebApp myParent;

    WebDeploymentSlotBasicImpl(SiteInner innerObject, WebApp myParent) {
        super(innerObject);
        this.myParent = myParent;
    }

    @Override
    public String name() {
        return super.name().replaceAll(".*/", "");
    }

    @Override
    public DeploymentSlot refresh() {
        return this.refreshAsync().block();
    }

    @Override
    public Mono<DeploymentSlot> refreshAsync() {
        return this.parent().deploymentSlots().getByIdAsync(this.id())
            .doOnNext(site -> this.setInner(site.innerModel()));
    }

    @Override
    public WebApp parent() {
        return myParent;
    }
}
