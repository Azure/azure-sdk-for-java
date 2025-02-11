// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.iot.modelsrepository.implementation.models.ModelMetadata;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.models.JsonArray;
import com.azure.json.models.JsonElement;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The {@link ModelsQuery} class is responsible for parsing DTDL v2 models to produce key metadata.
 * In the current form this class is focused on determining model dependencies recursively
 * via extends and component schemas.
 */
public class ModelsQuery {
    private static final String UTF8_BOM = "\uFEFF";
    private final String content;

    public ModelsQuery(String content) {
        if (content.startsWith(UTF8_BOM)) {
            this.content = content.substring(1);
        } else {
            this.content = content;
        }
    }

    public ModelMetadata parseModel() throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(this.content)) {
            JsonToken token = jsonReader.nextToken();
            if (token != JsonToken.START_OBJECT) {
                return new ModelMetadata("", new ArrayList<>(), new ArrayList<>());
            }

            return parseInterface(JsonObject.fromJson(jsonReader));
        }
    }

    public Map<String, String> listToMap() throws IOException {
        Map<String, String> result = new HashMap<>();

        try (JsonReader jsonReader = JsonProviders.createReader(this.content)) {
            JsonToken token = jsonReader.nextToken(); // Initialize reading.
            if (token == JsonToken.START_ARRAY) {
                while ((token = jsonReader.nextToken()) != JsonToken.END_ARRAY) {
                    if (token == JsonToken.START_OBJECT) {
                        String nodeString = jsonReader.readChildren();
                        String id = new ModelsQuery(nodeString).parseModel().getId();
                        result.put(id, nodeString);
                    } else {
                        jsonReader.skipChildren();
                    }
                }
            }
        }

        return result;
    }

    private static ModelMetadata parseInterface(JsonObject root) {
        String rootDtmi = parseRootDtmi(root);
        List<String> extend = parseExtends(root);
        List<String> contents = parseContents(root);

        return new ModelMetadata(rootDtmi, extend, contents);
    }

    private static String parseRootDtmi(JsonObject root) {
        JsonElement id = root.getProperty(ModelsRepositoryConstants.DTDL_ID);
        if (id != null && id.isString()) {
            return ((JsonString) id).getValue();
        }

        return "";
    }

    private static List<String> parseExtends(JsonObject root) {
        List<String> dependencies = new ArrayList<>();

        JsonElement extend = root.getProperty(ModelsRepositoryConstants.DTDL_EXTENDS);
        if (extend == null) {
            return dependencies;
        }

        if (extend.isString()) {
            dependencies.add(((JsonString) extend).getValue());
        } else if (isOfDtdlType(extend, ModelsRepositoryConstants.DTDL_INTERFACE)) {
            ModelMetadata metadata = parseInterface((JsonObject) extend);
            dependencies.addAll(metadata.getDependencies());
        } else if (extend.isArray()) {
            JsonArray array = (JsonArray) extend;
            for (int i = 0; i < array.size(); i++) {
                JsonElement extendNode = array.getElement(i);
                if (extendNode.isString()) {
                    dependencies.add(((JsonString) extendNode).getValue());
                } else if (isOfDtdlType(extendNode, ModelsRepositoryConstants.DTDL_INTERFACE)) {
                    ModelMetadata metadata = parseInterface((JsonObject) extendNode);
                    dependencies.addAll(metadata.getDependencies());
                }
            }
        }

        return dependencies;
    }

    private static List<String> parseContents(JsonObject root) {
        List<String> dependencies = new ArrayList<>();

        JsonElement contents = root.getProperty(ModelsRepositoryConstants.DTDL_CONTENTS);
        if (contents != null && contents.isArray()) {
            JsonArray array = (JsonArray) contents;
            for (int i = 0; i < array.size(); i++) {
                JsonElement contentNode = array.getElement(i);
                if (isOfDtdlType(contentNode, ModelsRepositoryConstants.DTDL_COMPONENT)) {
                    dependencies.addAll(parseComponent((JsonObject) contentNode));
                }
            }
        }

        return dependencies;
    }

    private static List<String> parseComponent(JsonObject root) {
        List<String> dependencies = new ArrayList<>();

        JsonElement componentSchema = root.getProperty(ModelsRepositoryConstants.DTDL_SCHEMA);
        if (componentSchema != null) {
            if (componentSchema.isString()) {
                dependencies.add(((JsonString) componentSchema).getValue());
            } else if (isOfDtdlType(componentSchema, ModelsRepositoryConstants.DTDL_INTERFACE)) {
                ModelMetadata metadata = parseInterface((JsonObject) componentSchema);
                dependencies.addAll(metadata.getDependencies());
            } else if (componentSchema.isArray()) {
                JsonArray array = (JsonArray) componentSchema;
                for (int i = 0; i < array.size(); i++) {
                    JsonElement componentNode = array.getElement(i);
                    if (componentNode.isString()) {
                        dependencies.add(((JsonString) componentNode).getValue());
                    } else if (isOfDtdlType(componentNode, ModelsRepositoryConstants.DTDL_INTERFACE)) {
                        ModelMetadata metadata = parseInterface((JsonObject) componentNode);
                        dependencies.addAll(metadata.getDependencies());
                    }
                }
            }
        }

        return dependencies;
    }

    private static boolean isOfDtdlType(JsonElement root, String objectTypeString) {
        if (root.isObject()) {
            JsonElement objectType = ((JsonObject) root).getProperty(ModelsRepositoryConstants.DTDL_TYPE);
            if (objectType != null) {
                return objectType.isString() && Objects.equals(((JsonString) objectType).getValue(), objectTypeString);
            }
        }

        return false;
    }
}
