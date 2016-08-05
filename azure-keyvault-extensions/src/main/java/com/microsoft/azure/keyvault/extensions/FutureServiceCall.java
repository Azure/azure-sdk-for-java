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

import com.google.common.util.concurrent.AbstractFuture;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

class FutureServiceCall<S> extends AbstractFuture<S> {

	private final Object _lock = new Object();
	
    private ServiceCall  _call;
    private Callback     _callback;
    
    class Callback extends ServiceCallback<S> {

		@Override
	    public void failure(Throwable t) {
			synchronized( _lock ) {
				// Set the exception
				FutureServiceCall.this.setException(t);
			}
	    }

		@Override
		public void success(ServiceResponse<S> result) {
			synchronized( _lock ) {
				// Set the result
				FutureServiceCall.this.set(result.getBody());
			}
		}
	}

    protected FutureServiceCall() {
    	_call     = null;
    	_callback = new Callback();
    }
    
    public ServiceCallback<S> getServiceCallback() {
    	return _callback;
    }
    
    public void setServiceCall(ServiceCall call) {
    	_call = call;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
    	
    	if ( !isCancelled()) {
	    	// TODO: Call<?>.cancel has no return value and does not support mayInterruptIfRunning
	    	_call.getCall().cancel();
    	}
    	
    	return super.cancel(mayInterruptIfRunning);
    }
    
    // Future<T> implementation

    @Override
    public boolean isCancelled() {
        return _call.getCall().isCanceled();
    }
}
