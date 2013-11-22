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
import java.text.ParseException;
import java.util.HashMap;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.storage.Constants;

/**
 * RESERVED FOR INTERNAL USE. A class used to parse SharedAccessPolicies from an input stream.
 */
public abstract class AccessPolicyResponseBase<T> {
    /**
     * Holds a flag indicating if the response has been parsed or not.
     */
    private boolean isParsed;

    /**
     * Holds the Hashmap of policies parsed from the stream
     */
    private final HashMap<String, T> policies = new HashMap<String, T>();

    /**
     * Holds a reference to the input stream to read from.
     */
    private final InputStream streamRef;

    /**
     * Initializes the AccessPolicyResponse object
     * 
     * @param stream
     *            the input stream to read error details from.
     */
    public AccessPolicyResponseBase(final InputStream stream) {
        this.streamRef = stream;
    }

    /**
     * Gets the HashMap of SharedAccessPolicies from the response
     * 
     * @return the HashMap of SharedAccessPolicies from the response
     * @throws XMLStreamException
     *             if an XMLStreamException occurs.
     * @throws ParseException
     *             if a date is incorrectly encoded in the stream
     */
    public HashMap<String, T> getAccessIdentifiers() throws XMLStreamException, ParseException {
        if (!this.isParsed) {
            this.parseResponse();
        }

        return this.policies;
    }

    /**
     * Parses the response for the Shared Access Policies
     * 
     * @throws XMLStreamException
     *             if an XMLStreamException occurs.
     * @throws ParseException
     *             if a date is incorrectly encoded in the stream
     */
    private void parseResponse() throws XMLStreamException, ParseException {
        final XMLStreamReader xmlr = Utility.createXMLStreamReaderFromStream(this.streamRef);

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();

                if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.SIGNED_IDENTIFIERS_ELEMENT)) {
                    this.readPolicies(xmlr);
                    break;
                }
            }
        }

        this.isParsed = true;
    }

    /**
     * Reads all the policies from the XMLStreamReader
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @throws XMLStreamException
     *             if an XMLStreamException occurs.
     * @throws ParseException
     *             if a date is incorrectly encoded in the stream
     */
    private void readPolicies(final XMLStreamReader xmlr) throws XMLStreamException, ParseException {
        int eventType = xmlr.getEventType();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.SIGNED_IDENTIFIERS_ELEMENT);

        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();

                if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.SIGNED_IDENTIFIER_ELEMENT)) {
                    this.readSignedIdentifier(xmlr);
                }
                else if (eventType == XMLStreamConstants.END_ELEMENT
                        && name.equals(Constants.SIGNED_IDENTIFIERS_ELEMENT)) {
                    break;
                }
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.SIGNED_IDENTIFIERS_ELEMENT);
    }

    /**
     * Reads a policy identifier / Shared Access Policy pair from the stream
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @throws XMLStreamException
     *             if an XMLStreamException occurs.
     * @throws ParseException
     *             if a date is incorrectly encoded in the stream
     */
    private void readSignedIdentifier(final XMLStreamReader xmlr) throws XMLStreamException, ParseException {
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.SIGNED_IDENTIFIER_ELEMENT);

        String id = null;
        T policy = null;
        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();

                if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.ID)) {
                    id = Utility.readElementFromXMLReader(xmlr, Constants.ID);
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.ACCESS_POLICY)) {
                    policy = this.readPolicyFromXML(xmlr);
                }
                else if (eventType == XMLStreamConstants.END_ELEMENT
                        && name.equals(Constants.SIGNED_IDENTIFIER_ELEMENT)) {
                    this.policies.put(id, policy);
                    break;
                }
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.SIGNED_IDENTIFIER_ELEMENT);
    }

    /**
     * Populates the object from the XMLStreamReader, reader must be at Start element of AccessPolicy.
     * 
     * @param xmlr
     *            the XMLStreamReader object
     * @throws XMLStreamException
     *             if there is a parsing exception
     * @throws ParseException
     *             if a date value is not correctly encoded
     */
    protected abstract T readPolicyFromXML(final XMLStreamReader xmlr) throws XMLStreamException, ParseException;
}
