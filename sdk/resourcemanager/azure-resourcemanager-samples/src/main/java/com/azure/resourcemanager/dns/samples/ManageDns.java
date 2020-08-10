// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.dns.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.CustomHostnameDnsRecordType;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.dns.models.ARecordSet;
import com.azure.resourcemanager.dns.models.CNameRecordSet;
import com.azure.resourcemanager.dns.models.DnsRecordSet;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.samples.Utils;

/**
 * Azure DNS sample for managing DNS zones.
 *  - Create a root DNS zone (contoso.com)
 *  - Create a web application
 *  - Add a CNAME record (www) to root DNS zone and bind it to web application host name
 *  - Creates a virtual machine with public IP
 *  - Add a A record (employees) to root DNS zone that points to virtual machine public IPV4 address
 *  - Creates a child DNS zone (partners.contoso.com)
 *  - Creates a virtual machine with public IP
 *  - Add a A record (partners) to child DNS zone that points to virtual machine public IPV4 address
 *  - Delegate from root domain to child domain by adding NS records
 *  - Remove A record from the root DNS zone
 *  - Delete the child DNS zone
 */
public class ManageDns {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String customDomainName         = "THE CUSTOM DOMAIN THAT YOU OWN (e.g. contoso.com)";
        final String rgName                   = azure.sdkContext().randomResourceName("rgNEMV_", 24);
        final String webAppName               = azure.sdkContext().randomResourceName("webapp1-", 20);

        try {
            ResourceGroup resourceGroup = azure.resourceGroups().define(rgName)
                    .withRegion(Region.US_EAST2)
                    .create();

            //============================================================
            // Creates root DNS Zone

            System.out.println("Creating root DNS zone " + customDomainName + "...");
            DnsZone rootDnsZone = azure.dnsZones().define(customDomainName)
                    .withExistingResourceGroup(resourceGroup)
                    .create();

            System.out.println("Created root DNS zone " + rootDnsZone.name());
            Utils.print(rootDnsZone);

            //============================================================
            // Sets NS records in the parent zone (hosting custom domain) to make Azure DNS the authoritative
            // source for name resolution for the zone

            System.out.println("Go to your registrar portal and configure your domain " + customDomainName
                    + " with following name server addresses");
            for (String nameServer : rootDnsZone.nameServers()) {
                System.out.println(" " + nameServer);
            }
            System.out.println("Press a key after finishing above step");
            System.in.read();

            //============================================================
            // Creates a web App

            System.out.println("Creating Web App " + webAppName + "...");
            WebApp webApp = azure.webApps().define(webAppName)
                    .withRegion(Region.US_EAST2)
                    .withExistingResourceGroup(rgName)
                    .withNewWindowsPlan(PricingTier.BASIC_B1)
                    .defineSourceControl()
                        .withPublicGitRepository("https://github.com/jianghaolu/azure-site-test")
                        .withBranch("master")
                        .attach()
                    .create();
            System.out.println("Created web app " + webAppName);
            Utils.print(webApp);

            //============================================================
            // Creates a CName record and bind it with the web app

            // Step 1: Adds CName Dns record to root DNS zone that specify web app host domain as an
            // alias for www.[customDomainName]

            System.out.println("Updating DNS zone by adding a CName record...");
            rootDnsZone = rootDnsZone.update()
                    .withCNameRecordSet("www", webApp.defaultHostname())
                    .apply();
            System.out.println("DNS zone updated");
            Utils.print(rootDnsZone);

            // Waiting for a minute for DNS CName entry to propagate
            System.out.println("Waiting a minute for CName record entry to propagate...");
            SdkContext.sleep(60 * 1000);

            // Step 2: Adds a web app host name binding for www.[customDomainName]
            //         This binding action will fail if the CName record propagation is not yet completed

            System.out.println("Updating Web app with host name binding...");
            webApp.update()
                    .defineHostnameBinding()
                        .withThirdPartyDomain(customDomainName)
                        .withSubDomain("www")
                        .withDnsRecordType(CustomHostnameDnsRecordType.CNAME)
                        .attach()
                    .apply();
            System.out.println("Web app updated");
            Utils.print(webApp);

            //============================================================
            // Creates a virtual machine with public IP

            System.out.println("Creating a virtual machine with public IP...");
            VirtualMachine virtualMachine1 = azure.virtualMachines()
                    .define(azure.sdkContext().randomResourceName("employeesvm-", 20))
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(resourceGroup)
                        .withNewPrimaryNetwork("10.0.0.0/28")
                        .withPrimaryPrivateIPAddressDynamic()
                        .withNewPrimaryPublicIPAddress(azure.sdkContext().randomResourceName("empip-", 20))
                        .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                        .withAdminUsername("testuser")
                        .withAdminPassword("12NewPA$$w0rd!")
                        .withSize(VirtualMachineSizeTypes.STANDARD_D1_V2)
                        .create();
            System.out.println("Virtual machine created");

            //============================================================
            // Update DNS zone by adding a A record in root DNS zone pointing to virtual machine IPv4 address

            PublicIpAddress vm1PublicIpAddress = virtualMachine1.getPrimaryPublicIPAddress();
            System.out.println("Updating root DNS zone " + customDomainName + "...");
            rootDnsZone = rootDnsZone.update()
                    .defineARecordSet("employees")
                        .withIPv4Address(vm1PublicIpAddress.ipAddress())
                        .attach()
                    .apply();
            System.out.println("Updated root DNS zone " + rootDnsZone.name());
            Utils.print(rootDnsZone);

            // Prints the CName and A Records in the root DNS zone
            //
            System.out.println("Getting CName record set in the root DNS zone " + customDomainName + "...");
            PagedIterable<CNameRecordSet> cnameRecordSets = rootDnsZone
                    .cNameRecordSets()
                    .list();

            for (CNameRecordSet cnameRecordSet : cnameRecordSets) {
                System.out.println("Name: " + cnameRecordSet.name() + " Canonical Name: " + cnameRecordSet.canonicalName());
            }

            System.out.println("Getting ARecord record set in the root DNS zone " + customDomainName + "...");
            PagedIterable<ARecordSet> aRecordSets = rootDnsZone
                    .aRecordSets()
                    .list();

            for (ARecordSet aRecordSet : aRecordSets) {
                System.out.println("Name: " + aRecordSet.name());
                for (String ipv4Address : aRecordSet.ipv4Addresses()) {
                    System.out.println("  " + ipv4Address);
                }
            }

            //============================================================
            // Creates a child DNS zone

            String partnerSubDomainName  = "partners." + customDomainName;
            System.out.println("Creating child DNS zone " + partnerSubDomainName + "...");
            DnsZone partnersDnsZone = azure.dnsZones().define(partnerSubDomainName)
                    .withExistingResourceGroup(resourceGroup)
                    .create();
            System.out.println("Created child DNS zone " + partnersDnsZone.name());
            Utils.print(partnersDnsZone);

            //============================================================
            // Adds NS records in the root dns zone to delegate partners.[customDomainName] to child dns zone

            System.out.println("Updating root DNS zone " + rootDnsZone + "...");
            DnsRecordSet.UpdateDefinitionStages.WithNSRecordNameServerOrAttachable<DnsZone.Update> nsRecordStage = rootDnsZone
                    .update()
                        .defineNSRecordSet("partners")
                        .withNameServer(partnersDnsZone.nameServers().get(0));
            for (int i = 1; i < partnersDnsZone.nameServers().size(); i++) {
                nsRecordStage = nsRecordStage.withNameServer(partnersDnsZone.nameServers().get(i));
            }
            nsRecordStage
                    .attach()
                    .apply();
            System.out.println("Root DNS zone updated");
            Utils.print(rootDnsZone);

            //============================================================
            // Creates a virtual machine with public IP

            System.out.println("Creating a virtual machine with public IP...");
            VirtualMachine virtualMachine2 = azure.virtualMachines()
                    .define(azure.sdkContext().randomResourceName("partnersvm-", 20))
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(resourceGroup)
                        .withNewPrimaryNetwork("10.0.0.0/28")
                        .withPrimaryPrivateIPAddressDynamic()
                        .withNewPrimaryPublicIPAddress(azure.sdkContext().randomResourceName("ptnerpip-", 20))
                        .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                        .withAdminUsername("testuser")
                        .withAdminPassword("12NewPA$$w0rd!")
                        .withSize(VirtualMachineSizeTypes.STANDARD_D1_V2)
                        .create();
            System.out.println("Virtual machine created");

            //============================================================
            // Update child DNS zone by adding a A record pointing to virtual machine IPv4 address

            PublicIpAddress vm2PublicIpAddress = virtualMachine2.getPrimaryPublicIPAddress();
            System.out.println("Updating child DNS zone " + partnerSubDomainName + "...");
            partnersDnsZone = partnersDnsZone.update()
                    .defineARecordSet("@")
                        .withIPv4Address(vm2PublicIpAddress.ipAddress())
                        .attach()
                    .apply();
            System.out.println("Updated child DNS zone " + partnersDnsZone.name());
            Utils.print(partnersDnsZone);

            //============================================================
            // Removes A record entry from the root DNS zone

            System.out.println("Removing A Record from root DNS zone " + rootDnsZone.name() + "...");
            rootDnsZone = rootDnsZone.update()
                    .withoutARecordSet("employees")
                    .apply();
            System.out.println("Removed A Record from root DNS zone");
            Utils.print(rootDnsZone);

            //============================================================
            // Deletes the DNS zone

            System.out.println("Deleting child DNS zone " + partnersDnsZone.name() + "...");
            azure.dnsZones().deleteById(partnersDnsZone.id());
            System.out.println("Deleted child DNS zone " + partnersDnsZone.name());

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
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

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
