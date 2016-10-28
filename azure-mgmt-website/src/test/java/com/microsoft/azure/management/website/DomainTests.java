/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DomainTests extends AppServiceTestBase {
    private static final String RG_NAME = "javacsmrg319";
    private static final String DOMAIN_NAME = "javatest319-3.com";

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
        Domain domain = appServiceManager.domains()
                .define(DOMAIN_NAME)
                .withExistingResourceGroup(RG_NAME)
                .defineRegistrantContact()
                    .withFirstName("Jianghao")
                    .withLastName("Lu")
                    .withEmail("jianghlu@microsoft.com")
                    .withAddressLine1("1 Microsoft Way")
                    .withCity("Redmond")
                    .withStateOrProvince("WA")
                    .withCountry(CountryISOCode.United_States)
                    .withPostalCode("98052")
                    .withPhoneCountryCode(CountryPhoneCode.United_States)
                    .withPhoneNumber("4258828080")
                    .withOrganziation("Microsoft")
                    .attach()
                .withDomainPrivacyEnabled(false)
                .withAutoRenewEnabled(true)
                .withNameServer("f1g1ns1.dnspod.net")
                .withNameServer("f1g1ns2.dnspod.net")
                .create();
//        Domain domain = appServiceManager.domains().getByGroup(RG_NAME, DOMAIN_NAME);
        Assert.assertNotNull(domain);
        domain.update()
                .withNameServer("f1g1ns1.dnspod.net")
                .withNameServer("f1g1ns2.dnspod.net")
                .apply();
    }
}