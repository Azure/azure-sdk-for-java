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
	private static MethodHandle jacksonFactoryConstructor;
	private static MethodHandle jacksonJsonParserInstance;
	private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
	
	private final Object jacksonParser;
	
    public JacksonJsonReader(Reader reader) {
    	if (!initialized) {
    		initializeMethodHandles();
    	}
    	try {
    		jacksonParser = jacksonJsonParserInstance.invoke(reader);
    	} catch (Throwable e) {
    		if (e instanceof RuntimeException) {
    			throw new RuntimeException(e);
    		} else {
    			throw new IllegalStateException("Incorrect library present.");
    		}
    	}
    	
    }
    
    private void initializeMethodHandles() {
    	try {
    		// The jacksonJsonParser is made via the JsonFactory
    		Class<?> jacksonJsonFactory = Class.forName("com.fasterxml.jackson.core.JsonFactory");
    		// The jacksonJsonParser is the equivalent of the Gson JasonReader
    		Class<?> jacksonJsonParser = Class.forName("com.fasterxml.jackson.core.JsonParser");
    		
    		jacksonFactoryConstructor = publicLookup.findConstructor(jacksonJsonFactory, methodType(void.class));
    		jacksonJsonParserInstance = publicLookup.findVirtual(jacksonJsonFactory, "JsonParser", jacksonJsonParser);
    		
    	} catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException e) {
    		throw new IllegalStateException("Incorrect library present.");
    	}
    	
    	initialized = true;
    }

    @Override
    public JsonToken currentToken() {
        return null;
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
        return false;
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
