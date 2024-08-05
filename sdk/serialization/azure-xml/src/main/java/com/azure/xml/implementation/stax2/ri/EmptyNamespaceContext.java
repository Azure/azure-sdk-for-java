// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.Collections;
import java.util.Iterator;

/**
 * Dummy {@link NamespaceContext} implementation that contains no
 * namespace information, except bindings that are specified by
 * the namespace specification itself (for prefixes "xml" and "xmlns")
 */
public class EmptyNamespaceContext implements NamespaceContext {
    final static EmptyNamespaceContext sInstance = new EmptyNamespaceContext();

    private EmptyNamespaceContext() {
    }

    public static EmptyNamespaceContext getInstance() {
        return sInstance;
    }

    /*
    /**********************************************************************
    /* NamespaceContext API
    /**********************************************************************
     */

    @Override
    public final String getNamespaceURI(String prefix) {
        /* First the known offenders; invalid args, 2 predefined xml namespace
         * prefixes
         */
        if (prefix == null) {
            throw new IllegalArgumentException("Illegal to pass null/empty prefix as argument.");
        }
        if (!prefix.isEmpty()) {
            if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
                return XMLConstants.XML_NS_URI;
            }
            if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            }
        }
        return null;
    }

    @Override
    public String getPrefix(String nsURI) {
        /* First the known offenders; invalid args, 2 predefined xml namespace
         * prefixes
         */
        if (nsURI == null || nsURI.isEmpty()) {
            throw new IllegalArgumentException("Illegal to pass null/empty URI as argument.");
        }
        if (nsURI.equals(XMLConstants.XML_NS_URI)) {
            return XMLConstants.XML_NS_PREFIX;
        }
        if (nsURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(String nsURI) {
        /* First the known offenders; invalid args, 2 predefined xml namespace
         * prefixes
         */
        if (nsURI == null || nsURI.isEmpty()) {
            throw new IllegalArgumentException("Illegal to pass null/empty prefix as argument.");
        }
        if (nsURI.equals(XMLConstants.XML_NS_URI)) {
            return Collections.singletonList(XMLConstants.XML_NS_PREFIX).iterator();
        }
        if (nsURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return Collections.singletonList(XMLConstants.XMLNS_ATTRIBUTE).iterator();
        }
        return Collections.emptyIterator();
    }
}
