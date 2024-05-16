// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;

/**
 * The collection of key resolvers that would iterate on a key id to resolve to {@link IKey}.
 */
public class AggregateKeyResolver implements IKeyResolver {

    /**
     * Future key class that resolves a key id after the async result is available.
     */
    class FutureKey extends AbstractFuture<IKey> {

        private final String kid;

        private boolean isCancelled = false;
        private boolean isDone      = false;
        private IKey    result    = null;

        FutureKey(String kid) {
            this.kid = kid;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {

            // mark cancelled
            isCancelled = true;

            return isCancelled;
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public boolean isDone() {

            // always true
            return isDone;
        }

        @Override
        public IKey get() throws InterruptedException, ExecutionException {

            // throw if cancelled
            if (isCancelled) {
                throw new InterruptedException();
            }

            synchronized (resolvers) {
                for (IKeyResolver resolver : resolvers) {
                    Future<IKey> futureKey = resolver.resolveKeyAsync(kid);
    
                    result = futureKey.get();
    
                    if (result != null) {
                        break;
                    }
                }
            }

            // Mark done
            isDone = true;

            return result;
        }

        @Override
        public IKey get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

            // throw if cancelled
            if (isCancelled) {
                throw new InterruptedException();
            }

            synchronized (resolvers) {
                for (IKeyResolver resolver : resolvers) {
                    Future<IKey> futureKey = resolver.resolveKeyAsync(kid);
    
                    result = futureKey.get(timeout, unit);
    
                    if (result != null) {
                        break;
                    }
                }
            }

            // Mark done
            isDone = true;

            return result;
        }
    }

    private final List<IKeyResolver> resolvers;

    /**
     * Constructor.
     */
    public AggregateKeyResolver() {

        resolvers = Collections.synchronizedList(new ArrayList<IKeyResolver>());
    }

    /**
     * Adds a key resolver to the collection of key resolvers.
     * @param resolver the key resolver
     */
    public void add(IKeyResolver resolver) {

        synchronized (resolvers) {
            resolvers.add(resolver);
        }
    }

    @Override
    public ListenableFuture<IKey> resolveKeyAsync(String kid) {
        return new FutureKey(kid);
    }

}
