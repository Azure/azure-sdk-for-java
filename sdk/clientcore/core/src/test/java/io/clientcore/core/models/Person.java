// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import io.clientcore.core.json.JsonReader;
import io.clientcore.core.json.JsonSerializable;
import io.clientcore.core.json.JsonToken;
import io.clientcore.core.json.JsonWriter;

import java.io.IOException;
import java.util.Objects;

public class Person implements JsonSerializable<Person> {
    private String name;
    private int age;

    public Person() {
    }

    public Person setName(String name) {
        this.name = name;

        return this;
    }

    public String getName() {
        return name;
    }

    public Person setAge(int age) {
        this.age = age;

        return this;
    }

    public int getAge() {
        return age;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Person person = (Person) other;

        return age == person.age && Objects.equals(name, person.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("name", name);
        jsonWriter.writeIntField("age", age);
        jsonWriter.writeEndObject();

        return jsonWriter;
    }

    public static Person fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Person person = new Person();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getString();
                reader.nextToken();

                if ("name".equals(fieldName)) {
                    person.setName(reader.getString());
                } else if ("age".equals(fieldName)) {
                    person.setAge(reader.getInt());
                } else {
                    reader.skipChildren();
                }
            }

            return person;
        });
    }
}
