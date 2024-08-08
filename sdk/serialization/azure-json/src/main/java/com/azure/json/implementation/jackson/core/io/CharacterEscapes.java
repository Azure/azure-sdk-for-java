// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.io;

/**
 * Abstract base class that defines interface for customizing character
 * escaping aspects for String values, for formats that use escaping.
 * For JSON this applies to both property names and String values.
 */
@SuppressWarnings("serial")
public abstract class CharacterEscapes implements java.io.Serializable // since 2.1
{

    /**
     * Value used for lookup tables to indicate that matching characters
     * are to be escaped using standard escaping; for JSON this means
     * (for example) using "backslash - u" escape method.
     */
    public final static int ESCAPE_STANDARD = -1;
}
