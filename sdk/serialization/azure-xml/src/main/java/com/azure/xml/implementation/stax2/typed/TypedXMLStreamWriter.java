// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.typed;

import java.math.BigDecimal;
import java.math.BigInteger;

// !!! 30-Jan-2008, TSa: JDK 1.5 only, can't add yet
//import javax.xml.datatype.XMLGregorianCalendar;

import javax.xml.namespace.QName;
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

    /**
     * Write a boolean value to the output as textual element content.
     * The lexical representation of content is defined by the
     * <a href="http://www.w3.org/TR/xmlschema-2/#boolean">XML Schema boolean</a> data type.
     *
     * @param value  The boolean value to write.
     */
    void writeBoolean(boolean value) throws XMLStreamException;

    /**
     * Write an int value to the output as textual element content.
     * The lexical representation of content is defined by the
     * <a href="http://www.w3.org/TR/xmlschema-2/#integer">XML Schema integer</a> data type.
     */
    void writeInt(int value) throws XMLStreamException;

    /**
     * Write a long value to the output as textual element content.
     * The lexical representation of content is defined by the
     * <a href="http://www.w3.org/TR/xmlschema-2/#long">XML Schema long</a> data type.
     */
    void writeLong(long value) throws XMLStreamException;

    /**
     * Write a float value to the output as textual element content.
     * The lexical representation of content is defined by the
     * <a href="http://www.w3.org/TR/xmlschema-2/#float">XML Schema float</a> data type.
     */
    void writeFloat(float value) throws XMLStreamException;

    /**
     * Write a double value to the output as textual element content.
     * The lexical representation of content is defined by the
     * <a href="http://www.w3.org/TR/xmlschema-2/#double">XML Schema double</a> data type.
     */
    void writeDouble(double value) throws XMLStreamException;

    void writeInteger(BigInteger value) throws XMLStreamException;

    /**
     * Write a decimal value to the output as textual element content.
     * The lexical representation of content is defined by the
     * <a href="http://www.w3.org/TR/xmlschema-2/#decimal">XML Schema decimal</a> data type.
     */
    void writeDecimal(BigDecimal value) throws XMLStreamException;

    void writeQName(QName value) throws XMLStreamException;

    // !!! 30-Jan-2008, TSa: JDK 1.5 only, can't add yet
    //void writeCalendar(XMLGregorianCalendar value) throws XMLStreamException;

    /*
    //////////////////////////////////////////////////////////
    // Then streaming/chunked typed element write methods
    // for non-scalar (array, binary data) values
    //////////////////////////////////////////////////////////
     */

    /**
     *<p>
     * Write binary content as base64 encoded characters to the output.
     * The lexical representation of a byte array is defined by the
     * <a href="http://www.w3.org/TR/xmlschema-2/#base64Binary">XML Schema base64Binary</a> data type. This method can be called 
     * multiple times to write the array in chunks; but if so,
     * callers should write output in chunks divisible by 3 (natural
     * atomic unit of base64 output, which avoids padding) to
     * maximize interoperability.
     * 
     *<p>
     * Note: base64 variant defaults to {@link Base64Variants#MIME}.
     *
     * @param value   The array from which to write the bytes.
     * @param from    The index in the array from which writing starts.
     * @param length  The number of bytes to write.
     */
    void writeBinary(byte[] value, int from, int length) throws XMLStreamException;

    void writeBinary(Base64Variant variant, byte[] value, int from, int length) throws XMLStreamException;

    /**
     * <p>Write int array to the output. The lexical
     * representation of a int array is defined by the following
     * XML schema type:
     * <pre>
     *    &lt;xs:simpleType name="intArray"&gt;
     *       &lt;xs:list itemType="xs:int"/&gt;
     *    &lt;/xs:simpleType&gt;</pre>
     * whose lexical space is a list of space-separated ints.
     * This method can be called multiple times to write the 
     * array in chunks.
     *
     * @param value   The array from which to write the ints.
     * @param from    The index in the array from which writing starts.
     * @param length  The number of ints to write.
     */
    void writeIntArray(int[] value, int from, int length) throws XMLStreamException;

    void writeLongArray(long[] value, int from, int length) throws XMLStreamException;

    void writeFloatArray(float[] value, int from, int length) throws XMLStreamException;

    void writeDoubleArray(double[] value, int from, int length) throws XMLStreamException;

    // -- Attributes --

    /**
     * Write a boolean value to the output as attribute value.
     * The lexical representation of content is defined by the
     * <a href="http://www.w3.org/TR/xmlschema-2/#boolean">XML Schema boolean</a> data type.
     *
     * @param prefix  The attribute's prefix. Null or "" if no prefix is to be used
     * @param namespaceURI  The attribute's URI (can be either null or empty String for "no namespace")
     * @param localName  The attribute's local name
     * @param value  The boolean value to write.
     */
    void writeBooleanAttribute(String prefix, String namespaceURI, String localName, boolean value)
        throws XMLStreamException;

    /**
     * Write an integer value to the output as attribute value.
     * The lexical representation of content is defined by the
     * <a href="http://www.w3.org/TR/xmlschema-2/#integer">XML Schema integer</a> data type.
     *
     * @param prefix  The attribute's prefix. Null or "" if no prefix is to be used
     * @param namespaceURI  The attribute's URI (can be either null or empty String for "no namespace")
     * @param localName  The attribute's local name
     * @param value  The integer value to write.
     */
    void writeIntAttribute(String prefix, String namespaceURI, String localName, int value) throws XMLStreamException;

    /**
     * Write an long value to the output as attribute value.
     * The lexical representation of content is defined by the
     * <a href="http://www.w3.org/TR/xmlschema-2/#long">XML Schema long</a> data type.
     *
     * @param prefix  The attribute's prefix. Null or "" if no prefix is to be used
     * @param namespaceURI  The attribute's URI (can be either null or empty String for "no namespace")
     * @param localName  The attribute's local name
     * @param value  The long value to write.
     */
    void writeLongAttribute(String prefix, String namespaceURI, String localName, long value) throws XMLStreamException;

    void writeFloatAttribute(String prefix, String namespaceURI, String localName, float value)
        throws XMLStreamException;

    void writeDoubleAttribute(String prefix, String namespaceURI, String localName, double value)
        throws XMLStreamException;

    void writeIntegerAttribute(String prefix, String namespaceURI, String localName, BigInteger value)
        throws XMLStreamException;

    void writeDecimalAttribute(String prefix, String namespaceURI, String localName, BigDecimal value)
        throws XMLStreamException;

    void writeQNameAttribute(String prefix, String namespaceURI, String localName, QName value)
        throws XMLStreamException;

    // !!! 30-Jan-2008, TSa: JDK 1.5 only -- is that ok?
    //void writeCalendarAttribute(String prefix, String namespaceURI, String localName, XMLGregorianCalendar value)  throws XMLStreamException;

    /* 25-Apr-2008, tatus: Do we even want to deal with structured
     *    or binary typed access with attributes?
     */

    /**
     * <p>Write a byte array attribute. The lexical
     * representation of a byte array is defined by the
     * <a href="http://www.w3.org/TR/xmlschema-2/#base64Binary">XML Schema base64Binary</a> data type.
     *<p>
     * Note: base64 variant defaults to {@link Base64Variants#MIME}.
     *
     * @param prefix  The attribute's prefix.
     * @param namespaceURI  The attribute's URI.
     * @param localName  The attribute's local name.
     * @param value   The array from which to write the bytes.
     */
    void writeBinaryAttribute(String prefix, String namespaceURI, String localName, byte[] value)
        throws XMLStreamException;

    void writeBinaryAttribute(Base64Variant variant, String prefix, String namespaceURI, String localName, byte[] value)
        throws XMLStreamException;

    /**
     * <p>Write int array attribute. The lexical
     * representation of a int array is defined by the following
     * XML schema type:
     * <pre>
     *    &lt;xs:simpleType name="intArray"&gt;
     *       &lt;xs:list itemType="xs:int"/&gt;
     *    &lt;/xs:simpleType&gt;</pre>
     * whose lexical space is a list of space-separated ints.
     *
     * @param prefix  The attribute's prefix.
     * @param namespaceURI  The attribute's URI.
     * @param localName  The attribute's local name.
     * @param value   The array from which to write the ints.
     */
    void writeIntArrayAttribute(String prefix, String namespaceURI, String localName, int[] value)
        throws XMLStreamException;

    void writeLongArrayAttribute(String prefix, String namespaceURI, String localName, long[] value)
        throws XMLStreamException;

    void writeFloatArrayAttribute(String prefix, String namespaceURI, String localName, float[] value)
        throws XMLStreamException;

    void writeDoubleArrayAttribute(String prefix, String namespaceURI, String localName, double[] value)
        throws XMLStreamException;
}
