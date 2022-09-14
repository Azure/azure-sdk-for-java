package com.azure.maps.timezone;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import org.junit.jupiter.api.Test;

public class TimezoneClientBuilderTest {
    // Test for null timezoneClientId, the client ID value
    @Test
    public void missingtimezoneClientId() {
        assertThrows(NullPointerException.class, () -> {
            final TimezoneClientBuilder builder = new TimezoneClientBuilder();
            builder.timezoneClientId(null);
        });
    }

    // Test for missing endpoint
    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            final TimezoneClientBuilder builder = new TimezoneClientBuilder();
            builder.endpoint(null);
        });
    }

    // Test for missing pipeline
    @Test
    public void missingPipeline() {
        assertThrows(NullPointerException.class, () -> {
            final TimezoneClientBuilder builder = new TimezoneClientBuilder();
            builder.pipeline(null);
        });
    }

    // Test for missing http client
    @Test
    public void missingHttpClient() {
        assertThrows(NullPointerException.class, () -> {
            final TimezoneClientBuilder builder = new TimezoneClientBuilder();
            builder.httpClient(null);
        });
    }

    // Test for missing configuration
    @Test
    public void missingConfiguration() {
        assertThrows(NullPointerException.class, () -> {
            final TimezoneClientBuilder builder = new TimezoneClientBuilder();
            builder.configuration(null);
        });
    }

    // Test for missing http log options
    @Test
    public void missingHttpLogOptions() {
        assertThrows(NullPointerException.class, () -> {
            final TimezoneClientBuilder builder = new TimezoneClientBuilder();
            builder.httpLogOptions(null);
        });
    }

    // Test for missing retry policy
    @Test
    public void missingRetryPolicy() {
        assertThrows(NullPointerException.class, () -> {
            final TimezoneClientBuilder builder = new TimezoneClientBuilder();
            builder.retryPolicy(null);
        });
    }

    // Test for missing client options
    @Test
    public void missingClientOptions() {
        assertThrows(NullPointerException.class, () -> {
            final TimezoneClientBuilder builder = new TimezoneClientBuilder();
            builder.clientOptions(null);
        });
    }

    // Test for missing add policy
    @Test
    public void missingAddPolicy() {
        assertThrows(NullPointerException.class, () -> {
            final TimezoneClientBuilder builder = new TimezoneClientBuilder();
            builder.addPolicy(null);
        });
    }

    // Test for null timezone id, valid token credential
    @Test
    public void missingTimezoneClientIdValidTokenCredential() {
        assertThrows(IllegalArgumentException.class, () -> {
            final TimezoneClientBuilder builder = new TimezoneClientBuilder();
            DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
            builder.credential(tokenCredential);
            builder.buildClient();
        });
    }

    // Test for null key credential and null token credential despite valid timezoneClientId
    @Test
    public void missingCredentials() {
        assertThrows(IllegalArgumentException.class, () -> {
            final TimezoneClientBuilder builder = new TimezoneClientBuilder();
            builder.timezoneClientId("timezoneClientId");
            builder.buildClient();
        });
    }
}
