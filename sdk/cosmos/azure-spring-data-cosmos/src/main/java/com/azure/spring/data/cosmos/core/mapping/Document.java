// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping;

import com.azure.spring.data.cosmos.Constants;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Annotation of cosmos document
 */
@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Document {
    /**
     * To set collection name
     * @return String
     */
    String collection() default Constants.DEFAULT_COLLECTION_NAME;

    /**
     * To set request unit
     * @return default as 4000
     */
    String ru() default Constants.DEFAULT_REQUEST_UNIT;

    /**
     * To set the ttl of container level
     * @return default as no ttl
     */
    int timeToLive() default Constants.DEFAULT_TIME_TO_LIVE;

    /**
     * To set if create collection automatically
     * @return default as true
     */
    boolean autoCreateCollection() default Constants.DEFAULT_AUTO_CREATE_CONTAINER;
}
