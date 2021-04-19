// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

/**
 * Class that abstracts material class information extracted from the metamodel digest provided by the meta-parser.
 */
public class MaterialClassDigest {

    private final List<Integer> dtdlVersions;
    private final boolean isAbstract;
    private final boolean isOvert;
    private final boolean isPartition;
    private final String parentClass;
    private final List<String> typeIds;
    private final Map<Integer, List<String>> concreteSubclasses;
    private final Map<Integer, List<String>> elementalSubclasses;
    private final Map<Integer, List<String>> extensibleMaterialSubclasses;
    private final Map<Integer, String> badTypeCauseFormat;
    private final Map<Integer, String> badTypeActionFormat;
    private final Map<String, MaterialPropertyDigest> properties;
    private final InstanceValidationDigest instance;
    private Map<Integer, List<String>> standardElementIds;

    /**
     * Initializes a new instance of the {@link MaterialClassDigest} class.
     */
    public MaterialClassDigest() {
        this.dtdlVersions = new ArrayList<>();
        this.isAbstract = false;
        this.isOvert = false;
        this.isPartition = false;
        this.parentClass = null;
        this.typeIds = new ArrayList<>();
        this.concreteSubclasses = new HashMap<>();
        this.elementalSubclasses = new HashMap<>();
        this.extensibleMaterialSubclasses = new HashMap<>();
        this.standardElementIds = new HashMap<>();
        this.badTypeActionFormat = new HashMap<>();
        this.badTypeCauseFormat = new HashMap<>();
        this.properties = new HashMap<>();
        this.instance = null;
    }

    /**
     * Initializes a new instance of the {@link MaterialClassDigest} class.
     * @param materialClassObject A {@link JsonNode} from the meta-model digest containing information about a material class.
     */
    public MaterialClassDigest(JsonNode materialClassObject) {
        this.isAbstract = JsonNodeHelper.getNotNullableBooleanValue(materialClassObject, DtdlStrings.ABSTRACT);
        this.isOvert = JsonNodeHelper.getNotNullableBooleanValue(materialClassObject, DtdlStrings.OVERT);
        this.isPartition = JsonNodeHelper.getNotNullableBooleanValue(materialClassObject, DtdlStrings.PARTITION);

        this.parentClass = JsonNodeHelper.getTextValue(materialClassObject, DtdlStrings.PARENT_CLASS);

        this.typeIds = JsonNodeHelper.getArrayValues(materialClassObject, DtdlStrings.TYPE_IDS, String.class);
        this.dtdlVersions = JsonNodeHelper.getArrayValues(materialClassObject, DtdlStrings.DTDL_VERSIONS, Integer.class);

        this.concreteSubclasses = JsonNodeHelper.getDictionaryOfListsValues(materialClassObject, DtdlStrings.CONCRETE_SUB_CLASSES, Integer.class, String.class);
        this.elementalSubclasses = JsonNodeHelper.getDictionaryOfListsValues(materialClassObject, DtdlStrings.ELEMENTAL_SUB_CLASSES, Integer.class, String.class);
        this.extensibleMaterialSubclasses = JsonNodeHelper.getDictionaryOfListsValues(materialClassObject, DtdlStrings.EXTENSIBLE_MATERIAL_SUB_CLASSES, Integer.class, String.class);
        this.standardElementIds = JsonNodeHelper.getDictionaryOfListsValues(materialClassObject, DtdlStrings.STANDARD_ELEMENT_IDS, Integer.class, String.class);

        if (this.standardElementIds == null) {
            this.standardElementIds = new HashMap<>();
        }

        this.badTypeCauseFormat = JsonNodeHelper.getDictionaryOfSingularValues(materialClassObject, DtdlStrings.BAD_TYPE_CAUSE_FORMAT, Integer.class, String.class);
        this.badTypeActionFormat = JsonNodeHelper.getDictionaryOfSingularValues(materialClassObject, DtdlStrings.BAD_TYPE_ACTION_FORMAT, Integer.class, String.class);
        this.properties = findAndCreateProperties(materialClassObject.get(DtdlStrings.PROPERTIES));

        JsonNode instanceNode = materialClassObject.get(DtdlStrings.INSTANCE);
        this.instance =  instanceNode != null ? new InstanceValidationDigest(instanceNode) : null;
    }

    /**
     *
     * @return Gets a list of DTDL versions in which the class has been defined.
     */
    public List<Integer> getDtdlVersions() {
        return this.dtdlVersions;
    }

    /**
     *
     * @return Gets a value indicating whether the obverse class is to be abstract according to the meta-model.
     */
    public boolean isAbstract() {
        return this.isAbstract;
    }

    /**
     *
     * @return Gets a value indicating whether the DTDL type is permitted to be used in a DTDL model.
     */
    public boolean isOvert() {
        return this.isOvert;
    }

    /**
     *
     * @return Gets a value indicating whether the class is designated as a partition type in the meta-model.
     */
    public boolean isPartition() {
        return this.isPartition;
    }

    /**
     *
     * @return Gets the parent class of the class.
     */
    public String getParentClass() {
        return this.parentClass;
    }

    /**
     *
     * @return Gets a list of type URIs of which the class is a sub-type.
     */
    public List<String> getTypeIds() {
        return this.typeIds;
    }

    /**
     *
     * @return Gets a map that maps from DTDL version to a list of type URIs that are subclasses of the class.
     */
    public Map<Integer, List<String>> getConcreteSubclasses() {
        return this.concreteSubclasses;
    }

    /**
     *
     * @return Gets a map that maps from DTDL version to a list of type URIs that are subclasses of the class and which have any instances that are standard elements.
     */
    public Map<Integer, List<String>> getElementalSubclasses() {
        return this.elementalSubclasses;
    }

    /**
     *
     * @return Gets a map that maps from DTDL version to a list of type URIs that are subclasses of the class and which are both extensible and material.
     */
    public Map<Integer, List<String>> getExtensibleMaterialSubclasses() {
        return this.extensibleMaterialSubclasses;
    }

    /**
     *
     * @return Gets a map that maps from DTDL version to a list of IDs of standard elements that are instances of the class.
     */
    public Map<Integer, List<String>> getStandardElementIds() {
        return this.standardElementIds;
    }

    /**
     *
     * @return Gets a map that maps from DTDL version to a string that describes the cause of a bad type error.
     */
    public Map<Integer, String> getBadTypeCauseFormat() {
        return this.badTypeCauseFormat;
    }

    /**
     *
     * @return Gets a map that maps from DTDL version to a string that describes the action that will resolve a bad type error.
     */
    public Map<Integer, String> getBadTypeActionFormat() {
        return this.badTypeActionFormat;
    }

    /**
     *
     * @return ets a dictionary that maps from property name to a {@link MaterialPropertyDigest} object providing details about the property.
     */
    public Map<String, MaterialPropertyDigest> getProperties() {
        return this.properties;
    }

    /**
     *
     * @return Gets a {@link InstanceValidationDigest} providing instance validation criteria for the DTDL type.
     */
    public InstanceValidationDigest getInstance() {
        return this.instance;
    }

    private Map<String, MaterialPropertyDigest> findAndCreateProperties(JsonNode materialPropertyObj) {
        HashMap<String, MaterialPropertyDigest> map = new HashMap<>();

        for (Iterator<String> it = materialPropertyObj.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            map.put(
                fieldName,
                new MaterialPropertyDigest(materialPropertyObj.get(fieldName)));
        }

        return map;
    }
}
