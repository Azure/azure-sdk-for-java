// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryPhoneCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DomainsTests extends AppServiceTest {
    private final String domainName = "javatest720.com";

    @Override
    protected void cleanUpResources() {
        // super.cleanUpResources();
    }

    @Test
    @Disabled(
        "Test is failing fix it. we may not intent to create a resource here but just to fetch existing resource.")
    public void canCRUDDomain() throws Exception {
        // CREATE
        AppServiceDomain domain =
            appServiceManager
                .domains()
                .define(domainName)
                .withExistingResourceGroup(rgName)
                .defineRegistrantContact()
                .withFirstName("Jianghao")
                .withLastName("Lu")
                .withEmail("jianghlu@microsoft.com")
                .withAddressLine1("1 Microsoft Way")
                .withCity("Seattle")
                .withStateOrProvince("WA")
                .withCountry(CountryIsoCode.UNITED_STATES)
                .withPostalCode("98101")
                .withPhoneCountryCode(CountryPhoneCode.UNITED_STATES)
                .withPhoneNumber("4258828080")
                .attach()
                .withDomainPrivacyEnabled(true)
                .withAutoRenewEnabled(true)
                .create();
        //        Domain domain = appServiceManager.domains().getByGroup(RG_NAME, DOMAIN_NAME);
        Assertions.assertNotNull(domain);
        domain.update().withAutoRenewEnabled(false).apply();
    }
}
