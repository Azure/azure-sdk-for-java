package com.azure.storage.file.datalake.models;

import com.azure.storage.file.datalake.implementation.models.PathGetPropertiesHeaders;

public class PathAccessControl {

    private String acl;
    private String group;
    private String owner;
    private String permissions;

    public PathAccessControl() {

    }

    public PathAccessControl(PathGetPropertiesHeaders deserializedHeaders) {
        this.acl = deserializedHeaders.getACL();
        this.group = deserializedHeaders.getGroup();
        this.owner = deserializedHeaders.getOwner();
        this.permissions = deserializedHeaders.getPermissions();
    }

    public String getAcl() {
        return acl;
    }

    public PathAccessControl setAcl(String acl) {
        this.acl = acl;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public PathAccessControl setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public PathAccessControl setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getPermissions() {
        return permissions;
    }

    public PathAccessControl setPermissions(String permissions) {
        this.permissions = permissions;
        return this;
    }
}
