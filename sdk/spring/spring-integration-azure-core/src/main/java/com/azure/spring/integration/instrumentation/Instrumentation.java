// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.instrumentation;

/**
 *
 */
public interface Instrumentation {

    /**
     * Specifies the type of queue and topic currently in use.
     */
    enum Type {

        CONSUMER,

        PRODUCER

    }

    String getName();

    Type getType();

    Throwable getException();

    boolean isDown();

    boolean isUp();

    default String getId() {
        return buildId(getType(), getName());
    }

    default void markDown(Throwable exception) {

    }

    default void markUp() {

    }

    static String buildId(Type type, String name) {
        return type + ":" + name;
    }
}
