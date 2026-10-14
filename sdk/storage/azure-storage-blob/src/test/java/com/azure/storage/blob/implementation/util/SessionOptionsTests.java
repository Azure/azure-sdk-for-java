// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.implementation.accesshelpers.SessionOptionsAccessor;
import com.azure.storage.blob.options.SessionOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SessionOptionsTests {

    @Test
    public void defaultOptionsUseAutomaticModeAndPipelineConfiguration() {
        SessionOptions options = new SessionOptions();

        assertEquals(SessionOptions.SessionMode.AUTO, options.getSessionMode());
        assertNull(SessionOptionsAccessor.getEndpoint(options));
        assertNull(SessionOptionsAccessor.getCredential(options));
    }

    @Test
    public void constructorStoresSessionAcquisitionConfiguration() {
        MockTokenCredential credential = new MockTokenCredential();
        SessionOptions options = new SessionOptions("https://account.blob.core.windows.net", credential);

        assertEquals(SessionOptions.SessionMode.AUTO, options.getSessionMode());
        assertEquals("https://account.blob.core.windows.net", SessionOptionsAccessor.getEndpoint(options));
        assertSame(credential, SessionOptionsAccessor.getCredential(options));
    }

    @Test
    public void sharedOptionsReuseOneInternalSessionClient() {
        SessionOptions options = createEnabledOptions();
        BlobServiceClient first = buildClient(options);
        SessionProvider firstProvider
            = SessionOptionsAccessor.getOrCreateSessionClient(options, SessionProvider.class, () -> null);
        BlobServiceClient second = buildClient(options);
        SessionProvider secondProvider
            = SessionOptionsAccessor.getOrCreateSessionClient(options, SessionProvider.class, () -> null);

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(firstProvider);
        assertSame(firstProvider, secondProvider);

        SessionOptions otherOptions = createEnabledOptions();
        buildClient(otherOptions);
        SessionProvider otherProvider
            = SessionOptionsAccessor.getOrCreateSessionClient(otherOptions, SessionProvider.class, () -> null);
        assertNotNull(otherProvider);
        assertNotSame(firstProvider, otherProvider);
    }

    @Test
    public void constructorRequiresEndpointAndCredential() {
        MockTokenCredential credential = new MockTokenCredential();

        assertThrows(NullPointerException.class, () -> new SessionOptions(null, credential));
        assertThrows(NullPointerException.class,
            () -> new SessionOptions("https://account.blob.core.windows.net", null));
    }

    @Test
    public void publicProviderTypesAreRemoved() {
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.azure.storage.blob.models.SessionProvider"));
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.azure.storage.blob.models.ContainerSessionProvider"));
    }

    private static SessionOptions createEnabledOptions() {
        return new SessionOptions("https://account.blob.core.windows.net", new MockTokenCredential())
            .setSessionMode(SessionOptions.SessionMode.ENABLED);
    }

    private static BlobServiceClient buildClient(SessionOptions options) {
        return new BlobServiceClientBuilder().endpoint("https://account.blob.core.windows.net")
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .sessionOptions(options)
            .buildClient();
    }
}
