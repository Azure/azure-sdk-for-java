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
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * The implementation for {@link Deployments}.
 */
final class DeploymentsImpl
    implements Deployments {

    private final DeploymentsInner client;
    private final DeploymentOperationsInner deploymentOperationsClient;
    private final ResourceManager resourceManager;
    private PagedListConverter<DeploymentExtendedInner, Deployment> converter;

    DeploymentsImpl(final DeploymentsInner client,
                           final DeploymentOperationsInner deploymentOperationsClient,
                           final ResourceManager resourceManager) {
        this.client = client;
        this.deploymentOperationsClient = deploymentOperationsClient;
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
        return new GroupPagedList<Deployment>(this.resourceManager.resourceGroups().list()) {
            @Override
            public List<Deployment> listNextGroup(String resourceGroupName) {
                return converter.convert(client.list(resourceGroupName));
            }
        };
    }

    @Override
    public PagedList<Deployment> listByGroup(String groupName) {
        return converter.convert(client.list(groupName));
    }

    @Override
    public Deployment getByName(String name) {
        for (ResourceGroup group : this.resourceManager.resourceGroups().list()) {
            DeploymentExtendedInner inner = client.get(group.name(), name);
            if (inner != null) {
                return createFluentModel(inner);
            }
        }
        return null;
    }

    @Override
    public Deployment getByGroup(String groupName, String name) {
        return createFluentModel(client.get(groupName, name));
    }

    @Override
    public void delete(String groupName, String name) {
        deleteAsync(groupName, name).toBlocking().subscribe();
    }

    @Override
    public ServiceCall<Void> deleteAsync(String groupName, String name, ServiceCallback<Void> callback) {
        return ServiceCall.create(deleteAsync(groupName, name).map(new Func1<Void, ServiceResponse<Void>>() {
            @Override
            public ServiceResponse<Void> call(Void aVoid) {
                return new ServiceResponse<>(aVoid, null);
            }
        }), callback);
    }

    @Override
    public Observable<Void> deleteAsync(String groupName, String name) {
        return client.deleteAsync(groupName, name);
    }

    @Override
    public DeploymentImpl define(String name) {
        return createFluentModel(name);
    }

    @Override
    public boolean checkExistence(String resourceGroupName, String deploymentName) {
        return client.checkExistence(resourceGroupName, deploymentName);
    }

    protected DeploymentImpl createFluentModel(String name) {
        return new DeploymentImpl(
                new DeploymentExtendedInner().withName(name),
                client,
                deploymentOperationsClient,
                this.resourceManager);
    }

    protected DeploymentImpl createFluentModel(DeploymentExtendedInner deploymentExtendedInner) {
        return new DeploymentImpl(deploymentExtendedInner, client, deploymentOperationsClient, this.resourceManager);
    }

    @Override
    public Deployment getById(String id) {
        return this.getByGroup(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String id) {
        deleteAsync(id).toBlocking().subscribe();
    }

    @Override
    public ServiceCall<Void> deleteAsync(String id, ServiceCallback<Void> callback) {
        return ServiceCall.create(deleteAsync(id).map(new Func1<Void, ServiceResponse<Void>>() {
            @Override
            public ServiceResponse<Void> call(Void aVoid) {
                return new ServiceResponse<>(aVoid, null);
            }
        }), callback);
    }

    @Override
    public Observable<Void> deleteAsync(String id) {
        return deleteAsync(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }
}
