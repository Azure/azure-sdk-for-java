// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.customerinsights.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.azure.core.management.ProxyResource;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.customerinsights.models.PredictionGradesItem;
import com.azure.resourcemanager.customerinsights.models.PredictionMappings;
import com.azure.resourcemanager.customerinsights.models.PredictionSystemGeneratedEntities;
import com.azure.resourcemanager.customerinsights.models.ProvisioningStates;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** The prediction resource format. */
@JsonFlatten
@Fluent
public class PredictionResourceFormatInner extends ProxyResource {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(PredictionResourceFormatInner.class);

    /*
     * Description of the prediction.
     */
    @JsonProperty(value = "properties.description")
    private Map<String, String> description;

    /*
     * Display name of the prediction.
     */
    @JsonProperty(value = "properties.displayName")
    private Map<String, String> displayName;

    /*
     * Interaction types involved in the prediction.
     */
    @JsonProperty(value = "properties.involvedInteractionTypes")
    private List<String> involvedInteractionTypes;

    /*
     * KPI types involved in the prediction.
     */
    @JsonProperty(value = "properties.involvedKpiTypes")
    private List<String> involvedKpiTypes;

    /*
     * Relationships involved in the prediction.
     */
    @JsonProperty(value = "properties.involvedRelationships")
    private List<String> involvedRelationships;

    /*
     * Negative outcome expression.
     */
    @JsonProperty(value = "properties.negativeOutcomeExpression")
    private String negativeOutcomeExpression;

    /*
     * Positive outcome expression.
     */
    @JsonProperty(value = "properties.positiveOutcomeExpression")
    private String positiveOutcomeExpression;

    /*
     * Primary profile type.
     */
    @JsonProperty(value = "properties.primaryProfileType")
    private String primaryProfileType;

    /*
     * Provisioning state.
     */
    @JsonProperty(value = "properties.provisioningState", access = JsonProperty.Access.WRITE_ONLY)
    private ProvisioningStates provisioningState;

    /*
     * Name of the prediction.
     */
    @JsonProperty(value = "properties.predictionName")
    private String predictionName;

    /*
     * Scope expression.
     */
    @JsonProperty(value = "properties.scopeExpression")
    private String scopeExpression;

    /*
     * The hub name.
     */
    @JsonProperty(value = "properties.tenantId", access = JsonProperty.Access.WRITE_ONLY)
    private String tenantId;

    /*
     * Whether do auto analyze.
     */
    @JsonProperty(value = "properties.autoAnalyze")
    private Boolean autoAnalyze;

    /*
     * Definition of the link mapping of prediction.
     */
    @JsonProperty(value = "properties.mappings")
    private PredictionMappings mappings;

    /*
     * Score label.
     */
    @JsonProperty(value = "properties.scoreLabel")
    private String scoreLabel;

    /*
     * The prediction grades.
     */
    @JsonProperty(value = "properties.grades")
    private List<PredictionGradesItem> grades;

    /*
     * System generated entities.
     */
    @JsonProperty(value = "properties.systemGeneratedEntities", access = JsonProperty.Access.WRITE_ONLY)
    private PredictionSystemGeneratedEntities systemGeneratedEntities;

    /**
     * Get the description property: Description of the prediction.
     *
     * @return the description value.
     */
    public Map<String, String> description() {
        return this.description;
    }

    /**
     * Set the description property: Description of the prediction.
     *
     * @param description the description value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withDescription(Map<String, String> description) {
        this.description = description;
        return this;
    }

    /**
     * Get the displayName property: Display name of the prediction.
     *
     * @return the displayName value.
     */
    public Map<String, String> displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName property: Display name of the prediction.
     *
     * @param displayName the displayName value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withDisplayName(Map<String, String> displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the involvedInteractionTypes property: Interaction types involved in the prediction.
     *
     * @return the involvedInteractionTypes value.
     */
    public List<String> involvedInteractionTypes() {
        return this.involvedInteractionTypes;
    }

    /**
     * Set the involvedInteractionTypes property: Interaction types involved in the prediction.
     *
     * @param involvedInteractionTypes the involvedInteractionTypes value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withInvolvedInteractionTypes(List<String> involvedInteractionTypes) {
        this.involvedInteractionTypes = involvedInteractionTypes;
        return this;
    }

    /**
     * Get the involvedKpiTypes property: KPI types involved in the prediction.
     *
     * @return the involvedKpiTypes value.
     */
    public List<String> involvedKpiTypes() {
        return this.involvedKpiTypes;
    }

    /**
     * Set the involvedKpiTypes property: KPI types involved in the prediction.
     *
     * @param involvedKpiTypes the involvedKpiTypes value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withInvolvedKpiTypes(List<String> involvedKpiTypes) {
        this.involvedKpiTypes = involvedKpiTypes;
        return this;
    }

    /**
     * Get the involvedRelationships property: Relationships involved in the prediction.
     *
     * @return the involvedRelationships value.
     */
    public List<String> involvedRelationships() {
        return this.involvedRelationships;
    }

    /**
     * Set the involvedRelationships property: Relationships involved in the prediction.
     *
     * @param involvedRelationships the involvedRelationships value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withInvolvedRelationships(List<String> involvedRelationships) {
        this.involvedRelationships = involvedRelationships;
        return this;
    }

    /**
     * Get the negativeOutcomeExpression property: Negative outcome expression.
     *
     * @return the negativeOutcomeExpression value.
     */
    public String negativeOutcomeExpression() {
        return this.negativeOutcomeExpression;
    }

    /**
     * Set the negativeOutcomeExpression property: Negative outcome expression.
     *
     * @param negativeOutcomeExpression the negativeOutcomeExpression value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withNegativeOutcomeExpression(String negativeOutcomeExpression) {
        this.negativeOutcomeExpression = negativeOutcomeExpression;
        return this;
    }

    /**
     * Get the positiveOutcomeExpression property: Positive outcome expression.
     *
     * @return the positiveOutcomeExpression value.
     */
    public String positiveOutcomeExpression() {
        return this.positiveOutcomeExpression;
    }

    /**
     * Set the positiveOutcomeExpression property: Positive outcome expression.
     *
     * @param positiveOutcomeExpression the positiveOutcomeExpression value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withPositiveOutcomeExpression(String positiveOutcomeExpression) {
        this.positiveOutcomeExpression = positiveOutcomeExpression;
        return this;
    }

    /**
     * Get the primaryProfileType property: Primary profile type.
     *
     * @return the primaryProfileType value.
     */
    public String primaryProfileType() {
        return this.primaryProfileType;
    }

    /**
     * Set the primaryProfileType property: Primary profile type.
     *
     * @param primaryProfileType the primaryProfileType value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withPrimaryProfileType(String primaryProfileType) {
        this.primaryProfileType = primaryProfileType;
        return this;
    }

    /**
     * Get the provisioningState property: Provisioning state.
     *
     * @return the provisioningState value.
     */
    public ProvisioningStates provisioningState() {
        return this.provisioningState;
    }

    /**
     * Get the predictionName property: Name of the prediction.
     *
     * @return the predictionName value.
     */
    public String predictionName() {
        return this.predictionName;
    }

    /**
     * Set the predictionName property: Name of the prediction.
     *
     * @param predictionName the predictionName value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withPredictionName(String predictionName) {
        this.predictionName = predictionName;
        return this;
    }

    /**
     * Get the scopeExpression property: Scope expression.
     *
     * @return the scopeExpression value.
     */
    public String scopeExpression() {
        return this.scopeExpression;
    }

    /**
     * Set the scopeExpression property: Scope expression.
     *
     * @param scopeExpression the scopeExpression value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withScopeExpression(String scopeExpression) {
        this.scopeExpression = scopeExpression;
        return this;
    }

    /**
     * Get the tenantId property: The hub name.
     *
     * @return the tenantId value.
     */
    public String tenantId() {
        return this.tenantId;
    }

    /**
     * Get the autoAnalyze property: Whether do auto analyze.
     *
     * @return the autoAnalyze value.
     */
    public Boolean autoAnalyze() {
        return this.autoAnalyze;
    }

    /**
     * Set the autoAnalyze property: Whether do auto analyze.
     *
     * @param autoAnalyze the autoAnalyze value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withAutoAnalyze(Boolean autoAnalyze) {
        this.autoAnalyze = autoAnalyze;
        return this;
    }

    /**
     * Get the mappings property: Definition of the link mapping of prediction.
     *
     * @return the mappings value.
     */
    public PredictionMappings mappings() {
        return this.mappings;
    }

    /**
     * Set the mappings property: Definition of the link mapping of prediction.
     *
     * @param mappings the mappings value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withMappings(PredictionMappings mappings) {
        this.mappings = mappings;
        return this;
    }

    /**
     * Get the scoreLabel property: Score label.
     *
     * @return the scoreLabel value.
     */
    public String scoreLabel() {
        return this.scoreLabel;
    }

    /**
     * Set the scoreLabel property: Score label.
     *
     * @param scoreLabel the scoreLabel value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withScoreLabel(String scoreLabel) {
        this.scoreLabel = scoreLabel;
        return this;
    }

    /**
     * Get the grades property: The prediction grades.
     *
     * @return the grades value.
     */
    public List<PredictionGradesItem> grades() {
        return this.grades;
    }

    /**
     * Set the grades property: The prediction grades.
     *
     * @param grades the grades value to set.
     * @return the PredictionResourceFormatInner object itself.
     */
    public PredictionResourceFormatInner withGrades(List<PredictionGradesItem> grades) {
        this.grades = grades;
        return this;
    }

    /**
     * Get the systemGeneratedEntities property: System generated entities.
     *
     * @return the systemGeneratedEntities value.
     */
    public PredictionSystemGeneratedEntities systemGeneratedEntities() {
        return this.systemGeneratedEntities;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (mappings() != null) {
            mappings().validate();
        }
        if (grades() != null) {
            grades().forEach(e -> e.validate());
        }
        if (systemGeneratedEntities() != null) {
            systemGeneratedEntities().validate();
        }
    }
}
