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
package com.microsoft.windowsazure.services.core.storage;

/**
 * Represents an event that is fired when a response is received.
 */
public final class ResponseReceivedEvent {

    /**
     * Represents a connection object. Currently only <code>java.net.HttpURLConnection</code> is supported as a
     * connection object.
     */
    private Object connectionObject;

    /**
     * Represents a context for the current operation. This object is used to track requests to the storage service, and
     * to provide additional runtime information about the operation.
     */
    private OperationContext opContext;

    /**
     * Creates an instance of the <code>ResponseReceivedEvent</code> class.
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param connectionObject
     *            Represents a connection object. Currently only <code>java.net.HttpURLConnection</code> is supported as
     *            a connection object.
     */
    public ResponseReceivedEvent(final OperationContext opContext, final Object connectionObject) {
        this.opContext = opContext;
        this.connectionObject = connectionObject;
    }

    /**
     * @return the connectionObject
     */
    public Object getConnectionObject() {
        return this.connectionObject;
    }

    /**
     * @return the opContext
     */
    public OperationContext getOpContext() {
        return this.opContext;
    }
}
