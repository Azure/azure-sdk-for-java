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
    private static MethodHandle peek;
    private static MethodHandle close;
    private static MethodHandle beginObject;
    private static MethodHandle endObject;
    private static MethodHandle beginArray;
    private static MethodHandle endArray;
    private static MethodHandle nextNull;
    private static MethodHandle nextBoolean;
    private static MethodHandle nextString;
    private static MethodHandle nextDouble;
    private static MethodHandle nextInt;
    private static MethodHandle nextLong;
    private static MethodHandle nextName;
    private static MethodHandle skipValue;

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
        if (!initialized) {
            initializeMethodHandles();
        }

        try {
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

    private static void initializeMethodHandles() {
        try {
            gsonTokenEnum = Class.forName("com.google.gson.stream.JsonToken");
            Class<?> gsonReaderClass = Class.forName("com.google.gson.stream.JsonReader");

            MethodType voidMT = methodType(void.class);
            gsonReaderConstructor = publicLookup.findConstructor(gsonReaderClass, methodType(void.class, Reader.class));
            peek = publicLookup.findVirtual(gsonReaderClass, "peek", methodType(gsonTokenEnum));
            close = publicLookup.findVirtual(gsonReaderClass, "close", voidMT);
            beginObject = publicLookup.findVirtual(gsonReaderClass, "beginObject", voidMT);
            endObject = publicLookup.findVirtual(gsonReaderClass, "endObject", voidMT);
            beginArray = publicLookup.findVirtual(gsonReaderClass, "beginArray", voidMT);
            endArray = publicLookup.findVirtual(gsonReaderClass, "endArray", voidMT);
            nextNull = publicLookup.findVirtual(gsonReaderClass, "nextNull", voidMT);
            nextBoolean = publicLookup.findVirtual(gsonReaderClass, "nextBoolean", methodType(boolean.class));
            nextString = publicLookup.findVirtual(gsonReaderClass, "nextString", methodType(String.class));
            nextDouble = publicLookup.findVirtual(gsonReaderClass, "nextDouble", methodType(double.class));
            nextInt = publicLookup.findVirtual(gsonReaderClass, "nextInt", methodType(int.class));
            nextLong = publicLookup.findVirtual(gsonReaderClass, "nextLong", methodType(long.class));
            nextName = publicLookup.findVirtual(gsonReaderClass, "nextName", methodType(String.class));
            skipValue = publicLookup.findVirtual(gsonReaderClass, "skipValue", voidMT);
        } catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException e) {
            throw new IllegalStateException("Incorrect library present.");
        }

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
                beginObject.invoke(gsonReader);
            } else if (currentToken == JsonToken.END_OBJECT) {
                endObject.invoke(gsonReader);
            } else if (currentToken == JsonToken.START_ARRAY) {
                beginArray.invoke(gsonReader);
            } else if (currentToken == JsonToken.END_ARRAY) {
                endArray.invoke(gsonReader);
            } else if (currentToken == JsonToken.NULL) {
                nextNull.invoke(gsonReader);
            }

            currentToken = mapToken((Enum<?>) peek.invoke(gsonReader));
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
                nextNull.invoke(gsonReader);
                return null;
            } else {
                return Base64.getDecoder().decode((String) nextString.invoke(gsonReader));
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
            return (boolean) nextBoolean.invoke(gsonReader);
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
            return (float) (double) nextDouble.invoke(gsonReader);
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
            return (double) nextDouble.invoke(gsonReader);
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
            return (int) nextInt.invoke(gsonReader);
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
            return (long) nextLong.invoke(gsonReader);
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
                return (String) nextString.invoke(gsonReader);
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
            return (String) nextName.invoke(gsonReader);
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
            skipValue.invoke(gsonReader);
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
            close.invoke();
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
