// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a caught throwable during a network call. It is used to serialize exceptions that were thrown
 * during the pipeline and deserialize them back into their actual throwable class when running in playback mode.
 */
public class NetworkCallError {
    @JsonProperty("ClassName")
    private Class<?> className;

    @JsonProperty("ArgTypes")
    private Class<?>[] argTypes;

    @JsonProperty("ArgValues")
    private Object[] argValues;

    private final ClientLogger logger = new ClientLogger(NetworkCallError.class);

    /**
     * Empty constructor used by deserialization.
     */
    public NetworkCallError() {
    }

    /**
     * Constructs the class setting the throwable and its class name.
     *
     * @param throwable Throwable thrown during a network call.
     * @throws RuntimeException when I/O error occurs.
     */
    public NetworkCallError(Throwable throwable) {
        try {
            this.className = throwable.getClass();
            this.argTypes = throwable.getClass().getConstructors()[0].getParameterTypes();
            int size = this.argTypes.length;
            this.argValues = new Object[size];
            for (int i = 0; i < size; i++) {
                this.argValues[i] = getDefaultValue(this.argTypes[i]);
            }
        } catch (Exception e) {
            logger.logExceptionAsError(new RuntimeException("Failed to serialize the exception class. Error details: "
                + e.getMessage()));
            return;
        }
    }

    /**
     * @return the thrown throwable as the class it was thrown as by converting is using its class name.
     * @throws Exception Cast the exception which recorded in json files to generic exception.
     * @throws RuntimeException when I/O error occurs.
     */
    public Throwable get() {
        try {
            if (argValues == null) {
                throw (Exception) className.getConstructor().newInstance();
            }
            throw (Exception) className.getConstructor(argTypes).newInstance(argValues);
        } catch (Exception e) {
            logger.logExceptionAsError(new RuntimeException("Failed to deserialize the exception class. Error details: "
                + e.getMessage()));
            return null;
        }
    }

    /**
     * Sets the name of the class of the throwable. This is used during deserialization the construct the throwable
     * as the actual class that was thrown.
     *
     * @param className Class name of the throwable.
     * @return The instance of NetworkCallError class.
     */
    public NetworkCallError className(Class<?> className) {
        this.className = className;
        return this;
    }

    /**
     * Gets the name of the class of the throwable. This is used during deserialization the construct the throwable
     * as the actual class that was thrown.
     *
     * @return The class name which is Class type.
     */
    public Class<?> className() {
        return this.className;
    }

    /**
     * Sets the throwable argument types which needed for new instance constructor. This is used during deserialization the construct the throwable
     * as the actual class that was thrown.
     *
     * @param argTypes The array of argument types.
     * @return The instance of NetworkCallError class.
     */
    public NetworkCallError argTypes(Class<?>[] argTypes) {
        this.argTypes = ImplUtils.clone(argTypes);
        return this;
    }

    /**
     * Gets the throwable argument types which needed for new instance constructor. This is used during deserialization the construct the throwable
     * as the actual class that was thrown.
     *
     * @return The array of argument types.
     */
    public Class<?>[] argTypes() {
        return ImplUtils.clone(this.argTypes);
    }

    /**
     * Sets the throwable argument default values which needed for new instance constructor. This is used during deserialization the construct the throwable
     * as the actual class that was thrown.
     *
     * @param argValues The array of argument values.
     * @return The instance of NetworkCallError class.
     */
    public NetworkCallError argValues(Object[] argValues) {
        this.argValues = ImplUtils.clone(argValues);
        return this;
    }

    /**
     * Gets the throwable argument default values which needed for new instance constructor. This is used during deserialization the construct the throwable
     * as the actual class that was thrown.
     *
     * @return The array of argument values.
     */
    public Object[] argValues() {
        return ImplUtils.clone(this.argValues);
    }

    private Object getDefaultValue(Class<?> type) throws Exception {
        // Get default value of non-primitive type
        if (!type.isPrimitive()) {
            return type.getConstructor().newInstance();
        }
        // Get default value of primitive type
        if (type == Boolean.TYPE) {
            return Boolean.FALSE;
        } else if (type == Integer.TYPE) {
            return 0;
        } else if (type == Long.TYPE) {
            return 0L;
        } else if (type == Byte.TYPE) {
            return 0;
        } else if (type == Float.TYPE) {
            return 0.0F;
        } else if (type == Short.TYPE) {
            return Short.valueOf((short) 0);
        } else if (type == Double.TYPE) {
            return type == Double.TYPE;
        } else if (type == Character.TYPE) {
            return '\u0000';
        }
        return null;
    }
}
