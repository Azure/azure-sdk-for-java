// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.core.util.logging.ClientLogger;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.azure.monitor.opentelemetry.exporter.implementation.ResourceAttributes.*;

public final class AksResourceAttributes {

    private static final String AZURE_AKS = "azure_aks";
    private static final String UNKNOWN_SERVICE = "unknown_service";
    private static final ClientLogger logger = new ClientLogger(AksResourceAttributes.class);
    static Map<String, String> otelResourceAttributes = initOtelResourceAttributes();

    // visible for tests only
    public static void reloadOtelResourceAttributes() {
        otelResourceAttributes = initOtelResourceAttributes();
    }

    public static Map<String, String> getOtelResourceAttributes() {
        return otelResourceAttributes;
    }

    public static boolean isAks() {
        return AZURE_AKS.equals(otelResourceAttributes.get(CLOUD_PLATFORM.toString()));
    }

    // https://github.com/aep-health-and-standards/Telemetry-Collection-Spec/blob/main/OpenTelemetry/resource/resourceMapping.md#aicloudrole-1
    public static String getAksRoleName() {
        String serviceName = otelResourceAttributes.get(SERVICE_NAME.toString());
        if (!Strings.isNullOrEmpty(serviceName) && !serviceName.startsWith(UNKNOWN_SERVICE)) {
            return serviceName;
        }
        String k8sDeploymentName = otelResourceAttributes.get(K8S_DEPLOYMENT_NAME.toString());
        if (!Strings.isNullOrEmpty(k8sDeploymentName)) {
            return k8sDeploymentName;
        }
        String k8sReplicaSetName = otelResourceAttributes.get(K8S_REPLICASET_NAME.toString());
        if (!Strings.isNullOrEmpty(k8sReplicaSetName)) {
            return k8sReplicaSetName;
        }
        String k8sStatefulSetName = otelResourceAttributes.get(K8S_STATEFULSET_NAME.toString());
        if (!Strings.isNullOrEmpty(k8sStatefulSetName)) {
            return k8sStatefulSetName;
        }
        String k8sJobName = otelResourceAttributes.get(K8S_JOB_NAME.toString());
        if (!Strings.isNullOrEmpty(k8sJobName)) {
            return k8sJobName;
        }
        String k8sCronJobName = otelResourceAttributes.get(K8S_CRONJOB_NAME.toString());
        if (!Strings.isNullOrEmpty(k8sCronJobName)) {
            return k8sCronJobName;
        }
        String k8sDaemonSetName = otelResourceAttributes.get(K8S_DAEMONSET_NAME.toString());
        if (!Strings.isNullOrEmpty(k8sDaemonSetName)) {
            return k8sDaemonSetName;
        }
        return serviceName; // default to "unknown_service:java" when no attribute is available
    }

    // https://github.com/aep-health-and-standards/Telemetry-Collection-Spec/blob/main/OpenTelemetry/resource/resourceMapping.md#aicloudroleinstance-1
    public static String getAksRoleInstance() {
        String serviceInstanceId = otelResourceAttributes.get(SERVICE_INSTANCE_ID.toString());
        if (!Strings.isNullOrEmpty(serviceInstanceId)) {
            return serviceInstanceId;
        }
        String k8sPodName = otelResourceAttributes.get(K8S_POD_NAME.toString());
        if (!Strings.isNullOrEmpty(k8sPodName)) {
            return k8sPodName;
        }
        return HostName.get(); // default to hostname
    }

    // TODO remove manual parsing once SDK 1.28.0 is released
    // (see https://github.com/open-telemetry/opentelemetry-java/pull/5554)
    // visible for testing
    private static Map<String, String> initOtelResourceAttributes() {
        Map<String, String> originalMap =
            DefaultConfigProperties.create(Collections.emptyMap()).getMap("otel.resource.attributes");
        Map<String, String> decodedMap = new HashMap<>(originalMap.size());
        // Attributes specified via otel.resource.attributes follow the W3C Baggage spec and
        // characters outside the baggage-octet range are percent encoded
        // https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/sdk.md#specifying-resource-information-via-an-environment-variable
        originalMap.forEach(
            (key, value) -> {
                try {
                    decodedMap.put(key, URLDecoder.decode(value, StandardCharsets.UTF_8.displayName()));
                } catch (UnsupportedEncodingException e) {
                    logger.warning("Fail to decode OTEL_RESOURCE_ATTRIBUTES value.", e);
                }
            });
        return decodedMap;
    }

    private AksResourceAttributes() {
    }
}
