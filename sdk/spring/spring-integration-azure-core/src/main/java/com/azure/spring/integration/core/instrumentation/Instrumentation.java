// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.instrumentation;

/**
 * Azure Instrumentation for Event Hubs or Service Bus processor.
 */
public interface Instrumentation {

    /**
     * Specifies the type of queue and topic currently in use.
     */
    enum Type {
        /**
         * Consumer
         */
        CONSUMER,

        /**
         * Producer
         */
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
     * Return the exception.
     * @return the exception.
     */
    Throwable getException();

    /**
     * Get the status of the instrumented component.
     * @return the status.
     */
    Status getStatus();

    /**
     * Set the status of the component.
     * @param status the status to set.
     */
    void setStatus(Status status);

    /**
     * Set the status of the component.
     * @param status the status to set.
     * @param exception the exception thrown by the component.
     */
    void setStatus(Status status, Throwable exception);
    /**
     * Get the unique id.
     * @return the id string.
     */
    default String getId() {
        return buildId(getType(), getName());
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

    /**
     * The status of the instrumented component.
     */
    enum Status {
        /**
         * Up
         */
        UP,

        /**
         * Down
         */
        DOWN
    }
}
