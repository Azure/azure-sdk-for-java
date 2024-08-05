// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.typed;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

/**
 * This class represents an exception throw by an
 * {@link TypedXMLStreamReader} or an {@link TypedXMLStreamWriter}. It is
 * used to indicate a problems occuring when trying convert
 * data for access using typed read or write methods.
 *<p>
 * Note that the lexical value is a mandatory thing to pass -- since
 * this exception subtype need not be used solely for wrapping
 * purposes ({@link XMLStreamException}s are to be passed as is,
 * other underlying root cause types should be unchecked), we
 * can mandate a lexical value (which may be null, however) to
 * be passed.
 * Similarly, aside from generic stream exception, it is mandated
 * that root causes to wrap need to be of type
 * {@link IllegalArgumentException}, since that is the supertype
 * of regular parsing/conversion types for primitive types.
 *
 * @since 3.0
 *
 * @author Santiago.PericasGeertsen@sun.com
 * @author Tatu Saloranta
 */
public class TypedXMLStreamException extends XMLStreamException {
    private static final long serialVersionUID = 1L;

    /**
     * Lexical representation of the content that could not be
     * converted to the requested type. May be <code>null</code>
     * if a processor is unable to provide it. Lexical representation
     * should preferably be as close to the original input String
     * as possible (as opposed to being normalized which often
     * is done before actual parsing).
     */
    protected String mLexical;

    /**
     * Construct an exception with the associated message.
     *
     * @param msg  The message to report.
     */
    public TypedXMLStreamException(String lexical, String msg) {
        super(msg);
        mLexical = lexical;
    }

    /**
     * Construct an exception with the associated message and exception
     *
     * @param msg  The message to report.
     * @param rootCause Underlying conversion problem
     */
    public TypedXMLStreamException(String lexical, String msg, IllegalArgumentException rootCause) {
        super(msg, rootCause);
        mLexical = lexical;
    }

    /**
     * Construct an exception with the associated message, exception and
     * location.
     *
     * @param msg  The message to report.
     * @param location  The location of the error.
     * @param rootCause Underlying conversion problem
     */
    public TypedXMLStreamException(String lexical, String msg, Location location, IllegalArgumentException rootCause) {
        super(msg, location, rootCause);
        mLexical = lexical;
    }

    /**
     * Construct an exception with the associated message, exception and
     * location.
     *
     * @param msg  The message to report.
     * @param location  The location of the error.
     */
    public TypedXMLStreamException(String lexical, String msg, Location location) {
        super(msg, location);
        mLexical = lexical;
    }

    /**
     * Return the lexical representation of the attribute or element
     * content that could not be converted as requested.
     *
     * @return  Lexical representation of unconverted content or
     *          <code>null</code> if unavailable.
     */
    public String getLexical() {
        return mLexical;
    }
}
