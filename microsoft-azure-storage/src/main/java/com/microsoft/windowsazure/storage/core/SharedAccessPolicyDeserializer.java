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
public final class SharedAccessPolicyDeserializer {

    /**
     * Gets the HashMap of SharedAccessPolicies from the response.
     * 
     * @param stream
     *            the stream to read from
     * @param cls
     *            the <code>SharedAccessPolicy</code> class type
     * @return the HashMap of SharedAccessPolicies from the response
     * @throws XMLStreamException
     *             if an XMLStreamException occurs.
     * @throws ParseException
     *             if a date is incorrectly encoded in the stream
     * @throws InstantiationException
     *             the class cls cannot be instantiated
     * @throws IllegalAccessException
     *             the class cls cannot be instantiated
     */
    public static <T extends SharedAccessPolicy> HashMap<String, T> getAccessIdentifiers(final InputStream stream,
            final Class<T> cls) throws XMLStreamException, ParseException, InstantiationException,
            IllegalAccessException {
        final XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromStream(stream);

        final HashMap<String, T> policies = new HashMap<String, T>();

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();

                if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.SIGNED_IDENTIFIERS_ELEMENT)) {
                    readPolicies(xmlr, policies, cls);
                }
            }
        }

        return policies;
    }

    /**
     * Reads all the policies from the XMLStreamReader
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @param cls
     *            the <code>SharedAccessPolicy</code> class type
     * @param policies
     *            the HashMap of SharedAccessPolicies to add to
     * @throws XMLStreamException
     *             if an XMLStreamException occurs.
     * @throws ParseException
     *             if a date is incorrectly encoded in the stream
     * @throws InstantiationException
     *             the class cls cannot be instantiated
     * @throws IllegalAccessException
     *             the class cls cannot be instantiated
     */
    private static <T extends SharedAccessPolicy> void readPolicies(final XMLStreamReader xmlr,
            HashMap<String, T> policies, final Class<T> cls) throws XMLStreamException, ParseException,
            InstantiationException, IllegalAccessException {
        int eventType = xmlr.getEventType();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.SIGNED_IDENTIFIERS_ELEMENT);

        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();

                if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.SIGNED_IDENTIFIER_ELEMENT)) {
                    readSignedIdentifier(xmlr, policies, cls);
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
     * @param cls
     *            the <code>SharedAccessPolicy</code> class type
     * @param policies
     *            the HashMap of SharedAccessPolicies to add to
     * @throws XMLStreamException
     *             if an XMLStreamException occurs.
     * @throws ParseException
     *             if a date is incorrectly encoded in the stream
     * @throws InstantiationException
     *             the class cls cannot be instantiated
     * @throws IllegalAccessException
     *             the class cls cannot be instantiated
     */
    private static <T extends SharedAccessPolicy> void readSignedIdentifier(final XMLStreamReader xmlr,
            HashMap<String, T> policies, final Class<T> cls) throws XMLStreamException, ParseException,
            InstantiationException, IllegalAccessException {
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.SIGNED_IDENTIFIER_ELEMENT);

        String id = null;
        T policy = null;
        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();

                if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.ID)) {
                    id = DeserializationHelper.readElementFromXMLReader(xmlr, Constants.ID);
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.ACCESS_POLICY)) {
                    policy = readPolicyFromXML(xmlr, cls);
                }
                else if (eventType == XMLStreamConstants.END_ELEMENT
                        && name.equals(Constants.SIGNED_IDENTIFIER_ELEMENT)) {
                    policies.put(id, policy);
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
     *            the XMLStreamReader to read from
     * @param cls
     *            the <code>SharedAccessPolicy</code> class type
     * @return the <code>SharedAccessPolicy</code> of class cls
     * @throws XMLStreamException
     *             if an XMLStreamException occurs.
     * @throws ParseException
     *             if a date is incorrectly encoded in the stream
     * @throws InstantiationException
     *             the class cls cannot be instantiated
     * @throws IllegalAccessException
     *             the class cls cannot be instantiated
     */
    private static <T extends SharedAccessPolicy> T readPolicyFromXML(final XMLStreamReader xmlr, final Class<T> cls)
            throws XMLStreamException, ParseException, InstantiationException, IllegalAccessException {
        int eventType = xmlr.getEventType();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.ACCESS_POLICY);

        final T retPolicy = cls.newInstance();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();

                if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.PERMISSION)) {
                    retPolicy.setPermissionsFromString(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.PERMISSION));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.START)) {
                    final String tempString = DeserializationHelper.readElementFromXMLReader(xmlr, Constants.START);
                    retPolicy.setSharedAccessStartTime(Utility.parseISO8061LongDateFromString(tempString));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.EXPIRY)) {
                    final String tempString = DeserializationHelper.readElementFromXMLReader(xmlr, Constants.EXPIRY);
                    retPolicy.setSharedAccessExpiryTime(Utility.parseISO8061LongDateFromString(tempString));
                }
                else if (eventType == XMLStreamConstants.END_ELEMENT && name.equals(Constants.ACCESS_POLICY)) {
                    break;
                }
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.ACCESS_POLICY);
        return retPolicy;
    }
}
