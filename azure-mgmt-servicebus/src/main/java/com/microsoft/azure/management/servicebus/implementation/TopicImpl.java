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
 * Implementation for Topic.
 */
class TopicImpl extends IndependentChildResourceImpl<Topic, NamespaceImpl, TopicResourceInner, TopicImpl, ServiceBusManager>
        implements
        Topic,
        Topic.Definition,
        Topic.Update {
    TopicImpl(String name, TopicResourceInner innerObject, ServiceBusManager manager) {
        super(name, innerObject, manager);
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
    public int subscriptionCount() {
        return 0;
    }

    @Override
    public EntityStatus status() {
        return null;
    }

    @Override
    public Subscriptions subscriptions() {
        return null;
    }

    @Override
    public TopicAuthorizationRules TopicAuthorizationRules() {
        return null;
    }

    @Override
    public TopicImpl withSizeInMB(int sizeInMB) {
        return this;
    }

    @Override
    public TopicImpl withPartitioning() {
        return this;
    }

    @Override
    public TopicImpl withoutPartitioning() {
        return this;
    }

    @Override
    public TopicImpl withDeleteOnIdleDurationInMinutes(int durationInMinutes) {
        return this;
    }

    @Override
    public TopicImpl withDefaultMessageTTL(Period ttl) {
        return this;
    }

    @Override
    public TopicImpl withExpressMessage() {
        return this;
    }

    @Override
    public TopicImpl withoutExpressMessage() {
        return this;
    }

    @Override
    public TopicImpl withMessageBatching() {
        return this;
    }

    @Override
    public TopicImpl withoutMessageBatching() {
        return this;
    }

    @Override
    public TopicImpl withDuplicateMessageDetection(Period duplicateDetectionHistoryDuration) {
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
    public TopicImpl withDuplicateMessageDetectionHistoryDuration(Period duration) {
        return this;
    }

    @Override
    public TopicImpl withoutDuplicateMessageDetection() {
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
        return null;
    }

    @Override
    protected Observable<Topic> createChildResourceAsync() {
        return null;
    }
}
