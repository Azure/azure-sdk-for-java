// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.ClientCertificateBinding;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IMtlsBindingContext;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentityCapabilities;
import com.microsoft.aad.msal4j.ManagedIdentityParameters;
import com.microsoft.aad.msal4j.MtlsBindingStrength;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import reactor.test.StepVerifier;

import javax.net.ssl.X509ExtendedKeyManager;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ManagedIdentityMtlsPopTests {
    private static final String RESOURCE = "https://vault.azure.net";
    private static final Duration TEST_TIMEOUT = Duration.ofSeconds(10);

    @Test
    public void requestsKeyGuardBoundTokenWithAttestation() throws Exception {
        // Resolves the attestation package on the test class path, as applications do.
        ManagedIdentityMtlsPop mtlsPop = ManagedIdentityMtlsPop.createIfAttestationAvailable();
        assertNotNull(mtlsPop);

        byte[] encodedCertificate = new byte[] { 1, 2, 3 };
        X509Certificate certificate = mockCertificate(encodedCertificate);
        X509ExtendedKeyManager keyManager = mock(X509ExtendedKeyManager.class);
        IMtlsBindingContext bindingContext = mockBindingContext(certificate, keyManager, keyId(encodedCertificate));
        ManagedIdentityApplication application = mock(ManagedIdentityApplication.class);
        ArgumentCaptor<ManagedIdentityParameters> parameters = ArgumentCaptor.forClass(ManagedIdentityParameters.class);
        IAuthenticationResult result = mockPopResult("pop-token", bindingContext);
        when(application.acquireTokenForManagedIdentity(parameters.capture()))
            .thenReturn(CompletableFuture.completedFuture(result));

        StepVerifier.create(mtlsPop.authenticate(application, RESOURCE)).assertNext(token -> {
            assertEquals("pop-token", token.getToken());
            assertEquals("mtls_pop", token.getTokenType());

            ClientCertificateBinding binding = token.getClientCertificateBinding();
            assertSame(certificate, binding.getCertificate());
            // The key manager of the binding generation, not the one that follows MSAL's rotations.
            assertSame(keyManager, binding.getKeyManager());
            assertEquals(bindingContext.keyId(), binding.getKeyId());
        }).expectComplete().verify(TEST_TIMEOUT);

        ManagedIdentityParameters sent = parameters.getValue();
        assertEquals(RESOURCE, sent.resource());
        assertTrue(sent.mtlsProofOfPossession());
        assertFalse(sent.requestOverMtls());
        assertEquals(MtlsBindingStrength.KEY_GUARD, sent.minimumBindingStrength());
        assertTrue(isAttestationEnabled(sent));
    }

    @Test
    public void eachBindingGenerationKeepsItsOwnCertificate() throws Exception {
        ManagedIdentityMtlsPop mtlsPop = ManagedIdentityMtlsPop.createIfAttestationAvailable();
        X509ExtendedKeyManager firstKeyManager = mock(X509ExtendedKeyManager.class);
        X509ExtendedKeyManager secondKeyManager = mock(X509ExtendedKeyManager.class);
        IAuthenticationResult first = mockPopResult("first-token",
            mockBindingContext(mockCertificate(new byte[] { 1 }), firstKeyManager, keyId(new byte[] { 1 })));
        IAuthenticationResult second = mockPopResult("second-token",
            mockBindingContext(mockCertificate(new byte[] { 2 }), secondKeyManager, keyId(new byte[] { 2 })));
        ManagedIdentityApplication application = mock(ManagedIdentityApplication.class);
        when(application.acquireTokenForManagedIdentity(any())).thenReturn(CompletableFuture.completedFuture(first))
            .thenReturn(CompletableFuture.completedFuture(second));

        ClientCertificateBinding firstBinding
            = mtlsPop.authenticate(application, RESOURCE).block(TEST_TIMEOUT).getClientCertificateBinding();
        ClientCertificateBinding secondBinding
            = mtlsPop.authenticate(application, RESOURCE).block(TEST_TIMEOUT).getClientCertificateBinding();

        assertNotEquals(firstBinding.getKeyId(), secondBinding.getKeyId());
        assertSame(firstKeyManager, firstBinding.getKeyManager());
        assertSame(secondKeyManager, secondBinding.getKeyManager());
    }

    @Test
    public void tokenWithoutBindingIsRejected() throws Exception {
        ManagedIdentityMtlsPop mtlsPop = ManagedIdentityMtlsPop.createIfAttestationAvailable();
        ManagedIdentityApplication application = mock(ManagedIdentityApplication.class);
        IAuthenticationResult result = mockPopResult("pop-token", null);
        when(application.acquireTokenForManagedIdentity(any())).thenReturn(CompletableFuture.completedFuture(result));

        StepVerifier.create(mtlsPop.authenticate(application, RESOURCE))
            .expectError(IllegalStateException.class)
            .verify(TEST_TIMEOUT);
    }

    @ParameterizedTest
    @EnumSource(MtlsBindingStrength.class)
    public void discoveryAnswerIsKeptForLaterRequests(MtlsBindingStrength strength) {
        ManagedIdentityMtlsPop mtlsPop = ManagedIdentityMtlsPop.createIfAttestationAvailable();
        ManagedIdentityApplication application = mock(ManagedIdentityApplication.class);
        ManagedIdentityCapabilities capabilities = mockCapabilities(strength);
        when(application.getManagedIdentityCapabilities()).thenReturn(CompletableFuture.completedFuture(capabilities));
        boolean expected = strength == MtlsBindingStrength.KEY_GUARD;

        StepVerifier.create(mtlsPop.isKeyGuardSupported(application, null))
            .expectNext(expected)
            .expectComplete()
            .verify(TEST_TIMEOUT);
        StepVerifier.create(mtlsPop.isKeyGuardSupported(application, null))
            .expectNext(expected)
            .expectComplete()
            .verify(TEST_TIMEOUT);

        // MSAL discovers again after every unsupported answer, so the answer must be kept here.
        verify(application, times(1)).getManagedIdentityCapabilities();
    }

    @Test
    public void discoveryThatOutlastsTheTimeoutIsRecordedForLaterRequests() {
        ManagedIdentityMtlsPop mtlsPop = ManagedIdentityMtlsPop.createIfAttestationAvailable();
        ManagedIdentityApplication application = mock(ManagedIdentityApplication.class);
        CompletableFuture<ManagedIdentityCapabilities> discovery = new CompletableFuture<>();
        when(application.getManagedIdentityCapabilities()).thenReturn(discovery);

        StepVerifier.create(mtlsPop.isKeyGuardSupported(application, Duration.ofMillis(50)))
            .expectError(TimeoutException.class)
            .verify(TEST_TIMEOUT);
        // MSAL shares the discovery between requests, so it must not be cancelled.
        assertFalse(discovery.isCancelled());

        discovery.complete(mockCapabilities(MtlsBindingStrength.KEY_GUARD));
        StepVerifier.create(mtlsPop.isKeyGuardSupported(application, Duration.ofMillis(50)))
            .expectNext(true)
            .expectComplete()
            .verify(TEST_TIMEOUT);
        verify(application, times(1)).getManagedIdentityCapabilities();
    }

    static X509Certificate mockCertificate(byte[] encoded) throws Exception {
        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getEncoded()).thenReturn(encoded);
        return certificate;
    }

    static IMtlsBindingContext mockBindingContext(X509Certificate certificate, X509ExtendedKeyManager keyManager,
        String keyId) {
        IMtlsBindingContext bindingContext = mock(IMtlsBindingContext.class);
        when(bindingContext.bindingCertificate()).thenReturn(certificate);
        when(bindingContext.keyManager()).thenReturn(keyManager);
        when(bindingContext.rotatingKeyManager()).thenReturn(mock(X509ExtendedKeyManager.class));
        when(bindingContext.keyId()).thenReturn(keyId);
        when(bindingContext.bindingStrength()).thenReturn(MtlsBindingStrength.KEY_GUARD);
        return bindingContext;
    }

    static IAuthenticationResult mockPopResult(String accessToken, IMtlsBindingContext bindingContext) {
        IAuthenticationResult result = mock(IAuthenticationResult.class);
        when(result.accessToken()).thenReturn(accessToken);
        when(result.expiresOnDate()).thenReturn(Date.from(OffsetDateTime.now().plusHours(1).toInstant()));
        when(result.tokenType()).thenReturn("mtls_pop");
        when(result.mtlsBindingContext()).thenReturn(bindingContext);
        return result;
    }

    static ManagedIdentityCapabilities mockCapabilities(MtlsBindingStrength strength) {
        ManagedIdentityCapabilities capabilities = mock(ManagedIdentityCapabilities.class);
        when(capabilities.maxSupportedBindingStrength()).thenReturn(strength);
        when(capabilities.isMtlsPopSupportedByHost()).thenReturn(strength != MtlsBindingStrength.NONE);
        when(capabilities.errorReason())
            .thenReturn(strength == MtlsBindingStrength.NONE ? "IMDS v2 isn't available." : null);
        return capabilities;
    }

    // The x5t#S256 thumbprint that MSAL reports as the binding's key ID.
    static String keyId(byte[] encodedCertificate) throws Exception {
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(MessageDigest.getInstance("SHA-256").digest(encodedCertificate));
    }

    // MSAL only exposes whether attestation is enabled to its own package.
    private static boolean isAttestationEnabled(ManagedIdentityParameters parameters) throws Exception {
        Method attestationSupport = ManagedIdentityParameters.class.getDeclaredMethod("attestationSupport");
        attestationSupport.setAccessible(true);
        return (boolean) attestationSupport.invoke(parameters);
    }
}
