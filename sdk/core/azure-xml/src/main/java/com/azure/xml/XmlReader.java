// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;
import java.io.Closeable;

/**
 * Reads an XML encoded value as a stream of tokens.
 */
public abstract class XmlReader implements Closeable {
    /**
     * Gets the {@link XmlToken} that the reader points to currently.
     * <p>
     * Returns null if the reader isn't pointing to a token. This happens if the reader hasn't begun to read the XML
     * value or if reading of the XML value has completed.
     *
     * @return The {@link XmlToken} that the reader points to currently, or null if the reader isn't pointing to a
     * token.
     */
    public abstract XmlToken currentToken();

    /**
     * Iterates to and returns the next {@link XmlToken} in the XML stream.
     * <p>
     * Returns null if iterating to the next token completes reading of the XML stream.
     *
     * @return The next {@link XmlToken} in the XML stream, or null if reading completes.
     */
    public abstract XmlToken nextToken();

    /**
     * Gets the {@link QName} for the current XML element.
     *
     * @return The {@link QName} for the current XML element.
     */
    public abstract QName getElementName();

    /**
     * Gets the {@link QName} for the current XML attribute.
     *
     * @return The {@link QName} fpr the current XML attribute.
     */
    public abstract QName getAttributeName();
}
