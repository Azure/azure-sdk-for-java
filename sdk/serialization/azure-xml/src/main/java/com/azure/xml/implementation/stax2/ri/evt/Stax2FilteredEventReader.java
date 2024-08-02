// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import javax.xml.stream.*;
import javax.xml.stream.events.*;

import com.azure.xml.implementation.stax2.XMLEventReader2;

/**
 *<p>
 * Some notes about implemention:
 *<ul>
 * <li>There is no way to filter out values of peek(), so we'll just straight
 *    dispatch the call to underlying reader
 *  </li>
 *</ul>
 */
public class Stax2FilteredEventReader implements XMLEventReader2, XMLStreamConstants {
    final XMLEventReader2 mReader;
    final EventFilter mFilter;

    public Stax2FilteredEventReader(XMLEventReader2 r, EventFilter f) {
        mReader = r;
        mFilter = f;
    }

    /*
    ////////////////////////////////////////////////////
    // XMLEventReader implementation
    ////////////////////////////////////////////////////
     */

    @Override
    public void close() throws XMLStreamException {
        mReader.close();
    }

    @Override
    public String getElementText() throws XMLStreamException {
        // Is this enough?
        return mReader.getElementText();
    }

    @Override
    public Object getProperty(String name) {
        return mReader.getProperty(name);
    }

    @Override
    public boolean hasNext() {
        try {
            return (peek() != null);
        } catch (XMLStreamException sex) { // shouldn't happen, but...
            throw new RuntimeException(sex);
        }
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        while (true) {
            XMLEvent evt = mReader.nextEvent();
            if (evt == null || mFilter.accept(evt)) {
                // should never get null, actually, but...
                return evt;
            }
        }
    }

    @Override
    public Object next() {
        try {
            return nextEvent();
        } catch (XMLStreamException sex) {
            throw new RuntimeException(sex);
        }
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        // This can be implemented very similar to next()...

        while (true) {
            XMLEvent evt = mReader.nextTag();
            if (evt == null || mFilter.accept(evt)) {
                return evt;
            }
        }
    }

    /**
     * This is bit tricky to implement, but it should filter out
     * events just as nextEvent() would.
     */
    @Override
    public XMLEvent peek() throws XMLStreamException {
        while (true) {
            XMLEvent evt = mReader.peek();
            if (evt == null || mFilter.accept(evt)) {
                return evt;
            }
            // Need to discard as long as we have events:
            mReader.nextEvent();
        }
    }

    /**
     * Note: only here because we implement Iterator interface
     */
    @Override
    public void remove() { // let's let underlying impl fail on it
        mReader.remove();
    }

    /*
    ////////////////////////////////////////////////////
    // XMLEventReader2 implementation
    ////////////////////////////////////////////////////
     */

    @Override
    public boolean hasNextEvent() throws XMLStreamException {
        return (peek() != null);
    }

    @Override
    public boolean isPropertySupported(String name) {
        return mReader.isPropertySupported(name);
    }

    @Override
    public boolean setProperty(String name, Object value) {
        return mReader.setProperty(name, value);
    }
}
