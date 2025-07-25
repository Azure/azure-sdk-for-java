// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.revapi.transforms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.revapi.AnalysisContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests exclusion logic for {@link ClassAndPackageTreeFilterProvider}.
 */
public class ClassAndPackageTreeFilterProviderTest {
    @ParameterizedTest
    @ValueSource(
        strings = {
            "com.azure.core.util.Configuration",
            "com.azure.cosmos.BridgeInternal",
            "com.azure.cosmos.CosmosBridgeInternal",
            "com.azure.cosmos.models.ModelBridgeInternal",
            "com.azure.cosmos.util.UtilBridgeInternal",
            "com.azure.spring.cloud.config.AppConfigurationBootstrapConfiguration",
            "com.azure.spring.cloud.config.AppConfigurationRefresh",
            "com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties",
            "com.azure.spring.cloud.config.web.AppConfigurationEndpoint",
            "com.azure.spring.cloud.config.web.pushrefresh.AppConfigurationRefreshEvent" })
    public void classesThatShouldBeExcluded(String className) {
        AnalysisContext context = AnalysisContext.builder().build().copyWithConfiguration(createDefaultConfiguration());

        try (ClassAndPackageTreeFilterProvider treeFilterProvider = new ClassAndPackageTreeFilterProvider()) {
            treeFilterProvider.initialize(context);
            assertTrue(treeFilterProvider.excludeClass(className));
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "com.azure.core.util.CoreUtils",
            "com.azure.cosmos.ConnectionMode",
            "com.azure.cosmos.CosmosClient",
            "com.azure.cosmos.models.ChangeFeedPolicy",
            "com.azure.cosmos.util.CosmosPagedFlux",
            "com.azure.spring.cloud.config.AppConfigurationConstants",
            "com.azure.spring.cloud.config.HostType",
            "com.azure.spring.cloud.config.properties.ConfigStore",
            "com.azure.spring.cloud.config.web.AppConfigurationWebAutoConfiguration",
            "com.azure.spring.cloud.config.web.pushrefresh.AppConfigurationRefreshEndpoint" })
    public void classesThatShouldNotBeExcluded(String className) {
        AnalysisContext context = AnalysisContext.builder().build().copyWithConfiguration(createDefaultConfiguration());

        try (ClassAndPackageTreeFilterProvider treeFilterProvider = new ClassAndPackageTreeFilterProvider()) {
            treeFilterProvider.initialize(context);
            assertFalse(treeFilterProvider.excludeClass(className));
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "com.azure.data.cosmos",
            "com.azure.core.implementation",
            "com.azure.resourcemanager.fluent",
            "com.azure.resourcemanager.fluent.models",
            "com.fasterxml.jackson",
            "com.google.gson",
            "com.microsoft.azure",
            "com.nimbusds",
            "io.micrometer",
            "io.netty",
            "io.vertx",
            "javax.jms",
            "javax.servlet",
            "kotlin",
            "okhttp3",
            "okio",
            "org.apache.avro",
            "org.apache.commons",
            "org.apache.qpid",
            "org.junit",
            "org.slf4j",
            "org.springframework",
            "reactor.core",
            "reactor.netty",
            "reactor.util" })
    public void packagesThatShouldBeExcluded(String packageName) {
        AnalysisContext context = AnalysisContext.builder().build().copyWithConfiguration(createDefaultConfiguration());

        try (ClassAndPackageTreeFilterProvider treeFilterProvider = new ClassAndPackageTreeFilterProvider()) {
            treeFilterProvider.initialize(context);
            assertTrue(treeFilterProvider.excludePackage(packageName));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "com.azure.cosmos", "com.azure.core.util", "com.azure.resourcemanager.fluentcore" })
    public void packagesThatShouldBeNotExcluded(String packageName) {
        AnalysisContext context = AnalysisContext.builder().build().copyWithConfiguration(createDefaultConfiguration());

        try (ClassAndPackageTreeFilterProvider treeFilterProvider = new ClassAndPackageTreeFilterProvider()) {
            treeFilterProvider.initialize(context);
            assertFalse(treeFilterProvider.excludePackage(packageName));
        }
    }

    private static JsonNode createDefaultConfiguration() {
        ObjectNode configuration = JsonNodeFactory.instance.objectNode();
        configuration.putObject("ignoredClasses")
            .putArray("com.azure.")
            .add("core.util.Configuration")
            .add("cosmos.BridgeInternal")
            .add("cosmos.CosmosBridgeInternal")
            .add("cosmos.models.ModelBridgeInternal")
            .add("cosmos.util.UtilBridgeInternal")
            .add("spring.cloud.config.AppConfigurationBootstrapConfiguration")
            .add("spring.cloud.config.AppConfigurationRefresh")
            .add("spring.cloud.config.properties.AppConfigurationProviderProperties")
            .add("spring.cloud.config.web.AppConfigurationEndpoint")
            .add("spring.cloud.config.web.pushrefresh.AppConfigurationRefreshEvent");

        ObjectNode ignoredPackages = configuration.putObject("ignoredPackages");
        ignoredPackages.putArray("com.")
            .add("azure.data.cosmos")
            .add("fasterxml.jackson")
            .add("google.gson")
            .add("microsoft.azure")
            .add("nimbusds");
        ignoredPackages.putArray("io.").add("micrometer").add("netty").add("vertx");
        ignoredPackages.putArray("javax.").add("jms").add("servlet");
        ignoredPackages.putArray("kotlin");
        ignoredPackages.putArray("okhttp3");
        ignoredPackages.putArray("okio");
        ignoredPackages.putArray("org.")
            .add("apache.avro")
            .add("apache.commons")
            .add("apache.qpid")
            .add("junit")
            .add("reactivestreams")
            .add("slf4j")
            .add("springframework");
        ignoredPackages.putArray("reactor.").add("core").add("netty").add("util");

        configuration.putArray("ignoredPackagesPatterns")
            .add("com\\.azure\\..*(implementation|samples).*")
            .add("com\\.azure\\.resourcemanager\\..*(fluent)(\\..*)?$");

        return configuration;
    }
}
