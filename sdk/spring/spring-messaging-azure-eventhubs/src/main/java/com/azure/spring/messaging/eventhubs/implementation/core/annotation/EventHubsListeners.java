// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation that aggregates several {@link EventHubsListener} annotations.
 *
 * <p>Can be used natively, declaring several nested {@link EventHubsListener} annotations.
 * Can also be used in conjunction with Java 8's support for repeatable annotations,
 * where {@link EventHubsListener} can simply be declared several times on the same method,
 * implicitly generating this container annotation.
 *
 * <p>This annotation may be used as a <em>meta-annotation</em> to create custom
 * <em>composed annotations</em>.
 *
 * @see EventHubsListener
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventHubsListeners {

    /**
     * Aggregates {@link EventHubsListener} annotations.
     * @return the AzureMessageListener annotations.
     */
    EventHubsListener[] value();

}
