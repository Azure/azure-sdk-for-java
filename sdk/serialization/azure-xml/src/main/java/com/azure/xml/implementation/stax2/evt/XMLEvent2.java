// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.evt;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.azure.xml.implementation.stax2.XMLStreamWriter2;

/**
 * Interface that extends basic {@link XMLEvent2} with method(s)
 * that are missing from it; specifically linkage that allows using
 * a stream/event writer for outputting.
 *<p>
 * NOTE: Unfortunately there is no way to cleanly retrofit this interface
 * to actual implementation classes, so some casting is necessary to
 * make use of new features.
 */
public interface XMLEvent2 extends XMLEvent {
    void writeUsing(XMLStreamWriter2 w) throws XMLStreamException;
}
