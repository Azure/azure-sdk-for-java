// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.blob.models.SessionRequestContext;
import com.azure.storage.common.test.shared.http.ScriptedHttpClient;
import com.azure.storage.common.test.shared.session.SessionTestHelper;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Small, focused tests for the public {@link SessionProvider} contract implemented by
 * {@link TokenCredentialSessionProvider}.
 * <p>
 * These verify that a context missing a container name is rejected rather than silently falling back to some default.
 * The successful sync and async routing paths are covered by {@code TokenCredentialSessionProviderTests} against the
 * live service, while {@code TokenCredentialSessionProviderCacheTest} fakes the transport to test cache timing.
 */
public class SessionProviderTests {

    @Test
    public void missingContextContainerThrowsSync() {
        TokenCredentialSessionProvider sessionProvider = createSessionProvider();

        // There is no constructor-supplied fallback container: a context with no container name must be
        // rejected rather than silently degrading to some default.
        SessionRequestContext context = new SessionRequestContext();

        assertThrows(IllegalArgumentException.class, () -> sessionProvider.getSession(context));
    }

    @Test
    public void missingContextContainerThrowsAsync() {
        TokenCredentialSessionProvider sessionProvider = createSessionProvider();

        SessionRequestContext context = new SessionRequestContext();

        StepVerifier.create(sessionProvider.getSessionAsync(context)).verifyError(IllegalArgumentException.class);
    }

    private static TokenCredentialSessionProvider createSessionProvider() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new ScriptedHttpClient()).build();
        return new TokenCredentialSessionProvider(pipeline,
            "https://" + SessionTestHelper.TEST_ACCOUNT_NAME + ".blob.core.windows.net", BlobServiceVersion.getLatest(),
            SessionTestHelper.TEST_ACCOUNT_NAME);
    }
}
