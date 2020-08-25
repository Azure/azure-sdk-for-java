/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.EventHubConsumerGroup;
import com.microsoft.azure.management.eventhub.EventHubConsumerGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.Objects;

/**
 * Implementation for {@link EventHubConsumerGroups}.
 */
@LangDefinition
class EventHubConsumerGroupsImpl extends WrapperImpl<ConsumerGroupsInner> implements EventHubConsumerGroups {
    private final EventHubManager manager;

    protected EventHubConsumerGroupsImpl(EventHubManager manager) {
        super(manager.inner().consumerGroups());
        this.manager = manager;
    }

    @Override
    public EventHubManager manager() {
        return this.manager;
    }

    @Override
    public EventHubConsumerGroupImpl define(String name) {
        return new EventHubConsumerGroupImpl(name, this.manager);
    }

    @Override
    public EventHubConsumerGroup getById(String id) {
        return getByIdAsync(id).toBlocking().last();
    }

    @Override
    public Observable<EventHubConsumerGroup> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);

        return getByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().parent().name(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public ServiceFuture<EventHubConsumerGroup> getByIdAsync(String id, ServiceCallback<EventHubConsumerGroup> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public Observable<EventHubConsumerGroup> getByNameAsync(String resourceGroupName, String namespaceName, String eventHubName, String name) {
        return this.inner().getAsync(resourceGroupName,
                namespaceName,
                eventHubName,
                name)
                .map(new Func1<ConsumerGroupInner, EventHubConsumerGroup>() {
                    @Override
                    public EventHubConsumerGroup call(ConsumerGroupInner inner) {
                        if (inner == null) {
                            return null;
                        } else {
                            return wrapModel(inner);
                        }
                    }
                });
    }

    @Override
    public EventHubConsumerGroup getByName(String resourceGroupName, String namespaceName, String eventHubName, String name) {
        return getByNameAsync(resourceGroupName, namespaceName, eventHubName, name).toBlocking().last();
    }

    @Override
    public PagedList<EventHubConsumerGroup> listByEventHub(String resourceGroupName, String namespaceName, String eventHubName) {
        return (new PagedListConverter<ConsumerGroupInner, EventHubConsumerGroup>() {
            @Override
            public Observable<EventHubConsumerGroup> typeConvertAsync(final ConsumerGroupInner inner) {
                return Observable.<EventHubConsumerGroup>just(wrapModel(inner));
            }
        }).convert(inner().listByEventHub(resourceGroupName, namespaceName, eventHubName));
    }

    @Override
    public Observable<EventHubConsumerGroup> listByEventHubAsync(String resourceGroupName, String namespaceName, String eventHubName) {
        return this.inner().listByEventHubAsync(resourceGroupName, namespaceName, eventHubName)
                .flatMapIterable(new Func1<Page<ConsumerGroupInner>, Iterable<ConsumerGroupInner>>() {
                    @Override
                    public Iterable<ConsumerGroupInner> call(Page<ConsumerGroupInner> page) {
                        return page.items();
                    }
                })
                .map(new Func1<ConsumerGroupInner, EventHubConsumerGroup>() {
                    @Override
                    public EventHubConsumerGroup call(ConsumerGroupInner inner) {
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
                resourceId.parent().parent().name(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public Completable deleteByNameAsync(String resourceGroupName, String namespaceName, String eventHubName, String name) {
        return this.inner().deleteAsync(resourceGroupName,
                namespaceName,
                eventHubName,
                name).toCompletable();
    }

    @Override
    public void deleteByName(String resourceGroupName, String namespaceName, String eventHubName, String name) {
        deleteByNameAsync(resourceGroupName, namespaceName, eventHubName, name).await();
    }

    private EventHubConsumerGroupImpl wrapModel(ConsumerGroupInner innerModel) {
        return new EventHubConsumerGroupImpl(innerModel.name(), innerModel, this.manager);
    }
}
