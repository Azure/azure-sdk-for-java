// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.servicebus.core.properties;

/**
 * An interface to provide properties by providing key.
 */
public interface SubscriptionPropertiesSupplier<V> {
    default V getQueueSubscription(String name) {
        return null;
    }

    default V getTopicSubscription(String name, String subscription) {
        return null;
    }
    
}
