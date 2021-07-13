// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelinePosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a set of {@link HttpPipelinePolicy HttpPipelinePolicies}.
 *
 * @see HttpPipelinePolicy
 */
public class HttpPipelinePolicies {
    private final List<HttpPipelinePolicy> policies = new ArrayList<>();

    /**
     * Adds a {@link HttpPipelinePolicy policy}.
     *
     * @param policy The policy to add.
     * @return The updated HttpPipelinePolicies object.
     * @throws NullPointerException If {@code policy} is null.
     */
    public HttpPipelinePolicies addPolicy(HttpPipelinePolicy policy) {
        this.policies.add(Objects.requireNonNull(policy, "'policy' cannot be null."));
        return this;
    }

    /**
     * Adds a set of {@link HttpPipelinePolicy policies}.
     *
     * @param policies The policies to add.
     * @return The updated HttpPipelinePolicies object.
     * @throws NullPointerException If {@code policies} is null.
     */
    public HttpPipelinePolicies addPolicies(List<HttpPipelinePolicy> policies) {
        Objects.requireNonNull(policies, "'policies' cannot be null.");

        this.policies.addAll(policies);
        return this;
    }

    /**
     * Adds another {@link HttpPipelinePolicies} into this {@link HttpPipelinePolicies}.
     *
     * @param policies The HttpPipelinePolicies to add.
     * @return The updated HttpPipelinePolicies object.
     * @throws NullPointerException If {@code policies} is null.
     */
    public HttpPipelinePolicies addPolicies(HttpPipelinePolicies policies) {
        Objects.requireNonNull(policies, "'policies' cannot be null.");

        this.policies.addAll(policies.policies);
        return this;
    }

    /**
     * Gets all {@link HttpPipelinePolicy HttpPipelinePolicies} that use the given position.
     *
     * @param pipelinePosition The {@link HttpPipelinePosition} to retrieve policies.
     * @return A list of {@link HttpPipelinePolicy policies} that use the given position.
     * @throws NullPointerException If {@code pipelinePosition} is null.
     */
    public List<HttpPipelinePolicy> getPositionPolicies(HttpPipelinePosition pipelinePosition) {
        Objects.requireNonNull(pipelinePosition, "'pipelinePosition' cannot be null.");

        return policies.stream()
            .filter(policy -> policy.getPipelinePosition() == pipelinePosition)
            .collect(Collectors.toList());
    }
}
