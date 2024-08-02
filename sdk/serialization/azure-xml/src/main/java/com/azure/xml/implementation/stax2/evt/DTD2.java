// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.evt;

import javax.xml.stream.events.DTD;

/**
 * Interface that extends basic {@link DTD} with methods that are
 * necessary to completely reproduce actual DOCTYPE declaration
 * constructs in xml documents.
 */
public interface DTD2 extends DTD {
    String getRootName();

    String getSystemId();

    String getPublicId();

    String getInternalSubset();
}
