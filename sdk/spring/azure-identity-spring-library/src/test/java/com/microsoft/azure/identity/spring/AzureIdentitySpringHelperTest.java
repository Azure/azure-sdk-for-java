// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.identity.spring;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;

/**
 * The unit tests for the AzureIdentitySpringHelper class.
 *
 * @author manfred.riem@microsoft.com
 */
public class AzureIdentitySpringHelperTest {

    /**
     * Test addNamedCredential method.
     */
    @Test
    public void testAddNamedCredential() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        AzureIdentitySpringHelper helper = new AzureIdentitySpringHelper();
        helper.addNamedCredential("cred1", credential);
        assertNotNull(helper.getNamedCredential("cred1"));
        helper.removeNamedCredential("cred1");
        assertNull(helper.getNamedCredential("cred1"));
    }

    /**
     * Test getDefaultCredential method.
     */
    @Test
    public void testGetDefaultCredential() {
        AzureIdentitySpringHelper helper = new AzureIdentitySpringHelper();
        assertNotNull(helper.getDefaultCredential());
    }

    /**
     * Test populate method.
     */
    @Test
    public void testPopulate() {
        System.setProperty("azure.credential.names", "");
        System.setProperty("azure.credential.tenantId", "tenantId");
        System.setProperty("azure.credential.clientId", "clientId");
        System.setProperty("azure.credential.clientSecret", "clientSecret");
        StandardEnvironment environment = new StandardEnvironment();
        AzureIdentitySpringHelper helper = new AzureIdentitySpringHelper();
        helper.populate(environment);
        assertNotNull(helper.getDefaultCredential());
        assertTrue(helper.getDefaultCredential() instanceof ClientSecretCredential);
    }

    /**
     * Test populate method.
     */
    @Test
    public void testPopulate2() {
        System.setProperty("azure.credential.names", "myname");
        System.setProperty("azure.credential.myname.tenantId", "tenantId");
        System.setProperty("azure.credential.myname.clientId", "clientId");
        System.setProperty("azure.credential.myname.clientSecret", "clientSecret");
        StandardEnvironment environment = new StandardEnvironment();
        AzureIdentitySpringHelper helper = new AzureIdentitySpringHelper();
        helper.populate(environment);
        assertNotNull(helper.getNamedCredential("myname"));
        assertTrue(helper.getNamedCredential("myname") instanceof ClientSecretCredential);
    }

    /**
     * Test populate method.
     */
    @Test
    public void testPopulate3() {
        System.setProperty("azure.credential.names", "myname2");
        System.setProperty("azure.credential.myname2.tenantId", "tenantId");
        System.setProperty("azure.credential.myname2.clientSecret", "clientSecret");
        StandardEnvironment environment = new StandardEnvironment();
        AzureIdentitySpringHelper helper = new AzureIdentitySpringHelper();
        try {
            helper.populate(environment);
            fail();
        } catch(RuntimeException re) {
        }
    }
}
