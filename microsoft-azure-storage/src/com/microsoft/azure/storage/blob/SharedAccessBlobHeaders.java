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
package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.SharedAccessHeaders;

/**
 * Represents the optional headers that can be returned with blobs accessed using SAS.
 */
public final class SharedAccessBlobHeaders extends SharedAccessHeaders {
    /**
     * Initializes a new instance of the {@link SharedAccessBlobHeaders} class.
     */
    public SharedAccessBlobHeaders() {
    }

    /**
     * Initializes a new instance of the {@link SharedAccessBlobHeaders} class based on an existing instance.
     * 
     * @param other
     *            A {@link SharedAccessHeaders} object which specifies the set of properties to clone.
     */
    public SharedAccessBlobHeaders(SharedAccessHeaders other) {
        super(other);
    }
}