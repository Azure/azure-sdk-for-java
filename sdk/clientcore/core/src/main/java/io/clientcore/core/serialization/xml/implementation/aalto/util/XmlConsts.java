// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package io.clientcore.core.serialization.xml.implementation.aalto.util;

/**
 * Simple constant container class, shared by input and output
 * sides.
 *
 * @author Tatu Saloranta
 */
public final class XmlConsts {
    // // // Re-declared properties from XMLInputFactory

    /**
     * Property that can be set to indicate that namespace information is
     * to be handled in conformance to the xml namespaces specifiation; or
     * false to indicate no namespace handling should be done.
     */
    public static final String XSP_NAMESPACE_AWARE = "javax.xml.stream.isNamespaceAware";

    /**
     * Property that can be set to specify a problem handler which will get
     * notified of non-fatal problem (validation errors in non-validating mode,
     * warnings). Its value has to be of type
     * {@link javax.xml.stream.XMLReporter}
     */
    public static final String XSP_PROBLEM_REPORTER = "javax.xml.stream.reporter";

    // // // Constants for XML declaration

    public static final String XML_DECL_KW_ENCODING = "encoding";
    public static final String XML_DECL_KW_VERSION = "version";
    public static final String XML_DECL_KW_STANDALONE = "standalone";

    public static final String XML_V_10_STR = "1.0";
    public static final String XML_V_11_STR = "1.1";

    /**
     * This constants refers to cases where the version has not been
     * declared explicitly; and needs to be considered to be 1.0.
     */
    public static final int XML_V_UNKNOWN = 0x0000;
    public static final int XML_V_10 = 0x0100;
    public static final int XML_V_11 = 0x0110;

    public static final String XML_SA_YES = "yes";
    public static final String XML_SA_NO = "no";

    /**
     * This constant defines the highest Unicode character allowed
     * in XML content.
     */
    public static final int MAX_UNICODE_CHAR = 0x10FFFF;

    /*
    ////////////////////////////////////////////////////////////
    // Char consts
    ////////////////////////////////////////////////////////////
     */

    public static final char CHAR_NULL = (char) 0;
    public static final char CHAR_SPACE = (char) 0x0020;

    public static final char CHAR_CR = '\r';
    public static final char CHAR_LF = '\n';

    private XmlConsts() {
    }
}
