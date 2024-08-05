// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto;

import com.azure.xml.implementation.stax2.XMLStreamReader2;

import com.azure.xml.implementation.aalto.in.ReaderConfig;

/**
 * Extension of {@link XMLStreamReader2} used by non-blocking ("async")
 * stream readers. The main difference is addition of a token ({@link #EVENT_INCOMPLETE})
 * to indicate that there is not yet enough content to parse to tokenize next event;
 * and method to access {@link AsyncInputFeeder} that is used to provide input data
 * in non-blocking manner.
 *
 * @param <F> Type of input feeder used by reader; determines how input can be fed.
 */
public interface AsyncXMLStreamReader<F extends AsyncInputFeeder> extends XMLStreamReader2 {
    /**
     * As per javadocs of {@link javax.xml.stream.XMLStreamConstants},
     * event codes 0 through 256 (inclusive?) are reserved by the Stax
     * specs, so we'll use the next available code.
     */
    int EVENT_INCOMPLETE = 257;

    ReaderConfig getConfig();
}
