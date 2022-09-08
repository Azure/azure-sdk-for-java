// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.function.Function;

/**
 * Equivalent in functionality to {@link Function} except that {@link #apply(Object)} is checked with
 * {@link XMLStreamException}
 *
 * @param <T> Input type of the function.
 * @param <R> Output type of the function.
 */
public interface XMLStreamExceptionFunction<T, R> {
    /**
     * Applies the function to the {@code input}.
     *
     * @param input Input to the function.
     * @return The output of the function.
     * @throws XMLStreamException If an XML stream error occurs during application of the function.
     * @throws IOException If an I/O error occurs during application of the function, {@link XmlReader} and
     * {@link XmlWriter} APIs will catch {@link IOException IOExceptions} and wrap them in an
     * {@link XMLStreamException}.
     */
    R apply(T input) throws XMLStreamException, IOException;
}
