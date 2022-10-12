package com.azure.json.reflect.jackson;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.implementation.DefaultJsonReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.lang.invoke.MethodType.methodType;

public class JacksonJsonReader extends JsonReader {
    private static boolean initialized = false;
    private static boolean attemptedInitialization = false;
    private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
    private static Class<?> jacksonTokenEnum = null;
    private static Class jacksonFeatureEnum;
    private static Object jsonFactory;

    private final Object jacksonParser;
    private JsonToken currentToken;

    private static MethodHandle createParserMethod;
    private static MethodHandle getBooleanMethod;
    private static MethodHandle getFloatValueMethod;
    private static MethodHandle getDoubleValueMethod;
    private static MethodHandle getIntValueMethod;
    private static MethodHandle getLongValueMethod;
    private static MethodHandle getBinaryValueMethod;
    private static MethodHandle nextTokenMethod;
    private static MethodHandle getValueAsStringMethod;
    private static MethodHandle currentNameMethod;
    private static MethodHandle skipChildrenMethod;
    private static MethodHandle closeMethod;
    private static MethodHandle configureMethod;

    private final byte[] jsonBytes;
    private final String jsonString;
    private final boolean resetSupported;
    private final boolean nonNumericNumbersSupported;

    /**
     * Constructs an instance of {@link JsonReader} from a {@code byte[]}.
     *
     * @param json JSON {@code byte[]}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If a {@link JsonReader} wasn't able to be constructed from the JSON {@code byte[]}.
     */
    static JsonReader fromBytes(byte[] json, JsonOptions options) throws IOException {
        return new JacksonJsonReader(new InputStreamReader(new ByteArrayInputStream(json), StandardCharsets.UTF_8), true, json, null, options);
    }

    /**
     * Constructs an instance of {@link JsonReader} from a String.
     *
     * @param json JSON String.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If a {@link JsonReader} wasn't able to be constructed from the JSON String.
     */
    static JsonReader fromString(String json, JsonOptions options) throws IOException {
        return new JacksonJsonReader(new StringReader(json), true, null, json, options);
    }

    /**
     * Constructs an instance of {@link JsonReader} from an {@link InputStream}.
     *
     * @param json JSON {@link InputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If a {@link JsonReader} wasn't able to be constructed from the JSON {@link InputStream}.
     */
    static JsonReader fromStream(InputStream json, JsonOptions options) throws IOException {
        return new JacksonJsonReader(new InputStreamReader(json, StandardCharsets.UTF_8), false, null, null, options);
    }

    /**
     * Constructs an instance of {@link DefaultJsonReader} from a {@link Reader}.
     *
     * @param reader JSON {@link Reader}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link DefaultJsonReader}.
     * @throws IOException If a {@link DefaultJsonReader} wasn't able to be constructed from the JSON {@link Reader}.
     */
    static JsonReader fromReader(Reader reader, JsonOptions options) throws IOException {
        return new JacksonJsonReader(reader, reader.markSupported(), null, null, options);
    }

    private JacksonJsonReader(Reader reader, boolean resetSupported, byte[] jsonBytes, String jsonString, JsonOptions options) throws IOException {
        this(reader, resetSupported, jsonBytes, jsonString, options.isNonNumericNumbersSupported());
    }

    private JacksonJsonReader(Reader reader, boolean resetSupported, byte[] jsonBytes, String jsonString, boolean nonNumericNumbersSupported) throws IOException {
        try {
            initialize();

            jacksonParser = createParserMethod.invoke(jsonFactory, reader);
            // Configure Jackson to support non-numeric numbers
            configureMethod.invoke(jacksonParser, Enum.valueOf(jacksonFeatureEnum, "ALLOW_NON_NUMERIC_NUMBERS"), nonNumericNumbersSupported);

        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new IllegalStateException("Jackson is not present or an incorrect version is present.");
            }
        }
        this.resetSupported = resetSupported;
        this.jsonBytes = jsonBytes;
        this.jsonString = jsonString;
        this.nonNumericNumbersSupported = nonNumericNumbersSupported;

    }
    
    static synchronized void initialize() throws ReflectiveOperationException {
        if (initialized) {
            return;
        } else if (attemptedInitialization) {
            throw new ReflectiveOperationException("Initialization of JacksonJsonReader has failed in the past.");
        }

        attemptedInitialization = true;

        jacksonTokenEnum =  Class.forName("com.fasterxml.jackson.core.JsonToken");

        // The jacksonParserClass is made via the JsonFactory
        Class<?> jacksonFactoryClass = Class.forName("com.fasterxml.jackson.core.JsonFactory");
        Class<?> jacksonParserClass = Class.forName("com.fasterxml.jackson.core.JsonParser");

        jacksonFeatureEnum = (Class) Arrays.stream(jacksonParserClass.getDeclaredClasses()).filter(c -> "Feature".equals(c.getSimpleName())).findAny().orElse(null);

        // Initializing the factory
        try {
            jsonFactory = publicLookup.findConstructor(jacksonFactoryClass, methodType(void.class)).invoke();
        } catch (Throwable e) {
            throw (RuntimeException) e.getCause();
        }

        // Initializing all the method handles.
        createParserMethod = publicLookup.findVirtual(jacksonFactoryClass, "createParser", methodType(jacksonParserClass, Reader.class));
        getBooleanMethod = publicLookup.findVirtual(jacksonParserClass, "getBooleanValue", methodType(boolean.class));
        getFloatValueMethod = publicLookup.findVirtual(jacksonParserClass, "getFloatValue", methodType(float.class));
        getDoubleValueMethod = publicLookup.findVirtual(jacksonParserClass, "getDoubleValue", methodType(double.class));
        getIntValueMethod = publicLookup.findVirtual(jacksonParserClass, "getIntValue", methodType(int.class));
        getLongValueMethod = publicLookup.findVirtual(jacksonParserClass, "getLongValue", methodType(long.class));
        getBinaryValueMethod = publicLookup.findVirtual(jacksonParserClass, "getBinaryValue", methodType(byte[].class));
        nextTokenMethod = publicLookup.findVirtual(jacksonParserClass, "nextToken", methodType(jacksonTokenEnum));
        getValueAsStringMethod = publicLookup.findVirtual(jacksonParserClass, "getValueAsString", methodType(String.class));
        currentNameMethod = publicLookup.findVirtual(jacksonParserClass, "currentName", methodType(String.class));
        skipChildrenMethod = publicLookup.findVirtual(jacksonParserClass, "skipChildren", methodType(jacksonParserClass));
        closeMethod = publicLookup.findVirtual(jacksonParserClass, "close", methodType(void.class));
        configureMethod = publicLookup.findVirtual(jacksonParserClass, "configure", methodType(jacksonParserClass, jacksonFeatureEnum, boolean.class));

        initialized = true;
    }

    @Override
    public JsonToken currentToken() {
        return currentToken;
    }

    @Override
    public JsonToken nextToken() throws IOException {
        try {
            currentToken = mapToken((Enum<?>) nextTokenMethod.invoke(jacksonParser));
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }

        return currentToken;
    }

    @Override
    public byte[] getBinary() throws IOException {
        try {
            // GetBinaryValue cannot handle a Null token
            if (currentToken() == JsonToken.NULL) {
                return null;
            }
            return (byte[]) getBinaryValueMethod.invoke(jacksonParser);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
    }

    @Override
    public boolean getBoolean() throws IOException {
        try {
            return (boolean) getBooleanMethod.invoke(jacksonParser);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
    }

    @Override
    public float getFloat() throws IOException {
        try {
            return (float) getFloatValueMethod.invoke(jacksonParser);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
    }

    @Override
    public double getDouble() throws IOException {
        try {
            return (double) getDoubleValueMethod.invoke(jacksonParser);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
    }

    @Override
    public int getInt() throws IOException {
        try {
            return (int) getIntValueMethod.invoke(jacksonParser);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
    }

    @Override
    public long getLong() throws IOException {
        try {
            return (long) getLongValueMethod.invoke(jacksonParser);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
    }

    @Override
    public String getString() throws IOException {
        try {
            return (String) getValueAsStringMethod.invoke(jacksonParser);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
    }

    @Override
    public String getFieldName() throws IOException {
        try {
            return (String) currentNameMethod.invoke(jacksonParser);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
    }

    @Override
    public void skipChildren() throws IOException {
        try {
            skipChildrenMethod.invoke(jacksonParser);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
    }

    @Override
    public JsonReader bufferObject() throws IOException {
        JsonToken currentToken = currentToken();
        if (currentToken == JsonToken.START_OBJECT
            || (currentToken == JsonToken.FIELD_NAME && nextToken() == JsonToken.START_OBJECT)) {
            StringBuilder bufferedObject = new StringBuilder();
            readChildren(bufferedObject);
            String json = bufferedObject.toString();
            return new JacksonJsonReader(new StringReader(json), true, null, json, nonNumericNumbersSupported);
        } else {
            throw new IllegalStateException("Cannot buffer a JSON object from a non-object, non-field name "
                + "starting location. Starting location: " + currentToken());
        }
    }

    @Override
    public boolean resetSupported() {
        return this.resetSupported;
    }

    @Override
    public JsonReader reset() throws IOException {
        if (!resetSupported) {
            throw new IllegalStateException("'reset' isn't supported by this JsonReader.");
        }

        if (jsonBytes != null) {
            return new JacksonJsonReader(new InputStreamReader(new ByteArrayInputStream(jsonBytes), StandardCharsets.UTF_8), true, jsonBytes, null, nonNumericNumbersSupported);
        } else {
            return new JacksonJsonReader(new StringReader(jsonString), true, null, jsonString, nonNumericNumbersSupported);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            closeMethod.invoke(jacksonParser);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
    }

    /*
     * Maps the Jackson JsonToken to azure-json JsonToken
     */
    private JsonToken mapToken(Enum<?> token) {
        if (token == null) {
            return null;
        }

        // Check token is Jackson token
        if (token.getClass() != jacksonTokenEnum) {
            throw new IllegalStateException("Unsupported enum, pass a Jackson JsonToken");
        }

    	switch(token.name()) {
            case "START_OBJECT":
                return JsonToken.START_OBJECT;
            case "END_OBJECT":
                return JsonToken.END_OBJECT;

            case "START_ARRAY":
                return JsonToken.START_ARRAY;
            case "END_ARRAY":
                return JsonToken.END_ARRAY;

            case "FIELD_NAME":
                return JsonToken.FIELD_NAME;
            case "VALUE_STRING":
                return JsonToken.STRING;

            case "VALUE_NUMBER_INT":
            case "VALUE_NUMBER_FLOAT":
                return JsonToken.NUMBER;

            case "VALUE_TRUE":
            case "VALUE_FALSE":
                return JsonToken.BOOLEAN;

            case "VALUE_NULL":
                return JsonToken.NULL;

            default:
                throw new IllegalStateException("Unsupported token type: '" + token + "'.");
        }
    }
}
