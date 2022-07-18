// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import java.io.Closeable;

/**
 * Writes an XML encoded value to a stream.
 */
public abstract class XmlWriter implements Closeable {
    /**
     * Begins an XML element start tag ({@code <tag}).
     *
     * @param localName Name of the tag.
     * @return The updated XmlWriter object.
     */
    public abstract XmlWriter writeStartTagBegin(String localName);

    /**
     * Begins an XML element start tag that has a prefix ({@code <prefix:tag}).
     *
     * @param prefix Prefix of the tag.
     * @param localName Name of the tag.
     * @return The updated XmlWriter object.
     */
    public abstract XmlWriter writeStartTagBegin(String prefix, String localName);

    /**
     * Writes an XML element attribute ({@code attribute="value"}).
     *
     * @param localName Name of the attribute.
     * @param value Value of the attribute.
     * @return The updated XmlWriter object.
     */
    public abstract XmlWriter writeAttribute(String localName, String value);

    /**
     * Writes an XML element attribute that has a prefix ({@code prefix:attribute="value"}).
     *
     * @param prefix Prefix of the attribute.
     * @param localName Name of the attribute.
     * @param value Value of the attribute.
     * @return The updated XmlWriter object.
     */
    public abstract XmlWriter writeAttribute(String prefix, String localName, String value);

    /**
     * Ends an XML element start tag ({@code >}).
     *
     * @return The updated XmlWriter object.
     */
    public abstract XmlWriter writeStartTagEnd();

    /**
     * Ends an XML element start tag as self-closing ({@code />}).
     *
     * @return The updated XmlWriter object.
     */
    public abstract XmlWriter writeStartTagSelfClosing();

    /**
     * Writes a value directly into an XML element ({@code <tag>value</tag>}).
     * <p>
     * This doesn't write the XML element start tag or end tag.
     * <p>
     * {@link #writeCData(String)} is a convenience API if an XML CData value needs to be written.
     *
     * @param value Value to write.
     * @return The updated XmlWriter object.
     */
    public abstract XmlWriter writeRaw(String value);

    /**
     * Writes a CData value directly into an XML element ({@code <tag><![CDATA[value]]></tag>}).
     * <p>
     * This doesn't write the XML element start tag or end tag.
     * <p>
     * This API is a convenience over {@link #writeRaw(String)} for CData values, it is possible to use
     * {@link #writeRaw(String)} instead of this API.
     *
     * @param value CData value to write.
     * @return The updated XmlWriter object.
     */
    public abstract XmlWriter writeCData(String value);

    /**
     * Writes an XML element end tag ({@code </tag>}).
     *
     * @param localName Name of the tag.
     * @return The updated XmlWriter object.
     */
    public abstract XmlWriter writeEndTag(String localName);

    /**
     * Writes an XML element end tag that has a prefix ({@code </prefix:tag>}).
     *
     * @param prefix Prefix of the tag.
     * @param localName Name of the tag.
     * @return The updated XmlWriter object.
     */
    public abstract XmlWriter writeEndTag(String prefix, String localName);
}
