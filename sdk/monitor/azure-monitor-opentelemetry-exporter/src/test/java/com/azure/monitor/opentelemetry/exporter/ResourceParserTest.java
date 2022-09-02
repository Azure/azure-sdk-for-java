// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import reactor.util.annotation.Nullable;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Disabled
@Execution(SAME_THREAD)
@ExtendWith(SystemStubsExtension.class)
class ResourceParserTest {

    @SystemStub
    EnvironmentVariables envVars = new EnvironmentVariables();

    private static final String DEFAULT_ROLE_INSTANCE = "fake-hostname";
    private MetricTelemetryBuilder builder;

    @BeforeEach
    void setup() {
        builder = MetricTelemetryBuilder.create();
        envVars.set("HOSTNAME", DEFAULT_ROLE_INSTANCE);
        assertThat(System.getenv("HOSTNAME")).isEqualTo(DEFAULT_ROLE_INSTANCE);
    }

    @Test
    void testDefaultResource() {
        ResourceParser.updateRoleNameAndInstance(builder, Resource.create(Attributes.empty()));
        assertThat(builder.build().getTags())
            .containsExactly(
                entry(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), DEFAULT_ROLE_INSTANCE));
    }

    @Test
    void testServiceNameFromResource() {
        Resource resource = createTestResource("fake-service-name", null, null);
        ResourceParser.updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("fake-service-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo(DEFAULT_ROLE_INSTANCE);
    }

    @Test
    void testServiceInstanceFromResource() {
        Resource resource = createTestResource(null, null, "fake-service-instance");
        ResourceParser.updateRoleNameAndInstance(builder, resource);
        assertThat(builder.build().getTags())
            .containsExactly(
                entry(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), "fake-service-instance"));
    }

    @Test
    void testServiceNamespaceFromResource() {
        Resource resource = createTestResource(null, "fake-service-namespace", null);
        ResourceParser.updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString()))
            .isEqualTo("[fake-service-namespace]");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo(DEFAULT_ROLE_INSTANCE);
    }

    @Test
    void testServiceNameAndInstanceFromResource() {
        Resource resource = createTestResource("fake-service-name", null, "fake-instance");
        ResourceParser.updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("fake-service-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("fake-instance");
    }

    @Test
    void testServiceNameAndInstanceAndNamespaceFromResource() {
        Resource resource =
            createTestResource("fake-service-name", "fake-service-namespace", "fake-instance");
        ResourceParser.updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString()))
            .isEqualTo("[fake-service-namespace]/fake-service-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("fake-instance");
    }

    @Test
    void testDoNotOverrideCustomRoleNameAndInstance() {
        builder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), "myrolename");
        builder.addTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), "myroleinstance");
        Resource resource =
            createTestResource("fake-service-name", "fake-service-namespace", "fake-instance");
        ResourceParser.updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("myrolename");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("myroleinstance");
    }

    @Test
    void testWebsiteSiteNameAndWebsiteInstanceId() {
        envVars.set("WEBSITE_SITE_NAME", "test_website_site_name");
        envVars.set("WEBSITE_INSTANCE_ID", "test_website_instance_id");
        Resource resource = createTestResource(null, null, null);
        ResourceParser.updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString()))
            .isEqualTo("test_website_site_name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("test_website_instance_id");
    }

    private static Resource createTestResource(
        @Nullable String serviceName,
        @Nullable String serviceNameSpace,
        @Nullable String serviceInstance) {
        AttributesBuilder builder = Attributes.builder();
        if (serviceName != null) {
            builder.put(ResourceAttributes.SERVICE_NAME, serviceName);
        }
        if (serviceNameSpace != null) {
            builder.put(ResourceAttributes.SERVICE_NAMESPACE, serviceNameSpace);
        }
        if (serviceInstance != null) {
            builder.put(ResourceAttributes.SERVICE_INSTANCE_ID, serviceInstance);
        }
        return Resource.create(builder.build());
    }
}
