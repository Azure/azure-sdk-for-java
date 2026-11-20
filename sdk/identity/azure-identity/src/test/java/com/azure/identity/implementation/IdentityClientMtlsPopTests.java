// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.ProofOfPossessionOptions;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.util.ImdsProbeTestServer;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IMtlsCapableHttpClient;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentityCapabilities;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.MtlsBindingStrength;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.net.ssl.X509ExtendedKeyManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.identity.implementation.ManagedIdentityMtlsPopTests.keyId;
import static com.azure.identity.implementation.ManagedIdentityMtlsPopTests.mockBindingContext;
import static com.azure.identity.implementation.ManagedIdentityMtlsPopTests.mockCapabilities;
import static com.azure.identity.implementation.ManagedIdentityMtlsPopTests.mockCertificate;
import static com.azure.identity.implementation.ManagedIdentityMtlsPopTests.mockPopResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IdentityClientMtlsPopTests {
    private static final String SCOPE = "https://vault.azure.net/.default";
    private static final TokenRequestContext BEARER_REQUEST = new TokenRequestContext().addScopes(SCOPE);
    private static final TokenRequestContext POP_REQUEST
        = new TokenRequestContext().addScopes(SCOPE).setProofOfPossessionOptions(new ProofOfPossessionOptions());
    private static final String LIBRARY_PATH = "C:\\attestation\\AttestationClientLib.dll";
    private static final Duration TEST_TIMEOUT = Duration.ofSeconds(10);

    private MockedStatic<ManagedIdentityApplication> managedIdentityApplication;
    private AtomicInteger bearerTokenRequests;
    private ManagedIdentityApplication mtlsPopApplication;

    @BeforeEach
    public void setup() throws Exception {
        managedIdentityApplication = mockStatic(ManagedIdentityApplication.class, CALLS_REAL_METHODS);
        managedIdentityApplication.when(ManagedIdentityApplication::getManagedIdentitySource)
            .thenReturn(ManagedIdentitySourceType.DEFAULT_TO_IMDS);
        bearerTokenRequests = new AtomicInteger();

        mtlsPopApplication = mock(ManagedIdentityApplication.class);
        IAuthenticationResult popResult
            = mockPopResult("pop-token", mockBindingContext(mockCertificate(new byte[] { 1 }),
                mock(X509ExtendedKeyManager.class), keyId(new byte[] { 1 })));
        when(mtlsPopApplication.acquireTokenForManagedIdentity(any()))
            .thenReturn(CompletableFuture.completedFuture(popResult));
    }

    @AfterEach
    public void cleanup() {
        managedIdentityApplication.close();
    }

    @Test
    public void popRequestGetsCertificateBoundToken() {
        supportedBindingStrength(MtlsBindingStrength.KEY_GUARD);
        TestIdentityClient client = new TestIdentityClient(options(false));

        StepVerifier.create(client.authenticateWithManagedIdentityMsalClient(POP_REQUEST)).assertNext(token -> {
            assertEquals("pop-token", token.getToken());
            assertEquals("mtls_pop", token.getTokenType());
            assertNotNull(token.getClientCertificateBinding());
        }).expectComplete().verify(TEST_TIMEOUT);
        assertEquals(0, bearerTokenRequests.get());
    }

    @Test
    public void bearerRequestDoesNotUseMtls() {
        supportedBindingStrength(MtlsBindingStrength.KEY_GUARD);
        TestIdentityClient client = new TestIdentityClient(options(false));

        StepVerifier.create(client.authenticateWithManagedIdentityMsalClient(BEARER_REQUEST))
            .assertNext(IdentityClientMtlsPopTests::assertBearerToken)
            .expectComplete()
            .verify(TEST_TIMEOUT);
        assertEquals(0, client.mtlsPopApplicationsCreated.get());
    }

    @ParameterizedTest
    @EnumSource(value = MtlsBindingStrength.class, names = { "NONE", "SOFTWARE" })
    public void popRequestGetsBearerTokenWithoutKeyGuard(MtlsBindingStrength strength) throws Exception {
        supportedBindingStrength(strength);
        TestIdentityClient client = new TestIdentityClient(options(false));

        for (int i = 0; i < 2; i++) {
            StepVerifier.create(client.authenticateWithManagedIdentityMsalClient(POP_REQUEST))
                .assertNext(IdentityClientMtlsPopTests::assertBearerToken)
                .expectComplete()
                .verify(TEST_TIMEOUT);
        }

        verify(mtlsPopApplication, times(1)).getManagedIdentityCapabilities();
        verify(mtlsPopApplication, never()).acquireTokenForManagedIdentity(any());
    }

    @ParameterizedTest
    @EnumSource(MissingPrerequisite.class)
    public void popRequestGetsBearerTokenWithoutPrerequisite(MissingPrerequisite prerequisite) {
        supportedBindingStrength(MtlsBindingStrength.KEY_GUARD);
        IdentityClientOptions options = options(false);
        TestIdentityClient client = new TestIdentityClient(options);
        switch (prerequisite) {
            case OPTED_OUT:
                options.disableMtlsProofOfPossession();
                break;

            case NOT_A_VIRTUAL_MACHINE:
                managedIdentityApplication.when(ManagedIdentityApplication::getManagedIdentitySource)
                    .thenReturn(ManagedIdentitySourceType.APP_SERVICE);
                break;

            case MSAL_WITHOUT_MTLS:
                client.mtlsSupportedByMsal = false;
                break;

            case ATTESTATION_PACKAGE:
                client.attestationAvailable = false;
                break;

            case ATTESTATION_LIBRARY_PATH:
                // A null value hides the environment variable from the test configuration.
                options.setConfiguration(TestUtils.createTestConfiguration(
                    new TestConfigurationSource().put("MSAL_KEY_ATTESTATION_LIBRARY_PATH", null)));
                break;

            default:
                throw new IllegalArgumentException(prerequisite.name());
        }

        StepVerifier.create(client.authenticateWithManagedIdentityMsalClient(POP_REQUEST))
            .assertNext(IdentityClientMtlsPopTests::assertBearerToken)
            .expectComplete()
            .verify(TEST_TIMEOUT);
        assertEquals(0, client.mtlsPopApplicationsCreated.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void mtlsPopFailureIsSurfacedWithoutBearerFallback(boolean chained) throws Exception {
        supportedBindingStrength(MtlsBindingStrength.KEY_GUARD);
        MsalServiceException failure = new MsalServiceException(
            "AADSTS3921996: The tenant isn't eligible for token binding.", "invalid_request");
        CompletableFuture<IAuthenticationResult> failed = new CompletableFuture<>();
        failed.completeExceptionally(failure);
        when(mtlsPopApplication.acquireTokenForManagedIdentity(any())).thenReturn(failed);

        try (ImdsProbeTestServer imds = new ImdsProbeTestServer((request, response) -> response.status(200).send())) {
            TestIdentityClient client = new TestIdentityClient(chained ? chainedOptions(imds) : options(false));

            StepVerifier.create(client.authenticateWithManagedIdentityMsalClient(POP_REQUEST))
                .expectErrorSatisfies(error -> {
                    assertSame(ClientAuthenticationException.class, error.getClass());
                    assertSame(failure, error.getCause());
                    assertTrue(error.getMessage().contains("AADSTS3921996"));
                })
                .verify(TEST_TIMEOUT);
        }
        assertEquals(0, bearerTokenRequests.get());
    }

    @Test
    public void standaloneDiscoveryWaitsForTheHost() {
        // Answers after the 1-second bound that DefaultAzureCredential applies.
        CompletableFuture<ManagedIdentityCapabilities> discovery = new CompletableFuture<>();
        when(mtlsPopApplication.getManagedIdentityCapabilities()).thenReturn(discovery);
        ManagedIdentityCapabilities keyGuard = mockCapabilities(MtlsBindingStrength.KEY_GUARD);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            scheduler.schedule(() -> discovery.complete(keyGuard), 1500, TimeUnit.MILLISECONDS);
            TestIdentityClient client = new TestIdentityClient(options(false));

            StepVerifier.create(client.authenticateWithManagedIdentityMsalClient(POP_REQUEST))
                .assertNext(token -> assertEquals("mtls_pop", token.getTokenType()))
                .expectComplete()
                .verify(TEST_TIMEOUT);
        } finally {
            scheduler.shutdownNow();
        }
    }

    @Test
    public void chainedDiscoveryTimeoutIsUnavailableAndAnsweredLater() {
        CompletableFuture<ManagedIdentityCapabilities> discovery = new CompletableFuture<>();
        when(mtlsPopApplication.getManagedIdentityCapabilities()).thenReturn(discovery);

        try (ImdsProbeTestServer imds = new ImdsProbeTestServer((request, response) -> response.status(200).send())) {
            TestIdentityClient client = new TestIdentityClient(chainedOptions(imds));

            StepVerifier.create(client.authenticateWithManagedIdentityMsalClient(POP_REQUEST))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(CredentialUnavailableException.class, error);
                    assertTrue(error.getMessage().contains("mTLS proof-of-possession"));
                })
                .verify(TEST_TIMEOUT);
            assertFalse(discovery.isCancelled());

            discovery.complete(mockCapabilities(MtlsBindingStrength.KEY_GUARD));
            StepVerifier.create(client.authenticateWithManagedIdentityMsalClient(POP_REQUEST))
                .assertNext(token -> assertEquals("mtls_pop", token.getTokenType()))
                .expectComplete()
                .verify(TEST_TIMEOUT);
        }
        verify(mtlsPopApplication, times(1)).getManagedIdentityCapabilities();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void discoveryFailureKeepsItsCause(boolean chained) {
        RuntimeException failure = new IllegalStateException("Discovery failed.");
        CompletableFuture<ManagedIdentityCapabilities> discovery = new CompletableFuture<>();
        discovery.completeExceptionally(failure);
        when(mtlsPopApplication.getManagedIdentityCapabilities()).thenReturn(discovery);

        try (ImdsProbeTestServer imds = new ImdsProbeTestServer((request, response) -> response.status(200).send())) {
            TestIdentityClient client = new TestIdentityClient(chained ? chainedOptions(imds) : options(false));

            StepVerifier.create(client.authenticateWithManagedIdentityMsalClient(POP_REQUEST))
                .expectErrorSatisfies(error -> {
                    assertSame(chained ? CredentialUnavailableException.class : ClientAuthenticationException.class,
                        error.getClass());
                    assertSame(failure, error.getCause());
                })
                .verify(TEST_TIMEOUT);
        }
        assertEquals(0, bearerTokenRequests.get());
    }

    @Test
    public void mtlsPopApplicationUsesMsalHttpClientAndProxy() {
        InetSocketAddress proxyAddress = InetSocketAddress.createUnresolved("proxy.contoso.com", 8888);
        IdentityClient client = new IdentityClientBuilder().clientId(UUID.randomUUID().toString())
            .identityClientOptions(
                new IdentityClientOptions().setProxyOptions(new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress)))
            .build();

        ManagedIdentityApplication application = client.getManagedIdentityMtlsPopMsalApplication();

        // MSAL only performs mTLS through HTTP clients that support it.
        assertInstanceOf(IMtlsCapableHttpClient.class, application.httpClient());
        assertEquals(new Proxy(Proxy.Type.HTTP, proxyAddress), application.proxy());
        assertEquals(IdentityClientBase.MTLS_POP_CONNECT_TIMEOUT_MILLIS,
            application.connectTimeoutForDefaultHttpClient());
        assertEquals(IdentityClientBase.MTLS_POP_READ_TIMEOUT_MILLIS, application.readTimeoutForDefaultHttpClient());
    }

    private void supportedBindingStrength(MtlsBindingStrength strength) {
        ManagedIdentityCapabilities capabilities = mockCapabilities(strength);
        when(mtlsPopApplication.getManagedIdentityCapabilities())
            .thenReturn(CompletableFuture.completedFuture(capabilities));
    }

    private static void assertBearerToken(AccessToken token) {
        assertEquals("managed-identity-token", token.getToken());
        assertEquals("Bearer", token.getTokenType());
        assertNull(token.getClientCertificateBinding());
    }

    private IdentityClientOptions options(boolean chained) {
        return options(chained, null);
    }

    private IdentityClientOptions chainedOptions(ImdsProbeTestServer imds) {
        return options(true, imds.getEndpoint());
    }

    private IdentityClientOptions options(boolean chained, String imdsEndpoint) {
        String tokenJson = "{\"access_token\":\"managed-identity-token\",\"expires_on\":\""
            + OffsetDateTime.now().plusHours(1).toEpochSecond() + "\",\"token_type\":\"Bearer\"}";
        HttpClient transport = request -> {
            bearerTokenRequests.incrementAndGet();
            return Mono.just(new MockHttpResponse(request, 200, tokenJson.getBytes(StandardCharsets.UTF_8)));
        };
        TestConfigurationSource configuration
            = new TestConfigurationSource().put("MSAL_KEY_ATTESTATION_LIBRARY_PATH", LIBRARY_PATH);
        if (imdsEndpoint != null) {
            configuration.put("AZURE_POD_IDENTITY_AUTHORITY_HOST", imdsEndpoint);
        }

        return new IdentityClientOptions().setChained(chained)
            .setConfiguration(TestUtils.createTestConfiguration(configuration))
            .setHttpClient(transport)
            .setRetryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)));
    }

    private enum MissingPrerequisite {
        OPTED_OUT, NOT_A_VIRTUAL_MACHINE, MSAL_WITHOUT_MTLS, ATTESTATION_PACKAGE, ATTESTATION_LIBRARY_PATH
    }

    /**
     * Uses a mocked MSAL application for mTLS proof-of-possession, and the real MSAL application with a mocked
     * transport for bearer tokens.
     */
    private final class TestIdentityClient extends IdentityClient {
        private final AtomicInteger mtlsPopApplicationsCreated = new AtomicInteger();
        private boolean mtlsSupportedByMsal = true;
        private boolean attestationAvailable = true;

        TestIdentityClient(IdentityClientOptions options) {
            // MSAL shares its managed identity token cache across applications, so each client uses its own identity.
            super(null, UUID.randomUUID().toString(), null, null, null, null, null, null, null, null, null, false, null,
                options);
        }

        @Override
        ManagedIdentityApplication getManagedIdentityMtlsPopMsalApplication() {
            mtlsPopApplicationsCreated.incrementAndGet();
            return mtlsPopApplication;
        }

        @Override
        boolean isMtlsSupportedByMsal() {
            return mtlsSupportedByMsal;
        }

        @Override
        ManagedIdentityMtlsPop findManagedIdentityAttestation() {
            return attestationAvailable ? super.findManagedIdentityAttestation() : null;
        }
    }
}
