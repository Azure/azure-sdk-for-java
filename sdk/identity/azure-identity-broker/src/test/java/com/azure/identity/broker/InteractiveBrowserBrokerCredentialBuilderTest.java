// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.broker;

import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.util.ClientOptions;
import com.azure.identity.InteractiveBrowserCredential;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


class InteractiveBrowserBrokerCredentialBuilderTest {

    @Test
    public void canEnableLegacyMsa() {

        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.enableLegacyMsaPassthrough();
            InteractiveBrowserCredential credential = builder.build();
        });
    }

    @Test
    public void canSetWindowHandle() {} {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.setWindowHandle(1L);
            InteractiveBrowserCredential credential = builder.build();
        });
    }


    @Test
    void clientOptions() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.clientOptions(new ClientOptions());
        });
    }

    @Test
    void addPolicy() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.addPolicy(new AddDatePolicy());
        });
    }

    @Test
    void port() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.port(1);
        });
    }

    @Test
    void additionallyAllowedTenants() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.additionallyAllowedTenants("tenant");
        });
    }

    @Test
    void authenticationRecord() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.authenticationRecord(null);
        });
    }

    @Test
    void browserCustomizationOptions() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.browserCustomizationOptions(null);
        });
    }

    @Test
    void clientId() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.clientId("client");
        });
    }

    @Test
    void authorityHost() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.authorityHost("https://localhost");
        });
    }

    @Test
    void disableAutomaticAuthentication() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.disableAutomaticAuthentication();
        });
    }

    @Test
    void disableInstanceDiscovery() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.disableInstanceDiscovery();
        });
    }

    @Test
    void enableAccountIdentifierLogging() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.enableAccountIdentifierLogging();
        });
    }

    @Test
    void enableUnsafeSupportLogging() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.enableUnsafeSupportLogging();
        });
    }

    @Test
    void executorService() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.executorService(null);
        });
    }

    @Test
    void httpLogOptions() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.httpLogOptions(null);
        });
    }

    @Test
    void loginHint() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.loginHint("hint");
        });
    }

    @Test
    void pipeline() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.pipeline(null);
        });
    }

    @Test
    void maxRetry() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.maxRetry(1);
        });
    }

    @Test
    void redirectUrl() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.redirectUrl("url");
        });
    }

    @Test
    void testAdditionallyAllowedTenants() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.additionallyAllowedTenants("tenant");
        });
    }

    @Test
    void retryOptions() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.retryOptions(null);
        });
    }

    @Test
    void retryPolicy() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.retryPolicy(null);
        });
    }

    @Test
    void tenantId() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.tenantId("tenant");
        });
    }

    @Test
    void retryTimeout() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.retryTimeout(null);
        });
    }

    @Test
    void tokenCachePersistenceOptions() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.tokenCachePersistenceOptions(null);
        });
    }

    @Test
    void httpPipeline() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.httpPipeline(null);
        });
    }

    @Test
    void proxyOptions() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.proxyOptions(null);
        });
    }

    @Test
    void setDefaultBrokerAccount() {
        assertDoesNotThrow(() -> {
            InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
            builder.useDefaultBrokerAccount();
            builder.build();
        });
    }
}
