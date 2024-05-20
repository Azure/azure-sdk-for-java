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
    END_ELEMENT
}
