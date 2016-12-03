/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.resources.fluentcore.arm.CountryISOCode;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryPhoneCode;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DomainsTests extends AppServiceTestBase {
    private static final String RG_NAME = "javacsmrg720";
    private static final String DOMAIN_NAME = "javatest720.com";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        //resourceManager.resourceGroups().delete(RG_NAME);
    }

    @Test
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