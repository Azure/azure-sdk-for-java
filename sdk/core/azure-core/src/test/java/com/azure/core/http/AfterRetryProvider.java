// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.AfterRetryPolicyProvider;
import com.azure.core.http.policy.HttpPipelinePolicy;

public class AfterRetryProvider implements AfterRetryPolicyProvider {
    @Override
    public HttpPipelinePolicy create() {
        return new AfterRetryPolicy();
    }
}
