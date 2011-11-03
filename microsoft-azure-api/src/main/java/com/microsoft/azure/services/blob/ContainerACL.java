package com.microsoft.azure.services.blob;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class ContainerACL {
    private String etag;
    private Date lastModified;
    private String publicAccess;
    private List<SignedIdentifier> signedIdentifiers = new ArrayList<SignedIdentifier>();

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(String publicAccess) {
        this.publicAccess = publicAccess;
    }

    public List<SignedIdentifier> getSignedIdentifiers() {
        return signedIdentifiers;
    }

    public void setSignedIdentifiers(List<SignedIdentifier> signedIdentifiers) {
        this.signedIdentifiers = signedIdentifiers;
    }

    public void AddSignedIdentifier(String id, String start, String expiry, String permission) {
        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setStart(start);
        accessPolicy.setExpiry(expiry);
        accessPolicy.setPermission(permission);

        SignedIdentifier signedIdentifier = new SignedIdentifier();
        signedIdentifier.setId(id);
        signedIdentifier.setAccessPolicy(accessPolicy);

        this.getSignedIdentifiers().add(signedIdentifier);
    }

    @XmlRootElement(name = "SignedIdentifiers")
    public static class SignedIdentifiers {
        private List<SignedIdentifier> signedIdentifiers = new ArrayList<SignedIdentifier>();

        @XmlElement(name = "SignedIdentifier")
        public List<SignedIdentifier> getSignedIdentifiers() {
            return signedIdentifiers;
        }

        public void setSignedIdentifiers(List<SignedIdentifier> signedIdentifiers) {
            this.signedIdentifiers = signedIdentifiers;
        }
    }

    public static class SignedIdentifier {
        private String id;
        private AccessPolicy accessPolicy;

        @XmlElement(name = "Id")
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @XmlElement(name = "AccessPolicy")
        public AccessPolicy getAccessPolicy() {
            return accessPolicy;
        }

        public void setAccessPolicy(AccessPolicy accessPolicy) {
            this.accessPolicy = accessPolicy;
        }
    }

    public static class AccessPolicy {
        private String start;
        private String expiry;
        private String permission;

        @XmlElement(name = "Start")
        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        @XmlElement(name = "Expiry")
        public String getExpiry() {
            return expiry;
        }

        public void setExpiry(String expiry) {
            this.expiry = expiry;
        }

        @XmlElement(name = "Permission")
        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }
    }
}
