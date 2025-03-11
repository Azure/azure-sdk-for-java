// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.serialization;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class is a composite serializer that manages multiple {@link ObjectSerializer} instances and will indicate
 * whether a certain format is supported by any of the serializers and will provide that serializer to serialize or
 * deserialize the object.
 */
public final class CompositeSerializer {
    private static final ClientLogger LOGGER = new ClientLogger(CompositeSerializer.class);

    private final List<ObjectSerializer> serializers;

    /**
     * Creates an instance of the {@link CompositeSerializer} with the provided serializers.
     * <p>
     * If multiple {@link ObjectSerializer} instances support the same format
     * ({@link ObjectSerializer#supportsFormat(SerializationFormat)}) then the first one in the array will be used when
     * requested.
     * <p>
     * If {@code serializers} is null or empty, an {@link IllegalArgumentException} will be thrown.
     *
     * @param serializers The {@link ObjectSerializer} instances to be used.
     * @return A new {@link CompositeSerializer} instance with the provided serializers.
     * @throws IllegalArgumentException Ff {@code serializers} is null or empty.
     */
    public static CompositeSerializer create(ObjectSerializer... serializers) {
        if (CoreUtils.isNullOrEmpty(serializers)) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("serializers cannot be null or empty"));
        }

        return new CompositeSerializer(serializers);
    }

    private CompositeSerializer(ObjectSerializer[] serializers) {
        this.serializers = Collections.unmodifiableList(Arrays.asList(CoreUtils.arrayCopy(serializers)));
    }

    /**
     * Gets the {@link ObjectSerializer} for the provided format.
     * <p>
     * If no serializer supports the provided format, an {@link IllegalArgumentException} will be thrown.
     *
     * @param format The {@link SerializationFormat} to be used.
     * @return The {@link ObjectSerializer} that supports the provided format.
     * @throws IllegalArgumentException If no serializer supports the provided format.
     */
    public ObjectSerializer getSerializerForFormat(SerializationFormat format) {
        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                return serializer;
            }
        }

        throw LOGGER.logThrowableAsError(new IllegalArgumentException("No serializer found for format: " + format));
    }

    /**
     * Gets a read-only list of the {@link ObjectSerializer} instances that are used by this
     * {@link CompositeSerializer}.
     *
     * @return The read-only list of {@link ObjectSerializer} instances.
     */
    public List<ObjectSerializer> getSerializers() {
        return serializers;
    }
}
