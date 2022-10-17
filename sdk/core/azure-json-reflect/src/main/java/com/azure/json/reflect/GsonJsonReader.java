package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.*;
import java.lang.invoke.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;

class GsonJsonReader extends JsonReader {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static Class<?> GSON_JSON_READER_CLASS;
    private static Class<?> GSON_JSON_TOKEN_ENUM;

    private static JsonReaderConstructor JSON_READER_CONSTRUCTOR;
    private static JsonReaderSetLenient JSON_READER_SET_LENIENT;
    private static JsonReaderClose JSON_READER_CLOSE;
    private static JsonReaderPeek JSON_READER_PEEK;
    private static JsonReaderBeginObject JSON_READER_BEGIN_OBJECT;
    private static JsonReaderEndObject JSON_READER_END_OBJECT;
    private static JsonReaderBeginArray JSON_READER_BEGIN_ARRAY;
    private static JsonReaderEndArray JSON_READER_END_ARRAY;
    private static JsonReaderNextNull JSON_READER_NEXT_NULL;
    private static JsonReaderNextName JSON_READER_NEXT_NAME;
    private static JsonReaderNextString JSON_READER_NEXT_STRING;
    private static JsonReaderNextBoolean JSON_READER_NEXT_BOOLEAN;
    private static JsonReaderNextInt JSON_READER_NEXT_INT;
    private static JsonReaderNextLong JSON_READER_NEXT_LONG;
    private static JsonReaderNextDouble JSON_READER_NEXT_DOUBLE;
    private static JsonReaderSkipValue JSON_READER_SKIP_VALUE;

    private static boolean INITIALIZED = false;
    private static boolean ATTEMPTED_INITIALIZATION = false;

    private final Object gsonJsonReader;
    private JsonToken currentToken;

    private final byte[] jsonBytes;
    private final String jsonString;
    private final boolean resetSupported;
    private final boolean nonNumericNumbersSupported;

    private boolean consumed = false;
    private boolean complete = false;

    /**
     * Constructs an instance of {@link JsonReader} from a {@code byte[]}.
     *
     * @param json JSON {@code byte[]}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    static JsonReader fromBytes(byte[] json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonReader(new InputStreamReader(new ByteArrayInputStream(json), StandardCharsets.UTF_8), true, json, null, options);
    }

    /**
     * Constructs an instance of {@link JsonReader} from a String.
     *
     * @param json JSON String.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    static JsonReader fromString(String json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonReader(new StringReader(json), true, null, json, options);
    }

    /**
     * Constructs an instance of {@link JsonReader} from an {@link InputStream}.
     *
     * @param json JSON {@link InputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    static JsonReader fromStream(InputStream json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonReader(new InputStreamReader(json, StandardCharsets.UTF_8), json.markSupported(), null, null, options);
    }

    /**
     * Constructs an instance of {@link GsonJsonReader} from a {@link Reader}.
     *
     * @param json JSON {@link Reader}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link GsonJsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    static JsonReader fromReader(Reader json, JsonOptions options) {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new GsonJsonReader(json, json.markSupported(), null, null, options);
    }

    private GsonJsonReader(Reader reader, boolean resetSupported, byte[] jsonBytes, String jsonString, JsonOptions options) {
        this(reader, resetSupported, jsonBytes, jsonString, options.isNonNumericNumbersSupported());
    }

    private GsonJsonReader(Reader reader, boolean resetSupported, byte[] jsonBytes, String jsonString, boolean nonNumericNumbersSupported) {
        try {
            initialize();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Gson is not present or an incorrect version is present.");
        }

        gsonJsonReader = JSON_READER_CONSTRUCTOR.createJsonReader(reader);
        JSON_READER_SET_LENIENT.setLenient(gsonJsonReader, nonNumericNumbersSupported);

        this.resetSupported = resetSupported;
        this.jsonBytes = jsonBytes;
        this.jsonString = jsonString;
        this.nonNumericNumbersSupported = nonNumericNumbersSupported;
    }

    static synchronized void initialize() throws ReflectiveOperationException {
        if (INITIALIZED) {
            return;
        } else if (ATTEMPTED_INITIALIZATION) {
            throw new ReflectiveOperationException("Initialization of GsonJsonReader has failed in the past.");
        }

        ATTEMPTED_INITIALIZATION = true;

        GSON_JSON_TOKEN_ENUM = Class.forName("com.google.gson.stream.JsonToken");
        GSON_JSON_READER_CLASS = Class.forName("com.google.gson.stream.JsonReader");

        MethodType voidMT = methodType(void.class);
        MethodType voidObjectMT = methodType(void.class, Object.class);

        try {
            MethodHandle gsonReaderConstructor = LOOKUP.findConstructor(GSON_JSON_READER_CLASS, methodType(void.class, Reader.class));
            JSON_READER_CONSTRUCTOR = (JsonReaderConstructor) LambdaMetafactory.metafactory(LOOKUP, "createJsonReader", methodType(JsonReaderConstructor.class), methodType(Object.class, Reader.class), gsonReaderConstructor, gsonReaderConstructor.type()).getTarget().invoke();

            JSON_READER_SET_LENIENT = createMetaFactory("setLenient", methodType(void.class, boolean.class), JsonReaderSetLenient.class, methodType(void.class, Object.class, boolean.class));
            JSON_READER_CLOSE = createMetaFactory("close", voidMT, JsonReaderClose.class, methodType(void.class, Object.class));
            JSON_READER_PEEK = createMetaFactory("peek", methodType(GSON_JSON_TOKEN_ENUM), JsonReaderPeek.class, methodType(Object.class, Object.class));
            JSON_READER_BEGIN_OBJECT = createMetaFactory("beginObject", voidMT, JsonReaderBeginObject.class, voidObjectMT);
            JSON_READER_END_OBJECT = createMetaFactory("endObject", voidMT, JsonReaderEndObject.class, voidObjectMT);
            JSON_READER_BEGIN_ARRAY = createMetaFactory("beginArray", voidMT, JsonReaderBeginArray.class, voidObjectMT);
            JSON_READER_END_ARRAY = createMetaFactory("endArray", voidMT, JsonReaderEndArray.class, voidObjectMT);
            JSON_READER_NEXT_NULL = createMetaFactory("nextNull", voidMT, JsonReaderNextNull.class, voidObjectMT);
            JSON_READER_NEXT_NAME = createMetaFactory("nextName", methodType(String.class), JsonReaderNextName.class, methodType(String.class, Object.class));
            JSON_READER_NEXT_STRING = createMetaFactory("nextString", methodType(String.class), JsonReaderNextString.class, methodType(String.class, Object.class));
            JSON_READER_NEXT_BOOLEAN = createMetaFactory("nextBoolean", methodType(boolean.class), JsonReaderNextBoolean.class, methodType(boolean.class, Object.class));
            JSON_READER_NEXT_INT = createMetaFactory("nextInt", methodType(int.class), JsonReaderNextInt.class, methodType(int.class, Object.class));
            JSON_READER_NEXT_LONG = createMetaFactory("nextLong", methodType(long.class), JsonReaderNextLong.class, methodType(long.class, Object.class));
            JSON_READER_NEXT_DOUBLE = createMetaFactory("nextDouble", methodType(double.class), JsonReaderNextDouble.class, methodType(double.class, Object.class));
            JSON_READER_SKIP_VALUE = createMetaFactory("skipValue", voidMT, JsonReaderSkipValue.class, voidObjectMT);
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new ReflectiveOperationException("Initialization of GsonJsonReader failed.");
            }
        }

        INITIALIZED = true;
    }

    @SuppressWarnings("unchecked")
    private static <T> T createMetaFactory(String methodName, MethodType implType, Class<T> invokedClass, MethodType invokedType) throws Throwable {
        MethodHandle handle = LOOKUP.findVirtual(GSON_JSON_READER_CLASS, methodName, implType);
        return (T) LambdaMetafactory.metafactory(LOOKUP, methodName, methodType(invokedClass), invokedType, handle, handle.type()).getTarget().invoke();
    }

    @Override
    public JsonToken currentToken() {
        return currentToken;
    }

    @Override
    public JsonToken nextToken() throws IOException {
    if (complete) {
        return currentToken;
    }

    // GSON requires explicitly beginning and ending arrays and objects and consuming null values.
    // The contract of JsonReader implicitly overlooks these properties.
        if (currentToken == JsonToken.START_OBJECT) {
            JSON_READER_BEGIN_OBJECT.beginObject(gsonJsonReader);
        } else if (currentToken == JsonToken.END_OBJECT) {
            JSON_READER_END_OBJECT.endObject(gsonJsonReader);
        } else if (currentToken == JsonToken.START_ARRAY) {
            JSON_READER_BEGIN_ARRAY.beginArray(gsonJsonReader);
        } else if (currentToken == JsonToken.END_ARRAY) {
            JSON_READER_END_ARRAY.endArray(gsonJsonReader);
        } else if (currentToken == JsonToken.NULL) {
            JSON_READER_NEXT_NULL.nextNull(gsonJsonReader);
        }

        if (!consumed && currentToken != null) {
            switch (currentToken) {
                case FIELD_NAME:
                    JSON_READER_NEXT_NAME.nextName(gsonJsonReader);
                    break;

                case BOOLEAN:
                    JSON_READER_NEXT_BOOLEAN.nextBoolean(gsonJsonReader);
                    break;

                case NUMBER:
                    JSON_READER_NEXT_DOUBLE.nextDouble(gsonJsonReader);
                    break;

                case STRING:
                    JSON_READER_NEXT_STRING.nextString(gsonJsonReader);
                    break;

                default:
                    break;
            }
        }

        currentToken = mapToken((Enum<?>) JSON_READER_PEEK.peek(gsonJsonReader));

        if (currentToken == JsonToken.END_DOCUMENT) {
            complete = true;
        }

        consumed = false;
        return currentToken;
    }

    @Override
    public byte[] getBinary() throws IOException {
        consumed = true;

        if (currentToken == JsonToken.NULL) {
            JSON_READER_NEXT_NULL.nextNull(gsonJsonReader);
            return null;
        } else {
            return Base64.getDecoder().decode((String) JSON_READER_NEXT_STRING.nextString(gsonJsonReader));
        }
    }

    @Override
    public boolean getBoolean() throws IOException {
        consumed = true;
        return JSON_READER_NEXT_BOOLEAN.nextBoolean(gsonJsonReader);
    }

    @Override
    public float getFloat() throws IOException {
        consumed = true;
        return (float) JSON_READER_NEXT_DOUBLE.nextDouble(gsonJsonReader);
    }

    @Override
    public double getDouble() throws IOException {
        consumed = true;
        return JSON_READER_NEXT_DOUBLE.nextDouble(gsonJsonReader);
    }

    @Override
    public int getInt() throws IOException {
        consumed = true;
        return JSON_READER_NEXT_INT.nextInt(gsonJsonReader);
    }

    @Override
    public long getLong() throws IOException {
        consumed = true;
        return JSON_READER_NEXT_LONG.nextLong(gsonJsonReader);
    }

    @Override
    public String getString() throws IOException {
        consumed = true;

        if (currentToken == JsonToken.NULL) {
            return null;
        } else {
            return JSON_READER_NEXT_STRING.nextString(gsonJsonReader);
        }
    }

    @Override
    public String getFieldName() throws IOException {
        consumed = true;
        return JSON_READER_NEXT_NAME.nextName(gsonJsonReader);
    }

    @Override
    public void skipChildren() throws IOException {
        consumed = true;
        JSON_READER_SKIP_VALUE.skipValue(gsonJsonReader);
    }

    @Override
    public JsonReader bufferObject() throws IOException {
        if (currentToken == JsonToken.START_OBJECT
            || (currentToken == JsonToken.FIELD_NAME && nextToken() == JsonToken.START_OBJECT)) {
            consumed = true;
            StringBuilder bufferedObject = new StringBuilder();
            readChildren(bufferedObject);
            String json = bufferedObject.toString();
            return new GsonJsonReader(new StringReader(json), true, null, json, nonNumericNumbersSupported);
        } else {
            throw new IllegalStateException("Cannot buffer a JSON object from a non-object, non-field name "
                + "starting location. Starting location: " + currentToken());
        }
    }

    @Override
    public boolean resetSupported() {
        return resetSupported;
    }

    @Override
    public JsonReader reset() {
        if (!resetSupported) {
            throw new IllegalStateException("'reset' isn't supported by this JsonReader.");
        }

        if (jsonBytes != null) {
            return new GsonJsonReader(new InputStreamReader(new ByteArrayInputStream(jsonBytes), StandardCharsets.UTF_8), true, jsonBytes, null, nonNumericNumbersSupported);
        } else {
            return new GsonJsonReader(new StringReader(jsonString), true, null, jsonString, nonNumericNumbersSupported);
        }
    }

    @Override
    public void close() throws IOException {
        JSON_READER_CLOSE.close(gsonJsonReader);
    }

    /*
     * Maps the GSON JsonToken to the azure-json JsonToken.
     */
    private JsonToken mapToken(Enum<?> token) {
        // Special case for when currentToken is called after instantiating the JsonReader.
        if (token == null) {
            return null;
        }

        // Check token is GSON JsonToken
        if (token.getClass() != GSON_JSON_TOKEN_ENUM) {
            throw new IllegalStateException("Unsupported enum, pass a Gson JsonToken");
        }

        switch (token.name()) {
            case "BEGIN_OBJECT":
                return JsonToken.START_OBJECT;
            case "END_OBJECT":
                return JsonToken.END_OBJECT;

            case "BEGIN_ARRAY":
                return JsonToken.START_ARRAY;
            case "END_ARRAY":
                return JsonToken.END_ARRAY;

            case "NAME":
                return JsonToken.FIELD_NAME;
            case "STRING":
                return JsonToken.STRING;
            case "NUMBER":
                return JsonToken.NUMBER;
            case "BOOLEAN":
                return JsonToken.BOOLEAN;
            case "NULL":
                return JsonToken.NULL;

            case "END_DOCUMENT":
                return JsonToken.END_DOCUMENT;

            default:
                throw new IllegalStateException("Unsupported token type: '" + token + "'.");
        }
    }

    @FunctionalInterface
    private interface JsonReaderConstructor {
        Object createJsonReader(Reader reader);
    }

    @FunctionalInterface
    private interface JsonReaderSetLenient {
        void setLenient(Object jsonReader, boolean lenient);
    }

    @FunctionalInterface
    private interface JsonReaderClose {
        void close(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderPeek {
        Object peek(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderBeginObject {
        void beginObject(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderEndObject {
        void endObject(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderBeginArray {
        void beginArray(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderEndArray {
        void endArray(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextNull {
        void nextNull(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextName {
        String nextName(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextString {
        String nextString(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextBoolean {
        boolean nextBoolean(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextInt {
        int nextInt(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextLong {
        long nextLong(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderNextDouble {
        double nextDouble(Object jsonReader) throws IOException;
    }

    @FunctionalInterface
    private interface JsonReaderSkipValue {
        void skipValue(Object jsonReader) throws IOException;
    }
}
