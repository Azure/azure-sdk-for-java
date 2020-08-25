/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.DisasterRecoveryPairingAuthorizationRules;
import com.microsoft.azure.management.eventhub.EventHubDisasterRecoveryPairing;
import com.microsoft.azure.management.eventhub.EventHubDisasterRecoveryPairings;
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
 * Implementation for {@link EventHubDisasterRecoveryPairings}.
 */
@LangDefinition
class EventHubDisasterRecoveryPairingsImpl extends WrapperImpl<DisasterRecoveryConfigsInner> implements EventHubDisasterRecoveryPairings {
    private EventHubManager manager;

    protected EventHubDisasterRecoveryPairingsImpl(EventHubManager manager) {
        super(manager.inner().disasterRecoveryConfigs());
        this.manager = manager;
    }

    @Override
    public EventHubManager manager() {
        return this.manager;
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl define(String name) {
        return new EventHubDisasterRecoveryPairingImpl(name, this.manager);
    }

    @Override
    public DisasterRecoveryPairingAuthorizationRules authorizationRules() {
        return null;
    }

    @Override
    public EventHubDisasterRecoveryPairing getById(String id) {
        return getByIdAsync(id).toBlocking().last();
    }

    @Override
    public Observable<EventHubDisasterRecoveryPairing> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);

        return getByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public ServiceFuture<EventHubDisasterRecoveryPairing> getByIdAsync(String id, ServiceCallback<EventHubDisasterRecoveryPairing> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public Observable<EventHubDisasterRecoveryPairing> getByNameAsync(String resourceGroupName, String namespaceName, String name) {
        return this.inner().getAsync(resourceGroupName,
                namespaceName,
                name)
                .map(new Func1<ArmDisasterRecoveryInner, EventHubDisasterRecoveryPairing>() {
                    @Override
                    public EventHubDisasterRecoveryPairing call(ArmDisasterRecoveryInner inner) {
                        if (inner == null) {
                            return null;
                        } else {
                            return wrapModel(inner);
                        }
                    }
                });
    }

    @Override
    public EventHubDisasterRecoveryPairing getByName(String resourceGroupName, String namespaceName, String name) {
        return getByNameAsync(resourceGroupName, namespaceName, name).toBlocking().last();
    }

    @Override
    public PagedList<EventHubDisasterRecoveryPairing> listByNamespace(String resourceGroupName, String namespaceName) {
        return (new PagedListConverter<ArmDisasterRecoveryInner, EventHubDisasterRecoveryPairing>() {
            @Override
            public Observable<EventHubDisasterRecoveryPairing> typeConvertAsync(final ArmDisasterRecoveryInner inner) {
                return Observable.<EventHubDisasterRecoveryPairing>just(wrapModel(inner));
            }
        }).convert(inner().list(resourceGroupName, namespaceName));
    }

    @Override
    public Observable<EventHubDisasterRecoveryPairing> listByNamespaceAsync(String resourceGroupName, String namespaceName) {
        return this.inner().listAsync(resourceGroupName, namespaceName)
                .flatMapIterable(new Func1<Page<ArmDisasterRecoveryInner>, Iterable<ArmDisasterRecoveryInner>>() {
                    @Override
                    public Iterable<ArmDisasterRecoveryInner> call(Page<ArmDisasterRecoveryInner> page) {
                        return page.items();
                    }
                })
                .map(new Func1<ArmDisasterRecoveryInner, EventHubDisasterRecoveryPairing>() {
                    @Override
                    public EventHubDisasterRecoveryPairing call(ArmDisasterRecoveryInner inner) {
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

    private EventHubDisasterRecoveryPairingImpl wrapModel(ArmDisasterRecoveryInner innerModel) {
        return new EventHubDisasterRecoveryPairingImpl(innerModel.name(), innerModel, this.manager);
    }
}
