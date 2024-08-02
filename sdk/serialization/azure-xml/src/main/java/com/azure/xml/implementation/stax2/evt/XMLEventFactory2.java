// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.evt;

import javax.xml.stream.XMLEventFactory;

/**
 * Interface that adds missing (but required) methods to
 * {@link XMLEventFactory}; especially ones for creating actual
 * well-behaving DOCTYPE events.
 */
public abstract class XMLEventFactory2 extends XMLEventFactory {
    protected XMLEventFactory2() {
        super();
    }

    public abstract DTD2 createDTD(String rootName, String sysId, String pubId, String intSubset);

    public abstract DTD2 createDTD(String rootName, String sysId, String pubId, String intSubset, Object processedDTD);
}
