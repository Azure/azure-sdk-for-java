// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.Access;
import com.azure.digitaltwins.parser.implementation.codegen.Multiplicity;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.codegen.Mutability;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;
import com.azure.digitaltwins.parser.implementation.parsergen.NameFormatter;

import java.util.List;
import java.util.Map;

/**
 * Represents an identifier property on a class that is materialized in the parser object model.
 */
public abstract class IdentifierProperty extends MaterialProperty {
    private final ClientLogger logger = new ClientLogger(IdentifierProperty.class);
    protected String baseClassName;

    /**
     * Initializes a new instance of the {@link MaterialProperty} class.
     *
     * @param propertyName         The name of the property in DTDL.
     * @param obversePropertyName  The name of the property in the java object model.
     * @param propertyNameUris     A map that maps from DTDL version to the URI of the property name.
     * @param propertyDigest       A {@link MaterialPropertyDigest} object containing material property information extracted from the meta-model.
     * @param propertyRestrictions A list of objects that implement the {@link PropertyRestriction} interface.
     * @param baseClassName The java name of the DTDL base class.
     */
    public IdentifierProperty(
        String propertyName,
        String obversePropertyName,
        Map<Integer, String> propertyNameUris,
        MaterialPropertyDigest propertyDigest,
        Map<Integer, List<PropertyRestriction>> propertyRestrictions,
        String baseClassName) {
        super(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions);
        this.baseClassName = baseClassName;
    }

    /**
     * @return Gets the C# name of the DTDL base class.
     */
    protected String getBaseClassName() {
        return this.baseClassName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyKind getPropertyKind() {
        return PropertyKind.IDENTIFIER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isParsable(int dtdlVersion) {
        return this.getPropertyDigest().getPropertyVersions().get(dtdlVersion).getMinCount() != null
            || this.getPropertyDigest().getPropertyVersions().get(dtdlVersion).getMaxCount() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasCountRestriction(int dtdlVersion) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMembers(List<Integer> dtdlVersions, JavaClass obverseClass, boolean classIsAugmentable) {
        super.addMembers(dtdlVersions, obverseClass, classIsAugmentable);

        if (!this.getPropertyDigest().isInherited()) {
            for (int dtdlVersion : dtdlVersions) {
                if (this.getPropertyDigest().getPropertyVersions().get(dtdlVersion).getPattern() != null) {
                    obverseClass.field(
                        Access.PROTECTED,
                        "Pattern",
                        NameFormatter.camelCaseToUnderScoreUpperCase(this.getObversePropertyName().concat(REGEX_PATTERN_FIELD_SUFFIX).concat(String.valueOf(dtdlVersion))),
                        "Pattern.compile(".concat("\"").concat(this.getPropertyDigest().getPropertyVersions().get(dtdlVersion).getPattern()).concat("\")"),
                        Multiplicity.STATIC,
                        Mutability.FINAL,
                        "Regular expression pattern for values of property '".concat(this.getObversePropertyName()).concat("' for DTDLv".concat(String.valueOf(dtdlVersion)).concat(".")));
                }
            }
        }
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
    public void addCheckForRequiredProperty(int dtdlVersion, JavaScope scope) {
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
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
