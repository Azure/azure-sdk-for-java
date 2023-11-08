// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.HostAndPortMapper;
import redis.clients.jedis.Protocol;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

import static org.mockito.Mockito.mock;

class AzureJedisClientConfigTest {

    AzureJedisClientConfig.Builder builder = AzureJedisClientConfig.builder();

    @Test
    void testCustomerParams() {
        SSLSocketFactory mockSslSocketFactory = mock(SSLSocketFactory.class);
        SSLParameters mockSslParameters = mock(SSLParameters.class);
        HostnameVerifier mockHostnameVerifier = mock(HostnameVerifier.class);
        HostAndPortMapper mockHostAndPortMapper = mock(HostAndPortMapper.class);

        builder.user("fake-user");
        builder.database(1);
        builder.clientName("fake-client-name");
        builder.connectionTimeoutMillis(1500);
        builder.socketTimeoutMillis(2000);
        builder.blockingSocketTimeoutMillis(1400);
        builder.ssl(false);
        builder.sslSocketFactory(mockSslSocketFactory);
        builder.sslParameters(mockSslParameters);
        builder.hostnameVerifier(mockHostnameVerifier);
        builder.hostAndPortMapper(mockHostAndPortMapper);

        AzureJedisClientConfig config = builder.build();

        Assertions.assertEquals("fake-user", config.getUser());
        Assertions.assertEquals(1, config.getDatabase());
        Assertions.assertEquals("fake-client-name", config.getClientName());
        Assertions.assertEquals(1500, config.getConnectionTimeoutMillis());
        Assertions.assertEquals(2000, config.getSocketTimeoutMillis());
        Assertions.assertEquals(1400, config.getBlockingSocketTimeoutMillis());
        Assertions.assertEquals(false, config.isSsl());

        Assertions.assertEquals(mockSslSocketFactory, config.getSslSocketFactory());
        Assertions.assertEquals(mockSslParameters, config.getSslParameters());
        Assertions.assertEquals(mockHostnameVerifier, config.getHostnameVerifier());
        Assertions.assertEquals(mockHostAndPortMapper, config.getHostAndPortMapper());

    }

    @Test
    void testDefaultParams() {
        AzureJedisClientConfig config = builder.build();

        Assertions.assertEquals(null, config.getUser());
        Assertions.assertEquals(Protocol.DEFAULT_DATABASE, config.getDatabase());
        Assertions.assertEquals(null, config.getClientName());
        Assertions.assertEquals(Protocol.DEFAULT_TIMEOUT, config.getConnectionTimeoutMillis());
        Assertions.assertEquals(Protocol.DEFAULT_TIMEOUT, config.getSocketTimeoutMillis());
        Assertions.assertEquals(0, config.getBlockingSocketTimeoutMillis());
        Assertions.assertEquals(false, config.isSsl());


        Assertions.assertEquals(null, config.getSslSocketFactory());
        Assertions.assertEquals(null, config.getSslParameters());
        Assertions.assertEquals(null, config.getHostnameVerifier());
        Assertions.assertEquals(null, config.getHostAndPortMapper());
    }


    @Test
    void testGetPasswordFromField() {
        builder.password("fake-password");
        AzureJedisClientConfig config = builder.build();

        Assertions.assertEquals("fake-password", config.getPassword());
    }

    @Test
    void testGetPasswordFromCredentialSupplier() {
        builder.credentialSupplier(() -> "password-from-credential-supplier");
        AzureJedisClientConfig config = builder.build();

        Assertions.assertEquals("password-from-credential-supplier", config.getPassword());
    }

    @Test
    void testGetPasswordFromCredentialSupplierReturnNull() {
        AzureJedisClientConfig config = builder.build();

        Assertions.assertEquals(null, config.getPassword());
    }

    @Test
    void testUpdatePassword() {
        builder.credentialSupplier(() -> "password-from-credential-supplier");
        AzureJedisClientConfig config = builder.build();

        Assertions.assertEquals("password-from-credential-supplier", config.getPassword());

        config.updatePassword("updated-password");
        Assertions.assertNotEquals("password-from-credential-supplier", config.getPassword());
        Assertions.assertEquals("updated-password", config.getPassword());
    }

}
