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

    // // // Methods for finding index of specific attributes

    /**
     * @return Index of the specified attribute, if the current element
     *   has such an attribute (explicit, or one created via default
     *   value expansion); -1 if not.
     */
    int findAttributeIndex(String nsURI, String localName);

}
