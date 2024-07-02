// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.validationstests.models;

import java.util.LinkedHashMap;

public class ValidationTestCase {
    private String friendlyName;
    private String featureFlagName;
    private LinkedHashMap<String, Object> inputs;
    private IsEnabled isEnabled;
    private Variant variant;
    private String description;

    /**
     * @return friendly name of test case
     * */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * @param friendlyName the friendly name of test case
     * */
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    /**
     * @return the name of feature flag
     * */
    public String getFeatureFlagName() {
        return featureFlagName;
    }

    /**
     * @param featureFlagName the name of feature flag
     * */
    public void setFeatureFlagName(String featureFlagName) {
        this.featureFlagName = featureFlagName;
    }

    /**
     * @return the inputs of feature flag
     * */
    public LinkedHashMap<String, Object> getInputs() {
        return inputs;
    }

    /**
     * @param inputs the inputs of feature flag
     * */
    public void setInputs(LinkedHashMap<String, Object> inputs) {
        this.inputs = inputs;
    }

    /**
     * @return IsEnabled object to represent result of feature flag, enabled or exception
     * */
    public IsEnabled getIsEnabled() {
        return isEnabled;
    }

    /**
     * @param isEnabled the result of feature flag, enabled or exception
     * */
    public void setIsEnabled(IsEnabled isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * @return variant
     * */
    public Variant getVariant() {
        return variant;
    }

    /**
     * @param variant the variant of test case
     * */
    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    /**
     * @return description
     * */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description of test case
     * */
    public void setDescription(String description) {
        this.description = description;
    }
}

