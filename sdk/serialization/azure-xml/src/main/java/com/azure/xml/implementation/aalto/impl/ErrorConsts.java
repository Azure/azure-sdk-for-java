// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.impl;

import com.azure.xml.implementation.stax2.ri.Stax2Util;

import javax.xml.XMLConstants;

/**
 * This class contains various String constants used for error reporting.
 *<p>
 * Note: although messages are constants, they are not marked as finals;
 * this is intentional, to avoid inlining (same String might get embedded
 * in multiple using class files). May not matter in the end, but for
 * now done to optimize class file sizes.
 *
 * @author Tatu Saloranta
 */
public final class ErrorConsts {
    // // // Generic input errors:

    public static String ERR_NULL_ARG = "Illegal to pass null as argument";

    // // // Wrong reader state:

    public static String ERR_STATE_NOT_STELEM = "Current state not START_ELEMENT";
    public static String ERR_STATE_NOT_ELEM = "Current state not START_ELEMENT or END_ELEMENT";
    public static String ERR_STATE_NOT_PI = "Current state not PROCESSING_INSTRUCTION";

    // // // Error messaging, reader

    public static String SUFFIX_IN_PROLOG = " in prolog";
    public static String SUFFIX_IN_EPILOG = " in epilog";
    public static String SUFFIX_IN_TREE = " in xml document";

    public static String ERR_WF_PI_XML_TARGET
        = "Illegal processing instruction target: 'xml' (case insensitive) is reserved by the xml specification";

    //public static String ERR_WF_DUP_ATTRS = "Duplicate attribute \"{0}\" (index {1})";
    public static String ERR_WF_DUP_ATTRS = "Duplicate attributes \"{0}\" (index {1}), \"{2}\" (index {3})";

    // // // Errors, namespace binding

    public static String ERR_NS_REDECL_XML
        = "Trying to redeclare prefix 'xml' from its default URI '" + XMLConstants.XML_NS_URI + "' to \"{0}\"";

    public static String ERR_NS_REDECL_XMLNS = "Trying to declare prefix 'xmlns' (illegal as per NS 1.1 #4)";

    public static String ERR_NS_REDECL_XML_URI
        = "Trying to bind URI '" + XMLConstants.XML_NS_URI + " to prefix \"{0}\" (can only bind to 'xml')";

    public static String ERR_NS_REDECL_XMLNS_URI = "Trying to bind URI '" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI
        + " to prefix \"{0}\" (can not be explicitly bound)";

    public static String ERR_NS_EMPTY
        = "Non-default namespace can not map to empty URI (as per Namespace 1.0 # 2) in XML 1.0 documents";

    // // // Output problems:

    public static String WERR_PROLOG_CDATA
        = "Trying to output a CDATA block outside main element tree (in prolog or epilog)";
    public static String WERR_PROLOG_ENTITY
        = "Trying to output an entity reference outside main element tree (in prolog or epilog)";

    public static String WERR_PROLOG_SECOND_ROOT = "Trying to output second root, <{0}>";
    public static String WERR_PROLOG_NO_ROOT
        = "Trying to write END_DOCUMENT when document has no root (ie. trying to output empty document).";
    public static String WERR_DUP_XML_DECL
        = "Can not output XML declaration, after other output has already been done.";

    public static String WERR_CDATA_CONTENT = "Illegal input: CDATA block has embedded ']]>' in it (index {0})";
    public static String WERR_COMMENT_CONTENT = "Illegal input: comment content has embedded '--' in it (index {0})";
    public static String WERR_PI_CONTENT
        = "Illegal input: processing instruction content has embedded '?>' in it (index {0})";
    public static String WERR_NO_ESCAPING
        = "Illegal input: {0} contains a character (code {1}) that can only be output as character entity";
    public static String WERR_SPACE_CONTENT
        = "Illegal input: SPACE content has a non-whitespace character (code {0}) in it (index {1})";

    public static String WERR_ATTR_NO_ELEM = "Trying to write an attribute when there is no open start element.";
    public static String WERR_NS_NO_ELEM
        = "Trying to write a namespace declaration when there is no open start element.";

    public static String WERR_NAME_EMPTY = "Illegal to pass empty name";

    // // // Warning-related:

    // // Types of warnings we issue via XMLReporter

    public static String WT_XML_DECL = "xml declaration";

    // // Warning messages:

    public static String W_MIXED_ENCODINGS
        = "Inconsistent text encoding; declared as \"{0}\" in xml declaration, application had passed \"{1}\"";

    /*
    ////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////
     */

    public static String tokenTypeDesc(int type) {
        return Stax2Util.eventTypeDesc(type);
    }

    public static void throwInternalError() {
        throwInternalError(null);
    }

    public static void throwInternalError(String type) {
        String msg = "Internal error";
        if (type != null) {
            msg += ": " + type;
        }
        throw new RuntimeException(msg);
    }
}
