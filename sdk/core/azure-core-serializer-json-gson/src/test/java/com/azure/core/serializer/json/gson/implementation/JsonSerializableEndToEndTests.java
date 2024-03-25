// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson.implementation;

import com.azure.core.serializer.json.gson.GsonJsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that the GSON {@link TypeAdapterFactory} is able to handle deserializing and serializing
 * {@link JsonSerializable} within a {@link Gson} context.
 */
public class JsonSerializableEndToEndTests {
    private static final Gson GSON
        = new GsonBuilder().registerTypeAdapterFactory(GsonJsonProvider.getJsonSerializableTypeAdapterFactory())
            .create();

    @Test
    public void serialization() {
        JsonSerializableWrapper wrapper
            = new JsonSerializableWrapper().setGeneralProperties(new GeneralProperties(42, true, "hello world", -0.0D));
        String expected
            = "{\"jsonserializable\":{\"anInt\":42,\"aBoolean\":true,\"aString\":\"hello world\",\"aNullableDecimal\":-0.0}}";

        String actual = GSON.toJson(wrapper);
        assertEquals(expected, actual);
    }

    @Test
    public void deserialization() {
        String json
            = "{\"jsonserializable\":{\"anInt\":42,\"aBoolean\":true,\"aString\":\"hello world\",\"aNullableDecimal\":-0.0}}";
        JsonSerializableWrapper expected
            = new JsonSerializableWrapper().setGeneralProperties(new GeneralProperties(42, true, "hello world", -0.0D));

        JsonSerializableWrapper actual = GSON.fromJson(json, JsonSerializableWrapper.class);
        assertEquals(expected, actual);
    }

    /**
     * Class that wraps a {@link JsonSerializable} type.
     */
    public static final class JsonSerializableWrapper {
        @Expose
        @SerializedName("jsonserializable")
        private GeneralProperties generalProperties;

        public JsonSerializableWrapper setGeneralProperties(GeneralProperties generalProperties) {
            this.generalProperties = generalProperties;
            return this;
        }

        public GeneralProperties getGeneralProperties() {
            return generalProperties;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(generalProperties);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof JsonSerializableWrapper)) {
                return false;
            }

            return Objects.equals(generalProperties, ((JsonSerializableWrapper) obj).generalProperties);
        }
    }

    /**
     * Class that implements {@link JsonSerializable}.
     */
    public static final class GeneralProperties implements JsonSerializable<GeneralProperties> {
        private final int anInt;
        private final boolean aBoolean;
        private final String aString;
        private final Double aNullableDecimal;

        public GeneralProperties(int anInt, boolean aBoolean, String aString, Double aNullableDecimal) {
            this.anInt = anInt;
            this.aBoolean = aBoolean;
            this.aString = aString;
            this.aNullableDecimal = aNullableDecimal;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeIntField("anInt", anInt)
                .writeBooleanField("aBoolean", aBoolean)
                .writeStringField("aString", aString)
                .writeNumberField("aNullableDecimal", aNullableDecimal)
                .writeEndObject();
        }

        /**
         * Deserializes an instance of {@link GeneralProperties} from the {@link JsonReader}.
         *
         * @param jsonReader The {@link JsonReader} being read.
         * @return An instance of {@link GeneralProperties}, or null if the {@link JsonReader} was pointing to JSON
         * null.
         * @throws IOException If an error occurs during deserialization.
         */
        public static GeneralProperties fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                int anInt = 0;
                boolean aBoolean = false;
                String aString = null;
                Double aNullableDouble = null;

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("anInt".equals(fieldName)) {
                        anInt = reader.getInt();
                    } else if ("aBoolean".equals(fieldName)) {
                        aBoolean = reader.getBoolean();
                    } else if ("aString".equals(fieldName)) {
                        aString = reader.getString();
                    } else if ("aNullableDecimal".equals(fieldName)) {
                        aNullableDouble = reader.getNullable(JsonReader::getDouble);
                    } else {
                        reader.skipChildren();
                    }
                }

                return new GeneralProperties(anInt, aBoolean, aString, aNullableDouble);
            });
        }

        @Override
        public int hashCode() {
            return Objects.hash(anInt, aBoolean, aString, aNullableDecimal);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof GeneralProperties)) {
                return false;
            }

            GeneralProperties other = (GeneralProperties) obj;
            return anInt == other.anInt
                && aBoolean == other.aBoolean
                && Objects.equals(aString, other.aString)
                && Objects.equals(aNullableDecimal, other.aNullableDecimal);
        }
    }
}
