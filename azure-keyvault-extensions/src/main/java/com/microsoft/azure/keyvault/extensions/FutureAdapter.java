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

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

abstract class FutureAdapter<S, T> extends ServiceCallback<S> implements Future<T> {

	private final Object _lock = new Object();
	
    private ServiceCall  _call;
    
    private S            _source;
    private Throwable    _throwable;

    protected FutureAdapter() {
    	_call      = null;
    	_source    = null;
    	_throwable = null;
    }
    
    public void setServiceCall(ServiceCall call) {
    	_call = call;
    }
    
    // ServiceCallback overrides
    
    @Override
    public void failure(Throwable t) {
		synchronized( _lock ) {
	    	_throwable = t;
	    	
	    	_lock.notifyAll();
		}
    }
    

	@Override
	public void success(ServiceResponse<S> result) {
		synchronized( _lock ) {
			_source = result.getBody();
	    	
	    	_lock.notifyAll();
		}
	}

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
    	
    	if ( !isCancelled()) {
	    	// TODO: Call<?>.cancel has no return value and does not support mayInterruptIfRunning
	    	_call.getCall().cancel();
    	}
    	
    	return true;
    }
    
    // Future<T> implementation

    @Override
    public boolean isCancelled() {
        return _call.getCall().isCanceled();
    }

    @Override
    public boolean isDone() {
    	// _call.getCall().isExecuted() is not the same as done as it will be true when the request is enqueued but not actually
    	// completed. So we check our results to determine whether we are actually done.
    	synchronized(_lock) {
    		return ( _source != null || _throwable != null );
    	}
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        try {
        	synchronized( _lock ) {
        		if ( _source == null && _throwable == null ) {
	        		_lock.wait();
        		}
        	}
        	
        	if ( _source != null ) {
        		return translate(_source);
        	} else {
        		throw _throwable;
        	}
        } catch (Throwable e) {
        	throw new ExecutionException(e);
		}
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
        	synchronized( _lock ) {
        		if ( _source == null && _throwable == null ) {
	        		_lock.wait();
        		}
        	}
        	
        	if ( _source != null ) {
        		return translate(_source);
        	} else {
        		throw _throwable;
        	}
        } catch (Throwable e) {
        	throw new ExecutionException(e);
		}
    }

    protected abstract T translate(S result) throws IOException;

}