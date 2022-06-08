// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.spring.cloud.autoconfigure.useragent.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;

import java.lang.reflect.Field;

public class UserAgentTestUtil {

    public static Object getPrivateFieldValue(Class<?> clazz, String fieldName, Object object) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    public static String getUserAgent(HttpPipeline pipeline) {
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            HttpPipelinePolicy policy = pipeline.getPolicy(i);
            if (policy instanceof UserAgentPolicy) {
                return (String) getPrivateFieldValue(UserAgentPolicy.class, "userAgent", policy);
            }
        }
        return null;
    }
}
