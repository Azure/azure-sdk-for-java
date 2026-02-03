// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerservice.implementation;

import com.azure.resourcemanager.containerservice.fluent.models.ManagedClusterInner;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KubernetesClusterImplTests {

    @Test
    void testNpeForKubernetesCluster() throws Exception {
        // npe test for KubernetesClusterImpl if inner model's properties are null

        // exclude those which do rest calls
        Set<String> excludeMethods = new HashSet<>(Arrays.asList("beginCreateAgentPool", "stopAsync", "stop", "start",
            "startAsync", "adminKubeConfigs", "adminKubeConfigContent", "userKubeConfigs", "userKubeConfigContent"));

        ManagedClusterInner inner = new ManagedClusterInner();
        KubernetesCluster cluster = new KubernetesClusterImpl("testCluster", inner, null);
        for (Method method : KubernetesCluster.class.getDeclaredMethods()) {
            if (!excludeMethods.contains(method.getName()) && method.getParameterCount() == 0) {
                method.invoke(cluster);
            }
        }
    }
}
