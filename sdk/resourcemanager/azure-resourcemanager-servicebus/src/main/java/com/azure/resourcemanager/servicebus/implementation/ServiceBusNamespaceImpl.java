// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.models.SBNamespaceInner;
import com.azure.resourcemanager.servicebus.models.NamespaceAuthorizationRule;
import com.azure.resourcemanager.servicebus.models.NamespaceSku;
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.SBSku;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.Topic;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for ServiceBusNamespace.
 */
class ServiceBusNamespaceImpl
    extends GroupableResourceImpl<
        ServiceBusNamespace,
        SBNamespaceInner,
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

    ServiceBusNamespaceImpl(String name, SBNamespaceInner inner, ServiceBusManager manager) {
        super(name, inner, manager);
        this.initChildrenOperationsCache();
    }

    @Override
    public String dnsLabel() {
        return this.innerModel().name();
    }

    @Override
    public String fqdn() {
        return this.innerModel().serviceBusEndpoint();
    }

    @Override
    public NamespaceSku sku() {
        return new NamespaceSku(this.innerModel().sku());
    }

    @Override
    public OffsetDateTime createdAt() {
        return this.innerModel().createdAt();
    }

    @Override
    public OffsetDateTime updatedAt() {
        return this.innerModel().updatedAt();
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
        this.innerModel().withSku(new SBSku()
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
    protected Mono<SBNamespaceInner> getInnerAsync() {
        return this.manager().serviceClient().getNamespaces().getByResourceGroupAsync(this.resourceGroupName(),
                this.name());
    }

    @Override
    public Mono<ServiceBusNamespace> createResourceAsync() {
        Mono<SBNamespaceInner> createTask = this.manager().serviceClient().getNamespaces()
            .createOrUpdateAsync(this.resourceGroupName(),
                    this.name(),
                    this.innerModel())
            .map(inner -> {
                setInner(inner);
                return inner;
            });

        Flux<Void> childOperationTasks = submitChildrenOperationsAsync();
        final ServiceBusNamespace self = this;
        return Flux.concat(createTask, childOperationTasks)
            .doOnTerminate(() -> initChildrenOperationsCache())
            .then(Mono.just(self));
    }

    private void initChildrenOperationsCache() {
        this.queuesToCreate = new ArrayList<>();
        this.topicsToCreate = new ArrayList<>();
        this.rulesToCreate = new ArrayList<>();
        this.queuesToDelete = new ArrayList<>();
        this.topicsToDelete = new ArrayList<>();
        this.rulesToDelete = new ArrayList<>();
    }

    private Flux<Void> submitChildrenOperationsAsync() {
        Flux<Void> queuesCreateStream = Flux.empty();
        if (this.queuesToCreate.size() > 0) {
            queuesCreateStream = this.queues().createAsync(this.queuesToCreate).then().flux();
        }
        Flux<Void> topicsCreateStream = Flux.empty();
        if (this.topicsToCreate.size() > 0) {
            topicsCreateStream = this.topics().createAsync(this.topicsToCreate).then().flux();
        }
        Flux<Void> rulesCreateStream = Flux.empty();
        if (this.rulesToCreate.size() > 0) {
            rulesCreateStream = this.authorizationRules().createAsync(this.rulesToCreate).then().flux();
        }
        Flux<Void> queuesDeleteStream = Flux.empty();
        if (this.queuesToDelete.size() > 0) {
            queuesDeleteStream = this.queues().deleteByNameAsync(this.queuesToDelete);
        }
        Flux<Void> topicsDeleteStream = Flux.empty();
        if (this.topicsToDelete.size() > 0) {
            topicsDeleteStream = this.topics().deleteByNameAsync(this.topicsToDelete);
        }
        Flux<Void> rulesDeleteStream = Flux.empty();
        if (this.rulesToDelete.size() > 0) {
            rulesDeleteStream = this.authorizationRules().deleteByNameAsync(this.rulesToDelete);
        }
        return Flux.mergeDelayError(32,
            queuesCreateStream,
            topicsCreateStream,
            rulesCreateStream,
            queuesDeleteStream,
            topicsDeleteStream,
            rulesDeleteStream);
    }
}
