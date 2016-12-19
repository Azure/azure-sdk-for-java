/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.keyvault.implementation.KeyVaultManager;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryISOCode;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryPhoneCode;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * The base for storage manager tests.
 */
public abstract class AppServiceTestBase {
    protected static ResourceManager resourceManager;
    protected static KeyVaultManager keyVaultManager;
    protected static AppServiceManager appServiceManager;

    protected static AppServiceDomain domain;
    protected static AppServiceCertificateOrder certificateOrder;

    protected static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                AzureEnvironment.AZURE);

        RestClient restClient = AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .withNetworkInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String s) {
                        System.out.println(s);
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY))
                .withReadTimeout(1, TimeUnit.MINUTES)
                .build();

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(System.getenv("subscription-id"));

        keyVaultManager = KeyVaultManager
                .authenticate(restClient, System.getenv("domain"), System.getenv("subscription-id"));

        appServiceManager = AppServiceManager
                .authenticate(restClient, System.getenv("domain"), System.getenv("subscription-id"));

        useExistingDomainAndCertificate();
        //createNewDomainAndCertificate();
    }

    private static void useExistingDomainAndCertificate() {
        domain = appServiceManager.domains().getByGroup(System.getenv("appservice-group"), System.getenv("appservice-domain"));
        certificateOrder = appServiceManager.certificateOrders().getByGroup(System.getenv("appservice-group"), System.getenv("appservice-certificateorder"));
    }

    private static void createNewDomainAndCertificate() {
        domain = appServiceManager.domains().define(System.getenv("appservice-domain"))
                .withExistingResourceGroup(System.getenv("appservice-group"))
                .defineRegistrantContact()
                    .withFirstName("Jon")
                    .withLastName("Doe")
                    .withEmail("jondoe@contoso.com")
                    .withAddressLine1("123 4th Ave")
                    .withCity("Redmond")
                    .withStateOrProvince("WA")
                    .withCountry(CountryISOCode.UNITED_STATES)
                    .withPostalCode("98052")
                    .withPhoneCountryCode(CountryPhoneCode.UNITED_STATES)
                    .withPhoneNumber("4258828080")
                    .attach()
                .withDomainPrivacyEnabled(true)
                .withAutoRenewEnabled(true)
                .create();
        certificateOrder = appServiceManager.certificateOrders()
                .define(System.getenv("appservice-certificateorder"))
                .withExistingResourceGroup(System.getenv("appservice-group"))
                .withHostName("*." + domain.name())
                .withWildcardSku()
                .withDomainVerification(domain)
                .withNewKeyVault("graphvault", Region.US_WEST)
                .withValidYears(1)
                .create();
    }
}