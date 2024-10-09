// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Extended interface that implements functionality that is necessary
 * to properly build event API on top of {@link XMLStreamWriter},
 * as well as to configure individual instances.
 * It also adds limited number of methods that are important for
 * efficient pass-through processing (such as one needed when routing
 * SOAP-messages).
 *<p>
 * Since version 3.0, stream writer will also implement "Typed Access API"
 * on output side.
 *
 * @version 3.0.1 06-Nov-2008
 * @author Tatu Saloranta (tatu.saloranta@iki.fi)
 */
public interface XMLStreamWriter2 extends XMLStreamWriter {

    /*
    /**********************************************************************
    /* Other accessors, mutators
    /**********************************************************************
     */

    /**
     * Method that should return current output location, if the writer
     * keeps track of it; null if it does not.
     */
    Location getLocation();

    /*
    /**********************************************************************
    /* Write methods base interface is missing
    /**********************************************************************
     */

    /**
     * Method that can be called to write whitespace-only content.
     * If so, it is to be written as is (with no escaping), and does
     * not contain non-whitespace characters (writer may validate this,
     * and throw an exception if it does).
     *<p>
     * This method is useful for things like outputting indentation.
     *
     * @since 3.0
     */
    void writeSpace(String text) throws XMLStreamException;

    /**
     * Method that can be called to write whitespace-only content.
     * If so, it is to be written as is (with no escaping), and does
     * not contain non-whitespace characters (writer may validate this,
     * and throw an exception if it does).
     *<p>
     * This method is useful for things like outputting indentation.
     *
     * @since 3.0
     */
    void writeSpace(char[] text, int offset, int length) throws XMLStreamException;

    /*
    /**********************************************************************
    /* Pass-through methods
    /**********************************************************************
     */

    /**
     * Method that writes specified content as is, without encoding or
     * deciphering it in any way. It will not update state of the writer
     * (except by possibly flushing output of previous writes, like
     * finishing a start element),
     * nor be validated in any way. As such, care must be taken, if this
     * method is used.
     *<p>
     * Method is usually used when encapsulating output from another writer
     * as a sub-tree, or when passing through XML fragments.
     *<p>
     * NOTE: since text to be written may be anything, including markup,
     * it can not be reliably validated. Because of this, validator(s)
     * attached to the writer will NOT be informed about writes.
     */
    void writeRaw(String text) throws XMLStreamException;

    /**
     * Method that writes specified content as is, without encoding or
     * deciphering it in any way. It will not update state of the writer
     * (except by possibly flushing output of previous writes, like
     * finishing a start element),
     * nor be validated in any way. As such, care must be taken, if this
     * method is used.
     *<p>
     * Method is usually used when encapsulating output from another writer
     * as a sub-tree, or when passing through XML fragments.
     *<p>
     * NOTE: since text to be written may be anything, including markup,
     * it can not be reliably validated. Because of this, validator(s)
     * attached to the writer will NOT be informed about writes.
     */
    void writeRaw(String text, int offset, int length) throws XMLStreamException;

    /**
     * Method that writes specified content as is, without encoding or
     * deciphering it in any way. It will not update state of the writer
     * (except by possibly flushing output of previous writes, like
     * finishing a start element),
     * nor be validated in any way. As such, care must be taken, if this
     * method is used.
     *<p>
     * Method is usually used when encapsulating output from another writer
     * as a sub-tree, or when passing through XML fragments.
     *<p>
     * NOTE: since text to be written may be anything, including markup,
     * it can not be reliably validated. Because of this, validator(s)
     * attached to the writer will NOT be informed about writes.
     */
    void writeRaw(char[] text, int offset, int length) throws XMLStreamException;
}
