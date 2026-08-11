// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

/**
 * Event tracking the evaluation of a feature flag. This class captures information about 
 * a feature flag evaluation, including which user evaluated it, whether it was enabled,
 * which variant was returned (if any), and the reason for the variant assignment.
 */
public class EvaluationEvent {

    /**
     * The feature flag that was evaluated. This contains information such as the
     * feature flag name, its conditions, and other associated metadata.
     */
    private final FeatureDefinition feature;

    /**
     * The identifier for the user who evaluated the feature flag. 
     * This is used for user-targeting scenarios and analytics.
     */
    private String user = "";

    /**
     * Indicates whether the feature flag was determined to be enabled
     * for this particular evaluation.
     */
    private boolean enabled = false;

    /**
     * The variant that was assigned during this feature flag evaluation.
     * This is used for feature flags that support multiple variants beyond
     * simply enabled or disabled.
     */
    private Variant variant;

    /**
     * The reason why a particular variant was assigned during the evaluation.
     * This helps track the decision-making process that led to the variant selection.
     */
    private VariantAssignmentReason reason = VariantAssignmentReason.NONE;

    /**
     * Creates an Evaluation Event for the given feature.
     * This constructor initializes a new evaluation event with the specified feature flag,
     * while setting default values for other properties.
     * 
     * @param feature The feature flag that is being evaluated
     */
    public EvaluationEvent(FeatureDefinition feature) {
        this.feature = feature;
    }

    /**
     * Gets the feature flag that was evaluated.
     * 
     * @return the feature flag associated with this evaluation event
     */
    public FeatureDefinition getFeature() {
        return feature;
    }

    /**
     * Gets the identifier of the user who evaluated the feature flag.
     * This is useful for targeting specific users with features and for analytics tracking.
     * 
     * @return the user identifier associated with this evaluation
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the identifier of the user who evaluated the feature flag.
     * This allows tracking which user accessed a particular feature.
     * 
     * @param user the user identifier to associate with this evaluation
     * @return the updated EvaluationEvent instance for method chaining
     */
    public EvaluationEvent setUser(String user) {
        this.user = user;
        return this;
    }

    /**
     * Determines whether the feature flag was enabled for this evaluation.
     * This indicates the result of the evaluation process for the feature flag.
     * 
     * @return true if the feature flag was enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the feature flag was enabled for this evaluation.
     * This allows recording the result of the feature flag evaluation process.
     * 
     * @param enabled true to mark the feature as enabled, false otherwise
     * @return the updated EvaluationEvent instance for method chaining
     */
    public EvaluationEvent setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Gets the variant that was assigned during this feature flag evaluation.
     * This is relevant for feature flags that support multiple variants
     * rather than just being enabled or disabled.
     * 
     * @return the variant assigned for this evaluation, or null if no variant was assigned
     */
    public Variant getVariant() {
        return variant;
    }

    /**
     * Sets the variant that was assigned during this feature flag evaluation.
     * This allows recording which specific variant of a feature flag was selected
     * when multiple variants are supported.
     * 
     * @param variant the variant to associate with this evaluation
     * @return the updated EvaluationEvent instance for method chaining
     */
    public EvaluationEvent setVariant(Variant variant) {
        this.variant = variant;
        return this;
    }

    /**
     * Gets the reason why a particular variant was assigned during this evaluation.
     * This helps track the decision-making process behind the variant selection
     * and can be useful for debugging and analytics.
     * 
     * @return the reason for the variant assignment
     */
    public VariantAssignmentReason getReason() {
        return reason;
    }

    /**
     * Sets the reason why a particular variant was assigned during this evaluation.
     * This documents the logic behind why a specific variant was chosen, which
     * can be useful for troubleshooting and analytics purposes.
     * 
     * @param reason the reason for the variant assignment
     * @return the updated EvaluationEvent instance for method chaining
     */
    public EvaluationEvent setReason(VariantAssignmentReason reason) {
        this.reason = reason;
        return this;
    }
}
