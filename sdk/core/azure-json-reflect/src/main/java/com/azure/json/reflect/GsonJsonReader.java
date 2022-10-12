package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;

class GsonJsonReader extends JsonReader {
    private static boolean initialized = false;
    private static boolean attemptedInitialization = false;
    private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
    private static Class<?> gsonTokenEnum = null;

    private final Object gsonReader;
    private JsonToken currentToken;

    private static MethodHandle gsonReaderConstructor;
    private static MethodHandle setLenientMethod;
    private static MethodHandle peekMethod;
    private static MethodHandle closeMethod;
    private static MethodHandle beginObjectMethod;
    private static MethodHandle endObjectMethod;
    private static MethodHandle beginArrayMethod;
    private static MethodHandle endArrayMethod;
    private static MethodHandle nextNullMethod;
    private static MethodHandle nextBooleanMethod;
    private static MethodHandle nextStringMethod;
    private static MethodHandle nextDoubleMethod;
    private static MethodHandle nextIntMethod;
    private static MethodHandle nextLongMethod;
    private static MethodHandle nextNameMethod;
    private static MethodHandle skipValueMethod;

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

            gsonReader = gsonReaderConstructor.invoke(reader);
            setLenientMethod.invoke(gsonReader, nonNumericNumbersSupported);
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new IllegalStateException("Gson is not present or an incorrect version is present.");
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
            throw new ReflectiveOperationException("Initialization of GsonJsonReader has failed in the past.");
        }

        attemptedInitialization = true;

        gsonTokenEnum = Class.forName("com.google.gson.stream.JsonToken");
        Class<?> gsonReaderClass = Class.forName("com.google.gson.stream.JsonReader");

        MethodType voidMT = methodType(void.class);
        gsonReaderConstructor = publicLookup.findConstructor(gsonReaderClass, methodType(void.class, Reader.class));
        setLenientMethod = publicLookup.findVirtual(gsonReaderClass, "setLenient", methodType(void.class, boolean.class));
        peekMethod = publicLookup.findVirtual(gsonReaderClass, "peek", methodType(gsonTokenEnum));
        closeMethod = publicLookup.findVirtual(gsonReaderClass, "close", voidMT);
        beginObjectMethod = publicLookup.findVirtual(gsonReaderClass, "beginObject", voidMT);
        endObjectMethod = publicLookup.findVirtual(gsonReaderClass, "endObject", voidMT);
        beginArrayMethod = publicLookup.findVirtual(gsonReaderClass, "beginArray", voidMT);
        endArrayMethod = publicLookup.findVirtual(gsonReaderClass, "endArray", voidMT);
        nextNullMethod = publicLookup.findVirtual(gsonReaderClass, "nextNull", voidMT);
        nextBooleanMethod = publicLookup.findVirtual(gsonReaderClass, "nextBoolean", methodType(boolean.class));
        nextStringMethod = publicLookup.findVirtual(gsonReaderClass, "nextString", methodType(String.class));
        nextDoubleMethod = publicLookup.findVirtual(gsonReaderClass, "nextDouble", methodType(double.class));
        nextIntMethod = publicLookup.findVirtual(gsonReaderClass, "nextInt", methodType(int.class));
        nextLongMethod = publicLookup.findVirtual(gsonReaderClass, "nextLong", methodType(long.class));
        nextNameMethod = publicLookup.findVirtual(gsonReaderClass, "nextName", methodType(String.class));
        skipValueMethod = publicLookup.findVirtual(gsonReaderClass, "skipValue", voidMT);

        initialized = true;
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
        try {
            if (currentToken == JsonToken.START_OBJECT) {
                beginObjectMethod.invoke(gsonReader);
            } else if (currentToken == JsonToken.END_OBJECT) {
                endObjectMethod.invoke(gsonReader);
            } else if (currentToken == JsonToken.START_ARRAY) {
                beginArrayMethod.invoke(gsonReader);
            } else if (currentToken == JsonToken.END_ARRAY) {
                endArrayMethod.invoke(gsonReader);
            } else if (currentToken == JsonToken.NULL) {
                nextNullMethod.invoke(gsonReader);
            }

            if (!consumed && currentToken != null) {
                switch (currentToken) {
                    case FIELD_NAME:
                        nextNameMethod.invoke(gsonReader);
                        break;

                    case BOOLEAN:
                        nextBooleanMethod.invoke(gsonReader);
                        break;

                    case NUMBER:
                        nextDoubleMethod.invoke(gsonReader);
                        break;

                    case STRING:
                        nextStringMethod.invoke(gsonReader);
                        break;

                    default:
                        break;
                }
            }

            currentToken = mapToken((Enum<?>) peekMethod.invoke(gsonReader));

            if (currentToken == JsonToken.END_DOCUMENT) {
                complete = true;
            }

            consumed = false;
            return currentToken;

        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
    }

    @Override
    public byte[] getBinary() throws IOException {
        consumed = true;

        try {
            if (currentToken == JsonToken.NULL) {
                nextNullMethod.invoke(gsonReader);
                return null;
            } else {
                return Base64.getDecoder().decode((String) nextStringMethod.invoke(gsonReader));
            }
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
        consumed = true;

        try {
            return (boolean) nextBooleanMethod.invoke(gsonReader);
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
        consumed = true;

        try {
            return (float) (double) nextDoubleMethod.invoke(gsonReader);
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
        consumed = true;

        try {
            return (double) nextDoubleMethod.invoke(gsonReader);
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
        consumed = true;

        try {
            return (int) nextIntMethod.invoke(gsonReader);
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
        consumed = true;

        try {
            return (long) nextLongMethod.invoke(gsonReader);
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
        consumed = true;

        try {
            if (currentToken == JsonToken.NULL) {
                return null;
            } else {
                return (String) nextStringMethod.invoke(gsonReader);
            }
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
        consumed = true;

        try {
            return (String) nextNameMethod.invoke(gsonReader);
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
        consumed = true;

        try {
            skipValueMethod.invoke(gsonReader);
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
        try {
            closeMethod.invoke(gsonReader);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw (RuntimeException) e.getCause();
            }
        }
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
        if (token.getClass() != gsonTokenEnum) {
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
}
