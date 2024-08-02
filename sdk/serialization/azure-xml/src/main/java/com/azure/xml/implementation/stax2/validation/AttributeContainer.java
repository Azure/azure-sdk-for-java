// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

/**
 * Interface that is used to allow validators to do attribute defaulting.
 * That is, to allow specific post-processing for elements, such that after
 * all attribute values that an xml element has have been processed, it
 * is possible to add new attribute/value pairs in cases where attribute
 * only has a default value in DTD or W3C Schema.
 */
public interface AttributeContainer {
    /**
     * @return Number of atributes container contains currently. Can be used
     *    to determine the index number for the next attribute to be added.
     */
    int getAttributeCount();

    /**
     * Method that can be used to add a new attribute value for an attribute
     * that was not yet contained by the container, as part of using attribute
     * default value mechanism.
     *<p>
     * Note: caller has to ensure that the addition would not introduce a
     * duplicate; attribute container implementation is not required to do
     * any validation on attribute name (local name, prefix, uri) or value.
     *
     * @return Index of the newly added attribute.
     */
    int addDefaultAttribute(String localName, String uri, String prefix, String value);
}
