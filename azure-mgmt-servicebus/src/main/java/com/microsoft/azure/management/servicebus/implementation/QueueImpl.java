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
 * Implementation for Queue.
 */
@LangDefinition
class QueueImpl extends IndependentChildResourceImpl<Queue, NamespaceImpl, QueueResourceInner, QueueImpl, ServiceBusManager>
        implements
        Queue,
        Queue.Definition,
        Queue.Update  {

    QueueImpl(String resourceGroupName,
              String namespaceName,
              String name,
              QueueResourceInner inner,
              ServiceBusManager manager) {
        super(name, inner, manager);
        this.withExistingParentResource(resourceGroupName, namespaceName);
    }

    @Override
    public Namespace parent() {
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
    public long maxSizeInMB() {
        return Utils.toPrimitiveLong(this.inner().maxSizeInMegabytes());
    }

    @Override
    public long currentSizeInBytes() {
        return Utils.toPrimitiveLong(this.inner().sizeInBytes());
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
    public boolean isExpressEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().enableExpress());
    }

    @Override
    public boolean isPartitioningEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().enablePartitioning());
    }

    @Override
    public boolean isSessionEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().requiresSession());
    }

    @Override
    public boolean isDuplicateDetectionEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().requiresDuplicateDetection());
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
    public Period duplicateMessageDetectionHistoryDuration() {
        if (this.inner().duplicateDetectionHistoryTimeWindow() == null) {
            return null;
        }
        TimeSpan timeSpan = TimeSpan.parse(this.inner().duplicateDetectionHistoryTimeWindow());
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
    public QueueAuthorizationRules authorizationRules() {
        return new QueueAuthorizationRulesImpl(this.resourceGroupName(),
                this.parentName,
                this.name(),
                manager());
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
    public QueueImpl withoutDuplicateMessageDetection() {
        return this;
    }
}
