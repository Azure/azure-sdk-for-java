// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.annotation;

import com.azure.spring.messaging.config.AzureListenerAnnotationBeanPostProcessor;
import com.azure.spring.messaging.container.ListenerContainerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;

/**
 * Annotation that marks a method to be the target of a Azure message listener on the
 * specified {@link #destination}. The {@link #containerFactory} identifies the
 * {@link ListenerContainerFactory} to use to build
 * the Azure listener container. If not set, a <em>default</em> container factory is
 * assumed to be available with a bean name of {@code azureListenerContainerFactory}
 * unless an explicit default has been provided through configuration.
 *
 * <p>Processing of {@code @AzureMessageListener} annotations is performed by registering a
 * {@link AzureListenerAnnotationBeanPostProcessor}. This can be done through the
 * {@link EnableAzureMessaging @EnableAzureMessaging} annotation.
 *
 * <p>Annotated Azure listener methods are allowed to have flexible signatures similar
 * to what {@link MessageMapping} provides:
 * <ul>
 * <li>{@link org.springframework.messaging.Message} to use Spring's messaging abstraction counterpart</li>
 * <li>{@link org.springframework.messaging.handler.annotation.Payload @Payload}-annotated method
 * arguments, including support for validation</li>
 * <li>{@link org.springframework.messaging.handler.annotation.Header @Header}-annotated method
 * arguments to extract specific header values, including standard Azure headers defined by
 * {@link String }</li>
 * <li>{@link org.springframework.messaging.handler.annotation.Headers @Headers}-annotated
 * method argument that must also be assignable to {@link java.util.Map} for obtaining
 * access to all headers</li>
 * <li>{@link org.springframework.messaging.MessageHeaders} arguments for obtaining
 * access to all headers</li>
 * <li>{@link org.springframework.messaging.support.MessageHeaderAccessor} or
 * access to all method arguments</li>
 * </ul>
 *
 * <p>This annotation may be used as a <em>meta-annotation</em> to create custom
 * <em>composed annotations</em> with attribute overrides.
 *
 * @author Warren Zhu
 * @see EnableAzureMessaging
 * @see AzureListenerAnnotationBeanPostProcessor
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MessageMapping
@Repeatable(AzureMessageListeners.class)
public @interface AzureMessageListener {

    /**
     * The unique identifier of the container managing this endpoint.
     * <p>If none is specified, an auto-generated one is provided.
     *
     * @return String
     */
    String id() default "";

    /**
     * The bean name of the {@link ListenerContainerFactory}
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
