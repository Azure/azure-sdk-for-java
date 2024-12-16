package com.azure.spring.cloud.feature.management.models;

public class EvaluationEvent {

    private final Feature feature;

    private String user = "";

    private boolean enabled = false;

    private Variant variant;

    private VariantAssignmentReason reason = VariantAssignmentReason.NONE;

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
     * @param mono the enabled to set
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
     */
    public EvaluationEvent setReason(VariantAssignmentReason reason) {
        this.reason = reason;
        return this;
    }

}
