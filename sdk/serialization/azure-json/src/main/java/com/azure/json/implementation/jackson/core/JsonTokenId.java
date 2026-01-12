// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

/**
 * Interface defined to contain ids accessible with {@link JsonToken#id()}.
 * Needed because it is impossible to define these constants in
 * {@link JsonToken} itself, as static constants (oddity of how Enums
 * are implemented by JVM).
 *
 * @since 2.3
 */
public interface JsonTokenId {
    /**
     * Id used to represent {@link JsonToken#NOT_AVAILABLE}, used in
     * cases where a token may become available when more input
     * is available: this occurs in non-blocking use cases.
     */
    int ID_NOT_AVAILABLE = -1;

    /**
     * Id used to represent {@link JsonToken#START_OBJECT}
     */
    int ID_START_OBJECT = 1;

    /**
     * Id used to represent {@link JsonToken#END_OBJECT}
     */
    int ID_END_OBJECT = 2;

    /**
     * Id used to represent {@link JsonToken#START_ARRAY}
     */
    int ID_START_ARRAY = 3;

    /**
     * Id used to represent {@link JsonToken#END_ARRAY}
     */
    int ID_END_ARRAY = 4;

    /**
     * Id used to represent {@link JsonToken#FIELD_NAME}
     */
    int ID_FIELD_NAME = 5;

    /**
     * Id used to represent {@link JsonToken#VALUE_STRING}
     */
    int ID_STRING = 6;

    /**
     * Id used to represent {@link JsonToken#VALUE_NUMBER_INT}
     */
    int ID_NUMBER_INT = 7;

    /**
     * Id used to represent {@link JsonToken#VALUE_NUMBER_FLOAT}
     */
    int ID_NUMBER_FLOAT = 8;

    /**
     * Id used to represent {@link JsonToken#VALUE_TRUE}
     */
    int ID_TRUE = 9;

    /**
     * Id used to represent {@link JsonToken#VALUE_FALSE}
     */
    int ID_FALSE = 10;
    /**
     * Id used to represent {@link JsonToken#VALUE_NULL}
     */

    int ID_NULL = 11;

    /**
     * Id used to represent {@link JsonToken#VALUE_EMBEDDED_OBJECT}
     */
    int ID_EMBEDDED_OBJECT = 12;
}
