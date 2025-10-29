/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */
package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.SkipException;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.net.ssl.SSLHandshakeException;
import java.net.InetAddress;
import java.net.URI;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.assertThat;
public class InvalidHostnameTest extends TestSuiteBase {
    @Factory(dataProvider = "clientBuilders")
    public InvalidHostnameTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void connectingFailsWhenHostnameIsInvalid() throws Exception {
        CosmosDatabase createdDatabase = null;
        CosmosClient client = null;
        CosmosClientBuilder builder = getClientBuilder();

        if (builder.getEndpoint().contains("localhost")) {
            throw new SkipException("This test is irrelevant for emulator");
        }

        try {
            URI uri = URI.create(builder.getEndpoint());
            InetAddress address = InetAddress.getByName(uri.getHost());
            URI uriWithInvalidHostname = new URI(
                uri.getScheme(),
                uri.getUserInfo(),
                address.getHostAddress(), // Use the DNS-resolved IP-address as new hostname - this is invalid form TLS cert perspective
                uri.getPort(),
                uri.getPath(),
                uri.getQuery(),
                uri.getFragment()
            );
            builder.endpoint(uriWithInvalidHostname.toString());
            client = builder.buildClient();
            String dbName = CosmosDatabaseForTest.generateId();
            createdDatabase = createSyncDatabase(client, dbName);
            fail("The attempt to connect to the Gateway endpoint to read account "
                + "metadata should have failed due to invalid hostname.");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(CosmosException.class);
            CosmosException cosmosException = (CosmosException)e.getCause();
            assertThat(cosmosException.getStatusCode()).isEqualTo(503);
            assertThat(cosmosException.getSubStatusCode())
                .isEqualTo(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE);
            assertThat(cosmosException).hasCauseInstanceOf(SSLHandshakeException.class);
            logger.info("Expected exception was thrown", cosmosException);
        }
        finally {
            if (createdDatabase != null) {
                safeDeleteSyncDatabase(createdDatabase);
            }

            if (client != null) {
                safeCloseSyncClient(client);
            }
        }
    }
}
