// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * ModelMetadata is designated to store KPIs from model parsing.
 */
public class ModelMetadata {
    private final String id;
    private final List<String> extend;
    private final List<String> componentSchemas;
    private final List<String> dependencies;

    public ModelMetadata(String id, List<String> extend, List<String> componentSchemas) {
        this.id = id;
        this.extend = extend;
        this.componentSchemas = componentSchemas;
        HashSet<String> mergedList = new HashSet<>(extend);
        mergedList.addAll(componentSchemas);
        this.dependencies = new ArrayList<>(mergedList);
    }

    public String getId() {
        return id;
    }

    public List<String> getExtend() {
        return this.extend;
    }

    public List<String> getComponentSchemas() {
        return this.componentSchemas;
    }

    public List<String> getDependencies() {
        return this.dependencies;
    }
}
