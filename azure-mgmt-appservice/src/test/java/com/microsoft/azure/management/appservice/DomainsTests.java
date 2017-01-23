/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.resources.fluentcore.arm.CountryISOCode;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryPhoneCode;
import org.junit.*;

public class DomainsTests extends AppServiceTest {
    private static final String DOMAIN_NAME = "javatest720.com";

    @Override
    protected void cleanUpResources() {
        //super.cleanUpResources();
    }

    @Test
    @Ignore("Test is failing fix it. we may not intent to create a resource here but just to fetch existing resource.")
    public void canCRUDDomain() throws Exception {
        // CREATE
        AppServiceDomain domain = appServiceManager.domains().define(DOMAIN_NAME)
                .withExistingResourceGroup(RG_NAME)
                .defineRegistrantContact()
                    .withFirstName("Jianghao")
                    .withLastName("Lu")
                    .withEmail("jianghlu@microsoft.com")
                    .withAddressLine1("1 Microsoft Way")
                    .withCity("Seattle")
                    .withStateOrProvince("WA")
                    .withCountry(CountryISOCode.UNITED_STATES)
                    .withPostalCode("98101")
                    .withPhoneCountryCode(CountryPhoneCode.UNITED_STATES)
                    .withPhoneNumber("4258828080")
                    .attach()
                .withDomainPrivacyEnabled(true)
                .withAutoRenewEnabled(true)
                .create();
//        Domain domain = appServiceManager.domains().getByGroup(RG_NAME, DOMAIN_NAME);
        Assert.assertNotNull(domain);
        domain.update()
                .withAutoRenewEnabled(false)
                .apply();
    }
}