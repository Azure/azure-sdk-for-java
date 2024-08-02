// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

/**
 * Specialized interface that covers schema features unique to DTDs.
 * Necessary to have basic level of support for entities and notations.
 */
public interface DTDValidationSchema extends XMLValidationSchema {
    /*
    //////////////////////////////////////////////////////////
    // Entity support
    //////////////////////////////////////////////////////////
     */

    /**
     * @return Number of general (non-parameter) entities (of all types)
     *    declared in this DTD
     *    schema (in subsets [internal, external] included)
     */
    int getEntityCount();

    /**
     * @return True if a general entity with the specified name was
     *   defined in this dtd
     */
    //public boolean isEntityDefined(String name);

    /**
     * @return Index of the entity with given name (between 0 and
     *   <code>getEntityCount()-1</code>)
     *   if such general entity
     *   was defined in this dtd; -1 if not.
     */
    //public int findEntity(String name);

    //public String getEntityName(int index);

    //public boolean isEntityParsed(int index);
    //public boolean isEntityExternal(int index);

    //public String getEntityPublicId(int index);
    //public String getEntitySystemId(int index);
    //public String getNotationIdOfEntity(int entityIndex);
    //public String getEntityExpansionText(int index);

    /*
    //////////////////////////////////////////////////////////
    // Notation support
    //////////////////////////////////////////////////////////
     */

    /**
     * @return Number of notations declared in this DTD
     *    schema (in subsets [internal, external] included)
     */
    int getNotationCount();

    //public boolean isNotationDefined(String name);

    /**
     * @return Index of the notation with given name (between 0 and
     *   <code>getNotationtyCount()-1</code>)
     *   if such notation was defined in this dtd; -1 if not.
     */
    //public int findNotation(String name);

    //public String getNotationName(int index);
    //public String getNotationPublicId(int index);
    //public String getNotationSystemId(int index);
}
