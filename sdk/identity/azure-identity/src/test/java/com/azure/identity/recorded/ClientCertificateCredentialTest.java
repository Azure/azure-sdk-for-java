package com.azure.identity.recorded;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ClientCertificateCredentialTest extends IdentityTestBase {

    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private ClientCertificateCredential credential;

    private void initializeClient(HttpClient httpClient) {
        ClientCertificateCredentialBuilder builder = new ClientCertificateCredentialBuilder()
            .clientId(isPlaybackMode() ? "Dummy-Id" : getClientId())
            .tenantId(isPlaybackMode() ? "Dummy-Id" : getTenantId())
            .pipeline(super.getHttpPipeline(httpClient));

        credential = isPlaybackMode()
            ? builder.pemCertificate(getClass().getClassLoader().getResourceAsStream("pemCert.pem")).build()
            : builder.pemCertificate(Configuration.getGlobalConfiguration().get("AZURE_CLIENT_CERTIFICATE")).build();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void getToken(HttpClient httpClient) {
        // arrange
        initializeClient(httpClient);

        // act
        AccessToken actual = credential.getTokenSync(new TokenRequestContext().addScopes("https://vault.azure.net/.default"));

        // assert
        assertNotNull(actual);
        assertNotNull(actual.getToken());
        assertNotNull(actual.getExpiresAt());
    }
}
