// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.network.models.TransportProtocol;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the MIGRATION_GUIDE.md
 */
public class MigrationGuideSamples {
    // extra empty lines to compensate import lines


































    // THIS LINE MUST BE AT LINE NO. 70
    public void authetication() {
        TokenCredential credential = new ClientSecretCredentialBuilder()
            .clientId("<ClientId>")
            .clientSecret("<ClientSecret>")
            .tenantId("<TenantId>")
            .build();
        AzureProfile profile = new AzureProfile("<TenantId>", "<SubscriptionId>", AzureEnvironment.AZURE);
    }

    public void customizedPolicy(TokenCredential credential, AzureProfile profile) {
        AzureResourceManager azure = AzureResourceManager.configure()
            .withPolicy(new CustomizedPolicy())
            .authenticate(credential, profile)
            .withDefaultSubscription();
    }

    public static class CustomizedPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process();
        }
    }

    public void customizedHttpClient(TokenCredential credential, AzureProfile profile) {
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("127.0.0.1", 8888)))
            .build();

        AzureResourceManager azure = AzureResourceManager.configure()
            .withHttpClient(client)
            .authenticate(credential, profile)
            .withDefaultSubscription();
    }

    public void errorHandling(AzureResourceManager azure) {
        final String resourceGroupName = "invalid resource group name";
        try {
            azure.resourceGroups().define(resourceGroupName)
                .withRegion(Region.US_WEST2)
                .create();
        } catch (ManagementException e) {
            System.err.printf("Response code: %s%n", e.getValue().getCode());
            System.err.printf("Response message: %s%n", e.getValue().getMessage());
        }
    }

    public void asynchronizeCreation(AzureResourceManager azure) {
        String rgName = "";
        Region region = Region.US_EAST;
        String vnetName = "";
        String publicIpName = "";
        String loadBalancerName1 = "";
        String httpLoadBalancingRule = "";
        String frontendName = "";
        String backendPoolName1 = "";
        String httpProbe = "";
        String httpsLoadBalancingRule = "";
        String backendPoolName2 = "";
        String httpsProbe = "";
        String natPool50XXto22 = "";
        String natPool60XXto23 = "";

        final List<Object> createdResources = new ArrayList<>();
        azure.resourceGroups().define(rgName).withRegion(region).create();
        Flux.merge(
            azure.networks().define(vnetName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withAddressSpace("172.16.0.0/16")
                .defineSubnet("Front-end").withAddressPrefix("172.16.1.0/24").attach()
                .createAsync(),
            azure.publicIpAddresses().define(publicIpName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withLeafDomainLabel(publicIpName)
                .createAsync()
                .flatMapMany(publicIp -> Flux.merge(
                    Flux.just(publicIp),
                    azure.loadBalancers().define(loadBalancerName1)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        // Add two rules that uses above backend and probe
                        .defineLoadBalancingRule(httpLoadBalancingRule).withProtocol(TransportProtocol.TCP).fromFrontend(frontendName).fromFrontendPort(80).toBackend(backendPoolName1).withProbe(httpProbe).attach()
                        .defineLoadBalancingRule(httpsLoadBalancingRule).withProtocol(TransportProtocol.TCP).fromFrontend(frontendName).fromFrontendPort(443).toBackend(backendPoolName2).withProbe(httpsProbe).attach()
                        // Add nat pools to enable direct VM connectivity for SSH to port 22 and TELNET to port 23
                        .defineInboundNatPool(natPool50XXto22).withProtocol(TransportProtocol.TCP).fromFrontend(frontendName).fromFrontendPortRange(5000, 5099).toBackendPort(22).attach()
                        .defineInboundNatPool(natPool60XXto23).withProtocol(TransportProtocol.TCP).fromFrontend(frontendName).fromFrontendPortRange(6000, 6099).toBackendPort(23).attach()
                        // Explicitly define the frontend
                        .definePublicFrontend(frontendName).withExistingPublicIpAddress(publicIp).attach()
                        // Add two probes one per rule
                        .defineHttpProbe(httpProbe).withRequestPath("/").withPort(80).attach()
                        .defineHttpProbe(httpsProbe).withRequestPath("/").withPort(443).attach()
                        .createAsync()))
        )
            .doOnNext(createdResources::add)
            .blockLast();
    }
}
