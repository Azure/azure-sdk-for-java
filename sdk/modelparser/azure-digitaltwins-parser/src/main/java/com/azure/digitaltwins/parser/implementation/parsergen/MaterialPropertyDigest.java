package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * Class that abstracts material property information extracted from the metamodel digest provided by the meta-parser.
 */
public class MaterialPropertyDigest {



    private boolean isLiteral;
    private boolean isAbstract;
    private boolean isPlural;
    private boolean isOptional;
    private boolean isInherited;
    private boolean isShadowed;
    private boolean isKey;
    private boolean isSegment;

    private String classType;
    private String dictionaryKey;
    private String dataType;
    private String dtmiSegment;

    private Map<Integer, PropertyVersionDigest> propertyVersions;


    /***
     * Initializes a new instance of the {@link MaterialPropertyDigest} class.
     */
    public MaterialPropertyDigest() {

    }

    /***
     * Initializes a new instance of the {@link MaterialPropertyDigest} class.
     * @param materialPropertyObj A {@link JsonNode} from the meta-model digest containing information about a material property.
     */
    public MaterialPropertyDigest(JsonNode materialPropertyObj) {

    }

    /**
     * @return Gets a value indicating whether the property is a literal value.
     */
    public boolean isLiteral() {
        return this.isLiteral;
    }

    /**
     * @return Gets the class for an object property.
     */
    public String getClassType() {
        return this.classType;
    }

    /**
     * @return Gets the name of a property of a child element that is used for the dictionary key of the property.
     */
    public String getDictionaryKey() {
        return this.dictionaryKey;
    }

    /**
     * @return Gets a value indicating whether the type of the property is an abstract type.
     */
    public boolean isAbstract() {
        return this.isAbstract;
    }

    /**
     * @return Gets the datatype for a literal property.
     */
    public String getDataType() {
        return this.dataType;
    }

    /**
     * @return Gets a value indicating whether the property is plural.
     */
    public boolean isPlural() {
        return this.isPlural;
    }

    /**
     * @return Gets a value indicating whether the property is optional.
     */
    public boolean isOptional() {
        return this.isOptional;
    }

    /**
     * @return Gets the name of a property of a child element that is used to determine a segment for an auto-generated identifier.
     */
    public String getDtmiSegment() {
        return this.dtmiSegment;
    }

    /**
     * @return Gets a value indicating whether the property is inherited from a proper superclass.
     */
    public boolean isInherited() {
        return this.isInherited;
    }

    /**
     * @return Gets a value indicating whether the property has a shadow field to hold the original value if it is updatable via import.
     */
    public boolean isShadowed() {
        return this.isShadowed;
    }

    /**
     * @return Gets a value indicating whether the property is a literal whose value is the dictionary key of a parent property.
     */
    public boolean isKey() {
        return this.isKey;
    }

    /**
     * @return Gets a value indicating whether the property is a literal whose value determines a segment for an auto-generated identifier.
     */
    public boolean isSegment() {
        return this.isSegment;
    }

    /**
     * @return Gets a dictionary that maps from DTDL version to a <see cref="PropertyVersionDigest"/> object providing version-specific details about the property.
     */
    public Map<Integer, PropertyVersionDigest> getPropertyVersions() {
        return this.propertyVersions;
    }
}
