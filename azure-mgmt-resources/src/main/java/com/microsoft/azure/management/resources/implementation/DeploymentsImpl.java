/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.SupportsGettingByResourceGroupImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * The implementation for {@link Deployments}.
 */
final class DeploymentsImpl
    extends SupportsGettingByResourceGroupImpl<Deployment>
    implements Deployments,
    HasManager<ResourceManager> {

    private final ResourceManager resourceManager;
    private PagedListConverter<DeploymentExtendedInner, Deployment> converter;

    DeploymentsImpl(final ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        converter = new PagedListConverter<DeploymentExtendedInner, Deployment>() {
            @Override
            public Deployment typeConvert(DeploymentExtendedInner deploymentInner) {
                return createFluentModel(deploymentInner);
            }
        };
    }

    @Override
    public PagedList<Deployment> list() {
        final DeploymentsInner client = this.manager().inner().deployments();
        return new GroupPagedList<Deployment>(this.resourceManager.resourceGroups().list()) {
            @Override
            public List<Deployment> listNextGroup(String resourceGroupName) {
                return converter.convert(client.listByResourceGroup(resourceGroupName));
            }
        };
    }

    @Override
    public PagedList<Deployment> listByResourceGroup(String groupName) {
        return converter.convert(this.manager().inner().deployments().listByResourceGroup(groupName));
    }

    @Override
    public Deployment getByName(String name) {
        for (ResourceGroup group : this.resourceManager.resourceGroups().list()) {
            DeploymentExtendedInner inner = this.manager().inner().deployments().getByResourceGroup(group.name(), name);
            if (inner != null) {
                return createFluentModel(inner);
            }
        }
        return null;
    }

    @Override
    public Observable<Deployment> getByResourceGroupAsync(String groupName, String name) {
        return this.manager().inner().deployments().getByResourceGroupAsync(groupName, name).map(new Func1<DeploymentExtendedInner, Deployment>() {
            @Override
            public Deployment call(DeploymentExtendedInner deploymentExtendedInner) {
                return createFluentModel(deploymentExtendedInner);
            }
        });
    }

    @Override
    public void deleteByResourceGroup(String groupName, String name) {
        deleteByResourceGroupAsync(groupName, name).await();
    }

    @Override
    public ServiceFuture<Void> deleteByResourceGroupAsync(String groupName, String name, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(deleteByResourceGroupAsync(groupName, name).<Void>toObservable(), callback);
    }

    @Override
    public Completable deleteByResourceGroupAsync(String groupName, String name) {
        return this.manager().inner().deployments().deleteAsync(groupName, name).toCompletable();
    }

    @Override
    public DeploymentImpl define(String name) {
        return createFluentModel(name);
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String deploymentName) {
        return this.manager().inner().deployments().checkExistence(resourceGroupName, deploymentName);
    }

    protected DeploymentImpl createFluentModel(String name) {
        return new DeploymentImpl(
                new DeploymentExtendedInner().withName(name),
                this.resourceManager);
    }

    protected DeploymentImpl createFluentModel(DeploymentExtendedInner deploymentExtendedInner) {
        return new DeploymentImpl(deploymentExtendedInner, this.resourceManager);
    }

    @Override
    public Deployment getById(String id) {
        return this.getByResourceGroup(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).await();
    }

    @Override
    public ServiceFuture<Void> deleteByIdAsync(String id, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(deleteByIdAsync(id).<Void>toObservable(), callback);
    }

    @Override
    public Completable deleteByIdAsync(String id) {
        return deleteByResourceGroupAsync(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public ResourceManager manager() {
        return this.resourceManager;
    }

    @Override
    public Observable<Deployment> listAsync() {
        return this.manager().resourceGroups().listAsync().flatMap(new Func1<ResourceGroup, Observable<Deployment>>() {
            @Override
            public Observable<Deployment> call(ResourceGroup resourceGroup) {
                return listByResourceGroupAsync(resourceGroup.name());
            }
        });
    }


    @Override
    public Observable<Deployment> listByResourceGroupAsync(String resourceGroupName) {
        final DeploymentsInner client = this.manager().inner().deployments();
        return ReadableWrappersImpl.convertPageToInnerAsync(client.listByResourceGroupAsync(resourceGroupName))
                .map(new Func1<DeploymentExtendedInner, Deployment>() {
                    @Override
                    public Deployment call(DeploymentExtendedInner deploymentExtendedInner) {
                        return createFluentModel(deploymentExtendedInner);
                    }
        });
    }
}
