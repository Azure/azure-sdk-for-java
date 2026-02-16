// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.testingmodels;

import com.azure.core.models.GeoPoint;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.BasicField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelWithPrimitiveCollections implements JsonSerializable<ModelWithPrimitiveCollections> {
    @BasicField(name = "Key", isKey = BasicField.BooleanHelper.TRUE)
    @JsonProperty(value = "Key")
    private String key;

    @BasicField(name = "Bools")
    @JsonProperty(value = "Bools")
    private Boolean[] bools;

    @BasicField(name = "Dates")
    @JsonProperty(value = "Dates")
    private OffsetDateTime[] dates;

    @BasicField(name = "Doubles")
    @JsonProperty(value = "Doubles")
    private Double[] doubles;

    @BasicField(name = "Ints")
    @JsonProperty(value = "Ints")
    private int[] ints;

    @BasicField(name = "Longs")
    @JsonProperty(value = "Longs")
    private Long[] longs;

    @BasicField(name = "Points")
    @JsonProperty(value = "Points")
    private GeoPoint[] points;

    @BasicField(name = "Strings")
    @JsonProperty(value = "Strings")
    private String[] strings;

    public String key() {
        return this.key;
    }

    public ModelWithPrimitiveCollections key(String key) {
        this.key = key;
        return this;
    }

    public ModelWithPrimitiveCollections bools(Boolean[] bools) {
        this.bools = CoreUtils.clone(bools);
        return this;
    }

    public Boolean[] bools() {
        return CoreUtils.clone(bools);
    }

    public ModelWithPrimitiveCollections dates(OffsetDateTime[] dates) {
        this.dates = CoreUtils.clone(dates);
        return this;
    }

    public OffsetDateTime[] dates() {
        return CoreUtils.clone(dates);
    }

    public ModelWithPrimitiveCollections doubles(Double[] doubles) {
        this.doubles = CoreUtils.clone(doubles);
        return this;
    }

    public Double[] doubles() {
        return CoreUtils.clone(doubles);
    }

    public ModelWithPrimitiveCollections ints(int[] ints) {
        this.ints = CoreUtils.clone(ints);
        return this;
    }

    public int[] ints() {
        return CoreUtils.clone(ints);
    }

    public ModelWithPrimitiveCollections longs(Long[] longs) {
        this.longs = CoreUtils.clone(longs);
        return this;
    }

    public Long[] longs() {
        return CoreUtils.clone(longs);
    }

    public ModelWithPrimitiveCollections points(GeoPoint[] points) {
        this.points = CoreUtils.clone(points);
        return this;
    }

    public GeoPoint[] points() {
        return CoreUtils.clone(points);
    }

    public ModelWithPrimitiveCollections strings(String[] strings) {
        this.strings = CoreUtils.clone(strings);
        return this;
    }

    public String[] strings() {
        return CoreUtils.clone(strings);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("Key", key)
            .writeArrayField("Bools", bools, JsonWriter::writeBoolean)
            .writeArrayField("Dates", dates, (writer, date) -> writer.writeString(Objects.toString(date, null)))
            .writeArrayField("Doubles", doubles, JsonWriter::writeNumber);
        if (ints != null) {
            jsonWriter.writeStartArray("Ints");
            for (int i : ints) {
                jsonWriter.writeInt(i);
            }
            jsonWriter.writeEndArray();
        }
        return jsonWriter.writeArrayField("Longs", longs, JsonWriter::writeNumber)
            .writeArrayField("Points", points, JsonWriter::writeJson)
            .writeArrayField("Strings", strings, JsonWriter::writeString)
            .writeEndObject();
    }

    public static ModelWithPrimitiveCollections fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ModelWithPrimitiveCollections model = new ModelWithPrimitiveCollections();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("Key".equals(fieldName)) {
                    model.key = reader.getString();
                } else if ("Bools".equals(fieldName)) {
                    List<Boolean> bools = reader.readArray(elem -> elem.getNullable(JsonReader::getBoolean));
                    if (bools != null) {
                        model.bools = bools.toArray(new Boolean[0]);
                    }
                } else if ("Dates".equals(fieldName)) {
                    List<OffsetDateTime> dates
                        = reader.readArray(elem -> CoreUtils.parseBestOffsetDateTime(elem.getString()));
                    if (dates != null) {
                        model.dates = dates.toArray(new OffsetDateTime[0]);
                    }
                } else if ("Doubles".equals(fieldName)) {
                    List<Double> doubles = reader.readArray(elem -> elem.getNullable(nonNull -> {
                        if (nonNull.currentToken() == JsonToken.STRING) {
                            String str = nonNull.getString();
                            if ("INF".equals(str) || "+INF".equals(str)) {
                                return Double.POSITIVE_INFINITY;
                            } else if ("-INF".equals(str)) {
                                return Double.NEGATIVE_INFINITY;
                            } else if ("NaN".equals(str)) {
                                return Double.NaN;
                            } else {
                                return Double.parseDouble(str);
                            }
                        } else {
                            return nonNull.getDouble();
                        }
                    }));
                    if (doubles != null) {
                        model.doubles = doubles.toArray(new Double[0]);
                    }
                } else if ("Ints".equals(fieldName)) {
                    List<Integer> ints = reader.readArray(JsonReader::getInt);
                    if (ints != null) {
                        model.ints = ints.stream().mapToInt(i -> i).toArray();
                    }
                } else if ("Longs".equals(fieldName)) {
                    List<Long> longs = reader.readArray(elem -> elem.getNullable(JsonReader::getLong));
                    if (longs != null) {
                        model.longs = longs.toArray(new Long[0]);
                    }
                } else if ("Points".equals(fieldName)) {
                    List<GeoPoint> points = reader.readArray(GeoPoint::fromJson);
                    if (points != null) {
                        model.points = points.toArray(new GeoPoint[0]);
                    }
                } else if ("Strings".equals(fieldName)) {
                    List<String> strings = reader.readArray(JsonReader::getString);
                    if (strings != null) {
                        model.strings = strings.toArray(new String[0]);
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return model;
        });
    }
}
