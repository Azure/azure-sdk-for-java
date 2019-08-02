/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CachingKeyResolverTest {

    @SuppressWarnings("unchecked")
    final ListenableFuture<IKey> ikeyAsync = mock(ListenableFuture.class);
    static final String KEY_ID = "https://test.vault.azure.net/keys/keyID/version";
    static final String KEY_ID_2 = "https://test.vault.azure.net/keys/keyID2/version";
    static final String KEY_ID_3 = "https://test.vault.azure.net/keys/keyID3/version";
    static final String NEWER_KEY_ID_3 = "https://test.vault.azure.net/keys/keyID3/version2";
    static final String UNVERSIONNED_KEY_ID_3 = "https://test.vault.azure.net/keys/keyID3";

    /*
     * Tests the capacity limit of CachingKeyResolver by adding more keys
     * than the cache limit and verifying that least recently used entity is evicted.
     */
    @Test
    public void capacityLimitOfCachingKeyResolver() {
        IKeyResolver mockedKeyResolver = mock(IKeyResolver.class);
        CachingKeyResolver resolver = new CachingKeyResolver(2, mockedKeyResolver);

        when(mockedKeyResolver.resolveKeyAsync(KEY_ID)).thenReturn(ikeyAsync);
        when(mockedKeyResolver.resolveKeyAsync(KEY_ID_2)).thenReturn(ikeyAsync);
        when(mockedKeyResolver.resolveKeyAsync(KEY_ID_3)).thenReturn(ikeyAsync);

        resolver.resolveKeyAsync(KEY_ID);
        resolver.resolveKeyAsync(KEY_ID_2);
        resolver.resolveKeyAsync(KEY_ID_3);

        resolver.resolveKeyAsync(KEY_ID_2);
        resolver.resolveKeyAsync(KEY_ID_3);
        resolver.resolveKeyAsync(KEY_ID);
        resolver.resolveKeyAsync(KEY_ID_3);

        verify(mockedKeyResolver, times(1)).resolveKeyAsync(KEY_ID_2);
        verify(mockedKeyResolver, times(1)).resolveKeyAsync(KEY_ID_3);
        verify(mockedKeyResolver, times(2)).resolveKeyAsync(KEY_ID);
    }

    /*
     * Tests the behavior of CachingKeyResolver when resolving key throws
     * and validate that the failed entity is not added to the cache.
     */
    @Test
    public void cachingKeyResolverThrows() {
        IKeyResolver mockedKeyResolver = mock(IKeyResolver.class);
        CachingKeyResolver resolver = new CachingKeyResolver(10, mockedKeyResolver);

        // First throw exception and for the second call return a value
        when(mockedKeyResolver.resolveKeyAsync(KEY_ID))
            .thenThrow(new RuntimeException("test"))
            .thenReturn(ikeyAsync);

        try {
            resolver.resolveKeyAsync(KEY_ID);
            fail("Should have thrown an exception.");
        } catch (UncheckedExecutionException e) {
            assertTrue("RuntimeException is expected.", e.getCause() instanceof RuntimeException);
        }

        resolver.resolveKeyAsync(KEY_ID);
        resolver.resolveKeyAsync(KEY_ID);

        verify(mockedKeyResolver, times(2)).resolveKeyAsync(KEY_ID);
    }

    /*
     * Tests that CachingKeyResolver does not cache un-versioned keys,
     * but does cache the result versioned key
     */
    @Test
    public void cachingUnversionnedKey() throws Exception {
        IKeyResolver mockedKeyResolver = mock(IKeyResolver.class);
        CachingKeyResolver resolver = new CachingKeyResolver(2, mockedKeyResolver);

        IKey key = mock(IKey.class);

        when(mockedKeyResolver.resolveKeyAsync(UNVERSIONNED_KEY_ID_3)).thenReturn(ikeyAsync);
        when(ikeyAsync.get()).thenReturn(key);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                invocationOnMock.getArgumentAt(0, Runnable.class).run();
                return null;
            }
        }).when(ikeyAsync).addListener(any(Runnable.class), any(Executor.class));
        when(key.getKid()).thenReturn(KEY_ID_3);

        /*
         * First resolve unversionned key
         */
        ListenableFuture<IKey> result = resolver.resolveKeyAsync(UNVERSIONNED_KEY_ID_3);
        assertEquals(result.get().getKid(), KEY_ID_3);
        verify(mockedKeyResolver, times(1)).resolveKeyAsync(UNVERSIONNED_KEY_ID_3);
        verify(mockedKeyResolver, times(0)).resolveKeyAsync(KEY_ID_3);

        /*
         * Second resolve unversionned key, but the result should be a newer key
         */
        when(key.getKid()).thenReturn(NEWER_KEY_ID_3);
        result = resolver.resolveKeyAsync(UNVERSIONNED_KEY_ID_3);
        assertEquals(result.get().getKid(), NEWER_KEY_ID_3);
        verify(mockedKeyResolver, times(2)).resolveKeyAsync(UNVERSIONNED_KEY_ID_3);
        verify(mockedKeyResolver, times(0)).resolveKeyAsync(KEY_ID_3);
        verify(mockedKeyResolver, times(0)).resolveKeyAsync(NEWER_KEY_ID_3);

        /*
         * Check that versionned keys were added to the cache, and do not get resolved again
         */
        resolver.resolveKeyAsync(KEY_ID_3);
        resolver.resolveKeyAsync(NEWER_KEY_ID_3);
        verify(mockedKeyResolver, times(0)).resolveKeyAsync(KEY_ID_3);
        verify(mockedKeyResolver, times(0)).resolveKeyAsync(NEWER_KEY_ID_3);
    }
}
