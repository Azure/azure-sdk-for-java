// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.List;

public class ComposeTurtles implements JsonSerializable<ComposeTurtles> {
    private String description;
    private TurtleWithTypeIdContainingDot turtlesSet1Lead;
    private List<TurtleWithTypeIdContainingDot> turtlesSet1;
    private NonEmptyAnimalWithTypeIdContainingDot turtlesSet2Lead;
    private List<NonEmptyAnimalWithTypeIdContainingDot> turtlesSet2;

    public String description() {
        return this.description;
    }

    public ComposeTurtles withDescription(String description) {
        this.description = description;
        return this;
    }

    public List<TurtleWithTypeIdContainingDot> turtlesSet1() {
        return this.turtlesSet1;
    }

    public TurtleWithTypeIdContainingDot turtlesSet1Lead() {
        return this.turtlesSet1Lead;
    }

    public ComposeTurtles withTurtlesSet1Lead(TurtleWithTypeIdContainingDot lead) {
        this.turtlesSet1Lead = lead;
        return this;
    }

    public ComposeTurtles withTurtlesSet1(List<TurtleWithTypeIdContainingDot> turtles) {
        this.turtlesSet1 = turtles;
        return this;
    }

    public List<NonEmptyAnimalWithTypeIdContainingDot> turtlesSet2() {
        return this.turtlesSet2;
    }

    public NonEmptyAnimalWithTypeIdContainingDot turtlesSet2Lead() {
        return this.turtlesSet2Lead;
    }

    public ComposeTurtles withTurtlesSet2Lead(NonEmptyAnimalWithTypeIdContainingDot lead) {
        this.turtlesSet2Lead = lead;
        return this;
    }

    public ComposeTurtles withTurtlesSet2(List<NonEmptyAnimalWithTypeIdContainingDot> turtles) {
        this.turtlesSet2 = turtles;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return jsonWriter.writeStartObject()
            .writeStringField("description", description, false)
            .writeJsonField("turtlesSet1Lead", turtlesSet1Lead, false)
            .writeArrayField("turtlesSet1", turtlesSet1, false, JsonWriter::writeJson)
            .writeJsonField("turtlesSet2Lead", turtlesSet2Lead, false)
            .writeArrayField("turtlesSet2", turtlesSet2, false, JsonWriter::writeJson)
            .writeEndObject()
            .flush();
    }

    public static ComposeTurtles fromJson(JsonReader jsonReader) {
        return jsonReader.readObject(reader -> {
            String description = null;
            TurtleWithTypeIdContainingDot turtleSet1Lead = null;
            List<TurtleWithTypeIdContainingDot> turtleSet1 = null;
            NonEmptyAnimalWithTypeIdContainingDot turtleSet2Lead = null;
            List<NonEmptyAnimalWithTypeIdContainingDot> turtleSet2 = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("description".equals(fieldName)) {
                    description = reader.getStringValue();
                } else if ("turtlesSet1Lead".equals(fieldName)) {
                    turtleSet1Lead = TurtleWithTypeIdContainingDot.fromJson(reader);
                } else if ("turtlesSet1".equals(fieldName) && reader.currentToken() == JsonToken.START_ARRAY) {
                    turtleSet1 = reader.readArray(TurtleWithTypeIdContainingDot::fromJson);
                } else if ("turtlesSet2Lead".equals(fieldName)) {
                    turtleSet2Lead = NonEmptyAnimalWithTypeIdContainingDot.fromJson(jsonReader);
                } else if ("turtlesSet2".equals(fieldName) && reader.currentToken() == JsonToken.START_ARRAY) {
                    turtleSet2 = reader.readArray(NonEmptyAnimalWithTypeIdContainingDot::fromJson);
                } else {
                    reader.skipChildren();
                }
            }

            return new ComposeTurtles().withDescription(description)
                .withTurtlesSet1Lead(turtleSet1Lead)
                .withTurtlesSet1(turtleSet1)
                .withTurtlesSet2Lead(turtleSet2Lead)
                .withTurtlesSet2(turtleSet2);
        });
    }
}
