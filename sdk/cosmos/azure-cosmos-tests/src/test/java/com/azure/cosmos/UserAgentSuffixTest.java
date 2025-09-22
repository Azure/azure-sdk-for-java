/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserAgentSuffixTest extends TestSuiteBase {
    private CosmosClient client;
    private String databaseName;
    private String containerName;

    @Factory(dataProvider = "clientBuildersWithDirectSessionIncludeComputeGateway")
    public UserAgentSuffixTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder.contentResponseOnWriteEnabled(true));
    }

    @BeforeClass(groups = { "fast", "emulator" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void before_UserAgentSuffixTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        this.databaseName = asyncContainer.getDatabase().getId();
        this.containerName = asyncContainer.getId();
    }

    @AfterClass(groups = { "fast", "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeCloseSyncClient(this.client);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void userAgentSuffixWithoutSpecialCharacter() {
        CosmosClient clientWithUserAgentSuffix = getClientBuilder()
            .userAgentSuffix("TestUserAgent")
            .buildClient();

        CosmosContainerResponse response =
            clientWithUserAgentSuffix.getDatabase(this.databaseName).getContainer(this.containerName).read();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getProperties()).isNotNull();
        assertThat(response.getProperties().getId()).isEqualTo(this.containerName);
        assertThat(response.getDiagnostics()).isNotNull();
        validateUserAgentSuffix(response.getDiagnostics().getUserAgent(), "TestUserAgent");
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void userAgentSuffixWithSpecialCharacter() {
        CosmosClient clientWithUserAgentSuffix = getClientBuilder()
            .userAgentSuffix("TéstUserAgent's")
            .buildClient();

        CosmosContainerResponse response =
            clientWithUserAgentSuffix.getDatabase(this.databaseName).getContainer(this.containerName).read();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getProperties()).isNotNull();
        assertThat(response.getProperties().getId()).isEqualTo(this.containerName);
        assertThat(response.getDiagnostics()).isNotNull();
        validateUserAgentSuffix(response.getDiagnostics().getUserAgent(), "TestUserAgent's");
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void userAgentSuffixWithUnicodeCharacter() {
        CosmosClient clientWithUserAgentSuffix = getClientBuilder()
            .userAgentSuffix("UnicodeChar鱀InUserAgent")
            .buildClient();

        CosmosContainerResponse response =
            clientWithUserAgentSuffix.getDatabase(this.databaseName).getContainer(this.containerName).read();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getProperties()).isNotNull();
        assertThat(response.getProperties().getId()).isEqualTo(this.containerName);
        assertThat(response.getDiagnostics()).isNotNull();
        validateUserAgentSuffix(response.getDiagnostics().getUserAgent(), "UnicodeChar_InUserAgent");
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void userAgentSuffixWithWhitespaceAndAsciiSpecialChars() {
        CosmosClient clientWithUserAgentSuffix = getClientBuilder()
            .userAgentSuffix("UserAgent with space$%_^()*&")
            .buildClient();

        CosmosContainerResponse response =
            clientWithUserAgentSuffix.getDatabase(this.databaseName).getContainer(this.containerName).read();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getProperties()).isNotNull();
        assertThat(response.getProperties().getId()).isEqualTo(this.containerName);
        assertThat(response.getDiagnostics()).isNotNull();
        validateUserAgentSuffix(response.getDiagnostics().getUserAgent(), "UserAgent with space$%_^()*&");
    }

    private void validateUserAgentSuffix(String actualUserAgent, String expectedUserAgentSuffix) {

        if (Configs.isHttp2Enabled()) {
            expectedUserAgentSuffix = expectedUserAgentSuffix + "|F10";
        }

        assertThat(actualUserAgent).endsWith(expectedUserAgentSuffix);
    }
}
