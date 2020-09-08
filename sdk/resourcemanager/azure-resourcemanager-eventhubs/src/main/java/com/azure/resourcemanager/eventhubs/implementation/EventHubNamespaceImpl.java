// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.inner.EHNamespaceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.VoidIndexable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
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
        return new EventHubNamespaceSkuType(this.inner().sku());
    }

    @Override
    public String azureInsightMetricId() {
        return this.inner().metricId();
    }

    @Override
    public String serviceBusEndpoint() {
        return this.inner().serviceBusEndpoint();
    }

    @Override
    public OffsetDateTime createdAt() {
        return this.inner().createdAt();
    }

    @Override
    public OffsetDateTime updatedAt() {
        return this.inner().updatedAt();
    }

    @Override
    public String provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public boolean isAutoScaleEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().isAutoInflateEnabled());
    }

    @Override
    public int currentThroughputUnits() {
        return Utils.toPrimitiveInt(this.inner().sku().capacity());
    }

    @Override
    public int throughputUnitsUpperLimit() {
        return Utils.toPrimitiveInt(this.inner().maximumThroughputUnits());
    }

    @Override
    public EventHubNamespaceImpl withNewEventHub(final String eventHubName) {
        concatPostRunTask(manager().eventHubs()
            .define(eventHubName)
            .withExistingNamespace(resourceGroupName(), name())
            .createAsync()
            .last());
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewEventHub(final String eventHubName, final int partitionCount) {
        concatPostRunTask(manager().eventHubs()
            .define(eventHubName)
            .withExistingNamespace(resourceGroupName(), name())
            .withPartitionCount(partitionCount)
            .createAsync()
            .last());
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
            .last());
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
            .last());
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewListenRule(final String ruleName) {
        concatPostRunTask(manager().namespaceAuthorizationRules()
            .define(ruleName)
            .withExistingNamespace(resourceGroupName(), name())
            .withListenAccess()
            .createAsync()
            .last());
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewManageRule(final String ruleName) {
        concatPostRunTask(manager().namespaceAuthorizationRules()
            .define(ruleName)
            .withExistingNamespace(resourceGroupName(), name())
            .withManageAccess()
            .createAsync()
            .last());
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
        this.inner().withIsAutoInflateEnabled(true);
        if (this.inner().maximumThroughputUnits() == null) {
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
        Sku currentSkuInner = this.inner().sku();

        boolean isDifferent = currentSkuInner == null || !currentSkuInner.name().equals(newSkuInner.name());
        if (isDifferent) {
            this.inner().withSku(newSkuInner);
            if (newSkuInner.name().equals(SkuName.STANDARD)) {
                newSkuInner.withCapacity(1);
            }
        }
        return this;
    }

    @Override
    public EventHubNamespaceImpl withCurrentThroughputUnits(int units) {
        this.setDefaultSkuIfNotSet();
        this.inner().sku().withCapacity(units);
        return this;
    }

    @Override
    public EventHubNamespaceImpl withThroughputUnitsUpperLimit(int units) {
        this.inner().withMaximumThroughputUnits(units);
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
        return this.manager().inner().getNamespaces()
                .createOrUpdateAsync(resourceGroupName(), name(), this.inner())
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
        return this.manager().inner().getNamespaces().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private void setDefaultSkuIfNotSet() {
        if (this.inner().sku() == null) {
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
