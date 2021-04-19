// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Class that abstracts supplemental type information extracted from the meta-model digest provided by the meta-parser.
 */
public class SupplementalTypeDigest {
    private final boolean isAbstract;
    private final String parent;
    private final String extensionKind;
    private final String extensionContext;
    private final List<String> coTypes;
    private final List<Integer> coTypeVersions;
    private final Map<String, SupplementalPropertyDigest> properties;
    private final List<SupplementalConstraintDigest> constraints;

    /**
     * Initializes a new instance of the {@link SupplementalTypeDigest} class.
     * @param supplementalTypeObject A {@link JsonNode} from the metamodel digest containing information about a supplemental type.
     */
    public SupplementalTypeDigest(JsonNode supplementalTypeObject) {
        this.isAbstract = JsonNodeHelper.getNotNullableBooleanValue(supplementalTypeObject, DtdlStrings.ABSTRACT);
        this.parent = JsonNodeHelper.getTextValue(supplementalTypeObject, DtdlStrings.PARENT);
        this.extensionKind = JsonNodeHelper.getTextValue(supplementalTypeObject, DtdlStrings.EXTENSION_KIND);
        this.extensionContext = JsonNodeHelper.getTextValue(supplementalTypeObject, DtdlStrings.EXTENSION_CONTEXT);
        this.coTypes = JsonNodeHelper.getArrayValues(supplementalTypeObject, DtdlStrings.CO_TYPES, String.class);
        this.coTypeVersions = JsonNodeHelper.getArrayValues(supplementalTypeObject, DtdlStrings.CO_TYPE_VERSIONS, Integer.class);
        this.properties = findAndCreateProperties(supplementalTypeObject);
        this.constraints = findAndCreateConstraints(supplementalTypeObject);
    }

    /**
     *
     * @return  Gets a value indicating whether the supplemental type is abstract.
     */
    public boolean isAbstract() {
        return isAbstract;
    }

    /**
     *
     * @return Gets the URI of the parent type of the supplemental type.
     */
    public String getParent() {
        return parent;
    }

    /**
     *
     * @return Gets th extension kind of the supplemental type.
     */
    public String getExtensionKind() {
        return extensionKind;
    }

    /**
     *
     * @return Gets the context specifier that refers to the extension in which the supplemental type is defined.
     */
    public String getExtensionContext() {
        return extensionContext;
    }

    /**
     *
     * @return Gets a list of names of material classes which may be co-typed with the supplemental type.
     */
    public List<String> getCoTypes() {
        return coTypes;
    }

    /**
     *
     * @return Gets a list of DTDL versions of material classes which may be co-typed with the supplemental type.
     */
    public List<Integer> getCoTypeVersions() {
        return coTypeVersions;
    }

    /**
     *
     * @return Gets a map that maps from property URI to a {@link SupplementalPropertyDigest} object providing details about the property.
     */
    public Map<String, SupplementalPropertyDigest> getProperties() {
        return properties;
    }

    /**
     *
     * @return Gets a list of {@link SupplementalConstraintDigest} objects, each of which provides details about a constraint.
     */
    public List<SupplementalConstraintDigest> getConstraints() {
        return constraints;
    }

    private Map<String, SupplementalPropertyDigest> findAndCreateProperties(JsonNode supplementalTypeObject) {
        JsonNode propertiesNode = supplementalTypeObject.get(DtdlStrings.PROPERTIES);
        if (propertiesNode == null) {
            return null;
        }

        HashMap<String, SupplementalPropertyDigest> result = new HashMap<>();

        for (Iterator<String> it = propertiesNode.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            result.put(fieldName, new SupplementalPropertyDigest(propertiesNode.get(fieldName)));
        }

        return result;
    }

    private List<SupplementalConstraintDigest> findAndCreateConstraints(JsonNode supplementalTypeObject) {
        JsonNode constraintsNode = supplementalTypeObject.get(DtdlStrings.CONSTRAINTS);
        if (constraintsNode == null) {
            return null;
        }

        List<SupplementalConstraintDigest> result = new ArrayList<>();

        for (JsonNode node : constraintsNode) {
            result.add(new SupplementalConstraintDigest(node));
        }

        return result;
    }

}
