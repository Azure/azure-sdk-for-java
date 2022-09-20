// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * An interface to be implemented by any azure-xml plugin that wishes to provide an alternate {@link XmlReader} or
 * {@link XmlWriter} implementation.
 */
public interface XmlProvider {
    /**
     * Creates an instance of {@link XmlReader} that reads a {@code byte[]}.
     *
     * @param json The JSON represented as a {@code byte[]}.
     * @return A new instance of {@link XmlReader}.
     * @throws NullPointerException If {@code json} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    XmlReader createReader(byte[] json) throws XMLStreamException;

    /**
     * Creates an instance of {@link XmlReader} that reads a {@link String}.
     *
     * @param json The JSON represented as a {@link String}.
     * @return A new instance of {@link XmlReader}.
     * @throws NullPointerException If {@code json} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    XmlReader createReader(String json) throws XMLStreamException;

    /**
     * Creates an instance of {@link XmlReader} that reads a {@link InputStream}.
     *
     * @param json The JSON represented as a {@link InputStream}.
     * @return A new instance of {@link XmlReader}.
     * @throws NullPointerException If {@code json} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    XmlReader createReader(InputStream json) throws XMLStreamException;

    /**
     * Creates an instance of {@link XmlReader} that reads a {@link Reader}.
     *
     * @param json The JSON represented as a {@link Reader}.
     * @return A new instance of {@link XmlReader}.
     * @throws NullPointerException If {@code json} is null.
     * @throws XMLStreamException If an {@link XmlReader} cannot be instantiated.
     */
    XmlReader createReader(Reader json) throws XMLStreamException;

    /**
     * Creates an instance of {@link XmlWriter} that writes to an {@link OutputStream}.
     *
     * @param json The JSON represented as an {@link OutputStream}.
     * @return A new instance of {@link XmlWriter}.
     * @throws NullPointerException If {@code json} is null.
     * @throws XMLStreamException If an {@link XmlWriter} cannot be instantiated.
     */
    XmlWriter createWriter(OutputStream json) throws XMLStreamException;

    /**
     * Creates an instance of {@link XmlWriter} that writes to an {@link Writer}.
     *
     * @param json The JSON represented as an {@link Writer}.
     * @return A new instance of {@link XmlWriter}.
     * @throws NullPointerException If {@code json} is null.
     * @throws XMLStreamException If an {@link XmlWriter} cannot be instantiated.
     */
    XmlWriter createWriter(Writer json) throws XMLStreamException;
}
