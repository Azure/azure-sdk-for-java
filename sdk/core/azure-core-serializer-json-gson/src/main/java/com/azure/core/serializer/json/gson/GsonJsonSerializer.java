// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.serializer.JsonSerializer;
import com.azure.core.util.CoreUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * GSON based implementation of the {@link JsonSerializer} interface.
 */
public final class GsonJsonSerializer implements JsonSerializer {
    private final Gson gson;

    /**
     * Constructs a {@link JsonSerializer} using the passed {@link Gson} serializer.
     *
     * @param gson Configured {@link Gson} serializer.
     */
    GsonJsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> T deserialize(byte[] input, Class<T> clazz) {
        return gson.fromJson(CoreUtils.bomAwareToString(input, null), clazz);
    }

    @Override
    public byte[] serialize(Object value) {
        return gson.toJson(value).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void serialize(Object value, OutputStream stream) {
        Objects.requireNonNull(stream, "'stream' cannot be null.");

        gson.toJson(value, new StreamWrappingAppendable(stream));
    }

    private static final class StreamWrappingAppendable implements Appendable {
        private final OutputStream stream;

        private StreamWrappingAppendable(OutputStream stream) {
            this.stream = stream;
        }

        @Override
        public Appendable append(CharSequence csq) throws IOException {
            stream.write(csq.toString().getBytes(StandardCharsets.UTF_8));
            return this;
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) throws IOException {
            return append(csq.subSequence(start, end));
        }

        @Override
        public Appendable append(char c) throws IOException {
            stream.write(String.valueOf(c).getBytes(StandardCharsets.UTF_8));
            return this;
        }
    }
}
