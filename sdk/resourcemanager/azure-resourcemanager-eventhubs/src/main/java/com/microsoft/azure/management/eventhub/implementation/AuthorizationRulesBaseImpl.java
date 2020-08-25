/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

@LangDefinition
abstract class AuthorizationRulesBaseImpl<InnerT, RuleT, RuleImpl>
        extends WrapperImpl<InnerT>
        implements
        HasManager<EventHubManager>,
        SupportsGettingById<RuleT>,
        SupportsDeletingById {

    protected final EventHubManager manager;

    protected AuthorizationRulesBaseImpl(EventHubManager manager, InnerT inner) {
        super(inner);
        this.manager = manager;
    }

    @Override
    public EventHubManager manager() {
        return this.manager;
    }

    @Override
    public RuleT getById(String id) {
        return getByIdAsync(id).toBlocking().last();
    }

    @Override
    public ServiceFuture<RuleT> getByIdAsync(String id, ServiceCallback<RuleT> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).await();
    }

    @Override
    public ServiceFuture<Void> deleteByIdAsync(String id, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(deleteByIdAsync(id), callback);
    }

    protected abstract RuleImpl wrapModel(AuthorizationRuleInner innerModel);
    public abstract Observable<RuleT> getByIdAsync(String id);
    public abstract Completable deleteByIdAsync(String id);
}
