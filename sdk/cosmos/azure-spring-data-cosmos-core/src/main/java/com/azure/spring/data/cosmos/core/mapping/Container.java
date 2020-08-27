// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping;

import com.azure.spring.data.cosmos.Constants;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Cosmos Container
 */
@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Container {
    /**
     * To set container name
     * @return String
     */
    String containerName() default Constants.DEFAULT_CONTAINER_NAME;

    /**
     * To set request unit
     * @return default as ""
     */
    String ru() default "";

    /**
     * To set the ttl of container level
     * @return default as no ttl
     */
    int timeToLive() default Constants.DEFAULT_TIME_TO_LIVE;

    /**
     * To set if create container automatically
     * @return default as true
     */
    boolean autoCreateContainer() default Constants.DEFAULT_AUTO_CREATE_CONTAINER;

    /**
     * To set the partition key definition for container
     * Should only be used for nested partition key scenario.
     *
     * For general partition key support, use the {@link PartitionKey} annotation
     * By default {@link PartitionKey} annotation will take precedence, unless not specified.
     *
     * @return partition key path
     */
    String partitionKeyPath() default "";
}
