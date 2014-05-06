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

package com.microsoft.azure.storage.table;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;
import com.microsoft.azure.storage.core.SR;

/***
 * RESERVED FOR INTERNAL USE. A class to help parse the error details from an input stream, specific to tables
 */
public final class TableStorageErrorDeserializer {

    /**
     * Gets the Extended Error information.
     * 
     * @return the Extended Error information.
     * 
     * @param reader
     *            the input stream to read error details from.
     * @param format
     *            The {@link TablePayloadFormat} to use for parsing
     * @throws XMLStreamException
     *             if an error occurs while accessing the stream with AtomPub.
     * @throws IOException
     *             if an error occurs while accessing the stream with Json.
     * @throws JsonParseException
     *             if an error occurs while parsing the stream.
     */
    @SuppressWarnings("deprecation")
    public static StorageExtendedErrorInformation getExtendedErrorInformation(final Reader reader,
            final TablePayloadFormat format) throws JsonParseException, IOException, XMLStreamException {
        if (format == TablePayloadFormat.AtomPub) {
            XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromReader(reader);
            try {
                return parseAtomResponse(xmlr);
            }
            finally {
                xmlr.close();
            }
        }
        else {
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser parser = jsonFactory.createParser(reader);
            try {
                return parseJsonResponse(parser);
            }
            finally {
                parser.close();
            }
        }
    }

    /**
     * Parses the error exception details from the Json-formatted response.
     * 
     * @param parser
     *            the {@link JsonParser} to use for parsing
     * @throws IOException
     *             if an error occurs while accessing the stream with Json.
     * @throws JsonParseException
     *             if an error occurs while parsing the stream.
     */
    private static HashMap<String, String[]> parseJsonErrorException(JsonParser parser) throws JsonParseException,
            IOException {
        HashMap<String, String[]> additionalDetails = new HashMap<String, String[]>();

        parser.nextToken();
        ODataUtilities.assertIsStartObjectJsonToken(parser);

        parser.nextToken();
        ODataUtilities.assertIsFieldNameJsonToken(parser);

        while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentName().equals(TableConstants.ErrorConstants.ERROR_MESSAGE)) {
                parser.nextToken();
                additionalDetails.put(TableConstants.ErrorConstants.ERROR_MESSAGE,
                        new String[] { parser.getValueAsString() });
            }
            else if (parser.getCurrentName().equals(TableConstants.ErrorConstants.ERROR_EXCEPTION_TYPE)) {
                parser.nextToken();
                additionalDetails.put(TableConstants.ErrorConstants.ERROR_EXCEPTION_TYPE,
                        new String[] { parser.getValueAsString() });
            }
            else if (parser.getCurrentName().equals(TableConstants.ErrorConstants.ERROR_EXCEPTION_STACK_TRACE)) {
                parser.nextToken();
                additionalDetails
                        .put(Constants.ERROR_EXCEPTION_STACK_TRACE, new String[] { parser.getValueAsString() });
            }
            parser.nextToken();
        }

        return additionalDetails;
    }

    /**
     * Parses the extended error information from the Json-formatted response.
     * 
     * @throws IOException
     *             if an error occurs while accessing the stream with Json.
     * @throws JsonParseException
     *             if an error occurs while parsing the stream.
     */
    private static StorageExtendedErrorInformation parseJsonResponse(JsonParser parser) throws JsonParseException,
            IOException {
        final StorageExtendedErrorInformation errorInfo = new StorageExtendedErrorInformation();

        if (!parser.hasCurrentToken()) {
            parser.nextToken();
        }

        ODataUtilities.assertIsStartObjectJsonToken(parser);

        parser.nextToken();
        ODataUtilities.assertIsFieldNameJsonToken(parser);
        ODataUtilities.assertIsExpectedFieldName(parser, "odata.error");

        // start getting extended error information
        parser.nextToken();
        ODataUtilities.assertIsStartObjectJsonToken(parser);

        // get code
        parser.nextValue();
        ODataUtilities.assertIsExpectedFieldName(parser, TableConstants.ErrorConstants.ERROR_CODE);
        errorInfo.setErrorCode(parser.getValueAsString());

        // get message
        parser.nextToken();
        ODataUtilities.assertIsFieldNameJsonToken(parser);
        ODataUtilities.assertIsExpectedFieldName(parser, TableConstants.ErrorConstants.ERROR_MESSAGE);

        parser.nextToken();
        ODataUtilities.assertIsStartObjectJsonToken(parser);

        parser.nextValue();
        ODataUtilities.assertIsExpectedFieldName(parser, "lang");

        parser.nextValue();
        ODataUtilities.assertIsExpectedFieldName(parser, "value");
        errorInfo.setErrorMessage(parser.getValueAsString());

        parser.nextToken();
        ODataUtilities.assertIsEndObjectJsonToken(parser);

        parser.nextToken();

        // get innererror if it exists
        if (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
            ODataUtilities.assertIsExpectedFieldName(parser, TableConstants.ErrorConstants.INNER_ERROR);
            errorInfo.getAdditionalDetails().putAll(parseJsonErrorException(parser));
            parser.nextToken();
        }

        // end code object
        ODataUtilities.assertIsEndObjectJsonToken(parser);

        // end odata.error object
        parser.nextToken();
        ODataUtilities.assertIsEndObjectJsonToken(parser);

        return errorInfo;
    }

    /**
     * Parses the error exception details from the AtomPub-formatted response.
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @throws XMLStreamException
     *             if an xml exception occurs
     */
    private static HashMap<String, String[]> parseAtomErrorException(XMLStreamReader xmlr) throws XMLStreamException {
        int eventType = xmlr.getEventType();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, TableConstants.ErrorConstants.INNER_ERROR);

        HashMap<String, String[]> additionalDetails = new HashMap<String, String[]>();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.CHARACTERS) {
                continue;
            }

            final String name = xmlr.getName().getLocalPart().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT
                    && name.equals(TableConstants.ErrorConstants.ERROR_MESSAGE)) {
                final String errorExceptionMessage = DeserializationHelper.readElementFromXMLReader(xmlr, name);
                additionalDetails.put(Constants.ERROR_EXCEPTION_MESSAGE, new String[] { errorExceptionMessage });

            }
            else if (eventType == XMLStreamConstants.START_ELEMENT
                    && name.equals(TableConstants.ErrorConstants.ERROR_EXCEPTION_STACK_TRACE)) {
                final String errorExceptionStack = DeserializationHelper.readElementFromXMLReader(xmlr, name);
                additionalDetails.put(Constants.ERROR_EXCEPTION_STACK_TRACE, new String[] { errorExceptionStack });
            }
            else if (eventType == XMLStreamConstants.START_ELEMENT
                    && name.equals(TableConstants.ErrorConstants.ERROR_EXCEPTION_TYPE)) {
                final String errorExceptionStack = DeserializationHelper.readElementFromXMLReader(xmlr, name);
                additionalDetails.put(TableConstants.ErrorConstants.ERROR_EXCEPTION_TYPE,
                        new String[] { errorExceptionStack });
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT) {
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, null);

        return additionalDetails;
    }

    /**
     * Parses the extended error information from the AtomPub-formatted response.
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @throws XMLStreamException
     *             if an xml exception occurs
     */
    private static StorageExtendedErrorInformation parseAtomResponse(XMLStreamReader xmlr) throws XMLStreamException {
        final StorageExtendedErrorInformation errorInfo = new StorageExtendedErrorInformation();

        String tempParseString;

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // 1. get Error Root Header
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, null);
        if (!xmlr.getName().getLocalPart().toString().equals(TableConstants.ErrorConstants.ERROR_ROOT_ELEMENT)) {
            throw new XMLStreamException(SR.EXPECTED_START_ELEMENT_TO_EQUAL_ERROR);
        }

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.CHARACTERS) {
                continue;
            }

            if (eventType == XMLStreamConstants.END_ELEMENT) {
                break;
            }

            String name = xmlr.getName().getLocalPart().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT) {

                if (name.equals(TableConstants.ErrorConstants.ERROR_CODE)) {
                    errorInfo.setErrorCode(DeserializationHelper.readElementFromXMLReader(xmlr, name));
                }
                else if (name.equals(TableConstants.ErrorConstants.ERROR_MESSAGE)) {
                    errorInfo.setErrorMessage(DeserializationHelper.readElementFromXMLReader(xmlr, name));
                }
                else if (name.equals(TableConstants.ErrorConstants.INNER_ERROR)) {
                    // get error exception
                    errorInfo.getAdditionalDetails().putAll(parseAtomErrorException(xmlr));
                }
                else {
                    // get additional details
                    tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr, name);

                    errorInfo.getAdditionalDetails().put(name, new String[] { tempParseString });

                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, null);
                }
            }
        }
        return errorInfo;
    }
}
