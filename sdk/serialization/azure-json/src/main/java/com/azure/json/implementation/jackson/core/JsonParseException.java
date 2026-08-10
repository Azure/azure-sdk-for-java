// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/*
 * Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.exc.StreamReadException;

/**
 * Exception type for parsing problems, used when non-well-formed content
 * (content that does not conform to JSON syntax as per specification)
 * is encountered.
 */
public class JsonParseException extends StreamReadException {
    private static final long serialVersionUID = 2L; // 2.7

    /**
     * Constructor that uses current parsing location as location.
     *
     * @param p Parser in use when encountering issue reported
     * @param msg Base exception message to use
     *
     * @since 2.7
     */
    public JsonParseException(JsonParser p, String msg) {
        super(p, msg);
    }

    // @since 2.7
    public JsonParseException(JsonParser p, String msg, Throwable root) {
        super(p, msg, root);
    }
}
