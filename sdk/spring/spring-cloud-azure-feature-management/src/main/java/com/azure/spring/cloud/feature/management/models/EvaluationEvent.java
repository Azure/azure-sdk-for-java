// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

/**
 * Event tracking the evaluation of a feature flag
 */
public class EvaluationEvent {

    private final Feature feature;

    private String user = "";

    private boolean enabled = false;

    private Variant variant;

    private VariantAssignmentReason reason = VariantAssignmentReason.NONE;

    /**
     * Creates an Evaluation Event for the given feature
     * @param feature Feature
     */
    public EvaluationEvent(Feature feature) {
        this.feature = feature;
    }

    /**
     * @return the feature
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     * @return EvaluationEvent
     */
    public EvaluationEvent setUser(String user) {
        this.user = user;
        return this;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     * @return EvaluationEvent
     */
    public EvaluationEvent setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @return the variant
     */
    public Variant getVariant() {
        return variant;
    }

    /**
     * @param variant the variant to set
     * @return EvaluationEvent
     */
    public EvaluationEvent setVariant(Variant variant) {
        this.variant = variant;
        return this;
    }

    /**
     * @return the reason
     */
    public VariantAssignmentReason getReason() {
        return reason;
    }

    /**
     * @param reason the reason to set
     * @return EvaluationEvent
     */
    public EvaluationEvent setReason(VariantAssignmentReason reason) {
        this.reason = reason;
        return this;
    }
}
