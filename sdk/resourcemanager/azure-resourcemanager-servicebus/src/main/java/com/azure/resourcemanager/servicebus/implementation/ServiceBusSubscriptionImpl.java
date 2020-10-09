// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.models.SubscriptionResourceInner;
import com.azure.resourcemanager.servicebus.models.EntityStatus;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.SubscriptionCreateOrUpdateParameters;
import com.azure.resourcemanager.servicebus.models.Topic;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Implementation for Subscription.
 */
class ServiceBusSubscriptionImpl extends
    IndependentChildResourceImpl<
        ServiceBusSubscription,
        Topic,
        SubscriptionResourceInner,
        ServiceBusSubscriptionImpl,
        ServiceBusManager>
    implements
        ServiceBusSubscription,
        ServiceBusSubscription.Definition,
        ServiceBusSubscription.Update {
    private final String namespaceName;
    private final Region region;

    ServiceBusSubscriptionImpl(String resourceGroupName,
                     String namespaceName,
                     String topicName,
                     String name,
                     Region region,
                     SubscriptionResourceInner inner,
                     ServiceBusManager manager) {
        super(name, inner, manager);
        this.namespaceName = namespaceName;
        this.region = region;
        this.withExistingParentResource(resourceGroupName, topicName);
        if (inner.location() == null) {
            inner.withLocation(this.region.toString());
        }
    }

    @Override
    public OffsetDateTime createdAt() {
        return this.innerModel().createdAt();
    }

    @Override
    public OffsetDateTime accessedAt() {
        return this.innerModel().accessedAt();
    }

    @Override
    public OffsetDateTime updatedAt() {
        return this.innerModel().updatedAt();
    }

    @Override
    public boolean isBatchedOperationsEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().enableBatchedOperations());
    }

    @Override
    public boolean isDeadLetteringEnabledForExpiredMessages() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().deadLetteringOnMessageExpiration());
    }

    @Override
    public boolean isSessionEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().requiresSession());
    }

    @Override
    public long lockDurationInSeconds() {
        if (this.innerModel().lockDuration() == null) {
            return 0;
        }
        TimeSpan timeSpan = TimeSpan.parse(this.innerModel().lockDuration());
        return (long) timeSpan.totalSeconds();
    }

    @Override
    public long deleteOnIdleDurationInMinutes() {
        if (this.innerModel().autoDeleteOnIdle() == null) {
            return 0;
        }
        TimeSpan timeSpan = TimeSpan.parse(this.innerModel().autoDeleteOnIdle());
        return (long) timeSpan.totalMinutes();
    }

    @Override
    public Duration defaultMessageTtlDuration() {
        if (this.innerModel().defaultMessageTimeToLive() == null) {
            return null;
        }
        return TimeSpan.parse(this.innerModel().defaultMessageTimeToLive()).toDuration();
    }

    @Override
    public int maxDeliveryCountBeforeDeadLetteringMessage() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().maxDeliveryCount());
    }

    @Override
    public long messageCount() {
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().messageCount());
    }

    @Override
    public long activeMessageCount() {
        if (this.innerModel().countDetails() == null
                || this.innerModel().countDetails().activeMessageCount() == null) {
            return 0;
        }
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().countDetails().activeMessageCount());
    }

    @Override
    public long deadLetterMessageCount() {
        if (this.innerModel().countDetails() == null
                || this.innerModel().countDetails().deadLetterMessageCount() == null) {
            return 0;
        }
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().countDetails().deadLetterMessageCount());
    }

    @Override
    public long scheduledMessageCount() {
        if (this.innerModel().countDetails() == null
                || this.innerModel().countDetails().scheduledMessageCount() == null) {
            return 0;
        }
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().countDetails().scheduledMessageCount());
    }

    @Override
    public long transferDeadLetterMessageCount() {
        if (this.innerModel().countDetails() == null
                || this.innerModel().countDetails().transferDeadLetterMessageCount() == null) {
            return 0;
        }
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().countDetails().transferDeadLetterMessageCount());
    }

    @Override
    public long transferMessageCount() {
        if (this.innerModel().countDetails() == null
                || this.innerModel().countDetails().transferMessageCount() == null) {
            return 0;
        }
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().countDetails().transferMessageCount());
    }

    @Override
    public EntityStatus status() {
        return this.innerModel().status();
    }

    @Override
    public boolean isDeadLetteringEnabledForFilterEvaluationFailedMessages() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().deadLetteringOnFilterEvaluationExceptions());
    }

    @Override
    public ServiceBusSubscriptionImpl withDeleteOnIdleDurationInMinutes(int durationInMinutes) {
        TimeSpan timeSpan = new TimeSpan().withMinutes(durationInMinutes);
        this.innerModel().withAutoDeleteOnIdle(timeSpan.toString());
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withMessageLockDurationInSeconds(int durationInSeconds) {
        TimeSpan timeSpan = new TimeSpan().withSeconds(durationInSeconds);
        this.innerModel().withLockDuration(timeSpan.toString());
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withDefaultMessageTTL(Duration ttl) {
        this.innerModel().withDefaultMessageTimeToLive(TimeSpan.fromDuration(ttl).toString());
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withSession() {
        this.innerModel().withRequiresSession(true);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withoutSession() {
        this.innerModel().withRequiresSession(false);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withMessageBatching() {
        this.innerModel().withEnableBatchedOperations(true);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withoutMessageBatching() {
        this.innerModel().withEnableBatchedOperations(false);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withMessageMovedToDeadLetterSubscriptionOnMaxDeliveryCount(int deliveryCount) {
        this.innerModel().withMaxDeliveryCount(deliveryCount);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException() {
        this.innerModel().withDeadLetteringOnFilterEvaluationExceptions(true);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withoutMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException() {
        this.innerModel().withDeadLetteringOnFilterEvaluationExceptions(false);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withExpiredMessageMovedToDeadLetterSubscription() {
        this.innerModel().withDeadLetteringOnMessageExpiration(true);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withoutExpiredMessageMovedToDeadLetterSubscription() {
        this.innerModel().withDeadLetteringOnMessageExpiration(false);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(int deliveryCount) {
        this.innerModel().withMaxDeliveryCount(deliveryCount);
        return this;
    }

    @Override
    protected Mono<SubscriptionResourceInner> getInnerAsync() {
        return this.manager().serviceClient().getSubscriptions()
                .getAsync(this.resourceGroupName(),
                        this.namespaceName,
                        this.parentName,
                        this.name());
    }

    @Override
    protected Mono<ServiceBusSubscription> createChildResourceAsync() {
        final ServiceBusSubscription self = this;
        return this.manager().serviceClient().getSubscriptions()
            .createOrUpdateAsync(this.resourceGroupName(),
                    this.namespaceName,
                    this.parentName,
                    this.name(),
                    prepareForCreate(this.innerModel()))
            .map(inner -> {
                setInner(inner);
                return self;
            });
    }

    private SubscriptionCreateOrUpdateParameters prepareForCreate(SubscriptionResourceInner inner) {
        return new SubscriptionCreateOrUpdateParameters()
            .withAutoDeleteOnIdle(inner.autoDeleteOnIdle())
            .withDefaultMessageTimeToLive(inner.defaultMessageTimeToLive())
            .withDeadLetteringOnFilterEvaluationExceptions(inner.deadLetteringOnFilterEvaluationExceptions())
            .withDeadLetteringOnMessageExpiration(inner.deadLetteringOnMessageExpiration())
            .withEnableBatchedOperations(inner.enableBatchedOperations())
            .withEntityAvailabilityStatus(inner.entityAvailabilityStatus())
            .withIsReadOnly(inner.isReadOnly())
            .withLockDuration(inner.lockDuration())
            .withMaxDeliveryCount(inner.maxDeliveryCount())
            .withRequiresSession(inner.requiresSession())
            .withStatus(inner.status())
            .withLocation(inner.location());
    }
}
