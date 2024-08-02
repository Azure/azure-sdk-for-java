// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2;

/**
 * Interface that specifies additional access methods for accessing
 * combined attribute information reader has, after parsing individual
 * and combining that with DTD information (if any available; depends on
 * parser's mode).
 *<p>
 * Note: instances of AttributeInfo are only guaranteed to persist as long
 * as the (stream) reader points to the START_ELEMENT event, during
 * which attribute information was parsed. It is possible that some
 * implementations persist instances afterwards, but it's equally
 * possible that instances get reused, and as such information
 * may change.
 */
public interface AttributeInfo {
    // // // Generic methods; some duplication from main stream reader

    /**
     * @return Number of all attributes accessible (including ones created
     *   from the default values, if any) using this Object.
     */
    int getAttributeCount();

    // // // Methods for finding index of specific attributes

    /**
     * @return Index of the specified attribute, if the current element
     *   has such an attribute (explicit, or one created via default
     *   value expansion); -1 if not.
     */
    int findAttributeIndex(String nsURI, String localName);

    /**
     * Returns the index of the id attribute (attribute with any name,
     * type ID from DTD) of current (start) element, if any. Note that
     * DTD only allows at most one such attribute per element.
     *
     * @return Index of the ID attribute of current element,
     *   if the current element has such an
     *   attribute defined; -1 if not.
     */
    int getIdAttributeIndex();

    /**
     * Returns the index of the notation attribute (attribute with any name,
     * type NOTATION from DTD) of current (start) element, if any. Note that
     * DTD only allows at most one such attribute per element.
     *
     * @return Index of the NOTATION attribute of current element,
     *   if the current element has such an
     *   attribute defined; -1 if not.
     */
    int getNotationAttributeIndex();
}
