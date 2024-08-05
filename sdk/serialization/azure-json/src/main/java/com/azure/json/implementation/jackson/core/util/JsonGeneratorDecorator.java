// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.util;

import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonGenerator;

/**
 * Simple interface to allow adding decorators around {@link JsonGenerator}s.
 *
 * @since 2.16
 */
public interface JsonGeneratorDecorator {
    /**
     * Allow to decorate {@link JsonGenerator} instances returned by {@link JsonFactory}.
     * 
     * @since 2.16
     * @param factory The factory which was used to build the original generator
     * @param generator The generator to decorate. This might already be a decorated instance, not the original.
     * @return decorated generator
     */
    JsonGenerator decorate(JsonFactory factory, JsonGenerator generator);
}
