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

package com.microsoft.windowsazure.storage.core;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.StorageExtendedErrorInformation;

/***
 * RESERVED FOR INTERNAL USE. A class to help parse the Error details from an input stream.
 */
public final class StorageErrorDeserializer {

    /**
     * Gets the Extended Error information from the response stream.
     * 
     * @return the Extended Error information from the response stream
     * @throws XMLStreamException
     *             if an xml exception occurs
     */
    public static StorageExtendedErrorInformation getExtendedErrorInformation(final Reader reader)
            throws XMLStreamException {
        final XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromReader(reader);
        return parseResponse(xmlr);
    }

    /**
     * Gets the Extended Error information from the response stream.
     * 
     * @return the Extended Error information from the response stream
     * @throws XMLStreamException
     *             if an xml exception occurs
     */
    public static StorageExtendedErrorInformation getExtendedErrorInformation(final InputStream stream)
            throws XMLStreamException {
        final XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromStream(stream);
        return parseResponse(xmlr);
    }

    /**
     * Parses the extended error information from the response stream.
     * 
     * @throws XMLStreamException
     *             if an xml exception occurs
     */
    private static StorageExtendedErrorInformation parseResponse(final XMLStreamReader xmlr) throws XMLStreamException {
        final StorageExtendedErrorInformation errorInfo = new StorageExtendedErrorInformation();

        String tempParseString;

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // 1. get Error Root Header
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.ERROR_ROOT_ELEMENT);

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

                if (name.equals(Constants.ERROR_CODE)) {
                    errorInfo.setErrorCode(DeserializationHelper.readElementFromXMLReader(xmlr, Constants.ERROR_CODE));
                }
                else if (name.equals(Constants.ERROR_MESSAGE)) {
                    errorInfo.setErrorMessage(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.ERROR_MESSAGE));
                }
                else if (name.equals(Constants.ERROR_EXCEPTION)) {
                    // get error exception
                    errorInfo.getAdditionalDetails().putAll(parseErrorException(xmlr));
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.ERROR_EXCEPTION);
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

    /**
     * Parses the Error Exception details from the response.
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @throws XMLStreamException
     *             if an xml exception occurs
     */
    private static HashMap<String, String[]> parseErrorException(final XMLStreamReader xmlr) throws XMLStreamException {
        int eventType = xmlr.getEventType();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.ERROR_EXCEPTION);

        HashMap<String, String[]> additionalDetails = new HashMap<String, String[]>();
        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.CHARACTERS) {
                continue;
            }

            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.ERROR_EXCEPTION_MESSAGE)) {
                final String errorExceptionMessage = DeserializationHelper.readElementFromXMLReader(xmlr,
                        Constants.ERROR_EXCEPTION_MESSAGE);
                additionalDetails.put(Constants.ERROR_EXCEPTION_MESSAGE, new String[] { errorExceptionMessage });

            }
            else if (eventType == XMLStreamConstants.START_ELEMENT
                    && name.equals(Constants.ERROR_EXCEPTION_STACK_TRACE)) {
                final String errorExceptionStack = DeserializationHelper.readElementFromXMLReader(xmlr,
                        Constants.ERROR_EXCEPTION_STACK_TRACE);
                additionalDetails.put(Constants.ERROR_EXCEPTION_STACK_TRACE, new String[] { errorExceptionStack });
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT) {
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.ERROR_EXCEPTION);

        return additionalDetails;
    }

}