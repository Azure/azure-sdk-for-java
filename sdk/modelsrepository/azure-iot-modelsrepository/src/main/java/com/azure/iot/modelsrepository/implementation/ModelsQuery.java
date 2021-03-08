package com.azure.iot.modelsrepository.implementation;

import com.azure.iot.modelsrepository.implementation.models.ModelMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link ModelsQuery} class is responsible for parsing DTDL v2 models to produce key metadata.
 * In the current form this class is focused on determining model dependencies recursively
 * via extends and component schemas.
 */
public class ModelsQuery {

    private final String content;
    private final ObjectMapper mapper;

    public ModelsQuery(String content) {
        mapper = new ObjectMapper();
        this.content = content;
    }

    private static ModelMetadata parseInterface(JsonNode root) {
        String rootDtmi = parseRootDtmi(root);
        List<String> extend = parseExtends(root);
        List<String> contents = parseContents(root);

        return new ModelMetadata(rootDtmi, extend, contents);
    }

    private static String parseRootDtmi(JsonNode root) {
        if (root.isObject()) {
            JsonNode id = root.get("@id");

            if (id != null && id.isTextual()) {
                return id.toString();
            }
        }

        return "";
    }

    private static List<String> parseExtends(JsonNode root) {

        List<String> dependencies = new ArrayList<>();

        if (!root.isObject()) {
            return dependencies;
        }

        JsonNode extend = root.get("extends");
        if (extend == null) {
            return dependencies;
        }

        if (extend.isTextual()) {
            dependencies.add(extend.toString());
        } else if (isOfDtdlType(extend, "Interface")) {
            ModelMetadata metadata = parseInterface(extend);
            dependencies.addAll(metadata.getDependencies());
        } else if (extend.isArray()) {
            for (JsonNode extendNode : extend) {
                if (extendNode.isTextual()) {
                    dependencies.add(extendNode.toString());
                } else if (isOfDtdlType(extendNode, "Interface")) {
                    ModelMetadata metadata = parseInterface(extendNode);
                    dependencies.addAll(metadata.getDependencies());
                }
            }
        }

        return dependencies;
    }

    private static List<String> parseContents(JsonNode root) {
        List<String> dependencies = new ArrayList<>();

        if (root.isObject()) {
            JsonNode contents = root.get("contents");
            if (contents != null && contents.isArray()) {
                for (JsonNode contentNode : contents) {
                    if (isOfDtdlType(contentNode, "Component")) {
                        dependencies.addAll(parseComponent(contentNode));
                    }
                }
            }
        }

        return dependencies;
    }


    private static List<String> parseComponent(JsonNode root) {
        List<String> dependencies = new ArrayList<>();

        if (root.isObject()) {
            JsonNode componentSchema = root.get("schema");
            if (componentSchema != null) {
                if (componentSchema.isTextual()) {
                    dependencies.add(componentSchema.toString());
                } else if (isOfDtdlType(componentSchema, "Interface")) {
                    ModelMetadata metadata = parseInterface(componentSchema);
                    dependencies.addAll(metadata.getDependencies());
                } else if (componentSchema.isArray()) {
                    for (JsonNode componentNode : componentSchema) {
                        if (componentNode.isTextual()) {
                            dependencies.add(componentNode.toString());
                        } else if (isOfDtdlType(componentNode, "Interface")) {
                            ModelMetadata metadata = parseInterface(componentNode);
                            dependencies.addAll(metadata.getDependencies());
                        }
                    }
                }
            }
        }

        return dependencies;
    }

    private static boolean isOfDtdlType(JsonNode root, String objectTypeString) {
        if (root.isObject()) {
            JsonNode objectType = root.get("@type");
            if (objectType != null) {
                return objectType.isTextual() && objectType.toString().equals(objectTypeString);
            }
        }

        return false;
    }

    public ModelMetadata parseModel() throws JsonProcessingException {
        JsonNode rootElement = mapper.readValue(this.content, JsonNode.class);
        return parseInterface(rootElement);
    }

    public Map<String, String> ListToMap() throws JsonProcessingException {
        Map<String, String> result = new HashMap<>();

        JsonNode root = mapper.readValue(this.content, JsonNode.class);

        if (root.isArray()) {
            for (JsonNode node : root) {
                if (node.isObject()) {
                    String nodeString = node.toString();
                    String id = new ModelsQuery(nodeString).parseModel().getId();
                    result.put(id, nodeString);
                }
            }
        }

        return result;
    }
}
