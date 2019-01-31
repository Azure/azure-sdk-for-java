// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.extensions.test;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;
import com.microsoft.azure.keyvault.extensions.CachingKeyResolver;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.Executor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CachingKeyResolverTest {

    @SuppressWarnings("unchecked")
    final ListenableFuture<IKey> ikeyAsync = mock(ListenableFuture.class);
    final static String keyId = "https://test.vault.azure.net/keys/keyID/version";
    final static String keyId2 = "https://test.vault.azure.net/keys/keyID2/version";
    final static String keyId3 = "https://test.vault.azure.net/keys/keyID3/version";
    final static String newerKeyId3 = "https://test.vault.azure.net/keys/keyID3/version2";
    final static String unversionnedKeyId3 = "https://test.vault.azure.net/keys/keyID3";


    /*
     * Tests the capacity limit of CachingKeyResolver by adding more keys
     * than the cache limit and verifying that least recently used entity is evicted.
     */
    @Test
    public void KeyVault_CapacityLimitOfCachingKeyResolver()
    {
        IKeyResolver mockedKeyResolver = mock(IKeyResolver.class);
        CachingKeyResolver resolver = new CachingKeyResolver(2, mockedKeyResolver);

        when(mockedKeyResolver.resolveKeyAsync(keyId)).thenReturn(ikeyAsync);
        when(mockedKeyResolver.resolveKeyAsync(keyId2)).thenReturn(ikeyAsync);
        when(mockedKeyResolver.resolveKeyAsync(keyId3)).thenReturn(ikeyAsync);

        resolver.resolveKeyAsync(keyId);
        resolver.resolveKeyAsync(keyId2);
        resolver.resolveKeyAsync(keyId3);

        resolver.resolveKeyAsync(keyId2);
        resolver.resolveKeyAsync(keyId3);
        resolver.resolveKeyAsync(keyId);
        resolver.resolveKeyAsync(keyId3);

        verify(mockedKeyResolver, times(1)).resolveKeyAsync(keyId2);
        verify(mockedKeyResolver, times(1)).resolveKeyAsync(keyId3);
        verify(mockedKeyResolver, times(2)).resolveKeyAsync(keyId);
    }

    /*
     * Tests the behavior of CachingKeyResolver when resolving key throws
     * and validate that the failed entity is not added to the cache.
     */
    @Test
    public void KeyVault_CachingKeyResolverThrows()
    {
        IKeyResolver mockedKeyResolver = mock(IKeyResolver.class);
        CachingKeyResolver resolver = new CachingKeyResolver(10, mockedKeyResolver);

        // First throw exception and for the second call return a value
        when(mockedKeyResolver.resolveKeyAsync(keyId))
            .thenThrow(new RuntimeException("test"))
            .thenReturn(ikeyAsync);

        try {
            resolver.resolveKeyAsync(keyId);
            fail("Should have thrown an exception.");
        }
        catch (UncheckedExecutionException e) {
            assertTrue("RuntimeException is expected.", e.getCause() instanceof RuntimeException);
        }

        resolver.resolveKeyAsync(keyId);
        resolver.resolveKeyAsync(keyId);

        verify(mockedKeyResolver, times(2)).resolveKeyAsync(keyId);
    }

    /*
     * Tests that CachingKeyResolver does not cache unversionned keys,
     * but does cache the result versionned key
     */
    @Test
    public void KeyVault_CachingUnversionnedKey() throws Exception {
        IKeyResolver mockedKeyResolver = mock(IKeyResolver.class);
        CachingKeyResolver resolver = new CachingKeyResolver(2, mockedKeyResolver);

        IKey key = mock(IKey.class);

        when(mockedKeyResolver.resolveKeyAsync(unversionnedKeyId3)).thenReturn(ikeyAsync);
        when(ikeyAsync.get()).thenReturn(key);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                invocationOnMock.getArgumentAt(0, Runnable.class).run();
                return null;
            }
        }).when(ikeyAsync).addListener(any(Runnable.class), any(Executor.class));
        when(key.getKid()).thenReturn(keyId3);

        /*
         * First resolve unversionned key
         */
        ListenableFuture<IKey> result = resolver.resolveKeyAsync(unversionnedKeyId3);
        assertEquals(result.get().getKid(), keyId3);
        verify(mockedKeyResolver, times(1)).resolveKeyAsync(unversionnedKeyId3);
        verify(mockedKeyResolver, times(0)).resolveKeyAsync(keyId3);

        /*
         * Second resolve unversionned key, but the result should be a newer key
         */
        when(key.getKid()).thenReturn(newerKeyId3);
        result = resolver.resolveKeyAsync(unversionnedKeyId3);
        assertEquals(result.get().getKid(), newerKeyId3);
        verify(mockedKeyResolver, times(2)).resolveKeyAsync(unversionnedKeyId3);
        verify(mockedKeyResolver, times(0)).resolveKeyAsync(keyId3);
        verify(mockedKeyResolver, times(0)).resolveKeyAsync(newerKeyId3);

        /*
         * Check that versionned keys were added to the cache, and do not get resolved again
         */
        resolver.resolveKeyAsync(keyId3);
        resolver.resolveKeyAsync(newerKeyId3);
        verify(mockedKeyResolver, times(0)).resolveKeyAsync(keyId3);
        verify(mockedKeyResolver, times(0)).resolveKeyAsync(newerKeyId3);
    }
}
