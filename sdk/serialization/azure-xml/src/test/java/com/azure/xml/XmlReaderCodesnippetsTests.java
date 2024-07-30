// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.xml;

import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Code snippets for {@link XmlReader}
 */
public class XmlReaderCodesnippetsTests {
    @Test
    public void getNullableAttribute() {
        // BEGIN: com.azure.xml.XmlReader.getNullableAttribute#String-String-ReadValueCallback
        try (XmlReader reader = XmlReader.fromString("<root><element attribute=\"1234\"/></root>")) {
            reader.nextElement(); // Progress to <root>
            reader.nextElement(); // Progress to <element>

            // Get the value of the attribute "attribute" as an Integer in a way that allows for the attribute to be
            // missing or have a null value.
            Objects.equals(1234, reader.getNullableAttribute(null, "attribute", Integer::parseInt));

            // This attribute doesn't exist, so null is returned without causing a NumberFormatException (which is what
            // Integer.parseInt throws on a null string being passed).
            Objects.isNull(reader.getNullableAttribute(null, "nonExistentAttribute", Integer::parseInt));
        } catch (XMLStreamException ex) {
            // Do something with the exception
        }
        // END: com.azure.xml.XmlReader.getNullableAttribute#String-String-ReadValueCallback

        // The real test
        try (XmlReader reader = XmlReader.fromString("<root><element attribute=\"1234\"/></root>")) {
            reader.nextElement(); // Progress to <root>
            reader.nextElement(); // Progress to <element>

            // Get the value of the attribute "attribute" as an Integer in a way that allows for the attribute to be
            // missing or have a null value.
            assertEquals(1234, (int) reader.getNullableAttribute(null, "attribute", Integer::parseInt));

            // This attribute doesn't exist, so null is returned without causing a NumberFormatException (which is what
            // Integer.parseInt throws on a null string being passed).
            assertNull(reader.getNullableAttribute(null, "nonExistentAttribute", Integer::parseInt));
        } catch (XMLStreamException ex) {
            // Do something with the exception
        }
    }

    @Test
    public void getNullableElement() {
        // BEGIN: com.azure.xml.XmlReader.getNullableElement#ReadValueCallback
        try (XmlReader reader = XmlReader.fromString("<root><element>1234</element><emptyElement/></root>")) {
            reader.nextElement(); // Progress to <root>
            reader.nextElement(); // Progress to <element>

            // Get the value of the element "element" as an Integer in a way that allows for the element to be missing
            // or have a null value.
            Objects.equals(1234, reader.getNullableElement(Integer::parseInt)); // 1234

            reader.nextElement(); // Progress to <emptyElement>

            // This element doesn't exist, so null is returned without causing a NumberFormatException (which is what
            // Integer.parseInt throws on a null string being passed).
            Objects.isNull(reader.getNullableElement(Integer::parseInt));
        } catch (XMLStreamException ex) {
            // Do something with the exception
        }
        // END: com.azure.xml.XmlReader.getNullableElement#ReadValueCallback

        // The real test
        try (XmlReader reader = XmlReader.fromString("<root><element>1234</element><emptyElement/></root>")) {
            reader.nextElement(); // Progress to <root>
            reader.nextElement(); // Progress to <element>

            // Get the value of the element "element" as an Integer in a way that allows for the element to be missing
            // or have a null value.
            assertEquals(1234, (int) reader.getNullableElement(Integer::parseInt)); // 1234

            reader.nextElement(); // Progress to <emptyElement>

            // This element doesn't exist, so null is returned without causing a NumberFormatException (which is what
            // Integer.parseInt throws on a null string being passed).
            assertNull(reader.getNullableElement(Integer::parseInt));
        } catch (XMLStreamException ex) {
            // Do something with the exception
        }
    }
}
