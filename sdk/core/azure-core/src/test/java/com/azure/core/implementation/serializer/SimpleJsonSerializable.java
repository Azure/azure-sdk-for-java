// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Objects;

/**
 * Test class implementing {@link JsonSerializable}.
 */
public final class SimpleJsonSerializable implements JsonSerializable<SimpleJsonSerializable> {
    private final boolean aBoolean;
    private final int anInt;
    private final double aDecimal;
    private final String aString;

    public SimpleJsonSerializable(boolean aBoolean, int anInt, double aDecimal, String aString) {
        this.aBoolean = aBoolean;
        this.anInt = anInt;
        this.aDecimal = aDecimal;
        this.aString = aString;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeBooleanField("boolean", aBoolean);
        jsonWriter.writeIntField("int", anInt);
        jsonWriter.writeDoubleField("decimal", aDecimal);
        jsonWriter.writeStringField("string", aString);

        return jsonWriter.writeEndObject();
    }

    public static SimpleJsonSerializable fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            boolean aBoolean = false;
            boolean foundABoolean = false;
            double aDecimal = 0.0D;
            boolean foundADecimal = false;
            int anInt = 0;
            boolean foundAnInt = false;
            String aString = null;
            boolean foundAString = false;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("boolean".equals(fieldName)) {
                    aBoolean = reader.getBoolean();
                    foundABoolean = true;
                } else if ("decimal".equals(fieldName)) {
                    aDecimal = reader.getDouble();
                    foundADecimal = true;
                } else if ("int".equals(fieldName)) {
                    anInt = reader.getInt();
                    foundAnInt = true;
                } else if ("string".equals(fieldName)) {
                    aString = reader.getString();
                    foundAString = true;
                } else {
                    reader.skipChildren();
                }
            }

            if (foundABoolean && foundADecimal && foundAnInt && foundAString) {
                return new SimpleJsonSerializable(aBoolean, anInt, aDecimal, aString);
            }

            throw new IllegalStateException("Missing required properties.");
        });
    }

    public boolean isABoolean() {
        return aBoolean;
    }

    public int getAnInt() {
        return anInt;
    }

    public double getADecimal() {
        return aDecimal;
    }

    public String getAString() {
        return aString;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aBoolean, anInt, aDecimal, aString);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleJsonSerializable)) {
            return false;
        }

        SimpleJsonSerializable other = (SimpleJsonSerializable) obj;

        return aBoolean == other.aBoolean
            && anInt == other.anInt
            && aDecimal == other.aDecimal
            && Objects.equals(aString, other.aString);
    }
}
