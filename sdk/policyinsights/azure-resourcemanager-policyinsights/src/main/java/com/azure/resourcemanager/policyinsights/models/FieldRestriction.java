// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.policyinsights.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** The restrictions on a field imposed by a specific policy. */
@Immutable
public final class FieldRestriction {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(FieldRestriction.class);

    /*
     * The type of restriction that is imposed on the field.
     */
    @JsonProperty(value = "result", access = JsonProperty.Access.WRITE_ONLY)
    private FieldRestrictionResult result;

    /*
     * The value that policy will set for the field if the user does not
     * provide a value.
     */
    @JsonProperty(value = "defaultValue", access = JsonProperty.Access.WRITE_ONLY)
    private String defaultValue;

    /*
     * The values that policy either requires or denies for the field.
     */
    @JsonProperty(value = "values", access = JsonProperty.Access.WRITE_ONLY)
    private List<String> values;

    /*
     * The details of the policy that is causing the field restriction.
     */
    @JsonProperty(value = "policy", access = JsonProperty.Access.WRITE_ONLY)
    private PolicyReference policy;

    /**
     * Get the result property: The type of restriction that is imposed on the field.
     *
     * @return the result value.
     */
    public FieldRestrictionResult result() {
        return this.result;
    }

    /**
     * Get the defaultValue property: The value that policy will set for the field if the user does not provide a value.
     *
     * @return the defaultValue value.
     */
    public String defaultValue() {
        return this.defaultValue;
    }

    /**
     * Get the values property: The values that policy either requires or denies for the field.
     *
     * @return the values value.
     */
    public List<String> values() {
        return this.values;
    }

    /**
     * Get the policy property: The details of the policy that is causing the field restriction.
     *
     * @return the policy value.
     */
    public PolicyReference policy() {
        return this.policy;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (policy() != null) {
            policy().validate();
        }
    }
}
