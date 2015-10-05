/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/***
 * RESERVED FOR INTERNAL USE. A class to hold utility methods for parsing OData payloads
 */
public final class JsonUtilities {
    /***
     * Reserved for internal use. Asserts that the current name of the parser equals the expected value
     * 
     * @param parser
     *            The {@link JsonParser} whose current token to check.
     * @param expectedValue
     *            The expected current name of the parser's current token.
     */
    public static void assertIsExpectedFieldName(final JsonParser parser, final String expectedValue)
            throws JsonParseException, IOException {
        final String actualValue = parser.getCurrentName();
        if (expectedValue == null) {
            if (actualValue != null) {
                throw new JsonParseException(String.format(SR.UNEXPECTED_FIELD_NAME, expectedValue, actualValue),
                        parser.getCurrentLocation());
            }
        }
        else {
            if (!expectedValue.equals(actualValue)) {
                throw new JsonParseException(String.format(SR.UNEXPECTED_FIELD_NAME, expectedValue, actualValue),
                        parser.getCurrentLocation());
            }
        }
    }

    /***
     * Reserved for internal use. Asserts that the current token of the parser is a field name.
     * 
     * @param parser
     *            The {@link JsonParser} whose current token to check.
     */
    public static void assertIsFieldNameJsonToken(final JsonParser parser) throws JsonParseException {
        if (!(parser.getCurrentToken() == JsonToken.FIELD_NAME)) {
            throw new JsonParseException(SR.EXPECTED_A_FIELD_NAME, parser.getCurrentLocation());
        }
    }

    /***
     * Reserved for internal use. Asserts that the current token of the parser is the start of an object.
     * 
     * @param parser
     *            The {@link JsonParser} whose current token to check.
     */
    public static void assertIsStartObjectJsonToken(final JsonParser parser) throws JsonParseException {
        if (!(parser.getCurrentToken() == JsonToken.START_OBJECT)) {
            throw new JsonParseException(SR.EXPECTED_START_OBJECT, parser.getCurrentLocation());
        }
    }

    /***
     * Reserved for internal use. Asserts that the current token of the parser is the end of an object.
     * 
     * @param parser
     *            The {@link JsonParser} whose current token to check.
     */
    public static void assertIsEndObjectJsonToken(final JsonParser parser) throws JsonParseException {
        if (!(parser.getCurrentToken() == JsonToken.END_OBJECT)) {
            throw new JsonParseException(SR.EXPECTED_END_OBJECT, parser.getCurrentLocation());
        }
    }

    /***
     * Reserved for internal use. Asserts that the token type of the parser is the start of an array.
     * 
     * @param parser
     *            The {@link JsonParser} whose current token to check.
     */
    public static void assertIsStartArrayJsonToken(final JsonParser parser) throws JsonParseException {
        if (!(parser.getCurrentToken() == JsonToken.START_ARRAY)) {
            throw new JsonParseException(SR.EXPECTED_START_ARRAY, parser.getCurrentLocation());
        }
    }

    /***
     * Reserved for internal use. Asserts that the token type of the parser is the end of an array.
     * 
     * @param parser
     *            The {@link JsonParser} whose current token to check.
     */
    public static void assertIsEndArrayJsonToken(final JsonParser parser) throws JsonParseException {
        if (!(parser.getCurrentToken() == JsonToken.END_ARRAY)) {
            throw new JsonParseException(SR.EXPECTED_END_ARRAY, parser.getCurrentLocation());
        }
    }
}
