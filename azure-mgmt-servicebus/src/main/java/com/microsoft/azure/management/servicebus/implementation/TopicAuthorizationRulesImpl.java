/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.management.servicebus.TopicAuthorizationRule;
import com.microsoft.azure.management.servicebus.TopicAuthorizationRules;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Completable;
import rx.Observable;

/**
 * Implementation for TopicAuthorizationRules.
 */
@LangDefinition
class TopicAuthorizationRulesImpl
        extends ServiceBusChildResourcesImpl<
        TopicAuthorizationRule,
        TopicAuthorizationRuleImpl,
        SharedAccessAuthorizationRuleInner,
        TopicsInner,
        ServiceBusManager,
        Topic>
        implements TopicAuthorizationRules {
    private final String resourceGroupName;
    private final String namespaceName;
    private final String topicName;
    private final Region region;

    TopicAuthorizationRulesImpl(String resourceGroupName,
                                String namespaceName,
                                String topicName,
                                Region region,
                                ServiceBusManager manager) {
        super(manager.inner().topics(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.topicName = topicName;
        this.region = region;
    }

    @Override
    public TopicAuthorizationRuleImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Completable deleteByNameAsync(String name) {
        return this.inner().deleteAuthorizationRuleAsync(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name).toCompletable();
    }

    @Override
    public ServiceFuture<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback) {
        return this.inner().deleteAuthorizationRuleAsync(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name,
                callback);
    }

    @Override
    protected Observable<SharedAccessAuthorizationRuleInner> getInnerByNameAsync(String name) {
        return this.inner().getAuthorizationRuleAsync(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name);
    }

    @Override
    protected Observable<ServiceResponse<Page<SharedAccessAuthorizationRuleInner>>> listInnerAsync() {
        return this.inner().listAuthorizationRulesWithServiceResponseAsync(this.resourceGroupName,
                this.namespaceName,
                this.topicName);
    }

    @Override
    protected PagedList<SharedAccessAuthorizationRuleInner> listInner() {
        return this.inner().listAuthorizationRules(this.resourceGroupName,
                this.namespaceName,
                this.topicName);
    }

    @Override
    protected TopicAuthorizationRuleImpl wrapModel(String name) {
        return new TopicAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name,
                this.region,
                new SharedAccessAuthorizationRuleInner(),
                this.manager());
    }

    @Override
    protected TopicAuthorizationRuleImpl wrapModel(SharedAccessAuthorizationRuleInner inner) {
        if (inner == null) {
            return null;
        }
        return new TopicAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                inner.name(),
                this.region,
                inner,
                this.manager());
    }

    @Override
    public PagedList<TopicAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }

    @Override
    public Observable<TopicAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }
}