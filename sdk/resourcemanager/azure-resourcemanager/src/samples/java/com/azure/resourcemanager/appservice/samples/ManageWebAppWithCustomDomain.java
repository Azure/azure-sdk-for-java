// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.appservice.models.CustomHostnameDnsRecordType;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryPhoneCode;
import com.azure.resourcemanager.samples.SampleUtils;

/**
 * Azure App Service sample for managing a web app with a custom domain.
 *  - Create a web app
 *  - Purchase an Azure-managed domain
 *  - Bind a subdomain of the managed domain to the web app
 */
public final class ManageWebAppWithCustomDomain {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rgNEMV_", 24);
        final String appName = SampleUtils.randomResourceName(azureResourceManager, "webapp-", 20);
        final String domainName = SampleUtils.randomResourceName(azureResourceManager, "jsdkdemo-", 20) + ".com";

        try {
            // Create a web app with a new app service plan.
            // HTTPS-only is enforced; minimum TLS 1.2 and FTPS-only are already the App Service defaults.
            WebApp app = azureResourceManager.webApps()
                .define(appName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withNewWindowsPlan(PricingTier.STANDARD_S1)
                .withHttpsOnly(true)
                .create();

            // Purchase an Azure-managed domain.
            AppServiceDomain domain = azureResourceManager.appServiceDomains()
                .define(domainName)
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

            // Bind a subdomain of the managed domain to the web app.
            app.update()
                .defineHostnameBinding()
                .withAzureManagedDomain(domain)
                .withSubDomain(appName)
                .withDnsRecordType(CustomHostnameDnsRecordType.CNAME)
                .attach()
                .apply();

            System.out.println("Bound " + appName + "." + domainName + " to web app " + app.name());
            return true;
        } finally {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ManageWebAppWithCustomDomain() {
    }
}
