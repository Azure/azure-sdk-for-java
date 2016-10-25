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
    private static final String DOMAIN_NAME = "javatest319.com";

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
                .withContact(new Contact()
                        .withNameFirst("Jianghao")
                        .withNameLast("Lu")
                        .withEmail("jianghlu@microsoft.com")
                        .withAddressMailing(new Address()
                            .withAddress1("1 Microsoft Way")
                            .withCity("Redmond")
                            .withState("WA")
                            .withCountry("US")
                            .withPostalCode("98052"))
                        .withPhone("+1.4258828080"))
                .withDomainPrivacyEnabled(false)
                .withAutoRenewEnabled(true)
                .create();
        Assert.assertNotNull(domain);
    }
}