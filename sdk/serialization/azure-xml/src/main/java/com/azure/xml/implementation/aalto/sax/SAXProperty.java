// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.sax;

/**
 * Enumeration for listing all currently (SAX 2.0.2) defined standard
 * properties.
 */
public enum SAXProperty {
    DECLARATION_HANDLER("declaration-handler"),
    DOCUMENT_XML_VERSION("document-xml-version"),
    DOM_NODE("dom-node"),
    LEXICAL_HANDLER("lexical-handler"),
    XML_STRING("xml-string");

    public final static String STD_PROPERTY_PREFIX = "http://xml.org/sax/properties/";

    private final String mSuffix;

    SAXProperty(String suffix) {
        mSuffix = suffix;
    }

    public String getSuffix() {
        return mSuffix;
    }

    public String toExternal() {
        return STD_PROPERTY_PREFIX + mSuffix;
    }
}
