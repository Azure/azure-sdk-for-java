// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.ContextTagKeys;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ServiceAttributes;
import io.opentelemetry.semconv.incubating.ServiceIncubatingAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

class ResourceParserTest {

    private static final String DEFAULT_ROLE_NAME = "unknown_service:java";
    private static final String DEFAULT_ROLE_INSTANCE = HostName.get();
    private MetricTelemetryBuilder builder;

    @BeforeEach
    void setup() {
        builder = MetricTelemetryBuilder.create();
    }

    @Test
    void testDefaultResource() {
        new ResourceParser().updateRoleNameAndInstanceAndVersion(builder, Resource.create(Attributes.empty()));

        Map<String, String> tags = builder.build().getTags();
        assertThat(tags).contains(entry(ContextTagKeys.AI_CLOUD_ROLE.toString(), DEFAULT_ROLE_NAME),
            entry(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), DEFAULT_ROLE_INSTANCE)).hasSize(2);
    }

    @Test
    void testServiceNameFromResource() {
        Resource resource = createTestResource("fake-service-name", null, null);

        new ResourceParser().updateRoleNameAndInstanceAndVersion(builder, resource);

        Map<String, String> tags = builder.build().getTags();
        assertThat(tags).contains(entry(ContextTagKeys.AI_CLOUD_ROLE.toString(), "fake-service-name"),
            entry(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), DEFAULT_ROLE_INSTANCE)).hasSize(2);
    }

    @Test
    void testServiceInstanceFromResource() {
        Resource resource = createTestResource(null, null, "fake-service-instance");

        new ResourceParser().updateRoleNameAndInstanceAndVersion(builder, resource);

        Map<String, String> tags = builder.build().getTags();
        assertThat(tags).contains(entry(ContextTagKeys.AI_CLOUD_ROLE.toString(), DEFAULT_ROLE_NAME),
            entry(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), "fake-service-instance")).hasSize(2);
    }

    @Test
    void testVersionFromResource() {
        Resource resource = Resource.create(Attributes.of(ServiceAttributes.SERVICE_VERSION, "fake-service-version"));

        new ResourceParser().updateRoleNameAndInstanceAndVersion(builder, resource);

        Map<String, String> tags = builder.build().getTags();
        assertThat(tags).contains(entry(ContextTagKeys.AI_CLOUD_ROLE.toString(), DEFAULT_ROLE_NAME),
            entry(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), DEFAULT_ROLE_INSTANCE),
            entry(ContextTagKeys.AI_APPLICATION_VER.toString(), "fake-service-version")).hasSize(3);
    }

    @Test
    void testServiceNamespaceFromResource() {
        Resource resource = createTestResource(null, "fake-service-namespace", null);

        new ResourceParser().updateRoleNameAndInstanceAndVersion(builder, resource);

        Map<String, String> tags = builder.build().getTags();
        assertThat(tags).contains(entry(ContextTagKeys.AI_CLOUD_ROLE.toString(), "[fake-service-namespace]"),
            entry(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), DEFAULT_ROLE_INSTANCE)).hasSize(2);
    }

    @Test
    void testServiceNameAndInstanceFromResource() {
        Resource resource = createTestResource("fake-service-name", null, "fake-instance");

        new ResourceParser().updateRoleNameAndInstanceAndVersion(builder, resource);

        Map<String, String> tags = builder.build().getTags();
        assertThat(tags).contains(entry(ContextTagKeys.AI_CLOUD_ROLE.toString(), "fake-service-name"),
            entry(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), "fake-instance")).hasSize(2);
    }

    @Test
    void testServiceNameAndInstanceAndNamespaceFromResource() {
        Resource resource = createTestResource("fake-service-name", "fake-service-namespace", "fake-instance");

        new ResourceParser().updateRoleNameAndInstanceAndVersion(builder, resource);

        Map<String, String> tags = builder.build().getTags();
        assertThat(tags)
            .contains(entry(ContextTagKeys.AI_CLOUD_ROLE.toString(), "[fake-service-namespace]/fake-service-name"),
                entry(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), "fake-instance"))
            .hasSize(2);
    }

    @Test
    void testWebsiteSiteNameAndWebsiteInstanceId() {
        Map<String, String> envVars = new HashMap<>();
        envVars.put("WEBSITE_SITE_NAME", "test_website_site_name");
        envVars.put("WEBSITE_INSTANCE_ID", "test_website_instance_id");
        Resource resource = createTestResource(null, null, null);

        new ResourceParser(envVars).updateRoleNameAndInstanceAndVersion(builder, resource);

        Map<String, String> tags = builder.build().getTags();
        assertThat(tags).contains(entry(ContextTagKeys.AI_CLOUD_ROLE.toString(), "test_website_site_name"),
            entry(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), "test_website_instance_id")).hasSize(2);
    }

    private static Resource createTestResource(@Nullable String serviceName, @Nullable String serviceNameSpace,
        @Nullable String serviceInstance) {
        AttributesBuilder builder = Attributes.builder();
        if (serviceName != null) {
            builder.put(ServiceAttributes.SERVICE_NAME, serviceName);
        }
        if (serviceNameSpace != null) {
            builder.put(ServiceIncubatingAttributes.SERVICE_NAMESPACE, serviceNameSpace);
        }
        if (serviceInstance != null) {
            builder.put(ServiceIncubatingAttributes.SERVICE_INSTANCE_ID, serviceInstance);
        }
        return Resource.create(builder.build());
    }
}
