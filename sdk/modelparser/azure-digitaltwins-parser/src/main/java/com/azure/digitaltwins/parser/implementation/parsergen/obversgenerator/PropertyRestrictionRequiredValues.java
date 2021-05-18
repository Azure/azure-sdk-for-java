// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.codegen.JavaScope;
import com.azure.digitaltwins.parser.implementation.parsergen.NameFormatter;
import com.azure.digitaltwins.parser.implementation.parsergen.ParserGeneratorStringValues;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a restriction on the set of values a property is allowed to have.
 */
public class PropertyRestrictionRequiredValues implements PropertyRestriction {
    private final ClientLogger logger = new ClientLogger(PropertyRestrictionRequiredValues.class);

    private String propertyName;
    private String requiredValues;
    private String conditionString;

    private List<String> requiredValueUris;

    /**
     * Initializes a new instance of the {@link PropertyRestrictionRequiredValues} class.
     * @param propertyName The name of the property.
     * @param values A list of strings that collectively itemize the allowed values.
     * @param context A map of term definitions.
     */
    public PropertyRestrictionRequiredValues(String propertyName, List<String> values, Map<String, String> context) {
        this.propertyName = propertyName;
        this.requiredValues = String.join(" or ", values.stream().map(s -> "'".concat(s).concat("'")).collect(Collectors.toList()));
        this.conditionString = String.join(" && ", values.stream().map(s ->
            "this."
                .concat(NameFormatter.formatNameAsProperty(this.propertyName))
                .concat(".")
                .concat(ParserGeneratorStringValues.IDENTIFIER_NAME)
                .concat(".getAbsoluteUri != \"")
                .concat(context.get(s))
                .concat("\""))
            .collect(Collectors.toList()));
        this.requiredValueUris = values.stream().map(v -> context.get(v)).collect(Collectors.toList());
    }

    /**
     * @return Gets a list of required value URI strings, used by the exemplifier, not by the parser.
     */
    List<String> getRequiredValueUris() {
        return this.requiredValueUris;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRestriction(JavaScope checkRestrictionMethodBody, String typeName, MaterialProperty materialProperty) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Not yet implemented."));
    }
}
