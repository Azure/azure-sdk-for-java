// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.util.logging.ClientLogger;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IMtlsBindingContext;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentityCapabilities;
import com.microsoft.aad.msal4j.ManagedIdentityParameters;
import com.microsoft.aad.msal4j.MtlsBindingStrength;
import com.microsoft.aad.msal4j.MtlsPopOptions;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Acquires managed identity tokens bound to a TLS client certificate (mutual TLS, or mTLS, proof-of-possession)
 * through MSAL, with KeyGuard attestation.
 * <p>
 * This is the only class that uses MSAL's mTLS API, which older msal4j versions don't have. {@link IdentityClient}
 * loads it only after checking that msal4j has that API, so applications that resolve an older msal4j keep getting
 * bearer tokens.
 */
final class ManagedIdentityMtlsPop {
    private static final ClientLogger LOGGER = new ClientLogger(ManagedIdentityMtlsPop.class);

    static final String ATTESTATION_EXTENSIONS_CLASS
        = "com.microsoft.aad.msal4j.mtls.ManagedIdentityAttestationExtensions";

    private final Method withAttestationSupport;
    private final AtomicReference<Boolean> keyGuardSupported = new AtomicReference<>();

    ManagedIdentityMtlsPop(Method withAttestationSupport) {
        this.withAttestationSupport = withAttestationSupport;
    }

    /**
     * Creates an instance if the optional {@code com.microsoft.azure:msal4j-key-attestation} package, which provides
     * KeyGuard attestation, is on the class path. It's resolved by reflection, so azure-identity doesn't depend on it.
     *
     * @return The instance, or null if the attestation package isn't available.
     */
    static ManagedIdentityMtlsPop createIfAttestationAvailable() {
        try {
            Class<?> extensions
                = Class.forName(ATTESTATION_EXTENSIONS_CLASS, false, ManagedIdentityApplication.class.getClassLoader());
            return new ManagedIdentityMtlsPop(extensions.getMethod("withAttestationSupport",
                ManagedIdentityParameters.ManagedIdentityParametersBuilder.class));
        } catch (ClassNotFoundException | NoSuchMethodException | LinkageError e) {
            LOGGER.verbose("The managed identity attestation package isn't available: {}", e.toString());
            return null;
        }
    }

    /**
     * Checks whether the host can bind tokens to a KeyGuard-protected key, through MSAL's capability discovery.
     * <p>
     * The answer is kept for the lifetime of this instance. MSAL shares a discovery in progress between requests, so
     * a request that stops waiting doesn't cancel it, and its answer is still recorded for later requests.
     *
     * @param application The MSAL application for mTLS proof-of-possession.
     * @param timeout How long to wait for MSAL's answer, or null to wait until it answers.
     * @return A publisher that emits whether the host supports KeyGuard binding, or a TimeoutException.
     */
    Mono<Boolean> isKeyGuardSupported(ManagedIdentityApplication application, Duration timeout) {
        Boolean supported = keyGuardSupported.get();
        if (supported != null) {
            return Mono.just(supported);
        }

        return Mono.defer(() -> {
            CompletableFuture<ManagedIdentityCapabilities> discovery = application.getManagedIdentityCapabilities();
            discovery.thenAccept(this::recordCapabilities);

            Mono<Boolean> result = Mono.fromFuture(discovery, true).map(this::recordCapabilities);
            return timeout == null ? result : result.timeout(timeout);
        });
    }

    private boolean recordCapabilities(ManagedIdentityCapabilities capabilities) {
        boolean supported = capabilities.maxSupportedBindingStrength() != null
            && capabilities.maxSupportedBindingStrength().value() >= MtlsBindingStrength.KEY_GUARD.value();

        if (keyGuardSupported.compareAndSet(null, supported)) {
            if (supported) {
                LOGGER.info("Managed identity mTLS proof-of-possession is available. Tokens requested with "
                    + "proof-of-possession are bound to a KeyGuard-protected TLS client certificate.");
            } else {
                LOGGER.info(
                    "Managed identity mTLS proof-of-possession isn't available on this host, so bearer tokens "
                        + "are returned. The host's strongest binding is {}. Reason: {}",
                    capabilities.maxSupportedBindingStrength(), capabilities.errorReason());
            }
        }

        return keyGuardSupported.get();
    }

    /**
     * Acquires a token for the resource that's bound to a KeyGuard-protected TLS client certificate, with KeyGuard
     * attestation.
     *
     * @param application The MSAL application for mTLS proof-of-possession.
     * @param resource The resource to acquire the token for.
     * @return A publisher that emits the token, which carries the certificate it's bound to.
     */
    Mono<AccessToken> authenticate(ManagedIdentityApplication application, String resource) {
        return Mono.fromFuture(() -> {
            try {
                return application.acquireTokenForManagedIdentity(createParameters(resource));
            } catch (InvocationTargetException e) {
                return failedFuture(e.getCause() == null ? e : e.getCause());
            } catch (Exception e) {
                return failedFuture(e);
            }
        }).map(ManagedIdentityMtlsPop::toAccessToken);
    }

    private ManagedIdentityParameters createParameters(String resource) throws ReflectiveOperationException {
        ManagedIdentityParameters.ManagedIdentityParametersBuilder builder = ManagedIdentityParameters.builder(resource)
            .withMtlsProofOfPossession(
                MtlsPopOptions.builder().minimumBindingStrength(MtlsBindingStrength.KEY_GUARD).build());

        return ((ManagedIdentityParameters.ManagedIdentityParametersBuilder) withAttestationSupport.invoke(null,
            builder)).build();
    }

    static AccessToken toAccessToken(IAuthenticationResult result) {
        IMtlsBindingContext bindingContext = result.mtlsBindingContext();
        if (bindingContext == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "MSAL returned an mTLS proof-of-possession token without the certificate it's bound to."));
        }

        // The key manager of this binding generation, so that the certificate keeps matching the token after MSAL
        // rotates the binding.
        return new MsalToken(result, result.tokenType(),
            new ClientCertificateBinding(bindingContext.bindingCertificate(), bindingContext.keyManager()));
    }

    private static CompletableFuture<IAuthenticationResult> failedFuture(Throwable error) {
        CompletableFuture<IAuthenticationResult> future = new CompletableFuture<>();
        future.completeExceptionally(error);
        return future;
    }
}
