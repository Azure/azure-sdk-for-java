package com.azure.iot.modelsrepository.implementation.models;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * ModelMetadata is designated to store KPIs from model parsing.
 */
public class ModelMetadata {
    private String id;
    private List<String> extend;
    private List<String> componentSchemas;
    private LinkedHashSet<String> dependencies;

    public ModelMetadata(String id, List<String> extend, List<String> componentSchemas) {
        this.id = id;
        this.extend = extend;
        this.componentSchemas = componentSchemas;
        this.dependencies = new LinkedHashSet<>(extend);
        this.dependencies.addAll(componentSchemas);
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

    public ArrayList<String> getDependencies() {
        ArrayList<String> all = new ArrayList<>(extend);
        all.addAll(componentSchemas);
        return all;
    }
}
