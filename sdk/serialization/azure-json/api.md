```java
maven {
    parent : com.azure:azure-client-sdk-parent:1.7.0
    properties : com.azure:azure-json:1.5.0
    configuration {
        jacoco {
            min-line-coverage : 0.8
            min-branch-coverage : 0.7
        }
    }
    name : Microsoft Azure Java JSON Library
    description : This package provides interfaces for reading and writing JSON.
    dependencies {
    }
}
module com.azure.json {
    exports com.azure.json;
    exports com.azure.json.models;
    uses com.azure.json.JsonProvider;
}
package com.azure.json {
    public final class JsonOptions {
        public JsonOptions()
        public boolean isJsoncSupported()
        public JsonOptions setJsoncSupported(boolean jsoncSupported)
        public boolean isNonNumericNumbersSupported()
        public JsonOptions setNonNumericNumbersSupported(boolean nonNumericNumbersSupported)
    }
    public interface JsonProvider {
        JsonReader createReader(byte[] json, JsonOptions options) throws IOException
        JsonReader createReader(String json, JsonOptions options) throws IOException
        JsonReader createReader(InputStream json, JsonOptions options) throws IOException
        JsonReader createReader(Reader json, JsonOptions options) throws IOException
        JsonWriter createWriter(OutputStream json, JsonOptions options) throws IOException
        JsonWriter createWriter(Writer json, JsonOptions options) throws IOException
    }
    public final class JsonProviders {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        public static JsonReader createReader(byte[] json) throws IOException
        public static JsonReader createReader(String json) throws IOException
        public static JsonReader createReader(InputStream json) throws IOException
        public static JsonReader createReader(Reader json) throws IOException
        public static JsonReader createReader(byte[] json, JsonOptions options) throws IOException
        public static JsonReader createReader(String json, JsonOptions options) throws IOException
        public static JsonReader createReader(InputStream json, JsonOptions options) throws IOException
        public static JsonReader createReader(Reader json, JsonOptions options) throws IOException
        public static JsonWriter createWriter(OutputStream json) throws IOException
        public static JsonWriter createWriter(Writer json) throws IOException
        public static JsonWriter createWriter(OutputStream json, JsonOptions options) throws IOException
        public static JsonWriter createWriter(Writer json, JsonOptions options) throws IOException
    }
    public abstract class JsonReader implements Closeable {
        public JsonReader()
        public abstract byte[] getBinary() throws IOException
        public abstract boolean getBoolean() throws IOException
        public abstract JsonReader bufferObject() throws IOException
        @Override public abstract void close() throws IOException
        public abstract JsonToken currentToken()
        public abstract double getDouble() throws IOException
        public final boolean isEndArrayOrObject()
        public abstract String getFieldName() throws IOException
        public abstract float getFloat() throws IOException
        public abstract int getInt() throws IOException
        public abstract long getLong() throws IOException
        public abstract JsonToken nextToken() throws IOException
        public final <T> T getNullable(ReadValueCallback<JsonReader, T> nonNullGetter) throws IOException
        public String getRawText() throws IOException
        public final <T> List<T> readArray(ReadValueCallback<JsonReader, T> elementReaderFunc) throws IOException
        public final String readChildren() throws IOException
        public final void readChildren(StringBuilder buffer) throws IOException
        public final <T> Map<String, T> readMap(ReadValueCallback<JsonReader, T> valueReaderFunc) throws IOException
        public final <T> T readObject(ReadValueCallback<JsonReader, T> objectReaderFunc) throws IOException
        public final String readRemainingFieldsAsJsonObject() throws IOException
        public final void readRemainingFieldsAsJsonObject(StringBuilder buffer) throws IOException
        public final Object readUntyped() throws IOException
        public abstract JsonReader reset() throws IOException
        public abstract boolean isResetSupported()
        public abstract void skipChildren() throws IOException
        public final boolean isStartArrayOrObject()
        public abstract String getString() throws IOException
        public final String getText() throws IOException
    }
    public interface JsonSerializable<T extends JsonSerializable<T>> {
        static <T extends JsonSerializable<T>> T fromJson(JsonReader jsonReader) throws IOException
        static <T extends JsonSerializable<T>> T fromJson(String string) throws IOException
        static <T extends JsonSerializable<T>> T fromJson(byte[] bytes) throws IOException
        static <T extends JsonSerializable<T>> T fromJson(InputStream inputStream) throws IOException
        static <T extends JsonSerializable<T>> T fromJson(Reader reader) throws IOException
        JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        default void toJson(OutputStream outputStream) throws IOException
        default void toJson(Writer writer) throws IOException
        default byte[] toJsonBytes() throws IOException
        default String toJsonString() throws IOException
    }
    public enum JsonToken {
        START_OBJECT,
        END_OBJECT,
        START_ARRAY,
        END_ARRAY,
        FIELD_NAME,
        BOOLEAN,
        NULL,
        NUMBER,
        STRING,
        END_DOCUMENT;
    }
    public final class JsonWriteContext {
        public static final JsonWriteContext ROOT = new JsonWriteContext ( /* Elided */ ) ;
        public static final JsonWriteContext COMPLETED = new JsonWriteContext ( /* Elided */ ) ;
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        public JsonWriteContext getParent()
        public JsonWriteContext updateContext(JsonToken token)
        public void validateToken(JsonToken token)
        public JsonWriteState getWriteState()
    }
    public enum JsonWriteState {
        ROOT,
        OBJECT,
        ARRAY,
        FIELD,
        COMPLETED;
    }
    public abstract class JsonWriter implements Closeable {
        public JsonWriter()
        @Override public abstract void close() throws IOException
        public abstract JsonWriter flush() throws IOException
        public final <T> JsonWriter writeArray(T[] array, WriteValueCallback<JsonWriter, T> elementWriterFunc) throws IOException
        public final <T> JsonWriter writeArray(Iterable<T> array, WriteValueCallback<JsonWriter, T> elementWriterFunc) throws IOException
        public <T> JsonWriter writeArray(T[] array, WriteValueCallback<JsonWriter, T> elementWriterFunc, boolean skipNullElements) throws IOException
        public <T> JsonWriter writeArray(Iterable<T> array, WriteValueCallback<JsonWriter, T> elementWriterFunc, boolean skipNullElements) throws IOException
        public final <T> JsonWriter writeArrayField(String fieldName, T[] array, WriteValueCallback<JsonWriter, T> elementWriterFunc) throws IOException
        public final <T> JsonWriter writeArrayField(String fieldName, Iterable<T> array, WriteValueCallback<JsonWriter, T> elementWriterFunc) throws IOException
        public <T> JsonWriter writeArrayField(String fieldName, T[] array, WriteValueCallback<JsonWriter, T> elementWriterFunc, boolean skipNullElements) throws IOException
        public <T> JsonWriter writeArrayField(String fieldName, Iterable<T> array, WriteValueCallback<JsonWriter, T> elementWriterFunc, boolean skipNullElements) throws IOException
        public abstract JsonWriter writeBinary(byte[] value) throws IOException
        public final JsonWriter writeBinaryField(String fieldName, byte[] value) throws IOException
        public abstract JsonWriter writeBoolean(boolean value) throws IOException
        public final JsonWriter writeBoolean(Boolean value) throws IOException
        public final JsonWriter writeBooleanField(String fieldName, boolean value) throws IOException
        public final JsonWriter writeBooleanField(String fieldName, Boolean value) throws IOException
        public abstract JsonWriteContext getWriteContext()
        public abstract JsonWriter writeDouble(double value) throws IOException
        public final JsonWriter writeDoubleField(String fieldName, double value) throws IOException
        public abstract JsonWriter writeEndArray() throws IOException
        public abstract JsonWriter writeEndObject() throws IOException
        public abstract JsonWriter writeFieldName(String fieldName) throws IOException
        public abstract JsonWriter writeFloat(float value) throws IOException
        public final JsonWriter writeFloatField(String fieldName, float value) throws IOException
        public abstract JsonWriter writeInt(int value) throws IOException
        public final JsonWriter writeIntField(String fieldName, int value) throws IOException
        public final JsonWriter writeJson(JsonSerializable<?> value) throws IOException
        public final JsonWriter writeJsonField(String fieldName, JsonSerializable<?> value) throws IOException
        public abstract JsonWriter writeLong(long value) throws IOException
        public final JsonWriter writeLongField(String fieldName, long value) throws IOException
        public final <T> JsonWriter writeMap(Map<String, T> map, WriteValueCallback<JsonWriter, T> valueWriterFunc) throws IOException
        public <T> JsonWriter writeMap(Map<String, T> map, WriteValueCallback<JsonWriter, T> valueWriterFunc, boolean skipNullValues) throws IOException
        public final <T> JsonWriter writeMapField(String fieldName, Map<String, T> map, WriteValueCallback<JsonWriter, T> valueWriterFunc) throws IOException
        public <T> JsonWriter writeMapField(String fieldName, Map<String, T> map, WriteValueCallback<JsonWriter, T> valueWriterFunc, boolean skipNullValues) throws IOException
        public abstract JsonWriter writeNull() throws IOException
        public final <T> JsonWriter writeNullableField(String fieldName, T nullable, WriteValueCallback<JsonWriter, T> writerFunc) throws IOException
        public final JsonWriter writeNullField(String fieldName) throws IOException
        public final JsonWriter writeNumber(Number value) throws IOException
        public final JsonWriter writeNumberField(String fieldName, Number value) throws IOException
        public final JsonWriter writeRawField(String fieldName, String value) throws IOException
        public abstract JsonWriter writeRawValue(String value) throws IOException
        public abstract JsonWriter writeStartArray() throws IOException
        public final JsonWriter writeStartArray(String fieldName) throws IOException
        public abstract JsonWriter writeStartObject() throws IOException
        public final JsonWriter writeStartObject(String fieldName) throws IOException
        public abstract JsonWriter writeString(String value) throws IOException
        public final JsonWriter writeStringField(String fieldName, String value) throws IOException
        public JsonWriter writeUntyped(Object value) throws IOException
        public JsonWriter writeUntypedField(String fieldName, Object value) throws IOException
    }
    @FunctionalInterface
    public interface ReadValueCallback<T, R> {
        R read(T input) throws IOException
    }
    @FunctionalInterface
    public interface WriteValueCallback<T, U> {
        void write(T input1, U input2) throws IOException
    }
}
package com.azure.json.models {
    public final class JsonArray extends JsonElement {
        public JsonArray()
        public JsonArray addElement(JsonElement element)
        public JsonArray addElement(boolean element)
        public JsonArray addElement(Number element)
        public JsonArray addElement(String element)
        public JsonArray addElement(int index, JsonElement element)
        public JsonArray addElement(int index, boolean element)
        public JsonArray addElement(int index, Number element)
        public JsonArray addElement(int index, String element)
        @Override public boolean isArray()
        public JsonElement getElement(int index) throws IndexOutOfBoundsException
        public JsonArray setElement(int index, JsonElement element)
        public JsonArray setElement(int index, boolean element)
        public JsonArray setElement(int index, Number element)
        public JsonArray setElement(int index, String element)
        public static JsonArray fromJson(JsonReader jsonReader) throws IOException
        public JsonElement removeElement(int index) throws IndexOutOfBoundsException
        public int size()
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public String toJsonString() throws IOException
    }
    public final class JsonBoolean extends JsonElement {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Override public boolean isBoolean()
        public static JsonBoolean fromJson(JsonReader jsonReader) throws IOException
        public static JsonBoolean getInstance(boolean value)
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public String toJsonString() throws IOException
        public boolean getValue()
    }
    public abstract class JsonElement implements JsonSerializable<JsonElement> {
        public JsonElement()
        public boolean isArray()
        public JsonArray asArray()
        public JsonBoolean asBoolean()
        public JsonNull asNull()
        public JsonNumber asNumber()
        public JsonObject asObject()
        public JsonString asString()
        public boolean isBoolean()
        public boolean isNull()
        public boolean isNumber()
        public boolean isObject()
        public boolean isString()
    }
    public final class JsonNull extends JsonElement {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        public static JsonNull fromJson(JsonReader jsonReader) throws IOException
        public static JsonNull getInstance()
        @Override public boolean isNull()
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public String toJsonString() throws IOException
    }
    public final class JsonNumber extends JsonElement {
        public JsonNumber(Number value)
        public static JsonNumber fromJson(JsonReader jsonReader) throws IOException
        @Override public boolean isNumber()
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public String toJsonString() throws IOException
        public Number getValue()
    }
    public final class JsonObject extends JsonElement {
        public JsonObject()
        public static JsonObject fromJson(JsonReader jsonReader) throws IOException
        public boolean hasProperty(String key)
        @Override public boolean isObject()
        public JsonElement getProperty(String key)
        public JsonObject setProperty(String key, JsonElement element)
        public JsonObject setProperty(String key, boolean element)
        public JsonObject setProperty(String key, Number element)
        public JsonObject setProperty(String key, String element)
        public JsonElement removeProperty(String key)
        public int size()
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public String toJsonString() throws IOException
    }
    public final class JsonString extends JsonElement {
        public JsonString(String value)
        public static JsonString fromJson(JsonReader jsonReader) throws IOException
        @Override public boolean isString()
        @Override public JsonWriter toJson(JsonWriter jsonWriter) throws IOException
        @Override public String toJsonString() throws IOException
        public String getValue()
    }
}
```
