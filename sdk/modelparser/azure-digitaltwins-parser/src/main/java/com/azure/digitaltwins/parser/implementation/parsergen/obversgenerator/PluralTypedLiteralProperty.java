// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSorted;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSwitch;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;

import java.util.List;
import java.util.Map;

/**
 * Represents a propertyDigest.IsPlural object property on a class that is materialized in the parser object model.
 */
public class PluralTypedLiteralProperty extends TypedLiteralProperty {
    private final ClientLogger logger = new ClientLogger(PluralTypedLiteralProperty.class);

    /**
     * Initializes a new instance of the {@link PluralTypedLiteralProperty} class.
     *
     * @param propertyName         The name of the property in DTDL.
     * @param obversePropertyName  The name of the property in the java object model.
     * @param propertyNameUris     A map that maps from DTDL version to the URI of the property name.
     * @param propertyDigest       A {@link MaterialPropertyDigest} object containing material property information extracted from the meta-model.
     * @param propertyRestrictions A list of objects that implement the {@link PropertyRestriction} interface.
     * @param datatype             A string representation of the data-type of the literal property.
     * @param literalType          An object that exports the {@link LiteralType} interface to support declaring and parsing a specific literal type.
     */
    public PluralTypedLiteralProperty(
        String propertyName,
        String obversePropertyName,
        Map<Integer, String> propertyNameUris,
        MaterialPropertyDigest propertyDigest,
        Map<Integer, List<PropertyRestriction>> propertyRestrictions,
        String datatype,
        LiteralType literalType) {
        super(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions, datatype, literalType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyRepresentation getRepresentation() {
        return PropertyRepresentation.LIST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPropertyType() {
        return "List<".concat(this.getLiteralType().getSingularType(false)).concat(">");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateConstructorCode(JavaSorted sorted) {
        if (!this.getPropertyDigest().isInherited()) {
            sorted.line("this.".concat(this.getObversePropertyName()).concat(" = new ").concat(this.getPropertyType()).concat("();"));
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
    public void addCaseToParseSwitch(int dtdlVersion, JavaSwitch switchOnProperty, boolean classIsAugmentable, boolean classIsPartition, String valueCountVar, String definedInVar) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addValueToObject(int dtdlVersion, JavaScope scope, String objectVar, String outlineByPartitionVar, String outlineIfIdentifiedVar, String contextIdsVar) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
