// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
     * The model definition that conforms to Digital Twins Definition Language (DTDL) v2.
     */
    @JsonProperty(value = "model")
    private String dtdlModel;

    /**
     * Construct a new DigitalTwinsModelData instance. This class should only be constructed internally since the
     * service never takes this as an input.
     *
     * @param modelId The Id of the model.
     * @param dtdlModel The contents of the model.
     * @param displayName The language map of the localized display names.
     * @param description The language map of the localized descriptions.
     * @param uploadedOn The time when this model was uploaded.
     * @param decommissioned If this model has been decommissioned.
     */
    public DigitalTwinsModelData(String modelId,
                                 String dtdlModel,
                                 Map<String, String> displayName,
                                 Map<String, String> description,
                                 OffsetDateTime uploadedOn,
                                 boolean decommissioned) {
        this.displayName = displayName;
        this.description = description;
        this.id = modelId;
        this.uploadTime = uploadedOn;
        this.decommissioned = decommissioned;
        this.dtdlModel = dtdlModel;
    }

    /**
     * Get the displayName property: A language map that contains the localized display names as specified in the model
     * definition.
     *
     * @return the displayName value.
     */
    public Map<String, String> getDisplayNameLanguageMap() {
        return this.displayName;
    }

    /**
     * Get the description property: A language map that contains the localized descriptions as specified in the model
     * definition.
     *
     * @return the description value.
     */
    public Map<String, String> getDescriptionLanguageMap() {
        return this.description;
    }

    /**
     * Get the id property: The id of the model as specified in the model definition.
     *
     * @return the id value.
     */
    public String getModelId() {
        return this.id;
    }

    /**
     * Get the time the model was uploaded to the service.
     *
     * @return the uploadTime value.
     */
    public OffsetDateTime getUploadedOn() {
        return this.uploadTime;
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
     * Get the model property: The model definition.
     *
     * @return the model value.
     */
    public String getDtdlModel() {
        return this.dtdlModel;
    }
}
