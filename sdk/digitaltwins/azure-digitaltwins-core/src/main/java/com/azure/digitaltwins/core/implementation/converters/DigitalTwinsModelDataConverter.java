package com.azure.digitaltwins.core.implementation.converters;

import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A converter between {@link com.azure.digitaltwins.core.implementation.models.DigitalTwinsModelData} and
 * {@link DigitalTwinsModelData}.
 */
public final class DigitalTwinsModelDataConverter {

    /**
     * Maps from {@link com.azure.digitaltwins.core.implementation.models.DigitalTwinsModelData} to
     * {@link DigitalTwinsModelData}. If the input is null, then the output will be null as well.
     */
    public static DigitalTwinsModelData map(com.azure.digitaltwins.core.implementation.models.DigitalTwinsModelData input) {
        if (input == null) {
            return null;
        }

        String modelStringValue = null;

        if (input.getModel() != null){
            try {
                modelStringValue = new ObjectMapper().writeValueAsString(input.getModel());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("ModelData does not have a valid model definition.", e);
            }
        }

        return new DigitalTwinsModelData()
            .setId(input.getId())
            .setUploadTime(input.getUploadTime())
            .setDisplayName(input.getDisplayName())
            .setDescription(input.getDescription())
            .setDecommissioned(input.isDecommissioned())
            .setModel(modelStringValue);
    }

    private DigitalTwinsModelDataConverter() {}
}
