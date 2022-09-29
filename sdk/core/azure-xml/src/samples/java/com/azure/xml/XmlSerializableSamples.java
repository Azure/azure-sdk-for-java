// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Samples for {@link XmlSerializable}.
 */
public class XmlSerializableSamples {
    // BEGIN: xmlserializablesample-basic
    public class XmlSerializableExample implements XmlSerializable<XmlSerializableExample> {
        private boolean aBooleanAttribute;
        private Double aNullableDecimalAttribute;
        private int anIntElement;
        private String aStringElement;

        @Override
        public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
            xmlWriter.writeStartElement("example");

            // Writing attributes must happen first so that they are written to the object start element.
            xmlWriter.writeBooleanAttribute("aBooleanAttribute", aBooleanAttribute);
            xmlWriter.writeNumberAttribute("aNullableDecimalAttribute", aNullableDecimalAttribute);

            xmlWriter.writeIntElement("anIntElement", anIntElement);
            xmlWriter.writeStringElement("aStringElement", aStringElement);

            return xmlWriter.writeEndElement();
        }

        public XmlSerializableExample fromXml(XmlReader xmlReader) throws XMLStreamException {
            // readObject is a convenience method on XmlReader which prepares the XML for being read as an object.
            // If the current token isn't an XmlToken.START_ELEMENT the next token element will be iterated to, if it's
            // still not an XmlToken.START_ELEMENT after iterating to the next element an exception will be thrown. If
            // the next element is an XmlToken.START_ELEMENT it will validate that the XML element matches the name
            // expected, if the name doesn't match an exception will be thrown. If the element name matches the reader
            // function will be called.
            return xmlReader.readObject("example", reader -> {
                // Since this class has no constructor reading to fields can be done inline.
                // If the class had a constructor with arguments the recommendation is using local variables to track
                // all field values.

                XmlSerializableExample result = new XmlSerializableExample();

                // Reading attributes must happen first so that the XmlReader is looking at the object start element.
                result.aBooleanAttribute = reader.getBooleanAttribute(null, "aBooleanAttribute");
                result.aNullableDecimalAttribute = reader.getNullableAttribute(null, "aNullableDecimalAttribute",
                    Double::parseDouble);

                while (reader.nextElement() != XmlToken.END_ELEMENT) {
                    QName elementName = reader.getElementName();

                    // Since this object doesn't use namespaces we can work with the local part directly.
                    // If it had namespaces the full QName would need to be inspected.
                    String localPart = elementName.getLocalPart();
                    if ("anIntElement".equals(localPart)) {
                        result.anIntElement = reader.getIntElement();
                    } else if ("aStringElement".equals(localPart)) {
                        // getStringElement coalesces XML text and XML CData into a single string without needing to
                        // manage state.
                        result.aStringElement = reader.getStringElement();
                    } else {
                        // Skip element when the element is unknown.
                        reader.skipElement();
                    }
                }

                return result;
            });
        }
    }
    // END: xmlserializablesample-basic
}
