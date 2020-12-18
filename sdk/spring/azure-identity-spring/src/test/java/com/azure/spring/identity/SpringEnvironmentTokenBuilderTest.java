// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.identity.ClientSecretCredential;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The unit tests for the AzureIdentitySpringHelper class.
 *
 * @author manfred.riem@microsoft.com
 */
public class SpringEnvironmentTokenBuilderTest {

    /**
     * Test getDefaultCredential method.
     */
    @Test
    public void testGetDefaultCredential() {
        SpringEnvironmentTokenBuilder builder = new SpringEnvironmentTokenBuilder();
        assertNotNull(builder.build());
        assertEquals(builder.build(), builder.defaultCredential().build());
    }

    /**
     * Test populate method.
     */
    @Test
    public void testPopulate() {
        Properties properties = new Properties();
        properties.put("azure.credential.names", "");
        properties.put("azure.credential.tenantId", "tenantId");
        properties.put("azure.credential.clientId", "clientId");
        properties.put("azure.credential.clientSecret", "clientSecret");

        SpringEnvironmentTokenBuilder builder = new SpringEnvironmentTokenBuilder();
        builder.fromEnvironment(buildEnvironment(properties));

        assertNotNull(builder.build());
        assertTrue(builder.build() instanceof ClientSecretCredential);
        assertEquals(builder.build(), builder.defaultCredential().build());

    }

    /**
     * Test populate method.
     */
    @Test
    public void testPopulate2() {
        Properties properties = new Properties();
        properties.put("azure.credential.names", "myname");
        properties.put("azure.credential.myname.tenantId", "tenantId");
        properties.put("azure.credential.myname.clientId", "clientId");
        properties.put("azure.credential.myname.clientSecret", "clientSecret");

        SpringEnvironmentTokenBuilder builder = new SpringEnvironmentTokenBuilder();
        builder.fromEnvironment(buildEnvironment(properties));
        assertNotNull(builder.namedCredential("myname").build());
        assertTrue(builder.build() instanceof ClientSecretCredential);
        assertNotEquals(builder.build(), builder.defaultCredential().build());

    }

    /**
     * Test populate method.
     */
    @Test
    public void testPopulate3() {
        Properties properties = new Properties();
        properties.put("azure.credential.names", "myname2");
        properties.put("azure.credential.myname2.tenantId", "tenantId");
        properties.put("azure.credential.myname2.clientSecret", "clientSecret");

        SpringEnvironmentTokenBuilder builder = new SpringEnvironmentTokenBuilder();
        try {
            builder.fromEnvironment(buildEnvironment(properties));
            fail();
        } catch (Throwable t) {
            assertEquals(IllegalStateException.class, t.getClass(),
                "Unexpected exception class on missing configuration field.");
        }

    }

    private StandardEnvironment buildEnvironment(Properties properties) {
        StandardEnvironment environment = new StandardEnvironment();
        final MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(new PropertiesPropertySource("test", properties));

        return environment;
    }

}
