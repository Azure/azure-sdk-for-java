// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.management;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.trafficmanager.TrafficManager;
import com.azure.resourcemanager.trafficmanager.models.EndpointPropertiesSubnetsItem;
import com.azure.resourcemanager.trafficmanager.models.GeographicLocation;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerExternalEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfile;
import com.azure.resourcemanager.trafficmanager.models.TrafficRoutingMethod;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TrafficManagerTests extends ResourceManagerTestBase {
    private String rgName = null;

    protected ResourceManager resourceManager;
    protected TrafficManager trafficManager;

    @Override
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient) {
        return HttpPipelineProvider
            .buildHttpPipeline(
                credential,
                profile,
                null,
                httpLogOptions,
                null,
                new RetryPolicy("Retry-After", ChronoUnit.SECONDS),
                policies,
                httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        trafficManager = buildManager(TrafficManager.class, httpPipeline, profile);
        resourceManager = trafficManager.resourceManager();
    }

    @Override
    protected void cleanUpResources() {
        if (rgName != null) {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    @Test
    public void canGetGeographicHierarchy() {
        GeographicLocation rootLocation = this.trafficManager.profiles().getGeographicHierarchyRoot();
        Assertions.assertNotNull(rootLocation);
        Assertions.assertNotNull(rootLocation.code());
        Assertions.assertNotNull(rootLocation.name());
        Assertions.assertNotNull(rootLocation.childLocations());
        Assertions.assertNotNull(rootLocation.descendantLocations());
        Assertions.assertFalse(rootLocation.childLocations().isEmpty());
        Assertions.assertFalse(rootLocation.descendantLocations().isEmpty());
    }

    @Test
    public void canCreateUpdateProfileWithGeographicEndpoint() {
        rgName = generateRandomResourceName("tmergtest", 15);
        final String tmProfileName = generateRandomResourceName("tmpr", 15);
        final String tmProfileDnsLabel = generateRandomResourceName("tmdns", 15);

        GeographicLocation geographicLocation = this.trafficManager.profiles().getGeographicHierarchyRoot();

        GeographicLocation california = null;
        GeographicLocation bangladesh = null;
        for (GeographicLocation location : geographicLocation.descendantLocations()) {
            if (california == null && location.name().equalsIgnoreCase("California")) {
                california = location;
            }
            if (bangladesh == null && location.name().equalsIgnoreCase("Bangladesh")) {
                bangladesh = location;
            }
            if (california != null && bangladesh != null) {
                break;
            }
        }
        Assertions.assertNotNull(california);
        Assertions.assertNotNull(bangladesh);

        TrafficManagerProfile profile =
            this
                .trafficManager
                .profiles()
                .define(tmProfileName)
                .withNewResourceGroup(rgName, Region.US_EAST)
                .withLeafDomainLabel(tmProfileDnsLabel)
                .withGeographicBasedRouting()
                .defineExternalTargetEndpoint("external-ep-1")
                .toFqdn("www.gitbook.com")
                .fromRegion(Region.ASIA_EAST)
                .withGeographicLocation(california)
                .withGeographicLocation(bangladesh)
                .attach()
                .withHttpsMonitoring()
                .withTimeToLive(500)
                .create();

        Assertions.assertNotNull(profile.innerModel());
        Assertions.assertTrue(profile.trafficRoutingMethod().equals(TrafficRoutingMethod.GEOGRAPHIC));
        Assertions.assertTrue(profile.externalEndpoints().containsKey("external-ep-1"));
        TrafficManagerExternalEndpoint endpoint = profile.externalEndpoints().get("external-ep-1");
        Assertions.assertNotNull(endpoint.geographicLocationCodes());
        Assertions.assertEquals(2, endpoint.geographicLocationCodes().size());

        profile
            .update()
            .updateExternalTargetEndpoint("external-ep-1")
            .withoutGeographicLocation(california)
            .parent()
            .apply();

        Assertions.assertTrue(profile.trafficRoutingMethod().equals(TrafficRoutingMethod.GEOGRAPHIC));
        Assertions.assertTrue(profile.externalEndpoints().containsKey("external-ep-1"));
        endpoint = profile.externalEndpoints().get("external-ep-1");
        Assertions.assertNotNull(endpoint.geographicLocationCodes());
        Assertions.assertEquals(1, endpoint.geographicLocationCodes().size());
    }

    @Test
    public void canCreateTrafficManagerWithSubnetRouting() {
        rgName = generateRandomResourceName("tmergtest", 15);
        final String tmProfileName = generateRandomResourceName("tmpr", 15);
        final String tmProfileDnsLabel = generateRandomResourceName("tmdns", 15);

        EndpointPropertiesSubnetsItem subnetCidr = new EndpointPropertiesSubnetsItem();
        subnetCidr.withFirst("80.83.228.0").withScope(22);

        EndpointPropertiesSubnetsItem subnetRange = new EndpointPropertiesSubnetsItem();
        subnetRange.withFirst("25.26.27.28").withLast("29.30.31.32");

        TrafficManagerProfile profile =
            this
                .trafficManager
                .profiles()
                .define(tmProfileName)
                .withNewResourceGroup(rgName, Region.US_EAST)
                .withLeafDomainLabel(tmProfileDnsLabel)
                .withTrafficRoutingMethod(TrafficRoutingMethod.SUBNET)
                .defineExternalTargetEndpoint("external-ep-1")
                .toFqdn("www.gitbook.com")
                .fromRegion(Region.ASIA_EAST)
                .withSubnet(subnetCidr.first(), subnetCidr.scope())
                .withSubnet(subnetRange.first(), subnetRange.last())
                .attach()
                .create();

        Assertions.assertNotNull(profile.innerModel());
        Assertions.assertEquals(TrafficRoutingMethod.SUBNET, profile.trafficRoutingMethod());
        Assertions.assertTrue(profile.externalEndpoints().containsKey("external-ep-1"));
        TrafficManagerExternalEndpoint endpoint = profile.externalEndpoints().get("external-ep-1");
        Assertions.assertEquals(endpoint.subnets().size(), 2);
        boolean foundCidr = false;
        boolean foundRange = false;
        for (EndpointPropertiesSubnetsItem subnet : endpoint.subnets()) {
            if (subnet.first() != null && subnet.scope() != null) {
                if (subnet.first().equalsIgnoreCase(subnetCidr.first()) && subnet.scope().equals(subnetCidr.scope())) {
                    foundCidr = true;
                }
            }
            if (subnet.first() != null && subnet.last() != null) {
                if (subnet.first().equalsIgnoreCase(subnetRange.first())
                    && subnet.last().equalsIgnoreCase(subnetRange.last())) {
                    foundRange = true;
                }
            }
        }

        Assertions
            .assertTrue(
                foundCidr,
                String.format("The subnet %s/%d not found in the endpoint.", subnetCidr.first(), subnetCidr.scope()));
        Assertions
            .assertTrue(
                foundRange,
                String
                    .format(
                        "The subnet range %s-%s not found in the endpoint.", subnetCidr.first(), subnetCidr.last()));

        profile =
            profile
                .update()
                .updateExternalTargetEndpoint("external-ep-1")
                .withoutSubnet(subnetRange.first(), subnetRange.last())
                .parent()
                .apply();

        endpoint = profile.externalEndpoints().get("external-ep-1");
        Assertions.assertTrue(endpoint.subnets().size() == 1);

        profile = this.trafficManager.profiles().getById(profile.id());

        endpoint = profile.externalEndpoints().get("external-ep-1");
        Assertions.assertTrue(endpoint.subnets().size() == 1);
    }
}
