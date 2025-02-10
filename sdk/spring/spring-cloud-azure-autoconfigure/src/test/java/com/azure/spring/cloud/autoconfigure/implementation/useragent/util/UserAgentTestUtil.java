// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.spring.cloud.autoconfigure.implementation.useragent.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;

import static com.azure.spring.cloud.core.implementation.util.ReflectionUtils.getField;

public class UserAgentTestUtil {

    public static String getUserAgent(HttpPipeline pipeline) {
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            HttpPipelinePolicy policy = pipeline.getPolicy(i);
            if (policy instanceof UserAgentPolicy) {
                return (String) getField(UserAgentPolicy.class, "userAgent", policy);
            }
        }
        return null;
    }
}
