// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resourcehealth;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resourcehealth.models.AvailabilityStateValues;
import com.azure.resourcemanager.resourcehealth.models.AvailabilityStatus;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.Provider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ResourceHealthTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();

    private static final Region REGION = Region.US_WEST2;
    private static final String VM_NAME = "vm" + randomPadding();

    private String resourceGroup = "rg" + randomPadding();


    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }

    @Test
    @LiveOnly
    public void resourceHealthTest() {
        TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        ResourceManager resourceManager = ResourceManager
            .configure()
            .authenticate(credential, profile)
            .withDefaultSubscription();

        ComputeManager computeManager = ComputeManager
            .configure().withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .authenticate(credential, profile);

        ResourceHealthManager resourceHealthManager = ResourceHealthManager
            .configure().withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .authenticate(credential, profile);

        registeredProvider(resourceManager, Arrays.asList("Microsoft.Compute", "Microsoft.ResourceHealth"));

        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        boolean testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroup = testResourceGroup;
        } else {
            resourceManager.resourceGroups().define(resourceGroup)
                .withRegion(REGION)
                .create();
        }

        try {
            // create vm
            VirtualMachine virtualMachine =
                computeManager
                    .virtualMachines()
                    .define(VM_NAME)
                    .withRegion(REGION)
                    .withExistingResourceGroup(resourceGroup)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                    .withRootUsername("azuser")
                    .withRootPassword("Pa5$123456")
                    .withSize(VirtualMachineSizeTypes.STANDARD_B1S)
                    .create();

            // get current availability status
            AvailabilityStatus vmAvailabilityStatus = resourceHealthManager.availabilityStatuses().getByResource(virtualMachine.id());
            while (!AvailabilityStateValues.AVAILABLE.equals(vmAvailabilityStatus.properties().availabilityState())) {
                sleepIfRunningAgainstService(1000 * 10);
                vmAvailabilityStatus = resourceHealthManager.availabilityStatuses().getByResource(virtualMachine.id());
            }
            Assertions.assertEquals(AvailabilityStateValues.AVAILABLE, vmAvailabilityStatus.properties().availabilityState());
            PagedIterable<AvailabilityStatus> historyEvents = resourceHealthManager.availabilityStatuses().list(virtualMachine.id());
            Assertions.assertEquals(AvailabilityStateValues.AVAILABLE, historyEvents.iterator().next().properties().availabilityState());

            // deallocate vm
            virtualMachine.deallocate();
            vmAvailabilityStatus = resourceHealthManager.availabilityStatuses().getByResource(virtualMachine.id());
            while (!AvailabilityStateValues.UNAVAILABLE.equals(vmAvailabilityStatus.properties().availabilityState())) {
                sleepIfRunningAgainstService(1000 * 10);
                vmAvailabilityStatus = resourceHealthManager.availabilityStatuses().getByResource(virtualMachine.id());
            }
            Assertions.assertEquals(AvailabilityStateValues.UNAVAILABLE, vmAvailabilityStatus.properties().availabilityState());

            // start vm again
            virtualMachine.start();
            vmAvailabilityStatus = resourceHealthManager.availabilityStatuses().getByResource(virtualMachine.id());
            while (!AvailabilityStateValues.AVAILABLE.equals(vmAvailabilityStatus.properties().availabilityState())) {
                sleepIfRunningAgainstService(1000 * 10);
                vmAvailabilityStatus = resourceHealthManager.availabilityStatuses().getByResource(virtualMachine.id());
            }

            historyEvents = resourceHealthManager.availabilityStatuses().list(virtualMachine.id(), null, "recommendedactions", Context.NONE);
            Assertions.assertTrue(historyEvents.stream().count() > 0);
//            Assertions.assertTrue(
//                historyEvents
//                    .stream()
//                    .anyMatch(
//                        status -> "current".equals(status.name())
//                            && AvailabilityStateValues.AVAILABLE.equals(status.properties().availabilityState())));
        } finally {
            if (!testEnv) {
                resourceManager.resourceGroups().beginDeleteByName(resourceGroup);
            }
        }
    }

    /**
     * Make sure the resource provider has been registered with this subscription.
     * So add function for registration provider.
     *
     * @param resourceManager the resource manager
     * @param resourceProviderNamespaces the namespace of resource providers
     */
    private static void registeredProvider(ResourceManager resourceManager, List<String> resourceProviderNamespaces) {
        resourceProviderNamespaces.forEach(resourceProviderNamespace -> {
            Provider provider = resourceManager.providers().getByName(resourceProviderNamespace);
            if (!"Registered".equalsIgnoreCase(provider.registrationState())
                && !"Registering".equalsIgnoreCase(provider.registrationState()) ) {
                provider = resourceManager.providers().register(resourceProviderNamespace);
            }
            while (!"Registered".equalsIgnoreCase(provider.registrationState())) {
                ResourceManagerUtils.sleep(Duration.ofSeconds(5));
                provider = resourceManager.providers().getByName(resourceProviderNamespace);
            }
        });
    }
}
