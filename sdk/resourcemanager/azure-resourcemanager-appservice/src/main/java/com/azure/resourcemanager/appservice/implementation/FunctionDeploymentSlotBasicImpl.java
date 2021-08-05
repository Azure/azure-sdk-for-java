// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionDeploymentSlot;
import com.azure.resourcemanager.appservice.models.FunctionDeploymentSlotBasic;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import reactor.core.publisher.Mono;

class FunctionDeploymentSlotBasicImpl extends WebSiteBaseImpl
    implements FunctionDeploymentSlotBasic, HasParent<FunctionApp> {

    private final FunctionApp myParent;

    FunctionDeploymentSlotBasicImpl(SiteInner innerObject, FunctionApp myParent) {
        super(innerObject);
        this.myParent = myParent;
    }

    @Override
    public String name() {
        return super.name().replaceAll(".*/", "");
    }

    @Override
    public FunctionDeploymentSlot refresh() {
        return this.refreshAsync().block();
    }

    @Override
    public Mono<FunctionDeploymentSlot> refreshAsync() {
        return this.parent().deploymentSlots().getByIdAsync(this.id())
            .doOnNext(site -> this.setInner(site.innerModel()));
    }

    @Override
    public FunctionApp parent() {
        return myParent;
    }
}
