// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.sax;

/**
 * Enumeration for listing all currently (SAX 2.0.2) defined standard
 * features
 */
public enum SAXFeature {
    EXTERNAL_GENERAL_ENTITIES("external-general-entities"),
    EXTERNAL_PARAMETER_ENTITIES("external-parameter-entities"),
    IS_STANDALONE("is-standalone"),
    LEXICAL_HANDLER_PARAMETER_ENTITIES("lexical-handler/parameter-entities"),
    NAMESPACES("namespaces"),
    NAMESPACE_PREFIXES("namespace-prefixes"),
    RESOLVE_DTD_URIS("resolve-dtd-uris"),
    STRING_INTERNING("string-interning"),
    UNICODE_NORMALIZATION_CHECKING("unicode-normalization-checking"),
    USE_ATTRIBUTES2("use-attributes2"),
    USE_LOCATOR2("use-locator2"),
    USE_ENTITY_RESOLVER2("use-entity-resolver2"),
    VALIDATION("validation"),
    XMLNS_URIS("xmlns-uris"),
    XML_1_1("xml-1.1");

    public final static String STD_FEATURE_PREFIX = "http://xml.org/sax/features/";

    private final String mSuffix;

    SAXFeature(String suffix) {
        mSuffix = suffix;
    }

    public String getSuffix() {
        return mSuffix;
    }

    public String toExternal() {
        return STD_FEATURE_PREFIX + mSuffix;
    }
}
