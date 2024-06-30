// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.validationstests.models;

public class ValidationJson {
    private String featureFlagName;
    private Object inputs;
    private IsEnabled isEnabled;
    private Variant variant;
    private String description;

    public String getFeatureFlagName() {
        return featureFlagName;
    }

    public void setFeatureFlagName(String featureFlagName) {
        this.featureFlagName = featureFlagName;
    }

    public Object getInputs() {
        return inputs;
    }

    public void setInputs(Object inputs) {
        this.inputs = inputs;
    }

    public IsEnabled getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(IsEnabled isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

