/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.EventHubNamespaceAuthorizationRule;
import com.microsoft.azure.management.eventhub.EventHubNamespaceAuthorizationRules;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.Objects;

/**
 * Implementation for {@link EventHubNamespaceAuthorizationRules}.
 */
@LangDefinition
class EventHubNamespaceAuthorizationRulesImpl
        extends AuthorizationRulesBaseImpl<NamespacesInner, EventHubNamespaceAuthorizationRule, EventHubNamespaceAuthorizationRuleImpl>
        implements EventHubNamespaceAuthorizationRules {

    EventHubNamespaceAuthorizationRulesImpl(EventHubManager manager) {
        super(manager, manager.inner().namespaces());
    }

    @Override
    public EventHubNamespaceAuthorizationRuleImpl define(String name) {
        return new EventHubNamespaceAuthorizationRuleImpl(name, this.manager);
    }

    @Override
    public Observable<EventHubNamespaceAuthorizationRule> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);

        return getByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public EventHubNamespaceAuthorizationRule getByName(String resourceGroupName, String namespaceName, String name) {
        return getByNameAsync(resourceGroupName, namespaceName, name).toBlocking().last();
    }

    @Override
    public Observable<EventHubNamespaceAuthorizationRule> getByNameAsync(String resourceGroupName, String namespaceName, String name) {
        return this.inner().getAuthorizationRuleAsync(resourceGroupName,
                namespaceName,
                name)
                .map(new Func1<AuthorizationRuleInner, EventHubNamespaceAuthorizationRule>() {
                    @Override
                    public EventHubNamespaceAuthorizationRule call(AuthorizationRuleInner inner) {
                        if (inner == null) {
                            return null;
                        } else {
                            return wrapModel(inner);
                        }
                    }
                });
    }

    @Override
    public PagedList<EventHubNamespaceAuthorizationRule> listByNamespace(final String resourceGroupName, final String namespaceName) {
        return (new PagedListConverter<AuthorizationRuleInner, EventHubNamespaceAuthorizationRule>() {
            @Override
            public Observable<EventHubNamespaceAuthorizationRule> typeConvertAsync(final AuthorizationRuleInner inner) {
                return Observable.<EventHubNamespaceAuthorizationRule>just(wrapModel(inner));
            }
        }).convert(inner().listAuthorizationRules(resourceGroupName, namespaceName));
    }

    @Override
    public Observable<EventHubNamespaceAuthorizationRule> listByNamespaceAsync(String resourceGroupName, String namespaceName) {
        return this.inner().listAuthorizationRulesAsync(resourceGroupName, namespaceName)
                .flatMapIterable(new Func1<Page<AuthorizationRuleInner>, Iterable<AuthorizationRuleInner>>() {
                    @Override
                    public Iterable<AuthorizationRuleInner> call(Page<AuthorizationRuleInner> page) {
                        return page.items();
                    }
                })
                .map(new Func1<AuthorizationRuleInner, EventHubNamespaceAuthorizationRule>() {
                    @Override
                    public EventHubNamespaceAuthorizationRule call(AuthorizationRuleInner inner) {
                        return wrapModel(inner);
                    }
                });
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
        return this.inner().deleteAuthorizationRuleAsync(resourceGroupName,
                namespaceName,
                name).toCompletable();
    }

    @Override
    public void deleteByName(String resourceGroupName, String namespaceName, String name) {
        deleteByNameAsync(resourceGroupName, namespaceName, name).await();
    }

    @Override
    protected EventHubNamespaceAuthorizationRuleImpl wrapModel(AuthorizationRuleInner innerModel) {
        return new EventHubNamespaceAuthorizationRuleImpl(innerModel.name(), innerModel, this.manager);
    }
}
