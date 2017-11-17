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
import com.microsoft.azure.management.servicebus.ServiceBusSubscription;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.management.servicebus.TopicAuthorizationRule;
import org.joda.time.DateTime;
import org.joda.time.Period;
import rx.Completable;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for Topic.
 */
@LangDefinition
class TopicImpl extends IndependentChildResourceImpl<Topic, ServiceBusNamespaceImpl, TopicInner, TopicImpl, ServiceBusManager>
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
              TopicInner inner,
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
    public Period defaultMessageTtlDuration() {
        if (this.inner().defaultMessageTimeToLive() == null) {
            return null;
        }
        TimeSpan timeSpan = TimeSpan.parse(this.inner().defaultMessageTimeToLive());
        return new Period()
                .withDays(timeSpan.days())
                .withHours(timeSpan.hours())
                .withMinutes(timeSpan.minutes())
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
                .withMinutes(timeSpan.minutes())
                .withSeconds(timeSpan.seconds())
                .withMillis(timeSpan.milliseconds());
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
    public TopicImpl withDefaultMessageTTL(Period ttl) {
        this.inner().withDefaultMessageTimeToLive(TimeSpan.fromPeriod(ttl).toString());
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
    public TopicImpl withDuplicateMessageDetection(Period duplicateDetectionHistoryDuration) {
        this.inner().withRequiresDuplicateDetection(true);
        this.inner().withDuplicateDetectionHistoryTimeWindow(TimeSpan
                .fromPeriod(duplicateDetectionHistoryDuration)
                .toString());
        return this;
    }

    @Override
    public TopicImpl withDuplicateMessageDetectionHistoryDuration(Period duration) {
        this.inner().withDuplicateDetectionHistoryTimeWindow(TimeSpan
                .fromPeriod(duration)
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
    protected Observable<TopicInner> getInnerAsync() {
        return this.manager().inner().topics()
                .getAsync(this.resourceGroupName(),
                        this.parentName,
                        this.name());
    }

    @Override
    protected Observable<Topic> createChildResourceAsync() {
        Completable createTopicCompletable = this.manager().inner().topics()
                .createOrUpdateAsync(this.resourceGroupName(),
                        this.parentName,
                        this.name(),
                        this.inner())
                .map(new Func1<TopicInner, TopicInner>() {
                    @Override
                    public TopicInner call(TopicInner inner) {
                        setInner(inner);
                        return inner;
                    }
                }).toCompletable();
        Completable childrenOperationsCompletable = submitChildrenOperationsAsync();
        final Topic self = this;
        return Completable.concat(createTopicCompletable, childrenOperationsCompletable)
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        initChildrenOperationsCache();
                    }
                })
                .andThen(Observable.just(self));
    }

    private void initChildrenOperationsCache() {
        this.subscriptionsToCreate = new ArrayList<>();
        this.rulesToCreate = new ArrayList<>();
        this.subscriptionsToDelete = new ArrayList<>();
        this.rulesToDelete = new ArrayList<>();
    }

    private Completable submitChildrenOperationsAsync() {
        Observable<?> subscriptionsCreateStream = Observable.empty();
        if (this.subscriptionsToCreate.size() > 0) {
            subscriptionsCreateStream = this.subscriptions().createAsync(this.subscriptionsToCreate);
        }
        Observable<?> rulesCreateStream = Observable.empty();
        if (this.rulesToCreate.size() > 0) {
            rulesCreateStream = this.authorizationRules().createAsync(this.rulesToCreate);
        }
        Observable<?> subscriptionsDeleteStream = Observable.empty();
        if (this.subscriptionsToDelete.size() > 0) {
            subscriptionsDeleteStream = this.subscriptions().deleteByNameAsync(this.subscriptionsToDelete);
        }
        Observable<?> rulesDeleteStream = Observable.empty();
        if (this.rulesToDelete.size() > 0) {
            rulesDeleteStream = this.authorizationRules().deleteByNameAsync(this.rulesToDelete);
        }
        return Completable.mergeDelayError(subscriptionsCreateStream.toCompletable(),
                rulesCreateStream.toCompletable(),
                subscriptionsDeleteStream.toCompletable(),
                rulesDeleteStream.toCompletable());
    }
}