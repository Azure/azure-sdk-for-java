/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserAgentSuffixTest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(
        JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(),
        true
    );

    private CosmosClient client;
    private String databaseName;
    private String containerName;

    @Factory(dataProvider = "clientBuildersWithDirectSessionIncludeComputeGateway")
    public UserAgentSuffixTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder.contentResponseOnWriteEnabled(true));
    }

    @BeforeClass(groups = {"fast"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        this.databaseName = asyncContainer.getDatabase().getId();
        this.containerName = asyncContainer.getId();
    }

    @AfterClass(groups = {"fast"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void UserAgentSuffixWithoutSpecialCharacter() {
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
        assertThat(response.getDiagnostics().getUserAgent()).endsWith("TestUserAgent");
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void UserAgentSuffixWithSpecialCharacter() {
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
        assertThat(response.getDiagnostics().getUserAgent()).endsWith("TestUserAgent's");
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void UserAgentSuffixWithUnicodeCharacter() {
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
        assertThat(response.getDiagnostics().getUserAgent()).endsWith("UnicodeChar_InUserAgent");
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void UserAgentSuffixWithWhitespaceAndAsciiSpecialChars() {
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
        assertThat(response.getDiagnostics().getUserAgent()).endsWith("UserAgent with space$%_^()*&");
    }
}
