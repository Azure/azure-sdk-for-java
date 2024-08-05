// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto;

/**
 * Interface used by {@link AsyncXMLStreamReader} to get more input to parse.
 * It is accessed by entity that feeds XML content to parse; at any given point
 * only one chunk of content can be processed so caller has to take care to
 * only feed more content when existing content has been parsed (which occurs
 * when parser's <code>nextToken</code> is called). Once application using
 * non-blocking parser has no more data to feed it should call
 * {@link #endOfInput} to indicate end of logical input stream.
 *
 * @author tatu
 */
public interface AsyncInputFeeder {
    /**
     * Method called to check whether it is ok to feed more data: parser returns true
     * if it has no more content to parse (and it is ok to feed more); otherwise false
     * (and no data should yet be fed).
     */
    boolean needMoreInput();

    /**
     * Method that should be called after last chunk of data to parse has been fed.
     * May be called regardless of what {@link #needMoreInput} returns.
     * After calling this method, no more data can be fed; and parser assumes
     * no more data will be available.
     */
    void endOfInput();
}
