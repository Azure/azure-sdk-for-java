// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A {@link ParameterizedType} implementation.
 */
public final class ParameterizedTypeImpl implements ParameterizedType {
    private final Class<?> raw;
    private final Type[] args;
    private String cachedToString;

    /**
     * Creates a new instance of {@link ParameterizedTypeImpl}.
     *
     * @param raw The raw type.
     * @param args The type arguments.
     */
    public ParameterizedTypeImpl(Class<?> raw, Type... args) {
        this.raw = raw;

        Type[] argsCopy = new Type[args.length];
        for (int i = 0; i < args.length; i++) {
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
            cachedToString = Arrays.stream(args)
                .map(type -> ((ParameterizedTypeImpl) type).getTypeName())
                .collect(Collectors.joining(", "));
        }
        return raw.getTypeName() + "<" + cachedToString + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParameterizedTypeImpl that = (ParameterizedTypeImpl) o;
        return Objects.equals(raw, that.raw) && Objects.deepEquals(args, that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw, Arrays.hashCode(args));
    }
}
