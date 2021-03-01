package com.azure.identity.implementation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.aad.msal4j.AuthenticationErrorCode;
import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.RequestedClaimAdditionalInfo;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class JsonHelper {
    public static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static <T> T convertJsonToObject(final String json, final Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new MsalClientException(e);
        }
    }

    /**
     * Throws exception if given String does not follow JSON syntax
     */
    static void validateJsonFormat(String jsonString) {
        try {
            mapper.readTree(jsonString);
        } catch (IOException e) {
            throw new MsalClientException(e.getMessage(), AuthenticationErrorCode.INVALID_JSON);
        }
    }



    /**
     * Merges given JSON strings into one Jackson JSONNode object, which is returned as a String
     */
    static String mergeJSONString(String mainJsonString, String addJsonString) {
        JsonNode mainJson;
        JsonNode addJson;

        try {
            mainJson = mapper.readTree(mainJsonString);
            addJson = mapper.readTree(addJsonString);
        } catch (IOException e) {
            throw new MsalClientException(e.getMessage(), AuthenticationErrorCode.INVALID_JSON);
        }

        mergeJSONNode(mainJson, addJson);

        return mainJson.toString();
    }


    /**
     * Merges given Jackson JsonNode object into another JsonNode
     */
    public static void mergeJSONNode(JsonNode mainNode, JsonNode addNode) {
        if (addNode == null) {
            return;
        }

        Iterator<String> fieldNames = addNode.fieldNames();
        while (fieldNames.hasNext()) {

            String fieldName = fieldNames.next();
            JsonNode jsonNode = mainNode.get(fieldName);

            if (jsonNode != null && jsonNode.isObject()) {
                mergeJSONNode(jsonNode, addNode.get(fieldName));
            } else {
                if (mainNode instanceof ObjectNode) {
                    JsonNode value = addNode.get(fieldName);
                    ((ObjectNode) mainNode).put(fieldName, value);
                }
            }
        }
    }
}
