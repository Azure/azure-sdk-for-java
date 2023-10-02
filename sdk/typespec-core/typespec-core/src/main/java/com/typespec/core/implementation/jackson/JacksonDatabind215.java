// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Utility methods for Jackson Databind types when it's known that the version is 2.15+.
 */
final class JacksonDatabind215 {
    private static final ClientLogger LOGGER = new ClientLogger(JacksonDatabind215.class);
    private static final String STREAM_READ_CONSTRAINTS = "com.fasterxml.jackson.core.StreamReadConstraints";
    private static final String STREAM_READ_CONSTRAINTS_BUILDER = STREAM_READ_CONSTRAINTS + "$Builder";

    private static final MethodHandle CREATE_STREAM_READ_CONSTRAINTS_BUILDER;
    private static final MethodHandle SET_MAX_STRING_LENGTH;
    private static final MethodHandle BUILD_STREAM_READ_CONSTRAINTS;
    private static final MethodHandle SET_STREAM_READ_CONSTRAINTS;

    private static final boolean USE_JACKSON_215;

    static {
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
        ClassLoader thisClassLoader = JacksonDatabind215.class.getClassLoader();

        MethodHandle createStreamReadConstraintsBuilder = null;
        MethodHandle setMaxStringLength = null;
        MethodHandle buildStreamReadConstraints = null;
        MethodHandle setStreamReadConstraints = null;
        boolean useJackson215 = false;
        try {
            Class<?> streamReadConstraints = Class.forName(STREAM_READ_CONSTRAINTS, true, thisClassLoader);
            Class<?> streamReadConstraintsBuilder = Class.forName(STREAM_READ_CONSTRAINTS_BUILDER, true,
                thisClassLoader);

            createStreamReadConstraintsBuilder = publicLookup.unreflect(streamReadConstraints
                .getDeclaredMethod("builder"));
            setMaxStringLength = publicLookup.unreflect(streamReadConstraintsBuilder
                .getDeclaredMethod("maxStringLength", int.class));
            buildStreamReadConstraints = publicLookup.unreflect(streamReadConstraintsBuilder
                .getDeclaredMethod("build"));
            setStreamReadConstraints = publicLookup.unreflect(JsonFactory.class.getDeclaredMethod(
                "setStreamReadConstraints", streamReadConstraints));
            useJackson215 = true;
        } catch (Throwable ex) {
            if (ex instanceof LinkageError) {
                LOGGER.info("Attempted to create MethodHandles for Jackson 2.15 features but failed. It's possible "
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
            Object streamReadConstraintsBuilder = CREATE_STREAM_READ_CONSTRAINTS_BUILDER.invoke();

            SET_MAX_STRING_LENGTH.invoke(streamReadConstraintsBuilder, 50 * 1024 * 1024);
            SET_STREAM_READ_CONSTRAINTS.invoke(objectMapper.tokenStreamFactory(),
                BUILD_STREAM_READ_CONSTRAINTS.invoke(streamReadConstraintsBuilder));

            return objectMapper;
        } catch (Throwable throwable) {
            if (throwable instanceof Error) {
                throw (Error) throwable;
            } else {
                throw LOGGER.logExceptionAsError(new IllegalStateException(throwable));
            }
        }
    }

    private JacksonDatabind215() {
    }
}
