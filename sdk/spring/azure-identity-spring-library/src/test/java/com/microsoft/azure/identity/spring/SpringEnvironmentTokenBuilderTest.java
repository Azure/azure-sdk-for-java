// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.identity.spring;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;

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
		System.setProperty("azure.credential.names", "");
		System.setProperty("azure.credential.tenantId", "tenantId");
		System.setProperty("azure.credential.clientId", "clientId");
		System.setProperty("azure.credential.clientSecret", "clientSecret");
		StandardEnvironment environment = new StandardEnvironment();
		SpringEnvironmentTokenBuilder builder = new SpringEnvironmentTokenBuilder();
		builder.fromEnvironment(environment);

		assertNotNull(builder.build());
		assertTrue(builder.build() instanceof ClientSecretCredential);
		assertEquals(builder.build(), builder.defaultCredential().build());
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
		SpringEnvironmentTokenBuilder builder = new SpringEnvironmentTokenBuilder();
		builder.fromEnvironment(environment);
		assertNotNull(builder.namedCredential("myname").build());
		assertTrue(builder.build() instanceof ClientSecretCredential);
		assertNotEquals(builder.build(), builder.defaultCredential().build());
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
		SpringEnvironmentTokenBuilder builder = new SpringEnvironmentTokenBuilder();
		try {
			builder.fromEnvironment(environment);
			fail();
		} catch (Throwable t) {
			assertEquals(IllegalStateException.class, t.getClass(),
					"Unexpected exception class on missing configuration field.");
		}
	}
}
