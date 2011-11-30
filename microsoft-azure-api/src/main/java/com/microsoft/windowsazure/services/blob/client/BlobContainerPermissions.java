package com.microsoft.windowsazure.services.blob.client;

import java.util.HashMap;

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
 * href='http://go.microsoft.com/fwlink/?LinkID=224643&clcid=0x409'>Managing Access to Containers and Blobs</a>.
 */
public final class BlobContainerPermissions {

    /**
     * Represents the public access setting for the container.
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
     * href='http://go.microsoft.com/fwlink/?LinkID=224644&clcid=0x409'>Setting Access Control for Containers</a>.
     */
    private BlobContainerPublicAccessType publicAccess;

    /**
     * Gets the set of shared access policies for the container.
     */
    private HashMap<String, SharedAccessPolicy> sharedAccessPolicies;

    /**
     * Creates an instance of the <code>BlobContainerPermissions</code> class.
     */
    public BlobContainerPermissions() {
        this.setPublicAccess(BlobContainerPublicAccessType.OFF);
        this.sharedAccessPolicies = new HashMap<String, SharedAccessPolicy>();
    }

    /**
     * @return the publicAccess
     */
    public BlobContainerPublicAccessType getPublicAccess() {
        return this.publicAccess;
    }

    /**
     * Returns the set of shared access policies for the container.
     * 
     * @return A <code>HashMap</code> object of {@link SharedAccessPolicy} objects that represent the set of shared
     *         access policies for the container.
     */
    public HashMap<String, SharedAccessPolicy> getSharedAccessPolicies() {
        return this.sharedAccessPolicies;
    }

    /**
     * @param publicAccess
     *            the publicAccess to set
     */
    public void setPublicAccess(final BlobContainerPublicAccessType publicAccess) {
        this.publicAccess = publicAccess;
    }

    /**
     * @param sharedAccessPolicies
     *            the sharedAccessPolicies to set
     */
    public void setSharedAccessPolicies(final HashMap<String, SharedAccessPolicy> sharedAccessPolicies) {
        this.sharedAccessPolicies = sharedAccessPolicies;
    }
}
