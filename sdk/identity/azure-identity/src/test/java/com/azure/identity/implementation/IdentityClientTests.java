// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credentials.AccessToken;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.AsymmetricKeyCredential;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ClientSecret;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.PublicClientApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfidentialClientApplication.class, ConfidentialClientApplication.Builder.class, PublicClientApplication.class, PublicClientApplication.Builder.class, IdentityClient.class })
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class IdentityClientTests {

    private static final Random RANDOM = new Random();
    private final String tenantId = "contoso.com";
    private final String clientId = UUID.randomUUID().toString();

    @Test
    public void testValidSecret() throws Exception {
        // setup
        String secret = "secret";
        String accessToken = "token";
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientSecret(secret, scopes, accessToken, expiresOn);

        // test
        IdentityClient client = new IdentityClientBuilder().tenantId(tenantId).clientId(clientId).build();
        AccessToken token = client.authenticateWithClientSecret(secret, scopes).block();
        Assert.assertEquals(accessToken, token.token());
        Assert.assertEquals(expiresOn.getSecond(), token.expiresOn().getSecond());
    }

    @Test
    public void testInvalidSecret() throws Exception {
        // setup
        String secret = "secret";
        String accessToken = "token";
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientSecret(secret, scopes, accessToken, expiresOn);

        // test
        try {
            IdentityClient client = new IdentityClientBuilder().tenantId(tenantId).clientId(clientId).build();
            client.authenticateWithClientSecret("bad secret", scopes).block();
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
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientCertificate(scopes, accessToken, expiresOn);

        // test
        IdentityClient client = new IdentityClientBuilder().tenantId(tenantId).clientId(clientId).build();
        AccessToken token = client.authenticateWithPfxCertificate(pfxPath, "StrongPass!123", scopes).block();
        Assert.assertEquals(accessToken, token.token());
        Assert.assertEquals(expiresOn.getSecond(), token.expiresOn().getSecond());
    }

    @Test
    public void testInvalidCertificatePassword() throws Exception {
        // setup
        String pfxPath = getClass().getResource("/keyStore.pfx").getPath();
        String accessToken = "token";
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForClientCertificate(scopes, accessToken, expiresOn);

        // test
        try {
            IdentityClient client = new IdentityClientBuilder().tenantId(tenantId).clientId(clientId).build();
            client.authenticateWithPfxCertificate(pfxPath, "BadPassword", scopes).block();
            fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("password was incorrect"));
        }
    }

    @Test
    public void testValidDeviceCodeFlow() throws Exception {
        // setup
        String accessToken = "token";
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        mockForDeviceCodeFlow(scopes, accessToken, expiresOn);

        // test
        IdentityClient client = new IdentityClientBuilder().tenantId(tenantId).clientId(clientId).build();
        AccessToken token = client.authenticateWithDeviceCode(scopes, deviceCodeChallenge -> { /* do nothing */ }).block();
        Assert.assertEquals(accessToken, token.token());
        Assert.assertEquals(expiresOn.getSecond(), token.expiresOn().getSecond());
    }

    /****** mocks ******/

    private void mockForClientSecret(String secret, String[] scopes, String accessToken, OffsetDateTime expiresOn) throws Exception {
        ConfidentialClientApplication application = PowerMockito.mock(ConfidentialClientApplication.class);
        when(application.acquireToken(any(ClientCredentialParameters.class))).thenAnswer(invocation -> {
            ClientCredentialParameters argument = (ClientCredentialParameters) invocation.getArguments()[0];
            if (argument.scopes().size() == 1 && scopes[0].equals(argument.scopes().iterator().next())) {
                return TestUtils.getMockAuthenticationResult(accessToken, expiresOn);
            } else {
                return CompletableFuture.runAsync(() -> {
                    throw new MsalServiceException("Invalid scopes", "InvalidScopes");
                });
            }
        });
        ConfidentialClientApplication.Builder builder = PowerMockito.mock(ConfidentialClientApplication.Builder.class);
        when(builder.build()).thenReturn(application);
        when(builder.authority(any())).thenReturn(builder);
        whenNew(ConfidentialClientApplication.Builder.class).withAnyArguments().thenAnswer(invocation -> {
            String cid = (String) invocation.getArguments()[0];
            ClientSecret clientSecret = (ClientSecret) invocation.getArguments()[1];
            if (!clientId.equals(cid)) {
                throw new MsalServiceException("Invalid clientId", "InvalidClientId");
            }
            if (!secret.equals(clientSecret.clientSecret())) {
                throw new MsalServiceException("Invalid clientSecret", "InvalidClientSecret");
            }
            return builder;
        });
    }

    private void mockForClientCertificate(String[] scopes, String accessToken, OffsetDateTime expiresOn) throws Exception {
        ConfidentialClientApplication application = PowerMockito.mock(ConfidentialClientApplication.class);
        when(application.acquireToken(any(ClientCredentialParameters.class))).thenAnswer(invocation -> {
            ClientCredentialParameters argument = (ClientCredentialParameters) invocation.getArguments()[0];
            if (argument.scopes().size() == 1 && scopes[0].equals(argument.scopes().iterator().next())) {
                return TestUtils.getMockAuthenticationResult(accessToken, expiresOn);
            } else {
                return CompletableFuture.runAsync(() -> {
                    throw new MsalServiceException("Invalid scopes", "InvalidScopes");
                });
            }
        });
        ConfidentialClientApplication.Builder builder = PowerMockito.mock(ConfidentialClientApplication.Builder.class);
        when(builder.build()).thenReturn(application);
        when(builder.authority(any())).thenReturn(builder);
        whenNew(ConfidentialClientApplication.Builder.class).withAnyArguments().thenAnswer(invocation -> {
            String cid = (String) invocation.getArguments()[0];
            AsymmetricKeyCredential keyCredential = (AsymmetricKeyCredential) invocation.getArguments()[1];
            if (!clientId.equals(cid)) {
                throw new MsalServiceException("Invalid clientId", "InvalidClientId");
            }
            if (keyCredential == null || keyCredential.key() == null) {
                throw new MsalServiceException("Invalid clientCertificate", "InvalidClientCertificate");
            }
            return builder;
        });
    }

    private void mockForDeviceCodeFlow(String[] scopes, String accessToken, OffsetDateTime expiresOn) throws Exception {
        PublicClientApplication application = PowerMockito.mock(PublicClientApplication.class);
        AtomicBoolean cached = new AtomicBoolean(false);
        when(application.acquireToken(any(DeviceCodeFlowParameters.class))).thenAnswer(invocation -> {
            DeviceCodeFlowParameters argument = (DeviceCodeFlowParameters) invocation.getArguments()[0];
            if (argument.scopes().size() != 1 || !scopes[0].equals(argument.scopes().iterator().next())) {
                return CompletableFuture.runAsync(() -> {
                    throw new MsalServiceException("Invalid scopes", "InvalidScopes");
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
        whenNew(PublicClientApplication.Builder.class).withArguments(clientId).thenReturn(builder);
    }
}
