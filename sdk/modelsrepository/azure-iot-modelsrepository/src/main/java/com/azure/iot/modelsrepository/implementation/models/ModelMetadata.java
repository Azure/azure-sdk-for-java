package com.azure.iot.modelsrepository.implementation.models;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * ModelMetadata is designated to store KPIs from model parsing.
 */
public class ModelMetadata {
    private String id;
    private ArrayList<String> extend;
    private ArrayList<String> componentSchemas;
    private LinkedHashSet<String> dependencies;

    public ModelMetadata(String id, ArrayList<String> extend, ArrayList<String> componentSchemas) {
        this.id = id;
        this.extend = extend;
        this.componentSchemas = componentSchemas;
        this.dependencies = new LinkedHashSet<>(extend);
        this.dependencies.addAll(componentSchemas);
    }

    public String getId() {
        return id;
    }

    public ArrayList<String> getExtend() {
        return this.extend;
    }

    public ArrayList<String> getComponentSchemas() {
        return this.componentSchemas;
    }

    public ArrayList<String> getDependencies() {
        ArrayList<String> all = new ArrayList<>(extend);
        all.addAll(componentSchemas);
        return all;
    }
}
