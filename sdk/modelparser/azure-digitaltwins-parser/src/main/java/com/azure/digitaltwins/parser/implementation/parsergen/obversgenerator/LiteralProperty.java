// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.codegen.JavaSwitch;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;
import com.azure.digitaltwins.parser.implementation.parsergen.ParserGeneratorStringValues;
import com.azure.digitaltwins.parser.implementation.parsergen.PropertyVersionDigest;

import java.util.List;
import java.util.Map;

/**
 * Represents a literal property on a class that is materialized in the parser object model.
 */
public abstract class LiteralProperty extends MaterialProperty {

    /**
     * Initializes a new instance of the {@link LiteralProperty} class.
     *
     * @param propertyName         The name of the property in DTDL.
     * @param obversePropertyName  The name of the property in the java object model.
     * @param propertyNameUris     A map that maps from DTDL version to the URI of the property name.
     * @param propertyDigest       A {@link MaterialPropertyDigest} object containing material property information extracted from the meta-model.
     * @param propertyRestrictions A list of objects that implement the {@link PropertyRestriction} interface.
     */
    public LiteralProperty(String propertyName, String obversePropertyName, Map<Integer, String> propertyNameUris, MaterialPropertyDigest propertyDigest, Map<Integer, List<PropertyRestriction>> propertyRestrictions) {
        super(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyKind getPropertyKind() {
        return PropertyKind.LITERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isParsable(int dtdlVersion) {
        PropertyVersionDigest propertyVersionDigest = this.propertyDigest.getPropertyVersions().get(dtdlVersion);
        return (propertyVersionDigest != null) && propertyVersionDigest.isAllowed();
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
    public void initMissingPropertyVariable(int dtdlVersion, JavaScope scope) {
        PropertyVersionDigest propertyVersionDigest = this.propertyDigest.getPropertyVersions().get(dtdlVersion);

        if (!this.propertyDigest.isOptional() && propertyVersionDigest != null && propertyVersionDigest.isAllowed()) {
            scope.line("boolean ".concat(this.missingPropertyVariable).concat(" = true;"));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCheckForRequiredProperty(int dtdlVersion, JavaScope scope) {
        PropertyVersionDigest propertyVersionDigest = this.propertyDigest.getPropertyVersions().get(dtdlVersion);

        if (!this.propertyDigest.isOptional() && propertyVersionDigest != null && propertyVersionDigest.isAllowed()) {
            scope.jIf(this.missingPropertyVariable)
                .multiLine("parsingErrorCollection.add(")
                    .line("new Uri(\"dtmi:dtdl:parsingError:missingRequiredProperty\"),")
                    .line("\"{primaryId:p} property '".concat(this.propertyName).concat("' is required but missing.\","))
                    .line("\"Add a '".concat(this.propertyName).concat("' property to the object.\","))
                    .line("this.".concat(ParserGeneratorStringValues.IDENTIFIER_NAME).concat(","))
                    .line("\"".concat(this.propertyName).concat("\");"));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObjectPropertiesToArray(JavaScope scope, String arrayVariable, String referenceVariable) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCaseToTrySetObjectPropertySwitch(JavaSwitch switchOnProperty, String valueVar, String keyVar) {
    }
}
