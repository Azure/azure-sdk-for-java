// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.io;

import com.azure.json.implementation.jackson.core.JsonParseException;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.JsonToken;

/**
 * Specialized {@link JsonParseException} that is thrown when end-of-input
 * is reached unexpectedly, either within token being decoded, or during
 * skipping of intervening white-space that is not between root-level
 * tokens (that is, is within JSON Object or JSON Array construct).
 *
 * @since 2.8
 */
public class JsonEOFException extends JsonParseException {
    private static final long serialVersionUID = 1L;

    /**
     * Type of token that was being decoded, if parser had enough information
     * to recognize type (such as starting double-quote for Strings)
     */
    protected final JsonToken _token;

    public JsonEOFException(JsonParser p, JsonToken token, String msg) {
        super(p, msg);
        _token = token;
    }

}
