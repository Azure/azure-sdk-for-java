// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.health;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ServiceBus health details entity class.
 */
public class Instrumentation {

    /**
     * Specifies the type of queue and topic currently in use.
     */
    public enum Type {

        /**
         * consume
         */
        CONSUME("consume"),

        /**
         * produce.
         */
        PRODUCE("produce");

        private String typeName;

        /**
         *
         * @param typeName The type name.
         */
        Type(String typeName) {
            this.typeName = typeName;
        }

        /**
         *
         * @return The type name.
         */
        public String getTypeName() {
            return typeName;
        }
    }

    private final String name;

    private final Type type;

    /**
     * Whether started.
     */
    protected final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * The start exception.
     */
    protected Exception startException = null;

    /**
     *
     * @param name The name.
     * @param type The type.
     */
    public Instrumentation(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    /**
     *
     * @return The type.
     */
    public Type getType() {
        return type;
    }

    /**
     *
     * @return Is down.
     */
    public boolean isDown() {
        return startException != null;
    }

    /**
     *
     * @return Is up.
     */
    public boolean isUp() {
        return started.get();
    }

    /**
     *
     * @return Is out of service.
     */
    public boolean isOutOfService() {
        return !started.get() && startException == null;
    }

    /**
     * Mark started successfully.
     */
    public void markStartedSuccessfully() {
        started.set(true);
    }

    /**
     *
     * @param e The exception.
     */
    public void markStartFailed(Exception e) {
        started.set(false);
        startException = e;
    }

    /**
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return true if started.
     */
    public boolean isStarted() {
        return started.get();
    }

    /**
     *
     * @return The startException.
     */
    public Exception getStartException() {
        return startException;
    }

}
