// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.cdn.models.CdnEndpoint;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryPhoneCode;
import com.azure.core.management.Region;
import com.azure.resourcemanager.samples.Utils;

import java.util.Map;

/**
 * Azure DNS sample for managing CDN profile.
 *  - Purchase a domain
 *  - Create root DNS zone
 *  - Create CNAME DNS record
 *  - Create CDN profile
 *  - Create CDN endpoint
 *  - Associate the custom domain with CDN endpoint
 */
public class ManageCdnWithCustomDomain {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgNEMV", 24);
        final String domainName = Utils.randomResourceName(azureResourceManager, "jsdkcdn", 15) + ".com";
        final String cdnProfileName = Utils.randomResourceName(azureResourceManager, "jsdkcdnp", 24);
        final String cdnEndpointName = Utils.randomResourceName(azureResourceManager, "jsdkcdne", 24);
        final Region region = Region.US_WEST;
        final String cnameRecordName = "sample";
        String customDomain = cnameRecordName + "." + domainName;

        try {
            azureResourceManager.resourceGroups().define(rgName)
                .withRegion(region)
                .create();

            //============================================================
            // Purchase a domain

            System.out.println("Purchasing a domain " + domainName + "...");
            AppServiceDomain domain = azureResourceManager.appServiceDomains().define(domainName)
                .withExistingResourceGroup(rgName)
                .defineRegistrantContact()
                    .withFirstName("Jon")
                    .withLastName("Doe")
                    .withEmail("jondoe@contoso.com")
                    .withAddressLine1("123 4th Ave")
                    .withCity("Redmond")
                    .withStateOrProvince("WA")
                    .withCountry(CountryIsoCode.UNITED_STATES)
                    .withPostalCode("98052")
                    .withPhoneCountryCode(CountryPhoneCode.UNITED_STATES)
                    .withPhoneNumber("4258828080")
                    .attach()
                .withDomainPrivacyEnabled(true)
                .withAutoRenewEnabled(false)
                .create();
            System.out.println("Purchased domain " + domain.name());

            //============================================================
            // Create root DNS zone

            System.out.println("Creating root DNS zone " + domainName + "...");
            DnsZone dnsZone = azureResourceManager.dnsZones().define(domainName)
                .withExistingResourceGroup(rgName)
                .create();
            System.out.println("Created root DNS zone " + dnsZone.name());

            //============================================================
            // Create CNAME DNS record

            System.out.println("Creating CNAME DNS record " + cnameRecordName + "...");
            dnsZone.update()
                .withCNameRecordSet(cnameRecordName, cdnEndpointName + ".azureedge.net")
                .apply();
            System.out.println("Created CNAME DNS record");

            //============================================================
            // Create CDN profile

            System.out.println("Creating CDN profile " + cdnProfileName + "...");
            CdnProfile cdnProfile = azureResourceManager.cdnProfiles().define(cdnProfileName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withStandardAkamaiSku()
                .create();
            System.out.println("Created CDN profile " + cdnProfile.name());

            //============================================================
            // Create CDN endpoint

            System.out.println("Creating CDN endpoint " + cdnEndpointName + "...");
            cdnProfile.update()
                .defineNewEndpoint(cdnEndpointName)
                    .withOrigin("origin1", "www.someDomain.net")
                    .withHttpAllowed(true)
                    .withHttpsAllowed(true)
                    .attach()
                .apply();

            Map<String, CdnEndpoint> cdnEndpoints = cdnProfile.endpoints();
            CdnEndpoint cdnEndpoint = cdnEndpoints.get(cdnEndpointName);
            System.out.println("Created CDN endpoint " + cdnEndpoint.name());

            //============================================================
            // Associate the custom domain with CDN endpoint

            System.out.println("Associating the custom domain with CDN endpoint " + cdnEndpoint.name());
            cdnProfile.update()
                .updateEndpoint(cdnEndpointName)
                    .withCustomDomain(customDomain)
                    .parent()
                .apply();
            cdnEndpoints = cdnProfile.endpoints();
            cdnEndpoint = cdnEndpoints.get(cdnEndpointName);
            System.out.println("Associated the custom domain with CDN endpoint " + cdnEndpoint.name());

            System.out.println("Listing custom domains associated with CDN endpoint " + cdnEndpoint.name() + "...");
            for (String attachedDomain : cdnEndpoint.customDomains()) {
                System.out.println(attachedDomain);
            }
            System.out.println("Listed custom domains.");

            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
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

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
