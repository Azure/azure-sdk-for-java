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
	private static Class<?> jacksonTokenEnum = null;
	private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
	private static Object jsonFactory;
	
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
		
		jacksonTokenEnum = Class.forName("com.fasterxml.jackson.core.JsonToken");
		parserCurrentToken = publicLookup.findVirtual(jacksonJsonParser, "currentToken", methodType(jacksonTokenEnum));
		parserGetBoolean = publicLookup.findVirtual(jacksonJsonParser, "getBooleanValue", methodType(boolean.class));
    	initialized = true;
    }

    @Override
    public JsonToken currentToken() {
        try {
			return (JsonToken) parserCurrentToken.invoke(jacksonParser);
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
        return null;
    }

    @Override
    public byte[] getBinary() {
        return new byte[0];
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
        return 0;
    }

    @Override
    public double getDouble() {
        return 0;
    }

    @Override
    public int getInt() {
        return 0;
    }

    @Override
    public long getLong() {
        return 0;
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
}
