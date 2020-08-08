// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;

/**
 * Azure Compute sample for assigning service identity to virtual machine scale set using newly created service principal
 *  - Create a VM scale-set
 *  - Create a managed service identity #1 and create a service principal. Configure the service principal to have 2 permissions, to update the scale set and assign the managed service identity #1 to the scale set
 *  - Create a managed service identity #2
 *  - Login using created service principle and verify it can assign/remove identity #1, but not #2
 */
public final class ManageScaleSetUserAssignedMSIFromServicePrincipal {
    private static final ClientLogger LOGGER = new ClientLogger(ManageScaleSetUserAssignedMSIFromServicePrincipal.class);
    /**
     * Main function which runs the actual sample.
     *
     * @param authenticated instance of Authenticated
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure.Authenticated authenticated) {
        Region region = Region.US_WEST_CENTRAL;
        String vmssName = authenticated.sdkContext().randomResourceName("vmss", 15);
        String spName1 = authenticated.sdkContext().randomResourceName("sp1", 21);
        String rgName = authenticated.sdkContext().randomResourceName("rg", 22);
        String identityName1 = authenticated.sdkContext().randomResourceName("msi-id1", 15);
        String identityName2 = authenticated.sdkContext().randomResourceName("msi-id1", 15);
        ServicePrincipal servicePrincipal = null;
        String subscription = "0b1f6471-1bf0-4dda-aec3-cb9272f09590";

        final String userName = "tirekicker";
        final String password = com.azure.resourcemanager.samples.Utils.password();

        Azure azure = null;

        try {
            azure = authenticated.withDefaultSubscription();

            System.out.println("Creating network for virtual machine scale sets");

            // ============================================================
            // Create Virtual Machine Scale Set
            Network network = azure.networks()
                    .define("vmssvnet")
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("subnet1", "10.0.0.0/28")
                    .create();

            VirtualMachineScaleSet virtualMachineScaleSet1 = azure.virtualMachineScaleSets()
                    .define(vmssName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D3_V2)
                    .withExistingPrimaryNetworkSubnet(network, "subnet1")
                    .withoutPrimaryInternetFacingLoadBalancer()
                    .withoutPrimaryInternalLoadBalancer()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .create();

            // ============================================================
            // Create a managed service identity #1 and create a service principal. Configure the service principal to have 2 permissions, to update the scale set and assign the managed service identity #1 to the scale set
            servicePrincipal = authenticated.servicePrincipals().define(spName1)
                    .withNewApplication("http://lenala.azure.com/ansp/" + spName1)
                    .definePasswordCredential("sppass")
                    .withPasswordValue("StrongPass!12")
                    .attach()
                    .withNewRole(BuiltInRole.CONTRIBUTOR, Utils.resourceGroupId(virtualMachineScaleSet1.id()))
                    .create();

            Identity identity1 = azure.identities().define(identityName1)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .create();

            servicePrincipal.update()
                    .withNewRole(BuiltInRole.MANAGED_IDENTITY_OPERATOR, identity1.id())
                    .apply();

            // ============================================================
            // Create a managed service identity #2
            Identity identity2 = azure.identities().define(identityName2)
                    .withRegion(region)
                    .withNewResourceGroup(rgName + "2")
                    .create();

            // ============================================================
            // Login using created service principle and verify it can assign/remove identity #1, but not #2
            ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(servicePrincipal.applicationId())
                .tenantId(servicePrincipal.manager().tenantId())
                .clientSecret("\"StrongPass!12\"")
                .authorityHost(AzureEnvironment.AZURE.getActiveDirectoryEndpoint())
                .build();
            AzureProfile profile = new AzureProfile(null, subscription, AzureEnvironment.AZURE);
            ComputeManager computeManager1 = ComputeManager.authenticate(credential, profile);

            VirtualMachineScaleSet vmss = computeManager1.virtualMachineScaleSets().getById(virtualMachineScaleSet1.id());

            vmss.update()
                    .withExistingUserAssignedManagedServiceIdentity(identity1)
                    .apply();

            // verify that cannot assign user identity #2 as service principal does not have permissions
            try {
                vmss.update()
                        .withExistingUserAssignedManagedServiceIdentity(identity2)
                        .apply();
                throw LOGGER.logExceptionAsError(
                    new RuntimeException("Should not be able to assign identity #2 as service principal does not have permissions")
                );
            } catch (ManagementException ex) {
                ex.printStackTrace();
            }
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            if (azure != null) {
                azure.resourceGroups().beginDeleteByName(rgName);
                try {
                    authenticated.servicePrincipals().deleteById(servicePrincipal.id());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
            try {
                authenticated.activeDirectoryApplications().deleteById(authenticated.activeDirectoryApplications().getByName(servicePrincipal.applicationId()).id());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .build();

            Azure.Authenticated authenticated = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile);

            runSample(authenticated);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageScaleSetUserAssignedMSIFromServicePrincipal() {
    }

}
