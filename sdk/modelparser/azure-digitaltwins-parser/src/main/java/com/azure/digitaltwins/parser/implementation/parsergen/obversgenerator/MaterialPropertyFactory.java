// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;
import com.azure.digitaltwins.parser.implementation.parsergen.NameFormatter;
import com.azure.digitaltwins.parser.implementation.parsergen.ParserGeneratorStringValues;
import com.azure.digitaltwins.parser.implementation.parsergen.PropertyVersionDigest;
import com.azure.digitaltwins.parser.implementation.parsergen.DtdlStrings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A factory for generating {@link MaterialProperty} objects.
 */
public class MaterialPropertyFactory {
    private final ClientLogger logger = new ClientLogger(MaterialPropertyFactory.class);

    private List<Integer> dtdlVersions;
    private Map<String, Map<String, String>> contexts;
    private String baseClassName;

    /**
     * Initializes a new instance of the {@link MaterialPropertyFactory} class.
     *
     * @param dtdlVersions A list of DTDL major version numbers to create properties for.
     * @param contexts A map that maps from a context Id to a map of term definitions.
     * @param baseClassName The name of the C# base class of all DTDL entities.
     */
    public MaterialPropertyFactory(List<Integer> dtdlVersions, Map<String, Map<String, String>> contexts, String baseClassName) {
        this.dtdlVersions = dtdlVersions;
        this.contexts = contexts;
        this.baseClassName = baseClassName;

        // TODO: remove once class is fully implemented.
        logger.info(String.format("%s", this.dtdlVersions));
        logger.info(String.format("%s", this.contexts));
        logger.info(String.format("%s", this.baseClassName));
    }

    /**
     * Create an object that is a subclass of {@link MaterialProperty}.
     * @param propertyName The name of the property.
     * @param propertyDigest A {@link MaterialPropertyDigest} object containing material property information extracted from the meta-model.
     * @return The object created.
     */
    public MaterialProperty create(String propertyName, MaterialPropertyDigest propertyDigest) throws Exception {
        String obversePropertyName = NameFormatter.formatNameAsProperty(propertyName);

        Map<Integer, String>  propertyNameUris = new HashMap<>();

        for (int v : this.dtdlVersions) {
            if (this.contexts.get(ParserGeneratorStringValues.getDtdlContextIdString(v)).containsKey(propertyName)) {
                propertyNameUris.put(v, this.contexts.get(ParserGeneratorStringValues.getDtdlContextIdString(v)).get(propertyName));
            }
        }

        Map<Integer, List<PropertyRestriction>> propertyRestrictions = this.getPropertyRestrictions(propertyName, propertyDigest);

        if (propertyDigest.isLiteral()) {
            if (propertyDigest.getDataType() != null) {
                if (propertyDigest.getDataType().equals(DtdlStrings.DTDL_DATATYPE_LANG_STRING)) {
                    return new LangStringLiteralProperty(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions);
                }

                LiteralType literalType;
                switch (propertyDigest.getDataType()) {
                    case DtdlStrings.DTDL_DATATYPE_BOOLEAN:
                        literalType = new BooleanLiteralType();
                        break;
                    case DtdlStrings.DTDL_DATATYPE_INTEGER:
                        literalType = new IntegerLiteralType();
                        break;
                    case DtdlStrings.DTDL_DATATYPE_STRING:
                        literalType = new StringLiteralType();
                        break;
                    default:
                        throw logger.logExceptionAsError(new RuntimeException("unrecognized type"));
                }

                if (propertyDigest.isPlural()) {
                    return new PluralTypedLiteralProperty(
                        propertyName,
                        obversePropertyName,
                        propertyNameUris,
                        propertyDigest,
                        propertyRestrictions,
                        propertyDigest.getDataType(),
                        literalType);
                } else {
                    return new SingularTypedLiteralProperty(
                        propertyName,
                        obversePropertyName,
                        propertyNameUris,
                        propertyDigest,
                        propertyRestrictions,
                        propertyDigest.getDataType(),
                        literalType);
                }
            } else {
                if (propertyDigest.isPlural()) {
                    return new PluralUntypedLiteralProperty(
                        propertyName,
                        obversePropertyName,
                        propertyNameUris,
                        propertyDigest,
                        propertyRestrictions);
                } else {
                    return new SingularUntypedLiteralProperty(
                        propertyName,
                        obversePropertyName,
                        propertyNameUris,
                        propertyDigest,
                        propertyRestrictions);
                }
            }
        } else if (propertyDigest.getClassType() != null) {
            if (propertyDigest.getDictionaryKey() != null) {
                return new MapProperty(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions);
            } else if (propertyDigest.isPlural()) {
                return new PluralObjectProperty(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions);
            } else {
                return new SingularObjectProperty(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions);
            }
        } else {
            if (propertyDigest.isPlural()) {
                return new PluralIdentifierProperty(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions, this.baseClassName);
            } else {
                return new SingularIdentifierProperty(propertyName, obversePropertyName, propertyNameUris, propertyDigest, propertyRestrictions, this.baseClassName);
            }
        }
    }

    private Map<Integer, List<PropertyRestriction>> getPropertyRestrictions(String propertyName, MaterialPropertyDigest propertyDigest) {
        Map<Integer, List<PropertyRestriction>> propertyRestrictions = new HashMap<>();

        for (Map.Entry<Integer, PropertyVersionDigest> kvp : propertyDigest.getPropertyVersions().entrySet()) {
            List<PropertyRestriction> versionRestrictions = propertyRestrictions.get(kvp.getKey());

            if (versionRestrictions == null) {
                versionRestrictions = new ArrayList<>();
                propertyRestrictions.put(kvp.getKey(), versionRestrictions);
            }

            if (kvp.getValue().getValues() != null) {
                versionRestrictions.add(new PropertyRestrictionRequiredValues(
                    propertyName,
                    kvp.getValue().getValues(),
                    this.contexts.get(ParserGeneratorStringValues.getDtdlContextIdString(kvp.getKey()))));
            }

            if (kvp.getValue().getUniqueProperties() != null) {
                for (String uniquePropertyName : kvp.getValue().getUniqueProperties()) {
                    if (!uniquePropertyName.equals(propertyDigest.getDictionaryKey())) {
                        versionRestrictions.add(new PropertyRestrictionUniqueProperties(propertyName, uniquePropertyName));
                    }
                }
            }
        }

        return propertyRestrictions;
    }
}
