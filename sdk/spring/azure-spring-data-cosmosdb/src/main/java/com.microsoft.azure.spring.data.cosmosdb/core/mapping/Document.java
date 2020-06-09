// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.core.mapping;

import com.microsoft.azure.spring.data.cosmosdb.Constants;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Document {

    String collection() default Constants.DEFAULT_COLLECTION_NAME;

    String ru() default Constants.DEFAULT_REQUEST_UNIT;

    int timeToLive() default Constants.DEFAULT_TIME_TO_LIVE;

    boolean autoCreateCollection() default Constants.DEFAULT_AUTO_CREATE_CONTAINER;
}
