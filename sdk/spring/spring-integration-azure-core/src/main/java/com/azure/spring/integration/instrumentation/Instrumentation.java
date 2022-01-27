// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.instrumentation;

/**
 * Azure Instrumentation for Event Hubs or Service Bus processor.
 */
public interface Instrumentation {

    /**
     * Specifies the type of queue and topic currently in use.
     */
    enum Type {

        CONSUMER,

        PRODUCER

    }

    /**
     * Get the name of destination entity.
     *
     * @return name the name of destination entity
     */
    String getName();

    /**
     * Get type.
     *
     * @return type the type
     * @see Type
     */
    Type getType();

    /**
     * @return the exception.
     */
    Throwable getException();

    /**
     * Check whether is down.
     *
     * @return true if the status is down,false otherwise
     */
    boolean isDown();

    /**
     * Check whether is up.
     *
     * @return false if the status is up,true otherwise
     */
    boolean isUp();

    /**
     * Get the unique id.
     * @return the id string.
     */
    default String getId() {
        return buildId(getType(), getName());
    }

    /**
     * Mark the current instrumentation status down by exception.
     * @param exception the occurred exception.
     */
    default void markDown(Throwable exception) {

    }

    /**
     * Mark the current instrumentation status up.
     */
    default void markUp() {

    }

    /**
     * Build the unique id.
     * @param type the instrumentation type.
     * @param name the instrumentation name.
     * @return the unique strings.
     */
    static String buildId(Type type, String name) {
        return type + ":" + name;
    }
}
