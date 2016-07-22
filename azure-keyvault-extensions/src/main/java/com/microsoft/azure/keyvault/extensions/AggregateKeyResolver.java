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

package com.microsoft.azure.keyvault.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;

public class AggregateKeyResolver implements IKeyResolver {

    class FutureKey implements Future<IKey> {

        private final String _kid;

        private boolean _cancelled = false;
        private boolean _done      = false;
        private IKey    _result    = null;

        FutureKey(String kid) {
            _kid = kid;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {

            // mark cancelled
            _cancelled = true;

            return _cancelled;
        }

        @Override
        public boolean isCancelled() {
            return _cancelled;
        }

        @Override
        public boolean isDone() {

            // always true
            return _done;
        }

        @Override
        public IKey get() throws InterruptedException, ExecutionException {

            // throw if cancelled
            if (_cancelled) {
                throw new InterruptedException();
            }

            synchronized( _resolvers ) {
	            for (IKeyResolver resolver : _resolvers) {
	                Future<IKey> futureKey = resolver.resolveKeyAsync(_kid);
	
	                _result = futureKey.get();
	
	                if (_result != null) {
	                    break;
	                }
	            }
            }

            // Mark done
            _done = true;

            return _result;
        }

        @Override
        public IKey get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

            // throw if cancelled
            if (_cancelled) {
                throw new InterruptedException();
            }

            synchronized( _resolvers ) {
	            for (IKeyResolver resolver : _resolvers) {
	                Future<IKey> futureKey = resolver.resolveKeyAsync(_kid);
	
	                _result = futureKey.get(timeout, unit);
	
	                if (_result != null) {
	                    break;
	                }
	            }
            }

            // Mark done
            _done = true;

            return _result;
        }
    }

    private final List<IKeyResolver> _resolvers;

    public AggregateKeyResolver() {

        _resolvers = Collections.synchronizedList(new ArrayList<IKeyResolver>());
    }

    public void Add(IKeyResolver resolver) {

    	synchronized( _resolvers ) {
	        _resolvers.add(resolver);
    	}
    }

    @Override
    public Future<IKey> resolveKeyAsync(String kid) {
        return new FutureKey(kid);
    }

}
