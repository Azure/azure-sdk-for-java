// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

/**
 * Token types used when reading XML content.
 */
public enum XmlToken {
    /**
     * Indicates the start of an XML document.
     */
    START_DOCUMENT,

    /**
     * Indicates the end of an XML document.
     */
    END_DOCUMENT,

    /**
     * Indicates the start of an XML element.
     */
    START_ELEMENT,

    /**
     * Indicates the end of an XML element.
     */
    END_ELEMENT,

    /**
     * Indicates an XML element attribute.
     */
    ATTRIBUTE,

    /**
     * Indicates a namespace declaration.
     */
    NAMESPACE,

    /**
     * Indicates XML characters, or text.
     */
    CHARACTERS,

    /**
     * Indicates an XML CData.
     */
    CDATA,

    /**
     * Indicates a comment.
     */
    COMMENT,

    /**
     * Indicates ignorable whitespace.
     */
    SPACE,

    /**
     * Indicates a processing instruction.
     */
    PROCESSING_INSTRUCTION,

    /**
     * Indicates an entity declaration.
     */
    ENTITY_DECLARATION,

    /**
     * Indicates an entity reference.
     */
    ENTITY_REFERENCE,

    /**
     * Indicates a notation declaration.
     */
    NOTATION_DECLARATION,

    /**
     * Indicates a document type declaration.
     */
    DTD
}
