package com.azure.json.reflect.gson;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static java.lang.invoke.MethodType.methodType;

public class GsonJsonReader extends JsonReader {
    private static boolean initialized = false;
    private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
    private static Class<?> gsonTokenEnum = null;

    private static MethodHandle gsonReaderConstructor;
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

    private final Object gsonReader;
    private JsonToken currentToken;

    /**
     * Constructs an instance of {@link GsonJsonReader} from a {@code byte[]}.
     *
     * @param json JSON {@code byte[]}.
     * @return An instance of {@link GsonJsonReader}.
     */
    public static JsonReader fromBytes(byte[] json) {
        return new GsonJsonReader(new InputStreamReader(new ByteArrayInputStream(json), StandardCharsets.UTF_8),
            true, json, null);
    }

    /**
     * Constructs an instance of {@link GsonJsonReader} from a String.
     *
     * @param json JSON String.
     * @return An instance of {@link GsonJsonReader}.
     */
    public static JsonReader fromString(String json) {
        return new GsonJsonReader(new StringReader(json), true, null, json);
    }

    /**
     * Constructs an instance of {@link GsonJsonReader} from an {@link InputStream}.
     *
     * @param json JSON {@link InputStream}.
     * @return An instance of {@link GsonJsonReader}.
     */
    public static JsonReader fromStream(InputStream json) {
        return new GsonJsonReader(new InputStreamReader(json, StandardCharsets.UTF_8), false, null, null);
    }

    private GsonJsonReader(Reader reader, boolean resetSupported, byte[] jsonBytes, String jsonString) {
        try {
            if (!initialized) {
                initialize();
            }
            gsonReader = gsonReaderConstructor.invoke(reader);
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw new RuntimeException(e);
            } else {
                throw new IllegalStateException("Incorrect library present.");
            }
        }

        this.resetSupported = resetSupported;
        this.jsonBytes = jsonBytes;
        this.jsonString = jsonString;
    }

    static void initialize() throws ReflectiveOperationException {
        gsonTokenEnum = Class.forName("com.google.gson.stream.JsonToken");
        Class<?> gsonReaderClass = Class.forName("com.google.gson.stream.JsonReader");

        MethodType voidMT = methodType(void.class);
        gsonReaderConstructor = publicLookup.findConstructor(gsonReaderClass, methodType(void.class, Reader.class));
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
    public JsonToken nextToken() {
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

            currentToken = mapToken((Enum<?>) peekMethod.invoke(gsonReader));
            return currentToken;
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public byte[] getBinary() {
        try {
            if (currentToken == JsonToken.NULL) {
                nextNullMethod.invoke(gsonReader);
                return null;
            } else {
                return Base64.getDecoder().decode((String) nextStringMethod.invoke(gsonReader));
            }
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean getBoolean() {
        try {
            return (boolean) nextBooleanMethod.invoke(gsonReader);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public float getFloat() {
        try {
            return (float) (double) nextDoubleMethod.invoke(gsonReader);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public double getDouble() {
        try {
            return (double) nextDoubleMethod.invoke(gsonReader);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getInt() {
        try {
            return (int) nextIntMethod.invoke(gsonReader);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public long getLong() {
        try {
            return (long) nextLongMethod.invoke(gsonReader);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getString() {
        try {
            if (currentToken == JsonToken.NULL) {
                return null;
            } else {
                return (String) nextStringMethod.invoke(gsonReader);
            }
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getFieldName() {
        try {
            return (String) nextNameMethod.invoke(gsonReader);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void skipChildren() {
        try {
            skipValueMethod.invoke(gsonReader);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public JsonReader bufferObject() {
        StringBuilder bufferedObject = new StringBuilder();
        if (isStartArrayOrObject()) {
            // If the current token is the beginning of an array or object use JsonReader's readChildren method.
            readChildren(bufferedObject);
        } else if (currentToken() == JsonToken.FIELD_NAME) {
            // Otherwise, we're in a complex case where the reading needs to be handled.

            // Add a starting object token.
            bufferedObject.append("{");

            JsonToken token = currentToken();
            boolean needsComa = false;
            while (token != JsonToken.END_OBJECT) {
                // Appending comas happens in the subsequent loop run to prevent the case of appending comas before
                // the end of the object, ex {"fieldName":true,}
                if (needsComa) {
                    bufferedObject.append(",");
                }

                if (token == JsonToken.FIELD_NAME) {
                    // Field names need to have quotes added and a trailing colon.
                    bufferedObject.append("\"").append(getFieldName()).append("\":");

                    // Comas shouldn't happen after a field name.
                    needsComa = false;
                } else {
                    if (token == JsonToken.STRING) {
                        // String fields need to have quotes added.
                        bufferedObject.append("\"").append(getString()).append("\"");
                    } else if (isStartArrayOrObject()) {
                        // Structures use readChildren.
                        readChildren(bufferedObject);
                    } else {
                        // All other value types use text value.
                        bufferedObject.append(getText());
                    }

                    // Comas should happen after a field value.
                    needsComa = true;
                }

                token = nextToken();
            }

            bufferedObject.append("}");
        } else {
            throw new IllegalStateException("Cannot buffer a JSON object from a non-object, non-field name "
                + "starting location. Starting location: " + currentToken());
        }

        return fromString(bufferedObject.toString());
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
            return fromBytes(jsonBytes);
        } else {
            return fromString(jsonString);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            closeMethod.invoke();
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
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

        return switch (token.name()) {
            case "BEGIN_OBJECT" -> JsonToken.START_OBJECT;
            case "END_OBJECT", "END_DOCUMENT" -> JsonToken.END_OBJECT;
            case "BEGIN_ARRAY" -> JsonToken.START_ARRAY;
            case "END_ARRAY" -> JsonToken.END_ARRAY;
            case "NAME" -> JsonToken.FIELD_NAME;
            case "STRING" -> JsonToken.STRING;
            case "NUMBER" -> JsonToken.NUMBER;
            case "BOOLEAN" -> JsonToken.BOOLEAN;
            case "NULL" -> JsonToken.NULL;
            default -> throw new IllegalStateException("Unsupported token type: '" + token + "'.");
        };
    }
}
