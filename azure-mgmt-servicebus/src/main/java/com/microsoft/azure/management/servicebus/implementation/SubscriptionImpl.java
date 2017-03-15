/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.servicebus.*;
import org.joda.time.DateTime;
import org.joda.time.Period;
import rx.Observable;

/**
 * Implementation for Subscription.
 */
@LangDefinition
class SubscriptionImpl extends
        IndependentChildResourceImpl<Subscription, Topic, SubscriptionResourceInner, SubscriptionImpl, ServiceBusManager>
        implements
        Subscription,
        Subscription.Definition,
        Subscription.Update {
    private final String namespaceName;

    SubscriptionImpl(String resourceGroupName,
                     String namespaceName,
                     String topicName,
                     String name,
                     SubscriptionResourceInner inner,
                     ServiceBusManager manager) {
        super(name, inner, manager);
        this.namespaceName = namespaceName;
        this.withExistingParentResource(resourceGroupName, topicName);
    }

    @Override
    public Topic parent() {
        return null;
    }

    @Override
    public DateTime createdAt() {
        return this.inner().createdAt();
    }

    @Override
    public DateTime accessedAt() {
        return this.inner().accessedAt();
    }

    @Override
    public DateTime updatedAt() {
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
    public Period defaultMessageTtlDuration() {
        if (this.inner().defaultMessageTimeToLive() == null) {
            return null;
        }
        TimeSpan timeSpan = TimeSpan.parse(this.inner().defaultMessageTimeToLive());
        return new Period()
                .withDays(timeSpan.days())
                .withHours(timeSpan.hours())
                .withSeconds(timeSpan.seconds())
                .withMillis(timeSpan.milliseconds());
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
    public SubscriptionAuthorizationRules authorizationRules() {
        return new SubscriptionAuthorizationRulesImpl(this.resourceGroupName(),
                this.namespaceName,
                this.parentName,
                this.name(),
                manager());
    }

    @Override
    protected Observable<SubscriptionResourceInner> getInnerAsync() {
        return null;
    }

    @Override
    protected Observable<Subscription> createChildResourceAsync() {
        return null;
    }

    @Override
    public SubscriptionImpl withDeleteOnIdleDurationInMinutes(int durationInMinutes) {
        return null;
    }

    @Override
    public SubscriptionImpl withMessageLockDurationInSeconds(int durationInSeconds) {
        return null;
    }

    @Override
    public SubscriptionImpl withDefaultMessageTTL(Period ttl) {
        return null;
    }

    @Override
    public SubscriptionImpl withSession() {
        return null;
    }

    @Override
    public SubscriptionImpl withoutSession() {
        return null;
    }

    @Override
    public SubscriptionImpl withMessageBatching() {
        return null;
    }

    @Override
    public SubscriptionImpl withoutMessageBatching() {
        return null;
    }

    @Override
    public SubscriptionImpl withExpiredMessageMovedToDeadLetterQueue() {
        return null;
    }

    @Override
    public SubscriptionImpl withMessageMovedToDeadLetterSubscriptionOnMaxDeliveryCount(int deliveryCount) {
        return null;
    }

    @Override
    public SubscriptionImpl withMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException() {
        return null;
    }

    @Override
    public SubscriptionImpl withoutMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException() {
        return null;
    }

    @Override
    public SubscriptionImpl withNewAuthorizationRule(String name, AccessRights... rights) {
        return null;
    }

    @Override
    public SubscriptionImpl withoutNewAuthorizationRule(String name) {
        return null;
    }

    @Override
    public SubscriptionImpl withExpiredMessageMovedToDeadLetterSubscription() {
        return null;
    }

    @Override
    public SubscriptionImpl withoutExpiredMessageMovedToDeadLetterSubscription() {
        return null;
    }

    @Override
    public SubscriptionImpl withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(int deliveryCount) {
        return null;
    }
}
