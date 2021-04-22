// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;

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
    public MaterialProperty create(String propertyName, MaterialPropertyDigest propertyDigest) {
        return null;
        //TODO: implement the rest.
    }
}
