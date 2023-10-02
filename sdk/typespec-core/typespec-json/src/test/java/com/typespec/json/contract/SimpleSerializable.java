// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json.contract;

import com.typespec.json.JsonReader;
import com.typespec.json.JsonSerializable;
import com.typespec.json.JsonToken;
import com.typespec.json.JsonWriter;

import java.io.IOException;
import java.util.Objects;

/**
 * Simple implementation of {@link JsonSerializable} to validate APIs that accept {@link JsonSerializable}.
 */
public final class SimpleSerializable implements JsonSerializable<SimpleSerializable> {
    private final boolean aBoolean;
    private final int anInt;
    private final double aDecimal;
    private final String aString;

    public SimpleSerializable(boolean aBoolean, int anInt, double aDecimal, String aString) {
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

    public static SimpleSerializable fromJson(JsonReader jsonReader) throws IOException {
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
                return new SimpleSerializable(aBoolean, anInt, aDecimal, aString);
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
        if (!(obj instanceof SimpleSerializable)) {
            return false;
        }

        SimpleSerializable other = (SimpleSerializable) obj;

        return aBoolean == other.aBoolean
            && anInt == other.anInt
            && aDecimal == other.aDecimal
            && Objects.equals(aString, other.aString);
    }
}
