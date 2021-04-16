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

    private List<Integer> dtdlVersions;
    private boolean isAbstract;
    private boolean isOvert;
    private boolean isPartition;
    private String parentClass;
    private List<String> typeIds;
    private Map<Integer, List<String>> concreteSubclasses;
    private Map<Integer, List<String>> elementalSubclasses;
    private Map<Integer, List<String>> extensibleMaterialSubclasses;
    private Map<Integer, List<String>> standardElementIds;
    private Map<Integer, String> badTypeCauseFormat;
    private Map<Integer, String> badTypeActionFormat;
    private Map<String, MaterialPropertyDigest> properties;
    //TODO: azabbasi: Instance.

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
    }

    public List<Integer> getDtdlVersions() {
        return dtdlVersions;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isOvert() {
        return isOvert;
    }

    public boolean isPartition() {
        return isPartition;
    }

    public String getParentClass() {
        return parentClass;
    }

    public List<String> getTypeIds() {
        return typeIds;
    }

    public Map<Integer, List<String>> getConcreteSubclasses() {
        return concreteSubclasses;
    }

    public Map<Integer, List<String>> getElementalSubclasses() {
        return elementalSubclasses;
    }

    public Map<Integer, List<String>> getExtensibleMaterialSubclasses() {
        return extensibleMaterialSubclasses;
    }

    public Map<Integer, List<String>> getStandardElementIds() {
        return standardElementIds;
    }

    public Map<Integer, String> getBadTypeCauseFormat() {
        return badTypeCauseFormat;
    }

    public Map<Integer, String> getBadTypeActionFormat() {
        return badTypeActionFormat;
    }

    public Map<String, MaterialPropertyDigest> getProperties() {
        return properties;
    }

    private Map<String, MaterialPropertyDigest> findAndCreateProperties(JsonNode materialPropertyObj) {
        HashMap<String, MaterialPropertyDigest> map = new HashMap<>();

        for (Iterator<String> it = materialPropertyObj.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            map.put(fieldName, new MaterialPropertyDigest(materialPropertyObj.get(fieldName)));
        }

        return map;
    }
}
