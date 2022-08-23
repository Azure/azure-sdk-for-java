package com.azure.json.reflect.jackson;


import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import static java.lang.invoke.MethodType.methodType;

public class JacksonJsonReader extends JsonReader {
	private static boolean initialized = false;
	private static MethodHandle createParseMethod;
	private static MethodHandle jsonFactoryConstructor;
	private static MethodHandle parserCurrentToken;
	private static MethodHandle parserGetBoolean;
	private static MethodHandle parserGetFloatValue;
	private static MethodHandle parserGetDoubleValue;
	private static MethodHandle parserGetIntValue;
	private static MethodHandle parserGetLongValue;
	private static MethodHandle parserGetBinaryValue;
	private static MethodHandle parserNextToken;
	private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
	private static Object jsonFactory;
	private static Class<?> jacksonTokenEnum = null;
	
	private final Object jacksonParser;
	
    public JacksonJsonReader(Reader reader) {
    	if (!initialized) {
    		try {
				initializeMethodHandles();
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
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
    }
    
    private static void initializeMethodHandles() throws ReflectiveOperationException{
		// The jacksonJsonParser is made via the JsonFactory
		Class<?> jacksonJsonFactory = Class.forName("com.fasterxml.jackson.core.JsonFactory");
		// The jacksonJsonParser is the equivalent of the Gson JsonReader
		Class<?> jacksonJsonParser = Class.forName("com.fasterxml.jackson.core.JsonParser");
		
		jsonFactoryConstructor = publicLookup.findConstructor(jacksonJsonFactory, methodType(void.class));
		createParseMethod = publicLookup.findVirtual(jacksonJsonFactory, "createParser", methodType(jacksonJsonParser, Reader.class));
		try {
			jsonFactory = jsonFactoryConstructor.invoke();
		} catch (Throwable e) {
			if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
            } else {
                throw new RuntimeException(e);
            }
		}
		
		jacksonTokenEnum =  Class.forName("com.fasterxml.jackson.core.JsonToken");
		parserCurrentToken = publicLookup.findVirtual(jacksonJsonParser, "currentToken", methodType(jacksonTokenEnum));
		parserGetBoolean = publicLookup.findVirtual(jacksonJsonParser, "getBooleanValue", methodType(boolean.class));
		parserGetFloatValue = publicLookup.findVirtual(jacksonJsonParser, "getFloatValue", methodType(float.class));
    	parserGetDoubleValue = publicLookup.findVirtual(jacksonJsonParser, "getDoubleValue", methodType(double.class));
		parserGetIntValue = publicLookup.findVirtual(jacksonJsonParser, "getIntValue", methodType(int.class));
    	parserGetLongValue = publicLookup.findVirtual(jacksonJsonParser, "getLongValue", methodType(long.class));
		parserGetBinaryValue = publicLookup.findVirtual(jacksonJsonParser, "getBinaryValue", methodType(byte[].class));
    	parserNextToken = publicLookup.findVirtual(jacksonJsonParser, "nextToken", methodType(jacksonTokenEnum));
		initialized = true;	
    }

    @Override
    public JsonToken currentToken() {
        try {
			return mapToken((Enum<?>) parserCurrentToken.invoke(jacksonParser));
		} catch (Throwable e) {
			if (e instanceof IOException) {
                throw new UncheckedIOException((IOException) e);
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
			if (e instanceof IOException) {
				throw new UncheckedIOException ((IOException) e);
			} else {
				throw new RuntimeException(e);
			}
		}
    }

    @Override
    public byte[] getBinary() {
    	try {
			return (byte[]) parserGetBinaryValue.invoke(jacksonParser);
		} catch (Throwable e) {
			if (e instanceof IOException) {
				throw new UncheckedIOException ((IOException) e);
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
			return (float) parserGetFloatValue.invoke(jacksonParser);
		} catch (Throwable e) {
			if (e instanceof IOException) {
				throw new UncheckedIOException ((IOException) e);
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
			if (e instanceof IOException) {
				throw new UncheckedIOException ((IOException) e);
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
			if (e instanceof IOException) {
				throw new UncheckedIOException ((IOException) e);
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
			if (e instanceof IOException) {
				throw new UncheckedIOException ((IOException) e);
			} else {
				throw new RuntimeException(e);
			}
		}
    }

    @Override
    public String getString() {
        return null;
    }

    @Override
    public String getFieldName() {
        return null;
    }

    @Override
    public void skipChildren() {

    }

    @Override
    public JsonReader bufferObject() {
        return null;
    }

    @Override
    public boolean resetSupported() {
        return false;
    }

    @Override
    public JsonReader reset() {
        return null;
    }

    @Override
    public void close() throws IOException {

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
