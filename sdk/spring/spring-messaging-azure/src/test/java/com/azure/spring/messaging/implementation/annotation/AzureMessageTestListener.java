// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.annotation;

import com.azure.spring.messaging.implementation.listener.MessageListenerContainerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MessageMapping
@Repeatable(value = AzureMessageTestListeners.class)
public @interface AzureMessageTestListener {

    /**
     * The unique identifier of the container managing this endpoint.
     * <p>If none is specified, an auto-generated one is provided.
     *
     * @return String
     */
    String id() default "";

    /**
     * The bean name of the {@link MessageListenerContainerFactory}
     * to use to create the message listener container responsible for serving this endpoint.
     * <p>If not specified, the default container factory is used, if any.
     * @return String
     */
    String containerFactory() default "";

    /**
     * The destination name for this listener, resolved through the container-wide
     * {@link org.springframework.messaging.core.DestinationResolver} strategy.
     * @return String
     */
    String destination();

    /**
     * The name for the durable group, if any.
     * @return String
     */
    String group() default "";

    /**
     * The concurrency limits for the listener, if any. Overrides the value defined
     * by the container factory used to create the listener container.
     * <p>The concurrency limits can be a "lower-upper" String &mdash; for example,
     * "5-10" &mdash; or a simple upper limit String &mdash; for example, "10", in
     * which case the lower limit will be 1.
     * <p>Note that the underlying container may or may not support all features.
     * For instance, it may not be able to scale, in which case only the upper limit
     * is used.
     * @return String
     */
    String concurrency() default "";

}
