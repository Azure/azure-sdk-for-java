// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.codegen.JavaClass;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;

import java.util.List;
import java.util.Map;

/**
 * Represents a untyped literal property on a class that is materialized in the parser object model.
 */
public abstract class UntypedLiteralProperty extends LiteralProperty {
    private final ClientLogger logger = new ClientLogger(UntypedLiteralProperty.class);
    private static final String DATA_TYPE_FIELD_SUFFIX = "DataType";
    protected String dataTypeField;

    /**
     * Initializes a new instance of the {@link LiteralProperty} class.
     *
     * @param propertyName         The name of the property in DTDL.
     * @param obversePropertyName  The name of the property in the java object model.
     * @param propertyNameUris     A map that maps from DTDL version to the URI of the property name.
     * @param propertyDigest       A {@link MaterialPropertyDigest} object containing material property information extracted from the meta-model.
     * @param propertyRestrictions A list of objects that implement the {@link PropertyRestriction} interface.
     */
    public UntypedLiteralProperty(
        String propertyName,
        String obversePropertyName,
        Map<Integer, String> propertyNameUris,
        MaterialPropertyDigest propertyDigest,
        Map<Integer, List<PropertyRestriction>> propertyRestrictions) {
        super(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions);
        this.dataTypeField = this.getPropertyName().concat(DATA_TYPE_FIELD_SUFFIX);
    }

    /**
     * @return Gets the name of a field that stores the datatype for this property.
     */
    protected String getDataTypeField() {
        return this.dataTypeField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMembers(List<Integer> dtdlVersions, JavaClass obverseClass, boolean classIsAugmentable) {
        super.addMembers(dtdlVersions, obverseClass, classIsAugmentable);
        // TODO: azabbasi: addMembers
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLiteralPropertiesToArray(JavaScope scope, String arrayVariable) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
