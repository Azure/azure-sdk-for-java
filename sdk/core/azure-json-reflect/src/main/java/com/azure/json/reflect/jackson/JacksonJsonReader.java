package com.azure.json.reflect.jackson;


import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import static java.lang.invoke.MethodType.methodType;

public class JacksonJsonReader extends JsonReader {
	private static boolean initialized = false;
	private static MethodHandle createParseMethod;
	private static MethodHandle parserCurrentToken;
	private static MethodHandle parserGetBoolean;
	private static MethodHandle parserGetFloatValue;
	private static MethodHandle parserGetDoubleValue;
	private static MethodHandle parserGetIntValue;
	private static MethodHandle parserGetLongValue;
	private static MethodHandle parserGetBinaryValue;
	private static MethodHandle parserNextToken;
	private static MethodHandle parserGetValueAsString;
	private static MethodHandle parserGetCurrentName;
	private static MethodHandle parserSkipChildren;
	private static MethodHandle parserClose;
	private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
	private static Object jsonFactory;
	private static Class<?> jacksonTokenEnum = null;
	private final Object jacksonParser;
	private final boolean resetSupported;
	private final byte[] jsonBytes;
	private final String jsonString;
		
	/**
	 * Constructs an instance of {@link JacksonJsonReader from a {@code byte[]}
	 * 
	 * @param json JSON {@code byte[]}
	 * @return An instance of {@link JacksonJsonReader}
	 */
	public static JsonReader fromBytes(byte[] json) {
		return new JacksonJsonReader(new InputStreamReader(new ByteArrayInputStream(json), StandardCharsets.UTF_8),
	            true, json, null);
	}
	
	/**
	 * Constructs an instance of {@link JacksonJsonReader} from a String
	 * 
	 * @param json JSON String
	 * @return An instance of {@link JacksonJsonReader}
	 */
	public static JsonReader fromString(String json) {
		return new JacksonJsonReader(new StringReader(json), true, null, json);
	}
	
	/**
	 * Constructs an instance of {@link JacksonJsonReader} from an {@link InputStream}
	 * 
	 * @param json JSON {@link InputStream}
	 * @return An instance of {@link JacksonJsonReaader}
	 */
	public static JsonReader fromStream(InputStream json) {
		return new JacksonJsonReader(new InputStreamReader(json, StandardCharsets.UTF_8), false, null, null);
	}
	
    public JacksonJsonReader(Reader reader, boolean resetSupported, byte[] jsonBytes, String jsonString) {
    	if (!initialized) {
			initializeMethodHandles();
    	}
    	try {
    		jacksonParser = createParseMethod.invoke(jsonFactory, reader);
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
			// The jacksonJsonParser is made via the JsonFactory
			Class<?> jacksonJsonFactory = Class.forName("com.fasterxml.jackson.core.JsonFactory");
			// The jacksonJsonParser is the equivalent of the Gson JsonReader
			Class<?> jacksonJsonParser = Class.forName("com.fasterxml.jackson.core.JsonParser");
			
			// Initializing the factory
			MethodHandle jsonFactoryConstructor = publicLookup.findConstructor(jacksonJsonFactory, methodType(void.class));
			createParseMethod = publicLookup.findVirtual(jacksonJsonFactory, "createParser", methodType(jacksonJsonParser, Reader.class));
			jsonFactory = jsonFactoryConstructor.invoke();
			
			jacksonTokenEnum =  Class.forName("com.fasterxml.jackson.core.JsonToken");
			// Initializing all the method handles.
			parserCurrentToken = publicLookup.findVirtual(jacksonJsonParser, "currentToken", methodType(jacksonTokenEnum));
			parserGetBoolean = publicLookup.findVirtual(jacksonJsonParser, "getBooleanValue", methodType(boolean.class));
			parserGetFloatValue = publicLookup.findVirtual(jacksonJsonParser, "getFloatValue", methodType(float.class));
	    	parserGetDoubleValue = publicLookup.findVirtual(jacksonJsonParser, "getDoubleValue", methodType(double.class));
			parserGetIntValue = publicLookup.findVirtual(jacksonJsonParser, "getIntValue", methodType(int.class));
	    	parserGetLongValue = publicLookup.findVirtual(jacksonJsonParser, "getLongValue", methodType(long.class));
			parserGetBinaryValue = publicLookup.findVirtual(jacksonJsonParser, "getBinaryValue", methodType(byte[].class));
	    	parserNextToken = publicLookup.findVirtual(jacksonJsonParser, "nextToken", methodType(jacksonTokenEnum));
			parserGetValueAsString = publicLookup.findVirtual(jacksonJsonParser, "getValueAsString", methodType(String.class));
	    	parserGetCurrentName = publicLookup.findVirtual(jacksonJsonParser, "getCurrentName", methodType(String.class));
			parserSkipChildren = publicLookup.findVirtual(jacksonJsonParser, "skipChildren", methodType(jacksonJsonParser));
	    	parserClose = publicLookup.findVirtual(jacksonJsonParser, "close", methodType(void.class));
	    	// exceptions thrown by forName and findVirtual. 
    	} catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
    		throw new IllegalStateException("Incorrect library present.");
    	} catch (Throwable e) {
    		// ioException is thrown by invoke
    		if (e instanceof IOException ioException) {
    			throw new UncheckedIOException (ioException);
    		} else {
    			throw new RuntimeException(e);
    		}
    	}
	    initialized = true;	
    }

    @Override
    public JsonToken currentToken() {
        try {
			return mapToken((Enum<?>) parserCurrentToken.invoke(jacksonParser));
		} catch (Throwable e) {
			if (e instanceof IOException ioException) {
                throw new UncheckedIOException(ioException);
            } else {
                throw new RuntimeException(e);
            }
		}
    }

    @Override
    public JsonToken nextToken() {
    	try {
			return mapToken((Enum<?>) parserNextToken.invoke(jacksonParser));
		} catch (Throwable e) {
			if (e instanceof IOException ioException) {
                throw new UncheckedIOException(ioException);
			} else {
				throw new RuntimeException(e);
			}
		}
    }

    @Override
    public byte[] getBinary() {
    	try {
    		// GetBinaryValue cannot handle a Null token
    		if (currentToken() == JsonToken.NULL) {
    			return null;
    		}
			return (byte[]) parserGetBinaryValue.invoke(jacksonParser);
		} catch (Throwable e) {
			if (e instanceof IOException ioException) {
                throw new UncheckedIOException(ioException);
			} else {
				throw new RuntimeException(e);
			}
		}
    }

    @Override
    public boolean getBoolean() {
        try {
			return (boolean) parserGetBoolean.invoke(jacksonParser);
		} catch (Throwable e) {
			if (e instanceof IOException ioException) {
                throw new UncheckedIOException(ioException);
            } else {
                throw new RuntimeException(e);
            }
		}
    }

    @Override
    public float getFloat() {
        try {
			return (float) parserGetFloatValue.invoke(jacksonParser);
		} catch (Throwable e) {
			if (e instanceof IOException ioException) {
                throw new UncheckedIOException(ioException);
			} else {
				throw new RuntimeException(e);
			}
		}
    }

    @Override
    public double getDouble() {
    	try {
			return (double) parserGetDoubleValue.invoke(jacksonParser);
		} catch (Throwable e) {
			if (e instanceof IOException ioException) {
                throw new UncheckedIOException(ioException);
			} else {
				throw new RuntimeException(e);
			}
		}
    }

    @Override
    public int getInt() {
    	try {
			return (int) parserGetIntValue.invoke(jacksonParser);
		} catch (Throwable e) {
			if (e instanceof IOException ioException) {
                throw new UncheckedIOException(ioException);
			} else {
				throw new RuntimeException(e);
			}
		}
    }

    @Override
    public long getLong() {
    	try {
			return (long) parserGetLongValue.invoke(jacksonParser);
		} catch (Throwable e) {
			if (e instanceof IOException ioException) {
                throw new UncheckedIOException(ioException);
			} else {
				throw new RuntimeException(e);
			}
		}
    }

    @Override
    public String getString() {
        try {
        	return (String) parserGetValueAsString.invoke(jacksonParser);
        } catch (Throwable e) {
        	if (e instanceof IOException ioException) {
                throw new UncheckedIOException(ioException);
        	} else {
        		throw new RuntimeException(e);
        	}
        }
    }

    @Override
    public String getFieldName() {
    	try {
        	return (String) parserGetCurrentName.invoke(jacksonParser);
        } catch (Throwable e) {
        	if (e instanceof IOException ioException) {
                throw new UncheckedIOException(ioException);
        	} else {
        		throw new RuntimeException(e);
        	}
        }
    }

    @Override
    public void skipChildren() {
    	try {
        	parserSkipChildren.invoke(jacksonParser);
        } catch (Throwable e) {
        	if (e instanceof IOException ioException) {
                throw new UncheckedIOException(ioException);
        	} else {
        		throw new RuntimeException(e);
        	}
        }
    }

    @Override
    public JsonReader bufferObject() throws IOException {
    	StringBuilder bufferedObject = new StringBuilder();
    	if (isStartArrayOrObject()) {
    		// If the current token is the beginning of an array or object, 
    		// use JsonReader's readChildren method.
    		readChildren(bufferedObject);
    	} else if (currentToken() == JsonToken.FIELD_NAME) {
    		// Otherwise, we're in a complex case where the reading needs to be handled.
    		
    		// Add a starting object token.
    		bufferedObject.append("{");
    		
    		JsonToken token = currentToken();
    		boolean needsComa = false;
    		while (token != JsonToken.END_OBJECT) {
    			// Appending commas happens in the subsequent loop run to prevent the case 
    			// of appending commas before the end of the object, e.g. {"fieldName":true,} //NOSONAR
    			if (needsComa) {
    				bufferedObject.append(",");
    			}
    			
    			if (token == JsonToken.FIELD_NAME) {
    				// Field names need to have quotes added and a trailing colon
    				bufferedObject.append("\"").append(getFieldName()).append("\":");
    				
    				// Commas shouldn't happen after a field name.
    				needsComa = false;
    			} else {
    				if (token == JsonToken.STRING) {
    					// String fields need to have quotes added.
    					bufferedObject.append("\"").append(getString()).append("\"");
    				} else if (isStartArrayOrObject()) {
    					// Structures and readChildren.
    					readChildren(bufferedObject);
    				} else {
    					// All other value types use text value.
    					bufferedObject.append(getText());
    				}
    				// Commas should happen after a field value.
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
        return this.resetSupported;
    }

    @Override
    public JsonReader reset() {
        if (!this.resetSupported) {
        	throw new IllegalStateException("'reset' isn't supported by this JsonReader");
        }
        if (jsonBytes != null) {
        	return fromBytes(this.jsonBytes);
        } else {
        	return fromString(this.jsonString);
        }
    }

    @Override
    public void close() throws IOException {
    	try {
    		parserClose.invoke(jacksonParser);
        } catch (Throwable e) {
        	if (e instanceof IOException ioException) {
        		throw new UncheckedIOException (ioException);
        	} else {
        		throw new RuntimeException(e);
        	}
        }
    }
    
    /*
     * Maps the Jackson JsonToken to Azure JsonToken
     * You cannot explicitly cast them
     */
    private JsonToken mapToken(Enum<?> token) {
    	if (token == null) {
    		return null;
    	}
    	
    	if (token.getClass() != jacksonTokenEnum) {
    		throw new IllegalStateException("Unsupported enum, pass a Jackson JsonToken");
    	}
    	
    	// Jackson has tokens which are not directly supported by JsonReader.
    	// However, this mapping seems to work.
    	// The following are Jackson tokens, but not JsonReader tokens.
    	// NOT_AVAILABLE, VALUE_NUMBER_FLOAT, VALUE_NUMBER_INT, 
    	// VALUE_TRUE
    	return switch(token.name()) {
    		case "END_ARRAY" -> JsonToken.END_ARRAY;
    		case "END_OBJECT" -> JsonToken.END_OBJECT;
    		case "FIELD_NAME" -> JsonToken.FIELD_NAME;
    		case "NOT_AVAILABLE" -> JsonToken.NULL;
    		case "START_ARRAY" -> JsonToken.START_ARRAY;
    		case "START_OBJECT" -> JsonToken.START_OBJECT;
    		case "VALUE_FALSE" -> JsonToken.BOOLEAN;
    		case "VALUE_NULL" -> JsonToken.NULL;
    		case "VALUE_NUMBER_FLOAT" -> JsonToken.NUMBER;
    		case "VALUE_NUMBER_INT" -> JsonToken.NUMBER;
    		case "VALUE_STRING" -> JsonToken.STRING;
    		case "VALUE_TRUE" -> JsonToken.BOOLEAN;
    		default -> throw new IllegalStateException("Unsupported token type: '" + token + "'.");
    	};
    }
}
