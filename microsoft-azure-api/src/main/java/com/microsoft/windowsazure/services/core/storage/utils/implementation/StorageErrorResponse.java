/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.core.storage.utils.implementation;

import java.io.InputStream;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.StorageExtendedErrorInformation;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/***
 * RESERVED FOR INTERNAL USE. A class to help parse the Error details from an input stream.
 */
public final class StorageErrorResponse {
    /**
     * Holds the StorageExtendedErrorInformation to return.
     */
    private final StorageExtendedErrorInformation errorInfo;

    /**
     * Holds a flag indicating if the response has been parsed or not.
     */
    private boolean isParsed;

    /**
     * Holds a reference to the input stream to read from.
     */
    private final InputStream streamRef;

    /**
     * Initializes the StorageErrorResponse object.
     * 
     * @param stream
     *            the input stream to read error details from.
     */
    public StorageErrorResponse(final InputStream stream) {
        this.streamRef = stream;
        this.errorInfo = new StorageExtendedErrorInformation();
    }

    /**
     * Gets the Extended Error information from the response stream.
     * 
     * @return the Extended Error information from the response stream
     * @throws XMLStreamException
     *             if an xml exception occurs
     */
    public StorageExtendedErrorInformation getExtendedErrorInformation() throws XMLStreamException {
        if (!this.isParsed) {
            this.parseResponse();
        }

        return this.errorInfo;
    }

    /**
     * Parses the Error Exception details from the response.
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @throws XMLStreamException
     *             if an xml exception occurs
     */
    private void parseErrorException(final XMLStreamReader xmlr) throws XMLStreamException {
        int eventType = xmlr.getEventType();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.ERROR_EXCEPTION);

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.ERROR_EXCEPTION_MESSAGE)) {
                final String errorExceptionMessage = Utility.readElementFromXMLReader(xmlr,
                        Constants.ERROR_EXCEPTION_MESSAGE);
                this.errorInfo.getAdditionalDetails().put(Constants.ERROR_EXCEPTION_MESSAGE,
                        new String[] { errorExceptionMessage });

            }
            else if (eventType == XMLStreamConstants.START_ELEMENT
                    && name.equals(Constants.ERROR_EXCEPTION_STACK_TRACE)) {
                final String errorExceptionStack = Utility.readElementFromXMLReader(xmlr,
                        Constants.ERROR_EXCEPTION_STACK_TRACE);
                this.errorInfo.getAdditionalDetails().put(Constants.ERROR_EXCEPTION_STACK_TRACE,
                        new String[] { errorExceptionStack });
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT) {
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.ERROR_EXCEPTION);
    }

    /**
     * Parses the extended error information from the response stream.
     * 
     * @throws XMLStreamException
     *             if an xml exception occurs
     */
    private void parseResponse() throws XMLStreamException {
        final XMLStreamReader xmlr = Utility.createXMLStreamReaderFromStream(this.streamRef);
        String tempParseString;

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // 1. get Error Root Header
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.ERROR_ROOT_ELEMENT);

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.END_ELEMENT) {
                break;
            }

            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.ERROR_CODE)) {
                this.errorInfo.setErrorCode(Utility.readElementFromXMLReader(xmlr, Constants.ERROR_CODE));
            }
            else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.ERROR_MESSAGE)) {
                this.errorInfo.setErrorMessage(Utility.readElementFromXMLReader(xmlr, Constants.ERROR_MESSAGE));
            }
            else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.ERROR_EXCEPTION)) {
                // get error exception
                this.parseErrorException(xmlr);
                xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.ERROR_EXCEPTION);
            }
            else if (eventType == XMLStreamConstants.START_ELEMENT) {
                // get additional details
                tempParseString = Utility.readElementFromXMLReader(xmlr, name);

                this.errorInfo.getAdditionalDetails().put(name, new String[] { tempParseString });

                xmlr.require(XMLStreamConstants.END_ELEMENT, null, null);
            }

        }
    }
}
