// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.resources.Resource;

public final class AksResourceAttributes {

    private static final String AZURE_AKS = "azure_aks";
    private static final String UNKNOWN_SERVICE = "unknown_service";

    public static boolean isAks(Resource resource) {
        return AZURE_AKS.equals(resource.getAttribute(AttributeKey.stringKey("cloud.platform")));
    }

    // https://github.com/aep-health-and-standards/Telemetry-Collection-Spec/blob/main/OpenTelemetry/resource/resourceMapping.md#aicloudrole-1
    public static String getAksRoleName(Resource resource) {
        String serviceName = resource.getAttribute(AttributeKey.stringKey("service.name"));
        if (!Strings.isNullOrEmpty(serviceName) && !serviceName.startsWith(UNKNOWN_SERVICE)) {
            return serviceName;
        }
        String k8sDeploymentName = resource.getAttribute(AttributeKey.stringKey("k8s.deployment.name"));
        if (!Strings.isNullOrEmpty(k8sDeploymentName)) {
            return k8sDeploymentName;
        }
        String k8sReplicaSetName = resource.getAttribute(AttributeKey.stringKey("k8s.replicaset.name"));
        if (!Strings.isNullOrEmpty(k8sReplicaSetName)) {
            return k8sReplicaSetName;
        }
        String k8sStatefulSetName = resource.getAttribute(AttributeKey.stringKey("k8s.statefulset.name"));
        if (!Strings.isNullOrEmpty(k8sStatefulSetName)) {
            return k8sStatefulSetName;
        }
        String k8sJobName = resource.getAttribute(AttributeKey.stringKey("k8s.job.name"));
        if (!Strings.isNullOrEmpty(k8sJobName)) {
            return k8sJobName;
        }
        String k8sCronJobName = resource.getAttribute(AttributeKey.stringKey("k8s.cronjob.name"));
        if (!Strings.isNullOrEmpty(k8sCronJobName)) {
            return k8sCronJobName;
        }
        String k8sDaemonSetName = resource.getAttribute(AttributeKey.stringKey("k8s.daemonset.name"));
        if (!Strings.isNullOrEmpty(k8sDaemonSetName)) {
            return k8sDaemonSetName;
        }
        return serviceName; // default to "unknown_service:java" when no attribute is available
    }

    // https://github.com/aep-health-and-standards/Telemetry-Collection-Spec/blob/main/OpenTelemetry/resource/resourceMapping.md#aicloudroleinstance-1
    public static String getAksRoleInstance(Resource resource) {
        String serviceInstanceId = resource.getAttribute(AttributeKey.stringKey("service.instance.id"));
        if (!Strings.isNullOrEmpty(serviceInstanceId)) {
            return serviceInstanceId;
        }
        String k8sPodName = resource.getAttribute(AttributeKey.stringKey("k8s.pod.name"));
        if (!Strings.isNullOrEmpty(k8sPodName)) {
            return k8sPodName;
        }
        return HostName.get(); // default to hostname
    }
}
