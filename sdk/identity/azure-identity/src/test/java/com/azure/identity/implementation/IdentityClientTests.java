// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.util.CertificateUtil;
import com.azure.identity.implementation.util.IdentityConstants;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IClientCertificate;
import com.microsoft.aad.msal4j.IClientSecret;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import reactor.test.StepVerifier;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IdentityClientTests {

    private static final String TENANT_ID = "contoso.com";
    private static final String CLIENT_ID = UUID.randomUUID().toString();

    @Test
    public void testValidSecret() {
        // setup
        String secret = "secret";
        String accessToken = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientSecret(secret, request, accessToken, expiresOn, () -> {
            // test
            IdentityClient client = new IdentityClientBuilder()
                .tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(secret).build();
            StepVerifier.create(client.authenticateWithConfidentialClient(request))
                .assertNext(token -> {
                    Assertions.assertEquals(accessToken, token.getToken());
                    Assertions.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
                })
                .verifyComplete();
        });
    }

    @Test
    public void testInvalidSecret() {
        // setup
        String secret = "secret";
        String accessToken = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientSecret(secret, request, accessToken, expiresOn, () -> {
            // test
            try {
                IdentityClient client = new IdentityClientBuilder()
                    .tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret("bad secret").build();
                client.authenticateWithConfidentialClient(request).block();
                fail();
            } catch (MsalServiceException e) {
                Assertions.assertEquals("Invalid clientSecret", e.getMessage());
            }
        });


    }

    @Test
    public void testValidCertificate() {
        // setup
        URL pfxUrl = getClass().getResource("/keyStore.pfx");
        String pfxPath;
        if (pfxUrl.getPath().contains(":")) {
            pfxPath = pfxUrl.getPath().substring(1);
        } else {
            pfxPath = pfxUrl.getPath();
        }
        String accessToken = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientCertificate(request, accessToken, expiresOn, () -> {
            IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID)
                .certificatePath(pfxPath).certificatePassword("StrongPass!123").build();
            StepVerifier.create(client.authenticateWithConfidentialClient(request))
                .assertNext(token -> {
                    Assertions.assertEquals(accessToken, token.getToken());
                    Assertions.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
                })
                .verifyComplete();
        });
    }

    @Test
    public void testPemCertificate() {
        // setup
        String pemPath;
        URL pemUrl = getClass().getClassLoader().getResource("certificate.pem");
        if (pemUrl.getPath().contains(":")) {
            pemPath = pemUrl.getPath().substring(1);
        } else {
            pemPath = pemUrl.getPath();
        }
        String accessToken = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientPemCertificate(accessToken, request, expiresOn, () -> {
            // test
            IdentityClient client = new IdentityClientBuilder()
                .tenantId(TENANT_ID).clientId(CLIENT_ID).certificatePath(pemPath).build();
            StepVerifier.create(client.authenticateWithConfidentialClient(request))
                .assertNext(token -> {
                    Assertions.assertEquals(accessToken, token.getToken());
                    Assertions.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
                })
                .verifyComplete();
        });

    }

    @Test
    public void testInvalidCertificatePassword() {
        // setup
        URL pfxUrl = getClass().getResource("/keyStore.pfx");
        String pfxPath;
        if (pfxUrl.getPath().contains(":")) {
            pfxPath = pfxUrl.getPath().substring(1);
        } else {
            pfxPath = pfxUrl.getPath();
        }
        String accessToken = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientCertificate(request, accessToken, expiresOn, () -> {
            // test
            IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID)
                .certificatePath(pfxPath).certificatePassword("BadPassword").build();
            StepVerifier.create(client.authenticateWithConfidentialClient(request))
                .verifyErrorSatisfies(e -> assertTrue(e.getMessage().contains("password was incorrect")));
        });
    }

    @Test
    public void testValidDeviceCodeFlow() {
        // setup
        String accessToken = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForDeviceCodeFlow(request, accessToken, expiresOn, () -> {
            IdentityClientOptions options = new IdentityClientOptions();
            IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID)
                .identityClientOptions(options)
                .build();

            StepVerifier.create(client.authenticateWithDeviceCode(request, deviceCodeChallenge -> { /* do nothing */ }))
                .assertNext(token -> {
                    Assertions.assertEquals(accessToken, token.getToken());
                    Assertions.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
                })
                .verifyComplete();
        });
    }


    @Test
    public void testValidServiceFabricCodeFlow() throws Exception {
        // setup
        String endpoint = "http://localhost";
        String secret = "secret";
        String thumbprint = "950a2c88d57b5e19ac5119315f9ec199ff3cb823";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("IDENTITY_ENDPOINT", endpoint)
            .put("IDENTITY_HEADER", secret)
            .put("IDENTITY_SERVER_THUMBPRINT", thumbprint));
        String tokenJson = "{ \"access_token\" : \"token1\", \"expires_on\" : \"" + expiresOn.toEpochSecond() + "\" }";

        // mock
        IdentityClientOptions options = new IdentityClientOptions()
            .setManagedIdentityType(ManagedIdentityType.SERVICE_FABRIC)
            .setManagedIdentityParameters(new ManagedIdentityParameters()
                .setIdentityEndpoint(endpoint)
                .setIdentityHeader(secret)
                .setIdentityServerThumbprint(thumbprint))
            .setConfiguration(configuration);
        IdentityClient client = new IdentityClientBuilder().identityClientOptions(options).build();
        mockForServiceFabricCodeFlow(tokenJson, () -> {
            // test
            StepVerifier.create(client.getTokenFromTargetManagedIdentity(request))
                .assertNext(token -> {
                    Assertions.assertEquals("token1", token.getToken());
                    Assertions.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
                })
                .verifyComplete();
        });
    }

    @Test
    public void testValidIdentityEndpointMSICodeFlow() throws Exception {
        // setup
        String endpoint = "http://localhost";
        String secret = "secret";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("IDENTITY_ENDPOINT", endpoint)
            .put("IDENTITY_HEADER", secret));
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss XXX");
        String tokenJson = "{ \"access_token\" : \"token1\", \"expires_on\" : \"" + expiresOn.format(dtf) + "\" }";

        // mock
        IdentityClientOptions options = new IdentityClientOptions()
            .setManagedIdentityType(ManagedIdentityType.APP_SERVICE)
            .setManagedIdentityParameters(new ManagedIdentityParameters()
                .setIdentityEndpoint(endpoint)
                .setIdentityHeader(secret))
            .setConfiguration(configuration);
        IdentityClient client = new IdentityClientBuilder().identityClientOptions(options).build();
        mockForMSICodeFlow(tokenJson, () -> {
            // test
            StepVerifier.create(client.getTokenFromTargetManagedIdentity(request))
                .assertNext(token -> {
                    Assertions.assertEquals("token1", token.getToken());
                    Assertions.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
                })
                .verifyComplete();
        });
    }

    @Test
    public void testInValidIdentityEndpointSecretArcCodeFlow() throws Exception {
        // setup
        String endpoint = "http://localhost";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("IDENTITY_ENDPOINT", endpoint));
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        // mock
        IdentityClientOptions options = new IdentityClientOptions()
            .setManagedIdentityType(ManagedIdentityType.ARC)
            .setManagedIdentityParameters(new ManagedIdentityParameters()
                .setIdentityEndpoint(endpoint))
            .setConfiguration(configuration);
        IdentityClient client = new IdentityClientBuilder().identityClientOptions(options).build();

        Assertions.assertThrows(ClientAuthenticationException.class,
            () -> mockForArcCodeFlow(401, () -> {
                client.getTokenFromTargetManagedIdentity(request).block();
            }));
    }

    @Test
    public void testInValidIdentityEndpointResponseCodeArcCodeFlow() throws Exception {
        // setup
        String endpoint = "http://localhost";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("IDENTITY_ENDPOINT", endpoint));
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        IdentityClientOptions options = new IdentityClientOptions()
            .setManagedIdentityType(ManagedIdentityType.ARC)
            .setManagedIdentityParameters(new ManagedIdentityParameters()
                .setIdentityEndpoint(endpoint))
            .setConfiguration(configuration);
        IdentityClient client = new IdentityClientBuilder().identityClientOptions(options).build();
        // mock

        Assertions.assertThrows(ClientAuthenticationException.class,
            () -> mockForArcCodeFlow(200, () -> client.getTokenFromTargetManagedIdentity(request).block()));
    }

    @Test
    public void testValidIMDSCodeFlow() throws Exception {
        // setup
        String endpoint = "http://localhost";
        String secret = "secret";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("MSI_ENDPOINT", endpoint)
            .put("MSI_SECRET", secret));
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss XXX");
        String tokenJson = "{ \"access_token\" : \"token1\", \"expires_on\" : \"" + expiresOn.format(dtf) + "\" }";


        IdentityClientOptions options = new IdentityClientOptions()
            .setManagedIdentityType(ManagedIdentityType.VM)
            .setConfiguration(configuration);
        IdentityClient client = new IdentityClientBuilder().identityClientOptions(options).build();
        // mock
        mockForIMDSCodeFlow(IdentityConstants.DEFAULT_IMDS_ENDPOINT, tokenJson, () -> {
            // test
            StepVerifier.create(client.getTokenFromTargetManagedIdentity(request))
                .assertNext(token -> {
                    Assertions.assertEquals("token1", token.getToken());
                    Assertions.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
                })
                .verifyComplete();
        });
    }

    @Test
    public void testCustomIMDSCodeFlow() throws Exception {
        // setup
        String endpoint = "http://awesome.pod.url";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(Configuration.PROPERTY_AZURE_POD_IDENTITY_TOKEN_URL, endpoint));
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss XXX");
        String tokenJson = "{ \"access_token\" : \"token1\", \"expires_on\" : \"" + expiresOn.format(dtf) + "\" }";


        IdentityClientOptions options = new IdentityClientOptions()
            .setManagedIdentityType(ManagedIdentityType.VM)
            .setConfiguration(configuration);
        IdentityClient client = new IdentityClientBuilder().identityClientOptions(options).build();
        // mock
        mockForIMDSCodeFlow(endpoint, tokenJson, () -> {
            // test
            StepVerifier.create(client.getTokenFromTargetManagedIdentity(request))
                .assertNext(token -> {
                    Assertions.assertEquals("token1", token.getToken());
                    Assertions.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
                })
                .verifyComplete();
        });
    }

    @Test
    public void testAuthorizationCodeFlow() throws Exception {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        String authCode1 = "authCode1";
        URI redirectUri = new URI("http://foo.com/bar");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForAuthorizationCodeFlow(token1, request, expiresAt, () -> {
            // test
            IdentityClientOptions options = new IdentityClientOptions();
            IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).identityClientOptions(options).build();
            StepVerifier.create(client.authenticateWithAuthorizationCode(request, authCode1, redirectUri))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
        });
    }

    @Test
    public void testUserRefreshTokenflow() {
        // setup
        String token1 = "token1";
        String token2 = "token1";
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForUserRefreshTokenFlow(token2, request2, expiresAt, () -> {
            // test
            IdentityClientOptions options = new IdentityClientOptions();
            IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).identityClientOptions(options).build();
            StepVerifier.create(client.authenticateWithPublicClientCache(request2, TestUtils.getMockMsalAccount(token1, expiresAt).block()))
                .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                    && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
        });
    }

    @Test
    public void testUsernamePasswordCodeFlow() {
        // setup
        String username = "testuser";
        String password = "testpassword";
        String token = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForUsernamePasswordCodeFlow(token, request, expiresOn, () -> {
            // test
            IdentityClientOptions options = new IdentityClientOptions();
            IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).identityClientOptions(options).build();
            StepVerifier.create(client.authenticateWithUsernamePassword(request, username, password))
                .expectNextMatches(accessToken -> token.equals(accessToken.getToken())
                    && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
        });
    }

    @Test
    public void testBrowserAuthenicationCodeFlow() {
        // setup
        String username = "testuser";
        String password = "testpassword";
        String token = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        IdentityClientOptions options = new IdentityClientOptions();
        IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).identityClientOptions(options).build();
        // mock
        mockForBrowserAuthenticationCodeFlow(token, request, expiresOn, () -> {
            // test
            StepVerifier.create(client.authenticateWithBrowserInteraction(request, 4567, null, null))
                .expectNextMatches(accessToken -> token.equals(accessToken.getToken())
                    && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
        });
    }


    @Test
    public void testOpenUrl() throws Exception {
        try (MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            Runtime runtimeMock = mock(Runtime.class);
            runtimeMockedStatic.when(Runtime::getRuntime).thenReturn(runtimeMock);
            when(runtimeMock.exec(anyString())).thenReturn(null);
            // test
            IdentityClient client = new IdentityClientBuilder().clientId("dummy").build();
            client.openUrl("https://localhost.com");
            verify(runtimeMock).exec(ArgumentMatchers.contains("https://localhost.com"));
        }
    }

    @Test
    public void testAuthWithManagedIdentityFlow() {
        // setup
        String secret = "SYSTEM-ASSIGNED-CLIENT-SECRET";
        String clientId = "SYSTEM-ASSIGNED-CLIENT-ID";
        String accessToken = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForManagedIdentityFlow(secret, clientId, request, accessToken, expiresOn, () -> {
            // test
            IdentityClient client = new IdentityClientBuilder()
                .tenantId(TENANT_ID)
                .clientId(clientId)
                .clientSecret(secret)
                .identityClientOptions(new IdentityClientOptions()
                    .setManagedIdentityType(ManagedIdentityType.VM))
                .build();
            AccessToken token = client.authenticateWithManagedIdentityConfidentialClient(request).block();
            Assertions.assertEquals(accessToken, token.getToken());
            Assertions.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
        });
    }

    /****** mocks ******/
    private void mockForManagedIdentityFlow(String secret, String clientId, TokenRequestContext request, String accessToken, OffsetDateTime expiresOn, Runnable test) {

        try (MockedStatic<ConfidentialClientApplication> staticConfidentialClientApplicationMock = mockStatic(ConfidentialClientApplication.class); MockedConstruction<ConfidentialClientApplication.Builder> confidentialClientApplicationBuilderMock = mockConstruction(ConfidentialClientApplication.Builder.class, (builder, context) -> {

            when(builder.authority(any())).thenReturn(builder);
            when(builder.httpClient(any())).thenReturn(builder);
            when(builder.appTokenProvider(any())).thenReturn(builder);
            ConfidentialClientApplication application = Mockito.mock(ConfidentialClientApplication.class);
            when(application.acquireToken(any(ClientCredentialParameters.class))).thenAnswer(invocation -> {
                ClientCredentialParameters argument = (ClientCredentialParameters) invocation.getArguments()[0];
                if (argument.scopes().size() == 1 && request.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                    return TestUtils.getMockAuthenticationResult(accessToken, expiresOn);
                } else {
                    return CompletableFuture.runAsync(() -> {
                        throw new MsalServiceException("Invalid request", "InvalidScopes");
                    });
                }
            });
            when(builder.logPii(anyBoolean())).thenReturn(builder);
            when(builder.validateAuthority(anyBoolean())).thenReturn(builder);
            when(builder.instanceDiscovery(anyBoolean())).thenReturn(builder);
            when(builder.build()).thenReturn(application);
        })) {
            // Mocking the static builder to ensure we pass the right thing to it.
            staticConfidentialClientApplicationMock.when(() -> ConfidentialClientApplication.builder(eq(clientId), argThat(cred -> ((IClientSecret) cred).clientSecret().equals(secret)))).thenCallRealMethod();
            staticConfidentialClientApplicationMock.when(() -> ConfidentialClientApplication.builder(anyString(), argThat(cred -> !((IClientSecret) cred).clientSecret().equals(secret)))).thenThrow(new MsalServiceException("Invalid clientSecret", "InvalidClientSecret"));
            staticConfidentialClientApplicationMock.when(() -> ConfidentialClientApplication.builder(AdditionalMatchers.not(eq(clientId)), any(IClientSecret.class))).thenThrow(new MsalServiceException("Invalid CLIENT_ID", "InvalidClientId"));

            test.run();
            Assertions.assertNotNull(confidentialClientApplicationBuilderMock);
        }
    }

    private void mockForClientSecret(String secret, TokenRequestContext request, String accessToken, OffsetDateTime expiresOn, Runnable test) {

        try (MockedStatic<ConfidentialClientApplication> staticConfidentialClientApplicationMock = mockStatic(ConfidentialClientApplication.class); MockedConstruction<ConfidentialClientApplication.Builder> confidentialClientApplicationBuilderMock = mockConstruction(ConfidentialClientApplication.Builder.class, (builder, context) -> {

            when(builder.authority(any())).thenReturn(builder);
            when(builder.instanceDiscovery(anyBoolean())).thenReturn(builder);
            when(builder.httpClient(any())).thenReturn(builder);
            when(builder.logPii(anyBoolean())).thenReturn(builder);
            ConfidentialClientApplication application = Mockito.mock(ConfidentialClientApplication.class);
            when(application.acquireToken(any(ClientCredentialParameters.class))).thenAnswer(invocation -> {
                ClientCredentialParameters argument = (ClientCredentialParameters) invocation.getArguments()[0];
                if (argument.scopes().size() == 1 && request.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                    return TestUtils.getMockAuthenticationResult(accessToken, expiresOn);
                } else {
                    return CompletableFuture.runAsync(() -> {
                        throw new MsalServiceException("Invalid request", "InvalidScopes");
                    });
                }
            });
            when(builder.build()).thenReturn(application);
        })) {
            // Mocking the static builder to ensure we pass the right thing to it.
            staticConfidentialClientApplicationMock.when(() -> ConfidentialClientApplication.builder(eq(CLIENT_ID), argThat(cred -> ((IClientSecret) cred).clientSecret().equals(secret)))).thenCallRealMethod();
            staticConfidentialClientApplicationMock.when(() -> ConfidentialClientApplication.builder(anyString(), argThat(cred -> !((IClientSecret) cred).clientSecret().equals(secret)))).thenThrow(new MsalServiceException("Invalid clientSecret", "InvalidClientSecret"));
            staticConfidentialClientApplicationMock.when(() -> ConfidentialClientApplication.builder(AdditionalMatchers.not(eq(CLIENT_ID)), any(IClientSecret.class))).thenThrow(new MsalServiceException("Invalid CLIENT_ID", "InvalidClientId"));

            test.run();
            Assertions.assertNotNull(confidentialClientApplicationBuilderMock);
        }
    }

    private void mockForClientCertificate(TokenRequestContext request, String accessToken, OffsetDateTime expiresOn, Runnable test) {

        try (MockedStatic<ConfidentialClientApplication> staticConfidentialClientApplicationMock = mockStatic(ConfidentialClientApplication.class); MockedConstruction<ConfidentialClientApplication.Builder> confidentialClientApplicationBuilderMock = mockConstruction(ConfidentialClientApplication.Builder.class, (builder, context) -> {
            when(builder.authority(any())).thenReturn(builder);
            when(builder.instanceDiscovery(anyBoolean())).thenReturn(builder);
            when(builder.httpClient(any())).thenReturn(builder);
            when(builder.logPii(anyBoolean())).thenReturn(builder);
            ConfidentialClientApplication application = Mockito.mock(ConfidentialClientApplication.class);
            when(application.acquireToken(any(ClientCredentialParameters.class))).thenAnswer(invocation -> {
                ClientCredentialParameters argument = (ClientCredentialParameters) invocation.getArguments()[0];
                if (argument.scopes().size() == 1 && request.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                    return TestUtils.getMockAuthenticationResult(accessToken, expiresOn);
                } else {
                    return CompletableFuture.runAsync(() -> {
                        throw new MsalServiceException("Invalid request", "InvalidScopes");
                    });
                }
            });
            when(builder.build()).thenReturn(application);
        })) {
            staticConfidentialClientApplicationMock.when(() -> ConfidentialClientApplication.builder(eq(CLIENT_ID), argThat(cred -> ((IClientCertificate) cred) != null))).thenCallRealMethod();
            staticConfidentialClientApplicationMock.when(() -> ConfidentialClientApplication.builder(anyString(), argThat(cred -> ((IClientCertificate) cred) == null))).thenThrow(new MsalServiceException("Invalid clientCertificate", "InvalidClientCertificate"));
            staticConfidentialClientApplicationMock.when(() -> ConfidentialClientApplication.builder(AdditionalMatchers.not(eq(CLIENT_ID)), any(IClientCertificate.class))).thenThrow(new MsalServiceException("Invalid CLIENT_ID", "InvalidClientId"));
            test.run();
            Assertions.assertNotNull(confidentialClientApplicationBuilderMock);
        }
    }

    @Test
    public void validateRedaction() {
        String s = "        WARNING: Could not retrieve credential from local cache for service principal *** under tenant organizations. Trying credential under tenant 72f988bf-86f1-41af-91ab-2d7cd011db47, assuming that is an app credential.\n"
            + "        {\n"
            + "            \"accessToken\": \"ANACCESSTOKEN\",\n"
            + "            \"expiresOn\": \"2023-08-03 12:29:07.000000\",\n"
            + "            \"subscription\": \"subscription\",\n"
            + "            \"tenant\": \"tenant\",\n"
            + "            \"tokenType\": \"Bearer\"\n"
            + "        }";
        IdentityClient client = new IdentityClientBuilder().clientId("dummy").build();
        String redacted = client.redactInfo(s);
        assertTrue(redacted.contains("****"));
        assertFalse(redacted.contains("accessToken"));
    }

    private void mockForDeviceCodeFlow(TokenRequestContext request, String accessToken, OffsetDateTime expiresOn, Runnable test) {
        try (MockedConstruction<PublicClientApplication.Builder> publicClientApplicationMock = mockConstruction(PublicClientApplication.Builder.class, (builder, context) -> {
            when(builder.authority(any())).thenReturn(builder);
            when(builder.httpClient(any())).thenReturn(builder);
            when(builder.logPii(anyBoolean())).thenReturn(builder);
            PublicClientApplication application = Mockito.mock(PublicClientApplication.class);
            when(application.acquireToken(any(DeviceCodeFlowParameters.class))).thenAnswer(invocation -> {
                DeviceCodeFlowParameters argument = (DeviceCodeFlowParameters) invocation.getArguments()[0];
                if (argument.scopes().size() != 1 || !request.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                    return CompletableFuture.runAsync(() -> {
                        throw new MsalServiceException("Invalid request", "InvalidScopes");
                    });
                }
                if (argument.deviceCodeConsumer() == null) {
                    return CompletableFuture.runAsync(() -> {
                        throw new MsalServiceException("Invalid device code consumer", "InvalidDeviceCodeConsumer");
                    });
                }
                return TestUtils.getMockAuthenticationResult(accessToken, expiresOn);
            });
            when(builder.build()).thenReturn(application);
            when(builder.instanceDiscovery(anyBoolean())).thenReturn(builder);
        })) {
            test.run();
            Assertions.assertNotNull(publicClientApplicationMock);
        }
    }

    private void mockForClientPemCertificate(String accessToken, TokenRequestContext request, OffsetDateTime expiresOn, Runnable test) {

        try (MockedStatic<CertificateUtil> certificateUtilMock = mockStatic(CertificateUtil.class);
             MockedStatic<ClientCredentialFactory> clientCredentialFactoryMock = mockStatic(ClientCredentialFactory.class);
             MockedStatic<ConfidentialClientApplication> staticConfidentialClientApplicationMock = mockStatic(ConfidentialClientApplication.class);
             MockedConstruction<ConfidentialClientApplication.Builder> builderMock = mockConstruction(ConfidentialClientApplication.Builder.class, (builder, context) -> {
                 ConfidentialClientApplication application = mock(ConfidentialClientApplication.class);
                 when(application.acquireToken(any(ClientCredentialParameters.class))).thenAnswer(invocation -> {
                     ClientCredentialParameters argument = (ClientCredentialParameters) invocation.getArguments()[0];
                     if (argument.scopes().size() == 1 && request.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                         return TestUtils.getMockAuthenticationResult(accessToken, expiresOn);
                     } else {
                         return CompletableFuture.runAsync(() -> {
                             throw new MsalServiceException("Invalid request", "InvalidScopes");
                         });
                     }
                 });
                 when(builder.build()).thenReturn(application);
                 when(builder.authority(any())).thenReturn(builder);
                 when(builder.instanceDiscovery(anyBoolean())).thenReturn(builder);
                 when(builder.httpClient(any())).thenReturn(builder);
                 when(builder.logPii(anyBoolean())).thenReturn(builder);
             })
        ) {
            staticConfidentialClientApplicationMock.when(() -> ConfidentialClientApplication.builder(eq(CLIENT_ID), any())).thenCallRealMethod();
            staticConfidentialClientApplicationMock.when(() -> ConfidentialClientApplication.builder(AdditionalMatchers.not(eq(CLIENT_ID)), any())).thenThrow(new MsalServiceException("Invalid CLIENT_ID", "InvalidClientId"));
            PrivateKey privateKey = mock(PrivateKey.class);
            IClientCertificate clientCertificate = mock(IClientCertificate.class);
            certificateUtilMock.when(() -> CertificateUtil.privateKeyFromPem(any())).thenReturn(privateKey);
            clientCredentialFactoryMock.when(() -> ClientCredentialFactory.createFromCertificate(any(PrivateKey.class), any(X509Certificate.class))).thenReturn(clientCertificate);
            test.run();
            Assertions.assertNotNull(builderMock);
        }
    }

    private void mockForMSICodeFlow(String tokenJson, Runnable test) throws Exception {
        try (MockedStatic<IdentityClientBase> identityClientMockedStatic = mockStatic(IdentityClientBase.class)) {
            URL url = mock(URL.class);
            HttpURLConnection huc = mock(HttpURLConnection.class);
            doNothing().when(huc).setRequestMethod(anyString());
            doNothing().when(huc).setRequestMethod(anyString());
            doNothing().when(huc).setRequestMethod(anyString());
            when(url.openConnection()).thenReturn(huc);
            InputStream inputStream = new ByteArrayInputStream(tokenJson.getBytes(Charset.defaultCharset()));
            when(huc.getInputStream()).thenReturn(inputStream);
            identityClientMockedStatic.when(() -> IdentityClientBase.getUrl(anyString())).thenReturn(url);
            test.run();
        }
    }

    private void mockForServiceFabricCodeFlow(String tokenJson, Runnable test) throws Exception {

        try (MockedStatic<IdentityClientBase> identityClientMockedStatic = mockStatic(IdentityClientBase.class)) {
            URL url = mock(URL.class);
            HttpsURLConnection huc = mock(HttpsURLConnection.class);
            doNothing().when(huc).setRequestMethod(anyString());
            doNothing().when(huc).setRequestMethod(anyString());
            doNothing().when(huc).setRequestMethod(anyString());
            doNothing().when(huc).setSSLSocketFactory(any());
            when(url.openConnection()).thenReturn(huc);
            InputStream inputStream = new ByteArrayInputStream(tokenJson.getBytes(Charset.defaultCharset()));
            when(huc.getInputStream()).thenReturn(inputStream);
            identityClientMockedStatic.when(() -> IdentityClientBase.getUrl(anyString())).thenReturn(url);
            test.run();
        }
    }

    private void mockForArcCodeFlow(int responseCode, Runnable test) throws Exception {
        try (MockedStatic<IdentityClientBase> identityClientMockedStatic = mockStatic(IdentityClientBase.class)) {
            URL url = mock(URL.class);
            HttpURLConnection huc = mock(HttpURLConnection.class);
            doNothing().when(huc).setRequestMethod(anyString());
            doNothing().when(huc).setRequestProperty(anyString(), anyString());
            doNothing().when(huc).connect();
            when(url.openConnection()).thenReturn(huc);
            when(huc.getInputStream()).thenThrow(new IOException());
            when(huc.getResponseCode()).thenReturn(responseCode);
            identityClientMockedStatic.when(() -> IdentityClientBase.getUrl(anyString())).thenReturn(url);
            test.run();
        }
    }

    private void mockForIMDSCodeFlow(String endpoint, String tokenJson, Runnable test) throws Exception {
        try (MockedStatic<IdentityClientBase> identityClientMockedStatic = mockStatic(IdentityClientBase.class)) {
            URL url = mock(URL.class);
            HttpURLConnection huc = mock(HttpURLConnection.class);
            doNothing().when(huc).setRequestMethod(anyString());
            doNothing().when(huc).setConnectTimeout(anyInt());
            doNothing().when(huc).connect();
            when(url.openConnection()).thenReturn(huc);
            InputStream inputStream = new ByteArrayInputStream(tokenJson.getBytes(Charset.defaultCharset()));
            when(huc.getInputStream()).thenReturn(inputStream);
            identityClientMockedStatic.when(() -> IdentityClientBase.getUrl(anyString())).thenReturn(url);
            test.run();
        }
    }

    private void mockForBrowserAuthenticationCodeFlow(String token, TokenRequestContext request, OffsetDateTime expiresOn, Runnable test) {
        try (MockedConstruction<PublicClientApplication.Builder> publicClientApplicationMock = mockConstruction(PublicClientApplication.Builder.class, (builder, context) -> {
            PublicClientApplication application = Mockito.mock(PublicClientApplication.class);
            when(application.acquireToken(any(InteractiveRequestParameters.class))).thenAnswer(invocation -> {
                InteractiveRequestParameters argument = (InteractiveRequestParameters) invocation.getArguments()[0];
                if (argument.scopes().size() != 1 || request.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                    return TestUtils.getMockAuthenticationResult(token, expiresOn);
                } else {
                    throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                }
            });
            when(builder.build()).thenReturn(application);
            when(builder.authority(any())).thenReturn(builder);
            when(builder.instanceDiscovery(anyBoolean())).thenReturn(builder);
            when(builder.httpClient(any())).thenReturn(builder);
            when(builder.logPii(anyBoolean())).thenReturn(builder);
        })) {
            test.run();
            Assertions.assertNotNull(publicClientApplicationMock);
        }
    }

    private void mockForAuthorizationCodeFlow(String token1, TokenRequestContext request, OffsetDateTime expiresAt, Runnable test) {
        try (MockedConstruction<PublicClientApplication.Builder> publicClientApplicationMock = mockConstruction(PublicClientApplication.Builder.class, (builder, context) -> {
            PublicClientApplication application = Mockito.mock(PublicClientApplication.class);
            when(application.acquireToken(any(AuthorizationCodeParameters.class))).thenAnswer(invocation -> {
                AuthorizationCodeParameters argument = (AuthorizationCodeParameters) invocation.getArguments()[0];
                if (argument.scopes().size() != 1 || !request.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                    return CompletableFuture.runAsync(() -> {
                        throw new MsalServiceException("Invalid request", "InvalidScopes");
                    });
                }
                if (argument.redirectUri() == null) {
                    return CompletableFuture.runAsync(() -> {
                        throw new MsalServiceException("Invalid redirect uri", "InvalidAuthorizationCodeRedirectUri");
                    });
                }
                if (argument.authorizationCode() == null) {
                    return CompletableFuture.runAsync(() -> {
                        throw new MsalServiceException("Invalid authorization code", "InvalidAuthorizationCode");
                    });
                }
                return TestUtils.getMockAuthenticationResult(token1, expiresAt);
            });
            when(builder.build()).thenReturn(application);
            when(builder.authority(any())).thenReturn(builder);
            when(builder.instanceDiscovery(anyBoolean())).thenReturn(builder);
            when(builder.httpClient(any())).thenReturn(builder);
            when(builder.logPii(anyBoolean())).thenReturn(builder);
        })) {
            test.run();
            Assertions.assertNotNull(publicClientApplicationMock);
        }
    }

    private void mockForUsernamePasswordCodeFlow(String token, TokenRequestContext request, OffsetDateTime expiresOn, Runnable test) {
        try (MockedConstruction<PublicClientApplication.Builder> publicClientApplicationMock = mockConstruction(PublicClientApplication.Builder.class, (builder, context) -> {
            PublicClientApplication application = Mockito.mock(PublicClientApplication.class);
            when(application.acquireToken(any(UserNamePasswordParameters.class))).thenAnswer(invocation -> {
                UserNamePasswordParameters argument = (UserNamePasswordParameters) invocation.getArguments()[0];
                if (argument.scopes().size() != 1 || request.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                    return TestUtils.getMockAuthenticationResult(token, expiresOn);
                } else {
                    throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                }
            });
            when(builder.build()).thenReturn(application);
            when(builder.authority(any())).thenReturn(builder);
            when(builder.instanceDiscovery(anyBoolean())).thenReturn(builder);
            when(builder.httpClient(any())).thenReturn(builder);
            when(builder.logPii(anyBoolean())).thenReturn(builder);
        })) {
            test.run();
            Assertions.assertNotNull(publicClientApplicationMock);
        }
    }

    private void mockForUserRefreshTokenFlow(String token, TokenRequestContext request, OffsetDateTime expiresOn, Runnable test) {
        try (MockedConstruction<PublicClientApplication.Builder> publicClientApplicationMock = mockConstruction(PublicClientApplication.Builder.class, (builder, context) -> {
            PublicClientApplication application = Mockito.mock(PublicClientApplication.class);
            when(application.acquireTokenSilently(any())).thenAnswer(invocation -> {
                SilentParameters argument = (SilentParameters) invocation.getArguments()[0];
                if (argument.scopes().size() != 1 || request.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                    return TestUtils.getMockAuthenticationResult(token, expiresOn);
                } else {
                    throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                }
            });
            when(builder.build()).thenReturn(application);
            when(builder.authority(any())).thenReturn(builder);
            when(builder.instanceDiscovery(anyBoolean())).thenReturn(builder);
            when(builder.httpClient(any())).thenReturn(builder);
            when(builder.logPii(anyBoolean())).thenReturn(builder);
        })) {
            test.run();
            Assertions.assertNotNull(publicClientApplicationMock);
        }
    }
}
