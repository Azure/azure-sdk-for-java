/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.servicebus.EntityStatus;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.QueueAuthorizationRule;
import org.joda.time.DateTime;
import org.joda.time.Period;
import rx.Completable;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for Queue.
 */
@LangDefinition
class QueueImpl extends IndependentChildResourceImpl<Queue, ServiceBusNamespaceImpl, QueueInner, QueueImpl, ServiceBusManager>
        implements
        Queue,
        Queue.Definition,
        Queue.Update  {
    private List<Creatable<QueueAuthorizationRule>> rulesToCreate;
    private List<String> rulesToDelete;

    QueueImpl(String resourceGroupName,
              String namespaceName,
              String name,
              Region region,
              QueueInner inner,
              ServiceBusManager manager) {
        super(name, inner, manager);
        this.withExistingParentResource(resourceGroupName, namespaceName);
        initChildrenOperationsCache();
        if (inner.location() == null) {
            inner.withLocation(region.toString());
        }
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
                .withMinutes(timeSpan.minutes())
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
                .withMinutes(timeSpan.minutes())
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
    public QueueAuthorizationRulesImpl authorizationRules() {
        return new QueueAuthorizationRulesImpl(this.resourceGroupName(),
                this.parentName,
                this.name(),
                this.region(),
                manager());
    }

    @Override
    public QueueImpl withSizeInMB(long sizeInMB) {
        this.inner().withMaxSizeInMegabytes(sizeInMB);
        return this;
    }

    @Override
    public QueueImpl withPartitioning() {
        this.inner().withEnablePartitioning(true);
        return this;
    }

    @Override
    public QueueImpl withoutPartitioning() {
        this.inner().withEnablePartitioning(false);
        return this;
    }

    @Override
    public QueueImpl withDeleteOnIdleDurationInMinutes(int durationInMinutes) {
        TimeSpan timeSpan = new TimeSpan().withMinutes(durationInMinutes);
        this.inner().withAutoDeleteOnIdle(timeSpan.toString());
        return this;
    }

    @Override
    public QueueImpl withMessageLockDurationInSeconds(int durationInSeconds) {
        TimeSpan timeSpan = new TimeSpan().withSeconds(durationInSeconds);
        this.inner().withLockDuration(timeSpan.toString());
        return this;
    }

    @Override
    public QueueImpl withDefaultMessageTTL(Period ttl) {
        this.inner().withDefaultMessageTimeToLive(TimeSpan.fromPeriod(ttl).toString());
        return this;
    }

    @Override
    public QueueImpl withSession() {
        this.inner().withRequiresSession(true);
        return this;
    }

    @Override
    public QueueImpl withoutSession() {
        this.inner().withRequiresSession(false);
        return this;
    }

    @Override
    public QueueImpl withExpressMessage() {
        this.inner().withEnableExpress(true);
        return this;
    }

    @Override
    public QueueImpl withoutExpressMessage() {
        this.inner().withEnableExpress(false);
        return this;
    }

    @Override
    public QueueImpl withMessageBatching() {
        this.inner().withEnableBatchedOperations(true);
        return this;
    }

    @Override
    public QueueImpl withoutMessageBatching() {
        this.inner().withEnableBatchedOperations(false);
        return this;
    }

    @Override
    public QueueImpl withExpiredMessageMovedToDeadLetterQueue() {
        this.inner().withDeadLetteringOnMessageExpiration(true);
        return this;
    }

    @Override
    public QueueImpl withoutExpiredMessageMovedToDeadLetterQueue() {
        this.inner().withDeadLetteringOnMessageExpiration(false);
        return this;
    }

    @Override
    public QueueImpl withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(int deliveryCount) {
        this.inner().withMaxDeliveryCount(deliveryCount);
        return this;
    }

    @Override
    public QueueImpl withDuplicateMessageDetection(Period duplicateDetectionHistoryDuration) {
        this.inner().withRequiresDuplicateDetection(true);
        this.inner().withDuplicateDetectionHistoryTimeWindow(TimeSpan
                .fromPeriod(duplicateDetectionHistoryDuration)
                .toString());
        return this;
    }

    @Override
    public QueueImpl withDuplicateMessageDetectionHistoryDuration(Period duration) {
        return withDuplicateMessageDetection(duration);
    }

    @Override
    public QueueImpl withoutDuplicateMessageDetection() {
        this.inner().withRequiresDuplicateDetection(false);
        return this;
    }

    @Override
    public QueueImpl withNewSendRule(String name) {
        this.rulesToCreate.add(this.authorizationRules().define(name).withSendingEnabled());
        return this;
    }

    @Override
    public QueueImpl withNewListenRule(String name) {
        this.rulesToCreate.add(this.authorizationRules().define(name).withListeningEnabled());
        return this;
    }

    @Override
    public QueueImpl withNewManageRule(String name) {
        this.rulesToCreate.add(this.authorizationRules().define(name).withManagementEnabled());
        return this;
    }

    @Override
    public QueueImpl withoutAuthorizationRule(String name) {
        this.rulesToDelete.add(name);
        return this;
    }

    @Override
    protected Observable<QueueInner> getInnerAsync() {
        return this.manager().inner().queues()
                .getAsync(this.resourceGroupName(),
                        this.parentName,
                        this.name());
    }

    @Override
    protected Observable<Queue> createChildResourceAsync() {
        Completable createQueueCompletable = this.manager().inner().queues()
                .createOrUpdateAsync(this.resourceGroupName(),
                        this.parentName,
                        this.name(),
                        this.inner())
                .map(new Func1<QueueInner, QueueInner>() {
                    @Override
                    public QueueInner call(QueueInner inner) {
                        setInner(inner);
                        return inner;
                    }
                }).toCompletable();
        Completable childrenOperationsCompletable = submitChildrenOperationsAsync();
        final Queue self = this;
        return Completable.concat(createQueueCompletable, childrenOperationsCompletable)
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        initChildrenOperationsCache();
                    }
                })
                .andThen(Observable.just(self));
    }

    private void initChildrenOperationsCache() {
        this.rulesToCreate = new ArrayList<>();
        this.rulesToDelete = new ArrayList<>();
    }

    private Completable submitChildrenOperationsAsync() {
        Observable<?> rulesCreateStream = Observable.empty();
        if (this.rulesToCreate.size() > 0) {
            rulesCreateStream = this.authorizationRules().createAsync(this.rulesToCreate);
        }
        Observable<?> rulesDeleteStream = Observable.empty();
        if (this.rulesToDelete.size() > 0) {
            rulesDeleteStream = this.authorizationRules().deleteByNameAsync(this.rulesToDelete);
        }
        return Completable.mergeDelayError(rulesCreateStream.toCompletable(),
                rulesDeleteStream.toCompletable());
    }
}