// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class AksResourceAttributesTest {

    private MetricTelemetryBuilder builder;

    @BeforeEach
    void setup() {
        builder = MetricTelemetryBuilder.create();
    }

    @Test
    void testDefault() {
        ConfigProperties config = DefaultConfigProperties.create(singletonMap("otel.resource.attributes",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java"));
        Resource resource = ResourceConfiguration.createEnvironmentResource(config);

        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        Map<String, String> map = new HashMap<>(2);
        map.put(ContextTagKeys.AI_CLOUD_ROLE.toString(), "unknown_service:java");
        map.put(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), HostName.get());
        assertThat(builder.build().getTags()).containsExactlyInAnyOrderEntriesOf(map);
    }

    @Test
    void testServiceNameAndK8sPodName() {
        ConfigProperties config = DefaultConfigProperties.create(singletonMap("otel.resource.attributes",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=test-service-name,k8s.pod.name=test-pod-name"));
        Resource resource = ResourceConfiguration.createEnvironmentResource(config);

        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-service-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString())).isEqualTo("test-pod-name");
    }

    @Test
    void testK8sDeploymentName() {
        ConfigProperties config = DefaultConfigProperties.create(singletonMap("otel.resource.attributes",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java,k8s.deployment.name=test-deployment-name,k8s.pod.name=test-pod-name"));
        Resource resource = ResourceConfiguration.createEnvironmentResource(config);

        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-deployment-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString())).isEqualTo("test-pod-name");
    }

    @Test
    void testK8sReplicaSetName() {
        ConfigProperties config = DefaultConfigProperties.create(singletonMap("otel.resource.attributes",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java,k8s.replicaset.name=test-replicaset-name,k8s.pod.name=test-pod-name"));
        Resource resource = ResourceConfiguration.createEnvironmentResource(config);

        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-replicaset-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString())).isEqualTo("test-pod-name");
    }

    @Test
    void testK8sStatefulSetName() {
        ConfigProperties config = DefaultConfigProperties.create(singletonMap("otel.resource.attributes",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java,k8s.statefulset.name=test-statefulset-name,k8s.pod.name=test-pod-name"));
        Resource resource = ResourceConfiguration.createEnvironmentResource(config);

        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-statefulset-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString())).isEqualTo("test-pod-name");
    }

    @Test
    void testKsJobName() {
        ConfigProperties config = DefaultConfigProperties.create(singletonMap("otel.resource.attributes",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java,k8s.job.name=test-job-name,k8s.pod.name=test-pod-name"));
        Resource resource = ResourceConfiguration.createEnvironmentResource(config);

        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-job-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString())).isEqualTo("test-pod-name");
    }

    @Test
    void testK8sCronJobName() {
        ConfigProperties config = DefaultConfigProperties.create(singletonMap("otel.resource.attributes",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java,k8s.cronjob.name=test-cronjob-name,k8s.pod.name=test-pod-name"));
        Resource resource = ResourceConfiguration.createEnvironmentResource(config);

        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-cronjob-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString())).isEqualTo("test-pod-name");
    }

    @Test
    void testK8sDaemonSetName() {
        ConfigProperties config = DefaultConfigProperties.create(singletonMap("otel.resource.attributes",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java,k8s.daemonset.name=test-daemonset-name,k8s.pod.name=test-pod-name"));
        Resource resource = ResourceConfiguration.createEnvironmentResource(config);

        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-daemonset-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString())).isEqualTo("test-pod-name");
    }
}
