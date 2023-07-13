// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AksResourceAttributes.reloadOtelResourceAttributes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD) // disable parallel test run
@ExtendWith(SystemStubsExtension.class)
public class AksResourceAttributesTest {

    @SystemStub
    EnvironmentVariables envVars = new EnvironmentVariables();

    private MetricTelemetryBuilder builder;

    @BeforeEach
    void setup() {
        builder = MetricTelemetryBuilder.create();
    }

    @Test
    void testDefault() {
        envVars.set(
            "OTEL_RESOURCE_ATTRIBUTES",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java");
        reloadOtelResourceAttributes();
        ResourceParser.updateRoleNameAndInstance(builder, Resource.empty(), getConfiguration());
        Map<String, String> map = new HashMap<>(2);
        map.put(ContextTagKeys.AI_CLOUD_ROLE.toString(), "unknown_service:java");
        map.put(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), HostName.get());
        assertThat(builder.build().getTags()).containsExactlyInAnyOrderEntriesOf(map);
    }

    @Test
    void testServiceNameAndK8sPodName() {
        envVars.set(
            "OTEL_RESOURCE_ATTRIBUTES",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=test-service-name,k8s.pod.name=test-pod-name");
        reloadOtelResourceAttributes();
        ResourceParser.updateRoleNameAndInstance(builder, Resource.empty(), getConfiguration());
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-service-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("test-pod-name");
    }

    @Test
    void testK8sDeploymentName() {
        envVars.set(
            "OTEL_RESOURCE_ATTRIBUTES",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java,k8s.deployment.name=test-deployment-name,k8s.pod.name=test-pod-name");
        reloadOtelResourceAttributes();
        ResourceParser.updateRoleNameAndInstance(builder, Resource.empty(), getConfiguration());
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-deployment-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("test-pod-name");
    }

    @Test
    void testK8sReplicaSetName() {
        envVars.set(
            "OTEL_RESOURCE_ATTRIBUTES",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java,k8s.replicaset.name=test-replicaset-name,k8s.pod.name=test-pod-name");
        reloadOtelResourceAttributes();
        ResourceParser.updateRoleNameAndInstance(builder, Resource.empty(), getConfiguration());
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-replicaset-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("test-pod-name");
    }

    @Test
    void testK8sStatefulSetName() {
        envVars.set(
            "OTEL_RESOURCE_ATTRIBUTES",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java,k8s.statefulset.name=test-statefulset-name,k8s.pod.name=test-pod-name");
        reloadOtelResourceAttributes();
        ResourceParser.updateRoleNameAndInstance(builder, Resource.empty(), getConfiguration());
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString()))
            .isEqualTo("test-statefulset-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("test-pod-name");
    }

    @Test
    void testKsJobName() {
        envVars.set(
            "OTEL_RESOURCE_ATTRIBUTES",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java,k8s.job.name=test-job-name,k8s.pod.name=test-pod-name");
        reloadOtelResourceAttributes();
        ResourceParser.updateRoleNameAndInstance(builder, Resource.empty(), getConfiguration());
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-job-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("test-pod-name");
    }

    @Test
    void testK8sCronJobName() {
        envVars.set(
            "OTEL_RESOURCE_ATTRIBUTES",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java,k8s.cronjob.name=test-cronjob-name,k8s.pod.name=test-pod-name");
        reloadOtelResourceAttributes();
        ResourceParser.updateRoleNameAndInstance(builder, Resource.empty(), getConfiguration());
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-cronjob-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("test-pod-name");
    }

    @Test
    void testK8sDaemonSetName() {
        envVars.set(
            "OTEL_RESOURCE_ATTRIBUTES",
            "cloud.provider=Azure,cloud.platform=azure_aks,service.name=unknown_service:java,k8s.daemonset.name=test-daemonset-name,k8s.pod.name=test-pod-name");
        reloadOtelResourceAttributes();
        ResourceParser.updateRoleNameAndInstance(builder, Resource.empty(), getConfiguration());
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("test-daemonset-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("test-pod-name");
    }

    private static Configuration getConfiguration() {
        return new ConfigurationBuilder(
            new TestConfigurationSource(),
            new TestConfigurationSource(),
            new TestConfigurationSource())
            .build();
    }

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
