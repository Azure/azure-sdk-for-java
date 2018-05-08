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
package com.microsoft.windowsazure.services.blob.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.services.blob.implementation.ContainerACLDateAdapter;

/**
 * Represents the public access properties and the container-level access
 * policies of a container in the Blob storage service. This is returned by
 * calls to implementations of {@link com.microsoft.windowsazure.services.blob.BlobContract#getContainerACL(String)} and
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getContainerACL(String, BlobServiceOptions)}, and passed
 * as a parameter to calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerACL(String, ContainerACL)} and
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerACL(String, ContainerACL, BlobServiceOptions)}
 * .
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179469.aspx">Get
 * Container ACL</a> and the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179391.aspx">Set
 * Container ACL</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operations.
 */
public class ContainerACL {
    private String etag;
    private Date lastModified;
    private PublicAccessType publicAccess;
    private List<SignedIdentifier> signedIdentifiers = new ArrayList<SignedIdentifier>();

    /**
     * Gets the <strong>Etag</strong> value associated with this
     * {@link ContainerACL} instance. This is the value returned for a container
     * by a Blob service REST API Get Container ACL operation, or the value to
     * set on a container with a Set Container ACL operation.
     * 
     * @return A {@link String} containing the <strong>Etag</strong> value
     *         associated with this {@link ContainerACL} instance.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Sets the <strong>Etag</strong> value to associate with this
     * {@link ContainerACL} instance.
     * <p>
     * This value is only set on a container when this {@link ContainerACL}
     * instance is passed as a parameter to a call to an implementation of
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerACL(String, ContainerACL)} or
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerACL(String, ContainerACL, BlobServiceOptions)}.
     * 
     * @param etag
     *            A {@link String} containing the <strong>Etag</strong> value to
     *            associate with this {@link ContainerACL} instance.
     * 
     * @return A reference to this {@link ContainerACL} instance.
     */
    public ContainerACL setEtag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * Gets the last modified time associated with this {@link ContainerACL}
     * instance. This is the value returned for a container by a Blob service
     * REST API Get Container ACL operation, or the value to set on a container
     * with a Set Container ACL operation.
     * 
     * @return A {@link Date} containing the last modified time associated with
     *         this {@link ContainerACL} instance.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the last modified time to associate with this {@link ContainerACL}
     * instance.
     * <p>
     * This value is only set on a container when this {@link ContainerACL}
     * instance is passed as a parameter to a call to an implementation of
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerACL(String, ContainerACL)} or
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerACL(String, ContainerACL, BlobServiceOptions)}.
     * 
     * @param lastModified
     *            A {@link java.util.Date} containing the last modified time to
     *            associate with this {@link ContainerACL} instance.
     * @return A reference to this {@link ContainerACL} instance.
     */
    public ContainerACL setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Gets the public access level associated with this {@link ContainerACL}
     * instance. This is the value returned for a container by a Blob service
     * REST API Get Container ACL operation, or the value to set on a container
     * with a Set Container ACL operation.
     * 
     * @return A {@link PublicAccessType} value representing the public access
     *         level associated with this {@link ContainerACL} instance.
     */
    public PublicAccessType getPublicAccess() {
        return publicAccess;
    }

    /**
     * Sets the public access level to associate with this {@link ContainerACL}
     * instance.
     * <p>
     * This value is only set on a container when this {@link ContainerACL}
     * instance is passed as a parameter to a call to an implementation of
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerACL(String, ContainerACL)} or
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerACL(String, ContainerACL, BlobServiceOptions)}.
     * 
     * @param publicAccess
     *            A {@link PublicAccessType} value representing the public
     *            access level to associate with this {@link ContainerACL}
     *            instance.
     * @return A reference to this {@link ContainerACL} instance.
     */
    public ContainerACL setPublicAccess(PublicAccessType publicAccess) {
        this.publicAccess = publicAccess;
        return this;
    }

    /**
     * Gets the list of container-level access policies associated with this
     * {@link ContainerACL} instance. This is the value returned for a container
     * by a Blob service REST API Get Container ACL operation, or the value to
     * set on a container with a Set Container ACL operation.
     * 
     * @return A {@link List} of {@link SignedIdentifier} instances containing
     *         up to five container-level access policies associated with this
     *         {@link ContainerACL} instance.
     */
    public List<SignedIdentifier> getSignedIdentifiers() {
        return signedIdentifiers;
    }

    /**
     * Sets the list of container-level access policies to associate with this
     * {@link ContainerACL} instance.
     * <p>
     * This value is only set on a container when this {@link ContainerACL}
     * instance is passed as a parameter to a call to an implementation of
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerACL(String, ContainerACL)} or
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerACL(String, ContainerACL, BlobServiceOptions)}.
     * 
     * @param signedIdentifiers
     *            A {@link List} of {@link SignedIdentifier} instances
     *            containing up to five container-level access policies to
     *            associate with this {@link ContainerACL} instance.
     * @return A reference to this {@link ContainerACL} instance.
     */
    public ContainerACL setSignedIdentifiers(
            List<SignedIdentifier> signedIdentifiers) {
        this.signedIdentifiers = signedIdentifiers;
        return this;
    }

    /**
     * Adds a container-level access policy to the list associated with this
     * {@link ContainerACL} instance. A container may have up to five
     * container-level access policies.
     * <p>
     * Use the <em>id</em> parameter to specify a name for the access policy.
     * The name may be up to 64 characters in length and must be unique within
     * the container.
     * <p>
     * Use the <em>start</em> parameter to specify the start time for valid
     * access to a resource using the access policy. If this value is
     * <code>null</code>, the start time for any resource request using the
     * access policy is assumed to be the time when the Blob service receives
     * the request.
     * <p>
     * Use the <em>expiry</em> parameter to specify the expiration time for
     * valid access to a resource using the access policy. If this value is
     * <code>null</code>, the expiry time must be included in the Shared Access
     * Signature for any resource request using the access policy.
     * <p>
     * Use the <em>permission</em> parameter to specify the operations that can
     * be performed on a blob that is accessed with the access policy. Supported
     * permissions include read (r), write (w), delete (d), and list (l).
     * Permissions may be grouped so as to allow multiple operations to be
     * performed with the access policy. For example, to grant all permissions
     * to a resource, specify "rwdl" for the parameter. To grant only read/write
     * permissions, specify "rw" for the parameter.
     * <p>
     * This value is only set on a container when this {@link ContainerACL}
     * instance is passed as a parameter to a call to an implementation of
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerACL(String, ContainerACL)} or
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#setContainerACL(String, ContainerACL, BlobServiceOptions)}.
     * 
     * @param id
     *            A {@link String} containing the name for the access policy.
     * @param start
     *            A {@link Date} representing the start time for the access
     *            policy. If this value is <code>null</code>, any Shared Access
     *            Signature that refers to this policy may specify the start
     *            time.
     * @param expiry
     *            A {@link Date} representing the expiration time for the access
     *            policy. If this value is <code>null</code>, any Shared Access
     *            Signature that refers to this policy must specify the expiry
     *            value. Resource access using a Shared Access Signature that
     *            refers to this policy after this time is not valid.
     * @param permission
     *            A {@link String} containing the permissions specified for the
     *            access policy.
     */
    public void addSignedIdentifier(String id, Date start, Date expiry,
            String permission) {
        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setStart(start);
        accessPolicy.setExpiry(expiry);
        accessPolicy.setPermission(permission);

        SignedIdentifier signedIdentifier = new SignedIdentifier();
        signedIdentifier.setId(id);
        signedIdentifier.setAccessPolicy(accessPolicy);

        this.getSignedIdentifiers().add(signedIdentifier);
    }

    /**
     * A static inner class representing a collection of container-level access
     * policies. A container may have up to five container-level access
     * policies, which may be associated with any number of Shared Access
     * Signatures.
     */
    @XmlRootElement(name = "SignedIdentifiers")
    public static class SignedIdentifiers {
        private List<SignedIdentifier> signedIdentifiers = new ArrayList<SignedIdentifier>();

        /**
         * Gets the list of container-level access policies associated with this
         * {@link SignedIdentifiers} instance.
         * 
         * @return A {@link List} of {@link SignedIdentifier} instances
         *         containing container-level access policies.
         */
        @XmlElement(name = "SignedIdentifier")
        public List<SignedIdentifier> getSignedIdentifiers() {
            return signedIdentifiers;
        }

        /**
         * Sets the list of container-level access policies associated with this
         * {@link SignedIdentifiers} instance.
         * 
         * @param signedIdentifiers
         *            A {@link List} of {@link SignedIdentifier} instances
         *            containing container-level access policies.
         */
        public void setSignedIdentifiers(
                List<SignedIdentifier> signedIdentifiers) {
            this.signedIdentifiers = signedIdentifiers;
        }
    }

    /**
     * A static inner class representing a container-level access policy with a
     * unique name.
     */
    public static class SignedIdentifier {
        private String id;
        private AccessPolicy accessPolicy;

        /**
         * Gets the name of the container-level access policy. The name may be
         * up to 64 characters in length and must be unique within the
         * container.
         * 
         * @return A {@link String} containing the name for the access policy.
         */
        @XmlElement(name = "Id")
        public String getId() {
            return id;
        }

        /**
         * Sets the name of the container-level access policy. The name may be
         * up to 64 characters in length and must be unique within the
         * container.
         * 
         * @param id
         *            A {@link String} containing the name for the access
         *            policy.
         * @return A reference to this {@link SignedIdentifier} instance.
         */
        public SignedIdentifier setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Gets an {@link AccessPolicy} reference containing the start time,
         * expiration time, and permissions associated with the container-level
         * access policy.
         * 
         * @return An {@link AccessPolicy} reference containing the start time,
         *         expiration time, and permissions associated with the access
         *         policy.
         */
        @XmlElement(name = "AccessPolicy")
        public AccessPolicy getAccessPolicy() {
            return accessPolicy;
        }

        /**
         * Sets an {@link AccessPolicy} reference containing the start time,
         * expiration time, and permissions to associate with the
         * container-level access policy.
         * 
         * @param accessPolicy
         *            An {@link AccessPolicy} reference containing the start
         *            time, expiration time, and permissions to associate with
         *            the access policy.
         * @return A reference to this {@link SignedIdentifier} instance.
         */
        public SignedIdentifier setAccessPolicy(AccessPolicy accessPolicy) {
            this.accessPolicy = accessPolicy;
            return this;
        }
    }

    /**
     * An inner class representing the start time, expiration time, and
     * permissions associated with an access policy.
     */
    public static class AccessPolicy {
        private Date start;
        private Date expiry;
        private String permission;

        /**
         * Gets the start time for valid access to a resource using the access
         * policy. If this value is <code>null</code>, the start time for any
         * resource request using the access policy is assumed to be the time
         * when the Blob service receives the request.
         * 
         * @return A {@link Date} representing the start time for the access
         *         policy, or <code>null</code> if none is specified.
         */
        @XmlElement(name = "Start")
        @XmlJavaTypeAdapter(ContainerACLDateAdapter.class)
        public Date getStart() {
            return start;
        }

        /**
         * Sets the start time for valid access to a resource using the access
         * policy. If this value is <code>null</code>, the start time for any
         * resource request using the access policy is assumed to be the time
         * when the Blob service receives the request.
         * 
         * @param start
         *            A {@link Date} representing the start time for the access
         *            policy, or <code>null</code> to leave the time
         *            unspecified.
         * @return A reference to this {@link AccessPolicy} instance.
         */
        public AccessPolicy setStart(Date start) {
            this.start = start;
            return this;
        }

        /**
         * Gets the expiration time for valid access to a resource using the
         * access policy. If this value is <code>null</code>, any Shared Access
         * Signature that refers to this access policy must specify the expiry
         * value.
         * 
         * @return A {@link Date} representing the expiration time for the
         *         access policy, or <code>null</code> if none is specified.
         */
        @XmlElement(name = "Expiry")
        @XmlJavaTypeAdapter(ContainerACLDateAdapter.class)
        public Date getExpiry() {
            return expiry;
        }

        /**
         * Sets the expiration time for valid access to a resource using the
         * access policy. If this value is <code>null</code>, any Shared Access
         * Signature that refers to this access policy must specify the expiry
         * value.
         * 
         * @param expiry
         *            A {@link Date} representing the expiration time for the
         *            access policy, or <code>null</code> to leave the time
         *            unspecified.
         * @return A reference to this {@link AccessPolicy} instance.
         */
        public AccessPolicy setExpiry(Date expiry) {
            this.expiry = expiry;
            return this;
        }

        /**
         * Gets the permissions for operations on resources specified by the
         * access policy. Supported permissions include read (r), write (w),
         * delete (d), and list (l). Permissions may be grouped so as to allow
         * multiple operations to be performed with the access policy. For
         * example, if all permissions to a resource are granted, the method
         * returns "rwdl" as the result. If only read/write permissions are
         * granted, the method returns "rw" as the result.
         * 
         * @return A {@link String} containing the permissions specified for the
         *         access policy.
         */
        @XmlElement(name = "Permission")
        public String getPermission() {
            return permission;
        }

        /**
         * Sets the permissions for operations on resources specified by the
         * access policy. Supported permissions include read (r), write (w),
         * delete (d), and list (l). Permissions may be grouped so as to allow
         * multiple operations to be performed with the access policy. For
         * example, to grant all permissions to a resource, specify "rwdl" for
         * the parameter. To grant only read/write permissions, specify "rw" for
         * the parameter.
         * 
         * @param permission
         *            A {@link String} containing the permissions specified for
         *            the access policy.
         * @return A reference to this {@link AccessPolicy} instance.
         */
        public AccessPolicy setPermission(String permission) {
            this.permission = permission;
            return this;
        }
    }

    /**
     * An enumeration type for the public access levels that can be set on a
     * blob container.
     */
    public static enum PublicAccessType {
        /**
         * Access to this container and its blobs is restricted to calls made
         * with the storage account private key.
         */
        NONE,
        /**
         * Anonymous public read-only access is allowed for individual blobs
         * within the container, but it is not possible to enumerate the blobs
         * within the container or to enumerate the containers in the storage
         * account.
         */
        BLOBS_ONLY,
        /**
         * Anonymous public read-only access is allowed for individual blobs
         * within the container, and the blobs within the container can be
         * enumerated, but it is not possible to enumerate the containers in the
         * storage account.
         */
        CONTAINER_AND_BLOBS,
    }
}
