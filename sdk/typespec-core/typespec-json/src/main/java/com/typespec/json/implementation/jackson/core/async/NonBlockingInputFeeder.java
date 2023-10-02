// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.async;

/**
 * Interface used by non-blocking {@link com.typespec.json.implementation.jackson.core.JsonParser}
 * implementations to feed input to parse.
 * Feeder is used by entity that feeds content to parse; at any given point
 * only one chunk of content can be processed so caller has to take care to
 * only feed more content when existing content has been parsed (which occurs
 * when parser's <code>nextToken</code> is called). Once application using
 * non-blocking parser has no more data to feed it should call
 * {@link #endOfInput} to indicate end of logical input (stream) to parse.
 *
 * @since 2.9
 */
public interface NonBlockingInputFeeder
{
    /**
     * Method called to check whether it is ok to feed more data: parser returns true
     * if it has no more content to parse (and it is ok to feed more); otherwise false
     * (and no data should yet be fed).
     *
     * @return {@code True} if more input is needed (and can be fed); {@code false} if
     *    there is still some input to decode
     */
    public boolean needMoreInput();

    /**
     * Method that should be called after last chunk of data to parse has been fed
     * (with {@code feedInput} in sub-class); can be called regardless of what
     * {@link #needMoreInput}
     * returns. After calling this method, no more data can be fed; and parser assumes
     * no more data will be available.
     */
    public void endOfInput();
}
