// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.models.ConsumerGroupInner;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroup;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Implementation for {@link EventHubConsumerGroup}.
 */
class EventHubConsumerGroupImpl
    extends NestedResourceImpl<EventHubConsumerGroup,
        ConsumerGroupInner,
        EventHubConsumerGroupImpl>
    implements EventHubConsumerGroup,
        EventHubConsumerGroup.Definition,
        EventHubConsumerGroup.Update {

    private Ancestors.TwoAncestor ancestor;

    EventHubConsumerGroupImpl(String name, ConsumerGroupInner inner, EventHubsManager manager) {
        super(name, inner, manager);
        this.ancestor =  new Ancestors().new TwoAncestor(inner.id());
    }

    EventHubConsumerGroupImpl(String name, EventHubsManager manager) {
        super(name, new ConsumerGroupInner(), manager);
    }

    @Override
    public String namespaceResourceGroupName() {
        return this.ancestor().resourceGroupName();
    }

    @Override
    public String namespaceName() {
        return this.ancestor().ancestor2Name();
    }

    @Override
    public String eventHubName() {
        return this.ancestor().ancestor1Name();
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
    public String userMetadata() {
        return this.innerModel().userMetadata();
    }

    @Override
    public EventHubConsumerGroupImpl withExistingEventHub(EventHub eventHub) {
        this.ancestor = new Ancestors().new TwoAncestor(selfId(eventHub.id()));
        return this;
    }

    @Override
    public EventHubConsumerGroupImpl withExistingEventHubId(String eventHubId) {
        this.ancestor = new Ancestors().new TwoAncestor(selfId(eventHubId));
        return this;
    }

    @Override
    public EventHubConsumerGroupImpl withExistingEventHub(
        String resourceGroupName, String namespaceName, String eventHubName) {
        this.ancestor = new Ancestors().new TwoAncestor(resourceGroupName, eventHubName, namespaceName);
        return this;
    }

    @Override
    public EventHubConsumerGroupImpl withUserMetadata(String metadata) {
        this.innerModel().withUserMetadata(metadata);
        return this;
    }

    @Override
    public Mono<EventHubConsumerGroup> createResourceAsync() {
        return this.manager.serviceClient().getConsumerGroups()
                .createOrUpdateAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor2Name(),
                        this.ancestor().ancestor1Name(),
                        this.name(),
                        this.innerModel().userMetadata())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<ConsumerGroupInner> getInnerAsync() {
        return this.manager.serviceClient().getConsumerGroups()
                .getAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor2Name(),
                        this.ancestor().ancestor1Name(),
                        this.name());
    }

    private Ancestors.TwoAncestor ancestor() {
        Objects.requireNonNull(this.ancestor);
        return this.ancestor;
    }

    private String selfId(String parentId) {
        return String.format("%s/consumerGroups/%s", parentId, this.name());
    }
}
