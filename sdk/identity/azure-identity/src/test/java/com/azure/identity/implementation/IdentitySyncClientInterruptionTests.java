// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.implementation.util.IdentityUtil;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientSecret;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.SilentParameters;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * When the thread calling a synchronous token acquisition is interrupted while waiting on the MSAL future (for
 * example a Reactor scheduler disposing its worker on cancellation), the interruption must be surfaced as a
 * cancellation: the interrupt status is restored and the {@link InterruptedException} is rethrown as the cause of a
 * {@link RuntimeException}, not reported as an authentication failure.
 */
public class IdentitySyncClientInterruptionTests {
    private static final String CLIENT_ID = "11111111-1111-1111-1111-111111111111";
    private static final String TENANT_ID = "22222222-2222-2222-2222-222222222222";
    private static final String CLIENT_SECRET = "fakeSecretPlaceholder";
    private static final TokenRequestContext REQUEST
        = new TokenRequestContext().addScopes("https://graph.microsoft.com/.default");

    @Test
    public void confidentialClientAcquireTokenSurfacesInterruptionAsCancellation() throws Exception {
        Outcome outcome
            = runInterrupted(client -> client.authenticateWithConfidentialClient(REQUEST), new CompletableFuture<>());

        assertInterruption(outcome);
    }

    @Test
    public void confidentialClientCacheLookupSurfacesInterruptionAsCancellation() throws Exception {
        // Before the fix this path dereferenced InterruptedException.getMessage(), which is null, and threw a
        // NullPointerException that the credentials then swallowed.
        Outcome outcome = runInterrupted(client -> client.authenticateWithConfidentialClientCache(REQUEST),
            new CompletableFuture<>());

        assertInterruption(outcome);
        assertFalse(outcome.thrown instanceof ClientAuthenticationException, String.valueOf(outcome.thrown));
        assertFalse(outcome.thrown instanceof NullPointerException, String.valueOf(outcome.thrown));
    }

    @Test
    public void onBehalfOfSurfacesInterruptionAsCancellation() throws Exception {
        Outcome outcome = runInterrupted(client -> client.authenticateWithOBO(REQUEST), new CompletableFuture<>(),
            new IdentityClientOptions().userAssertion("fake-user-assertion"));

        assertInterruption(outcome);
        // The interruption is not disguised as an On-Behalf-Of authentication failure.
        assertFalse(outcome.thrown instanceof ClientAuthenticationException, String.valueOf(outcome.thrown));
    }

    @Test
    public void workloadIdentitySurfacesInterruptionAsCancellation() throws Exception {
        Outcome outcome = runInterrupted(client -> client.authenticateWithWorkloadIdentityConfidentialClient(REQUEST),
            new CompletableFuture<>());

        assertInterruption(outcome);
        // The interruption is not disguised as "authentication is not available", which would make
        // DefaultAzureCredential move on to the next credential in the chain as if the identity were absent.
        assertFalse(outcome.thrown instanceof CredentialUnavailableException, String.valueOf(outcome.thrown));
    }

    @Test
    public void tokenRequestFailedByExecutorInterruptionIsCancellationWithoutInterruptingCaller() throws Exception {
        // MSAL's own worker was interrupted (for example while the executor shuts down): the caller was not.
        CompletableFuture<IAuthenticationResult> failed = new CompletableFuture<>();
        failed.completeExceptionally(new InterruptedException());

        Outcome outcome = run(client -> client.authenticateWithConfidentialClient(REQUEST), failed, false,
            new IdentityClientOptions());

        assertNotNull(outcome.thrown);
        assertInstanceOf(RuntimeException.class, outcome.thrown);
        assertTrue(IdentityUtil.isInterruption(outcome.thrown), String.valueOf(outcome.thrown));
        assertFalse(outcome.interruptedAfterCall, "the caller was not interrupted, so its status must stay clear");
    }

    @Test
    public void tokenRequestFailedByJvmShutdownIsCancellationWithoutInterruptingCaller() throws Exception {
        // azure-core's shared executor cannot register its shutdown hook once JVM shutdown has begun; MSAL
        // surfaces that as the token request's failure.
        CompletableFuture<IAuthenticationResult> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IllegalStateException("Shutdown in progress"));

        Outcome outcome = run(client -> client.authenticateWithConfidentialClient(REQUEST), failed, false,
            new IdentityClientOptions());

        assertNotNull(outcome.thrown);
        assertInstanceOf(RuntimeException.class, outcome.thrown);
        assertTrue(IdentityUtil.isShutdownSignal(outcome.thrown), String.valueOf(outcome.thrown));
        assertFalse(IdentityUtil.isInterruption(outcome.thrown), String.valueOf(outcome.thrown));
        assertFalse(outcome.interruptedAfterCall, "the caller was not interrupted, so its status must stay clear");
    }

    @Test
    public void otherTokenRequestFailuresAreStillAuthenticationErrors() throws Exception {
        CompletableFuture<IAuthenticationResult> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IllegalStateException("some other failure"));

        Outcome outcome = run(client -> client.authenticateWithConfidentialClient(REQUEST), failed, false,
            new IdentityClientOptions());

        assertNotNull(outcome.thrown);
        assertFalse(IdentityUtil.isShutdownSignal(outcome.thrown), String.valueOf(outcome.thrown));
        assertFalse(outcome.interruptedAfterCall);
    }

    @Test
    public void completedTokenRequestIsUnaffected() throws Exception {
        CompletableFuture<IAuthenticationResult> completed = new CompletableFuture<>();
        IAuthenticationResult result = Mockito.mock(IAuthenticationResult.class);
        when(result.accessToken()).thenReturn("token");
        when(result.expiresOnDate()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3_600_000L));
        completed.complete(result);

        Outcome outcome = run(client -> client.authenticateWithConfidentialClient(REQUEST), completed, false,
            new IdentityClientOptions());

        assertNull(outcome.thrown);
        assertFalse(outcome.interruptedAfterCall);
    }

    private static void assertInterruption(Outcome outcome) {
        assertNotNull(outcome.thrown, "the interrupted call should have thrown");
        assertInstanceOf(RuntimeException.class, outcome.thrown);
        assertTrue(IdentityUtil.isInterruption(outcome.thrown), String.valueOf(outcome.thrown));
        assertTrue(outcome.interruptedAfterCall, "the interrupt status must be restored for the caller");
    }

    private static Outcome runInterrupted(Function<IdentitySyncClient, Object> call,
        CompletableFuture<IAuthenticationResult> pendingResult) throws Exception {
        return run(call, pendingResult, true, new IdentityClientOptions());
    }

    private static Outcome runInterrupted(Function<IdentitySyncClient, Object> call,
        CompletableFuture<IAuthenticationResult> pendingResult, IdentityClientOptions options) throws Exception {
        return run(call, pendingResult, true, options);
    }

    /**
     * Runs {@code call} on a worker thread against an {@link IdentitySyncClient} whose MSAL application returns
     * {@code pendingResult} for every acquisition, optionally interrupting the worker once it is blocked.
     */
    private static Outcome run(Function<IdentitySyncClient, Object> call,
        CompletableFuture<IAuthenticationResult> pendingResult, boolean interrupt, IdentityClientOptions options)
        throws Exception {
        try (MockedStatic<ConfidentialClientApplication> staticMock = mockStatic(ConfidentialClientApplication.class);
            MockedConstruction<ConfidentialClientApplication.Builder> builderMock
                = mockConstruction(ConfidentialClientApplication.Builder.class, (builder, context) -> {
                    when(builder.authority(any())).thenReturn(builder);
                    when(builder.instanceDiscovery(anyBoolean())).thenReturn(builder);
                    when(builder.disableInternalRetries()).thenReturn(builder);
                    when(builder.httpClient(any())).thenReturn(builder);
                    when(builder.logPii(anyBoolean())).thenReturn(builder);
                    ConfidentialClientApplication application = Mockito.mock(ConfidentialClientApplication.class);
                    when(application.acquireToken(any(ClientCredentialParameters.class))).thenReturn(pendingResult);
                    when(application.acquireToken(any(OnBehalfOfParameters.class))).thenReturn(pendingResult);
                    when(application.acquireTokenSilently(any(SilentParameters.class))).thenReturn(pendingResult);
                    when(builder.build()).thenReturn(application);
                })) {
            staticMock.when(() -> ConfidentialClientApplication.builder(anyString(),
                argThat(cred -> cred instanceof IClientSecret))).thenCallRealMethod();

            IdentitySyncClient client = new IdentityClientBuilder().tenantId(TENANT_ID)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .identityClientOptions(options)
                .buildSyncClient();
            // The MSAL application is built lazily on first use; make sure the mocked builder is what backs it.
            assertTrue(builderMock.constructed().isEmpty(), "no application should be built before the first call");

            // Mockito static and construction mocks are scoped to the thread that created them, so the
            // acquisition runs on this thread and a helper thread delivers the interrupt, the way
            // BoundedElasticScheduler.Worker.dispose() cancels a worker mid-call.
            Thread target = Thread.currentThread();
            Thread interrupter = new Thread(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                    return;
                }
                target.interrupt();
            }, "identity-sync-interruption-test");
            AtomicReference<Throwable> thrown = new AtomicReference<>();
            AtomicBoolean interruptedAfterCall = new AtomicBoolean();
            if (interrupt) {
                interrupter.start();
            }
            try {
                call.apply(client);
            } catch (Throwable t) {
                thrown.set(t);
            } finally {
                // Future.get() clears the interrupt status when it throws; the client must restore it. Read and
                // clear it here so the test thread is left clean for JUnit.
                interruptedAfterCall.set(Thread.interrupted());
                interrupter.join(TimeUnit.SECONDS.toMillis(5));
            }
            return new Outcome(thrown.get(), interruptedAfterCall.get());
        }
    }

    private static final class Outcome {
        private final Throwable thrown;
        private final boolean interruptedAfterCall;

        private Outcome(Throwable thrown, boolean interruptedAfterCall) {
            this.thrown = thrown;
            this.interruptedAfterCall = interruptedAfterCall;
        }
    }
}
