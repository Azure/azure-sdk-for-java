// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.inner.SubscriptionResourceInner;
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
        return this.inner().createdAt();
    }

    @Override
    public OffsetDateTime accessedAt() {
        return this.inner().accessedAt();
    }

    @Override
    public OffsetDateTime updatedAt() {
        return this.inner().updatedAt();
    }

    @Override
    public boolean isBatchedOperationsEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().enableBatchedOperations());
    }

    @Override
    public boolean isDeadLetteringEnabledForExpiredMessages() {
        return Utils.toPrimitiveBoolean(this.inner().deadLetteringOnMessageExpiration());
    }

    @Override
    public boolean isSessionEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().requiresSession());
    }

    @Override
    public long lockDurationInSeconds() {
        if (this.inner().lockDuration() == null) {
            return 0;
        }
        TimeSpan timeSpan = TimeSpan.parse(this.inner().lockDuration());
        return (long) timeSpan.totalSeconds();
    }

    @Override
    public long deleteOnIdleDurationInMinutes() {
        if (this.inner().autoDeleteOnIdle() == null) {
            return 0;
        }
        TimeSpan timeSpan = TimeSpan.parse(this.inner().autoDeleteOnIdle());
        return (long) timeSpan.totalMinutes();
    }

    @Override
    public Duration defaultMessageTtlDuration() {
        if (this.inner().defaultMessageTimeToLive() == null) {
            return null;
        }
        return TimeSpan.parse(this.inner().defaultMessageTimeToLive()).toDuration();
    }

    @Override
    public int maxDeliveryCountBeforeDeadLetteringMessage() {
        return Utils.toPrimitiveInt(this.inner().maxDeliveryCount());
    }

    @Override
    public long messageCount() {
        return Utils.toPrimitiveLong(this.inner().messageCount());
    }

    @Override
    public long activeMessageCount() {
        if (this.inner().countDetails() == null
                || this.inner().countDetails().activeMessageCount() == null) {
            return 0;
        }
        return Utils.toPrimitiveLong(this.inner().countDetails().activeMessageCount());
    }

    @Override
    public long deadLetterMessageCount() {
        if (this.inner().countDetails() == null
                || this.inner().countDetails().deadLetterMessageCount() == null) {
            return 0;
        }
        return Utils.toPrimitiveLong(this.inner().countDetails().deadLetterMessageCount());
    }

    @Override
    public long scheduledMessageCount() {
        if (this.inner().countDetails() == null
                || this.inner().countDetails().scheduledMessageCount() == null) {
            return 0;
        }
        return Utils.toPrimitiveLong(this.inner().countDetails().scheduledMessageCount());
    }

    @Override
    public long transferDeadLetterMessageCount() {
        if (this.inner().countDetails() == null
                || this.inner().countDetails().transferDeadLetterMessageCount() == null) {
            return 0;
        }
        return Utils.toPrimitiveLong(this.inner().countDetails().transferDeadLetterMessageCount());
    }

    @Override
    public long transferMessageCount() {
        if (this.inner().countDetails() == null
                || this.inner().countDetails().transferMessageCount() == null) {
            return 0;
        }
        return Utils.toPrimitiveLong(this.inner().countDetails().transferMessageCount());
    }

    @Override
    public EntityStatus status() {
        return this.inner().status();
    }

    @Override
    public boolean isDeadLetteringEnabledForFilterEvaluationFailedMessages() {
        return Utils.toPrimitiveBoolean(this.inner().deadLetteringOnFilterEvaluationExceptions());
    }

    @Override
    public ServiceBusSubscriptionImpl withDeleteOnIdleDurationInMinutes(int durationInMinutes) {
        TimeSpan timeSpan = new TimeSpan().withMinutes(durationInMinutes);
        this.inner().withAutoDeleteOnIdle(timeSpan.toString());
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withMessageLockDurationInSeconds(int durationInSeconds) {
        TimeSpan timeSpan = new TimeSpan().withSeconds(durationInSeconds);
        this.inner().withLockDuration(timeSpan.toString());
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withDefaultMessageTTL(Duration ttl) {
        this.inner().withDefaultMessageTimeToLive(TimeSpan.fromDuration(ttl).toString());
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withSession() {
        this.inner().withRequiresSession(true);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withoutSession() {
        this.inner().withRequiresSession(false);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withMessageBatching() {
        this.inner().withEnableBatchedOperations(true);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withoutMessageBatching() {
        this.inner().withEnableBatchedOperations(false);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withMessageMovedToDeadLetterSubscriptionOnMaxDeliveryCount(int deliveryCount) {
        this.inner().withMaxDeliveryCount(deliveryCount);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException() {
        this.inner().withDeadLetteringOnFilterEvaluationExceptions(true);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withoutMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException() {
        this.inner().withDeadLetteringOnFilterEvaluationExceptions(false);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withExpiredMessageMovedToDeadLetterSubscription() {
        this.inner().withDeadLetteringOnMessageExpiration(true);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withoutExpiredMessageMovedToDeadLetterSubscription() {
        this.inner().withDeadLetteringOnMessageExpiration(false);
        return this;
    }

    @Override
    public ServiceBusSubscriptionImpl withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(int deliveryCount) {
        this.inner().withMaxDeliveryCount(deliveryCount);
        return this;
    }

    @Override
    protected Mono<SubscriptionResourceInner> getInnerAsync() {
        return this.manager().inner().getSubscriptions()
                .getAsync(this.resourceGroupName(),
                        this.namespaceName,
                        this.parentName,
                        this.name());
    }

    @Override
    protected Mono<ServiceBusSubscription> createChildResourceAsync() {
        final ServiceBusSubscription self = this;
        return this.manager().inner().getSubscriptions()
            .createOrUpdateAsync(this.resourceGroupName(),
                    this.namespaceName,
                    this.parentName,
                    this.name(),
                    prepareForCreate(this.inner()))
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
