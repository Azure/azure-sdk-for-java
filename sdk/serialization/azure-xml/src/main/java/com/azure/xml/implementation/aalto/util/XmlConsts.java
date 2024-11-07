// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

/**
 * Simple constant container interface, shared by input and output
 * sides.
 *
 * @author Tatu Saloranta
 */
public interface XmlConsts {
    // // // Constants for XML declaration

    String XML_DECL_KW_ENCODING = "encoding";
    String XML_DECL_KW_VERSION = "version";
    String XML_DECL_KW_STANDALONE = "standalone";

    String XML_V_10_STR = "1.0";
    String XML_V_11_STR = "1.1";

    /**
     * This constants refers to cases where the version has not been
     * declared explicitly; and needs to be considered to be 1.0.
     */
    int XML_V_UNKNOWN = 0x0000;
    int XML_V_10 = 0x0100;
    int XML_V_11 = 0x0110;

    String XML_SA_YES = "yes";
    String XML_SA_NO = "no";

    /**
     * This constant defines the highest Unicode character allowed
     * in XML content.
     */
    int MAX_UNICODE_CHAR = 0x10FFFF;

    /*
    ////////////////////////////////////////////////////////////
    // Char consts
    ////////////////////////////////////////////////////////////
     */

    char CHAR_NULL = (char) 0;
    char CHAR_SPACE = (char) 0x0020;

    char CHAR_CR = '\r';
    char CHAR_LF = '\n';

    /*
    ////////////////////////////////////////////////////////////
    // Stax defaults:
    ////////////////////////////////////////////////////////////
     */

    String STAX_DEFAULT_OUTPUT_ENCODING = "UTF-8";

    String STAX_DEFAULT_OUTPUT_VERSION = "1.0";
}
