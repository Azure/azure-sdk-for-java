// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.common.http.policy.spi;

import com.azure.common.http.policy.HttpPipelinePolicy;

// TODO
public interface PolicyProvider {

    // TODO
    HttpPipelinePolicy create();
}
