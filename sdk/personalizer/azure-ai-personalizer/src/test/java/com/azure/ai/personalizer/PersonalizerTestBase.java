package com.azure.ai.personalizer;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.util.Objects;

import static com.azure.ai.personalizer.TestUtils.*;
import static com.azure.ai.personalizer.implementation.util.Constants.DEFAULT_POLL_INTERVAL;


public abstract class PersonalizerTestBase extends TestBase {

    Duration durationTestMode;

    /**
     * Use duration of nearly zero value for PLAYBACK test mode, otherwise, use default duration value for LIVE mode.
     */
    @Override
    protected void beforeTest() {
        durationTestMode = interceptorManager.isPlaybackMode() ? ONE_NANO_DURATION : DEFAULT_POLL_INTERVAL;
    }

    public PersonalizerClientBuilder getPersonalizerClientBuilder(HttpClient httpClient,
                                                                  PersonalizerServiceVersion serviceVersion,
                                                                  boolean useKeyCredential) {
        String endpoint = getEndpoint();
        PersonalizerAudience audience = TestUtils.getAudience(endpoint);

        PersonalizerClientBuilder builder = new PersonalizerClientBuilder()
            .endpoint(endpoint)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy())
            .audience(audience);

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else {
            if (useKeyCredential) {
                builder.credential(new AzureKeyCredential(PERSONALIZER_API_KEY_SINGLE_SLOT));
            } else {
                builder.credential(getCredentialByAuthority(endpoint));
            }
        }
        return builder;
    }

    static TokenCredential getCredentialByAuthority(String endpoint) {
        String authority = TestUtils.getAuthority(endpoint);
        if (Objects.equals(authority, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)) {
            return new DefaultAzureCredentialBuilder()
                .authorityHost(TestUtils.getAuthority(endpoint))
                .build();
        } else {
            return new ClientSecretCredentialBuilder()
                .tenantId(AZURE_TENANT_ID)
                .clientId(AZURE_CLIENT_ID)
                .clientSecret(AZURE_PERSONALIZER_CLIENT_SECRET)
                .authorityHost(authority)
                .build();
        }
    }

    private String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080" : PERSONALIZER_ENDPOINT_SINGLE_SLOT;
    }
}
