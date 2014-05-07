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

import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.azure.storage.Constants;

/**
 * RESERVED FOR INTERNAL USE. An internal helper class used to parse objects from responses.
 */
final class DeserializationHelper {
    // see use below for more explanation
    private final static String ENTITY_EXPANSION_EXCEPTION_MESSAGE = "(.|\n)*Message: JAXP00010001: The parser has encountered more than \"\\d+\" entity expansions in this document; this is the limit imposed by the JDK\\.";

    private static XMLInputFactory xmlif;

    static {
        setupXMLInputFactory();
    }

    /**
     * Sets this class's <code>XMLInputFactory</code> instance to a new factory instance and sets the appropriate
     * properties on it. This is not synchronized so multiple threads executing this will cause multiple factories to be
     * created, but this is okay as it does not matter for the reader which factory creates it as they are functionally
     * identical.
     */
    public static void setupXMLInputFactory() {
        final XMLInputFactory xmlif = XMLInputFactory.newInstance();
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        // set the IS_COALESCING property to true , if application desires to
        // get whole text data as one event.
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        DeserializationHelper.xmlif = xmlif;
    }

    /**
     * Tests whether the provided exception is an entity expansion exception.
     * 
     * Some versions of Java cause an exception to be incorrectly thrown when more XMLStreamReaders have been
     * created by a single factory than the entity expansion limit property (default 64000). The default form of this
     * exception is: "ParseError at [row,col]:[1,1]\n
     * Message: JAXP00010001: The parser has encountered more than
     * \"64000\" entity expansions in this document; this is the limit imposed by the JDK."
     * 
     * @param e
     *            the <code>XMLStreamException</code> to test
     * @return
     *         true if entity expansion exception, false otherwise
     */
    public static boolean isEntityExpansionLimitException(XMLStreamException e) {
        return e.getMessage() != null && e.getMessage().matches(ENTITY_EXPANSION_EXCEPTION_MESSAGE);
    }

    /**
     * Creates an XML stream reader from the specified input stream.
     * 
     * Some versions of Java cause an exception to be incorrectly thrown when more XMLStreamReaders have been
     * created by a single factory than the entity expansion limit property (default 64000). This model catches
     * that exception, creates a new factory with the appropriate settings, and then retries the reader creation.
     * 
     * @param reader
     *            An <code>InputStreamReader</code> object that represents the input reader to use as the source.
     * 
     * @return A <code>java.xml.stream.XMLStreamReader</code> object that represents the XML stream reader created from
     *         the specified input stream.
     * 
     * @throws XMLStreamException
     *             If the XML stream reader could not be created.
     * @see <a
     *      href="https://bugs.openjdk.java.net/browse/JDK-8028111">https://bugs.openjdk.java.net/browse/JDK-8028111</a>
     */
    public static XMLStreamReader createXMLStreamReaderFromReader(final Reader reader) throws XMLStreamException {
        while (true) {
            try {
                return xmlif.createXMLStreamReader(reader);
            }
            catch (XMLStreamException e) {
                if (isEntityExpansionLimitException(e)) {
                    setupXMLInputFactory();
                }
                else {
                    throw e;
                }
            }
        }
    }

    /**
     * Creates an XML stream reader from the specified input stream.
     * 
     * Some versions of Java cause an exception to be incorrectly thrown when more XMLStreamReaders have been
     * created by a single factory than the entity expansion limit property (default 64000). This model catches
     * that exception, creates a new factory with the appropriate settings, and then retries the reader creation.
     * 
     * @param streamRef
     *            An <code>InputStream</code> object that represents the input stream to use as the source.
     * 
     * @return A <code>java.xml.stream.XMLStreamReader</code> object that represents the XML stream reader created from
     *         the specified input stream.
     * 
     * @throws XMLStreamException
     *             If the XML stream reader could not be created.
     * 
     * @see <a
     *      href="https://bugs.openjdk.java.net/browse/JDK-8028111">https://bugs.openjdk.java.net/browse/JDK-8028111</a>
     */
    public static XMLStreamReader createXMLStreamReaderFromStream(final InputStream streamRef)
            throws XMLStreamException {
        while (true) {
            try {
                return xmlif.createXMLStreamReader(streamRef, Constants.UTF8_CHARSET);
            }
            catch (XMLStreamException e) {
                if (isEntityExpansionLimitException(e)) {
                    setupXMLInputFactory();
                }
                else {
                    throw e;
                }
            }
        }
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
     * Private Default Ctor
     */
    private DeserializationHelper() {
        // No op
    }
}
