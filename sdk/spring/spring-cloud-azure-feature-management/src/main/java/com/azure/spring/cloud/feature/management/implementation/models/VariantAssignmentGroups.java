// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.models;

import java.util.Map;

public class VariantAssignmentGroups {

    private String variant;

    private Map<String, String> groups;

    /**
     * @return the variant
     */
    public String getVariant() {
        return variant;
    }

    /**
     * @param variant the variant to set
     */
    public void setVariant(String variant) {
        this.variant = variant;
    }

    /**
     * @return the groups
     */
    public Map<String, String> getGroups() {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    public void setGroups(Map<String, String> groups) {
        this.groups = groups;
    }

}
