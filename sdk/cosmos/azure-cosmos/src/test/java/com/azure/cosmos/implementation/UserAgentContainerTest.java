// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserAgentContainerTest {
    private final static String SPACE = " ";
    private final static int TIMEOUT = 40000;

    @Test(groups = {"unit"})
    public void userAgentContainerSetSuffix() {
        String expectedStringFixedPart = getUserAgentFixedPart();

        //With suffix less than 64 character
        String userProvidedSuffix = "test-application-id";
        UserAgentContainer userAgentContainer = new UserAgentContainer();
        userAgentContainer.setSuffix(userProvidedSuffix);
        String expectedString = expectedStringFixedPart + SPACE + userProvidedSuffix;
        assertThat(userAgentContainer.getUserAgent()).isEqualTo(expectedString);

        //Without suffix
        userAgentContainer = new UserAgentContainer();
        expectedString = expectedStringFixedPart;
        assertThat(userAgentContainer.getUserAgent()).isEqualTo(expectedString);

        //With suffix greater than 64 character
        userProvidedSuffix = "greater than 64 characters ###########################################";
        userAgentContainer = new UserAgentContainer();
        userAgentContainer.setSuffix(userProvidedSuffix);
        expectedString = expectedStringFixedPart + SPACE + userProvidedSuffix.substring(0, 64);
        assertThat(userAgentContainer.getUserAgent()).isEqualTo(expectedString);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void UserAgentIntegration() {
        String userProvidedSuffix = "test-application-id";
        CosmosAsyncClient gatewayClient = null;
        CosmosAsyncClient directClient = null;
        try {
            gatewayClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .gatewayMode()
                .userAgentSuffix(userProvidedSuffix)
                .buildAsyncClient();
            RxDocumentClientImpl documentClient =
                (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(gatewayClient);
            UserAgentContainer userAgentContainer = ReflectionUtils.getUserAgentContainer(documentClient);
            String expectedString = getUserAgentFixedPart() + SPACE + userProvidedSuffix;
            assertThat(userAgentContainer.getUserAgent()).isEqualTo(expectedString);

            directClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .directMode()
                .userAgentSuffix(userProvidedSuffix)
                .buildAsyncClient();
            documentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(directClient);
            userAgentContainer = ReflectionUtils.getUserAgentContainer(documentClient);
            assertThat(userAgentContainer.getUserAgent()).isEqualTo(expectedString);
        } finally {
            if (gatewayClient != null) {
                gatewayClient.close();
            }

            if (directClient != null) {
                directClient.close();
            }
        }

    }

    private String getUserAgentFixedPart() {
        String osName = System.getProperty("os.name");
        if (osName == null) {
            osName = "Unknown";
        }
        osName = osName.replaceAll("\\s", "");
        String geteUserAgentFixedPart = "azsdk-java-" +
            "cosmos" +
            "/" +
            HttpConstants.Versions.SDK_VERSION +
            SPACE +
            osName +
            "/" +
            System.getProperty("os.version") +
            SPACE +
            "JRE/" +
            System.getProperty("java.version");
        return geteUserAgentFixedPart;
    }
}
