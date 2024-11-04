// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ServiceAttributes;
import io.opentelemetry.semconv.incubating.CloudIncubatingAttributes;
import io.opentelemetry.semconv.incubating.K8sIncubatingAttributes;
import io.opentelemetry.semconv.incubating.ServiceIncubatingAttributes;

public final class AksResourceAttributes {

    private static final String AZURE_AKS = "azure_aks";
    private static final String UNKNOWN_SERVICE = "unknown_service";

    public static boolean isAks(Resource resource) {
        return AZURE_AKS.equals(resource.getAttribute(CloudIncubatingAttributes.CLOUD_PLATFORM));
    }

    // https://github.com/aep-health-and-standards/Telemetry-Collection-Spec/blob/main/OpenTelemetry/resource/resourceMapping.md#aicloudrole-1
    public static String getAksRoleName(Resource resource) {
        String serviceName = resource.getAttribute(ServiceAttributes.SERVICE_NAME);
        if (!Strings.isNullOrEmpty(serviceName) && !serviceName.startsWith(UNKNOWN_SERVICE)) {
            return serviceName;
        }
        String k8sDeploymentName = resource.getAttribute(K8sIncubatingAttributes.K8S_DEPLOYMENT_NAME);
        if (!Strings.isNullOrEmpty(k8sDeploymentName)) {
            return k8sDeploymentName;
        }
        String k8sReplicaSetName = resource.getAttribute(K8sIncubatingAttributes.K8S_REPLICASET_NAME);
        if (!Strings.isNullOrEmpty(k8sReplicaSetName)) {
            return k8sReplicaSetName;
        }
        String k8sStatefulSetName = resource.getAttribute(K8sIncubatingAttributes.K8S_STATEFULSET_NAME);
        if (!Strings.isNullOrEmpty(k8sStatefulSetName)) {
            return k8sStatefulSetName;
        }
        String k8sJobName = resource.getAttribute(K8sIncubatingAttributes.K8S_JOB_NAME);
        if (!Strings.isNullOrEmpty(k8sJobName)) {
            return k8sJobName;
        }
        String k8sCronJobName = resource.getAttribute(K8sIncubatingAttributes.K8S_CRONJOB_NAME);
        if (!Strings.isNullOrEmpty(k8sCronJobName)) {
            return k8sCronJobName;
        }
        String k8sDaemonSetName = resource.getAttribute(K8sIncubatingAttributes.K8S_DAEMONSET_NAME);
        if (!Strings.isNullOrEmpty(k8sDaemonSetName)) {
            return k8sDaemonSetName;
        }
        return serviceName; // default to "unknown_service:java" when no attribute is available
    }

    // https://github.com/aep-health-and-standards/Telemetry-Collection-Spec/blob/main/OpenTelemetry/resource/resourceMapping.md#aicloudroleinstance-1
    public static String getAksRoleInstance(Resource resource) {
        String serviceInstanceId = resource.getAttribute(ServiceIncubatingAttributes.SERVICE_INSTANCE_ID);
        if (!Strings.isNullOrEmpty(serviceInstanceId)) {
            return serviceInstanceId;
        }
        String k8sPodName = resource.getAttribute(K8sIncubatingAttributes.K8S_POD_NAME);
        if (!Strings.isNullOrEmpty(k8sPodName)) {
            return k8sPodName;
        }
        return HostName.get(); // default to hostname
    }
}
