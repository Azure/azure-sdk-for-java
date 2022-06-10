// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerservice.implementation;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.resourcemanager.containerservice.fluent.models.AgentPoolInner;
import com.azure.resourcemanager.containerservice.models.ManagedClusterAgentPoolProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class KubernetesClusterAgentPoolImplTests {

    private static final Random RANDOM = new Random(3);

    @Test
    void testGetAgentPoolInner() throws Exception {
        // test case for the manual conversion of ManagedClusterAgentPoolProfile to AgentPoolInner

        Set<String> excludeMethods = new HashSet<>(Arrays.asList(
            "name",
            "type",
            "vnetSubnetId"  // skip because this had to be a well-formed resource ID
        ));

        Map<String, Object> mockValues = new HashMap<>();
        ManagedClusterAgentPoolProfile managedClusterAgentPoolProfile = new ManagedClusterAgentPoolProfile();
        for (Method method : managedClusterAgentPoolProfile.getClass().getDeclaredMethods()) {
            String name = method.getName();
            if (name.startsWith("with") && method.getParameterTypes().length > 0) {
                Class<?> parameterType = method.getParameterTypes()[0];
                Object value = null;
                if (parameterType.equals(Integer.class)) {
                    value = RANDOM.nextInt() & Integer.MAX_VALUE;
                } else if (parameterType.equals(Long.class)) {
                    value = RANDOM.nextLong() & Long.MAX_VALUE;
                } else if (parameterType.equals(String.class)) {
                    value = randomString();
                } else if (ExpandableStringEnum.class.isAssignableFrom(parameterType)) {
                    String valueStr = randomString();
                    value = parameterType.getDeclaredMethod("fromString", String.class).invoke(null, valueStr);
                }
                if (value != null) {
                    name = name.substring(4);
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                    if (!excludeMethods.contains(name)) {
                        mockValues.put(name, value);
                        method.invoke(managedClusterAgentPoolProfile, value);
                    }
                }
            }
        }
        KubernetesClusterAgentPoolImpl impl = new KubernetesClusterAgentPoolImpl(managedClusterAgentPoolProfile, null);
        AgentPoolInner agentPoolInner = impl.getAgentPoolInner();
        for (Method method : agentPoolInner.getClass().getDeclaredMethods()) {
            String name = method.getName();
            if (mockValues.containsKey(name)) {
                Object value = method.invoke(agentPoolInner);
                Assertions.assertEquals(mockValues.get(name), value, String.format("Field %s mismatch", name));
            }
        }
    }

    private static String randomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        return RANDOM.ints(leftLimit, rightLimit + 1)
            .limit(targetStringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }
}
