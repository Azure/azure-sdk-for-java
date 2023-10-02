// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.typespec.json.implementation.jackson.core.*;

/**
 * Helper class that implements
 * <a href="http://en.wikipedia.org/wiki/Delegation_pattern">delegation pattern</a> for {@link JsonParser},
 * to allow for simple overridability of basic parsing functionality.
 * The idea is that any functionality to be modified can be simply
 * overridden; and anything else will be delegated by default.
 */
public class JsonParserDelegate extends JsonParser
{
    /**
     * Delegate object that method calls are delegated to.
     */
    protected JsonParser delegate;

    public JsonParserDelegate(JsonParser d) {
        delegate = d;
    }

    /*
    /**********************************************************************
    /* Public API, configuration
    /**********************************************************************
     */

    @Override public void setCodec(ObjectCodec c) { delegate.setCodec(c); }
    @Override public ObjectCodec getCodec() { return delegate.getCodec(); }

    @Override
    public JsonParser enable(Feature f) {
        delegate.enable(f);
        return this;
    }

    @Override
    public JsonParser disable(Feature f) {
        delegate.disable(f);
        return this;
    }
 
    @Override public boolean isEnabled(Feature f) { return delegate.isEnabled(f); }
    @Override public int getFeatureMask() { return delegate.getFeatureMask(); }

    @Override
    @Deprecated // since 2.7
    public JsonParser setFeatureMask(int mask) {
        delegate.setFeatureMask(mask);
        return this;
    }

    @Override
    public JsonParser overrideStdFeatures(int values, int mask) {
        delegate.overrideStdFeatures(values, mask);
        return this;
    }

    @Override
    public JsonParser overrideFormatFeatures(int values, int mask) {
        delegate.overrideFormatFeatures(values, mask);
        return this;
    }

    @Override public FormatSchema getSchema() { return delegate.getSchema(); }
    @Override public void setSchema(FormatSchema schema) { delegate.setSchema(schema); }
    @Override public boolean canUseSchema(FormatSchema schema) {  return delegate.canUseSchema(schema); }
    @Override public Version version() { return delegate.version(); }
    @Override public Object getInputSource() { return delegate.getInputSource(); }

    /*
    /**********************************************************************
    /* Capability introspection
    /**********************************************************************
     */

    @Override public boolean requiresCustomCodec() { return delegate.requiresCustomCodec(); }

    @Override public JacksonFeatureSet<StreamReadCapability> getReadCapabilities() { return delegate.getReadCapabilities(); }

    /*
    /**********************************************************************
    /* Closeable impl
    /**********************************************************************
     */

    @Override public void close() throws IOException { delegate.close(); }
    @Override public boolean isClosed() { return delegate.isClosed(); }

    /*
    /**********************************************************************
    /* Public API, state change/override methods
    /**********************************************************************
     */

    @Override public void clearCurrentToken() { delegate.clearCurrentToken(); }
    @Override public JsonToken getLastClearedToken() { return delegate.getLastClearedToken(); }
    @Override public void overrideCurrentName(String name) { delegate.overrideCurrentName(name); }

    @Override // since 2.13
    public void assignCurrentValue(Object v) { delegate.assignCurrentValue(v); }

    // TODO: deprecate in 2.14 or later
    @Override
    public void setCurrentValue(Object v) { delegate.setCurrentValue(v); }

    /*
    /**********************************************************************
    /* Public API, state/location accessors
    /**********************************************************************
     */

    @Override public JsonStreamContext getParsingContext() { return delegate.getParsingContext(); }

    @Override public JsonToken currentToken() { return delegate.currentToken(); }
    @Override public int currentTokenId() { return delegate.currentTokenId(); }
    @Override public String currentName() throws IOException { return delegate.currentName(); }
    @Override // since 2.13
    public Object currentValue() { return delegate.currentValue(); }

    @Override // since 2.13
    public JsonLocation currentLocation() { return delegate.getCurrentLocation(); }
    @Override // since 2.13
    public JsonLocation currentTokenLocation() { return delegate.getTokenLocation(); }

    // TODO: deprecate in 2.14 or later
    @Override public JsonToken getCurrentToken() { return delegate.getCurrentToken(); }
    @Deprecated // since 2.12
    @Override public int getCurrentTokenId() { return delegate.getCurrentTokenId(); }
    // TODO: deprecate in 2.14 or later
    @Override public String getCurrentName() throws IOException { return delegate.getCurrentName(); }
    // TODO: deprecate in 2.14 or later
    @Override public Object getCurrentValue() { return delegate.getCurrentValue(); }

    // TODO: deprecate in 2.14 or later
    @Override public JsonLocation getCurrentLocation() { return delegate.getCurrentLocation(); }
    // TODO: deprecate in 2.14 or later
    @Override public JsonLocation getTokenLocation() { return delegate.getTokenLocation(); }

    /*
    /**********************************************************************
    /* Public API, token accessors
    /**********************************************************************
     */

    @Override public boolean hasCurrentToken() { return delegate.hasCurrentToken(); }
    @Override public boolean hasTokenId(int id) { return delegate.hasTokenId(id); }
    @Override public boolean hasToken(JsonToken t) { return delegate.hasToken(t); }

    @Override public boolean isExpectedStartArrayToken() { return delegate.isExpectedStartArrayToken(); }
    @Override public boolean isExpectedStartObjectToken() { return delegate.isExpectedStartObjectToken(); }
    @Override public boolean isExpectedNumberIntToken() { return delegate.isExpectedNumberIntToken(); }

    @Override public boolean isNaN() throws IOException { return delegate.isNaN(); }

    /*
    /**********************************************************************
    /* Public API, access to token textual content
    /**********************************************************************
     */

    @Override public String getText() throws IOException { return delegate.getText();  }
    @Override public boolean hasTextCharacters() { return delegate.hasTextCharacters(); }
    @Override public char[] getTextCharacters() throws IOException { return delegate.getTextCharacters(); }
    @Override public int getTextLength() throws IOException { return delegate.getTextLength(); }
    @Override public int getTextOffset() throws IOException { return delegate.getTextOffset(); }
    @Override public int getText(Writer writer) throws IOException, UnsupportedOperationException { return delegate.getText(writer);  }

    /*
    /**********************************************************************
    /* Public API, access to token numeric values
    /**********************************************************************
     */
    
    @Override
    public BigInteger getBigIntegerValue() throws IOException { return delegate.getBigIntegerValue(); }

    @Override
    public boolean getBooleanValue() throws IOException { return delegate.getBooleanValue(); }
    
    @Override
    public byte getByteValue() throws IOException { return delegate.getByteValue(); }

    @Override
    public short getShortValue() throws IOException { return delegate.getShortValue(); }

    @Override
    public BigDecimal getDecimalValue() throws IOException { return delegate.getDecimalValue(); }

    @Override
    public double getDoubleValue() throws IOException { return delegate.getDoubleValue(); }

    @Override
    public float getFloatValue() throws IOException { return delegate.getFloatValue(); }

    @Override
    public int getIntValue() throws IOException { return delegate.getIntValue(); }

    @Override
    public long getLongValue() throws IOException { return delegate.getLongValue(); }

    @Override
    public NumberType getNumberType() throws IOException { return delegate.getNumberType(); }

    @Override
    public Number getNumberValue() throws IOException { return delegate.getNumberValue(); }

    @Override
    public Number getNumberValueExact() throws IOException { return delegate.getNumberValueExact(); }

    /*
    /**********************************************************************
    /* Public API, access to token information, coercion/conversion
    /**********************************************************************
     */

    @Override public int getValueAsInt() throws IOException { return delegate.getValueAsInt(); }
    @Override public int getValueAsInt(int defaultValue) throws IOException { return delegate.getValueAsInt(defaultValue); }
    @Override public long getValueAsLong() throws IOException { return delegate.getValueAsLong(); }
    @Override public long getValueAsLong(long defaultValue) throws IOException { return delegate.getValueAsLong(defaultValue); }
    @Override public double getValueAsDouble() throws IOException { return delegate.getValueAsDouble(); }
    @Override public double getValueAsDouble(double defaultValue) throws IOException { return delegate.getValueAsDouble(defaultValue); }
    @Override public boolean getValueAsBoolean() throws IOException { return delegate.getValueAsBoolean(); }
    @Override public boolean getValueAsBoolean(boolean defaultValue) throws IOException { return delegate.getValueAsBoolean(defaultValue); }
    @Override public String getValueAsString() throws IOException { return delegate.getValueAsString(); }
    @Override public String getValueAsString(String defaultValue) throws IOException { return delegate.getValueAsString(defaultValue); }

    /*
    /**********************************************************************
    /* Public API, access to token values, other
    /**********************************************************************
     */

    @Override public Object getEmbeddedObject() throws IOException { return delegate.getEmbeddedObject(); }
    @Override public byte[] getBinaryValue(Base64Variant b64variant) throws IOException { return delegate.getBinaryValue(b64variant); }
    @Override public int readBinaryValue(Base64Variant b64variant, OutputStream out) throws IOException { return delegate.readBinaryValue(b64variant, out); }

    @Override public JsonToken nextToken() throws IOException { return delegate.nextToken(); }

    @Override public JsonToken nextValue() throws IOException { return delegate.nextValue(); }

    @Override public void finishToken() throws IOException { delegate.finishToken(); }
    
    @Override
    public JsonParser skipChildren() throws IOException {
        delegate.skipChildren();
        // NOTE: must NOT delegate this method to delegate, needs to be self-reference for chaining
        return this;
    }

    /*
    /**********************************************************************
    /* Public API, Native Ids (type, object)
    /**********************************************************************
     */

    @Override public boolean canReadObjectId() { return delegate.canReadObjectId(); }
    @Override public boolean canReadTypeId() { return delegate.canReadTypeId(); }
    @Override public Object getObjectId() throws IOException { return delegate.getObjectId(); }
    @Override public Object getTypeId() throws IOException { return delegate.getTypeId(); }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    /**
     * Accessor for getting the immediate {@link JsonParser} this parser delegates calls to.
     *
     * @return Underlying parser calls are delegated to
     *
     * @since 2.10
     */
    public JsonParser delegate() { return delegate; }
}
