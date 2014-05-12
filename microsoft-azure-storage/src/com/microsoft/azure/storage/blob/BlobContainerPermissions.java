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

import com.microsoft.azure.storage.Permissions;

/**
 * Represents the permissions for a container.
 * <p>
 * The container's permissions encompass two types of access settings for the container:
 * <ul>
 * <li>The container's public access setting, represented by the {@link #publicAccess} property. The public access
 * setting indicates whether the container and its blobs can be read via an anonymous request.</li>
 * <li>The container's access policies, represented by the {@link #getSharedAccessPolicies} method. This setting
 * references a collection of shared access policies for the container. A shared access policy may be used to control
 * the start time, expiry time, and permissions for one or more shared access signatures. A shared access signature
 * provides delegated access to the container's resources.</li>
 * </ul>
 * For more information on managing container permissions, see <a
 * href='http://go.microsoft.com/fwlink/?LinkID=224643'>Managing Access to Containers and Blobs</a>.
 */
public final class BlobContainerPermissions extends Permissions<SharedAccessBlobPolicy> {

    /**
     * Represents the public access setting for the container.
     */
    private BlobContainerPublicAccessType publicAccess;

    /**
     * Creates an instance of the <code>BlobContainerPermissions</code> class.
     */
    public BlobContainerPermissions() {
        super();
        this.setPublicAccess(BlobContainerPublicAccessType.OFF);
    }

    /**
     * Gets the public access setting for the container.
     * <p>
     * The public access setting indicates whether the container and its blobs can be read via an anonymous request.
     * <p>
     * The {@link BlobContainerPublicAccessType} enumeration provides three levels of anonymous read access:
     * <ul>
     * <li>{@link BlobContainerPublicAccessType#OFF}, which prevents anonymous access.</li>
     * <li>{@link BlobContainerPublicAccessType#BLOB}, which permits anonymous read access to blob resources, but not to
     * container metadata or to the list of blobs in the container.</li>
     * <li>{@link BlobContainerPublicAccessType#CONTAINER}, which permits anonymous read access to blob resources,
     * container metadata, and the list of blobs in the container.</li>
     * </ul>
     * For more information on managing anonymous access to Blob service resources, see <a
     * href='http://go.microsoft.com/fwlink/?LinkID=224644'>Setting Access Control for Containers</a>.
     */
    public BlobContainerPublicAccessType getPublicAccess() {
        return this.publicAccess;
    }

    /**
     * Sets the public access setting for the container.
     * <p>
     * The public access setting indicates whether the container and its blobs can be read via an anonymous request.
     * <p>
     * The {@link BlobContainerPublicAccessType} enumeration provides three levels of anonymous read access:
     * <ul>
     * <li>{@link BlobContainerPublicAccessType#OFF}, which prevents anonymous access.</li>
     * <li>{@link BlobContainerPublicAccessType#BLOB}, which permits anonymous read access to blob resources, but not to
     * container metadata or to the list of blobs in the container.</li>
     * <li>{@link BlobContainerPublicAccessType#CONTAINER}, which permits anonymous read access to blob resources,
     * container metadata, and the list of blobs in the container.</li>
     * </ul>
     * For more information on managing anonymous access to Blob service resources, see <a
     * href='http://go.microsoft.com/fwlink/?LinkID=224644'>Setting Access Control for Containers</a>.
     */
    public void setPublicAccess(final BlobContainerPublicAccessType publicAccess) {
        this.publicAccess = publicAccess;
    }
}
