// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.serializer;

import io.clientcore.core.implementation.http.serializer.DefaultJsonSerializer;
import io.clientcore.core.json.JsonReader;
import io.clientcore.core.json.JsonSerializable;
import io.clientcore.core.json.JsonToken;
import io.clientcore.core.json.JsonWriter;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinaryDataSerializationTests {
    private static final ObjectSerializer SERIALIZER = new DefaultJsonSerializer();
    private static final byte[] HELLO_BYTES = "hello".getBytes(StandardCharsets.UTF_8);
    private static final String BASE64_HELLO_BYTES = Base64.getEncoder().encodeToString(HELLO_BYTES);
    private static final SimplePojo SIMPLE_POJO = new SimplePojo()
        .setString("hello")
        .setNumber(3.14)
        .setBool(true);
    private static final BinaryData FROM_BYTES = BinaryData.fromBytes(HELLO_BYTES);
    private static final Supplier<BinaryData> FROM_STREAM =
        () -> BinaryData.fromStream(new ByteArrayInputStream(HELLO_BYTES), (long) HELLO_BYTES.length);
    private static final BinaryData FROM_STRING = BinaryData.fromString("hello");
    private static final BinaryData FROM_OBJECT = BinaryData.fromObject(SIMPLE_POJO);
    private static final BinaryData FROM_STRING_BASE64 = BinaryData.fromString(BASE64_HELLO_BYTES);
    private static final String EMPTY_OBJECT_JSON = "{}";
    private static final String SIMPLE_PROPERTY_BYTES_JSON = "{\"simpleProperty\":\"" + BASE64_HELLO_BYTES + "\"}";
    private static final String SIMPLE_PROPERTY_STREAM_JSON = "{\"simpleProperty\":\"" + BASE64_HELLO_BYTES + "\"}";
    private static final String SIMPLE_PROPERTY_STRING_JSON = "{\"simpleProperty\":\"hello\"}";
    private static final String SIMPLE_PROPERTY_OBJECT_JSON =
        "{\"simpleProperty\":\"{\\\"string\\\":\\\"hello\\\",\\\"number\\\":3.14,\\\"boolean\\\":true}\"}";
    private static final SimpleProperty SIMPLE_PROPERTY_NULL = new SimpleProperty();
    private static final SimpleProperty SIMPLE_PROPERTY_FROM_STRING = new SimpleProperty()
        .setSimpleProperty(FROM_STRING);
    private static final SimpleProperty SIMPLE_PROPERTY_FROM_OBJECT = new SimpleProperty()
        .setSimpleProperty(FROM_OBJECT);
    private static final SimpleProperty SIMPLE_PROPERTY_BASE64 = new SimpleProperty()
        .setSimpleProperty(FROM_STRING_BASE64);
    private static final String LIST_PROPERTY_BYTES_JSON = "{\"listProperty\":[\"" + BASE64_HELLO_BYTES + "\"]}";
    private static final String LIST_PROPERTY_STREAM_JSON = "{\"listProperty\":[\"" + BASE64_HELLO_BYTES + "\"]}";
    private static final String LIST_PROPERTY_STRING_JSON = "{\"listProperty\":[\"hello\"]}";
    private static final String LIST_PROPERTY_OBJECT_JSON =
        "{\"listProperty\":[\"{\\\"string\\\":\\\"hello\\\",\\\"number\\\":3.14,\\\"boolean\\\":true}\"]}";
    private static final String LIST_PROPERTY_MULTI_JSON =
        "{\"listProperty\":[\"hello\",\"{\\\"string\\\":\\\"hello\\\",\\\"number\\\":3.14,\\\"boolean\\\":true}\"]}";

    private static final ListProperty LIST_PROPERTY_NULL = new ListProperty();
    private static final ListProperty LIST_PROPERTY_FROM_STRING = new ListProperty()
        .setListProperty(singletonList(FROM_STRING));
    private static final ListProperty LIST_PROPERTY_FROM_OBJECT = new ListProperty()
        .setListProperty(singletonList(FROM_OBJECT));
    private static final ListProperty LIST_PROPERTY_MULTIPLE = new ListProperty()
        .setListProperty(Arrays.asList(FROM_STRING, FROM_OBJECT));
    private static final ListProperty LIST_PROPERTY_BASE64 = new ListProperty()
        .setListProperty(singletonList(FROM_STRING_BASE64));
    private static final ListProperty LIST_PROPERTY_BASE64_MULTIPLE = new ListProperty()
        .setListProperty(Arrays.asList(FROM_STRING, FROM_OBJECT));
    private static final String MAP_PROPERTY_BYTES_JSON = "{\"mapProperty\":{\"key\":\"" + BASE64_HELLO_BYTES + "\"}}";
    private static final String MAP_PROPERTY_STREAM_JSON = "{\"mapProperty\":{\"key\":\"" + BASE64_HELLO_BYTES + "\"}}";
    private static final String MAP_PROPERTY_STRING_JSON = "{\"mapProperty\":{\"key\":\"hello\"}}";
    private static final String MAP_PROPERTY_OBJECT_JSON =
        "{\"mapProperty\":{\"key\":\"{\\\"string\\\":\\\"hello\\\",\\\"number\\\":3.14,\\\"boolean\\\":true}\"}}";
    private static final String MAP_PROPERTY_MULTI_JSON =
        "{\"mapProperty\":{"
            + "\"fromBytes\":\"" + BASE64_HELLO_BYTES + "\","
            + "\"fromStream\":\"" + BASE64_HELLO_BYTES + "\","
            + "\"fromString\":\"hello\","
            + "\"fromObject\":\"{\\\"string\\\":\\\"hello\\\",\\\"number\\\":3.14,\\\"boolean\\\":true}\"}}";
    private static final MapProperty MAP_PROPERTY_NULL = new MapProperty();
    private static final MapProperty MAP_PROPERTY_FROM_BYTES = new MapProperty()
        .setMapProperty(singletonMap("key", FROM_BYTES));
    private static final MapProperty MAP_PROPERTY_FROM_STREAM = new MapProperty()
        .setMapProperty(singletonMap("key", FROM_STREAM.get()));
    private static final MapProperty MAP_PROPERTY_FROM_STRING = new MapProperty()
        .setMapProperty(singletonMap("key", FROM_STRING));
    private static final MapProperty MAP_PROPERTY_FROM_OBJECT = new MapProperty()
        .setMapProperty(singletonMap("key", FROM_OBJECT));
    private static final MapProperty MAP_PROPERTY_MULTIPLE = new MapProperty()
        .setMapProperty(createMapPropertyMultiMap(false));
    private static final MapProperty MAP_PROPERTY_BASE64 = new MapProperty()
        .setMapProperty(singletonMap("key", FROM_STRING_BASE64));
    private static final MapProperty MAP_PROPERTY_BASE64_MULTIPLE = new MapProperty()
        .setMapProperty(createMapPropertyMultiMap(true));

    private static Map<String, BinaryData> createMapPropertyMultiMap(boolean base64) {
        Map<String, BinaryData> map = new LinkedHashMap<>();

        map.put("fromBytes", base64 ? FROM_STRING_BASE64 : FROM_BYTES);
        map.put("fromStream", base64 ? FROM_STRING_BASE64 : FROM_STREAM.get());
        map.put("fromString", FROM_STRING);
        map.put("fromObject", FROM_OBJECT);

        return map;
    }

    @ParameterizedTest
    @MethodSource("binaryDataSerializationSupplier")
    public void binaryDataSerialization(Object serializable, String expected) throws IOException {
        String actual = new String(SERIALIZER.serializeToBytes(serializable));

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> binaryDataSerializationSupplier() {
        return Stream.of(
            Arguments.of(SIMPLE_PROPERTY_NULL, EMPTY_OBJECT_JSON),
            Arguments.of(SIMPLE_PROPERTY_FROM_STRING, SIMPLE_PROPERTY_STRING_JSON),
            Arguments.of(SIMPLE_PROPERTY_FROM_OBJECT, SIMPLE_PROPERTY_OBJECT_JSON),

            Arguments.of(LIST_PROPERTY_NULL, EMPTY_OBJECT_JSON),
            Arguments.of(LIST_PROPERTY_FROM_STRING, LIST_PROPERTY_STRING_JSON),
            Arguments.of(LIST_PROPERTY_FROM_OBJECT, LIST_PROPERTY_OBJECT_JSON),
            Arguments.of(LIST_PROPERTY_MULTIPLE, LIST_PROPERTY_MULTI_JSON),

            Arguments.of(MAP_PROPERTY_NULL, EMPTY_OBJECT_JSON),
            Arguments.of(MAP_PROPERTY_FROM_STRING, MAP_PROPERTY_STRING_JSON),
            Arguments.of(MAP_PROPERTY_FROM_OBJECT, MAP_PROPERTY_OBJECT_JSON),
            Arguments.of(MAP_PROPERTY_MULTIPLE, MAP_PROPERTY_MULTI_JSON)
        );
    }

    @ParameterizedTest
    @MethodSource("binaryDataDeserializationSupplier")
    @Execution(ExecutionMode.SAME_THREAD)
    public void binaryDataDeserialization(Object expected, String json, Class<?> type) throws IOException {
        Object actual = SERIALIZER.deserializeFromBytes(json.getBytes(), type);

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> binaryDataDeserializationSupplier() {
        return Stream.of(
            Arguments.of(SIMPLE_PROPERTY_NULL, EMPTY_OBJECT_JSON, SimpleProperty.class),
            Arguments.of(SIMPLE_PROPERTY_BASE64, SIMPLE_PROPERTY_BYTES_JSON, SimpleProperty.class),
            Arguments.of(SIMPLE_PROPERTY_BASE64, SIMPLE_PROPERTY_STREAM_JSON, SimpleProperty.class),
            Arguments.of(SIMPLE_PROPERTY_FROM_STRING, SIMPLE_PROPERTY_STRING_JSON, SimpleProperty.class),
            Arguments.of(SIMPLE_PROPERTY_FROM_OBJECT, SIMPLE_PROPERTY_OBJECT_JSON, SimpleProperty.class),

            Arguments.of(LIST_PROPERTY_NULL, EMPTY_OBJECT_JSON, ListProperty.class),
            Arguments.of(LIST_PROPERTY_BASE64, LIST_PROPERTY_BYTES_JSON, ListProperty.class),
            Arguments.of(LIST_PROPERTY_BASE64, LIST_PROPERTY_STREAM_JSON, ListProperty.class),
            Arguments.of(LIST_PROPERTY_FROM_STRING, LIST_PROPERTY_STRING_JSON, ListProperty.class),
            Arguments.of(LIST_PROPERTY_FROM_OBJECT, LIST_PROPERTY_OBJECT_JSON, ListProperty.class),
            Arguments.of(LIST_PROPERTY_BASE64_MULTIPLE, LIST_PROPERTY_MULTI_JSON, ListProperty.class),

            Arguments.of(MAP_PROPERTY_NULL, EMPTY_OBJECT_JSON, MapProperty.class),
            Arguments.of(MAP_PROPERTY_BASE64, MAP_PROPERTY_BYTES_JSON, MapProperty.class),
            Arguments.of(MAP_PROPERTY_BASE64, MAP_PROPERTY_STREAM_JSON, MapProperty.class),
            Arguments.of(MAP_PROPERTY_FROM_STRING, MAP_PROPERTY_STRING_JSON, MapProperty.class),
            Arguments.of(MAP_PROPERTY_FROM_OBJECT, MAP_PROPERTY_OBJECT_JSON, MapProperty.class),
            Arguments.of(MAP_PROPERTY_BASE64_MULTIPLE, MAP_PROPERTY_MULTI_JSON, MapProperty.class)
        );
    }

    public static final class SimpleProperty implements JsonSerializable<SimpleProperty> {
        private BinaryData simpleProperty;

        public BinaryData getSimpleProperty() {
            return simpleProperty;
        }

        public SimpleProperty setSimpleProperty(BinaryData simpleProperty) {
            this.simpleProperty = simpleProperty;

            return this;
        }

        @Override
        public int hashCode() {
            return binaryDataHash(simpleProperty);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SimpleProperty)) {
                return false;
            }

            SimpleProperty other = (SimpleProperty) obj;

            return binaryDataEquals(simpleProperty, other.simpleProperty);
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();

            String simplePropertyText;

            if (simpleProperty == null) {
                simplePropertyText = null;
            } else {
                byte[] bytes = simpleProperty.toBytes();

                if (bytes == null) {
                    simplePropertyText = null;
                } else if (bytes.length == 0) {
                    simplePropertyText = "";
                } else {
                    simplePropertyText = new String(bytes);
                }
            }

            jsonWriter.writeStringField("simpleProperty", simplePropertyText);
            jsonWriter.writeEndObject();

            return jsonWriter;
        }

        public static SimpleProperty fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                SimpleProperty simpleProperty = new SimpleProperty();

                while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = jsonReader.getString();
                    jsonReader.nextToken();

                    if ("simpleProperty".equals(fieldName)) {
                        Object object = reader.readUntyped();

                        if (object instanceof String) {
                            simpleProperty.setSimpleProperty(BinaryData.fromString((String) object));
                        } else {
                            simpleProperty.setSimpleProperty(BinaryData.fromObject(object));
                        }
                    } else {
                        jsonReader.skipChildren();
                    }
                }

                return simpleProperty;
            });
        }
    }

    public static final class ListProperty implements JsonSerializable<ListProperty> {
        private List<BinaryData> listProperty;

        public List<BinaryData> getListProperty() {
            return listProperty;
        }

        public ListProperty setListProperty(List<BinaryData> listProperty) {
            this.listProperty = listProperty;

            return this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(listProperty);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ListProperty)) {
                return false;
            }

            ListProperty other = (ListProperty) obj;

            if (listProperty == null && other.listProperty == null) {
                return true;
            } else if (listProperty != null && other.listProperty == null) {
                return false;
            } else if (listProperty == null && other.listProperty != null) {
                return false;
            }

            if (listProperty.size() != other.listProperty.size()) {
                return false;
            }

            int size = listProperty.size();

            for (int i = 0; i < size; i++) {
                if (!binaryDataEquals(listProperty.get(i), other.listProperty.get(i))) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();

            if (listProperty != null) {
                jsonWriter.writeFieldName("listProperty");
                jsonWriter.writeStartArray();

                for (BinaryData binaryData : listProperty) {
                    String binaryDataText;
                    byte[] bytes = binaryData.toBytes();

                    if (bytes == null) {
                        binaryDataText = null;
                    } else if (bytes.length == 0) {
                        binaryDataText = "";
                    } else {
                        binaryDataText = new String(bytes);
                    }

                    jsonWriter.writeString(binaryDataText);
                }

                jsonWriter.writeEndArray();
            }

            jsonWriter.writeEndObject();

            return jsonWriter;
        }

        public static ListProperty fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                ListProperty listProperty = new ListProperty();

                while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = jsonReader.getString();
                    jsonReader.nextToken();

                    if ("listProperty".equals(fieldName)) {
                        List<BinaryData> list = new ArrayList<>();
                        @SuppressWarnings("unchecked")
                        List<Object> objectList = (List<Object>) reader.readUntyped();

                        objectList.forEach(object -> {
                            if (object instanceof String) {
                                list.add(BinaryData.fromString((String) object));
                            } else {
                                list.add(BinaryData.fromObject(object));
                            }
                        });

                        listProperty.setListProperty(list);
                    } else {
                        jsonReader.skipChildren();
                    }
                }

                return listProperty;
            });
        }
    }

    public static final class MapProperty implements JsonSerializable<MapProperty> {
        private Map<String, BinaryData> mapProperty;

        public Map<String, BinaryData> getMapProperty() {
            return mapProperty;
        }

        public MapProperty setMapProperty(Map<String, BinaryData> mapProperty) {
            this.mapProperty = mapProperty;

            return this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mapProperty);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MapProperty)) {
                return false;
            }

            MapProperty other = (MapProperty) obj;

            if (mapProperty == null && other.mapProperty == null) {
                return true;
            } else if (mapProperty != null && other.mapProperty == null) {
                return false;
            } else if (mapProperty == null && other.mapProperty != null) {
                return false;
            }

            if (mapProperty.size() != other.mapProperty.size()) {
                return false;
            }

            for (Map.Entry<String, BinaryData> entry : mapProperty.entrySet()) {
                if (!other.mapProperty.containsKey(entry.getKey())) {
                    return false;
                }

                if (!binaryDataEquals(entry.getValue(), other.mapProperty.get(entry.getKey()))) {
                    return false;
                }
            }

            return true;
        }


        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();

            if (mapProperty != null) {
                jsonWriter.writeFieldName("mapProperty");
                jsonWriter.writeStartObject();

                for (Map.Entry<String, BinaryData> entry : mapProperty.entrySet()) {
                    jsonWriter.writeFieldName(entry.getKey());

                    BinaryData binaryData = entry.getValue();
                    String binaryDataText;

                    if (binaryData == null) {
                        binaryDataText = null;
                    } else {
                        byte[] bytes = binaryData.toBytes();

                        if (bytes == null) {
                            binaryDataText = null;
                        } else if (bytes.length == 0) {
                            binaryDataText = "";
                        } else {
                            if (entry.getKey().equals("fromBytes") || entry.getKey().equals("fromStream")) {
                                binaryDataText = Base64.getEncoder().encodeToString(bytes);
                            } else {
                                binaryDataText = new String(bytes);
                            }
                        }
                    }

                    jsonWriter.writeString(binaryDataText);
                }

                jsonWriter.writeEndObject();
            }

            jsonWriter.writeEndObject();

            return jsonWriter;
        }

        public static MapProperty fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                MapProperty mapProperty = new MapProperty();

                while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = jsonReader.getString();
                    jsonReader.nextToken();

                    if ("mapProperty".equals(fieldName)) {
                        Map<String, BinaryData> map = new LinkedHashMap<>();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> objectMap = (Map<String, Object>) reader.readUntyped();

                        objectMap.forEach((key, value) -> {
                            if (value instanceof String) {
                                map.put(key, BinaryData.fromString((String) value));
                            } else {
                                map.put(key, BinaryData.fromObject(value));
                            }
                        });

                        mapProperty.setMapProperty(map);
                    } else {
                        jsonReader.skipChildren();
                    }
                }

                return mapProperty;
            });
        }
    }

    public static final class SimplePojo implements JsonSerializable<SimplePojo> {
        private String string;
        private double number;
        private boolean bool;

        public String getString() {
            return string;
        }

        public SimplePojo setString(String string) {
            this.string = string;

            return this;
        }

        public double getNumber() {
            return number;
        }

        public SimplePojo setNumber(double number) {
            this.number = number;

            return this;
        }

        public boolean isBool() {
            return bool;
        }

        public SimplePojo setBool(boolean bool) {
            this.bool = bool;

            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("string", string);
            jsonWriter.writeNumberField("number", number);
            jsonWriter.writeBooleanField("boolean", bool);
            jsonWriter.writeEndObject();

            return jsonWriter;
        }

        public static SimplePojo fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                SimplePojo simplePojo = new SimplePojo();

                while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = jsonReader.getString();
                    jsonReader.nextToken();

                    if ("string".equals(fieldName)) {
                        simplePojo.setString(jsonReader.getString());
                    } else if ("number".equals(fieldName)) {
                        simplePojo.setNumber(jsonReader.getDouble());
                    } else if ("boolean".equals(fieldName)) {
                        simplePojo.setBool(jsonReader.getBoolean());
                    } else {
                        jsonReader.skipChildren();
                    }
                }

                return simplePojo;
            });
        }
    }

    private static int binaryDataHash(BinaryData data) {
        return Objects.hash(data == null ? null : data.toString());
    }

    private static boolean binaryDataEquals(BinaryData data1, BinaryData data2) {
        return (data1 == null && data2 == null)
            || (data1 != null && data2 != null && Objects.equals(data1.toString(), data2.toString()));
    }
}
