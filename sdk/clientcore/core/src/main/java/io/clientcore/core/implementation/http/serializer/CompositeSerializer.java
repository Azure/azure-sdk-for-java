// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.http.serializer;

import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.serializer.ObjectSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * An implementation of {@link ObjectSerializer} which comprises multiple serializers.
 * <p>
 * This class is meant to aid situations where the serialization formats being used are unknown ahead of time, allowing
 * the consumer of {@link ObjectSerializer} to provide a list of serializers to try in order until one is successful,
 * or the list is exhausted.
 */
public final class CompositeSerializer extends ObjectSerializer {
    private static final ClientLogger LOGGER = new ClientLogger(CompositeSerializer.class);

    private final List<ObjectSerializer> serializers;
    private final EnumSet<ObjectSerializer.Format> supportedFormats;

    /**
     * Creates an instance of the {@link CompositeSerializer}.
     *
     * @param serializers The list of serializers to try in order.
     */
    public CompositeSerializer(List<ObjectSerializer> serializers) {
        Objects.requireNonNull(serializers, "The list of serializers cannot be null.");
        if (serializers.isEmpty()) {
            throw new IllegalArgumentException("The list of serializers cannot be empty.");
        }

        this.serializers = new ArrayList<>(serializers);
        this.supportedFormats = EnumSet.allOf(Format.class);

        // Loop over all serializers and remove unsupported formats.
        formatters_loop: for (Format format : supportedFormats) {
            for (ObjectSerializer serializer : serializers) {
                if (serializer.supportsFormat(format)) {
                    break formatters_loop;
                }
            }

            // Will only be reached if none of the serializers support the format.
            supportedFormats.remove(format);
        }
    }

    @Override
    public <T> T deserializeFromBytes(byte[] data, Type type, ObjectSerializer.Format format) throws IOException {
        verifyFormat(format);

        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                return serializer.deserializeFromBytes(data, type, format);
            }
        }

        // Should never be reached.
        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("None of the provided serializers support the format: " + format + "."));
    }

    @Override
    public <T> T deserializeFromStream(InputStream stream, Type type, ObjectSerializer.Format format)
        throws IOException {
        verifyFormat(format);

        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                return serializer.deserializeFromStream(stream, type, format);
            }
        }

        // Should never be reached.
        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("None of the provided serializers support the format: " + format + "."));
    }

    @Override
    public byte[] serializeToBytes(Object value, ObjectSerializer.Format format) throws IOException {
        verifyFormat(format);

        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                return serializer.serializeToBytes(value, format);
            }
        }

        // Should never be reached.
        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("None of the provided serializers support the format: " + format + "."));
    }

    @Override
    public void serializeToStream(OutputStream stream, Object value, ObjectSerializer.Format format)
        throws IOException {
        verifyFormat(format);

        for (ObjectSerializer serializer : serializers) {
            if (serializer.supportsFormat(format)) {
                serializer.serializeToStream(stream, value, format);
                return;
            }
        }
    }

    @Override
    public boolean supportsFormat(ObjectSerializer.Format format) {
        return supportedFormats.contains(format);
    }

    private void verifyFormat(ObjectSerializer.Format format) {
        if (!supportsFormat(format)) {
            throw LOGGER.logThrowableAsError(
                new UnsupportedOperationException("The provided format (" + format + ") is not supported."));
        }
    }
}
