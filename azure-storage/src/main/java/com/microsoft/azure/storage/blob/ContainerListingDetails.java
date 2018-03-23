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

import com.microsoft.azure.storage.models.ListContainersIncludeType;

/**
 * Details indicating what additional information the service should return with each container.
 */
public final class ContainerListingDetails {

    /**
     * An object indicating that no extra details should be returned.
     */
    public static final ContainerListingDetails NONE = new ContainerListingDetails(false);

    private final boolean metadata;

    /**
     * A {@link ContainerListingDetails} object.
     *
     * @param metadata
     *      A {@code boolean} indicating if metadata should be returned.
     */
    ContainerListingDetails(boolean metadata) {
        this.metadata = metadata;
    }

    /**
     * @return
     *      A {@code boolean} indicating if metadata should be returned.
     */
    public boolean getMetadata() {
        return this.metadata;
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
