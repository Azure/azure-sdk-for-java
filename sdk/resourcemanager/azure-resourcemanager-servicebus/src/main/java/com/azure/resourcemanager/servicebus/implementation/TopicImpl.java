// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.models.TopicResourceInner;
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
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().maxSizeInMegabytes());
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
    public boolean isExpressEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().enableExpress());
    }

    @Override
    public boolean isPartitioningEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().enablePartitioning());
    }

    @Override
    public boolean isDuplicateDetectionEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().requiresDuplicateDetection());
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
    public Duration duplicateMessageDetectionHistoryDuration() {
        if (this.innerModel().duplicateDetectionHistoryTimeWindow() == null) {
            return null;
        }
        return TimeSpan.parse(this.innerModel().duplicateDetectionHistoryTimeWindow()).toDuration();
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
    public int subscriptionCount() {
        if (this.innerModel().subscriptionCount() == null) {
            return 0;
        }
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().subscriptionCount());
    }

    @Override
    public EntityStatus status() {
        return this.innerModel().status();
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
        this.innerModel().withMaxSizeInMegabytes(sizeInMB);
        return this;
    }

    @Override
    public TopicImpl withPartitioning() {
        this.innerModel().withEnablePartitioning(true);
        return this;
    }

    @Override
    public TopicImpl withoutPartitioning() {
        this.innerModel().withEnablePartitioning(false);
        return this;
    }

    @Override
    public TopicImpl withDeleteOnIdleDurationInMinutes(int durationInMinutes) {
        TimeSpan timeSpan = new TimeSpan().withMinutes(durationInMinutes);
        this.innerModel().withAutoDeleteOnIdle(timeSpan.toString());
        return this;
    }

    @Override
    public TopicImpl withDefaultMessageTTL(Duration ttl) {
        this.innerModel().withDefaultMessageTimeToLive(TimeSpan.fromDuration(ttl).toString());
        return this;
    }

    @Override
    public TopicImpl withExpressMessage() {
        this.innerModel().withEnableExpress(true);
        return this;
    }

    @Override
    public TopicImpl withoutExpressMessage() {
        this.innerModel().withEnableExpress(false);
        return this;
    }

    @Override
    public TopicImpl withMessageBatching() {
        this.innerModel().withEnableBatchedOperations(true);
        return this;
    }

    @Override
    public TopicImpl withoutMessageBatching() {
        this.innerModel().withEnableBatchedOperations(false);
        return this;
    }

    @Override
    public TopicImpl withDuplicateMessageDetection(Duration duplicateDetectionHistoryDuration) {
        this.innerModel().withRequiresDuplicateDetection(true);
        this.innerModel().withDuplicateDetectionHistoryTimeWindow(TimeSpan
                .fromDuration(duplicateDetectionHistoryDuration)
                .toString());
        return this;
    }

    @Override
    public TopicImpl withDuplicateMessageDetectionHistoryDuration(Duration duration) {
        this.innerModel().withDuplicateDetectionHistoryTimeWindow(TimeSpan
                .fromDuration(duration)
                .toString());
        // Below shortcut cannot be used as 'withRequiresDuplicateDetection' cannot be changed
        // once the topic is created.
        // return withDuplicateMessageDetection(duration);
        return this;
    }

    @Override
    public TopicImpl withoutDuplicateMessageDetection() {
        this.innerModel().withRequiresDuplicateDetection(false);
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
        return this.manager().serviceClient().getTopics()
                .getAsync(this.resourceGroupName(),
                        this.parentName,
                        this.name());
    }

    @Override
    protected Mono<Topic> createChildResourceAsync() {
        Mono<TopicResourceInner> createTask = this.manager().serviceClient().getTopics()
            .createOrUpdateAsync(this.resourceGroupName(),
                    this.parentName,
                    this.name(),
                    prepareForCreate(this.innerModel()))
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
