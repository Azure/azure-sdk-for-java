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
 * Implementation for Topic.
 */
@LangDefinition
class TopicImpl extends IndependentChildResourceImpl<Topic, NamespaceImpl, TopicResourceInner, TopicImpl, ServiceBusManager>
        implements
        Topic,
        Topic.Definition,
        Topic.Update {
    TopicImpl(String resourceGroupName,
              String namespaceName,
              String name,
              TopicResourceInner inner,
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
    public Subscriptions subscriptions() {
        return new SubscriptionsImpl(this.resourceGroupName(),
                this.parentName,
                this.name(),
                manager());
    }

    @Override
    public TopicAuthorizationRules TopicAuthorizationRules() {
        return new TopicAuthorizationRulesImpl(this.resourceGroupName(),
                this.parentName,
                this.name(),
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
       return withDuplicateMessageDetection(duration);
    }

    @Override
    public TopicImpl withoutDuplicateMessageDetection() {
        this.inner().withRequiresDuplicateDetection(false);
        return this;
    }

    @Override
    public TopicImpl withNewAuthorizationRule(String name, AccessRights... rights) {
        return this;
    }

    @Override
    public TopicImpl withoutNewAuthorizationRule(String name) {
        return this;
    }

    @Override
    public TopicImpl withNewSubscription(String name, int maxSizeInMB) {
        return this;
    }

    @Override
    public TopicImpl withoutSubscription(String name) {
        return this;
    }

    @Override
    protected Observable<TopicResourceInner> getInnerAsync() {
        return this.manager().inner().topics()
                .getAsync(this.resourceGroupName(),
                        this.parentName,
                        this.name());
    }

    @Override
    protected Observable<Topic> createChildResourceAsync() {
        return null;
    }
}
