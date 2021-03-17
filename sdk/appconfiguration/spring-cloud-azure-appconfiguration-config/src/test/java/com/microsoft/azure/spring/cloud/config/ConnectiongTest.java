// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.spring.cloud.config.resource.Connection;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionPool;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static com.microsoft.azure.spring.cloud.config.resource.Connection.ENDPOINT_ERR_MSG;
import static org.assertj.core.api.Assertions.assertThat;

public class ConnectiongTest {
    private static final String NO_ENDPOINT_CONN_STRING = "Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";
    private static final String NO_ID_CONN_STRING =
            "Endpoint=https://fake.test.config.io;Secret=ZmFrZS1jb25uLXNlY3JldA==";
    private static final String NO_SECRET_CONN_STRING = "Endpoint=https://fake.test.config.io;Id=fake-conn-id;";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void endpointMustExistInConnectionString() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(ENDPOINT_ERR_MSG);
        new Connection(NO_ENDPOINT_CONN_STRING);
    }

    @Test
    public void idMustExistInConnectionString() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(ENDPOINT_ERR_MSG);
        new Connection(NO_ID_CONN_STRING);
    }

    @Test
    public void secretMustExistInConnectionString() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(ENDPOINT_ERR_MSG);
        new Connection(NO_SECRET_CONN_STRING);
    }

    @Test
    public void validConnectionStringCanBeExtracted() {
        Connection connString = new Connection(TEST_CONN_STRING);
        assertConnStringFieldsValid(connString);
    }

    @Test
    public void connectionPoolMapCanBePut() {
        ConnectionPool pool = new ConnectionPool();
        pool.put(TEST_STORE_NAME, TEST_CONN_STRING);
        Connection connString = pool.get(TEST_STORE_NAME);
        assertConnStringFieldsValid(connString);
    }

    @Test
    public void nullConnectionPoolShouldNotBePut() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Connection string should not be null.");
        ConnectionPool pool = new ConnectionPool();
        Connection nullConnString = null;
        pool.put(TEST_STORE_NAME, nullConnString);
    }

    private void assertConnStringFieldsValid(Connection connString) {
        assertThat(connString).isNotNull();
        assertThat(connString.getEndpoint()).isEqualTo("https://fake.test.config.io");
    }
}
