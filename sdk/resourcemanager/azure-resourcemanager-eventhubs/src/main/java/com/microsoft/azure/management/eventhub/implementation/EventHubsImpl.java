/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubAuthorizationRules;
import com.microsoft.azure.management.eventhub.EventHubConsumerGroups;
import com.microsoft.azure.management.eventhub.EventHubs;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.Objects;

/**
 * Implementation for {@link EventHubs}.
 */
@LangDefinition
class EventHubsImpl extends WrapperImpl<EventHubsInner> implements EventHubs {
    private final EventHubManager manager;
    private final StorageManager storageManager;

    protected EventHubsImpl(EventHubManager manager, StorageManager storageManager) {
        super(manager.inner().eventHubs());
        this.manager = manager;
        this.storageManager = storageManager;
    }

    @Override
    public EventHubManager manager() {
        return this.manager;
    }

    @Override
    public EventHubImpl define(String name) {
        return new EventHubImpl(name, this.manager, this.storageManager);
    }

    public EventHubAuthorizationRules authorizationRules() {
        return this.manager().eventHubAuthorizationRules();
    }

    public EventHubConsumerGroups consumerGroups() {
        return this.manager().consumerGroups();
    }

    @Override
    public EventHub getById(String id) {
        return getByIdAsync(id).toBlocking().last();
    }

    @Override
    public Observable<EventHub> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);

        return getByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public ServiceFuture<EventHub> getByIdAsync(String id, ServiceCallback<EventHub> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public Observable<EventHub> getByNameAsync(String resourceGroupName, String namespaceName, String name) {
        return this.inner().getAsync(resourceGroupName,
                namespaceName,
                name)
                .map(new Func1<EventhubInner, EventHub>() {
                    @Override
                    public EventHub call(EventhubInner inner) {
                        if (inner == null) {
                            return null;
                        } else {
                            return wrapModel(inner);
                        }
                    }
                });
    }

    @Override
    public EventHub getByName(String resourceGroupName, String namespaceName, String name) {
        return getByNameAsync(resourceGroupName, namespaceName, name).toBlocking().last();
    }

    @Override
    public PagedList<EventHub> listByNamespace(String resourceGroupName, String namespaceName) {
        return (new PagedListConverter<EventhubInner, EventHub>() {
            @Override
            public Observable<EventHub> typeConvertAsync(final EventhubInner inner) {
                return Observable.<EventHub>just(wrapModel(inner));
            }
        }).convert(inner().listByNamespace(resourceGroupName, namespaceName));
    }

    @Override
    public Observable<EventHub> listByNamespaceAsync(String resourceGroupName, String namespaceName) {
        return this.inner().listByNamespaceAsync(resourceGroupName, namespaceName)
                .flatMapIterable(new Func1<Page<EventhubInner>, Iterable<EventhubInner>>() {
                    @Override
                    public Iterable<EventhubInner> call(Page<EventhubInner> page) {
                        return page.items();
                    }
                })
                .map(new Func1<EventhubInner, EventHub>() {
                    @Override
                    public EventHub call(EventhubInner inner) {
                        return wrapModel(inner);
                    }
                });
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).await();
    }

    @Override
    public ServiceFuture<Void> deleteByIdAsync(String id, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(deleteByIdAsync(id), callback);
    }

    @Override
    public Completable deleteByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);

        return deleteByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public Completable deleteByNameAsync(String resourceGroupName, String namespaceName, String name) {
        return this.inner().deleteAsync(resourceGroupName,
                namespaceName,
                name).toCompletable();
    }

    @Override
    public void deleteByName(String resourceGroupName, String namespaceName, String name) {
        deleteByNameAsync(resourceGroupName, namespaceName, name).await();
    }

    private EventHubImpl wrapModel(EventhubInner innerModel) {
        return new EventHubImpl(innerModel.name(), innerModel, this.manager, this.storageManager);
    }
}
