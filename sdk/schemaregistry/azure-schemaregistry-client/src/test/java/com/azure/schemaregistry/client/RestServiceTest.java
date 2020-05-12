/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client;

import com.azure.schemaregistry.client.rest.RestService;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;

public class RestServiceTest extends TestCase{
    public RestServiceTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(RestServiceTest.class);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testValidateRegistryUrl() {
        ArrayList<String> badUrls = new ArrayList<String>();
        badUrls.add("mock");
        badUrls.add("sb://contoso.servicebus.onebox.windows-int.net:4446");

        for (String badUrl : badUrls) {
            try {
                new RestService(badUrl, null);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        }

        new RestService("contoso.servicebus.onebox.windows-int.net:4446", null); // testing
        new RestService("contoso.servicebus.windows.net", null);
        assertTrue(true);
    }
}