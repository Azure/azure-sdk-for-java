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
import static java.lang.invoke.MethodType.methodType;

public class JacksonJsonReader extends JsonReader {
	private static boolean initialized = false;
    private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
    private static Class<?> jacksonTokenEnum = null;
    // Wildcard cannot be used here, otherwise the class cannot be cast to an enum.
    private static Class<Enum> jacksonReadFeatureEnum = null;
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
    private static MethodHandle enableFeatureMethod;

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
            if (!initialized) {
                initialize();
            }
            // Support for cases such as Inf
            if (nonNumericNumbersSupported) {
                // unfortunately, you can't pass features when creating a parser
                Object temp = createParserMethod.invoke(jsonFactory, reader);
                jacksonParser = enableFeatureMethod.invoke(temp, Enum.valueOf(jacksonReadFeatureEnum, "ALLOW_NON_NUMERIC_NUMBERS"));
            } else {
                jacksonParser = createParserMethod.invoke(jsonFactory, reader);
            }
    	} catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e.getCause();
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new IllegalStateException("Incorrect Library Present");
            }
    	}
        this.resetSupported = resetSupported;
    	this.jsonBytes = jsonBytes;
    	this.jsonString = jsonString;
        this.nonNumericNumbersSupported = nonNumericNumbersSupported;

    }

    static void initialize() throws ReflectiveOperationException {
        jacksonTokenEnum =  Class.forName("com.fasterxml.jackson.core.JsonToken");

		// The jacksonJsonParser is made via the JsonFactory
		Class<?> jacksonJsonFactory = Class.forName("com.fasterxml.jackson.core.JsonFactory");
		// The jacksonJsonParser is the equivalent of the Gson JsonReader

		Class<?> jacksonJsonParser = Class.forName("com.fasterxml.jackson.core.JsonParser");

        // JsonParser.Feature is a nested class.
        // This is the easiest way to access nested classes reflectively.
        // NOTE: JsonParser.Feature does not work.
        for (Class i: jacksonJsonParser.getDeclaredClasses()) {
            if (i.getSimpleName().equals("Feature")){
                jacksonReadFeatureEnum = i;
                break;
            }
        }

    	// Initializing the factory
        try {
            jsonFactory = publicLookup.findConstructor(jacksonJsonFactory, methodType(void.class)).invoke();
        } catch (Throwable e) {
            throw (RuntimeException) e.getCause();
        }

        // Initializing all the method handles.
        createParserMethod = publicLookup.findVirtual(jacksonJsonFactory, "createParser", methodType(jacksonJsonParser, Reader.class));
        enableFeatureMethod = publicLookup.findVirtual(jacksonJsonParser, "enable", methodType(jacksonJsonParser, jacksonReadFeatureEnum));
		getBooleanMethod = publicLookup.findVirtual(jacksonJsonParser, "getBooleanValue", methodType(boolean.class));
		getFloatValueMethod = publicLookup.findVirtual(jacksonJsonParser, "getFloatValue", methodType(float.class));
    	getDoubleValueMethod = publicLookup.findVirtual(jacksonJsonParser, "getDoubleValue", methodType(double.class));
		getIntValueMethod = publicLookup.findVirtual(jacksonJsonParser, "getIntValue", methodType(int.class));
    	getLongValueMethod = publicLookup.findVirtual(jacksonJsonParser, "getLongValue", methodType(long.class));
		getBinaryValueMethod = publicLookup.findVirtual(jacksonJsonParser, "getBinaryValue", methodType(byte[].class));
    	nextTokenMethod = publicLookup.findVirtual(jacksonJsonParser, "nextToken", methodType(jacksonTokenEnum));
		getValueAsStringMethod = publicLookup.findVirtual(jacksonJsonParser, "getValueAsString", methodType(String.class));
    	currentNameMethod = publicLookup.findVirtual(jacksonJsonParser, "currentName", methodType(String.class));
		skipChildrenMethod = publicLookup.findVirtual(jacksonJsonParser, "skipChildren", methodType(jacksonJsonParser));
    	closeMethod = publicLookup.findVirtual(jacksonJsonParser, "close", methodType(void.class));

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

    	return switch(token.name()) {
            case "START_OBJECT" -> JsonToken.START_OBJECT;
            case "END_OBJECT" -> JsonToken.END_OBJECT;
            case "START_ARRAY" -> JsonToken.START_ARRAY;
    		case "END_ARRAY" -> JsonToken.END_ARRAY;
    		case "FIELD_NAME" -> JsonToken.FIELD_NAME;
            case "VALUE_STRING" -> JsonToken.STRING;
            case "VALUE_NUMBER_INT", "VALUE_NUMBER_FLOAT" -> JsonToken.NUMBER;
    		case "VALUE_TRUE", "VALUE_FALSE" -> JsonToken.BOOLEAN;
            case "VALUE_NULL" -> JsonToken.NULL;
            default -> throw new IllegalStateException("Unsupported token type: '" + token + "'.");
    	};
    }
}
