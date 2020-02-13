// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.BeforeRetryPolicyProvider;
import com.azure.core.http.policy.HttpPipelinePolicy;

public class BeforeRetryProvider implements BeforeRetryPolicyProvider {
    @Override
    public HttpPipelinePolicy create() {
        return new BeforeRetryPolicy();
    }
}
