package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The DigitalTwinsModelData representing the model and its corresponding metadata.
 */
@Fluent
public final class DigitalTwinsModelData {

    /*
     * A language map that contains the localized display names as specified in
     * the model definition.
     */
    @JsonProperty(value = "displayName")
    private Map<String, String> displayName;

    /*
     * A language map that contains the localized descriptions as specified in
     * the model definition.
     */
    @JsonProperty(value = "description")
    private Map<String, String> description;

    /*
     * The id of the model as specified in the model definition.
     */
    @JsonProperty(value = "id", required = true)
    private String id;

    /*
     * The time the model was uploaded to the service.
     */
    @JsonProperty(value = "uploadTime")
    private OffsetDateTime uploadTime;

    /*
     * Indicates if the model is decommissioned. Decommissioned models cannot
     * be referenced by newly created digital twins.
     */
    @JsonProperty(value = "decommissioned")
    private boolean decommissioned;

    /*
     * The model definition.
     */
    @JsonProperty(value = "model")
    private String model;

    /**
     * Get the displayName property: A language map that contains the localized display names as specified in the model
     * definition.
     *
     * @return the displayName value.
     */
    public Map<String, String> getDisplayName() {
        return this.displayName;
    }

    /**
     * Set the displayName property: A language map that contains the localized display names as specified in the model
     * definition.
     *
     * @param displayName the displayName value to set.
     * @return the ModelData object itself.
     */
    public DigitalTwinsModelData setDisplayName(Map<String, String> displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the description property: A language map that contains the localized descriptions as specified in the model
     * definition.
     *
     * @return the description value.
     */
    public Map<String, String> getDescription() {
        return this.description;
    }

    /**
     * Set the description property: A language map that contains the localized descriptions as specified in the model
     * definition.
     *
     * @param description the description value to set.
     * @return the DigitalTwinsModelData object itself.
     */
    public DigitalTwinsModelData setDescription(Map<String, String> description) {
        this.description = description;
        return this;
    }

    /**
     * Get the id property: The id of the model as specified in the model definition.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: The id of the model as specified in the model definition.
     *
     * @param id the id value to set.
     * @return the DigitalTwinsModelData object itself.
     */
    public DigitalTwinsModelData setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the uploadTime property: The time the model was uploaded to the service.
     *
     * @return the uploadTime value.
     */
    public OffsetDateTime getUploadTime() {
        return this.uploadTime;
    }

    /**
     * Set the uploadTime property: The time the model was uploaded to the service.
     *
     * @param uploadTime the uploadTime value to set.
     * @return the DigitalTwinsModelData object itself.
     */
    public DigitalTwinsModelData setUploadTime(OffsetDateTime uploadTime) {
        this.uploadTime = uploadTime;
        return this;
    }

    /**
     * Get the decommissioned property: Indicates if the model is decommissioned. Decommissioned models cannot be
     * referenced by newly created digital twins.
     *
     * @return the decommissioned value.
     */
    public boolean isDecommissioned() {
        return this.decommissioned;
    }

    /**
     * Set the decommissioned property: Indicates if the model is decommissioned. Decommissioned models cannot be
     * referenced by newly created digital twins.
     *
     * @param decommissioned the decommissioned value to set.
     * @return the DigitalTwinsModelData object itself.
     */
    public DigitalTwinsModelData setDecommissioned(boolean decommissioned) {
        this.decommissioned = decommissioned;
        return this;
    }

    /**
     * Get the model property: The model definition.
     *
     * @return the model value.
     */
    public String getModel() {
        return this.model;
    }

    /**
     * Set the model property: The model definition.
     *
     * @param model the model value to set.
     * @return the DigitalTwinsModelData object itself.
     */
    public DigitalTwinsModelData setModel(String model) {
        this.model = model;
        return this;
    }
}
