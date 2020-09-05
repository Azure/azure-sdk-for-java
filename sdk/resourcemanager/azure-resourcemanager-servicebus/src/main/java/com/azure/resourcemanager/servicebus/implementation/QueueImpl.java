// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.inner.QueueResourceInner;
import com.azure.resourcemanager.servicebus.models.EntityStatus;
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.QueueAuthorizationRule;
import com.azure.resourcemanager.servicebus.models.QueueCreateOrUpdateParameters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for Queue.
 */
class QueueImpl
    extends IndependentChildResourceImpl<
        Queue,
        ServiceBusNamespaceImpl,
        QueueResourceInner,
        QueueImpl,
        ServiceBusManager>
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
              QueueResourceInner inner,
              ServiceBusManager manager) {
        super(name, inner, manager);
        this.withExistingParentResource(resourceGroupName, namespaceName);
        initChildrenOperationsCache();
        if (inner.location() == null) {
            inner.withLocation(region.toString());
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
    public Duration defaultMessageTtlDuration() {
        if (this.inner().defaultMessageTimeToLive() == null) {
            return null;
        }
        return TimeSpan.parse(this.inner().defaultMessageTimeToLive()).toDuration();
    }

    @Override
    public Duration duplicateMessageDetectionHistoryDuration() {
        if (this.inner().duplicateDetectionHistoryTimeWindow() == null) {
            return null;
        }
        return TimeSpan.parse(this.inner().duplicateDetectionHistoryTimeWindow()).toDuration();
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
    public QueueImpl withDefaultMessageTTL(Duration ttl) {
        this.inner().withDefaultMessageTimeToLive(TimeSpan.fromDuration(ttl).toString());
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
    public QueueImpl withDuplicateMessageDetection(Duration duplicateDetectionHistoryDuration) {
        this.inner().withRequiresDuplicateDetection(true);
        this.inner().withDuplicateDetectionHistoryTimeWindow(TimeSpan
                .fromDuration(duplicateDetectionHistoryDuration)
                .toString());
        return this;
    }

    @Override
    public QueueImpl withDuplicateMessageDetectionHistoryDuration(Duration duration) {
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
    protected Mono<QueueResourceInner> getInnerAsync() {
        return this.manager().inner().getQueues()
                .getAsync(this.resourceGroupName(),
                        this.parentName,
                        this.name());
    }

    @Override
    protected Mono<Queue> createChildResourceAsync() {

        Mono<QueueResourceInner> createTask = this.manager().inner().getQueues()
            .createOrUpdateAsync(this.resourceGroupName(),
                this.parentName,
                this.name(),
                prepareForCreate(this.inner()))
            .map(inner -> {
                setInner(inner);
                return inner;
            });
        Flux<Void> childOperationTasks = submitChildrenOperationsAsync();
        final Queue self = this;
        return Flux.concat(createTask, childOperationTasks)
            .doOnTerminate(() -> initChildrenOperationsCache())
            .then(Mono.just(self));
    }

    private void initChildrenOperationsCache() {
        this.rulesToCreate = new ArrayList<>();
        this.rulesToDelete = new ArrayList<>();
    }

    private Flux<Void> submitChildrenOperationsAsync() {
        Flux<Void> rulesCreateStream = Flux.empty();
        if (this.rulesToCreate.size() > 0) {
            rulesCreateStream = this.authorizationRules().createAsync(this.rulesToCreate).then().flux();
        }
        Flux<Void> rulesDeleteStream = Flux.empty();
        if (this.rulesToDelete.size() > 0) {
            rulesDeleteStream = this.authorizationRules().deleteByNameAsync(this.rulesToDelete);
        }
        return Flux.mergeDelayError(32, rulesCreateStream,
                rulesDeleteStream);
    }

    private QueueCreateOrUpdateParameters prepareForCreate(QueueResourceInner inner) {
        return new QueueCreateOrUpdateParameters()
            .withLockDuration(inner.lockDuration())
            .withAutoDeleteOnIdle(inner.autoDeleteOnIdle())
            .withEntityAvailabilityStatus(inner.entityAvailabilityStatus())
            .withDefaultMessageTimeToLive(inner.defaultMessageTimeToLive())
            .withDuplicateDetectionHistoryTimeWindow(inner.duplicateDetectionHistoryTimeWindow())
            .withEnableBatchedOperations(inner.enableBatchedOperations())
            .withDeadLetteringOnMessageExpiration(inner.deadLetteringOnMessageExpiration())
            .withEnableExpress(inner.enableExpress())
            .withEnablePartitioning(inner.enablePartitioning())
            .withIsAnonymousAccessible(inner.isAnonymousAccessible())
            .withMaxDeliveryCount(inner.maxDeliveryCount())
            .withMaxSizeInMegabytes(inner.maxSizeInMegabytes())
            .withRequiresDuplicateDetection(inner.requiresDuplicateDetection())
            .withRequiresSession(inner.requiresSession())
            .withStatus(inner.status())
            .withSupportOrdering(inner.supportOrdering())
            .withLocation(inner.location());
    }
}
