// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.Access;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSorted;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSwitch;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;

import java.util.HashMap;
import java.util.List;

/**
 * Represents a singular typed literal property on a class that is materialized in the parser object model.
 */
public class InternalProperty extends MaterialProperty {
    private final ClientLogger logger = new ClientLogger(InternalProperty.class);

    private Access access;
    private String value;
    private String description;
    private boolean isRelevantToIdentity;

    /**
     * Initializes a new instance of the {@link InternalProperty} class.
     * @param propertyType The type of the property.
     * @param obversePropertyName The name of the property in the java object model.
     * @param access The {@link Access} for the property.
     * @param value The value for the property.
     * @param description Text description of property.
     * @param isRelevantToIdentity True if the property factors into equivalence and hash calculation.
     */
    public InternalProperty(String propertyType, String obversePropertyName, Access access, String value, String description, boolean isRelevantToIdentity) {
        super(null, obversePropertyName, new HashMap<>(), new MaterialPropertyDigest(), new HashMap<>());
        this.propertyType = propertyType;
        this.access = access;
        this.value = value;
        this.description = description;
        this.isRelevantToIdentity = isRelevantToIdentity;

        // TODO: remove once class is fully implemented.
        logger.info(String.format("%s", this.access));
        logger.info(String.format("%s", this.description));
        logger.info(String.format("%s", this.isRelevantToIdentity));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyKind getPropertyKind() {
        return PropertyKind.INTERNAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyRepresentation getRepresentation() {
        return PropertyRepresentation.ITEM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPropertyType() {
        return this.propertyType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isParsable(int dtdlVersion) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasCountRestriction(int dtdlVersion) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateConstructorCode(JavaSorted sorted) {
        if (this.value != null) {
            sorted.line("this.".concat(this.getObversePropertyName()).concat(" = ").concat(this.value).concat(";"));
        }
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
        if (!this.getPropertyDigest().isInherited()) {
            obverseClass.property(
                Access.PRIVATE,
                Access.PUBLIC,
                Access.PRIVATE,
                this.getPropertyType(),
                this.getObversePropertyName(),
                this.description);
        }
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
        return outerScope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(int dtdlVersion, JavaScope scope, String infoVar) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initMissingPropertyVariable(int dtdlVersion, JavaScope scope) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCaseToParseSwitch(
        int dtdlVersion,
        JavaSwitch switchOnProperty,
        boolean classIsAugmentable,
        boolean classIsPartition,
        String valueCountVar,
        String definedInVar) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCheckForRequiredProperty(int dtdlVersion, JavaScope scope) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addValueToObject(
        int dtdlVersion,
        JavaScope scope,
        String objectVar,
        String outlineByPartitionVar,
        String outlineIfIdentifiedVar,
        String contextIdsVar) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObjectPropertiesToArray(JavaScope scope, String arrayVariable, String referenceVariable) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLiteralPropertiesToArray(JavaScope scope, String arrayVariable) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCaseToTrySetObjectPropertySwitch(JavaSwitch switchOnProperty, String valueVar, String keyVar) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRestrictions(JavaScope checkRestrictionsMethodBody, int dtdlVersion, String typeName, boolean classIsAugmentable) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));    }
}
