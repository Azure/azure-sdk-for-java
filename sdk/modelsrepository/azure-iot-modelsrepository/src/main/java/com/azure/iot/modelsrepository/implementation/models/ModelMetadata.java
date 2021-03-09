package com.azure.iot.modelsrepository.implementation.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * ModelMetadata is designated to store KPIs from model parsing.
 */
public class ModelMetadata {
    private String id;
    private List<String> extend;
    private List<String> componentSchemas;
    private List<String> dependencies;

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
