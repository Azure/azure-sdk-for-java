/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.core.pipeline.jersey;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * Base class for filters that enforces idempotency - the filter will only be
 * applied once for a particular request, even if the request passes through
 * this filter more than once.
 * 
 */
public abstract class IdempotentClientFilter extends ClientFilter {
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sun.jersey.api.client.filter.ClientFilter#handle(com.sun.jersey.api
     * .client.ClientRequest)
     */
    @Override
    public ClientResponse handle(ClientRequest cr) {
        String key = getKey();

        if (cr.getProperties().containsKey(key)) {
            return this.getNext().handle(cr);
        }
        cr.getProperties().put(key, this);
        return doHandle(cr);
    }

    /**
     * Implemented by derived classes to provide the actual implementation for
     * filtering.
     * 
     * @param cr
     *            The ClientRequest being processed
     * @return The returned ClientResponse
     */
    protected abstract ClientResponse doHandle(ClientRequest cr);

    /**
     * Get the key value used to detect multiple runs. By default, defaults to
     * the class name for the filter.
     * 
     * @return Key name as a string
     */
    protected String getKey() {
        return this.getClass().getName();
    }
}