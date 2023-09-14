// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.models;

import java.util.Map;

public class VariantAssignmentUsers {

    private String variant;

    private Map<String, String> users;

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
     * @return the users
     */
    public Map<String, String> getUsers() {
        return users;
    }

    /**
     * @param users the users to set
     */
    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

}
