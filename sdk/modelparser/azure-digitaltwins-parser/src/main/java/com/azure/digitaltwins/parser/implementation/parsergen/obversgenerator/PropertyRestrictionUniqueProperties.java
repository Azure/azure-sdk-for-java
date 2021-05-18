// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.parsergen.NameFormatter;

/**
 * Represents a restriction on the set of values a property is allowed to have.
 */
public class PropertyRestrictionUniqueProperties implements PropertyRestriction {
    private final ClientLogger logger = new ClientLogger(PropertyRestrictionUniqueProperties.class);

    private String propertyName;
    private String uniquePropertyName;
    private String uniquePropertyObverseName;
    private String hashSetName;

    /**
     * Initializes a new instance of the {@link PropertyRestrictionUniqueProperties} class.
     *
     * @param propertyName The name of the property.
     * @param uniquePropertyName The name of the property that must be unique.
     */
    public PropertyRestrictionUniqueProperties(String propertyName, String uniquePropertyName) {
        this.propertyName = propertyName;
        this.uniquePropertyName = uniquePropertyName;
        this.uniquePropertyObverseName = NameFormatter.formatNameAsProperty(uniquePropertyName);
        this.hashSetName = String.format("%s%sSet", this.propertyName, this.uniquePropertyObverseName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRestriction(JavaScope checkRestrictionMethodBody, String typeName, MaterialProperty materialProperty) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
