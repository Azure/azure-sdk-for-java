// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.CertificateUtil;
import com.azure.identity.implementation.util.IdentityConstants;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IClientCertificate;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.IClientSecret;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "javax.net.ssl.*", "org.xml.*"})
@PrepareForTest({CertificateUtil.class, ClientCredentialFactory.class, Runtime.class, URL.class, ConfidentialClientApplication.class, ConfidentialClientApplication.Builder.class, PublicClientApplication.class, PublicClientApplication.Builder.class, IdentityClient.class})
public class IdentityClientTests {

    private static final String TENANT_ID = "contoso.com";
    private static final String CLIENT_ID = UUID.randomUUID().toString();
    private final ClientLogger logger = new ClientLogger(IdentityClientTests.class);

    @Test
    public void testValidSecret() throws Exception {
        // setup
        String secret = "secret";
        String accessToken = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientSecret(secret, request, accessToken, expiresOn);

        // test
        IdentityClient client = new IdentityClientBuilder()
            .tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret(secret).build();
        AccessToken token = client.authenticateWithConfidentialClient(request).block();
        Assert.assertEquals(accessToken, token.getToken());
        Assert.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
    }

    @Test
    public void testInvalidSecret() throws Exception {
        // setup
        String secret = "secret";
        String accessToken = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientSecret(secret, request, accessToken, expiresOn);

        // test
        try {
            IdentityClient client = new IdentityClientBuilder()
                .tenantId(TENANT_ID).clientId(CLIENT_ID).clientSecret("bad secret").build();
            client.authenticateWithConfidentialClient(request).block();
            fail();
        } catch (MsalServiceException e) {
            Assert.assertEquals("Invalid clientSecret", e.getMessage());
        }
    }

    @Test
    public void testValidCertificate() throws Exception {
        // setup
        String pfxPath = getClass().getResource("/keyStore.pfx").getPath();
        String accessToken = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientCertificate(request, accessToken, expiresOn);

        // test
        IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID)
            .certificatePath(pfxPath).certificatePassword("StrongPass!123").build();
        AccessToken token = client.authenticateWithConfidentialClient(request).block();
        Assert.assertEquals(accessToken, token.getToken());
        Assert.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
    }

    @Test
    public void testPemCertificate() throws Exception {
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
        mockForClientPemCertificate(accessToken, request, expiresOn);
        // test
        IdentityClient client = new IdentityClientBuilder()
            .tenantId(TENANT_ID).clientId(CLIENT_ID).certificatePath(pemPath).build();
        AccessToken token = client.authenticateWithConfidentialClient(request).block();
        Assert.assertEquals(accessToken, token.getToken());
        Assert.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
    }

    @Test
    public void testInvalidCertificatePassword() throws Exception {
        // setup
        String pfxPath = getClass().getResource("/keyStore.pfx").getPath();
        String accessToken = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientCertificate(request, accessToken, expiresOn);

        // test
        try {
            IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID)
                .certificatePath(pfxPath).certificatePassword("BadPassword").build();
            client.authenticateWithConfidentialClient(request).block();
            fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("password was incorrect"));
        }
    }

    @Test
    public void testValidDeviceCodeFlow() throws Exception {
        // setup
        String accessToken = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForDeviceCodeFlow(request, accessToken, expiresOn);

        // test
        IdentityClientOptions options = new IdentityClientOptions();
        IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).identityClientOptions(options).build();
        AccessToken token = client.authenticateWithDeviceCode(request, deviceCodeChallenge -> { /* do nothing */ }).block();
        Assert.assertEquals(accessToken, token.getToken());
        Assert.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
    }

    @Test
    public void testValidMSICodeFlow() throws Exception {
        // setup
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = "http://localhost";
        String secret = "secret";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        configuration.put("MSI_ENDPOINT", endpoint);
        configuration.put("MSI_SECRET", secret);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss XXX");
        String tokenJson = "{ \"access_token\" : \"token1\", \"expires_on\" : \"" + expiresOn.format(dtf) + "\" }";

        // mock
        mockForMSICodeFlow(tokenJson);

        // test
        IdentityClient client = new IdentityClientBuilder().build();
        AccessToken token = client.authenticateToManagedIdentityEndpoint(null, null, endpoint, secret, request).block();
        Assert.assertEquals("token1", token.getToken());
        Assert.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
    }

    @Test
    public void testValidServiceFabricCodeFlow() throws Exception {
        // setup
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = "http://localhost";
        String secret = "secret";
        String thumbprint = "950a2c88d57b5e19ac5119315f9ec199ff3cb823";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        configuration.put("IDENTITY_ENDPOINT", endpoint);
        configuration.put("IDENTITY_HEADER", secret);
        configuration.put("IDENTITY_SERVER_THUMBPRINT", thumbprint);
        String tokenJson = "{ \"access_token\" : \"token1\", \"expires_on\" : \"" + expiresOn.toEpochSecond() + "\" }";

        // mock
        mockForServiceFabricCodeFlow(tokenJson);

        // test
        IdentityClient client = new IdentityClientBuilder().build();
        AccessToken token = client.authenticateToServiceFabricManagedIdentityEndpoint(endpoint, secret,
            thumbprint, request).block();
        Assert.assertEquals("token1", token.getToken());
        Assert.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
    }

    @Test
    public void testValidIdentityEndpointMSICodeFlow() throws Exception {
        // setup
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = "http://localhost";
        String secret = "secret";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        configuration.put("IDENTITY_ENDPOINT", endpoint);
        configuration.put("IDENTITY_HEADER", secret);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss XXX");
        String tokenJson = "{ \"access_token\" : \"token1\", \"expires_on\" : \"" + expiresOn.format(dtf) + "\" }";

        // mock
        mockForMSICodeFlow(tokenJson);

        // test
        IdentityClient client = new IdentityClientBuilder().build();
        AccessToken token = client.authenticateToManagedIdentityEndpoint(endpoint, secret, null, null, request).block();
        Assert.assertEquals("token1", token.getToken());
        Assert.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
    }

    @Test (expected = ClientAuthenticationException.class)
    public void testInValidIdentityEndpointSecretArcCodeFlow() throws Exception {
        // setup
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = "http://localhost";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        configuration.put("IDENTITY_ENDPOINT", endpoint);
        // mock
        mockForArcCodeFlow(401);

        // test
        IdentityClient client = new IdentityClientBuilder().build();
        client.authenticateToArcManagedIdentityEndpoint(endpoint, request).block();
    }

    @Test (expected = ClientAuthenticationException.class)
    public void testInValidIdentityEndpointResponseCodeArcCodeFlow() throws Exception {
        // setup
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = "http://localhost";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        configuration.put("IDENTITY_ENDPOINT", endpoint);
        // mock
        mockForArcCodeFlow(200);

        // test
        IdentityClient client = new IdentityClientBuilder().build();
        client.authenticateToArcManagedIdentityEndpoint(endpoint, request).block();
    }

    @Test
    public void testValidIMDSCodeFlow() throws Exception {
        // setup
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = "http://localhost";
        String secret = "secret";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        configuration.put("MSI_ENDPOINT", endpoint);
        configuration.put("MSI_SECRET", secret);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss XXX");
        String tokenJson = "{ \"access_token\" : \"token1\", \"expires_on\" : \"" + expiresOn.format(dtf) + "\" }";


        // mock
        mockForIMDSCodeFlow(IdentityConstants.DEFAULT_IMDS_ENDPOINT, tokenJson);

        // mockForDeviceCodeFlow(request, accessToken, expiresOn);

        // test
        IdentityClient client = new IdentityClientBuilder().build();
        AccessToken token = client.authenticateToIMDSEndpoint(request).block();
        Assert.assertEquals("token1", token.getToken());
        Assert.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
    }

    @Test
    public void testCustomIMDSCodeFlow() throws Exception {
        // setup
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = "http://awesome.pod.url";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        configuration.put(Configuration.PROPERTY_AZURE_POD_IDENTITY_TOKEN_URL, endpoint);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss XXX");
        String tokenJson = "{ \"access_token\" : \"token1\", \"expires_on\" : \"" + expiresOn.format(dtf) + "\" }";


        // mock
        mockForIMDSCodeFlow(endpoint, tokenJson);

        // mockForDeviceCodeFlow(request, accessToken, expiresOn);

        // test
        IdentityClient client = new IdentityClientBuilder().build();
        AccessToken token = client.authenticateToIMDSEndpoint(request).block();
        Assert.assertEquals("token1", token.getToken());
        Assert.assertEquals(expiresOn.getSecond(), token.getExpiresAt().getSecond());
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
        mockForAuthorizationCodeFlow(token1, request, expiresAt);

        // test
        IdentityClientOptions options = new IdentityClientOptions();
        IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).identityClientOptions(options).build();
        StepVerifier.create(client.authenticateWithAuthorizationCode(request, authCode1, redirectUri))
            .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
    }

    @Test
    public void testUserRefreshTokenflow() throws Exception {
        // setup
        String token1 = "token1";
        String token2 = "token1";
        TokenRequestContext request2 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForUserRefreshTokenFlow(token2, request2, expiresAt);

        // test
        IdentityClientOptions options = new IdentityClientOptions();
        IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).identityClientOptions(options).build();
        StepVerifier.create(client.authenticateWithPublicClientCache(request2, TestUtils.getMockMsalAccount(token1, expiresAt).block()))
            .expectNextMatches(accessToken -> token2.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
    }

    @Test
    public void testUsernamePasswordCodeFlow() throws Exception {
        // setup
        String username = "testuser";
        String password = "testpassword";
        String token = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForUsernamePasswordCodeFlow(token, request, expiresOn);

        // test
        IdentityClientOptions options = new IdentityClientOptions();
        IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).identityClientOptions(options).build();
        StepVerifier.create(client.authenticateWithUsernamePassword(request, username, password))
            .expectNextMatches(accessToken -> token.equals(accessToken.getToken())
                && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
    }

    @Test
    public void testBrowserAuthenicationCodeFlow() throws Exception {
        // setup
        String username = "testuser";
        String password = "testpassword";
        String token = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mocForBrowserAuthenticationCodeFlow(token, request, expiresOn);

        // test
        IdentityClientOptions options = new IdentityClientOptions();
        IdentityClient client = new IdentityClientBuilder().tenantId(TENANT_ID).clientId(CLIENT_ID).identityClientOptions(options).build();
        StepVerifier.create(client.authenticateWithBrowserInteraction(request, 4567, null, null))
            .expectNextMatches(accessToken -> token.equals(accessToken.getToken())
                && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
    }


    @Test
    public void testOpenUrl() throws Exception {
        // mock
        PowerMockito.mockStatic(Runtime.class);
        Runtime rt = PowerMockito.mock(Runtime.class);
        when(Runtime.getRuntime()).thenReturn(rt);
        Process a = PowerMockito.mock(Process.class);
        when(rt.exec(anyString())).thenReturn(a);

        // test
        IdentityClient client = new IdentityClientBuilder().clientId("dummy").build();
        client.openUrl("https://localhost.com");
    }

    /****** mocks ******/

    private void mockForClientSecret(String secret, TokenRequestContext request, String accessToken, OffsetDateTime expiresOn) throws Exception {
        ConfidentialClientApplication application = PowerMockito.mock(ConfidentialClientApplication.class);
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
        ConfidentialClientApplication.Builder builder = PowerMockito.mock(ConfidentialClientApplication.Builder.class);
        when(builder.build()).thenReturn(application);
        when(builder.authority(any())).thenReturn(builder);
        when(builder.httpClient(any())).thenReturn(builder);
        whenNew(ConfidentialClientApplication.Builder.class).withAnyArguments().thenAnswer(invocation -> {
            String cid = (String) invocation.getArguments()[0];
            IClientSecret clientSecret = (IClientSecret) invocation.getArguments()[1];
            if (!CLIENT_ID.equals(cid)) {
                throw new MsalServiceException("Invalid CLIENT_ID", "InvalidClientId");
            }
            if (!secret.equals(clientSecret.clientSecret())) {
                throw new MsalServiceException("Invalid clientSecret", "InvalidClientSecret");
            }
            return builder;
        });
    }

    private void mockForClientCertificate(TokenRequestContext request, String accessToken, OffsetDateTime expiresOn) throws Exception {
        ConfidentialClientApplication application = PowerMockito.mock(ConfidentialClientApplication.class);
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
        ConfidentialClientApplication.Builder builder = PowerMockito.mock(ConfidentialClientApplication.Builder.class);
        when(builder.build()).thenReturn(application);
        when(builder.authority(any())).thenReturn(builder);
        when(builder.httpClient(any())).thenReturn(builder);
        whenNew(ConfidentialClientApplication.Builder.class).withAnyArguments().thenAnswer(invocation -> {
            String cid = (String) invocation.getArguments()[0];
            IClientCredential keyCredential = (IClientCredential) invocation.getArguments()[1];
            if (!CLIENT_ID.equals(cid)) {
                throw new MsalServiceException("Invalid CLIENT_ID", "InvalidClientId");
            }
            if (keyCredential == null) {
                throw new MsalServiceException("Invalid clientCertificate", "InvalidClientCertificate");
            }
            return builder;
        });
    }

    private void mockForDeviceCodeFlow(TokenRequestContext request, String accessToken, OffsetDateTime expiresOn) throws Exception {
        PublicClientApplication application = PowerMockito.mock(PublicClientApplication.class);
        AtomicBoolean cached = new AtomicBoolean(false);
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
            cached.set(true);
            return TestUtils.getMockAuthenticationResult(accessToken, expiresOn);
        });
        PublicClientApplication.Builder builder = PowerMockito.mock(PublicClientApplication.Builder.class);
        when(builder.build()).thenReturn(application);
        when(builder.authority(any())).thenReturn(builder);
        when(builder.httpClient(any())).thenReturn(builder);
        whenNew(PublicClientApplication.Builder.class).withArguments(CLIENT_ID).thenReturn(builder);
    }

    private void mockForClientPemCertificate(String accessToken, TokenRequestContext request, OffsetDateTime expiresOn) throws Exception {
        ConfidentialClientApplication application = PowerMockito.mock(ConfidentialClientApplication.class);
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
        ConfidentialClientApplication.Builder builder = PowerMockito.mock(ConfidentialClientApplication.Builder.class);
        when(builder.build()).thenReturn(application);
        when(builder.authority(any())).thenReturn(builder);
        when(builder.httpClient(any())).thenReturn(builder);
        whenNew(ConfidentialClientApplication.Builder.class).withAnyArguments().thenAnswer(invocation -> {
            String cid = (String) invocation.getArguments()[0];
            if (!CLIENT_ID.equals(cid)) {
                throw new MsalServiceException("Invalid CLIENT_ID", "InvalidClientId");
            }
            return builder;
        });
        PowerMockito.mockStatic(CertificateUtil.class);
        PowerMockito.mockStatic(ClientCredentialFactory.class);
        PrivateKey privateKey = PowerMockito.mock(PrivateKey.class);
        IClientCertificate clientCertificate = PowerMockito.mock(IClientCertificate.class);
        when(CertificateUtil.privateKeyFromPem(any())).thenReturn(privateKey);
        when(ClientCredentialFactory.createFromCertificate(any(PrivateKey.class), any(X509Certificate.class)))
            .thenReturn(clientCertificate);
    }

    private void mockForMSICodeFlow(String tokenJson) throws Exception {
        URL u = PowerMockito.mock(URL.class);
        whenNew(URL.class).withAnyArguments().thenReturn(u);
        HttpURLConnection huc = PowerMockito.mock(HttpsURLConnection.class);
        when(u.openConnection()).thenReturn(huc);
        PowerMockito.doNothing().when(huc).setRequestMethod(anyString());
        PowerMockito.doNothing().when(huc).setRequestMethod(anyString());
        PowerMockito.doNothing().when(huc).setRequestMethod(anyString());
        InputStream inputStream = new ByteArrayInputStream(tokenJson.getBytes(Charset.defaultCharset()));
        when(huc.getInputStream()).thenReturn(inputStream);
    }

    private void mockForServiceFabricCodeFlow(String tokenJson) throws Exception {
        URL u = PowerMockito.mock(URL.class);
        whenNew(URL.class).withAnyArguments().thenReturn(u);
        HttpsURLConnection huc = PowerMockito.mock(HttpsURLConnection.class);
        when(u.openConnection()).thenReturn(huc);
        PowerMockito.doNothing().when(huc).setRequestMethod(anyString());
        PowerMockito.doNothing().when(huc).setRequestMethod(anyString());
        PowerMockito.doNothing().when(huc).setRequestMethod(anyString());
        PowerMockito.doNothing().when(huc).setSSLSocketFactory(any());

        InputStream inputStream = new ByteArrayInputStream(tokenJson.getBytes(Charset.defaultCharset()));
        when(huc.getInputStream()).thenReturn(inputStream);
    }

    private void mockForArcCodeFlow(int responseCode) throws Exception {
        URL u = PowerMockito.mock(URL.class);
        whenNew(URL.class).withAnyArguments().thenReturn(u);
        HttpURLConnection initConnection = PowerMockito.mock(HttpURLConnection.class);
        when(u.openConnection()).thenReturn(initConnection);
        PowerMockito.doNothing().when(initConnection).setRequestMethod(anyString());
        PowerMockito.doNothing().when(initConnection).setRequestProperty(anyString(), anyString());
        PowerMockito.doNothing().when(initConnection).connect();
        when(initConnection.getInputStream()).thenThrow(new IOException());
        when(initConnection.getResponseCode()).thenReturn(responseCode);
    }

    private void mockForIMDSCodeFlow(String endpoint, String tokenJson) throws Exception {
        URL u = PowerMockito.mock(URL.class);
        whenNew(URL.class).withArguments(ArgumentMatchers.startsWith(endpoint)).thenReturn(u);
        whenNew(URL.class).withAnyArguments().thenReturn(u);
        HttpURLConnection huc = PowerMockito.mock(HttpURLConnection.class);
        when(u.openConnection()).thenReturn(huc);
        PowerMockito.doNothing().when(huc).setRequestMethod(anyString());
        PowerMockito.doNothing().when(huc).setConnectTimeout(ArgumentMatchers.anyInt());
        InputStream inputStream = new ByteArrayInputStream(tokenJson.getBytes(Charset.defaultCharset()));
        when(huc.getInputStream()).thenReturn(inputStream);
    }

    private void mocForBrowserAuthenticationCodeFlow(String token, TokenRequestContext request, OffsetDateTime expiresOn) throws Exception {
        PublicClientApplication application = PowerMockito.mock(PublicClientApplication.class);
        when(application.acquireToken(any(InteractiveRequestParameters.class)))
            .thenAnswer(invocation -> {
                InteractiveRequestParameters argument = (InteractiveRequestParameters) invocation.getArguments()[0];
                if (argument.scopes().size() != 1 || request.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                    return TestUtils.getMockAuthenticationResult(token, expiresOn);
                } else {
                    throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                }
            });
        PublicClientApplication.Builder builder = PowerMockito.mock(PublicClientApplication.Builder.class);
        when(builder.build()).thenReturn(application);
        when(builder.authority(any())).thenReturn(builder);
        when(builder.httpClient(any())).thenReturn(builder);
        whenNew(PublicClientApplication.Builder.class).withArguments(CLIENT_ID).thenReturn(builder);
    }

    private void mockForAuthorizationCodeFlow(String token1, TokenRequestContext request, OffsetDateTime expiresAt) throws Exception {
        PublicClientApplication application = PowerMockito.mock(PublicClientApplication.class);
        AtomicBoolean cached = new AtomicBoolean(false);
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
            cached.set(true);
            return TestUtils.getMockAuthenticationResult(token1, expiresAt);
        });
        PublicClientApplication.Builder builder = PowerMockito.mock(PublicClientApplication.Builder.class);
        when(builder.build()).thenReturn(application);
        when(builder.authority(any())).thenReturn(builder);
        when(builder.httpClient(any())).thenReturn(builder);
        whenNew(PublicClientApplication.Builder.class).withArguments(CLIENT_ID).thenReturn(builder);
    }

    private void mockForUsernamePasswordCodeFlow(String token, TokenRequestContext request, OffsetDateTime expiresOn) throws Exception {
        PublicClientApplication application = PowerMockito.mock(PublicClientApplication.class);
        when(application.acquireToken(any(UserNamePasswordParameters.class)))
            .thenAnswer(invocation -> {
                UserNamePasswordParameters argument = (UserNamePasswordParameters) invocation.getArguments()[0];
                if (argument.scopes().size() != 1 || request.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                    return TestUtils.getMockAuthenticationResult(token, expiresOn);
                } else {
                    throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                }
            });
        PublicClientApplication.Builder builder = PowerMockito.mock(PublicClientApplication.Builder.class);
        when(builder.build()).thenReturn(application);
        when(builder.authority(any())).thenReturn(builder);
        when(builder.httpClient(any())).thenReturn(builder);
        whenNew(PublicClientApplication.Builder.class).withArguments(CLIENT_ID).thenReturn(builder);
    }

    private void mockForUserRefreshTokenFlow(String token2, TokenRequestContext request2, OffsetDateTime expiresAt) throws Exception {
        PublicClientApplication application = PowerMockito.mock(PublicClientApplication.class);
        when(application.acquireTokenSilently(any()))
            .thenAnswer(invocation -> {
                SilentParameters argument = (SilentParameters) invocation.getArguments()[0];
                if (argument.scopes().size() != 1 || request2.getScopes().get(0).equals(argument.scopes().iterator().next())) {
                    return TestUtils.getMockAuthenticationResult(token2, expiresAt);
                } else {
                    throw new InvalidUseOfMatchersException(String.format("Argument %s does not match", (Object) argument));
                }
            });
        PublicClientApplication.Builder builder = PowerMockito.mock(PublicClientApplication.Builder.class);
        when(builder.build()).thenReturn(application);
        when(builder.authority(any())).thenReturn(builder);
        when(builder.httpClient(any())).thenReturn(builder);
        whenNew(PublicClientApplication.Builder.class).withArguments(CLIENT_ID).thenReturn(builder);
    }
}
