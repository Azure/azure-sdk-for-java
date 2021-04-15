// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import java.util.List;
import java.util.Map;

/**
 * Class that abstracts material class information extracted from the metamodel digest provided by the meta-parser.
 */
public class MaterialClassDigest {

    /**
     * Initializes a new instance of the {@link MaterialClassDigest} class.
     */
    public MaterialClassDigest() {

    }

    private List<Integer> dtdlVersions;
    private boolean isAbstract;
    private boolean isOvert;
    private boolean isPartition;
    private String parentClass;
    private List<String> typeIds;
    private Map<Integer, List<String>> concreteSubclasses;
    private Map<Integer, List<String>> elementalSubclasses;
    private Map<Integer, List<String>> extensibleMaterialSubclasses;
    private Map<Integer, List<String>> standardElementIds;
    private Map<Integer, String> badTypeCauseFormat;
    private Map<Integer, String> badTypeActionFormat;
}
