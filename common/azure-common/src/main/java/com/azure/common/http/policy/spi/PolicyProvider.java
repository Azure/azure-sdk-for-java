// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.common.http.policy.spi;

import com.azure.common.http.policy.HttpPipelinePolicy;

public interface PolicyProvider {
    HttpPipelinePolicy create();
}
