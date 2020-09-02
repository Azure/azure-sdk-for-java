package com.azure.digitaltwins.core.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Creates publicly facing object types
 */
public class ModelConverter {
    public static DigitalTwinModelData convertToDigitalTwinModelData(
        String id,
        Object model,
        OffsetDateTime uploadTime,
        Map<String, String> description,
        Map<String, String> displayName,
        Boolean isDecommissioned)
    {
        String modelStringValue = null;

        if (model!= null){
            try {
                modelStringValue = new ObjectMapper().writeValueAsString(model);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return new DigitalTwinModelData()
            .setId(id)
            .setUploadTime(uploadTime)
            .setDisplayName(displayName)
            .setDescription(description)
            .setDecommissioned(isDecommissioned)
            .setModel(modelStringValue);
    }
}
