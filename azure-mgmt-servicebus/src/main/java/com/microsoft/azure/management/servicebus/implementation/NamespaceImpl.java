/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.servicebus.*;
import org.joda.time.DateTime;
import rx.Completable;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for Namespace.
 */
@LangDefinition
class NamespaceImpl extends GroupableResourceImpl<
        Namespace,
        NamespaceResourceInner,
        NamespaceImpl,
        ServiceBusManager>
        implements
        Namespace,
        Namespace.Definition,
        Namespace.Update {
    private List<Creatable<Queue>> queuesToCreate;
    private List<Creatable<Topic>> topicsToCreate;
    private List<Creatable<NamespaceAuthorizationRule>> rulesToCreate;
    private List<String> queuesToDelete;
    private List<String> topicsToDelete;
    private List<String> rulesToDelete;

    NamespaceImpl(String name, NamespaceResourceInner inner, ServiceBusManager manager) {
        super(name, inner, manager);
        this.initChildrenOperationsCache();
    }

    @Override
    public String dnsLabel() {
        return this.inner().name();
    }

    @Override
    public String fqdn() {
        return this.inner().serviceBusEndpoint();
    }

    @Override
    public NamespaceSku sku() {
        return new NamespaceSku(this.inner().sku());
    }

    @Override
    public DateTime createdAt() {
        return this.inner().createdAt();
    }

    @Override
    public DateTime updatedAt() {
        return this.inner().updatedAt();
    }

    @Override
    public QueuesImpl queues() {
        return new QueuesImpl(this.resourceGroupName(),
                this.name(),
                this.manager());
    }

    @Override
    public TopicsImpl topics() {
        return new TopicsImpl(this.resourceGroupName(),
                this.name(),
                this.manager());
    }

    @Override
    public NamespaceAuthorizationRulesImpl authorizationRules() {
        return new NamespaceAuthorizationRulesImpl(this.resourceGroupName(),
                this.name(),
                manager());
    }

    @Override
    public NamespaceImpl withSku(NamespaceSku namespaceSku) {
        this.inner().withSku(new Sku()
                .withName(namespaceSku.name())
                .withTier(namespaceSku.tier())
                .withCapacity(namespaceSku.capacity()));
        return this;
    }

    @Override
    public NamespaceImpl withNewQueue(String name, int maxSizeInMB) {
        this.queuesToCreate.add(queues().define(name).withSizeInMB(maxSizeInMB));
        return this;
    }

    @Override
    public NamespaceImpl withoutQueue(String name) {
        this.queuesToDelete.add(name);
        return this;
    }

    @Override
    public NamespaceImpl withNewTopic(String name, int maxSizeInMB) {
        this.topicsToCreate.add(topics().define(name).withSizeInMB(maxSizeInMB));
        return this;
    }

    @Override
    public NamespaceImpl withoutTopic(String name) {
        this.topicsToDelete.add(name);
        return this;
    }

    @Override
    public NamespaceImpl withNewAuthorizationRule(String name, AccessRights... rights) {
        this.rulesToCreate.add(this.authorizationRules().define(name).withAccessRights(rights));
        return this;
    }

    @Override
    public NamespaceImpl withoutAuthorizationRule(String name) {
        this.rulesToDelete.add(name);
        return this;
    }

    @Override
    protected Observable<NamespaceResourceInner> getInnerAsync() {
        return this.manager().inner().namespaces().getByResourceGroupAsync(this.resourceGroupName(),
                this.name());
    }
    
    @Override
    public Observable<Namespace> createResourceAsync() {
        Completable createNamespaceCompletable = this.manager().inner().namespaces()
                .createOrUpdateAsync(this.resourceGroupName(),
                        this.name(),
                        this.inner())
                .map(new Func1<NamespaceResourceInner, NamespaceResourceInner>() {
                    @Override
                    public NamespaceResourceInner call(NamespaceResourceInner inner) {
                        setInner(inner);
                        return inner;
                    }
                }).toCompletable();
        Completable childrenOperationsCompletable = submitChildrenOperationsAsync();
        final Namespace self = this;
        return Completable.concat(createNamespaceCompletable, childrenOperationsCompletable)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        initChildrenOperationsCache();
                    }
                })
                .toObservable()
                .map(new Func1<Object, Namespace>() {
                    @Override
                    public Namespace call(Object o) {
                        initChildrenOperationsCache();
                        return self;
                    }
                });
    }

    private void initChildrenOperationsCache() {
        this.queuesToCreate = new ArrayList<>();
        this.topicsToCreate = new ArrayList<>();
        this.rulesToCreate = new ArrayList<>();
        this.queuesToDelete = new ArrayList<>();
        this.topicsToDelete = new ArrayList<>();
        this.rulesToDelete = new ArrayList<>();
    }

    private Completable submitChildrenOperationsAsync() {
        Observable<?> queuesCreateStream = Observable.empty();
        if (this.queuesToCreate.size() > 0) {
            queuesCreateStream = this.queues().createAsync(this.queuesToCreate);
        }
        Observable<?> topicsCreateStream = Observable.empty();
        if (this.topicsToCreate.size() > 0) {
            topicsCreateStream = this.topics().createAsync(this.topicsToCreate);
        }
        Observable<?> rulesCreateStream = Observable.empty();
        if (this.rulesToCreate.size() > 0) {
            rulesCreateStream = this.authorizationRules().createAsync(this.rulesToCreate);
        }
        Observable<?> queuesDeleteStream = Observable.empty();
        if (this.queuesToDelete.size() > 0) {
            queuesCreateStream = this.queues().deleteByNameAsync(this.queuesToDelete);
        }
        Observable<?> topicsDeleteStream = Observable.empty();
        if (this.topicsToDelete.size() > 0) {
            topicsDeleteStream = this.topics().deleteByNameAsync(this.topicsToDelete);
        }
        Observable<?> rulesDeleteStream = Observable.empty();
        if (this.rulesToDelete.size() > 0) {
            rulesDeleteStream = this.authorizationRules().deleteByNameAsync(this.rulesToDelete);
        }
        return Observable.mergeDelayError(queuesCreateStream,
                topicsCreateStream,
                rulesCreateStream,
                queuesDeleteStream,
                topicsDeleteStream,
                rulesDeleteStream).toCompletable();
    }

}
