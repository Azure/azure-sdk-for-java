// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.inner.TopicResourceInner;
import com.azure.resourcemanager.servicebus.models.EntityStatus;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.resourcemanager.servicebus.models.TopicAuthorizationRule;
import com.azure.resourcemanager.servicebus.models.TopicCreateOrUpdateParameters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for Topic.
 */
class TopicImpl
    extends IndependentChildResourceImpl<
        Topic,
        ServiceBusNamespaceImpl,
        TopicResourceInner,
        TopicImpl,
        ServiceBusManager>
    implements
        Topic,
        Topic.Definition,
        Topic.Update {
    private List<Creatable<ServiceBusSubscription>> subscriptionsToCreate;
    private List<Creatable<TopicAuthorizationRule>> rulesToCreate;
    private List<String> subscriptionsToDelete;
    private List<String> rulesToDelete;

    TopicImpl(String resourceGroupName,
              String namespaceName,
              String name,
              Region region,
              TopicResourceInner inner,
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
    public boolean isExpressEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().enableExpress());
    }

    @Override
    public boolean isPartitioningEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().enablePartitioning());
    }

    @Override
    public boolean isDuplicateDetectionEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().requiresDuplicateDetection());
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
    public int subscriptionCount() {
        if (this.inner().subscriptionCount() == null) {
            return 0;
        }
        return Utils.toPrimitiveInt(this.inner().subscriptionCount());
    }

    @Override
    public EntityStatus status() {
        return this.inner().status();
    }

    @Override
    public ServiceBusSubscriptionsImpl subscriptions() {
        return new ServiceBusSubscriptionsImpl(this.resourceGroupName(),
                this.parentName,
                this.name(),
                this.region(),
                manager());
    }

    @Override
    public TopicAuthorizationRulesImpl authorizationRules() {
        return new TopicAuthorizationRulesImpl(this.resourceGroupName(),
                this.parentName,
                this.name(),
                this.region(),
                manager());
    }

    @Override
    public TopicImpl withSizeInMB(long sizeInMB) {
        this.inner().withMaxSizeInMegabytes(sizeInMB);
        return this;
    }

    @Override
    public TopicImpl withPartitioning() {
        this.inner().withEnablePartitioning(true);
        return this;
    }

    @Override
    public TopicImpl withoutPartitioning() {
        this.inner().withEnablePartitioning(false);
        return this;
    }

    @Override
    public TopicImpl withDeleteOnIdleDurationInMinutes(int durationInMinutes) {
        TimeSpan timeSpan = new TimeSpan().withMinutes(durationInMinutes);
        this.inner().withAutoDeleteOnIdle(timeSpan.toString());
        return this;
    }

    @Override
    public TopicImpl withDefaultMessageTTL(Duration ttl) {
        this.inner().withDefaultMessageTimeToLive(TimeSpan.fromDuration(ttl).toString());
        return this;
    }

    @Override
    public TopicImpl withExpressMessage() {
        this.inner().withEnableExpress(true);
        return this;
    }

    @Override
    public TopicImpl withoutExpressMessage() {
        this.inner().withEnableExpress(false);
        return this;
    }

    @Override
    public TopicImpl withMessageBatching() {
        this.inner().withEnableBatchedOperations(true);
        return this;
    }

    @Override
    public TopicImpl withoutMessageBatching() {
        this.inner().withEnableBatchedOperations(false);
        return this;
    }

    @Override
    public TopicImpl withDuplicateMessageDetection(Duration duplicateDetectionHistoryDuration) {
        this.inner().withRequiresDuplicateDetection(true);
        this.inner().withDuplicateDetectionHistoryTimeWindow(TimeSpan
                .fromDuration(duplicateDetectionHistoryDuration)
                .toString());
        return this;
    }

    @Override
    public TopicImpl withDuplicateMessageDetectionHistoryDuration(Duration duration) {
        this.inner().withDuplicateDetectionHistoryTimeWindow(TimeSpan
                .fromDuration(duration)
                .toString());
        // Below shortcut cannot be used as 'withRequiresDuplicateDetection' cannot be changed
        // once the topic is created.
        // return withDuplicateMessageDetection(duration);
        return this;
    }

    @Override
    public TopicImpl withoutDuplicateMessageDetection() {
        this.inner().withRequiresDuplicateDetection(false);
        return this;
    }

    @Override
    public TopicImpl withNewSendRule(String name) {
        this.rulesToCreate.add(this.authorizationRules().define(name).withSendingEnabled());
        return this;
    }

    @Override
    public TopicImpl withNewListenRule(String name) {
        this.rulesToCreate.add(this.authorizationRules().define(name).withListeningEnabled());
        return this;
    }

    @Override
    public TopicImpl withNewManageRule(String name) {
        this.rulesToCreate.add(this.authorizationRules().define(name).withManagementEnabled());
        return this;
    }

    @Override
    public TopicImpl withoutAuthorizationRule(String name) {
        this.rulesToDelete.add(name);
        return this;
    }

    @Override
    public TopicImpl withNewSubscription(String name) {
        this.subscriptionsToCreate.add(this.subscriptions().define(name));
        return this;
    }

    @Override
    public TopicImpl withoutSubscription(String name) {
        this.subscriptionsToDelete.add(name);
        return this;
    }

    @Override
    protected Mono<TopicResourceInner> getInnerAsync() {
        return this.manager().inner().getTopics()
                .getAsync(this.resourceGroupName(),
                        this.parentName,
                        this.name());
    }

    @Override
    protected Mono<Topic> createChildResourceAsync() {
        Mono<TopicResourceInner> createTask = this.manager().inner().getTopics()
            .createOrUpdateAsync(this.resourceGroupName(),
                    this.parentName,
                    this.name(),
                    prepareForCreate(this.inner()))
            .map(inner -> {
                setInner(inner);
                return inner;
            });
        Flux<Void> childOperationTasks = submitChildrenOperationsAsync();
        final Topic self = this;
        return Flux.concat(createTask, childOperationTasks)
            .doOnTerminate(() -> initChildrenOperationsCache())
            .then(Mono.just(self));
    }

    private void initChildrenOperationsCache() {
        this.subscriptionsToCreate = new ArrayList<>();
        this.rulesToCreate = new ArrayList<>();
        this.subscriptionsToDelete = new ArrayList<>();
        this.rulesToDelete = new ArrayList<>();
    }

    private Flux<Void> submitChildrenOperationsAsync() {
        Flux<Void> subscriptionsCreateStream = Flux.empty();
        if (this.subscriptionsToCreate.size() > 0) {
            subscriptionsCreateStream = this.subscriptions().createAsync(this.subscriptionsToCreate).then().flux();
        }
        Flux<Void> rulesCreateStream = Flux.empty();
        if (this.rulesToCreate.size() > 0) {
            rulesCreateStream = this.authorizationRules().createAsync(this.rulesToCreate).then().flux();
        }
        Flux<Void> subscriptionsDeleteStream = Flux.empty();
        if (this.subscriptionsToDelete.size() > 0) {
            subscriptionsDeleteStream = this.subscriptions().deleteByNameAsync(this.subscriptionsToDelete);
        }
        Flux<Void> rulesDeleteStream = Flux.empty();
        if (this.rulesToDelete.size() > 0) {
            rulesDeleteStream = this.authorizationRules().deleteByNameAsync(this.rulesToDelete);
        }
        return Flux.mergeDelayError(32,
            subscriptionsCreateStream,
            rulesCreateStream,
            subscriptionsDeleteStream,
            rulesDeleteStream);
    }

    private TopicCreateOrUpdateParameters prepareForCreate(TopicResourceInner inner) {
        return new TopicCreateOrUpdateParameters()
            .withAutoDeleteOnIdle(inner.autoDeleteOnIdle())
            .withEntityAvailabilityStatus(inner.entityAvailabilityStatus())
            .withDefaultMessageTimeToLive(inner.defaultMessageTimeToLive())
            .withDuplicateDetectionHistoryTimeWindow(inner.duplicateDetectionHistoryTimeWindow())
            .withEnableBatchedOperations(inner.enableBatchedOperations())
            .withEnableExpress(inner.enableExpress())
            .withEnablePartitioning(inner.enablePartitioning())
            .withFilteringMessagesBeforePublishing(inner.filteringMessagesBeforePublishing())
            .withIsAnonymousAccessible(inner.isAnonymousAccessible())
            .withIsExpress(inner.isExpress())
            .withMaxSizeInMegabytes(inner.maxSizeInMegabytes())
            .withRequiresDuplicateDetection(inner.requiresDuplicateDetection())
            .withStatus(inner.status())
            .withSupportOrdering(inner.supportOrdering())
            .withLocation(inner.location());
    }
}
