// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.io;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Interface that defines API for the factories stream writers use for
 * creating "escaping writers". These factories are used when defining
 * custom escaping of text (as well as possibly used by the
 * implementations for default escaping too). Escaping in this context
 * refers to the process of replacing individual text/attribute content
 * character with pre-defined and character entities, as per XML
 * specification (2.4, Appendix D).
 *<p>
 * Typical escaping writers replace characters like '&lt;' and '&amp;',
 * as well as some additional characters depending on context.
 * Custom implementations may choose to escape additional characters,
 * for example to make it easier to manually view or edit resulting
 * serialized XML document.
 *<p>
 * Note about implementing escaping writers: writers need to obey normal
 * Writer semantics, and specifically they should pass calls to
 * <code>flush()</code> and <code>close()</code> to the underlying
 * Writer.
 */
public interface EscapingWriterFactory {
    /**
     * Method called to create an instance of escaping writer that
     * will output content using specified writer,
     * and escaping necessary characters (depending on
     * both type [attr, element text] and encoding).
     *
     * @param w Underlying writer that the encoding writer should
     *    output
     * @param enc Encoding to use, as specified by the stream writer
     *    (based on information application has passed)
     */
    Writer createEscapingWriterFor(Writer w, String enc) throws UnsupportedEncodingException;

    /**
     * Method called to create an instance of escaping writer that
     * will output to the specified stream, using the specified
     * encoding,
     * and escaping necessary characters (depending on
     * both type [attr, element text] and encoding).
     *
     * @param out Underlying stream that the encoding writer should
     *    output using
     * @param enc Encoding to use, as specified by the stream writer
     *    (based on information application has passed)
     */
    Writer createEscapingWriterFor(OutputStream out, String enc) throws UnsupportedEncodingException;
}
