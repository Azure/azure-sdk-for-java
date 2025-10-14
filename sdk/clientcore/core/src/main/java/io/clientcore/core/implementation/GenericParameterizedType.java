// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation;

import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A {@link ParameterizedType} implementation that allows for reference type arguments.
 */
public final class GenericParameterizedType implements ParameterizedType {
    private static final ClientLogger LOGGER = new ClientLogger(GenericParameterizedType.class);

    private final Class<?> raw;
    private final Type[] args;
    private String cachedToString;

    /**
     * Creates a new instance of {@link GenericParameterizedType}.
     *
     * @param raw The raw type.
     * @param args The type arguments.
     */
    public GenericParameterizedType(Class<?> raw, Type... args) {
        this.raw = raw;

        if (args == null) {
            throw LOGGER.throwableAtError().log("args cannot be null", IllegalArgumentException::new);
        }

        Type[] argsCopy = new Type[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                throw LOGGER.throwableAtError()
                    .addKeyValue("index", i)
                    .log("args cannot contain null", IllegalArgumentException::new);
            }
            argsCopy[i] = args[i];
        }
        this.args = argsCopy;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return args;
    }

    @Override
    public Type getRawType() {
        return raw;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public String toString() {
        if (cachedToString == null) {
            cachedToString = raw.getTypeName() + "<"
                + Arrays.stream(args).map(Type::getTypeName).collect(Collectors.joining(", ")) + ">";
        }
        return cachedToString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GenericParameterizedType that = (GenericParameterizedType) o;
        return Objects.equals(raw, that.raw) && Objects.deepEquals(args, that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw, Arrays.hashCode(args));
    }
}
