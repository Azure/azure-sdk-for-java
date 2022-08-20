package com.azure.json.reflect.jackson;


import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class JacksonJsonReader extends JsonReader {
	private static boolean initialized = false;
	private static MethodHandle createParseMethod;
	private static MethodHandle jsonFactoryConstructor;
	private static MethodHandle parserCurrentToken;
	private static MethodHandle parserGetBoolean;
	private static Class<?> jacksonTokenEnum = null;
	private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
	private static final Object jsonFactory;
	
	private final Object jacksonParser;
	
    public JacksonJsonReader(Reader reader) {
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
    }
    
    public static void initializeMethodHandles() throws ReflectiveOperationException{
		// The jacksonJsonParser is made via the JsonFactory
		Class<?> jacksonJsonFactory = Class.forName("com.fasterxml.jackson.core.JsonFactory");
		// The jacksonJsonParser is the equivalent of the Gson JasonReader
		Class<?> jacksonJsonParser = Class.forName("com.fasterxml.jackson.core.JsonParser");
		
		jsonFactoryConstructor = publicLookup.findConstructor(jacksonJsonFactory, methodType(void.class));
		createParseMethod = publicLookup.findVirtual(jacksonJsonFactory, "createParser", methodType(jacksonJsonParser));
		jsonFactory = jsonFactoryConstructor.invoke();
		
		jacksonTokenEnum = Class.forName("com.fasterxml.jackson.core.JsonToken");
		parserCurrentToken = publicLookup.findVirtual(jacksonJsonParser, "currentToken", methodType(jacksonTokenEnum));
		parserGetBoolean = publicLookup.findVirtual(jacksonJsonParser, "getBooleanValue", methodType(boolean.class));
    	initialized = true;
    }

    @Override
    public JsonToken currentToken() {
        return parserCurrentToken.invoke(jacksonParser);
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
        return parserGetBoolean.invoke(jacksonParser);
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
