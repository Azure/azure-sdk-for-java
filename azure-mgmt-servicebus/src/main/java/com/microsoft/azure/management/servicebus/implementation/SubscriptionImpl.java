/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
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
    SubscriptionImpl(String name, SubscriptionResourceInner innerObject, ServiceBusManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public Topic parent() {
        return null;
    }

    @Override
    public DateTime createdAt() {
        return null;
    }

    @Override
    public DateTime accessedAt() {
        return null;
    }

    @Override
    public DateTime updatedAt() {
        return null;
    }

    @Override
    public boolean isBatchedOperationsEnabled() {
        return false;
    }

    @Override
    public boolean isDeadLetteringEnabledForExpiredMessages() {
        return false;
    }

    @Override
    public boolean isSessionEnabled() {
        return false;
    }

    @Override
    public int lockDurationInSeconds() {
        return 0;
    }

    @Override
    public int deleteOnIdleDurationInMinutes() {
        return 0;
    }

    @Override
    public Period defaultMessageTtlDuration() {
        return null;
    }

    @Override
    public int maxDeliveryCountBeforeDeadLetteringMessage() {
        return 0;
    }

    @Override
    public int messageCount() {
        return 0;
    }

    @Override
    public int activeMessageCount() {
        return 0;
    }

    @Override
    public int deadLetterMessageCount() {
        return 0;
    }

    @Override
    public int scheduledMessageCount() {
        return 0;
    }

    @Override
    public int transferDeadLetterMessageCount() {
        return 0;
    }

    @Override
    public int transferMessageCount() {
        return 0;
    }

    @Override
    public EntityStatus status() {
        return null;
    }

    @Override
    public boolean isDeadLetteringEnabledForFilterEvaluationFailedMessages() {
        return false;
    }

    @Override
    public SubscriptionAuthorizationRules authorizationRules() {
        return null;
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
