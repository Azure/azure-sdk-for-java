// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class for testing serialization.
 */
public class NewFoo implements JsonSerializable<NewFoo> {
    private String bar;
    private List<String>  baz;
    private Map<String, String> qux;
    private String moreProps;
    private Integer empty;
    private Map<String, Object> additionalProperties;
    private Map<String, Object> additionalPropertiesProperty;

    public String bar() {
        return bar;
    }

    public void bar(String bar) {
        this.bar = bar;
    }

    public List<String> baz() {
        return baz;
    }

    public void baz(List<String> baz) {
        this.baz = baz;
    }

    public Map<String, String> qux() {
        return qux;
    }

    public void qux(Map<String, String> qux) {
        this.qux = qux;
    }

    public String moreProps() {
        return moreProps;
    }

    public void moreProps(String moreProps) {
        this.moreProps = moreProps;
    }

    public Integer empty() {
        return empty;
    }

    public void empty(Integer empty) {
        this.empty = empty;
    }

    private void additionalProperties(String key, Object value) {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }
        additionalProperties.put(key.replace("\\.", "."), value);
    }

    public Map<String, Object> additionalProperties() {
        return additionalProperties;
    }

    public void additionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Map<String, Object> additionalPropertiesProperty() {
        return additionalPropertiesProperty;
    }

    public void additionalPropertiesProperty(Map<String, Object> additionalPropertiesProperty) {
        this.additionalPropertiesProperty = additionalPropertiesProperty;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return toJsonInternal(jsonWriter, "newfoo");
    }

    JsonWriter toJsonInternal(JsonWriter jsonWriter, String type) {
        jsonWriter.writeStartObject()
            .writeStringField("$type", type);

        if (bar != null || baz != null || qux != null || moreProps != null) {
            jsonWriter.writeStartObject("properties")
                .writeStringField("bar", bar, false);

            if (baz != null || qux != null) {
                jsonWriter.writeStartObject("props")
                    .writeArrayField("baz", baz, false, JsonWriter::writeString);

                if (qux != null) {
                    jsonWriter.writeStartObject("q")
                        .writeMapField("qux", qux, JsonWriter::writeString)
                        .writeEndObject();
                }

                jsonWriter.writeEndObject();
            }

            jsonWriter.writeStringField("more.props", moreProps, false)
                .writeEndObject();
        }

        if (empty != null) {
            jsonWriter.writeStartObject("props")
                .writeIntField("empty", empty)
                .writeEndObject();
        }

        jsonWriter.writeMapField("additionalProperties", additionalPropertiesProperty, false,
            JsonUtils::writeUntypedField);

        if (additionalProperties != null) {
            additionalProperties.forEach((key, value) ->
                JsonUtils.writeUntypedField(jsonWriter.writeFieldName(key), value));

        }

        return jsonWriter.writeEndObject().flush();
    }

    public static NewFoo fromJson(JsonReader jsonReader) {
        return fromJsonInternal(jsonReader, null);
    }

    static NewFoo fromJsonInternal(JsonReader jsonReader, String expectedType) {
        return JsonUtils.readObject(jsonReader, reader -> {
            String type = null;
            String bar = null;
            List<String> baz = null;
            Map<String, String> qux = null;
            String moreProps = null;
            Integer empty = null;
            Map<String, Object> additionalProperties = null;
            Map<String, Object> additionalPropertiesProperty = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("$type".equals(fieldName)) {
                    type = reader.getStringValue();
                } else if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("bar".equals(fieldName)) {
                            bar = reader.getStringValue();
                        } else if ("more.props".equals(fieldName)) {
                            moreProps = reader.getStringValue();
                        } else if ("props".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                            while (reader.nextToken() != JsonToken.END_OBJECT) {
                                fieldName = reader.getFieldName();
                                reader.nextToken();

                                if ("baz".equals(fieldName)) {
                                    baz = reader.readArray(JsonReader::getStringValue);
                                } else if ("q".equals(fieldName)) {
                                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                                        fieldName = reader.getFieldName();
                                        reader.nextToken();

                                        if ("qux".equals(fieldName)
                                            && reader.currentToken() == JsonToken.START_OBJECT) {
                                            if (qux == null) {
                                                qux = new LinkedHashMap<>();
                                            }

                                            while (reader.nextToken() != JsonToken.END_OBJECT) {
                                                fieldName = reader.getFieldName();
                                                reader.nextToken();

                                                qux.put(fieldName, reader.getStringValue());
                                            }
                                        } else {
                                            reader.skipChildren();
                                        }
                                    }
                                } else {
                                    reader.skipChildren();
                                }
                            }
                        } else {
                            // TODO (alzimmer): Determine the question below.
                            // This is a level down in the JSON from the root, does this count as additional properties?
                            reader.skipChildren();
                        }
                    }
                } else if ("props".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("empty".equals(fieldName)) {
                            empty = reader.getIntegerNullableValue();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else if ("additionalProperties".equals(fieldName)
                    && reader.currentToken() == JsonToken.START_OBJECT) {
                    if (additionalPropertiesProperty == null) {
                        additionalPropertiesProperty = new LinkedHashMap<>();
                    }

                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        additionalPropertiesProperty.put(fieldName, JsonUtils.readUntypedField(reader));
                    }
                } else {
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }

                    additionalProperties.put(fieldName, JsonUtils.readUntypedField(reader));
                }
            }

            // When called from a subtype, the expected $type will be passed and verified, as long as the
            // $type in the JSON wasn't null or missing.
            // TODO (alzimmer): Should this throw if it was present and null?
            if (expectedType != null && type != null && !Objects.equals(expectedType, type)) {
                throw new IllegalStateException("Discriminator field '$type' didn't match expected value: "
                    + "'" + expectedType + "'. It was: '" + type + "'.");
            }

            if ((type == null && expectedType == null) || "newfoo".equals(type)) {
                NewFoo newFoo = new NewFoo();
                newFoo.bar(bar);
                newFoo.baz(baz);
                newFoo.qux(qux);
                newFoo.moreProps(moreProps);
                newFoo.empty(empty);
                newFoo.additionalProperties(additionalProperties);
                newFoo.additionalPropertiesProperty(additionalPropertiesProperty);

                return newFoo;
            } else if ("newfoochild".equals(expectedType) || "newfoochild".equals(type)) {
                NewFooChild newFooChild = new NewFooChild();
                newFooChild.bar(bar);
                newFooChild.baz(baz);
                newFooChild.qux(qux);
                newFooChild.moreProps(moreProps);
                newFooChild.empty(empty);
                newFooChild.additionalProperties(additionalProperties);
                newFooChild.additionalPropertiesProperty(additionalPropertiesProperty);

                return newFooChild;
            } else {
                throw new IllegalStateException("Invalid discriminator value '" + reader.getStringValue()
                    + "', expected: 'newfoo' or 'newfoochild'.");
            }
        });
    }
}
