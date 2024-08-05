// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import javax.xml.stream.*;
import javax.xml.stream.events.*;

import com.azure.xml.implementation.stax2.XMLEventReader2;

/**
 * This adapter implements parts of {@link XMLEventReader2}, the
 * extended stream reader defined by Stax2 extension, by wrapping
 * a vanilla Stax 1.0 {@link XMLEventReader} implementation.
 *<p>
 * Note: the implementation may be incomplete as-is, since not all
 * features needed are necessarily accessible via basic Stax 1.0 interface.
 * As such, two main use cases for this wrapper are:
 *<ul>
 * <li>Serve as convenient base class for a complete implementation,
 *    which can use native accessors provided by the wrapped Stax
 *    implementation
 *  </li>
 * <li>To be used for tasks that make limited use of Stax2 API, such
 *   that missing parts are not needed
 *  </li>
 * </ul>
 *
 * @author Tatu Saloranta
 */
public class Stax2EventReaderAdapter implements XMLEventReader2 {
    final protected XMLEventReader mReader;

    /*
    /**********************************************************************
    /* Life-cycle methods
    /**********************************************************************
     */

    protected Stax2EventReaderAdapter(XMLEventReader er) {
        mReader = er;
    }

    /**
     * Method that should be used to add dynamic support for
     * {@link XMLEventReader2}. Method will check whether the
     * stream reader passed happens to be a {@link XMLEventReader2};
     * and if it is, return it properly cast. If not, it will create
     * necessary wrapper.
     */
    public static XMLEventReader2 wrapIfNecessary(XMLEventReader er) {
        if (er instanceof XMLEventReader2) {
            return (XMLEventReader2) er;
        }
        return new Stax2EventReaderAdapter(er);
    }

    /*
    /**********************************************************************
    /* XMLEventReader pass-through methods
    /**********************************************************************
     */

    @Override
    public void close() throws XMLStreamException {
        mReader.close();
    }

    @Override
    public String getElementText() throws XMLStreamException {
        return mReader.getElementText();
    }

    @Override
    public Object getProperty(String name) {
        return mReader.getProperty(name);
    }

    @Override
    public boolean hasNext() {
        return mReader.hasNext();
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        return mReader.nextEvent();
    }

    @Override
    public Object next() {
        return mReader.next();
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        return mReader.nextTag();
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        return mReader.peek();
    }

    /**
     * Note: only here because we implement Iterator interface.
     */
    @Override
    public void remove() {
        mReader.remove();
    }

    /*
    /**********************************************************************
    /* XMLEventReader2 implementation
    /**********************************************************************
     */

    @Override
    public boolean isPropertySupported(String name) {
        /* No way to support properly via Stax1 interface... but
         * let's approximate: we can be sure it is supported, if
         * we can access value without IllegalArgumentException
         */
        try {
            /*Object x =*/ mReader.getProperty(name);
        } catch (IllegalArgumentException iae) {
            return false;
        }
        return true;
    }

    @Override
    public boolean setProperty(String name, Object value) {
        // No way to support via Stax1 interface
        return false;
    }
}
