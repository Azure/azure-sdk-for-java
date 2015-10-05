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
package com.microsoft.azure.storage;

import java.net.URI;

/**
 * Represents credentials for anonymous access.
 */
public final class StorageCredentialsAnonymous extends StorageCredentials {
    /**
     * Stores the singleton instance of this class.
     */
    public static final StorageCredentials ANONYMOUS = new StorageCredentialsAnonymous();

    /**
     * Returns the singleton instance of the <code>StorageCredentials</code> class.
     *
     * @return the singleton instance of this class
     */
    protected static StorageCredentials getInstance() {
        return StorageCredentialsAnonymous.ANONYMOUS;
    }

    /**
     * Enforces the singleton pattern via a private constructor.
     */
    private StorageCredentialsAnonymous() {
        // Empty Default Ctor 
    }

    /**
     * Returns a <code>String</code> object that represents this instance.
     *
     * @param exportSecrets
     *            <code>true</code> to include sensitive data in the string; otherwise, <code>false</code>
     * @return a string representation of the credentials, optionally including sensitive data.
     */
    @Override
    public String toString(final boolean exportSecrets) {
        return Constants.EMPTY_STRING;
    }

    @Override
    public URI transformUri(URI resourceUri, OperationContext opContext) {
        return resourceUri;
    }

    @Override
    public StorageUri transformUri(StorageUri resourceUri, OperationContext opContext) {
        return resourceUri;
    }
}
