// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.v2.util.CoreUtils;
import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLStreamException;
import java.util.Objects;

/**
 * Test class implementing {@link XmlSerializable}.
 */
public final class SimpleXmlSerializable implements XmlSerializable<SimpleXmlSerializable> {
    private final boolean aBooleanAsAttribute;
    private final int anInt;
    private final double aDecimalAsAttribute;
    private final String aString;

    public SimpleXmlSerializable(boolean aBooleanAsAttribute, int anInt, double aDecimalAsAttribute, String aString) {
        this.aBooleanAsAttribute = aBooleanAsAttribute;
        this.anInt = anInt;
        this.aDecimalAsAttribute = aDecimalAsAttribute;
        this.aString = aString;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        rootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? "SimpleXml" : rootElementName;
        xmlWriter.writeStartElement(rootElementName);

        xmlWriter.writeBooleanAttribute("boolean", aBooleanAsAttribute);
        xmlWriter.writeDoubleAttribute("decimal", aDecimalAsAttribute);

        xmlWriter.writeIntElement("int", anInt);
        xmlWriter.writeStringElement("string", aString);

        return xmlWriter.writeEndElement();
    }

    public static SimpleXmlSerializable fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    public static SimpleXmlSerializable fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        rootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? "SimpleXml" : rootElementName;
        return xmlReader.readObject(rootElementName, reader -> {
            boolean aBooleanAsAttribute = xmlReader.getBooleanAttribute(null, "boolean");
            double aDecimalAsAttribute = xmlReader.getDoubleAttribute(null, "decimal");
            int anInt = 0;
            boolean foundAnInt = false;
            String aString = null;
            boolean foundAString = false;

            while (xmlReader.nextElement() != XmlToken.END_ELEMENT) {
                String elementName = xmlReader.getElementName().getLocalPart();
                if ("int".equals(elementName)) {
                    anInt = xmlReader.getIntElement();
                    foundAnInt = true;
                } else if ("string".equals(elementName)) {
                    aString = xmlReader.getStringElement();
                    foundAString = true;
                } else {
                    xmlReader.skipElement();
                }
            }

            if (foundAnInt && foundAString) {
                return new SimpleXmlSerializable(aBooleanAsAttribute, anInt, aDecimalAsAttribute, aString);
            }

            throw new IllegalStateException("Missing required elements.");
        });
    }

    public boolean isABoolean() {
        return aBooleanAsAttribute;
    }

    public int getAnInt() {
        return anInt;
    }

    public double getADecimal() {
        return aDecimalAsAttribute;
    }

    public String getAString() {
        return aString;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aBooleanAsAttribute, anInt, aDecimalAsAttribute, aString);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleXmlSerializable)) {
            return false;
        }

        SimpleXmlSerializable other = (SimpleXmlSerializable) obj;

        return aBooleanAsAttribute == other.aBooleanAsAttribute
            && anInt == other.anInt
            && aDecimalAsAttribute == other.aDecimalAsAttribute
            && Objects.equals(aString, other.aString);
    }
}
