/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config;

import com.microsoft.azure.spring.cloud.keyvault.config.auth.*;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

import static org.junit.Assert.assertTrue;

public class DefaultAuthenticationExecutorFactoryTest {
    private static final String CLIENT_ID = "fake-client-id";
    private static final String CLIENT_SECRET = "fake-client-secret";
    private static final String CLIENT_CERT = "test-cert.pem";

    private final Resource clientCertificate =
            new FileUrlResource(getClass().getClassLoader().getResource(CLIENT_CERT));

    private final AuthenticationExecutorFactory factory = new DefaultAuthenticationExecutorFactory();

    @Test
    public void testCreateCertificateExecutorWithCertificate() {
        Credentials credentials = new Credentials();
        credentials.setClientId(CLIENT_ID);
        credentials.setClientCertificate(clientCertificate);

        AuthenticationExecutor executor = factory.create(credentials);
        assertTrue(executor instanceof CertificateAuthenticationExecutor);
    }

    @Test
    public void testCreateCertificateExecutorWithCertificateAndSecret() {
        Credentials credentials = new Credentials();
        credentials.setClientId(CLIENT_ID);
        credentials.setClientSecret(CLIENT_SECRET);
        credentials.setClientCertificate(clientCertificate);

        AuthenticationExecutor executor = factory.create(credentials);
        assertTrue(executor instanceof CertificateAuthenticationExecutor);
    }

    @Test
    public void testCreateSecretExecutorWithSecret() {
        Credentials credentials = new Credentials();
        credentials.setClientId(CLIENT_ID);
        credentials.setClientSecret(CLIENT_SECRET);

        AuthenticationExecutor executor = factory.create(credentials);
        assertTrue(executor instanceof SecretAuthenticationExecutor);
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowErrorWithInvalidCertificate() {
        Credentials credentials = new Credentials();
        credentials.setClientId(CLIENT_ID);
        credentials.setClientCertificate(new FileSystemResource("./not-existed.pem"));

        factory.create(credentials);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowErrorWithNoCertificateAndNoSecret() {
        Credentials credentials = new Credentials();
        credentials.setClientId(CLIENT_ID);

        factory.create(credentials);
    }
}
