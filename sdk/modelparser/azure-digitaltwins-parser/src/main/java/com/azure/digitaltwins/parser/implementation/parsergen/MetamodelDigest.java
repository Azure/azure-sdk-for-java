// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Class that abstracts information extracted from the meta-model digest provided by the meta-parser.
 */
public class MetamodelDigest {
    private final List<Integer> dtdlVersions;
    private final Map<String, Map<String, String>> contexts;
    private final String baseClass;
    private final List<String> partitionClasses;
    private final Map<Integer, List<String>> rootableClasses;
    private final Map<Integer, StringRestriction> identifierDefinitionRestrictions;
    private final Map<Integer, StringRestriction> identifierReferenceRestrictions;
    private final List<Integer> dtdlVersionsAllowingLocalTerms;
    private final Map<String, Map<Integer, StringRestriction>> classIdentifierDefinitionRestrictions;
    private final List<String> extensionKinds;
    private final Map<Integer, List<String>> extensibleMaterialClasses;
    private final Map<String, MaterialClassDigest> materialClasses;
    private final List<DescendantControlDigest> descendantControls;
    private final Map<String, SupplementalTypeDigest> supplementalTypes;
    private final String elementsJsonText;

    /**
     * Initializes a new instance of the {@link MetamodelDigest} class.
     * @param digestText JSON text of the metamodel digest from the meta-parser.
     */
    public MetamodelDigest(String digestText) throws JsonProcessingException {
        JsonNode digest = new ObjectMapper().readValue(digestText, JsonNode.class);

        this.dtdlVersions = JsonNodeHelper.getArrayValues(digest, DtdlStrings.DTDL_VERSIONS, Integer.class);
        this.contexts = findAndCreateContext(digest);
        this.baseClass = JsonNodeHelper.getTextValue(digest, DtdlStrings.BASE_CLASS);
        this.partitionClasses = JsonNodeHelper.getArrayValues(digest, DtdlStrings.PARTITION_CLASSES, String.class);
        this.rootableClasses = findAndCreateRootableClasses(digest);
        this.identifierDefinitionRestrictions = findAndCreateIdentifierDefinitionRestrictions(digest);
        this.identifierReferenceRestrictions = findAndCreateIdentifierRefRestrictions(digest);
        this.dtdlVersionsAllowingLocalTerms = JsonNodeHelper.getArrayValues(digest, DtdlStrings.DTDL_VERSIONS_ALLOWING_LOCAL_TERMS, Integer.class);
        this.classIdentifierDefinitionRestrictions = findAndCreateClassIdDefRestrictions(digest);
        this.extensionKinds = JsonNodeHelper.getArrayValues(digest, DtdlStrings.EXTENSION_KINDS, String.class);
        this.extensibleMaterialClasses = findAndCreateExtensibleMaterialClasses(digest);
        this.materialClasses = findAndCreateMaterialClasses(digest);
        this.descendantControls = findAndCreateDescendantControls(digest);
        this.supplementalTypes = findAndCreateSupplementalTypes(digest);
        this.elementsJsonText = digest.get(DtdlStrings.ELEMENTS).asText();
    }

    /**
     * @return Gets a list of DTDL (major) version numbers defined in the meta-model digest.
     */
    public List<Integer> getDtdlVersions() {
        return this.dtdlVersions;
    }

    /**
     * @return Gets a dictionary that maps from a context ID to a dictionary of term definitions.
     */
    public Map<String, Map<String, String>> getContexts() {
        return this.contexts;
    }

    /**
     * @return Gets the name of the base class defined in the meta-model digest.
     */
    public String getBaseClass() {
        return this.baseClass;
    }

    /**
     * @return Gets a list of partition classes defined in the meta-model digest.
     */
    public List<String> getPartitionClasses() {
        return this.partitionClasses;
    }

    /**
     * @return Gets a map that maps from DTDL version to a list of rootable classes defined in the meta-model digest.
     */
    public Map<Integer, List<String>> getRootableClasses() {
        return this.rootableClasses;
    }

    /**
     * @return Gets a map that maps from DTDL version to a {@link StringRestriction} object that describes restrictions on identifiers used in element definitions.
     */
    public Map<Integer, StringRestriction> getIdentifierDefinitionRestrictions() {
        return this.identifierDefinitionRestrictions;
    }

    /**
     * @return Gets a map that maps from DTDL version to a {@link StringRestriction} object that describes restrictions on identifiers used in element references.
     */
    public Map<Integer, StringRestriction> getIdentifierReferenceRestrictions() {
        return this.identifierReferenceRestrictions;
    }

    /**
     * @return Gets a list of DTDL versions that allow local term definitions in context blocks.
     */
    public List<Integer> getDtdlVersionsAllowingLocalTerms() {
        return this.dtdlVersionsAllowingLocalTerms;
    }

    /**
     * @return Gets a map that maps from class name to a dictionary that maps from DTDL version to a {@link StringRestriction} object that describes restrictions on identifiers used in specific classes of definitions.
     */
    public Map<String, Map<Integer, StringRestriction>> getClassIdentifierDefinitionRestrictions() {
        return this.classIdentifierDefinitionRestrictions;
    }

    /**
     * @return Gets a list of strings that indicate the extension points defined in the meta-model digest.
     */
    public List<String> getExtensionKinds() {
        return this.extensionKinds;
    }

    /**
     * @return Gets a map that maps from DTDL version to a list of strings that each indicate a material class that is extensible.
     */
    public Map<Integer, List<String>> getExtensibleMaterialClasses() {
        return this.extensibleMaterialClasses;
    }

    /**
     * @return Gets a map that maps from class name to a {@link MaterialClassDigest} object providing details about the named material class.
     */
    public Map<String, MaterialClassDigest> getMaterialClasses() {
        return this.materialClasses;
    }

    /**
     * @return Gets a list of {@link DescendantControlDigest} objects that each describe a descendant control defined in the metamodel digest.
     */
    public List<DescendantControlDigest> getDescendantControls() {
        return this.descendantControls;
    }

    /**
     * @return Gets a map that maps from type URI to a {@link SupplementalTypeDigest} object providing details about the identified supplemental type.
     */
    public Map<String, SupplementalTypeDigest> getSupplementalTypes() {
        return this.supplementalTypes;
    }

    /**
     * @return Gets the JSON text of the 'elements' property in the meta-model digest.
     */
    public String getElementsJsonText() {
        return this.elementsJsonText;
    }

    private Map<String, SupplementalTypeDigest> findAndCreateSupplementalTypes(JsonNode digest) {
        JsonNode supplementalTypes = digest.get(DtdlStrings.SUPPLEMENTAL_TYPES);
        Map<String, SupplementalTypeDigest> result = new HashMap<>();

        for (Iterator<String> it = supplementalTypes.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            result.put(fieldName, new SupplementalTypeDigest(supplementalTypes.get(fieldName)));
        }

        return result;
    }

    private List<DescendantControlDigest> findAndCreateDescendantControls(JsonNode digest) {
        JsonNode descendantControls = digest.get(DtdlStrings.DESCENDANT_CONTROLS);

        List<DescendantControlDigest> result = new ArrayList<>();
        for (JsonNode node : descendantControls) {
            result.add(new DescendantControlDigest(node));
        }

        return result;
    }

    private Map<String, MaterialClassDigest> findAndCreateMaterialClasses(JsonNode digest) {
        JsonNode materialClasses = digest.get(DtdlStrings.MATERIAL_CLASSES);
        Map<String, MaterialClassDigest> result = new HashMap<>();

        for (Iterator<String> it = materialClasses.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            result.put(fieldName, new MaterialClassDigest(materialClasses.get(fieldName)));
        }

        return result;
    }

    private Map<Integer, List<String>> findAndCreateExtensibleMaterialClasses(JsonNode digest) {
        JsonNode extensibleClassNode = digest.get(DtdlStrings.EXTENSIBLE_MATERIAL_CLASSES);
        Map<Integer, List<String>> result = new HashMap<>();

        for (Iterator<String> it = extensibleClassNode.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            result.put(Integer.parseInt(fieldName), JsonNodeHelper.getArrayValues(extensibleClassNode, fieldName, String.class));
        }

        return result;
    }

    private Map<String, Map<Integer, StringRestriction>> findAndCreateClassIdDefRestrictions(JsonNode digest) {
        JsonNode identifierDefRestriction = digest.get(DtdlStrings.IDENTIFIER_REFERENCE);
        HashMap<String, Map<Integer, StringRestriction>> result = new HashMap<>();

        for (Iterator<String> it = identifierDefRestriction.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            if (!JsonNodeHelper.isNumeric(fieldName)) {
                JsonNode innerNode = identifierDefRestriction.get(fieldName);
                HashMap<Integer, StringRestriction> innerMap = new HashMap<>();
                for (Iterator<String> inner = identifierDefRestriction.get(fieldName).fieldNames(); inner.hasNext();) {
                    String innerFieldName = inner.next();
                    innerMap.put(Integer.parseInt(innerFieldName), new StringRestriction(innerNode.get(innerFieldName)));
                }

                result.put(fieldName, innerMap);
            }
        }

        return result;
    }

    private Map<Integer, StringRestriction> findAndCreateIdentifierRefRestrictions(JsonNode digest) {
        JsonNode identifierRefRestriction = digest.get(DtdlStrings.IDENTIFIER_REFERENCE);
        HashMap<Integer, StringRestriction> result = new HashMap<>();

        for (Iterator<String> it = identifierRefRestriction.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            result.put(Integer.parseInt(fieldName), new StringRestriction(digest.get(fieldName)));
        }

        return result;
    }

    private Map<Integer, StringRestriction> findAndCreateIdentifierDefinitionRestrictions(JsonNode digest) {
        JsonNode identifierDefRestriction = digest.get(DtdlStrings.IDENTIFIER_DEFINITION);
        HashMap<Integer, StringRestriction> result = new HashMap<>();

        for (Iterator<String> it = identifierDefRestriction.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            if (JsonNodeHelper.isNumeric(fieldName)) {
                result.put(Integer.parseInt(fieldName), new StringRestriction(digest.get(fieldName)));
            }
        }

        return result;
    }

    private Map<String, Map<String, String>> findAndCreateContext(JsonNode digest) {
        JsonNode contextNode = digest.get(DtdlStrings.CONTEXTS);
        HashMap<String, Map<String, String>> result = new HashMap<>();

        for (Iterator<String> it = contextNode.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            HashMap<String, String> innerResult = new HashMap<>();
            JsonNode innerNode = contextNode.get(fieldName);
            for (Iterator<String> inner = innerNode.fieldNames(); inner.hasNext();) {
                String innerFieldName = inner.next();
                innerResult.put(innerFieldName, innerNode.get(innerFieldName).textValue());
            }

            result.put(fieldName, innerResult);
        }

        return result;
    }

    private Map<Integer, List<String>> findAndCreateRootableClasses(JsonNode digest) {
        JsonNode contextNode = digest.get(DtdlStrings.ROOTABLE_CLASSES);
        HashMap<Integer, List<String>> result = new HashMap<>();

        for (Iterator<String> it = contextNode.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            List<String> innerList = new ArrayList<>();

            JsonNode innerNode = contextNode.get(fieldName);

            for (JsonNode node : innerNode) {
                innerList.add(node.textValue());
            }

            result.put(Integer.parseInt(fieldName), innerList);
        }

        return result;
    }
}
