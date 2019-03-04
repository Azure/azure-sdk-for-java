/*
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

import com.microsoft.azure.storage.blob.models.ListContainersIncludeType;

/**
 * This type allows users to specify additional information the service should return with each container when listing
 * containers in an account (via a {@link ServiceURL} object). This type is immutable to ensure thread-safety of
 * requests, so changing the details for a different listing operation requires construction of a new object. Null may
 * be passed if none of the options are desirable.
 */
public final class ContainerListingDetails {

    /**
     * An object indicating that no extra details should be returned.
     */
    public static final ContainerListingDetails NONE = new ContainerListingDetails();

    private boolean metadata;

    public ContainerListingDetails() {

    }

    /**
     * Whether metadata should be returned.
     */
    public boolean metadata() {
        return this.metadata;
    }

    /**
     * Whether metadata should be returned.
     */
    public ContainerListingDetails withMetadata(boolean metadata) {
        this.metadata = metadata;
        return this;
    }

    /*
     This is used internally to convert the details structure into the appropriate type to pass to the protocol layer.
     It is intended to mirror the BlobListingDetails.toList() method, but is slightly different since there is only one
     possible value here currently. The customer should never have need for this.
     */
    ListContainersIncludeType toIncludeType() {
        if (this.metadata) {
            return ListContainersIncludeType.METADATA;
        }
        return null;
    }
}
