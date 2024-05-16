// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.azure.core.implementation.ReflectiveInvoker;
import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility methods for Jackson Databind types when it's known that the version is 2.15+.
 */
final class JacksonDatabind215 {
    private static final ClientLogger LOGGER = new ClientLogger(JacksonDatabind215.class);
    private static final String STREAM_READ_CONSTRAINTS = "com.fasterxml.jackson.core.StreamReadConstraints";
    private static final String STREAM_READ_CONSTRAINTS_BUILDER = STREAM_READ_CONSTRAINTS + "$Builder";

    private static final ReflectiveInvoker CREATE_STREAM_READ_CONSTRAINTS_BUILDER;
    private static final ReflectiveInvoker SET_MAX_STRING_LENGTH;
    private static final ReflectiveInvoker BUILD_STREAM_READ_CONSTRAINTS;
    private static final ReflectiveInvoker SET_STREAM_READ_CONSTRAINTS;

    private static final boolean USE_JACKSON_215;

    static {
        ClassLoader thisClassLoader = JacksonDatabind215.class.getClassLoader();

        ReflectiveInvoker createStreamReadConstraintsBuilder = null;
        ReflectiveInvoker setMaxStringLength = null;
        ReflectiveInvoker buildStreamReadConstraints = null;
        ReflectiveInvoker setStreamReadConstraints = null;
        boolean useJackson215 = false;
        try {
            Class<?> streamReadConstraints = Class.forName(STREAM_READ_CONSTRAINTS, true, thisClassLoader);
            Class<?> streamReadConstraintsBuilder
                = Class.forName(STREAM_READ_CONSTRAINTS_BUILDER, true, thisClassLoader);

            createStreamReadConstraintsBuilder = ReflectionUtils.getMethodInvoker(streamReadConstraints,
                streamReadConstraints.getDeclaredMethod("builder"), false);
            setMaxStringLength = ReflectionUtils.getMethodInvoker(streamReadConstraintsBuilder,
                streamReadConstraintsBuilder.getDeclaredMethod("maxStringLength", int.class), false);
            buildStreamReadConstraints = ReflectionUtils.getMethodInvoker(streamReadConstraintsBuilder,
                streamReadConstraintsBuilder.getDeclaredMethod("build"), false);
            setStreamReadConstraints = ReflectionUtils.getMethodInvoker(JsonFactory.class,
                JsonFactory.class.getDeclaredMethod("setStreamReadConstraints", streamReadConstraints), false);
            useJackson215 = true;
        } catch (Throwable ex) {
            if (ex instanceof LinkageError) {
                LOGGER.info("Attempted to create invokers for Jackson 2.15 features but failed. It's possible "
                    + "that your application will run without error even with this failure. The Azure SDKs only set "
                    + "updated StreamReadConstraints to allow for larger payloads to be handled.");
            } else if (ex instanceof Error) {
                throw (Error) ex;
            } else {
                throw LOGGER.logExceptionAsError(new IllegalStateException(ex));
            }
        }

        CREATE_STREAM_READ_CONSTRAINTS_BUILDER = createStreamReadConstraintsBuilder;
        SET_MAX_STRING_LENGTH = setMaxStringLength;
        BUILD_STREAM_READ_CONSTRAINTS = buildStreamReadConstraints;
        SET_STREAM_READ_CONSTRAINTS = setStreamReadConstraints;
        USE_JACKSON_215 = useJackson215;
    }

    /**
     * Updates the StreamReadConstraints to allow for larger Strings to be read.
     *
     * @param objectMapper The ObjectMapper being mutated.
     * @return The updated ObjectMapper.
     */
    static ObjectMapper mutateStreamReadConstraints(ObjectMapper objectMapper) {
        if (!USE_JACKSON_215) {
            return objectMapper;
        }

        try {
            Object streamReadConstraintsBuilder = CREATE_STREAM_READ_CONSTRAINTS_BUILDER.invokeStatic();

            SET_MAX_STRING_LENGTH.invokeWithArguments(streamReadConstraintsBuilder, 50 * 1024 * 1024);
            SET_STREAM_READ_CONSTRAINTS.invokeWithArguments(objectMapper.tokenStreamFactory(),
                BUILD_STREAM_READ_CONSTRAINTS.invokeWithArguments(streamReadConstraintsBuilder));

            return objectMapper;
        } catch (Exception exception) {
            if (exception instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) exception);
            } else {
                throw LOGGER.logExceptionAsError(new IllegalStateException(exception));
            }
        }
    }

    private JacksonDatabind215() {
    }
}
