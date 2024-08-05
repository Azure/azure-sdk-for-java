// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.typed;

// !!! 30-Jan-2008, TSa: JDK 1.5 only, can't add yet
//import javax.xml.datatype.XMLGregorianCalendar;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * This interface provides a typed extension to
 * {@link javax.xml.stream.XMLStreamWriter}. It defines methods for
 * writing XML data from Java types.
 *<p>
 * Exceptions to throw are declared to be basic {@link XMLStreamException}s,
 * because in addition to specific {@link TypedXMLStreamException}s
 * (which are more specific subclasses)
 * that are thrown if conversion itself fails, methods also need to
 * access underlying textual content which may throw other subtypes
 * of stream exception.
 *
 * @author Santiago.PericasGeertsen@sun.com
 * @author Tatu Saloranta
 *
 * @since 3.0
 */
public interface TypedXMLStreamWriter extends XMLStreamWriter {
    /*
    //////////////////////////////////////////////////////////
    // First, typed element write methods for scalar values
    //////////////////////////////////////////////////////////
     */

    // !!! 30-Jan-2008, TSa: JDK 1.5 only, can't add yet
    //void writeCalendar(XMLGregorianCalendar value) throws XMLStreamException;

    /*
    //////////////////////////////////////////////////////////
    // Then streaming/chunked typed element write methods
    // for non-scalar (array, binary data) values
    //////////////////////////////////////////////////////////
     */

    // -- Attributes --

    // !!! 30-Jan-2008, TSa: JDK 1.5 only -- is that ok?
    //void writeCalendarAttribute(String prefix, String namespaceURI, String localName, XMLGregorianCalendar value)  throws XMLStreamException;

    /* 25-Apr-2008, tatus: Do we even want to deal with structured
     *    or binary typed access with attributes?
     */

}
