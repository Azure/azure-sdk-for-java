// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static io.clientcore.core.implementation.utils.ImplUtils.isNullOrEmpty;

/**
 * Encodes and decodes using Base64 URI encoding.
 */
public final class Base64Uri implements JsonSerializable<Base64Uri> {

    private static final ClientLogger LOGGER = new ClientLogger(Base64Uri.class);

    /**
     * The Base64Uri encoded bytes.
     */
    private final byte[] bytes;

    /**
     * Creates a new Base64Uri object with the specified encoded string.
     *
     * @param string The encoded string.
     */
    public Base64Uri(String string) {
        if (string == null) {
            this.bytes = null;
        } else {
            string = unquote(string);
            this.bytes = string.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Creates a new Base64Uri object with the specified encoded bytes.
     *
     * @param bytes The encoded bytes.
     */
    public Base64Uri(byte[] bytes) {
        this.bytes = unquote(bytes);
    }

    private static byte[] unquote(byte[] bytes) {
        if (bytes != null && bytes.length > 1) {
            byte firstByte = bytes[0];
            if (firstByte == '\"' || firstByte == '\'') {
                byte lastByte = bytes[bytes.length - 1];
                if (lastByte == firstByte) {
                    return Arrays.copyOfRange(bytes, 1, bytes.length - 1);
                }
            }
        }
        return bytes;
    }

    private static String unquote(String string) {
        if (!isNullOrEmpty(string)) {
            final char firstCharacter = string.charAt(0);
            if (firstCharacter == '\"' || firstCharacter == '\'') {
                final int base64UriStringLength = string.length();
                final char lastCharacter = string.charAt(base64UriStringLength - 1);
                if (lastCharacter == firstCharacter) {
                    return string.substring(1, base64UriStringLength - 1);
                }
            }
        }
        return string;
    }

    /**
     * Encodes a byte array into Base64Uri encoded bytes.
     *
     * @param bytes The byte array to encode.
     * @return A new Base64Uri instance.
     */
    public static Base64Uri encode(byte[] bytes) {
        if (bytes == null) {
            return new Base64Uri((String) null);
        } else {
            return new Base64Uri(Base64Util.encodeURIWithoutPadding(bytes));
        }
    }

    /**
     * Returns the underlying encoded byte array.
     *
     * @return The underlying encoded byte array.
     */
    public byte[] encodedBytes() {
        if (bytes == null) {
            return null;
        }

        byte[] copy = new byte[bytes.length];

        System.arraycopy(bytes, 0, copy, 0, bytes.length);

        return copy;
    }

    /**
     * Decode the bytes and returns its value.
     *
     * @return The decoded byte array.
     */
    public byte[] decodedBytes() {
        if (this.bytes == null) {
            return null;
        }

        return Base64Util.decodeURI(bytes);
    }

    @Override
    public String toString() {
        return bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Base64Uri)) {
            return false;
        }

        return Arrays.equals(this.bytes, ((Base64Uri) obj).encodedBytes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        if (bytes == null) {
            return jsonWriter.writeNull();
        } else {
            return jsonWriter.writeString(this.toString());
        }
    }

    /**
     * Reads an instance of Base64Uri from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of Base64Uri if the JsonReader was pointing to an instance of it.
     * @throws IOException If an error occurs while reading the Base64Uri.
     * @throws IllegalStateException If unexpected JSON token is found.
     */
    public static Base64Uri fromJson(JsonReader jsonReader) throws IOException {
        JsonToken nextToken = jsonReader.nextToken();
        if (nextToken == JsonToken.NULL) {
            return null;
        }
        if (nextToken != JsonToken.STRING) {
            throw LOGGER.logThrowableAsError(new IllegalStateException(
                String.format("Unexpected JSON token for Base64Uri deserialization: %s", nextToken)));
        }
        return new Base64Uri(jsonReader.getString());
    }
}
