// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.evt;

import javax.xml.stream.events.NotationDeclaration;

/**
 * Interface that extends basic {@link NotationDeclaration} to add
 * support for handling Base URI needed to resolve Notation references.
 * This
 */
public interface NotationDeclaration2 extends NotationDeclaration {
    String getBaseURI();
}
