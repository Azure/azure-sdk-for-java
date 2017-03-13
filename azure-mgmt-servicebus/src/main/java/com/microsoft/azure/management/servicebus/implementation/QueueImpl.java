/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.servicebus.*;
import org.joda.time.DateTime;
import org.joda.time.Period;
import rx.Observable;

/**
 * Implementation for Queue.
 */
class QueueImpl extends IndependentChildResourceImpl<Queue, NamespaceImpl, QueueResourceInner, QueueImpl, ServiceBusManager>
        implements
        Queue,
        Queue.Definition,
        Queue.Update  {

    QueueImpl(String name, QueueResourceInner innerObject, ServiceBusManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public Namespace parent() {
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
    public int maxSizeInMB() {
        return 0;
    }

    @Override
    public int currentSizeInBytes() {
        return 0;
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
    public boolean isExpressEnabled() {
        return false;
    }

    @Override
    public boolean isPartitioningEnabled() {
        return false;
    }

    @Override
    public boolean isSessionEnabled() {
        return false;
    }

    @Override
    public boolean isDuplicateDetectionEnabled() {
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
    public Period duplicateMessageDetectionHistoryDuration() {
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
    public QueueAuthorizationRules authorizationRules() {
        return null;
    }

    @Override
    protected Observable<QueueResourceInner> getInnerAsync() {
        return null;
    }

    @Override
    protected Observable<Queue> createChildResourceAsync() {
        return null;
    }

    @Override
    public QueueImpl withSizeInMB(int sizeInMB) {
        return this;
    }

    @Override
    public QueueImpl withPartitioning() {
        return this;
    }

    @Override
    public QueueImpl withoutPartitioning() {
        return this;
    }

    @Override
    public QueueImpl withDeleteOnIdleDurationInMinutes(int durationInMinutes) {
        return this;
    }

    @Override
    public QueueImpl withMessageLockDurationInSeconds(int durationInSeconds) {
        return this;
    }

    @Override
    public QueueImpl withDefaultMessageTTL(Period ttl) {
        return this;
    }

    @Override
    public QueueImpl withSession() {
        return this;
    }

    @Override
    public QueueImpl withoutSession() {
        return this;
    }

    @Override
    public QueueImpl withExpressMessage() {
        return this;
    }

    @Override
    public QueueImpl withoutExpressMessage() {
        return this;
    }

    @Override
    public QueueImpl withMessageBatching() {
        return this;
    }

    @Override
    public QueueImpl withoutMessageBatching() {
        return this;
    }

    @Override
    public QueueImpl withDuplicateMessageDetection(Period duplicateDetectionHistoryDuration) {
        return this;
    }

    @Override
    public QueueImpl withExpiredMessageMovedToDeadLetterQueue() {
        return this;
    }

    @Override
    public QueueImpl withoutExpiredMessageMovedToDeadLetterQueue() {
        return this;
    }

    @Override
    public QueueImpl withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(int deliveryCount) {
        return this;
    }

    @Override
    public QueueImpl withNewAuthorizationRule(String name, AccessRights... rights) {
        return this;
    }

    @Override
    public QueueImpl withoutNewAuthorizationRule(String name) {
        return this;
    }

    @Override
    public QueueImpl withDuplicateMessageDetectionHistoryDuration(Period duration) {
        return this;
    }

    @Override
    public Update withoutDuplicateMessageDetection() {
        return this;
    }
}
