/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubConsumerGroup;
import org.joda.time.DateTime;
import rx.Observable;

import java.util.Objects;

/**
 * Implementation for {@link EventHubConsumerGroup}.
 */
@LangDefinition
class EventHubConsumerGroupImpl
        extends NestedResourceImpl<EventHubConsumerGroup,
        ConsumerGroupInner,
        EventHubConsumerGroupImpl>
        implements
        EventHubConsumerGroup,
        EventHubConsumerGroup.Definition,
        EventHubConsumerGroup.Update {

    private Ancestors.TwoAncestor ancestor;

    EventHubConsumerGroupImpl(String name, ConsumerGroupInner inner, EventHubManager manager) {
        super(name, inner, manager);
        this.ancestor =  new Ancestors().new TwoAncestor(inner.id());
    }

    EventHubConsumerGroupImpl(String name, EventHubManager manager) {
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
    public DateTime createdAt() {
        return this.inner().createdAt();
    }

    @Override
    public DateTime updatedAt() {
        return this.inner().updatedAt();
    }

    @Override
    public String userMetadata() {
        return this.inner().userMetadata();
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
    public EventHubConsumerGroupImpl withExistingEventHub(String resourceGroupName, String namespaceName, String eventHubName) {
        this.ancestor = new Ancestors().new TwoAncestor(resourceGroupName, eventHubName, namespaceName);
        return this;
    }

    @Override
    public EventHubConsumerGroupImpl withUserMetadata(String metadata) {
        this.inner().withUserMetadata(metadata);
        return this;
    }

    @Override
    public Observable<EventHubConsumerGroup> createResourceAsync() {
        return this.manager.inner().consumerGroups()
                .createOrUpdateAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor2Name(),
                        this.ancestor().ancestor1Name(),
                        this.name(),
                        this.inner().userMetadata())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Observable<ConsumerGroupInner> getInnerAsync() {
        return this.manager.inner().consumerGroups()
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
