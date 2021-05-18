// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSorted;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSwitch;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;

import java.util.List;
import java.util.Map;

/**
 * Represents a singular object property on a class that is materialized in the parser object model.
 */
public class SingularObjectProperty extends ObjectProperty {
    private final ClientLogger logger = new ClientLogger(SingularObjectProperty.class);

    private static final String SEQUENCER_FIELD_SUFFIX = "Sequencer";
    private static final String REQUIRED_VALUES_FIELD_SUFFIX = "RequiredValues";

    private final String sequencerField;
    private final String requiredValuesField;

    // private final PropertyRestrictionRequiredValues propertyRestrictionRequiredValues;

    /**
     * Initializes a new instance of the {@link SingularObjectProperty} class.
     *
     * @param propertyName         The name of the property in DTDL.
     * @param obversePropertyName  The name of the property in the java object model.
     * @param propertyNameUris     A map that maps from DTDL version to the URI of the property name.
     * @param propertyDigest       A {@link MaterialPropertyDigest} object containing material property information extracted from the meta-model.
     * @param propertyRestrictions A list of objects that implement the {@link PropertyRestriction} interface.
     */
    public SingularObjectProperty(
        String propertyName,
        String obversePropertyName,
        Map<Integer, String> propertyNameUris,
        MaterialPropertyDigest propertyDigest,
        Map<Integer, List<PropertyRestriction>> propertyRestrictions) {
        super(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions);
        this.sequencerField = this.getPropertyName().concat("Sequencer");
        this.requiredValuesField = this.getObversePropertyName().concat("RequiredValues");
        //TODO: implement PropertyRestrictionReuquiredValues.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyRepresentation getRepresentation() {
        return this.getPropertyDigest().isOptional() ? PropertyRepresentation.NULLABLE_ITEM : PropertyRepresentation.ITEM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPropertyType() {
        return this.getClassName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateConstructorCode(JavaSorted sorted) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEqualsLine(JavaSorted sorted) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHashLine(JavaSorted sorted) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMembers(List<Integer> dtdlVersions, JavaClass obverseClass, boolean classIsAugmentable) {
        super.addMembers(dtdlVersions, obverseClass, classIsAugmentable);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public JavaScope iterate(JavaScope outerScope, String varName) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JavaScope checkPresence(JavaScope outerScope) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addValueToObject(int dtdlVersion, JavaScope scope, String objectVar, String outlineByPartitionVar, String outlineIfIdentifiedVar, String contextIdsVar) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCaseToParseSwitch(int dtdlVersion, JavaSwitch switchOnProperty, boolean classIsAugmentable, boolean classIsPartition, String valueCountVar, String definedInVar) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCaseToTrySetObjectPropertySwitch(JavaSwitch switchOnProperty, String valueVar, String keyVar) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRestrictions(JavaScope checkRestrictionsMethodBody, int dtdlVersion, String typeName, boolean classIsAugmentable) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
