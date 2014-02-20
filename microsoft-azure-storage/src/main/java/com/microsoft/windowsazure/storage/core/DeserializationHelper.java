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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.storage.Constants;

/**
 * RESERVED FOR INTERNAL USE. An internal helper class used to parse objects from responses.
 */
public final class DeserializationHelper {

    private final static XMLInputFactory xmlif = XMLInputFactory.newInstance();

    static {
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        // set the IS_COALESCING property to true , if application desires to
        // get whole text data as one event.
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    }

    /**
     * Reads Metadata from the XMLStreamReader.
     * 
     * @param xmlr
     *            the XMLStreamReader object
     * @return the metadata as a hashmap.
     * @throws XMLStreamException
     *             if there is a parsing exception
     */
    public static HashMap<String, String> parseMetadateFromXML(final XMLStreamReader xmlr) throws XMLStreamException {
        final HashMap<String, String> retVals = new HashMap<String, String>();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.METADATA_ELEMENT);

        int eventType = xmlr.getEventType();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.END_ELEMENT && Constants.METADATA_ELEMENT.equals(name)) {
                break;
            }
            else if (Constants.INVALID_METADATA_NAME.equals(name)) {
                // no op , skip
            }
            else if (eventType == XMLStreamConstants.START_ELEMENT) {
                final String tValue = DeserializationHelper.readElementFromXMLReader(xmlr, name);
                if (!Utility.isNullOrEmpty(tValue)) {
                    retVals.put(name, tValue);
                }
            }
        }

        return retVals;
    }

    /**
     * Creates an XML stream reader from the specified input stream.
     * 
     * @param reader
     *            An <code>InputStreamReader</code> object that represents the input reader to use as the source.
     * 
     * @return A <code>java.xml.stream.XMLStreamReader</code> object that represents the XML stream reader created from
     *         the specified input stream.
     * 
     * @throws XMLStreamException
     *             If the XML stream reader could not be created.
     */
    public static XMLStreamReader createXMLStreamReaderFromReader(final Reader reader) throws XMLStreamException {
        return xmlif.createXMLStreamReader(reader);
    }

    /**
     * Creates an XML stream reader from the specified input stream.
     * 
     * @param streamRef
     *            An <code>InputStream</code> object that represents the input stream to use as the source.
     * 
     * @return A <code>java.xml.stream.XMLStreamReader</code> object that represents the XML stream reader created from
     *         the specified input stream.
     * 
     * @throws XMLStreamException
     *             If the XML stream reader could not be created.
     */
    public static XMLStreamReader createXMLStreamReaderFromStream(final InputStream streamRef)
            throws XMLStreamException {
        return xmlif.createXMLStreamReader(streamRef, Constants.UTF8_CHARSET);
    }

    /**
     * Reads character data for the specified XML element from an XML stream reader. This method will read start events,
     * characters, and end events from a stream.
     * 
     * @param xmlr
     *            An <code>XMLStreamReader</code> object that represents the source XML stream reader.
     * 
     * @param elementName
     *            A <code>String</code> that represents XML element name.
     * 
     * @return A <code>String</code> that represents the character data for the specified element.
     * 
     * @throws XMLStreamException
     *             If an XML stream failure occurs.
     */
    public static String readElementFromXMLReader(final XMLStreamReader xmlr, final String elementName)
            throws XMLStreamException {
        return readElementFromXMLReader(xmlr, elementName, true);
    }

    /**
     * Reads character data for the specified XML element from an XML stream reader. This method will read start events,
     * characters, and end events from a stream.
     * 
     * @param xmlr
     *            An <code>XMLStreamReader</code> object that represents the source XML stream reader.
     * 
     * @param elementName
     *            A <code>String</code> that represents XML element name.
     * @param returnNullOnEmpty
     *            If true, returns null when a empty string is read, otherwise EmptyString ("") is returned.
     * 
     * @return A <code>String</code> that represents the character data for the specified element.
     * 
     * @throws XMLStreamException
     *             If an XML stream failure occurs.
     */
    public static String readElementFromXMLReader(final XMLStreamReader xmlr, final String elementName,
            boolean returnNullOnEmpty) throws XMLStreamException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, elementName);
        int eventType = xmlr.next();
        final StringBuilder retVal = new StringBuilder();

        if (eventType == XMLStreamConstants.CHARACTERS) {
            // This do while is in case the XMLStreamReader does not have
            // the IS_COALESCING property set
            // to true which may result in text being read in multiple events
            // If we ensure all xmlreaders have this property we can optimize
            // the StringBuilder and while loop
            // away
            do {
                retVal.append(xmlr.getText());
                eventType = xmlr.next();

            } while (eventType == XMLStreamConstants.CHARACTERS);
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, elementName);
        if (retVal.length() == 0) {
            return returnNullOnEmpty ? null : Constants.EMPTY_STRING;
        }
        else {
            return retVal.toString();
        }
    }

    /**
     * Reads character data for the Etag element from an XML stream reader.
     * 
     * @param xmlr
     *            An <code>XMLStreamReader</code> object that represents the source XML stream reader.
     * 
     * @return A <code>String</code> that represents the character data for the Etag element.
     * 
     * @throws XMLStreamException
     *             If an XML stream failure occurs.
     */
    public static String readETagFromXMLReader(final XMLStreamReader xmlr) throws XMLStreamException {
        String etag = readElementFromXMLReader(xmlr, Constants.ETAG_ELEMENT, true);
        if (etag.startsWith("\"") && etag.endsWith("\"")) {
            return etag;
        }
        else {
            return String.format("\"%s\"", etag);
        }
    }

    /**
     * Private Default Ctor
     */
    private DeserializationHelper() {
        // No op
    }
}
