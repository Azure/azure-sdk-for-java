// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.edgeactions;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.resourcemanager.edgeactions.fluent.models.VersionCodeInner;
import com.azure.resourcemanager.edgeactions.models.EdgeAction;
import com.azure.resourcemanager.edgeactions.models.EdgeActionIsDefaultVersion;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersion;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersionDeploymentType;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersionProperties;
import com.azure.resourcemanager.edgeactions.models.ProvisioningState;
import com.azure.resourcemanager.edgeactions.models.SkuType;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract class EdgeActionsTestBase extends ResourceManagerTestProxyTestBase {
    protected EdgeActionsManager manager;
    protected String resourceGroupName;
    protected String edgeActionName;

    EdgeActionsTestBase() {
        addSanitizers(edgeActionsSanitizers().toArray(new TestProxySanitizer[0]));
    }

    static List<TestProxySanitizer> edgeActionsSanitizers() {
        // TestProxyUtils embeds patterns directly in JSON, so regex escapes also need JSON escaping.
        String subscription = "(?i)(?<=/subscriptions/)[^/?\\\\s\\\"\\\\\\\\#]+";
        String zeroUuid = "00000000-0000-0000-0000-000000000000";
        // Stop at JSON string boundaries as well as query delimiters when sanitizing embedded polling URLs.
        String pollingSecret = "(?i)(?<=[?&][tcsh]=)[^&\\\\s\\\"\\\\\\\\#]+";
        List<TestProxySanitizer> sanitizers = new ArrayList<>(Arrays.asList(
            new TestProxySanitizer(subscription, zeroUuid, TestProxySanitizerType.BODY_REGEX),
            new TestProxySanitizer("$..systemData.createdBy", null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..systemData.lastModifiedBy", null, REDACTED_VALUE,
                TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer(pollingSecret, REDACTED_VALUE, TestProxySanitizerType.URL),
            new TestProxySanitizer(pollingSecret, REDACTED_VALUE, TestProxySanitizerType.BODY_REGEX)));
        for (String field : new String[] { "tenantId", "clientId", "principalId" }) {
            sanitizers.add(new TestProxySanitizer("$.." + field, null, zeroUuid, TestProxySanitizerType.BODY_KEY));
        }
        for (String field : new String[] { "access_token", "refresh_token", "id_token", "client_secret" }) {
            sanitizers
                .add(new TestProxySanitizer("$.." + field, null, REDACTED_VALUE, TestProxySanitizerType.BODY_KEY));
        }
        for (String header : new String[] {
            "x-ms-correlation-request-id",
            "x-ms-routing-request-id",
            "x-ms-operation-identifier",
            "x-ms-original-request-ids",
            "X-MSEdge-Ref",
            "Set-Cookie",
            "Cookie" }) {
            sanitizers.add(new TestProxySanitizer(header, null, REDACTED_VALUE, TestProxySanitizerType.HEADER));
        }
        for (String header : new String[] { "Location", "Azure-AsyncOperation", "Operation-Location" }) {
            sanitizers.add(new TestProxySanitizer(header, subscription, zeroUuid, TestProxySanitizerType.HEADER));
            sanitizers
                .add(new TestProxySanitizer(header, pollingSecret, REDACTED_VALUE, TestProxySanitizerType.HEADER));
        }
        String listedEdgeActions
            = "$.value[?(@.type == 'Microsoft.Cdn/EdgeActions' || @.type == 'Microsoft.Cdn/edgeActions')]";
        sanitizers.add(new TestProxySanitizer(listedEdgeActions + ".name", "(?i)^(?!javaea[0-9a-f]+$).+$",
            REDACTED_VALUE, TestProxySanitizerType.BODY_KEY));
        sanitizers.add(new TestProxySanitizer(listedEdgeActions + ".id", "(?i)^(?!.*?/edgeactions/javaea[0-9a-f]+$).+$",
            "/subscriptions/" + zeroUuid + "/resourceGroups/REDACTED/providers/Microsoft.Cdn/edgeActions/REDACTED",
            TestProxySanitizerType.BODY_KEY));
        for (String field : new String[] { "id", "attachedResourceId" }) {
            sanitizers.add(new TestProxySanitizer(listedEdgeActions + ".properties.attachments[*]." + field, null,
                REDACTED_VALUE, TestProxySanitizerType.BODY_KEY));
        }
        return sanitizers;
    }

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile,
        HttpLogOptions httpLogOptions, List<HttpPipelinePolicy> policies, HttpClient httpClient) {
        EdgeActionsManager.Configurable configurable = EdgeActionsManager.configure()
            .withHttpClient(httpClient)
            .withLogOptions(httpLogOptions)
            .withPolicy(new EdgeActionsLroResponsePolicy());
        policies.forEach(configurable::withPolicy);
        return configurable.authenticate(credential, profile).serviceClient().getHttpPipeline();
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        manager = buildManager(EdgeActionsManager.class, httpPipeline, profile,
            isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(5));
        Assertions.assertEquals("2026-10-01", manager.serviceClient().getApiVersion());
        String configuredGroup
            = isPlaybackMode() ? null : Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        if (!isPlaybackMode() && (configuredGroup == null || configuredGroup.trim().isEmpty())) {
            throw new IllegalStateException("Set AZURE_RESOURCE_GROUP_NAME to an existing test resource group.");
        }
        resourceGroupName = testResourceNamer.recordValueFromConfig(configuredGroup);
    }

    protected EdgeAction createEdgeAction() {
        return createEdgeAction(Collections.singletonMap("purpose", "java-sdk-test"));
    }

    protected EdgeAction createEdgeAction(Map<String, String> tags) {
        String name = generateRandomResourceName("javaea", 24);
        assertNotFound(() -> manager.edgeActions().getByResourceGroup(resourceGroupName, name));
        // Track the previously absent resource before PUT so partial creation can also be cleaned up.
        edgeActionName = name;
        EdgeAction action = manager.edgeActions()
            .define(name)
            .withRegion("global")
            .withExistingResourceGroup(resourceGroupName)
            .withSku(new SkuType().withName("Standard").withTier("Standard"))
            .withTags(tags)
            .create();
        Assertions.assertEquals(ProvisioningState.SUCCEEDED, action.properties().provisioningState());
        return action;
    }

    protected EdgeActionVersion createVersion(String version, EdgeActionVersionDeploymentType deploymentType) {
        return manager.edgeActionVersions()
            .define(version)
            .withRegion("global")
            .withExistingEdgeAction(resourceGroupName, edgeActionName)
            .withProperties(new EdgeActionVersionProperties().withDeploymentType(deploymentType)
                .withIsDefaultVersion(EdgeActionIsDefaultVersion.FALSE))
            .create();
    }

    protected EdgeActionVersion deployVersion(String version, EdgeActionVersionDeploymentType deploymentType)
        throws IOException {
        EdgeActionVersion created = createVersion(version, deploymentType);
        VersionCodeInner code = new VersionCodeInner().withName("testdeployment")
            .withContent(EdgeActionsTestCode.deploymentContent(deploymentType));
        EdgeActionVersion deployed = created.deployVersionCode(code);
        Assertions.assertNotNull(deployed);
        Assertions.assertEquals(created.id(), deployed.id());
        Assertions.assertEquals(version, deployed.name());
        Assertions.assertNotNull(deployed.properties());
        Assertions.assertEquals(ProvisioningState.SUCCEEDED, deployed.properties().provisioningState());
        created.refresh();
        Assertions.assertEquals(ProvisioningState.SUCCEEDED, created.properties().provisioningState());
        return created;
    }

    protected static void assertNotFound(Executable operation) {
        ManagementException exception = Assertions.assertThrows(ManagementException.class, operation);
        Assertions.assertNotNull(exception.getResponse());
        Assertions.assertEquals(404, exception.getResponse().getStatusCode());
    }

    @Override
    protected void cleanUpResources() {
        if (edgeActionName == null) {
            return;
        }
        try {
            manager.edgeActions().getByResourceGroup(resourceGroupName, edgeActionName);
        } catch (ManagementException exception) {
            if (exception.getResponse() != null && exception.getResponse().getStatusCode() == 404) {
                return;
            }
            throw exception;
        }

        manager.edgeActionExecutionFilters()
            .listByEdgeAction(resourceGroupName, edgeActionName)
            .forEach(filter -> manager.edgeActionExecutionFilters()
                .delete(resourceGroupName, edgeActionName, filter.name()));
        List<EdgeActionVersion> versions = manager.edgeActionVersions()
            .listByEdgeAction(resourceGroupName, edgeActionName)
            .stream()
            .sorted(Comparator
                .comparing(version -> EdgeActionIsDefaultVersion.TRUE.equals(version.properties().isDefaultVersion())))
            .collect(Collectors.toList());
        // Delete the current default last, including after a successful or partially completed swap.
        for (EdgeActionVersion version : versions) {
            manager.edgeActionVersions().delete(resourceGroupName, edgeActionName, version.name());
        }
        manager.edgeActions().deleteByResourceGroup(resourceGroupName, edgeActionName);
        assertNotFound(() -> manager.edgeActions().getByResourceGroup(resourceGroupName, edgeActionName));
    }
}
