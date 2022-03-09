// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.models.SBQueueInner;
import com.azure.resourcemanager.servicebus.models.EntityStatus;
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.QueueAuthorizationRule;
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
        SBQueueInner,
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
              SBQueueInner inner,
              ServiceBusManager manager) {
        super(name, inner, manager);
        this.withExistingParentResource(resourceGroupName, namespaceName);
        initChildrenOperationsCache();
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
    public long maxSizeInMB() {
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().maxSizeInMegabytes().longValue());
    }

    @Override
    public long currentSizeInBytes() {
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().sizeInBytes());
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
    public boolean isExpressEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().enableExpress());
    }

    @Override
    public boolean isPartitioningEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().enablePartitioning());
    }

    @Override
    public boolean isSessionEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().requiresSession());
    }

    @Override
    public boolean isDuplicateDetectionEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().requiresDuplicateDetection());
    }

    @Override
    public long lockDurationInSeconds() {
        if (this.innerModel().lockDuration() == null) {
            return 0;
        }
        return this.innerModel().lockDuration().getSeconds();
    }

    @Override
    public long deleteOnIdleDurationInMinutes() {
        if (this.innerModel().autoDeleteOnIdle() == null) {
            return 0;
        }
        return this.innerModel().autoDeleteOnIdle().toMinutes();
    }

    @Override
    public Duration defaultMessageTtlDuration() {
        if (this.innerModel().defaultMessageTimeToLive() == null) {
            return null;
        }
        return this.innerModel().defaultMessageTimeToLive();
    }

    @Override
    public Duration duplicateMessageDetectionHistoryDuration() {
        if (this.innerModel().duplicateDetectionHistoryTimeWindow() == null) {
            return null;
        }
        return this.innerModel().duplicateDetectionHistoryTimeWindow();
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
    public QueueAuthorizationRulesImpl authorizationRules() {
        return new QueueAuthorizationRulesImpl(this.resourceGroupName(),
                this.parentName,
                this.name(),
                this.region(),
                manager());
    }

    @Override
    public QueueImpl withSizeInMB(long sizeInMB) {
        this.innerModel().withMaxSizeInMegabytes((int) sizeInMB);
        return this;
    }

    @Override
    public QueueImpl withPartitioning() {
        this.innerModel().withEnablePartitioning(true);
        return this;
    }

    @Override
    public QueueImpl withoutPartitioning() {
        this.innerModel().withEnablePartitioning(false);
        return this;
    }

    @Override
    public QueueImpl withDeleteOnIdleDurationInMinutes(int durationInMinutes) {
        this.innerModel().withAutoDeleteOnIdle(Duration.ofMinutes(durationInMinutes));
        return this;
    }

    @Override
    public QueueImpl withMessageLockDurationInSeconds(int durationInSeconds) {
        this.innerModel().withLockDuration(Duration.ofSeconds(durationInSeconds));
        return this;
    }

    @Override
    public QueueImpl withDefaultMessageTTL(Duration ttl) {
        this.innerModel().withDefaultMessageTimeToLive(ttl);
        return this;
    }

    @Override
    public QueueImpl withSession() {
        this.innerModel().withRequiresSession(true);
        return this;
    }

    @Override
    public QueueImpl withoutSession() {
        this.innerModel().withRequiresSession(false);
        return this;
    }

    @Override
    public QueueImpl withExpressMessage() {
        this.innerModel().withEnableExpress(true);
        return this;
    }

    @Override
    public QueueImpl withoutExpressMessage() {
        this.innerModel().withEnableExpress(false);
        return this;
    }

    @Override
    public QueueImpl withMessageBatching() {
        this.innerModel().withEnableBatchedOperations(true);
        return this;
    }

    @Override
    public QueueImpl withoutMessageBatching() {
        this.innerModel().withEnableBatchedOperations(false);
        return this;
    }

    @Override
    public QueueImpl withExpiredMessageMovedToDeadLetterQueue() {
        this.innerModel().withDeadLetteringOnMessageExpiration(true);
        return this;
    }

    @Override
    public QueueImpl withoutExpiredMessageMovedToDeadLetterQueue() {
        this.innerModel().withDeadLetteringOnMessageExpiration(false);
        return this;
    }

    @Override
    public QueueImpl withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(int deliveryCount) {
        this.innerModel().withMaxDeliveryCount(deliveryCount);
        return this;
    }

    @Override
    public QueueImpl withDuplicateMessageDetection(Duration duplicateDetectionHistoryDuration) {
        this.innerModel().withRequiresDuplicateDetection(true);
        this.innerModel().withDuplicateDetectionHistoryTimeWindow(duplicateDetectionHistoryDuration);
        return this;
    }

    @Override
    public QueueImpl withDuplicateMessageDetectionHistoryDuration(Duration duration) {
        return withDuplicateMessageDetection(duration);
    }

    @Override
    public QueueImpl withoutDuplicateMessageDetection() {
        this.innerModel().withRequiresDuplicateDetection(false);
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
    protected Mono<SBQueueInner> getInnerAsync() {
        return this.manager().serviceClient().getQueues()
                .getAsync(this.resourceGroupName(),
                        this.parentName,
                        this.name());
    }

    @Override
    protected Mono<Queue> createChildResourceAsync() {

        Mono<SBQueueInner> createTask = this.manager().serviceClient().getQueues()
            .createOrUpdateAsync(this.resourceGroupName(),
                this.parentName,
                this.name(),
                this.innerModel())
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
}
