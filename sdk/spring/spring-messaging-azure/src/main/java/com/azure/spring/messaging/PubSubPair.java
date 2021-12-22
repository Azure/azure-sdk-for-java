// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.messaging;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * The class to describe the publish/subscribe relationship.
 */
public class PubSubPair {
    private final String publisher;
    private final String subscriber;

    private PubSubPair(String publisher, String subscriber) {
        Assert.notNull(publisher, "First must not be null!");
        Assert.notNull(subscriber, "Second must not be null!");

        this.publisher = publisher;
        this.subscriber = subscriber;
    }

    /**
     * Creates a new {@link PubSubPair} for the given elements.
     *
     * @param publisher must not be {@literal null}.
     * @param subscriber must not be {@literal null}.
     * @return
     */
    public static PubSubPair of(String publisher, String subscriber) {
        return new PubSubPair(publisher, subscriber);
    }

    /**
     * Get the publisher.
     * @return the publisher.
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * Get the subscriber.
     * @return the subscriber.
     */
    public String getSubscriber() {
        return subscriber;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PubSubPair)) {
            return false;
        }

        PubSubPair pair = (PubSubPair) o;

        if (!ObjectUtils.nullSafeEquals(publisher, pair.publisher)) {
            return false;
        }

        return ObjectUtils.nullSafeEquals(subscriber, pair.subscriber);
    }

    @Override
    public int hashCode() {
        int result = ObjectUtils.nullSafeHashCode(publisher);
        result = 31 * result + ObjectUtils.nullSafeHashCode(subscriber);
        return result;
    }
}
