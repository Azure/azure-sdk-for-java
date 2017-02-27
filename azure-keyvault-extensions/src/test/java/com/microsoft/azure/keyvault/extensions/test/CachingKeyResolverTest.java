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

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;
import com.microsoft.azure.keyvault.extensions.CachingKeyResolver;
import static org.mockito.Mockito.*;

public class CachingKeyResolverTest {
    
    @SuppressWarnings("unchecked")
    final ListenableFuture<IKey> ikeyAsync = mock(ListenableFuture.class);
    final static String keyId = "keyID";
    final static String keyId2 = "keyID2";
    final static String keyId3 = "keyID3";
    

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
            assertFalse("Should have thrown an exception.", true);
        }
        catch (UncheckedExecutionException e) {
            assertTrue("RuntimeException is expected.", e.getCause() instanceof RuntimeException);
        }
        
        resolver.resolveKeyAsync(keyId);
        resolver.resolveKeyAsync(keyId);
        
        verify(mockedKeyResolver, times(2)).resolveKeyAsync(keyId);
    }
}
