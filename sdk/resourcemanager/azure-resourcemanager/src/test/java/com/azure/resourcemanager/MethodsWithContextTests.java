// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Context;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentMode;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MethodsWithContextTests extends ResourceManagerTestProxyTestBase {

    private String rgName;
    private final Region region = Region.US_WEST2;
    private AzureResourceManager azureResourceManager;

    final AtomicInteger createCounter = new AtomicInteger(0);
    final AtomicInteger deleteCounter = new AtomicInteger(0);
    final AtomicInteger getCounter = new AtomicInteger();
    final String correlationId = UUID.randomUUID().toString();
    final String correlationKey = "x-ms-correlation-id";
    final Context context = new Context(correlationKey, correlationId);

    @Test
    public void canGetWithContext() throws IOException {
        String deploymentName = generateRandomResourceName("dp", 15);
        String templateJson;
        try (InputStream templateStream = this.getClass().getResourceAsStream("/deploymentTemplate.json")) {
            templateJson = new BufferedReader(new InputStreamReader(templateStream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));
        }
        Map<String, Object> parametersMap = new HashMap<>();
        String nicName = generateRandomResourceName("nic", 15);
        String networkName = generateRandomResourceName("vnet", 15);
        String nsgName = generateRandomResourceName("nsg", 15);
        String asgName = generateRandomResourceName("asg", 15);
        addParameters(parametersMap, "networkInterfaceName", nicName);
        addParameters(parametersMap, "virtualNetworkName", networkName);
        addParameters(parametersMap, "networkSecurityGroupName", nsgName);
        addParameters(parametersMap, "applicationSecurityGroupName", asgName);

        Deployment deployment = azureResourceManager.deployments()
            .define(deploymentName)
            .withNewResourceGroup(rgName, region)
            .withTemplate(templateJson)
            .withParameters(parametersMap)
            .withMode(DeploymentMode.INCREMENTAL)
            .beginCreate(context)
            .getFinalResult();

        int getCount = getCounter.get();
        azureResourceManager.applicationSecurityGroups().getByResourceGroup(rgName, asgName, context);
        NetworkInterface nic
            = azureResourceManager.networkInterfaces().listByResourceGroup(rgName, context).stream().findFirst().get();
        azureResourceManager.networkSecurityGroups().getByResourceGroup(rgName, nsgName, context);
        azureResourceManager.networks().getByResourceGroup(rgName, networkName, context);
        nic.primaryIPConfiguration().listAssociatedApplicationSecurityGroups(context).stream().count();

        Assertions.assertEquals(5, getCounter.get() - getCount);

        azureResourceManager.deployments().beginDeleteById(deployment.id(), context).getFinalResult();
        azureResourceManager.resourceGroups().beginDeleteByName(rgName, null, context);

        // resource group + deployment = 2 creations
        Assertions.assertEquals(2, createCounter.get());
        // deployment + resource group = 2 deletions
        Assertions.assertEquals(2, deleteCounter.get());
    }

    private void addParameters(Map<String, Object> parametersMap, String parameterName, Object parameterValue) {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("value", parameterValue);

        parametersMap.put(parameterName, valueMap);
    }

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile,
        HttpLogOptions httpLogOptions, List<HttpPipelinePolicy> policies, HttpClient httpClient) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        VerificationPolicy verificationPolicy = new VerificationPolicy();
        policies.add(0, verificationPolicy);
        return HttpPipelineProvider.buildHttpPipeline(credential, profile, null, httpLogOptions, null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {

        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        ResourceManagerUtils.InternalRuntimeContext internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        internalContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));
        this.azureResourceManager = buildManager(AzureResourceManager.class, httpPipeline, profile);
        setInternalContext(internalContext, azureResourceManager);
    }

    @Override
    protected void cleanUpResources() {
    }

    private class VerificationPolicy implements HttpPipelinePolicy {
        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {

            if (context.getHttpRequest().getHttpMethod() == HttpMethod.PUT) {
                // verify that all co-related resource creation requests will have the Context information
                Object correlationData = context.getContext().getData(correlationKey).get();
                Assertions.assertEquals(correlationId, correlationData);
                createCounter.incrementAndGet();
            } else if (context.getHttpRequest().getHttpMethod() == HttpMethod.DELETE) {
                // verify that all co-related resource deletion requests will have the Context information
                Object correlationData = context.getContext().getData(correlationKey).get();
                Assertions.assertEquals(correlationId, correlationData);
                deleteCounter.incrementAndGet();
            } else if (context.getHttpRequest().getHttpMethod() == HttpMethod.GET) {
                // some GET requests are nested inside implementations, thus only verify methods we are interested in
                context.getData(correlationKey).ifPresent(data -> {
                    Assertions.assertEquals(correlationId, data);
                    getCounter.incrementAndGet();
                });
            }
            return next.process();
        }
    }
}
