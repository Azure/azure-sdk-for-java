// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.digitaltwins.parser.implementation.codegen.Access;
import com.azure.digitaltwins.parser.implementation.codegen.Multiplicity;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.codegen.Mutability;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;
import com.azure.digitaltwins.parser.implementation.parsergen.NameFormatter;
import com.azure.digitaltwins.parser.implementation.parsergen.PropertyVersionDigest;

import java.util.List;
import java.util.Map;

/**
 * Represents a typed literal property on a class that is materialized in the parser object model.
 */
public abstract class TypedLiteralProperty extends LiteralProperty {

    protected String dataType;
    protected LiteralType literalType;

    /**
     * Initializes a new instance of the {@link TypedLiteralProperty} class.
     *
     * @param propertyName         The name of the property in DTDL.
     * @param obversePropertyName  The name of the property in the java object model.
     * @param propertyNameUris     A map that maps from DTDL version to the URI of the property name.
     * @param propertyDigest       A {@link MaterialPropertyDigest} object containing material property information extracted from the meta-model.
     * @param propertyRestrictions A list of objects that implement the {@link PropertyRestriction} interface.
     * @param datatype A string representation of the data-type of the literal property.
     * @param literalType An object that exports the {@link LiteralType} interface to support declaring and parsing a specific literal type.
     */
    public TypedLiteralProperty(
        String propertyName,
        String obversePropertyName,
        Map<Integer, String> propertyNameUris,
        MaterialPropertyDigest propertyDigest,
        Map<Integer, List<PropertyRestriction>> propertyRestrictions,
        String datatype,
        LiteralType literalType) {
        super(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions);
        this.dataType = datatype;
        this.literalType = literalType;
    }

    /**
     * @return Gets a string representation of the datatype of the literal property.
     */
    public String getDataType() {
        return this.dataType;
    }

    /**
     * @return Gets an object that exports the {@link LiteralType} interface to support declaring and parsing a specific literal type.
     */
    public LiteralType getLiteralType() {
        return this.literalType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMembers(List<Integer> dtdlVersions, JavaClass obverseClass, boolean classIsAugmentable) {
        super.addMembers(dtdlVersions, obverseClass, classIsAugmentable);

        if (!this.getPropertyDigest().isInherited()) {
            for (int dtdlVersion : dtdlVersions) {
                PropertyVersionDigest propertyVersionDigest = this.getPropertyDigest().getPropertyVersions().get(dtdlVersion);

                if (propertyVersionDigest != null && propertyVersionDigest.getPattern() != null) {
                    obverseClass.field(
                        Access.PROTECTED,
                        "Pattern",
                        NameFormatter.camelCaseToUnderScoreUpperCase(this.getObversePropertyName().concat(REGEX_PATTERN_FIELD_SUFFIX).concat(String.valueOf(dtdlVersion))),
                        "Pattern.compile(".concat("\"").concat(propertyVersionDigest.getPattern()).concat("\")"),
                        Multiplicity.STATIC,
                        Mutability.FINAL,
                        null);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLiteralPropertiesToArray(JavaScope scope, String arrayVariable) {
        if (!this.getPropertyDigest().isInherited()) {
            String varName = "item";
            JavaScope iterationScope = this.iterate(scope, varName);
            iterationScope.line(arrayVariable
                .concat(".add(")
                .concat("new ObjectMapper().createArrayNode().add(\"")
                .concat(this.propertyName)
                .concat("\").add(")
                .concat(varName)
                .concat("toString()).add(\"\").add(\"#")
                .concat(this.getDataType())
                .concat("\"));"));
        }
    }

}
