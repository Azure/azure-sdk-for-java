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

package com.microsoft.azure.keyvault.cryptography;

import java.util.concurrent.Future;

abstract class FutureBase<T> implements Future<T> {

    private boolean _cancelled = false;
    private boolean _done      = false;
    
    protected FutureBase() {
    	this(false, false);
    }
    
    protected FutureBase(boolean done) {
    	this(done, false);
    }
    
    protected FutureBase(boolean done, boolean cancelled) {
    	_done      = done;
    	_cancelled = cancelled;
    }
    
    protected void setDone() {
    	_cancelled = false;
    	_done      = true;
    }
    
    protected void setCancelled() {
    	_cancelled = true;
    	_done      = true;
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

        return _done;
    }
}
