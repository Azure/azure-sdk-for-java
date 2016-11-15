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

import java.util.Locale;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Specifies the level of public access that is allowed on the container.
 * <p>
 * The public access setting indicates whether the container and its blobs can be read via an anonymous request.
 * <p>
 * The {@link BlobContainerPublicAccessType} enumeration provides three levels of anonymous read access:
 * <ul>
 * <li>{@link BlobContainerPublicAccessType#OFF}, which prevents anonymous access.</li>
 * <li>{@link BlobContainerPublicAccessType#BLOB}, which permits anonymous read access to blob resources, but not to
 * container metadata or to the list of blobs in the container.</li>
 * <li>{@link BlobContainerPublicAccessType#CONTAINER}, which permits anonymous read access to blob resources, container
 * metadata, and the list of blobs in the container.</li>
 * </ul>
 * For more information on managing anonymous access to Blob service resources, see <a
 * href='http://go.microsoft.com/fwlink/?LinkID=224644&clcid=0x409'>Setting Access Control for Containers</a>.
 */
public enum BlobContainerPublicAccessType {
    /**
     * Specifies blob-level public access. Clients can read the content and metadata of blobs within this container, but
     * cannot read container metadata or list the blobs within the container.
     */
    BLOB,

    /**
     * Specifies container-level public access. Clients can read blob content and metadata and container metadata, and
     * can list the blobs within the container.
     **/
    CONTAINER,

    /**
     * Specifies no public access. Only the account owner can access resources in this container.
     */
    OFF,

    /**
     * Specifies that the public access type is unknown.
     */
    UNKNOWN;

    /**
     * Parses a public access level from the specified string.
     *
     * @param typeString
     *            A <code>String</code> which contains the public access level
     *            to parse.
     *
     * @return A <code>BlobContainerPublicAccessType</code> value that
     *         represents the public access level for the container.
     */
    protected static BlobContainerPublicAccessType parse(final String typeString) {
        if (Utility.isNullOrEmpty(typeString)) {
            return OFF;
        }
        else if (SR.BLOB.equals(typeString.toLowerCase(Locale.US))) {
            return BLOB;
        }
        else if (SR.CONTAINER.equals(typeString.toLowerCase(Locale.US))) {
            return CONTAINER;
        }
        else {
            return UNKNOWN;
        }
    }
}
