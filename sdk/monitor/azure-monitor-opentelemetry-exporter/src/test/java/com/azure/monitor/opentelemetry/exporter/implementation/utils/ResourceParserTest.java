// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ServiceAttributes;
import io.opentelemetry.semconv.incubating.ServiceIncubatingAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

class ResourceParserTest {

    private static final String DEFAULT_ROLE_INSTANCE = HostName.get();
    private MetricTelemetryBuilder builder;

    @BeforeEach
    void setup() {
        builder = MetricTelemetryBuilder.create();
    }

    @Test
    void testDefaultResource() {
        new ResourceParser().updateRoleNameAndInstance(builder, Resource.create(Attributes.empty()));
        assertThat(builder.build().getTags())
            .contains(
                entry(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), DEFAULT_ROLE_INSTANCE));
    }

    @Test
    void testServiceNameFromResource() {
        Resource resource = createTestResource("fake-service-name", null, null);
        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("fake-service-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo(DEFAULT_ROLE_INSTANCE);
    }

    @Test
    void testServiceInstanceFromResource() {
        Resource resource = createTestResource(null, null, "fake-service-instance");
        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        assertThat(builder.build().getTags())
            .contains(
                entry(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), "fake-service-instance"));
    }

    @Test
    void testServiceNamespaceFromResource() {
        Resource resource = createTestResource(null, "fake-service-namespace", null);
        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString()))
            .isEqualTo("[fake-service-namespace]");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo(DEFAULT_ROLE_INSTANCE);
    }

    @Test
    void testServiceNameAndInstanceFromResource() {
        Resource resource = createTestResource("fake-service-name", null, "fake-instance");
        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString())).isEqualTo("fake-service-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("fake-instance");
    }

    @Test
    void testServiceNameAndInstanceAndNamespaceFromResource() {
        Resource resource =
            createTestResource("fake-service-name", "fake-service-namespace", "fake-instance");
        new ResourceParser().updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString()))
            .isEqualTo("[fake-service-namespace]/fake-service-name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("fake-instance");
    }

    @Test
    void testWebsiteSiteNameAndWebsiteInstanceId() {
        Map<String, String> envVars = new HashMap<>();
        envVars.put("WEBSITE_SITE_NAME", "test_website_site_name");
        envVars.put("WEBSITE_INSTANCE_ID", "test_website_instance_id");
        Resource resource = createTestResource(null, null, null);
        new ResourceParser(envVars).updateRoleNameAndInstance(builder, resource);
        Map<String, String> tags = builder.build().getTags();
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString()))
            .isEqualTo("test_website_site_name");
        assertThat(tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()))
            .isEqualTo("test_website_instance_id");
    }

    private static ConfigProperties getConfiguration() {
        return DefaultConfigProperties.create(Collections.singletonMap("HOSTNAME", DEFAULT_ROLE_INSTANCE));
    }

    private static Resource createTestResource(
        @Nullable String serviceName,
        @Nullable String serviceNameSpace,
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
