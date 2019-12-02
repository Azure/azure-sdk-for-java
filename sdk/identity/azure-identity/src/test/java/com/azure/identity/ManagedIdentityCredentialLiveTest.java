package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import org.junit.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ManagedIdentityCredentialLiveTest {
    @Test
    public void testMSIEndpointOnAzure() throws Exception {
        Configuration configuration = Configuration.getGlobalConfiguration();
        org.junit.Assume.assumeTrue("Skipping MSI tests on Azure", configuration.contains(Configuration.PROPERTY_MSI_ENDPOINT));

        IdentityClient client = new IdentityClientBuilder().build();
        StepVerifier.create(client.authenticateToManagedIdentityEndpoint(
                    configuration.get(Configuration.PROPERTY_MSI_ENDPOINT),
                    configuration.get(Configuration.PROPERTY_MSI_SECRET),
                    new TokenRequestContext().addScopes("https://management.azure.com/.default")))
                .expectNextMatches(accessToken -> accessToken != null && accessToken.getToken() != null)
                .verifyComplete();
    }

    @Test
    public void testIMDSEndpointOnAzure() throws Exception {
        org.junit.Assume.assumeTrue("Skipping MSI tests on Azure", checkIMDSAvailable());

        IdentityClient client = new IdentityClientBuilder().build();
        StepVerifier.create(client.authenticateToIMDSEndpoint(
                    new TokenRequestContext().addScopes("https://management.azure.com/.default")))
                .expectNextMatches(accessToken -> accessToken != null && accessToken.getToken() != null)
                .verifyComplete();
    }

    private boolean checkIMDSAvailable() {
        StringBuilder payload = new StringBuilder();

        try {
            payload.append("api-version=");
            payload.append(URLEncoder.encode("2018-02-01", "UTF-8"));
        } catch (IOException exception) {
            return false;
        }

        HttpURLConnection connection = null;

        try {
            URL url = new URL(String.format("http://169.254.169.254/metadata/identity/oauth2/token?%s",
                    payload.toString()));

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(500);
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
