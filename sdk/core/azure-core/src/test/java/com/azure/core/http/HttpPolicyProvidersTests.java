// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class HttpPolicyProvidersTests {
    @Test
    public void addBeforePolicies() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        Assertions.assertTrue(policies.stream().anyMatch(policy -> policy.getClass() == BeforeRetryPolicy.class));
    }

    @Test
    public void addAfterPolicies() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        Assertions.assertTrue(policies.stream().anyMatch(policy -> policy.getClass() == AfterRetryPolicy.class));
    }
}
