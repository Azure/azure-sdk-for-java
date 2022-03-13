// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.models.EHNamespaceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.VoidIndexable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceSkuType;
import com.azure.resourcemanager.eventhubs.models.Sku;
import com.azure.resourcemanager.eventhubs.models.SkuName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Implementation for {@link EventHubNamespace}.
 */
class EventHubNamespaceImpl
        extends GroupableResourceImpl<EventHubNamespace, EHNamespaceInner, EventHubNamespaceImpl, EventHubsManager>
        implements
        EventHubNamespace,
        EventHubNamespace.Definition,
        EventHubNamespace.Update {

    private Flux<Indexable> postRunTasks;

    protected EventHubNamespaceImpl(String name, EHNamespaceInner innerObject, EventHubsManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public EventHubNamespaceSkuType sku() {
        return new EventHubNamespaceSkuType(this.innerModel().sku());
    }

    @Override
    public String azureInsightMetricId() {
        return this.innerModel().metricId();
    }

    @Override
    public String serviceBusEndpoint() {
        return this.innerModel().serviceBusEndpoint();
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
    public String provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public boolean isAutoScaleEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().isAutoInflateEnabled());
    }

    @Override
    public int currentThroughputUnits() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().sku().capacity());
    }

    @Override
    public int throughputUnitsUpperLimit() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().maximumThroughputUnits());
    }

    @Override
    public EventHubNamespaceImpl withNewEventHub(final String eventHubName) {
        concatPostRunTask(manager().eventHubs()
            .define(eventHubName)
            .withExistingNamespace(resourceGroupName(), name())
            .createAsync()
            .cast(Indexable.class));
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewEventHub(final String eventHubName, final int partitionCount) {
        concatPostRunTask(manager().eventHubs()
            .define(eventHubName)
            .withExistingNamespace(resourceGroupName(), name())
            .withPartitionCount(partitionCount)
            .createAsync()
            .cast(Indexable.class));
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewEventHub(
        final String eventHubName, final int partitionCount, final int retentionPeriodInDays) {
        concatPostRunTask(manager().eventHubs()
            .define(eventHubName)
            .withExistingNamespace(resourceGroupName(), name())
            .withPartitionCount(partitionCount)
            .withRetentionPeriodInDays(retentionPeriodInDays)
            .createAsync()
            .cast(Indexable.class));
        return this;
    }

    @Override
    public Update withoutEventHub(final String eventHubName) {
        concatPostRunTask(manager().eventHubs()
            .deleteByNameAsync(resourceGroupName(), name(), eventHubName)
            .map(aVoid -> new VoidIndexable(UUID.randomUUID().toString())));
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewSendRule(final String ruleName) {
        concatPostRunTask(manager().namespaceAuthorizationRules()
            .define(ruleName)
            .withExistingNamespace(resourceGroupName(), name())
            .withSendAccess()
            .createAsync()
            .cast(Indexable.class));
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewListenRule(final String ruleName) {
        concatPostRunTask(manager().namespaceAuthorizationRules()
            .define(ruleName)
            .withExistingNamespace(resourceGroupName(), name())
            .withListenAccess()
            .createAsync()
            .cast(Indexable.class));
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewManageRule(final String ruleName) {
        concatPostRunTask(manager().namespaceAuthorizationRules()
            .define(ruleName)
            .withExistingNamespace(resourceGroupName(), name())
            .withManageAccess()
            .createAsync()
            .cast(Indexable.class));
        return this;
    }

    @Override
    public EventHubNamespaceImpl withoutAuthorizationRule(final String ruleName) {
        concatPostRunTask(manager().namespaceAuthorizationRules()
            .deleteByNameAsync(resourceGroupName(), name(), ruleName)
            .map(aVoid -> new VoidIndexable(UUID.randomUUID().toString())));
        return this;
    }

    @Override
    public EventHubNamespaceImpl withAutoScaling() {
        // Auto-inflate requires a Sku > 'Basic' with capacity.
        this.setDefaultSkuIfNotSet();
        this.innerModel().withIsAutoInflateEnabled(true);
        if (this.innerModel().maximumThroughputUnits() == null) {
            // Required when auto-inflate is set & use portal default.
            this.withThroughputUnitsUpperLimit(20);
        }
        return this;
    }

    @Override
    public EventHubNamespaceImpl withSku(EventHubNamespaceSkuType namespaceSku) {
        Sku newSkuInner = new Sku()
                .withName(namespaceSku.name())
                .withTier(namespaceSku.tier())
                .withCapacity(null);
        Sku currentSkuInner = this.innerModel().sku();

        boolean isDifferent = currentSkuInner == null || !currentSkuInner.name().equals(newSkuInner.name());
        if (isDifferent) {
            this.innerModel().withSku(newSkuInner);
            if (newSkuInner.name().equals(SkuName.STANDARD)) {
                newSkuInner.withCapacity(1);
            }
        }
        return this;
    }

    @Override
    public EventHubNamespaceImpl withCurrentThroughputUnits(int units) {
        this.setDefaultSkuIfNotSet();
        this.innerModel().sku().withCapacity(units);
        return this;
    }

    @Override
    public EventHubNamespaceImpl withThroughputUnitsUpperLimit(int units) {
        this.innerModel().withMaximumThroughputUnits(units);
        return this;
    }

    @Override
    public void beforeGroupCreateOrUpdate() {
        if (postRunTasks != null) {
            addPostRunDependent(context -> postRunTasks.last());
        }
    }

    @Override
    public Mono<EventHubNamespace> createResourceAsync() {
        return this.manager().serviceClient().getNamespaces()
                .createOrUpdateAsync(resourceGroupName(), name(), this.innerModel())
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        postRunTasks = null;
        return Mono.empty();
    }

    @Override
    public PagedFlux<EventHub> listEventHubsAsync() {
        return this.manager().eventHubs().listByNamespaceAsync(resourceGroupName(), name());
    }

    @Override
    public PagedFlux<EventHubNamespaceAuthorizationRule> listAuthorizationRulesAsync() {
        return this.manager().namespaceAuthorizationRules()
            .listByNamespaceAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public PagedIterable<EventHub> listEventHubs() {
        return this.manager().eventHubs().listByNamespace(resourceGroupName(), name());
    }

    @Override
    public PagedIterable<EventHubNamespaceAuthorizationRule> listAuthorizationRules() {
        return this.manager().namespaceAuthorizationRules()
                .listByNamespace(this.resourceGroupName(), this.name());
    }

    @Override
    protected Mono<EHNamespaceInner> getInnerAsync() {
        return this.manager().serviceClient().getNamespaces()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private void setDefaultSkuIfNotSet() {
        if (this.innerModel().sku() == null) {
            this.withSku(EventHubNamespaceSkuType.STANDARD);
        }
    }

    private void concatPostRunTask(Mono<Indexable> task) {
        if (postRunTasks == null) {
            postRunTasks = Flux.empty();
        }
        postRunTasks = postRunTasks.concatWith(task);
    }
}
