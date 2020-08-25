/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.EventHubAuthorizationRule;
import com.microsoft.azure.management.eventhub.EventHubAuthorizationRules;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.Objects;

/**
 * Implementation for {@link EventHubAuthorizationRules}.
 */
@LangDefinition
class EventHubAuthorizationRulesImpl
        extends AuthorizationRulesBaseImpl<EventHubsInner,
        EventHubAuthorizationRule,
        EventHubAuthorizationRuleImpl>
        implements EventHubAuthorizationRules {

    EventHubAuthorizationRulesImpl(EventHubManager manager) {
        super(manager, manager.inner().eventHubs());
    }

    @Override
    public EventHubAuthorizationRuleImpl define(String name) {
        return new EventHubAuthorizationRuleImpl(name, this.manager);
    }

    @Override
    public Observable<EventHubAuthorizationRule> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);

        return getByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().parent().name(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public EventHubAuthorizationRule getByName(String resourceGroupName, String namespaceName, String eventHubName, String name) {
        return getByNameAsync(resourceGroupName, namespaceName, eventHubName, name).toBlocking().last();
    }

    @Override
    public Observable<EventHubAuthorizationRule> getByNameAsync(String resourceGroupName, String namespaceName, String eventHubName, String name) {
        return this.inner().getAuthorizationRuleAsync(resourceGroupName,
                namespaceName,
                eventHubName,
                name)
                .map(new Func1<AuthorizationRuleInner, EventHubAuthorizationRule>() {
                    @Override
                    public EventHubAuthorizationRule call(AuthorizationRuleInner inner) {
                        if (inner == null) {
                            return null;
                        } else {
                            return wrapModel(inner);
                        }
                    }
                });
    }

    @Override
    public PagedList<EventHubAuthorizationRule> listByEventHub(final String resourceGroupName, final String namespaceName, final String eventHubName) {
        return (new PagedListConverter<AuthorizationRuleInner, EventHubAuthorizationRule>() {
            @Override
            public Observable<EventHubAuthorizationRule> typeConvertAsync(final AuthorizationRuleInner inner) {
                return Observable.<EventHubAuthorizationRule>just(wrapModel(inner));
            }
        }).convert(inner().listAuthorizationRules(resourceGroupName, namespaceName, eventHubName));
    }

    @Override
    public Observable<EventHubAuthorizationRule> listByEventHubAsync(String resourceGroupName, String namespaceName, final String eventHubName) {
        return this.inner().listAuthorizationRulesAsync(resourceGroupName, namespaceName, eventHubName)
                .flatMapIterable(new Func1<Page<AuthorizationRuleInner>, Iterable<AuthorizationRuleInner>>() {
                    @Override
                    public Iterable<AuthorizationRuleInner> call(Page<AuthorizationRuleInner> page) {
                        return page.items();
                    }
                })
                .map(new Func1<AuthorizationRuleInner, EventHubAuthorizationRule>() {
                    @Override
                    public EventHubAuthorizationRule call(AuthorizationRuleInner inner) {
                        return wrapModel(inner);
                    }
                });
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
        return this.inner().deleteAuthorizationRuleAsync(resourceGroupName,
                namespaceName,
                eventHubName,
                name).toCompletable();
    }

    @Override
    public void deleteByName(String resourceGroupName, String namespaceName, String eventHubName, String name) {
        deleteByNameAsync(resourceGroupName, namespaceName, eventHubName, name).await();
    }

    @Override
    protected EventHubAuthorizationRuleImpl wrapModel(AuthorizationRuleInner innerModel) {
        return new EventHubAuthorizationRuleImpl(innerModel.name(), innerModel, this.manager);
    }
}
