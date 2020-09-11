// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.DeploymentsClient;
import com.azure.resourcemanager.appplatform.fluent.inner.DeploymentResourceInner;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppDeployment;
import com.azure.resourcemanager.appplatform.models.SpringAppDeployments;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;
import reactor.core.publisher.Mono;

public class SpringAppDeploymentsImpl
    extends ExternalChildResourcesNonCachedImpl<
        SpringAppDeploymentImpl, SpringAppDeployment, DeploymentResourceInner, SpringAppImpl, SpringApp>
    implements SpringAppDeployments<SpringAppDeploymentImpl> {

    SpringAppDeploymentsImpl(SpringAppImpl parent) {
        super(parent, parent.taskGroup(), "SpringAppDeployment");
    }

    @Override
    public SpringAppDeployment getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<SpringAppDeployment> getByIdAsync(String id) {
        return getByNameAsync(ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public SpringAppDeployment getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<SpringAppDeployment> getByNameAsync(String name) {
        return inner().getAsync(parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name)
            .map(this::wrapModel);
    }

    @Override
    public AppPlatformManager manager() {
        return parent().parent().manager();
    }

    @Override
    public SpringAppImpl parent() {
        return super.getParent();
    }

    @Override
    public SpringAppDeploymentImpl define(String name) {
        return super.prepareIndependentDefine(wrapModel(name));
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).block();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return deleteByNameAsync(ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).block();
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return inner().deleteAsync(
            parent().parent().resourceGroupName(), parent().parent().name(), parent().name(), name);
    }

    @Override
    public PagedIterable<SpringAppDeployment> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<SpringAppDeployment> listAsync() {
        return inner().listAsync(parent().parent().resourceGroupName(), parent().parent().name(), parent().name())
            .mapPage(this::wrapModel);
    }

    private SpringAppDeploymentImpl wrapModel(String name) {
        return new SpringAppDeploymentImpl(name, parent(), new DeploymentResourceInner());
    }

    private SpringAppDeploymentImpl wrapModel(DeploymentResourceInner inner) {
        return inner == null ? null : new SpringAppDeploymentImpl(inner.name(), parent(), inner);
    }

    @Override
    public DeploymentsClient inner() {
        return manager().inner().getDeployments();
    }
}
