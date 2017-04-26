/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.NamespaceAuthorizationRule;
import com.microsoft.azure.management.servicebus.NamespaceSku;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.Sku;
import com.microsoft.azure.management.servicebus.Topic;
import org.joda.time.DateTime;
import rx.Completable;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for ServiceBusNamespace.
 */
@LangDefinition
class ServiceBusNamespaceImpl extends GroupableResourceImpl<
        ServiceBusNamespace,
        NamespaceInner,
        ServiceBusNamespaceImpl,
        ServiceBusManager>
        implements
        ServiceBusNamespace,
        ServiceBusNamespace.Definition,
        ServiceBusNamespace.Update {
    private List<Creatable<Queue>> queuesToCreate;
    private List<Creatable<Topic>> topicsToCreate;
    private List<Creatable<NamespaceAuthorizationRule>> rulesToCreate;
    private List<String> queuesToDelete;
    private List<String> topicsToDelete;
    private List<String> rulesToDelete;

    ServiceBusNamespaceImpl(String name, NamespaceInner inner, ServiceBusManager manager) {
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
                this.region(),
                this.manager());
    }

    @Override
    public TopicsImpl topics() {
        return new TopicsImpl(this.resourceGroupName(),
                this.name(),
                this.region(),
                this.manager());
    }

    @Override
    public NamespaceAuthorizationRulesImpl authorizationRules() {
        return new NamespaceAuthorizationRulesImpl(this.resourceGroupName(),
                this.name(),
                this.region(),
                manager());
    }

    @Override
    public ServiceBusNamespaceImpl withSku(NamespaceSku namespaceSku) {
        this.inner().withSku(new Sku()
                .withName(namespaceSku.name())
                .withTier(namespaceSku.tier())
                .withCapacity(namespaceSku.capacity()));
        return this;
    }

    @Override
    public ServiceBusNamespaceImpl withNewQueue(String name, int maxSizeInMB) {
        this.queuesToCreate.add(queues().define(name).withSizeInMB(maxSizeInMB));
        return this;
    }

    @Override
    public ServiceBusNamespaceImpl withoutQueue(String name) {
        this.queuesToDelete.add(name);
        return this;
    }

    @Override
    public ServiceBusNamespaceImpl withNewTopic(String name, int maxSizeInMB) {
        this.topicsToCreate.add(topics().define(name).withSizeInMB(maxSizeInMB));
        return this;
    }

    @Override
    public ServiceBusNamespaceImpl withoutTopic(String name) {
        this.topicsToDelete.add(name);
        return this;
    }

    @Override
    public ServiceBusNamespaceImpl withNewSendRule(String name) {
        this.rulesToCreate.add(this.authorizationRules().define(name).withSendingEnabled());
        return this;
    }

    @Override
    public ServiceBusNamespaceImpl withNewListenRule(String name) {
        this.rulesToCreate.add(this.authorizationRules().define(name).withListeningEnabled());
        return this;
    }

    @Override
    public ServiceBusNamespaceImpl withNewManageRule(String name) {
        this.rulesToCreate.add(this.authorizationRules().define(name).withManagementEnabled());
        return this;
    }

    @Override
    public ServiceBusNamespaceImpl withoutAuthorizationRule(String name) {
        this.rulesToDelete.add(name);
        return this;
    }

    @Override
    protected Observable<NamespaceInner> getInnerAsync() {
        return this.manager().inner().namespaces().getByResourceGroupAsync(this.resourceGroupName(),
                this.name());
    }

    @Override
    public Observable<ServiceBusNamespace> createResourceAsync() {
        Completable createNamespaceCompletable = this.manager().inner().namespaces()
                .createOrUpdateAsync(this.resourceGroupName(),
                        this.name(),
                        this.inner())
                .map(new Func1<NamespaceInner, NamespaceInner>() {
                    @Override
                    public NamespaceInner call(NamespaceInner inner) {
                        setInner(inner);
                        return inner;
                    }
                }).toCompletable();
        Completable childrenOperationsCompletable = submitChildrenOperationsAsync();
        final ServiceBusNamespace self = this;
        return Completable.concat(createNamespaceCompletable, childrenOperationsCompletable)
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        initChildrenOperationsCache();
                    }
                })
                .andThen(Observable.just(self));
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
            queuesDeleteStream = this.queues().deleteByNameAsync(this.queuesToDelete);
        }
        Observable<?> topicsDeleteStream = Observable.empty();
        if (this.topicsToDelete.size() > 0) {
            topicsDeleteStream = this.topics().deleteByNameAsync(this.topicsToDelete);
        }
        Observable<?> rulesDeleteStream = Observable.empty();
        if (this.rulesToDelete.size() > 0) {
            rulesDeleteStream = this.authorizationRules().deleteByNameAsync(this.rulesToDelete);
        }
        return Completable.mergeDelayError(queuesCreateStream.toCompletable(),
                topicsCreateStream.toCompletable(),
                rulesCreateStream.toCompletable(),
                queuesDeleteStream.toCompletable(),
                topicsDeleteStream.toCompletable(),
                rulesDeleteStream.toCompletable());
    }
}